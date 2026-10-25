package br.edu.superapi;

import java.awt.GraphicsEnvironment;
import javax.sound.sampled.*;

/** Ten original fixed chiptune scores: pulse lead, triangle bass and noise percussion. */
public final class RegionMusic implements AutoCloseable {
    public static final int RATE = 22050;
    private static final int[] BPM = {124,108,132,88,112,140,92,84,118,148};
    private static final int[] ROOT = {60,50,62,57,55,64,65,48,67,54};
    private static final int[][] MOTIFS = {
        {0,4,7,12,7,4,2,4,0,7,9,7,4,2,0,-1},
        {0,0,7,3,0,7,10,7,5,3,2,0,2,3,7,-1},
        {0,7,0,4,2,9,2,5,4,11,4,7,5,12,7,4},
        {0,-1,7,10,12,-1,10,7,5,-1,3,7,2,-1,0,-1},
        {0,1,4,7,8,7,4,1,0,4,5,8,7,4,1,-1},
        {0,4,7,11,14,11,7,4,2,5,9,12,16,12,9,5},
        {12,-1,7,4,9,-1,7,2,4,-1,0,7,2,-1,0,-1},
        {0,-1,0,6,3,-1,2,0,7,-1,6,3,2,-1,0,-1},
        {0,7,9,7,4,-1,2,4,5,9,12,9,7,-1,4,2},
        {0,3,7,12,10,7,3,0,2,5,8,14,12,8,5,2}
    };
    public static String title(int region) {
        return new String[]{"Pulso verde","Chaves em marcha","Engrenagens","Ecos prismáticos","Sinais ao vento",
                "Órbita suspensa","Flocos de sinal","Última frequência","Corrente azul","Aurora elétrica"}[region];
    }
    /** Eight bars, identical on every visit; no campaign RNG enters the score. */
    public static byte[] compose(int region) {
        if (region < 0 || region >= 10) throw new IllegalArgumentException("Região inválida");
        int stepSamples = RATE * 30 / BPM[region], length = stepSamples * 64;
        byte[] pcm = new byte[length * 2]; int noise = 0x12345;
        for (int i = 0; i < length; i++) {
            int step = i / stepSamples, local = i % stepSamples;
            double t = local / (double)RATE, fraction = local / (double)stepSamples;
            int motif = MOTIFS[region][step % 16];
            int transpose = new int[]{0,5,7,0}[step / 16];
            double envelope = Math.min(1, t / .006) * Math.max(0, Math.min(1, (1 - fraction) / .12));
            double lead = motif < 0 ? 0 : (Math.sin(2 * Math.PI * frequency(ROOT[region] + motif + transpose) * t) >= 0 ? .14 : -.14) * envelope;
            double phase = (i / (double)RATE * frequency(ROOT[region] - 24 + transpose)) % 1;
            double bass = (1 - 4 * Math.abs(phase - .5)) * .12 * envelope;
            noise ^= noise << 13; noise ^= noise >>> 17; noise ^= noise << 5;
            double drum = (noise / (double)Integer.MAX_VALUE) * .045 * Math.exp(-t * 45);
            if (step % 4 == 0) drum += Math.sin(2 * Math.PI * (75 * t - 40 * t * t)) * .16 * Math.exp(-t * 22);
            short sample = (short)((lead + bass + drum) * 32767);
            pcm[i * 2] = (byte)sample; pcm[i * 2 + 1] = (byte)(sample >> 8);
        }
        return pcm;
    }
    private static double frequency(int note) { return 440 * Math.pow(2, (note - 69) / 12.0); }
    private volatile int requested = -1;
    private volatile boolean audible, closed;
    private boolean muted;
    private Thread worker;
    public boolean muted() { return muted; }
    public synchronized void toggleMute() { muted = !muted; }
    public synchronized void update(int region, boolean active) {
        requested = region; audible = active && !muted;
        if (worker == null && audible && !closed && !GraphicsEnvironment.isHeadless()) {
            worker = new Thread(this::play, "region-chiptune"); worker.setDaemon(true); worker.start();
        }
    }
    private void play() {
        Clip clip = null; int loaded = -1;
        try {
            while (!closed) {
                if (requested != loaded && audible) {
                    if (clip != null) { clip.stop(); clip.close(); }
                    loaded = requested; byte[] pcm = compose(loaded);
                    clip = AudioSystem.getClip(); clip.open(new AudioFormat(RATE, 16, 1, true, false), pcm, 0, pcm.length);
                }
                if (clip != null) {
                    if (audible && requested == loaded && !clip.isRunning()) clip.loop(Clip.LOOP_CONTINUOUSLY);
                    else if (!audible && clip.isRunning()) clip.stop();
                }
                Thread.sleep(40);
            }
        } catch (LineUnavailableException | IllegalArgumentException | SecurityException ex) {
            // A missing audio device must never interrupt the expedition.
            System.err.println("Áudio indisponível: a travessia continuará sem música.");
        } catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
        finally { if (clip != null) { clip.stop(); clip.close(); } }
    }
    @Override public void close() { closed = true; if (worker != null) worker.interrupt(); }
}
