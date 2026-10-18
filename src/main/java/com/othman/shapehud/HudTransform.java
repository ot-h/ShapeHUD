package com.othman.shapehud;

import net.minecraft.client.gui.GuiGraphics;
import org.joml.Matrix3x2fStack;

/** Pushes/pops the 2D pose around each vanilla HUD element's render call. */
public final class HudTransform {
    private static final int[] DEPTH = new int[HudElement.values().length];

    private HudTransform() {}

    /** @return false if the element is hidden and must not be rendered. */
    public static boolean begin(GuiGraphics g, HudElement e) {
        ElementState s = ShapeHud.config().get(e);
        if (s.hidden) return false;
        Matrix3x2fStack pose = g.pose();
        pose.pushMatrix();
        apply(pose, e, g.guiWidth(), g.guiHeight());
        DEPTH[e.ordinal()]++;
        return true;
    }

    /** Multiplies the user's offset/scale for {@code e} onto the current pose (caller pushes/pops). */
    public static void apply(Matrix3x2fStack pose, HudElement e, int w, int h) {
        ElementState s = ShapeHud.config().get(e);
        if (s.dx == 0 && s.dy == 0 && s.scale == 1f) return;
        HudElement.Rect b = e.box(w, h);
        float cx = b.cx(), cy = b.cy();
        pose.translate(cx + s.dx, cy + s.dy);
        pose.scale(s.scale, s.scale);
        pose.translate(-cx, -cy);
    }

    public static void end(GuiGraphics g, HudElement e) {
        int i = e.ordinal();
        if (DEPTH[i] > 0) {
            DEPTH[i]--;
            g.pose().popMatrix();
        }
    }
}
