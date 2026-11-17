package de.nebula.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

/** Farben und Zeichenhilfen im Look des Nebula Launchers (kantig, Kontur, Licht und Schatten). */
public final class Ui {
    public static final int BG = 0xFF0B0D14;
    public static final int PANEL = 0xFF131622;
    public static final int PANEL2 = 0xFF1A1E2E;
    public static final int SIDEBAR = 0xFF0E101A;
    public static final int BTN = 0xFF2C3250;
    public static final int BTN_HOVER = 0xFF3A4270;
    public static final int EDGE = 0xFF05060A;
    public static final int ACCENT = 0xFF7C5CFF;
    public static final int ACCENT2 = 0xFF2DD4BF;
    public static final int TEXT = 0xFFE8EAF4;
    public static final int MUTED = 0xFF8B92AD;
    public static final int OK = 0xFF3DDC97;
    private static final int HI = 0x2AFFFFFF;
    private static final int LO = 0x60000000;

    private Ui() {}

    /** Eckige Fläche mit Kontur; bevel = Licht oben links, Schatten unten rechts. */
    public static void panel(DrawContext c, int x, int y, int w, int h, int fill, boolean bevel) {
        c.fill(x, y, x + w, y + h, EDGE);
        c.fill(x + 2, y + 2, x + w - 2, y + h - 2, fill);
        if (bevel) {
            c.fill(x + 2, y + 2, x + w - 2, y + 4, HI);
            c.fill(x + 2, y + 2, x + 4, y + h - 2, HI);
            c.fill(x + 2, y + h - 4, x + w - 2, y + h - 2, LO);
            c.fill(x + w - 4, y + 2, x + w - 2, y + h - 2, LO);
        }
    }

    public static void panel(DrawContext c, int x, int y, int w, int h, int fill) {
        panel(c, x, y, w, h, fill, true);
    }

    public static void text(DrawContext c, String s, int x, int y, int color) {
        c.drawText(MinecraftClient.getInstance().textRenderer, s, x, y, color, true);
    }

    public static void centered(DrawContext c, String s, int cx, int y, int color) {
        var tr = MinecraftClient.getInstance().textRenderer;
        c.drawText(tr, s, cx - tr.getWidth(s) / 2, y, color, true);
    }

    public static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && my >= y && mx < x + w && my < y + h;
    }

    /** Schalter wie im Launcher (eingelassene Spur, eckiger Knopf). */
    public static void toggle(DrawContext c, int x, int y, boolean on) {
        c.fill(x, y, x + 28, y + 14, EDGE);
        c.fill(x + 2, y + 2, x + 26, y + 12, on ? 0xFF1C5C40 : 0xFF07080D);
        int kx = on ? x + 15 : x + 3;
        c.fill(kx, y + 3, kx + 10, y + 11, on ? 0xFFE8EAF4 : 0xFF8B92AD);
    }
}
