package de.nebula.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import org.lwjgl.glfw.GLFW;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.List;

/** Alle HUD-Module und ihre Darstellung im Spiel. */
public final class Hud {
    private static final ArrayDeque<Long> LEFT = new ArrayDeque<>();
    private static final ArrayDeque<Long> RIGHT = new ArrayDeque<>();
    private static boolean wasL, wasR;

    public static final List<HudModule> MODULES = List.of(
        new HudModule("fps", "FPS", true, 4, 4, mc -> mc.getCurrentFps() + " FPS"),
        new HudModule("coords", "Koordinaten", true, 4, 22,
            mc -> String.format("XYZ %.1f / %.1f / %.1f", mc.player.getX(), mc.player.getY(), mc.player.getZ())),
        new HudModule("direction", "Blickrichtung", false, 4, 40, mc -> switch (mc.player.getHorizontalFacing()) {
            case NORTH -> "Norden (-Z)";
            case SOUTH -> "Süden (+Z)";
            case WEST -> "Westen (-X)";
            case EAST -> "Osten (+X)";
            default -> "–";
        }),
        new HudModule("cps", "CPS", false, 4, 58, mc -> "CPS " + leftCps() + " | " + rightCps()),
        new HudModule("ping", "Ping", false, 4, 76, mc -> {
            PlayerListEntry e = mc.getNetworkHandler() == null ? null : mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
            return "Ping " + (e == null ? 0 : e.getLatency()) + " ms";
        }),
        new HudModule("clock", "Uhrzeit", false, 4, 94, mc -> LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))),
        new HudModule("memory", "Arbeitsspeicher", false, 4, 112, mc -> {
            Runtime r = Runtime.getRuntime();
            return "RAM " + ((r.totalMemory() - r.freeMemory()) >> 20) + " / " + (r.maxMemory() >> 20) + " MB";
        }),
        new HudModule("keys", "Tastenanzeige", false, 4, 134, null)
    );

    private Hud() {}

    public static int leftCps() { return LEFT.size(); }
    public static int rightCps() { return RIGHT.size(); }

    private static void trackClicks(MinecraftClient mc) {
        long now = System.currentTimeMillis();
        long win = mc.getWindow().getHandle();
        boolean l = mc.currentScreen == null && GLFW.glfwGetMouseButton(win, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean r = mc.currentScreen == null && GLFW.glfwGetMouseButton(win, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
        if (l && !wasL) LEFT.add(now);
        if (r && !wasR) RIGHT.add(now);
        wasL = l;
        wasR = r;
        while (!LEFT.isEmpty() && now - LEFT.peekFirst() > 1000) LEFT.pollFirst();
        while (!RIGHT.isEmpty() && now - RIGHT.peekFirst() > 1000) RIGHT.pollFirst();
    }

    public static void render(DrawContext ctx, MinecraftClient mc) {
        if (mc.player == null) return;
        trackClicks(mc);
        if (mc.options.hudHidden) return;
        for (HudModule m : MODULES) if (m.enabled) m.draw(ctx, mc);
    }
}
