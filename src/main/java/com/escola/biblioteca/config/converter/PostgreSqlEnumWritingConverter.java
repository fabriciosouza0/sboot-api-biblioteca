package com.escola.biblioteca.config.converter;

import org.postgresql.util.PGobject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

import java.sql.SQLException;

@WritingConverter
public class PostgreSqlEnumWritingConverter implements Converter<PostgreSqlEnum, PGobject> {

    @Override
    public PGobject convert(PostgreSqlEnum source) {
        PGobject pgObject = new PGobject();
        pgObject.setType(source.getPgTypeName());
        try {
            pgObject.setValue(((Enum<?>) source).name());
        } catch (SQLException e) {
            throw new IllegalArgumentException("Erro ao converter enum " + source.getClass().getName() + " para PGobject", e);
        }
        return pgObject;
    }
}
