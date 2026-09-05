package com.escola.biblioteca.model;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("refresh_token")
@Getter
@Setter
@NoArgsConstructor
public class RefreshToken {

    @Id
    private Long id;

    private String tokenHash;

    private Integer admId;

    private String userAgent;

    private String ip;

    private LocalDateTime expiresAt;

    private LocalDateTime revokedAt;

    private LocalDateTime createdAt;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }
}
