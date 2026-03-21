package com.bharatbloodbank.repository;

import com.bharatbloodbank.entity.PasswordResetToken;
import com.bharatbloodbank.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    Optional<PasswordResetToken> findByUserAndUsedFalse(User user);

    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.expiryDate < :now OR t.used = true")
    void deleteExpiredAndUsedTokens(@Param("now") LocalDateTime now);
}
