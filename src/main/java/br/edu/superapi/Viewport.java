package br.edu.superapi;

import java.awt.Point;

/** One transform for drawing and hit-testing, preserving the 1000:600 aspect ratio. */
public record Viewport(double scale, double left, double top) {
    public static Viewport fit(int width, int height) {
        double scale = Math.max(.001, Math.min(width / 1000.0, height / 600.0));
        return new Viewport(scale, (width - 1000 * scale) / 2, (height - 600 * scale) / 2);
    }
    public Point toLogical(int x, int y) {
        double lx = (x - left) / scale, ly = (y - top) / scale;
        if (lx < 0 || ly < 0 || lx >= 1000 || ly >= 600) return null;
        return new Point((int)lx, (int)ly);
    }
}
