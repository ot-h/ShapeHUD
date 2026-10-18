package com.othman.shapehud;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.lwjgl.glfw.GLFW;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class ShapeConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("shapehud.json");

    /** Shortcut to open the editor. -1 = unused. Both keys must be held. */
    public int openKey1 = GLFW.GLFW_KEY_LEFT_ALT;
    public int openKey2 = GLFW.GLFW_KEY_H;
    public boolean snapToGrid = false;
    public int gridSize = 4;
    public int nextGroupId = 1;
    /** Editor: draw example content (chat, boss bar, title...) for elements that aren't on screen right now. */
    public boolean preview = true;
    public boolean panelRight = false;
    public boolean panelCollapsed = false;
    public boolean seenIntro = false;
    /** "auto" (follow the game language), "en" or "ar". */
    public String language = "auto";
    public Map<String, ElementState> elements = new LinkedHashMap<>();

    public ElementState get(HudElement e) {
        return elements.computeIfAbsent(e.name(), k -> new ElementState());
    }

    public static ShapeConfig load() {
        if (Files.exists(FILE)) {
            try (Reader r = Files.newBufferedReader(FILE)) {
                ShapeConfig c = GSON.fromJson(r, ShapeConfig.class);
                if (c != null) {
                    if (c.elements == null) c.elements = new LinkedHashMap<>();
                    return c;
                }
            } catch (Exception ex) {
                ShapeHud.LOGGER.error("Failed to read shapehud.json, using defaults", ex);
            }
        }
        ShapeConfig c = new ShapeConfig();
        c.save();
        return c;
    }

    public void save() {
        try {
            Files.createDirectories(FILE.getParent());
            try (Writer w = Files.newBufferedWriter(FILE)) { GSON.toJson(this, w); }
        } catch (Exception ex) {
            ShapeHud.LOGGER.error("Failed to save shapehud.json", ex);
        }
    }
}
