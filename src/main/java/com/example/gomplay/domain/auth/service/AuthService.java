package com.example.gomplay.domain.auth.service;

import com.example.gomplay.domain.auth.dto.*;
import com.example.gomplay.domain.auth.entity.AuthUser;
import com.example.gomplay.domain.auth.entity.EmailVerification;
import com.example.gomplay.domain.auth.entity.RefreshToken;
import com.example.gomplay.domain.auth.repository.AuthUserRepository;
import com.example.gomplay.domain.auth.repository.EmailVerificationRepository;
import com.example.gomplay.domain.auth.repository.RefreshTokenRepository;
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
    private final EmailVerificationRepository emailVerificationRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;

    // 회원가입 + 인증코드 발송
    @Transactional
    public SignupResponse signup(SignupRequest request) {

        // 1. 이메일 도메인 검증
        if (!request.getSchoolEmail().endsWith("@dankook.ac.kr")) {
            throw new IllegalArgumentException("단국대학교 이메일만 가입 가능합니다.");
        }

        // 2. 이메일 중복 검사
        if (authUserRepository.existsBySchoolEmail(request.getSchoolEmail())) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        // 3. auth_user INSERT
        AuthUser authUser = AuthUser.builder()
                .schoolEmail(request.getSchoolEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .isVerified(false)
                .isActive(true)
                .build();
        authUserRepository.save(authUser);

        // 4. 6자리 인증코드 생성
        String token = generateVerificationCode();

        // 5. email_verification INSERT (5분 만료)
        EmailVerification verification = EmailVerification.builder()
                .authUser(authUser)
                .token(token)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .build();
        emailVerificationRepository.save(verification);

        // 6. 이메일 발송
        mailService.sendVerificationEmail(request.getSchoolEmail(), token);

        return SignupResponse.builder()
                .message("인증 코드가 발송되었습니다.")
                .email(request.getSchoolEmail())
                .build();
    }

    // 이메일 인증코드 확인
    @Transactional
    public void verifyEmail(VerifyEmailRequest request) {

        // 1. 토큰으로 인증 정보 찾기
        EmailVerification verification = emailVerificationRepository
                .findByToken(request.getToken())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 인증 코드입니다."));

        // 2. 만료 시간 확인
        if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("만료된 인증 코드입니다.");
        }

        // 3. auth_user is_verified 업데이트
        AuthUser authUser = verification.getAuthUser();
        authUser.verify();  // 아래에서 메서드 추가
        authUserRepository.save(authUser);

        // 4. email_verification 삭제
        emailVerificationRepository.deleteByAuthUser_Id(authUser.getId());
    }

    // 로그인
    @Transactional
    public LoginResponse login(LoginRequest request) {

        // 1. 이메일로 유저 찾기
        AuthUser authUser = authUserRepository
                .findBySchoolEmail(request.getSchoolEmail())
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다."));

        // 2. 계정 활성화 확인
        if (!authUser.isActive()) {
            throw new IllegalArgumentException("탈퇴한 계정입니다.");
        }

        // 3. 이메일 인증 확인
        if (!authUser.isVerified()) {
            throw new IllegalArgumentException("이메일 인증이 필요합니다.");
        }

        // 4. 비밀번호 확인
        if (!passwordEncoder.matches(request.getPassword(), authUser.getPasswordHash())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        // 5. JWT 발급
        String accessToken = jwtUtil.generateAccessToken(authUser.getId());
        String refreshToken = jwtUtil.generateRefreshToken(authUser.getId());

        // 6. refresh_token 저장 (기존 거 삭제 후 새로 저장)
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
                .userId(authUser.getId())
                .isMatching(false)
                .build();
    }

    // 토큰 재발급
    @Transactional
    public LoginResponse reissueToken(TokenReissueRequest request) {

        // 1. refresh token 유효성 검증
        if (!jwtUtil.validateToken(request.getRefreshToken())) {
            throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
        }

        // 2. DB에서 refresh token 찾기
        RefreshToken refreshToken = refreshTokenRepository
                .findByToken(request.getRefreshToken())
                .orElseThrow(() -> new IllegalArgumentException("리프레시 토큰이 존재하지 않습니다."));

        // 3. 만료 시간 확인
        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("만료된 리프레시 토큰입니다.");
        }

        // 4. 새 토큰 발급
        AuthUser authUser = refreshToken.getAuthUser();
        String newAccessToken = jwtUtil.generateAccessToken(authUser.getId());
        String newRefreshToken = jwtUtil.generateRefreshToken(authUser.getId());

        // 5. refresh token 업데이트
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