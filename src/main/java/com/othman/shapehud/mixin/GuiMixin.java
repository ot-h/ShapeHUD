package com.othman.shapehud.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.othman.shapehud.HudElement;
import com.othman.shapehud.HudTransform;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.contextualbar.ContextualBarRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Wraps each vanilla HUD render method of {@link Gui} with a pose push/transform/pop.
 * Hidden elements are cancelled at HEAD.
 */
@Mixin(Gui.class)
public abstract class GuiMixin {

    // ---- HOTBAR ----
    @Inject(method = "renderItemHotbar", at = @At("HEAD"), cancellable = true)
    private void shapehud$hotbar$head(CallbackInfo ci, @Local(argsOnly = true) GuiGraphics g) {
        if (!HudTransform.begin(g, HudElement.HOTBAR)) ci.cancel();
    }

    @Inject(method = "renderItemHotbar", at = @At("RETURN"))
    private void shapehud$hotbar$tail(CallbackInfo ci, @Local(argsOnly = true) GuiGraphics g) {
        HudTransform.end(g, HudElement.HOTBAR);
    }

    // ---- HEALTH ----
    @Inject(method = "renderHearts", at = @At("HEAD"), cancellable = true)
    private void shapehud$health$head(CallbackInfo ci, @Local(argsOnly = true) GuiGraphics g) {
        if (!HudTransform.begin(g, HudElement.HEALTH)) ci.cancel();
    }

    @Inject(method = "renderHearts", at = @At("RETURN"))
    private void shapehud$health$tail(CallbackInfo ci, @Local(argsOnly = true) GuiGraphics g) {
        HudTransform.end(g, HudElement.HEALTH);
    }

    // ---- ARMOR ----
    @Inject(method = "renderArmor", at = @At("HEAD"), cancellable = true)
    private static void shapehud$armor$head(CallbackInfo ci, @Local(argsOnly = true) GuiGraphics g) {
        if (!HudTransform.begin(g, HudElement.ARMOR)) ci.cancel();
    }

    @Inject(method = "renderArmor", at = @At("RETURN"))
    private static void shapehud$armor$tail(CallbackInfo ci, @Local(argsOnly = true) GuiGraphics g) {
        HudTransform.end(g, HudElement.ARMOR);
    }

    // ---- FOOD ----
    @Inject(method = "renderFood", at = @At("HEAD"), cancellable = true)
    private void shapehud$food$head(CallbackInfo ci, @Local(argsOnly = true) GuiGraphics g) {
        if (!HudTransform.begin(g, HudElement.FOOD)) ci.cancel();
    }

    @Inject(method = "renderFood", at = @At("RETURN"))
    private void shapehud$food$tail(CallbackInfo ci, @Local(argsOnly = true) GuiGraphics g) {
        HudTransform.end(g, HudElement.FOOD);
    }

    // ---- AIR ----
    @Inject(method = "renderAirBubbles", at = @At("HEAD"), cancellable = true)
    private void shapehud$air$head(CallbackInfo ci, @Local(argsOnly = true) GuiGraphics g) {
        if (!HudTransform.begin(g, HudElement.AIR)) ci.cancel();
    }

    @Inject(method = "renderAirBubbles", at = @At("RETURN"))
    private void shapehud$air$tail(CallbackInfo ci, @Local(argsOnly = true) GuiGraphics g) {
        HudTransform.end(g, HudElement.AIR);
    }

    // ---- MOUNT_HEALTH ----
    @Inject(method = "renderVehicleHealth", at = @At("HEAD"), cancellable = true)
    private void shapehud$mount_health$head(CallbackInfo ci, @Local(argsOnly = true) GuiGraphics g) {
        if (!HudTransform.begin(g, HudElement.MOUNT_HEALTH)) ci.cancel();
    }

    @Inject(method = "renderVehicleHealth", at = @At("RETURN"))
    private void shapehud$mount_health$tail(CallbackInfo ci, @Local(argsOnly = true) GuiGraphics g) {
        HudTransform.end(g, HudElement.MOUNT_HEALTH);
    }

    // ---- ITEM_NAME ----
    @Inject(method = "renderSelectedItemName", at = @At("HEAD"), cancellable = true)
    private void shapehud$item_name$head(CallbackInfo ci, @Local(argsOnly = true) GuiGraphics g) {
        if (!HudTransform.begin(g, HudElement.ITEM_NAME)) ci.cancel();
    }

    @Inject(method = "renderSelectedItemName", at = @At("RETURN"))
    private void shapehud$item_name$tail(CallbackInfo ci, @Local(argsOnly = true) GuiGraphics g) {
        HudTransform.end(g, HudElement.ITEM_NAME);
    }

    // ---- ACTION_BAR ----
    @Inject(method = "renderOverlayMessage", at = @At("HEAD"), cancellable = true)
    private void shapehud$action_bar$head(CallbackInfo ci, @Local(argsOnly = true) GuiGraphics g) {
        if (!HudTransform.begin(g, HudElement.ACTION_BAR)) ci.cancel();
    }

    @Inject(method = "renderOverlayMessage", at = @At("RETURN"))
    private void shapehud$action_bar$tail(CallbackInfo ci, @Local(argsOnly = true) GuiGraphics g) {
        HudTransform.end(g, HudElement.ACTION_BAR);
    }

    // ---- CROSSHAIR ----
    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void shapehud$crosshair$head(CallbackInfo ci, @Local(argsOnly = true) GuiGraphics g) {
        if (!HudTransform.begin(g, HudElement.CROSSHAIR)) ci.cancel();
    }

    @Inject(method = "renderCrosshair", at = @At("RETURN"))
    private void shapehud$crosshair$tail(CallbackInfo ci, @Local(argsOnly = true) GuiGraphics g) {
        HudTransform.end(g, HudElement.CROSSHAIR);
    }

    // ---- EFFECTS ----
    @Inject(method = "renderEffects", at = @At("HEAD"), cancellable = true)
    private void shapehud$effects$head(CallbackInfo ci, @Local(argsOnly = true) GuiGraphics g) {
        if (!HudTransform.begin(g, HudElement.EFFECTS)) ci.cancel();
    }

    @Inject(method = "renderEffects", at = @At("RETURN"))
    private void shapehud$effects$tail(CallbackInfo ci, @Local(argsOnly = true) GuiGraphics g) {
        HudTransform.end(g, HudElement.EFFECTS);
    }

    // ---- BOSS_BAR ----
    @Inject(method = "renderBossOverlay", at = @At("HEAD"), cancellable = true)
    private void shapehud$boss_bar$head(CallbackInfo ci, @Local(argsOnly = true) GuiGraphics g) {
        if (!HudTransform.begin(g, HudElement.BOSS_BAR)) ci.cancel();
    }

    @Inject(method = "renderBossOverlay", at = @At("RETURN"))
    private void shapehud$boss_bar$tail(CallbackInfo ci, @Local(argsOnly = true) GuiGraphics g) {
        HudTransform.end(g, HudElement.BOSS_BAR);
    }

    // ---- SCOREBOARD ----
    @Inject(method = "renderScoreboardSidebar", at = @At("HEAD"), cancellable = true)
    private void shapehud$scoreboard$head(CallbackInfo ci, @Local(argsOnly = true) GuiGraphics g) {
        if (!HudTransform.begin(g, HudElement.SCOREBOARD)) ci.cancel();
    }

    @Inject(method = "renderScoreboardSidebar", at = @At("RETURN"))
    private void shapehud$scoreboard$tail(CallbackInfo ci, @Local(argsOnly = true) GuiGraphics g) {
        HudTransform.end(g, HudElement.SCOREBOARD);
    }

    // ---- CHAT ----
    @Inject(method = "renderChat", at = @At("HEAD"), cancellable = true)
    private void shapehud$chat$head(CallbackInfo ci, @Local(argsOnly = true) GuiGraphics g) {
        if (!HudTransform.begin(g, HudElement.CHAT)) ci.cancel();
    }

    @Inject(method = "renderChat", at = @At("RETURN"))
    private void shapehud$chat$tail(CallbackInfo ci, @Local(argsOnly = true) GuiGraphics g) {
        HudTransform.end(g, HudElement.CHAT);
    }

    // ---- TITLE ----
    @Inject(method = "renderTitle", at = @At("HEAD"), cancellable = true)
    private void shapehud$title$head(CallbackInfo ci, @Local(argsOnly = true) GuiGraphics g) {
        if (!HudTransform.begin(g, HudElement.TITLE)) ci.cancel();
    }

    @Inject(method = "renderTitle", at = @At("RETURN"))
    private void shapehud$title$tail(CallbackInfo ci, @Local(argsOnly = true) GuiGraphics g) {
        HudTransform.end(g, HudElement.TITLE);
    }

    // ---- TAB_LIST ----
    @Inject(method = "renderTabList", at = @At("HEAD"), cancellable = true)
    private void shapehud$tab_list$head(CallbackInfo ci, @Local(argsOnly = true) GuiGraphics g) {
        if (!HudTransform.begin(g, HudElement.TAB_LIST)) ci.cancel();
    }

    @Inject(method = "renderTabList", at = @At("RETURN"))
    private void shapehud$tab_list$tail(CallbackInfo ci, @Local(argsOnly = true) GuiGraphics g) {
        HudTransform.end(g, HudElement.TAB_LIST);
    }

    // ---- SUBTITLES ----
    @Inject(method = "renderSubtitleOverlay", at = @At("HEAD"), cancellable = true)
    private void shapehud$subtitles$head(CallbackInfo ci, @Local(argsOnly = true) GuiGraphics g) {
        if (!HudTransform.begin(g, HudElement.SUBTITLES)) ci.cancel();
    }

    @Inject(method = "renderSubtitleOverlay", at = @At("RETURN"))
    private void shapehud$subtitles$tail(CallbackInfo ci, @Local(argsOnly = true) GuiGraphics g) {
        HudTransform.end(g, HudElement.SUBTITLES);
    }

    // ---- XP_BAR (contextual bar: experience / locator / jump bar) ----
    @WrapOperation(
        method = {"renderHotbarAndDecorations", "render"},
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/contextualbar/ContextualBarRenderer;renderBackground(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V"),
        require = 0)
    private void shapehud$barBackground(ContextualBarRenderer r, GuiGraphics g, DeltaTracker dt, Operation<Void> original) {
        if (!HudTransform.begin(g, HudElement.XP_BAR)) return;
        try { original.call(r, g, dt); } finally { HudTransform.end(g, HudElement.XP_BAR); }
    }

    @WrapOperation(
        method = {"renderHotbarAndDecorations", "render"},
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/contextualbar/ContextualBarRenderer;render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V"),
        require = 0)
    private void shapehud$barForeground(ContextualBarRenderer r, GuiGraphics g, DeltaTracker dt, Operation<Void> original) {
        if (!HudTransform.begin(g, HudElement.XP_BAR)) return;
        try { original.call(r, g, dt); } finally { HudTransform.end(g, HudElement.XP_BAR); }
    }

    // ---- XP_LEVEL (the green number) ----
    @WrapOperation(
        method = {"renderHotbarAndDecorations", "render"},
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/contextualbar/ContextualBarRenderer;renderExperienceLevel(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Font;I)V"),
        require = 0)
    private void shapehud$xpLevel(GuiGraphics g, Font font, int level, Operation<Void> original) {
        if (!HudTransform.begin(g, HudElement.XP_LEVEL)) return;
        try { original.call(g, font, level); } finally { HudTransform.end(g, HudElement.XP_LEVEL); }
    }
}
