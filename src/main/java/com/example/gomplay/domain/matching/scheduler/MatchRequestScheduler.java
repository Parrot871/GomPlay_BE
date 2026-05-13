package com.example.gomplay.domain.matching.scheduler;

import com.example.gomplay.domain.matching.entity.MatchRequest;
import com.example.gomplay.domain.matching.repository.MatchRequestRepository;
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
public class MatchRequestScheduler {

    private final MatchRequestRepository matchRequestRepository;

    @Scheduled(fixedDelay = 10000) // 10초마다 실행
    @Transactional
    public void timeoutExpiredRequests() {
        List<MatchRequest> expiredRequests = matchRequestRepository
                .findByStatusAndExpiresAtBefore(
                        MatchRequest.MatchRequestStatus.PENDING,
                        LocalDateTime.now()
                );

        expiredRequests.forEach(MatchRequest::timeout);
        log.info("TIMEOUT 처리된 요청 수: {}", expiredRequests.size());
    }
}