package com.chaotic_loom.loom.core.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;

public interface CommandFunction {
    String getID();
    LiteralArgumentBuilder<CommandSourceStack> build();
}
