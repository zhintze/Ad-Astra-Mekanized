package com.hecookin.adastramekanized.api.planets.atmosphere;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

/**
 * Configuration for planetary atmospheric rendering effects.
 * Based on Ad Astra's atmospheric rendering system.
 */
public record AtmosphericRendering(
    SkyConfiguration sky,
    FogConfiguration fog,
    CelestialBodies celestialBodies,
    WeatherConfiguration weather,
    ParticleConfiguration particles
) {

    public static final Codec<AtmosphericRendering> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            SkyConfiguration.CODEC.fieldOf("sky").forGetter(AtmosphericRendering::sky),
            FogConfiguration.CODEC.fieldOf("fog").forGetter(AtmosphericRendering::fog),
            CelestialBodies.CODEC.fieldOf("celestial_bodies").forGetter(AtmosphericRendering::celestialBodies),
            WeatherConfiguration.CODEC.fieldOf("weather").forGetter(AtmosphericRendering::weather),
            ParticleConfiguration.CODEC.fieldOf("particles").forGetter(AtmosphericRendering::particles)
        ).apply(instance, AtmosphericRendering::new)
    );

    /**
     * Sky rendering configuration
     */
    public record SkyConfiguration(
        int skyColor,           // RGB hex color for sky
        int sunriseColor,       // RGB hex color for sunrise/sunset
        boolean customSky,      // Whether to use custom sky rendering
        boolean hasStars,       // Whether stars are visible
        int starCount,          // Number of stars to render
        float starBrightness,   // Star brightness multiplier
        Optional<ResourceLocation> skyTexture  // Custom sky texture
    ) {
        public static final Codec<SkyConfiguration> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                Codec.INT.fieldOf("sky_color").forGetter(SkyConfiguration::skyColor),
                Codec.INT.fieldOf("sunrise_color").forGetter(SkyConfiguration::sunriseColor),
                Codec.BOOL.fieldOf("custom_sky").forGetter(SkyConfiguration::customSky),
                Codec.BOOL.fieldOf("has_stars").forGetter(SkyConfiguration::hasStars),
                Codec.INT.fieldOf("star_count").forGetter(SkyConfiguration::starCount),
                Codec.FLOAT.fieldOf("star_brightness").forGetter(SkyConfiguration::starBrightness),
                ResourceLocation.CODEC.optionalFieldOf("sky_texture").forGetter(SkyConfiguration::skyTexture)
            ).apply(instance, SkyConfiguration::new)
        );
    }

    /**
     * Fog rendering configuration
     */
    public record FogConfiguration(
        int fogColor,           // RGB hex color for fog
        boolean hasFog,         // Whether fog is present
        float fogDensity,       // Fog density (0.0 - 1.0)
        float nearPlane,        // Near fog plane distance
        float farPlane          // Far fog plane distance
    ) {
        public static final Codec<FogConfiguration> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                Codec.INT.fieldOf("fog_color").forGetter(FogConfiguration::fogColor),
                Codec.BOOL.fieldOf("has_fog").forGetter(FogConfiguration::hasFog),
                Codec.FLOAT.fieldOf("fog_density").forGetter(FogConfiguration::fogDensity),
                Codec.FLOAT.fieldOf("near_plane").forGetter(FogConfiguration::nearPlane),
                Codec.FLOAT.fieldOf("far_plane").forGetter(FogConfiguration::farPlane)
            ).apply(instance, FogConfiguration::new)
        );
    }

    /**
     * Celestial bodies configuration (sun, moons, planets)
     */
    public record CelestialBodies(
        SunConfiguration sun,
        List<MoonConfiguration> moons,
        List<PlanetConfiguration> visiblePlanets
    ) {
        public static final Codec<CelestialBodies> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                SunConfiguration.CODEC.fieldOf("sun").forGetter(CelestialBodies::sun),
                MoonConfiguration.CODEC.listOf().fieldOf("moons").forGetter(CelestialBodies::moons),
                PlanetConfiguration.CODEC.listOf().fieldOf("visible_planets").forGetter(CelestialBodies::visiblePlanets)
            ).apply(instance, CelestialBodies::new)
        );
    }

    /**
     * Sun rendering configuration
     */
    public record SunConfiguration(
        Optional<ResourceLocation> texture,  // Custom sun texture
        float scale,                        // Sun size scale
        int color,                          // Sun color tint
        boolean visible                     // Whether sun is visible
    ) {
        public static final Codec<SunConfiguration> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                ResourceLocation.CODEC.optionalFieldOf("texture").forGetter(SunConfiguration::texture),
                Codec.FLOAT.fieldOf("scale").forGetter(SunConfiguration::scale),
                Codec.INT.fieldOf("color").forGetter(SunConfiguration::color),
                Codec.BOOL.fieldOf("visible").forGetter(SunConfiguration::visible)
            ).apply(instance, SunConfiguration::new)
        );
    }

    /**
     * Moon/satellite rendering configuration
     */
    public record MoonConfiguration(
        ResourceLocation texture,     // Moon texture
        float scale,                 // Moon size scale
        int color,                   // Moon color tint
        float orbitPhase,           // Current orbital phase (0.0 - 1.0)
        boolean visible             // Whether moon is visible
    ) {
        public static final Codec<MoonConfiguration> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                ResourceLocation.CODEC.fieldOf("texture").forGetter(MoonConfiguration::texture),
                Codec.FLOAT.fieldOf("scale").forGetter(MoonConfiguration::scale),
                Codec.INT.fieldOf("color").forGetter(MoonConfiguration::color),
                Codec.FLOAT.fieldOf("orbit_phase").forGetter(MoonConfiguration::orbitPhase),
                Codec.BOOL.fieldOf("visible").forGetter(MoonConfiguration::visible)
            ).apply(instance, MoonConfiguration::new)
        );
    }

    /**
     * Visible planet rendering configuration
     */
    public record PlanetConfiguration(
        ResourceLocation texture,     // Planet texture
        float scale,                 // Planet size scale
        int color,                   // Planet color tint
        float distance,             // Distance modifier for visibility
        boolean visible             // Whether planet is visible
    ) {
        public static final Codec<PlanetConfiguration> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                ResourceLocation.CODEC.fieldOf("texture").forGetter(PlanetConfiguration::texture),
                Codec.FLOAT.fieldOf("scale").forGetter(PlanetConfiguration::scale),
                Codec.INT.fieldOf("color").forGetter(PlanetConfiguration::color),
                Codec.FLOAT.fieldOf("distance").forGetter(PlanetConfiguration::distance),
                Codec.BOOL.fieldOf("visible").forGetter(PlanetConfiguration::visible)
            ).apply(instance, PlanetConfiguration::new)
        );
    }

    /**
     * Weather and cloud configuration
     */
    public record WeatherConfiguration(
        boolean hasClouds,          // Whether clouds are present
        boolean hasRain,            // Whether rain can occur
        boolean hasSnow,            // Whether snow can occur
        boolean hasStorms,          // Whether storms can occur
        float rainAcidity,          // Rain acidity (for Venus-like planets)
        Optional<ResourceLocation> cloudTexture  // Custom cloud texture
    ) {
        public static final Codec<WeatherConfiguration> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                Codec.BOOL.fieldOf("has_clouds").forGetter(WeatherConfiguration::hasClouds),
                Codec.BOOL.fieldOf("has_rain").forGetter(WeatherConfiguration::hasRain),
                Codec.BOOL.fieldOf("has_snow").forGetter(WeatherConfiguration::hasSnow),
                Codec.BOOL.fieldOf("has_storms").forGetter(WeatherConfiguration::hasStorms),
                Codec.FLOAT.fieldOf("rain_acidity").forGetter(WeatherConfiguration::rainAcidity),
                ResourceLocation.CODEC.optionalFieldOf("cloud_texture").forGetter(WeatherConfiguration::cloudTexture)
            ).apply(instance, WeatherConfiguration::new)
        );
    }

    /**
     * Atmospheric particle effects configuration
     */
    public record ParticleConfiguration(
        boolean hasDust,            // Dust particles (Mars, desert planets)
        boolean hasAsh,             // Ash particles (volcanic planets)
        boolean hasSpores,          // Spore particles (jungle planets)
        boolean hasSnowfall,        // Snow particles (ice planets)
        float particleDensity,      // Particle density multiplier
        int particleColor           // Particle color tint
    ) {
        public static final Codec<ParticleConfiguration> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                Codec.BOOL.fieldOf("has_dust").forGetter(ParticleConfiguration::hasDust),
                Codec.BOOL.fieldOf("has_ash").forGetter(ParticleConfiguration::hasAsh),
                Codec.BOOL.fieldOf("has_spores").forGetter(ParticleConfiguration::hasSpores),
                Codec.BOOL.fieldOf("has_snowfall").forGetter(ParticleConfiguration::hasSnowfall),
                Codec.FLOAT.fieldOf("particle_density").forGetter(ParticleConfiguration::particleDensity),
                Codec.INT.fieldOf("particle_color").forGetter(ParticleConfiguration::particleColor)
            ).apply(instance, ParticleConfiguration::new)
        );
    }

    /**
     * Create default atmospheric rendering for Earth-like planets
     */
    public static AtmosphericRendering createDefault() {
        return new AtmosphericRendering(
            new SkyConfiguration(0x78A7FF, 0xFFA500, false, true, 1500, 1.0f, Optional.empty()),
            new FogConfiguration(0xC0D8FF, true, 0.3f, 32.0f, 256.0f),
            new CelestialBodies(
                new SunConfiguration(Optional.empty(), 1.0f, 0xFFFFFF, true),
                List.of(),
                List.of()
            ),
            new WeatherConfiguration(true, true, true, true, 0.0f, Optional.empty()),
            new ParticleConfiguration(false, false, false, false, 1.0f, 0xFFFFFF)
        );
    }

    /**
     * Create atmospheric rendering for airless bodies (Moon, Mercury)
     */
    public static AtmosphericRendering createAirless(int skyColor, int fogColor) {
        return new AtmosphericRendering(
            new SkyConfiguration(skyColor, skyColor, true, true, 13000, 0.6f, Optional.empty()),
            new FogConfiguration(fogColor, true, 0.1f, 16.0f, 128.0f),
            new CelestialBodies(
                new SunConfiguration(
                    Optional.of(ResourceLocation.fromNamespaceAndPath("ad_astra", "textures/sky/sun.png")),
                    9.0f, 0xFFFFFF, true
                ),
                List.of(),
                List.of()
            ),
            new WeatherConfiguration(false, false, false, false, 0.0f, Optional.empty()),
            new ParticleConfiguration(false, false, false, false, 0.0f, 0xFFFFFF)
        );
    }

    /**
     * Create atmospheric rendering for Mars-like planets
     */
    public static AtmosphericRendering createMarsLike(int skyColor, int fogColor) {
        return new AtmosphericRendering(
            new SkyConfiguration(skyColor, 0xd85f33, true, true, 8000, 0.8f, Optional.empty()),
            new FogConfiguration(fogColor, true, 0.4f, 24.0f, 192.0f),
            new CelestialBodies(
                new SunConfiguration(Optional.empty(), 0.6f, 0xFFE4B5, true),
                List.of(),
                List.of()
            ),
            new WeatherConfiguration(false, false, false, true, 0.0f, Optional.empty()), // No clouds as requested
            new ParticleConfiguration(true, false, false, false, 0.8f, 0xD2691E)
        );
    }

    /**
     * Create atmospheric rendering for Venus-like planets
     */
    public static AtmosphericRendering createVenusLike(int skyColor, int fogColor) {
        return new AtmosphericRendering(
            new SkyConfiguration(skyColor, 0xFF6600, true, false, 0, 0.0f, Optional.empty()),
            new FogConfiguration(fogColor, true, 0.9f, 8.0f, 64.0f),
            new CelestialBodies(
                new SunConfiguration(Optional.empty(), 0.3f, 0xFFCC00, false), // Sun barely visible
                List.of(),
                List.of()
            ),
            new WeatherConfiguration(true, true, false, true, 8.5f, Optional.empty()), // Acid rain
            new ParticleConfiguration(false, true, false, false, 1.2f, 0xFFA500)
        );
    }

    /**
     * Create atmospheric rendering for ice planets
     */
    public static AtmosphericRendering createIcePlanet(int skyColor, int fogColor) {
        return new AtmosphericRendering(
            new SkyConfiguration(skyColor, 0xB0E0E6, true, true, 2000, 1.2f, Optional.empty()),
            new FogConfiguration(fogColor, true, 0.5f, 20.0f, 160.0f),
            new CelestialBodies(
                new SunConfiguration(Optional.empty(), 0.8f, 0xE6F3FF, true),
                List.of(),
                List.of()
            ),
            new WeatherConfiguration(true, false, true, true, 0.0f, Optional.empty()),
            new ParticleConfiguration(false, false, false, true, 1.5f, 0xF0F8FF)
        );
    }
}