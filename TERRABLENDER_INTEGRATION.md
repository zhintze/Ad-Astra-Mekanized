# TerraBlender Integration Guide

## Overview

Ad Astra Mekanized uses a hybrid TerraBlender integration system that provides full control over planetary terrain generation, biome distribution, and surface materials. This comprehensive guide covers current implementation status, configuration options, troubleshooting, and future development roadmap.

## Current Implementation Status ✅

**Active TerraBlender Integration:**

- ✅ **TerraBlender loaded**: Version 4.1.0.8
- ✅ **Moon region registered**: `adastramekanized:moon_region to index 5 for type OVERWORLD`
- ✅ **Surface rules active**: Moon template surface rules working
- ✅ **Biome injection working**: 2 lunar biomes (highlands + maria)

**Architecture Flow:**

```
Moon Dimension: minecraft:noise generator → TerraBlender → Custom Surface Rules & Biomes
Venus Dimension: adastramekanized:planet generator → Fixed biome → Custom generation
```

## System Architecture

### Hybrid Generation Strategy

| Planet | Generator Type            | Biome System            | Surface Rules    | Status  |
| ------ | ------------------------- | ----------------------- | ---------------- | ------- |
| Moon   | `minecraft:noise`         | TerraBlender (2 biomes) | Template-based   | Active  |
| Venus  | `adastramekanized:planet` | Fixed biome             | Custom generator | Active  |
| Mars   | N/A                       | N/A                     | N/A              | Removed |

### Key Files Structure

```
src/main/java/com/hecookin/adastramekanized/
├── common/biomes/
│   ├── TerraBlenderIntegration.java        # Main integration coordinator
│   ├── PlanetBiomes.java                   # Biome registry for TerraBlender
│   ├── TemplateSurfaceRules.java           # Dynamic surface rule generation
│   └── regions/MoonRegion.java             # Moon-specific biome distribution
├── common/config/
│   ├── StaticPlanetConfig.java             # Planet type definitions & materials
│   └── StaticDimensionManager.java         # Generation strategy coordinator
└── common/worldgen/
    └── PlanetChunkGenerator.java           # Custom chunk generator (Venus)

src/main/resources/data/adastramekanized/
├── dimension/moon.json                     # Moon dimension configuration
└── worldgen/noise_settings/moon.json      # Moon terrain noise settings
```

## Configuration and Customization

### 🌍 Terrain Shape & Height Control

**File: `src/main/resources/data/adastramekanized/worldgen/noise_settings/moon.json`**

**Key parameters for terrain modification:**

```json
"noise_router": {
  "continents": {
    "xz_scale": 0.25,     // Terrain smoothness: Lower = smoother
    "argument2": 0.1      // Height variation: Lower = flatter
  },
  "erosion": {
    "xz_scale": 0.25,     // Erosion pattern scale
    "argument2": 0.05     // Erosion intensity: Lower = smoother
  }
}
```

**Terrain Intensity Scale:**

- `xz_scale: 0.1` = Ultra-smooth, minimal variation
- `xz_scale: 0.25` = Gentle hills (current Moon setting)
- `xz_scale: 0.5` = Moderate terrain variation
- `xz_scale: 1.0` = Standard Minecraft terrain
- `xz_scale: 2.0` = Extreme mountainous terrain

### 🎨 Surface Materials & Block Placement

**Configuration File: `src/main/java/com/hecookin/adastramekanized/common/config/StaticPlanetConfig.java`**

```java
// Lunar terrain materials
DEFAULT_TERRAIN_CONFIGS.put(PlanetType.LUNAR, new TerrainConfig(
    64, 32,
    "minecraft:light_gray_concrete",    // Surface layer
    "minecraft:gray_concrete",          // Subsurface layer
    "minecraft:stone",                  // Deep underground layer
    true, 0.6  // generateCraters, roughness
));

// Volcanic terrain materials
DEFAULT_TERRAIN_CONFIGS.put(PlanetType.VOLCANIC, new TerrainConfig(
    68, 40,
    "minecraft:magma_block",            // Surface layer
    "minecraft:netherrack",             // Subsurface layer
    "minecraft:blackstone",             // Deep layer
    false, 0.8
));
```

### 🏞️ Biome Distribution

**File: `src/main/java/com/hecookin/adastramekanized/common/biomes/regions/MoonRegion.java`**

**Climate parameter system controls biome placement:**

```java
// Lunar Highlands (cold, dry, elevated areas)
Climate.ParameterPoint highlandsPoint = Climate.parameters(
    Climate.Parameter.point(-0.8f), // Temperature: -1.0 (frozen) to 1.0 (hot)
    Climate.Parameter.point(-0.9f), // Humidity: -1.0 (dry) to 1.0 (wet)
    Climate.Parameter.point(0.4f),  // Continentalness: -1.0 (ocean) to 1.0 (inland)
    Climate.Parameter.point(-0.4f), // Erosion: -1.0 (flat) to 1.0 (eroded)
    Climate.Parameter.point(0.0f),  // Depth: -1.0 (valley) to 1.0 (peak)
    Climate.Parameter.point(0.0f),  // Weirdness: -1.0 to 1.0 (terrain oddness)
    0.0f // Offset priority
);
```

## Troubleshooting

### Venus Crash Fix - Custom Chunk Generator Bypass

**Root Cause**: TerraBlender operates globally across ALL dimensions using `minecraft:noise` chunk generators, causing biome array index conflicts when custom biomes aren't properly registered in the global arrays.

**Solution Implemented**: Created selective TerraBlender bypass for Venus using custom chunk generator architecture:

#### Technical Implementation:

1. **Custom Chunk Generator Registration**:
   
   - `ModChunkGenerators.java` - Registers `adastramekanized:planet` chunk generator type
   - `PlanetChunkGenerator.CODEC` - Modified to handle optional `generation_settings` fields
   - Registered with NeoForge's deferred registry system

2. **Venus Dimension Configuration**:
   
   ```json
   // src/main/resources/data/adastramekanized/dimension/venus.json
   {
     "type": "adastramekanized:venus",
     "generator": {
       "type": "adastramekanized:planet",  // ← Custom generator, not minecraft:noise
       "biome_source": {
         "type": "minecraft:fixed",
         "biome": "minecraft:desert"
       },
       "generation_settings": null,        // ← Optional field, uses defaults
       "planet_id": "adastramekanized:venus"
     }
   }
   ```

#### Verification Results:

- ✅ **Venus teleportation successful**: `Successfully teleported Dev to planet Venus`
- ✅ **Custom chunk generation working**: `PlanetChunkGenerator.fillFromNoise called for planet: adastramekanized:venus`
- ✅ **No crashes**: ArrayIndexOutOfBoundsException completely eliminated
- ✅ **TerraBlender bypass confirmed**: `Generated default Venus terrain (TerraBlender bypass)`

### Common Issues and Solutions

**Issue: Moon generating as "giant pile of lava"**

- **Status**: Under investigation
- **Potential causes**: Aquifer lava generation, surface rules not applying
- **Current workaround**: Check noise settings for lava generation parameters

**Issue: Biome array index out of bounds**

- **Solution**: Use custom chunk generator bypass for problematic planets
- **Prevention**: Ensure all custom biomes are properly registered in TerraBlender arrays

## Customization Examples

### Example 1: Make Moon More Mountainous

Edit `src/main/resources/data/adastramekanized/worldgen/noise_settings/moon.json`:

```json
"continents": {
  "xz_scale": 0.5,      // Increase from 0.25 for more variation
  "argument2": 0.3      // Increase from 0.1 for higher peaks
},
"erosion": {
  "xz_scale": 0.5,      // Increase from 0.25
  "argument2": 0.15     // Increase from 0.05 for more erosion
}
```

### Example 2: Add Third Moon Biome (Lunar Craters)

**Step 1:** Add biome to `PlanetBiomes.java`:

```java
public static final ResourceKey<Biome> LUNAR_CRATERS = register("lunar_craters");
```

**Step 2:** Add biome distribution to `MoonRegion.java`:

```java
// Lunar Craters (very cold, dry, highly eroded areas)
Climate.ParameterPoint cratersPoint = Climate.parameters(
    Climate.Parameter.point(-0.9f), // Very cold
    Climate.Parameter.point(-1.0f), // Completely dry
    Climate.Parameter.point(-0.3f), // Oceanic (crater depressions)
    Climate.Parameter.point(0.8f),  // Highly eroded
    Climate.Parameter.point(-0.5f), // Below surface (crater floors)
    Climate.Parameter.point(0.5f),  // High weirdness for unusual terrain
    0.0f
);
builder.add(cratersPoint, PlanetBiomes.LUNAR_CRATERS);
```

### Example 3: Create New Planet Type

**Step 1:** Add planet type to `StaticPlanetConfig.java`:

```java
public enum PlanetType {
    // ... existing types ...
    OCEANIC("oceanic", "Water-covered world with islands");
}
```

**Step 2:** Add terrain configuration:

```java
DEFAULT_TERRAIN_CONFIGS.put(PlanetType.OCEANIC, new TerrainConfig(
    45, 20,  // Lower base height, less variation for islands
    "minecraft:sand",           // Beach/island surface
    "minecraft:sandstone",      // Island subsurface
    "minecraft:stone",          // Underwater bedrock
    false, 0.3  // No craters, low roughness for gentle islands
));
```

## Development Roadmap

### Phase 1: Enhanced Venus Terrain (Immediate)

**Current Issue**: Venus generates basic netherrack terrain without proper variation
**Solution**: Improve `generateDefaultVenusTerrain()` method in PlanetChunkGenerator

- Add proper terrain noise sampling
- Implement height-based block placement logic
- Add volcanic features and surface variation

### Phase 2: Moon Lava Investigation (High Priority)

**Current Issue**: Moon still generates as "giant pile of lava"
**Investigation needed**:

- Check if Moon uses TerraBlender or custom generator
- Examine Moon noise settings and surface rules
- Verify Moon biome configuration

### Phase 3: Venus TerraBlender Re-Integration (Future)

**Goal**: Migrate Venus back to TerraBlender for full biome diversity
**Approach**: Use successful Venus bypass as template for proper TerraBlender registration
**Requirements**:

1. Register Venus biomes in global TerraBlender biome arrays
2. Create proper Venus region with weighted biome distribution
3. Implement Venus-specific surface rules and climate parameters
4. Test gradual migration path: Custom → Hybrid → Full TerraBlender

### Phase 4: Universal TerraBlender Integration (Long-term)

**Final Goal**: All planets using TerraBlender with programmatic generation control
**Architecture**:

- Mars: Multi-biome TerraBlender regions with Tectonic features
- Moon: Lunar-specific TerraBlender biomes and crater generation
- Venus: Volcanic TerraBlender biomes with atmospheric effects
- Future planets: Template-based TerraBlender region generation

## WorldGen Mod Integration Roadmap

### Phase 1: Foundation and Analysis ✅ COMPLETED

- [x] Configure CurseForge Maven repository
- [x] Add all generation mod dependencies (Terralith, Tectonic, TerraBlender, BOP, etc.)
- [x] Resolve dependency conflicts (Lithostitched)
- [x] Verify successful mod loading
- [x] Analyze Tectonic source code architecture
- [x] Study Terralith datapack structure
- [x] Examine TerraBlender biome injection API

### Phase 2: Integration Architecture Design 🔄 IN PROGRESS

- [x] Design `PlanetTerrainIntegration` class
- [ ] Implement biome set definitions for each planet type
- [ ] Design terrain profile system for varied landscapes
- [ ] Create configuration system for planet-specific parameters

### Phase 3: Basic Integration Implementation ⏳ PENDING

- [ ] Implement basic TerraBlender API integration
- [ ] Create simple biome injection for 2-3 test planets
- [ ] Test biome placement and verify generation
- [ ] Debug and resolve initial integration issues

### Phase 4: Advanced Terrain Features ⏳ PENDING

- [ ] Implement Continental terrain (Earth-like landmasses)
- [ ] Create Archipelago terrain (island chains)
- [ ] Design Mountainous terrain (massive ranges)
- [ ] Implement Crater terrain (Moon-like impacts)
- [ ] Create Canyon terrain (badlands-style)

### Phase 5: Biome Ecosystem Enhancement ⏳ PENDING

- [ ] Integrate Terralith biomes for appropriate planet types
- [ ] Configure biome placement for enhanced variety
- [ ] Test compatibility with planet-specific themes
- [ ] Integrate BOP biomes for unique planet environments

## Performance Considerations

### Biome Weight Guidelines

TerraBlender regions are registered with weights to control their influence:

```java
// Moon region - weight 1 (minimal influence, planet-specific)
Regions.register(new MoonRegion(ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "moon_region"), 1));
```

**Weight recommendations:**

- **Planet-specific regions**: 1-3 (minimal overworld interference)
- **Overworld enhancement**: 5-10 (moderate influence)
- **Major overworld overhaul**: 15+ (high influence)

### Noise Performance

Lower `xz_scale` values improve chunk generation performance:

- `xz_scale: 0.1-0.3` = High performance, smooth terrain
- `xz_scale: 0.5-1.0` = Moderate performance, detailed terrain
- `xz_scale: 1.5+` = Lower performance, complex terrain

## Testing and Debugging

### Logs to Monitor

When testing changes, watch for these log messages:

```
[INFO] Registered region adastramekanized:moon_region to index 5 for type OVERWORLD
[DEBUG] Registered Moon region with weight 1
[DEBUG] Creating lunar surface rules for planet: adastramekanized:moon
[DEBUG] Registered Moon template surface rules
[INFO] TerraBlender integration initialized successfully
```

### File Reload Requirements

After editing:

- **Noise settings** (`moon.json`): Requires new world generation
- **Surface rules** (Java files): Requires mod rebuild (`./gradlew build`)
- **Biome parameters** (Java files): Requires mod rebuild
- **Material configs** (Java files): Requires mod rebuild

### Testing Workflow

1. Make changes to configuration files
2. Run `./gradlew build` if Java files were modified
3. Launch client with `./gradlew runClient`
4. Create new world or teleport to planet: `/planet teleport moon`
5. Verify terrain generation matches expectations

## Technical Lessons Learned

### Key Insight

**TerraBlender operates globally across ALL chunk generation**, not just registered regions. Any dimension using `minecraft:noise` chunk generators triggers TerraBlender's biome resolution system, regardless of biome source type.

### Successful Solution Pattern

The **custom chunk generator bypass** provides a proven solution path:

1. **Selective bypass**: Specific planets can use custom generators while others use TerraBlender
2. **Gradual migration**: Planets can be migrated from custom → TerraBlender individually
3. **Debug capabilities**: Comprehensive logging helps track generation issues
4. **Codec flexibility**: Optional generation settings allow both simple and complex planet configs

## Current Limitations

1. **Venus TerraBlender integration disabled** due to biome array conflicts
2. **Mars completely removed** from generation system
3. **Custom biomes require data files** for full TerraBlender compatibility
4. **Only Moon currently uses full TerraBlender integration**

## Future Expansion

The system is designed for easy expansion:

1. **Add more planet types** to `StaticPlanetConfig.PlanetType`
2. **Create planet-specific regions** following `MoonRegion.java` pattern
3. **Implement complex surface rules** with conditional logic
4. **Add structure generation** through the features system
5. **Enable Venus TerraBlender** once biome conflicts are resolved

This hybrid approach provides the flexibility to use TerraBlender's advanced biome system where needed while maintaining custom generation control for simpler planets.

---

*Last Updated: September 22, 2025*
*Status: Venus crash RESOLVED ✅, Moon lava investigation in progress 🔄*
*Major Breakthrough: Custom chunk generator bypass architecture successful*