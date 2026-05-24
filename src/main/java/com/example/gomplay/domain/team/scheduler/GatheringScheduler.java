package com.example.gomplay.domain.team.scheduler;

import com.example.gomplay.domain.team.entity.Gathering;
import com.example.gomplay.domain.team.repository.GatheringRepository;
import com.example.gomplay.domain.notification.entity.Notification;
import com.example.gomplay.domain.notification.service.NotificationService;
import com.example.gomplay.domain.team.entity.GatheringParticipant;
import com.example.gomplay.domain.team.repository.GatheringParticipantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.List;


@Slf4j
@Component
@RequiredArgsConstructor
public class GatheringScheduler {

    private final GatheringRepository gatheringRepository;
    private final NotificationService notificationService;
    private final GatheringParticipantRepository gatheringParticipantRepository;

    @Scheduled(fixedRate = 600000)
    @Transactional
    public void autoCompleteGathering() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(2);

        List<Gathering> gatherings = gatheringRepository
                .findByStatusInAndScheduledEndAtBefore(
                        List.of(Gathering.Status.OPEN, Gathering.Status.CLOSED),
                        cutoff
                );

        for (Gathering gathering : gatherings) {
            gathering.updateStatus(Gathering.Status.COMPLETED);
            log.info("자동 완료 처리: gatheringId={}", gathering.getId());

            List<GatheringParticipant> participants = gatheringParticipantRepository
                    .findByGathering_Id(gathering.getId());

            // 방장에게 자동 종료 + 리뷰 가능 알림
            notificationService.createNotification(
                gathering.getHost(),
                Notification.NotificationType.match_auto_ended,
                "매칭 자동 종료",
                "'" + gathering.getTitle() + "' 매칭이 자동 종료되었어요!",
                gathering.getId()
            );
            notificationService.createNotification(
                gathering.getHost(),
                Notification.NotificationType.review_available,
                "평가 가능",
                "'" + gathering.getTitle() + "' 운동이 완료됐어요! 상대방을 평가해보세요.",
                gathering.getId()
            );

            // 참여자들에게 자동 종료 + 리뷰 가능 알림
            participants.forEach(p -> {
                notificationService.createNotification(
                    p.getUser(),
                    Notification.NotificationType.match_auto_ended,
                    "매칭 자동 종료",
                    "'" + gathering.getTitle() + "' 매칭이 자동 종료되었어요!",
                    gathering.getId()
                );
                notificationService.createNotification(
                    p.getUser(),
                    Notification.NotificationType.review_available,
                    "평가 가능",
                    "'" + gathering.getTitle() + "' 운동이 완료됐어요! 상대방을 평가해보세요.",
                    gathering.getId()
                );
            });
        }

        log.info("자동 완료 처리 완료: {}건", gatherings.size());
    }

    // 매 10분마다 부스트 만료 체크
    @Scheduled(fixedRate = 600000)
    @Transactional
    public void expireBoost() {
        List<Gathering> boostedGatherings = gatheringRepository
            .findByBoostedTrueAndBoostExpiredAtBefore(LocalDateTime.now());

    for (Gathering gathering : boostedGatherings) {
        gathering.boost(null);
    }
    log.info("부스트 만료 처리: {}건", boostedGatherings.size());
    }

    // 매 10분마다 EXPIRED 처리
    @Scheduled(fixedRate = 600000)
    @Transactional
    public void expireGathering() {
        LocalDateTime now = LocalDateTime.now();

        List<Gathering> gatherings = gatheringRepository
                .findByStatusAndScheduledAtBefore(Gathering.Status.OPEN, now);

        for (Gathering gathering : gatherings) {
            gathering.updateStatus(Gathering.Status.EXPIRED);
            log.info("만료 처리: gatheringId={}", gathering.getId());
        }

        log.info("만료 처리 완료: {}건", gatherings.size());
    }

}