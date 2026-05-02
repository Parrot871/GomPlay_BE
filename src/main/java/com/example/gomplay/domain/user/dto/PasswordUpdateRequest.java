package com.example.gomplay.domain.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PasswordUpdateRequest {
    private String currentPassword;  // 현재 비밀번호
    private String newPassword;      // 새 비밀번호
}