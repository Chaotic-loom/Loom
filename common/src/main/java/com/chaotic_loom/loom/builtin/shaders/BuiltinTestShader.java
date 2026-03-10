package com.chaotic_loom.loom.builtin.shaders;

/*
 * BuiltinTestShader — Loom's integrated shader example
 *
 * PURPOSE
 * -------
 * This class is a self-contained, working demonstration of Loom's shader system.
 * It is not a production feature — it exists so developers can read real source
 * code that exercises every part of the pipeline:
 *
 *   ShaderProgram    → declared here as a static field
 *   ShaderRegistry   → used here to register it
 *   ManagedShaderInstance → used here to set per-frame uniforms
 *   UniformSnapshot  → exercised implicitly via restoreUniformDefaults()
 *   GuiMixin         → calls renderOverlay() to draw the fullscreen quad
 *
 * WHAT IT DOES
 * ------------
 * When enabled, renders a fullscreen translucent colour overlay on top of the
 * entire HUD (drawn at the tail of Gui.render). By default the overlay is off
 * and has no visual effect on the game.
 *
 * USAGE
 * -----
 *   BuiltinTestShader.enable();                   // red tint, default intensity
 *   BuiltinTestShader.enable(0f, 0f, 1f, 0.3f);   // blue tint at 30% alpha
 *   BuiltinTestShader.setIntensity(0.5f);          // halve the intensity
 *   BuiltinTestShader.disable();                   // remove the overlay
 *
 * INITIALISATION
 * --------------
 * Call BuiltinTestShader.init() from your client initialiser (or any client-side
 * code that runs before the first resource reload). This forces the class to load
 * so its static TINT field registers with ShaderRegistry before reload fires.
 */

import com.chaotic_loom.loom.Constants;
import com.chaotic_loom.loom.core.rendering.shader.ManagedShaderInstance;
import com.chaotic_loom.loom.core.rendering.shader.ShaderProgram;
import com.chaotic_loom.loom.core.rendering.shader.ShaderRegistry;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public final class BuiltinTestShader {

    // -------------------------------------------------------------------------
    // Shader declaration
    //
    // This is how every shader in Loom starts: one static final ShaderProgram.
    // No GPU work happens here — just a description and registration.
    //
    // trackUniforms("Tint", "Intensity") tells ManagedShaderInstance to snapshot
    // only these two uniforms from the JSON. ModelViewMat and ProjMat are set
    // automatically by RenderSystem and do not need snapshotting.
    // -------------------------------------------------------------------------

    public static final ShaderProgram TINT = ShaderRegistry.getInstance().register(
            ShaderProgram.builder(
                            new ResourceLocation("loom", "test_tint"),
                            DefaultVertexFormat.POSITION)
                    .trackUniforms("Tint", "Intensity")
                    .build()
    );

    // -------------------------------------------------------------------------
    // Runtime state
    //
    // volatile so changes are visible across threads. In practice, enable() and
    // disable() are called from the game thread, and renderOverlay() reads from
    // the render thread — both of which are the same thread in vanilla Minecraft,
    // but volatile is cheap and removes the assumption.
    // -------------------------------------------------------------------------

    private static volatile boolean enabled   = true;
    private static volatile float   tintR     = 1.0f;
    private static volatile float   tintG     = 0.0f;
    private static volatile float   tintB     = 0.0f;
    private static volatile float   tintA     = 0.4f;
    private static volatile float   intensity = 1.0f;

    private BuiltinTestShader() {}

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Forces this class to load, registering {@link #TINT} with
     * {@link ShaderRegistry} before the first resource reload fires.
     *
     * <p>Call this from your client initialiser:
     * <pre>{@code BuiltinTestShader.init(); }</pre>
     *
     * <p>The method body is intentionally empty — the act of loading the class
     * triggers the static field initialiser that does the real work.
     */
    public static void init() {
        // Static fields above run on first class load. Nothing else needed.
        Constants.LOG.info("Built-in test shader init method called.");
    }

    /**
     * Enables the overlay with the default semi-transparent red tint.
     */
    public static void enable() {
        tintR     = 1.0f;
        tintG     = 0.0f;
        tintB     = 0.0f;
        tintA     = 0.4f;
        intensity = 1.0f;
        enabled   = true;
    }

    /**
     * Enables the overlay with a custom RGBA tint colour.
     *
     * @param r red   channel [0.0 – 1.0]
     * @param g green channel [0.0 – 1.0]
     * @param b blue  channel [0.0 – 1.0]
     * @param a alpha channel [0.0 – 1.0]; controls how opaque the overlay is
     */
    public static void enable(float r, float g, float b, float a) {
        tintR     = r;
        tintG     = g;
        tintB     = b;
        tintA     = a;
        intensity = 1.0f;
        enabled   = true;
    }

    /**
     * Enables the overlay with a custom RGBA tint colour and intensity multiplier.
     *
     * @param r         red       [0.0 – 1.0]
     * @param g         green     [0.0 – 1.0]
     * @param b         blue      [0.0 – 1.0]
     * @param a         alpha     [0.0 – 1.0]
     * @param intensity multiplier applied to the alpha channel [0.0 – 1.0]
     */
    public static void enable(float r, float g, float b, float a, float intensity) {
        tintR                     = r;
        tintG                     = g;
        tintB                     = b;
        tintA                     = a;
        BuiltinTestShader.intensity = intensity;
        enabled                   = true;
    }

    /** Removes the overlay. Has no effect if already disabled. */
    public static void disable() {
        enabled = false;
    }

    /** Returns {@code true} if the overlay is currently active. */
    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * Changes the tint colour while the overlay is running.
     * Has no effect if the overlay is disabled.
     */
    public static void setTint(float r, float g, float b, float a) {
        tintR = r;
        tintG = g;
        tintB = b;
        tintA = a;
    }

    /**
     * Changes the intensity multiplier while the overlay is running.
     * Has no effect if the overlay is disabled.
     *
     * @param value [0.0 – 1.0]; 0.0 makes the overlay invisible, 1.0 is full
     */
    public static void setIntensity(float value) {
        intensity = value;
    }

    // -------------------------------------------------------------------------
    // Rendering — called by GuiMixin
    //
    // This is the part that connects the ShaderProgram to actual pixels on screen.
    // It is package-accessible (no modifier) so only GuiMixin can call it;
    // external code uses the enable/disable API instead.
    // -------------------------------------------------------------------------

    /**
     * Draws a fullscreen quad using {@link #TINT} over the current HUD frame.
     * Called by {@link com.chaotic_loom.loom.mixin.rendering.GuiMixin} at the
     * tail of {@code Gui.render}. Do not call this directly.
     *
     * <p>The method is a no-op when {@link #enabled} is {@code false} or when
     * the shader has not yet been loaded by a resource reload.
     */
    public static void renderOverlay(GuiGraphics guiGraphics) {
        if (!enabled) return;

        // getInstance() returns null before the first resource reload.
        ManagedShaderInstance shader = TINT.getInstance();
        if (shader == null) return;

        // ------------------------------------------------------------------
        // Step 1: Set per-frame uniform values.
        //
        // The snapshot system captured the JSON defaults when the shader loaded.
        // We override them here for this frame. restoreUniformDefaults() at the
        // end of this method resets them back for the next caller.
        // ------------------------------------------------------------------

        Uniform tintUniform = shader.getUniform("Tint");
        if (tintUniform != null) {
            tintUniform.set(tintR, tintG, tintB, tintA);
        }

        Uniform intensityUniform = shader.getUniform("Intensity");
        if (intensityUniform != null) {
            intensityUniform.set(intensity);
        }

        // ------------------------------------------------------------------
        // Step 2: Draw a fullscreen quad in screen (GUI) coordinates.
        //
        // GuiGraphics gives us the current pose stack, which already holds the
        // correct orthographic projection for 2D HUD rendering.
        //
        // The quad covers the full GUI-scaled screen:
        //   top-left     (0, 0)
        //   bottom-left  (0, height)
        //   bottom-right (width, height)
        //   top-right    (width, 0)
        //
        // DefaultVertexFormat.POSITION only needs x, y, z per vertex.
        // ModelViewMat and ProjMat are set automatically by RenderSystem when
        // BufferUploader.drawWithShader is called.
        // ------------------------------------------------------------------

        Minecraft mc = Minecraft.getInstance();
        int screenWidth  = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(TINT::getInstance);

        org.joml.Matrix4f matrix = guiGraphics.pose().last().pose();

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer  = tesselator.getBuilder();

        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        buffer.vertex(matrix, 0,           0,            0).endVertex();
        buffer.vertex(matrix, 0,           screenHeight, 0).endVertex();
        buffer.vertex(matrix, screenWidth, screenHeight, 0).endVertex();
        buffer.vertex(matrix, screenWidth, 0,            0).endVertex();
        BufferUploader.drawWithShader(buffer.end());

        RenderSystem.disableBlend();

        // ------------------------------------------------------------------
        // Step 3: Restore uniform defaults.
        //
        // This resets Tint and Intensity back to the values captured from
        // test_tint.json at load time. Any subsequent code that uses this
        // shader in the same frame will start from known-good defaults.
        // ------------------------------------------------------------------

        shader.restoreUniformDefaults();
    }
}