package com.example.gomplay.domain.review.service;

import com.example.gomplay.domain.review.dto.ReviewRequest;
import com.example.gomplay.domain.review.dto.ReviewResponse;
import com.example.gomplay.domain.review.entity.Review;
import com.example.gomplay.domain.review.repository.ReviewRepository;
import com.example.gomplay.domain.report.entity.Report;
import com.example.gomplay.domain.report.repository.ReportRepository;
import com.example.gomplay.domain.team.entity.Gathering;
import com.example.gomplay.domain.team.repository.GatheringRepository;
import com.example.gomplay.domain.user.entity.UserProfile;
import com.example.gomplay.domain.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserProfileRepository userProfileRepository;
    private final GatheringRepository gatheringRepository;
    private final ReportRepository reportRepository;

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

        // 매너온도 업데이트
        BigDecimal newTemperature = BigDecimal.valueOf(request.getMannerTemperature());
        reviewee.updateMannerTemperature(newTemperature);

        // 노쇼 처리
        if (request.isNoShow()) {
            reviewee.incrementNoShowCount();
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

        // 신고 처리
        if (request.getReportCategories() != null && !request.getReportCategories().isEmpty()) {
            Gathering gathering = gatheringRepository.findById(request.getGatheringId())
                    .orElse(null);

            if (gathering != null) {
                String reason = String.join(",", request.getReportCategories());
                if (request.getReportContent() != null) {
                    reason += " - " + request.getReportContent();
                }

                Report report = Report.builder()
                        .reporter(reviewer)
                        .reportee(reviewee)
                        .gathering(gathering)
                        .reason(reason)
                        .build();

                reportRepository.save(report);
            }
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