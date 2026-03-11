package com.chaotic_loom.loom.core.imgui.editor;

import com.chaotic_loom.loom.core.imgui.DebugWindows;
import com.chaotic_loom.loom.core.imgui.editor.panels.*;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiConfigFlags;
import imgui.flag.ImGuiDockNodeFlags;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;

/**
 * Axiom/Flashback-style editor layout.
 *
 * ┌──────────────────────────────────────────────────────┐
 * │  [File] [Edit] [View] [Tools]        FPS  [●] [■]   │  ← MenuBar
 * ├──────────┬───────────────────────────┬───────────────┤
 * │          │                           │               │
 * │ Hierarchy│       VIEWPORT            │   Inspector   │
 * │  (left)  │     (passthru / FBO)      │   (right)     │
 * │          │                           │               │
 * ├──────────┴───────────────────────────┴───────────────┤
 * │  Console │ Assets │ ...  (bottom tabs)               │
 * └──────────────────────────────────────────────────────┘
 */
public class EditorLayout {

    // Panel instances
    private static final HierarchyPanel   hierarchy   = new HierarchyPanel();
    private static final InspectorPanel   inspector   = new InspectorPanel();
    private static final ConsolePanel     console     = new ConsolePanel();
    private static final AssetBrowserPanel assets     = new AssetBrowserPanel();
    private static final ViewportPanel    viewport    = new ViewportPanel();

    // Seed initial panel positions once per editor session.
    // After that, imgui.ini (if enabled) or the dockspace remembers everything.
    private static boolean initialPositionsSet = false;

    // ── Colour palette (dark editor theme) ──────────────────────────────────
    private static final float[] BG_DARK       = hex(0x1A1A1F);
    private static final float[] BG_MID        = hex(0x23232A);
    private static final float[] BG_LIGHT      = hex(0x2C2C35);
    private static final float[] ACCENT        = hex(0x4A9EFF);
    private static final float[] ACCENT_HOVER  = hex(0x6AB4FF);
    private static final float[] ACCENT_ACTIVE = hex(0x2A7EDF);
    private static final float[] TEXT_PRIMARY   = hex(0xE8E8F0);
    private static final float[] TEXT_DIM       = hex(0x888899);
    private static final float[] BORDER        = hex(0x3A3A48);
    private static final float[] TAB_ACTIVE    = hex(0x2C2C35);
    private static final float[] TAB_INACTIVE  = hex(0x1E1E25);
    private static final float[] TITLE_BG      = hex(0x1A1A1F);
    private static final float[] HEADER        = hex(0x35354A);

    // ────────────────────────────────────────────────────────────────────────

    public static void init() {
        applyEditorTheme();
        viewport.init();
    }

    public static void render() {
        pushFullscreenHostWindow();

        int dockspaceId = ImGui.getID("EditorDockSpace");
        ImGui.dockSpace(dockspaceId, 0, 0, ImGuiDockNodeFlags.PassthruCentralNode);

        popFullscreenHostWindow();

        // Seed initial panel positions/sizes on the very first frame.
        // ImGui respects these only while no saved layout exists (FirstUseEver).
        if (!initialPositionsSet) {
            seedInitialLayout();
            initialPositionsSet = true;
        }

        // ── Render all panels ──────────────────────────────────────────────
        renderMenuBar();
        hierarchy.render();
        inspector.render();
        console.render();
        assets.render();
        viewport.render();
    }

    // ── Initial layout seeding ────────────────────────────────────────────────
    //
    // imgui-java does not expose the DockBuilder API (dockBuilderSplitNode etc.).
    // Instead we set each panel's position and size using ImGuiCond.FirstUseEver
    // so ImGui only applies them when there is no saved layout for that window.
    // The user can then freely drag / resize docks and their arrangement persists.
    //
    // Approximate default layout (percentage of screen):
    //   Left  20 %  → Hierarchy
    //   Right 22 %  → Inspector
    //   Bottom 25 % → Console / Assets
    //   Centre rest → Viewport
    //
    private static void seedInitialLayout() {
        imgui.ImGuiViewport vp = ImGui.getMainViewport();
        float W  = vp.getSizeX();
        float H  = vp.getSizeY();
        float ox = vp.getPosX();
        float oy = vp.getPosY();

        float menuBarH = ImGui.getFrameHeight(); // approximate menu-bar height

        float leftW   = W * 0.20f;
        float rightW  = W * 0.22f;
        float midW    = W - leftW - rightW;
        float bottomH = (H - menuBarH) * 0.25f;
        float topH    = H - menuBarH - bottomH;

        // Hierarchy – left column
        ImGui.setNextWindowPos (ox,               oy + menuBarH,        ImGuiCond.FirstUseEver);
        ImGui.setNextWindowSize(leftW,             topH,                 ImGuiCond.FirstUseEver);
        // (window is opened by HierarchyPanel.render() immediately after)

        hierarchy.seedNextFrame(ox,           oy + menuBarH,        leftW,  topH);
        inspector.seedNextFrame(ox + W - rightW, oy + menuBarH,     rightW, topH);
        viewport .seedNextFrame(ox + leftW,   oy + menuBarH,        midW,   topH);
        console  .seedNextFrame(ox,           oy + menuBarH + topH, W * 0.5f, bottomH);
        assets   .seedNextFrame(ox + W * 0.5f,oy + menuBarH + topH, W * 0.5f, bottomH);
    }

    /** Reset to default positions next frame (clears saved docking state). */
    public static void resetLayout() {
        initialPositionsSet = false;
        hierarchy.resetSeed();
        inspector.resetSeed();
        viewport .resetSeed();
        console  .resetSeed();
        assets   .resetSeed();
    }

    // ── Menu bar ─────────────────────────────────────────────────────────────

    private static void renderMenuBar() {
        if (ImGui.beginMainMenuBar()) {

            // ── File ──────────────────────────────────────────────────────
            if (ImGui.beginMenu("File")) {
                if (ImGui.menuItem("New Project",    "Ctrl+N")) { /* TODO */ }
                if (ImGui.menuItem("Open Project…",  "Ctrl+O")) { /* TODO */ }
                ImGui.separator();
                if (ImGui.menuItem("Save",           "Ctrl+S")) { /* TODO */ }
                if (ImGui.menuItem("Save As…",       "Ctrl+Shift+S")) { /* TODO */ }
                ImGui.separator();
                if (ImGui.menuItem("Exit")) {
                    net.minecraft.client.Minecraft.getInstance().stop();
                }
                ImGui.endMenu();
            }

            // ── Edit ──────────────────────────────────────────────────────
            if (ImGui.beginMenu("Edit")) {
                if (ImGui.menuItem("Undo",  "Ctrl+Z")) { /* TODO */ }
                if (ImGui.menuItem("Redo",  "Ctrl+Y")) { /* TODO */ }
                ImGui.separator();
                if (ImGui.menuItem("Preferences…")) { /* TODO */ }
                ImGui.endMenu();
            }

            // ── View ──────────────────────────────────────────────────────
            if (ImGui.beginMenu("View")) {
                if (ImGui.menuItem("Hierarchy",    null, hierarchy.isVisible()))   hierarchy.toggleVisibility();
                if (ImGui.menuItem("Inspector",    null, inspector.isVisible()))   inspector.toggleVisibility();
                if (ImGui.menuItem("Console",      null, console.isVisible()))     console.toggleVisibility();
                if (ImGui.menuItem("Asset Browser",null, assets.isVisible()))      assets.toggleVisibility();
                ImGui.separator();
                if (ImGui.menuItem("Reset Layout")) resetLayout();
                ImGui.endMenu();
            }

            // ── Tools ─────────────────────────────────────────────────────
            if (ImGui.beginMenu("Tools")) {
                if (ImGui.menuItem("World Settings")) { /* TODO */ }
                if (ImGui.menuItem("Lighting Override")) { /* TODO */ }
                if (ImGui.menuItem("Entity Spawner")) { /* TODO */ }
                ImGui.separator();
                if (ImGui.menuItem("ImGui Demo")) DebugWindows.toggleVisibility();
                ImGui.endMenu();
            }

            // ── Right-aligned status ──────────────────────────────────────
            float fps = net.minecraft.client.Minecraft.getInstance().getFps();
            String fpsText = String.format("%.0f FPS", fps);
            float fpsWidth = ImGui.calcTextSize(fpsText).x + 16f;
            ImGui.setCursorPosX(ImGui.getContentRegionAvailX() - fpsWidth);

            float[] fpsColor = fps >= 60 ? hex(0x4AFF88) : fps >= 30 ? hex(0xFFCC44) : hex(0xFF5555);
            ImGui.textColored(fpsColor[0], fpsColor[1], fpsColor[2], 1f, fpsText);

            ImGui.endMainMenuBar();
        }
    }

    // ── Fullscreen transparent host window ───────────────────────────────────

    private static void pushFullscreenHostWindow() {
        imgui.ImGuiViewport vp = ImGui.getMainViewport();
        ImGui.setNextWindowPos(vp.getPosX(), vp.getPosY());
        ImGui.setNextWindowSize(vp.getSizeX(), vp.getSizeY());
        ImGui.setNextWindowViewport(vp.getID());

        int hostFlags =
                ImGuiWindowFlags.NoDocking             |
                        ImGuiWindowFlags.NoTitleBar             |
                        ImGuiWindowFlags.NoCollapse             |
                        ImGuiWindowFlags.NoResize               |
                        ImGuiWindowFlags.NoMove                 |
                        ImGuiWindowFlags.NoBringToFrontOnFocus  |
                        ImGuiWindowFlags.NoBackground           |
                        ImGuiWindowFlags.MenuBar;

        ImGui.pushStyleVar(ImGuiStyleVar.WindowRounding, 0f);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowBorderSize, 0f);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 0f, 0f);
        ImGui.begin("##EditorHost", hostFlags);
        ImGui.popStyleVar(3);
    }

    private static void popFullscreenHostWindow() {
        ImGui.end();
    }

    // ── Theme ────────────────────────────────────────────────────────────────

    public static void applyEditorTheme() {
        imgui.ImGuiStyle style = ImGui.getStyle();

        // Rounding & borders
        style.setWindowRounding(4f);
        style.setChildRounding(4f);
        style.setFrameRounding(3f);
        style.setPopupRounding(4f);
        style.setScrollbarRounding(3f);
        style.setGrabRounding(3f);
        style.setTabRounding(4f);
        style.setWindowBorderSize(1f);
        style.setFrameBorderSize(0f);
        style.setPopupBorderSize(1f);

        // Spacing
        style.setFramePadding(8f, 4f);
        style.setItemSpacing(8f, 4f);
        style.setItemInnerSpacing(6f, 4f);
        style.setIndentSpacing(16f);
        style.setScrollbarSize(12f);
        style.setGrabMinSize(8f);
        style.setWindowMinSize(100f, 60f);

        // Colours
        ImGui.pushStyleColor(ImGuiCol.WindowBg,          BG_MID[0],   BG_MID[1],   BG_MID[2],   1.00f);
        ImGui.pushStyleColor(ImGuiCol.ChildBg,           BG_DARK[0],  BG_DARK[1],  BG_DARK[2],  1.00f);
        ImGui.pushStyleColor(ImGuiCol.PopupBg,           BG_MID[0],   BG_MID[1],   BG_MID[2],   0.96f);
        ImGui.pushStyleColor(ImGuiCol.Border,            BORDER[0],   BORDER[1],   BORDER[2],   1.00f);
        ImGui.pushStyleColor(ImGuiCol.FrameBg,           BG_DARK[0],  BG_DARK[1],  BG_DARK[2],  1.00f);
        ImGui.pushStyleColor(ImGuiCol.FrameBgHovered,    BG_LIGHT[0], BG_LIGHT[1], BG_LIGHT[2], 1.00f);
        ImGui.pushStyleColor(ImGuiCol.FrameBgActive,     ACCENT[0],   ACCENT[1],   ACCENT[2],   0.30f);
        ImGui.pushStyleColor(ImGuiCol.TitleBg,           TITLE_BG[0], TITLE_BG[1], TITLE_BG[2], 1.00f);
        ImGui.pushStyleColor(ImGuiCol.TitleBgActive,     BG_LIGHT[0], BG_LIGHT[1], BG_LIGHT[2], 1.00f);
        ImGui.pushStyleColor(ImGuiCol.MenuBarBg,         BG_DARK[0],  BG_DARK[1],  BG_DARK[2],  1.00f);
        ImGui.pushStyleColor(ImGuiCol.ScrollbarBg,       BG_DARK[0],  BG_DARK[1],  BG_DARK[2],  1.00f);
        ImGui.pushStyleColor(ImGuiCol.ScrollbarGrab,     BG_LIGHT[0], BG_LIGHT[1], BG_LIGHT[2], 1.00f);
        ImGui.pushStyleColor(ImGuiCol.ScrollbarGrabHovered, ACCENT[0], ACCENT[1],  ACCENT[2],   0.60f);
        ImGui.pushStyleColor(ImGuiCol.CheckMark,         ACCENT[0],   ACCENT[1],   ACCENT[2],   1.00f);
        ImGui.pushStyleColor(ImGuiCol.SliderGrab,        ACCENT[0],   ACCENT[1],   ACCENT[2],   1.00f);
        ImGui.pushStyleColor(ImGuiCol.SliderGrabActive,  ACCENT_ACTIVE[0], ACCENT_ACTIVE[1], ACCENT_ACTIVE[2], 1.00f);
        ImGui.pushStyleColor(ImGuiCol.Button,            BG_LIGHT[0], BG_LIGHT[1], BG_LIGHT[2], 1.00f);
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered,     ACCENT[0],   ACCENT[1],   ACCENT[2],   0.80f);
        ImGui.pushStyleColor(ImGuiCol.ButtonActive,      ACCENT_ACTIVE[0], ACCENT_ACTIVE[1], ACCENT_ACTIVE[2], 1.00f);
        ImGui.pushStyleColor(ImGuiCol.Header,            HEADER[0],   HEADER[1],   HEADER[2],   1.00f);
        ImGui.pushStyleColor(ImGuiCol.HeaderHovered,     ACCENT[0],   ACCENT[1],   ACCENT[2],   0.40f);
        ImGui.pushStyleColor(ImGuiCol.HeaderActive,      ACCENT[0],   ACCENT[1],   ACCENT[2],   0.70f);
        ImGui.pushStyleColor(ImGuiCol.Separator,         BORDER[0],   BORDER[1],   BORDER[2],   1.00f);
        ImGui.pushStyleColor(ImGuiCol.Tab,               TAB_INACTIVE[0], TAB_INACTIVE[1], TAB_INACTIVE[2], 1.00f);
        ImGui.pushStyleColor(ImGuiCol.TabHovered,        ACCENT[0],   ACCENT[1],   ACCENT[2],   0.50f);
        ImGui.pushStyleColor(ImGuiCol.TabActive,         TAB_ACTIVE[0], TAB_ACTIVE[1], TAB_ACTIVE[2], 1.00f);
        ImGui.pushStyleColor(ImGuiCol.TabUnfocused,      TAB_INACTIVE[0], TAB_INACTIVE[1], TAB_INACTIVE[2], 1.00f);
        ImGui.pushStyleColor(ImGuiCol.TabUnfocusedActive,BG_LIGHT[0], BG_LIGHT[1], BG_LIGHT[2], 1.00f);
        ImGui.pushStyleColor(ImGuiCol.DockingPreview,    ACCENT[0],   ACCENT[1],   ACCENT[2],   0.35f);
        ImGui.pushStyleColor(ImGuiCol.Text,              TEXT_PRIMARY[0], TEXT_PRIMARY[1], TEXT_PRIMARY[2], 1.00f);
        ImGui.pushStyleColor(ImGuiCol.TextDisabled,      TEXT_DIM[0], TEXT_DIM[1], TEXT_DIM[2], 1.00f);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /** Convert 0xRRGGBB hex to a float[3] {r, g, b} in [0,1] range */
    private static float[] hex(int rgb) {
        return new float[]{
                ((rgb >> 16) & 0xFF) / 255f,
                ((rgb >>  8) & 0xFF) / 255f,
                ( rgb        & 0xFF) / 255f
        };
    }
}