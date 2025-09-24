# Planetary Generation System

## Overview

The planetary generation system in Ad Astra Mekanized uses a comprehensive PlanetMaker API that provides complete control over Minecraft 1.21.1 noise generation, terrain shaping, ore distribution, and surface rules. This system generates unique planets with distinct characteristics using NeoForge's world generation framework.

## PlanetMaker API

### Core Builder Pattern

The PlanetMaker system uses a fluent builder pattern for configuring planets:

```java
PlanetMaker.planet("planet_name")
    .continentalScale(25.0f)
    .erosionScale(35.0f)
    .ridgeScale(15.0f)
    .heightVariation(15.0f, 10.0f, 5.0f, 3.0f)
    .surfaceBlock("minecraft:grass_block")
    .generate();
```

### Terrain Control Parameters

#### Continental and Erosion Parameters
- **continentalScale(float)**: Controls continental landmass size and distribution (1.0-100.0)
- **erosionScale(float)**: Controls erosion patterns and terrain smoothness (1.0-100.0)
- **ridgeScale(float)**: Controls ridge formation and mountain spine generation (1.0-50.0)

#### Height Variation System
- **heightVariation(float, float, float, float)**: Four-parameter height control system
  - Parameter 1: Base height multiplier (1.0-50.0)
  - Parameter 2: Secondary height variation (1.0-25.0)
  - Parameter 3: Tertiary height detail (1.0-15.0)
  - Parameter 4: Fine height detail (1.0-10.0)

#### Advanced Noise Controls
- **temperatureScale(float)**: Temperature variation across planet (0.1-10.0)
- **humidityScale(float)**: Humidity gradients for biome distribution (0.1-8.0)
- **weirdnessScale(float)**: Terrain weirdness factor for unusual formations (0.1-15.0)
- **densityFactor(float)**: Density multiplication for terrain density (0.1-5.0)
- **densityOffset(float)**: Base density offset (-2.0 to 2.0)

#### Vertical Gradient System
- **verticalGradient(int fromY, int toY, float fromValue, float toValue)**: Y-clamped gradient control
- **gradientMultiplier(float)**: Multiplier for gradient strength (0.1-3.0)

#### Advanced Terrain Shaping
- **initialDensityOffset(float)**: Initial density calculation offset (-1.0 to 1.0)
- **terrainShapingFactor(float)**: Overall terrain complexity factor (0.1-1.0)
- **legacyRandomSource(boolean)**: Use legacy random generation

#### Hill and Mountain Generation
- **jaggednessScale(float)**: Mountain jaggedness and sharpness (0.1-1.0)
- **jaggednessNoiseScale(float)**: Noise scale for mountain features (100.0-2000.0)
- **depthFactor(float)**: Depth variation multiplier (0.1-5.0)
- **depthOffset(float)**: Base depth offset (-1.0 to 1.0)
- **terrainFactor(float)**: Terrain intensity multiplier (0.1-3.0)
- **base3DNoiseScale(float, float)**: 3D noise scaling for complex terrain
- **base3DNoiseFactor(float, float)**: 3D noise amplitude for dramatic features
- **smearScaleMultiplier(float)**: Terrain smoothing factor (1.0-20.0)

### Surface Block Configuration

#### Primary Surface Blocks
- **surfaceBlock(String)**: Top surface block (e.g., "minecraft:grass_block")
- **subsurfaceBlock(String)**: Subsurface layer block (e.g., "minecraft:dirt")
- **deepBlock(String)**: Deep underground block (e.g., "minecraft:stone")
- **defaultBlock(String)**: Default filler block
- **defaultFluid(String)**: Default fluid (e.g., "minecraft:water", "minecraft:lava")

#### Underwater and Special Blocks
- **underwaterBlock(String)**: Block for underwater areas
- **shallowUnderwaterBlock(String)**: Shallow underwater areas
- **deepUnderwaterBlock(String)**: Deep underwater areas
- **bedrockBlock(String)**: Custom bedrock replacement

#### Surface Generation Control
- **preventGrassGeneration(boolean)**: Disable default grass generation
- **preventGravelGeneration(boolean)**: Disable default gravel generation
- **preventSandGeneration(boolean)**: Disable default sand generation
- **disableDefaultSurfaceGeneration(boolean)**: Complete surface control override

### Noise Router Configuration

#### Atmospheric Noise
- **barrierNoise(float)**: Atmospheric barrier effects (0.0-1.0)
- **fluidLevelFloodedness(float)**: Fluid area distribution (0.0-1.0)
- **fluidLevelSpread(float)**: Fluid spreading patterns (0.0-1.0)
- **lavaNoise(float)**: Lava generation intensity (0.0-1.0)
- **temperatureNoise(float)**: Temperature noise patterns (0.0-1.0)
- **vegetationNoise(float)**: Vegetation distribution noise (0.0-1.0)

### Dynamic Biome System

#### Biome Configuration Methods
- **addBiome(String name, float weight)**: Add biome with automatic climate parameters
  - Weight: 0.1-1.0 (relative frequency)
  - Auto-detects biome type from name for appropriate parameters
- **addBiome(String name, float temp, float humid, float cont, float erosion, float depth, float weird)**: Full control over biome parameters
  - All parameters: -1.0 to 1.0
- **clearBiomes()**: Clear all default biomes before adding custom set

#### Smart Biome Detection
The system automatically configures climate parameters based on biome name keywords:
- **Desert/Badlands**: Hot, dry climate
- **Frozen/Snowy/Ice**: Cold climate
- **Jungle/Swamp**: Hot, humid climate
- **Forest/Taiga**: Temperate climate
- **Ocean/River**: Water-based parameters
- **Mountain/Peaks**: High elevation parameters
- **Plains/Meadow**: Neutral parameters
- **Savanna**: Hot, dry with variation
- **Basalt/Soul/Nether**: Volcanic parameters

### Ore Generation System

#### Ore Vein Controls
- **veinToggle(float)**: Ore vein activation level (0.0-1.0)
- **veinRidged(float)**: Ridged vein formation intensity (0.0-1.0)
- **veinGap(float)**: Gaps between ore veins (0.0-1.0)

#### Advanced Ore Configuration
- **oreVeinDensity(float)**: Overall ore density multiplier (0.1-3.0)
- **oreVeinSize(float)**: Individual vein size multiplier (0.1-2.0)
- **maxOreVeinCount(int)**: Maximum different ore types (1-50)
- **enableRareOres(boolean)**: Include rare ore types
- **enableCommonOres(boolean)**: Include common ore types
- **enableDeepslateOres(boolean)**: Include deepslate variants

#### Custom Ore Veins
- **addCustomOreVein(String)**: Add specific ore type (e.g., "minecraft:diamond_ore")

### Noise-Based Ore Distribution

The system uses Minecraft's `ore_gap` noise with threshold-based rarity:

```java
// Diamond ore: 8% spawn chance (0.92-1.0 threshold)
.addCustomOreVein("minecraft:diamond_ore")

// Iron ore: 30% spawn chance (0.7-1.0 threshold)
.addCustomOreVein("minecraft:iron_ore")
```

**Ore Rarity Thresholds:**
- Diamond: 0.92-1.0 (8% spawn chance)
- Gold: 0.85-1.0 (15% spawn chance)
- Emerald: 0.74-1.0 (26% spawn chance, rare planets only)
- Iron: 0.7-1.0 (30% spawn chance)
- Copper: 0.65-1.0 (35% spawn chance)
- Redstone: 0.75-1.0 (25% spawn chance)
- Lapis: 0.8-1.0 (20% spawn chance)

### World Dimension Configuration

#### Basic World Structure
- **worldDimensions(int minY, int height)**: World height bounds (e.g., -64 to 384)
- **noiseSize(int horizontal, int vertical)**: Noise resolution (1-4 each)
- **seaLevel(int)**: Sea level height (0-320)

#### Generation Controls
- **disableMobGeneration(boolean)**: Prevent mob spawning
- **aquifersEnabled(boolean)**: Enable aquifer generation
- **oreVeinsEnabled(boolean)**: Enable ore vein system
- **abovePreliminaryRule(boolean)**: Use surface smoothing rules
- **waterRule(boolean)**: Apply water generation rules
- **surfaceDepthMultiplier(int)**: Surface layer thickness (1-10)
- **addStoneDepth(boolean)**: Add extra stone depth layers

### Visual and Atmospheric Properties

#### Sky and Atmosphere
- **skyColor(int)**: Sky color in hex (e.g., 0x87CEEB for sky blue)
- **fogColor(int)**: Fog color in hex (e.g., 0xC0C0C0 for light gray)
- **hasAtmosphere(boolean)**: Enable atmospheric effects
- **ambientLight(float)**: Ambient light level (0.0-1.0)

#### Biome Distribution
- **biomeDistribution(float, float, float, float, float, float)**: Six-parameter biome control system

## Example Planet Configurations

### Earth-like Planet with Multiple Biomes

```java
PlanetMaker.planet("earthlike")
    // Realistic terrain shaping
    .continentalScale(8.0f)
    .erosionScale(12.0f)
    .ridgeScale(5.0f)
    .heightVariation(0.8f, 0.5f, 0.3f, 0.2f)

    // Dynamic biome system
    .clearBiomes()
    .addBiome("minecraft:plains", 0.30f)
    .addBiome("minecraft:forest", 0.25f)
    .addBiome("minecraft:taiga", 0.15f)
    .addBiome("minecraft:savanna", 0.10f)
    .addBiome("minecraft:snowy_plains", 0.08f)
    .addBiome("minecraft:swamp", 0.07f)
    .addBiome("minecraft:desert", 0.05f)

    // Surface configuration
    .surfaceBlock("minecraft:grass_block")
    .subsurfaceBlock("minecraft:dirt")
    .seaLevel(64)
    .generate();
```

### Volcanic Alien World

```java
PlanetMaker.planet("volcanic")
    // Extreme terrain
    .continentalScale(50.0f)
    .erosionScale(100.0f)
    .jaggednessScale(1.0f)
    .jaggednessNoiseScale(2000.0f)

    // Nether biomes for volcanic landscape
    .clearBiomes()
    .addBiome("minecraft:basalt_deltas", 0.35f)
    .addBiome("minecraft:crimson_forest", 0.25f)
    .addBiome("minecraft:warped_forest", 0.20f)
    .addBiome("minecraft:soul_sand_valley", 0.15f)

    // Lava surface
    .surfaceBlock("minecraft:magma_block")
    .subsurfaceBlock("minecraft:netherrack")
    .defaultFluid("minecraft:lava")
    .seaLevel(0)
    .generate();
```

### Custom Biome Parameters Example

```java
PlanetMaker.planet("custom")
    .clearBiomes()
    // Full control over biome climate parameters
    .addBiome("minecraft:ice_spikes",
        -0.9f,  // temperature (very cold)
        -0.5f,  // humidity (dry)
        0.7f,   // continentalness (inland)
        0.3f,   // erosion (moderate)
        0.8f,   // depth (high elevation)
        0.2f)   // weirdness (slight variation)
    .generate();

    // Earth-like surface blocks
    .surfaceBlock("minecraft:grass_block")
    .subsurfaceBlock("minecraft:dirt")
    .deepBlock("minecraft:stone")
    .underwaterBlock("minecraft:cobblestone")

    // Balanced ore generation
    .veinToggle(0.8f)
    .veinRidged(0.6f)
    .veinGap(0.2f)
    .oreVeinDensity(1.0f)
    .addCustomOreVein("minecraft:diamond_ore")
    .addCustomOreVein("minecraft:iron_ore")
    .addCustomOreVein("minecraft:copper_ore")

    // Standard world configuration
    .worldDimensions(-64, 384)
    .seaLevel(64)
    .skyColor(0x87CEEB)
    .fogColor(0xC0C0C0)
    .generate();
```

### Extreme Stress Test Planet (Hemphy)

```java
PlanetMaker.planet("hemphy")
    // EXTREME terrain shaping - pushes all limits
    .continentalScale(50.0f)        // MAX: Massive continental variations
    .erosionScale(100.0f)           // MAX: Extreme erosion patterns
    .ridgeScale(25.0f)              // MAX: Towering ridge formations
    .heightVariation(50f, 25f, 15f, 10f)  // MAX: All height variations

    // MAXED noise parameters
    .temperatureScale(10.0f)        // MAX: Wild temperature variations
    .densityFactor(5.0f)            // MAX: Extreme density multiplication
    .densityOffset(2.0f)            // MAX: Maximum density offset

    // EXTREME vertical gradients
    .verticalGradient(-128, 512, 10.0f, -10.0f)  // MAX: Extreme height range
    .gradientMultiplier(3.0f)       // MAX: Triple gradient strength

    // EXTREME mountain generation
    .jaggednessScale(1.0f)          // MAX: Maximum mountain jaggedness
    .jaggednessNoiseScale(2000.0f)  // MAX: Ultra-high scale terrain
    .depthFactor(5.0f)              // MAX: Extreme depth variation
    .terrainFactor(3.0f)            // MAX: Triple terrain intensity

    // Exotic surface blocks
    .surfaceBlock("minecraft:magma_block")
    .subsurfaceBlock("minecraft:netherrack")
    .deepBlock("minecraft:blackstone")
    .defaultBlock("minecraft:crying_obsidian")
    .defaultFluid("minecraft:lava")

    // MAXED ore generation
    .veinToggle(1.0f)               // MAX: Full vein activation
    .veinRidged(1.0f)               // MAX: Maximum ridged veins
    .veinGap(0.0f)                  // MIN: No gaps (solid veins)
    .oreVeinDensity(3.0f)           // MAX: Triple ore density
    .maxOreVeinCount(50)            // MAX: 50 different ore types

    // Extended world dimensions
    .worldDimensions(-128, 512)     // MAX: 640 total height
    .noiseSize(4, 4)                // MAX: Highest resolution noise
    .seaLevel(0)                    // MIN: No sea level (lava world)

    // Alien visuals
    .skyColor(0xFF00FF)             // Magenta alien sky
    .fogColor(0x00FFFF)             // Cyan alien fog
    .ambientLight(1.0f)             // MAX: Always bright
    .generate();
```

### Mars-like Rocky Planet

```java
PlanetMaker.planet("mars")
    // Martian terrain characteristics
    .continentalScale(1.5f)
    .erosionScale(15f)
    .ridgeScale(2f)
    .heightVariation(3f, 2f, 0.8f, 0.5f)

    // Atmospheric dust effects
    .temperatureNoise(0.3f)
    .vegetationNoise(0.1f)
    .barrierNoise(0.2f)
    .fluidLevelFloodedness(0.1f)

    // Dramatic mountain generation
    .jaggednessScale(0.7f)          // High jaggedness for peaks
    .jaggednessNoiseScale(1200.0f)  // Dramatic terrain scale
    .depthFactor(2.0f)              // Deep canyons and high mountains
    .terrainFactor(1.8f)            // Intense terrain features

    // Mars surface blocks
    .surfaceBlock("adastramekanized:mars_sand")
    .subsurfaceBlock("adastramekanized:mars_stone")
    .deepBlock("minecraft:stone")
    .seaLevel(48)

    // Enhanced ore generation
    .veinToggle(0.8f)
    .veinRidged(0.6f)
    .veinGap(0.4f)

    // Mars atmosphere
    .skyColor(0xD2691E)             // Orange-brown sky
    .fogColor(0xCD853F)             // Sandy fog
    .hasAtmosphere(true)
    .ambientLight(0.2f)
    .generate();
```

## Technical Implementation Details

### JSON Generation

The PlanetMaker system automatically generates the following files:
- `dimension/<planet_name>.json` - Dimension configuration
- `worldgen/noise_settings/<planet_name>.json` - Noise parameters and surface rules
- Biome and dimension type files as needed

### Surface Rule Structure

Surface rules are generated with proper validation including required `surface_type` parameters:

```json
{
  "type": "minecraft:condition",
  "if_true": {
    "type": "minecraft:stone_depth",
    "offset": 16,
    "surface_type": "floor",
    "add_surface_depth": false,
    "secondary_depth_range": 16
  },
  "then_run": {
    "type": "minecraft:block",
    "result_state": {
      "Name": "minecraft:diamond_ore"
    }
  }
}
```

### Noise Router Configuration

All noise parameters are properly scaled and validated for Minecraft's noise system:

```json
"noise_router": {
  "continents": {
    "type": "minecraft:shifted_noise",
    "noise": "minecraft:continentalness",
    "xz_scale": 25.0,
    "y_scale": 1,
    "shift_x": "minecraft:shift_x",
    "shift_z": "minecraft:shift_z"
  }
}
```

### Parameter Validation and Safe Ranges

#### Critical Limits
- **noiseSize**: Maximum 4x4 (Minecraft validation limit)
- **worldDimensions**: Height range must be divisible by 16
- **oreVeinCount**: Practical limit around 50 for performance
- **jaggednessNoiseScale**: Upper safe limit around 2000.0

#### Performance Considerations
- Higher noise resolution (4x4) increases chunk generation time
- Extreme ore density (3.0+) can impact world loading
- Complex surface rules with many conditions affect performance

## Planet Generation Workflow

### Using PlanetGenerationRunner

1. Configure planets in `PlanetGenerationRunner.configurePlanets()`
2. Run generation: `PlanetMaker.generateAllPlanets()`
3. Files are automatically generated in `src/main/resources/data/adastramekanized/`

### Testing and Debugging

Use the built-in planet commands for testing:
```
/planet list                          # View all generated planets
/planet teleport <planet_name>        # Travel to specific planet
/planet info <planet_name>           # View planet details
```

### Regeneration Process

To regenerate planets:
1. Set `OVERWRITE_EXISTING = true` in PlanetGenerationTool
2. Run PlanetGenerationTool to regenerate all planet files
3. Restart Minecraft to load new configurations

## Cave Generation System

The cave generation system provides comprehensive control over underground cave networks, ravines, and cave decorations.

### Cave Configuration

#### Basic Cave Parameters
- **caveConfig(float frequency, float size)**: Configure cave generation
  - frequency: Cave generation frequency (0.0-2.0)
  - size: Cave tunnel size multiplier (0.5-3.0)
- **caveYScale(float)**: Vertical stretch of caves (0.1-2.0)
- **ravineConfig(float frequency, float depth)**: Configure ravines
  - frequency: Ravine generation chance (0.0-1.0)
  - depth: Ravine depth multiplier (1.0-5.0)

#### Cave Types
- **cheeseCaves(boolean)**: Enable/disable large open cave systems
- **spaghettiCaves(boolean)**: Enable/disable winding tunnel systems
- **noodleCaves(boolean)**: Enable/disable thin winding tunnels

#### Special Cave Types
- **lavaTubes(boolean)**: Enable lava tube generation with specific characteristics
- **crystalCaves(boolean)**: Enable crystal-lined cave systems
- **iceCaves(boolean)**: Enable ice-themed cave generation

#### Flooded Caves
- **floodedCaves(String fluid, float level)**: Configure fluid-filled caves
  - fluid: Fluid type (e.g., "minecraft:water", "minecraft:lava")
  - level: Y level where caves flood

#### Cave Decorations
- **addCaveDecoration(String block, float frequency, int minHeight, int maxHeight, boolean ceiling)**
  - block: Block to use as decoration
  - frequency: How often it appears (0.0-1.0)
  - minHeight/maxHeight: Y level range
  - ceiling: true for ceiling decorations, false for floor

### Cave Presets

```java
.addCavePreset("standard")    // Normal Earth-like caves
.addCavePreset("volcanic")    // Lava tubes and volcanic caves
.addCavePreset("underwater")  // Flooded cave systems
.addCavePreset("crystal")     // Crystal-decorated caves
.addCavePreset("frozen")      // Ice cave systems
.addCavePreset("massive")     // Large open caverns
.addCavePreset("dense")       // Many interconnected tunnels
.addCavePreset("minimal")     // Sparse cave generation
.addCavePreset("none")        // No caves
```

### Example Cave Configurations

#### Volcanic Planet (Hemphy)
```java
.addCavePreset("lava_tubes")
.ravineConfig(0.3f, 5.0f)
.addCaveDecoration("minecraft:blackstone", 0.3f, -128, 128, false)
.addCaveDecoration("minecraft:basalt", 0.2f, -128, 128, true)
.addCaveDecoration("minecraft:ancient_debris", 0.01f, -64, 32, false)
```

#### Earth-like Planet (Oretest)
```java
.addCavePreset("standard")
.floodedCaves("minecraft:water", 10f)
.addCaveDecoration("minecraft:dripstone_block", 0.1f, -64, 64, true)
.addCaveDecoration("minecraft:pointed_dripstone", 0.08f, -64, 64, true)
.addCaveDecoration("minecraft:moss_block", 0.05f, -32, 64, false)
.addCaveDecoration("minecraft:glow_lichen", 0.15f, -64, 256, true)
```

## Common Issues and Solutions

### Ore Generation Problems
- **Diamond ore flooding**: Use noise-based rarity thresholds instead of simple depth rules
- **No ore spawning**: Ensure `oreVeinsEnabled(true)` and proper threshold values
- **Unbalanced distribution**: Adjust `min_threshold` values for ore rarity

### Terrain Generation Issues
- **Flat terrain**: Increase `heightVariation` parameters and terrain factors
- **Too extreme terrain**: Reduce `jaggednessScale` and `depthFactor`
- **Missing features**: Verify noise router parameters are within valid ranges

### Surface Block Problems
- **Unwanted default blocks**: Use prevention flags and proper surface rules
- **Missing custom blocks**: Ensure block IDs are valid and mod-specific blocks exist
- **Surface rule conflicts**: Order surface rules from most specific to least specific

### JSON Validation Errors
- **Missing surface_type**: All stone_depth conditions require `surface_type` parameter
- **Invalid noise values**: Ensure all noise parameters are within 0.0-1.0 range where applicable
- **Malformed structure**: Use proper JSON formatting and validate against Minecraft schemas