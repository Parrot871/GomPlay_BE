package com.example.gomplay.domain.review.dto;

import lombok.Getter;
import java.util.List;

@Getter
public class ReviewRequest {
    private Long revieweeId;
    private Long matchResultId;
    private Long gatheringId; 
    private List<String> goodTags;
    private List<String> badTags;
    private boolean isNoShow;
    private String comment;
    private List<String> reportCategories;
    private String reportContent;
}