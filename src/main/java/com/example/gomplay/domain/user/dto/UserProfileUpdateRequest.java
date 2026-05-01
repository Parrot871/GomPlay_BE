package com.example.gomplay.domain.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserProfileUpdateRequest {
    private String profileImageUrl;  // 프로필 이미지 URL 
    private String exerciseTypes;    // 운동 종목 
    private String difficulty;       // 난이도 
    private String bio;              // 한 줄 소개 
}