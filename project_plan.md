# AdAstra Mekanized - Project Plan

## Project Overview

**AdAstra Mekanized** is a complete replacement mod for Ad Astra that integrates space exploration features with Mekanism's infrastructure systems. This mod provides rockets, planets, space stations, and oxygen distribution while using Mekanism's chemical systems, energy systems, and Immersive Engineering fuels.

### Core Requirements
- **Target Platform**: NeoForge 1.21.1 (version 21.1.209)
- **Replacement Approach**: Complete replacement for Ad Astra, no world compatibility required
- **Integration Focus**: Deep integration with Mekanism, Immersive Engineering, and Create
- **Development Strategy**: Incremental development with hybrid integration approach

## Technical Architecture

### Mod Dependencies & Integration Strategy

**Hybrid Integration Approach**: Uses reflection-based integration to support optional mod dependencies while providing full API access during development.

#### Dependency Configuration
- **Mekanism** (10.7.8+): Optional - Chemical systems (oxygen) and energy systems
- **Immersive Engineering** (12.4.3+): Optional - Fuel systems (diesel/biodiesel replace Ad Astra fuels)
- **Create** (0.5.1+): Optional - Mechanical systems for contraptions and automation

#### Integration Architecture
Located in `src/main/java/com/hecookin/adastramekanized/integration/`:

1. **Interface Layer** (`/api/`):
   - `IChemicalIntegration.java` - Oxygen and chemical handling
   - `IEnergyIntegration.java` - Power systems
   - `IFuelIntegration.java` - Rocket fuel systems

2. **Implementation Layer**:
   - `MekanismIntegration.java` - Reflection-based Mekanism API access
   - `ImmersiveEngineeringIntegration.java` - Reflection-based IE fuel integration
   - `CreateIntegration.java` - Reflection-based Create mechanical integration

3. **Management Layer**:
   - `ModIntegrationManager.java` - Central coordinator with fallback implementations
   - `ModIntegrationTest.java` - Integration testing during mod setup

### Build System Configuration

**Key Build Files**:
- `build.gradle.kts` - NeoGradle 7.0.192 with NeoForge 21.1.209
- `gradle.properties` - Project metadata and version configuration
- `src/main/resources/META-INF/neoforge.mods.toml` - Mod metadata with **optional** dependencies

**Critical Configuration Notes**:
- Dependencies marked as "optional" not "required" to support hybrid approach
- No compile-time dependencies on integration mods (reflection handles runtime access)
- Template expansion simplified to avoid version range issues

## Current Development Status

### ✅ Phase 1 Complete: Integration Architecture Foundation
**Status**: Fully implemented and tested (September 2025)

**Achievements**:
- Reflection-based integration system for all target mods
- Graceful fallback implementations when mods are missing
- Central integration manager with unified API
- Clean compilation without dependency mods present
- Successful mod loading with optional dependency system

**Key Files Implemented**:
- All integration architecture files
- Main mod class with integration manager initialization
- Configuration system with integration settings
- Build system properly configured for hybrid approach

### 🚧 Next Phase: Block & Material Migration

**Phase 2 Priority**: Migrate all basic block types with Ad Astra textures
- Migrate all Ad Astra blocks and materials (excluding lamps and globes)
- Implement all planet-specific ores (moon cheese, desh, etc.)
- Create all industrial/factory block sets
- Migrate all alien wood blocks
- Set up texture assets and registration patterns

**Note**: Industrial lamps (32 blocks) and globes (6 blocks) were excluded from migration by design decision.

## Development Roadmap

### Phase 2: Block & Material Migration (Current Priority)
- [ ] Core Materials & Items (ingots, nuggets, plates, rods)
- [ ] Planet-specific Ores (all planets: Moon, Mars, Venus, Mercury, Glacio, Earth Deepslate)
- [ ] Processed Material Blocks (metal blocks, raw material blocks, special blocks)
- [ ] Planet Stone Sets (complete building block sets for all planets)
- [ ] Industrial/Factory Block Sets (Iron, Steel, Etrium, Desh, Ostrum, Calorite)
- [ ] Alien Wood Blocks (Aeronos and Strophar sets)
- [ ] Decorative Blocks (Flags only - Lamps and Globes excluded)
- [ ] Texture Assets Migration and Registration

### Phase 3: Dimension Generation System
- [ ] Planet terrain generation and biomes
- [ ] Planet-specific world generation features
- [ ] Dimension management and registration
- [ ] Environmental effects and atmosphere handling

### Phase 4: Core Space Travel System
- [ ] Rocket block entity and GUI
- [ ] Rocket fuel tank integration (IE fuels)
- [ ] Basic launch mechanics and trajectory calculation
- [ ] Oxygen consumption and Mekanism chemical integration

### Phase 5: Advanced Systems
- [ ] Space stations and construction
- [ ] Advanced oxygen distribution networks
- [ ] Create integration for automated systems
- [ ] Resource extraction and processing

### Phase 6: Polish & Optimization
- [ ] Performance optimization
- [ ] Advanced GUI systems
- [ ] Achievement system
- [ ] Documentation and compatibility

## Development Guidelines

### Code Standards
- **Package Structure**: `com.hecookin.adastramekanized.{feature}`
- **Integration Access**: Always use `AdAstraMekanized.getIntegrationManager()`
- **Error Handling**: Graceful fallbacks for all integration failures
- **Logging**: Use `AdAstraMekanized.LOGGER` for consistent logging

### Integration Best Practices
1. **Always check integration availability** before using mod-specific features
2. **Provide fallback behavior** for all integrated systems
3. **Cache reflected classes and methods** for performance
4. **Test both with and without** integration mods present

### Build and Testing
- **Clean builds recommended** when changing integration code
- **Test with `./gradlew runData`** to verify mod loading
- **Integration tests** automatically run during mod setup
- **No compile-time dependencies** should be added to integration mods

## Key Files Reference

### Core Files
- `AdAstraMekanized.java` - Main mod class, integration manager initialization
- `AdAstraMekanizedConfig.java` - Configuration system with integration settings
- `ModIntegrationManager.java` - Central integration coordinator

### Integration Files
- `mekanism/MekanismIntegration.java` - Chemical and energy systems
- `immersiveengineering/ImmersiveEngineeringIntegration.java` - Fuel systems
- `create/CreateIntegration.java` - Mechanical systems

### Build Files
- `build.gradle.kts` - Main build configuration
- `neoforge.mods.toml` - Mod metadata with optional dependencies
- `gradle.properties` - Project properties and versions

## Historical Context

### Why Reflection-Based Integration?
The project initially attempted Maven-based compilation dependencies but encountered NeoGradle restrictions. The reflection-based approach was adopted to:
- Work around ModDevGradle dependency limitations
- Maintain clean compilation without mod JARs
- Support true optional integration (mod works with or without dependencies)
- Provide full API access when mods are present

### Design Decisions
- **No Ad Astra compatibility**: Complete replacement approach reduces complexity
- **Single large mod**: Easier to maintain than multiple smaller mods
- **Mekanism-first**: Primary integration focus due to chemical system fit
- **IE fuel replacement**: Better integration than custom fuel systems

## Future Considerations

### Potential Extensions
- **Additional mod integrations**: Applied Energistics, Thermal Expansion
- **Compatibility layers**: For popular space mods if requested
- **API exposure**: Allow other mods to integrate with our systems

### Performance Notes
- Reflection overhead is minimized through caching
- Integration checks performed once during initialization
- Fallback implementations are lightweight no-ops

## Development Environment Setup

### Required Tools
- Java 21 (OpenJDK recommended)
- Gradle 8.14.3+ (wrapper included)
- NeoForge MDK 1.21.1

### Quick Start Commands
```bash
./gradlew build          # Build the mod
./gradlew runData        # Test mod loading and integrations
./gradlew runClient      # Launch development client
./gradlew clean build    # Clean build (recommended after integration changes)
```

### Integration Testing
The mod includes automatic integration tests that run during setup. Look for logs containing:
- "Mekanism integration initialized"
- "Immersive Engineering integration initialized"
- "Create integration initialized"
- Integration status summary

---

**Last Updated**: September 2025
**Current Phase**: Phase 1 Complete, Phase 2 Ready
**Status**: Ready for game content implementation