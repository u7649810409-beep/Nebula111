package de.nebula.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class NebulaClient implements ClientModInitializer {
    private static KeyBinding menuKey;
    private static KeyBinding zoomKey;
    private static final KeyBinding.Category CATEGORY = KeyBinding.Category.create(Identifier.of("nebula", "main"));
    private static boolean zooming;
    private static int savedFov;

    @Override
    public void onInitializeClient() {
        Config.load();
        menuKey = KeyBindingHelper.registerKeyBinding(
            new KeyBinding("key.nebula.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, CATEGORY));
        zoomKey = KeyBindingHelper.registerKeyBinding(
            new KeyBinding("key.nebula.zoom", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_C, CATEGORY));

        ClientTickEvents.END_CLIENT_TICK.register(NebulaClient::tick);
        // Beim Beenden mit gehaltener Zoom-Taste den echten FOV-Wert zurückschreiben
        ClientLifecycleEvents.CLIENT_STOPPING.register(mc -> {
            if (zooming) {
                mc.options.getFov().setValue(savedFov);
                zooming = false;
            }
        });
        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, Identifier.of("nebula", "hud"),
            (ctx, tickCounter) -> Hud.render(ctx, MinecraftClient.getInstance()));
    }

    private static void tick(MinecraftClient mc) {
        while (menuKey.wasPressed()) {
            if (mc.player != null && mc.currentScreen == null) mc.setScreen(new NebulaMenuScreen());
        }
        // Zoom: solange die Taste gehalten wird, ist das Sichtfeld auf das Minimum gestellt
        boolean want = zoomKey.isPressed() && mc.currentScreen == null && mc.player != null;
        if (want && !zooming) {
            savedFov = mc.options.getFov().getValue();
            mc.options.getFov().setValue(30);
            zooming = true;
        } else if (!want && zooming) {
            mc.options.getFov().setValue(savedFov);
            zooming = false;
        }
    }
}
