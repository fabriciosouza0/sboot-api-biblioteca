package com.escola.biblioteca.config;

import com.escola.biblioteca.model.enums.AdminRole;
import com.escola.biblioteca.security.AdminAuthenticationToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RlsAspectTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private Statement statement;

    private RlsAspect rlsAspect;

    @BeforeEach
    void setUp() throws Exception {
        rlsAspect = new RlsAspect(dataSource);
        lenient().when(dataSource.getConnection()).thenReturn(connection);
        lenient().when(connection.createStatement()).thenReturn(statement);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void globalAdmin_withInstitutionSelected_scopesToInstitution() throws Exception {
        UUID instId = UUID.randomUUID();
        var auth = new AdminAuthenticationToken("global_admin", AdminRole.GLOBAL_ADMIN,
                List.of(), instId, List.of(new SimpleGrantedAuthority("ROLE_GLOBAL_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        rlsAspect.setRlsVariables();

        verify(statement).execute("SET LOCAL app.is_global_admin = 'false'");
        verify(statement).execute("SET LOCAL app.current_institution_id = '" + instId + "'");
        verify(statement).execute("SET LOCAL app.current_adm_codigo = 'global_admin'");
    }

    @Test
    void globalAdmin_withoutInstitutionSelected_bypassesRls() throws Exception {
        var auth = new AdminAuthenticationToken("global_admin", AdminRole.GLOBAL_ADMIN,
                List.of(), null, List.of(new SimpleGrantedAuthority("ROLE_GLOBAL_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        rlsAspect.setRlsVariables();

        verify(statement).execute("SET LOCAL app.is_global_admin = 'true'");
        verify(statement).execute("SET LOCAL app.current_institution_id = ''");
        verify(statement).execute("SET LOCAL app.current_adm_codigo = 'global_admin'");
    }

    @Test
    void institutionAdmin_scopesToInstitution() throws Exception {
        UUID instId = UUID.randomUUID();
        var auth = new AdminAuthenticationToken("escola_admin", AdminRole.INSTITUTION_ADMIN,
                List.of(instId), instId, List.of(new SimpleGrantedAuthority("ROLE_INSTITUTION_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        rlsAspect.setRlsVariables();

        verify(statement).execute("SET LOCAL app.is_global_admin = 'false'");
        verify(statement).execute("SET LOCAL app.current_institution_id = '" + instId + "'");
        verify(statement).execute("SET LOCAL app.current_adm_codigo = 'escola_admin'");
    }
}
