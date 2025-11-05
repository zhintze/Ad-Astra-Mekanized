# Tectonic Mod Analysis: World Generation Implementation for Minecraft 1.21 NeoForge

**Source Repository**: https://github.com/Apollounknowndev/tectonic (branch: rewrite-squared)
**Analysis Date**: November 2025
**Purpose**: Understanding Tectonic's world generation techniques for application in Ad Astra Mekanized

---

## Executive Summary

Tectonic is a sophisticated world generation mod that uses **custom density functions** and **configuration-driven terrain generation** to create dramatic, varied terrain without modifying core biome systems. The mod is fully compatible with TerraBlender and other biome mods, focusing purely on terrain shaping rather than biome replacement.

**Key Architecture Principles:**
1. **Datapack-first approach** - Most world generation defined in JSON
2. **Custom density functions** - Configurable noise and constants via Java code
3. **Modular overlay system** - Multiple configuration profiles via resource pack overlays
4. **Configuration-driven** - Runtime-modifiable terrain parameters without recompilation

---

## 1. Architecture Overview

### 1.1 Project Structure

```
tectonic/
├── src/
│   ├── common/main/                          # Shared logic across loaders
│   │   ├── java/dev/worldgen/tectonic/
│   │   │   ├── config/                       # Configuration system
│   │   │   │   ├── ConfigHandler.java        # JSON config loading/saving
│   │   │   │   └── state/                    # Configuration state classes
│   │   │   ├── worldgen/densityfunction/     # Custom density functions
│   │   │   │   ├── ConfigConstant.java       # Configurable constants
│   │   │   │   ├── ConfigNoise.java          # Configurable noise
│   │   │   │   └── Invert.java               # Inversion function
│   │   │   ├── mixin/                        # Minecraft modifications
│   │   │   ├── command/                      # Commands
│   │   │   └── Tectonic.java                 # Main mod class
│   │   └── resources/resourcepacks/tectonic/
│   │       ├── data/minecraft/worldgen/      # Override vanilla worldgen
│   │       │   ├── density_function/
│   │       │   │   └── overworld/
│   │       │   │       └── noise_router/     # 15 custom noise router files
│   │       │   ├── noise/                    # Noise configurations
│   │       │   └── noise_settings/
│   │       │       └── overworld.json        # Main terrain config
│   │       └── data/tectonic/worldgen/       # Custom worldgen data
│   │           ├── density_function/         # 7 files + 8 subdirectories
│   │           │   ├── biome_parameter/      # Erosion, ridges
│   │           │   ├── cave/                 # Cave systems
│   │           │   ├── lava_tunnel/          # Lava tunnel generation
│   │           │   ├── mountain_ridges/      # Mountain features
│   │           │   ├── noise/                # Custom noise functions
│   │           │   │   ├── full_continents.json
│   │           │   │   ├── raw_continents.json
│   │           │   │   ├── raw_islands.json
│   │           │   │   └── continent/ and island/ subdirs
│   │           │   ├── region/               # Regional selectors
│   │           │   ├── terrain_spline/       # Spline-based transitions
│   │           │   ├── underground_river/    # Underground river systems
│   │           │   ├── base_terrain.json
│   │           │   ├── sloped_cheese.json    # Primary terrain shaper
│   │           │   └── caves.json
│   │           ├── configured_feature/       # Custom features
│   │           ├── noise/                    # Noise definitions
│   │           └── placed_feature/           # Feature placement
│   │       └── overlay.*/                    # Configuration overlays
│   │           ├── overlay.1_21_9/           # Version-specific
│   │           ├── overlay.clifftree/        # Cliff tree features
│   │           ├── overlay.datapack/         # Datapack mode
│   │           ├── overlay.mod/              # Mod mode
│   │           ├── overlay.no_carvers/       # Disable carvers
│   │           ├── overlay.terratonic/       # Terratonic integration
│   │           └── overlay.ultrasmooth/      # Ultra-smooth terrain
│   ├── neoforge/1.21.1/main/java/            # NeoForge-specific
│   │   └── dev/worldgen/tectonic/
│   │       ├── TectonicNeoforge.java         # NeoForge initialization
│   │       ├── TectonicNeoforgeClient.java   # Client setup
│   │       └── ConfigResourceCondition.java  # Conditional loading
│   ├── fabric/                               # Fabric loader support
│   └── forge/                                # Legacy Forge support
```

### 1.2 Multi-Loader Design

Tectonic supports **three mod loaders** (Fabric, Forge, NeoForge) using:
- **Common code** in `src/common/main/` - shared logic
- **Loader-specific initialization** in `src/neoforge/`, `src/fabric/`, etc.
- **Version-specific code** in separate directories (1.21.1, 1.21.10)

This is similar to how Ad Astra Mekanized could be structured if multi-loader support is desired in the future.

---

## 2. Custom Density Functions

### 2.1 Registration Process (NeoForge 1.21.1)

**File**: `src/neoforge/1.21.1/main/java/dev/worldgen/tectonic/TectonicNeoforge.java`

```java
public TectonicNeoforge(IEventBus bus) {
    Tectonic.init(NeoForgeHelper.getConfigPath());

    // Register custom density function types
    bus.addListener(this::registerDensityFunctionTypes);
    bus.addListener(this::registerEnabledPacks);
}

private void registerDensityFunctionTypes(RegisterEvent event) {
    event.register(Registries.DENSITY_FUNCTION_TYPE, helper -> {
        // Register custom density functions
        helper.register(id("config_constant"), ConfigConstant.CODEC_HOLDER.codec());
        helper.register(id("config_noise"), ConfigNoise.CODEC_HOLDER.codec());
        helper.register(id("invert"), Invert.CODEC_HOLDER.codec());

        // Register modifiers
        helper.register(id("set_height_limits"), SetHeightLimitsModifier.CODEC);
    });
}
```

**Key Insight**: Custom density functions are registered using NeoForge's `RegisterEvent` with the `DENSITY_FUNCTION_TYPE` registry. Each function needs a unique ID and a codec for serialization.

### 2.2 ConfigNoise - Configuration-Driven Noise

**File**: `src/common/main/java/dev/worldgen/tectonic/worldgen/densityfunction/ConfigNoise.java`

```java
public record ConfigNoise(
    NoiseHolder noise,
    DensityFunction shiftX,
    DensityFunction shiftZ,
    double scale,
    double multiplier,
    double offset
) implements DensityFunction {

    public static ConfigNoise create(Holder<NormalNoise.NoiseParameters> noise,
                                     String key,
                                     DensityFunction shiftX,
                                     DensityFunction shiftZ) {
        // Load runtime configuration
        NoiseState state = ConfigHandler.getState().getNoiseState(key);
        return new ConfigNoise(
            new NoiseHolder(noise),
            shiftX,
            shiftZ,
            state.scale,      // From config
            state.multiplier, // From config
            state.offset      // From config
        );
    }

    @Override
    public double compute(FunctionContext context) {
        // Apply scale and shifts to coordinates
        double x = context.blockX() * scale + shiftX.compute(context);
        double z = context.blockZ() * scale + shiftZ.compute(context);

        // Sample noise and apply multiplier/offset
        return noise.getValue(x, 0, z) * multiplier + offset;
    }

    // Codec for serialization
    public static final KeyDispatchDataCodec<ConfigNoise> CODEC_HOLDER =
        KeyDispatchDataCodec.of(RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                NoiseHolder.CODEC.fieldOf("noise").forGetter(ConfigNoise::noise),
                DensityFunction.HOLDER_HELPER_CODEC.fieldOf("xz_shift_x")
                    .forGetter(ConfigNoise::shiftX),
                DensityFunction.HOLDER_HELPER_CODEC.fieldOf("xz_shift_z")
                    .forGetter(ConfigNoise::shiftZ),
                Codec.DOUBLE.fieldOf("xz_scale").forGetter(ConfigNoise::scale),
                Codec.DOUBLE.fieldOf("multiplier").forGetter(ConfigNoise::multiplier),
                Codec.DOUBLE.fieldOf("offset").forGetter(ConfigNoise::offset)
            ).apply(instance, ConfigNoise::new)
        ));
}
```

**Key Features:**
- **Runtime configuration** - Parameters loaded from config file
- **Noise shifting** - Offset noise sampling for variation
- **Scale/multiply/offset** - Transform noise output range
- **Codec integration** - Proper serialization for datapacks

### 2.3 ConfigConstant - Configuration-Driven Constants

**File**: `src/common/main/java/dev/worldgen/tectonic/worldgen/densityfunction/ConfigConstant.java`

```java
public record ConfigConstant(double value, boolean minMaxHack) implements DensityFunction {

    public static ConfigConstant create(String key, boolean minMaxHack) {
        // Load constant value from configuration
        double value = ConfigHandler.getState().getValue(key);
        return new ConfigConstant(value, minMaxHack);
    }

    @Override
    public double compute(FunctionContext context) {
        return value; // Simple constant return
    }

    @Override
    public void fillArray(double[] array, ContextProvider provider) {
        Arrays.fill(array, value); // Efficient array filling
    }

    @Override
    public double minValue() {
        return minMaxHack ? Double.NEGATIVE_INFINITY : value;
    }

    @Override
    public double maxValue() {
        return minMaxHack ? Double.POSITIVE_INFINITY : value;
    }
}
```

**Key Features:**
- **Simple but powerful** - Constants that can be changed without recompiling
- **Min/max hack** - Allows constants to bypass range checks
- **Efficient filling** - Optimized for array operations

### 2.4 Invert - Simple Transform Function

**File**: `src/common/main/java/dev/worldgen/tectonic/worldgen/densityfunction/Invert.java`

```java
public record Invert(DensityFunction wrapped) implements DensityFunction {

    @Override
    public double compute(FunctionContext context) {
        return -wrapped.compute(context); // Simple negation
    }

    @Override
    public double minValue() {
        return -wrapped.maxValue(); // Inverted range
    }

    @Override
    public double maxValue() {
        return -wrapped.minValue(); // Inverted range
    }
}
```

**Key Features:**
- **Simple transformation** - Negates another density function
- **Proper range handling** - Min/max values are inverted
- **Composable** - Can wrap any density function

---

## 3. Configuration System

### 3.1 ConfigHandler - Central Configuration Management

**File**: `src/common/main/java/dev/worldgen/tectonic/config/ConfigHandler.java`

```java
public class ConfigHandler {
    private static ConfigState LOADED_STATE = getDefaults();

    public static void load(Path path) {
        if (!Files.exists(path)) {
            // Create default config if missing
            save(path);
        }

        try {
            String json = Files.readString(path);
            // Parse using Minecraft's codec system
            ConfigState.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json))
                .resultOrPartial(error -> LOGGER.error("Invalid codec: " + error))
                .ifPresent(state -> LOADED_STATE = state);
        } catch (IOException e) {
            LOGGER.error("Failed to load config", e);
        }
    }

    public static void save(Path path) {
        try {
            // Encode to JSON with 2-space indentation
            ConfigState.CODEC.encodeStart(JsonOps.INSTANCE, LOADED_STATE)
                .resultOrPartial(error -> LOGGER.error("Invalid codec: " + error))
                .ifPresent(json -> {
                    try {
                        Files.writeString(path,
                            new GsonBuilder().setPrettyPrinting().create()
                                .toJson(json));
                    } catch (IOException e) {
                        LOGGER.error("Failed to save config", e);
                    }
                });
        } catch (Exception e) {
            LOGGER.error("Failed to encode config", e);
        }
    }

    public static ConfigState getState() {
        return LOADED_STATE;
    }

    private static ConfigState getDefaults() {
        return new ConfigState(
            GeneralState.getDefaults(),
            GlobalTerrainState.getDefaults(),
            ContinentsState.getDefaults(),
            IslandsState.getDefaults(),
            OceansState.getDefaults(),
            BiomesState.getDefaults(),
            CavesState.getDefaults()
        );
    }
}
```

### 3.2 Configuration Structure

The configuration is organized into **7 major categories**:

1. **GeneralState** - Core mod settings
2. **GlobalTerrainState** - Broad terrain parameters
3. **ContinentsState** - Continental landmass configuration
4. **IslandsState** - Island generation rules
5. **OceansState** - Water body characteristics
6. **BiomesState** - Biome distribution properties
7. **CavesState** - Underground cave system generation

Each state class contains dozens of individual parameters that can be tweaked at runtime.

---

## 4. Density Function Architecture

### 4.1 Noise Router Customization

Tectonic overrides **15 noise router components** in `data/minecraft/worldgen/density_function/overworld/noise_router/`:

1. **barrier.json** - Barrier layer for dimension boundaries
2. **continents.json** - Continental landmass generation
3. **depth.json** - Terrain depth adjustments
4. **erosion.json** - Erosion effects on terrain
5. **final_density.json** - Final density calculations with caves
6. **fluid_level_floodedness.json** - Water flooding behavior
7. **fluid_level_spread.json** - Fluid spreading mechanics
8. **initial_density_without_jaggedness.json** - Base density before roughness
9. **lava.json** - Lava generation parameters
10. **ridges.json** - Ridge formation
11. **temperature.json** - Temperature biome data
12. **vegetation.json** - Vegetation distribution
13. **vein_gap.json** - Ore vein spacing
14. **vein_ridged.json** - Ridged vein patterns
15. **vein_toggle.json** - Vein generation toggle

### 4.2 Custom Density Functions

Tectonic defines **dozens of custom density functions** in `data/tectonic/worldgen/density_function/`:

#### Core Terrain Functions
- **base_terrain.json** - Foundation terrain shape (references sloped_cheese)
- **sloped_cheese.json** - Primary terrain shaper with continental/island logic
- **caves.json** - Cave system density
- **depth.json** - Depth calculations
- **continent_selector.json** - Switches between continent and island modes
- **island_selector.json** - Island presence mask

#### Specialized Systems
- **biome_parameter/** - Erosion and ridge parameters for biomes
- **cave/** - Noodle caves, cheese caves, spaghetti caves
- **lava_tunnel/** - Underground lava tunnel generation
- **mountain_ridges/** - Mountain ridge features
- **terrain_spline/** - Spline-based terrain transitions
- **underground_river/** - Underground river systems

#### Noise Functions
- **noise/full_continents.json** - Combined continent + island noise
- **noise/raw_continents.json** - Base continental noise
- **noise/raw_islands.json** - Base island noise
- **noise/region_selector.json** - Regional variation selector
- **noise/temperature_index.json** - Temperature noise
- **noise/vegetation_index.json** - Vegetation noise

### 4.3 Example: Sloped Cheese (Primary Terrain Shaper)

**File**: `data/tectonic/worldgen/density_function/sloped_cheese.json`

```json
{
  "type": "minecraft:cache_once",
  "argument": {
    "type": "minecraft:add",
    "argument1": {
      "type": "minecraft:mul",
      "argument1": 4,
      "argument2": {
        "type": "minecraft:quarter_negative",
        "argument": {
          "type": "minecraft:add",
          "argument1": {
            "type": "minecraft:mul",
            "argument1": "tectonic:base/depth",
            "argument2": {
              "type": "minecraft:cache_2d",
              "argument": {
                "type": "minecraft:add",
                "argument1": {
                  "type": "minecraft:mul",
                  "argument1": "tectonic:noise/island_jaggedness",
                  "argument2": {
                    "type": "minecraft:cache_2d",
                    "argument": {
                      "type": "minecraft:mul",
                      "argument1": "minecraft:overworld/jagged",
                      "argument2": {
                        "type": "minecraft:noise",
                        "noise": "minecraft:jagged",
                        "xz_scale": 1500.0,
                        "y_scale": 0.0
                      }
                    }
                  }
                },
                "argument2": {
                  "type": "minecraft:mul",
                  "argument1": "tectonic:noise/continental_jaggedness",
                  "argument2": {
                    "type": "minecraft:mul",
                    "argument1": {
                      "type": "minecraft:range_choice",
                      "input": "tectonic:continent_selector",
                      "min_inclusive": -1.0,
                      "max_exclusive": 0.0,
                      "when_in_range": "tectonic:factor/island_factor",
                      "when_out_of_range": "tectonic:factor/continent_factor"
                    },
                    "argument2": "tectonic:mountain_ridges/mountain_ridges_weathered"
                  }
                }
              }
            }
          },
          "argument2": "tectonic:base/sloped_cheese_base"
        }
      }
    },
    "argument2": {
      "type": "minecraft:add",
      "argument1": {
        "type": "minecraft:max",
        "argument1": {
          "type": "minecraft:mul",
          "argument1": "tectonic:region/dune_splines",
          "argument2": "tectonic:region/dune_final"
        },
        "argument2": 0
      },
      "argument2": {
        "type": "minecraft:add",
        "argument1": {
          "type": "minecraft:mul",
          "argument1": {
            "type": "minecraft:spline",
            "spline": {
              "coordinate": "tectonic:noise/full_continents",
              "points": [
                {"location": -1.05, "value": 0.0, "derivative": 0.0},
                {"location": -0.25, "value": 1.0, "derivative": 0.0}
              ]
            }
          },
          "argument2": "tectonic:region/roughness"
        },
        "argument2": {
          "type": "minecraft:mul",
          "argument1": {
            "type": "minecraft:spline",
            "spline": {
              "coordinate": "tectonic:noise/full_continents",
              "points": [
                {"location": -1.0, "value": 1.0, "derivative": 0.0},
                {"location": -0.35, "value": 0.0, "derivative": 0.0}
              ]
            }
          },
          "argument2": "tectonic:region/ocean"
        }
      }
    }
  }
}
```

**Key Techniques Used:**
1. **cache_once** - Performance optimization for expensive calculations
2. **Nested operations** - Complex combinations of add, multiply, quarter_negative
3. **Spline functions** - Smooth transitions between terrain types
4. **Range choice** - Switch between continent/island logic based on selector
5. **Jaggedness** - Mountain features using noise at scale 1500
6. **Multiple layers** - Base terrain + dunes + roughness + ocean features

### 4.4 Example: Full Continents Noise

**File**: `data/tectonic/worldgen/density_function/noise/full_continents.json`

```json
{
  "type": "minecraft:add",
  "argument1": {
    "type": "minecraft:mul",
    "argument1": "tectonic:island_selector",
    "argument2": "tectonic:noise/raw_islands"
  },
  "argument2": {
    "type": "minecraft:mul",
    "argument1": "tectonic:continent_selector",
    "argument2": {
      "type": "minecraft:cache_2d",
      "argument": {
        "type": "minecraft:spline",
        "spline": {
          "coordinate": "tectonic:noise/raw_continents",
          "points": [
            {"location": 0.05, "value": 0.05, "derivative": 1.0},
            {"location": 0.175, "value": 0.3, "derivative": 1.0}
          ]
        }
      }
    }
  }
}
```

**Key Techniques:**
1. **Additive blending** - Combine islands and continents
2. **Selector masks** - Multiply by selectors to control where each applies
3. **Spline transformation** - Remap continental noise to desired output range
4. **cache_2d** - Optimize 2D noise calculations

---

## 5. Noise Settings Configuration

### 5.1 Overworld Noise Settings

**File**: `data/minecraft/worldgen/noise_settings/overworld.json`

Key parameters from the analysis:

```json
{
  "noise": {
    "min_y": -64,
    "height": 384,
    "size_horizontal": 1,
    "size_vertical": 2
  },
  "default_block": {
    "Name": "minecraft:stone"
  },
  "default_fluid": {
    "Name": "minecraft:water",
    "Properties": {"level": "0"}
  },
  "sea_level": 63,

  "noise_router": {
    "continents": "minecraft:overworld/continents",
    "erosion": "minecraft:overworld/erosion",
    "depth": "minecraft:overworld/depth",
    "ridges": "minecraft:overworld/ridges",
    "temperature": "minecraft:overworld/temperature",
    "vegetation": "minecraft:overworld/vegetation",
    "fluid_level_floodedness": "minecraft:overworld/fluid_level_floodedness",
    "fluid_level_spread": "minecraft:overworld/fluid_level_spread",
    "vein_toggle": "minecraft:overworld/vein_toggle",
    "vein_gap": "minecraft:overworld/vein_gap",
    "vein_ridged": "minecraft:overworld/vein_ridged",
    "final_density": "minecraft:overworld/final_density",
    "initial_density_without_jaggedness": "minecraft:overworld/initial_density_without_jaggedness",
    "lava": "minecraft:overworld/lava",
    "barrier": "minecraft:overworld/barrier"
  },

  "spawn_target": [
    {
      "continentalness": [-0.11, 1.0],
      "erosion": [-1.0, 1.0],
      "weirdness": [-1.0, -0.16],
      "depth": 0,
      "offset": 0
    },
    {
      "continentalness": [-0.11, 1.0],
      "erosion": [-1.0, 1.0],
      "weirdness": [0.16, 1.0],
      "depth": 0,
      "offset": 0
    }
  ],

  "surface_rule": {
    "type": "minecraft:sequence",
    "sequence": [
      // Bedrock placement at bottom
      {
        "type": "minecraft:condition",
        "if_true": {
          "type": "minecraft:vertical_gradient",
          "random_name": "minecraft:bedrock_floor",
          "true_at_and_below": {"absolute": 0},
          "false_at_and_above": {"absolute": 5}
        },
        "then_run": {
          "type": "minecraft:block",
          "result_state": {"Name": "minecraft:bedrock"}
        }
      },

      // Deepslate transition
      {
        "type": "minecraft:condition",
        "if_true": {
          "type": "minecraft:vertical_gradient",
          "random_name": "minecraft:deepslate",
          "true_at_and_below": {"absolute": 0},
          "false_at_and_above": {"absolute": 8}
        },
        "then_run": {
          "type": "minecraft:block",
          "result_state": {"Name": "minecraft:deepslate"}
        }
      },

      // Biome-specific surface rules
      // ... hundreds of lines of surface rule conditions ...
    ]
  }
}
```

**Key Insights:**
- **Noise router references** - All point to custom density functions
- **Spawn targets** - Control where players can spawn based on biome parameters
- **Surface rules** - Extensive biome-specific block placement logic
- **Height settings** - Standard Overworld height (-64 to 320)

---

## 6. Overlay System

### 6.1 Configuration Profiles

Tectonic uses **resource pack overlays** to provide multiple configuration profiles:

1. **overlay.1_21_9/** - Version-specific adjustments for Minecraft 1.21.9
2. **overlay.clifftree/** - Enables cliff tree generation features
3. **overlay.datapack/** - Datapack-only mode (no mod features)
4. **overlay.mod/** - Full mod features enabled
5. **overlay.no_carvers/** - Disables vanilla cave carvers
6. **overlay.terratonic/** - Integration with Terratonic mod
7. **overlay.ultrasmooth/** - Ultra-smooth terrain variant

**How overlays work:**
- Each overlay contains partial data that overrides or extends base configuration
- Players can enable/disable overlays when creating worlds
- Overlays allow shipping multiple terrain profiles without separate downloads

### 6.2 Conditional Loading

**File**: `src/neoforge/1.21.1/main/java/dev/worldgen/tectonic/ConfigResourceCondition.java`

Tectonic implements **conditional resource loading** based on configuration:

```java
public record ConfigResourceCondition(ResourceLocation id) implements ICondition {
    public static final MapCodec<ConfigResourceCondition> CODEC =
        RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                ResourceLocation.CODEC.fieldOf("value")
                    .forGetter(ConfigResourceCondition::id)
            ).apply(instance, ConfigResourceCondition::new)
        );

    @Override
    public boolean test(IContext context) {
        // Check if feature is enabled in config
        return ConfigHandler.getState().isFeatureEnabled(id);
    }
}
```

This allows datapacks to conditionally load based on user configuration, providing fine-grained control over which features are active.

---

## 7. Biome Compatibility

### 7.1 No Custom Biomes

**Important**: Tectonic does **not add custom biomes** in the traditional sense. It only modifies:
- Terrain generation (mountains, valleys, caves)
- Feature placement (trees, ores, structures)
- Biome distribution (how vanilla biomes are arranged)

### 7.2 TerraBlender Compatibility

From the mod description:
> "Tectonic is compatible with mods that rely on Terrablender or Biolith to add their biomes to worldgen, including Biomes O Plenty, Regions Unexplored, Nature's Spirit, and more."

**How this works:**
- Tectonic only modifies the **noise router** and **density functions**
- Biome placement still uses vanilla or TerraBlender systems
- Custom biomes from other mods work seamlessly
- The terrain just looks more dramatic/interesting

### 7.3 Underground River Biomes

Tectonic does add **6 underground river biomes**:
1. Lush River
2. Dripstone River
3. Lantern River
4. Icy River
5. Coral River
6. Ancient River

These are generated when a river would spawn but terrain is too high, creating underground water features.

---

## 8. Performance Optimizations

### 8.1 Caching Strategies

Tectonic uses several caching strategies:

```json
{
  "type": "minecraft:cache_once",
  "argument": { /* expensive calculation */ }
}
```

**cache_once** - Compute once per chunk column, reuse result

```json
{
  "type": "minecraft:cache_2d",
  "argument": { /* 2D noise function */ }
}
```

**cache_2d** - Cache 2D noise samples (X/Z only, no Y variation)

```json
{
  "type": "minecraft:cache_all_in_cell",
  "argument": { /* cell-based calculation */ }
}
```

**cache_all_in_cell** - Cache for entire generation cell (4x4x4 blocks)

### 8.2 Avoiding Expensive Operations

From the code analysis:
- **Splines are cached** - Spline evaluation is expensive, always cache results
- **Noise at large scales** - Using scales like 1500.0 means fewer samples needed
- **Selector patterns** - Multiply by 0/1 selectors to avoid expensive branches
- **Range choices** - More efficient than conditional statements

---

## 9. Application to Ad Astra Mekanized

### 9.1 Recommended Approaches

Based on Tectonic's implementation, here are recommendations for Ad Astra Mekanized:

#### ✅ **Use Custom Density Functions**

**Benefits:**
- Runtime configuration of terrain parameters
- Fine-grained control over terrain generation
- No need to modify Minecraft's core systems

**Implementation:**
1. Create custom density function types (like ConfigNoise, ConfigConstant)
2. Register them via NeoForge's RegisterEvent
3. Reference them in dimension JSON files

**Example for Moon:**
```java
public record MoonCraterDensity(
    double craterFrequency,
    double craterDepth,
    DensityFunction baseNoise
) implements DensityFunction {

    @Override
    public double compute(FunctionContext context) {
        double base = baseNoise.compute(context);

        // Sample crater noise
        double crater = getCraterValue(context.blockX(), context.blockZ());

        // Subtract craters from terrain
        return base - (crater * craterDepth);
    }

    private double getCraterValue(int x, int z) {
        // Voronoi-based crater generation
        // Return 0.0 to 1.0 based on distance to nearest crater center
    }
}
```

#### ✅ **Use Configuration System**

**Benefits:**
- Players can tweak planet generation without editing JSON
- Easier for server admins to customize
- Can add in-game config UI later

**Implementation:**
```java
public class PlanetConfig {
    private static Map<String, PlanetSettings> PLANET_CONFIGS = new HashMap<>();

    public static void load(Path configPath) {
        // Load from JSON using Minecraft's codec system
    }

    public static PlanetSettings getPlanetSettings(String planetId) {
        return PLANET_CONFIGS.getOrDefault(planetId, getDefaults());
    }
}

public record PlanetSettings(
    double continentalScale,
    double erosionScale,
    double mountainHeight,
    double craterFrequency,
    double atmosphericDensity
) {
    public static final Codec<PlanetSettings> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.DOUBLE.fieldOf("continental_scale").forGetter(PlanetSettings::continentalScale),
            Codec.DOUBLE.fieldOf("erosion_scale").forGetter(PlanetSettings::erosionScale),
            Codec.DOUBLE.fieldOf("mountain_height").forGetter(PlanetSettings::mountainHeight),
            Codec.DOUBLE.fieldOf("crater_frequency").forGetter(PlanetSettings::craterFrequency),
            Codec.DOUBLE.fieldOf("atmospheric_density").forGetter(PlanetSettings::atmosphericDensity)
        ).apply(instance, PlanetSettings::new)
    );
}
```

#### ✅ **Leverage Splines for Biome Transitions**

Tectonic uses splines extensively for smooth transitions:

```json
{
  "type": "minecraft:spline",
  "spline": {
    "coordinate": "tectonic:noise/full_continents",
    "points": [
      {"location": -1.05, "value": 0.0, "derivative": 0.0},
      {"location": -0.25, "value": 1.0, "derivative": 0.0}
    ]
  }
}
```

**For Ad Astra Mekanized:**
- Use splines for atmosphere density gradients (breathable to vacuum)
- Smooth transitions between lunar maria and highlands
- Crater rim height gradients

#### ✅ **Use Overlay System for Planet Variants**

**Benefits:**
- Ship multiple terrain profiles per planet
- Easy for players to choose variants (flat mars, dramatic mars, etc.)
- No code changes needed to add new profiles

**Implementation:**
```
src/main/resources/planets/moon/
├── base/                    # Base moon generation
│   └── data/adastramekanized/worldgen/
├── overlay.realistic/       # More realistic moon
│   └── data/adastramekanized/worldgen/
└── overlay.dramatic/        # Dramatic tall mountains
    └── data/adastramekanized/worldgen/
```

#### ❌ **Don't Use Mixins Unless Necessary**

Tectonic uses mixins sparingly. For Ad Astra Mekanized:
- Prefer custom density functions over mixins
- Use NeoForge events for most modifications
- Only use mixins for truly unavoidable core modifications

#### ✅ **Use Separate Namespaces for Overrides**

Tectonic structure:
- `data/minecraft/worldgen/` - Overrides vanilla
- `data/tectonic/worldgen/` - Custom content

Ad Astra Mekanized structure:
- `data/minecraft/worldgen/` - Only if overriding vanilla Overworld
- `data/adastramekanized/worldgen/` - All custom planet content

### 9.2 Planet-Specific Techniques

#### **Moon (Craters and Maria)**

```java
public record LunarTerrainDensity(
    DensityFunction continents,
    DensityFunction craters,
    DensityFunction maria
) implements DensityFunction {

    @Override
    public double compute(FunctionContext context) {
        double base = continents.compute(context);

        // Sample crater density (-1.0 to 1.0)
        double crater = craters.compute(context);
        if (crater > 0.7) {
            // Inside crater - lower terrain
            base -= (crater - 0.7) * 10.0;
        } else if (crater > 0.6) {
            // Crater rim - raise terrain
            base += (crater - 0.6) * 20.0;
        }

        // Maria are smooth, flat areas
        double mariaValue = maria.compute(context);
        if (mariaValue > 0.5) {
            // Flatten terrain in maria regions
            base = lerp(base, getSeaLevel(), (mariaValue - 0.5) * 2.0);
        }

        return base;
    }
}
```

**JSON Definition:**
```json
{
  "type": "adastramekanized:lunar_terrain",
  "continents": "adastramekanized:moon/base_continents",
  "craters": {
    "type": "adastramekanized:voronoi_craters",
    "frequency": 0.01,
    "depth_multiplier": 2.5
  },
  "maria": {
    "type": "minecraft:noise",
    "noise": "adastramekanized:moon_maria",
    "xz_scale": 500.0,
    "y_scale": 0.0
  }
}
```

#### **Mars (Canyons and Volcanoes)**

```java
public record MartianTerrainDensity(
    DensityFunction continents,
    DensityFunction canyons,
    DensityFunction volcanoes
) implements DensityFunction {

    @Override
    public double compute(FunctionContext context) {
        double base = continents.compute(context);

        // Deep canyons (Valles Marineris style)
        double canyon = canyons.compute(context);
        if (canyon < -0.6) {
            // Carve deep canyon
            base -= Math.abs(canyon + 0.6) * 50.0;
        }

        // Volcanic peaks (Olympus Mons style)
        double volcano = volcanoes.compute(context);
        if (volcano > 0.8) {
            // Add massive volcanic peak
            base += Math.pow(volcano - 0.8, 2.0) * 200.0;
        }

        return base;
    }
}
```

#### **Venus (Thick Atmosphere Effects)**

For Venus, terrain generation is less important than atmosphere:

```java
public record VenusAtmosphereDensity(
    DensityFunction baseTerrain,
    double fogDensity,
    double pressureMultiplier
) implements DensityFunction {

    @Override
    public double compute(FunctionContext context) {
        double base = baseTerrain.compute(context);

        // Atmospheric pressure increases density at lower altitudes
        int y = context.blockY();
        double pressure = Math.max(0, (100 - y) / 100.0) * pressureMultiplier;

        return base + pressure;
    }
}
```

### 9.3 Integration with Current PlanetMaker System

The current PlanetMaker system could be enhanced with Tectonic's techniques:

**Before (Current PlanetMaker):**
```java
registerPlanet("moon")
    .continentalScale(2.0f)
    .erosionScale(3.0f)
    .surfaceBlock("adastramekanized:moon_stone")
    .generate();
```

**After (With Custom Density Functions):**
```java
registerPlanet("moon")
    .customDensityFunction("lunar_terrain", new LunarTerrainBuilder()
        .craterFrequency(0.01)
        .craterDepth(2.5)
        .mariaFrequency(0.005)
        .build())
    .surfaceBlock("adastramekanized:moon_stone")
    .generate();
```

**Implementation:**
```java
public class PlanetMaker {
    private Map<String, DensityFunction> customDensityFunctions = new HashMap<>();

    public PlanetMaker customDensityFunction(String key, DensityFunction function) {
        this.customDensityFunctions.put(key, function);
        return this;
    }

    public void generate() {
        // Generate dimension JSON
        JsonObject dimensionJson = new JsonObject();

        // If custom density functions exist, generate references
        if (!customDensityFunctions.isEmpty()) {
            JsonObject noiseRouter = new JsonObject();
            for (Map.Entry<String, DensityFunction> entry : customDensityFunctions.entrySet()) {
                // Save density function to separate file
                saveDensityFunction(entry.getKey(), entry.getValue());

                // Reference in noise router
                noiseRouter.addProperty("final_density",
                    "adastramekanized:" + planetId + "/" + entry.getKey());
            }
            dimensionJson.add("noise_router", noiseRouter);
        }

        // ... rest of generation
    }
}
```

### 9.4 Recommended File Structure

```
src/main/
├── java/com/hecookin/adastramekanized/
│   ├── worldgen/
│   │   ├── densityfunction/
│   │   │   ├── AdAstraDensityFunctions.java      # Registration
│   │   │   ├── ConfigurablePlanetNoise.java      # Config-driven noise
│   │   │   ├── LunarCraterDensity.java           # Moon craters
│   │   │   ├── MartianCanyonDensity.java         # Mars canyons
│   │   │   ├── VenusAtmosphereDensity.java       # Venus atmosphere
│   │   │   └── VoronoiCraterGenerator.java       # Crater utility
│   │   └── config/
│   │       ├── PlanetConfigHandler.java           # Config loading
│   │       └── PlanetSettings.java                # Settings data class
│   └── AdAstraMekanized.java
└── resources/
    ├── planets/
    │   ├── moon/
    │   │   ├── data/adastramekanized/worldgen/
    │   │   │   ├── density_function/
    │   │   │   │   ├── lunar/
    │   │   │   │   │   ├── base_terrain.json
    │   │   │   │   │   ├── craters.json
    │   │   │   │   │   └── maria.json
    │   │   │   ├── noise_settings/
    │   │   │   │   └── moon.json
    │   │   │   └── noise/
    │   │   │       └── moon_maria.json
    │   │   └── overlay.realistic/              # Realistic variant
    │   │       └── data/adastramekanized/worldgen/
    │   ├── mars/
    │   │   ├── data/adastramekanized/worldgen/
    │   │   │   ├── density_function/
    │   │   │   │   ├── martian/
    │   │   │   │   │   ├── base_terrain.json
    │   │   │   │   │   ├── canyons.json
    │   │   │   │   │   └── volcanoes.json
    │   │   │   └── noise_settings/
    │   │   │       └── mars.json
    │   │   └── overlay.dramatic/               # Dramatic variant
    │   │       └── data/adastramekanized/worldgen/
    │   └── [other planets...]
    └── config/
        └── planet_defaults.json
```

---

## 10. Key Takeaways

### 10.1 What Tectonic Does Well

1. ✅ **Configuration-driven** - All parameters can be changed without recompiling
2. ✅ **Modular design** - Density functions are composable and reusable
3. ✅ **Performance-conscious** - Heavy use of caching strategies
4. ✅ **Overlay system** - Multiple terrain profiles in one download
5. ✅ **Compatibility-first** - Works with TerraBlender and other biome mods
6. ✅ **Codec-based serialization** - Proper integration with Minecraft's systems
7. ✅ **Multi-loader support** - Fabric, Forge, and NeoForge from one codebase

### 10.2 What Ad Astra Mekanized Should Adopt

1. ✅ **Custom density functions** - Essential for unique planet terrain
2. ✅ **Configuration system** - Allow players to tweak generation
3. ✅ **Spline-based transitions** - Smooth biome and terrain transitions
4. ✅ **Overlay system** - Multiple variants per planet
5. ✅ **Proper caching** - Performance is critical for generation
6. ✅ **Codec usage** - Follow Minecraft's serialization patterns

### 10.3 What Ad Astra Mekanized Doesn't Need

1. ❌ **Multi-loader support** - NeoForge-only is fine for now
2. ❌ **Biome overhaul** - Tectonic doesn't do this, neither should we
3. ❌ **Extensive mixins** - Density functions are cleaner
4. ❌ **Underground rivers** - Not relevant for airless planets

### 10.4 Priority Implementation Order

**Phase 1: Foundation (1-2 weeks)**
1. Create custom density function registration system
2. Implement basic ConfigurablePlanetNoise density function
3. Set up PlanetConfigHandler for runtime configuration
4. Test with Moon dimension

**Phase 2: Planet-Specific Features (2-3 weeks)**
1. Implement LunarCraterDensity with Voronoi-based craters
2. Implement MartianCanyonDensity with deep valleys
3. Implement VenusAtmosphereDensity with pressure gradients
4. Create corresponding JSON definitions

**Phase 3: Polish and Optimization (1-2 weeks)**
1. Add caching strategies (cache_once, cache_2d)
2. Implement spline-based transitions
3. Create overlay system for planet variants
4. Performance testing and optimization

**Phase 4: Documentation and Expansion (1 week)**
1. Document custom density functions for modders
2. Create example configurations for all planets
3. Add in-game config UI (optional)
4. Create tutorial for custom planet creation

---

## 11. Code Examples for Implementation

### 11.1 Density Function Registration

**File**: `src/main/java/com/hecookin/adastramekanized/worldgen/AdAstraDensityFunctions.java`

```java
package com.hecookin.adastramekanized.worldgen;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.worldgen.densityfunction.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.RegisterEvent;

public class AdAstraDensityFunctions {

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(AdAstraDensityFunctions::registerDensityFunctions);
    }

    private static void registerDensityFunctions(RegisterEvent event) {
        event.register(Registries.DENSITY_FUNCTION_TYPE, helper -> {
            // Basic configurable functions
            helper.register(
                id("configurable_noise"),
                ConfigurablePlanetNoise.CODEC_HOLDER.codec()
            );

            helper.register(
                id("configurable_constant"),
                ConfigurablePlanetConstant.CODEC_HOLDER.codec()
            );

            // Planet-specific functions
            helper.register(
                id("lunar_craters"),
                LunarCraterDensity.CODEC_HOLDER.codec()
            );

            helper.register(
                id("martian_canyons"),
                MartianCanyonDensity.CODEC_HOLDER.codec()
            );

            helper.register(
                id("martian_volcanoes"),
                MartianVolcanoDensity.CODEC_HOLDER.codec()
            );

            helper.register(
                id("venus_atmosphere"),
                VenusAtmosphereDensity.CODEC_HOLDER.codec()
            );

            AdAstraMekanized.LOGGER.info("Registered Ad Astra density functions");
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

### 11.2 Configurable Planet Noise

**File**: `src/main/java/com/hecookin/adastramekanized/worldgen/densityfunction/ConfigurablePlanetNoise.java`

```java
package com.hecookin.adastramekanized.worldgen.densityfunction;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import java.util.Arrays;

/**
 * Configurable noise density function for planet terrain generation.
 * Allows runtime modification of noise scale, multiplier, and offset.
 */
public record ConfigurablePlanetNoise(
    String planetId,
    String noiseKey,
    NormalNoise.NoiseParameters noise,
    double xzScale,
    double yScale,
    double multiplier,
    double offset
) implements DensityFunction {

    public static final MapCodec<ConfigurablePlanetNoise> CODEC =
        RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                Codec.STRING.fieldOf("planet_id")
                    .forGetter(ConfigurablePlanetNoise::planetId),
                Codec.STRING.fieldOf("noise_key")
                    .forGetter(ConfigurablePlanetNoise::noiseKey),
                NormalNoise.NoiseParameters.DIRECT_CODEC.fieldOf("noise")
                    .forGetter(ConfigurablePlanetNoise::noise),
                Codec.DOUBLE.fieldOf("xz_scale")
                    .forGetter(ConfigurablePlanetNoise::xzScale),
                Codec.DOUBLE.fieldOf("y_scale")
                    .forGetter(ConfigurablePlanetNoise::yScale),
                Codec.DOUBLE.fieldOf("multiplier")
                    .forGetter(ConfigurablePlanetNoise::multiplier),
                Codec.DOUBLE.fieldOf("offset")
                    .forGetter(ConfigurablePlanetNoise::offset)
            ).apply(instance, ConfigurablePlanetNoise::new)
        );

    public static final KeyDispatchDataCodec<ConfigurablePlanetNoise> CODEC_HOLDER =
        KeyDispatchDataCodec.of(CODEC);

    @Override
    public double compute(FunctionContext context) {
        // Apply scale to coordinates
        double x = context.blockX() * xzScale;
        double y = context.blockY() * yScale;
        double z = context.blockZ() * xzScale;

        // Sample noise (implement noise sampling here)
        double noiseValue = sampleNoise(x, y, z);

        // Apply multiplier and offset
        return noiseValue * multiplier + offset;
    }

    private double sampleNoise(double x, double y, double z) {
        // TODO: Implement proper noise sampling using NormalNoise
        // For now, placeholder
        return Math.sin(x * 0.1) * Math.cos(z * 0.1);
    }

    @Override
    public void fillArray(double[] array, ContextProvider provider) {
        // Efficient array filling for chunk generation
        for (int i = 0; i < array.length; i++) {
            array[i] = compute(provider.forIndex(i));
        }
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return visitor.apply(new ConfigurablePlanetNoise(
            planetId,
            noiseKey,
            noise,
            xzScale,
            yScale,
            multiplier,
            offset
        ));
    }

    @Override
    public double minValue() {
        return -1.0 * multiplier + offset;
    }

    @Override
    public double maxValue() {
        return 1.0 * multiplier + offset;
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC_HOLDER;
    }
}
```

### 11.3 Lunar Crater Density

**File**: `src/main/java/com/hecookin/adastramekanized/worldgen/densityfunction/LunarCraterDensity.java`

```java
package com.hecookin.adastramekanized.worldgen.densityfunction;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

/**
 * Generates lunar-style craters using Voronoi cell distance.
 * Creates circular depressions with raised rims.
 */
public record LunarCraterDensity(
    DensityFunction baseTerrain,
    double frequency,
    double depth,
    double rimHeight,
    double rimWidth
) implements DensityFunction {

    public static final MapCodec<LunarCraterDensity> CODEC =
        RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                DensityFunction.HOLDER_HELPER_CODEC.fieldOf("base_terrain")
                    .forGetter(LunarCraterDensity::baseTerrain),
                Codec.DOUBLE.fieldOf("frequency")
                    .forGetter(LunarCraterDensity::frequency),
                Codec.DOUBLE.fieldOf("depth")
                    .forGetter(LunarCraterDensity::depth),
                Codec.DOUBLE.fieldOf("rim_height")
                    .forGetter(LunarCraterDensity::rimHeight),
                Codec.DOUBLE.fieldOf("rim_width")
                    .forGetter(LunarCraterDensity::rimWidth)
            ).apply(instance, LunarCraterDensity::new)
        );

    public static final KeyDispatchDataCodec<LunarCraterDensity> CODEC_HOLDER =
        KeyDispatchDataCodec.of(CODEC);

    @Override
    public double compute(FunctionContext context) {
        double base = baseTerrain.compute(context);

        // Find nearest crater center using Voronoi
        double craterInfluence = getCraterInfluence(
            context.blockX(),
            context.blockZ()
        );

        return base + craterInfluence;
    }

    private double getCraterInfluence(int x, int z) {
        // Grid-based Voronoi for deterministic crater placement
        int cellSize = (int) (1.0 / frequency);
        int cellX = Math.floorDiv(x, cellSize);
        int cellZ = Math.floorDiv(z, cellSize);

        double minDist = Double.MAX_VALUE;

        // Check 3x3 grid of cells for nearest crater center
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                // Deterministic random position within cell
                long seed = hash(cellX + dx, cellZ + dz);
                double centerX = (cellX + dx) * cellSize +
                    (random(seed) - 0.5) * cellSize;
                double centerZ = (cellZ + dz) * cellSize +
                    (random(seed + 1) - 0.5) * cellSize;

                // Distance to crater center
                double dist = Math.sqrt(
                    Math.pow(x - centerX, 2) +
                    Math.pow(z - centerZ, 2)
                );

                minDist = Math.min(minDist, dist);
            }
        }

        // Convert distance to crater profile
        double craterRadius = cellSize * 0.4;
        double rimRadius = craterRadius * (1.0 + rimWidth);

        if (minDist < craterRadius) {
            // Inside crater - depression
            double factor = minDist / craterRadius;
            return -depth * (1.0 - Math.pow(factor, 2));
        } else if (minDist < rimRadius) {
            // Crater rim - raised edge
            double factor = (minDist - craterRadius) / (rimRadius - craterRadius);
            return rimHeight * Math.pow(1.0 - factor, 2);
        }

        return 0.0; // Outside crater influence
    }

    private long hash(int x, int z) {
        // Simple hash function for deterministic randomness
        long h = x * 374761393L + z * 668265263L;
        h = (h ^ (h >> 13)) * 1274126177L;
        return h ^ (h >> 16);
    }

    private double random(long seed) {
        // Convert hash to 0.0-1.0 range
        return (seed & 0xFFFFFFL) / (double) 0xFFFFFFL;
    }

    @Override
    public void fillArray(double[] array, ContextProvider provider) {
        for (int i = 0; i < array.length; i++) {
            array[i] = compute(provider.forIndex(i));
        }
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return visitor.apply(new LunarCraterDensity(
            baseTerrain.mapAll(visitor),
            frequency,
            depth,
            rimHeight,
            rimWidth
        ));
    }

    @Override
    public double minValue() {
        return baseTerrain.minValue() - depth;
    }

    @Override
    public double maxValue() {
        return baseTerrain.maxValue() + rimHeight;
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC_HOLDER;
    }
}
```

### 11.4 Usage in Dimension JSON

**File**: `src/generated/resources/data/adastramekanized/worldgen/noise_settings/moon.json`

```json
{
  "noise": {
    "min_y": -64,
    "height": 256,
    "size_horizontal": 1,
    "size_vertical": 2
  },
  "default_block": {
    "Name": "adastramekanized:moon_stone"
  },
  "default_fluid": {
    "Name": "minecraft:air"
  },
  "sea_level": 0,

  "noise_router": {
    "continents": "adastramekanized:moon/continents",
    "erosion": "adastramekanized:moon/erosion",
    "depth": "minecraft:zero",
    "ridges": "adastramekanized:moon/ridges",
    "temperature": "minecraft:zero",
    "vegetation": "minecraft:zero",
    "fluid_level_floodedness": "minecraft:zero",
    "fluid_level_spread": "minecraft:zero",
    "vein_toggle": "minecraft:zero",
    "vein_gap": "minecraft:zero",
    "vein_ridged": "minecraft:zero",
    "final_density": "adastramekanized:moon/final_density",
    "initial_density_without_jaggedness": "adastramekanized:moon/base_density",
    "lava": "minecraft:zero",
    "barrier": "minecraft:overworld/barrier"
  },

  "surface_rule": {
    "type": "minecraft:sequence",
    "sequence": [
      {
        "type": "minecraft:condition",
        "if_true": {
          "type": "minecraft:stone_depth",
          "offset": 0,
          "surface_type": "floor",
          "add_surface_depth": false,
          "secondary_depth_range": 0
        },
        "then_run": {
          "type": "minecraft:block",
          "result_state": {
            "Name": "adastramekanized:moon_regolith"
          }
        }
      },
      {
        "type": "minecraft:block",
        "result_state": {
          "Name": "adastramekanized:moon_stone"
        }
      }
    ]
  }
}
```

**File**: `src/generated/resources/data/adastramekanized/worldgen/density_function/moon/final_density.json`

```json
{
  "type": "minecraft:cache_once",
  "argument": {
    "type": "adastramekanized:lunar_craters",
    "base_terrain": {
      "type": "minecraft:add",
      "argument1": "adastramekanized:moon/base_terrain",
      "argument2": {
        "type": "minecraft:mul",
        "argument1": "adastramekanized:moon/maria",
        "argument2": -10.0
      }
    },
    "frequency": 0.01,
    "depth": 15.0,
    "rim_height": 5.0,
    "rim_width": 0.2
  }
}
```

---

## 12. Conclusion

Tectonic demonstrates a **sophisticated, modular approach** to world generation that:
- ✅ Uses custom density functions for fine-grained control
- ✅ Implements configuration-driven terrain parameters
- ✅ Leverages Minecraft's codec system properly
- ✅ Maintains compatibility with biome mods via TerraBlender
- ✅ Provides multiple terrain profiles via overlays
- ✅ Optimizes performance through intelligent caching

**For Ad Astra Mekanized**, adopting Tectonic's architecture will provide:
1. **Unique planet terrain** - Craters, canyons, volcanoes specific to each world
2. **Runtime configurability** - Players can tweak generation parameters
3. **Performance** - Proper caching ensures smooth chunk generation
4. **Maintainability** - Modular density functions are easier to debug/extend
5. **Future-proofing** - Proper codec usage ensures compatibility with future Minecraft versions

**Recommended next steps:**
1. Implement basic density function registration system
2. Create ConfigurablePlanetNoise with runtime configuration
3. Test with Moon dimension using LunarCraterDensity
4. Gradually port existing planets to new system
5. Add overlay system for terrain variants

---

## References

- **Tectonic GitHub**: https://github.com/Apollounknowndev/tectonic
- **Tectonic on CurseForge**: https://www.curseforge.com/minecraft/mc-mods/tectonic
- **Minecraft Wiki - Custom World Generation**: https://minecraft.wiki/w/Custom_world_generation
- **NeoForge Documentation**: https://docs.neoforged.net/

---

**Document Version**: 1.0
**Last Updated**: November 2025
**Author**: Claude Code (Anthropic)
**For**: Ad Astra Mekanized Development Team
