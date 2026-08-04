package com.escola.biblioteca.repository;

import com.escola.biblioteca.model.RefreshToken;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHashAndRevokedAtIsNull(String tokenHash);

    @Modifying
    @Query("update RefreshToken r set r.revokedAt = :now where r.adm.id = :admId and r.revokedAt is null")
    int revokeAllActiveByAdmId(@Param("admId") Integer admId, @Param("now") LocalDateTime now);

    @Modifying
    @Query("delete from RefreshToken r where r.expiresAt < :now or r.revokedAt is not null")
    int deleteExpiredOrRevoked(@Param("now") LocalDateTime now);
}
