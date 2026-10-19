package br.edu.superapi;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/** Ponto de partida para os exercícios de consumo de APIs em Java. */
public final class ApiClient {
    private final URI base;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
    public ApiClient(URI base) { this.base = base; }
    public URI baseUri() { return base; }
    public CompletableFuture<HttpResponse<String>> send(String method, String path, String token, String type, String body) {
        URI target = base.resolve(path);
        // O laboratório só chama a API local; não aceita URLs externas ou redirecionamentos.
        if (!target.getScheme().equals(base.getScheme()) || !target.getAuthority().equals(base.getAuthority())
                || target.getFragment() != null || !path.startsWith("/")) {
            throw new IllegalArgumentException("Use um caminho local iniciado por /.");
        }
        HttpRequest.Builder request = HttpRequest.newBuilder(target).timeout(Duration.ofSeconds(5))
                .header("Accept", "application/json");
        if (!token.isBlank()) request.header("Authorization", token);
        if (!type.isBlank()) request.header("Content-Type", type);
        request.method(method, body.isEmpty() ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
        return http.sendAsync(request.build(), HttpResponse.BodyHandlers.ofString());
    }
}
