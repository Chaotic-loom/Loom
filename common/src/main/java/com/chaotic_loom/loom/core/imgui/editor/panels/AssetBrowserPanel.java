package com.chaotic_loom.loom.core.imgui.editor.panels;

import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiSelectableFlags;
import imgui.type.ImString;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * Asset Browser panel – docked alongside Console in the bottom strip.
 *
 * Shows navigable namespaces/paths from Minecraft's resource pack system.
 * Click an asset to "select" it (useful as a drag-source for your tools).
 */
public class AssetBrowserPanel extends EditorPanel {

    public static final String TITLE = "  Assets";

    private final ImString search = new ImString(128);
    private String selectedPath   = null;

    // Breadcrumb navigation
    private String currentNamespace = null;
    private String currentFolder    = null;

    // Simulated asset categories (replace with real resource-pack scanning)
    private static final String[] NAMESPACES = { "minecraft", "mod_assets", "textures", "sounds" };
    private static final String[] FOLDERS    = { "block", "item", "entity", "gui", "environment" };

    @Override public String getTitle() { return TITLE; }

    @Override
    protected void renderContent() {
        renderBreadcrumb();
        ImGui.separator();

        float leftW = 130f;
        renderNamespaceList(leftW);
        ImGui.sameLine();
        ImGui.separator();
        ImGui.sameLine();
        renderAssetGrid();
    }

    // ── Breadcrumb ────────────────────────────────────────────────────────

    private void renderBreadcrumb() {
        ImGui.setNextItemWidth(200f);
        ImGui.pushStyleColor(ImGuiCol.FrameBg, 0.10f, 0.10f, 0.13f, 1f);
        ImGui.inputTextWithHint("##asset_search", "  Filter…", search, ImGuiInputTextFlags.None);
        ImGui.popStyleColor();

        ImGui.sameLine(0, 12);

        // Breadcrumb path buttons
        if (ImGui.smallButton("  Root")) { currentNamespace = null; currentFolder = null; }
        if (currentNamespace != null) {
            ImGui.sameLine(); ImGui.textDisabled("›");
            ImGui.sameLine();
            if (ImGui.smallButton(currentNamespace)) currentFolder = null;
        }
        if (currentFolder != null) {
            ImGui.sameLine(); ImGui.textDisabled("›");
            ImGui.sameLine();
            ImGui.smallButton(currentFolder);
        }
    }

    // ── Left namespace list ───────────────────────────────────────────────

    private void renderNamespaceList(float width) {
        ImGui.pushStyleColor(ImGuiCol.ChildBg, 0.10f, 0.10f, 0.13f, 1f);
        ImGui.beginChild("##asset_ns", width, 0f, true);

        ImGui.textDisabled("  Namespaces");
        ImGui.separator();

        for (String ns : NAMESPACES) {
            boolean active = ns.equals(currentNamespace);
            if (active) ImGui.pushStyleColor(ImGuiCol.Header, 0.29f, 0.62f, 1.00f, 0.35f);
            if (ImGui.selectable("  " + ns + "##ns", active, ImGuiSelectableFlags.None)) {
                currentNamespace = ns;
                currentFolder    = null;
            }
            if (active) ImGui.popStyleColor();
        }

        ImGui.endChild();
        ImGui.popStyleColor();
    }

    // ── Right asset grid ──────────────────────────────────────────────────

    private void renderAssetGrid() {
        ImGui.beginChild("##asset_grid", 0f, 0f, false);

        if (currentNamespace == null) {
            renderLandingTiles();
        } else if (currentFolder == null) {
            renderFolderTiles();
        } else {
            renderAssetTiles();
        }

        ImGui.endChild();
    }

    private void renderLandingTiles() {
        ImGui.textDisabled("  Select a namespace to browse assets");
        ImGui.spacing();
        float tileSize = 80f;
        for (String ns : NAMESPACES) {
            renderTile("  " + ns, tileSize, false, () -> {
                currentNamespace = ns;
            });
            ImGui.sameLine(0, 4);
        }
    }

    private void renderFolderTiles() {
        ImGui.textDisabled("  " + currentNamespace + " / …");
        ImGui.spacing();
        float tileSize = 80f;
        for (String folder : FOLDERS) {
            renderTile("  " + folder, tileSize, false, () -> {
                currentFolder = folder;
            });
            ImGui.sameLine(0, 4);
        }
    }

    private void renderAssetTiles() {
        String filter = search.get().toLowerCase();

        // Simulated asset list – replace with real ResourceLocation enumeration
        List<String> fakeAssets = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            String name = currentFolder + "_" + String.format("%03d", i) + ".png";
            if (filter.isEmpty() || name.contains(filter)) fakeAssets.add(name);
        }

        float tileSize = 72f;
        float panelWidth = ImGui.getContentRegionAvailX();
        int cols = Math.max(1, (int)(panelWidth / (tileSize + 6)));
        int col = 0;

        for (String asset : fakeAssets) {
            String fullPath = currentNamespace + ":" + currentFolder + "/" + asset;
            boolean selected = fullPath.equals(selectedPath);

            renderTile("  " + asset.replace(".png",""), tileSize, selected, () -> {
                selectedPath = fullPath;
                EditorSelection.setSelectedAsset(fullPath);
            });

            col++;
            if (col < cols) ImGui.sameLine(0, 4);
            else            col = 0;
        }
    }

    // ── Tile helper ───────────────────────────────────────────────────────

    private void renderTile(String label, float size, boolean selected, Runnable onClick) {
        float[] bgColor = selected
                ? new float[]{ 0.29f, 0.62f, 1.00f, 0.40f }
                : new float[]{ 0.15f, 0.15f, 0.20f, 1.00f };

        ImGui.pushStyleColor(ImGuiCol.Button,        bgColor[0], bgColor[1], bgColor[2], bgColor[3]);
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0.29f, 0.62f, 1.00f, 0.30f);

        if (ImGui.button(label + "##tile_" + label, size, size)) {
            onClick.run();
        }

        ImGui.popStyleColor(2);
    }
}