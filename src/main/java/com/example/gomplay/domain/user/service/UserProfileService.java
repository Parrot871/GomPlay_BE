package com.example.gomplay.domain.user.service;

import com.example.gomplay.domain.user.dto.UserProfileResponse;
import com.example.gomplay.domain.user.dto.UserProfileUpdateRequest;
import com.example.gomplay.domain.user.entity.UserProfile;
import com.example.gomplay.domain.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;

    // 프로필 조회
    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(Long userId) {
        UserProfile userProfile = userProfileRepository.findByAuthUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("프로필을 찾을 수 없습니다."));
        return new UserProfileResponse(userProfile);
    }

    // 프로필 수정
    @Transactional
    public UserProfileResponse updateProfile(Long userId, UserProfileUpdateRequest request) {
        UserProfile userProfile = userProfileRepository.findByAuthUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("프로필을 찾을 수 없습니다."));

        userProfile.updateProfile(
                request.getProfileImageUrl(),
                request.getExerciseTypes(),
                request.getDifficulty(),
                request.getBio()
        );

        return new UserProfileResponse(userProfile);
    }
}