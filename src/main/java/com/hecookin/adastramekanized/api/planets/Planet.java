package com.hecookin.adastramekanized.api.planets;

import com.hecookin.adastramekanized.api.planets.generation.PlanetGenerationSettings;
import com.hecookin.adastramekanized.api.planets.atmosphere.AtmosphericRendering;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;

/**
 * Core planet data structure for Ad Astra Mekanized.
 *
 * Represents a planet with all necessary properties for dimension generation,
 * environmental effects, and space travel calculations.
 */
public record Planet(
        ResourceLocation id,
        String displayName,
        PlanetProperties properties,
        AtmosphereData atmosphere,
        DimensionSettings dimension,
        PlanetGenerationSettings generation,
        AtmosphericRendering rendering
) {

    public static final Codec<Planet> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("id").forGetter(Planet::id),
                    Codec.STRING.fieldOf("display_name").forGetter(Planet::displayName),
                    PlanetProperties.CODEC.fieldOf("properties").forGetter(Planet::properties),
                    AtmosphereData.CODEC.fieldOf("atmosphere").forGetter(Planet::atmosphere),
                    DimensionSettings.CODEC.fieldOf("dimension").forGetter(Planet::dimension),
                    PlanetGenerationSettings.CODEC.optionalFieldOf("generation", PlanetGenerationSettings.createEarthlike()).forGetter(Planet::generation),
                    AtmosphericRendering.CODEC.optionalFieldOf("rendering", AtmosphericRendering.createDefault()).forGetter(Planet::rendering)
            ).apply(instance, Planet::new)
    );

    /**
     * Physical and environmental properties of the planet
     */
    public record PlanetProperties(
            float gravity,           // Gravity multiplier (1.0 = Earth gravity)
            float temperature,       // Average temperature in Celsius
            float dayLength,         // Length of day in Earth hours
            int orbitDistance,       // Distance from star in millions of km
            boolean hasRings,        // Whether planet has ring system
            int moonCount           // Number of natural satellites
    ) {
        public static final Codec<PlanetProperties> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.FLOAT.fieldOf("gravity").forGetter(PlanetProperties::gravity),
                        Codec.FLOAT.fieldOf("temperature").forGetter(PlanetProperties::temperature),
                        Codec.FLOAT.fieldOf("day_length").forGetter(PlanetProperties::dayLength),
                        Codec.INT.fieldOf("orbit_distance").forGetter(PlanetProperties::orbitDistance),
                        Codec.BOOL.optionalFieldOf("has_rings", false).forGetter(PlanetProperties::hasRings),
                        Codec.INT.optionalFieldOf("moon_count", 0).forGetter(PlanetProperties::moonCount)
                ).apply(instance, PlanetProperties::new)
        );

        /**
         * Validate planet properties for reasonable values
         */
        public boolean isValid() {
            return gravity > 0.0f && gravity < 10.0f &&
                   temperature > -300.0f && temperature < 2000.0f &&
                   dayLength > 0.0f &&
                   orbitDistance > 0 &&
                   moonCount >= 0;
        }
    }

    /**
     * Atmospheric composition and properties
     */
    public record AtmosphereData(
            boolean hasAtmosphere,
            boolean breathable,
            float pressure,          // Atmospheric pressure (1.0 = Earth sea level)
            float oxygenLevel,       // Oxygen percentage (0.0-1.0)
            AtmosphereType type
    ) {
        public static final Codec<AtmosphereData> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.BOOL.fieldOf("has_atmosphere").forGetter(AtmosphereData::hasAtmosphere),
                        Codec.BOOL.fieldOf("breathable").forGetter(AtmosphereData::breathable),
                        Codec.FLOAT.fieldOf("pressure").forGetter(AtmosphereData::pressure),
                        Codec.FLOAT.fieldOf("oxygen_level").forGetter(AtmosphereData::oxygenLevel),
                        AtmosphereType.CODEC.fieldOf("type").forGetter(AtmosphereData::type)
                ).apply(instance, AtmosphereData::new)
        );

        /**
         * Check if the atmosphere requires life support systems
         */
        public boolean requiresLifeSupport() {
            return !hasAtmosphere || !breathable || oxygenLevel < 0.16f || pressure < 0.5f;
        }
    }

    /**
     * Types of planetary atmospheres
     */
    public enum AtmosphereType {
        NONE,           // No atmosphere (vacuum)
        THIN,           // Very thin atmosphere
        NORMAL,         // Earth-like atmosphere
        THICK,          // Dense atmosphere
        TOXIC,          // Poisonous atmosphere
        CORROSIVE;      // Corrosive atmosphere

        public static final Codec<AtmosphereType> CODEC = Codec.STRING.xmap(
                name -> {
                    try {
                        return AtmosphereType.valueOf(name.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        return NONE;
                    }
                },
                AtmosphereType::name
        );
    }

    /**
     * Dimension generation and management settings
     */
    public record DimensionSettings(
            ResourceLocation dimensionType,
            ResourceLocation biomeSource,
            ResourceLocation chunkGenerator,
            boolean isOrbital,       // True for space stations and orbital platforms
            int skyColor,           // RGB color for sky rendering
            int fogColor,           // RGB color for fog
            float ambientLight      // Ambient light level (0.0-1.0)
    ) {
        public static final Codec<DimensionSettings> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        ResourceLocation.CODEC.fieldOf("dimension_type").forGetter(DimensionSettings::dimensionType),
                        ResourceLocation.CODEC.fieldOf("biome_source").forGetter(DimensionSettings::biomeSource),
                        ResourceLocation.CODEC.fieldOf("chunk_generator").forGetter(DimensionSettings::chunkGenerator),
                        Codec.BOOL.optionalFieldOf("is_orbital", false).forGetter(DimensionSettings::isOrbital),
                        Codec.INT.optionalFieldOf("sky_color", 0x000000).forGetter(DimensionSettings::skyColor),
                        Codec.INT.optionalFieldOf("fog_color", 0x000000).forGetter(DimensionSettings::fogColor),
                        Codec.FLOAT.optionalFieldOf("ambient_light", 0.0f).forGetter(DimensionSettings::ambientLight)
                ).apply(instance, DimensionSettings::new)
        );
    }

    /**
     * Validate the complete planet data structure
     */
    public boolean isValid() {
        return id != null &&
               displayName != null && !displayName.isEmpty() &&
               properties != null && properties.isValid() &&
               atmosphere != null &&
               dimension != null &&
               generation != null && generation.isValid();
    }

    /**
     * Get the dimension resource location for this planet
     */
    public ResourceLocation getDimensionLocation() {
        // For our datapack dimensions, the dimension files are at adastramekanized:moon, adastramekanized:mars, etc.
        // Not in a "planets/" subdirectory
        return ResourceLocation.fromNamespaceAndPath(id.getNamespace(), id.getPath());
    }

    /**
     * Calculate fuel cost for travel to this planet (placeholder implementation)
     */
    public int calculateFuelCost(Planet fromPlanet) {
        if (fromPlanet == null) return properties.orbitDistance / 10; // From Earth

        int distance = Math.abs(properties.orbitDistance - fromPlanet.properties().orbitDistance);
        return Math.max(1, distance / 100); // Simplified fuel calculation
    }

    /**
     * Check if this planet can support life without equipment
     */
    public boolean isHabitable() {
        return atmosphere.hasAtmosphere &&
               atmosphere.breathable &&
               atmosphere.oxygenLevel >= 0.16f &&
               atmosphere.pressure >= 0.5f &&
               properties.temperature >= -50.0f &&
               properties.temperature <= 50.0f;
    }
}