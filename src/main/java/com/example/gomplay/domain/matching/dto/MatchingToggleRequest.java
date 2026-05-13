package com.example.gomplay.domain.matching.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MatchingToggleRequest {
    private Boolean isMatching;
    public Boolean isMatching() {
        return isMatching;
    }
}
