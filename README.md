# Ad Astra Mekanized

A space exploration mod for Minecraft 1.21.1 that integrates the best features of Ad Astra with Mekanism's infrastructure systems.

## Overview

Ad Astra Mekanized is a complete replacement for the Ad Astra mod, designed to provide seamless integration with Mekanism, Immersive Engineering, and Create. This mod combines space exploration, planet travel, and rocket building with advanced chemical processing, energy systems, and fuel production chains.

## Key Features

### 🚀 Space Exploration
- **Rocket Building**: Construct rockets using Mekanism energy and IE fuel systems
- **Planet Travel**: Visit multiple planets with unique environmental conditions
- **Space Stations**: Build and manage orbital facilities
- **Dimensional Travel**: Seamless travel between planets and space

### 🌍 Planet System
- **Working Planets**: Moon (TerraBlender integration), Venus (custom generation)
- **Celestial Customization**: Configurable suns, moons, planets, and star fields via JSON
- **Environmental Effects**: Gravity, temperature, and atmospheric conditions
- **Terrain Generation**: Hybrid TerraBlender system with custom surface rules

### 💨 Oxygen System (Mekanism Integration)
- **Chemical-based Oxygen**: Oxygen handled as Mekanism chemical
- **Oxygen Distribution**: Use Mekanism gas pipes and tanks
- **Area Effects**: Positional oxygen for breathable environments
- **Space Suit Integration**: Oxygen storage in Mekanism-compatible suits

### ⛽ Fuel System (Immersive Engineering Integration)
- **IE Fuel Types**: Uses IE fuel and biodiesel instead of Ad Astra fuels
- **Production Chains**: Integrate with IE fuel production systems
- **No Ad Astra Fuel**: Complete replacement of Ad Astra fuel system

### ⚡ Energy System (Mekanism Integration)
- **Mekanism Energy**: All machines use Mekanism energy API
- **No Solar Panels**: Rely on Mekanism's existing energy generation
- **Power Distribution**: Use Mekanism cables and energy systems

## Technical Architecture

### Dependencies
- **NeoForge 1.21.1**: 21.1.209+
- **Mekanism**: 10.7.8+ (chemical and energy systems)
- **Immersive Engineering**: 12.4.3+ (fuel production)
- **Create**: 0.5.1+ (mechanical systems and material processing)

### Package Structure
```
com.hecookin.adastramekanized/
├── api/                    # Public API for integration
├── common/                 # Core game logic and systems
│   ├── planets/           # Planet management and generation
│   ├── rockets/           # Rocket entities and mechanics
│   ├── oxygen/            # Oxygen distribution system
│   ├── fuel/              # Fuel system integration
│   └── integration/       # Mod integration handlers
├── client/                # Client-side rendering and UI
│   ├── screens/           # Planet selection and machine GUIs
│   ├── renderers/         # Rocket and space station rendering
│   └── effects/           # Space and planetary effects
└── config/                # Configuration system
```

### Key Systems

#### Planet Storage System
- **Data-driven**: Planets defined in JSON files in `data/planets/`
- **Codec-based**: Uses Minecraft's Codec system for serialization
- **Network Sync**: Efficient client-server synchronization
- **Dynamic Loading**: Hot-reloadable planet configurations

#### Oxygen Distribution
```java
// Mekanism chemical integration
ChemicalStack oxygen = ChemicalStack.of(MekanismChemicals.OXYGEN, amount);
// Position-based distribution (from Ad Astra)
OxygenDistributor.setOxygen(level, pos, hasOxygen);
```

#### Fuel Integration
```java
// Immersive Engineering fuel compatibility
FluidStack fuel = new FluidStack(IEFluids.FUEL, amount);
// Rocket fuel consumption
rocket.consumeFuel(IEFluids.FUEL, consumptionRate);
```

## Current Implementation Status

### ✅ Completed Phases

#### Phase 1: Foundation & Integration Architecture
- [x] Project scaffold and build configuration (NeoForge 1.21.1)
- [x] Reflection-based integration system for optional dependencies
- [x] Central integration manager with fallback implementations
- [x] Comprehensive configuration system

#### Phase 2: Block & Material Migration + Planet System Core
- [x] Complete block and material migration (112+ blocks, 25+ items)
- [x] Planet stone variants, industrial blocks, ore blocks, alien wood sets
- [x] Creative tab organization (4-tab system)
- [x] Core planet data structures with Codec serialization
- [x] JSON-based planet loading and client-server networking
- [x] Planet registry system with thread-safe management

### 🔄 Current Phase: Planetary Dimensions & TerraBlender Integration
- [x] Moon dimension with TerraBlender integration (2 biomes, custom surface rules)
- [x] Venus dimension with custom chunk generator bypass
- [x] Custom celestial body configuration system
- [ ] Enhanced terrain generation and biome diversity
- [ ] Complete TerraBlender integration for all planets

### 📋 Future Roadmap
See [IMPLEMENTATION_ROADMAP.md](./IMPLEMENTATION_ROADMAP.md) for detailed future development plans.

## Documentation

- **[IMPLEMENTATION_ROADMAP.md](./IMPLEMENTATION_ROADMAP.md)** - Future development roadmap and planned features
- **[DEVELOPMENT_HISTORY.md](./DEVELOPMENT_HISTORY.md)** - Complete development history and lessons learned
- **[TERRABLENDER_INTEGRATION.md](./TERRABLENDER_INTEGRATION.md)** - TerraBlender integration guide and configuration
- **[CELESTIAL_CONFIGURATION.md](./CELESTIAL_CONFIGURATION.md)** - Celestial bodies and sky customization guide
- **[CLAUDE.md](./CLAUDE.md)** - Development guidelines and project overview for Claude Code

## Development Guidelines

### Code Standards
- Follow NeoForge 1.21.1 patterns and best practices
- Use Mekanism's API patterns for consistency
- Document all public APIs thoroughly
- Include comprehensive inline comments

### Integration Principles
- **Mekanism First**: Use Mekanism systems whenever possible
- **No Duplication**: Don't recreate what Mekanism already provides
- **Seamless Integration**: Feel like a natural extension of Mekanism
- **Performance**: Maintain good performance with large planet systems

### Testing Strategy
- Unit tests for core logic
- Integration tests with Mekanism APIs
- In-game testing for all major features
- Performance testing for planet generation

## Configuration

The mod uses a comprehensive configuration system:

```toml
[space_exploration]
enableSpaceExploration = true
enablePlanetGeneration = true
maxPlanetsPerSolarSystem = 8

[oxygen_system]
enableOxygenSystem = true
oxygenDistributorRange = 16
oxygenConsumptionRate = 1.0

[mod_integration]
enableMekanismIntegration = true
enableImmersiveEngineeringIntegration = true
enableCreateIntegration = true
```

## Building

```bash
./gradlew build
```

## Running in Development

```bash
# Client
./gradlew runClient

# Server
./gradlew runServer

# Data Generation
./gradlew runData
```

## Contributing

1. Follow the established code structure
2. Test integration with all dependent mods
3. Document any new APIs or significant changes
4. Ensure compatibility with the target NeoForge version

## License

All Rights Reserved

## Credits

- **Ad Astra**: Original space exploration concepts and textures
- **Mekanism**: Chemical and energy system integration
- **Immersive Engineering**: Fuel production systems
- **Create**: Mechanical system integration