package com.example.gomplay.domain.point.dto;

import com.example.gomplay.domain.point.entity.PointLog;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class PointLogResponse {
    private Long id;
    private Integer delta;
    private Integer balanceSnapshot;
    private String reason;
    private LocalDateTime createdAt;

    public PointLogResponse(PointLog pointLog) {
        this.id = pointLog.getId();
        this.delta = pointLog.getDelta();
        this.balanceSnapshot = pointLog.getBalanceSnapshot();
        this.reason = pointLog.getReason();
        this.createdAt = pointLog.getCreatedAt();
    }
}