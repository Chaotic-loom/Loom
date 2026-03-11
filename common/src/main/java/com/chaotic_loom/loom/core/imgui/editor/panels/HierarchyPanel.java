package com.chaotic_loom.loom.core.imgui.editor.panels;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiSelectableFlags;
import imgui.flag.ImGuiTreeNodeFlags;
import imgui.type.ImString;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Hierarchy panel – left sidebar.
 * Lists entities in the loaded world, grouped by category.
 * Click an entity to select it (fires a selection event you can hook into).
 */
public class HierarchyPanel extends EditorPanel {

    public static final String TITLE = "  Hierarchy";

    private final ImString searchFilter = new ImString(64);
    private Entity selectedEntity = null;

    @Override public String getTitle() { return TITLE; }

    @Override
    protected void renderContent() {
        renderSearchBar();
        ImGui.separator();
        renderEntityTree();
    }

    // ── Search bar ────────────────────────────────────────────────────────

    private void renderSearchBar() {
        ImGui.setNextItemWidth(-1);
        ImGui.pushStyleColor(ImGuiCol.FrameBg, 0.10f, 0.10f, 0.13f, 1f);
        ImGui.inputTextWithHint("##hierarchy_search", "  Search entities…", searchFilter,
                ImGuiInputTextFlags.None);
        ImGui.popStyleColor();
    }

    // ── Entity tree ───────────────────────────────────────────────────────

    private void renderEntityTree() {
        Minecraft mc          = Minecraft.getInstance();
        ClientLevel level     = mc.level;   // mc.level is always ClientLevel on the client

        if (level == null) {
            ImGui.textDisabled("No world loaded");
            return;
        }

        String filter = searchFilter.get().toLowerCase().trim();

        // Collect entities
        List<Entity> players   = new ArrayList<>();
        List<Entity> creatures = new ArrayList<>();
        List<Entity> items     = new ArrayList<>();
        List<Entity> other     = new ArrayList<>();

        for (Entity e : level.entitiesForRendering()) {
            String name = e.getName().getString().toLowerCase();
            if (!filter.isEmpty() && !name.contains(filter)) continue;

            if (e instanceof Player)                          players.add(e);
            else if (e instanceof net.minecraft.world.entity.Mob) creatures.add(e);
            else if (e instanceof net.minecraft.world.entity.item.ItemEntity) items.add(e);
            else                                              other.add(e);
        }

        renderGroup("Players",   "  ", players);
        renderGroup("Creatures", "  ", creatures);
        renderGroup("Items",     "  ", items);
        renderGroup("Other",     "  ", other);
    }

    private void renderGroup(String label, String icon, List<Entity> entities) {
        if (entities.isEmpty()) return;

        int flags = ImGuiTreeNodeFlags.DefaultOpen | ImGuiTreeNodeFlags.SpanAvailWidth;
        boolean open = ImGui.treeNodeEx(icon + label + " (" + entities.size() + ")##grp_" + label, flags);

        if (open) {
            for (Entity e : entities) {
                renderEntityRow(e);
            }
            ImGui.treePop();
        }
    }

    private void renderEntityRow(Entity entity) {
        boolean selected = (entity == selectedEntity);
        String label = "  " + entity.getName().getString()
                + "##entity_" + entity.getId();

        if (selected) {
            ImGui.pushStyleColor(ImGuiCol.Header,        0.29f, 0.62f, 1.00f, 0.35f);
            ImGui.pushStyleColor(ImGuiCol.HeaderHovered, 0.29f, 0.62f, 1.00f, 0.50f);
        }

        int selFlags = ImGuiSelectableFlags.SpanAllColumns;
        if (ImGui.selectable(label, selected, selFlags)) {
            selectedEntity = (selected ? null : entity);
            // Broadcast selection for InspectorPanel to pick up
            EditorSelection.setSelectedEntity(entity);
        }

        if (selected) ImGui.popStyleColor(2);

        // Right-click context menu
        if (ImGui.beginPopupContextItem("##ctx_" + entity.getId())) {
            if (ImGui.menuItem("Teleport to Entity")) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null) {
                    mc.player.teleportTo(entity.getX(), entity.getY(), entity.getZ());
                }
            }
            if (ImGui.menuItem("Focus Camera"))  { /* TODO: smooth camera pan */ }
            ImGui.separator();
            if (ImGui.menuItem("Remove Entity")) { entity.discard(); }
            ImGui.endPopup();
        }
    }
}