package com.example.gomplay.domain.review.dto;

import lombok.Getter;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;


@Getter
public class ReviewRequest {
    private Long revieweeId;
    private Long matchResultId;
    private Long gatheringId; 
    private List<String> goodTags;
    private List<String> badTags;
    private String comment;
    private List<String> reportCategories;
    private String reportContent;
    @JsonProperty("isNoShow")
    private boolean isNoShow;
}

