package com.escola.biblioteca.model.enums;

import com.escola.biblioteca.config.converter.PostgreSqlEnum;

public enum AdminRole implements PostgreSqlEnum {
    GLOBAL_ADMIN,
    INSTITUTION_ADMIN
}