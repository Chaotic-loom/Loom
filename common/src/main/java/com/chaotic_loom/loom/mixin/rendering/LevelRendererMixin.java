package com.chaotic_loom.loom.mixin.rendering;

import com.chaotic_loom.loom.CustomBorderData;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderWorldBorder(Lnet/minecraft/client/Camera;)V"))
    private void render(PoseStack $$0, float $$1, long $$2, boolean $$3, Camera camera, GameRenderer $$5, LightTexture $$6, Matrix4f $$7, CallbackInfo ci) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull(); // See walls from both sides
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        // 2. Prepare the Buffer
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();

        // 3. Calculate Coordinates
        double halfSize = CustomBorderData.SIZE / 2.0D;
        double minX = CustomBorderData.CENTER_X - halfSize;
        double maxX = CustomBorderData.CENTER_X + halfSize;
        double minZ = CustomBorderData.CENTER_Z - halfSize;
        double maxZ = CustomBorderData.CENTER_Z + halfSize;

        // Render relative to camera (Substract camera pos)
        double camX = camera.getPosition().x;
        double camY = camera.getPosition().y; // Not strictly needed for vertical walls unless we clamp Y
        double camZ = camera.getPosition().z;

        // Start drawing Quads with Position and Color
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        // Helper variables for clean matrix math
        // We translate manually by subtracting camX/camZ to keep precision high

        // --- Wall 1: North (along Min Z) ---
        // Draws from MinX to MaxX at MinZ
        loom$drawWall(bufferbuilder, minX - camX, maxX - camX, minZ - camZ, minZ - camZ);

        // --- Wall 2: South (along Max Z) ---
        // Draws from MinX to MaxX at MaxZ
        loom$drawWall(bufferbuilder, minX - camX, maxX - camX, maxZ - camZ, maxZ - camZ);

        // --- Wall 3: East (along Max X) ---
        // Draws at MaxX from MinZ to MaxZ
        loom$drawWall(bufferbuilder, maxX - camX, maxX - camX, minZ - camZ, maxZ - camZ);

        // --- Wall 4: West (along Min X) ---
        // Draws at MinX from MinZ to MaxZ
        loom$drawWall(bufferbuilder, minX - camX, minX - camX, minZ - camZ, maxZ - camZ);

        tesselator.end();

        // 4. Cleanup State
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    // Helper method to draw a vertical quad
    @Unique
    private void loom$drawWall(BufferBuilder buffer, double x1, double x2, double z1, double z2) {
        float bottom = (float) (CustomBorderData.MIN_Y);
        float top = (float) (CustomBorderData.MAX_Y);

        int r = CustomBorderData.R;
        int g = CustomBorderData.G;
        int b = CustomBorderData.B;
        int a = CustomBorderData.A;

        // 4 Vertices for a quad
        buffer.vertex(x1, top, z1).color(r, g, b, a).endVertex();
        buffer.vertex(x1, bottom, z1).color(r, g, b, a).endVertex();
        buffer.vertex(x2, bottom, z2).color(r, g, b, a).endVertex();
        buffer.vertex(x2, top, z2).color(r, g, b, a).endVertex();
    }
}
