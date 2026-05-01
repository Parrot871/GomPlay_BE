package com.example.gomplay.domain.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserProfileUpdateRequest {
    private String name;
    private String department;
    private String studentId;
    private String profileImageUrl;
}