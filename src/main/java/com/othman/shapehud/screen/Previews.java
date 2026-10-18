package com.othman.shapehud.screen;

import com.othman.shapehud.HudElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.scores.DisplaySlot;

import java.util.List;

/**
 * Example content for HUD elements that aren't on screen while editing
 * (no boss nearby, empty chat, no effects, creative mode...), so users can see what they're placing.
 * Everything is drawn at the vanilla position; the caller applies the element's transform.
 */
final class Previews {
    private Previews() {}

    private static final int WHITE = 0xFFFFFFFF;

    /** Whether the real element is probably invisible right now and an example should be drawn. */
    static boolean needed(Minecraft mc, HudElement e) {
        LocalPlayer p = mc.player;
        if (p == null || mc.gameMode == null) return false;
        boolean survival = mc.gameMode.canHurtPlayer();
        return switch (e) {
            case HOTBAR, CROSSHAIR -> false;
            case HEALTH, FOOD -> !survival;
            case ARMOR -> !survival || p.getArmorValue() <= 0;
            case XP_BAR -> !mc.gameMode.hasExperience();
            case XP_LEVEL -> !mc.gameMode.hasExperience() || p.experienceLevel <= 0;
            case AIR -> !survival || p.getAirSupply() >= p.getMaxAirSupply();
            case MOUNT_HEALTH -> !(p.getVehicle() instanceof LivingEntity);
            case EFFECTS -> p.getActiveEffects().isEmpty();
            case SCOREBOARD -> mc.level == null || mc.level.getScoreboard().getDisplayObjective(DisplaySlot.SIDEBAR) == null;
            case CHAT, ACTION_BAR, TITLE, ITEM_NAME, BOSS_BAR, TAB_LIST, SUBTITLES -> true;
        };
    }

    static void draw(GuiGraphics g, Font font, HudElement e, int w, int h) {
        int left = w / 2 - 91, right = w / 2 + 91;
        switch (e) {
            case HEALTH -> {
                for (int i = 0; i < 10; i++) {
                    Draw.sprite(g, "hud/heart/container", left + i * 8, h - 39, 9, 9);
                    Draw.sprite(g, "hud/heart/full", left + i * 8, h - 39, 9, 9);
                }
            }
            case ARMOR -> {
                for (int i = 0; i < 10; i++) Draw.sprite(g, i < 7 ? "hud/armor_full" : "hud/armor_empty", left + i * 8, h - 49, 9, 9);
            }
            case FOOD -> {
                for (int i = 0; i < 10; i++) {
                    Draw.sprite(g, "hud/food_empty", right - i * 8 - 9, h - 39, 9, 9);
                    Draw.sprite(g, "hud/food_full", right - i * 8 - 9, h - 39, 9, 9);
                }
            }
            case AIR -> {
                for (int i = 0; i < 10; i++) Draw.sprite(g, "hud/air", right - i * 8 - 9, h - 49, 9, 9);
            }
            case MOUNT_HEALTH -> {
                for (int i = 0; i < 10; i++) {
                    Draw.sprite(g, "hud/heart/vehicle_container", right - i * 8 - 9, h - 39, 9, 9);
                    Draw.sprite(g, "hud/heart/vehicle_full", right - i * 8 - 9, h - 39, 9, 9);
                }
            }
            case XP_BAR -> Draw.sprite(g, "hud/experience_bar_background", left, h - 29, 182, 5);
            case XP_LEVEL -> outlinedCentered(g, font, "30", w / 2, h - 35, 0xFF80FF20);
            case ITEM_NAME -> g.drawCenteredString(font, "Diamond Sword", w / 2, h - 59, WHITE);
            case ACTION_BAR -> g.drawCenteredString(font, "§eThis is an action bar message", w / 2, h - 72, WHITE);
            case TITLE -> {
                var pose = g.pose();
                pose.pushMatrix();
                pose.translate(w / 2f, h / 2f);
                pose.pushMatrix();
                pose.scale(4f, 4f);
                String t = "Title";
                g.drawString(font, t, -font.width(t) / 2, -10, WHITE, true);
                pose.popMatrix();
                pose.pushMatrix();
                pose.scale(2f, 2f);
                String s = "§7Subtitle text";
                g.drawString(font, s, -font.width(s) / 2, 5, WHITE, true);
                pose.popMatrix();
                pose.popMatrix();
            }
            case CHAT -> {
                String[] lines = {
                    "§e<Steve>§r hey everyone!",
                    "§b<Alex>§r this is a test message",
                    "§7[Server] Welcome to the server",
                    "§d<ot_h>§r ShapeHUD looks great"
                };
                int bottom = h - 40;
                for (int i = 0; i < lines.length; i++) {
                    int y = bottom - (lines.length - i) * 9;
                    g.fill(0, y, 324, y + 9, 0x80000000);
                    g.drawString(font, lines[i], 4, y + 1, WHITE, true);
                }
            }
            case BOSS_BAR -> {
                g.drawCenteredString(font, "Ender Dragon", w / 2, 3, WHITE);
                Draw.sprite(g, "boss_bar/purple_background", left, 12, 182, 5);
                Draw.sprite(g, "boss_bar/purple_progress", left, 12, 182, 5);
            }
            case EFFECTS -> {
                List<Holder<MobEffect>> effects = List.of(MobEffects.REGENERATION, MobEffects.FIRE_RESISTANCE, MobEffects.NIGHT_VISION);
                for (int i = 0; i < effects.size(); i++) {
                    int x = w - 25 * (i + 1), y = 1;
                    Draw.sprite(g, "hud/effect_background", x, y, 24, 24);
                    Draw.sprite(g, Gui.getMobEffectSprite(effects.get(i)), x + 3, y + 3, 18, 18);
                }
            }
            case SCOREBOARD -> {
                String[][] rows = {{"Kills", "12"}, {"Deaths", "3"}, {"Coins", "250"}, {"Online", "18"}, {"Level", "7"}};
                int x1 = w - 108, x2 = w - 2;
                int top = h / 2 - 27;
                g.fill(x1, top, x2, top + 9, 0x66000000);
                g.drawCenteredString(font, "§6§lShapeHUD", (x1 + x2) / 2, top + 1, WHITE);
                for (int i = 0; i < rows.length; i++) {
                    int y = top + 9 + i * 9;
                    g.fill(x1, y, x2, y + 9, 0x4C000000);
                    g.drawString(font, rows[i][0], x1 + 2, y + 1, 0xFFAAAAAA, false);
                    g.drawString(font, rows[i][1], x2 - 1 - font.width(rows[i][1]), y + 1, 0xFFFF5555, false);
                }
            }
            case TAB_LIST -> {
                String[] names = {"ot_h", "Steve", "Alex", "Notch", "jeb_", "Dinnerbone", "Herobrine", "Grumm"};
                int x1 = w / 2 - 128, x2 = w / 2 + 128;
                g.fill(x1, 10, x2, 68, 0x80000000);
                g.drawCenteredString(font, "§ePlayers online", w / 2, 13, WHITE);
                for (int i = 0; i < names.length; i++) {
                    int col = i / 4, row = i % 4;
                    int cx = x1 + 2 + col * 127, cy = 25 + row * 10;
                    g.fill(cx, cy, cx + 125, cy + 9, 0x20FFFFFF);
                    g.drawString(font, names[i], cx + 2, cy + 1, WHITE, true);
                    g.fill(cx + 115, cy + 5, cx + 117, cy + 8, 0xFF55FF55);
                    g.fill(cx + 118, cy + 3, cx + 120, cy + 8, 0xFF55FF55);
                    g.fill(cx + 121, cy + 1, cx + 123, cy + 8, 0xFF55FF55);
                }
            }
            case SUBTITLES -> {
                String[] lines = {"< Footsteps", "Zombie groans >", "Door creaks"};
                for (int i = 0; i < lines.length; i++) {
                    int y = h - 70 + i * 11;
                    int tw = font.width(lines[i]);
                    g.fill(w - 6 - tw - 6, y - 1, w - 2, y + 9, 0xCC000000);
                    g.drawString(font, lines[i], w - 6 - tw, y, WHITE, false);
                }
            }
            default -> {}
        }
    }

    private static void outlinedCentered(GuiGraphics g, Font font, String s, int cx, int y, int color) {
        int x = cx - font.width(s) / 2;
        g.drawString(font, s, x + 1, y, 0xFF000000, false);
        g.drawString(font, s, x - 1, y, 0xFF000000, false);
        g.drawString(font, s, x, y + 1, 0xFF000000, false);
        g.drawString(font, s, x, y - 1, 0xFF000000, false);
        g.drawString(font, s, x, y, color, false);
    }
}
