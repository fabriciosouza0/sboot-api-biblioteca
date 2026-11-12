package com.escola.biblioteca.config;

import com.escola.biblioteca.config.converter.PostgreSqlEnumReadingConverterFactory;
import com.escola.biblioteca.config.converter.PostgreSqlEnumWritingConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Configuration
public class JdbcConfig {

    @Bean
    public JdbcCustomConversions jdbcCustomConversions() {
        return new JdbcCustomConversions(List.of(
                new PostgreSqlEnumWritingConverter(),
                new PostgreSqlEnumReadingConverterFactory(),
                TimestampToOffsetDateTimeConverter.INSTANCE,
                OffsetDateTimeToTimestampConverter.INSTANCE
        ));
    }

    @ReadingConverter
    public enum TimestampToOffsetDateTimeConverter implements Converter<Timestamp, OffsetDateTime> {
        INSTANCE;

        @Override
        public OffsetDateTime convert(Timestamp source) {
            return source.toInstant().atOffset(ZoneOffset.UTC);
        }
    }

    @WritingConverter
    public enum OffsetDateTimeToTimestampConverter implements Converter<OffsetDateTime, Timestamp> {
        INSTANCE;

        @Override
        public Timestamp convert(OffsetDateTime source) {
            return Timestamp.from(source.toInstant());
        }
    }
}
