# Dynamic Planet System Documentation

## Current Status: CRITICAL ISSUES - System Not Working as Intended

### Issue Summary
The dynamic planet system is **not creating real dimensions** despite extensive implementation. The teleportation attempt shows:

```
[WARN] Dimension not found after datapack reload: adastramekanized:planet_002
[INFO] Available dimensions: minecraft:overworld, adastramekanized:moon, adastramekanized:mars, minecraft:the_end, minecraft:the_nether
[CHAT] Teleportation failed: Planet not found: adastramekanized:planet_002
```

**Problem**: Dynamic planets are generating JSON files but these are NOT creating actual Minecraft dimensions. Only static planets (moon, mars) have real dimensions.

---

## Requirements: How Dynamic Planets SHOULD Work

### Core Functionality Requirements

#### 1. **Real Dimension Creation** (CURRENTLY BROKEN)
- Each dynamic planet must create an **actual new Minecraft dimension**
- Like `adastramekanized:moon` and `adastramekanized:mars` but for `adastramekanized:planet_001`, `adastramekanized:planet_002`, etc.
- Must appear in server's dimension registry as real traversable dimensions
- Must support player teleportation with unique terrain generation

#### 2. **Unique Terrain Per Planet** (CURRENTLY BROKEN)
- Each planet must have **completely different terrain**
- No more "bedrock plane" - each planet needs proper surface blocks, caves, structures
- Terrain varies by `DimensionEffectsType`:
  - `MOON_LIKE`: Airless, cratered surface, low gravity effects
  - `ROCKY`: Mars-like red stone, rocky terrain
  - `ICE_WORLD`: Snow/ice surfaces, cold environment
  - `VOLCANIC`: Lava, magma blocks, hot environment
  - `GAS_GIANT`: Floating islands, thick atmosphere

#### 3. **Planet-Specific Customization** (PARTIALLY IMPLEMENTED)
- **Surface blocks**: Different base blocks per planet type
- **Mob spawning**: Configurable per planet (airless = no mobs)
- **Cave generation**: Caves and mineshafts should spawn on appropriate planets
- **Atmospheric effects**: Visual and gameplay effects based on planet properties
- **Gravity simulation**: Different gravity values affecting player movement
- **Day/night cycles**: Potentially different cycle lengths per planet

#### 4. **Lazy Loading & Performance** (IMPLEMENTED BUT NOT WORKING)
- Maximum 1 dynamic dimension created programmatically at a time
- Maximum 100 planets per server total
- LRU cleanup after 10 minutes of inactivity
- Dimensions created only when first accessed (player teleportation)

#### 5. **Datapack Integration** (ATTEMPTED BUT FAILING)
- All planet data must persist in world datapacks
- Dimension files must be generated and loaded by Minecraft
- Must survive server restarts and world reloads
- Integration with Minecraft's native dimension system

---

## Current Implementation Analysis

### What's Working ✅
1. **Planet JSON Generation**: Creates planet data files in `./saves/New World/datapacks/data/adastramekanized/planets/`
2. **Batch Creation**: Successfully creates multiple planet records
3. **Command System**: Planet listing and info commands work
4. **Static Planet Teleportation**: Moon and Mars teleportation works perfectly

### What's Broken ❌
1. **Dynamic Dimension Creation**: JSON files generated but no actual dimensions created
2. **Datapack Integration**: Files exist but Minecraft doesn't recognize them as dimensions
3. **Teleportation to Dynamic Planets**: Fails because dimensions don't exist
4. **Terrain Generation**: No unique terrain because no dimensions exist

---

## Technical Architecture

### Current File Structure
```
./saves/New World/datapacks/data/adastramekanized/
├── planets/group_01/           # Dynamic planet JSON files ✅
│   ├── planet_001.json
│   ├── planet_002.json
│   └── planet_003.json
├── dimension_type/             # Static dimension types ✅
│   ├── mars.json
│   └── moon.json
├── dimension/                  # Static dimensions ✅
│   ├── mars.json
│   └── moon.json
└── worldgen/                   # Missing for dynamic planets ❌
    ├── noise_settings/
    └── biome/
```

### Critical Missing Components
1. **Dynamic Dimension Types**: No `dimension_type/planet_001.json` files
2. **Dynamic Dimensions**: No `dimension/planet_001.json` files
3. **Dynamic Worldgen**: No `worldgen/noise_settings/planet_001.json` files
4. **Dynamic Biomes**: No `worldgen/biome/planet_001_plains.json` files

---

## Implementation Components

### Core Classes
- `RuntimeDimensionManager`: Manages real dimension creation (NOT WORKING)
- `DimensionJsonGenerator`: Generates dimension JSON files (NOT CALLED FOR DYNAMIC PLANETS)
- `PlanetTeleportationSystem`: Handles teleportation logic
- `DynamicPlanetRegistry`: Manages dynamic planet data
- `EnhancedDynamicPlanetCreator`: Creates planet JSON files

### Key Methods
- `RuntimeDimensionManager.getOrCreateDynamicDimension()`: Should create real dimensions
- `DimensionJsonGenerator.generateDimensionFiles()`: Should generate all required JSON files
- `PlanetTeleportationSystem.teleportToDynamicPlanet()`: Should teleport to real dimensions

---

## Root Cause Analysis

### Primary Issue: Missing Dimension File Generation
The system creates planet JSON files but **does NOT generate the required Minecraft dimension files**:
- `dimension_type/planet_XXX.json`
- `dimension/planet_XXX.json`
- `worldgen/noise_settings/planet_XXX.json`
- `worldgen/biome/planet_XXX_plains.json`

### Secondary Issue: NeoForge Dimension Registration
Even if files exist, the system may need proper NeoForge integration to register dimensions at runtime.

---

## Immediate Action Plan

### Fix 1: Connect Planet Creation to Dimension Generation
- `EnhancedDynamicPlanetCreator` must call `DimensionJsonGenerator.generateDimensionFiles()`
- Ensure all 4 required JSON files are created for each dynamic planet

### Fix 2: Implement Proper NeoForge Integration
- Research NeoForge 1.21.1 runtime dimension registration
- May need `DimensionManager` or similar NeoForge APIs
- Ensure dimensions are properly registered with server

### Fix 3: Verify Datapack Loading
- Ensure generated files are in correct format
- Test datapack reload triggers dimension recognition
- Verify file paths and naming conventions

---

## Expected User Workflow (When Fixed)

1. **Create Planet**: `/planets batch create 1 gas_giant ring_system`
2. **Verify Creation**: `/planets dynamic` shows new planet
3. **Teleport**: `/planets teleport adastramekanized:planet_001`
4. **Experience**: Player lands on unique gas giant terrain with rings visible in sky
5. **Persistence**: Planet survives server restart and reloads

---

## Success Criteria

The system will be considered working when:
1. Dynamic planets appear in `server.getAllLevels()` like static planets
2. Teleportation to dynamic planets succeeds without errors
3. Each dynamic planet has unique, appropriate terrain
4. Planets persist across server restarts
5. Performance stays within limits (max 10 simultaneous, LRU cleanup)

---

## Integration Context

- **NeoForge 1.21.1**: Modern Minecraft modding platform
- **Mekanism Integration**: Oxygen systems, energy systems
- **Ad Astra Replacement**: Complete space exploration overhaul
- **Performance Focus**: Must handle up to 100 planets efficiently

This documentation should provide the next agent with complete context on what needs to be fixed to make dynamic planets work as intended.