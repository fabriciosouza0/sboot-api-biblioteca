package com.escola.biblioteca;

import com.escola.biblioteca.model.Adm;
import com.escola.biblioteca.repository.AdmRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthFlowIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("biblioteca");

    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AdmRepository admRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @BeforeEach
    void seedAdmin() {
        if (admRepository.findByLogin("tester").isEmpty()) {
            Adm adm = new Adm();
            adm.setLogin("tester");
            adm.setNome("Teste");
            adm.setSenha(passwordEncoder.encode("senha123"));
            admRepository.save(adm);
        }
    }

    private String doLogin() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"login":"tester","senha":"senha123"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", not(blankOrNullString())))
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andExpect(cookie().httpOnly("refreshToken", true))
                .andReturn();

        Cookie refreshCookie = result.getResponse().getCookie("refreshToken");
        if (refreshCookie == null) {
            throw new IllegalStateException("refreshToken cookie não foi definido no login");
        }
        return refreshCookie.getValue();
    }

    @Test
    void loginRetornaAccessNoBody_eRefreshNoCookieHttpOnly() throws Exception {
        doLogin();
    }

    @Test
    void accessTokenAutenticaEndpointProtegido() throws Exception {
        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"login":"tester","senha":"senha123"}"""))
                .andExpect(status().isOk())
                .andReturn();
        String accessToken = com.jayway.jsonpath.JsonPath.read(
                login.getResponse().getContentAsString(), "$.accessToken");

        mockMvc.perform(get("/api/livros")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    void semTokenRetorna401() throws Exception {
        mockMvc.perform(get("/api/livros"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshRotacionaToken() throws Exception {
        String refresh = doLogin();

        // 1º refresh: ok e rotaciona (novo cookie, body com accessToken)
        MvcResult first = mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie("refreshToken", refresh)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", not(blankOrNullString())))
                .andReturn();
        String newRefresh = first.getResponse().getCookie("refreshToken").getValue();

        // Refresh antigo NÃO pode ser reutilizado (rotação estrita)
        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie("refreshToken", refresh)))
                .andExpect(status().isBadRequest());

        // Novo refresh continua funcionando
        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie("refreshToken", newRefresh)))
                .andExpect(status().isOk());
    }

    @Test
    void refreshComTokenInvalidoRetorna400() throws Exception {
        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie("refreshToken", "token-invalido")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void refreshSemCookieRetorna400() throws Exception {
        mockMvc.perform(post("/api/auth/refresh"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void logoutRevogaRefreshToken() throws Exception {
        String refresh = doLogin();

        mockMvc.perform(post("/api/auth/logout")
                        .cookie(new Cookie("refreshToken", refresh)))
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge("refreshToken", 0));

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie("refreshToken", refresh)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void logoutAllRevogaTodasAsSessoes() throws Exception {
        String sessao1 = doLogin();
        String sessao2 = doLogin();

        // Logout-all autenticado via access token
        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"login":"tester","senha":"senha123"}"""))
                .andReturn();
        String accessToken = com.jayway.jsonpath.JsonPath.read(
                login.getResponse().getContentAsString(), "$.accessToken");

        mockMvc.perform(post("/api/auth/logout-all")
                        .header("Authorization", "Bearer " + accessToken)
                        .cookie(new Cookie("refreshToken", sessao2)))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie("refreshToken", sessao1)))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie("refreshToken", sessao2)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rateLimitDesabilitadoEmTesteDeFluxo() throws Exception {
        for (int i = 0; i < 25; i++) {
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"login":"inexistente","senha":"x"}"""))
                    .andExpect(status().isBadRequest());
        }
    }
}
