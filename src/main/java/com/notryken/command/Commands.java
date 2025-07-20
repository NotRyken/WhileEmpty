package com.notryken.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.notryken.WhileEmpty;
import com.notryken.config.Config;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;

import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;
import static com.notryken.config.Config.options;

@SuppressWarnings("unchecked")
public class Commands<S> extends CommandDispatcher<S> {

    public void register(
            CommandDispatcher<S> dispatcher,
            CommandRegistryAccess access,
            CommandManager.RegistrationEnvironment environment
    ) {
        dispatcher.register((LiteralArgumentBuilder<S>) literal(WhileEmpty.MOD_ID)
                .requires((source) -> ((ServerCommandSource) source).hasPermissionLevel(2))
                .then(literal("enable")
                        .executes(ctx -> {
                            MutableText msg = WhileEmpty.PREFIX.copy();
                            if (options().enabled) {
                                msg.append("Already enabled!");
                            } else {
                                options().enabled = true;
                                Config.save();
                                msg.append("Enabled!");
                            }
                            ((ServerCommandSource) ctx.getSource()).sendMessage(msg);
                            return Command.SINGLE_SUCCESS;
                        })
                )
                .then(literal("disable")
                        .executes(ctx -> {
                            MutableText msg = WhileEmpty.PREFIX.copy();
                            if (!options().enabled) {
                                msg.append("Already disabled!");
                            } else {
                                options().enabled = false;
                                Config.save();
                                msg.append("Disabled!");
                            }
                            ((ServerCommandSource) ctx.getSource()).sendMessage(msg);
                            return Command.SINGLE_SUCCESS;
                        })
                )
                .then(literal("reload")
                        .executes(ctx -> {
                            MutableText msg = WhileEmpty.PREFIX.copy();
                            Config.reloadAndSave();
                            msg.append("Config reloaded!");
                            ((ServerCommandSource) ctx.getSource()).sendMessage(msg);
                            return Command.SINGLE_SUCCESS;
                        })
                )
                .then(literal("reset")
                        .executes(ctx -> {
                            MutableText msg = WhileEmpty.PREFIX.copy();
                            Config.resetAndSave();
                            msg.append("Config reset!");
                            ((ServerCommandSource) ctx.getSource()).sendMessage(msg);
                            return Command.SINGLE_SUCCESS;
                        })
                )
        );
    }
}
