package com.example.gomplay.domain.user.service;

import com.example.gomplay.domain.auth.entity.AuthUser;
import com.example.gomplay.domain.auth.repository.AuthUserRepository;
import com.example.gomplay.domain.user.dto.PasswordUpdateRequest;
import com.example.gomplay.domain.user.dto.UserProfileResponse;
import com.example.gomplay.domain.user.dto.UserProfileUpdateRequest;
import com.example.gomplay.domain.user.dto.UserProfileUpdateResponse;
import com.example.gomplay.domain.user.dto.PartnerProfileResponse;

import com.example.gomplay.domain.user.entity.UserProfile;
import com.example.gomplay.domain.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.example.gomplay.global.s3.S3Service;
import com.example.gomplay.domain.user.dto.ProfileImageResponse;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final AuthUserRepository authUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final S3Service s3Service;

    // 프로필 조회
    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(Long userId) {
        UserProfile userProfile = userProfileRepository.findByAuthUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("프로필을 찾을 수 없습니다."));
        return new UserProfileResponse(userProfile);
    }

    // 프로필 수정
    @Transactional
    public UserProfileUpdateResponse updateProfile(Long userId, UserProfileUpdateRequest request) {
        UserProfile userProfile = userProfileRepository.findByAuthUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("프로필을 찾을 수 없습니다."));

        userProfile.updateProfile(request.getProfileImageUrl());

        return new UserProfileUpdateResponse(userId, userProfile.getUpdatedAt());
    }

    // 비밀번호 수정
    @Transactional
    public void updatePassword(Long userId, PasswordUpdateRequest request) {
        AuthUser authUser = authUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(request.getCurrentPassword(), authUser.getPasswordHash())) {
            throw new IllegalArgumentException("현재 비밀번호가 올바르지 않습니다.");
        }

        authUser.updatePassword(passwordEncoder.encode(request.getNewPassword()));
    }

    @Transactional
    public ProfileImageResponse uploadProfileImage(Long userId, MultipartFile file) throws IOException {
    UserProfile userProfile = userProfileRepository.findByAuthUser_Id(userId)
            .orElseThrow(() -> new IllegalArgumentException("프로필을 찾을 수 없습니다."));

    String imageUrl = s3Service.uploadFile(file);
    userProfile.updateProfile(imageUrl);

    return new ProfileImageResponse(imageUrl);
    }

    // 상대방 프로필 조회
    @Transactional(readOnly = true)
    public PartnerProfileResponse getUserProfile(Long userProfileId) {
    UserProfile userProfile = userProfileRepository.findById(userProfileId)
            .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
    return new PartnerProfileResponse(userProfile);
    }
}