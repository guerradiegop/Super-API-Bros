package br.edu.superapi;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** API HTTP de verdade, restrita ao computador do aluno. */
public final class AcademyServer implements AutoCloseable {
    private final HttpServer server;
    private final ExecutorService workers = Executors.newFixedThreadPool(2);
    public AcademyServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", this::handle);
        server.setExecutor(workers);
        server.start();
    }
    public URI baseUri() { return URI.create("http://127.0.0.1:" + server.getAddress().getPort()); }
    private void handle(HttpExchange exchange) throws IOException {
        try (exchange) {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();
            Lesson lesson = java.util.Arrays.stream(Lesson.ALL)
                    .filter(l -> URI.create(l.path()).getPath().equals(path)).findFirst().orElse(null);
            if (lesson == null) { reply(exchange, 404, "{\"error\":\"Endpoint inexistente. Confira o protocolo.\"}"); return; }
            if (!lesson.method().equals(method)) {
                exchange.getResponseHeaders().set("Allow", lesson.method());
                reply(exchange, 405, "{\"error\":\"Método incorreto. Consulte Allow.\"}"); return;
            }
            if (lesson.authenticated() && !"Bearer aluno-api".equals(exchange.getRequestHeaders().getFirst("Authorization"))) {
                exchange.getResponseHeaders().set("WWW-Authenticate", "Bearer");
                reply(exchange, 401, "{\"error\":\"Envie Authorization: Bearer aluno-api.\"}"); return;
            }
            if (!java.util.Objects.equals(URI.create(lesson.path()).getRawQuery(), exchange.getRequestURI().getRawQuery())) {
                reply(exchange, 400, "{\"error\":\"Confira a query no protocolo.\"}"); return;
            }
            if (!lesson.requestBody().isEmpty()) {
                String type = exchange.getRequestHeaders().getFirst("Content-Type");
                if (type == null || !type.split(";", 2)[0].trim().equalsIgnoreCase("application/json")) {
                    reply(exchange, 415, "{\"error\":\"Envie Content-Type: application/json.\"}"); return;
                }
                byte[] bytes = exchange.getRequestBody().readNBytes(4097);
                if (bytes.length > 4096) { reply(exchange, 413, "{\"error\":\"Corpo excedeu 4096 bytes.\"}"); return; }
                Map<String, String> body;
                try { body = Json.parse(new String(bytes, StandardCharsets.UTF_8)); }
                catch (IllegalArgumentException ex) { reply(exchange, 400, "{\"error\":\"JSON inválido. Use valores string.\"}"); return; }
                if (!body.entrySet().containsAll(Json.parse(lesson.requestBody()).entrySet())) {
                    reply(exchange, 422, "{\"error\":\"Confira os valores do corpo no protocolo.\"}"); return;
                }
            }
            if (lesson.status() == 201) exchange.getResponseHeaders().set("Location", path + "/demo");
            String extra = path.equals("/bridges") ? ",\"material\":\"wood\"" : "";
            reply(exchange, lesson.status(), "{\"" + lesson.field() + "\":\"" + lesson.answer() + "\",\"gate\":\"open\"" + extra + "}");
        }
    }
    private void reply(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
    }
    @Override public void close() { server.stop(0); workers.shutdownNow(); }
}
