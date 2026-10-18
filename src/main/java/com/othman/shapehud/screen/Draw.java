package com.othman.shapehud.screen;

import com.othman.shapehud.HudElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/** Small drawing helpers shared by the editor and the previews. */
final class Draw {
    private Draw() {}

    private static final Map<String, Identifier> IDS = new HashMap<>();
    private static final Map<HudElement, ItemStack> ICON_STACKS = new EnumMap<>(HudElement.class);

    static Identifier id(String path) {
        return IDS.computeIfAbsent(path, Identifier::withDefaultNamespace);
    }

    static void sprite(GuiGraphics g, String path, int x, int y, int w, int h) {
        g.blitSprite(RenderPipelines.GUI_TEXTURED, id(path), x, y, w, h);
    }

    static void sprite(GuiGraphics g, Identifier id, int x, int y, int w, int h) {
        g.blitSprite(RenderPipelines.GUI_TEXTURED, id, x, y, w, h);
    }

    static Item item(String id) {
        return switch (id) {
            case "diamond_sword" -> Items.DIAMOND_SWORD;
            case "experience_bottle" -> Items.EXPERIENCE_BOTTLE;
            case "enchanting_table" -> Items.ENCHANTING_TABLE;
            case "target" -> Items.TARGET;
            case "writable_book" -> Items.WRITABLE_BOOK;
            case "oak_sign" -> Items.OAK_SIGN;
            case "book" -> Items.BOOK;
            case "name_tag" -> Items.NAME_TAG;
            case "potion" -> Items.POTION;
            case "dragon_head" -> Items.DRAGON_HEAD;
            case "map" -> Items.MAP;
            case "player_head" -> Items.PLAYER_HEAD;
            case "saddle" -> Items.SADDLE;
            case "note_block" -> Items.NOTE_BLOCK;
            default -> Items.BARRIER;
        };
    }

    /** Draws the element's icon in a size x size square. */
    static void icon(GuiGraphics g, HudElement e, int x, int y, int size) {
        HudElement.Icon icon = e.icon;
        if (icon.sprite() != null) {
            int s = Math.round(size * 0.75f);
            int o = (size - s) / 2;
            sprite(g, icon.sprite(), x + o, y + o, s, s);
            return;
        }
        ItemStack stack = ICON_STACKS.computeIfAbsent(e, k -> new ItemStack(item(icon.item())));
        var pose = g.pose();
        pose.pushMatrix();
        pose.translate(x, y);
        pose.scale(size / 16f, size / 16f);
        g.renderItem(stack, 0, 0);
        pose.popMatrix();
    }

    static void outline(GuiGraphics g, float x, float y, float w, float h, int color) {
        int ix = Math.round(x), iy = Math.round(y);
        g.renderOutline(ix, iy, Math.max(1, Math.round(w)), Math.max(1, Math.round(h)), color);
    }

    static void fill(GuiGraphics g, float x, float y, float w, float h, int color) {
        g.fill(Math.round(x), Math.round(y), Math.round(x + w), Math.round(y + h), color);
    }

    /** Four L-shaped corner marks — much calmer than full rectangles. */
    static void corners(GuiGraphics g, HudElement.Rect r, int color) {
        int x1 = Math.round(r.x()), y1 = Math.round(r.y()), x2 = Math.round(r.x2()), y2 = Math.round(r.y2());
        int lx = Math.max(2, Math.min(6, (x2 - x1) / 3)), ly = Math.max(2, Math.min(6, (y2 - y1) / 3));
        g.fill(x1, y1, x1 + lx, y1 + 1, color); g.fill(x1, y1, x1 + 1, y1 + ly, color);
        g.fill(x2 - lx, y1, x2, y1 + 1, color); g.fill(x2 - 1, y1, x2, y1 + ly, color);
        g.fill(x1, y2 - 1, x1 + lx, y2, color); g.fill(x1, y2 - ly, x1 + 1, y2, color);
        g.fill(x2 - lx, y2 - 1, x2, y2, color); g.fill(x2 - 1, y2 - ly, x2, y2, color);
    }

    /** Rounded-looking pill (1px corner cut). */
    static void pill(GuiGraphics g, int x, int y, int w, int h, int color) {
        g.fill(x + 1, y, x + w - 1, y + h, color);
        g.fill(x, y + 1, x + 1, y + h - 1, color);
        g.fill(x + w - 1, y + 1, x + w, y + h - 1, color);
    }

    /** iOS-style on/off switch, 16x8. */
    static void toggle(GuiGraphics g, int x, int y, boolean on, int onColor) {
        pill(g, x, y, 16, 8, on ? onColor : 0xFF4A4E58);
        int kx = on ? x + 9 : x + 1;
        pill(g, kx, y + 1, 6, 6, 0xFFFFFFFF);
    }
}
