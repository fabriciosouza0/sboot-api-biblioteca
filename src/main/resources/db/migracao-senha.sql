-- Migracao: senha do admin em texto puro -> BCrypt (60 caracteres).
-- Necessario antes do primeiro login na API Spring Boot.
USE `biblioteca`;
ALTER TABLE `adms`
    MODIFY `senha` varchar(60) NOT NULL;
