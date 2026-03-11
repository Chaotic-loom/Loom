package com.chaotic_loom.loom.core.imgui.editor;

import com.chaotic_loom.loom.core.imgui.DebugWindows;
import com.chaotic_loom.loom.core.imgui.editor.panels.*;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiDir;
import imgui.flag.ImGuiDockNodeFlags;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImInt;

/**
 * Axiom/Flashback-style editor layout.
 *
 * Layout is built once via dockBuilder on the first frame (or after Reset Layout).
 * After that ImGui's docking system owns the arrangement — nothing is forced
 * per-frame.
 *
 * The game renders through the central node because:
 *  - PassthruCentralNode tells the GL3 backend to leave that rect empty
 *  - ViewportPanel (docked there) uses NoBackground + NoScrollbar, so it draws
 *    nothing behind its content — only the toolbar + stats are visible
 */
public class EditorLayout {

    private static final HierarchyPanel    hierarchy = new HierarchyPanel();
    private static final InspectorPanel    inspector = new InspectorPanel();
    private static final ConsolePanel      console   = new ConsolePanel();
    private static final AssetBrowserPanel assets    = new AssetBrowserPanel();
    private static final ViewportPanel     viewport  = new ViewportPanel();

    private static boolean layoutBuilt = false;

    // ── Colour palette ────────────────────────────────────────────────────────
    private static final float[] BG_DARK        = hex(0x1A1A1F);
    private static final float[] BG_MID         = hex(0x23232A);
    private static final float[] BG_LIGHT       = hex(0x2C2C35);
    private static final float[] ACCENT         = hex(0x4A9EFF);
    private static final float[] ACCENT_ACTIVE  = hex(0x2A7EDF);
    private static final float[] TEXT_PRIMARY   = hex(0xE8E8F0);
    private static final float[] TEXT_DIM       = hex(0x888899);
    private static final float[] BORDER         = hex(0x3A3A48);
    private static final float[] TAB_ACTIVE     = hex(0x2C2C35);
    private static final float[] TAB_INACTIVE   = hex(0x1E1E25);
    private static final float[] TITLE_BG       = hex(0x1A1A1F);
    private static final float[] HEADER         = hex(0x35354A);

    // ── Public API ────────────────────────────────────────────────────────────

    public static void init() {
        applyEditorTheme();
    }

    public static void render() {
        pushFullscreenHostWindow();

        int dockspaceId = ImGui.getID("EditorDockSpace");

        if (!layoutBuilt) {
            buildLayout(dockspaceId);
            layoutBuilt = true;
        }

        // PassthruCentralNode: the GL3 renderer leaves the central rect empty,
        // so the game that was already drawn to the framebuffer shows through.
        ImGui.dockSpace(dockspaceId, 0f, 0f, ImGuiDockNodeFlags.PassthruCentralNode);

        popFullscreenHostWindow();

        renderMenuBar();
        hierarchy.render();
        inspector.render();
        console.render();
        assets.render();
        viewport.render();   // docked to the central node, NoBackground
    }

    public static void resetLayout() {
        layoutBuilt = false;
    }

    // ── DockBuilder ───────────────────────────────────────────────────────────
    //
    // imgui.internal.ImGui exposes the DockBuilder API absent from the public class.
    // This runs BEFORE dockSpace() so the node tree is ready on the first frame.

    private static void buildLayout(int dockspaceId) {
        imgui.ImGuiViewport vp = ImGui.getMainViewport();
        float W     = vp.getSizeX();
        float H     = vp.getSizeY();
        float ox    = vp.getPosX();
        float oy    = vp.getPosY();
        float menuH = ImGui.getFrameHeight();

        imgui.internal.ImGui.dockBuilderRemoveNode(dockspaceId);
        imgui.internal.ImGui.dockBuilderAddNode(dockspaceId, ImGuiDockNodeFlags.PassthruCentralNode);
        imgui.internal.ImGui.dockBuilderSetNodePos (dockspaceId, ox, oy + menuH);
        imgui.internal.ImGui.dockBuilderSetNodeSize(dockspaceId, W,  H - menuH);

        // Left: Hierarchy (20%)
        ImInt leftId   = new ImInt();
        ImInt centerId = new ImInt();
        imgui.internal.ImGui.dockBuilderSplitNode(dockspaceId, ImGuiDir.Left, 0.20f, leftId, centerId);

        // Right: Inspector (27% of remainder ≈ 21% of total)
        ImInt rightId = new ImInt();
        ImInt midId   = new ImInt();
        imgui.internal.ImGui.dockBuilderSplitNode(centerId.get(), ImGuiDir.Right, 0.27f, rightId, midId);

        // Bottom: Console + Assets (25% of mid height)
        ImInt bottomId    = new ImInt();
        ImInt viewportId  = new ImInt();
        imgui.internal.ImGui.dockBuilderSplitNode(midId.get(), ImGuiDir.Down, 0.25f, bottomId, viewportId);

        imgui.internal.ImGui.dockBuilderDockWindow(HierarchyPanel.TITLE,    leftId.get());
        imgui.internal.ImGui.dockBuilderDockWindow(InspectorPanel.TITLE,    rightId.get());
        imgui.internal.ImGui.dockBuilderDockWindow(ConsolePanel.TITLE,      bottomId.get());
        imgui.internal.ImGui.dockBuilderDockWindow(AssetBrowserPanel.TITLE, bottomId.get()); // tabbed alongside Console
        imgui.internal.ImGui.dockBuilderDockWindow(ViewportPanel.TITLE,     viewportId.get());

        imgui.internal.ImGui.dockBuilderFinish(dockspaceId);
    }

    // ── Menu bar ──────────────────────────────────────────────────────────────

    private static void renderMenuBar() {
        if (!ImGui.beginMainMenuBar()) return;

        if (ImGui.beginMenu("File")) {
            if (ImGui.menuItem("New Project",   "Ctrl+N"))       { /* TODO */ }
            if (ImGui.menuItem("Open Project…", "Ctrl+O"))       { /* TODO */ }
            ImGui.separator();
            if (ImGui.menuItem("Save",          "Ctrl+S"))       { /* TODO */ }
            if (ImGui.menuItem("Save As…",      "Ctrl+Shift+S")) { /* TODO */ }
            ImGui.separator();
            if (ImGui.menuItem("Exit")) net.minecraft.client.Minecraft.getInstance().stop();
            ImGui.endMenu();
        }

        if (ImGui.beginMenu("Edit")) {
            if (ImGui.menuItem("Undo", "Ctrl+Z")) { /* TODO */ }
            if (ImGui.menuItem("Redo", "Ctrl+Y")) { /* TODO */ }
            ImGui.separator();
            if (ImGui.menuItem("Preferences…")) { /* TODO */ }
            ImGui.endMenu();
        }

        if (ImGui.beginMenu("View")) {
            if (ImGui.menuItem("Hierarchy",    null, hierarchy.isVisible())) hierarchy.toggleVisibility();
            if (ImGui.menuItem("Inspector",    null, inspector.isVisible())) inspector.toggleVisibility();
            if (ImGui.menuItem("Console",      null, console.isVisible()))   console.toggleVisibility();
            if (ImGui.menuItem("Asset Browser",null, assets.isVisible()))    assets.toggleVisibility();
            ImGui.separator();
            if (ImGui.menuItem("Reset Layout")) resetLayout();
            ImGui.endMenu();
        }

        if (ImGui.beginMenu("Tools")) {
            if (ImGui.menuItem("World Settings"))    { /* TODO */ }
            if (ImGui.menuItem("Lighting Override")) { /* TODO */ }
            if (ImGui.menuItem("Entity Spawner"))    { /* TODO */ }
            ImGui.separator();
            if (ImGui.menuItem("ImGui Demo")) DebugWindows.toggleVisibility();
            ImGui.endMenu();
        }

        int fps = net.minecraft.client.Minecraft.getInstance().getFps();
        String fpsStr = fps + " FPS";
        ImGui.setCursorPosX(ImGui.getContentRegionAvailX() - ImGui.calcTextSize(fpsStr).x - 8f);
        float[] fc = fps >= 60 ? hex(0x4AFF88) : fps >= 30 ? hex(0xFFCC44) : hex(0xFF5555);
        ImGui.textColored(fc[0], fc[1], fc[2], 1f, fpsStr);

        ImGui.endMainMenuBar();
    }

    // ── Fullscreen host window ────────────────────────────────────────────────

    private static void pushFullscreenHostWindow() {
        imgui.ImGuiViewport vp = ImGui.getMainViewport();
        ImGui.setNextWindowPos (vp.getPosX(),  vp.getPosY(),  ImGuiCond.Always);
        ImGui.setNextWindowSize(vp.getSizeX(), vp.getSizeY(), ImGuiCond.Always);
        ImGui.setNextWindowViewport(vp.getID());

        int flags =
                ImGuiWindowFlags.NoDocking            |
                        ImGuiWindowFlags.NoTitleBar            |
                        ImGuiWindowFlags.NoCollapse            |
                        ImGuiWindowFlags.NoResize              |
                        ImGuiWindowFlags.NoMove                |
                        ImGuiWindowFlags.NoBringToFrontOnFocus |
                        ImGuiWindowFlags.NoBackground          |
                        ImGuiWindowFlags.MenuBar;

        ImGui.pushStyleVar(ImGuiStyleVar.WindowRounding,   0f);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowBorderSize, 0f);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding,    0f, 0f);
        ImGui.begin("##EditorHost", flags);
        ImGui.popStyleVar(3);
    }

    private static void popFullscreenHostWindow() {
        ImGui.end();
    }

    // ── Theme ─────────────────────────────────────────────────────────────────

    public static void applyEditorTheme() {
        imgui.ImGuiStyle s = ImGui.getStyle();
        s.setWindowRounding(4f);       s.setChildRounding(4f);      s.setFrameRounding(3f);
        s.setPopupRounding(4f);        s.setScrollbarRounding(3f);  s.setGrabRounding(3f);
        s.setTabRounding(4f);          s.setWindowBorderSize(1f);   s.setFrameBorderSize(0f);
        s.setPopupBorderSize(1f);      s.setFramePadding(8f, 4f);   s.setItemSpacing(8f, 4f);
        s.setItemInnerSpacing(6f, 4f); s.setIndentSpacing(16f);     s.setScrollbarSize(12f);
        s.setGrabMinSize(8f);          s.setWindowMinSize(100f, 60f);

        ImGui.pushStyleColor(ImGuiCol.WindowBg,            BG_MID[0],       BG_MID[1],       BG_MID[2],       1.00f);
        ImGui.pushStyleColor(ImGuiCol.ChildBg,             BG_DARK[0],      BG_DARK[1],      BG_DARK[2],      1.00f);
        ImGui.pushStyleColor(ImGuiCol.PopupBg,             BG_MID[0],       BG_MID[1],       BG_MID[2],       0.96f);
        ImGui.pushStyleColor(ImGuiCol.Border,              BORDER[0],       BORDER[1],       BORDER[2],       1.00f);
        ImGui.pushStyleColor(ImGuiCol.FrameBg,             BG_DARK[0],      BG_DARK[1],      BG_DARK[2],      1.00f);
        ImGui.pushStyleColor(ImGuiCol.FrameBgHovered,      BG_LIGHT[0],     BG_LIGHT[1],     BG_LIGHT[2],     1.00f);
        ImGui.pushStyleColor(ImGuiCol.FrameBgActive,       ACCENT[0],       ACCENT[1],       ACCENT[2],       0.30f);
        ImGui.pushStyleColor(ImGuiCol.TitleBg,             TITLE_BG[0],     TITLE_BG[1],     TITLE_BG[2],     1.00f);
        ImGui.pushStyleColor(ImGuiCol.TitleBgActive,       BG_LIGHT[0],     BG_LIGHT[1],     BG_LIGHT[2],     1.00f);
        ImGui.pushStyleColor(ImGuiCol.MenuBarBg,           BG_DARK[0],      BG_DARK[1],      BG_DARK[2],      1.00f);
        ImGui.pushStyleColor(ImGuiCol.ScrollbarBg,         BG_DARK[0],      BG_DARK[1],      BG_DARK[2],      1.00f);
        ImGui.pushStyleColor(ImGuiCol.ScrollbarGrab,       BG_LIGHT[0],     BG_LIGHT[1],     BG_LIGHT[2],     1.00f);
        ImGui.pushStyleColor(ImGuiCol.ScrollbarGrabHovered,ACCENT[0],       ACCENT[1],       ACCENT[2],       0.60f);
        ImGui.pushStyleColor(ImGuiCol.CheckMark,           ACCENT[0],       ACCENT[1],       ACCENT[2],       1.00f);
        ImGui.pushStyleColor(ImGuiCol.SliderGrab,          ACCENT[0],       ACCENT[1],       ACCENT[2],       1.00f);
        ImGui.pushStyleColor(ImGuiCol.SliderGrabActive,    ACCENT_ACTIVE[0],ACCENT_ACTIVE[1],ACCENT_ACTIVE[2],1.00f);
        ImGui.pushStyleColor(ImGuiCol.Button,              BG_LIGHT[0],     BG_LIGHT[1],     BG_LIGHT[2],     1.00f);
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered,       ACCENT[0],       ACCENT[1],       ACCENT[2],       0.80f);
        ImGui.pushStyleColor(ImGuiCol.ButtonActive,        ACCENT_ACTIVE[0],ACCENT_ACTIVE[1],ACCENT_ACTIVE[2],1.00f);
        ImGui.pushStyleColor(ImGuiCol.Header,              HEADER[0],       HEADER[1],       HEADER[2],       1.00f);
        ImGui.pushStyleColor(ImGuiCol.HeaderHovered,       ACCENT[0],       ACCENT[1],       ACCENT[2],       0.40f);
        ImGui.pushStyleColor(ImGuiCol.HeaderActive,        ACCENT[0],       ACCENT[1],       ACCENT[2],       0.70f);
        ImGui.pushStyleColor(ImGuiCol.Separator,           BORDER[0],       BORDER[1],       BORDER[2],       1.00f);
        ImGui.pushStyleColor(ImGuiCol.Tab,                 TAB_INACTIVE[0], TAB_INACTIVE[1], TAB_INACTIVE[2], 1.00f);
        ImGui.pushStyleColor(ImGuiCol.TabHovered,          ACCENT[0],       ACCENT[1],       ACCENT[2],       0.50f);
        ImGui.pushStyleColor(ImGuiCol.TabActive,           TAB_ACTIVE[0],   TAB_ACTIVE[1],   TAB_ACTIVE[2],   1.00f);
        ImGui.pushStyleColor(ImGuiCol.TabUnfocused,        TAB_INACTIVE[0], TAB_INACTIVE[1], TAB_INACTIVE[2], 1.00f);
        ImGui.pushStyleColor(ImGuiCol.TabUnfocusedActive,  BG_LIGHT[0],     BG_LIGHT[1],     BG_LIGHT[2],     1.00f);
        ImGui.pushStyleColor(ImGuiCol.DockingPreview,      ACCENT[0],       ACCENT[1],       ACCENT[2],       0.35f);
        ImGui.pushStyleColor(ImGuiCol.Text,                TEXT_PRIMARY[0], TEXT_PRIMARY[1], TEXT_PRIMARY[2], 1.00f);
        ImGui.pushStyleColor(ImGuiCol.TextDisabled,        TEXT_DIM[0],     TEXT_DIM[1],     TEXT_DIM[2],     1.00f);
    }

    static float[] hex(int rgb) {
        return new float[]{
                ((rgb >> 16) & 0xFF) / 255f,
                ((rgb >>  8) & 0xFF) / 255f,
                ( rgb        & 0xFF) / 255f
        };
    }
}