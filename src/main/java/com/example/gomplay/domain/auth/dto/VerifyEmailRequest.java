package com.example.gomplay.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class VerifyEmailRequest {

    @NotBlank
    private String token;
}
