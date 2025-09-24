# Ore Generation System Documentation

## Overview

The ore generation system in Ad Astra Mekanized uses Minecraft 1.21.1's noise-based ore vein system combined with custom threshold-based rarity controls. This provides realistic ore distribution patterns that can be fine-tuned per planet.

## Core Concepts

### Noise-Based Distribution

Instead of simple depth-based spawning, the system uses `minecraft:ore_gap` noise to create natural ore clustering and distribution patterns. This creates more realistic geological formations with proper ore vein structures.

### Threshold-Based Rarity

Each ore type is assigned a threshold range within the 0.0-1.0 noise space. Only areas where the noise value falls within the threshold spawn that ore type. Lower thresholds (closer to 1.0) create rarer ores.

## PlanetMaker Ore Configuration

### Basic Ore Controls

```java
PlanetMaker.planet("planet_name")
    // Ore vein system controls
    .veinToggle(0.8f)               // Ore vein activation level (0.0-1.0)
    .veinRidged(0.6f)               // Ridged vein formation intensity (0.0-1.0)
    .veinGap(0.2f)                  // Gaps between ore veins (0.0-1.0)

    // Ore density and distribution
    .oreVeinDensity(1.0f)           // Overall ore density multiplier (0.1-3.0)
    .oreVeinSize(1.0f)              // Individual vein size multiplier (0.1-2.0)
    .maxOreVeinCount(6)             // Maximum different ore types (1-50)

    // Ore type categories
    .enableRareOres(true)           // Include rare ore types (diamond, emerald)
    .enableCommonOres(true)         // Include common ore types (iron, copper, coal)
    .enableDeepslateOres(false)     // Include deepslate variants

    // Custom ore additions
    .addCustomOreVein("minecraft:diamond_ore")
    .addCustomOreVein("minecraft:iron_ore")
    .addCustomOreVein("minecraft:copper_ore")
    .generate();
```

### Advanced Ore Configuration

```java
// Extreme ore generation (Hemphy example)
PlanetMaker.planet("hemphy")
    // MAXED ore vein generation
    .veinToggle(1.0f)               // MAX: Full vein activation
    .veinRidged(1.0f)               // MAX: Maximum ridged veins
    .veinGap(0.0f)                  // MIN: No gaps (solid veins)

    // Enhanced ore configuration
    .oreVeinDensity(3.0f)           // MAX: Triple ore density
    .oreVeinSize(2.0f)              // MAX: Double vein size
    .maxOreVeinCount(50)            // MAX: 50 different ore types
    .enableRareOres(true)           // MAX: All rare ores
    .enableCommonOres(true)         // MAX: All common ores
    .enableDeepslateOres(true)      // MAX: All deepslate variants

    // All ore types
    .addCustomOreVein("minecraft:diamond_ore")
    .addCustomOreVein("minecraft:emerald_ore")
    .addCustomOreVein("minecraft:ancient_debris")
    .addCustomOreVein("minecraft:gold_ore")
    .addCustomOreVein("minecraft:iron_ore")
    .addCustomOreVein("minecraft:copper_ore")
    .addCustomOreVein("minecraft:redstone_ore")
    .addCustomOreVein("minecraft:lapis_ore")
    .generate();
```

## Ore Rarity System

### Standard Threshold Ranges

The system uses standardized threshold ranges for consistent ore rarity across planets:

| Ore Type | Threshold Range | Spawn Chance | Rarity Level |
|----------|----------------|--------------|--------------|
| **Diamond** | 0.92-1.0 | 8% | Very Rare |
| **Ancient Debris** | 0.76-1.0 | 24% | Rare (Nether planets only) |
| **Emerald** | 0.74-1.0 | 26% | Rare (Special planets only) |
| **Gold** | 0.85-1.0 | 15% | Uncommon |
| **Lapis** | 0.8-1.0 | 20% | Uncommon |
| **Redstone** | 0.75-1.0 | 25% | Common |
| **Iron** | 0.7-1.0 | 30% | Common |
| **Copper** | 0.65-1.0 | 35% | Very Common |

### Custom Threshold Implementation

The ore generation system uses this threshold calculation:

```java
private static double getOreThreshold(String oreBlock, PlanetBuilder planet) {
    return switch (oreBlock) {
        case "minecraft:diamond_ore" -> 0.92;      // 8% spawn chance
        case "minecraft:ancient_debris" -> 0.76;   // 24% spawn chance
        case "minecraft:emerald_ore" -> 0.74;      // 26% spawn chance
        case "minecraft:gold_ore" -> 0.85;         // 15% spawn chance
        case "minecraft:lapis_ore" -> 0.8;         // 20% spawn chance
        case "minecraft:redstone_ore" -> 0.75;     // 25% spawn chance
        case "minecraft:iron_ore" -> 0.7;          // 30% spawn chance
        case "minecraft:copper_ore" -> 0.65;       // 35% spawn chance
        default -> 0.75;                           // 25% default
    };
}
```

## Surface Rule Generation

### Ore Placement Logic

Each ore type generates a surface rule with proper depth and noise conditions:

```json
{
  "type": "minecraft:condition",
  "if_true": {
    "type": "minecraft:noise_threshold",
    "noise": "minecraft:ore_gap",
    "min_threshold": 0.92,
    "max_threshold": 1.0
  },
  "then_run": {
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
}
```

### Depth Configuration

Ore placement includes depth-based controls:

| Ore Type | Offset | Secondary Range | Placement Strategy |
|----------|--------|-----------------|-------------------|
| **Diamond** | 16 | 16 | Deep underground only |
| **Emerald** | 12 | 32 | Mid-depth with wider range |
| **Ancient Debris** | 16 | 32 | Deep with extended range |
| **Gold** | 12 | 16 | Mid-depth standard |
| **Iron** | 8 | 16 | Shallow to mid-depth |
| **Copper** | 8 | 16 | Shallow to mid-depth |
| **Redstone** | 10 | 16 | Mid-depth standard |
| **Lapis** | 10 | 16 | Mid-depth standard |

## Planet-Specific Ore Configuration

### Earth-Like Planets (Oretest)

Balanced ore distribution suitable for testing and normal gameplay:

```java
// Balanced ore generation
.veinToggle(0.8f)               // Most veins active
.veinRidged(0.6f)               // Some ridged veins
.veinGap(0.2f)                  // Some gaps between veins
.oreVeinDensity(1.0f)           // Normal ore density (1x)
.oreVeinSize(1.0f)              // Standard vein size
.maxOreVeinCount(6)             // Standard ore count
.enableRareOres(true)           // Include rare ores
.enableCommonOres(true)         // Include common ores
.enableDeepslateOres(false)     // No deepslate variants
```

### Extreme Planets (Hemphy)

Maximum ore generation for stress testing:

```java
// MAXED ore generation
.veinToggle(1.0f)               // Full vein activation
.veinRidged(1.0f)               // Maximum ridged veins
.veinGap(0.0f)                  // No gaps (solid veins)
.oreVeinDensity(3.0f)           // Triple ore density
.oreVeinSize(2.0f)              // Double vein size
.maxOreVeinCount(50)            // Maximum ore types
.enableRareOres(true)           // All rare ores
.enableCommonOres(true)         // All common ores
.enableDeepslateOres(true)      // All deepslate variants
```

### Mars-Like Planets

Enhanced ore generation with realistic distribution:

```java
// Enhanced ore generation for Mars minerals
.veinToggle(0.8f)
.veinRidged(0.6f)
.veinGap(0.4f)
.oreVeinDensity(1.2f)           // Slightly enhanced density
.enableRareOres(true)
.enableCommonOres(true)
// Mars-specific ores would be added here
```

## Debugging Ore Generation

### Common Issues

1. **No Ore Spawning**
   - Verify `oreVeinsEnabled(true)` is set
   - Check threshold values are valid (0.0-1.0)
   - Ensure proper depth conditions

2. **Ore Flooding**
   - Reduce `oreVeinDensity` from 3.0 to 1.0 or lower
   - Increase threshold values (closer to 1.0)
   - Add proper stone_depth conditions

3. **Missing Rare Ores**
   - Check `enableRareOres(true)` is set
   - Verify diamond threshold (0.92) isn't too high
   - Ensure sufficient underground depth

### Testing Commands

```bash
# In-game testing
/planet teleport oretest        # Travel to balanced test planet
/planet teleport hemphy         # Travel to extreme ore planet
/give @s minecraft:diamond_pickaxe  # Get mining tools
/gamemode creative              # Enable creative for testing
```

### Validation Checklist

1. **Surface Rules**: Verify all ore rules include `surface_type: "floor"`
2. **Threshold Values**: Ensure all thresholds are between 0.0 and 1.0
3. **Depth Conditions**: Check offset and secondary_depth_range values
4. **Ore Activation**: Confirm `veinToggle` is above 0.0
5. **Block Names**: Verify all ore block IDs are valid

## Advanced Ore Customization

### Custom Ore Types

To add mod-specific ores:

```java
.addCustomOreVein("mekanism:osmium_ore")
.addCustomOreVein("immersiveengineering:aluminum_ore")
.addCustomOreVein("adastramekanized:desh_ore")
```

### Planet-Specific Distributions

Different planets can have unique ore compositions:

```java
// Moon - rare metals focus
.addCustomOreVein("minecraft:iron_ore")
.addCustomOreVein("adastramekanized:desh_ore")
.disableCommonOres(true)        // No coal, copper on Moon

// Mars - iron oxide focus
.addCustomOreVein("minecraft:iron_ore")
.addCustomOreVein("minecraft:gold_ore")
.oreVeinDensity(1.5f)           // Enhanced iron on Mars

// Gas Giant Moons - exotic materials
.addCustomOreVein("minecraft:diamond_ore")
.addCustomOreVein("minecraft:emerald_ore")
.enableRareOres(true)
.disableCommonOres(false)
```

### Performance Considerations

1. **Ore Count Limits**: Keep `maxOreVeinCount` below 50 for performance
2. **Density Moderation**: Avoid `oreVeinDensity` above 3.0 in production
3. **Vein Size Control**: Large `oreVeinSize` values can impact chunk generation
4. **Complex Rules**: Multiple ore types with tight thresholds increase generation time

## Technical Implementation

### Ore Rule Generation Method

```java
private static void addOreVeinRule(JsonArray sequence, String oreBlock, PlanetBuilder planet) {
    JsonObject oreRule = new JsonObject();
    oreRule.addProperty("type", "minecraft:condition");

    // Noise threshold condition
    JsonObject condition = new JsonObject();
    condition.addProperty("type", "minecraft:noise_threshold");
    condition.addProperty("noise", "minecraft:ore_gap");
    condition.addProperty("min_threshold", getOreThreshold(oreBlock, planet));
    condition.addProperty("max_threshold", 1.0);
    oreRule.add("if_true", condition);

    // Depth condition
    JsonObject depthCondition = new JsonObject();
    depthCondition.addProperty("type", "minecraft:stone_depth");
    depthCondition.addProperty("offset", getOreDepth(oreBlock));
    depthCondition.addProperty("surface_type", "floor");
    depthCondition.addProperty("add_surface_depth", false);
    depthCondition.addProperty("secondary_depth_range", getOreRange(oreBlock));

    // Block placement
    JsonObject blockPlacement = new JsonObject();
    blockPlacement.addProperty("type", "minecraft:block");
    JsonObject resultState = new JsonObject();
    resultState.addProperty("Name", oreBlock);
    blockPlacement.add("result_state", resultState);

    // Nested conditions
    JsonObject nestedCondition = new JsonObject();
    nestedCondition.addProperty("type", "minecraft:condition");
    nestedCondition.add("if_true", depthCondition);
    nestedCondition.add("then_run", blockPlacement);
    oreRule.add("then_run", nestedCondition);

    sequence.add(oreRule);
}
```

### Noise Router Integration

Ore generation integrates with the planet's noise router:

```json
"noise_router": {
  "vein_toggle": 0.8,
  "vein_ridged": 0.6,
  "vein_gap": 0.2,
  // ... other noise parameters
}
```

This creates natural ore vein patterns that follow geological noise structures rather than random distribution.

## Future Enhancements

### Planned Features

1. **Biome-Specific Ores**: Different ore types based on planetary biomes
2. **Geological Formations**: Ore concentration in specific terrain features
3. **Rare Metal Planets**: Planets with unique ore compositions
4. **Mining Progression**: Ore rarity scaling with distance from spawn
5. **Mod Integration**: Automatic detection and integration of modded ores

### Development Roadmap

1. **Phase 1**: Implement biome-aware ore distribution
2. **Phase 2**: Add geological formation-based ore spawning
3. **Phase 3**: Create unique ore planets with special materials
4. **Phase 4**: Integrate with mod ore systems automatically