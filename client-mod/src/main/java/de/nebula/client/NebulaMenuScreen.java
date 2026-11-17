package de.nebula.client;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/** Das Ingame-Menü (Rechtsshift) im Nebula-Launcher-Design. */
public class NebulaMenuScreen extends Screen {
    private static final String[] TABS = {"Module", "HUD-Editor", "Spiele", "Info"};
    private static int tab = 0; // wird über Öffnungen hinweg gemerkt

    private record Hit(int x, int y, int w, int h, Runnable action) {}

    private final List<Hit> hits = new ArrayList<>();
    private HudModule dragging;
    private int dragDx, dragDy;

    public NebulaMenuScreen() {
        super(Text.literal("Nebula"));
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        Config.save();
        super.close();
    }

    private void button(DrawContext c, int mx, int my, int x, int y, int w, int h, String label, boolean active, Runnable r) {
        boolean hover = Ui.inside(mx, my, x, y, w, h);
        Ui.panel(c, x, y, w, h, active ? Ui.ACCENT : hover ? Ui.BTN_HOVER : Ui.BTN);
        Ui.centered(c, label, x + w / 2, y + (h - 8) / 2 + 1, Ui.TEXT);
        hits.add(new Hit(x, y, w, h, r));
    }

    @Override
    public void render(DrawContext c, int mx, int my, float delta) {
        hits.clear();
        if (tab == 1) renderEditor(c, mx, my);
        else renderMenu(c, mx, my);
    }

    // ---------------------------------------------------------------- Menü

    private void renderMenu(DrawContext c, int mx, int my) {
        c.fill(0, 0, width, height, 0xA0070812);
        int pw = Math.min(width - 20, 500);
        int ph = Math.min(height - 20, 300);
        int px = (width - pw) / 2;
        int py = (height - ph) / 2;
        Ui.panel(c, px, py, pw, ph, Ui.PANEL);

        // Seitenleiste
        Ui.panel(c, px + 8, py + 8, 96, ph - 16, Ui.SIDEBAR);
        Ui.text(c, "NEBULA", px + 16, py + 18, Ui.ACCENT);
        Ui.text(c, "CLIENT", px + 16, py + 30, Ui.ACCENT2);
        for (int i = 0; i < TABS.length; i++) {
            final int idx = i;
            button(c, mx, my, px + 14, py + 54 + i * 26, 84, 22, TABS[i], tab == i, () -> tab = idx);
        }
        Ui.text(c, "Rechtsshift: Menü", px + 14, py + ph - 30, Ui.MUTED);
        Ui.text(c, "C halten: Zoom", px + 14, py + ph - 20, Ui.MUTED);

        // Inhalt
        int cx = px + 112;
        int cy = py + 8;
        int cw = pw - 120;
        int ch = ph - 16;
        Ui.panel(c, cx, cy, cw, ch, Ui.PANEL2);
        switch (tab) {
            case 0 -> renderModules(c, mx, my, cx, cy, cw, ch);
            case 2 -> renderGames(c, mx, my, cx, cy, cw);
            default -> renderInfo(c, cx, cy, cw);
        }
    }

    private void renderModules(DrawContext c, int mx, int my, int cx, int cy, int cw, int ch) {
        Ui.text(c, "HUD-MODULE", cx + 12, cy + 12, Ui.MUTED);
        int colW = (cw - 36) / 2;
        for (int i = 0; i < Hud.MODULES.size(); i++) {
            HudModule m = Hud.MODULES.get(i);
            int x = cx + 12 + (i % 2) * (colW + 12);
            int y = cy + 28 + (i / 2) * 26;
            boolean hover = Ui.inside(mx, my, x, y, colW, 22);
            Ui.panel(c, x, y, colW, 22, hover ? Ui.BTN_HOVER : Ui.PANEL);
            Ui.text(c, m.name, x + 8, y + 7, m.enabled ? Ui.TEXT : Ui.MUTED);
            Ui.toggle(c, x + colW - 36, y + 4, m.enabled);
            hits.add(new Hit(x, y, colW, 22, () -> { m.enabled = !m.enabled; Config.save(); }));
        }
        int by = cy + ch - 34;
        button(c, mx, my, cx + 12, by, 130, 22, "HUD-Editor öffnen", false, () -> tab = 1);
        Ui.text(c, "Module an/aus mit einem Klick", cx + 152, by + 7, Ui.MUTED);
    }

    private void renderGames(DrawContext c, int mx, int my, int cx, int cy, int cw) {
        Ui.text(c, "MINISPIELE", cx + 12, cy + 12, Ui.MUTED);
        int w = (cw - 36) / 2;
        card(c, mx, my, cx + 12, cy + 28, w, "Pong", "Maus bewegen,\ngegen den Computer", () -> client.setScreen(new PongScreen(this)));
        card(c, mx, my, cx + 24 + w, cy + 28, w, "Tetris", "Pfeile / WASD,\nLeertaste = fallen", () -> client.setScreen(new TetrisScreen(this)));
    }

    private void card(DrawContext c, int mx, int my, int x, int y, int w, String title, String desc, Runnable r) {
        boolean hover = Ui.inside(mx, my, x, y, w, 80);
        Ui.panel(c, x, y, w, 80, hover ? Ui.BTN_HOVER : Ui.PANEL);
        Ui.text(c, title, x + 10, y + 10, Ui.ACCENT2);
        int ly = y + 28;
        for (String line : desc.split("\n")) {
            Ui.text(c, line, x + 10, ly, Ui.MUTED);
            ly += 11;
        }
        Ui.text(c, "Spielen ›", x + 10, y + 62, Ui.TEXT);
        hits.add(new Hit(x, y, w, 80, r));
    }

    private void renderInfo(DrawContext c, int cx, int cy, int cw) {
        Ui.text(c, "INFO", cx + 12, cy + 12, Ui.MUTED);
        String[] lines = {
            "Nebula Client " + "1.0",
            "",
            "Rechtsshift  Menü öffnen / schließen",
            "C (halten)   Zoom",
            "Tasten ändern: Optionen > Steuerung",
            "",
            "Alle HUD-Module sind verschiebbar.",
            "Einstellungen: config/nebula-client.json",
            "",
            "Nicht enthalten: Cosmetics, Freundesliste und",
            "gehostete Welten – dafür braucht es einen Server.",
        };
        int y = cy + 28;
        for (String l : lines) {
            Ui.text(c, l, cx + 12, y, l.startsWith("Nebula") ? Ui.ACCENT : Ui.TEXT);
            y += 11;
        }
    }

    // ---------------------------------------------------------------- HUD-Editor

    private void renderEditor(DrawContext c, int mx, int my) {
        c.fill(0, 0, width, height, 0x70070812);
        Ui.centered(c, "HUD-Editor: Module mit der Maus ziehen", width / 2, 8, Ui.TEXT);
        button(c, mx, my, width / 2 - 40, 22, 80, 20, "Fertig", true, () -> { tab = 0; Config.save(); });
        for (HudModule m : Hud.MODULES) {
            if (!m.enabled) continue;
            int w = m.width(client);
            int h = m.height();
            int col = m == dragging || Ui.inside(mx, my, m.x, m.y, w, h) ? Ui.ACCENT2 : Ui.ACCENT;
            c.fill(m.x - 1, m.y - 1, m.x + w + 1, m.y, col);
            c.fill(m.x - 1, m.y + h, m.x + w + 1, m.y + h + 1, col);
            c.fill(m.x - 1, m.y, m.x, m.y + h, col);
            c.fill(m.x + w, m.y, m.x + w + 1, m.y + h, col);
            Ui.text(c, m.name, m.x, m.y + h + 3, Ui.MUTED);
        }
    }

    // ---------------------------------------------------------------- Eingaben

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mx = click.x();
        double my = click.y();
        if (click.button() == 0) {
            for (int i = hits.size() - 1; i >= 0; i--) {
                Hit h = hits.get(i);
                if (Ui.inside(mx, my, h.x, h.y, h.w, h.h)) {
                    h.action.run();
                    return true;
                }
            }
            if (tab == 1) {
                for (int i = Hud.MODULES.size() - 1; i >= 0; i--) {
                    HudModule m = Hud.MODULES.get(i);
                    if (m.enabled && Ui.inside(mx, my, m.x, m.y, m.width(client), m.height())) {
                        dragging = m;
                        dragDx = (int) mx - m.x;
                        dragDy = (int) my - m.y;
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double dx, double dy) {
        double mx = click.x();
        double my = click.y();
        if (dragging != null) {
            dragging.x = Math.max(0, Math.min(width - dragging.width(client), (int) mx - dragDx));
            dragging.y = Math.max(0, Math.min(height - dragging.height(), (int) my - dragDy));
            return true;
        }
        return super.mouseDragged(click, dx, dy);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (dragging != null) {
            dragging = null;
            Config.save();
            return true;
        }
        return super.mouseReleased(click);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (input.key() == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            close();
            return true;
        }
        return super.keyPressed(input);
    }
}
