package de.nebula.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/** Speichert Position und Zustand der HUD-Module in config/nebula-client.json. */
public final class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("nebula-client.json");

    static final class Saved {
        boolean on;
        int x, y;
    }

    private Config() {}

    public static void load() {
        try {
            if (!Files.exists(FILE)) return;
            Map<String, Saved> map = GSON.fromJson(Files.readString(FILE), new TypeToken<Map<String, Saved>>() {}.getType());
            if (map == null) return;
            for (HudModule m : Hud.MODULES) {
                Saved s = map.get(m.id);
                if (s != null) {
                    m.enabled = s.on;
                    m.x = s.x;
                    m.y = s.y;
                }
            }
        } catch (Exception e) {
            System.err.println("[Nebula] Konfiguration konnte nicht gelesen werden: " + e);
        }
    }

    public static void save() {
        try {
            Map<String, Saved> map = new HashMap<>();
            for (HudModule m : Hud.MODULES) {
                Saved s = new Saved();
                s.on = m.enabled;
                s.x = m.x;
                s.y = m.y;
                map.put(m.id, s);
            }
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(map));
        } catch (Exception e) {
            System.err.println("[Nebula] Konfiguration konnte nicht gespeichert werden: " + e);
        }
    }
}
