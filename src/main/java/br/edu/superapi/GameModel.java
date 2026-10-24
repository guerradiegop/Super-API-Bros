package br.edu.superapi;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.*;

/** Fixed-step simulation shared by solo and local co-op. */
public final class GameModel {
    public static final int WIDTH = 7344, VIEW_WIDTH = 1000, ROPE_LENGTH = VIEW_WIDTH / 2;
    public static final int FLOOR = 490, TERMINAL_X = WIDTH / 2, LOGIC_X = WIDTH - 250;
    public static final int GATE_X = WIDTH - 130, EXIT_X = WIDTH - 100, MAX_ENERGY = 7, REGION_COUNT = 10;
    public static final int BLOCK_COUNT = 10, ABILITY_COOLDOWN = 20 * 60;
    public static final String[] REGION_NAMES = {"Jardim dos endpoints", "Fortaleza das chaves", "Oficina das pontes",
            "Cavernas de cristal", "Deserto dos sinais", "Cidade suspensa", "Cordilheira branca", "Horizonte das cinzas",
            "Arquipélago das marés", "Reator da aurora"};
    public static final class Player {
        public double x, y, vy;
        public boolean grounded;
        public Explorer explorer;
        public int abilityTicks, cooldownTicks, effectTicks;
        public boolean active(Explorer ability) { return explorer == ability && abilityTicks > 0; }
        Player(Explorer explorer) { this.explorer = explorer; }
        public Rectangle2D.Double bounds() { return new Rectangle2D.Double(x, y, 28, 38); }
    }
    public record Input(boolean left, boolean right, boolean jump, boolean flyUp, boolean flyDown) {
        public Input(boolean left, boolean right, boolean jump) { this(left, right, jump, jump, false); }
        public static final Input NONE = new Input(false, false, false);
    }
    public final Player first = new Player(Explorer.NEY), second = new Player(Explorer.LEO);
    public boolean cooperative, won, gameOver;
    public int level, lives, coins, invincible;
    private boolean apiSolved, logicSolved;
    private int levelCoinTotal;
    private long seed;
    private int[] order, apiOrder;
    private LogicChallenge[] logic;
    public final List<Rectangle> platforms = new ArrayList<>(), collectibles = new ArrayList<>(), hearts = new ArrayList<>();
    public final List<Enemy> enemies = new ArrayList<>();
    public final List<Ball> balls = new ArrayList<>();
    public GameModel() { restart(); }
    public GameModel(long seed) { restart(seed); }
    public void selectExplorer(int player, int index) {
        Player selected = player == 0 ? first : second;
        Explorer next = Explorer.values()[Math.floorMod(index, Explorer.values().length)];
        if (selected.explorer != next) { selected.abilityTicks = selected.effectTicks = 0; }
        selected.explorer = next; // Switching appearance does not bypass the cooldown.
    }
    public int region() { return order[level]; }
    public long seed() { return seed; }
    public int[] regionOrder() { return order.clone(); }
    public Lesson apiLesson() { return Lesson.ALL[apiOrder[level]]; }
    public LogicChallenge logicChallenge() { return logic[level]; }
    public boolean apiSolved() { return apiSolved; }
    public boolean logicSolved() { return logicSolved; }
    public boolean unlocked() { return apiSolved && logicSolved; }
    private static int[] shuffle(Random random) {
        int[] result = new int[REGION_COUNT];
        for (int i = 0; i < result.length; i++) result[i] = i;
        for (int i = result.length - 1; i > 0; i--) { int j = random.nextInt(i + 1), t = result[i]; result[i] = result[j]; result[j] = t; }
        return result;
    }
    public void restart() {
        int[] previous = order;
        do { restart(new Random().nextLong()); } while (previous != null && Arrays.equals(previous, order));
    }
    public void restart(long newSeed) {
        seed = newSeed; Random random = new Random(seed);
        order = shuffle(random); apiOrder = shuffle(random); int[] kinds = shuffle(random);
        logic = new LogicChallenge[REGION_COUNT];
        for (int i = 0; i < REGION_COUNT; i++) logic[i] = LogicChallenge.create(kinds[i], random);
        level = 0; lives = 3; coins = 0; won = gameOver = false; load();
    }
    public static int blockStart(int block) {
        return (block < 5 ? 140 : TERMINAL_X + 140) + (block % 5) * 640;
    }
    public void load() {
        apiSolved = logicSolved = false;
        platforms.clear(); collectibles.clear(); hearts.clear(); enemies.clear(); balls.clear();
        for (Player p : List.of(first, second)) p.abilityTicks = p.cooldownTicks = p.effectTicks = 0;
        Random random = new Random(seed ^ (region() * 7919L));
        int from = 0;
        // Ten hand-bounded blocks around the midpoint checkpoint. Random variants never exceed tested jump limits.
        for (int block = 0; block < BLOCK_COUNT; block++) {
            int start = blockStart(block), gap = start + 500, gapWidth = 100 + (region() % 2) * 20;
            platforms.add(new Rectangle(from, FLOOR, gap - from, 100)); from = gap + gapWidth;
            int variant = random.nextInt(3);
            for (int step = 0; step < 3; step++) {
                int px = start + step * 160;
                int py = step == 0 ? 410 : step == 1 ? 340 : variant == 0 ? 270 : 410;
                Rectangle coin = new Rectangle(px + 55, py - 32, 18, 22); collectibles.add(coin);
                boolean moving = step == 1 && (region() == 6 || region() == 8 || region() == 5 && block % 2 == 0);
                boolean fragile = step == 1 && (region() == 7 || region() == 9 || region() == 2 && block % 3 == 0);
                platforms.add(moving || fragile ? new TerrainPlatform(px, py, 120,
                        moving ? TerrainPlatform.Kind.ICE_LIFT : TerrainPlatform.Kind.CRUMBLING, coin)
                        : new Rectangle(px, py, 120, 22));
            }
            EnemyKind ground = switch (region()) {
                case 6 -> EnemyKind.SKATER;
                case 7, 9 -> EnemyKind.SCAVENGER;
                case 1, 4, 8 -> EnemyKind.HOPPER;
                default -> EnemyKind.CRAWLER;
            };
            enemies.add(new Enemy(start + 20, start + 180, FLOOR - 28, 1.2 + random.nextDouble() * .4, ground));
            if (block % 5 == 1 || block % 5 == 3) hearts.add(new Rectangle(start + 70, FLOOR - 30, 24, 24));
        }
        platforms.add(new Rectangle(from, FLOOR, WIDTH - from, 100));
        // ceil(previous population * 1.8), with 22 for the two new regions.
        int population = region() < 6 ? 20 : region() == 6 ? 17 : 22;
        for (int i = 0; enemies.size() < population; i++) {
            int block = i % BLOCK_COUNT, step = i < BLOCK_COUNT ? 0 : 2;
            int start = blockStart(block) + step * 160;
            EnemyKind kind = region() == 6 ? EnemyKind.FROST_WISP : region() == 7 || region() == 9
                    ? EnemyKind.SCAVENGER : region() == 5 || region() == 8 ? EnemyKind.DRONE : EnemyKind.CRAWLER;
            int baseY = kind == EnemyKind.DRONE || kind == EnemyKind.FROST_WISP ? 245 : platforms.get(block * 4 + 1 + step).y - 28;
            enemies.add(new Enemy(start + 10, start + 85, baseY, 1.1, kind));
        }
        levelCoinTotal = collectibles.size(); respawn();
    }
    private List<Player> players() { return cooperative ? List.of(first, second) : List.of(first); }
    private void respawn() { respawn(true); }
    private void respawn(boolean cancelAbilities) {
        balls.clear();
        if (cancelAbilities) for (Player p : List.of(first, second)) p.abilityTicks = p.effectTicks = 0;
        for (Rectangle platform : platforms) if (platform instanceof TerrainPlatform terrain) terrain.resetMotion();
        double checkpoint = apiSolved ? TERMINAL_X - 70 : 70;
        first.x = checkpoint; second.x = checkpoint + 45;
        for (Player p : List.of(first, second)) { p.y = 420; p.vy = 0; p.grounded = false; }
        invincible = 90;
    }
    public int remainingCoins() { return collectibles.size(); }
    public int levelCoinTotal() { return levelCoinTotal; }
    public int collectedLevelCoins() { return levelCoinTotal - remainingCoins(); }
    public int checkpointCoinsRemaining() { return (int)collectibles.stream().filter(c -> c.x < TERMINAL_X).count(); }
    public boolean centralReady(int central) {
        return central == 0 ? checkpointCoinsRemaining() == 0 : apiSolved && collectibles.isEmpty();
    }
    public boolean nearTerminal(int central) {
        int tx = central == 0 ? TERMINAL_X : LOGIC_X;
        return players().stream().anyMatch(p -> Math.abs(p.x - tx) < 80 && p.y > 370);
    }
    public boolean canUseTerminal(int central) {
        if (central < 0 || central > 1 || won || gameOver || !centralReady(central) || !nearTerminal(central)) return false;
        int tx = central == 0 ? TERMINAL_X : LOGIC_X;
        return !cooperative || players().stream().allMatch(p -> Math.abs(p.x - tx) < 180 && p.y > 370);
    }
    public void completeApi() { if (canUseTerminal(0)) apiSolved = true; }
    public boolean submitLogic(String answer) {
        if (!canUseTerminal(1) || !logicChallenge().accepts(answer)) return false;
        logicSolved = true; return true;
    }
    public double ropeDistance() { return Math.hypot(first.x - second.x, first.y - second.y); }
    public double camera() {
        double center = cooperative ? (first.x + second.x) / 2 + 14 : first.x + 170;
        return Math.max(0, Math.min(WIDTH - VIEW_WIDTH, center - VIEW_WIDTH / 2.0));
    }
    public void tick(boolean left, boolean right, boolean jump) { tick(new Input(left, right, jump), Input.NONE); }
    public void tick(Input one, Input two) {
        if (won || gameOver) return;
        tickWorld(one, two);
        for (Player p : players()) {
            if (p.abilityTicks > 0) p.abilityTicks--;
            if (p.cooldownTicks > 0) p.cooldownTicks--;
            if (p.effectTicks > 0) p.effectTicks--;
        }
    }
    private void tickWorld(Input one, Input two) {
        if (invincible > 0) invincible--;
        double[] old = {first.x, first.y, second.x, second.y};
        for (Rectangle platform : platforms) if (platform instanceof TerrainPlatform terrain) {
            List<Player> riders = new ArrayList<>();
            for (Player p : players()) if (terrain.active() && p.grounded && Math.abs(p.y + 38 - terrain.y) < 1
                    && p.x + 28 > terrain.x && p.x < terrain.x + terrain.width) riders.add(p);
            int shift = terrain.tick(!riders.isEmpty());
            for (Player p : riders) { if (terrain.active()) p.y += shift; else p.grounded = false; }
        }
        List<Rectangle> solids = new ArrayList<>();
        for (Rectangle platform : platforms) if (!(platform instanceof TerrainPlatform terrain) || terrain.active()) solids.add(platform);
        if (!unlocked()) solids.add(new Rectangle(GATE_X, 0, 38, FLOOR));
        double firstBottom = first.y + 38, secondBottom = second.y + 38;
        move(first, one, solids); if (cooperative) move(second, two, solids);
        if (cooperative) enforceRope(old, solids);
        collectibles.removeIf(coin -> {
            if (players().stream().anyMatch(p -> p.bounds().intersects(coin))) { coins++; return true; } return false;
        });
        hearts.removeIf(heart -> {
            if (lives < MAX_ENERGY && players().stream().anyMatch(p -> p.bounds().intersects(heart))) { lives++; return true; }
            return false;
        });
        for (Enemy enemy : enemies) {
            if (!enemy.alive) continue;
            int oldTop = enemy.bounds().y;
            double target = cooperative && Math.abs(second.x - enemy.x) < Math.abs(first.x - enemy.x) ? second.x : first.x;
            enemy.tick(target);
            for (Player p : players()) if (enemy.alive && p.bounds().intersects(enemy.bounds())) {
                double bottom = p == first ? firstBottom : secondBottom;
                if (p.vy > 0 && bottom <= oldTop + 8) { enemy.alive = false; p.vy = -8; }
                else if (invincible == 0 && !p.active(Explorer.CRIS)) { hurt(); return; }
            }
        }
        balls.removeIf(ball -> !ball.tick());
        if (players().stream().anyMatch(p -> p.y > 650 && !p.active(Explorer.CRIS))) { hurt(); return; }
        if (players().stream().anyMatch(p -> p.y > 650)) { respawn(false); return; }
        if (unlocked() && players().stream().allMatch(p -> p.x > EXIT_X)) {
            if (level == REGION_COUNT - 1) won = true; else { level++; load(); }
        }
    }
    private static void move(Player p, Input input, List<Rectangle> solids) {
        double speed = p.active(Explorer.NEY) ? 11.0 : 4.4;
        double dx = (input.right ? speed : 0) - (input.left ? speed : 0);
        if (input.jump && p.grounded) { p.vy = -12.5; p.grounded = false; }
        p.x = Math.max(0, Math.min(WIDTH - 28, p.x + dx));
        for (Rectangle solid : solids) if (p.bounds().intersects(solid)) {
            if (dx > 0) p.x = solid.x - 28; else if (dx < 0) p.x = solid.x + solid.width;
        }
        double oldBottom = p.y + 38;
        if (p.active(Explorer.VIVI)) p.vy = (input.flyDown ? 5.5 : 0) - (input.flyUp ? 5.5 : 0);
        else p.vy = Math.min(p.vy + .55, 13);
        p.y += p.vy; p.grounded = false;
        if (p.active(Explorer.VIVI) && p.y < 90) { p.y = 90; p.vy = 0; }
        for (Rectangle solid : solids) if (p.bounds().intersects(solid)) {
            if (p.vy >= 0 && oldBottom <= solid.y + 1) { p.y = solid.y - 38; p.vy = 0; p.grounded = true; }
            else if (p.vy < 0) { p.y = solid.y + solid.height; p.vy = 0; }
        }
    }
    private void enforceRope(double[] old, List<Rectangle> solids) {
        if (ropeDistance() <= ROPE_LENGTH) return;
        double[] next = {first.x, first.y, second.x, second.y};
        double low = 0, high = 1;
        // Restrict the attempted displacement, never teleport either player through a wall.
        for (int i = 0; i < 24; i++) {
            double t = (low + high) / 2;
            setPositions(old, next, t);
            if (ropeDistance() <= ROPE_LENGTH && clear(first, solids) && clear(second, solids)) low = t; else high = t;
        }
        setPositions(old, next, low);
        if (!clear(first, solids) || !clear(second, solids) || ropeDistance() > ROPE_LENGTH + .001) { respawn(); return; }
        for (Player p : players()) {
            p.vy = 0;
            p.grounded = solids.stream().anyMatch(s -> Math.abs(p.y + 38 - s.y) < 1 && p.x + 28 > s.x && p.x < s.x + s.width);
        }
    }
    private static boolean clear(Player p, List<Rectangle> solids) { return solids.stream().noneMatch(s -> p.bounds().intersects(s)); }
    private void setPositions(double[] old, double[] next, double t) {
        first.x = old[0] + (next[0] - old[0]) * t; first.y = old[1] + (next[1] - old[1]) * t;
        second.x = old[2] + (next[2] - old[2]) * t; second.y = old[3] + (next[3] - old[3]) * t;
    }
    public Rectangle2D.Double visibleArea() { return new Rectangle2D.Double(camera(), 82, VIEW_WIDTH, 468); }
    public boolean activateAbility(int index) {
        if (won || gameOver || index < 0 || index > 1 || index == 1 && !cooperative) return false;
        Player p = index == 0 ? first : second;
        if (p.cooldownTicks > 0) return false;
        p.cooldownTicks = ABILITY_COOLDOWN; p.effectTicks = 45;
        Rectangle2D visible = visibleArea();
        switch (p.explorer) {
            case NEY, VIVI -> p.abilityTicks = 600;
            case CRIS -> p.abilityTicks = 300;
            case NITA -> enemies.stream().filter(e -> e.alive && visible.intersects(e.bounds())).forEach(e -> e.alive = false);
            case DILA -> {
                collectibles.removeIf(coin -> { if (visible.intersects(coin)) { coins++; return true; } return false; });
                hearts.removeIf(heart -> { if (lives < MAX_ENERGY && visible.intersects(heart)) { lives++; return true; } return false; });
            }
            case LEO -> enemies.stream().filter(e -> e.alive)
                    .sorted(Comparator.comparingDouble(e -> Math.hypot(e.x + 15 - p.x - 14, e.bounds().getCenterY() - p.y - 19)))
                    .limit(5).forEach(e -> balls.add(new Ball(p.x + 14, p.y + 19, e)));
        }
        return true;
    }
    public static final class Ball {
        public double x, y;
        public final Enemy target;
        private int lifetime = WIDTH / 14 + 180;
        Ball(double x, double y, Enemy target) { this.x = x; this.y = y; this.target = target; }
        boolean tick() {
            if (!target.alive || --lifetime <= 0) return false;
            double dx = target.bounds().getCenterX() - x, dy = target.bounds().getCenterY() - y;
            double distance = Math.hypot(dx, dy);
            if (distance <= 20) { target.alive = false; return false; }
            x += dx / distance * 14; y += dy / distance * 14;
            return true;
        }
    }
    private void hurt() { lives--; if (lives <= 0) gameOver = true; else respawn(); }
    public enum EnemyKind { CRAWLER, HOPPER, DRONE, SKATER, FROST_WISP, SCAVENGER }
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
                case CRAWLER, SKATER, SCAVENGER -> 0;
                case HOPPER -> (int)(Math.abs(Math.sin(age * .045)) * 58);
                case DRONE -> (int)(Math.sin(age * .035) * 30);
                case FROST_WISP -> (int)(Math.sin(age * .055) * 55);
            };
            return new Rectangle((int)x, baseY - lift, 30, 28);
        }
        void tick(double playerX) {
            age++;
            double factor = kind == EnemyKind.SKATER ? (age % 120 < 60 ? 1.8 : .4)
                    : kind == EnemyKind.SCAVENGER ? (Math.abs(playerX - x) < 200 ? 2.3 : .65) : 1;
            x += speed * factor;
            if (x < min || x > max) { x = Math.max(min, Math.min(max, x)); speed *= -1; }
        }
    }
}
