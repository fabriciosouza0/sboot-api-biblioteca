-- V1: Schema inicial do sistema bibliotecario
-- Equivalente ao SQL legado (SQL/Biblioteca.sql) normalizado para Flyway.
-- Em bancos existentes este arquivo e ignorado via baseline (baseline-version: 1).

CREATE TABLE `adms`
(
    `codigo` int(11)     NOT NULL AUTO_INCREMENT,
    `login`  varchar(15) NOT NULL,
    `senha`  varchar(60) NOT NULL,
    `nome`   varchar(20) NOT NULL,
    PRIMARY KEY (`codigo`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE `turma`
(
    `CODIGO`    int(11)     NOT NULL AUTO_INCREMENT,
    `DESCRICAO` varchar(45) NOT NULL,
    PRIMARY KEY (`CODIGO`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE `aluno`
(
    `CODIGO`       int(11) NOT NULL AUTO_INCREMENT,
    `CODIGO_TURMA` int(11) DEFAULT NULL,
    PRIMARY KEY (`CODIGO`),
    KEY `CODIGO_TURMA` (`CODIGO_TURMA`),
    CONSTRAINT `aluno_ibfk_1` FOREIGN KEY (`CODIGO_TURMA`) REFERENCES `turma` (`CODIGO`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE `professor`
(
    `CODIGO` int(11) NOT NULL AUTO_INCREMENT,
    PRIMARY KEY (`CODIGO`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE `autor`
(
    `codigo` int(11)     NOT NULL AUTO_INCREMENT,
    `nome`   varchar(45) DEFAULT NULL,
    PRIMARY KEY (`codigo`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE `cdd`
(
    `CODIGO`    int(11)     NOT NULL AUTO_INCREMENT,
    `DESCRICAO` varchar(45) NOT NULL,
    PRIMARY KEY (`CODIGO`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE `livro`
(
    `codigo`       bigint(13)  NOT NULL,
    `titulo`       varchar(45) NOT NULL,
    `codigo_autor` int(11) DEFAULT NULL,
    `codigo_cdd`   int(11) DEFAULT NULL,
    `qtd`          int(11) DEFAULT NULL,
    PRIMARY KEY (`codigo`),
    KEY `autor` (`codigo_autor`),
    KEY `cdd` (`codigo_cdd`),
    CONSTRAINT `autor` FOREIGN KEY (`codigo_autor`) REFERENCES `autor` (`codigo`),
    CONSTRAINT `cdd` FOREIGN KEY (`codigo_cdd`) REFERENCES `cdd` (`CODIGO`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE `locatario`
(
    `CPF`              varchar(14) NOT NULL,
    `NOME`             varchar(45) NOT NULL,
    `telefone`         varchar(14) DEFAULT NULL,
    `CODIGO_PROFESSOR` int(11)     DEFAULT NULL,
    `CODIGO_ALUNO`     int(11)     DEFAULT NULL,
    PRIMARY KEY (`CPF`),
    KEY `CODIGO_PROFESSOR` (`CODIGO_PROFESSOR`),
    KEY `CODIGO_ALUNO` (`CODIGO_ALUNO`),
    CONSTRAINT `locatario_ibfk_1` FOREIGN KEY (`CODIGO_PROFESSOR`) REFERENCES `professor` (`CODIGO`),
    CONSTRAINT `locatario_ibfk_2` FOREIGN KEY (`CODIGO_ALUNO`) REFERENCES `aluno` (`CODIGO`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE `loca`
(
    `codigo`            int(11)     NOT NULL AUTO_INCREMENT,
    `codigo_livro`      bigint(13)  NOT NULL,
    `cpfLocatario`      varchar(14) NOT NULL,
    `dataDeLocacao`     date        NOT NULL,
    `dataParaDevolucao` date        NOT NULL,
    `atrasado`          varchar(1)  NOT NULL,
    PRIMARY KEY (`codigo`),
    KEY `FK_cpfLocatario` (`cpfLocatario`),
    KEY `FK_codigo_livro` (`codigo_livro`),
    CONSTRAINT `FK_codigo_livro` FOREIGN KEY (`codigo_livro`) REFERENCES `livro` (`codigo`),
    CONSTRAINT `FK_cpfLocatario` FOREIGN KEY (`cpfLocatario`) REFERENCES `locatario` (`CPF`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

-- Seed: admin padrao (senha em texto puro, migrada para BCrypt no primeiro login)
INSERT INTO `adms` (`codigo`, `login`, `senha`, `nome`)
VALUES (1, '000.000.000-00', 'admin', 'admin');

INSERT INTO `turma` (`CODIGO`, `DESCRICAO`)
VALUES (1, '1º ADM'),
       (2, '1º AGRO'),
       (3, '1º FIN'),
       (4, '1º INF'),
       (5, '2º ADM'),
       (6, '2º AGRO'),
       (7, '2º DCC'),
       (8, '2º FIN'),
       (9, '2º INF'),
       (10, '3º ADM'),
       (11, '3º AGRO'),
       (12, '3º DCC'),
       (13, '3º FIN'),
       (14, '3º INF');
