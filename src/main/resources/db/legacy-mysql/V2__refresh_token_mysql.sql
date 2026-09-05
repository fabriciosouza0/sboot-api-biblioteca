-- V2: Tabela de refresh tokens (opacos, armazenados como SHA-256)
CREATE TABLE `refresh_token`
(
    `id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `token_hash` VARCHAR(64)  NOT NULL,
    `adm_id`     INT          NOT NULL,
    `user_agent` VARCHAR(500) DEFAULT NULL,
    `ip`         VARCHAR(45)  DEFAULT NULL,
    `expires_at` DATETIME     NOT NULL,
    `revoked_at` DATETIME     DEFAULT NULL,
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_refresh_token_hash` (`token_hash`),
    KEY `idx_refresh_token_adm` (`adm_id`),
    KEY `idx_refresh_token_expires` (`expires_at`),
    CONSTRAINT `fk_refresh_token_adm` FOREIGN KEY (`adm_id`) REFERENCES `adms` (`codigo`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;