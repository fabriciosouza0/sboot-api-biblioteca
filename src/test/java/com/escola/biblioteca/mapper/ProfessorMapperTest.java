package com.escola.biblioteca.mapper;

import com.escola.biblioteca.dto.request.ProfessorRequest;
import com.escola.biblioteca.model.Locatario;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ProfessorMapperTest {

    private final ProfessorMapper mapper = new ProfessorMapperImpl();

    @Test
    void toLocatarioMapeiaCamposEditaveis() {
        ProfessorRequest request = new ProfessorRequest("98765432100", "João Souza", "85966666666");

        Locatario locatario = mapper.toLocatario(request);

        assertEquals("98765432100", locatario.getCpf());
        assertEquals("João Souza", locatario.getNome());
        assertEquals("85966666666", locatario.getTelefone());
        assertNull(locatario.getCodigoAluno());
        assertNull(locatario.getCodigoProfessor());
    }

    @Test
    void updateAlteraSomenteCamposEditaveis() {
        Locatario locatario = new Locatario();
        locatario.setCpf("98765432100");
        locatario.setNome("Antigo");
        locatario.setCodigoProfessor(9);

        mapper.update(new ProfessorRequest("98765432100", "Novo Nome", "85955555555"), locatario);

        assertEquals("98765432100", locatario.getCpf());
        assertEquals("Novo Nome", locatario.getNome());
        assertEquals("85955555555", locatario.getTelefone());
        assertEquals(9, locatario.getCodigoProfessor());
        assertNull(locatario.getCodigoAluno());
    }
}