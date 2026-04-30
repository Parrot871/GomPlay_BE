package com.example.gomplay.domain.auth;

import com.example.gomplay.domain.auth.dto.*;
import com.example.gomplay.domain.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // 회원가입 + 인증코드 발송
    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.ok(authService.signup(request));
    }

    // 이메일 인증코드 확인
    @PostMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        authService.verifyEmail(request);
        return ResponseEntity.ok("이메일 인증이 완료되었습니다.");
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // 토큰 재발급
    @PostMapping("/reissue")
    public ResponseEntity<LoginResponse> reissue(@Valid @RequestBody TokenReissueRequest request) {
        return ResponseEntity.ok(authService.reissueToken(request));
    }

    // 인증코드 재전송
    @PostMapping("/resend-verification")
    public ResponseEntity<String> resendVerification(@RequestBody ResendVerificationRequest request) {
        authService.resendVerificationEmail(request.getSchoolEmail());
        return ResponseEntity.ok("인증 코드가 재전송되었습니다.");
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@AuthenticationPrincipal Long userId) {
        authService.logout(userId);
        return ResponseEntity.ok("로그아웃 되었습니다.");
    }
}
