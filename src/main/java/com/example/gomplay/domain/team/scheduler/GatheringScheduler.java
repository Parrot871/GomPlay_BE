package com.example.gomplay.domain.team.scheduler;

import com.example.gomplay.domain.team.entity.Gathering;
import com.example.gomplay.domain.team.repository.GatheringRepository;
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

    // 매 10분마다 실행
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