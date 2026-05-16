package com.example.gomplay.domain.point.service;

import com.example.gomplay.domain.point.dto.PointLogResponse;
import com.example.gomplay.domain.point.entity.PointLog;
import com.example.gomplay.domain.point.repository.PointLogRepository;
import com.example.gomplay.domain.team.entity.Gathering;
import com.example.gomplay.domain.team.repository.GatheringRepository;
import com.example.gomplay.domain.user.entity.UserProfile;
import com.example.gomplay.domain.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PointService {

    private final PointLogRepository pointLogRepository;
    private final UserProfileRepository userProfileRepository;
    private final GatheringRepository gatheringRepository;

    // 포인트 내역 조회
    @Transactional(readOnly = true)
    public List<PointLogResponse> getPointLogs(Long userId) {
        UserProfile user = userProfileRepository.findByAuthUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        return pointLogRepository.findByUserProfile_IdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(PointLogResponse::new)
                .collect(Collectors.toList());
    }

    // 포인트 지급/차감
    @Transactional
    public void addPoint(UserProfile user, int delta, String reason, Long gatheringId) {
        user.addPoint(delta);

        Gathering gathering = null;
        if (gatheringId != null) {
            gathering = gatheringRepository.findById(gatheringId).orElse(null);
        }

        PointLog pointLog = PointLog.builder()
                .userProfile(user)
                .delta(delta)
                .balanceSnapshot(user.getPointBalance())
                .reason(reason)
                .gathering(gathering)
                .build();

        pointLogRepository.save(pointLog);
    }
}