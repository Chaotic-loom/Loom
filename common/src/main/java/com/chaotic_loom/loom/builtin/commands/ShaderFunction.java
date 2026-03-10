package com.chaotic_loom.loom.builtin.commands;

import com.chaotic_loom.loom.builtin.shaders.BuiltinTestShader;
import com.chaotic_loom.loom.core.commands.CommandFunction;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class ShaderFunction implements CommandFunction {
    @Override
    public String getID() {
        return "shader";
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal(getID())
                .then(Commands.literal("enable")
                        .executes(ctx -> {
                            BuiltinTestShader.enable();
                            ctx.getSource().sendSuccess(() -> Component.literal("Test shader enabled."), false);
                            return 1;
                        })
                )
                .then(Commands.literal("disable")
                        .executes(ctx -> {
                            BuiltinTestShader.disable();
                            ctx.getSource().sendSuccess(() -> Component.literal("Test shader disabled."), false);
                            return 1;
                        })
                )
                .then(Commands.literal("tint")
                        .then(Commands.argument("r", FloatArgumentType.floatArg(0, 1))
                                .then(Commands.argument("g", FloatArgumentType.floatArg(0, 1))
                                        .then(Commands.argument("b", FloatArgumentType.floatArg(0, 1))
                                                .then(Commands.argument("a", FloatArgumentType.floatArg(0, 1))
                                                        .executes(ctx -> {
                                                            float r = FloatArgumentType.getFloat(ctx, "r");
                                                            float g = FloatArgumentType.getFloat(ctx, "g");
                                                            float b = FloatArgumentType.getFloat(ctx, "b");
                                                            float a = FloatArgumentType.getFloat(ctx, "a");
                                                            BuiltinTestShader.setTint(r, g, b, a);
                                                            ctx.getSource().sendSuccess(() -> Component.literal("Shader tint set to: " + r + " " + g + " " + b + " " + a), false);
                                                            return 1;
                                                        })
                                                )
                                        )
                                )
                        )
                )
                .then(Commands.literal("intensity")
                        .then(Commands.argument("value", FloatArgumentType.floatArg(0, 1))
                                .executes(ctx -> {
                                    float value = FloatArgumentType.getFloat(ctx, "value");
                                    BuiltinTestShader.setIntensity(value);
                                    ctx.getSource().sendSuccess(() -> Component.literal("Shader intensity set to: " + value), false);
                                    return 1;
                                })
                        )
                );
    }
}
