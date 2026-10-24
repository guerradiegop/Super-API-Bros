package br.edu.superapi;

import java.awt.Rectangle;

/** Deterministic moving ice lifts and recoverable crumbling salvage platforms. */
public final class TerrainPlatform extends Rectangle {
    public enum Kind { ICE_LIFT, CRUMBLING }
    public final Kind kind;
    private final int originY;
    private int age, stress, recovery;
    private final Rectangle coin;
    public TerrainPlatform(int x, int y, int width, Kind kind, Rectangle coin) {
        super(x, y, width, 20); this.originY = y; this.kind = kind; this.coin = coin;
    }
    public boolean active() { return recovery == 0; }
    public boolean cracking() { return stress > 0; }
    public int tick(boolean carrying) {
        int before = y;
        if (kind == Kind.ICE_LIFT) {
            age++; y = originY + (int)(25 * Math.sin(age * .025));
            coin.y += y - before;
        } else if (recovery > 0) {
            if (--recovery == 0) stress = 0;
        } else if ((carrying || stress > 0) && ++stress >= 36) {
            recovery = 180;
        }
        return y - before;
    }
    public void resetMotion() { coin.y += originY - y; y = originY; age = stress = recovery = 0; }
}
