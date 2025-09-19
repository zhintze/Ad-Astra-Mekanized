package com.hecookin.adastramekanized.common.commands;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.api.planets.Planet;
import com.hecookin.adastramekanized.api.planets.PlanetRegistry;
import com.hecookin.adastramekanized.common.dimensions.DynamicDimensionManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

/**
 * Debug command for testing planetary dimension travel.
 *
 * Usage: /planet_travel <player> <dimension>
 * Example: /planet_travel @s moon
 * Example: /planet_travel @s mars
 */
public class DimensionTravelCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("planet_travel")
            .requires(source -> source.hasPermission(2)) // OP level 2
            .then(Commands.argument("player", EntityArgument.player())
                .then(Commands.argument("dimension", StringArgumentType.string())
                    .suggests((context, builder) -> {
                        builder.suggest("moon");
                        builder.suggest("mars");
                        return builder.buildFuture();
                    })
                    .executes(context -> {
                        ServerPlayer player = EntityArgument.getPlayer(context, "player");
                        String dimensionName = StringArgumentType.getString(context, "dimension");
                        return teleportToDimension(context.getSource(), player, dimensionName);
                    })
                )
            )
        );
    }

    private static int teleportToDimension(CommandSourceStack source, ServerPlayer player, String dimensionName) {
        try {
            // Create the dimension resource location using exact names from our datapack
            ResourceLocation dimensionLocation = ResourceLocation.fromNamespaceAndPath(
                AdAstraMekanized.MOD_ID, dimensionName
            );

            // Get the dimension key
            ResourceKey<Level> dimensionKey = ResourceKey.create(Registries.DIMENSION, dimensionLocation);

            AdAstraMekanized.LOGGER.info("Attempting to teleport to dimension: {}", dimensionKey.location());

            // Check if planet exists first
            ResourceLocation planetId = ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, dimensionName);
            Planet planet = PlanetRegistry.getInstance().getPlanet(planetId);

            if (planet == null) {
                source.sendFailure(Component.literal("Planet '" + dimensionName + "' not found in registry!"));
                source.sendFailure(Component.literal("Available planets: moon, mars"));
                return 0;
            }

            // Get or create the server level
            ServerLevel targetLevel = getOrCreateDimension(source.getServer(), dimensionKey, dimensionName);

            if (targetLevel == null) {
                source.sendFailure(Component.literal("Failed to create or access dimension '" + dimensionName + "'!"));
                source.sendFailure(Component.literal("Dimension key: " + dimensionKey.location()));
                return 0;
            }

            // Check if we're using the overworld as fallback
            boolean isOverworldFallback = targetLevel.dimension().equals(Level.OVERWORLD);

            // Find a safe spawn location (surface level)
            BlockPos spawnPos = findSafeSpawnLocation(targetLevel);

            // Teleport the player
            player.teleportTo(targetLevel, spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5,
                             player.getYRot(), player.getXRot());

            if (isOverworldFallback) {
                source.sendSuccess(() -> Component.literal(
                    "Planet travel demo: " + player.getName().getString() + " attempted travel to " + planet.displayName() +
                    " (using Overworld as simulation) at " + spawnPos.getX() + ", " + spawnPos.getY() + ", " + spawnPos.getZ()
                ), true);

                player.sendSystemMessage(Component.literal(
                    "§6Planet Travel Demo: §fSimulating travel to " + planet.displayName() +
                    " (Gravity: " + planet.properties().gravity() + "x, Temp: " + planet.properties().temperature() + "°C)"
                ));

                player.sendSystemMessage(Component.literal(
                    "§7Note: Actual planetary dimensions will be implemented in Phase 3.2!"
                ));
            } else {
                source.sendSuccess(() -> Component.literal(
                    "Teleported " + player.getName().getString() + " to " + planet.displayName() +
                    " at " + spawnPos.getX() + ", " + spawnPos.getY() + ", " + spawnPos.getZ()
                ), true);

                player.sendSystemMessage(Component.literal(
                    "Welcome to " + planet.displayName() + "! You are now on another world."
                ));
            }

            AdAstraMekanized.LOGGER.info("Successfully teleported {} to dimension {}",
                player.getName().getString(), dimensionKey.location());

            return 1;

        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Failed to teleport to dimension {}: {}", dimensionName, e.getMessage(), e);
            source.sendFailure(Component.literal("Failed to teleport: " + e.getMessage()));
            return 0;
        }
    }

    /**
     * Get existing dimension or create it dynamically if it doesn't exist
     */
    private static ServerLevel getOrCreateDimension(MinecraftServer server, ResourceKey<Level> dimensionKey, String dimensionName) {
        // First, try to get existing dimension
        ServerLevel existingLevel = server.getLevel(dimensionKey);
        if (existingLevel != null) {
            AdAstraMekanized.LOGGER.info("Using existing dimension: {}", dimensionKey.location());
            return existingLevel;
        }

        // Log available dimensions for debugging
        var allDimensions = server.levelKeys();
        AdAstraMekanized.LOGGER.info("Dimension {} not found. Available dimensions:", dimensionKey.location());
        for (var dim : allDimensions) {
            AdAstraMekanized.LOGGER.info("  - {}", dim.location());
        }

        // Check if planet exists in registry
        ResourceLocation planetId = ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, dimensionName);
        Planet planet = PlanetRegistry.getInstance().getPlanet(planetId);

        if (planet != null) {
            AdAstraMekanized.LOGGER.info("Found planet in registry: {} - {}", planet.displayName(), planet.id());
            AdAstraMekanized.LOGGER.info("Planet details: Gravity {}, Temperature {}°C, {}habitable",
                planet.properties().gravity(), planet.properties().temperature(),
                planet.isHabitable() ? "" : "not ");

            // Try dynamic dimension manager
            DynamicDimensionManager dimensionManager = DynamicDimensionManager.getInstance();
            dimensionManager.initialize(server);

            ServerLevel dynamicLevel = dimensionManager.getOrCreatePlanetDimension(dimensionKey, dimensionName);
            if (dynamicLevel != null) {
                return dynamicLevel;
            }
        } else {
            AdAstraMekanized.LOGGER.warn("Planet not found in registry: {}", planetId);
        }

        AdAstraMekanized.LOGGER.warn("Could not create dimension for: {}", dimensionKey.location());
        AdAstraMekanized.LOGGER.warn("Using Overworld as fallback for testing...");

        // Return overworld as fallback for testing, but with a clear message
        return server.getLevel(Level.OVERWORLD);
    }

    private static BlockPos findSafeSpawnLocation(ServerLevel level) {
        // Start at world spawn and find the surface
        BlockPos worldSpawn = level.getSharedSpawnPos();

        // Find the highest solid block at spawn location
        BlockPos.MutableBlockPos mutablePos = worldSpawn.mutable();

        // Start from a reasonable height and go down to find ground
        mutablePos.setY(100);
        while (mutablePos.getY() > level.getMinBuildHeight() &&
               level.getBlockState(mutablePos).isAir()) {
            mutablePos.move(0, -1, 0);
        }

        // Move up one block to stand on the surface
        mutablePos.move(0, 1, 0);

        // Make sure we're not too low
        if (mutablePos.getY() < level.getMinBuildHeight() + 10) {
            mutablePos.setY(level.getMinBuildHeight() + 10);
        }

        return mutablePos.immutable();
    }
}