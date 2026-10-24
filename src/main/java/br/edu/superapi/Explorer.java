package br.edu.superapi;

import java.awt.Color;
import java.awt.Graphics2D;

/** Original, code-drawn pixel avatars; all share the same physics and hitbox. */
public enum Explorer {
    NEY("Ney", "O driblador", 0xb8794d, 0xe5c16a, 0xf3ce36),
    LEO("Leo", "O maestro", 0xe3b18b, 0x57352a, 0x80cbe5),
    CRIS("Cris", "O artilheiro", 0xc98e64, 0x25232a, 0xb82f42),
    DILA("Dila", "A comandante", 0xe6af92, 0x743c2d, 0xd3444b);

    public final String displayName, subtitle;
    private final Color skin, hair, shirt;
    Explorer(String name, String subtitle, int skin, int hair, int shirt) {
        this.displayName = name; this.subtitle = subtitle;
        this.skin = new Color(skin); this.hair = new Color(hair); this.shirt = new Color(shirt);
    }
    private static void pixels(Graphics2D g, Color color, int x, int y, int w, int h) {
        g.setColor(color); g.fillRect(x * 2, y * 2, w * 2, h * 2);
    }
    public void draw(Graphics2D source, int x, int y, int scale) {
        Graphics2D g = (Graphics2D)source.create(); g.translate(x, y); g.scale(scale, scale);
        Color dark = new Color(29, 33, 45), white = new Color(248, 240, 218);
        pixels(g, hair, 3, 1, 9, 7);
        pixels(g, skin, 4, 3, 7, 7); pixels(g, skin, 3, 5, 9, 3);
        pixels(g, skin, 6, 9, 3, 2);
        pixels(g, shirt, 2, 10, 10, 6);
        pixels(g, skin, 0, 11, 2, 5); pixels(g, skin, 12, 11, 2, 5);
        pixels(g, dark, 3, 16, 3, 3); pixels(g, dark, 8, 16, 3, 3);
        pixels(g, white, 2, 18, 4, 1); pixels(g, white, 8, 18, 4, 1);
        pixels(g, dark, 5, 5, 1, 1); pixels(g, dark, 9, 5, 1, 1);
        pixels(g, new Color(167, 99, 75), 7, 8, 2, 1);
        switch (this) {
            case NEY -> {
                pixels(g, new Color(67, 41, 30), 3, 2, 2, 3);
                pixels(g, hair, 5, 0, 5, 3); pixels(g, hair, 8, 0, 3, 1);
                pixels(g, new Color(45, 116, 75), 2, 10, 10, 1);
                pixels(g, dark, 5, 12, 1, 3); pixels(g, dark, 8, 12, 3, 3);
                pixels(g, shirt, 9, 13, 1, 1); pixels(g, white, 11, 7, 1, 1);
            }
            case LEO -> {
                pixels(g, hair, 3, 1, 9, 2); pixels(g, hair, 3, 3, 2, 2);
                pixels(g, hair, 4, 7, 1, 2); pixels(g, hair, 10, 7, 1, 2);
                pixels(g, hair, 5, 9, 5, 1); pixels(g, hair, 6, 8, 3, 1);
                for (int i = 3; i < 12; i += 3) pixels(g, white, i, 10, 1, 6);
                pixels(g, dark, 6, 12, 1, 3); pixels(g, dark, 8, 12, 3, 3);
                pixels(g, shirt, 9, 13, 1, 1);
            }
            case CRIS -> {
                pixels(g, hair, 3, 2, 9, 2); pixels(g, hair, 5, 0, 7, 2);
                pixels(g, new Color(102, 91, 83), 7, 1, 4, 1);
                pixels(g, new Color(44, 106, 69), 2, 10, 2, 6);
                pixels(g, white, 6, 12, 4, 1); pixels(g, white, 9, 13, 1, 2);
            }
            case DILA -> {
                pixels(g, hair, 2, 2, 3, 6); pixels(g, hair, 10, 2, 3, 6);
                pixels(g, hair, 4, 0, 7, 3); pixels(g, hair, 3, 1, 9, 2);
                pixels(g, white, 5, 10, 1, 1); pixels(g, white, 6, 11, 3, 1);
                pixels(g, white, 9, 10, 1, 1);
                pixels(g, new Color(136, 34, 47), 6, 13, 1, 3);
                pixels(g, new Color(250, 212, 104), 10, 12, 1, 1);
            }
        }
        g.dispose();
    }
}
