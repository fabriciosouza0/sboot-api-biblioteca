package com.escola.biblioteca.config.converter;

public interface PostgreSqlEnum {
    default String getPgTypeName() {
        return getClass().getSimpleName().replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }
}
