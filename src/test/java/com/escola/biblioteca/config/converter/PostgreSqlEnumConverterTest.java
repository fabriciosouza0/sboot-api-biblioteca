package com.escola.biblioteca.config.converter;

import com.escola.biblioteca.model.enums.AdminRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.postgresql.util.PGobject;
import org.springframework.core.convert.converter.Converter;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PostgreSqlEnumConverterTest {

    private PostgreSqlEnumWritingConverter writingConverter;
    private PostgreSqlEnumReadingConverterFactory readingConverterFactory;

    @BeforeEach
    void setUp() {
        writingConverter = new PostgreSqlEnumWritingConverter();
        readingConverterFactory = new PostgreSqlEnumReadingConverterFactory();
    }

    @Test
    void writingConverter_deveConverterAdminRoleParaPGobjectComTipoSnakeCase() {
        PGobject result = writingConverter.convert(AdminRole.GLOBAL_ADMIN);

        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo("admin_role");
        assertThat(result.getValue()).isEqualTo("GLOBAL_ADMIN");
    }

    @Test
    void writingConverter_deveConverterQualquerEnumImplementandoPostgreSqlEnum() {
        PGobject result = writingConverter.convert(SampleCustomEnum.CUSTOM_VAL);

        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo("sample_custom_enum");
        assertThat(result.getValue()).isEqualTo("CUSTOM_VAL");
    }

    @Test
    void readingConverter_deveConverterPGobjectParaAdminRole() throws SQLException {
        PGobject pgObject = new PGobject();
        pgObject.setType("admin_role");
        pgObject.setValue("INSTITUTION_ADMIN");

        Converter<PGobject, AdminRole> converter = readingConverterFactory.getConverter(AdminRole.class);
        AdminRole role = converter.convert(pgObject);

        assertThat(role).isEqualTo(AdminRole.INSTITUTION_ADMIN);
    }

    @Test
    void readingConverter_deveConverterPGobjectParaQualquerEnumCustomizado() throws SQLException {
        PGobject pgObject = new PGobject();
        pgObject.setType("sample_custom_enum");
        pgObject.setValue("CUSTOM_VAL");

        Converter<PGobject, SampleCustomEnum> converter = readingConverterFactory.getConverter(SampleCustomEnum.class);
        SampleCustomEnum val = converter.convert(pgObject);

        assertThat(val).isEqualTo(SampleCustomEnum.CUSTOM_VAL);
    }

    @Test
    void readingConverter_quandoValorNulo_retornaNulo() {
        PGobject pgObject = new PGobject();
        pgObject.setType("admin_role");

        Converter<PGobject, AdminRole> converter = readingConverterFactory.getConverter(AdminRole.class);
        assertThat(converter.convert(pgObject)).isNull();
        assertThat(converter.convert(null)).isNull();
    }

    @Test
    void readingConverter_quandoValorInvalido_lancaExcecao() throws SQLException {
        PGobject pgObject = new PGobject();
        pgObject.setType("admin_role");
        pgObject.setValue("VALOR_INEXISTENTE");

        Converter<PGobject, AdminRole> converter = readingConverterFactory.getConverter(AdminRole.class);
        assertThatThrownBy(() -> converter.convert(pgObject))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private enum SampleCustomEnum implements PostgreSqlEnum {
        CUSTOM_VAL
    }
}
