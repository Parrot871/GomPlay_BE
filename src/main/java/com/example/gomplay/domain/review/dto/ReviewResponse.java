package com.example.gomplay.domain.review.dto;

import com.example.gomplay.domain.review.entity.Review;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class ReviewResponse {
    private Long id;
    private Long revieweeId;
    private String goodTags;
    private String badTags;
    private boolean isNoShow;
    private String comment;
    private LocalDateTime createdAt;

    public ReviewResponse(Review review) {
        this.id = review.getId();
        this.revieweeId = review.getReviewee().getId();
        this.goodTags = review.getGoodTags();
        this.badTags = review.getBadTags();
        this.isNoShow = review.isNoShow();
        this.comment = review.getComment();
        this.createdAt = review.getCreatedAt();
    }
}