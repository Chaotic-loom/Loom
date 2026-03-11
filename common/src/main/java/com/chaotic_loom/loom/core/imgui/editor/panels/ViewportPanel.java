package com.chaotic_loom.loom.core.imgui.editor.panels;

import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiWindowFlags;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

/**
 * Viewport panel – docked to the central node.
 *
 * Why the game shows through:
 *   The dockspace uses PassthruCentralNode, which tells the GL3 backend to
 *   leave the central rect of the framebuffer untouched.  This panel is
 *   docked there with NoBackground + NoScrollbar, so it draws no window
 *   background of its own either.  Only the toolbar strip and the stats
 *   corner are visible; everything else is the live game beneath.
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
                        ImGuiWindowFlags.NoBackground        // transparent – game shows through
        );
    }

    @Override
    public String getTitle() { return TITLE; }

    @Override
    protected void renderContent() {
        renderToolbar();
        renderStatsCorner();
    }

    // ── Toolbar ───────────────────────────────────────────────────────────────

    private void renderToolbar() {
        // Semi-transparent child so the toolbar is readable but doesn't hide the game
        ImGui.pushStyleColor(ImGuiCol.ChildBg, 0.10f, 0.10f, 0.14f, 0.75f);
        boolean childOpen = ImGui.beginChild("##vp_bar", 0f, 34f, false,
                ImGuiWindowFlags.NoScrollbar | ImGuiWindowFlags.NoScrollWithMouse);
        ImGui.popStyleColor();

        if (!childOpen) { ImGui.endChild(); return; }

        ImGui.setCursorPosY(ImGui.getCursorPosY() + 4f);

        // Tool buttons
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
            // TODO: push to your world time manager
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

        // Position in the bottom-right of this panel's content area
        float cx = ImGui.getWindowPos().x + ImGui.getWindowWidth()  - boxW - pad;
        float cy = ImGui.getWindowPos().y + ImGui.getWindowHeight() - boxH - pad;
        ImGui.getWindowDrawList().addRectFilled(cx - pad, cy - pad, cx + boxW, cy + boxH,
                ImGui.colorConvertFloat4ToU32(0.07f, 0.07f, 0.10f, 0.70f), 4f);

        ImGui.setCursorPos(
                ImGui.getWindowWidth()  - boxW - pad,
                ImGui.getWindowHeight() - boxH - pad);

        ImGui.pushStyleColor(ImGuiCol.Text, 0.85f, 0.85f, 0.95f, 1f);
        for (String line : lines) {
            ImGui.textUnformatted(line);
        }
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