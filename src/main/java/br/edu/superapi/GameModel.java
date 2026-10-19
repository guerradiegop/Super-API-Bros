package br.edu.superapi;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

/** Física em passos fixos, independente da interface e testável sem tela. */
public final class GameModel {
    public static final int WIDTH = 2400, FLOOR = 490, GATE_X = 1500, TERMINAL_X = 1370;
    public double x, y, vy;
    public int level, lives, coins, invincible;
    public boolean grounded, unlocked, won, gameOver;
    public final List<Rectangle> platforms = new ArrayList<>();
    public final List<Rectangle> collectibles = new ArrayList<>();
    public final List<Enemy> enemies = new ArrayList<>();
    public static final class Enemy {
        public double x, speed;
        public final int min, max;
        public boolean alive = true;
        Enemy(int min, int max, double speed) { this.min = min; this.max = max; this.x = min; this.speed = speed; }
        public Rectangle bounds() { return new Rectangle((int)x, FLOOR - 28, 30, 28); }
    }
    public GameModel() { restart(); }
    public void restart() { level = 0; lives = 3; coins = 0; won = false; gameOver = false; load(); }
    public void load() {
        unlocked = false; platforms.clear(); collectibles.clear(); enemies.clear();
        platforms.add(new Rectangle(0, FLOOR, 780, 100));
        platforms.add(new Rectangle(900, FLOOR, 1500, 100));
        platforms.add(new Rectangle(330, 405, 150, 22));
        platforms.add(new Rectangle(550, 335, 145, 22));
        platforms.add(new Rectangle(1060, 400, 130, 22));
        platforms.add(new Rectangle(1730, 400, 150, 22));
        platforms.add(new Rectangle(1940, 330, 150, 22));
        for (int cx : new int[]{365, 420, 580, 635, 1090, 1770, 1990, 2050, 2200}) {
            int cy = cx < 500 ? 370 : cx < 700 ? 300 : cx < 1900 ? 365 : cx < 2100 ? 295 : 450;
            collectibles.add(new Rectangle(cx, cy, 18, 22));
        }
        enemies.add(new Enemy(490, 710, 1.4 + level * .3));
        enemies.add(new Enemy(1770, 2110, 1.6 + level * .3));
        respawn();
    }
    private void respawn() { x = unlocked ? 1390 : 70; y = 420; vy = 0; grounded = false; invincible = 90; }
    public java.awt.geom.Rectangle2D.Double player() { return new java.awt.geom.Rectangle2D.Double(x, y, 28, 38); }
    public boolean nearTerminal() { return Math.abs(x - TERMINAL_X) < 110 && y > 370; }
    public void tick(boolean left, boolean right, boolean jump) {
        if (won || gameOver) return;
        if (invincible > 0) invincible--;
        double dx = (right ? 4.4 : 0) - (left ? 4.4 : 0);
        if (jump && grounded) { vy = -12.5; grounded = false; }
        List<Rectangle> solids = new ArrayList<>(platforms);
        if (!unlocked) solids.add(new Rectangle(GATE_X, 0, 38, FLOOR));
        x = Math.max(0, Math.min(WIDTH - 28, x + dx));
        for (Rectangle solid : solids) if (player().intersects(solid)) {
            if (dx > 0) x = solid.x - 28; else if (dx < 0) x = solid.x + solid.width;
        }
        double oldBottom = y + 38;
        vy = Math.min(vy + .55, 13); y += vy; grounded = false;
        for (Rectangle solid : solids) if (player().intersects(solid)) {
            if (vy >= 0 && oldBottom <= solid.y + 1) { y = solid.y - 38; vy = 0; grounded = true; }
            else if (vy < 0) { y = solid.y + solid.height; vy = 0; }
        }
        collectibles.removeIf(coin -> { if (player().intersects(coin)) { coins++; return true; } return false; });
        for (Enemy enemy : enemies) {
            if (!enemy.alive) continue;
            enemy.x += enemy.speed;
            if (enemy.x < enemy.min || enemy.x > enemy.max) { enemy.x = Math.max(enemy.min, Math.min(enemy.max, enemy.x)); enemy.speed *= -1; }
            if (player().intersects(enemy.bounds())) {
                if (vy > 0 && oldBottom <= FLOOR - 20) { enemy.alive = false; vy = -8; }
                else if (invincible == 0) { hurt(); return; }
            }
        }
        if (y > 650) { hurt(); return; }
        if (unlocked && x > 2290) { if (level == 2) won = true; else { level++; load(); } }
    }
    private void hurt() { lives--; if (lives <= 0) gameOver = true; else respawn(); }
}
