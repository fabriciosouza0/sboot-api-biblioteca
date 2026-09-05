package com.escola.biblioteca;

import com.escola.biblioteca.dashboard.DashboardBroadcaster;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class DashboardSseIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("biblioteca");

    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @org.springframework.boot.test.web.server.LocalServerPort
    int port;

    @Autowired
    DashboardBroadcaster broadcaster;

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Test
    void streamEnviaSnapshot_eNotificaMudanca_eFechaTunnelNaDesconexao() throws Exception {
        String base = "http://localhost:" + port;
        String accessToken = login(base);

        HttpRequest streamRequest = HttpRequest.newBuilder(URI.create(base + "/api/dashboard/stream"))
                .header("Authorization", "Bearer " + accessToken)
                .header("Accept", "text/event-stream")
                .build();
        HttpResponse<InputStream> response = http.send(streamRequest, HttpResponse.BodyHandlers.ofInputStream());
        assertEquals(200, response.statusCode());

        BlockingQueue<String> events = new LinkedBlockingQueue<>();
        ExecutorService reader = Executors.newSingleThreadExecutor();
        reader.submit(() -> readEvents(response.body(), events));

        String snapshot = events.poll(10, TimeUnit.SECONDS);
        assertNotNull(snapshot, "snapshot inicial não chegou via SSE");
        assertTrue(snapshot.contains("counters"));
        int autoresIniciais = autoresDoEvento(snapshot);

        // Mutação -> broadcast deve notificar a stream
        int novoAutor = autoresIniciais + 1;
        HttpRequest create = HttpRequest.newBuilder(URI.create(base + "/api/autores"))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"nome\":\"Autor SSE " + System.nanoTime() + "\"}"))
                .build();
        assertEquals(201, http.send(create, HttpResponse.BodyHandlers.discarding()).statusCode());

        String updated = events.poll(10, TimeUnit.SECONDS);
        assertNotNull(updated, "evento de atualização não chegou após mutação");
        assertTrue(autoresDoEvento(updated) >= novoAutor, "contador de autores não foi atualizado");

        // Desconexão -> tunnel deve ser fechado e emitter removido do registry
        response.body().close();
        reader.shutdownNow();
        waitFor(() -> broadcaster.activeConnections() == 0, 10_000);
    }

    private String login(String base) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(base + "/api/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"login\":\"000.000.000-00\",\"senha\":\"admin\"}"))
                .build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        return (String) com.jayway.jsonpath.JsonPath.read(response.body(), "$.accessToken");
    }

    private void readEvents(InputStream stream, BlockingQueue<String> events) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            StringBuilder block = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    if (block.toString().contains("data:")) {
                        events.offer(block.toString());
                    }
                    block.setLength(0);
                } else {
                    block.append(line).append("\n");
                }
            }
        } catch (IOException ignored) {
        }
    }

    private int autoresDoEvento(String event) {
        String data = event.lines()
                .filter(l -> l.startsWith("data:"))
                .map(l -> l.substring(l.indexOf(":") + 1).trim())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("evento sem data: " + event));
        Number autores = com.jayway.jsonpath.JsonPath.read(data, "$.autores");
        return autores.intValue();
    }

    private void waitFor(java.util.function.BooleanSupplier condition, long timeoutMs) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            if (condition.getAsBoolean()) {
                return;
            }
            Thread.sleep(200);
        }
        throw new AssertionError("condição não satisfeita em " + timeoutMs + "ms");
    }
}