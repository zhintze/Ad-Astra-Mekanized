package com.hecookin.adastramekanized.common.commands;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.api.planets.Planet;
import com.hecookin.adastramekanized.api.planets.PlanetRegistry;
import com.hecookin.adastramekanized.common.planets.PlanetManager;
import com.hecookin.adastramekanized.common.teleportation.PlanetTeleportationSystem;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.concurrent.CompletableFuture;

/**
 * Debug commands for planet teleportation and exploration.
 *
 * Provides /planet teleport <planet_name> and /planet list commands
 * for debugging and testing planet functionality.
 */
public class PlanetDebugCommands {

    /**
     * Register planet debug commands
     */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("planet")
            .requires(source -> source.hasPermission(2)) // Require OP level 2
            .then(Commands.literal("list")
                .executes(PlanetDebugCommands::listPlanets))
            .then(Commands.literal("teleport")
                .then(Commands.argument("planet", StringArgumentType.string())
                    .suggests(PLANET_SUGGESTIONS)
                    .executes(PlanetDebugCommands::teleportToPlanet)))
            .then(Commands.literal("info")
                .then(Commands.argument("planet", StringArgumentType.string())
                    .suggests(PLANET_SUGGESTIONS)
                    .executes(PlanetDebugCommands::showPlanetInfo)))
        );
    }

    /**
     * Suggestion provider for planet names
     */
    private static final SuggestionProvider<CommandSourceStack> PLANET_SUGGESTIONS =
        (context, builder) -> {
            PlanetRegistry registry = PlanetRegistry.getInstance();
            if (!registry.isDataLoaded()) {
                return Suggestions.empty();
            }

            // Create suggestions for both short names and full IDs
            var suggestions = registry.getAllPlanets().stream()
                .flatMap(planet -> {
                    var id = planet.id();
                    // Add both the short name (path) and full ID
                    return java.util.stream.Stream.of(
                        id.getPath(),           // Short name like "moon"
                        id.toString()           // Full ID like "adastramekanized:moon"
                    );
                })
                .distinct()
                .toArray(String[]::new);

            return SharedSuggestionProvider.suggest(suggestions, builder);
        };

    /**
     * List all available planets
     */
    private static int listPlanets(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        PlanetRegistry registry = PlanetRegistry.getInstance();
        PlanetManager manager = PlanetManager.getInstance();

        if (!manager.isReady()) {
            source.sendFailure(Component.literal("Planet system not ready. Manager status: " + manager.getStatus()));
            return 0;
        }

        var planets = registry.getAllPlanets();
        if (planets.isEmpty()) {
            source.sendFailure(Component.literal("No planets found in registry"));
            return 0;
        }

        source.sendSuccess(() -> Component.literal("§6=== Planet List (" + planets.size() + " planets) ==="), false);

        for (Planet planet : planets) {
            boolean dimensionLoaded = manager.isPlanetDimensionLoaded(planet.id());
            String status = dimensionLoaded ? "§a✓" : "§c✗";
            String habitableIcon = planet.isHabitable() ? "§2🌍" : "§4🪨";

            Component planetInfo = Component.literal(String.format(
                "%s %s §f%s §7(%s) - %s°C, %.1fg gravity %s",
                status,
                habitableIcon,
                planet.displayName(),
                planet.id(),
                Math.round(planet.properties().temperature()),
                planet.properties().gravity(),
                dimensionLoaded ? "§a[LOADED]" : "§c[NOT LOADED]"
            ));

            source.sendSuccess(() -> planetInfo, false);
        }

        source.sendSuccess(() -> Component.literal("§7Use '/planet teleport <planet_id>' to teleport to a planet"), false);
        source.sendSuccess(() -> Component.literal("§7Use '/planet info <planet_id>' for detailed planet information"), false);

        return planets.size();
    }

    /**
     * Teleport player to specified planet
     */
    private static int teleportToPlanet(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        String planetIdString = StringArgumentType.getString(context, "planet");

        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be used by players"));
            return 0;
        }

        PlanetManager manager = PlanetManager.getInstance();
        if (!manager.isReady()) {
            source.sendFailure(Component.literal("Planet system not ready"));
            return 0;
        }

        // Parse planet ID
        ResourceLocation planetId;
        try {
            if (planetIdString.contains(":")) {
                planetId = ResourceLocation.parse(planetIdString);
            } else {
                planetId = ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, planetIdString);
            }
        } catch (Exception e) {
            source.sendFailure(Component.literal("Invalid planet ID: " + planetIdString));
            return 0;
        }

        // Get planet
        Planet planet = manager.getPlanet(planetId);
        if (planet == null) {
            source.sendFailure(Component.literal("Planet not found: " + planetId));
            return 0;
        }

        // Check if dimension is loaded
        if (!manager.isPlanetDimensionLoaded(planetId)) {
            source.sendFailure(Component.literal("Planet dimension not loaded: " + planetId));
            return 0;
        }

        // Attempt teleportation
        source.sendSuccess(() -> Component.literal("§6Attempting teleportation to " + planet.displayName() + "..."), false);

        PlanetTeleportationSystem teleportSystem = PlanetTeleportationSystem.getInstance();

        CompletableFuture<PlanetTeleportationSystem.TeleportResult> teleportFuture =
            teleportSystem.teleportToAnyPlanet(player, planetId);

        teleportFuture.whenComplete((result, throwable) -> {
            if (throwable != null) {
                AdAstraMekanized.LOGGER.error("Teleportation failed", throwable);
                source.sendFailure(Component.literal("§cTeleportation failed: " + throwable.getMessage()));
            } else if (result.isSuccess()) {
                source.sendSuccess(() -> Component.literal("§aTeleportation successful! Welcome to " + planet.displayName()), false);

                // Show planet info after teleportation
                if (!planet.isHabitable()) {
                    source.sendSuccess(() -> Component.literal("§c⚠ Warning: This planet is not habitable! You may need oxygen support."), false);
                }

                source.sendSuccess(() -> Component.literal(String.format(
                    "§7Planet Info: %.1f°C, %.1fg gravity, %s atmosphere",
                    planet.properties().temperature(),
                    planet.properties().gravity(),
                    planet.atmosphere().type().name().toLowerCase()
                )), false);

                if (result.getPosition() != null) {
                    Vec3 pos = result.getPosition();
                    source.sendSuccess(() -> Component.literal(String.format(
                        "§7Location: %.1f, %.1f, %.1f",
                        pos.x, pos.y, pos.z
                    )), false);
                }
            } else {
                source.sendFailure(Component.literal("§cTeleportation failed: " + result.getMessage()));
            }
        });

        return 1;
    }

    /**
     * Show detailed information about a planet
     */
    private static int showPlanetInfo(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        String planetIdString = StringArgumentType.getString(context, "planet");

        PlanetManager manager = PlanetManager.getInstance();
        if (!manager.isReady()) {
            source.sendFailure(Component.literal("Planet system not ready"));
            return 0;
        }

        // Parse planet ID
        ResourceLocation planetId;
        try {
            if (planetIdString.contains(":")) {
                planetId = ResourceLocation.parse(planetIdString);
            } else {
                planetId = ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, planetIdString);
            }
        } catch (Exception e) {
            source.sendFailure(Component.literal("Invalid planet ID: " + planetIdString));
            return 0;
        }

        // Get planet
        Planet planet = manager.getPlanet(planetId);
        if (planet == null) {
            source.sendFailure(Component.literal("Planet not found: " + planetId));
            return 0;
        }

        // Display detailed planet information
        boolean dimensionLoaded = manager.isPlanetDimensionLoaded(planetId);
        String habitableIcon = planet.isHabitable() ? "§2🌍" : "§4🪨";

        source.sendSuccess(() -> Component.literal("§6=== " + habitableIcon + " " + planet.displayName() + " ==="), false);
        source.sendSuccess(() -> Component.literal("§7ID: §f" + planet.id()), false);
        source.sendSuccess(() -> Component.literal("§7Dimension: §f" + planet.getDimensionLocation() + (dimensionLoaded ? " §a[LOADED]" : " §c[NOT LOADED]")), false);

        // Physical properties
        source.sendSuccess(() -> Component.literal("§6Physical Properties:"), false);
        source.sendSuccess(() -> Component.literal(String.format("  §7Temperature: §f%.1f°C", planet.properties().temperature())), false);
        source.sendSuccess(() -> Component.literal(String.format("  §7Gravity: §f%.1fg", planet.properties().gravity())), false);
        source.sendSuccess(() -> Component.literal(String.format("  §7Day Length: §f%.1f hours", planet.properties().dayLength())), false);
        source.sendSuccess(() -> Component.literal(String.format("  §7Orbit Distance: §f%d million km", planet.properties().orbitDistance())), false);
        source.sendSuccess(() -> Component.literal(String.format("  §7Moons: §f%d", planet.properties().moonCount())), false);
        source.sendSuccess(() -> Component.literal(String.format("  §7Rings: §f%s", planet.properties().hasRings() ? "Yes" : "No")), false);

        // Atmosphere
        source.sendSuccess(() -> Component.literal("§6Atmosphere:"), false);
        source.sendSuccess(() -> Component.literal(String.format("  §7Type: §f%s", planet.atmosphere().type().name().toLowerCase())), false);
        source.sendSuccess(() -> Component.literal(String.format("  §7Pressure: §f%.1f atm", planet.atmosphere().pressure())), false);
        source.sendSuccess(() -> Component.literal(String.format("  §7Oxygen: §f%.1f%%", planet.atmosphere().oxygenLevel() * 100)), false);
        source.sendSuccess(() -> Component.literal(String.format("  §7Breathable: §f%s", planet.atmosphere().breathable() ? "Yes" : "No")), false);
        source.sendSuccess(() -> Component.literal(String.format("  §7Life Support Required: §f%s", planet.atmosphere().requiresLifeSupport() ? "Yes" : "No")), false);

        // Habitability
        String habitabilityColor = planet.isHabitable() ? "§a" : "§c";
        source.sendSuccess(() -> Component.literal("§6Habitability: " + habitabilityColor + (planet.isHabitable() ? "Habitable" : "Hostile")), false);

        return 1;
    }
}