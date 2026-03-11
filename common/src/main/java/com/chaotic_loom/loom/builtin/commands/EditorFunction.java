package com.chaotic_loom.loom.builtin.commands;

import com.chaotic_loom.loom.builtin.shaders.BuiltinTestShader;
import com.chaotic_loom.loom.core.commands.CommandFunction;
import com.chaotic_loom.loom.core.imgui.ImGuiManager;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class EditorFunction implements CommandFunction {
    @Override
    public String getID() {
        return "editor";
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal(getID())
                .then(Commands.literal("enable")
                        .executes(ctx -> {
                            ImGuiManager.setEditorMode(true);
                            return 1;
                        })
                )
                .then(Commands.literal("disable")
                        .executes(ctx -> {
                            ImGuiManager.setEditorMode(false);
                            return 1;
                        })
                );
    }
}
