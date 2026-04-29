package com.example.gomplay.domain.auth.Repository;

import com.example.gomplay.domain.auth.entity.AuthUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthUserRepository extends JpaRepository<AuthUser, Long>{

    Optional<AuthUser> findBySchoolEmail(String schoolEmail);
    boolean existsBySchoolEmail(String schoolEmail);
}
