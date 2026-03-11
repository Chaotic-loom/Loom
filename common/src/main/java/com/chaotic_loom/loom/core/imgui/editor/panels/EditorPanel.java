package com.chaotic_loom.loom.core.imgui.editor.panels;

import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;

/**
 * Base class for all dockable editor panels.
 * Subclasses implement {@link #renderContent()} and declare a static TITLE.
 */
public abstract class EditorPanel {

    private final ImBoolean visible = new ImBoolean(true);
    private final int       defaultFlags;

    // Seed geometry: applied with FirstUseEver on the next render() call.
    private boolean hasSeed   = false;
    private boolean seedDirty = false;   // true after resetSeed() forces re-application
    private float   seedX, seedY, seedW, seedH;

    protected EditorPanel() {
        this(0);
    }

    protected EditorPanel(int extraFlags) {
        this.defaultFlags = extraFlags;
    }

    // ── Called by EditorLayout ────────────────────────────────────────────

    /**
     * Store the desired initial position/size.
     * ImGui will only apply it when there is no saved state for this window
     * ({@link ImGuiCond#FirstUseEver}).
     */
    public void seedNextFrame(float x, float y, float w, float h) {
        seedX = x; seedY = y; seedW = w; seedH = h;
        hasSeed = true;
    }

    /**
     * Force the seed to be re-applied next frame (used by "Reset Layout").
     * We switch to {@link ImGuiCond#Always} for one frame so the window
     * actually moves even if ImGui already has a saved position for it.
     */
    public void resetSeed() {
        seedDirty = true;
    }

    /** Called once per frame from EditorLayout – do not override. */
    public final void render() {
        if (!visible.get()) return;

        if (hasSeed) {
            int cond = seedDirty ? ImGuiCond.Always : ImGuiCond.FirstUseEver;
            ImGui.setNextWindowPos (seedX, seedY, cond);
            ImGui.setNextWindowSize(seedW, seedH, cond);
            seedDirty = false;
        }

        if (ImGui.begin(getTitle(), visible, defaultFlags)) {
            renderContent();
        }
        ImGui.end();
    }

    /** The unique window title / dock ID. Must match the string passed to EditorLayout. */
    public abstract String getTitle();

    /** Implement your panel UI here. Called inside ImGui.begin / ImGui.end. */
    protected abstract void renderContent();

    public boolean isVisible()           { return visible.get(); }
    public void    setVisible(boolean v) { visible.set(v); }
    public void    toggleVisibility()    { visible.set(!visible.get()); }
}