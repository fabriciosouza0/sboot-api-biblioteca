package com.escola.biblioteca.mapper;

import com.escola.biblioteca.dto.request.AlunoRequest;
import com.escola.biblioteca.model.Locatario;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AlunoMapperTest {

    private final AlunoMapper mapper = new AlunoMapperImpl();

    @Test
    void toLocatarioMapeiaCamposEditaveis() {
        AlunoRequest request = new AlunoRequest("12345678901", "Maria Silva", "85999999999", 3);

        Locatario locatario = mapper.toLocatario(request);

        assertEquals("12345678901", locatario.getCpf());
        assertEquals("Maria Silva", locatario.getNome());
        assertEquals("85999999999", locatario.getTelefone());
        assertNull(locatario.getCodigoAluno());
        assertNull(locatario.getCodigoProfessor());
    }

    @Test
    void updateAlteraSomenteCamposEditaveis() {
        Locatario locatario = new Locatario();
        locatario.setCpf("12345678901");
        locatario.setNome("Antigo");
        locatario.setTelefone("85988888888");
        locatario.setCodigoAluno(7);

        mapper.update(new AlunoRequest("12345678901", "Novo Nome", "85977777777", 4), locatario);

        assertEquals("12345678901", locatario.getCpf());
        assertEquals("Novo Nome", locatario.getNome());
        assertEquals("85977777777", locatario.getTelefone());
        assertEquals(7, locatario.getCodigoAluno());
        assertNull(locatario.getCodigoProfessor());
    }
}