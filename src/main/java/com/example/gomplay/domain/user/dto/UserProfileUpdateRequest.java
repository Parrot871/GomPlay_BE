package com.example.gomplay.domain.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserProfileUpdateRequest {
    private String profileImageUrl;  // 프로필 이미지 URL 
}