package com.example.gomplay.domain.user.dto;

import com.example.gomplay.domain.user.entity.UserProfile;
import lombok.Getter;
import java.math.BigDecimal;

@Getter
public class PartnerProfileResponse {

    private Long id;
    private String name;
    private String department;
    private String studentId;
    private BigDecimal mannerTemperature;

    public PartnerProfileResponse(UserProfile userProfile) {
        this.id = userProfile.getId();
        this.name = userProfile.getName();
        this.department = userProfile.getDepartment();
        this.studentId = userProfile.getStudentId();
        this.mannerTemperature = userProfile.getMannerTemperature();
    }
}