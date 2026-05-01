package com.example.gomplay.domain.user.controller;

import com.example.gomplay.domain.user.dto.UserProfileResponse;
import com.example.gomplay.domain.user.dto.UserProfileUpdateRequest;
import com.example.gomplay.domain.user.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserProfileController {

    private final UserProfileService userProfileService;

    // 프로필 조회
    @GetMapping("/profile/{id}")
    public ResponseEntity<UserProfileResponse> getProfile(@PathVariable Long id) {
        return ResponseEntity.ok(userProfileService.getProfile(id));
    }

    // 프로필 수정
    @PatchMapping("/profile/{id}")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @PathVariable Long id,
            @RequestBody UserProfileUpdateRequest request) {
        return ResponseEntity.ok(userProfileService.updateProfile(id, request));
    }
}