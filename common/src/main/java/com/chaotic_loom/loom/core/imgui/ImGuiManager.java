package com.chaotic_loom.loom.core.imgui;

import com.chaotic_loom.loom.core.imgui.editor.EditorLayout;
import com.chaotic_loom.loom.core.imgui.editor.panels.ConsolePanel;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiConfigFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import net.minecraft.client.Minecraft;

/**
 * Manages the ImGui lifecycle: init, per-frame begin/end, dispose.
 *
 * Editor mode vs. HUD mode
 * ─────────────────────────
 * When editorMode = true:
 *   - Full EditorLayout is rendered (dockspace, menu bar, all panels).
 *   - ImGui captures mouse / keyboard input.
 *
 * When editorMode = false:
 *   - Only lightweight HUD windows (e.g. DebugWindows) are rendered.
 *   - ImGui does NOT capture input (Minecraft controls normally).
 */
public class ImGuiManager {

    private static ImGuiImplGlfw imguiGlfw;
    private static ImGuiImplGl3  imguiGl3;
    private static boolean       initialized = false;
    private static boolean       editorMode  = false;

    // ── Lifecycle ─────────────────────────────────────────────────────────

    public static void init() {
        if (initialized) return;

        long windowHandle = Minecraft.getInstance().getWindow().getWindow();

        ImGui.createContext();

        ImGuiIO io = ImGui.getIO();
        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard);
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable);
        io.setIniFilename(null);   // Disable layout persistence (managed by EditorLayout)

        imguiGlfw = new ImGuiImplGlfw();
        imguiGl3  = new ImGuiImplGl3();

        // 'true' = install GLFW callbacks (handles mouse & keyboard)
        imguiGlfw.init(windowHandle, true);
        imguiGl3.init("#version 150");

        // Apply theme before the first frame so no flash of unstyled UI
        EditorLayout.applyEditorTheme();

        initialized = true;
    }

    /** Call once when a world is loaded to finish editor setup */
    public static void initEditor() {
        if (!initialized) init();
        EditorLayout.init();
    }

    // ── Per-frame API ─────────────────────────────────────────────────────

    /** Call at the start of each frame, before any ImGui calls. */
    public static void beginFrame() {
        if (!initialized) init();

        // Allow / block Minecraft input based on editor mode
        updateInputPassthrough();

        imguiGlfw.newFrame();
        imguiGl3.newFrame();
        ImGui.newFrame();
    }

    /** Call after all your ImGui window calls. */
    public static void endFrame() {
        ImGui.render();
        imguiGl3.renderDrawData(ImGui.getDrawData());

        // Required when multi-viewport is enabled (optional, enable in io flags if needed)
        // ImGui.updatePlatformWindows();
        // ImGui.renderPlatformWindowsDefault();
    }

    /**
     * Render the appropriate UI for this frame.
     * Call this between {@link #beginFrame()} and {@link #endFrame()}.
     */
    public static void renderUI() {
        if (editorMode) {
            EditorLayout.render();
        }
        DebugWindows.render();   // always-on lightweight overlay
    }

    public static void dispose() {
        if (!initialized) return;
        imguiGl3.shutdown();
        imguiGlfw.shutdown();
        ImGui.destroyContext();
        initialized = false;
    }

    // ── Editor mode toggle ────────────────────────────────────────────────

    /** Toggle between full editor layout and normal play */
    public static void toggleEditorMode() {
        editorMode = !editorMode;
        ConsolePanel.info(editorMode ? "Editor mode ON" : "Editor mode OFF");
    }

    public static void setEditorMode(boolean on) { editorMode = on; }
    public static boolean isEditorMode()         { return editorMode; }
    public static boolean isInitialized()        { return initialized; }

    // ── Input passthrough ─────────────────────────────────────────────────

    /**
     * In editor mode: ImGui captures all input.
     * In play mode: ImGui does NOT steal input from Minecraft.
     */
    private static void updateInputPassthrough() {
        ImGuiIO io = ImGui.getIO();
        if (!editorMode) {
            // Remove the flags so Minecraft gets all input
            io.setConfigFlags(io.getConfigFlags() & ~ImGuiConfigFlags.NavEnableKeyboard);
        } else {
            io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard);
        }
    }
}