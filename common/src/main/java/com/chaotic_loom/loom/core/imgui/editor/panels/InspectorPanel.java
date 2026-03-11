package com.chaotic_loom.loom.core.imgui.editor.panels;

import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiTableFlags;
import imgui.flag.ImGuiTreeNodeFlags;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * Inspector panel – right sidebar.
 * Shows properties of the entity selected in {@link HierarchyPanel},
 * or the local player when nothing is selected.
 */
public class InspectorPanel extends EditorPanel {

    public static final String TITLE = "  Inspector";

    @Override public String getTitle() { return TITLE; }

    @Override
    protected void renderContent() {
        Entity target = EditorSelection.getSelectedEntity();
        if (target == null) {
            Minecraft mc = Minecraft.getInstance();
            target = mc.player;
        }

        if (target == null) {
            ImGui.textDisabled("Nothing selected");
            return;
        }

        renderEntityHeader(target);
        ImGui.spacing();
        renderTransformSection(target);
        renderEntitySection(target);
        if (target instanceof LivingEntity living) {
            renderLivingSection(living);
        }
        if (target instanceof Player player) {
            renderPlayerSection(player);
        }
    }

    // ── Header ────────────────────────────────────────────────────────────

    private void renderEntityHeader(Entity e) {
        ImGui.pushStyleColor(ImGuiCol.ChildBg, 0.12f, 0.12f, 0.16f, 1f);
        ImGui.beginChild("##insp_header", 0f, 56f, false);

        ImGui.setCursorPos(12f, 8f);
        ImGui.textColored(0.9f, 0.9f, 1.0f, 1f, e.getName().getString());

        ImGui.setCursorPos(12f, 28f);
        String typeStr = e.getType().toShortString();
        ImGui.textDisabled(typeStr + "  #" + e.getId());

        ImGui.endChild();
        ImGui.popStyleColor();
        ImGui.separator();
    }

    // ── Sections ──────────────────────────────────────────────────────────

    private void renderTransformSection(Entity e) {
        if (!sectionHeader("Transform", true)) return;

        float[] pos = { (float) e.getX(), (float) e.getY(), (float) e.getZ() };
        float[] rot = { e.getYRot(), e.getXRot() };

        ImGui.setNextItemWidth(-1);
        if (propDragFloat3("Position", pos, 0.1f)) {
            e.setPos(pos[0], pos[1], pos[2]);
        }

        ImGui.setNextItemWidth(-1);
        if (propDragFloat2("Rotation", rot, 0.5f)) {
            e.setYRot(rot[0]);
            e.setXRot(rot[1]);
        }

        float[] vel = {
                (float) e.getDeltaMovement().x,
                (float) e.getDeltaMovement().y,
                (float) e.getDeltaMovement().z
        };
        ImGui.setNextItemWidth(-1);
        if (propDragFloat3("Velocity", vel, 0.01f)) {
            e.setDeltaMovement(vel[0], vel[1], vel[2]);
        }

        ImGui.treePop();
    }

    private void renderEntitySection(Entity e) {
        if (!sectionHeader("Entity", false)) return;

        readOnlyProp("ID",        String.valueOf(e.getId()));
        readOnlyProp("UUID",      e.getStringUUID());
        readOnlyProp("Dimension", e.level().dimension().location().toString());
        readOnlyProp("On Ground", String.valueOf(e.onGround()));
        readOnlyProp("In Water",  String.valueOf(e.isInWater()));
        readOnlyProp("Tags",      e.getTags().isEmpty() ? "—" : String.join(", ", e.getTags()));

        ImGui.treePop();
    }

    private void renderLivingSection(LivingEntity e) {
        if (!sectionHeader("Living Entity", false)) return;

        float[] hp   = { e.getHealth() };
        float maxHp  =   e.getMaxHealth();
        ImGui.text("Health");
        ImGui.sameLine(labelWidth());
        ImGui.setNextItemWidth(ImGui.getContentRegionAvailX() - 50f);
        if (ImGui.sliderFloat("##hp", hp, 0f, maxHp)) {
            e.setHealth(hp[0]);
        }
        ImGui.sameLine();
        ImGui.textDisabled(String.format("%.1f / %.1f", hp[0], maxHp));

        readOnlyProp("Dead",         String.valueOf(e.isDeadOrDying()));
        readOnlyProp("Armor",        String.valueOf(e.getArmorValue()));
        readOnlyProp("Active effects",
                e.getActiveEffects().isEmpty() ? "none" :
                        e.getActiveEffects().stream()
                                .map(eff -> eff.getEffect().getDescriptionId()
                                        .replace("effect.minecraft.", ""))
                                .reduce((a, b) -> a + ", " + b).orElse("—"));

        ImGui.treePop();
    }

    private void renderPlayerSection(Player p) {
        if (!sectionHeader("Player", false)) return;

        readOnlyProp("Username",    p.getGameProfile().getName());
        readOnlyProp("Game mode",   Minecraft.getInstance().gameMode != null
                ? Minecraft.getInstance().gameMode.getPlayerMode().getName()
                : "?");

        float[] food = { p.getFoodData().getFoodLevel() };
        ImGui.text("Hunger");
        ImGui.sameLine(labelWidth());
        ImGui.setNextItemWidth(ImGui.getContentRegionAvailX() - 50f);
        if (ImGui.sliderFloat("##hunger", food, 0f, 20f)) {
            p.getFoodData().setFoodLevel((int) food[0]);
        }
        ImGui.sameLine();
        ImGui.textDisabled(String.format("%.0f / 20", food[0]));

        float[] xp = { p.experienceProgress };
        ImGui.text("XP");
        ImGui.sameLine(labelWidth());
        ImGui.setNextItemWidth(ImGui.getContentRegionAvailX() - 50f);
        if (ImGui.sliderFloat("##xp", xp, 0f, 1f)) {
            p.experienceProgress = xp[0];
        }
        ImGui.sameLine();
        ImGui.textDisabled("Lvl " + p.experienceLevel);

        ImGui.treePop();
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    /** Returns true when the collapsible section is open. Caller must call ImGui.treePop(). */
    private boolean sectionHeader(String label, boolean defaultOpen) {
        int flags = ImGuiTreeNodeFlags.SpanAvailWidth | ImGuiTreeNodeFlags.Framed |
                ImGuiTreeNodeFlags.FramePadding;
        if (defaultOpen) flags |= ImGuiTreeNodeFlags.DefaultOpen;

        ImGui.pushStyleColor(ImGuiCol.Header,        0.20f, 0.20f, 0.28f, 1f);
        ImGui.pushStyleColor(ImGuiCol.HeaderHovered, 0.25f, 0.25f, 0.35f, 1f);
        boolean open = ImGui.treeNodeEx("  " + label + "##sec", flags);
        ImGui.popStyleColor(2);
        return open;
    }

    private void readOnlyProp(String label, String value) {
        ImGui.textDisabled(label);
        ImGui.sameLine(labelWidth());
        ImGui.textColored(0.85f, 0.85f, 0.95f, 1f, value);
    }

    /** Drag control for 3 floats. Returns true when changed. */
    private boolean propDragFloat3(String id, float[] v, float speed) {
        ImGui.textDisabled(id);
        ImGui.sameLine(labelWidth());
        ImGui.setNextItemWidth(-1f);
        return ImGui.dragFloat3("##" + id, v, speed);
    }

    private boolean propDragFloat2(String id, float[] v, float speed) {
        ImGui.textDisabled(id);
        ImGui.sameLine(labelWidth());
        ImGui.setNextItemWidth(-1f);
        return ImGui.dragFloat2("##" + id, v, speed);
    }

    private float labelWidth() { return 90f; }
}