package com.hecookin.adastramekanized.common.commands;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.api.planets.Planet;
import com.hecookin.adastramekanized.api.planets.PlanetRegistry;
import com.hecookin.adastramekanized.common.dimensions.PlanetDimensionManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;

/**
 * Commands for testing and managing planet dimensions.
 *
 * Provides commands for:
 * - Traveling to planet dimensions
 * - Listing available planets
 * - Getting planet information
 * - Testing dimension creation
 */
public class PlanetCommands {

    private static final DynamicCommandExceptionType PLANET_NOT_FOUND =
            new DynamicCommandExceptionType(planet ->
                Component.literal("Planet not found: " + planet));

    private static final DynamicCommandExceptionType DIMENSION_NOT_AVAILABLE =
            new DynamicCommandExceptionType(planet ->
                Component.literal("Dimension not available for planet: " + planet));

    /**
     * Register planet commands
     */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("adastra")
                .requires(source -> source.hasPermission(2)) // OP level 2
                .then(Commands.literal("travel")
                        .then(Commands.argument("planet", StringArgumentType.greedyString())
                                .suggests(PLANET_SUGGESTIONS)
                                .executes(PlanetCommands::travelToPlanet)))
                .then(Commands.literal("teleport")
                        .then(Commands.argument("planet", StringArgumentType.string())
                                .suggests(PLANET_SUGGESTIONS)
                                .then(Commands.argument("pos", Vec3Argument.vec3())
                                        .executes(PlanetCommands::teleportToPlanetAt))))
                .then(Commands.literal("list")
                        .executes(PlanetCommands::listPlanets))
                .then(Commands.literal("info")
                        .then(Commands.argument("planet", StringArgumentType.greedyString())
                                .suggests(PLANET_SUGGESTIONS)
                                .executes(PlanetCommands::planetInfo)))
                .then(Commands.literal("status")
                        .executes(PlanetCommands::systemStatus)));

        AdAstraMekanized.LOGGER.info("Registered planet commands");
    }

    /**
     * Suggestion provider for planet names
     */
    private static final SuggestionProvider<CommandSourceStack> PLANET_SUGGESTIONS =
            (context, builder) -> {
                PlanetRegistry registry = PlanetRegistry.getInstance();
                Collection<Planet> planets = registry.getAllPlanets();

                return SharedSuggestionProvider.suggest(
                        planets.stream().map(planet -> planet.id().toString()),
                        builder);
            };

    /**
     * Travel to a planet dimension at spawn coordinates
     */
    private static int travelToPlanet(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String planetIdString = StringArgumentType.getString(context, "planet");
        CommandSourceStack source = context.getSource();

        // Parse planet ID
        ResourceLocation planetId;
        try {
            planetId = ResourceLocation.parse(planetIdString);
        } catch (Exception e) {
            // Try with our namespace if no namespace provided
            planetId = ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, planetIdString);
        }

        return teleportToPlanet(source, planetId, new Vec3(0, 100, 0));
    }

    /**
     * Teleport to specific coordinates on a planet
     */
    private static int teleportToPlanetAt(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String planetIdString = StringArgumentType.getString(context, "planet");
        Vec3 position = Vec3Argument.getVec3(context, "pos");
        CommandSourceStack source = context.getSource();

        // Parse planet ID
        ResourceLocation planetId;
        try {
            planetId = ResourceLocation.parse(planetIdString);
        } catch (Exception e) {
            planetId = ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, planetIdString);
        }

        return teleportToPlanet(source, planetId, position);
    }

    /**
     * Core teleportation logic
     */
    private static int teleportToPlanet(CommandSourceStack source, ResourceLocation planetId, Vec3 position)
            throws CommandSyntaxException {

        // Get the planet from registry
        PlanetRegistry registry = PlanetRegistry.getInstance();
        Planet planet = registry.getPlanet(planetId);
        if (planet == null) {
            throw PLANET_NOT_FOUND.create(planetId);
        }

        // Get dimension manager
        PlanetDimensionManager dimensionManager = PlanetDimensionManager.getInstance();

        // Ensure dimension is registered
        if (!dimensionManager.hasDimension(planetId)) {
            source.sendSuccess(() -> Component.literal("Registering dimension for planet: " + planet.displayName()), false);
            dimensionManager.registerPlanetDimension(planet);
        }

        // Get dimension key
        ResourceKey<Level> dimensionKey = dimensionManager.getDimensionKey(planetId);
        if (dimensionKey == null) {
            throw DIMENSION_NOT_AVAILABLE.create(planetId);
        }

        // Get the server level (this will create the dimension if it doesn't exist)
        ServerLevel targetLevel = source.getServer().getLevel(dimensionKey);
        if (targetLevel == null) {
            // Try to create the dimension
            source.sendSuccess(() -> Component.literal("Creating dimension for planet: " + planet.displayName()), false);

            // For now, we'll inform that manual dimension creation is needed
            source.sendFailure(Component.literal("Dimension creation not yet implemented. " +
                    "Dimension key: " + dimensionKey.location()));
            return 0;
        }

        // Teleport the player
        if (source.getEntity() instanceof ServerPlayer player) {
            player.teleportTo(targetLevel, position.x, position.y, position.z, player.getYRot(), player.getXRot());

            source.sendSuccess(() -> Component.literal("Traveled to " + planet.displayName() +
                    " at " + String.format("%.1f, %.1f, %.1f", position.x, position.y, position.z)), true);

            AdAstraMekanized.LOGGER.info("Player {} traveled to planet {} at {}",
                    player.getName().getString(), planetId, position);

            return 1;
        } else {
            source.sendFailure(Component.literal("Only players can travel to planets"));
            return 0;
        }
    }

    /**
     * List all available planets
     */
    private static int listPlanets(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        PlanetRegistry registry = PlanetRegistry.getInstance();
        Collection<Planet> planets = registry.getAllPlanets();

        if (planets.isEmpty()) {
            source.sendSuccess(() -> Component.literal("No planets are currently registered"), false);
            return 0;
        }

        source.sendSuccess(() -> Component.literal("Available planets (" + planets.size() + "):"), false);

        for (Planet planet : planets) {
            String habitableStatus = planet.isHabitable() ? "habitable" : "hostile";
            String atmosphereStatus = planet.atmosphere().hasAtmosphere() ? "atmosphere" : "no atmosphere";

            source.sendSuccess(() -> Component.literal("  • " + planet.displayName() +
                    " (" + planet.id() + ") - " + habitableStatus + ", " + atmosphereStatus), false);
        }

        return planets.size();
    }

    /**
     * Get detailed information about a planet
     */
    private static int planetInfo(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String planetIdString = StringArgumentType.getString(context, "planet");
        CommandSourceStack source = context.getSource();

        // Parse planet ID
        ResourceLocation planetId;
        try {
            planetId = ResourceLocation.parse(planetIdString);
        } catch (Exception e) {
            planetId = ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, planetIdString);
        }

        // Get the planet
        PlanetRegistry registry = PlanetRegistry.getInstance();
        Planet planet = registry.getPlanet(planetId);
        if (planet == null) {
            throw PLANET_NOT_FOUND.create(planetId);
        }

        // Display planet information
        source.sendSuccess(() -> Component.literal("=== " + planet.displayName() + " ==="), false);
        source.sendSuccess(() -> Component.literal("ID: " + planet.id()), false);
        source.sendSuccess(() -> Component.literal("Gravity: " + String.format("%.2f", planet.properties().gravity()) + "x Earth"), false);
        source.sendSuccess(() -> Component.literal("Temperature: " + String.format("%.1f", planet.properties().temperature()) + "°C"), false);
        source.sendSuccess(() -> Component.literal("Day Length: " + String.format("%.1f", planet.properties().dayLength()) + " hours"), false);
        source.sendSuccess(() -> Component.literal("Orbit Distance: " + planet.properties().orbitDistance() + " million km"), false);

        if (planet.atmosphere().hasAtmosphere()) {
            source.sendSuccess(() -> Component.literal("Atmosphere: " + planet.atmosphere().type() +
                    " (pressure: " + String.format("%.2f", planet.atmosphere().pressure()) +
                    ", oxygen: " + String.format("%.1f%%", planet.atmosphere().oxygenLevel() * 100) + ")"), false);
        } else {
            source.sendSuccess(() -> Component.literal("Atmosphere: None (vacuum)"), false);
        }

        source.sendSuccess(() -> Component.literal("Habitable: " + (planet.isHabitable() ? "Yes" : "No")), false);

        // Check dimension status
        PlanetDimensionManager dimensionManager = PlanetDimensionManager.getInstance();
        boolean hasDimension = dimensionManager.hasDimension(planetId);
        boolean isLoaded = dimensionManager.isDimensionLoaded(planetId);

        source.sendSuccess(() -> Component.literal("Dimension registered: " + hasDimension), false);
        source.sendSuccess(() -> Component.literal("Dimension loaded: " + isLoaded), false);

        return 1;
    }

    /**
     * Show system status
     */
    private static int systemStatus(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        PlanetRegistry registry = PlanetRegistry.getInstance();
        PlanetDimensionManager dimensionManager = PlanetDimensionManager.getInstance();

        var registryStats = registry.getStats();
        var dimensionStatus = dimensionManager.getStatus();

        source.sendSuccess(() -> Component.literal("=== Planet System Status ==="), false);
        source.sendSuccess(() -> Component.literal("Registry - Planets: " + registryStats.totalPlanets() +
                ", Habitable: " + registryStats.habitablePlanets() +
                ", With Atmosphere: " + registryStats.atmosphericPlanets()), false);
        source.sendSuccess(() -> Component.literal("Registry - Data Loaded: " + registryStats.dataLoaded() +
                ", Client Synced: " + registryStats.clientSynced()), false);
        source.sendSuccess(() -> Component.literal("Dimensions - Initialized: " + dimensionStatus.initialized() +
                ", Registered: " + dimensionStatus.registeredPlanets()), false);

        return 1;
    }
}