package br.edu.superapi;

import java.awt.Rectangle;

/** Deterministic moving ice lifts and recoverable crumbling salvage platforms. */
public final class TerrainPlatform extends Rectangle {
    public enum Kind { ICE_LIFT, CRUMBLING }
    public final Kind kind;
    private final int originY;
    private final int amplitude;
    private final double speed, phase;
    private int age, stress, recovery;
    private final Rectangle coin;
    public TerrainPlatform(int x, int y, int width, Kind kind, Rectangle coin) {
        this(x,y,width,kind,coin,25,.025,0);
    }
    public TerrainPlatform(int x, int y, int width, Kind kind, Rectangle coin, int amplitude, double speed, double phase) {
        super(x,y,width,20); this.originY=y; this.kind=kind; this.coin=coin;
        this.amplitude=amplitude; this.speed=speed; this.phase=phase;
        resetMotion();
    }
    public boolean active() { return recovery == 0; }
    public boolean cracking() { return stress > 0; }
    public int tick(boolean carrying) {
        int before = y;
        if (kind == Kind.ICE_LIFT) {
            age++; y = originY + (int)(amplitude * Math.sin(age * speed + phase));
            coin.y += y - before;
        } else if (recovery > 0) {
            if (--recovery == 0) stress = 0;
        } else if ((carrying || stress > 0) && ++stress >= 36) {
            recovery = 180;
        }
        return y - before;
    }
    public void resetMotion() { int initial = originY + (kind == Kind.ICE_LIFT ? (int)(amplitude * Math.sin(phase)) : 0); coin.y += initial - y; y = initial; age = stress = recovery = 0; }
}
