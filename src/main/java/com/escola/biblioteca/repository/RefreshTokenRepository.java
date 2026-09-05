package com.escola.biblioteca.repository;

import com.escola.biblioteca.model.RefreshToken;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHashAndRevokedAtIsNull(String tokenHash);

    @Modifying
    @Query("UPDATE refresh_token SET revoked_at = :now WHERE adm_id = :admId AND revoked_at IS NULL")
    int revokeAllActiveByAdmId(@Param("admId") Integer admId, @Param("now") LocalDateTime now);

    @Modifying
    @Query("DELETE FROM refresh_token WHERE expires_at < :now OR revoked_at IS NOT NULL")
    int deleteExpiredOrRevoked(@Param("now") LocalDateTime now);
}