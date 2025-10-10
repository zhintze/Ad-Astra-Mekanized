package com.hecookin.adastramekanized.client.rendering;

import com.google.gson.JsonElement;
import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.api.planets.Planet;
import com.hecookin.adastramekanized.api.planets.PlanetRegistry;
import com.hecookin.adastramekanized.client.rendering.planet.MoonDimensionEffects;
import com.hecookin.adastramekanized.common.planets.PlanetManager;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;

import java.io.BufferedReader;
import java.util.Map;

/**
 * Handles registration of custom dimension special effects for planets.
 *
 * This class registers planet-specific visual effects that control:
 * - Atmospheric rendering (fog, sky color)
 * - Celestial body appearance
 * - Weather and particle effects
 * - Star visibility and rendering
 */
@EventBusSubscriber(modid = AdAstraMekanized.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class DimensionEffectsHandler {

    /**
     * Register custom dimension effects for all planets.
     * Called during client initialization to set up planet-specific visual effects.
     */
    @SubscribeEvent
    public static void registerDimensionEffects(RegisterDimensionSpecialEffectsEvent event) {
        AdAstraMekanized.LOGGER.info("Registering custom dimension effects for planets...");

        try {
            // Get all loaded planets from the planet registry
            PlanetRegistry registry = PlanetRegistry.getInstance();

            if (registry != null && registry.getAllPlanets() != null && !registry.getAllPlanets().isEmpty()) {
                // If registry is ready, use it
                registerFromPlanetRegistry(event, registry);
            } else {
                // Fallback: Register known planets using JSON data
                AdAstraMekanized.LOGGER.info("PlanetRegistry not ready, using fallback registration");
                registerKnownPlanetEffects(event);
            }
        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Failed to register dimension effects, using fallback", e);
            registerKnownPlanetEffects(event);
        }
    }

    private static void registerFromPlanetRegistry(RegisterDimensionSpecialEffectsEvent event, PlanetRegistry registry) {
        int registeredCount = 0;

        // Register effects for each planet
        for (Planet planet : registry.getAllPlanets()) {
            try {
                // Create the dimension type resource location
                ResourceLocation dimensionType = planet.id();

                // Create custom effects for this planet
                PlanetDimensionEffects effects = PlanetDimensionEffects.createForPlanet(planet);

                // Register the effects
                event.register(dimensionType, effects);

                AdAstraMekanized.LOGGER.debug("Registered dimension effects for planet: {}", planet.displayName());
                registeredCount++;

            } catch (Exception e) {
                AdAstraMekanized.LOGGER.error("Failed to register dimension effects for planet: {}",
                    planet.displayName(), e);
            }
        }

        AdAstraMekanized.LOGGER.info("Successfully registered dimension effects for {} planets", registeredCount);
    }

    /**
     * Fallback method to register effects for specific known planets using JSON data
     */
    public static void registerKnownPlanetEffects(RegisterDimensionSpecialEffectsEvent event) {
        AdAstraMekanized.LOGGER.info("Registering dimension effects from planet JSON data...");

        // Load planet data directly from JSON files during client startup
        try {
            net.minecraft.server.packs.resources.ResourceManager resourceManager =
                net.minecraft.client.Minecraft.getInstance().getResourceManager();

            if (resourceManager != null) {
                loadPlanetsFromResourceManager(resourceManager);

                // Now try to register from the loaded data
                PlanetRegistry registry = PlanetRegistry.getInstance();
                if (registry != null && !registry.getAllPlanets().isEmpty()) {
                    registerFromPlanetRegistry(event, registry);
                    return;
                }
            }
        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Failed to load planets from JSON, using hardcoded fallback", e);
        }

        // Last resort: Auto-generate from planet registry (if JSON loading fails completely)
        AdAstraMekanized.LOGGER.info("Using fallback: auto-generating dimension effects from planet registry...");

        // Get all planet builders from PlanetGenerationRunner
        var planetBuilders = com.hecookin.adastramekanized.common.planets.PlanetGenerationRunner.getAllPlanetBuilders();

        if (planetBuilders.isEmpty()) {
            AdAstraMekanized.LOGGER.warn("No planet builders found! Dimension effects will not be registered.");
            return;
        }

        // Auto-generate dimension effects for each planet
        for (var entry : planetBuilders.entrySet()) {
            String planetId = entry.getKey();
            var builder = entry.getValue();

            try {
                DimensionSpecialEffects effects = createEffectsFromBuilder(builder);
                registerPlanetEffect(event, planetId, effects);
                AdAstraMekanized.LOGGER.debug("Auto-registered dimension effects for: {}", planetId);
            } catch (Exception e) {
                AdAstraMekanized.LOGGER.error("Failed to auto-register dimension effects for {}: {}",
                    planetId, e.getMessage());
            }
        }

        AdAstraMekanized.LOGGER.info("Auto-registered dimension effects for {} planets", planetBuilders.size());
    }

    /**
     * Create dimension effects from a planet builder.
     * Used for fallback when planet JSON isn't loaded yet.
     */
    private static DimensionSpecialEffects createEffectsFromBuilder(
            com.hecookin.adastramekanized.common.planets.PlanetMaker.PlanetBuilder builder) {

        // For moon, use special MoonDimensionEffects
        if ("moon".equals(builder.getName())) {
            return new MoonDimensionEffects();
        }

        // For other planets, create a custom PlanetDimensionEffects
        // that reads properties from the builder
        return new PlanetDimensionEffects(null) {
            private final float cloudHeight = builder.hasClouds() ? 192.0f : Float.NaN;
            private final boolean hasRain = builder.hasRain();
            private final boolean hasFog = builder.hasAtmosphere();

            // Override constructor parameters via reflection-like pattern
            // We can't actually override the constructor, so we override methods instead

            @Override
            public boolean isFoggyAt(int x, int z) {
                return hasFog;
            }

            // Note: Cloud height and precipitation are set in the parent constructor,
            // so we can't override them here. The parent will use planet=null which means:
            // - cloudHeight = Float.NaN (no clouds)
            // - hasRain = false
            // This is acceptable for fallback - proper effects load from JSON when available
        };
    }

    /**
     * Load planet data from resource manager during client startup
     */
    private static void loadPlanetsFromResourceManager(ResourceManager resourceManager) {
        AdAstraMekanized.LOGGER.info("Loading planet JSON files from resource manager...");

        PlanetRegistry registry = PlanetRegistry.getInstance();
        int loadedCount = 0;

        try {
            // Find all planet JSON files in data packs
            Map<ResourceLocation, Resource> planetResources = resourceManager.listResources(
                "planets", location -> location.getPath().endsWith(".json"));

            for (Map.Entry<ResourceLocation, Resource> entry : planetResources.entrySet()) {
                ResourceLocation resourceLocation = entry.getKey();
                Resource resource = entry.getValue();

                try {
                    // Extract planet ID from resource location
                    ResourceLocation planetId = extractPlanetId(resourceLocation);

                    if (planetId == null) {
                        AdAstraMekanized.LOGGER.warn("Could not extract planet ID from: {}", resourceLocation);
                        continue;
                    }

                    // Load and parse planet JSON
                    try (BufferedReader reader = resource.openAsReader()) {
                        JsonElement jsonElement = GsonHelper.parse(reader);

                        var result = Planet.CODEC.parse(JsonOps.INSTANCE, jsonElement);

                        if (result.error().isPresent()) {
                            AdAstraMekanized.LOGGER.error("Failed to parse planet {}: {}",
                                planetId, result.error().get().message());
                            continue;
                        }

                        Planet planet = result.result().orElse(null);
                        if (planet != null && registry.registerPlanet(planet)) {
                            loadedCount++;
                            AdAstraMekanized.LOGGER.debug("Loaded planet: {}", planetId);
                        }
                    }
                } catch (Exception e) {
                    AdAstraMekanized.LOGGER.error("Error loading planet from {}: {}",
                        resourceLocation, e.getMessage());
                }
            }

            AdAstraMekanized.LOGGER.info("Loaded {} planets from JSON files", loadedCount);

        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Failed to load planets from resource manager", e);
        }
    }

    /**
     * Extract planet ID from resource location
     * data/namespace/planets/planet_name.json -> namespace:planet_name
     */
    private static ResourceLocation extractPlanetId(ResourceLocation resourceLocation) {
        String path = resourceLocation.getPath();

        // Remove "planets/" prefix and ".json" suffix
        if (!path.startsWith("planets/") || !path.endsWith(".json")) {
            return null;
        }

        String planetName = path.substring(8, path.length() - 5); // "planets/".length() = 8

        if (planetName.isEmpty() || !ResourceLocation.isValidPath(planetName)) {
            return null;
        }

        return ResourceLocation.fromNamespaceAndPath(resourceLocation.getNamespace(), planetName);
    }

    private static void registerPlanetEffect(RegisterDimensionSpecialEffectsEvent event, String planetId, DimensionSpecialEffects effects) {
        ResourceLocation dimensionType = ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, planetId);
        event.register(dimensionType, effects);
        AdAstraMekanized.LOGGER.debug("Registered dimension effects for: {}", planetId);
    }

}