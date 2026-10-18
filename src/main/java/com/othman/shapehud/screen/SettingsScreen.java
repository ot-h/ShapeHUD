package com.othman.shapehud.screen;

import com.othman.shapehud.ShapeHud;
import com.othman.shapehud.HudElement;
import com.othman.shapehud.KeyNames;
import com.othman.shapehud.L;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/** Settings: editor shortcut (two keys), grid snapping, open editor, reset. Opened from Mod Menu or the editor. */
public class SettingsScreen extends Screen {
    private final Screen parent;
    /** 0 = not listening, 1 = waiting for key 1, 2 = waiting for key 2. */
    private int listening = 0;
    private Button key1Button, key2Button, snapButton, gridButton, langButton;

    public SettingsScreen(Screen parent) {
        super(Component.literal("ShapeHUD Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = width / 2, y = height / 4, w = 200, h = 20, gap = 24;
        key1Button = addRenderableWidget(Button.builder(Component.empty(), b -> listening = 1).bounds(cx - w / 2, y, w, h).build());
        key2Button = addRenderableWidget(Button.builder(Component.empty(), b -> listening = 2).bounds(cx - w / 2, y += gap, w, h).build());
        snapButton = addRenderableWidget(Button.builder(Component.empty(), b -> {
            ShapeHud.config().snapToGrid = !ShapeHud.config().snapToGrid;
            ShapeHud.config().save();
        }).bounds(cx - w / 2, y += gap, w / 2 - 2, h).build());
        gridButton = addRenderableWidget(Button.builder(Component.empty(), b -> {
            int[] sizes = {1, 2, 4, 5, 8, 10, 16};
            int cur = ShapeHud.config().gridSize, next = sizes[0];
            for (int i = 0; i < sizes.length; i++) if (sizes[i] == cur) next = sizes[(i + 1) % sizes.length];
            ShapeHud.config().gridSize = next;
            ShapeHud.config().save();
        }).bounds(cx + 2, y, w / 2 - 2, h).build());

        langButton = addRenderableWidget(Button.builder(Component.empty(), b -> {
            String l = ShapeHud.config().language;
            ShapeHud.config().language = "auto".equals(l) ? "en" : "en".equals(l) ? "ar" : "auto";
            ShapeHud.config().save();
            rebuildWidgets();
        }).bounds(cx - w / 2, y += gap, w, h).build());

        Button open = addRenderableWidget(Button.builder(L.c("set.open"),
            b -> minecraft.setScreen(new EditorScreen(parent instanceof EditorScreen ? null : this))).bounds(cx - w / 2, y += gap + 8, w, h).build());
        open.active = minecraft.level != null;

        addRenderableWidget(Button.builder(L.c("set.resetAll"), b -> {
            for (HudElement e : HudElement.values()) { ShapeHud.config().get(e).reset(); ShapeHud.config().get(e).group = null; }
            ShapeHud.config().save();
        }).bounds(cx - w / 2, y += gap, w, h).build());

        addRenderableWidget(Button.builder(L.c("btn.done"), b -> onClose()).bounds(cx - w / 2, y += gap + 8, w, h).build());
        refreshLabels();
    }

    private void refreshLabels() {
        var c = ShapeHud.config();
        key1Button.setMessage(L.c("set.key1", listening == 1 ? L.raw("set.press") : KeyNames.name(c.openKey1)));
        key2Button.setMessage(L.c("set.key2", listening == 2 ? L.raw("set.press") : KeyNames.name(c.openKey2)));
        snapButton.setMessage(L.c("set.snap", L.raw(c.snapToGrid ? "on" : "off")));
        gridButton.setMessage(L.c("set.grid", c.gridSize));
        langButton.setMessage(L.c("set.lang", L.raw("lang." + c.language)));
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (listening != 0) {
            int key = event.key();
            if (key != GLFW.GLFW_KEY_ESCAPE) {
                int value = (key == GLFW.GLFW_KEY_BACKSPACE || key == GLFW.GLFW_KEY_DELETE) ? -1 : key;
                if (listening == 1) ShapeHud.config().openKey1 = value; else ShapeHud.config().openKey2 = value;
                ShapeHud.config().save();
            }
            listening = 0;
            refreshLabels();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        refreshLabels();
        super.render(g, mouseX, mouseY, delta);
        g.drawCenteredString(font, L.c("set.title"), width / 2, height / 4 - 30, 0xFFFFFFFF);
        g.drawCenteredString(font, L.c("set.help"), width / 2, height / 4 - 16, 0xFFAAAAAA);
    }

    @Override
    public void onClose() {
        ShapeHud.config().save();
        minecraft.setScreen(parent);
    }
}
