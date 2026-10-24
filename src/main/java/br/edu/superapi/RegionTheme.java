package br.edu.superapi;

import java.awt.Color;
import java.awt.Graphics2D;

/** Palettes and silhouettes are kept together so every region has its own identity. */
public record RegionTheme(Color sky, Color horizon, Color stone, Color accent) {
    public static final RegionTheme[] ALL = {
        theme(0x102d39, 0x428b70, 0x294b42, 0x78df9b),
        theme(0x211e40, 0x63537e, 0x443c59, 0xc6a8ef),
        theme(0x38271e, 0xb87846, 0x624331, 0xf2ba65),
        theme(0x100f32, 0x42356e, 0x302a58, 0x6ce9ef),
        theme(0x633e54, 0xefb36c, 0x986447, 0xffd794),
        theme(0x102448, 0x507ba0, 0x304d6c, 0xa1e8ff),
        theme(0x264f78, 0xb6dfef, 0x588ba8, 0xf1fcff),
        theme(0x302a24, 0xa88354, 0x574c3f, 0xb7bb65),
        theme(0x123c56, 0x4eb5b5, 0x416f83, 0x7cf2dc),
        theme(0x251647, 0x825686, 0x443757, 0xffba67)
    };
    private static RegionTheme theme(int sky, int horizon, int stone, int accent) {
        return new RegionTheme(new Color(sky), new Color(horizon), new Color(stone), new Color(accent));
    }
    public void drawBackdrop(Graphics2D g, int camera, int region) {
        for (int layer = 0; layer < 2; layer++) {
            g.setColor(layer == 0 ? stone.darker() : stone);
            for (int i = -1; i < 8; i++) {
                int x = i * 190 - Math.floorMod(camera / (layer == 0 ? 5 : 3), 190);
                int top = 235 + (i & 1) * 45 + layer * 55;
                switch (region) {
                    case 0 -> { // Branches and overlapping forest canopy.
                        g.fillRect(x + 65, top, 22, 300);
                        g.fillOval(x, top - 65, 150, 135);
                    }
                    case 1 -> { // Crenellated fortress walls.
                        g.fillRect(x + 20, top, 125, 300);
                        for (int n = 0; n < 4; n++) g.fillRect(x + 20 + n * 35, top - 18, 20, 22);
                    }
                    case 2 -> { // Workshop scaffolds, girders and wheels.
                        g.fillRect(x + 20, top, 12, 300); g.fillRect(x + 140, top, 12, 300);
                        g.fillRect(x + 20, top, 132, 14); g.drawLine(x + 20, top, x + 152, 490);
                        g.drawOval(x + 60, top + 45, 70, 70);
                    }
                    case 3 -> { // Crystal clusters and stalactites.
                        g.fillPolygon(new int[]{x, x + 45, x + 100, x + 70}, new int[]{490, top - 35, 490, 515}, 4);
                        g.fillPolygon(new int[]{x, x + 50, x + 85}, new int[]{82, 190 + layer * 20, 82}, 3);
                    }
                    case 4 -> { // Dunes and distant sandstone arches.
                        g.fillOval(x - 100, top + 80, 400, 260);
                        g.drawArc(x + 35, top, 100, 190, 0, 180);
                    }
                    case 5 -> { // Floating islands and suspended towers.
                        g.fillPolygon(new int[]{x, x + 150, x + 75}, new int[]{top + 100, top + 100, top + 155}, 3);
                        g.fillRect(x + 45, top - 50, 50, 150);
                        g.drawLine(x + 150, top + 100, x + 190, top + 100);
                    }
                    case 6 -> {
                        g.fillPolygon(new int[]{x - 80, x + 70, x + 220}, new int[]{490, top - 80, 490}, 3);
                        g.setColor(new Color(216, 242, 250));
                        g.fillPolygon(new int[]{x + 36, x + 70, x + 105}, new int[]{top + 4, top - 80, top + 4}, 3);
                        g.setColor(layer == 0 ? stone.darker() : stone);
                        g.fillRect(x + 145, top + 55, 8, 160);
                        g.fillPolygon(new int[]{x + 110, x + 150, x + 185}, new int[]{top + 115, top + 25, top + 115}, 3);
                    }
                    case 7 -> {
                        g.fillPolygon(new int[]{x + 15, x + 15, x + 50, x + 65, x + 115, x + 130, x + 130},
                                new int[]{490, top - 35, top - 35, top + 10, top - 10, top + 35, 490}, 7);
                        g.setColor(horizon.darker());
                        for (int row = 0; row < 4; row++) {
                            g.fillRect(x + 30, top + 40 + row * 35, 15, 20);
                            g.fillRect(x + 85, top + 40 + row * 35, 15, 20);
                        }
                        g.setColor(layer == 0 ? stone.darker() : stone);
                        g.drawLine(x + 130, top + 60, x + 190, top + 100);
                        g.fillOval(x + 95, top - 100, 45, 80);
                    }
                    case 8 -> {
                        g.fillOval(x - 20, top + 80, 180, 240);
                        g.fillRect(x + 60, top + 20, 12, 100);
                        g.drawArc(x + 20, top + 10, 100, 50, 0, 180);
                        g.setColor(accent);
                        g.drawArc(x - 30, 460 + layer * 10, 160, 30, 0, 180);
                        g.setColor(layer == 0 ? stone.darker() : stone);
                    }
                    case 9 -> {
                        g.fillRect(x + 25, top, 110, 300);
                        g.drawOval(x + 35, top - 65, 90, 120);
                        g.setColor(accent);
                        g.drawOval(x + 50, top - 45, 60, 80);
                        g.drawLine(x + 80, top + 35, x + 80, 490);
                        g.setColor(layer == 0 ? stone.darker() : stone);
                    }
                    default -> throw new IllegalArgumentException("Unknown region");
                }
            }
        }
        g.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 130));
        for (int i = 0; i < 24; i++) {
            int x = Math.floorMod(i * 137 - camera / 2, 1000);
            g.fillOval(x, 115 + i * 43 % 300, region == 6 ? 6 : region == 3 ? 5 : 3, region == 4 ? 2 : 5);
        }
    }
}
