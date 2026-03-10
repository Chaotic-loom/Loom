package com.chaotic_loom.loom.mixin.rendering;

import com.chaotic_loom.loom.builtin.shaders.BuiltinTestShader;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hooks into {@link Gui#render(GuiGraphics, float)} to draw Loom's built-in
 * test shader overlay when it is enabled.
 *
 * <p><b>Why TAIL?</b></p>
 * Injecting at {@code TAIL} means the overlay is drawn <em>after</em> all vanilla
 * HUD elements (health bar, hotbar, crosshair, etc.), so it sits on top of
 * everything without interfering with any vanilla rendering state.
 *
 * <p><b>Why Gui.render and not GameRenderer?</b></p>
 * {@code Gui.render} is called during 2D HUD rendering, where the orthographic
 * projection and screen-space pose stack are already set up. This lets us draw a
 * fullscreen quad using simple (x, y) screen coordinates without manually
 * configuring any matrices.
 */
@Mixin(Gui.class)
public abstract class GuiMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void loom$onRenderHud(GuiGraphics guiGraphics, float partialTick, CallbackInfo ci) {
        // Delegates entirely to BuiltinTestShader, which does nothing when disabled.
        // The check for the shader being enabled is inside renderOverlay() itself,
        // so this injection has zero overhead when the test shader is off.
        BuiltinTestShader.renderOverlay(guiGraphics);
    }
}