package com.chaotic_loom.loom.core.imgui;

import imgui.ImGui;
import imgui.type.ImBoolean;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public class DebugWindows {

    private static ImBoolean showDemo    = new ImBoolean(false);
    private static ImBoolean showDebug   = new ImBoolean(true);

    public static void render() {
        Minecraft mc = Minecraft.getInstance();

        // Toggle with a keybind (see Step 4)
        if (!showDebug.get()) return;

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
            ImGui.checkbox("Show ImGui Demo", showDemo);
            if (showDemo.get()) ImGui.showDemoWindow();
        }
        ImGui.end();
    }

    public static void toggleVisibility() {
        showDebug.set(!showDebug.get());
    }
}