package br.edu.superapi;

import java.awt.*;
import javax.swing.JFrame;

/** Borderless fullscreen on the current monitor, with a windowed fallback and restoration. */
public final class WindowMode {
    private final JFrame frame;
    private GraphicsDevice device;
    private Rectangle savedBounds;
    private int savedState;
    private boolean fullscreen;
    public WindowMode(JFrame frame) { this.frame = frame; }
    public boolean isFullscreen() { return fullscreen; }
    public void toggle() { if (fullscreen) exit(); else enter(); }
    private void enter() {
        savedBounds = frame.getBounds(); savedState = frame.getExtendedState();
        device = frame.getGraphicsConfiguration().getDevice();
        frame.dispose(); frame.setUndecorated(true); frame.setExtendedState(JFrame.NORMAL);
        // Use borderless monitor bounds everywhere; no display-resolution switch is necessary.
        frame.setBounds(device.getDefaultConfiguration().getBounds());
        fullscreen = true; frame.setVisible(true);
        if (device.isFullScreenSupported()) device.setFullScreenWindow(frame);
        frame.getContentPane().requestFocusInWindow();
    }
    public void exit() {
        if (!fullscreen) return;
        if (device.getFullScreenWindow() == frame) device.setFullScreenWindow(null);
        frame.dispose(); frame.setUndecorated(false); frame.setBounds(savedBounds);
        frame.setExtendedState(savedState); fullscreen = false;
        frame.setVisible(true); frame.getContentPane().requestFocusInWindow();
    }
}
