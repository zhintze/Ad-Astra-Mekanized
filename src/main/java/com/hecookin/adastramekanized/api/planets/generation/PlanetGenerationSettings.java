package com.hecookin.adastramekanized.api.planets.generation;

import com.hecookin.adastramekanized.api.planets.atmosphere.AtmosphericRendering;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

/**
 * Comprehensive generation settings for planetary dimensions.
 *
 * Controls terrain generation, atmospheric effects, environmental hazards,
 * and planet-specific features for creating robust, unique dimensions.
 */
public record PlanetGenerationSettings(
    TerrainSettings terrain,
    AtmosphericSettings atmosphere,
    EnvironmentalSettings environment,
    BiomeDistribution biomes,
    ResourceGeneration resources
) {

    public static final Codec<PlanetGenerationSettings> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            TerrainSettings.CODEC.fieldOf("terrain").forGetter(PlanetGenerationSettings::terrain),
            AtmosphericSettings.CODEC.fieldOf("atmosphere").forGetter(PlanetGenerationSettings::atmosphere),
            EnvironmentalSettings.CODEC.fieldOf("environment").forGetter(PlanetGenerationSettings::environment),
            BiomeDistribution.CODEC.fieldOf("biomes").forGetter(PlanetGenerationSettings::biomes),
            ResourceGeneration.CODEC.fieldOf("resources").forGetter(PlanetGenerationSettings::resources)
        ).apply(instance, PlanetGenerationSettings::new)
    );

    /**
     * Terrain generation parameters
     */
    public record TerrainSettings(
        int baseHeight,              // Base terrain height (Y level)
        int heightVariation,         // Maximum height variation
        float roughness,             // Terrain roughness (0.0-2.0)
        float erosion,               // Erosion factor (0.0-1.0)
        float continentalness,       // Continental vs oceanic (0.0-1.0)
        NoiseConfiguration noise,    // Noise generation settings
        boolean generateCaves,       // Whether to generate cave systems
        boolean generateRavines,     // Whether to generate ravines
        SurfaceConfiguration surface // Surface block configuration
    ) {
        public static final Codec<TerrainSettings> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                Codec.INT.fieldOf("base_height").forGetter(TerrainSettings::baseHeight),
                Codec.INT.fieldOf("height_variation").forGetter(TerrainSettings::heightVariation),
                Codec.FLOAT.fieldOf("roughness").forGetter(TerrainSettings::roughness),
                Codec.FLOAT.fieldOf("erosion").forGetter(TerrainSettings::erosion),
                Codec.FLOAT.fieldOf("continentalness").forGetter(TerrainSettings::continentalness),
                NoiseConfiguration.CODEC.fieldOf("noise").forGetter(TerrainSettings::noise),
                Codec.BOOL.optionalFieldOf("generate_caves", true).forGetter(TerrainSettings::generateCaves),
                Codec.BOOL.optionalFieldOf("generate_ravines", true).forGetter(TerrainSettings::generateRavines),
                SurfaceConfiguration.CODEC.fieldOf("surface").forGetter(TerrainSettings::surface)
            ).apply(instance, TerrainSettings::new)
        );
    }

    /**
     * Noise generation configuration
     */
    public record NoiseConfiguration(
        float temperatureScale,      // Temperature noise scale
        float humidityScale,         // Humidity noise scale
        float altitudeScale,         // Altitude noise scale
        float weirdnessScale,        // Weirdness noise scale
        int octaves,                 // Number of noise octaves
        float persistence,           // Noise persistence
        float lacunarity             // Noise lacunarity
    ) {
        public static final Codec<NoiseConfiguration> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                Codec.FLOAT.fieldOf("temperature_scale").forGetter(NoiseConfiguration::temperatureScale),
                Codec.FLOAT.fieldOf("humidity_scale").forGetter(NoiseConfiguration::humidityScale),
                Codec.FLOAT.fieldOf("altitude_scale").forGetter(NoiseConfiguration::altitudeScale),
                Codec.FLOAT.fieldOf("weirdness_scale").forGetter(NoiseConfiguration::weirdnessScale),
                Codec.INT.fieldOf("octaves").forGetter(NoiseConfiguration::octaves),
                Codec.FLOAT.fieldOf("persistence").forGetter(NoiseConfiguration::persistence),
                Codec.FLOAT.fieldOf("lacunarity").forGetter(NoiseConfiguration::lacunarity)
            ).apply(instance, NoiseConfiguration::new)
        );
    }

    /**
     * Surface block configuration
     */
    public record SurfaceConfiguration(
        ResourceLocation topBlock,       // Top surface block
        ResourceLocation subsurfaceBlock, // Subsurface block
        ResourceLocation deepBlock,      // Deep underground block
        int subsurfaceDepth,             // Depth of subsurface layer
        int deepDepth,                   // Depth to deep layer
        boolean hasLiquid,               // Whether surface has liquid
        ResourceLocation liquidBlock,    // Liquid block type
        int liquidLevel                  // Sea level for liquid
    ) {
        public static final Codec<SurfaceConfiguration> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                ResourceLocation.CODEC.fieldOf("top_block").forGetter(SurfaceConfiguration::topBlock),
                ResourceLocation.CODEC.fieldOf("subsurface_block").forGetter(SurfaceConfiguration::subsurfaceBlock),
                ResourceLocation.CODEC.fieldOf("deep_block").forGetter(SurfaceConfiguration::deepBlock),
                Codec.INT.fieldOf("subsurface_depth").forGetter(SurfaceConfiguration::subsurfaceDepth),
                Codec.INT.fieldOf("deep_depth").forGetter(SurfaceConfiguration::deepDepth),
                Codec.BOOL.optionalFieldOf("has_liquid", false).forGetter(SurfaceConfiguration::hasLiquid),
                ResourceLocation.CODEC.optionalFieldOf("liquid_block", ResourceLocation.withDefaultNamespace("water")).forGetter(SurfaceConfiguration::liquidBlock),
                Codec.INT.optionalFieldOf("liquid_level", 63).forGetter(SurfaceConfiguration::liquidLevel)
            ).apply(instance, SurfaceConfiguration::new)
        );
    }

    /**
     * Atmospheric rendering and effects
     */
    public record AtmosphericSettings(
        SkyConfiguration sky,            // Sky rendering settings
        FogConfiguration fog,            // Fog settings
        LightingConfiguration lighting,  // Lighting settings
        WeatherConfiguration weather,    // Weather patterns
        boolean hasWeather,              // Whether weather occurs
        float windStrength,              // Wind effects strength
        ParticleEffects particles        // Atmospheric particles
    ) {
        public static final Codec<AtmosphericSettings> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                SkyConfiguration.CODEC.fieldOf("sky").forGetter(AtmosphericSettings::sky),
                FogConfiguration.CODEC.fieldOf("fog").forGetter(AtmosphericSettings::fog),
                LightingConfiguration.CODEC.fieldOf("lighting").forGetter(AtmosphericSettings::lighting),
                WeatherConfiguration.CODEC.fieldOf("weather").forGetter(AtmosphericSettings::weather),
                Codec.BOOL.optionalFieldOf("has_weather", false).forGetter(AtmosphericSettings::hasWeather),
                Codec.FLOAT.optionalFieldOf("wind_strength", 0.0f).forGetter(AtmosphericSettings::windStrength),
                ParticleEffects.CODEC.fieldOf("particles").forGetter(AtmosphericSettings::particles)
            ).apply(instance, AtmosphericSettings::new)
        );
    }

    /**
     * Sky rendering configuration
     */
    public record SkyConfiguration(
        int skyColor,                    // RGB sky color
        int horizonColor,                // RGB horizon color
        int cloudColor,                  // RGB cloud color
        boolean hasClouds,               // Whether clouds render
        boolean hasSun,                  // Whether sun renders
        boolean hasMoon,                 // Whether moon renders
        boolean hasStars,                // Whether stars render
        float starBrightness,            // Star brightness multiplier
        AtmosphericRendering.StarVisibility starVisibility, // When stars are visible
        int sunSize,                     // Sun size multiplier
        int moonSize                     // Moon size multiplier
    ) {
        public static final Codec<SkyConfiguration> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                Codec.INT.fieldOf("sky_color").forGetter(SkyConfiguration::skyColor),
                Codec.INT.fieldOf("horizon_color").forGetter(SkyConfiguration::horizonColor),
                Codec.INT.fieldOf("cloud_color").forGetter(SkyConfiguration::cloudColor),
                Codec.BOOL.optionalFieldOf("has_clouds", true).forGetter(SkyConfiguration::hasClouds),
                Codec.BOOL.optionalFieldOf("has_sun", true).forGetter(SkyConfiguration::hasSun),
                Codec.BOOL.optionalFieldOf("has_moon", true).forGetter(SkyConfiguration::hasMoon),
                Codec.BOOL.optionalFieldOf("has_stars", true).forGetter(SkyConfiguration::hasStars),
                Codec.FLOAT.optionalFieldOf("star_brightness", 1.0f).forGetter(SkyConfiguration::starBrightness),
                Codec.STRING.optionalFieldOf("star_visibility", "night_only")
                    .xmap(s -> AtmosphericRendering.StarVisibility.valueOf(s.toUpperCase()),
                          v -> v.name().toLowerCase())
                    .forGetter(SkyConfiguration::starVisibility),
                Codec.INT.optionalFieldOf("sun_size", 1).forGetter(SkyConfiguration::sunSize),
                Codec.INT.optionalFieldOf("moon_size", 1).forGetter(SkyConfiguration::moonSize)
            ).apply(instance, SkyConfiguration::new)
        );
    }

    /**
     * Fog rendering configuration
     */
    public record FogConfiguration(
        int fogColor,                    // RGB fog color
        float fogDensity,                // Fog density (0.0-1.0)
        float fogStart,                  // Fog start distance
        float fogEnd,                    // Fog end distance
        boolean atmosphericFog,          // Whether fog varies by height
        float fogHeightFactor            // Height variation factor
    ) {
        public static final Codec<FogConfiguration> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                Codec.INT.fieldOf("fog_color").forGetter(FogConfiguration::fogColor),
                Codec.FLOAT.fieldOf("fog_density").forGetter(FogConfiguration::fogDensity),
                Codec.FLOAT.fieldOf("fog_start").forGetter(FogConfiguration::fogStart),
                Codec.FLOAT.fieldOf("fog_end").forGetter(FogConfiguration::fogEnd),
                Codec.BOOL.optionalFieldOf("atmospheric_fog", false).forGetter(FogConfiguration::atmosphericFog),
                Codec.FLOAT.optionalFieldOf("fog_height_factor", 1.0f).forGetter(FogConfiguration::fogHeightFactor)
            ).apply(instance, FogConfiguration::new)
        );
    }

    /**
     * Lighting configuration
     */
    public record LightingConfiguration(
        float ambientLight,              // Ambient light level (0.0-1.0)
        float sunlightMultiplier,        // Sunlight strength multiplier
        float moonlightMultiplier,       // Moonlight strength multiplier
        int lightColor,                  // RGB light color tint
        boolean hasDayNightCycle,        // Whether day/night cycle occurs
        float dayLength                  // Day length multiplier
    ) {
        public static final Codec<LightingConfiguration> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                Codec.FLOAT.fieldOf("ambient_light").forGetter(LightingConfiguration::ambientLight),
                Codec.FLOAT.fieldOf("sunlight_multiplier").forGetter(LightingConfiguration::sunlightMultiplier),
                Codec.FLOAT.fieldOf("moonlight_multiplier").forGetter(LightingConfiguration::moonlightMultiplier),
                Codec.INT.fieldOf("light_color").forGetter(LightingConfiguration::lightColor),
                Codec.BOOL.optionalFieldOf("has_day_night_cycle", true).forGetter(LightingConfiguration::hasDayNightCycle),
                Codec.FLOAT.optionalFieldOf("day_length", 1.0f).forGetter(LightingConfiguration::dayLength)
            ).apply(instance, LightingConfiguration::new)
        );
    }

    /**
     * Weather configuration
     */
    public record WeatherConfiguration(
        boolean canRain,                 // Whether rain can occur
        boolean canSnow,                 // Whether snow can occur
        boolean canStorm,                // Whether storms can occur
        float rainFrequency,             // Rain frequency (0.0-1.0)
        float stormFrequency,            // Storm frequency (0.0-1.0)
        float temperatureVariation,      // Temperature variation factor
        ResourceLocation precipitationBlock // Block type for precipitation
    ) {
        public static final Codec<WeatherConfiguration> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                Codec.BOOL.optionalFieldOf("can_rain", false).forGetter(WeatherConfiguration::canRain),
                Codec.BOOL.optionalFieldOf("can_snow", false).forGetter(WeatherConfiguration::canSnow),
                Codec.BOOL.optionalFieldOf("can_storm", false).forGetter(WeatherConfiguration::canStorm),
                Codec.FLOAT.optionalFieldOf("rain_frequency", 0.1f).forGetter(WeatherConfiguration::rainFrequency),
                Codec.FLOAT.optionalFieldOf("storm_frequency", 0.05f).forGetter(WeatherConfiguration::stormFrequency),
                Codec.FLOAT.optionalFieldOf("temperature_variation", 0.1f).forGetter(WeatherConfiguration::temperatureVariation),
                ResourceLocation.CODEC.optionalFieldOf("precipitation_block", ResourceLocation.withDefaultNamespace("water")).forGetter(WeatherConfiguration::precipitationBlock)
            ).apply(instance, WeatherConfiguration::new)
        );
    }

    /**
     * Atmospheric particle effects
     */
    public record ParticleEffects(
        boolean dustParticles,           // Dust/sand particles
        boolean smokeParticles,          // Smoke/gas particles
        boolean crystalParticles,        // Crystal/ice particles
        ResourceLocation dustColor,      // Dust particle color
        float particleDensity,           // Particle density (0.0-1.0)
        float particleSpeed              // Particle movement speed
    ) {
        public static final Codec<ParticleEffects> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                Codec.BOOL.optionalFieldOf("dust_particles", false).forGetter(ParticleEffects::dustParticles),
                Codec.BOOL.optionalFieldOf("smoke_particles", false).forGetter(ParticleEffects::smokeParticles),
                Codec.BOOL.optionalFieldOf("crystal_particles", false).forGetter(ParticleEffects::crystalParticles),
                ResourceLocation.CODEC.optionalFieldOf("dust_color", ResourceLocation.withDefaultNamespace("brown")).forGetter(ParticleEffects::dustColor),
                Codec.FLOAT.optionalFieldOf("particle_density", 0.1f).forGetter(ParticleEffects::particleDensity),
                Codec.FLOAT.optionalFieldOf("particle_speed", 1.0f).forGetter(ParticleEffects::particleSpeed)
            ).apply(instance, ParticleEffects::new)
        );
    }

    /**
     * Environmental hazards and effects
     */
    public record EnvironmentalSettings(
        HazardConfiguration hazards,     // Environmental hazards
        GravityConfiguration gravity,    // Gravity effects
        RadiationConfiguration radiation, // Radiation effects
        TemperatureConfiguration temperature, // Temperature effects
        boolean hasOxygen,               // Whether breathable oxygen exists
        float airPressure                // Atmospheric pressure
    ) {
        public static final Codec<EnvironmentalSettings> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                HazardConfiguration.CODEC.fieldOf("hazards").forGetter(EnvironmentalSettings::hazards),
                GravityConfiguration.CODEC.fieldOf("gravity").forGetter(EnvironmentalSettings::gravity),
                RadiationConfiguration.CODEC.fieldOf("radiation").forGetter(EnvironmentalSettings::radiation),
                TemperatureConfiguration.CODEC.fieldOf("temperature").forGetter(EnvironmentalSettings::temperature),
                Codec.BOOL.optionalFieldOf("has_oxygen", true).forGetter(EnvironmentalSettings::hasOxygen),
                Codec.FLOAT.optionalFieldOf("air_pressure", 1.0f).forGetter(EnvironmentalSettings::airPressure)
            ).apply(instance, EnvironmentalSettings::new)
        );
    }

    /**
     * Environmental hazard configuration
     */
    public record HazardConfiguration(
        boolean acidRain,                // Corrosive precipitation
        boolean solarFlares,             // Solar radiation bursts
        boolean sandstorms,              // Sand/dust storms
        boolean volcanicActivity,        // Volcanic eruptions
        boolean meteorShowers,           // Meteor impacts
        float hazardFrequency,           // Overall hazard frequency
        float hazardIntensity            // Hazard damage/effect intensity
    ) {
        public static final Codec<HazardConfiguration> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                Codec.BOOL.optionalFieldOf("acid_rain", false).forGetter(HazardConfiguration::acidRain),
                Codec.BOOL.optionalFieldOf("solar_flares", false).forGetter(HazardConfiguration::solarFlares),
                Codec.BOOL.optionalFieldOf("sandstorms", false).forGetter(HazardConfiguration::sandstorms),
                Codec.BOOL.optionalFieldOf("volcanic_activity", false).forGetter(HazardConfiguration::volcanicActivity),
                Codec.BOOL.optionalFieldOf("meteor_showers", false).forGetter(HazardConfiguration::meteorShowers),
                Codec.FLOAT.optionalFieldOf("hazard_frequency", 0.1f).forGetter(HazardConfiguration::hazardFrequency),
                Codec.FLOAT.optionalFieldOf("hazard_intensity", 1.0f).forGetter(HazardConfiguration::hazardIntensity)
            ).apply(instance, HazardConfiguration::new)
        );
    }

    /**
     * Gravity effects configuration
     */
    public record GravityConfiguration(
        float gravityMultiplier,         // Gravity strength multiplier
        boolean affectsMovement,         // Whether gravity affects player movement
        boolean affectsJumping,          // Whether gravity affects jumping
        boolean affectsFalling,          // Whether gravity affects fall damage
        float jumpBoostFactor,           // Jump height modification
        float fallDamageFactor           // Fall damage modification
    ) {
        public static final Codec<GravityConfiguration> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                Codec.FLOAT.fieldOf("gravity_multiplier").forGetter(GravityConfiguration::gravityMultiplier),
                Codec.BOOL.optionalFieldOf("affects_movement", true).forGetter(GravityConfiguration::affectsMovement),
                Codec.BOOL.optionalFieldOf("affects_jumping", true).forGetter(GravityConfiguration::affectsJumping),
                Codec.BOOL.optionalFieldOf("affects_falling", true).forGetter(GravityConfiguration::affectsFalling),
                Codec.FLOAT.optionalFieldOf("jump_boost_factor", 1.0f).forGetter(GravityConfiguration::jumpBoostFactor),
                Codec.FLOAT.optionalFieldOf("fall_damage_factor", 1.0f).forGetter(GravityConfiguration::fallDamageFactor)
            ).apply(instance, GravityConfiguration::new)
        );
    }

    /**
     * Radiation effects configuration
     */
    public record RadiationConfiguration(
        boolean hasRadiation,            // Whether radiation is present
        float radiationLevel,            // Radiation intensity
        boolean requiresProtection,      // Whether protection is needed
        float damageRate,                // Damage per tick from radiation
        boolean affectsEquipment,        // Whether radiation damages equipment
        float equipmentDamageRate        // Equipment damage rate
    ) {
        public static final Codec<RadiationConfiguration> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                Codec.BOOL.optionalFieldOf("has_radiation", false).forGetter(RadiationConfiguration::hasRadiation),
                Codec.FLOAT.optionalFieldOf("radiation_level", 0.0f).forGetter(RadiationConfiguration::radiationLevel),
                Codec.BOOL.optionalFieldOf("requires_protection", false).forGetter(RadiationConfiguration::requiresProtection),
                Codec.FLOAT.optionalFieldOf("damage_rate", 0.0f).forGetter(RadiationConfiguration::damageRate),
                Codec.BOOL.optionalFieldOf("affects_equipment", false).forGetter(RadiationConfiguration::affectsEquipment),
                Codec.FLOAT.optionalFieldOf("equipment_damage_rate", 0.0f).forGetter(RadiationConfiguration::equipmentDamageRate)
            ).apply(instance, RadiationConfiguration::new)
        );
    }

    /**
     * Temperature effects configuration
     */
    public record TemperatureConfiguration(
        float baseTemperature,           // Base temperature in Celsius
        float temperatureRange,          // Temperature variation range
        boolean extremeTemperatures,     // Whether extreme temps cause damage
        float coldDamageThreshold,       // Temperature for cold damage
        float heatDamageThreshold,       // Temperature for heat damage
        float temperatureDamageRate      // Damage rate from temperature
    ) {
        public static final Codec<TemperatureConfiguration> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                Codec.FLOAT.fieldOf("base_temperature").forGetter(TemperatureConfiguration::baseTemperature),
                Codec.FLOAT.fieldOf("temperature_range").forGetter(TemperatureConfiguration::temperatureRange),
                Codec.BOOL.optionalFieldOf("extreme_temperatures", false).forGetter(TemperatureConfiguration::extremeTemperatures),
                Codec.FLOAT.optionalFieldOf("cold_damage_threshold", -20.0f).forGetter(TemperatureConfiguration::coldDamageThreshold),
                Codec.FLOAT.optionalFieldOf("heat_damage_threshold", 50.0f).forGetter(TemperatureConfiguration::heatDamageThreshold),
                Codec.FLOAT.optionalFieldOf("temperature_damage_rate", 0.5f).forGetter(TemperatureConfiguration::temperatureDamageRate)
            ).apply(instance, TemperatureConfiguration::new)
        );
    }

    /**
     * Biome distribution configuration
     */
    public record BiomeDistribution(
        ResourceLocation primaryBiome,   // Main biome for the planet
        java.util.List<BiomeVariant> variants, // Biome variants
        float biomeSize,                 // Scale of biome regions
        float biomeVariation,            // Variation in biome distribution
        boolean hasOceans,               // Whether ocean biomes exist
        boolean hasPolarRegions          // Whether polar biomes exist
    ) {
        public static final Codec<BiomeDistribution> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                ResourceLocation.CODEC.fieldOf("primary_biome").forGetter(BiomeDistribution::primaryBiome),
                BiomeVariant.CODEC.listOf().optionalFieldOf("variants", java.util.List.of()).forGetter(BiomeDistribution::variants),
                Codec.FLOAT.optionalFieldOf("biome_size", 1.0f).forGetter(BiomeDistribution::biomeSize),
                Codec.FLOAT.optionalFieldOf("biome_variation", 0.5f).forGetter(BiomeDistribution::biomeVariation),
                Codec.BOOL.optionalFieldOf("has_oceans", false).forGetter(BiomeDistribution::hasOceans),
                Codec.BOOL.optionalFieldOf("has_polar_regions", false).forGetter(BiomeDistribution::hasPolarRegions)
            ).apply(instance, BiomeDistribution::new)
        );
    }

    /**
     * Biome variant configuration
     */
    public record BiomeVariant(
        ResourceLocation biome,          // Biome resource location
        float weight,                    // Spawn weight
        float temperatureMin,            // Minimum temperature for this biome
        float temperatureMax,            // Maximum temperature for this biome
        float humidityMin,               // Minimum humidity for this biome
        float humidityMax                // Maximum humidity for this biome
    ) {
        public static final Codec<BiomeVariant> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                ResourceLocation.CODEC.fieldOf("biome").forGetter(BiomeVariant::biome),
                Codec.FLOAT.fieldOf("weight").forGetter(BiomeVariant::weight),
                Codec.FLOAT.fieldOf("temperature_min").forGetter(BiomeVariant::temperatureMin),
                Codec.FLOAT.fieldOf("temperature_max").forGetter(BiomeVariant::temperatureMax),
                Codec.FLOAT.fieldOf("humidity_min").forGetter(BiomeVariant::humidityMin),
                Codec.FLOAT.fieldOf("humidity_max").forGetter(BiomeVariant::humidityMax)
            ).apply(instance, BiomeVariant::new)
        );
    }

    /**
     * Resource generation configuration
     */
    public record ResourceGeneration(
        java.util.List<OreConfiguration> ores, // Ore generation settings
        java.util.List<StructureConfiguration> structures, // Structure generation
        boolean generateVegetation,      // Whether to generate plants
        VegetationConfiguration vegetation, // Vegetation settings
        boolean generateDecorations,     // Whether to generate decorative features
        float resourceAbundance          // Overall resource abundance multiplier
    ) {
        public static final Codec<ResourceGeneration> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                OreConfiguration.CODEC.listOf().optionalFieldOf("ores", java.util.List.of()).forGetter(ResourceGeneration::ores),
                StructureConfiguration.CODEC.listOf().optionalFieldOf("structures", java.util.List.of()).forGetter(ResourceGeneration::structures),
                Codec.BOOL.optionalFieldOf("generate_vegetation", false).forGetter(ResourceGeneration::generateVegetation),
                VegetationConfiguration.CODEC.optionalFieldOf("vegetation",
                    new VegetationConfiguration(java.util.List.of(), 0.1f, 0.5f, false)).forGetter(ResourceGeneration::vegetation),
                Codec.BOOL.optionalFieldOf("generate_decorations", true).forGetter(ResourceGeneration::generateDecorations),
                Codec.FLOAT.optionalFieldOf("resource_abundance", 1.0f).forGetter(ResourceGeneration::resourceAbundance)
            ).apply(instance, ResourceGeneration::new)
        );
    }

    /**
     * Ore generation configuration
     */
    public record OreConfiguration(
        ResourceLocation oreBlock,       // Ore block type
        ResourceLocation replaceBlock,   // Block to replace
        int minHeight,                   // Minimum generation height
        int maxHeight,                   // Maximum generation height
        int veinSize,                    // Ore vein size
        float rarity,                    // Ore rarity (0.0-1.0)
        int maxVeinsPerChunk             // Maximum veins per chunk
    ) {
        public static final Codec<OreConfiguration> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                ResourceLocation.CODEC.fieldOf("ore_block").forGetter(OreConfiguration::oreBlock),
                ResourceLocation.CODEC.fieldOf("replace_block").forGetter(OreConfiguration::replaceBlock),
                Codec.INT.fieldOf("min_height").forGetter(OreConfiguration::minHeight),
                Codec.INT.fieldOf("max_height").forGetter(OreConfiguration::maxHeight),
                Codec.INT.fieldOf("vein_size").forGetter(OreConfiguration::veinSize),
                Codec.FLOAT.fieldOf("rarity").forGetter(OreConfiguration::rarity),
                Codec.INT.fieldOf("max_veins_per_chunk").forGetter(OreConfiguration::maxVeinsPerChunk)
            ).apply(instance, OreConfiguration::new)
        );
    }

    /**
     * Structure generation configuration
     */
    public record StructureConfiguration(
        ResourceLocation structure,      // Structure type
        float spawnChance,               // Spawn chance per chunk
        int minDistance,                 // Minimum distance between structures
        java.util.List<ResourceLocation> allowedBiomes // Biomes where structure can spawn
    ) {
        public static final Codec<StructureConfiguration> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                ResourceLocation.CODEC.fieldOf("structure").forGetter(StructureConfiguration::structure),
                Codec.FLOAT.fieldOf("spawn_chance").forGetter(StructureConfiguration::spawnChance),
                Codec.INT.fieldOf("min_distance").forGetter(StructureConfiguration::minDistance),
                ResourceLocation.CODEC.listOf().optionalFieldOf("allowed_biomes", java.util.List.of()).forGetter(StructureConfiguration::allowedBiomes)
            ).apply(instance, StructureConfiguration::new)
        );
    }

    /**
     * Vegetation generation configuration
     */
    public record VegetationConfiguration(
        java.util.List<ResourceLocation> plantTypes, // Types of plants to generate
        float density,                   // Plant density
        float diversity,                 // Plant diversity
        boolean requiresWater            // Whether plants require water
    ) {
        public static final Codec<VegetationConfiguration> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                ResourceLocation.CODEC.listOf().optionalFieldOf("plant_types", java.util.List.of()).forGetter(VegetationConfiguration::plantTypes),
                Codec.FLOAT.optionalFieldOf("density", 0.1f).forGetter(VegetationConfiguration::density),
                Codec.FLOAT.optionalFieldOf("diversity", 0.5f).forGetter(VegetationConfiguration::diversity),
                Codec.BOOL.optionalFieldOf("requires_water", false).forGetter(VegetationConfiguration::requiresWater)
            ).apply(instance, VegetationConfiguration::new)
        );
    }

    /**
     * Validate the generation settings
     */
    public boolean isValid() {
        return terrain != null &&
               atmosphere != null &&
               environment != null &&
               biomes != null &&
               resources != null &&
               terrain.baseHeight >= -64 && terrain.baseHeight <= 320 &&
               terrain.heightVariation >= 0 && terrain.heightVariation <= 256 &&
               terrain.roughness >= 0.0f && terrain.roughness <= 2.0f &&
               atmosphere.sky.skyColor >= 0 && atmosphere.sky.skyColor <= 0xFFFFFF &&
               environment.gravity.gravityMultiplier > 0.0f;
    }

    /**
     * Create default settings for Earth-like planets
     */
    public static PlanetGenerationSettings createEarthlike() {
        return new PlanetGenerationSettings(
            createDefaultTerrain(),
            createDefaultAtmosphere(),
            createDefaultEnvironment(),
            createDefaultBiomes(),
            createDefaultResources()
        );
    }

    /**
     * Create default settings for Moon-like planets
     */
    public static PlanetGenerationSettings createMoonlike() {
        return new PlanetGenerationSettings(
            createMoonTerrain(),
            createMoonAtmosphere(),
            createMoonEnvironment(),
            createMoonBiomes(),
            createMoonResources()
        );
    }

    /**
     * Create default settings for Mars-like planets
     */
    public static PlanetGenerationSettings createMarslike() {
        return new PlanetGenerationSettings(
            createMarsTerrain(),
            createMarsAtmosphere(),
            createMarsEnvironment(),
            createMarsBiomes(),
            createMarsResources()
        );
    }

    // Helper methods for default configurations
    private static TerrainSettings createDefaultTerrain() {
        return new TerrainSettings(
            64, 128, 1.0f, 0.3f, 0.5f,
            new NoiseConfiguration(1.0f, 1.0f, 1.0f, 1.0f, 4, 0.5f, 2.0f),
            true, true,
            new SurfaceConfiguration(
                ResourceLocation.withDefaultNamespace("grass_block"),
                ResourceLocation.withDefaultNamespace("dirt"),
                ResourceLocation.withDefaultNamespace("stone"),
                3, 10, true,
                ResourceLocation.withDefaultNamespace("water"), 63
            )
        );
    }

    private static AtmosphericSettings createDefaultAtmosphere() {
        return new AtmosphericSettings(
            new SkyConfiguration(0x87CEEB, 0xFDB813, 0xFFFFFF, true, true, true, true, 1.0f, AtmosphericRendering.StarVisibility.NIGHT_ONLY, 1, 1),
            new FogConfiguration(0xC0C0C0, 0.1f, 16.0f, 128.0f, false, 1.0f),
            new LightingConfiguration(0.0f, 1.0f, 0.25f, 0xFFFFFF, true, 1.0f),
            new WeatherConfiguration(true, true, true, 0.1f, 0.05f, 0.1f, ResourceLocation.withDefaultNamespace("water")),
            true, 0.0f,
            new ParticleEffects(false, false, false, ResourceLocation.withDefaultNamespace("brown"), 0.0f, 1.0f)
        );
    }

    private static EnvironmentalSettings createDefaultEnvironment() {
        return new EnvironmentalSettings(
            new HazardConfiguration(false, false, false, false, false, 0.0f, 1.0f),
            new GravityConfiguration(1.0f, true, true, true, 1.0f, 1.0f),
            new RadiationConfiguration(false, 0.0f, false, 0.0f, false, 0.0f),
            new TemperatureConfiguration(15.0f, 30.0f, false, -20.0f, 50.0f, 0.5f),
            true, 1.0f
        );
    }

    private static BiomeDistribution createDefaultBiomes() {
        return new BiomeDistribution(
            ResourceLocation.withDefaultNamespace("plains"),
            java.util.List.of(),
            1.0f, 0.5f, true, true
        );
    }

    private static ResourceGeneration createDefaultResources() {
        return new ResourceGeneration(
            java.util.List.of(),
            java.util.List.of(),
            true,
            new VegetationConfiguration(java.util.List.of(), 0.5f, 0.8f, false),
            true, 1.0f
        );
    }

    // Moon-specific configurations
    private static TerrainSettings createMoonTerrain() {
        return new TerrainSettings(
            64, 64, 0.8f, 0.1f, 0.8f,
            new NoiseConfiguration(0.5f, 0.2f, 1.2f, 0.3f, 3, 0.4f, 2.2f),
            false, false,
            new SurfaceConfiguration(
                ResourceLocation.fromNamespaceAndPath("adastramekanized", "moon_dust"),
                ResourceLocation.fromNamespaceAndPath("adastramekanized", "moon_rock"),
                ResourceLocation.fromNamespaceAndPath("adastramekanized", "moon_stone"),
                2, 8, false,
                ResourceLocation.withDefaultNamespace("air"), 0
            )
        );
    }

    private static AtmosphericSettings createMoonAtmosphere() {
        return new AtmosphericSettings(
            new SkyConfiguration(0x000000, 0x101010, 0x000000, false, true, false, true, 3.0f, AtmosphericRendering.StarVisibility.CONSTANT, 1, 0),
            new FogConfiguration(0x202020, 0.0f, 256.0f, 512.0f, false, 1.0f),
            new LightingConfiguration(0.0f, 1.5f, 0.0f, 0xFFFFFF, true, 1.0f),
            new WeatherConfiguration(false, false, false, 0.0f, 0.0f, 0.0f, ResourceLocation.withDefaultNamespace("air")),
            false, 0.0f,
            new ParticleEffects(true, false, false, ResourceLocation.withDefaultNamespace("light_gray"), 0.05f, 0.5f)
        );
    }

    private static EnvironmentalSettings createMoonEnvironment() {
        return new EnvironmentalSettings(
            new HazardConfiguration(false, true, false, false, true, 0.2f, 2.0f),
            new GravityConfiguration(0.17f, true, true, true, 6.0f, 0.17f),
            new RadiationConfiguration(true, 0.3f, true, 0.1f, true, 0.05f),
            new TemperatureConfiguration(-173.0f, 250.0f, true, -180.0f, 120.0f, 1.0f),
            false, 0.0f
        );
    }

    private static BiomeDistribution createMoonBiomes() {
        return new BiomeDistribution(
            ResourceLocation.fromNamespaceAndPath("adastramekanized", "moon_plains"),
            java.util.List.of(),
            2.0f, 0.1f, false, false
        );
    }

    private static ResourceGeneration createMoonResources() {
        return new ResourceGeneration(
            java.util.List.of(
                new OreConfiguration(
                    ResourceLocation.fromNamespaceAndPath("adastramekanized", "moon_ore"),
                    ResourceLocation.fromNamespaceAndPath("adastramekanized", "moon_stone"),
                    0, 64, 8, 0.8f, 4
                )
            ),
            java.util.List.of(),
            false,
            new VegetationConfiguration(java.util.List.of(), 0.0f, 0.0f, false),
            false, 2.0f
        );
    }

    // Mars-specific configurations
    private static TerrainSettings createMarsTerrain() {
        return new TerrainSettings(
            64, 96, 1.2f, 0.5f, 0.7f,
            new NoiseConfiguration(0.8f, 0.3f, 1.1f, 0.8f, 4, 0.6f, 1.8f),
            true, true,
            new SurfaceConfiguration(
                ResourceLocation.fromNamespaceAndPath("adastramekanized", "mars_sand"),
                ResourceLocation.fromNamespaceAndPath("adastramekanized", "mars_rock"),
                ResourceLocation.fromNamespaceAndPath("adastramekanized", "mars_stone"),
                4, 12, false,
                ResourceLocation.withDefaultNamespace("air"), 0
            )
        );
    }

    private static AtmosphericSettings createMarsAtmosphere() {
        return new AtmosphericSettings(
            new SkyConfiguration(0xCD853F, 0xA0522D, 0x8B4513, false, true, true, true, 1.5f, AtmosphericRendering.StarVisibility.NIGHT_ONLY, 1, 1),
            new FogConfiguration(0xDEB887, 0.3f, 32.0f, 192.0f, true, 0.8f),
            new LightingConfiguration(0.05f, 0.8f, 0.15f, 0xFFDEB3, true, 1.0f),
            new WeatherConfiguration(false, false, true, 0.0f, 0.15f, 0.2f, ResourceLocation.withDefaultNamespace("sand")),
            true, 0.3f,
            new ParticleEffects(true, false, false, ResourceLocation.withDefaultNamespace("orange"), 0.2f, 1.2f)
        );
    }

    private static EnvironmentalSettings createMarsEnvironment() {
        return new EnvironmentalSettings(
            new HazardConfiguration(false, false, true, false, false, 0.15f, 1.5f),
            new GravityConfiguration(0.38f, true, true, true, 2.6f, 0.38f),
            new RadiationConfiguration(true, 0.2f, true, 0.05f, false, 0.0f),
            new TemperatureConfiguration(-63.0f, 80.0f, true, -80.0f, 20.0f, 0.8f),
            false, 0.01f
        );
    }

    private static BiomeDistribution createMarsBiomes() {
        return new BiomeDistribution(
            ResourceLocation.fromNamespaceAndPath("adastramekanized", "mars_plains"),
            java.util.List.of(
                new BiomeVariant(
                    ResourceLocation.fromNamespaceAndPath("adastramekanized", "mars_desert"),
                    0.7f, -70.0f, -50.0f, 0.0f, 0.2f
                ),
                new BiomeVariant(
                    ResourceLocation.fromNamespaceAndPath("adastramekanized", "mars_polar"),
                    0.1f, -80.0f, -70.0f, 0.0f, 0.1f
                )
            ),
            1.5f, 0.3f, false, true
        );
    }

    private static ResourceGeneration createMarsResources() {
        return new ResourceGeneration(
            java.util.List.of(
                new OreConfiguration(
                    ResourceLocation.fromNamespaceAndPath("adastramekanized", "mars_iron_ore"),
                    ResourceLocation.fromNamespaceAndPath("adastramekanized", "mars_stone"),
                    0, 80, 6, 0.6f, 3
                )
            ),
            java.util.List.of(),
            false,
            new VegetationConfiguration(java.util.List.of(), 0.0f, 0.0f, false),
            true, 1.2f
        );
    }
}