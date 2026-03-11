package com.chaotic_loom.loom.core.imgui;

import com.chaotic_loom.loom.core.imgui.editor.EditorLayout;
import com.chaotic_loom.loom.core.imgui.editor.panels.ConsolePanel;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiConfigFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL30;

public class ImGuiManager {

    private static ImGuiImplGlfw imguiGlfw;
    private static ImGuiImplGl3  imguiGl3;
    private static boolean       initialized = false;
    private static boolean       editorMode  = true;

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    public static void init() {
        if (initialized) return;

        long windowHandle = Minecraft.getInstance().getWindow().getWindow();

        ImGui.createContext();

        ImGuiIO io = ImGui.getIO();
        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard);
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable);
        io.setIniFilename(null);

        imguiGlfw = new ImGuiImplGlfw();
        imguiGl3  = new ImGuiImplGl3();

        imguiGlfw.init(windowHandle, true);
        imguiGl3.init("#version 150");

        EditorLayout.init();

        initialized = true;
    }

    // ── Per-frame API ─────────────────────────────────────────────────────────

    public static void beginFrame() {
        if (!initialized) init();
        updateInputPassthrough();
        imguiGlfw.newFrame();
        imguiGl3.newFrame();
        ImGui.newFrame();
    }

    public static void endFrame() {
        ImGui.render();

        // Unbind Minecraft's FBO before rendering ImGui so it draws to the
        // real screen backbuffer, not into the game texture.
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);

        imguiGl3.renderDrawData(ImGui.getDrawData());
    }

    public static void renderUI() {
        if (editorMode) {
            EditorLayout.render();
        }
        DebugWindows.render();
    }

    public static void dispose() {
        if (!initialized) return;
        imguiGl3.shutdown();
        imguiGlfw.shutdown();
        ImGui.destroyContext();
        initialized = false;
    }

    // ── Editor mode ───────────────────────────────────────────────────────────

    public static void toggleEditorMode() {
        editorMode = !editorMode;
        ConsolePanel.info(editorMode ? "Editor mode ON" : "Editor mode OFF");
    }

    public static void setEditorMode(boolean on) { editorMode = on; }
    public static boolean isEditorMode()         { return editorMode; }
    public static boolean isInitialized()        { return initialized; }

    // ── Input passthrough ─────────────────────────────────────────────────────

    private static void updateInputPassthrough() {
        ImGuiIO io = ImGui.getIO();
        if (!editorMode) {
            io.setConfigFlags(io.getConfigFlags() & ~ImGuiConfigFlags.NavEnableKeyboard);
        } else {
            io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard);
        }
    }
}