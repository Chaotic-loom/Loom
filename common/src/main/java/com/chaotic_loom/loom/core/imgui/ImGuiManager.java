package com.chaotic_loom.loom.core.imgui;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiConfigFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import net.minecraft.client.Minecraft;

public class ImGuiManager {
    private static ImGuiImplGlfw imguiGlfw;
    private static ImGuiImplGl3  imguiGl3;
    private static boolean initialized = false;

    public static void init() {
        if (initialized) return;

        long windowHandle = Minecraft.getInstance().getWindow().getWindow();

        ImGui.createContext();

        ImGuiIO io = ImGui.getIO();
        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard);
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable);
        io.setIniFilename(null); // Disable imgui.ini if you don't want saved layouts

        imguiGlfw = new ImGuiImplGlfw();
        imguiGl3  = new ImGuiImplGl3();

        // 'true' = install GLFW callbacks (handles mouse/keyboard for you)
        imguiGlfw.init(windowHandle, true);
        imguiGl3.init("#version 150");

        initialized = true;
    }

    /** Call at the start of each frame, before any ImGui calls */
    public static void beginFrame() {
        if (!initialized) init();
        imguiGlfw.newFrame();
        ImGui.newFrame();
    }

    /** Call after all your ImGui window calls */
    public static void endFrame() {
        ImGui.render();
        imguiGl3.renderDrawData(ImGui.getDrawData());
    }

    public static void dispose() {
        if (!initialized) return;
        imguiGl3.dispose();
        imguiGlfw.dispose();
        ImGui.destroyContext();
        initialized = false;
    }

    public static boolean isInitialized() {
        return initialized;
    }
}