# Planetary Generation Guide

This guide explains how to configure planetary generation, spawning, and atmospheric attributes in the AdAstra Mekanized mod.

## Planet Definition Files

Planets are defined using JSON files located in `src/main/resources/data/adastramekanized/planets/`. Each planet has its own JSON file (e.g., `mars.json`, `moon.json`, `glacio.json`).

### Basic Planet Structure

```json
{
  "id": "adastramekanized:planet_name",
  "display_name": "Planet Display Name",
  "properties": { ... },
  "atmosphere": { ... },
  "dimension": { ... },
  "rendering": { ... }
}
```

## Planet Properties

The `properties` section defines core planetary characteristics:

```json
"properties": {
  "gravity": 0.379,           // Gravity multiplier (1.0 = Earth gravity)
  "temperature": -65.0,       // Average temperature in Celsius
  "day_length": 24.6,         // Length of day in hours
  "orbit_distance": 228,      // Distance from sun in millions of km
  "has_rings": false,         // Whether planet has ring system
  "moon_count": 2            // Number of moons
}
```

### Gravity Settings
- `0.165` - Moon gravity (very low)
- `0.379` - Mars gravity (low)
- `1.0` - Earth gravity (normal)
- `2.5+` - High gravity planets

### Temperature Guidelines
- `-173.0` - Moon (extreme cold)
- `-65.0` - Mars (cold)
- `-20.0` - Glacio (cold but habitable)
- `15.0` - Earth-like temperature
- `200.0+` - Venus-like (extreme heat)

## Atmosphere Configuration

The `atmosphere` section controls breathability and atmospheric composition:

```json
"atmosphere": {
  "has_atmosphere": true,     // Whether planet has atmosphere
  "breathable": false,        // Can players breathe without suit
  "pressure": 0.006,          // Atmospheric pressure (1.0 = Earth)
  "oxygen_level": 0.001,      // Oxygen percentage (0.21 = Earth)
  "type": "THIN"             // Atmosphere type
}
```

### Atmosphere Types
- `"NONE"` - No atmosphere (Moon, asteroids)
- `"THIN"` - Thin atmosphere (Mars)
- `"NORMAL"` - Earth-like atmosphere
- `"THICK"` - Dense atmosphere (Venus-like)

### Pressure Guidelines
- `0.0` - No atmosphere
- `0.006` - Mars (very thin)
- `1.0` - Earth pressure
- `90.0+` - Venus-like (crushing)

## Dimension Settings

The `dimension` section configures the world generation:

```json
"dimension": {
  "dimension_type": "adastramekanized:mars_type",
  "biome_source": "adastramekanized:mars_biome_source",
  "chunk_generator": "adastramekanized:mars_generator",
  "is_orbital": false,        // Is this an orbital station/ship
  "sky_color": 15510660,      // Sky color as RGB integer
  "fog_color": 13791774,      // Fog color as RGB integer
  "ambient_light": 0.6        // Ambient light level (0.0-1.0)
}
```

### Color Conversion
Colors are stored as RGB integers. Convert using:
- Red: `(color >> 16) & 0xFF`
- Green: `(color >> 8) & 0xFF`
- Blue: `color & 0xFF`

Common colors:
- `0` - Black (space)
- `15510660` - Mars orange/red sky
- `12640255` - Glacio blue-white sky
- `16777215` - White

## Atmospheric Rendering

The `rendering` section controls visual appearance:

### Sky Configuration
```json
"sky": {
  "sky_color": 15510660,      // Base sky color
  "sunrise_color": 14349555,  // Sunrise/sunset color
  "custom_sky": true,         // Use custom sky rendering
  "has_stars": true,          // Show stars in sky
  "star_count": 8000,         // Number of visible stars
  "star_brightness": 0.8      // Star brightness (0.0-2.0)
}
```

### Fog Settings
```json
"fog": {
  "fog_color": 13791774,      // Fog color
  "has_fog": true,            // Enable fog effects
  "fog_density": 0.4,         // Fog thickness (0.0-1.0)
  "near_plane": 24.0,         // Near fog distance
  "far_plane": 192.0          // Far fog distance
}
```

### Celestial Bodies
```json
"celestial_bodies": {
  "sun": {
    "scale": 0.6,             // Sun size multiplier
    "color": 16769205,        // Sun color
    "visible": true           // Show sun
  },
  "moons": [
    {
      "texture": "minecraft:textures/environment/moon_phases.png",
      "scale": 0.3,           // Moon size
      "color": 11184810,      // Moon color tint
      "distance": 2.0,        // Distance from center
      "visible": true
    }
  ],
  "visible_planets": [
    {
      "texture": "minecraft:textures/entity/enderdragon/dragon.png",
      "scale": 4.0,           // Planet size in sky
      "color": 6737151,       // Planet color
      "distance": 1.0,        // Distance from center
      "visible": true
    }
  ]
}
```

### Weather and Particles
```json
"weather": {
  "has_clouds": false,        // Show clouds
  "has_rain": false,          // Allow rain
  "has_snow": false,          // Allow snow
  "has_storms": true,         // Dust storms
  "rain_acidity": 0.0         // Acid rain damage
},
"particles": {
  "has_dust": true,           // Dust particles
  "has_ash": false,           // Ash particles
  "has_spores": false,        // Spore particles
  "has_snowfall": false,      // Snow particles
  "particle_density": 0.8,    // Particle spawn rate
  "particle_color": 13791774  // Particle color
}
```

## Dimension Effects Classes

For advanced atmospheric control, create Java classes extending `DimensionSpecialEffects`:

```java
public class PlanetDimensionEffects extends DimensionSpecialEffects {
    public PlanetDimensionEffects() {
        super(
            Float.NaN,         // cloudLevel (Float.NaN = disabled)
            true,              // hasGround (horizon fog)
            SkyType.NORMAL,    // skyType (NORMAL/NONE/END)
            false,             // forceBrightLightmap
            false              // constantAmbientLight
        );
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 biomeFogColor, float daylight) {
        // Return custom fog color based on time of day
        float red = 0.8f + (daylight * 0.2f);
        float green = 0.4f + (daylight * 0.3f);
        float blue = 0.2f + (daylight * 0.1f);
        return new Vec3(red, green, blue);
    }

    @Override
    public boolean isFoggyAt(int x, int z) {
        // Return true for foggy areas
        return false;
    }

    @Override
    public float[] getSunriseColor(float timeOfDay, float partialTicks) {
        // Return sunrise/sunset colors [r, g, b, alpha]
        if (timeOfDay >= 0.75f || timeOfDay <= 0.25f) {
            return new float[]{0.6f, 0.7f, 0.9f, 0.3f};
        }
        return null;
    }
}
```

### Sky Types
- `SkyType.NORMAL` - Standard sky with horizon, allows stars and celestial bodies
- `SkyType.NONE` - No sky rendering, pure void
- `SkyType.END` - End-like sky with fixed color

## Best Practices

### Realistic Planets
1. **Match gravity to size**: Smaller planets = lower gravity
2. **Atmosphere affects visibility**: Thick atmospheres = more fog
3. **Temperature affects weather**: Cold planets = no rain, possible snow
4. **Star visibility**: No atmosphere = more/brighter stars

### Performance Considerations
1. **Particle density**: Keep under 1.0 for performance
2. **Star count**: Limit to 20,000 maximum
3. **Fog distance**: Shorter distances improve performance

### Visual Coherence
1. **Color coordination**: Match sky, fog, and particle colors
2. **Lighting consistency**: Ambient light should match atmosphere
3. **Celestial body realism**: Consider actual astronomy

## Example Configurations

### Earth-like Planet
```json
"properties": { "gravity": 1.0, "temperature": 15.0 },
"atmosphere": { "breathable": true, "pressure": 1.0, "type": "NORMAL" },
"rendering": {
  "sky": { "has_stars": true, "star_count": 6000, "star_brightness": 0.5 },
  "weather": { "has_clouds": true, "has_rain": true }
}
```

### Airless Moon
```json
"properties": { "gravity": 0.165, "temperature": -173.0 },
"atmosphere": { "has_atmosphere": false, "type": "NONE" },
"rendering": {
  "sky": { "sky_color": 0, "has_stars": true, "star_count": 15000, "star_brightness": 1.5 },
  "weather": { "has_clouds": false, "has_rain": false }
}
```

### Desert Planet
```json
"properties": { "gravity": 0.8, "temperature": 45.0 },
"atmosphere": { "breathable": false, "pressure": 0.3, "type": "THIN" },
"rendering": {
  "particles": { "has_dust": true, "particle_density": 1.2 },
  "weather": { "has_storms": true, "has_rain": false }
}
```

## Known Limitations

### Celestial Body Rendering
**Current Issue**: The `celestial_bodies` configuration in JSON files is loaded but not currently rendered.

**Cause**: Celestial body rendering requires integration with Ad Astra's `SkyRenderable` system, which is separate from standard Minecraft atmospheric effects.

**What Works**:
- Sky colors, fog colors, stars, atmospheric effects
- DimensionSpecialEffects fog rendering
- Basic atmospheric properties

**What Doesn't Work Yet**:
- Sun scale/color customization from JSON
- Moon visibility/scale from JSON
- Planet visibility in sky from JSON
- Custom celestial body textures

**Technical Details**:
- Atmospheric colors are handled by `DimensionSpecialEffects` classes
- Celestial bodies require Ad Astra's `ModSkyRenderer` and `SkyRenderable` system
- JSON `celestial_bodies` data needs bridge to Ad Astra's planet renderer system
- Ad Astra uses separate `assets/ad_astra/planet_renderers/` JSON files

**Future Implementation**:
To enable celestial body configuration, we need to:
1. Create bridge between `CelestialBodies` JSON data and Ad Astra's `SkyRenderable` format
2. Generate/register Ad Astra planet renderer files dynamically
3. Integrate with `AdAstraPlanetRenderers` registration system

### Testing Configuration Changes
Use extreme values to verify changes are applied:
```json
"sky_color": 16711935,    // Bright magenta for testing
"fog_color": 16711935,    // Should be visible immediately
"ambient_light": 0.9      // Much brighter than normal
```

If atmospheric changes work but celestial bodies don't, this confirms the known limitation above.

## Block Replacement System

### Overview

The planetary generation system in Ad Astra Mekanized uses a **hybrid approach** that combines template dimensions with custom block replacement capabilities. This allows dynamic planets to have unique terrain composition while leveraging existing dimension infrastructure.

### Current Implementation Status

**Template-Based Foundation**:
- Dynamic planets map to template dimensions (Overworld, Nether, End) based on their `DimensionEffectsType`
- Template mapping provides basic world structure and physics
- 5 template types: MOON_LIKE → End, VOLCANIC → Nether, ROCKY/ICE_WORLD/GAS_GIANT → Overworld

**Block Replacement Architecture**:
- `PlanetChunkGenerator` serves as the core extensible generation system
- Custom block replacement is handled through the chunk generation pipeline
- Planet-specific block mappings can override template terrain

### PlanetChunkGenerator Architecture

The current `PlanetChunkGenerator` in `src/main/java/com/hecookin/adastramekanized/common/worldgen/PlanetChunkGenerator.java` provides an extensible foundation for complete terrain customization:

```java
public class PlanetChunkGenerator extends NoiseBasedChunkGenerator {
    private final DynamicPlanetData planetData;
    private final Map<Block, Block> blockReplacements;

    // Core generation methods
    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(...) {
        // Custom block placement logic
        // Overrides template terrain with planet-specific blocks
    }

    @Override
    public void buildSurface(...) {
        // Surface layer customization
        // Applies atmosphere-dependent surface materials
    }
}
```

### Block Replacement Capabilities

**Current Features**:
- Template dimension mapping for basic structure
- Chunk-level block replacement during generation
- Planet-specific surface composition
- Atmosphere-dependent material selection

**Planned Block Replacement Features** (TODOs in PlanetChunkGenerator):

1. **Terrain Composition**:
   ```java
   // TODO: Planet-specific ore distributions
   // TODO: Rare element concentrations (helium-3, exotic crystals)
   // TODO: Atmosphere-dependent surface layers
   // TODO: Temperature-based material selection
   ```

2. **Resource Generation**:
   ```java
   // TODO: Custom ore generation patterns
   // TODO: Planet-unique resource types
   // TODO: Gravity-affected ore distribution
   // TODO: Atmospheric chemistry-based deposits
   ```

3. **Surface Features**:
   ```java
   // TODO: Crater generation for airless worlds
   // TODO: Volcanic features for high-temperature planets
   // TODO: Ice formations for cold worlds
   // TODO: Wind erosion patterns for atmospheric worlds
   ```

### Integration with Template System

**Template Dimension Usage**:
- Templates provide basic physics (gravity, atmosphere rendering)
- Templates handle dimension infrastructure (ServerLevel, registration)
- Block replacement occurs during chunk generation, not dimension creation

**Block Replacement Process**:
1. Template dimension provides base world structure
2. `PlanetChunkGenerator` intercepts chunk generation
3. Planet-specific block mappings applied during `fillFromNoise()`
4. Surface customization applied during `buildSurface()`
5. Features added during `applyCarvers()` and `addFeatures()`

### Extensible Generation Systems

The `PlanetChunkGenerator` is designed for modular expansion:

```java
// TODO: Modular generation systems
// private final Map<Class<? extends IWorldGenSystem>, IWorldGenSystem> generationSystems;

// Example future systems:
// - BiomeGenSystem: Planet-specific biome distributions
// - OreGenSystem: Custom ore placement algorithms
// - FeatureGenSystem: Unique planetary features
// - AtmosphereGenSystem: Atmosphere-dependent generation
```

### Performance Considerations

**Template Efficiency**:
- Template mapping avoids creating actual new dimensions
- Lazy loading with LRU eviction (max 10 loaded simultaneously)
- Block replacement occurs only during active chunk generation

**Generation Optimization**:
- Block replacements cached per planet type
- Chunk-level batching for block updates
- Minimal overhead for template dimension access

### Configuration Integration

**Planet JSON Integration**:
Block replacement settings will integrate with existing planet JSON files:

```json
"terrain": {
  "surface_block": "adastramekanized:mars_stone",
  "subsurface_block": "adastramekanized:mars_regolith",
  "ore_replacements": {
    "minecraft:iron_ore": "adastramekanized:mars_iron_ore",
    "minecraft:stone": "adastramekanized:mars_stone"
  },
  "rare_elements": [
    {
      "type": "adastramekanized:helium_3_ore",
      "rarity": 0.001,
      "depth_range": [10, 30]
    }
  ]
}
```

**Template Type Mapping**:
- MOON_LIKE: Stone → Lunar regolith, remove water/vegetation
- ROCKY: Stone → Mars-like rock, iron-rich deposits
- VOLCANIC: Stone → Volcanic rock, lava pockets, sulfur deposits
- ICE_WORLD: Stone → Frozen materials, water ice, methane
- GAS_GIANT: Floating islands, dense atmosphere blocks

### Future Roadmap

**Phase 1 - Basic Block Replacement** (Next Priority):
- Implement core block mapping system in `PlanetChunkGenerator`
- Add surface layer customization
- Integrate with existing template system

**Phase 2 - Advanced Terrain Features**:
- Custom ore generation patterns
- Atmosphere-dependent surface features
- Planet-specific resource distributions

**Phase 3 - Complete Customization**:
- Full biome system override
- Custom structure generation
- Unique planetary features (craters, geysers, etc.)

**Phase 4 - Ecosystem Integration**:
- Planet-specific mob spawning
- Atmospheric life support systems
- Resource extraction mechanics

### Technical Notes

**Block Replacement vs New Dimensions**:
- Block replacement approach chosen over creating new dimension types
- Avoids complex dimension registration and mod compatibility issues
- Maintains compatibility with existing dimension-based systems
- Allows unlimited planet variety without registry limits

**Integration Points**:
- `EnhancedRuntimeDimensionRegistry`: Template dimension mapping
- `DynamicPlanetData`: Planet-specific configuration storage
- `PlanetChunkGenerator`: Core block replacement implementation
- Planet JSON files: Configuration data source

---

*This guide will be updated as new features and generation options are discovered and implemented.*