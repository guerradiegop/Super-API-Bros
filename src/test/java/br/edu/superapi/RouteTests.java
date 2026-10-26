package br.edu.superapi;

import java.awt.Rectangle;
import java.util.*;

public final class RouteTests {
    private static int checks;
    private static void check(boolean ok, String message) { checks++; if (!ok) throw new AssertionError(message); }
    public static void run() {
        Set<Integer> counts=new HashSet<>(), gaps=new HashSet<>(), positions=new HashSet<>();
        Set<String> layouts=new HashSet<>();
        for (int seed=0;seed<100;seed++) {
            GameModel m=new GameModel(seed);
            for(int phase=0;phase<10;phase++) {
                m.level=phase;m.load(); int before=0;
                Set<Integer> shapes=new HashSet<>();
                int previous=-1;
                for(var section:m.sections()) {
                    check(section.shape()!=previous,"Sem motivos adjacentes repetidos"); previous=section.shape();shapes.add(section.shape());
                    check(section.coins().size()==3,"Três moedas em cada percurso");
                    counts.add(section.ledges().size());gaps.add(section.gap().width);positions.add(section.ledges().get(0).x);
                    check(section.ledges().get(0).x-85>=before,"Espaço de aproximação após a fenda anterior");before=section.gap().x+section.gap().width;
                    for(Rectangle p:section.ledges()) {
                        check(p.x+p.width<=section.gap().x-40,"Sem teto bloqueando o salto da fenda");
                        check(p.y>=240 && p.y<445,"Altura dentro da área de exploração");
                        check(p.x>0 && p.x+p.width<GameModel.LOGIC_X,"Plataforma antes da central final");
                    }
                    for(Rectangle c:section.coins()) check(c.y>82,"Moeda visível abaixo do HUD");
                }
                check(shapes.size()==6,"Todas as seis famílias presentes");
                check(m.checkpointCoinsRemaining()==15 && m.remainingCoins()==30,"Metades mantêm coleta acessível");
                for(int x:new int[]{70,115,GameModel.TERMINAL_X-70,GameModel.TERMINAL_X+45,GameModel.LOGIC_X,GameModel.EXIT_X})
                    check(m.platforms.stream().anyMatch(p->p.y==GameModel.FLOOR && p.contains(x,GameModel.FLOOR)),"Spawn, centrais e saída em chão seguro");
                for(var enemy:m.enemies) if(enemy.kind!=GameModel.EnemyKind.DRONE && enemy.kind!=GameModel.EnemyKind.FROST_WISP)
                    check(m.platforms.stream().anyMatch(p->!(p instanceof TerrainPlatform) && p.y==enemy.baseY+28 && enemy.min>=p.x && enemy.max+30<=p.x+p.width),"Patrulha apoiada em chão estável");
                layouts.add(m.platforms.toString());
            }
        }
        check(counts.equals(Set.of(3,4,5)) && gaps.size()>20 && positions.size()>100 && layouts.size()==1000,"Diversidade de geometria entre seeds");
        GameModel m=new GameModel(2);m.collectibles.clear();m.first.x=GameModel.TERMINAL_X;m.first.y=600;
        m.completeApi();check(!m.apiSolved(),"Queda não ativa checkpoint");
        m.first.y=452;m.completeApi();check(m.apiSolved(),"Central funciona em altura válida");
        m.first.x=GameModel.LOGIC_X;m.first.y=600;check(!m.submitLogic(m.logicChallenge().answer()),"Queda não libera saída");
        m.cooperative=true;m.first.y=452;m.second.x=m.first.x;m.second.y=600;
        check(!m.canUseTerminal(1),"Parceiro caindo não habilita central");
        System.out.println("OK: "+checks+" verificações de geração e regressões de plataforma.");
    }
}
