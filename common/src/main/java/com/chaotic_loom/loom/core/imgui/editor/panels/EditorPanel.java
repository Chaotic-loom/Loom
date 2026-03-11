package com.chaotic_loom.loom.core.imgui.editor.panels;

import imgui.ImGui;
import imgui.type.ImBoolean;

/**
 * Base class for all dockable editor panels.
 *
 * Placement is handled entirely by EditorLayout.buildLayout() via the
 * DockBuilder API — this class does nothing to force position or size.
 *
 * Subclasses must:
 *   - Declare {@code public static final String TITLE} matching the string
 *     passed to {@code dockBuilderDockWindow}.
 *   - Implement {@link #renderContent()}.
 */
public abstract class EditorPanel {

    private final ImBoolean visible    = new ImBoolean(true);
    private final int       extraFlags;

    protected EditorPanel() {
        this(0);
    }

    protected EditorPanel(int extraFlags) {
        this.extraFlags = extraFlags;
    }

    public void render() {
        if (!visible.get()) return;
        if (ImGui.begin(getTitle(), visible, extraFlags)) {
            renderContent();
        }
        ImGui.end();
    }

    public abstract String getTitle();

    protected abstract void renderContent();

    public boolean isVisible()           { return visible.get(); }
    public void    setVisible(boolean v) { visible.set(v); }
    public void    toggleVisibility()    { visible.set(!visible.get()); }
}