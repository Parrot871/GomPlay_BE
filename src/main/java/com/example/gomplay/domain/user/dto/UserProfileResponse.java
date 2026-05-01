package com.example.gomplay.domain.user.dto;

import com.example.gomplay.domain.user.entity.UserProfile;
import lombok.Getter;
import java.math.BigDecimal;


@Getter
public class UserProfileResponse {
    private Long id;
    private String name;
    private String department;
    private String studentId;
    private String profileImageUrl;
    private BigDecimal mannerTemperature;
    private Integer noShowCount;
    private Integer pointBalance;
    private Integer matchCount;
    private String exerciseTypes;  
    private String difficulty;     
    private String bio;           

    public UserProfileResponse(UserProfile userProfile) {
        this.id = userProfile.getId();
        this.name = userProfile.getName();
        this.department = userProfile.getDepartment();
        this.studentId = userProfile.getStudentId();
        this.profileImageUrl = userProfile.getProfileImageUrl();
        this.mannerTemperature = userProfile.getMannerTemperature();
        this.noShowCount = userProfile.getNoShowCount();
        this.pointBalance = userProfile.getPointBalance();
        this.matchCount = userProfile.getMatchCount();
        this.exerciseTypes = userProfile.getExerciseTypes();  
        this.difficulty = userProfile.getDifficulty();        
        this.bio = userProfile.getBio();
    }
}