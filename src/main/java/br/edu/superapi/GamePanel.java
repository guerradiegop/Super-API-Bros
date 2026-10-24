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
    private boolean started, paused, labOpen, jumpQueued;
    private boolean choosing = true;
    private final Timer timer;
    private long previous = System.nanoTime();
    private double accumulator;
    public GamePanel(ApiClient api) {
        this.api = api;
        setPreferredSize(new Dimension(1000, 600)); setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent event) {
                int key = event.getKeyCode();
                boolean fresh = keys.add(key);
                if (!fresh) return;
                if (key == KeyEvent.VK_C && !labOpen && (!started || paused || model.won || model.gameOver)) {
                    choosing = true; keys.clear(); jumpQueued = false; repaint(); return;
                }
                if (choosing) {
                    if (key >= KeyEvent.VK_1 && key <= KeyEvent.VK_4) model.selectExplorer(key - KeyEvent.VK_1);
                    if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) model.selectExplorer(model.explorer.ordinal() - 1);
                    if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) model.selectExplorer(model.explorer.ordinal() + 1);
                    if (key == KeyEvent.VK_ENTER) confirmExplorer();
                    if (key == KeyEvent.VK_ESCAPE && started) choosing = false;
                    repaint(); return;
                }
                if (key == KeyEvent.VK_SPACE || key == KeyEvent.VK_UP || key == KeyEvent.VK_W) jumpQueued = true;
                if (key == KeyEvent.VK_P && started && !labOpen) paused = !paused;
                if (key == KeyEvent.VK_R && (model.won || model.gameOver)) { model.restart(); started = true; paused = false; }
                if (key == KeyEvent.VK_E && started && !paused && !labOpen && !model.gameOver && !model.won && model.canUseTerminal()) openLab();
            }
            @Override public void keyReleased(KeyEvent event) { keys.remove(event.getKeyCode()); }
        });
        addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent event) {
                if (!choosing) return;
                int mx = event.getX() * 1000 / Math.max(1, getWidth());
                int my = event.getY() * 600 / Math.max(1, getHeight());
                for (int i = 0; i < Explorer.values().length; i++)
                    if (new Rectangle(55 + i * 230, 215, 200, 235).contains(mx, my)) model.selectExplorer(i);
                if (new Rectangle(310, 475, 380, 45).contains(mx, my)) confirmExplorer();
                requestFocusInWindow(); repaint();
            }
        });
        addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) { keys.clear(); jumpQueued = false; if (started && !labOpen) paused = true; }
        });
        timer = new Timer(16, event -> {
            long now = System.nanoTime();
            accumulator += Math.min((now - previous) / 1_000_000_000.0, .1); previous = now;
            while (accumulator >= 1.0 / 60) {
                if (started && !paused && !labOpen && !choosing) {
                    model.tick(keys.contains(KeyEvent.VK_LEFT) || keys.contains(KeyEvent.VK_A), keys.contains(KeyEvent.VK_RIGHT) || keys.contains(KeyEvent.VK_D), jumpQueued);
                }
                jumpQueued = false; accumulator -= 1.0 / 60;
            }
            repaint();
        });
    }
    @Override public void addNotify() { super.addNotify(); previous = System.nanoTime(); timer.start(); }
    @Override public void removeNotify() { timer.stop(); super.removeNotify(); }
    private void confirmExplorer() {
        if (model.won || model.gameOver) model.restart();
        choosing = false; started = true; paused = false; keys.clear(); jumpQueued = false;
    }
    private void openLab() {
        if (!model.canUseTerminal()) return;
        labOpen = true; keys.clear(); jumpQueued = false;
        LabDialog dialog = new LabDialog(SwingUtilities.getWindowAncestor(this), api, Lesson.ALL[model.level]);
        dialog.setVisible(true);
        if (dialog.solved()) model.unlocked = true;
        labOpen = false; requestFocusInWindow();
    }
    @Override protected void paintComponent(Graphics original) {
        super.paintComponent(original);
        Graphics2D g = (Graphics2D) original.create();
        g.scale(getWidth() / 1000.0, getHeight() / 600.0);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        RegionTheme theme = RegionTheme.ALL[model.level];
        g.setPaint(new GradientPaint(0, 0, theme.sky(), 0, 600, theme.horizon()));
        g.fillRect(0, 0, 1000, 600);
        int camera = (int)Math.max(0, Math.min(GameModel.WIDTH - 1000, model.x - 330));
        theme.drawBackdrop(g, camera, model.level);
        g.translate(-camera, 0);
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
        g.setColor(new Color(14, 26, 45)); g.fillRoundRect(GameModel.TERMINAL_X - 15, 430, 58, 60, 8, 8);
        g.setColor(model.centralReady() ? new Color(93, 231, 213) : new Color(115, 125, 140)); g.fillRect(GameModel.TERMINAL_X - 8, 438, 44, 26);
        label(g, "{ }", GameModel.TERMINAL_X, 458, 17, new Color(14, 26, 45));
        label(g, "CENTRAL", GameModel.TERMINAL_X - 32, 415, 13, Color.WHITE);
        if (!model.unlocked) {
            g.setColor(new Color(255, 189, 89, 160)); g.fillRect(GameModel.GATE_X, 110, 38, 380);
            label(g, "API", GameModel.GATE_X + 3, 300, 14, new Color(31, 35, 51));
        } else label(g, Lesson.ALL[model.level].status() + " • passagem liberada", GameModel.GATE_X - 240, 395, 15, new Color(122, 247, 193));
        g.setColor(new Color(215, 235, 232)); g.fillRect(GameModel.EXIT_X + 20, 310, 5, 180);
        g.setColor(new Color(102, 235, 189)); g.fillPolygon(new int[]{GameModel.EXIT_X + 25, GameModel.EXIT_X + 85, GameModel.EXIT_X + 25}, new int[]{310, 335, 360}, 3);
        if (model.invincible % 12 < 8) model.explorer.draw(g, (int)model.x, (int)model.y, 1);
        if (model.nearTerminal() && started) label(g,
                model.centralReady() ? "[E] Conectar à central" : "Central inativa • faltam " + model.remainingCoins() + " moedas",
                GameModel.TERMINAL_X - 240, 380, 16, Color.WHITE);
        g.translate(camera, 0);
        g.setColor(new Color(9, 18, 34, 225)); g.fillRect(0, 0, 1000, 82); g.fillRect(0, 550, 1000, 50);
        label(g, "REDE DOS HORIZONTES", 28, 32, 22, new Color(104, 235, 199));
        label(g, Lesson.ALL[model.level].title(), 28, 60, 15, Color.WHITE);
        label(g, "ENERGIA  " + model.lives + "     MOEDAS  " + model.collectedLevelCoins() + "/" + model.levelCoinTotal() + "     SETOR  " + (model.level + 1) + "/" + Lesson.ALL.length, 510, 43, 16, Color.WHITE);
        label(g, "A/D ou ←/→  mover     Espaço  pular     E  terminal     P  pausar", 28, 581, 15, new Color(190, 211, 223));
        if (choosing) drawSelection(g);
        else if (started && !model.won && !model.gameOver && !paused && model.level >= 6) {
            label(g, model.level == 6 ? "Gelo em movimento: espere a altura certa para saltar."
                    : "Sucata instável: salte antes que as placas cedam.", 28, 108, 15, theme.accent());
        }
        if (!choosing && model.won) overlay(g, "OS HORIZONTES RESPONDEM", "As oito regiões voltaram a se comunicar. Um novo horizonte desperta.", "Cada sinal recuperado agora ilumina um novo caminho.", "Dados coletados: " + model.coins + " • R para nova expedição • C para escolher personagem");
        else if (!choosing && model.gameOver) overlay(g, "SINAL PERDIDO", "Sua conexão se apagou, mas as centrais ainda aguardam.", "Salte sobre os sentinelas e procure abrigo nas plataformas.", "R para retomar • C para escolher personagem");
        else if (!choosing && paused) overlay(g, "EM ABRIGO", "Seu percurso está preservado enquanto você descansa.", "Reúna todas as moedas, restabeleça a central e siga até o sinal.", "P para continuar • C para escolher personagem");
        g.dispose();
    }
    private void drawSelection(Graphics2D g) {
        g.setColor(new Color(7, 16, 30, 245)); g.fillRect(0, 82, 1000, 468);
        label(g, "QUEM ATRAVESSARÁ O HORIZONTE?", 55, 130, 27, new Color(108, 239, 202));
        label(g, "Oito regiões em silêncio. Reúna as moedas e restabeleça suas centrais.", 55, 164, 17, Color.WHITE);
        label(g, "← / → ou 1–4 para escolher • clique nos retratos", 55, 193, 14, new Color(184, 205, 222));
        for (Explorer explorer : Explorer.values()) {
            int cx = 55 + explorer.ordinal() * 230;
            boolean selected = model.explorer == explorer;
            g.setColor(selected ? new Color(35, 77, 81) : new Color(22, 36, 55));
            g.fillRect(cx, 215, 200, 235);
            g.setColor(selected ? new Color(108, 239, 202) : new Color(56, 75, 95));
            g.drawRect(cx, 215, 200, 235);
            label(g, "0" + (explorer.ordinal() + 1), cx + 14, 239, 13, Color.WHITE);
            explorer.draw(g, cx + 58, 250, 3);
            label(g, explorer.displayName, cx + 20, 397, 24, Color.WHITE);
            label(g, explorer.subtitle, cx + 20, 426, 15, new Color(184, 205, 222));
        }
        g.setColor(new Color(108, 239, 202)); g.fillRect(310, 475, 380, 45);
        label(g, started ? "ENTER • Retomar travessia" : "ENTER • Iniciar travessia", 362, 504, 18, new Color(9, 25, 35));
        label(g, "A aparência muda. A coragem e os controles são os mesmos.", 266, 540, 14, new Color(184, 205, 222));
    }
    private static void label(Graphics2D g, String text, int x, int y, int size, Color color) {
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, size)); g.setColor(color); g.drawString(text, x, y);
    }
    private static void overlay(Graphics2D g, String title, String first, String second, String action) {
        g.setColor(new Color(4, 10, 24, 200)); g.fillRect(0, 82, 1000, 468);
        g.setColor(new Color(22, 39, 61)); g.fillRoundRect(80, 175, 840, 260, 24, 24);
        label(g, title, 115, 235, 30, new Color(108, 239, 202));
        label(g, first, 115, 290, 17, Color.WHITE);
        label(g, second, 115, 325, 16, new Color(184, 205, 222));
        label(g, action, 115, 390, 18, new Color(255, 212, 115));
    }
}
