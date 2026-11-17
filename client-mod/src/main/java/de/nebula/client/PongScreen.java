package de.nebula.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.Random;

/** Pong gegen den Computer. Maus bewegt den Schläger, ESC geht zurück. */
public class PongScreen extends Screen {
    private static final int W = 300, H = 170, PH = 36;
    private final Screen parent;
    private final Random rnd = new Random();
    private double bx, by, vx, vy;
    private double playerY = H / 2.0, aiY = H / 2.0;
    private int mouseY, scoreP, scoreA;

    public PongScreen(Screen parent) {
        super(Text.literal("Pong"));
        this.parent = parent;
        serve(rnd.nextBoolean() ? 1 : -1);
    }

    private void serve(int dir) {
        bx = W / 2.0;
        by = H / 2.0;
        vx = 3.0 * dir;
        vy = (rnd.nextDouble() - 0.5) * 3.0;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }

    @Override
    public void mouseMoved(double x, double y) {
        mouseY = (int) y;
    }

    @Override
    public void tick() {
        int oy = (height - H) / 2;
        playerY = Math.max(PH / 2.0, Math.min(H - PH / 2.0, mouseY - oy));

        double d = by - aiY;
        aiY += Math.max(-2.6, Math.min(2.6, d));
        aiY = Math.max(PH / 2.0, Math.min(H - PH / 2.0, aiY));

        bx += vx;
        by += vy;
        if (by < 3) { by = 3; vy = Math.abs(vy); }
        if (by > H - 3) { by = H - 3; vy = -Math.abs(vy); }

        if (vx < 0 && bx <= 14 && bx >= 6 && Math.abs(by - playerY) <= PH / 2.0 + 3) {
            vx = Math.min(8, -vx * 1.06);
            vy += (by - playerY) * 0.12;
        }
        if (vx > 0 && bx >= W - 14 && bx <= W - 6 && Math.abs(by - aiY) <= PH / 2.0 + 3) {
            vx = -Math.min(8, vx * 1.06);
            vy += (by - aiY) * 0.12;
        }
        if (bx < 0) { scoreA++; serve(1); }
        if (bx > W) { scoreP++; serve(-1); }
    }

    @Override
    public void render(DrawContext c, int mx, int my, float delta) {
        c.fill(0, 0, width, height, 0xC0070812);
        int ox = (width - W) / 2;
        int oy = (height - H) / 2;
        Ui.panel(c, ox - 6, oy - 6, W + 12, H + 12, Ui.PANEL);
        c.fill(ox, oy, ox + W, oy + H, 0xFF07080D);
        for (int y = 0; y < H; y += 10) c.fill(ox + W / 2 - 1, oy + y, ox + W / 2 + 1, oy + y + 5, Ui.BTN);
        c.fill(ox + 8, (int) (oy + playerY - PH / 2.0), ox + 12, (int) (oy + playerY + PH / 2.0), Ui.ACCENT);
        c.fill(ox + W - 12, (int) (oy + aiY - PH / 2.0), ox + W - 8, (int) (oy + aiY + PH / 2.0), Ui.ACCENT);
        c.fill((int) (ox + bx - 3), (int) (oy + by - 3), (int) (ox + bx + 3), (int) (oy + by + 3), Ui.ACCENT2);
        Ui.centered(c, scoreP + "   :   " + scoreA, width / 2, oy - 20, Ui.TEXT);
        Ui.centered(c, "Maus bewegen  |  ESC: zurück", width / 2, oy + H + 12, Ui.MUTED);
    }
}
