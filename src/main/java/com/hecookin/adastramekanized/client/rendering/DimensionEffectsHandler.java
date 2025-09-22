package com.hecookin.adastramekanized.client.rendering;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.api.planets.Planet;
import com.hecookin.adastramekanized.api.planets.PlanetRegistry;
import com.hecookin.adastramekanized.client.rendering.planet.MoonDimensionEffects;
import com.hecookin.adastramekanized.common.planets.PlanetManager;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;

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

        // Define known planets with their JSON-based properties
        registerPlanetEffect(event, "moon", createMoonEffects());
        registerPlanetEffect(event, "venus", createVenusEffects());
        registerPlanetEffect(event, "mercury", createMercuryEffects());
        registerPlanetEffect(event, "glacio", createGlacioEffects());
        registerPlanetEffect(event, "earth_example", createEarthEffects());
        registerPlanetEffect(event, "binary_world", createBinaryWorldEffects());
    }

    private static void registerPlanetEffect(RegisterDimensionSpecialEffectsEvent event, String planetId, DimensionSpecialEffects effects) {
        ResourceLocation dimensionType = ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, planetId);
        event.register(dimensionType, effects);
        AdAstraMekanized.LOGGER.debug("Registered dimension effects for: {}", planetId);
    }

    // Create dimension effects based on actual planet JSON data

    private static MoonDimensionEffects createMoonEffects() {
        // Moon: gravity=0.165, temp=-173.0, no atmosphere, black space sky
        return new MoonDimensionEffects();
    }

    private static PlanetDimensionEffects createVenusEffects() {
        // Venus: gravity=0.9, temp=85.0, thick toxic atmosphere, yellowish
        return new PlanetDimensionEffects(null) {
            @Override
            public boolean isFoggyAt(int x, int z) {
                return true; // Thick atmosphere = heavy fog
            }
        };
    }

    private static PlanetDimensionEffects createMercuryEffects() {
        // Mercury: gravity=0.38, temp=50.0, no atmosphere, metallic
        return new PlanetDimensionEffects(null) {
            @Override
            public boolean isFoggyAt(int x, int z) {
                return false; // No atmosphere = no fog
            }
        };
    }

    private static PlanetDimensionEffects createGlacioEffects() {
        // Glacio: gravity=0.8, temp=-45.0, thin atmosphere, icy
        return new PlanetDimensionEffects(null) {
            @Override
            public boolean isFoggyAt(int x, int z) {
                return true; // Thin atmosphere with ice crystals
            }
        };
    }

    private static PlanetDimensionEffects createEarthEffects() {
        // Earth Example: gravity=1.0, temp=15.0, breathable atmosphere
        return new PlanetDimensionEffects(null) {
            @Override
            public boolean isFoggyAt(int x, int z) {
                return true; // Earth-like atmosphere
            }
        };
    }

    private static PlanetDimensionEffects createBinaryWorldEffects() {
        // Binary World: gravity=0.8, temp=45.0, toxic atmosphere, dual stars
        return new PlanetDimensionEffects(null) {
            @Override
            public boolean isFoggyAt(int x, int z) {
                return true; // Toxic atmosphere creates haze
            }
        };
    }
}