package de.nebula.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.function.Function;

/** Ein verschiebbares HUD-Element. Ohne text-Funktion ist es die Tastenanzeige (Keystrokes). */
public final class HudModule {
    public final String id;
    public final String name;
    public boolean enabled;
    public int x, y;
    private final Function<MinecraftClient, String> text;

    public HudModule(String id, String name, boolean enabled, int x, int y, Function<MinecraftClient, String> text) {
        this.id = id;
        this.name = name;
        this.enabled = enabled;
        this.x = x;
        this.y = y;
        this.text = text;
    }

    public int width(MinecraftClient mc) {
        if (text == null) return 58;
        return mc.textRenderer.getWidth(safe(mc)) + 10;
    }

    public int height() {
        return text == null ? 72 : 16;
    }

    private String safe(MinecraftClient mc) {
        try {
            return text.apply(mc);
        } catch (Exception e) {
            return name + ": –";
        }
    }

    public void draw(DrawContext ctx, MinecraftClient mc) {
        if (text != null) {
            int w = width(mc);
            Ui.panel(ctx, x, y, w, 16, Ui.PANEL, false);
            ctx.drawText(mc.textRenderer, safe(mc), x + 5, y + 4, Ui.TEXT, true);
        } else {
            drawKeys(ctx, mc);
        }
    }

    private void key(DrawContext ctx, MinecraftClient mc, int kx, int ky, int w, int h, String label, boolean down) {
        ctx.fill(kx, ky, kx + w, ky + h, down ? Ui.ACCENT : 0xB0131622);
        ctx.fill(kx, ky, kx + w, ky + 1, 0xFF05060A);
        ctx.fill(kx, ky + h - 1, kx + w, ky + h, 0xFF05060A);
        ctx.fill(kx, ky, kx + 1, ky + h, 0xFF05060A);
        ctx.fill(kx + w - 1, ky, kx + w, ky + h, 0xFF05060A);
        int tw = mc.textRenderer.getWidth(label);
        ctx.drawText(mc.textRenderer, label, kx + (w - tw) / 2, ky + (h - 8) / 2 + 1, Ui.TEXT, false);
    }

    private void drawKeys(DrawContext ctx, MinecraftClient mc) {
        var o = mc.options;
        key(ctx, mc, x + 20, y, 18, 18, "W", o.forwardKey.isPressed());
        key(ctx, mc, x, y + 20, 18, 18, "A", o.leftKey.isPressed());
        key(ctx, mc, x + 20, y + 20, 18, 18, "S", o.backKey.isPressed());
        key(ctx, mc, x + 40, y + 20, 18, 18, "D", o.rightKey.isPressed());
        key(ctx, mc, x, y + 40, 58, 14, "SPACE", o.jumpKey.isPressed());
        key(ctx, mc, x, y + 56, 28, 16, Hud.leftCps() + " CPS", o.attackKey.isPressed());
        key(ctx, mc, x + 30, y + 56, 28, 16, Hud.rightCps() + " CPS", o.useKey.isPressed());
    }
}
