package com.escola.biblioteca.config.converter;

import org.postgresql.util.PGobject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class PostgreSqlEnumReadingConverterFactory implements ConverterFactory<PGobject, PostgreSqlEnum> {

    @Override
    @SuppressWarnings("unchecked")
    public <T extends PostgreSqlEnum> Converter<PGobject, T> getConverter(Class<T> targetType) {
        return source -> {
            if (source == null || source.getValue() == null) {
                return null;
            }
            if (targetType.isEnum()) {
                return (T) Enum.valueOf((Class<? extends Enum>) targetType, source.getValue());
            }
            throw new IllegalArgumentException("Tipo alvo não é um enum: " + targetType.getName());
        };
    }
}
