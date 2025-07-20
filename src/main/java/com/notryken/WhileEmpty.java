package com.notryken;

import com.notryken.command.Commands;
import com.notryken.config.Config;
import com.notryken.config.DelayedMessage;
import com.notryken.util.ModLogger;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

import static com.notryken.config.Config.options;

public class WhileEmpty implements ModInitializer {

    public static final String MOD_ID = "whileempty";
    public static final String MOD_NAME = "WhileEmpty";
    public static final ModLogger LOG = new ModLogger(MOD_NAME);
    public static final Text PREFIX = Text.empty().formatted(Formatting.GRAY)
            .append(Text.literal("[").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("While").formatted(Formatting.AQUA))
            .append(Text.literal("Empty").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("] ").formatted(Formatting.DARK_GRAY));

    public static final List<DelayedMessage> tickingMessages = new ArrayList<>();

    @Override
    public void onInitialize() {
        Config.getAndSave();

        CommandRegistrationCallback.EVENT.register((source, access, environment) -> {
            new Commands<ServerCommandSource>().register(source, access, environment);
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            if (!options().enabled)
                return;
            if (server.getCurrentPlayerCount() == options().emptyThreshold + 1) {
                LOG.info(
                        "Last player left, triggering {} message(s)",
                        options().onLastPlayerLeave.size()
                );
                tickingMessages.clear();
                tickingMessages.addAll(options().onLastPlayerLeave);
            }
        });

        ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
            if (!options().enabled)
                return;
            if (options().runLastPlayerLeaveOnStart) {
                LOG.info(
                        "Server started, triggering {} message(s)",
                        options().onLastPlayerLeave.size()
                );
                tickingMessages.clear();
                tickingMessages.addAll(options().onLastPlayerLeave);
            }
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (!options().enabled)
                return;
            if (server.getCurrentPlayerCount() == options().emptyThreshold) {
                LOG.info(
                        "First player joined, triggering {} message(s)",
                        options().onFirstPlayerJoin.size()
                );
                tickingMessages.clear();
                tickingMessages.addAll(options().onFirstPlayerJoin);
            }
        });

        ServerTickEvents.END_SERVER_TICK.register((server) -> {
            tickingMessages.removeIf((dm) -> {
                if (dm.tick()) {
                    send(server, dm.message());
                    return true;
                }
                return false;
            });
        });
    }

    public static void onConfigSaved(Config config) {
        tickingMessages.clear();
    }

    private static void send(MinecraftServer server, String message) {
        if (message.startsWith("/")) {
            runCommand(server, message);
        } else {
            sendMessage(server, message);
        }
    }

    private static void runCommand(MinecraftServer server, String command) {
        if (server != null && command != null && !command.isBlank()) {
            server.getCommandManager()
                    .executeWithPrefix(server.getCommandSource().withLevel(4), command);
        }
    }

    private static void sendMessage(MinecraftServer server, String message) {
        server.getPlayerManager().broadcast(Text.literal(message), false);
    }
}
