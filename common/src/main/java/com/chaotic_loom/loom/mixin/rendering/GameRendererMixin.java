package com.chaotic_loom.loom.mixin.rendering;

import com.chaotic_loom.loom.Constants;
import com.chaotic_loom.loom.core.imgui.DebugWindows;
import com.chaotic_loom.loom.core.imgui.ImGuiManager;
import com.chaotic_loom.loom.core.imgui.editor.EditorLayout;
import com.chaotic_loom.loom.core.imgui.editor.panels.ViewportPanel;
import com.chaotic_loom.loom.core.rendering.shader.ShaderRegistrationCallback;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

/**
 * Intercepts {@link GameRenderer#reloadShaders(ResourceProvider)} to fire
 * {@link ShaderRegistrationCallback#EVENT} at the correct moment in Minecraft's
 * shader loading pipeline.
 *
 * <p><b>Why this mixin instead of platform events</b></p>
 * Both Fabric's {@code CoreShaderRegistrationCallback} and Forge's
 * {@code RegisterShadersEvent} are themselves wrappers around the same vanilla
 * code path — they inject here (or equivalent) and expose the list upward.
 * By going directly to the source we need no platform API at all, not even in
 * bridge classes. The entire shader system, including its entry point, lives in
 * the common module.
 *
 * <p><b>Injection point</b></p>
 * {@code reloadShaders} builds a {@code List<Pair<ShaderInstance,
 * Consumer<ShaderInstance>>>}, populates it with all vanilla shader pairs, and
 * then iterates it to call each consumer (handing the compiled instance back to
 * whatever field or variable owns it). We inject immediately before that
 * iteration — using {@code INVOKE} targeting the {@code forEach} call on the
 * list — so that:
 * <ol>
 *   <li>The {@link ResourceProvider} is available (it is the method parameter).</li>
 *   <li>The list is fully populated with vanilla entries and available as a
 *       local variable (captured via {@link LocalCapture#CAPTURE_FAILSOFT}).</li>
 *   <li>Our custom pairs are added before Minecraft processes the list, so they
 *       go through exactly the same compilation and consumer-callback pipeline
 *       as vanilla shaders.</li>
 * </ol>
 *
 * <p><b>Local capture order</b></p>
 * Mixin resolves captured locals by matching the declared parameter types against
 * the live local variable table at the injection point, in slot order. At the
 * {@code forEach} call site the only relevant local of a list type is the shader
 * pair list itself, so the capture is unambiguous. If a future Minecraft version
 * changes the local variable layout here, Mixin will emit a warning at load time
 * (with {@code CAPTURE_FAILSOFT}) rather than crashing, and the callback simply
 * will not fire — making the failure visible without breaking the game.
 */
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "reloadShaders", at = @At(value = "INVOKE_ASSIGN", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 0))
    private void registerShaders(ResourceProvider resourceProvider, CallbackInfo ci, @Local(ordinal = 1) List<Pair<ShaderInstance, Consumer<ShaderInstance>>> shaderPairList) throws IOException {
        Constants.LOG.info("[GameRendererMixin] reloadShaders called! Invoking ShaderRegistrationCallback");
        ShaderRegistrationCallback.EVENT.invoke(resourceProvider, shaderPairList);
        Constants.LOG.info("[GameRendererMixin] ShaderRegistrationCallback.invoke completed");
    }

    @Inject(
            method = "render",
            at = @At("RETURN")   // After Minecraft finishes the full frame
    )
    private void onRenderEnd(float partialTick, long nanoTime, boolean renderLevel, CallbackInfo ci) {
        ImGuiManager.beginFrame();
        ImGuiManager.renderUI();
        ImGuiManager.endFrame();
    }
}