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

### Phase 2 Complete: Dynamic Planet Generation System
**Status**: ✅ Implemented September 2024

#### Core Systems Implemented:
1. **Dynamic Planet Creation**:
   - `PlanetGenerationTool.java` - Procedural planet generation with 10 unique worlds
   - `DynamicPlanetCreator.java` - Runtime planet creation and management
   - `EnhancedDynamicPlanetCreator.java` - Advanced planet diversity features

2. **Planet Teleportation & Debugging**:
   - `PlanetDebugCommands.java` - `/planet list`, `/planet teleport`, `/planet info` commands
   - `UniversalPlanetCommands.java` - Enhanced command system with autocomplete
   - Fixed autocomplete to support both short names and full IDs

3. **Planet Diversity & Randomization**:
   - Unique noise settings per planet with randomized terrain parameters
   - Planet-type-specific characteristics (Volcanic, Ice World, Gas Giant, etc.)
   - Randomized celestial textures and atmospheric effects
   - 6 distinct dimension effect types implemented

4. **Data Generation System**:
   - Automatic dimension JSON generation
   - Biome and noise settings creation
   - Dimension effects class generation
   - Automatic cleanup system for planet regeneration

#### Technical Achievements:
- **10 Unique Planets Generated**: Each with distinct noise parameters and terrain characteristics
- **Planet Types Implemented**: Moon-like, Rocky, Ice World, Volcanic, Gas Giant, Asteroid-like, Altered Overworld
- **Noise Generation Fixed**: Resolved identical terrain issue with planet-specific randomization
- **Build System**: Successful integration with NeoForge 21.1.209 and Mekanism 10.7.15

### Current Issues Identified:
1. **Terrain Quality**: Current noise generation produces suboptimal landscapes
2. **Ore Spawning**: No ore generation system implemented yet
3. **Biome Diversity**: Limited biome variety per planet
4. **Command Interface**: Planet command autofill suggestions are poorly implemented and need cleanup

### Next Development Phase: Terrain & Resource Systems

#### Priority 1: Enhanced Terrain Generation
- **Improve noise functions**: Replace basic noise with more sophisticated terrain algorithms
- **Multi-biome planets**: Implement varied biome distribution per world
- **Realistic geological features**: Add proper mountain ranges, valleys, crater systems
- **Planet-appropriate terrain**: Mars-like for rocky worlds, ice formations for ice worlds, etc.

#### Priority 1.5: Command System Improvements
- **Clean up planet command suggestions**: Improve autocomplete quality and remove redundant/confusing suggestions
- **Streamline command interface**: Make planet teleportation commands more intuitive
- **Better error messages**: Provide clearer feedback for invalid planet names or commands

#### Priority 2: Ore Spawning & Resource Systems
- **Planet-specific ore distribution**: Unique mineral deposits per planet type
- **Depth-based ore spawning**: Realistic ore layer distribution
- **Rare resource concentration**: Special ores in specific planet types
- **Integration with existing mod ores**: Mekanism, Create, and vanilla ores

#### Priority 3: Advanced Planet Features
- **Multi-biome generation**: Temperature/humidity gradients across planets
- **Atmospheric effects**: Proper gas giant atmospheric rendering
- **Surface features**: Craters, canyons, volcanic formations
- **Resource scarcity balance**: Ensure exploration motivation

#### Priority 4: Performance & Polish
- **Chunk generation optimization**: Improve world loading performance
- **Memory usage**: Optimize planet data storage
- **Visual improvements**: Enhanced sky rendering and atmospheric effects

### Development Tools & Commands
```bash
# Planet generation and testing
./gradlew runClient                    # Test in-game
/planet list                          # View all generated planets
/planet teleport <planet_name>        # Travel to specific planet
/planet info <planet_name>           # View planet details

# Planet regeneration
# Set OVERWRITE_EXISTING = true in PlanetGenerationTool.java
# Run PlanetGenerationTool to regenerate all planets
```

## Project Documentation References

- `README.md` - Comprehensive project overview and features
- `IMPLEMENTATION_ROADMAP.md` - Detailed 7-phase implementation plan
- `project_plan.md` - Technical architecture and development history
- `planetary_generation.md` - Planet generation system documentation
- `CELESTIAL_CUSTOMIZATION_GUIDE.md` - Planet customization guidelines
- Additional working directories with reference implementations:
  - `/home/keroppi/Development/Minecraft/Ad-Astra/` (original Ad Astra reference)
  - `/home/keroppi/Development/Minecraft/Mekanism/` (Mekanism reference)