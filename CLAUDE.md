# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Ad Astra Mekanized is a Minecraft 1.21.1 NeoForge mod that provides space exploration features integrated with Mekanism's infrastructure systems. It's a complete replacement for Ad Astra, combining rockets, planets, space stations, and oxygen distribution with Mekanism's chemical systems, energy systems, and Immersive Engineering fuels.

## Build System and Development Commands

### Primary Development Commands
```bash
# Build the mod
./gradlew build

# Development environment
./gradlew runClient     # Launch development client
./gradlew runServer     # Launch development server
./gradlew runData       # Run data generation and test mod loading

# Testing and verification
./gradlew test          # Run test suite
./gradlew clean build   # Clean build (recommended after integration changes)

# Local dependency setup (if needed)
./gradlew -b install-local-deps.gradle.kts setupLocalMaven
```

### Project Configuration
- **Platform**: NeoForge 1.21.1 (version 21.1.209)
- **Java Version**: Java 21 (required)
- **Gradle**: 8.14.3+ (wrapper included)
- **NeoGradle**: 7.0.192

## Architecture Overview

### Package Structure
```
com.hecookin.adastramekanized/
├── api/                    # Public API interfaces for integration
│   ├── IChemicalIntegration.java
│   ├── IEnergyIntegration.java
│   └── IFuelIntegration.java
├── integration/            # Mod integration handlers (reflection-based)
│   ├── ModIntegrationManager.java
│   ├── mekanism/MekanismIntegration.java
│   ├── immersiveengineering/ImmersiveEngineeringIntegration.java
│   └── create/CreateIntegration.java
├── config/                 # Configuration system
└── [future packages for rockets, planets, oxygen, fuel systems]
```

### Integration Architecture

The mod uses a **hybrid reflection-based integration approach** to support optional dependencies:

- **Dependencies are marked as optional** in neoforge.mods.toml
- **Runtime reflection** provides full API access when mods are present
- **Graceful fallbacks** when integration mods are missing
- **No compile-time dependencies** on integration mods

#### Key Integration Points
- **Mekanism**: Chemical systems (oxygen distribution), energy systems
- **Immersive Engineering**: Fuel systems (diesel/biodiesel replace Ad Astra fuels)
- **Create**: Mechanical systems for contraptions and automation

#### Integration Manager Usage
Always access integrations through the central manager:
```java
ModIntegrationManager manager = AdAstraMekanized.getIntegrationManager();
if (manager.isMekanismAvailable()) {
    manager.getMekanismIntegration().handleOxygen(...);
}
```

## Development Guidelines

### Code Standards
- Use existing project logging: `AdAstraMekanized.LOGGER`
- Follow NeoForge 1.21.1 patterns and conventions
- Always check integration availability before using mod-specific features
- Provide fallback behavior for all integrated systems
- Cache reflected classes and methods for performance

### Integration Best Practices
1. **Test both with and without integration mods** present
2. **Use graceful fallbacks** for all integration failures
3. **Access integrations only through ModIntegrationManager**
4. **Check integration status** before attempting mod-specific operations

### Development Status
- **Phase 1 Complete**: Integration architecture foundation is fully implemented
- **Phase 2 Complete**: Dynamic planet generation and teleportation system implemented
- **Current Status**: Planet generation functional but needs terrain quality improvements
- **Next Priority**: Enhanced terrain generation, ore spawning systems, improved biome diversity

## Key Files and Configuration

### Core Implementation Files
- `AdAstraMekanized.java` - Main mod class with integration manager initialization
- `ModIntegrationManager.java` - Central integration coordinator
- `AdAstraMekanizedConfig.java` - Configuration system with integration settings

### Build Configuration
- `build.gradle` and `build.gradle.kts` - Gradle build configuration
- `gradle.properties` - Project metadata (mod_id: adastramekanized, version: 1.0.0)
- `install-local-deps.gradle.kts` - Local dependency setup for development

### Resources
- `src/main/resources/META-INF/neoforge.mods.toml` - Mod metadata with optional dependencies
- Local development dependencies in `libs/` directory (Mekanism, IE homebaked builds)

## Testing and Verification

### Integration Testing
The mod includes automatic integration tests during startup. Monitor logs for:
- "Mekanism integration initialized" / "Mekanism integration failed"
- "Immersive Engineering integration initialized" / "IE integration failed"
- "Create integration initialized" / "Create integration failed"
- Integration status summary from ModIntegrationManager

### Recommended Testing Approach
- Clean builds after integration changes: `./gradlew clean build`
- Test mod loading with: `./gradlew runData`
- Verify both with and without optional dependencies present
- Integration tests run automatically during mod setup

## Asset Development Guidelines

### Block and Item Model Requirements
When creating block models, **always use the correct parent namespace**:

```json
// CORRECT - Working block model
{
  "parent": "block/cube_all",
  "textures": {
    "all": "adastramekanized:block/block_name"
  }
}

// INCORRECT - Will cause texture loading failure
{
  "parent": "cube_all",
  "textures": {
    "all": "adastramekanized:block/block_name"
  }
}
```

**Critical Asset Requirements:**
1. **Block Models**: Must use `"parent": "block/cube_all"` (with block/ prefix)
2. **Item Models**: Use `"parent": "adastramekanized:block/block_name"` for blocks
3. **Blockstates**: Use proper multi-line JSON formatting (never compressed single-line)
4. **Textures**: Must be valid 16x16 PNG files in RGBA format

### Common Asset Issues and Solutions

#### Texture Loading Failures
**Problem**: Textures appear as purple/black missing texture blocks
**Root Causes**:
1. Missing `block/` prefix in block model parent reference
2. Compressed JSON in blockstate files (single-line format)
3. Invalid texture file paths or missing textures

**Solution Process**:
1. Compare failing models with working models (e.g., desh_block.json)
2. Verify all block models use `"parent": "block/cube_all"`
3. Ensure blockstate JSON uses proper multi-line formatting
4. Confirm texture files exist and are valid PNG format

#### Asset Generation Workflow
1. **Manual Creation**: Create models/blockstates manually following working examples
2. **Batch Operations**: Use proper JSON formatting when creating multiple files
3. **Verification**: Always test build after asset changes: `./gradlew build`
4. **Comparison**: Compare new assets with known working examples

### Historical Issues Resolved
**September 2024 - Planet Stone Texture Loading**:
- **Issue**: All 18 planet stone blocks showed missing textures
- **Root Cause**: Block models used `"parent": "cube_all"` instead of `"parent": "block/cube_all"`
- **Secondary Issue**: Some blockstates had compressed JSON format from bash generation
- **Resolution**: Fixed parent references and reformatted blockstates to multi-line JSON
- **Prevention**: Always reference working block models when creating new assets

## Future Feature Considerations

### Celestial Time Systems
**Per-Planet Day/Night Cycles**: Potential future feature to implement different day/night cycle lengths per planet without affecting game tick rate. This would require:
- Integration with time management mods (e.g., BetterDays)
- Custom sky rendering system overrides
- Complex client-server synchronization
- Careful handling of time-dependent game systems (crops, spawning, redstone)
- Cross-dimensional player transitions

**Current Status**: Not implemented. Standard Minecraft time progression used for all dimensions.
**Complexity**: High risk due to impact on core game systems and mod compatibility.

## Current Implementation Progress

### ✅ Phase 1 Complete: Integration Architecture Foundation
**Status**: ✅ Completed September 2025
- Reflection-based integration system for optional mod dependencies
- Central integration manager with graceful fallbacks
- Comprehensive configuration system
- Build system properly configured for hybrid approach

### ✅ Phase 2 Complete: Block & Material Migration + Planet System Core
**Status**: ✅ Completed September 2025
- Complete block and material migration (112+ blocks, 25+ items)
- Planet stone variants, industrial blocks, ore blocks, alien wood sets
- Core planet data structures with Codec serialization
- JSON-based planet loading and client-server networking
- Planet registry system with thread-safe management

### ✅ Phase 3 Complete: Comprehensive PlanetMaker System
**Status**: ✅ Completed September 2025

#### Advanced Planet Generation System:
1. **PlanetMaker API**:
   - ✅ Fluent builder pattern with 50+ configurable parameters
   - ✅ Complete noise router control (continents, erosion, ridges, jaggedness)
   - ✅ Advanced terrain shaping (height variations, density factors, gradients)
   - ✅ Comprehensive surface block configuration
   - ✅ Noise-based ore generation with proper rarity distribution

2. **Planet Examples Implemented**:
   - ✅ **Moon**: Craterous lunar landscape with moon-specific materials
   - ✅ **Mars**: Dramatic Martian terrain with canyons and highlands
   - ✅ **Hemphy**: Extreme stress test planet (lava world with maximum parameters)
   - ✅ **Oretest**: Balanced test planet with Earth-like characteristics

3. **Technical Features**:
   - ✅ Automatic JSON generation for dimensions and noise settings
   - ✅ Noise-based ore distribution using `minecraft:ore_gap` threshold system
   - ✅ Surface rule validation with proper `surface_type` parameters
   - ✅ Multi-parameter terrain control (continental, erosion, ridge scales)
   - ✅ Advanced hill/mountain generation with jaggedness controls

#### Current Working Features:
- ✅ Planet teleportation: `/planet teleport moon`, `/planet teleport mars`, `/planet teleport hemphy`, `/planet teleport oretest`
- ✅ Planet information: `/planet info <planet_name>` for detailed planet characteristics
- ✅ Planet listing: `/planet list` to view all available worlds
- ✅ Automatic planet file generation with `PlanetGenerationRunner`

#### Advanced Terrain Controls:
1. **Noise Parameters**: Continental scale (1.0-100.0), erosion scale (1.0-100.0), ridge scale (1.0-50.0)
2. **Height Variation**: Four-parameter system for base/secondary/tertiary/fine height control
3. **Mountain Generation**: Jaggedness scale (0.1-1.0), noise scale (100.0-2000.0), depth factors
4. **Ore Distribution**: Threshold-based rarity (Diamond: 8%, Iron: 30%, Copper: 35%)
5. **Surface Control**: Complete block override system with prevention flags

#### Technical Implementation:
- **PlanetMaker.java**: Core builder class with comprehensive parameter control
- **PlanetGenerationRunner.java**: Example configurations and planet setup
- **Generated JSON files**: Automatic creation of dimension and noise settings
- **Ore generation**: Noise-based system using proper threshold validation

### 🔄 Current Phase: Documentation and Polish
**Status**: 🔄 In Progress

#### Next Priorities:
1. **Documentation Updates**: Complete planetary generation documentation
2. **Performance Optimization**: Optimize chunk generation for complex planets
3. **Visual Enhancements**: Improve sky rendering and atmospheric effects
4. **Resource Balancing**: Fine-tune ore distribution and terrain features

**For detailed TerraBlender information, see [TERRABLENDER_INTEGRATION.md](./TERRABLENDER_INTEGRATION.md)**
**For celestial configuration, see [CELESTIAL_CONFIGURATION.md](./CELESTIAL_CONFIGURATION.md)**

### Development Tools & Commands
```bash
# Build and testing
./gradlew clean build                  # Clean build (recommended after integration changes)
./gradlew runClient                    # Launch development client
./gradlew runData                      # Test mod loading and integrations

# Planet generation and testing
# Run PlanetGenerationRunner to generate all planets
# Set OVERWRITE_EXISTING = true to regenerate existing planets

# Planet commands (in-game)
/planet list                          # View all generated planets
/planet teleport moon                 # Travel to Moon (craterous lunar landscape)
/planet teleport mars                 # Travel to Mars (dramatic Martian terrain)
/planet teleport hemphy               # Travel to Hemphy (extreme stress test planet)
/planet teleport oretest              # Travel to Oretest (balanced test planet)
/planet info <planet_name>           # View detailed planet characteristics

# Time progression testing
/time set day                          # Test day cycle
/time set night                        # Test night cycle
```

## Planet Generation System

### PlanetMaker API Usage

The comprehensive PlanetMaker system provides complete control over planetary generation. Key usage:

```java
// Example: Balanced test planet
PlanetMaker.planet("oretest")
    .continentalScale(25.0f)           // Continental landmass control
    .erosionScale(35.0f)               // Erosion pattern intensity
    .ridgeScale(15.0f)                 // Ridge formation strength
    .heightVariation(15.0f, 10.0f, 5.0f, 3.0f)  // Multi-level height control
    .jaggednessScale(0.3f)             // Mountain sharpness
    .jaggednessNoiseScale(800.0f)      // Mountain feature scale
    .surfaceBlock("minecraft:grass_block")      // Surface material
    .subsurfaceBlock("minecraft:dirt")          // Subsurface layer
    .veinToggle(0.8f)                  // Ore vein activation
    .addCustomOreVein("minecraft:diamond_ore")  // Specific ore types
    .oreVeinDensity(1.0f)              // Ore density multiplier
    .worldDimensions(-64, 384)         // World height bounds
    .seaLevel(64)                      // Sea level height
    .skyColor(0x87CEEB)                # Sky color (hex)
    .generate();
```

### Ore Generation System

The noise-based ore generation uses threshold-based rarity:
- **Diamond**: 0.92-1.0 threshold (8% spawn chance)
- **Gold**: 0.85-1.0 threshold (15% spawn chance)
- **Iron**: 0.7-1.0 threshold (30% spawn chance)
- **Copper**: 0.65-1.0 threshold (35% spawn chance)

Ore distribution is controlled by `minecraft:ore_gap` noise with proper depth conditions.

### Planet Configuration Examples

**For detailed planet generation documentation, see [planetary_generation.md](./planetary_generation.md)**

#### Extreme Testing (Hemphy)
- Continental scale: 50.0 (maximum landmass variation)
- Erosion scale: 100.0 (extreme erosion patterns)
- Jaggedness: 1.0 with 2000.0 noise scale (maximum terrain drama)
- Ore density: 3.0x (triple normal ore spawning)
- World height: -128 to 512 (640 total height)

#### Balanced Testing (Oretest)
- Continental scale: 25.0 (moderate landmass variation)
- Erosion scale: 35.0 (normal erosion patterns)
- Jaggedness: 0.3 with 800.0 noise scale (moderate mountains)
- Ore density: 1.0x (normal ore spawning)
- World height: -64 to 384 (standard Minecraft range)

## Project Documentation References

- `README.md` - Project overview and current implementation status
- `IMPLEMENTATION_ROADMAP.md` - Future development roadmap and planned features
- `DEVELOPMENT_HISTORY.md` - Complete development history and lessons learned
- `TERRABLENDER_INTEGRATION.md` - TerraBlender integration guide and configuration
- `CELESTIAL_CONFIGURATION.md` - Celestial bodies and sky customization guide
- Additional working directories with reference implementations:
  - `/home/keroppi/Development/Minecraft/Ad-Astra/` (original Ad Astra reference)
  - `/home/keroppi/Development/Minecraft/Mekanism/` (Mekanism reference)
- always update planetGenerationRunner when adding/editing planet generation