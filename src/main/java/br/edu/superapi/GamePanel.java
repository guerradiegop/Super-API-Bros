package br.edu.superapi;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Set;

public final class GamePanel extends JPanel {
    private final GameModel model = new GameModel();
    private final ApiClient api;
    private final Set<Integer> keys = new HashSet<>();
    private boolean started, paused, labOpen, choosing = true, jumpOne, jumpTwo;
    private int selectionPlayer;
    private WindowMode windowMode;
    public void setWindowMode(WindowMode mode) { windowMode = mode; }
    private final Timer timer;
    private long previous = System.nanoTime();
    private double accumulator;
    public GamePanel(ApiClient api) {
        this.api = api;
        setPreferredSize(new Dimension(1000, 600)); setFocusable(true); setBackground(Color.BLACK);
        addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent event) {
                int key = event.getKeyCode(); if (!keys.add(key)) return;
                if (windowMode != null && (key == KeyEvent.VK_F11 || key == KeyEvent.VK_ESCAPE && windowMode.isFullscreen())) {
                    clearInput(); if (started) paused = true;
                    windowMode.toggle(); return;
                }
                if (key == KeyEvent.VK_C && !labOpen && (!started || paused || model.won || model.gameOver)) {
                    choosing = true; clearInput(); repaint(); return;
                }
                if (choosing) {
                    if (key == KeyEvent.VK_M && (!started || model.won || model.gameOver)) toggleMode();
                    if (key == KeyEvent.VK_TAB && model.cooperative) selectionPlayer = 1 - selectionPlayer;
                    if (key >= KeyEvent.VK_1 && key <= KeyEvent.VK_6) model.selectExplorer(selectionPlayer, key - KeyEvent.VK_1);
                    if (key == KeyEvent.VK_LEFT) model.selectExplorer(0, model.first.explorer.ordinal() - 1);
                    if (key == KeyEvent.VK_RIGHT) model.selectExplorer(0, model.first.explorer.ordinal() + 1);
                    if (key == KeyEvent.VK_A) model.selectExplorer(model.cooperative ? 1 : 0,
                            (model.cooperative ? model.second : model.first).explorer.ordinal() - 1);
                    if (key == KeyEvent.VK_D) model.selectExplorer(model.cooperative ? 1 : 0,
                            (model.cooperative ? model.second : model.first).explorer.ordinal() + 1);
                    if (key == KeyEvent.VK_ENTER) confirmExplorer();
                    if (key == KeyEvent.VK_ESCAPE && started) choosing = false;
                    repaint(); return;
                }
                if (started && !paused && !labOpen && (key == KeyEvent.VK_K || key == KeyEvent.VK_F)) {
                    model.activateAbility(key == KeyEvent.VK_F && model.cooperative ? 1 : 0); repaint(); return;
                }
                if (key == KeyEvent.VK_UP || !model.cooperative && (key == KeyEvent.VK_W || key == KeyEvent.VK_SPACE)) jumpOne = true;
                if (model.cooperative && key == KeyEvent.VK_W) jumpTwo = true;
                if (key == KeyEvent.VK_P && started && !labOpen) { paused = !paused; clearInput(); }
                if (key == KeyEvent.VK_N && paused && !labOpen) { model.restart(); started = false; choosing = true; clearInput(); }
                if (key == KeyEvent.VK_R && (model.won || model.gameOver)) { model.restart(); started = true; paused = false; clearInput(); }
                if ((key == KeyEvent.VK_E || key == KeyEvent.VK_ENTER) && started && !paused && !labOpen) openCentral();
            }
            @Override public void keyReleased(KeyEvent event) { keys.remove(event.getKeyCode()); }
        });
        setFocusTraversalKeysEnabled(false);
        addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent event) {
                if (!choosing) return;
                Point logical = Viewport.fit(getWidth(), getHeight()).toLogical(event.getX(), event.getY());
                if (logical == null) return;
                int mx = logical.x, my = logical.y;
                for (int i = 0; i < Explorer.values().length; i++)
                    if (new Rectangle(25 + i * 160, 225, 150, 215).contains(mx, my)) model.selectExplorer(selectionPlayer, i);
                if (new Rectangle(25, 158, 265, 37).contains(mx, my) && (!started || model.won || model.gameOver)) toggleMode();
                if (model.cooperative && new Rectangle(315, 158, 380, 37).contains(mx, my)) selectionPlayer = 1 - selectionPlayer;
                if (new Rectangle(300, 475, 400, 42).contains(mx, my)) confirmExplorer();
                requestFocusInWindow(); repaint();
            }
        });
        addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) { clearInput(); if (started && !labOpen) paused = true; }
        });
        timer = new Timer(16, event -> {
            long now = System.nanoTime(); accumulator += Math.min((now - previous) / 1_000_000_000.0, .1); previous = now;
            while (accumulator >= 1.0 / 60) {
                if (started && !paused && !labOpen && !choosing) {
                    model.tick(new GameModel.Input(keys.contains(KeyEvent.VK_LEFT) || !model.cooperative && keys.contains(KeyEvent.VK_A),
                                    keys.contains(KeyEvent.VK_RIGHT) || !model.cooperative && keys.contains(KeyEvent.VK_D), jumpOne,
                                    keys.contains(KeyEvent.VK_UP) || !model.cooperative && (keys.contains(KeyEvent.VK_W) || keys.contains(KeyEvent.VK_SPACE)),
                                    keys.contains(KeyEvent.VK_DOWN) || !model.cooperative && keys.contains(KeyEvent.VK_S)),
                            new GameModel.Input(keys.contains(KeyEvent.VK_A), keys.contains(KeyEvent.VK_D), jumpTwo, keys.contains(KeyEvent.VK_W), keys.contains(KeyEvent.VK_S)));
                }
                jumpOne = jumpTwo = false; accumulator -= 1.0 / 60;
            }
            repaint();
        });
    }
    private void clearInput() { keys.clear(); jumpOne = jumpTwo = false; }
    private void toggleMode() { model.cooperative = !model.cooperative; selectionPlayer = 0; }
    @Override public void addNotify() { super.addNotify(); previous = System.nanoTime(); timer.start(); }
    @Override public void removeNotify() { timer.stop(); super.removeNotify(); }
    private void confirmExplorer() {
        if (model.won || model.gameOver) model.restart();
        choosing = false; started = true; paused = false; clearInput();
    }
    private void openCentral() {
        int central = model.canUseTerminal(0) && !model.apiSolved() ? 0
                : model.canUseTerminal(1) && !model.logicSolved() ? 1 : -1;
        if (central < 0) return;
        labOpen = true; clearInput();
        if (central == 0) {
            LabDialog dialog = new LabDialog(SwingUtilities.getWindowAncestor(this), api, model.apiLesson());
            dialog.setVisible(true); if (dialog.solved()) model.completeApi();
        } else {
            LogicDialog dialog = new LogicDialog(SwingUtilities.getWindowAncestor(this), model.logicChallenge());
            dialog.setVisible(true); if (dialog.solved()) model.submitLogic(model.logicChallenge().answer());
        }
        labOpen = false; clearInput(); requestFocusInWindow();
    }
    @Override protected void paintComponent(Graphics original) {
        super.paintComponent(original);
        Graphics2D g = (Graphics2D)original.create();
        Viewport viewport = Viewport.fit(getWidth(), getHeight());
        g.translate(viewport.left(), viewport.top()); g.scale(viewport.scale(), viewport.scale());
        g.clipRect(0, 0, 1000, 600);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        RegionTheme theme = RegionTheme.ALL[model.region()];
        g.setPaint(new GradientPaint(0, 0, theme.sky(), 0, 600, theme.horizon())); g.fillRect(0, 0, 1000, 600);
        int camera = (int)model.camera(); theme.drawBackdrop(g, camera, model.region()); g.translate(-camera, 0);
        for (Rectangle platform : model.platforms) {
            if (platform instanceof TerrainPlatform terrain && !terrain.active()) {
                g.setColor(new Color(220, 159, 88, 80));
                g.drawRect(platform.x, platform.y, platform.width, platform.height); continue;
            }
            g.setColor(theme.stone()); g.fillRect(platform.x, platform.y, platform.width, platform.height);
            g.setColor(theme.accent()); g.fillRect(platform.x, platform.y, platform.width, 7);
            if (platform instanceof TerrainPlatform terrain) {
                g.setColor(terrain.kind == TerrainPlatform.Kind.ICE_LIFT ? Color.WHITE : new Color(255, 173, 72));
                if (terrain.kind == TerrainPlatform.Kind.ICE_LIFT) {
                    g.drawLine(platform.x + 10, platform.y + 12, platform.x + platform.width - 10, platform.y + 12);
                } else {
                    for (int mark = 8; mark < platform.width; mark += 18)
                        g.drawLine(platform.x + mark, platform.y, platform.x + mark + 9, platform.y + (terrain.cracking() ? 20 : 9));
                }
            }
            g.setColor(theme.stone().brighter());
            for (int tx = platform.x; tx < platform.x + platform.width; tx += 32) g.drawRect(tx, platform.y + 8, Math.min(31, platform.x + platform.width - tx - 1), 22);
        }
        for (Rectangle coin : model.collectibles) {
            g.setColor(new Color(255, 209, 93)); g.fillOval(coin.x, coin.y, coin.width, coin.height);
            g.setColor(new Color(171, 110, 38)); g.drawLine(coin.x + 9, coin.y + 5, coin.x + 9, coin.y + 17);
        }
        for (GameModel.Enemy enemy : model.enemies) if (enemy.alive) {
            Rectangle r = enemy.bounds();
            g.setColor(switch (enemy.kind) {
                case CRAWLER -> new Color(240, 101, 116);
                case HOPPER -> new Color(255, 185, 75);
                case DRONE -> new Color(165, 145, 255);
                case SKATER -> new Color(129, 227, 255);
                case FROST_WISP -> new Color(230, 250, 255);
                case SCAVENGER -> new Color(154, 175, 69);
            });
            if (enemy.kind == GameModel.EnemyKind.DRONE) {
                g.fillOval(r.x - 12, r.y + 9, 54, 8);
                g.drawLine(r.x - 5, r.y + 3, r.x + 35, r.y + 3);
            } else if (enemy.kind == GameModel.EnemyKind.HOPPER) {
                g.fillRect(r.x - 3, r.y + 23, 10, 5); g.fillRect(r.x + 23, r.y + 23, 10, 5);
            }
            g.fillRoundRect(r.x, r.y, r.width, r.height, 8, 8);
            if (enemy.kind == GameModel.EnemyKind.SKATER) {
                g.setColor(Color.WHITE); g.fillRect(r.x - 5, r.y + 26, 40, 3);
                g.fillRect(r.x + 7, r.y - 5, 16, 5);
            } else if (enemy.kind == GameModel.EnemyKind.FROST_WISP) {
                g.drawLine(r.x + 15, r.y - 9, r.x + 15, r.y + 36);
                g.drawLine(r.x - 8, r.y + 14, r.x + 38, r.y + 14);
            } else if (enemy.kind == GameModel.EnemyKind.SCAVENGER) {
                g.setColor(new Color(79, 66, 52)); g.fillRect(r.x - 3, r.y + 10, 36, 8);
                g.fillRect(r.x + 9, r.y - 5, 12, 5);
            }
            g.setColor(Color.WHITE); g.fillRect(r.x + 5, r.y + 6, 6, 6); g.fillRect(r.x + 19, r.y + 6, 6, 6);
            g.setColor(new Color(30, 28, 57)); g.fillRect(r.x + 8, r.y + 8, 3, 4); g.fillRect(r.x + 19, r.y + 8, 3, 4);
        }
        for (GameModel.Ball ball : model.balls) {
            g.setColor(Color.WHITE); g.fillOval((int)ball.x - 7, (int)ball.y - 7, 14, 14);
            g.setColor(new Color(30, 40, 55)); g.fillRect((int)ball.x - 2, (int)ball.y - 2, 5, 5);
            g.drawOval((int)ball.x - 7, (int)ball.y - 7, 14, 14);
        }
        for (Rectangle heart : model.hearts) {
            g.setColor(new Color(245, 89, 121));
            g.fillRect(heart.x, heart.y + 3, 10, 10); g.fillRect(heart.x + 14, heart.y + 3, 10, 10);
            g.fillRect(heart.x + 4, heart.y + 8, 16, 11); g.fillRect(heart.x + 8, heart.y + 19, 8, 5);
            g.setColor(new Color(255, 218, 225)); g.fillRect(heart.x + 3, heart.y + 5, 4, 3);
        }
        drawCentral(g, 0, GameModel.TERMINAL_X); drawCentral(g, 1, GameModel.LOGIC_X);
        if (!model.unlocked()) {
            g.setColor(new Color(255, 189, 89, 160)); g.fillRect(GameModel.GATE_X, 110, 38, 380);
            label(g, ((model.apiSolved() ? 1 : 0) + (model.logicSolved() ? 1 : 0)) + "/2", GameModel.GATE_X + 2, 300, 13, new Color(31, 35, 51));
        }
        g.setColor(new Color(215, 235, 232)); g.fillRect(GameModel.EXIT_X + 20, 310, 5, 180);
        g.setColor(new Color(102, 235, 189)); g.fillPolygon(new int[]{GameModel.EXIT_X + 25, GameModel.EXIT_X + 85, GameModel.EXIT_X + 25}, new int[]{310, 335, 360}, 3);
        if (model.cooperative) {
            g.setColor(model.ropeDistance() > 470 ? new Color(255, 155, 80) : new Color(237, 220, 165));
            g.setStroke(new BasicStroke(3));
            g.drawLine((int)model.first.x + 14, (int)model.first.y + 20, (int)model.second.x + 14, (int)model.second.y + 20);
            g.setStroke(new BasicStroke(1));
        }
        if (model.invincible % 12 < 8) {
            model.first.explorer.draw(g, (int)model.first.x, (int)model.first.y, 1);
            if (model.cooperative) model.second.explorer.draw(g, (int)model.second.x, (int)model.second.y, 1);
        }
        drawAbilityEffect(g, model.first, camera);
        if (model.cooperative) drawAbilityEffect(g, model.second, camera);
        if (model.cooperative) {
            label(g, "P1", (int)model.first.x, (int)model.first.y - 8, 11, Color.WHITE);
            label(g, "P2", (int)model.second.x, (int)model.second.y - 8, 11, new Color(255, 211, 110));
        }
        g.translate(camera, 0);
        g.setColor(new Color(9, 18, 34, 235)); g.fillRect(0, 0, 1000, 82); g.fillRect(0, 550, 1000, 50);
        label(g, "REDE DOS HORIZONTES", 25, 30, 21, new Color(104, 235, 199));
        label(g, GameModel.REGION_NAMES[model.region()], 25, 59, 16, Color.WHITE);
        label(g, (model.cooperative ? "EQUIPE " : "ENERGIA ") + model.lives + "/7   MOEDAS " + model.collectedLevelCoins() + "/" + model.levelCoinTotal()
                + "   TRECHO " + (model.level + 1) + "/10", 485, 29, 16, Color.WHITE);
        label(g, "API " + (model.apiSolved() ? "OK" : "—") + "   LÓGICA " + (model.logicSolved() ? "OK" : "—")
                + (model.cooperative ? "   CORDA " + (int)model.ropeDistance() + "/500" : "   SOLO"), 485, 58, 15, new Color(184, 205, 222));
        label(g, model.cooperative ? "P1: ← → ↑ / Enter / K    P2: A D W / E / F    Voo: ↑↓ ou W/S    P: pausa    F11: tela cheia"
                : "A/D ou ←/→: mover    W/↑/Espaço: salto    E/Enter: central    F/K: especial    P: pausa    F11: tela cheia", 20, 568, 13, Color.WHITE);
        label(g, "P1 " + abilityStatus(model.first), 20, 590, 13, new Color(108, 239, 202));
        if (model.cooperative) label(g, "P2 " + abilityStatus(model.second), 510, 590, 13, new Color(255, 211, 110));
        if (choosing) drawSelection(g);
        else if (model.won) overlay(g, "DEZ HORIZONTES RESTAURADOS", "As duas centrais de cada região voltaram a responder.",
                "Uma nova expedição trará outra ordem e novos desafios.", "R: nova expedição • C: escolher personagens");
        else if (model.gameOver) overlay(g, "SINAL PERDIDO", "A conexão se apagou. Uma nova rota está à espera.",
                "Corações restauram energia. Na dupla, ninguém fica para trás.", "R: nova expedição • C: escolher personagens");
        else if (paused) overlay(g, "EM ABRIGO", "O percurso e os desafios desta expedição estão preservados.",
                "Reúna as moedas e restabeleça as centrais de API e de lógica.", "P: continuar • C: personagens • N: nova expedição");
        else if (model.nearTerminal(0) || model.nearTerminal(1)) {
            int central = model.nearTerminal(0) ? 0 : 1;
            String message = central == 1 && !model.apiSolved() ? "Ative primeiro a central de API no meio da rota."
                    : !model.centralReady(central) ? "Faltam " + (central == 0 ? model.checkpointCoinsRemaining() : model.remainingCoins())
                            + " moedas " + (central == 0 ? "desta metade para ativar o checkpoint." : "para liberar a central final.")
                    : !model.canUseTerminal(central) ? "Aproxime os dois exploradores desta central."
                    : central == 0 && model.apiSolved() ? "Checkpoint ativo. Siga até a central final."
                    : "Enter / E: " + (central == 0 ? "restabelecer API e ativar checkpoint." : "resolver lógica e liberar a próxima região.");
            label(g, message, 25, 108, 16, Color.WHITE);
        } else label(g, "API no meio: checkpoint • lógica no final: próxima região • F/K: habilidade", 25, 108, 14, theme.accent());
        g.dispose();
    }
    private void drawCentral(Graphics2D g, int index, int x) {
        boolean solved = index == 0 ? model.apiSolved() : model.logicSolved();
        g.setColor(new Color(14, 26, 45)); g.fillRect(x - 15, 430, 58, 60);
        g.setColor(solved ? new Color(116, 238, 160) : model.centralReady(index) ? new Color(100, 210, 240) : new Color(115, 125, 140));
        g.fillRect(x - 8, 438, 44, 26);
        label(g, solved ? "OK" : index == 0 ? "{ }" : "01", x, 457, 16, new Color(14, 26, 45));
        label(g, index == 0 ? "API • CHECKPOINT" : "LÓGICA • SAÍDA", x - 55, 415, 12, Color.WHITE);
    }
    private void drawSelection(Graphics2D g) {
        g.setColor(new Color(7, 16, 30, 245)); g.fillRect(0, 82, 1000, 468);
        label(g, "ESCOLHA QUEM ATRAVESSARÁ OS HORIZONTES", 25, 121, 25, new Color(108, 239, 202));
        g.setColor(new Color(35, 77, 81)); g.fillRect(25, 158, 265, 37);
        label(g, "M • " + (model.cooperative ? "DUPLA LOCAL" : "EXPEDIÇÃO SOLO"), 40, 183, 17, Color.WHITE);
        label(g, model.cooperative ? "TAB • selecionando P" + (selectionPlayer + 1) : "1–6 ou setas para escolher", 315, 183, 16, Color.WHITE);
        label(g, model.cooperative ? "Setas escolhem P1 • A/D escolhem P2 • clique nos retratos" : "Dez regiões e desafios sorteados a cada nova expedição", 25, 211, 14, new Color(184, 205, 222));
        for (Explorer explorer : Explorer.values()) {
            int cx = 25 + explorer.ordinal() * 160;
            boolean p1 = model.first.explorer == explorer, p2 = model.cooperative && model.second.explorer == explorer;
            g.setColor(p1 || p2 ? new Color(35, 77, 81) : new Color(22, 36, 55)); g.fillRect(cx, 225, 150, 215);
            g.setColor(p1 ? new Color(108, 239, 202) : p2 ? new Color(255, 211, 110) : new Color(56, 75, 95)); g.drawRect(cx, 225, 150, 215);
            label(g, "" + (explorer.ordinal() + 1) + (p1 ? "  P1" : "") + (p2 ? "  P2" : ""), cx + 10, 247, 13, Color.WHITE);
            explorer.draw(g, cx + 33, 262, 3);
            label(g, explorer.displayName, cx + 12, 402, 22, Color.WHITE);
            label(g, explorer.abilityName(), cx + 10, 421, 11, new Color(184, 205, 222));
            label(g, explorer.abilitySummary(), cx + 10, 436, 10, new Color(184, 205, 222));
        }
        label(g, model.cooperative ? "Energia compartilhada • corda de 500 unidades • cheguem juntos às centrais e à saída"
                : "API no meio salva o percurso. Lógica no final libera a próxima região.", 25, 462, 14, Color.WHITE);
        g.setColor(new Color(108, 239, 202)); g.fillRect(300, 475, 400, 42);
        label(g, started && !model.won && !model.gameOver ? "ENTER • Retomar travessia" : "ENTER • Iniciar travessia", 365, 503, 18, new Color(9, 25, 35));
        label(g, "Especiais: P1 K / P2 F • Recarga 20 s • F11: tela cheia • N na pausa: nova expedição", 190, 539, 14, new Color(184, 205, 222));
    }
    private static String abilityStatus(GameModel.Player p) {
        String state = p.abilityTicks > 0 ? "ATIVA " + ((p.abilityTicks + 59) / 60) + " s"
                : p.cooldownTicks > 0 ? "recarga " + ((p.cooldownTicks + 59) / 60) + " s" : "PRONTA";
        return p.explorer.abilityName() + " • " + state;
    }
    private static void drawAbilityEffect(Graphics2D g, GameModel.Player p, int camera) {
        int x = (int)p.x, y = (int)p.y;
        if (p.active(Explorer.CRIS)) {
            g.setColor(new Color(255, 226, 112, 110)); g.fillOval(x - 10, y - 10, 48, 58);
            g.setColor(new Color(255, 241, 172)); g.drawOval(x - 10, y - 10, 48, 58);
        } else if (p.active(Explorer.NEY)) {
            g.setColor(new Color(241, 224, 78, 170));
            for (int i = 0; i < 3; i++) g.drawLine(x - 35, y + 8 + i * 10, x - 5, y + 8 + i * 10);
        } else if (p.active(Explorer.VIVI)) {
            g.setColor(new Color(193, 207, 230)); g.fillRect(x - 10, y + 20, 48, 9);
            g.setColor(new Color(77, 116, 157)); g.fillRect(x - 13, y + 26, 15, 7); g.fillRect(x + 26, y + 26, 15, 7);
            g.setColor(new Color(255, 175, 65)); g.fillRect(x - 9, y + 33, 6, 12); g.fillRect(x + 30, y + 33, 6, 12);
        }
        if (p.effectTicks > 0 && p.explorer == Explorer.NITA) {
            g.setColor(new Color(255, 128, 67, p.effectTicks * 2)); g.fillRect(camera, 82, 1000, 468);
            int radius = (45 - p.effectTicks) * 12;
            g.setColor(new Color(255, 220, 130, p.effectTicks * 5)); g.drawOval(x + 14 - radius, y + 19 - radius, radius * 2, radius * 2);
        } else if (p.effectTicks > 0 && p.explorer == Explorer.DILA) {
            g.setColor(new Color(126, 244, 231, p.effectTicks * 5));
            int radius = 40 + p.effectTicks * 5;
            g.drawOval(x + 14 - radius, y + 19 - radius, radius * 2, radius * 2);
            label(g, "ÍMÃ", x - 5, y - 20, 14, new Color(126, 244, 231));
        }
    }
    private static void label(Graphics2D g, String text, int x, int y, int size, Color color) {
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, size)); g.setColor(color); g.drawString(text, x, y);
    }
    private static void overlay(Graphics2D g, String title, String first, String second, String action) {
        g.setColor(new Color(4, 10, 24, 220)); g.fillRect(0, 82, 1000, 468);
        g.setColor(new Color(22, 39, 61)); g.fillRoundRect(55, 175, 890, 260, 24, 24);
        label(g, title, 85, 235, 28, new Color(108, 239, 202));
        label(g, first, 85, 290, 16, Color.WHITE);
        label(g, second, 85, 325, 16, new Color(184, 205, 222));
        label(g, action, 85, 390, 17, new Color(255, 212, 115));
    }
}
