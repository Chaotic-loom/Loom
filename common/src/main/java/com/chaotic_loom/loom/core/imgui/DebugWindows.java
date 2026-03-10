package com.chaotic_loom.loom.core.imgui;

import imgui.ImGui;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public class DebugWindows {

    private static boolean showDemo    = false;
    private static boolean showDebug   = true;

    public static void render() {
        Minecraft mc = Minecraft.getInstance();

        // Toggle with a keybind (see Step 4)
        if (!showDebug) return;

        if (ImGui.begin("My Mod Debug")) {
            ImGui.text("FPS: " + mc.getFps());

            Player player = mc.player;
            if (player != null) {
                ImGui.separator();
                ImGui.text("Player Info");
                ImGui.text(String.format("Pos: %.2f, %.2f, %.2f",
                        player.getX(), player.getY(), player.getZ()));
                ImGui.text(String.format("Health: %.1f / %.1f",
                        player.getHealth(), player.getMaxHealth()));
            }

            ImGui.separator();
            ImGui.checkbox("Show ImGui Demo", new boolean[]{showDemo}); // see note below
            if (showDemo) ImGui.showDemoWindow();
        }
        ImGui.end();
    }

    public static void toggleVisibility() {
        showDebug = !showDebug;
    }
}