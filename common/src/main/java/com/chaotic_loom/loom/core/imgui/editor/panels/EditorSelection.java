package com.chaotic_loom.loom.core.imgui.editor.panels;

import net.minecraft.world.entity.Entity;

/**
 * Shared selection state.
 * HierarchyPanel writes here; InspectorPanel and other panels read from here.
 * Keep this simple – no events, no listeners, just plain static state.
 */
public class EditorSelection {

    private static Entity selectedEntity = null;
    private static String selectedAsset  = null;

    // ── Entity ────────────────────────────────────────────────────────────

    public static void setSelectedEntity(Entity e) {
        selectedEntity = e;
        selectedAsset  = null;  // deselect asset when entity is chosen
    }

    public static Entity getSelectedEntity() {
        // Guard against stale/removed entities
        if (selectedEntity != null && selectedEntity.isRemoved()) {
            selectedEntity = null;
        }
        return selectedEntity;
    }

    // ── Asset ─────────────────────────────────────────────────────────────

    public static void setSelectedAsset(String path) {
        selectedAsset  = path;
        selectedEntity = null;
    }

    public static String getSelectedAsset() { return selectedAsset; }

    // ── Clear ─────────────────────────────────────────────────────────────

    public static void clearAll() {
        selectedEntity = null;
        selectedAsset  = null;
    }
}