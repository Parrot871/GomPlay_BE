package com.example.gomplay.domain.matching.service;

import com.example.gomplay.domain.matching.dto.MatchingToggleResponse;
import com.example.gomplay.domain.matching.entity.QuickMatchLog;
import com.example.gomplay.domain.matching.repository.QuickMatchLogRepository;
import com.example.gomplay.domain.user.entity.UserProfile;
import com.example.gomplay.domain.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QuickMatchService {

    private final UserProfileRepository userProfileRepository;
    private final QuickMatchLogRepository quickMatchLogRepository;

    @Transactional
    public MatchingToggleResponse updateMatchingStatus(Long userId, boolean isMatching) {
        UserProfile userProfile = userProfileRepository.findByAuthUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("프로필을 찾을 수 없습니다."));

        userProfile.updateMatchingStatus(isMatching);

        if (isMatching) {
            quickMatchLogRepository.save(QuickMatchLog.createWaiting(userProfile));
        } else {
            quickMatchLogRepository.findTopByUserProfileIdAndStatus(
                            userProfile.getId(), QuickMatchLog.MatchStatus.WAITING)
                    .ifPresent(QuickMatchLog::cancel);
        }

        String message = isMatching ? "매칭 대기 중입니다." : "매칭이 종료되었습니다.";
        return MatchingToggleResponse.builder()
                .isMatching(isMatching)
                .message(message)
                .build();
    }
}
