package com.example.gomplay.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignupRequest {

    @NotBlank
    @Email
    private String schoolEmail;

    @NotBlank
    private String password;

    @NotBlank
    private String name;

    @NotBlank
    private String studentId;

    @NotBlank
    private String college;

    @NotBlank
    private String department;
}
