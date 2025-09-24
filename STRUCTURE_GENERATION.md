# Structure Generation System for Minecraft 1.21.1

## Overview

Structure generation in Minecraft 1.21.1 uses a multi-layered JSON system to define where and how structures spawn in dimensions. This document outlines the requirements for implementing structure generation in custom planetary dimensions.

## Structure Generation Layers

### 1. Structure Sets (`data/[namespace]/worldgen/structure_set/`)

Structure sets define groups of structures and their placement rules.

```json
{
  "structures": [
    {
      "structure": "minecraft:village",
      "weight": 1
    }
  ],
  "placement": {
    "type": "minecraft:random_spread",
    "spacing": 32,
    "separation": 8,
    "salt": 10387312
  }
}
```

**Key Parameters:**
- `structures`: Array of structures with weights for random selection
- `placement.spacing`: Average distance between structures (in chunks)
- `placement.separation`: Minimum distance between structures (must be less than spacing)
- `placement.salt`: Random seed modifier for this structure set

### 2. Structure Definitions (`data/[namespace]/worldgen/structure/`)

Individual structure configurations that reference structure templates.

```json
{
  "type": "minecraft:jigsaw",
  "start_pool": "minecraft:village/plains/town_centers",
  "size": 6,
  "max_distance_from_center": 80,
  "use_expansion_hack": false,
  "biomes": "#minecraft:has_structure/village",
  "step": "surface_structures",
  "spawn_overrides": {},
  "terrain_adaptation": "beard_thin"
}
```

### 3. Biome Tags (`data/[namespace]/tags/worldgen/biome/`)

Tags that control which biomes can spawn structures.

```json
{
  "values": [
    "minecraft:plains",
    "minecraft:savanna",
    "minecraft:taiga"
  ]
}
```

## Implementation for PlanetMaker

### Required JSON Files per Planet

For each planet with structures, we need:

1. **Structure Set File**: `worldgen/structure_set/[planet]_structures.json`
2. **Biome Tags**: `tags/worldgen/biome/has_structure/[planet]_structures.json`
3. **Dimension Configuration**: Reference structure sets in dimension JSON

### Example: Adding Villages to a Custom Planet

#### 1. Structure Set (`worldgen/structure_set/planet_villages.json`)
```json
{
  "structures": [
    {
      "structure": "minecraft:village",
      "weight": 1
    }
  ],
  "placement": {
    "type": "minecraft:random_spread",
    "spacing": 32,
    "separation": 8,
    "salt": 10387312
  },
  "frequency": 0.2
}
```

#### 2. Biome Tag (`tags/worldgen/biome/has_structure/planet_villages.json`)
```json
{
  "replace": false,
  "values": [
    "minecraft:plains",
    "minecraft:forest",
    "minecraft:taiga"
  ]
}
```

## Structure Types in Minecraft 1.21.1

### Overworld Structures
| Structure | Default Spacing | Separation | Salt | Generation Step |
|-----------|----------------|------------|------|-----------------|
| Village | 32 | 8 | 10387312 | surface_structures |
| Pillager Outpost | 32 | 8 | 165745296 | surface_structures |
| Stronghold | 64 | 32 | custom | underground_structures |
| Mineshaft | 16 | 4 | 14357618 | underground_structures |
| Desert Pyramid | 32 | 8 | 14357617 | surface_structures |
| Jungle Temple | 32 | 8 | 14357619 | surface_structures |
| Igloo | 32 | 8 | 14357618 | surface_structures |
| Woodland Mansion | 80 | 20 | 10387319 | surface_structures |
| Ocean Monument | 32 | 5 | 10387313 | surface_structures |
| Shipwreck | 20 | 8 | 165745295 | surface_structures |
| Buried Treasure | 16 | 8 | 10387320 | underground_structures |
| Ruined Portal | 25 | 10 | 34222645 | surface_structures |

### Nether Structures
| Structure | Default Spacing | Separation | Salt | Generation Step |
|-----------|----------------|------------|------|-----------------|
| Fortress | 27 | 4 | 30084232 | underground_structures |
| Bastion Remnant | 27 | 4 | 30084232 | surface_structures |
| Nether Fossil | 2 | 1 | 14357921 | underground_structures |

### End Structures
| Structure | Default Spacing | Separation | Salt | Generation Step |
|-----------|----------------|------------|------|-----------------|
| End City | 20 | 11 | 10387313 | surface_structures |

## Generation Steps

Structures generate in specific steps during world generation:

1. `raw_generation` - Very early, affects terrain
2. `lakes` - Lake features
3. `local_modifications` - Small local changes
4. `underground_structures` - Mineshafts, strongholds, etc.
5. `surface_structures` - Villages, temples, etc.
6. `strongholds` - Special handling for strongholds
7. `underground_ores` - Ore generation
8. `underground_decoration` - Cave decorations
9. `fluid_springs` - Water/lava springs
10. `vegetal_decoration` - Trees, grass, etc.
11. `top_layer_modification` - Final surface changes

## PlanetMaker Integration Strategy

### Current Implementation

The PlanetMaker system currently provides an API for structure configuration:

```java
.addStructure("minecraft:village", 32, 8, 10387312)
.addStructurePreset("overworld")
.enableVillages()
```

### Required Enhancements

To fully implement structure generation, PlanetMaker needs to:

1. **Generate Structure Set JSONs**: Create `worldgen/structure_set/` files for each planet
2. **Generate Biome Tags**: Create appropriate biome tags for structure placement
3. **Modify Dimension JSON**: Reference structure sets in the dimension configuration
4. **Handle Custom Structures**: Support mod-specific structures with proper namespacing

### Proposed JSON Generation Method

```java
private static void generateStructureSet(PlanetBuilder planet) {
    if (planet.customStructures.isEmpty()) return;

    JsonObject structureSet = new JsonObject();
    JsonArray structures = new JsonArray();

    for (StructureEntry entry : planet.customStructures) {
        JsonObject structure = new JsonObject();
        structure.addProperty("structure", entry.structureName);
        structure.addProperty("weight", 1);
        structures.add(structure);
    }

    structureSet.add("structures", structures);

    JsonObject placement = new JsonObject();
    placement.addProperty("type", "minecraft:random_spread");
    placement.addProperty("spacing", entry.spacing);
    placement.addProperty("separation", entry.separation);
    placement.addProperty("salt", entry.salt);

    structureSet.add("placement", placement);

    writeJsonFile(RESOURCES_PATH +
        "worldgen/structure_set/" + planet.name + "_structures.json",
        structureSet);
}
```

## Limitations and Considerations

### Current Limitations

1. **Structure Templates**: Cannot generate new structure templates (NBT files)
2. **Jigsaw Pools**: Complex structures like villages need jigsaw pool definitions
3. **Custom Structures**: Mod structures need their own templates and pools
4. **Biome Restrictions**: Structures check biome tags for valid placement
5. **Dimension Restrictions**: Some structures are hardcoded to specific dimensions

### Best Practices

1. **Use Vanilla Structures**: Leverage existing Minecraft structures when possible
2. **Proper Spacing**: Ensure separation < spacing to avoid errors
3. **Unique Salts**: Use different salt values for different structure types
4. **Biome Compatibility**: Ensure planet biomes match structure requirements
5. **Performance**: Too many structures can impact world generation performance

## Future Implementation Tasks

1. ✅ Create structure configuration API in PlanetBuilder
2. ⬜ Implement structure set JSON generation
3. ⬜ Implement biome tag generation for structures
4. ⬜ Add structure references to dimension JSONs
5. ⬜ Test structure spawning in custom dimensions
6. ⬜ Create custom structure templates for space-themed structures
7. ⬜ Implement structure-specific loot tables

## Example: Complete Structure Implementation

For a planet with villages and mineshafts:

### 1. During Planet Generation
```java
PlanetMaker.planet("earthlike")
    .enableVillages()
    .enableMineshafts()
    .generate();
```

### 2. Generated Files

**`worldgen/structure_set/earthlike_villages.json`**
```json
{
  "structures": [
    {"structure": "minecraft:village", "weight": 1}
  ],
  "placement": {
    "type": "minecraft:random_spread",
    "spacing": 32,
    "separation": 8,
    "salt": 10387312
  }
}
```

**`worldgen/structure_set/earthlike_mineshafts.json`**
```json
{
  "structures": [
    {"structure": "minecraft:mineshaft", "weight": 1}
  ],
  "placement": {
    "type": "minecraft:random_spread",
    "spacing": 16,
    "separation": 4,
    "salt": 14357618
  }
}
```

**`tags/worldgen/biome/has_structure/earthlike_structures.json`**
```json
{
  "values": [
    "minecraft:plains",
    "minecraft:forest",
    "minecraft:taiga"
  ]
}
```

## Conclusion

Full structure generation requires creating multiple JSON files beyond what PlanetMaker currently generates. The system has the API framework in place but needs the JSON generation implementation to actually spawn structures in-game. This would be a valuable Phase 4 enhancement to complete the structure system.