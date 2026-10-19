package br.edu.superapi;

import java.net.http.HttpResponse;
import java.util.concurrent.TimeUnit;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import javax.swing.SwingUtilities;

/** Testes de integração reais, sem bibliotecas ou acesso à internet. */
public final class AllTests {
    private static int checks;
    private static void check(boolean ok, String description) {
        checks++;
        if (!ok) throw new AssertionError(description);
    }
    private static HttpResponse<String> request(ApiClient client, String method, String path, String auth, String type, String body) throws Exception {
        return client.send(method, path, auth, type, body).get(7, TimeUnit.SECONDS);
    }
    public static void main(String[] args) throws Exception {
        try (AcademyServer server = new AcademyServer()) {
            ApiClient client = new ApiClient(server.baseUri());
            HttpResponse<String> world = request(client, "GET", "/world/1", "", "", "");
            check(world.statusCode() == 200, "GET retorna 200");
            check(Lesson.ALL[0].accepts("GET", "/world/1", world), "Fase 1 aceita o contrato");
            check(!Lesson.ALL[1].accepts("GET", "/world/1", world), "Outra missão não pode ser liberada");
            check(request(client, "GET", "/missing", "", "", "").statusCode() == 404, "404");
            check(request(client, "POST", "/world/1", "", "", "").statusCode() == 405, "405");
            check(request(client, "GET", "/keys?color=blue", "", "", "").statusCode() == 401, "401");
            check(request(client, "GET", "/keys?color=red", "Bearer aluno-api", "", "").statusCode() == 400, "Query inválida");
            HttpResponse<String> key = request(client, "GET", "/keys?color=blue", "Bearer aluno-api", "", "");
            check(Lesson.ALL[1].accepts("GET", "/keys?color=blue", key), "Fase 2");
            check(request(client, "POST", "/bridges", "", "", "{}").statusCode() == 415, "415");
            check(request(client, "POST", "/bridges", "", "application/json", "{").statusCode() == 400, "JSON malformado");
            check(request(client, "POST", "/bridges", "", "application/json", "{\"material\":\"stone\"}").statusCode() == 422, "422");
            check(request(client, "POST", "/bridges", "", "application/json", " ".repeat(4097)).statusCode() == 413, "Corpo limitado");
            HttpResponse<String> bridge = request(client, "POST", "/bridges", "", "application/json", "{ \"material\" : \"wood\" }");
            check(Lesson.ALL[2].accepts("POST", "/bridges", bridge), "Fase 3 / 201");
            check(bridge.headers().firstValue("Location").orElse("").equals("/bridges/demo"), "Location");
            try { client.send("GET", "https://example.com/", "", "", ""); throw new AssertionError("URL externa aceita"); }
            catch (IllegalArgumentException expected) { checks++; }
            check(Json.parse("{\"a\":\"olá\\nAPI\"}").get("a").equals("olá\nAPI"), "Escape e UTF-8");
            for (String bad : new String[]{"{}x", "{\"a\":1}", "{\"a\":\"x\",}", "{\"a\":\"x\",\"a\":\"y\"}"}) {
                try { Json.parse(bad); throw new AssertionError("JSON inválido aceito"); }
                catch (IllegalArgumentException expected) { checks++; }
            }
            GameModel game = new GameModel();
            for (int i = 0; i < 40; i++) game.tick(false, false, false);
            check(game.grounded && game.y == GameModel.FLOOR - 38, "Gravidade e chão");
            game.tick(false, false, true); check(game.vy < 0, "Salto");
            double speed = game.vy; game.tick(false, false, true); check(game.vy > speed, "Sem salto infinito no ar");
            game.x = 1465; game.y = 452; game.vy = 0;
            for (int i = 0; i < 20; i++) game.tick(false, true, false);
            check(game.x <= GameModel.GATE_X - 28, "Portal bloqueia passagem");
            game.unlocked = true;
            for (int i = 0; i < 20; i++) game.tick(false, true, false);
            check(game.x > GameModel.GATE_X, "Portal liberado");
            game.x = 740; game.y = 452; game.grounded = true; game.vy = 0;
            for (int i = 0; i < 43; i++) game.tick(false, true, i == 0);
            check(game.x > 900 && game.lives == 3, "Buraco é atravessável com salto");
            game.y = 700; game.tick(false, false, false);
            check(game.lives == 2 && game.x == 1390, "Queda e checkpoint");
            game.x = 2300; game.tick(false, false, false);
            check(game.level == 1 && !game.unlocked, "Avanço reseta portal");
            game.unlocked = true; game.x = 2300; game.tick(false, false, false);
            game.unlocked = true; game.x = 2300; game.tick(false, false, false);
            check(game.won, "Vitória nas três fases");
            game.restart(); game.lives = 1; game.y = 700; game.tick(false, false, false);
            check(game.gameOver, "Fim de jogo");
            game.restart(); check(game.lives == 3 && !game.won && !game.gameOver && game.level == 0, "Reinício");
            // Renderização Swing headless para verificar layout da tela inicial.
            if (args.length > 0 && args[0].equals("--render")) SwingUtilities.invokeAndWait(() -> {
                try {
                    GamePanel panel = new GamePanel(client); panel.setSize(1000, 600);
                    BufferedImage image = new BufferedImage(1000, 600, BufferedImage.TYPE_INT_RGB);
                    var graphics = image.createGraphics(); panel.paint(graphics); graphics.dispose();
                    ImageIO.write(image, "png", new File("build/preview.png"));
                } catch (Exception ex) { throw new RuntimeException(ex); }
            });
        }
        System.out.println("OK: " + checks + " verificações (HTTP, JSON, física e progressão).");
    }
}
