package br.edu.superapi;

import java.awt.Rectangle;
import java.util.List;

/** Contract tests for effects, expiry, scope, shared resources and pixel-perfect viewport mapping. */
public final class AbilityTests {
    private static int checks;
    private static void check(boolean condition, String description) {
        checks++; if (!condition) throw new AssertionError(description);
    }
    private static GameModel flat(Explorer explorer) {
        GameModel model = new GameModel(0); model.first.explorer = explorer;
        model.platforms.clear(); model.platforms.add(new Rectangle(0, 490, GameModel.WIDTH, 100));
        model.collectibles.clear(); model.hearts.clear(); model.enemies.clear();
        model.first.x = 1000; model.first.y = 452; model.first.vy = 0; model.first.grounded = true; model.invincible = 0;
        return model;
    }
    private static GameModel.Enemy enemy(int x, int y) {
        return new GameModel.Enemy(x, x + 1, y, 0, GameModel.EnemyKind.CRAWLER);
    }
    public static void run() {
        GameModel m = flat(Explorer.NITA);
        var inside = enemy(1000, 462); var edge = enemy(659, 462); var outside = enemy(2000, 462); var above = enemy(1000, 20);
        m.enemies.addAll(List.of(inside, edge, outside, above));
        check(m.activateAbility(0) && !inside.alive && !edge.alive && outside.alive && above.alive, "Explosão afeta exatamente inimigos visíveis");
        check(!m.activateAbility(0), "Uso único bloqueia explosão repetida");
        m = flat(Explorer.NITA); m.cooperative = true; m.second.x = 1450; m.second.y = 452;
        inside = enemy(1700, 462); outside = enemy(650, 462); m.enemies.addAll(List.of(inside, outside));
        m.activateAbility(0); check(!inside.alive && outside.alive, "Explosão usa a câmera compartilhada na dupla");
        m = flat(Explorer.DILA); m.lives = 6;
        m.collectibles.add(new Rectangle(1200, 300, 18, 22)); m.collectibles.add(new Rectangle(2000, 300, 18, 22));
        m.hearts.add(new Rectangle(1300, 400, 24, 24)); m.hearts.add(new Rectangle(1400, 400, 24, 24)); m.hearts.add(new Rectangle(2100, 400, 24, 24));
        check(m.activateAbility(0) && m.coins == 1 && m.collectibles.size() == 1, "Ímã coleta apenas moedas visíveis");
        check(m.lives == 7 && m.hearts.size() == 2, "Ímã respeita teto e preserva corações excedentes");
        m.first.y = 700; m.tick(false, false, false);
        check(m.hearts.size() == 2, "Coração do ímã não reaparece após queda");
        m = flat(Explorer.NEY); check(m.activateAbility(0), "Ativa velocidade");
        double x = m.first.x; m.tick(false, true, false);
        check(Math.abs(m.first.x - x - 11) < .00001, "+150% equivale a 2,5 vezes a velocidade");
        GameModel normal = flat(Explorer.NEY), fast = flat(Explorer.NEY); fast.activateAbility(0);
        normal.tick(false, true, true); fast.tick(false, true, true);
        for (int i = 0; i < 43; i++) { normal.tick(false, true, false); fast.tick(false, true, false); }
        check(Math.abs((fast.first.x - 1000) / (normal.first.x - 1000) - 2.5) < .00001, "Alcance horizontal do salto +150%");
        m = flat(Explorer.NEY); m.activateAbility(0);
        for (int i = 0; i < 599; i++) m.tick(false, false, false);
        check(m.first.abilityTicks == 1 && m.first.active(Explorer.NEY), "Velocidade dura 599 passos ainda ativa");
        m.tick(false, false, false); check(!m.first.active(Explorer.NEY), "Velocidade termina aos dez segundos");
        x = m.first.x; m.tick(false, true, false); check(Math.abs(m.first.x - x - 4.4) < .00001, "Velocidade normal restaurada");
        for (int i = 0; i < 599; i++) m.tick(false, false, false);
        check(m.first.abilityUsed && !m.activateAbility(0), "Tempo não restaura uso nesta fase");
        m.selectExplorer(0, Explorer.CRIS.ordinal()); check(m.first.abilityTicks == 0 && !m.activateAbility(0), "Troca cancela efeito sem burlar uso único");
        m = flat(Explorer.CRIS); m.activateAbility(0); m.enemies.add(enemy(1000, 462));
        m.tick(false, false, false); check(m.lives == 3, "Cris imune a contato lateral");
        m.first.y = 700; m.tick(false, false, false);
        check(m.lives == 3 && m.first.y == 420 && m.first.active(Explorer.CRIS), "Cris resgatado de queda sem perder energia ou efeito");
        m.enemies.clear();
        for (int i = 0; i < 298; i++) m.tick(false, false, false);
        check(!m.first.active(Explorer.CRIS), "Proteção termina aos cinco segundos");
        m.invincible = 0; m.first.y = 700; m.tick(false, false, false); check(m.lives == 2, "Queda após efeito causa dano");
        m = flat(Explorer.CRIS); m.first.x = GameModel.TERMINAL_X; m.completeApi(); m.activateAbility(0);
        m.first.y = 700; m.tick(false, false, false);
        check(m.lives == 3 && m.apiSolved() && m.first.x == GameModel.TERMINAL_X - 70, "Cris resgatado no checkpoint ativo");
        m = flat(Explorer.VIVI); m.activateAbility(0);
        double y = m.first.y;
        m.tick(new GameModel.Input(false, false, false, true, false), GameModel.Input.NONE);
        check(m.first.y < y, "Vivi sobe com controle de voo");
        y = m.first.y; m.tick(GameModel.Input.NONE, GameModel.Input.NONE); check(m.first.y == y, "Jatinho paira sem entrada vertical");
        m.tick(new GameModel.Input(false, false, false, false, true), GameModel.Input.NONE); check(m.first.y > y, "Vivi desce com controle");
        for (int i = 0; i < 100; i++) m.tick(new GameModel.Input(false, false, false, true, false), GameModel.Input.NONE);
        check(m.first.y == 90, "Voo limitado ao teto visível");
        for (int i = 0; i < 497; i++) m.tick(GameModel.Input.NONE, GameModel.Input.NONE);
        check(!m.first.active(Explorer.VIVI), "Voo termina após dez segundos");
        y = m.first.y; m.tick(false, false, false); check(m.first.y > y, "Gravidade restaurada após voo");
        m = flat(Explorer.VIVI); m.activateAbility(0);
        m.platforms.add(new Rectangle(995, 380, 100, 22));
        for (int i = 0; i < 30; i++) m.tick(new GameModel.Input(false, false, false, true, false), GameModel.Input.NONE);
        check(m.first.y >= 402, "Jatinho não atravessa plataformas");
        m = flat(Explorer.LEO);
        for (int pos : new int[]{3000, 1700, 1600, 1500, 1400, 1300}) m.enemies.add(enemy(pos, 462));
        var farthest = m.enemies.get(0); m.activateAbility(0);
        check(m.balls.size() == 5 && m.balls.stream().noneMatch(ball -> ball.target == farthest), "Leo escolhe os cinco inimigos mais próximos");
        for (int i = 0; i < 120; i++) m.tick(false, false, false);
        check(m.enemies.stream().filter(e -> !e.alive).count() == 5 && farthest.alive && m.balls.isEmpty(), "Cinco bolas atingem cinco alvos distintos");
        m = flat(Explorer.LEO); m.enemies.add(enemy(1200, 462)); m.activateAbility(0);
        check(m.balls.size() == 1, "Menos de cinco inimigos usa apenas os alvos disponíveis");
        m = flat(Explorer.VIVI); m.cooperative = true; m.second.x = 1500; m.second.y = 452; m.second.grounded = true;
        m.activateAbility(0);
        for (int i = 0; i < 90; i++) {
            m.tick(new GameModel.Input(true, false, false, true, false), GameModel.Input.NONE);
            check(m.ropeDistance() <= 500.001, "Voo respeita corda");
        }
        m = flat(Explorer.NEY); m.cooperative = true; m.second.x = 500; m.second.y = 452; m.second.grounded = true;
        m.activateAbility(0); m.second.explorer = Explorer.CRIS; check(m.activateAbility(1), "Uso únicos independentes na dupla");
        for (int i = 0; i < 80; i++) { m.tick(false, true, false); check(m.ropeDistance() <= 500.001, "Velocidade respeita corda"); }
        m = flat(Explorer.NITA); m.gameOver = true; check(!m.activateAbility(0), "Sem habilidade após derrota");
        m.gameOver = false; check(!m.activateAbility(1), "Solo não ativa personagem ausente");
        m.activateAbility(0); m.load(); check(!m.first.abilityUsed && m.balls.isEmpty(), "Nova fase restaura habilidades e remove projéteis");
        for (Explorer explorer : Explorer.values()) {
            m = flat(explorer); check(m.activateAbility(0), "Primeiro uso de " + explorer);
            m.first.abilityTicks = 0; m.first.y = 700; m.tick(false, false, false);
            check(m.first.abilityUsed && !m.activateAbility(0), "Morte mantém uso de " + explorer);
            m.first.x = GameModel.TERMINAL_X; m.first.y = 452; m.completeApi();
            m.first.y = 700; m.tick(false, false, false);
            check(m.apiSolved() && !m.activateAbility(0), "Checkpoint não restaura especial");
            m.first.x = GameModel.LOGIC_X; m.first.y = 452; m.submitLogic(m.logicChallenge().answer());
            m.first.x = GameModel.EXIT_X + 1; m.tick(false, false, false);
            check(m.level == 1 && !m.first.abilityUsed && m.activateAbility(0), "Próxima fase restaura especial");
        }
        for (int[] size : new int[][]{{1000,600},{1920,1080},{2560,1080},{800,1200},{640,420}}) {
            Viewport v = Viewport.fit(size[0], size[1]);
            check(1000 * v.scale() <= size[0] + .01 && 600 * v.scale() <= size[1] + .01, "Canvas cabe sem distorção");
            var point = v.toLogical((int)(v.left() + 500 * v.scale()), (int)(v.top() + 300 * v.scale()));
            check(point != null && Math.abs(point.x - 500) <= 1 && Math.abs(point.y - 300) <= 1, "Cliques convertidos com proporção preservada");
        }
        check(Viewport.fit(2560,1080).toLogical(0, 500) == null, "Barras laterais não acionam interface");
        check(Viewport.fit(800,1200).toLogical(300, 0) == null, "Barras superior/inferior não acionam interface");
        System.out.println("OK: " + checks + " verificações de habilidades e proporções.");
    }
}
