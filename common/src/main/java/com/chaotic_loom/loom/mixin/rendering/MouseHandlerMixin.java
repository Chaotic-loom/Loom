package com.chaotic_loom.loom.mixin.rendering;

import com.chaotic_loom.loom.core.imgui.ImGuiManager;
import com.chaotic_loom.loom.core.imgui.editor.panels.ViewportPanel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Remaps raw GLFW mouse coordinates into game-space coordinates when in
 * editor mode.
 *
 * Problem:
 *   The game image is letterboxed inside the viewport panel — smaller and
 *   offset from the window's top-left corner.  Minecraft's MouseHandler
 *   receives raw GLFW screen-pixel coordinates and uses them directly for
 *   UI hit-testing, camera delta, block-breaking etc. — all of which are
 *   computed as if the render fills the entire window.
 *
 * Fix:
 *   Intercept x and y at the HEAD of onMove / onPress (before anything
 *   reads them) and replace with remapped game-space coordinates:
 *
 *     gameX = (screenX - imageX) * (nativeW / imageW)
 *     gameY = (screenY - imageY) * (nativeH / imageH)
 *
 *   After remapping, all downstream logic (GUI clicks, camera turn delta,
 *   block/entity picking) sees coordinates as if the game were full-screen.
 *
 * Local-variable slot layout for instance methods on 64-bit JVM:
 *   Slot 0 = this
 *   Slot 1 = window (long  → occupies slots 1-2)
 *   Slot 3 = x      (double → occupies slots 3-4)
 *   Slot 5 = y      (double → occupies slots 5-6)
 */
@Mixin(MouseHandler.class)
public class MouseHandlerMixin {

    // ── onMove(long window, double x, double y) ───────────────────────────────

    @ModifyVariable(method = "onMove", at = @At("HEAD"), index = 3, argsOnly = true)
    private double remapMoveX(double x) {
        return remapX(x);
    }

    @ModifyVariable(method = "onMove", at = @At("HEAD"), index = 5, argsOnly = true)
    private double remapMoveY(double y) {
        return remapY(y);
    }

    // ── onPress(long window, int button, int action, int modifiers) ───────────
    //
    // onPress doesn't receive x/y directly — it reads MouseHandler.xpos / ypos
    // which were stored by the last onMove call.  Because onMove is already
    // remapped above, click positions are automatically correct.
    // No additional injection needed here.

    // ── Scroll (onScroll) ─────────────────────────────────────────────────────
    //
    // Scroll delta (horizontal/vertical amounts) is not a screen position,
    // so it needs no remapping.

    // ── Remapping helpers ─────────────────────────────────────────────────────

    private static double remapX(double screenX) {
        if (!ImGuiManager.isEditorMode()) return screenX;
        if (ViewportPanel.imageW <= 0)    return screenX;

        double nativeW = Minecraft.getInstance().getWindow().getWidth();
        return (screenX - ViewportPanel.imageX) * (nativeW / ViewportPanel.imageW);
    }

    private static double remapY(double screenY) {
        if (!ImGuiManager.isEditorMode()) return screenY;
        if (ViewportPanel.imageH <= 0)    return screenY;

        double nativeH = Minecraft.getInstance().getWindow().getHeight();
        return (screenY - ViewportPanel.imageY) * (nativeH / ViewportPanel.imageH);
    }
}