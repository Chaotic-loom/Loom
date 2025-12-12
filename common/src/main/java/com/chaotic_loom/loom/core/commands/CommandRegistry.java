package com.chaotic_loom.loom.core.commands;

import com.chaotic_loom.loom.Constants;
import com.chaotic_loom.loom.builtin.commands.HelpFunction;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CommandRegistry {
    private static final List<LiteralArgumentBuilder<CommandSourceStack>> REGULAR_COMMANDS = new ArrayList<>();

    // Registry for "Function" style commands (Root -> Subfunctions)
    // Key: The root command name (e.g., "loom")
    // Value: List of subfunctions for that root
    private static final Map<String, List<CommandFunction>> ROOT_REGISTRIES = new LinkedHashMap<>();

    /**
     * Registers a standalone vanilla-style command.
     * Example: registerVanilla(Commands.literal("heal").executes(...));
     */
    public static void register(LiteralArgumentBuilder<CommandSourceStack> commandBuilder) {
        REGULAR_COMMANDS.add(commandBuilder);
    }

    /**
     * Registers a subfunction to a specific root command.
     * If the root doesn't exist yet, it is created automatically.
     * * @param rootCommand The main command name (e.g., "loom")
     * @param function The subfunction to add (e.g., PrintFunction)
     */
    public static void registerFunction(String rootCommand, CommandFunction function) {
        ROOT_REGISTRIES.computeIfAbsent(rootCommand, k -> new ArrayList<>()).add(function);
    }

    // This gets called by commands.CommandsMixin
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // Registers custom commands
        CommandRegistry.registerFunction(Constants.MOD_ID, new HelpFunction());

        // Register all Vanilla commands
        for (LiteralArgumentBuilder<CommandSourceStack> cmd : REGULAR_COMMANDS) {
            dispatcher.register(cmd);
        }

        // Register all Root/Function commands
        for (Map.Entry<String, List<CommandFunction>> entry : ROOT_REGISTRIES.entrySet()) {
            String rootName = entry.getKey();
            List<CommandFunction> functions = entry.getValue();

            // Create the root literal
            LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(rootName);

            // Append all subfunctions to this root
            for (CommandFunction func : functions) {
                root.then(func.build());
            }

            dispatcher.register(root);
        }
    }
}