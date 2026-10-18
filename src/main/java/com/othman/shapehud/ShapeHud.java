package com.othman.shapehud;

import com.othman.shapehud.screen.EditorScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShapeHud implements ClientModInitializer {
    public static final String MOD_ID = "shapehud";
    public static final Logger LOGGER = LoggerFactory.getLogger("ShapeHUD");

    private static ShapeConfig config;
    private static boolean comboWasDown = false;

    public static ShapeConfig config() {
        if (config == null) config = ShapeConfig.load();
        return config;
    }

    @Override
    public void onInitializeClient() {
        config();
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            boolean down = isComboDown(mc);
            if (down && !comboWasDown && mc.screen == null && mc.player != null) {
                mc.setScreen(new EditorScreen(null));
            }
            comboWasDown = down;
        });
    }

    private static boolean isComboDown(Minecraft mc) {
        int k1 = config().openKey1, k2 = config().openKey2;
        if (k1 < 0 && k2 < 0) return false;
        long handle = mc.getWindow().handle();
        return (k1 < 0 || GLFW.glfwGetKey(handle, k1) == GLFW.GLFW_PRESS)
            && (k2 < 0 || GLFW.glfwGetKey(handle, k2) == GLFW.GLFW_PRESS);
    }

    /** Human readable shortcut, e.g. "Left Alt + H". */
    public static String comboName() {
        int k1 = config().openKey1, k2 = config().openKey2;
        if (k1 < 0) return KeyNames.name(k2);
        if (k2 < 0) return KeyNames.name(k1);
        return KeyNames.name(k1) + " + " + KeyNames.name(k2);
    }
}
