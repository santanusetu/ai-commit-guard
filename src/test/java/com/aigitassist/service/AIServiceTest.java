package com.aigitassist.service;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AIServiceTest {

    private HttpServer server;
    private final AtomicReference<String> lastRequestBody = new AtomicReference<>();
    private final AtomicReference<String> lastAuthHeader = new AtomicReference<>();
    private volatile int status = 200;
    private volatile String reply = "{\"choices\":[{\"message\":{\"content\":\"  feat: add login  \\n\"}}]}";

    @BeforeEach
    void startStubServer() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/chat/completions", exchange -> {
            lastRequestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            lastAuthHeader.set(exchange.getRequestHeaders().getFirst("Authorization"));
            byte[] body = reply.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(status, body.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(body);
            }
        });
        server.start();
    }

    @AfterEach
    void stopStubServer() {
        server.stop(0);
    }

    private AIService service() {
        return new AIService("test-key", "test-model",
                "http://127.0.0.1:" + server.getAddress().getPort() + "/v1");
    }

    @Test
    void missingApiKeyFailsFast() {
        assertThrows(IllegalStateException.class, () -> new AIService("", "m", "http://localhost"));
    }

    @Test
    void generatesTrimmedCommitMessageFromConfiguredEndpoint() {
        String message = service().generateCommitMessage("+int x = 1;");

        assertEquals("feat: add login", message);
        assertEquals("Bearer test-key", lastAuthHeader.get());
        assertTrue(lastRequestBody.get().contains("\"model\":\"test-model\""));
        assertTrue(lastRequestBody.get().contains("+int x = 1;"));
    }

    @Test
    void wrapsProviderErrorsWithContext() {
        status = 500;
        reply = "{\"error\":\"boom\"}";
        RuntimeException e = assertThrows(RuntimeException.class,
                () -> service().generateCommitMessage("+int x = 1;"));
        assertTrue(e.getMessage().startsWith("Failed to generate commit message"));
    }
}
