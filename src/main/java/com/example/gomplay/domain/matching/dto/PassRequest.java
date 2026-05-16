package com.example.gomplay.domain.matching.dto;

import lombok.Getter;
import java.util.List;

@Getter
public class PassRequest {
    private List<Long> excludeIds;
}
