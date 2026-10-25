package br.edu.superapi;

import java.awt.Rectangle;

public final class ScoreTests {
    private static int checks;
    private static void check(boolean ok, String message) { checks++; if (!ok) throw new AssertionError(message); }
    private static GameModel flat() {
        GameModel m = new GameModel(42); m.enemies.clear(); m.collectibles.clear(); m.hearts.clear();
        m.platforms.clear(); m.platforms.add(new Rectangle(0,490,GameModel.WIDTH,100));
        m.first.x = 1000; m.first.y = 452; m.first.grounded = true; m.invincible = 0; return m;
    }
    private static GameModel.Enemy enemy(int x) { return new GameModel.Enemy(x,x+1,462,0,GameModel.EnemyKind.CRAWLER); }
    private static void finish(GameModel m) {
        m.first.x = GameModel.TERMINAL_X; m.first.y = 452; m.completeApi();
        m.first.x = GameModel.LOGIC_X; check(m.submitLogic(m.logicChallenge().answer()), "Lógica concluída");
        m.first.x = GameModel.EXIT_X + 1; m.tick(false,false,false);
    }
    public static void run() {
        check(GameModel.timeBonus(0)==6000 && GameModel.timeBonus(1)==5990 && GameModel.timeBonus(60)==5990
                && GameModel.timeBonus(61)==5980, "Bônus arredonda segundos para cima");
        check(GameModel.timeBonus(36000)==0 && GameModel.timeBonus(Long.MAX_VALUE)==0, "Bônus nunca negativo");
        GameModel m = flat(); m.enemies.add(enemy(1000)); m.first.y=424; m.first.vy=2;
        m.tick(false,false,false); check(m.score()==100 && m.defeatedEnemies()==1, "Salto concede 100 pontos");
        m.first.explorer=Explorer.NITA; m.activateAbility(0); check(m.score()==100, "Inimigo morto não pontua novamente");
        m.first.y=700; m.tick(false,false,false); check(m.score()==100 && m.totalTicks()==2 && m.phaseTicks()==2, "Morte preserva pontuação e tempo");
        m=flat(); m.first.explorer=Explorer.NITA; m.enemies.add(enemy(1000)); m.enemies.add(enemy(1200)); m.enemies.add(enemy(3000));
        m.activateAbility(0); check(m.score()==200, "Explosão pontua cada alvo visível");
        m=flat(); m.cooperative=true; m.second.x=1040; m.second.y=452; m.second.explorer=Explorer.NITA; m.first.explorer=Explorer.LEO;
        m.enemies.add(enemy(1200)); m.activateAbility(0); m.activateAbility(1);
        for(int i=0;i<30;i++) m.tick(false,false,false);
        check(m.score()==100 && m.balls.isEmpty(), "Dois ataques no mesmo alvo não duplicam pontos");
        m=flat(); m.first.explorer=Explorer.LEO; m.enemies.add(enemy(1200)); m.enemies.add(enemy(1300)); m.activateAbility(0);
        for(int i=0;i<40;i++) m.tick(false,false,false);
        check(m.enemyPoints()==200, "Bolas pontuam ao atingir o alvo");
        m=flat(); for(int i=0;i<59;i++) m.tick(false,false,false);
        check(m.timePoints()==0, "Não concede bônus antes de concluir a fase");
        finish(m); check(m.level==1 && m.score()==5990 && m.phaseTicks()==0 && m.totalTicks()==60, "Transição soma bônus e reinicia só o relógio da fase");
        check(m.results().size()==1 && m.results().get(0).ticks()==60, "Resumo retém tempo da fase");
        m.enemies.clear(); m.collectibles.clear(); finish(m);
        check(m.results().size()==2 && m.timePoints()==11980, "Bônus acumula entre fases");
        m.level=9; m.load(); m.enemies.clear(); m.collectibles.clear(); finish(m);
        int score=m.score(); long ticks=m.totalTicks();
        for(int i=0;i<100;i++) m.tick(false,false,false);
        check(m.won && m.score()==score && m.totalTicks()==ticks && m.results().size()==3, "Vitória encerra relógio e concede bônus uma vez");
        m.restart(42); check(m.score()==0 && m.totalTicks()==0 && m.phaseTicks()==0 && m.results().isEmpty(), "Nova partida zera placar");
        m=flat(); m.lives=1; m.first.y=700; m.tick(false,false,false); m.tick(false,false,false);
        check(m.gameOver && m.totalTicks()==1 && m.timePoints()==0, "Derrota congela relógio sem bônus de fase incompleta");
        System.out.println("OK: " + checks + " verificações de pontuação.");
    }
}
