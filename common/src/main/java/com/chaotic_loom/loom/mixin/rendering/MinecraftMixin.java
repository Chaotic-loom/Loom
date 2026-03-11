package com.chaotic_loom.loom.mixin.rendering;

import com.chaotic_loom.loom.core.imgui.ImGuiManager;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * In editor mode, prevents Minecraft from blitting its mainRenderTarget to the
 * actual screen.  The game still renders normally into its FBO every frame —
 * we just skip the step where that FBO is drawn full-screen, so ImGui can
 * display the texture inside the viewport panel instead.
 *
 * The blit happens at a specific INVOKE site inside Minecraft.runTick().
 * We target that exact call so nothing else (e.g. screenshots) is affected.
 */
@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Redirect(
            method = "runTick",
            at = @At(
                    value  = "INVOKE",
                    target = "Lcom/mojang/blaze3d/pipeline/RenderTarget;blitToScreen(II)V"
            )
    )
    private void redirectBlitToScreen(RenderTarget target, int width, int height, boolean linearFilter) {
        if (ImGuiManager.isEditorMode()) {
            // No-op: suppress the full-screen blit.
            // The game texture is displayed inside the ImGui viewport panel instead.
            return;
        }
        // Normal mode: blit as usual.
        target.blitToScreen(width, height, linearFilter);
    }
}