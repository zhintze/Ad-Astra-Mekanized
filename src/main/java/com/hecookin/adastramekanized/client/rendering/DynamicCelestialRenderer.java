package com.hecookin.adastramekanized.client.rendering;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.api.planets.atmosphere.AtmosphericRendering;
import com.hecookin.adastramekanized.client.sky.CelestialSkyRenderer;
import com.hecookin.adastramekanized.client.sky.MovementType;
import com.hecookin.adastramekanized.client.sky.SkyRenderable;
import com.hecookin.adastramekanized.common.dimensions.EnhancedRuntimeDimensionRegistry;
import com.hecookin.adastramekanized.common.planets.DynamicPlanetData;
import com.hecookin.adastramekanized.common.planets.DynamicPlanetRegistry;
import com.hecookin.adastramekanized.common.planets.DimensionEffectsType;
import com.hecookin.adastramekanized.common.planets.CelestialType;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

/**
 * Renders celestial bodies for dynamic planets.
 * Integrates with DynamicPlanetRegistry to provide sky rendering based on planet configuration.
 */
public class DynamicCelestialRenderer {

    /**
     * Render celestial bodies for the current dimension if it's a dynamic planet
     */
    public static void renderCelestialBodies(PoseStack poseStack, Matrix4f projectionMatrix, float partialTick,
                                           ClientLevel level, Camera camera) {
        // Get the current dimension key
        ResourceKey<Level> dimensionKey = level.dimension();

        // Check if this dimension is managed by our enhanced registry
        EnhancedRuntimeDimensionRegistry registry = EnhancedRuntimeDimensionRegistry.getInstance();
        if (!registry.hasDimensionMapping(dimensionKey)) {
            return; // Not a dynamic planet dimension
        }

        // Get planet data from the dimension key
        DynamicPlanetData planetData = getDynamicPlanetDataForDimension(dimensionKey);
        if (planetData == null) {
            return;
        }

        AdAstraMekanized.LOGGER.debug("Rendering celestial bodies for dynamic planet: {}", planetData.getDisplayName());

        // Generate celestial configuration from planet data
        JsonObject celestialBodies = planetData.getCelestialType().generateCelestialBodies();

        // Convert JSON configuration to SkyRenderable objects
        List<SkyRenderable> skyRenderables = convertJsonToSkyRenderables(celestialBodies);

        if (!skyRenderables.isEmpty()) {
            // Render the celestial bodies
            CelestialSkyRenderer.renderCelestialBodies(poseStack, projectionMatrix, partialTick, level, camera, skyRenderables);
        }

        // Also render stars based on planet configuration
        renderDynamicStars(poseStack, projectionMatrix, partialTick, level, planetData);
    }

    /**
     * Get DynamicPlanetData for a dimension key
     */
    private static DynamicPlanetData getDynamicPlanetDataForDimension(ResourceKey<Level> dimensionKey) {
        // Extract planet ID from dimension key
        ResourceLocation dimensionLocation = dimensionKey.location();

        if (!AdAstraMekanized.MOD_ID.equals(dimensionLocation.getNamespace())) {
            return null; // Not our mod's dimension
        }

        // For dynamic planets, the dimension key should match the planet ID
        ResourceLocation planetId = dimensionLocation;
        AdAstraMekanized.LOGGER.debug("Looking up planet data for dimension: {}", planetId);

        // Create a temporary planet data for demonstration purposes
        // In a full implementation, this would be synced from server or cached on client
        return createTemporaryPlanetData(planetId);
    }

    /**
     * Create temporary planet data for client-side rendering
     * TODO: Replace with proper client-server synchronization
     */
    private static DynamicPlanetData createTemporaryPlanetData(ResourceLocation planetId) {
        // This is a temporary solution for demonstration
        // In production, you would sync this data from server to client

        String planetName = planetId.getPath();

        // Create basic planet data with reasonable defaults
        // These will be used for celestial rendering until proper sync is implemented
        return new DynamicPlanetData(
            planetId,
            capitalizeWords(planetName.replace("_", " ")),
            DimensionEffectsType.ROCKY, // Default type
            CelestialType.SINGLE_SUN,   // Default celestial type
            0.8f,    // gravity
            -20.0f,  // temperature
            24.0f,   // day length
            150,     // orbit distance
            true,    // has atmosphere
            false,   // not breathable
            0.5f     // atmosphere pressure
        );
    }

    /**
     * Capitalize words in a string
     */
    private static String capitalizeWords(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        String[] words = input.split(" ");
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            if (i > 0) {
                result.append(" ");
            }
            if (!words[i].isEmpty()) {
                result.append(Character.toUpperCase(words[i].charAt(0)));
                if (words[i].length() > 1) {
                    result.append(words[i].substring(1).toLowerCase());
                }
            }
        }

        return result.toString();
    }

    /**
     * Convert JSON celestial configuration to SkyRenderable objects
     */
    private static List<SkyRenderable> convertJsonToSkyRenderables(JsonObject celestialBodies) {
        List<SkyRenderable> renderables = new ArrayList<>();

        try {
            // Handle sun
            if (celestialBodies.has("sun")) {
                JsonObject sun = celestialBodies.getAsJsonObject("sun");
                if (sun.get("visible").getAsBoolean()) {
                    renderables.add(createSunRenderable(sun));
                }
            }

            // Handle moons
            if (celestialBodies.has("moons")) {
                JsonArray moons = celestialBodies.getAsJsonArray("moons");
                for (JsonElement moonElement : moons) {
                    JsonObject moon = moonElement.getAsJsonObject();
                    if (moon.get("visible").getAsBoolean()) {
                        renderables.add(createMoonRenderable(moon));
                    }
                }
            }

            // Handle visible planets
            if (celestialBodies.has("visible_planets")) {
                JsonArray planets = celestialBodies.getAsJsonArray("visible_planets");
                for (JsonElement planetElement : planets) {
                    JsonObject planet = planetElement.getAsJsonObject();
                    if (planet.get("visible").getAsBoolean()) {
                        renderables.add(createPlanetRenderable(planet));
                    }
                }
            }

        } catch (Exception e) {
            AdAstraMekanized.LOGGER.warn("Failed to convert celestial JSON to renderables: {}", e.getMessage());
        }

        return renderables;
    }

    /**
     * Create SkyRenderable for sun
     */
    private static SkyRenderable createSunRenderable(JsonObject sun) {
        ResourceLocation texture = ResourceLocation.parse(sun.get("texture").getAsString());
        float scale = sun.get("scale").getAsFloat();
        int color = sun.get("color").getAsInt();

        return new SkyRenderable(
            texture,
            scale,
            new Vec3(0, 0, 0), // Global rotation - sun follows time of day
            new Vec3(0, 0, 0), // Local rotation
            MovementType.TIME_OF_DAY, // Sun moves with time
            true, // Blend enabled
            color,
            1.0f  // Distance multiplier
        );
    }

    /**
     * Create SkyRenderable for moon
     */
    private static SkyRenderable createMoonRenderable(JsonObject moon) {
        ResourceLocation texture = ResourceLocation.parse(moon.get("texture").getAsString());
        float scale = moon.get("scale").getAsFloat();
        int color = moon.get("color").getAsInt();

        // Extract position if available
        float horizontalPos = moon.has("horizontal_position") ? moon.get("horizontal_position").getAsFloat() : 0.0f;
        float verticalPos = moon.has("vertical_position") ? moon.get("vertical_position").getAsFloat() : 0.0f;
        boolean movesWithTime = moon.has("moves_with_time") ? moon.get("moves_with_time").getAsBoolean() : true;

        Vec3 globalRotation = new Vec3(verticalPos * 90.0, horizontalPos * 180.0, 0);
        MovementType movement = movesWithTime ? MovementType.TIME_OF_DAY : MovementType.STATIC;

        return new SkyRenderable(
            texture,
            scale,
            globalRotation,
            new Vec3(0, 0, 0), // Local rotation
            movement,
            true, // Blend enabled
            color,
            0.8f  // Distance multiplier - moons are closer
        );
    }

    /**
     * Create SkyRenderable for visible planet
     */
    private static SkyRenderable createPlanetRenderable(JsonObject planet) {
        ResourceLocation texture = ResourceLocation.parse(planet.get("texture").getAsString());
        float scale = planet.get("scale").getAsFloat();
        int color = planet.get("color").getAsInt();

        // Extract position if available
        float horizontalPos = planet.has("horizontal_position") ? planet.get("horizontal_position").getAsFloat() : 0.0f;
        float verticalPos = planet.has("vertical_position") ? planet.get("vertical_position").getAsFloat() : 0.0f;
        boolean movesWithTime = planet.has("moves_with_time") ? planet.get("moves_with_time").getAsBoolean() : false;

        Vec3 globalRotation = new Vec3(verticalPos * 45.0, horizontalPos * 90.0, 0);
        MovementType movement = movesWithTime ? MovementType.TIME_OF_DAY : MovementType.STATIC;

        return new SkyRenderable(
            texture,
            scale,
            globalRotation,
            new Vec3(0, 0, 0), // Local rotation
            movement,
            true, // Blend enabled
            color,
            1.2f  // Distance multiplier - planets are distant
        );
    }

    /**
     * Render stars based on dynamic planet configuration
     */
    private static void renderDynamicStars(PoseStack poseStack, Matrix4f projectionMatrix, float partialTick,
                                         ClientLevel level, DynamicPlanetData planetData) {
        // Generate sky configuration from planet data
        JsonObject skyConfig = planetData.getCelestialType().generateSkyConfig(planetData.getEffectsType());

        // Convert to AtmosphericRendering.SkyConfiguration for compatibility with existing star renderer
        AtmosphericRendering.SkyConfiguration starConfig = createSkyConfigFromJson(skyConfig);

        // Render stars using existing system
        CelestialSkyRenderer.renderStars(poseStack, projectionMatrix, partialTick, level, starConfig);
    }

    /**
     * Convert JSON sky configuration to AtmosphericRendering.SkyConfiguration
     */
    private static AtmosphericRendering.SkyConfiguration createSkyConfigFromJson(JsonObject skyConfig) {
        int skyColor = skyConfig.get("sky_color").getAsInt();
        int sunriseColor = skyColor; // Use same color for sunrise if not specified
        boolean customSky = true;
        boolean hasStars = skyConfig.get("has_stars").getAsBoolean();
        int starCount = skyConfig.get("star_count").getAsInt();
        float starBrightness = skyConfig.get("star_brightness").getAsFloat();

        String starVisibilityStr = skyConfig.get("star_visibility").getAsString();
        AtmosphericRendering.StarVisibility starVisibility = switch (starVisibilityStr) {
            case "constant" -> AtmosphericRendering.StarVisibility.CONSTANT;
            case "night_only" -> AtmosphericRendering.StarVisibility.NIGHT_ONLY;
            default -> AtmosphericRendering.StarVisibility.NIGHT_ONLY;
        };

        return new AtmosphericRendering.SkyConfiguration(
            skyColor,
            sunriseColor,
            customSky,
            hasStars,
            starCount,
            starBrightness,
            starVisibility,
            java.util.Optional.empty() // No custom sky texture
        );
    }
}