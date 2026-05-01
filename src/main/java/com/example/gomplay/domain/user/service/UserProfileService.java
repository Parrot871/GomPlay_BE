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
    public UserProfileResponse getProfile(Long userProfileId) {
        UserProfile userProfile = userProfileRepository.findById(userProfileId)
                .orElseThrow(() -> new RuntimeException("프로필을 찾을 수 없습니다."));
        return new UserProfileResponse(userProfile);
    }

    // 프로필 수정
    @Transactional
    public UserProfileResponse updateProfile(Long userProfileId, UserProfileUpdateRequest request) {
        UserProfile userProfile = userProfileRepository.findById(userProfileId)
                .orElseThrow(() -> new RuntimeException("프로필을 찾을 수 없습니다."));

        UserProfile updated = UserProfile.builder()
                .id(userProfile.getId())
                .authUser(userProfile.getAuthUser())
                .name(request.getName() != null ? request.getName() : userProfile.getName())
                .department(request.getDepartment() != null ? request.getDepartment() : userProfile.getDepartment())
                .studentId(request.getStudentId() != null ? request.getStudentId() : userProfile.getStudentId())
                .profileImageUrl(request.getProfileImageUrl() != null ? request.getProfileImageUrl() : userProfile.getProfileImageUrl())
                .mannerTemperature(userProfile.getMannerTemperature())
                .noShowCount(userProfile.getNoShowCount())
                .pointBalance(userProfile.getPointBalance())
                .matchCount(userProfile.getMatchCount())
                .createdAt(userProfile.getCreatedAt())
                .build();

        return new UserProfileResponse(userProfileRepository.save(updated));
    }
}