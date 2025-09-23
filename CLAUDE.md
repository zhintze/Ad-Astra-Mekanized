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

### 🔄 Current Phase: Enhanced Planetary Generation
**Status**: 🔄 In Progress

#### Active Systems:
1. **TerraBlender Integration**:
   - ✅ Moon dimension with TerraBlender (2 biomes: highlands + maria)
   - ✅ Venus dimension with custom chunk generator bypass
   - ✅ Custom surface rules and template-based generation
   - ✅ Celestial body configuration system

2. **Current Working Features**:
   - ✅ Planet teleportation: `/planet teleport moon`, `/planet teleport venus`
   - ✅ Celestial body customization via JSON (suns, moons, planets, stars)
   - ✅ Custom sky rendering with configurable star fields
   - ✅ Atmospheric effects and dimension-specific environments

#### Current Issues Being Addressed:
1. **Venus Terrain Quality**: Replace basic netherrack with volcanic landscapes
2. **Moon Lava Generation**: Fix Moon generating as "giant pile of lava" instead of lunar terrain
3. **Enhanced Noise Systems**: Implement sophisticated terrain algorithms beyond basic noise

#### Next Immediate Priorities:
1. **Terrain Quality Improvements**: Enhance `generateDefaultVenusTerrain()` method in PlanetChunkGenerator
2. **TerraBlender Expansion**: Migrate Venus back to TerraBlender, re-enable Mars
3. **Multi-biome Planets**: Implement varied biome distribution per world
4. **Geological Features**: Add proper mountain ranges, valleys, crater systems

**For detailed TerraBlender information, see [TERRABLENDER_INTEGRATION.md](./TERRABLENDER_INTEGRATION.md)**
**For celestial configuration, see [CELESTIAL_CONFIGURATION.md](./CELESTIAL_CONFIGURATION.md)**

### Development Tools & Commands
```bash
# Build and testing
./gradlew clean build                  # Clean build (recommended after integration changes)
./gradlew runClient                    # Launch development client
./gradlew runData                      # Test mod loading and integrations

# Planet teleportation (current working commands)
/planet teleport moon                  # Travel to Moon (TerraBlender)
/planet teleport venus                 # Travel to Venus (custom generator)

# Time progression testing
/time set day                          # Test day cycle
/time set night                        # Test night cycle
```

## Project Documentation References

- `README.md` - Project overview and current implementation status
- `IMPLEMENTATION_ROADMAP.md` - Future development roadmap and planned features
- `DEVELOPMENT_HISTORY.md` - Complete development history and lessons learned
- `TERRABLENDER_INTEGRATION.md` - TerraBlender integration guide and configuration
- `CELESTIAL_CONFIGURATION.md` - Celestial bodies and sky customization guide
- Additional working directories with reference implementations:
  - `/home/keroppi/Development/Minecraft/Ad-Astra/` (original Ad Astra reference)
  - `/home/keroppi/Development/Minecraft/Mekanism/` (Mekanism reference)