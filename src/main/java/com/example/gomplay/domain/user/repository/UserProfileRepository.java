package com.example.gomplay.domain.user.repository;

import com.example.gomplay.domain.user.entity.UserProfile;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByAuthUser_Id(Long authUserId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM UserProfile u WHERE u.authUser.id = :authUserId")
    Optional<UserProfile> findByAuthUserIdWithLock(@Param("authUserId") Long authUserId);
}