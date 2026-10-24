package br.edu.superapi;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

/** Física em passos fixos, independente da interface e testável sem tela. */
public final class GameModel {
    public static final int WIDTH = 4800, FLOOR = 490, GATE_X = 4660, TERMINAL_X = 4600, EXIT_X = WIDTH - 110;
    public double x, y, vy;
    public int level, lives, coins, invincible;
    private int levelCoinTotal;
    public boolean grounded, unlocked, won, gameOver;
    public final List<Rectangle> platforms = new ArrayList<>();
    public final List<Rectangle> collectibles = new ArrayList<>();
    public final List<Enemy> enemies = new ArrayList<>();
    public enum EnemyKind { CRAWLER, HOPPER, DRONE }
    public static final class Enemy {
        public double x, speed;
        public final int min, max, baseY;
        public final EnemyKind kind;
        private int age;
        public boolean alive = true;
        Enemy(int min, int max, int baseY, double speed, EnemyKind kind) {
            this.min = min; this.max = max; this.x = min;
            this.baseY = baseY; this.speed = speed; this.kind = kind;
        }
        public Rectangle bounds() {
            int lift = switch (kind) {
                case CRAWLER -> 0;
                case HOPPER -> (int)(Math.abs(Math.sin(age * .045)) * 58);
                case DRONE -> (int)(Math.sin(age * .035) * 30);
            };
            return new Rectangle((int)x, baseY - lift, 30, 28);
        }
        void tick() {
            age++; x += speed;
            if (x < min || x > max) { x = Math.max(min, Math.min(max, x)); speed *= -1; }
        }
    }
    public GameModel() { restart(); }
    public void restart() { level = 0; lives = 3; coins = 0; won = false; gameOver = false; load(); }
    public void load() {
        unlocked = false; platforms.clear(); collectibles.clear(); enemies.clear();
        // Two 120-unit gaps; their positions differ by region. Maximum jump spans ~200 units.
        int gapA = 780 + level * 40, gapB = 3300 + level * 45;
        platforms.add(new Rectangle(0, FLOOR, gapA, 100));
        platforms.add(new Rectangle(gapA + 120, FLOOR, gapB - gapA - 120, 100));
        platforms.add(new Rectangle(gapB + 120, FLOOR, WIDTH - gapB - 120, 100));
        // Stair pairs: 85 then 70 units high, safely within the 142-unit jump apex.
        for (int section = 0; section < 6; section++) {
            int start = (section < 4 ? 300 + section * 700 : 3700 + (section - 4) * 500) + (level % 3) * 30;
            int lowerY = 405 - (level % 2) * 10;
            platforms.add(new Rectangle(start, lowerY, 150, 22));
            platforms.add(new Rectangle(start + 210, lowerY - 70, 150, 22));
            collectibles.add(new Rectangle(start + 55, lowerY - 32, 18, 22));
            collectibles.add(new Rectangle(start + 255, lowerY - 102, 18, 22));
            collectibles.add(new Rectangle(start + 310, lowerY - 102, 18, 22));
            if (section != 3) enemies.add(new Enemy(start + 10, start + 110, lowerY - 28,
                    1.1 + level * .12, EnemyKind.CRAWLER));
        }
        enemies.add(new Enemy(490, 710, FLOOR - 28, 1.4 + level * .15, EnemyKind.CRAWLER));
        enemies.add(new Enemy(1500, 1740, FLOOR - 28, 1.5 + level * .15, EnemyKind.HOPPER));
        enemies.add(new Enemy(2130, 2410, 365, 1.6 + level * .15, EnemyKind.DRONE));
        enemies.add(new Enemy(3030, 3220, FLOOR - 28, 1.6 + level * .15, EnemyKind.HOPPER));
        enemies.add(new Enemy(3800, 4060, 355, 1.7 + level * .15, EnemyKind.DRONE));
        enemies.add(new Enemy(4390, 4570, FLOOR - 28, 1.8 + level * .15, EnemyKind.CRAWLER));
        levelCoinTotal = collectibles.size();
        respawn();
    }
    private void respawn() { x = unlocked ? TERMINAL_X + 20 : 70; y = 420; vy = 0; grounded = false; invincible = 90; }
    public java.awt.geom.Rectangle2D.Double player() { return new java.awt.geom.Rectangle2D.Double(x, y, 28, 38); }
    public boolean nearTerminal() { return Math.abs(x - TERMINAL_X) < 110 && y > 370; }
    public int remainingCoins() { return collectibles.size(); }
    public int levelCoinTotal() { return levelCoinTotal; }
    public int collectedLevelCoins() { return levelCoinTotal - remainingCoins(); }
    public boolean centralReady() { return remainingCoins() == 0; }
    public boolean canUseTerminal() { return !won && !gameOver && nearTerminal() && centralReady(); }
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
            int oldEnemyTop = enemy.bounds().y;
            enemy.tick();
            if (player().intersects(enemy.bounds())) {
                if (vy > 0 && oldBottom <= oldEnemyTop + 8) { enemy.alive = false; vy = -8; }
                else if (invincible == 0) { hurt(); return; }
            }
        }
        if (y > 650) { hurt(); return; }
        if (unlocked && x > EXIT_X) { if (level == Lesson.ALL.length - 1) won = true; else { level++; load(); } }
    }
    private void hurt() { lives--; if (lives <= 0) gameOver = true; else respawn(); }
}
