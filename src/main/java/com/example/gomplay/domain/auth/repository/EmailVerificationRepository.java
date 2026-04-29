package com.example.gomplay.domain.auth.repository;

import com.example.gomplay.domain.auth.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {

    Optional<EmailVerification> findByToken(String token);
    void deleteByAuthUser_Id(Long userId);
}
