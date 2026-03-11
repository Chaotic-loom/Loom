package com.chaotic_loom.loom.core.imgui.editor.panels;

import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImString;
import net.minecraft.client.Minecraft;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Console panel – bottom tab.
 *
 * Usage:
 *   ConsolePanel.log(Level.INFO,  "Server started");
 *   ConsolePanel.log(Level.WARN,  "High memory usage: 4 GB");
 *   ConsolePanel.log(Level.ERROR, "NullPointerException at ...");
 */
public class ConsolePanel extends EditorPanel {

    public static final String TITLE = "  Console";

    // ── Log storage ───────────────────────────────────────────────────────

    public enum Level { DEBUG, INFO, WARN, ERROR }

    private record LogEntry(Level level, String message, String timestamp) {}

    private static final int MAX_ENTRIES = 2000;
    private static final Deque<LogEntry> entries = new ConcurrentLinkedDeque<>();

    // ── Panel state ───────────────────────────────────────────────────────
    private final ImString commandInput = new ImString(256);
    private boolean autoScroll   = true;
    private boolean filterDebug  = true;
    private boolean filterInfo   = true;
    private boolean filterWarn   = true;
    private boolean filterError  = true;
    private boolean scrollToBottom = false;

    // ── Public API ────────────────────────────────────────────────────────

    public static void log(Level level, String message) {
        String time = java.time.LocalTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
        entries.addLast(new LogEntry(level, message, time));
        while (entries.size() > MAX_ENTRIES) entries.pollFirst();
    }

    public static void info(String msg)  { log(Level.INFO,  msg); }
    public static void warn(String msg)  { log(Level.WARN,  msg); }
    public static void error(String msg) { log(Level.ERROR, msg); }
    public static void debug(String msg) { log(Level.DEBUG, msg); }

    // ── Rendering ─────────────────────────────────────────────────────────

    @Override public String getTitle() { return TITLE; }

    @Override
    protected void renderContent() {
        renderToolbar();
        ImGui.separator();
        renderLogArea();
        renderCommandInput();
    }

    private void renderToolbar() {
        // Filter toggles
        filterDebug = renderFilterToggle("DEBUG", filterDebug, 0.55f, 0.55f, 0.55f);
        ImGui.sameLine(0, 4);
        filterInfo  = renderFilterToggle("INFO",  filterInfo,  0.29f, 0.72f, 1.00f);
        ImGui.sameLine(0, 4);
        filterWarn  = renderFilterToggle("WARN",  filterWarn,  1.00f, 0.80f, 0.20f);
        ImGui.sameLine(0, 4);
        filterError = renderFilterToggle("ERROR", filterError, 1.00f, 0.35f, 0.35f);

        ImGui.sameLine(0, 16);
        ImGui.separator();
        ImGui.sameLine(0, 16);

        if (ImGui.button("Clear")) entries.clear();

        ImGui.sameLine(0, 8);
        ImGui.checkbox("Auto-scroll", new imgui.type.ImBoolean(autoScroll));

        ImGui.sameLine();
        ImGui.textDisabled(String.format("  %d entries", entries.size()));
    }

    private boolean renderFilterToggle(String label, boolean state,
                                       float r, float g, float b) {
        if (state) ImGui.pushStyleColor(ImGuiCol.Button,        r, g, b, 0.30f);
        else       ImGui.pushStyleColor(ImGuiCol.Button,        0.13f, 0.13f, 0.16f, 1f);
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, r, g, b, 0.50f);
        ImGui.pushStyleColor(ImGuiCol.Text, r, g, b, state ? 1f : 0.45f);

        if (ImGui.button(label)) state = !state;

        ImGui.popStyleColor(3);
        return state;
    }

    private void renderLogArea() {
        float footerH = ImGui.getFrameHeightWithSpacing() + 4f;
        ImGui.pushStyleColor(ImGuiCol.ChildBg, 0.09f, 0.09f, 0.12f, 1f);
        ImGui.beginChild("##console_log", 0, -footerH, false,
                ImGuiWindowFlags.HorizontalScrollbar);

        for (LogEntry e : entries) {
            if (!shouldShow(e.level())) continue;

            float[] col = levelColor(e.level());
            // Timestamp (dim)
            ImGui.textDisabled(e.timestamp());
            ImGui.sameLine();
            // Level badge
            ImGui.textColored(col[0], col[1], col[2], 1f,
                    "[" + e.level().name() + "]");
            ImGui.sameLine();
            // Message
            ImGui.textUnformatted(e.message());
        }

        if (autoScroll && scrollToBottom) {
            ImGui.setScrollHereY(1.0f);
            scrollToBottom = false;
        }
        if (autoScroll && ImGui.getScrollY() >= ImGui.getScrollMaxY()) {
            ImGui.setScrollHereY(1.0f);
        }

        ImGui.endChild();
        ImGui.popStyleColor();
    }

    private void renderCommandInput() {
        ImGui.separator();
        ImGui.pushStyleColor(ImGuiCol.FrameBg, 0.10f, 0.10f, 0.13f, 1f);
        ImGui.setNextItemWidth(-80f);
        boolean enter = ImGui.inputText("##cmd", commandInput,
                ImGuiInputTextFlags.EnterReturnsTrue);
        ImGui.popStyleColor();
        ImGui.sameLine();
        if (ImGui.button("Run") || enter) {
            executeCommand(commandInput.get().trim());
            commandInput.set("");
            ImGui.setKeyboardFocusHere(-1);
        }
    }

    private void executeCommand(String cmd) {
        if (cmd.isEmpty()) return;
        log(Level.INFO, "> " + cmd);
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            if (cmd.startsWith("/")) mc.player.connection.sendCommand(cmd.substring(1));
            else                    mc.player.connection.sendCommand(cmd);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private boolean shouldShow(Level l) {
        return switch (l) {
            case DEBUG -> filterDebug;
            case INFO  -> filterInfo;
            case WARN  -> filterWarn;
            case ERROR -> filterError;
        };
    }

    private float[] levelColor(Level l) {
        return switch (l) {
            case DEBUG -> new float[]{ 0.55f, 0.55f, 0.55f };
            case INFO  -> new float[]{ 0.29f, 0.72f, 1.00f };
            case WARN  -> new float[]{ 1.00f, 0.80f, 0.20f };
            case ERROR -> new float[]{ 1.00f, 0.35f, 0.35f };
        };
    }
}