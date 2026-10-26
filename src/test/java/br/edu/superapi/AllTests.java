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
    private static void press(GamePanel panel, int key) {
        var down = new java.awt.event.KeyEvent(panel, java.awt.event.KeyEvent.KEY_PRESSED, 0, 0, key, java.awt.event.KeyEvent.CHAR_UNDEFINED);
        var up = new java.awt.event.KeyEvent(panel, java.awt.event.KeyEvent.KEY_RELEASED, 0, 0, key, java.awt.event.KeyEvent.CHAR_UNDEFINED);
        for (var listener : panel.getKeyListeners()) { listener.keyPressed(down); listener.keyReleased(up); }
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
            String[] paths = {"/weather?sensor=remote", "/beacon", "/interference"};
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
            String[] bodies = {"", "", "{\"material\":\"wood\"}", "", "{\"signal\":\"on\"}", "", "{\"fuel\":\"thermal\"}", "{\"filter\":\"carbon\"}", "", "{\"cell\":\"solar\"}"};
            for (int i = 0; i < Lesson.ALL.length; i++) {
                Lesson lesson = Lesson.ALL[i];
                var response = request(client, lesson.method(), lesson.path(), "Bearer aluno-api", "application/json", lesson.requestBody());
                check(lesson.accepts(lesson.method(), lesson.path(), response), "Contrato HTTP " + i);
                for (int other = 0; other < Lesson.ALL.length; other++) if (other != i)
                    check(!Lesson.ALL[other].accepts(lesson.method(), lesson.path(), response), "Sem liberação cruzada");
                String badMethod = lesson.method().equals("GET") ? "POST" : "GET";
                check(request(client, badMethod, lesson.path(), "Bearer aluno-api", "application/json", lesson.requestBody()).statusCode() == 405, "405 para cada contrato");
            }
            check(request(client, "GET", "/tides?mode=high", "", "", "").statusCode() == 400, "Maré inválida");
            check(request(client, "POST", "/reactor", "", "application/json", "{}").statusCode() == 422, "Célula obrigatória");
            check(request(client, "POST", "/reactor", "", "", "{}").statusCode() == 415, "Reator exige JSON");
            check(request(client, "POST", "/reactor", "", "application/json", "{").statusCode() == 400, "Reator rejeita JSON inválido");
            check(request(client, "POST", "/reactor", "", "application/json", " ".repeat(4097)).statusCode() == 413, "Limite do reator");
            check(request(client, "PUT", "/purifier", "", "application/json", bodies[7]).statusCode() == 401, "Purificador autenticado");
            String[] answers = {"8", "4", "5", "4", "falso", "2", "2", "3", "2", "2"};
            java.util.Random zeros = new java.util.Random(0) { @Override public int nextInt(int bound) { return 0; } };
            for (int kind = 0; kind < 10; kind++) {
                var challenge = LogicChallenge.create(kind, zeros);
                check(challenge.accepts(" " + answers[kind] + " "), "Gabarito independente lógica " + kind);
                check(!challenge.accepts("incorreto") && !challenge.accepts(null), "Lógica rejeita entrada inválida");
            }
            check(GameModel.WIDTH == 7344 && GameModel.ROPE_LENGTH == 500, "Dimensões: -15% e corda metade da tela");
            check(Explorer.values().length == 6 && RegionTheme.ALL.length == 10, "Seis personagens e dez regiões");
            java.util.Set<String> campaigns = new java.util.HashSet<>(), puzzles = new java.util.HashSet<>();
            for (int seed = 0; seed < 30; seed++) {
                GameModel game = new GameModel(seed), replay = new GameModel(seed);
                check(java.util.Arrays.equals(game.regionOrder(), replay.regionOrder()), "Seed reproduz ordem");
                campaigns.add(java.util.Arrays.toString(game.regionOrder()));
                java.util.Set<Integer> regions = new java.util.HashSet<>();
                java.util.Set<String> apis = new java.util.HashSet<>(), logicKinds = new java.util.HashSet<>();
                for (int phase = 0; phase < 10; phase++) {
                    game.level = phase; game.load(); replay.level = phase; replay.load();
                    regions.add(game.region()); apis.add(game.apiLesson().path()); logicKinds.add(game.logicChallenge().title()); puzzles.add(game.logicChallenge().prompt());
                    check(game.apiLesson().equals(replay.apiLesson()) && game.logicChallenge().equals(replay.logicChallenge()), "Seed reproduz desafios");
                    check(game.collectibles.size() == 30 && game.hearts.size() == 2, "Moedas e corações de cada região");
                    int expected = game.region() < 6 ? 20 : game.region() == 6 ? 17 : 22;
                    check(game.enemies.size() == expected, "Inimigos aumentados em ao menos 80%");
                    check(game.checkpointCoinsRemaining() == 15, "Quinze moedas antes do checkpoint");
                    check(game.collectibles.stream().allMatch(c -> c.x + c.width < GameModel.LOGIC_X), "Todas as moedas antes da central final");
                    check(GameModel.TERMINAL_X == GameModel.WIDTH / 2, "Checkpoint no meio exato");
                    check(game.platforms.equals(replay.platforms), "Seed reproduz mapa");
                }
                check(regions.size() == 10 && apis.size() == 10 && logicKinds.size() == 10, "Campanha sem repetição de fases ou tipos de desafio");
            }
            check(campaigns.size() > 25 && puzzles.size() > 100, "Variedade real de campanhas e parâmetros");
            GameModel game = new GameModel(123);
            int[] previousOrder = game.regionOrder(); game.restart();
            check(!java.util.Arrays.equals(previousOrder, game.regionOrder()), "Nova expedição muda ordem");
            GameModel outOfOrder = new GameModel(123); outOfOrder.collectibles.clear();
            outOfOrder.first.x = GameModel.LOGIC_X; outOfOrder.first.y = 452;
            check(!outOfOrder.submitLogic(outOfOrder.logicChallenge().answer()), "API obrigatória mesmo com todas as moedas");
            game.restart(123); game.enemies.clear();
            for (int i = 0; i < 40; i++) game.tick(false, false, false);
            check(game.first.grounded && game.first.y == GameModel.FLOOR - 38, "Chão e gravidade");
            game.tick(false, false, true); check(game.first.vy < 0, "Salto");
            double speed = game.first.vy; game.tick(false, false, true); check(game.first.vy > speed, "Sem salto infinito");
            game.first.x = GameModel.GATE_X - 35; game.first.y = 452; game.first.vy = 0;
            for (int i = 0; i < 30; i++) game.tick(false, true, false);
            check(game.first.x <= GameModel.GATE_X - 28, "Portal fechado bloqueia passagem");
            game.first.x = GameModel.TERMINAL_X; game.first.y = 452;
            check(!game.canUseTerminal(0), "Moedas bloqueiam API"); game.completeApi(); check(!game.apiSolved(), "Não resolve central bloqueada");
            game.first.x = GameModel.LOGIC_X;
            check(!game.submitLogic(game.logicChallenge().answer()), "Moedas bloqueiam lógica");
            game.collectibles.removeIf(c -> c.x < GameModel.TERMINAL_X);
            game.first.x = GameModel.TERMINAL_X; game.completeApi();
            check(game.apiSolved() && !game.unlocked() && game.remainingCoins() == 15, "Meia coleta ativa checkpoint sem abrir saída");
            game.first.y = 700; game.tick(false, false, false);
            check(game.first.x == GameModel.TERMINAL_X - 70 && game.apiSolved() && game.remainingCoins() == 15, "Queda retorna ao checkpoint intermediário");
            game.first.x = GameModel.LOGIC_X; game.first.y = 452;
            check(!game.submitLogic(game.logicChallenge().answer()), "Moedas restantes bloqueiam central final");
            game.collectibles.clear();
            check(!game.submitLogic("errado") && !game.unlocked(), "Resposta incorreta não abre passagem");
            check(game.submitLogic(game.logicChallenge().answer()) && game.unlocked(), "Central final libera saída após checkpoint");
            String retained = game.logicChallenge().prompt(); game.first.y = 700; game.tick(false, false, false);
            check(game.apiSolved() && game.logicSolved() && game.remainingCoins() == 0 && game.logicChallenge().prompt().equals(retained), "Morte mantém coleta, centrais e desafio");
            check(game.first.x == GameModel.TERMINAL_X - 70, "Checkpoint nas centrais");
            game.first.x = GameModel.EXIT_X + 1; game.first.y = 452; game.tick(false, false, false);
            check(game.level == 1 && !game.apiSolved() && !game.logicSolved() && game.remainingCoins() == 30, "Nova fase reinicia requisitos");
            // Consume all hearts using collision, never re-create them on respawn.
            game.restart(0); game.enemies.clear();
            for (var heart : new java.util.ArrayList<>(game.hearts)) {
                game.first.x = heart.x; game.first.y = heart.y; game.first.vy = 0; game.tick(false, false, false);
            }
            check(game.lives == 5 && game.hearts.isEmpty(), "Dois corações levam energia de três a cinco");
            game.first.y = 700; game.tick(false, false, false);
            check(game.lives == 4 && game.hearts.isEmpty(), "Corações consumidos não voltam após dano");
            game.restart(0); game.enemies.clear(); game.lives = 7;
            var heart = game.hearts.get(0); game.first.x = heart.x; game.first.y = heart.y;
            game.tick(false, false, false); check(game.lives == 7 && game.hearts.size() == 2, "No teto, coração permanece disponível");
            game.lives = 6; game.tick(false, false, false); check(game.lives == 7 && game.hearts.size() == 1, "Coleta após perder energia");
            // Traverse every elevated route and every gap under multiple seeds and lift phases.
            for (int seed = 0; seed < 10; seed++) for (int phase = 0; phase < 10; phase++)
                for (int block = 0; block < GameModel.BLOCK_COUNT; block++) for (int delay : new int[]{0, 45, 90}) {
                    game.restart(seed); game.level = phase; game.load(); game.enemies.clear();
                    for (int i = 0; i < delay; i++) game.tick(false, false, false);
                    var route = game.sections().get(block);
                    var lower = route.ledges().get(0);
                    game.first.x = lower.x - 85; game.first.y = 452; game.first.vy = 0; game.first.grounded = true;
                    for (int step = 0; step < route.ledges().size(); step++) {
                        var target = route.ledges().get(step);
                        jumpTo(game, target.x + target.width / 2 - 14);
                        check(Math.abs(game.first.y + 38 - target.y) < 1, "Plataforma alcançável: " + game.region() + "/" + block + "/" + step + " seed " + seed + " delay " + delay);
                    }
                    check(game.collectedLevelCoins() == 3, "Todas as moedas da rota alcançáveis");
                    int gap = route.gap().x, width = route.gap().width;
                    game.first.x = gap - 40; game.first.y = 452; game.first.vy = 0; game.first.grounded = true;
                    int energy = game.lives;
                    game.tick(false, true, true); for (int i = 0; i < 42; i++) game.tick(false, true, false);
                    check(game.first.x > gap + width && game.lives == energy, "Fenda atravessável " + game.region() + "/" + block + " seed " + seed);
                }
            // Both explorers climb the generated routes with staggered positions and independent inputs.
            for (int phase = 0; phase < 10; phase++) for (int block = 0; block < GameModel.BLOCK_COUNT; block++) {
                game.restart(99); game.cooperative = true; game.level = phase; game.load(); game.enemies.clear();
                var route = game.sections().get(block);
                var lower = route.ledges().get(0);
                game.first.x = lower.x - 85; game.second.x = lower.x - 80;
                game.first.y = game.second.y = 452; game.first.vy = game.second.vy = 0;
                game.first.grounded = game.second.grounded = true;
                for (int step = 0; step < route.ledges().size(); step++) {
                    var target = route.ledges().get(step);
                    int targetOne = target.x + target.width / 2 - 14, targetTwo = targetOne + 15;
                    game.tick(new GameModel.Input(false, true, true), new GameModel.Input(false, true, true));
                    for (int frame = 0; frame < 65 && (!game.first.grounded || !game.second.grounded); frame++) {
                        game.tick(new GameModel.Input(game.first.x > targetOne + 4, game.first.x < targetOne, false),
                                new GameModel.Input(game.second.x > targetTwo + 4, game.second.x < targetTwo, false));
                        check(game.ropeDistance() <= 500.001, "Corda durante salto da dupla");
                    }
                    check(Math.abs(game.first.y + 38 - target.y) < 1 && Math.abs(game.second.y + 38 - target.y) < 1,
                            "Rota cooperativa alcançável " + game.region() + "/" + block + "/" + step);
                }
                check(game.collectedLevelCoins() == 3, "Moedas compartilhadas sem contagem dupla");
            }
            game.cooperative = false;
            // Co-op physics: independent inputs, strict Euclidean tether, walls, camera and recovery.
            game.restart(41); game.cooperative = true; game.enemies.clear();
            for (int i = 0; i < 40; i++) game.tick(GameModel.Input.NONE, GameModel.Input.NONE);
            double secondX = game.second.x;
            game.tick(new GameModel.Input(false, true, false), new GameModel.Input(false, false, true));
            check(game.first.x > 70 && game.second.x == secondX && game.second.vy < 0, "Controles simultâneos independentes");
            game.platforms.clear(); game.platforms.add(new java.awt.Rectangle(0, 490, GameModel.WIDTH, 100));
            game.first.x = 6000; game.second.x = 5500; game.first.y = game.second.y = 452;
            game.first.vy = game.second.vy = 0;
            for (int i = 0; i < 100; i++) {
                game.tick(new GameModel.Input(false, true, i % 30 == 0), GameModel.Input.NONE);
                check(game.ropeDistance() <= 500.001, "Limite real de comprimento da corda");
                double camera = game.camera();
                check(game.first.x >= camera && game.second.x >= camera && game.first.x + 28 <= camera + 1000 && game.second.x + 28 <= camera + 1000, "Ambos na tela");
            }
            game.first.x = GameModel.GATE_X - 29; game.second.x = game.first.x - 450;
            game.first.y = game.second.y = 452; game.first.vy = game.second.vy = 0;
            for (int i = 0; i < 30; i++) game.tick(new GameModel.Input(false, true, false), new GameModel.Input(true, false, false));
            check(game.first.x <= GameModel.GATE_X - 28 && game.ropeDistance() <= 500.001, "Corda não atravessa portal");
            game.collectibles.clear(); game.first.x = GameModel.TERMINAL_X; game.second.x = GameModel.TERMINAL_X - 400;
            check(!game.canUseTerminal(0), "Dupla precisa estar junto na central");
            game.second.x = GameModel.TERMINAL_X - 50; game.completeApi();
            check(game.apiSolved() && !game.unlocked(), "API compartilhada, lógica ainda pendente");
            game.first.x = GameModel.LOGIC_X; game.second.x = GameModel.LOGIC_X - 50;
            check(game.submitLogic(game.logicChallenge().answer()), "Dupla completa lógica");
            game.first.x = GameModel.EXIT_X + 1; game.second.x = GameModel.EXIT_X - 50; game.tick(false, false, false);
            check(game.level == 0, "Saída aguarda segundo jogador");
            game.second.x = GameModel.EXIT_X + 1; game.tick(false, false, false); check(game.level == 1, "Avanço conjunto");
            game.enemies.clear(); game.first.x = game.second.x = 100; game.first.y = game.second.y = 700;
            int energy = game.lives; game.tick(false, false, false);
            check(game.lives == energy - 1 && game.ropeDistance() <= 500, "Queda conjunta custa apenas uma energia");
            check(game.first.y == 420 && game.second.y == 420, "Respawn recupera os dois");
            game.restart(0); game.enemies.clear(); game.cooperative = true;
            heart = game.hearts.get(0); game.first.x = heart.x - 30; game.second.x = heart.x; game.first.y = game.second.y = heart.y;
            game.tick(false, false, false); check(game.lives == 4 && game.hearts.size() == 1, "P2 coleta coração único para equipe");
            game.restart(0); game.cooperative = true; game.invincible = 0;
            var enemy = game.enemies.get(0);
            game.first.x = enemy.x - 40; game.first.y = 452;
            game.second.x = enemy.x; game.second.y = enemy.bounds().y - 39; game.second.vy = 3;
            game.tick(false, false, false);
            check(!enemy.alive && game.second.vy < 0, "P2 derrota inimigo por cima");
            game.load(); game.invincible = 0; enemy = game.enemies.get(0);
            game.first.x = enemy.x; game.second.x = enemy.x; game.first.y = game.second.y = 452;
            energy = game.lives; game.tick(false, false, false);
            check(game.lives == energy - 1 && game.invincible > 0, "Contato simultâneo não duplica dano");
            // Shared platforms advance once per frame even with two passengers.
            game.restart(0); game.cooperative = true;
            for (int phase = 0; phase < 10; phase++) if (game.regionOrder()[phase] == 6) { game.level = phase; break; }
            game.load(); game.enemies.clear();
            TerrainPlatform lift = (TerrainPlatform)game.platforms.stream().filter(p -> p instanceof TerrainPlatform t && t.kind == TerrainPlatform.Kind.ICE_LIFT).findFirst().orElseThrow();
            game.first.x = lift.x + 15; game.second.x = lift.x + 45;
            game.first.y = game.second.y = lift.y - 38; game.first.grounded = game.second.grounded = true;
            for (int i = 0; i < 40; i++) game.tick(false, false, false);
            check(game.first.y == lift.y - 38 && game.second.y == lift.y - 38, "Elevador carrega os dois");
            for (int phase = 0; phase < 10; phase++) if (game.regionOrder()[phase] == 7) { game.level = phase; break; }
            game.load(); game.enemies.clear();
            TerrainPlatform fragile = (TerrainPlatform)game.platforms.stream().filter(p -> p instanceof TerrainPlatform t && t.kind == TerrainPlatform.Kind.CRUMBLING).findFirst().orElseThrow();
            game.first.x = fragile.x + 15; game.second.x = fragile.x + 45;
            game.first.y = game.second.y = fragile.y - 38; game.first.grounded = game.second.grounded = true;
            for (int i = 0; i < 35; i++) game.tick(false, false, false);
            check(fragile.active() && fragile.cracking(), "Dois passageiros não aceleram rachadura");
            game.tick(false, false, false); check(!fragile.active(), "Placa cede no passo 36");
            game.first.x = 70; game.second.x = 115; game.first.y = game.second.y = 452;
            game.first.vy = game.second.vy = 0;
            for (int i = 0; i < 180; i++) game.tick(false, false, false);
            check(fragile.active(), "Placa retorna após 180 passos");
            // All ten transitions require both central results, including the final region.
            game.restart(5); game.cooperative = false;
            for (int phase = 0; phase < 10; phase++) {
                game.collectibles.clear(); game.enemies.clear(); game.first.x = GameModel.TERMINAL_X; game.first.y = 452;
                game.completeApi(); game.first.x = GameModel.LOGIC_X; check(game.submitLogic(game.logicChallenge().answer()), "Lógica de cada fase");
                game.first.x = GameModel.EXIT_X + 1; game.tick(false, false, false);
                check(game.won == (phase == 9), "Vitória somente após dez fases");
            }
            game.restart(); check(!game.won && game.lives == 3 && game.hearts.size() == 2, "Reinício integral");
            AbilityTests.run();
            ScoreTests.run();
            RouteTests.run();
            ExpansionTests.run(client);
            testUI(client, args.length > 0 && args[0].equals("--render"));
        }
        System.out.println("OK: " + checks + " verificações (HTTP, lógica, mapas, corações, cooperação e seleção).");
    }
    private static void jumpTo(GameModel game, int targetX) {
        game.tick(false, true, true);
        for (int frame = 0; frame < 65 && !game.first.grounded; frame++)
            game.tick(game.first.x > targetX + 4, game.first.x < targetX, false);
    }
    private static void testUI(ApiClient client, boolean render) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                if (render) {
                    var apiView = new CentralViews.Api(Lesson.ALL[7], client.baseUri().toString());
                    apiView.output.setText("PUT /purifier\nHTTP 200 • 12 ms\nContent-Type: application/json\n\n{\"air\":\"clean\",\"gate\":\"open\"}");
                    renderSurface(apiView, 1080, 620, "build/central-api.png");
                    var logicView = new CentralViews.Logic(LogicChallenge.create(3, new java.util.Random(42)));
                    logicView.feedback.setText("Tente novamente. Confira cada etapa da sequência antes de informar o resultado.");
                    renderSurface(logicView, 780, 620, "build/central-logic.png");
                }
                GamePanel panel = new GamePanel(client); panel.setSize(1000, 600);
                var modelField = GamePanel.class.getDeclaredField("model"); modelField.setAccessible(true);
                GameModel model = (GameModel)modelField.get(panel);
                var choosing = GamePanel.class.getDeclaredField("choosing"); choosing.setAccessible(true);
                var started = GamePanel.class.getDeclaredField("started"); started.setAccessible(true);
                check(choosing.getBoolean(panel), "Seleção inicial");
                press(panel, java.awt.event.KeyEvent.VK_M); check(model.cooperative, "M ativa dupla");
                for (int i = 0; i < 6; i++) { press(panel, java.awt.event.KeyEvent.VK_1 + i); check(model.first.explorer == Explorer.values()[i], "Seleção de seis avatares"); }
                press(panel, java.awt.event.KeyEvent.VK_TAB); press(panel, java.awt.event.KeyEvent.VK_5);
                check(model.second.explorer == Explorer.NITA && model.first.explorer == Explorer.VIVI, "Escolhas independentes");
                if (render) render(panel, "build/selection-duo.png");
                press(panel, java.awt.event.KeyEvent.VK_ENTER); check(started.getBoolean(panel) && !choosing.getBoolean(panel), "Confirma dupla");
                boolean mode = model.cooperative; press(panel, java.awt.event.KeyEvent.VK_P); press(panel, java.awt.event.KeyEvent.VK_C);
                press(panel, java.awt.event.KeyEvent.VK_M); check(model.cooperative == mode, "Não muda modo no meio da expedição");
                press(panel, java.awt.event.KeyEvent.VK_ENTER);
                model.enemies.clear();
                var timerField = GamePanel.class.getDeclaredField("timer"); timerField.setAccessible(true);
                var accumulator = GamePanel.class.getDeclaredField("accumulator"); accumulator.setAccessible(true);
                var previous = GamePanel.class.getDeclaredField("previous"); previous.setAccessible(true);
                var keysField = GamePanel.class.getDeclaredField("keys"); keysField.setAccessible(true);
                @SuppressWarnings("unchecked") var keys = (java.util.Set<Integer>)keysField.get(panel);
                model.first.grounded = model.second.grounded = true; model.first.y = model.second.y = 452;
                double firstX = model.first.x, secondX = model.second.x;
                keys.add(java.awt.event.KeyEvent.VK_RIGHT); keys.add(java.awt.event.KeyEvent.VK_A);
                accumulator.setDouble(panel, 1.0 / 60); previous.setLong(panel, System.nanoTime());
                ((javax.swing.Timer)timerField.get(panel)).getActionListeners()[0].actionPerformed(new java.awt.event.ActionEvent(panel, 0, "tick")); keys.clear();
                check(model.first.x > firstX && model.second.x < secondX, "Teclas separadas movem os dois simultaneamente");
                model.first.grounded = model.second.grounded = true;
                model.first.y = model.second.y = 452; model.first.vy = model.second.vy = 0;
                press(panel, java.awt.event.KeyEvent.VK_W);
                accumulator.setDouble(panel, 1.0 / 60); previous.setLong(panel, System.nanoTime());
                ((javax.swing.Timer)timerField.get(panel)).getActionListeners()[0].actionPerformed(new java.awt.event.ActionEvent(panel, 0, "tick"));
                check(model.second.vy < 0 && model.first.vy == 0, "W salta apenas P2");
                press(panel, java.awt.event.KeyEvent.VK_UP);
                accumulator.setDouble(panel, 1.0 / 60); previous.setLong(panel, System.nanoTime());
                ((javax.swing.Timer)timerField.get(panel)).getActionListeners()[0].actionPerformed(new java.awt.event.ActionEvent(panel, 0, "tick"));
                check(model.first.vy < 0, "Seta para cima salta P1");
                press(panel, java.awt.event.KeyEvent.VK_K); press(panel, java.awt.event.KeyEvent.VK_F);
                check(model.first.active(Explorer.VIVI) && model.second.abilityUsed, "K ativa P1 e F ativa P2");
                press(panel, java.awt.event.KeyEvent.VK_P);
                int active = model.first.abilityTicks; long scoreTicks = model.totalTicks();
                accumulator.setDouble(panel, 1.0 / 60); previous.setLong(panel, System.nanoTime());
                ((javax.swing.Timer)timerField.get(panel)).getActionListeners()[0].actionPerformed(new java.awt.event.ActionEvent(panel, 0, "tick"));
                check(model.totalTicks() == scoreTicks && model.first.abilityTicks == active && model.first.abilityUsed, "Pausa congela duração e preserva uso");
                long seed = model.seed(); press(panel, java.awt.event.KeyEvent.VK_N);
                check(model.seed() != seed && choosing.getBoolean(panel) && !started.getBoolean(panel), "N inicia novo sorteio");
                panel.setSize(2560, 1080);
                Explorer beforeClick = model.second.explorer;
                for (var listener : panel.getMouseListeners()) listener.mousePressed(new java.awt.event.MouseEvent(panel,
                        java.awt.event.MouseEvent.MOUSE_PRESSED, 0, 0, 10, 500, 1, false));
                check(model.second.explorer == beforeClick, "Clique na barra preta não altera seleção");
                Viewport viewport = Viewport.fit(2560, 1080);
                for (var listener : panel.getMouseListeners()) listener.mousePressed(new java.awt.event.MouseEvent(panel,
                        java.awt.event.MouseEvent.MOUSE_PRESSED, 0, 0, (int)(viewport.left() + 220 * viewport.scale()),
                        (int)(viewport.top() + 300 * viewport.scale()), 1, false));
                check(model.second.explorer == Explorer.LEO, "Clique proporcional escolhe retrato correto em ultrawide");
                press(panel, java.awt.event.KeyEvent.VK_5);
                if (render) render(panel, "build/selection-ultrawide.png");
                panel.setSize(1000, 600);
                if (render) {
                    press(panel, java.awt.event.KeyEvent.VK_ENTER);
                    model.restart(42);
                    for (int phase = 0; phase < 10; phase++) {
                        model.level = phase; model.load(); model.first.x = 500; model.second.x = 750;
                        render(panel, "build/theme-" + model.region() + ".png");
                    }
                    model.first.x = GameModel.TERMINAL_X; model.second.x = GameModel.TERMINAL_X + 45;
                    model.first.y = model.second.y = 452; model.collectibles.removeIf(c -> c.x < GameModel.TERMINAL_X);
                    render(panel, "build/checkpoint-duo.png"); model.completeApi();
                    model.first.x = GameModel.LOGIC_X - 45; model.second.x = GameModel.LOGIC_X; model.collectibles.clear();
                    render(panel, "build/final-duo.png");
                    model.cooperative = false;
                    for (Explorer explorer : Explorer.values()) {
                        model.restart(42); model.first.explorer = explorer;
                        model.first.x = 700; model.first.y = 350; model.invincible = 0;
                        model.activateAbility(0);
                        for (int i = 0; i < 12; i++) model.tick(false, false, false);
                        render(panel, "build/ability-" + explorer.name() + ".png");
                    }
                }
            } catch (Exception ex) { throw new RuntimeException(ex); }
        });
    }
    private static void layoutTree(java.awt.Container c) {
        c.doLayout(); for (var child : c.getComponents()) if (child instanceof java.awt.Container nested) layoutTree(nested);
    }
    private static void renderSurface(javax.swing.JComponent panel, int width, int height, String path) throws Exception {
        panel.setSize(width, height); layoutTree(panel);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        var g = image.createGraphics(); panel.paint(g); g.dispose(); ImageIO.write(image, "png", new File(path));
    }
    private static void render(GamePanel panel, String path) throws Exception {
        BufferedImage image = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_RGB);
        var g = image.createGraphics(); panel.paint(g); g.dispose(); ImageIO.write(image, "png", new File(path));
    }
}
