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
            String[] methods = {"GET", "PUT", "DELETE"};
            String[] paths = {"/weather?region=crystal", "/beacon", "/interference"};
            for (int i = 0; i < 3; i++) {
                var response = request(client, methods[i], paths[i], "Bearer aluno-api", "application/json", i == 1 ? "{\"signal\":\"on\"}" : "");
                check(Lesson.ALL[i + 3].accepts(methods[i], paths[i], response), "Novo contrato " + i);
                check(!Lesson.ALL[i].accepts(methods[i], paths[i], response), "Sem desbloqueio cruzado");
                check(request(client, "POST", paths[i], "", "", "").statusCode() == 405, "Método novo inválido");
            }
            check(request(client, "GET", "/weather?region=desert", "", "", "").statusCode() == 400, "Sensor inválido");
            check(request(client, "DELETE", "/interference", "", "", "").statusCode() == 401, "Remoção exige credencial");
            check(request(client, "PUT", "/beacon", "", "", "{}").statusCode() == 415, "Farol exige JSON");
            check(request(client, "PUT", "/beacon", "", "application/json", "{").statusCode() == 400, "Farol rejeita JSON quebrado");
            check(request(client, "PUT", "/beacon", "", "application/json", "{}").statusCode() == 422, "Farol exige signal");
            check(request(client, "PUT", "/beacon", "", "application/json", " ".repeat(4097)).statusCode() == 413, "Farol limita corpo");
            GameModel game = new GameModel();
            for (int i = 0; i < 40; i++) game.tick(false, false, false);
            check(game.grounded && game.y == GameModel.FLOOR - 38, "Gravidade e chão");
            game.tick(false, false, true); check(game.vy < 0, "Salto");
            double speed = game.vy; game.tick(false, false, true); check(game.vy > speed, "Sem salto infinito no ar");
            game.x = GameModel.GATE_X - 35; game.y = 452; game.vy = 0;
            for (int i = 0; i < 10; i++) game.tick(false, true, false);
            check(game.x <= GameModel.GATE_X - 28, "Portal bloqueia passagem");
            game.unlocked = true;
            for (int i = 0; i < 10; i++) game.tick(false, true, false);
            check(game.x > GameModel.GATE_X, "Portal liberado");
            game.x = 740; game.y = 452; game.grounded = true; game.vy = 0;
            for (int i = 0; i < 43; i++) game.tick(false, true, i == 0);
            check(game.x > 900 && game.lives == 3, "Buraco é atravessável com salto");
            game.y = 700; game.tick(false, false, false);
            check(game.lives == 2 && game.x == GameModel.TERMINAL_X + 20, "Queda e checkpoint");
            game.x = GameModel.EXIT_X + 10; game.tick(false, false, false);
            check(game.level == 1 && !game.unlocked, "Avanço reseta portal");
            while (!game.won) {
                game.unlocked = true; game.x = GameModel.EXIT_X + 10; game.tick(false, false, false);
            }
            check(game.won && game.level == 5, "Vitória somente após seis regiões");
            check(GameModel.WIDTH == 4800, "Comprimento duplicado");
            for (int region = 0; region < Lesson.ALL.length; region++) {
                game.restart(); game.level = region; game.load();
                check(game.platforms.size() == 15 && game.enemies.size() == 11, "Plataformas e inimigos por região");
                check(game.collectibles.size() == 18, "Dados nas rotas elevadas");
                game.enemies.clear();
                game.x = GameModel.TERMINAL_X; game.y = 452;
                check(game.nearTerminal(), "Central acessível");
                // Both gaps can be crossed with the actual fixed-step physics.
                for (int gap : new int[]{780 + region * 40, 3300 + region * 45}) {
                    game.x = gap - 40; game.y = 452; game.vy = 0; game.grounded = true;
                    int lives = game.lives;
                    for (int step = 0; step < 43; step++) game.tick(false, true, step == 0);
                    check(game.x > gap + 120 && game.lives == lives, "Salto atravessa buraco " + region + " gap=" + gap + " x=" + game.x + " lives=" + game.lives);
                }
                game.load(); game.invincible = 0;
                var enemy = game.enemies.get(5);
                game.x = enemy.x; game.y = enemy.bounds().y - 39; game.vy = 3;
                game.tick(false, false, false);
                check(!enemy.alive && game.vy < 0, "Pisar elimina patrulheiro");
                game.load(); game.invincible = 0;
                enemy = game.enemies.get(5); game.x = enemy.x; game.y = enemy.bounds().y;
                int lives = game.lives; game.tick(false, false, false);
                check(game.lives == lives - 1, "Contato lateral causa dano");
                game.lives = 3;
            }
            game.restart(); game.lives = 1; game.y = 700; game.tick(false, false, false);
            check(game.gameOver, "Fim de jogo");
            game.restart(); check(game.lives == 3 && !game.won && !game.gameOver && game.level == 0, "Reinício");
            for (int region = 0; region < Lesson.ALL.length; region++) {
                game.restart(); game.level = region; game.load(); game.enemies.clear();
                game.coins = 999; // Previous regions' totals cannot unlock this central.
                game.x = GameModel.TERMINAL_X; game.y = 452;
                check(!game.canUseTerminal() && game.remainingCoins() == 18, "Central bloqueada na chegada");
                check(game.collectibles.stream().allMatch(coin -> coin.x + coin.width < GameModel.GATE_X),
                        "Nenhuma moeda atrás do portal fechado");
                var route = new java.util.ArrayList<>(game.collectibles);
                for (int i = 0; i < route.size(); i++) {
                    var coin = route.get(i);
                    game.x = coin.x; game.y = coin.y; game.vy = 0;
                    game.tick(false, false, false); // Collect through real collision handling.
                    check(game.remainingCoins() == 17 - i, "Coleta individual sem duplicação");
                    check(game.centralReady() == (i == route.size() - 1), "Só a última moeda ativa a central");
                }
                check(!game.unlocked, "Coleta não substitui desafio HTTP");
                check(!game.canUseTerminal(), "Central exige proximidade");
                game.x = GameModel.TERMINAL_X; game.y = 452;
                check(game.canUseTerminal() && game.collectedLevelCoins() == 18, "Central liberada após coleta completa");
                game.y = 700; game.tick(false, false, false);
                check(game.remainingCoins() == 0, "Morte preserva moedas da região");
                game.x = GameModel.TERMINAL_X; game.y = 452;
                check(game.canUseTerminal(), "Central permanece ativa após respawn");
                game.unlocked = true; game.x = GameModel.EXIT_X + 10;
                game.tick(false, false, false);
                if (region < Lesson.ALL.length - 1) {
                    check(game.remainingCoins() == 18 && !game.centralReady(), "Nova região exige nova coleta");
                } else check(game.won && !game.canUseTerminal(), "Vitória encerra interação");
                game.restart();
                check(game.coins == 0 && game.collectedLevelCoins() == 0 && !game.centralReady(), "Reinício restaura todas as moedas");
            }
            // Renderização Swing headless para verificar layout da tela inicial.
            if (args.length > 0 && args[0].equals("--render")) SwingUtilities.invokeAndWait(() -> {
                try {
                    GamePanel panel = new GamePanel(client); panel.setSize(1000, 600);
                    BufferedImage image = new BufferedImage(1000, 600, BufferedImage.TYPE_INT_RGB);
                    var graphics = image.createGraphics(); panel.paint(graphics); graphics.dispose();
                    ImageIO.write(image, "png", new File("build/preview.png"));
                    var field = GamePanel.class.getDeclaredField("model"); field.setAccessible(true);
                    var started = GamePanel.class.getDeclaredField("started"); started.setAccessible(true); started.set(panel, true);
                    GameModel rendered = (GameModel)field.get(panel);
                    for (int region = 0; region < Lesson.ALL.length; region++) {
                        rendered.level = region; rendered.load(); rendered.x = GameModel.TERMINAL_X; rendered.y = 452;
                        graphics = image.createGraphics(); panel.paint(graphics); graphics.dispose();
                        ImageIO.write(image, "png", new File("build/region-" + region + ".png"));
                    }
                } catch (Exception ex) { throw new RuntimeException(ex); }
            });
        }
        System.out.println("OK: " + checks + " verificações (HTTP, JSON, física e progressão).");
    }
}
