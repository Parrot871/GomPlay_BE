package com.example.gomplay.domain.review.repository;

import com.example.gomplay.domain.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByReviewer_IdAndMatchResultId(Long reviewerId, Long matchResultId);
    boolean existsByReviewer_IdAndGatheringId(Long reviewerId, Long gatheringId);
    List<Review> findByReviewee_Id(Long revieweeId);
}