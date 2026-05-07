package com.example.gomplay.domain.user.dto;

import lombok.Getter;

@Getter
public class ProfileImageResponse {
    private String profileImageUrl;

    public ProfileImageResponse(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}