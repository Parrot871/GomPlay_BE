package com.example.gomplay.domain.auth.service;

import com.example.gomplay.domain.auth.dto.*;
import com.example.gomplay.domain.auth.entity.AuthUser;
import com.example.gomplay.domain.auth.entity.EmailVerification;
import com.example.gomplay.domain.auth.entity.RefreshToken;
import com.example.gomplay.domain.auth.repository.AuthUserRepository;
import com.example.gomplay.domain.auth.repository.EmailVerificationRepository;
import com.example.gomplay.domain.auth.repository.RefreshTokenRepository;
import com.example.gomplay.domain.user.entity.UserProfile;
import com.example.gomplay.domain.user.repository.UserProfileRepository;
import com.example.gomplay.global.util.JwtUtil;
import com.example.gomplay.global.mail.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthUserRepository authUserRepository;
    private final UserProfileRepository userProfileRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;

    // 회원가입 + 인증코드 발송
    @Transactional
    public SignupResponse signup(SignupRequest request) {

        if (!request.getSchoolEmail().endsWith("@dankook.ac.kr")) {
            throw new IllegalArgumentException("단국대학교 이메일만 가입 가능합니다.");
        }

        if (authUserRepository.existsBySchoolEmail(request.getSchoolEmail())) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        AuthUser authUser = AuthUser.builder()
                .schoolEmail(request.getSchoolEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .isVerified(false)
                .isActive(true)
                .build();
        authUserRepository.save(authUser);

        String token = generateVerificationCode();

        EmailVerification verification = EmailVerification.builder()
                .authUser(authUser)
                .token(token)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .name(request.getName())
                .studentId(request.getStudentId())
                .department(request.getDepartment())
                .college(request.getCollege())
                .build();
        emailVerificationRepository.save(verification);

        mailService.sendVerificationEmail(request.getSchoolEmail(), token);

        return SignupResponse.builder()
                .email(request.getSchoolEmail())
                .build();
    }

    // 인증코드 재전송
    @Transactional
    public void resendVerificationEmail(String schoolEmail) {

        AuthUser authUser = authUserRepository.findBySchoolEmail(schoolEmail)
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));

        if (authUser.isVerified()) {
            throw new IllegalArgumentException("이미 인증된 계정입니다.");
        }

        EmailVerification verification = emailVerificationRepository.findByAuthUser(authUser)
                .orElseThrow(() -> new IllegalArgumentException("인증 정보를 찾을 수 없습니다."));

        String newToken = generateVerificationCode();
        verification.updateToken(newToken, LocalDateTime.now().plusMinutes(5));

        mailService.sendVerificationEmail(schoolEmail, newToken);
    }

    // 이메일 인증코드 확인
    @Transactional
    public void verifyEmail(VerifyEmailRequest request) {

        EmailVerification verification = emailVerificationRepository
                .findByToken(request.getToken())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 인증 코드입니다."));

        if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("만료된 인증 코드입니다.");
        }

        AuthUser authUser = verification.getAuthUser();
        if (authUser.isVerified()) {
            return;
        }

        authUser.verify();
        authUserRepository.save(authUser);

        emailVerificationRepository.deleteByAuthUser_Id(authUser.getId());

        UserProfile userProfile = UserProfile.builder()
                .authUser(authUser)
                .name(verification.getName())
                .studentId(verification.getStudentId())
                .department(verification.getDepartment())
                .college(verification.getCollege())
                .build();
        userProfileRepository.save(userProfile);
    }

    // 로그인
    @Transactional
    public LoginResponse login(LoginRequest request) {

        AuthUser authUser = authUserRepository
                .findBySchoolEmail(request.getSchoolEmail())
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!authUser.isActive()) {
            throw new IllegalArgumentException("탈퇴한 계정입니다.");
        }

        if (!authUser.isVerified()) {
            throw new IllegalArgumentException("이메일 인증이 필요합니다.");
        }

        if (!passwordEncoder.matches(request.getPassword(), authUser.getPasswordHash())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        String accessToken = jwtUtil.generateAccessToken(authUser.getId());
        String refreshToken = jwtUtil.generateRefreshToken(authUser.getId());

        refreshTokenRepository.deleteByAuthUser_Id(authUser.getId());
        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .authUser(authUser)
                .token(refreshToken)
                .expiresAt(LocalDateTime.now().plusWeeks(2))
                .build();
        refreshTokenRepository.save(refreshTokenEntity);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .userId(authUser.getId())
                .isMatching(false)
                .build();
    }

    // 토큰 재발급
    @Transactional
    public LoginResponse reissueToken(TokenReissueRequest request) {

        if (!jwtUtil.validateToken(request.getRefreshToken())) {
            throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
        }

        RefreshToken refreshToken = refreshTokenRepository
                .findByToken(request.getRefreshToken())
                .orElseThrow(() -> new IllegalArgumentException("리프레시 토큰이 존재하지 않습니다."));

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("만료된 리프레시 토큰입니다.");
        }

        AuthUser authUser = refreshToken.getAuthUser();
        String newAccessToken = jwtUtil.generateAccessToken(authUser.getId());
        String newRefreshToken = jwtUtil.generateRefreshToken(authUser.getId());

        refreshTokenRepository.deleteByAuthUser_Id(authUser.getId());
        RefreshToken newRefreshTokenEntity = RefreshToken.builder()
                .authUser(authUser)
                .token(newRefreshToken)
                .expiresAt(LocalDateTime.now().plusWeeks(2))
                .build();
        refreshTokenRepository.save(newRefreshTokenEntity);

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .userId(authUser.getId())
                .isMatching(false)
                .build();
    }

    // 로그아웃
    @Transactional
    public void logout(Long userId) {
        refreshTokenRepository.deleteByAuthUser_Id(userId);
    }

    // 6자리 인증코드 생성
    private String generateVerificationCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }
}