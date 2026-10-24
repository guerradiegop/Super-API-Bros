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
            if (!path.equals("/world/1") && !path.equals("/keys") && !path.equals("/bridges") && !path.equals("/weather")
                    && !path.equals("/beacon") && !path.equals("/interference")
                    && !path.equals("/heaters") && !path.equals("/purifier")
                    && !path.equals("/tides") && !path.equals("/reactor")) {
                reply(exchange, 404, "{\"error\":\"Endpoint inexistente. Confira o caminho no contrato.\"}"); return;
            }
            String expected = (path.equals("/bridges") || path.equals("/heaters") || path.equals("/reactor")) ? "POST"
                    : (path.equals("/beacon") || path.equals("/purifier")) ? "PUT"
                    : path.equals("/interference") ? "DELETE" : "GET";
            if (!expected.equals(method)) {
                exchange.getResponseHeaders().set("Allow", expected);
                reply(exchange, 405, "{\"error\":\"Método incorreto. Consulte o cabeçalho Allow.\"}"); return;
            }
            if (path.equals("/world/1")) {
                reply(exchange, 200, "{\"world\":\"Jardim dos endpoints\",\"gate\":\"open\"}");
            } else if (path.equals("/tides")) {
                if (!"mode=low".equals(exchange.getRequestURI().getRawQuery())) {
                    reply(exchange, 400, "{\"error\":\"Envie mode=low.\"}"); return;
                }
                reply(exchange, 200, "{\"tide\":\"low\",\"gate\":\"open\"}");
            } else if (path.equals("/weather")) {
                if (!"region=crystal".equals(exchange.getRequestURI().getRawQuery())) {
                    reply(exchange, 400, "{\"error\":\"Envie region=crystal.\"}"); return;
                }
                reply(exchange, 200, "{\"weather\":\"clear\",\"gate\":\"open\"}");
            } else if (path.equals("/keys") || path.equals("/interference")) {
                if (!"Bearer aluno-api".equals(exchange.getRequestHeaders().getFirst("Authorization"))) {
                    exchange.getResponseHeaders().set("WWW-Authenticate", "Bearer");
                    reply(exchange, 401, "{\"error\":\"Envie Authorization: Bearer aluno-api. Token somente didático.\"}"); return;
                }
                if (path.equals("/interference")) {
                    reply(exchange, 200, "{\"interference\":\"removed\",\"gate\":\"open\"}"); return;
                }
                if (!"color=blue".equals(exchange.getRequestURI().getRawQuery())) {
                    reply(exchange, 400, "{\"error\":\"Esta API espera a query exata color=blue.\"}"); return;
                }
                reply(exchange, 200, "{\"key\":\"blue\",\"gate\":\"open\"}");
            } else {
                if (path.equals("/purifier") && !"Bearer aluno-api".equals(exchange.getRequestHeaders().getFirst("Authorization"))) {
                    exchange.getResponseHeaders().set("WWW-Authenticate", "Bearer");
                    reply(exchange, 401, "{\"error\":\"Credencial de manutenção ausente ou inválida.\"}"); return;
                }
                String type = exchange.getRequestHeaders().getFirst("Content-Type");
                if (type == null || !type.split(";", 2)[0].trim().equalsIgnoreCase("application/json")) {
                    reply(exchange, 415, "{\"error\":\"Envie Content-Type: application/json.\"}"); return;
                }
                byte[] bytes = exchange.getRequestBody().readNBytes(4097);
                if (bytes.length > 4096) { reply(exchange, 413, "{\"error\":\"Corpo excedeu 4096 bytes.\"}"); return; }
                Map<String, String> body;
                try { body = Json.parse(new String(bytes, StandardCharsets.UTF_8)); }
                catch (IllegalArgumentException ex) { reply(exchange, 400, "{\"error\":\"JSON inválido. Use um objeto com valores string.\"}"); return; }
                if (path.equals("/reactor")) {
                    if (!"solar".equals(body.get("cell"))) {
                        reply(exchange, 422, "{\"error\":\"cell deve ser solar.\"}"); return;
                    }
                    exchange.getResponseHeaders().set("Location", "/reactor/demo");
                    reply(exchange, 201, "{\"reactor\":\"online\",\"gate\":\"open\"}"); return;
                }
                if (path.equals("/heaters") || path.equals("/purifier")) {
                    boolean heater = path.equals("/heaters");
                    String required = heater ? "thermal" : "carbon";
                    if (!required.equals(body.get(heater ? "fuel" : "filter"))) {
                        reply(exchange, 422, "{\"error\":\"Confira o combustível ou filtro no protocolo.\"}"); return;
                    }
                    if (heater) exchange.getResponseHeaders().set("Location", "/heaters/demo");
                    reply(exchange, heater ? 201 : 200, heater ? "{\"heat\":\"warm\",\"gate\":\"open\"}"
                            : "{\"air\":\"clean\",\"gate\":\"open\"}"); return;
                }
                if (path.equals("/beacon")) {
                    if (!"on".equals(body.get("signal"))) {
                        reply(exchange, 422, "{\"error\":\"signal deve ser on.\"}"); return;
                    }
                    reply(exchange, 200, "{\"beacon\":\"lit\",\"gate\":\"open\"}"); return;
                }
                if (!"wood".equals(body.get("material"))) {
                    reply(exchange, 422, "{\"error\":\"JSON válido, mas material deve ser wood.\"}"); return;
                }
                exchange.getResponseHeaders().set("Location", "/bridges/demo");
                reply(exchange, 201, "{\"bridge\":\"built\",\"material\":\"wood\",\"gate\":\"open\"}");
            }
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
