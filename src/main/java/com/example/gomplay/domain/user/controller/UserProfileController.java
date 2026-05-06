package com.example.gomplay.domain.user.controller;

import com.example.gomplay.domain.user.dto.PasswordUpdateRequest;
import com.example.gomplay.domain.user.dto.UserProfileResponse;
import com.example.gomplay.domain.user.dto.UserProfileUpdateRequest;
import com.example.gomplay.domain.user.dto.UserProfileUpdateResponse;
import com.example.gomplay.domain.user.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.example.gomplay.domain.user.dto.ProfileImageResponse;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserProfileController {

    private final UserProfileService userProfileService;

    // 프로필 조회
    @GetMapping("/me/profile")
    public ResponseEntity<UserProfileResponse> getProfile(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(userProfileService.getProfile(userId));
    }

    // 프로필 수정
    @PatchMapping("/me/profile")
    public ResponseEntity<UserProfileUpdateResponse> updateProfile(
            @AuthenticationPrincipal Long userId,
            @RequestBody UserProfileUpdateRequest request) {
        return ResponseEntity.ok(userProfileService.updateProfile(userId, request));
    }

    // 비밀번호 수정
    @PatchMapping("/me/password")
    public ResponseEntity<String> updatePassword(
            @AuthenticationPrincipal Long userId,
            @RequestBody PasswordUpdateRequest request) {
        userProfileService.updatePassword(userId, request);
        return ResponseEntity.ok("비밀번호가 변경되었습니다.");
    }

    @PostMapping("/me/profile-image")
    public ResponseEntity<ProfileImageResponse> uploadProfileImage(
        @AuthenticationPrincipal Long userId,
        @RequestParam("file") MultipartFile file) throws IOException {
    return ResponseEntity.ok(userProfileService.uploadProfileImage(userId, file));
}
}