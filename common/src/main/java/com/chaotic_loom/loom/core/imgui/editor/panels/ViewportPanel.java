package com.chaotic_loom.loom.core.imgui.editor.panels;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiWindowFlags;
import net.minecraft.client.Minecraft;

/**
 * The centre viewport panel.
 *
 * Because ImGuiDockNodeFlags.PassthruCentralNode is set on the DockSpace,
 * the area occupied by this window is transparent – Minecraft renders
 * directly behind it. This panel draws only an overlay toolbar at the top.
 *
 * If you later want to redirect Minecraft rendering into an FBO and display
 * it as an ImGui image (gives you multi-viewport, picture-in-picture, etc.),
 * replace the passthru approach with {@code ImGui.image(fboTextureId, w, h)}.
 */
public class ViewportPanel extends EditorPanel {

    public static final String TITLE = "  Viewport";

    // Toolbar button state
    private int   activeTool   = 0;   // 0=Select 1=Move 2=Rotate 3=Scale
    private boolean showGrid   = true;
    private boolean showGizmos = true;
    private float   timeOfDay  = 0.5f;

    private static final String[] TOOL_ICONS  = { "  Select", "  Move", "  Rotate", "  Scale" };
    private static final int[]    TOOL_KEYS   = { 0, 1, 2, 3 };

    public ViewportPanel() {
        super(
                ImGuiWindowFlags.NoScrollbar      |
                        ImGuiWindowFlags.NoScrollWithMouse |
                        ImGuiWindowFlags.NoCollapse
        );
    }

    public void init() { /* future FBO setup here */ }

    @Override public String getTitle() { return TITLE; }

    @Override
    protected void renderContent() {
        renderViewportToolbar();
        renderViewportOverlayStats();
    }

    // ── Toolbar (rendered inside the docked panel) ────────────────────────

    private void renderViewportToolbar() {
        ImGui.pushStyleColor(ImGuiCol.ChildBg, 0.12f, 0.12f, 0.15f, 0.90f);
        ImGui.beginChild("##vp_toolbar", 0, 36, false,
                ImGuiWindowFlags.NoScrollbar | ImGuiWindowFlags.NoScrollWithMouse);

        ImGui.setCursorPosY(ImGui.getCursorPosY() + 5f);

        // Tool buttons
        for (int i = 0; i < TOOL_ICONS.length; i++) {
            if (i > 0) ImGui.sameLine(0, 2);
            boolean active = (activeTool == i);
            if (active) ImGui.pushStyleColor(ImGuiCol.Button, 0.29f, 0.62f, 1.00f, 1f);
            if (ImGui.button(TOOL_ICONS[i])) activeTool = i;
            if (active) ImGui.popStyleColor();
        }

        ImGui.sameLine(0, 16);
        ImGui.separator();
        ImGui.sameLine(0, 16);

        // Toggles
        pushToggleStyle(showGrid);
        if (ImGui.button("  Grid")) showGrid = !showGrid;
        popToggleStyle(showGrid);

        ImGui.sameLine(0, 4);
        pushToggleStyle(showGizmos);
        if (ImGui.button("  Gizmos")) showGizmos = !showGizmos;
        popToggleStyle(showGizmos);

        ImGui.sameLine(0, 16);
        ImGui.separator();
        ImGui.sameLine(0, 16);

        // Time of day slider
        ImGui.text("  Time");
        ImGui.sameLine(0, 6);
        ImGui.setNextItemWidth(100f);
        float[] tod = { timeOfDay };
        if (ImGui.sliderFloat("##tod", tod, 0f, 1f)) {
            timeOfDay = tod[0];
            // TODO: push time to your world tick manager
        }

        ImGui.endChild();
        ImGui.popStyleColor();
    }

    // ── Corner stats overlay ──────────────────────────────────────────────

    private void renderViewportOverlayStats() {
        Minecraft mc = Minecraft.getInstance();

        // Position it in the bottom-right of the viewport panel
        ImVec2 panelPos  = ImGui.getWindowPos();
        ImVec2 panelSize = ImGui.getWindowSize();

        float pad = 8f;
        String[] lines = buildStatLines(mc);
        float lineH   = ImGui.getTextLineHeightWithSpacing();
        float boxH    = lineH * lines.length + pad * 2;
        float boxW    = 200f;

        ImGui.setNextWindowPos(
                panelPos.x + panelSize.x - boxW - pad,
                panelPos.y + panelSize.y - boxH - pad
        );
        ImGui.setNextWindowBgAlpha(0.55f);
        ImGui.setNextWindowSize(boxW, boxH);

        int overlayFlags =
                ImGuiWindowFlags.NoDecoration   |
                        ImGuiWindowFlags.NoInputs        |
                        ImGuiWindowFlags.NoNav           |
                        ImGuiWindowFlags.NoMove          |
                        ImGuiWindowFlags.NoSavedSettings |
                        ImGuiWindowFlags.NoDocking;

        if (ImGui.begin("##vp_stats", overlayFlags)) {
            for (String line : lines) {
                ImGui.textDisabled(line);
            }
        }
        ImGui.end();
    }

    private String[] buildStatLines(Minecraft mc) {
        if (mc.player == null) return new String[]{ "No player" };
        net.minecraft.world.entity.player.Player p = mc.player;
        return new String[]{
                String.format("XYZ  %.1f  %.1f  %.1f", p.getX(), p.getY(), p.getZ()),
                String.format("Facing  %.1f°  %.1f°",  p.getYRot(), p.getXRot()),
                String.format("Biome  %s",
                        mc.level != null
                                ? mc.level.getBiome(p.blockPosition()).unwrapKey()
                                .map(k -> k.location().getPath()).orElse("?")
                                : "—"),
                String.format("FPS  %d", mc.getFps()),
        };
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private void pushToggleStyle(boolean on) {
        if (on) ImGui.pushStyleColor(ImGuiCol.Button, 0.20f, 0.45f, 0.20f, 1f);
        else    ImGui.pushStyleColor(ImGuiCol.Button, 0.18f, 0.18f, 0.22f, 1f);
    }
    private void popToggleStyle(boolean on) {
        ImGui.popStyleColor();
    }
}