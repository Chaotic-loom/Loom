package com.chaotic_loom.loom.builtin.commands;

import com.chaotic_loom.loom.core.commands.CommandFunction;
import com.chaotic_loom.loom.platform.Services;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class HelpFunction implements CommandFunction {
    @Override
    public String getID() {
        return "help";
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal(getID())
                .then(Commands.literal("version")
                        .executes(ctx -> {
                            ctx.getSource().sendSuccess(() -> Component.literal("Loom version: " + Services.PLATFORM.getModVersion()), false);
                            return 1;
                        })
                );
    }
}