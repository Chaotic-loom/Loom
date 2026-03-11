package com.chaotic_loom.loom.core.imgui.editor.panels;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiWindowFlags;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

/**
 * Viewport panel – docked to the central node.
 *
 * The game renders normally into mc.getMainRenderTarget() every frame.
 * MixinMinecraft suppresses the blit-to-screen call so it doesn't appear
 * full-screen behind ImGui.  We then display the same FBO's colour texture
 * here via ImGui.image().
 *
 * UV flip: OpenGL stores textures bottom-up; ImGui expects top-down.
 * uv0 = (0, 1), uv1 = (1, 0) corrects this.
 */
public class ViewportPanel extends EditorPanel {

    public static final String TITLE = "  Viewport";

    private int     activeTool = 0;
    private boolean showGrid   = true;
    private boolean showGizmos = true;
    private float   timeOfDay  = 0.5f;

    private static final String[] TOOLS = { "Select", "Move", "Rotate", "Scale" };

    public ViewportPanel() {
        super(
                ImGuiWindowFlags.NoScrollbar       |
                        ImGuiWindowFlags.NoScrollWithMouse  |
                        ImGuiWindowFlags.NoBackground
        );
    }

    @Override public String getTitle() { return TITLE; }

    @Override
    protected void renderContent() {
        renderGameImage();
        renderToolbarOverlay();
        renderStatsCorner();
    }

    // ── Game image ────────────────────────────────────────────────────────────

    private void renderGameImage() {
        ImVec2 avail = ImGui.getContentRegionAvail();
        if (avail.x <= 0 || avail.y <= 0) return;

        int texId = Minecraft.getInstance().getMainRenderTarget().getColorTextureId();

        // Flip Y: OpenGL textures are bottom-up, ImGui images are top-down.
        ImGui.image(texId, avail.x, avail.y, 0f, 1f, 1f, 0f);
    }

    // ── Toolbar overlay ───────────────────────────────────────────────────────
    // Drawn after the image so it sits on top of it.
    // setCursorPos resets back to the top of the content area.

    private void renderToolbarOverlay() {
        // Overlap back onto the top of the image
        ImGui.setCursorPos(0f, 0f);

        ImGui.pushStyleColor(ImGuiCol.ChildBg, 0.08f, 0.08f, 0.12f, 0.80f);
        boolean open = ImGui.beginChild("##vp_bar", 0f, 34f, false,
                ImGuiWindowFlags.NoScrollbar | ImGuiWindowFlags.NoScrollWithMouse);
        ImGui.popStyleColor();

        if (!open) { ImGui.endChild(); return; }

        ImGui.setCursorPosY(ImGui.getCursorPosY() + 4f);

        for (int i = 0; i < TOOLS.length; i++) {
            if (i > 0) ImGui.sameLine(0, 2);
            boolean active = (activeTool == i);
            if (active) ImGui.pushStyleColor(ImGuiCol.Button, 0.29f, 0.62f, 1.00f, 1f);
            else        ImGui.pushStyleColor(ImGuiCol.Button, 0.18f, 0.18f, 0.24f, 1f);
            if (ImGui.button(TOOLS[i])) activeTool = i;
            ImGui.popStyleColor();
        }

        ImGui.sameLine(0, 12);
        ImGui.textDisabled("|");
        ImGui.sameLine(0, 12);

        showGrid   = toolToggle("Grid",   showGrid);
        ImGui.sameLine(0, 4);
        showGizmos = toolToggle("Gizmos", showGizmos);

        ImGui.sameLine(0, 12);
        ImGui.textDisabled("|");
        ImGui.sameLine(0, 12);

        ImGui.text("Time");
        ImGui.sameLine(0, 6);
        ImGui.setNextItemWidth(100f);
        float[] tod = { timeOfDay };
        if (ImGui.sliderFloat("##tod", tod, 0f, 1f)) {
            timeOfDay = tod[0];
            // TODO: apply to world time manager
        }

        ImGui.endChild();
    }

    private boolean toolToggle(String label, boolean state) {
        if (state) ImGui.pushStyleColor(ImGuiCol.Button, 0.18f, 0.45f, 0.18f, 1f);
        else       ImGui.pushStyleColor(ImGuiCol.Button, 0.18f, 0.18f, 0.24f, 1f);
        if (ImGui.button(label)) state = !state;
        ImGui.popStyleColor();
        return state;
    }

    // ── Stats corner ──────────────────────────────────────────────────────────

    private void renderStatsCorner() {
        Minecraft mc = Minecraft.getInstance();
        Player    p  = mc.player;
        if (p == null) return;

        String[] lines = {
                String.format("XYZ   %.1f  %.1f  %.1f", p.getX(), p.getY(), p.getZ()),
                String.format("Facing  %.1f  %.1f",      p.getYRot(), p.getXRot()),
                "Biome  " + getBiomeName(mc, p),
                "FPS   " + mc.getFps(),
        };

        float pad   = 8f;
        float lineH = ImGui.getTextLineHeightWithSpacing();
        float boxH  = lineH * lines.length + pad * 2f;
        float boxW  = 220f;
        float panW  = ImGui.getWindowWidth();
        float panH  = ImGui.getWindowHeight();

        float bx = ImGui.getWindowPos().x + panW - boxW - pad;
        float by = ImGui.getWindowPos().y + panH - boxH - pad;

        ImGui.getWindowDrawList().addRectFilled(
                bx - pad, by - pad, bx + boxW, by + boxH,
                ImGui.colorConvertFloat4ToU32(0.07f, 0.07f, 0.10f, 0.72f), 4f);

        ImGui.setCursorPos(panW - boxW - pad, panH - boxH - pad);
        ImGui.pushStyleColor(ImGuiCol.Text, 0.85f, 0.85f, 0.95f, 1f);
        for (String line : lines) ImGui.textUnformatted(line);
        ImGui.popStyleColor();
    }

    private String getBiomeName(Minecraft mc, Player p) {
        if (mc.level == null) return "—";
        return mc.level.getBiome(p.blockPosition())
                .unwrapKey()
                .map(k -> k.location().getPath())
                .orElse("?");
    }
}