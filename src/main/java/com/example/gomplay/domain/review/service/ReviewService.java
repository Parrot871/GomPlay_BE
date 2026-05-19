package com.example.gomplay.domain.review.service;

import com.example.gomplay.domain.review.dto.ReviewRequest;
import com.example.gomplay.domain.review.dto.ReviewResponse;
import com.example.gomplay.domain.review.entity.Review;
import com.example.gomplay.domain.review.repository.ReviewRepository;
import com.example.gomplay.domain.report.entity.Report;
import com.example.gomplay.domain.report.repository.ReportRepository;
import com.example.gomplay.domain.point.service.PointService;
import com.example.gomplay.domain.user.entity.UserProfile;
import com.example.gomplay.domain.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserProfileRepository userProfileRepository;
    private final ReportRepository reportRepository;
    private final PointService pointService;

    // 평가 제출
    @Transactional
    public ReviewResponse submitReview(Long userId, ReviewRequest request) {
        UserProfile reviewer = userProfileRepository.findByAuthUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        UserProfile reviewee = userProfileRepository.findById(request.getRevieweeId())
                .orElseThrow(() -> new IllegalArgumentException("평가 대상자를 찾을 수 없습니다."));

        // 중복 평가 방지
        if (request.getMatchResultId() != null &&
            reviewRepository.existsByReviewer_IdAndMatchResultId(reviewer.getId(), request.getMatchResultId())) {
             throw new IllegalArgumentException("이미 평가한 매칭입니다.");
        }
        if (request.getGatheringId() != null &&
            reviewRepository.existsByReviewer_IdAndGatheringId(reviewer.getId(), request.getGatheringId())) {
            throw new IllegalArgumentException("이미 평가한 모임입니다.");
        }

        // 매너온도 변동 계산
        double delta = 0;

        // 좋았어요 태그 하나라도 있으면 +0.5도
        if (request.getGoodTags() != null && !request.getGoodTags().isEmpty()) {
            delta += 0.5;
        }

        // 아쉬워요 태그 1개당 -0.5도
        if (request.getBadTags() != null) {
            delta -= request.getBadTags().size() * 0.5;
        }

        // 노쇼 -3도
        if (request.isNoShow()) {
            delta -= 3.0;
        }

        // 매너온도 업데이트 (0~100 범위 제한)
        BigDecimal newTemperature = reviewee.getMannerTemperature()
                .add(BigDecimal.valueOf(delta));
        newTemperature = newTemperature.max(BigDecimal.ZERO).min(BigDecimal.valueOf(100));
        reviewee.updateMannerTemperature(newTemperature);

        // 노쇼 처리
        if (request.isNoShow()) {
            reviewee.incrementNoShowCount();
    
        // 노쇼 패널티 포인트 차감
        int noShowCount = reviewee.getNoShowCount();
        if (noShowCount <= 4) {
        pointService.addPoint(reviewee, -(noShowCount * 10), "no_show", null);
        }
        // 5회 이상은 매칭 제한 7일 처리
       if (noShowCount >= 5) {
        reviewee.restrictMatching(LocalDateTime.now().plusDays(7));
      }
}

        // 좋았어요/아쉬워요 태그 저장
        String goodTags = request.getGoodTags() != null ?
                String.join(",", request.getGoodTags()) : null;
        String badTags = request.getBadTags() != null ?
                String.join(",", request.getBadTags()) : null;

        Review review = Review.builder()
                .reviewer(reviewer)
                .reviewee(reviewee)
                .matchResultId(request.getMatchResultId())
                .gatheringId(request.getGatheringId())
                .goodTags(goodTags)
                .badTags(badTags)
                .isNoShow(request.isNoShow())
                .comment(request.getComment())
                .build();

        reviewRepository.save(review);

        // 리뷰 작성 포인트 지급 +5P
        pointService.addPoint(reviewer, 5, "review", null);

        // 신고 처리
        if (request.getReportCategories() != null && !request.getReportCategories().isEmpty()) {
            String reason = String.join(",", request.getReportCategories());
            if (request.getReportContent() != null) {
                reason += " - " + request.getReportContent();
            }

            Report report = Report.builder()
                    .reporter(reviewer)
                    .reportee(reviewee)
                    .gathering(null)
                    .reason(reason)
                    .build();

            reportRepository.save(report);
        }

        return new ReviewResponse(review);
    }

    // 평가 조회
    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviews(Long userId) {
        UserProfile user = userProfileRepository.findByAuthUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        return reviewRepository.findByReviewee_Id(user.getId())
                .stream()
                .map(ReviewResponse::new)
                .collect(Collectors.toList());
    }
}