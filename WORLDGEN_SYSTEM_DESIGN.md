# Ad Astra Mekanized - Tectonic-Based World Generation System Design

**Design Version**: 1.0
**Date**: November 2025
**Status**: Implementation Ready
**Timeline**: 6-8 weeks (Robust Foundation)

---

## 1. Executive Summary

### Requirements Confirmed

✅ **Integration Approach**: Use Tectonic as a required dependency
✅ **Biome Strategy**: 3-10 custom biomes per planet
✅ **API Design**: Extend existing PlanetMaker with Tectonic integration
✅ **Configuration**: Developer-friendly (compile-time in Java)
✅ **Noise System**: Use Tectonic's noise system directly
✅ **Biome Distribution**: Simple zone-based (noise thresholds)
✅ **Compatibility**: TerraBlender required, existing worlds will break
✅ **Success Criteria**: Tectonic-style biomes on planets + easy extensibility + full documentation

### What We're Building

A **Tectonic integration layer** for Ad Astra Mekanized that:
1. Adds Tectonic as a required dependency
2. Extends PlanetMaker API with Tectonic density function configuration
3. Creates planet-specific custom biomes (Moon: 5 biomes, Mars: 7 biomes, Venus: 4 biomes, etc.)
4. Uses zone-based biome distribution (altitude, region type, features)
5. Leverages Tectonic's noise system with planet-specific customizations
6. Provides easy-to-follow templates for adding new planets

---

## 2. System Architecture

### 2.1 Dependency Structure

```
Ad Astra Mekanized
    ├── NeoForge 1.21.1
    ├── Mekanism (optional integration)
    ├── Immersive Engineering (optional integration)
    ├── TerraBlender (required) ← For biome registration
    └── Tectonic (required) ← For world generation system
```

**neoforge.mods.toml additions:**
```toml
[[dependencies.adastramekanized]]
    modId = "tectonic"
    type = "required"
    versionRange = "[3.0.0,)"
    ordering = "BEFORE"
    side = "BOTH"

[[dependencies.adastramekanized]]
    modId = "terrablender"
    type = "required"
    versionRange = "[1.21.1-4.0.0,)"
    ordering = "BEFORE"
    side = "BOTH"
```

### 2.2 Package Structure

```
com.hecookin.adastramekanized/
├── worldgen/
│   ├── biome/
│   │   ├── PlanetBiomeRegistry.java              # Central biome registration
│   │   ├── PlanetBiomeProvider.java              # TerraBlender integration
│   │   ├── moon/
│   │   │   ├── MoonBiomes.java                   # 5 Moon biomes
│   │   │   └── MoonBiomeDistribution.java        # Zone logic
│   │   ├── mars/
│   │   │   ├── MarsBiomes.java                   # 7 Mars biomes
│   │   │   └── MarsBiomeDistribution.java        # Zone logic
│   │   ├── venus/
│   │   │   ├── VenusBiomes.java                  # 4 Venus biomes
│   │   │   └── VenusBiomeDistribution.java       # Zone logic
│   │   └── [other planets...]
│   ├── densityfunction/
│   │   ├── TectonicIntegration.java              # Tectonic helper utilities
│   │   ├── PlanetDensityFunctions.java           # Custom planet density functions
│   │   ├── moon/
│   │   │   ├── LunarCraterDensity.java           # Crater generation
│   │   │   ├── LunarMariaDensity.java            # Maria (flat plains)
│   │   │   └── LunarRegolithDensity.java         # Regolith depth
│   │   ├── mars/
│   │   │   ├── MartianCanyonDensity.java         # Deep canyons
│   │   │   ├── MartianVolcanoDensity.java        # Massive volcanoes
│   │   │   ├── MartianPolarCapDensity.java       # Ice caps
│   │   │   └── MartianDuneDensity.java           # Dust dunes
│   │   ├── venus/
│   │   │   ├── VenusAtmosphereDensity.java       # Atmospheric pressure
│   │   │   └── VenusVolcanicDensity.java         # Volcanic features
│   │   └── mercury/
│   │       ├── MercuryCraterDensity.java         # Extreme craters
│   │       ├── MercuryScarpDensity.java          # Cliff faces
│   │       └── MercuryBasinDensity.java          # Basin features
│   ├── surfacerules/
│   │   ├── PlanetSurfaceRuleBuilder.java         # Helper for surface rules
│   │   └── [planet-specific surface rule files]
│   └── PlanetWorldGenRegistry.java               # Central registration point
├── planet/
│   ├── PlanetMaker.java                          # EXTENDED with new methods
│   ├── PlanetGenerationRunner.java               # UPDATED with Tectonic config
│   └── PlanetRegistry.java                       # Existing registry
└── AdAstraMekanized.java                         # Main mod class
```

### 2.3 Data Structure

```
src/main/resources/
└── data/adastramekanized/
    └── worldgen/
        ├── biome/
        │   ├── moon/
        │   │   ├── lunar_highlands.json
        │   │   ├── lunar_maria.json
        │   │   ├── lunar_crater_rim.json
        │   │   ├── lunar_crater_floor.json
        │   │   └── lunar_polar.json
        │   ├── mars/
        │   │   ├── martian_highlands.json
        │   │   ├── martian_canyon.json
        │   │   ├── martian_volcanic.json
        │   │   ├── martian_polar_ice.json
        │   │   ├── martian_dunes.json
        │   │   ├── martian_lowlands.json
        │   │   └── martian_channel.json
        │   └── [other planets...]
        ├── density_function/
        │   ├── moon/
        │   │   ├── final_density.json              # References Tectonic + custom
        │   │   ├── craters.json
        │   │   ├── maria.json
        │   │   └── base_terrain.json
        │   └── [other planets...]
        ├── noise_settings/
        │   ├── moon.json
        │   ├── mars.json
        │   └── [other planets...]
        └── configured_feature/
            └── [planet-specific features]
```

---

## 3. Extended PlanetMaker API

### 3.1 New API Methods

```java
public class PlanetMaker {

    // ========== EXISTING METHODS (Keep as-is) ==========
    public PlanetMaker continentalScale(float scale) { ... }
    public PlanetMaker erosionScale(float scale) { ... }
    public PlanetMaker surfaceBlock(String blockId) { ... }
    // ... all existing methods ...

    // ========== NEW TECTONIC INTEGRATION METHODS ==========

    /**
     * Configure crater generation using Tectonic density functions
     */
    public PlanetMaker withCraters(CraterConfig config) {
        this.craterConfig = config;
        return this;
    }

    /**
     * Configure canyon/valley generation
     */
    public PlanetMaker withCanyons(CanyonConfig config) {
        this.canyonConfig = config;
        return this;
    }

    /**
     * Configure volcanic features
     */
    public PlanetMaker withVolcanoes(VolcanoConfig config) {
        this.volcanoConfig = config;
        return this;
    }

    /**
     * Configure polar ice caps
     */
    public PlanetMaker withPolarCaps(PolarCapConfig config) {
        this.polarCapConfig = config;
        return this;
    }

    /**
     * Configure dune fields
     */
    public PlanetMaker withDunes(DuneConfig config) {
        this.duneConfig = config;
        return this;
    }

    /**
     * Configure maria (flat plains)
     */
    public PlanetMaker withMaria(MariaConfig config) {
        this.mariaConfig = config;
        return this;
    }

    /**
     * Configure atmospheric effects on terrain
     */
    public PlanetMaker withAtmosphericEffects(AtmosphereConfig config) {
        this.atmosphereConfig = config;
        return this;
    }

    /**
     * Configure scarps (cliff faces)
     */
    public PlanetMaker withScarps(ScarpConfig config) {
        this.scarpConfig = config;
        return this;
    }

    /**
     * Configure basin features
     */
    public PlanetMaker withBasins(BasinConfig config) {
        this.basinConfig = config;
        return this;
    }

    /**
     * Configure regolith depth variation
     */
    public PlanetMaker withRegolithDepth(RegolithConfig config) {
        this.regolithConfig = config;
        return this;
    }

    /**
     * Add a custom biome to this planet
     */
    public PlanetMaker addBiome(String biomeName, BiomeConfig config) {
        this.biomes.put(biomeName, config);
        return this;
    }

    /**
     * Configure biome distribution zones
     */
    public PlanetMaker withBiomeZones(BiomeZoneConfig config) {
        this.biomeZoneConfig = config;
        return this;
    }

    /**
     * Use Tectonic's noise router with custom overrides
     */
    public PlanetMaker withTectonicNoise(TectonicNoiseConfig config) {
        this.tectonicNoiseConfig = config;
        return this;
    }

    /**
     * Configure surface rules for biomes
     */
    public PlanetMaker withSurfaceRules(SurfaceRuleConfig config) {
        this.surfaceRuleConfig = config;
        return this;
    }
}
```

### 3.2 Configuration Classes

```java
/**
 * Configuration for crater generation
 */
public class CraterConfig {
    private double frequency;           // How often craters appear (0.0-1.0)
    private double minSize;             // Minimum crater radius in blocks
    private double maxSize;             // Maximum crater radius in blocks
    private double depth;               // Depth multiplier (1.0 = normal depth)
    private double rimHeight;           // Rim height multiplier (1.0 = normal rim)
    private double rimWidth;            // Rim width as fraction of radius (0.0-1.0)
    private boolean varySize;           // Allow size variation
    private String distributionType;    // "uniform", "clustered", "sparse"

    public static CraterConfig realistic() {
        return new CraterConfig()
            .frequency(0.02)
            .minSize(10)
            .maxSize(100)
            .depth(1.0)
            .rimHeight(0.15)
            .rimWidth(0.2)
            .varySize(true)
            .distributionType("clustered");
    }

    public static CraterConfig dramatic() {
        return new CraterConfig()
            .frequency(0.05)
            .minSize(20)
            .maxSize(200)
            .depth(2.0)
            .rimHeight(0.3)
            .rimWidth(0.25)
            .varySize(true)
            .distributionType("uniform");
    }
}

/**
 * Configuration for canyon generation
 */
public class CanyonConfig {
    private double frequency;           // Canyon network density
    private double minDepth;            // Minimum canyon depth in blocks
    private double maxDepth;            // Maximum canyon depth in blocks
    private double width;               // Average canyon width in blocks
    private double sinuosity;           // How winding canyons are (0.0-1.0)
    private boolean branches;           // Allow canyon branches
    private String pattern;             // "radial", "dendritic", "parallel"

    public static CanyonConfig vallesMarineris() {
        return new CanyonConfig()
            .frequency(0.01)
            .minDepth(50)
            .maxDepth(150)
            .width(200)
            .sinuosity(0.3)
            .branches(true)
            .pattern("radial");
    }
}

/**
 * Configuration for volcanic features
 */
public class VolcanoConfig {
    private double frequency;           // Volcano density
    private double minHeight;           // Minimum volcano height
    private double maxHeight;           // Maximum volcano height
    private double slopeAngle;          // Slope steepness (degrees)
    private double calderaSize;         // Caldera radius as fraction of base
    private boolean lavaFlows;          // Generate lava flows
    private String type;                // "shield", "stratovolcano", "cinder_cone"

    public static VolcanoConfig olympusMons() {
        return new VolcanoConfig()
            .frequency(0.005)
            .minHeight(100)
            .maxHeight(250)
            .slopeAngle(5.0)
            .calderaSize(0.15)
            .lavaFlows(true)
            .type("shield");
    }
}

/**
 * Configuration for polar ice caps
 */
public class PolarCapConfig {
    private double northCapRadius;      // Radius of north polar cap
    private double southCapRadius;      // Radius of south polar cap
    private double thickness;           // Ice thickness
    private boolean layered;            // Show layered structure
    private String material;            // "ice", "dry_ice", "mixed"

    public static PolarCapConfig mars() {
        return new PolarCapConfig()
            .northCapRadius(500)
            .southCapRadius(600)
            .thickness(20)
            .layered(true)
            .material("mixed");
    }
}

/**
 * Configuration for dune fields
 */
public class DuneConfig {
    private double frequency;           // Dune field density
    private double height;              // Average dune height
    private double wavelength;          // Distance between dune crests
    private String orientation;         // "north_south", "east_west", "variable"
    private String type;                // "barchan", "linear", "star"

    public static DuneConfig martian() {
        return new DuneConfig()
            .frequency(0.1)
            .height(15)
            .wavelength(80)
            .orientation("variable")
            .type("barchan");
    }
}

/**
 * Configuration for maria (flat plains)
 */
public class MariaConfig {
    private double frequency;           // How much of planet is maria
    private double flatness;            // How flat (0.0-1.0, 1.0 = perfectly flat)
    private double fillLevel;           // Y-level where maria fill to
    private String material;            // "basalt", "dust", "ice"

    public static MariaConfig lunar() {
        return new MariaConfig()
            .frequency(0.3)
            .flatness(0.9)
            .fillLevel(64)
            .material("basalt");
    }
}

/**
 * Configuration for atmospheric effects
 */
public class AtmosphereConfig {
    private double pressureMultiplier;  // Atmospheric pressure effect
    private double erosionEffect;       // How atmosphere affects terrain
    private boolean weathering;         // Enable weathering effects

    public static AtmosphereConfig venus() {
        return new AtmosphereConfig()
            .pressureMultiplier(2.5)
            .erosionEffect(0.8)
            .weathering(true);
    }
}

/**
 * Configuration for scarps (cliff faces)
 */
public class ScarpConfig {
    private double frequency;           // Scarp density
    private double height;              // Average scarp height
    private double length;              // Average scarp length
    private String orientation;         // Preferred orientation

    public static ScarpConfig mercury() {
        return new ScarpConfig()
            .frequency(0.03)
            .height(40)
            .length(500)
            .orientation("random");
    }
}

/**
 * Configuration for basin features
 */
public class BasinConfig {
    private double frequency;           // Basin density
    private double minRadius;           // Minimum basin radius
    private double maxRadius;           // Maximum basin radius
    private double depth;               // Basin depth

    public static BasinConfig caloris() {
        return new BasinConfig()
            .frequency(0.01)
            .minRadius(200)
            .maxRadius(600)
            .depth(30);
    }
}

/**
 * Configuration for regolith depth
 */
public class RegolithConfig {
    private double minDepth;            // Minimum regolith depth
    private double maxDepth;            // Maximum regolith depth
    private double variation;           // Depth variation (0.0-1.0)

    public static RegolithConfig lunar() {
        return new RegolithConfig()
            .minDepth(2)
            .maxDepth(8)
            .variation(0.7);
    }
}

/**
 * Configuration for individual biomes
 */
public class BiomeConfig {
    private String name;                // Biome identifier
    private int temperature;            // Temperature (for colors/effects)
    private int humidity;               // Humidity (for colors/effects)
    private int skyColor;               // Sky color (RGB hex)
    private int fogColor;               // Fog color (RGB hex)
    private int waterColor;             // Water color (RGB hex, if applicable)
    private String surfaceBlock;        // Primary surface block
    private String subsurfaceBlock;     // Block beneath surface
    private String undergroundBlock;    // Deep underground block
    private List<String> features;      // Configured features to add

    public BiomeConfig(String name) {
        this.name = name;
        this.features = new ArrayList<>();
    }

    public BiomeConfig temperature(int temp) {
        this.temperature = temp;
        return this;
    }

    public BiomeConfig surfaceBlock(String block) {
        this.surfaceBlock = block;
        return this;
    }

    public BiomeConfig addFeature(String feature) {
        this.features.add(feature);
        return this;
    }

    // ... builder methods ...
}

/**
 * Configuration for biome zone distribution
 */
public class BiomeZoneConfig {
    private Map<String, ZoneDefinition> zones;

    public BiomeZoneConfig() {
        this.zones = new HashMap<>();
    }

    /**
     * Define a zone based on altitude
     */
    public BiomeZoneConfig altitudeZone(String biomeName, int minY, int maxY) {
        zones.put(biomeName, new ZoneDefinition()
            .type("altitude")
            .minY(minY)
            .maxY(maxY));
        return this;
    }

    /**
     * Define a zone based on noise value
     */
    public BiomeZoneConfig noiseZone(String biomeName, String noiseKey,
                                     double minValue, double maxValue) {
        zones.put(biomeName, new ZoneDefinition()
            .type("noise")
            .noiseKey(noiseKey)
            .minValue(minValue)
            .maxValue(maxValue));
        return this;
    }

    /**
     * Define a zone based on feature (e.g., inside craters)
     */
    public BiomeZoneConfig featureZone(String biomeName, String featureType) {
        zones.put(biomeName, new ZoneDefinition()
            .type("feature")
            .featureType(featureType));
        return this;
    }

    public static class ZoneDefinition {
        private String type;
        private int minY, maxY;
        private String noiseKey;
        private double minValue, maxValue;
        private String featureType;

        // Builder methods...
    }
}

/**
 * Configuration for Tectonic noise integration
 */
public class TectonicNoiseConfig {
    private boolean useTectonicContinents;
    private boolean useTectonicErosion;
    private boolean useTectonicRidges;
    private Map<String, String> customNoiseOverrides;

    public TectonicNoiseConfig() {
        this.customNoiseOverrides = new HashMap<>();
    }

    public TectonicNoiseConfig useContinents(boolean use) {
        this.useTectonicContinents = use;
        return this;
    }

    public TectonicNoiseConfig overrideNoise(String noiseKey, String customFunction) {
        this.customNoiseOverrides.put(noiseKey, customFunction);
        return this;
    }

    public static TectonicNoiseConfig fullIntegration() {
        return new TectonicNoiseConfig()
            .useContinents(true)
            .useErosion(true)
            .useRidges(true);
    }

    public static TectonicNoiseConfig minimal() {
        return new TectonicNoiseConfig()
            .useContinents(false)
            .useErosion(false)
            .useRidges(false);
    }
}

/**
 * Configuration for surface rules
 */
public class SurfaceRuleConfig {
    private List<SurfaceRuleEntry> rules;

    public SurfaceRuleConfig() {
        this.rules = new ArrayList<>();
    }

    public SurfaceRuleConfig addRule(String biomeName, String condition, String block) {
        rules.add(new SurfaceRuleEntry(biomeName, condition, block));
        return this;
    }

    public static class SurfaceRuleEntry {
        String biomeName;
        String condition;  // "floor", "ceiling", "underwater", etc.
        String block;

        // Constructor and methods...
    }
}
```

---

## 4. Implementation Examples

### 4.1 Moon Configuration (5 Biomes)

```java
public class MoonPlanet {

    public static void register() {
        registerPlanet("moon")
            // Basic planet properties
            .gravity(0.166f)
            .hasAtmosphere(false)
            .skyColor(0x000000)
            .starsVisibleDuringDay(true)

            // Tectonic noise integration
            .withTectonicNoise(TectonicNoiseConfig.fullIntegration())

            // Crater features
            .withCraters(CraterConfig.realistic()
                .frequency(0.02)
                .minSize(10)
                .maxSize(100)
                .depth(1.5)
                .rimHeight(0.2)
                .distributionType("clustered"))

            // Maria (flat plains)
            .withMaria(MariaConfig.lunar()
                .frequency(0.35)
                .flatness(0.95)
                .material("basalt"))

            // Regolith depth
            .withRegolithDepth(RegolithConfig.lunar()
                .minDepth(2)
                .maxDepth(8)
                .variation(0.7))

            // Biome 1: Lunar Highlands
            .addBiome("lunar_highlands", new BiomeConfig("lunar_highlands")
                .temperature(-100)
                .skyColor(0x000000)
                .fogColor(0x1A1A1A)
                .surfaceBlock("adastramekanized:moon_regolith")
                .subsurfaceBlock("adastramekanized:moon_stone")
                .addFeature("boulder_scatters"))

            // Biome 2: Lunar Maria
            .addBiome("lunar_maria", new BiomeConfig("lunar_maria")
                .temperature(-100)
                .skyColor(0x000000)
                .fogColor(0x0A0A0A)
                .surfaceBlock("adastramekanized:moon_basalt")
                .subsurfaceBlock("adastramekanized:moon_basalt")
                .addFeature("smooth_terrain"))

            // Biome 3: Crater Rim
            .addBiome("lunar_crater_rim", new BiomeConfig("lunar_crater_rim")
                .temperature(-100)
                .skyColor(0x000000)
                .fogColor(0x2A2A2A)
                .surfaceBlock("adastramekanized:moon_regolith")
                .subsurfaceBlock("adastramekanized:moon_stone")
                .addFeature("rim_boulders"))

            // Biome 4: Crater Floor
            .addBiome("lunar_crater_floor", new BiomeConfig("lunar_crater_floor")
                .temperature(-100)
                .skyColor(0x000000)
                .fogColor(0x0F0F0F)
                .surfaceBlock("adastramekanized:moon_dust")
                .subsurfaceBlock("adastramekanized:moon_regolith")
                .addFeature("dust_plains"))

            // Biome 5: Polar Regions
            .addBiome("lunar_polar", new BiomeConfig("lunar_polar")
                .temperature(-200)
                .skyColor(0x000000)
                .fogColor(0x1F1F2F)
                .surfaceBlock("adastramekanized:moon_ice")
                .subsurfaceBlock("adastramekanized:moon_stone")
                .addFeature("ice_deposits"))

            // Biome distribution zones
            .withBiomeZones(new BiomeZoneConfig()
                // Maria in low-lying areas
                .noiseZone("lunar_maria", "altitude", -0.5, -0.2)
                // Highlands on elevated terrain
                .noiseZone("lunar_highlands", "altitude", 0.0, 0.5)
                // Crater rims where crater density is high
                .featureZone("lunar_crater_rim", "crater_rim")
                // Crater floors inside craters
                .featureZone("lunar_crater_floor", "crater_floor")
                // Polar regions near poles
                .altitudeZone("lunar_polar", -1000, 1000) // Special polar logic
            )

            // Surface rules
            .withSurfaceRules(new SurfaceRuleConfig()
                .addRule("lunar_highlands", "floor", "adastramekanized:moon_regolith")
                .addRule("lunar_maria", "floor", "adastramekanized:moon_basalt")
                .addRule("lunar_crater_rim", "floor", "adastramekanized:moon_regolith")
                .addRule("lunar_crater_floor", "floor", "adastramekanized:moon_dust"))

            .generate();
    }
}
```

### 4.2 Mars Configuration (7 Biomes)

```java
public class MarsPlanet {

    public static void register() {
        registerPlanet("mars")
            // Basic planet properties
            .gravity(0.38f)
            .hasAtmosphere(true)
            .skyColor(0xFFCC99)
            .fogColor(0xFFAA66)

            // Tectonic noise integration
            .withTectonicNoise(TectonicNoiseConfig.fullIntegration()
                .overrideNoise("erosion", "adastramekanized:mars/high_erosion"))

            // Canyon features
            .withCanyons(CanyonConfig.vallesMarineris()
                .frequency(0.01)
                .minDepth(50)
                .maxDepth(150)
                .width(200)
                .pattern("radial"))

            // Volcanic features
            .withVolcanoes(VolcanoConfig.olympusMons()
                .frequency(0.005)
                .minHeight(100)
                .maxHeight(250)
                .type("shield"))

            // Polar ice caps
            .withPolarCaps(PolarCapConfig.mars()
                .northCapRadius(500)
                .southCapRadius(600)
                .material("dry_ice"))

            // Dune fields
            .withDunes(DuneConfig.martian()
                .frequency(0.1)
                .height(15)
                .type("barchan"))

            // Biome 1: Martian Highlands
            .addBiome("martian_highlands", new BiomeConfig("martian_highlands")
                .temperature(-50)
                .skyColor(0xFFCC99)
                .surfaceBlock("adastramekanized:mars_stone")
                .addFeature("highland_rocks"))

            // Biome 2: Martian Canyon
            .addBiome("martian_canyon", new BiomeConfig("martian_canyon")
                .temperature(-40)
                .skyColor(0xFFCC99)
                .surfaceBlock("adastramekanized:mars_sand")
                .addFeature("canyon_layering"))

            // Biome 3: Volcanic Plains
            .addBiome("martian_volcanic", new BiomeConfig("martian_volcanic")
                .temperature(-30)
                .skyColor(0xFFCC99)
                .surfaceBlock("adastramekanized:mars_basalt")
                .addFeature("lava_tubes"))

            // Biome 4: Polar Ice
            .addBiome("martian_polar_ice", new BiomeConfig("martian_polar_ice")
                .temperature(-120)
                .skyColor(0xFFDDBB)
                .surfaceBlock("adastramekanized:mars_ice")
                .addFeature("layered_ice"))

            // Biome 5: Dune Fields
            .addBiome("martian_dunes", new BiomeConfig("martian_dunes")
                .temperature(-60)
                .skyColor(0xFFCC99)
                .surfaceBlock("adastramekanized:mars_sand")
                .addFeature("dune_crests"))

            // Biome 6: Lowlands
            .addBiome("martian_lowlands", new BiomeConfig("martian_lowlands")
                .temperature(-45)
                .skyColor(0xFFCC99)
                .surfaceBlock("adastramekanized:mars_dust")
                .addFeature("dust_devils"))

            // Biome 7: Channels (ancient riverbeds)
            .addBiome("martian_channel", new BiomeConfig("martian_channel")
                .temperature(-50)
                .skyColor(0xFFCC99)
                .surfaceBlock("adastramekanized:mars_gravel")
                .addFeature("channel_carving"))

            // Biome distribution zones
            .withBiomeZones(new BiomeZoneConfig()
                .noiseZone("martian_highlands", "altitude", 0.3, 0.7)
                .featureZone("martian_canyon", "canyon")
                .featureZone("martian_volcanic", "volcano")
                .altitudeZone("martian_polar_ice", -1000, 1000)
                .noiseZone("martian_dunes", "dune_noise", 0.6, 1.0)
                .noiseZone("martian_lowlands", "altitude", -0.5, 0.0)
                .noiseZone("martian_channel", "erosion", -0.8, -0.4))

            .generate();
    }
}
```

### 4.3 Venus Configuration (4 Biomes)

```java
public class VenusPlanet {

    public static void register() {
        registerPlanet("venus")
            // Basic planet properties
            .gravity(0.91f)
            .hasAtmosphere(true)
            .skyColor(0xFFFFCC)
            .fogColor(0xFFFFAA)
            .ambientLight(0.4f) // Very dim due to thick atmosphere

            // Tectonic noise integration (minimal - Venus is relatively flat)
            .withTectonicNoise(TectonicNoiseConfig.minimal())

            // Atmospheric effects
            .withAtmosphericEffects(AtmosphereConfig.venus()
                .pressureMultiplier(2.5)
                .erosionEffect(0.8))

            // Volcanic features (Venus is volcanically active)
            .withVolcanoes(VolcanoConfig.venusian()
                .frequency(0.03)
                .type("shield")
                .lavaFlows(true))

            // Biome 1: Volcanic Plains
            .addBiome("venus_volcanic_plains", new BiomeConfig("venus_volcanic_plains")
                .temperature(460) // Very hot!
                .skyColor(0xFFFFCC)
                .surfaceBlock("adastramekanized:venus_basalt")
                .addFeature("lava_plains"))

            // Biome 2: Highlands
            .addBiome("venus_highlands", new BiomeConfig("venus_highlands")
                .temperature(450)
                .skyColor(0xFFFFCC)
                .surfaceBlock("adastramekanized:venus_stone")
                .addFeature("tessera_terrain"))

            // Biome 3: Lowlands
            .addBiome("venus_lowlands", new BiomeConfig("venus_lowlands")
                .temperature(470)
                .skyColor(0xFFFFCC)
                .surfaceBlock("adastramekanized:venus_dust")
                .addFeature("smooth_plains"))

            // Biome 4: Volcanic Peaks
            .addBiome("venus_volcanic_peaks", new BiomeConfig("venus_volcanic_peaks")
                .temperature(440)
                .skyColor(0xFFFFCC)
                .surfaceBlock("adastramekanized:venus_volcanic_rock")
                .addFeature("volcanic_vents"))

            // Biome distribution zones (simple altitude-based)
            .withBiomeZones(new BiomeZoneConfig()
                .altitudeZone("venus_lowlands", -64, 70)
                .altitudeZone("venus_volcanic_plains", 70, 150)
                .altitudeZone("venus_highlands", 150, 220)
                .featureZone("venus_volcanic_peaks", "volcano"))

            .generate();
    }
}
```

### 4.4 Mercury Configuration (4 Biomes)

```java
public class MercuryPlanet {

    public static void register() {
        registerPlanet("mercury")
            // Basic planet properties
            .gravity(0.38f)
            .hasAtmosphere(false)
            .skyColor(0x000000)
            .starsVisibleDuringDay(true)

            // Tectonic noise integration
            .withTectonicNoise(TectonicNoiseConfig.fullIntegration())

            // Extreme craters
            .withCraters(CraterConfig.dramatic()
                .frequency(0.08)
                .minSize(15)
                .maxSize(250)
                .depth(2.5))

            // Scarps (cliff faces from planetary cooling)
            .withScarps(ScarpConfig.mercury()
                .frequency(0.03)
                .height(40)
                .length(500))

            // Basin features (Caloris-style)
            .withBasins(BasinConfig.caloris()
                .frequency(0.01)
                .maxRadius(600)
                .depth(30))

            // Biome 1: Cratered Plains
            .addBiome("mercury_cratered_plains", new BiomeConfig("mercury_cratered_plains")
                .temperature(200) // Very hot on day side
                .skyColor(0x000000)
                .surfaceBlock("adastramekanized:mercury_regolith")
                .addFeature("heavy_cratering"))

            // Biome 2: Scarps
            .addBiome("mercury_scarps", new BiomeConfig("mercury_scarps")
                .temperature(200)
                .skyColor(0x000000)
                .surfaceBlock("adastramekanized:mercury_stone")
                .addFeature("cliff_faces"))

            // Biome 3: Basin Floor
            .addBiome("mercury_basin_floor", new BiomeConfig("mercury_basin_floor")
                .temperature(210)
                .skyColor(0x000000)
                .surfaceBlock("adastramekanized:mercury_dust")
                .addFeature("smooth_basin"))

            // Biome 4: Highlands
            .addBiome("mercury_highlands", new BiomeConfig("mercury_highlands")
                .temperature(190)
                .skyColor(0x000000)
                .surfaceBlock("adastramekanized:mercury_regolith")
                .addFeature("ancient_terrain"))

            // Biome distribution zones
            .withBiomeZones(new BiomeZoneConfig()
                .noiseZone("mercury_cratered_plains", "altitude", -0.3, 0.3)
                .featureZone("mercury_scarps", "scarp")
                .featureZone("mercury_basin_floor", "basin")
                .noiseZone("mercury_highlands", "altitude", 0.4, 1.0))

            .generate();
    }
}
```

---

## 5. Technical Implementation Details

### 5.1 Density Function Integration

**File**: `com/hecookin/adastramekanized/worldgen/densityfunction/PlanetDensityFunctions.java`

```java
public class PlanetDensityFunctions {

    /**
     * Register all custom density functions with NeoForge
     */
    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(PlanetDensityFunctions::registerDensityFunctions);
    }

    private static void registerDensityFunctions(RegisterEvent event) {
        event.register(Registries.DENSITY_FUNCTION_TYPE, helper -> {

            // Moon density functions
            helper.register(
                id("lunar_craters"),
                LunarCraterDensity.CODEC_HOLDER.codec()
            );

            helper.register(
                id("lunar_maria"),
                LunarMariaDensity.CODEC_HOLDER.codec()
            );

            helper.register(
                id("lunar_regolith"),
                LunarRegolithDensity.CODEC_HOLDER.codec()
            );

            // Mars density functions
            helper.register(
                id("martian_canyons"),
                MartianCanyonDensity.CODEC_HOLDER.codec()
            );

            helper.register(
                id("martian_volcanoes"),
                MartianVolcanoDensity.CODEC_HOLDER.codec()
            );

            helper.register(
                id("martian_polar_caps"),
                MartianPolarCapDensity.CODEC_HOLDER.codec()
            );

            helper.register(
                id("martian_dunes"),
                MartianDuneDensity.CODEC_HOLDER.codec()
            );

            // Venus density functions
            helper.register(
                id("venus_atmosphere"),
                VenusAtmosphereDensity.CODEC_HOLDER.codec()
            );

            helper.register(
                id("venus_volcanic"),
                VenusVolcanicDensity.CODEC_HOLDER.codec()
            );

            // Mercury density functions
            helper.register(
                id("mercury_craters"),
                MercuryCraterDensity.CODEC_HOLDER.codec()
            );

            helper.register(
                id("mercury_scarps"),
                MercuryScarpDensity.CODEC_HOLDER.codec()
            );

            helper.register(
                id("mercury_basins"),
                MercuryBasinDensity.CODEC_HOLDER.codec()
            );

            AdAstraMekanized.LOGGER.info("Registered planet-specific density functions");
        });
    }

    private static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(
            AdAstraMekanized.MOD_ID,
            name
        );
    }
}
```

### 5.2 Tectonic Integration Helper

**File**: `com/hecookin/adastramekanized/worldgen/densityfunction/TectonicIntegration.java`

```java
public class TectonicIntegration {

    /**
     * Get a Tectonic density function by name
     */
    public static DensityFunction getTectonicFunction(String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath("tectonic", name);
        // Look up Tectonic's registered density function
        return DensityFunctions.lookup(id);
    }

    /**
     * Create a density function that combines Tectonic's continents with custom features
     */
    public static DensityFunction combinedTerrain(
        String tectonicBase,
        DensityFunction customFeatures,
        double blendFactor
    ) {
        DensityFunction tectonic = getTectonicFunction(tectonicBase);

        return DensityFunctions.add(
            DensityFunctions.mul(tectonic, constant(1.0 - blendFactor)),
            DensityFunctions.mul(customFeatures, constant(blendFactor))
        );
    }

    /**
     * Apply planet-specific modifications to Tectonic's noise router
     */
    public static NoiseRouter modifyNoiseRouter(
        NoiseRouter tectonicRouter,
        Map<String, DensityFunction> overrides
    ) {
        // Replace specific noise functions with planet-specific ones
        return new NoiseRouter(
            overrides.getOrDefault("barrier", tectonicRouter.barrier()),
            overrides.getOrDefault("fluid_level_floodedness", tectonicRouter.fluidLevelFloodedness()),
            overrides.getOrDefault("fluid_level_spread", tectonicRouter.fluidLevelSpread()),
            overrides.getOrDefault("lava", tectonicRouter.lava()),
            overrides.getOrDefault("temperature", tectonicRouter.temperature()),
            overrides.getOrDefault("vegetation", tectonicRouter.vegetation()),
            overrides.getOrDefault("continents", tectonicRouter.continents()),
            overrides.getOrDefault("erosion", tectonicRouter.erosion()),
            overrides.getOrDefault("depth", tectonicRouter.depth()),
            overrides.getOrDefault("ridges", tectonicRouter.ridges()),
            overrides.getOrDefault("initial_density_without_jaggedness", tectonicRouter.initialDensityWithoutJaggedness()),
            overrides.getOrDefault("final_density", tectonicRouter.finalDensity()),
            overrides.getOrDefault("vein_toggle", tectonicRouter.veinToggle()),
            overrides.getOrDefault("vein_ridged", tectonicRouter.veinRidged()),
            overrides.getOrDefault("vein_gap", tectonicRouter.veinGap())
        );
    }
}
```

### 5.3 Biome Registration with TerraBlender

**File**: `com/hecookin/adastramekanized/worldgen/biome/PlanetBiomeProvider.java`

```java
public class PlanetBiomeProvider extends Region {

    private final String planetId;
    private final Map<ResourceKey<Biome>, BiomeZoneConfig.ZoneDefinition> biomeZones;

    public PlanetBiomeProvider(String planetId,
                               Map<ResourceKey<Biome>, BiomeZoneConfig.ZoneDefinition> zones) {
        super(ResourceLocation.fromNamespaceAndPath(
            AdAstraMekanized.MOD_ID,
            planetId + "_biomes"
        ), RegionType.OVERWORLD, 2); // Weight of 2

        this.planetId = planetId;
        this.biomeZones = zones;
    }

    @Override
    public void addBiomes(Registry<Biome> registry, Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {

        // For each biome, add it to the climate parameter space
        for (Map.Entry<ResourceKey<Biome>, BiomeZoneConfig.ZoneDefinition> entry : biomeZones.entrySet()) {
            ResourceKey<Biome> biome = entry.getKey();
            BiomeZoneConfig.ZoneDefinition zone = entry.getValue();

            // Convert zone definition to climate parameters
            Climate.ParameterPoint point = zoneToClimatePoint(zone);

            mapper.accept(Pair.of(point, biome));
        }
    }

    private Climate.ParameterPoint zoneToClimatePoint(BiomeZoneConfig.ZoneDefinition zone) {
        switch (zone.getType()) {
            case "altitude":
                // Map altitude to continentalness parameter
                float continentalness = altitudeToContinentalness(zone.getMinY(), zone.getMaxY());
                return Climate.parameters(
                    Climate.Parameter.span(-1.0f, 1.0f),  // temperature
                    Climate.Parameter.span(-1.0f, 1.0f),  // humidity
                    Climate.Parameter.point(continentalness),  // continentalness
                    Climate.Parameter.span(-1.0f, 1.0f),  // erosion
                    Climate.Parameter.point(0.0f),        // depth
                    Climate.Parameter.span(-1.0f, 1.0f),  // weirdness
                    0.0f                                   // offset
                );

            case "noise":
                // Map noise range to appropriate climate parameter
                float value = (float) ((zone.getMinValue() + zone.getMaxValue()) / 2.0);
                return Climate.parameters(
                    Climate.Parameter.span(-1.0f, 1.0f),
                    Climate.Parameter.span(-1.0f, 1.0f),
                    Climate.Parameter.span(-1.0f, 1.0f),
                    Climate.Parameter.point(value),  // Use erosion for noise zones
                    Climate.Parameter.point(0.0f),
                    Climate.Parameter.span(-1.0f, 1.0f),
                    0.0f
                );

            case "feature":
                // Feature-based zones use specific parameter combinations
                return getFeatureClimatePoint(zone.getFeatureType());

            default:
                // Default to full range
                return Climate.parameters(
                    Climate.Parameter.span(-1.0f, 1.0f),
                    Climate.Parameter.span(-1.0f, 1.0f),
                    Climate.Parameter.span(-1.0f, 1.0f),
                    Climate.Parameter.span(-1.0f, 1.0f),
                    Climate.Parameter.point(0.0f),
                    Climate.Parameter.span(-1.0f, 1.0f),
                    0.0f
                );
        }
    }

    private float altitudeToContinentalness(int minY, int maxY) {
        // Map Y coordinates to -1.0 to 1.0 range
        int midY = (minY + maxY) / 2;
        return (midY - 64) / 128.0f; // Normalize around sea level
    }

    private Climate.ParameterPoint getFeatureClimatePoint(String featureType) {
        // Special climate points for feature-based biomes
        switch (featureType) {
            case "crater_rim":
                return Climate.parameters(
                    Climate.Parameter.span(-1.0f, 1.0f),
                    Climate.Parameter.span(-1.0f, 1.0f),
                    Climate.Parameter.span(0.5f, 0.7f),   // High continentalness
                    Climate.Parameter.span(-1.0f, 1.0f),
                    Climate.Parameter.point(0.0f),
                    Climate.Parameter.span(0.3f, 1.0f),   // High weirdness for rims
                    0.0f
                );

            case "crater_floor":
                return Climate.parameters(
                    Climate.Parameter.span(-1.0f, 1.0f),
                    Climate.Parameter.span(-1.0f, 1.0f),
                    Climate.Parameter.span(-0.7f, -0.5f), // Low continentalness
                    Climate.Parameter.span(-1.0f, 1.0f),
                    Climate.Parameter.point(0.0f),
                    Climate.Parameter.span(0.3f, 1.0f),   // High weirdness
                    0.0f
                );

            // Add more feature types as needed

            default:
                return Climate.parameters(
                    Climate.Parameter.span(-1.0f, 1.0f),
                    Climate.Parameter.span(-1.0f, 1.0f),
                    Climate.Parameter.span(-1.0f, 1.0f),
                    Climate.Parameter.span(-1.0f, 1.0f),
                    Climate.Parameter.point(0.0f),
                    Climate.Parameter.span(-1.0f, 1.0f),
                    0.0f
                );
        }
    }
}
```

### 5.4 Central Registration

**File**: `com/hecookin/adastramekanized/worldgen/PlanetWorldGenRegistry.java`

```java
public class PlanetWorldGenRegistry {

    private static final Map<String, PlanetWorldGenConfig> PLANET_CONFIGS = new HashMap<>();

    /**
     * Called during mod initialization
     */
    public static void init(IEventBus modEventBus) {
        // Register density functions
        PlanetDensityFunctions.register(modEventBus);

        // Register biomes (via TerraBlender)
        modEventBus.addListener(PlanetWorldGenRegistry::registerBiomes);

        AdAstraMekanized.LOGGER.info("Planet world generation system initialized");
    }

    /**
     * Register a planet's world generation configuration
     */
    public static void registerPlanetWorldGen(String planetId, PlanetWorldGenConfig config) {
        PLANET_CONFIGS.put(planetId, config);
        AdAstraMekanized.LOGGER.info("Registered world generation for planet: {}", planetId);
    }

    /**
     * Called by TerraBlender to register biomes
     */
    private static void registerBiomes(RegisterEvent event) {
        for (Map.Entry<String, PlanetWorldGenConfig> entry : PLANET_CONFIGS.entrySet()) {
            String planetId = entry.getKey();
            PlanetWorldGenConfig config = entry.getValue();

            // Create and register biome provider
            PlanetBiomeProvider provider = new PlanetBiomeProvider(
                planetId,
                config.getBiomeZones()
            );

            // Register with TerraBlender
            Regions.register(provider);

            AdAstraMekanized.LOGGER.info("Registered biomes for planet: {}", planetId);
        }
    }

    /**
     * Get world generation config for a planet
     */
    public static PlanetWorldGenConfig getConfig(String planetId) {
        return PLANET_CONFIGS.get(planetId);
    }
}

/**
 * Holds all world generation configuration for a planet
 */
public class PlanetWorldGenConfig {
    private final String planetId;
    private final Map<ResourceKey<Biome>, BiomeZoneConfig.ZoneDefinition> biomeZones;
    private final List<DensityFunction> customDensityFunctions;
    private final NoiseGeneratorSettings noiseSettings;

    // Constructor and getters...
}
```

---

## 6. JSON Generation System

### 6.1 Automatic JSON Generation

The `PlanetMaker.generate()` method will automatically create all necessary JSON files:

```java
public void generate() {
    // 1. Generate dimension JSON
    generateDimensionJson();

    // 2. Generate noise settings JSON
    generateNoiseSettingsJson();

    // 3. Generate density function JSONs
    generateDensityFunctionJsons();

    // 4. Generate biome JSONs
    generateBiomeJsons();

    // 5. Generate configured feature JSONs
    generateFeatureJsons();

    // 6. Register with world gen system
    PlanetWorldGenRegistry.registerPlanetWorldGen(planetId, buildConfig());
}

private void generateNoiseSettingsJson() {
    JsonObject noiseSettings = new JsonObject();

    // Noise configuration
    JsonObject noise = new JsonObject();
    noise.addProperty("min_y", minY);
    noise.addProperty("height", height);
    noise.addProperty("size_horizontal", 1);
    noise.addProperty("size_vertical", 2);
    noiseSettings.add("noise", noise);

    // Default blocks
    noiseSettings.add("default_block", blockStateJson(surfaceBlock));
    noiseSettings.add("default_fluid", blockStateJson("minecraft:air"));
    noiseSettings.addProperty("sea_level", seaLevel);

    // Noise router - integrate Tectonic + custom
    JsonObject noiseRouter = new JsonObject();

    if (tectonicNoiseConfig.useTectonicContinents()) {
        noiseRouter.addProperty("continents", "tectonic:noise/full_continents");
    } else {
        noiseRouter.addProperty("continents", "adastramekanized:" + planetId + "/continents");
    }

    if (tectonicNoiseConfig.useTectonicErosion()) {
        noiseRouter.addProperty("erosion", "tectonic:biome_parameter/erosion");
    } else {
        noiseRouter.addProperty("erosion", "minecraft:zero");
    }

    // Custom final density that includes craters, canyons, etc.
    noiseRouter.addProperty("final_density", "adastramekanized:" + planetId + "/final_density");

    // Other standard noise router entries
    noiseRouter.addProperty("barrier", "minecraft:overworld/barrier");
    noiseRouter.addProperty("fluid_level_floodedness", "minecraft:zero");
    noiseRouter.addProperty("fluid_level_spread", "minecraft:zero");
    noiseRouter.addProperty("lava", "minecraft:zero");
    noiseRouter.addProperty("temperature", "minecraft:zero");
    noiseRouter.addProperty("vegetation", "minecraft:zero");
    noiseRouter.addProperty("vein_toggle", "minecraft:zero");
    noiseRouter.addProperty("vein_gap", "minecraft:zero");
    noiseRouter.addProperty("vein_ridged", "minecraft:zero");

    noiseSettings.add("noise_router", noiseRouter);

    // Surface rules (per-biome)
    noiseSettings.add("surface_rule", generateSurfaceRules());

    // Save JSON file
    saveJson(noiseSettings, "worldgen/noise_settings/" + planetId + ".json");
}

private JsonObject generateSurfaceRules() {
    JsonArray sequence = new JsonArray();

    // Add bedrock layer
    sequence.add(bedrockRule());

    // Add biome-specific surface rules
    for (Map.Entry<String, BiomeConfig> entry : biomes.entrySet()) {
        String biomeName = entry.getKey();
        BiomeConfig biomeConfig = entry.getValue();

        JsonObject biomeRule = new JsonObject();
        biomeRule.addProperty("type", "minecraft:condition");

        // Condition: is this biome?
        JsonObject condition = new JsonObject();
        condition.addProperty("type", "minecraft:biome");
        JsonArray biomeIds = new JsonArray();
        biomeIds.add("adastramekanized:" + biomeName);
        condition.add("biome_is", biomeIds);
        biomeRule.add("if_true", condition);

        // Surface blocks for this biome
        JsonObject surfaceSequence = new JsonObject();
        surfaceSequence.addProperty("type", "minecraft:sequence");
        JsonArray biomeSequence = new JsonArray();

        // Surface block
        biomeSequence.add(surfaceBlockRule(biomeConfig.getSurfaceBlock()));

        // Subsurface block
        if (biomeConfig.getSubsurfaceBlock() != null) {
            biomeSequence.add(subsurfaceBlockRule(biomeConfig.getSubsurfaceBlock()));
        }

        surfaceSequence.add("sequence", biomeSequence);
        biomeRule.add("then_run", surfaceSequence);

        sequence.add(biomeRule);
    }

    // Default underground block
    sequence.add(defaultBlockRule(deepBlock));

    JsonObject root = new JsonObject();
    root.addProperty("type", "minecraft:sequence");
    root.add("sequence", sequence);

    return root;
}
```

### 6.2 Density Function JSON Generation

```java
private void generateDensityFunctionJsons() {
    // Generate final_density.json that combines everything
    JsonObject finalDensity = new JsonObject();
    finalDensity.addProperty("type", "minecraft:cache_once");

    JsonObject argument = new JsonObject();
    argument.addProperty("type", "minecraft:add");

    // Base terrain (use Tectonic or custom)
    if (tectonicNoiseConfig.useTectonicContinents()) {
        argument.addProperty("argument1", "tectonic:base_terrain");
    } else {
        argument.addProperty("argument1", "adastramekanized:" + planetId + "/base_terrain");
    }

    // Add all custom features
    JsonObject features = new JsonObject();
    features.addProperty("type", "minecraft:add");

    JsonArray featureList = new JsonArray();

    // Add craters if configured
    if (craterConfig != null) {
        JsonObject craterDensity = new JsonObject();
        craterDensity.addProperty("type", "adastramekanized:lunar_craters");
        craterDensity.addProperty("frequency", craterConfig.getFrequency());
        craterDensity.addProperty("min_size", craterConfig.getMinSize());
        craterDensity.addProperty("max_size", craterConfig.getMaxSize());
        craterDensity.addProperty("depth", craterConfig.getDepth());
        craterDensity.addProperty("rim_height", craterConfig.getRimHeight());
        craterDensity.addProperty("rim_width", craterConfig.getRimWidth());
        featureList.add(craterDensity);
    }

    // Add canyons if configured
    if (canyonConfig != null) {
        JsonObject canyonDensity = new JsonObject();
        canyonDensity.addProperty("type", "adastramekanized:martian_canyons");
        canyonDensity.addProperty("frequency", canyonConfig.getFrequency());
        canyonDensity.addProperty("min_depth", canyonConfig.getMinDepth());
        canyonDensity.addProperty("max_depth", canyonConfig.getMaxDepth());
        canyonDensity.addProperty("width", canyonConfig.getWidth());
        canyonDensity.addProperty("sinuosity", canyonConfig.getSinuosity());
        featureList.add(canyonDensity);
    }

    // Add volcanoes if configured
    if (volcanoConfig != null) {
        JsonObject volcanoDensity = new JsonObject();
        volcanoDensity.addProperty("type", "adastramekanized:martian_volcanoes");
        volcanoDensity.addProperty("frequency", volcanoConfig.getFrequency());
        volcanoDensity.addProperty("min_height", volcanoConfig.getMinHeight());
        volcanoDensity.addProperty("max_height", volcanoConfig.getMaxHeight());
        volcanoDensity.addProperty("slope_angle", volcanoConfig.getSlopeAngle());
        volcanoDensity.addProperty("type", volcanoConfig.getType());
        featureList.add(volcanoDensity);
    }

    // Continue for all other feature types...

    // Combine all features with addition
    if (featureList.size() > 0) {
        JsonObject combined = combineFeatures(featureList);
        argument.add("argument2", combined);
    } else {
        argument.addProperty("argument2", 0);
    }

    finalDensity.add("argument", argument);

    // Save final_density.json
    saveJson(finalDensity, "worldgen/density_function/" + planetId + "/final_density.json");
}

private JsonObject combineFeatures(JsonArray features) {
    if (features.size() == 1) {
        return features.get(0).getAsJsonObject();
    }

    // Recursively combine with add operations
    JsonObject combined = new JsonObject();
    combined.addProperty("type", "minecraft:add");
    combined.add("argument1", features.get(0));

    if (features.size() == 2) {
        combined.add("argument2", features.get(1));
    } else {
        JsonArray remaining = new JsonArray();
        for (int i = 1; i < features.size(); i++) {
            remaining.add(features.get(i));
        }
        combined.add("argument2", combineFeatures(remaining));
    }

    return combined;
}
```

---

## 7. Implementation Roadmap (6-8 Weeks)

### Phase 1: Foundation (Weeks 1-2)

**Week 1: Dependency Setup & API Design**
- [ ] Add Tectonic and TerraBlender as dependencies in build.gradle
- [ ] Create package structure (worldgen/biome, worldgen/densityfunction, etc.)
- [ ] Design and implement configuration classes (CraterConfig, CanyonConfig, etc.)
- [ ] Extend PlanetMaker with new API methods
- [ ] Test compilation

**Week 2: Core Integration**
- [ ] Implement TectonicIntegration helper class
- [ ] Implement PlanetDensityFunctions registration
- [ ] Implement PlanetBiomeProvider for TerraBlender
- [ ] Implement PlanetWorldGenRegistry
- [ ] Create basic JSON generation methods in PlanetMaker
- [ ] Test with simple planet (no features yet)

### Phase 2: Moon Implementation (Weeks 3-4)

**Week 3: Moon Density Functions**
- [ ] Implement LunarCraterDensity (Voronoi-based craters)
- [ ] Implement LunarMariaDensity (flat plains)
- [ ] Implement LunarRegolithDensity (depth variation)
- [ ] Test crater generation in isolation
- [ ] Test maria generation in isolation

**Week 4: Moon Biomes**
- [ ] Create 5 Moon biomes (highlands, maria, crater rim, crater floor, polar)
- [ ] Implement MoonBiomeDistribution (zone logic)
- [ ] Configure Moon in PlanetGenerationRunner
- [ ] Generate Moon JSON files
- [ ] Test Moon dimension in-game
- [ ] Verify biome distribution
- [ ] Performance testing

### Phase 3: Mars Implementation (Weeks 5-6)

**Week 5: Mars Density Functions**
- [ ] Implement MartianCanyonDensity
- [ ] Implement MartianVolcanoDensity
- [ ] Implement MartianPolarCapDensity
- [ ] Implement MartianDuneDensity
- [ ] Test each feature independently

**Week 6: Mars Biomes**
- [ ] Create 7 Mars biomes (highlands, canyon, volcanic, polar, dunes, lowlands, channels)
- [ ] Implement MarsBiomeDistribution
- [ ] Configure Mars in PlanetGenerationRunner
- [ ] Generate Mars JSON files
- [ ] Test Mars dimension in-game
- [ ] Verify all features work together
- [ ] Performance testing

### Phase 4: Venus & Mercury (Week 7)

**Venus (Days 1-3)**
- [ ] Implement VenusAtmosphereDensity
- [ ] Implement VenusVolcanicDensity
- [ ] Create 4 Venus biomes
- [ ] Configure and test Venus

**Mercury (Days 4-7)**
- [ ] Implement MercuryCraterDensity
- [ ] Implement MercuryScarpDensity
- [ ] Implement MercuryBasinDensity
- [ ] Create 4 Mercury biomes
- [ ] Configure and test Mercury

### Phase 5: Remaining Planets & Polish (Week 8)

**Remaining Planets (Days 1-4)**
- [ ] Configure Glacio (using existing ice-world features)
- [ ] Configure Binary World (using atmospheric features)
- [ ] Configure Earth-like planets (using Tectonic defaults)
- [ ] Test all planets

**Polish & Documentation (Days 5-7)**
- [ ] Performance optimization (caching, profiling)
- [ ] Create PLANET_CREATION_GUIDE.md
- [ ] Add JavaDoc to all new classes
- [ ] Create example planet configurations
- [ ] Final testing pass on all 13 planets
- [ ] Update README.md with new features

---

## 8. Documentation to Create

### 8.1 PLANET_CREATION_GUIDE.md

Create comprehensive guide showing:
- How to create a new planet with custom biomes
- How to configure each feature type
- How to test biome distribution
- Common pitfalls and solutions
- Performance considerations

### 8.2 API_REFERENCE.md

Document all new API methods:
- PlanetMaker extensions
- Configuration class constructors
- Preset configurations (e.g., CraterConfig.realistic())
- Density function parameters

### 8.3 TECTONIC_INTEGRATION.md

Explain:
- How we integrate with Tectonic
- Which Tectonic features we use
- How to override Tectonic's noise
- Troubleshooting Tectonic conflicts

---

## 9. Testing Strategy

### 9.1 Unit Tests

**Test Density Functions:**
```java
@Test
public void testLunarCraterDensity() {
    LunarCraterDensity craters = new LunarCraterDensity(
        /* ... params ... */
    );

    // Test crater centers have negative density
    double centerValue = craters.compute(craterCenterContext);
    assertTrue(centerValue < 0);

    // Test crater rims have positive density
    double rimValue = craters.compute(craterRimContext);
    assertTrue(rimValue > 0);

    // Test outside crater is neutral
    double outsideValue = craters.compute(outsideContext);
    assertEquals(0, outsideValue, 0.01);
}
```

**Test Configuration:**
```java
@Test
public void testPlanetMakerConfiguration() {
    PlanetMaker moon = registerPlanet("test_moon")
        .withCraters(CraterConfig.realistic())
        .addBiome("test_biome", new BiomeConfig("test_biome"));

    assertNotNull(moon.getCraterConfig());
    assertEquals(1, moon.getBiomes().size());
}
```

### 9.2 Integration Tests

**Test JSON Generation:**
```java
@Test
public void testJsonGeneration() {
    registerPlanet("test_moon")
        .withCraters(CraterConfig.realistic())
        .generate();

    // Verify JSON files were created
    assertTrue(Files.exists(Paths.get("data/adastramekanized/worldgen/noise_settings/test_moon.json")));
    assertTrue(Files.exists(Paths.get("data/adastramekanized/worldgen/density_function/test_moon/final_density.json")));
}
```

**Test TerraBlender Integration:**
```java
@Test
public void testBiomeRegistration() {
    // Create test planet with biomes
    registerPlanet("test_planet")
        .addBiome("test_biome_1", new BiomeConfig("test_biome_1"))
        .addBiome("test_biome_2", new BiomeConfig("test_biome_2"))
        .generate();

    // Verify biomes registered with TerraBlender
    Collection<ResourceKey<Biome>> registeredBiomes =
        PlanetBiomeRegistry.getBiomesForPlanet("test_planet");
    assertEquals(2, registeredBiomes.size());
}
```

### 9.3 In-Game Testing

**Test Checklist per Planet:**
- [ ] Dimension loads without crash
- [ ] Terrain generates (no missing chunks)
- [ ] All biomes appear in F3 debug screen
- [ ] Biome distribution looks correct
- [ ] Features generate correctly (craters, canyons, etc.)
- [ ] Performance is acceptable (< 50ms per chunk)
- [ ] No z-fighting or rendering artifacts
- [ ] Blocks place/break correctly
- [ ] Teleport command works
- [ ] Can set spawn point

**Performance Benchmarks:**
- Target: < 50ms per chunk generation
- Profile with JProfiler or VisualVM
- Identify hot paths in density functions
- Add caching where needed

---

## 10. Migration Plan

### 10.1 Breaking Changes

**Existing worlds WILL break.** Document this clearly:

```
⚠️ WARNING: Version X.X.X is a MAJOR BREAKING UPDATE

This update completely rebuilds the world generation system for all planets.
Existing worlds using Ad Astra Mekanized planets will be CORRUPTED.

BACKUP YOUR WORLDS before updating!

What breaks:
- All planet dimensions (Moon, Mars, Venus, etc.)
- Any bases/structures built on planets
- Items/blocks in planet dimensions

What stays:
- Overworld content
- Player inventory (if not on a planet)
- Mod blocks/items in inventories

Recommended: Start a fresh world or use this on new saves only.
```

### 10.2 Migration Script (Optional)

If you want to help players migrate:

```java
public class WorldMigrationHelper {

    /**
     * Attempt to extract player structures from old planet dimensions
     */
    public static void exportStructures(ServerLevel oldWorld, Path exportPath) {
        // Scan chunks, find player-placed blocks
        // Export to schematic format
        // Allow re-import into new world
    }
}
```

---

## 11. Success Criteria (Final Checklist)

### ✅ **Criterion 1: Tectonic-Style Biomes on Planets**

- [ ] Each planet has 3-10 unique biomes
- [ ] Biomes use Tectonic's density function system
- [ ] Biomes have distinct appearance and features
- [ ] Biome transitions are smooth (no harsh borders)
- [ ] Biomes registered via TerraBlender successfully

### ✅ **Criterion 2: Easy to Extend**

- [ ] Adding a new planet takes < 2 hours
- [ ] Template code provided for common planet types
- [ ] Configuration classes are intuitive
- [ ] Preset configurations available (realistic, dramatic, etc.)
- [ ] No boilerplate code required

### ✅ **Criterion 3: Full Documentation**

- [ ] PLANET_CREATION_GUIDE.md with step-by-step tutorial
- [ ] API_REFERENCE.md documenting all methods
- [ ] TECTONIC_INTEGRATION.md explaining integration
- [ ] JavaDoc on all public classes/methods
- [ ] Example configurations for 5+ planet types
- [ ] Troubleshooting section for common issues

### ✅ **Criterion 4: Visual Quality**

- [ ] Terrain looks interesting and varied
- [ ] Features (craters, canyons) are visually impressive
- [ ] Biomes are easily distinguishable
- [ ] No visual artifacts (z-fighting, gaps, etc.)
- [ ] Performance is acceptable (< 50ms per chunk)

---

## 12. Risk Mitigation

### Risk 1: Tectonic Compatibility Issues

**Risk**: Tectonic updates break our integration

**Mitigation**:
- Pin specific Tectonic version in dependencies
- Test with each Tectonic update before releasing
- Maintain compatibility layer (TectonicIntegration.java)
- Document which Tectonic version we support

### Risk 2: Performance Problems

**Risk**: Complex density functions cause lag

**Mitigation**:
- Profile early and often
- Use Minecraft's caching strategies (cache_once, cache_2d)
- Simplify density functions if needed
- Add config option to reduce feature density

### Risk 3: TerraBlender Conflicts

**Risk**: Biome distribution doesn't work as expected

**Mitigation**:
- Test biome registration thoroughly
- Use higher region weight if needed
- Document biome distribution logic clearly
- Provide debugging tools (show biome zones)

### Risk 4: Scope Creep

**Risk**: Adding too many features delays completion

**Mitigation**:
- Stick to 6-8 week timeline
- Phase 1-3 are MUST-HAVE (Moon, Mars)
- Phase 4-5 can be delayed if needed
- Focus on core features first, polish later

---

## 13. File Checklist

### Files to Create (Phase 1)

```
✅ Configuration Classes:
- [ ] CraterConfig.java
- [ ] CanyonConfig.java
- [ ] VolcanoConfig.java
- [ ] PolarCapConfig.java
- [ ] DuneConfig.java
- [ ] MariaConfig.java
- [ ] AtmosphereConfig.java
- [ ] ScarpConfig.java
- [ ] BasinConfig.java
- [ ] RegolithConfig.java
- [ ] BiomeConfig.java
- [ ] BiomeZoneConfig.java
- [ ] TectonicNoiseConfig.java
- [ ] SurfaceRuleConfig.java

✅ Core System:
- [ ] PlanetMaker.java (extend existing)
- [ ] PlanetDensityFunctions.java
- [ ] TectonicIntegration.java
- [ ] PlanetBiomeProvider.java
- [ ] PlanetBiomeRegistry.java
- [ ] PlanetWorldGenRegistry.java

✅ Dependencies:
- [ ] build.gradle (add Tectonic, TerraBlender)
- [ ] neoforge.mods.toml (add dependencies)
```

### Files to Create (Phase 2-4)

```
✅ Moon:
- [ ] LunarCraterDensity.java
- [ ] LunarMariaDensity.java
- [ ] LunarRegolithDensity.java
- [ ] MoonBiomes.java
- [ ] MoonBiomeDistribution.java

✅ Mars:
- [ ] MartianCanyonDensity.java
- [ ] MartianVolcanoDensity.java
- [ ] MartianPolarCapDensity.java
- [ ] MartianDuneDensity.java
- [ ] MarsBiomes.java
- [ ] MarsBiomeDistribution.java

✅ Venus:
- [ ] VenusAtmosphereDensity.java
- [ ] VenusVolcanicDensity.java
- [ ] VenusBiomes.java
- [ ] VenusBiomeDistribution.java

✅ Mercury:
- [ ] MercuryCraterDensity.java
- [ ] MercuryScarpDensity.java
- [ ] MercuryBasinDensity.java
- [ ] MercuryBiomes.java
- [ ] MercuryBiomeDistribution.java
```

---

## 14. Next Steps

### Immediate Actions (This Week)

1. **Review this design document** - Ensure team agrees with approach
2. **Add dependencies** - Tectonic and TerraBlender to build.gradle
3. **Test Tectonic installation** - Verify it loads with Ad Astra Mekanized
4. **Create package structure** - Set up all the directories
5. **Start Phase 1, Week 1** - Begin implementation

### Questions to Resolve

1. Do we need compatibility with older Minecraft versions?
2. Should we support both Tectonic datapack AND mod versions?
3. Do we want in-game config GUI or just code-based?
4. Should we create a test/debug dimension for faster iteration?

---

**END OF DESIGN DOCUMENT**

This design is **implementation-ready**. All requirements are clear, architecture is defined, and code examples are provided. Ready to begin Phase 1, Week 1 immediately.
