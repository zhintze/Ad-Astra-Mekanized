package com.hecookin.adastramekanized.common.commands;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.world.SpaceStationSpawner;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

/**
 * Debug command to manually trigger space station spawning
 */
public class SpawnStationCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("spawnstation")
                .requires(source -> source.hasPermission(2))
                .executes(context -> {
                    ServerLevel level = context.getSource().getLevel();

                    AdAstraMekanized.LOGGER.info("Manual spawn station command executed in dimension: {}",
                        level.dimension().location());

                    SpaceStationSpawner.trySpawnStation(level);

                    context.getSource().sendSuccess(
                        () -> Component.literal("Attempted to spawn space station in " + level.dimension().location()),
                        true
                    );

                    return 1;
                })
                .then(Commands.literal("reset")
                    .executes(context -> {
                        ServerLevel level = context.getSource().getLevel();

                        SpaceStationSpawner.resetSpawnTracker(level);

                        context.getSource().sendSuccess(
                            () -> Component.literal("Reset space station spawn tracker - it will spawn again on next entry"),
                            true
                        );

                        return 1;
                    })
                )
                .then(Commands.literal("status")
                    .executes(context -> {
                        ServerLevel level = context.getSource().getLevel();

                        com.hecookin.adastramekanized.common.world.SpaceStationBlockProtection protection =
                            com.hecookin.adastramekanized.common.world.SpaceStationBlockProtection.get(level);

                        int protectedCount = protection.getProtectedBlockCount();

                        context.getSource().sendSuccess(
                            () -> Component.literal("Protected blocks in this dimension: " + protectedCount),
                            false
                        );

                        return 1;
                    })
                )
                .then(Commands.literal("clearprotection")
                    .executes(context -> {
                        ServerLevel level = context.getSource().getLevel();

                        com.hecookin.adastramekanized.common.world.SpaceStationBlockProtection protection =
                            com.hecookin.adastramekanized.common.world.SpaceStationBlockProtection.get(level);

                        int clearedCount = protection.getProtectedBlockCount();
                        protection.clearAll();

                        context.getSource().sendSuccess(
                            () -> Component.literal("Cleared " + clearedCount + " protected blocks"),
                            true
                        );

                        return 1;
                    })
                )
        );
    }
}
