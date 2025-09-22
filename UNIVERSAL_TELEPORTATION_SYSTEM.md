# Universal Planet Teleportation System

## Overview

The Universal Planet Teleportation System provides comprehensive, modular teleportation to **every planet created by the mod**, automatically discovering planets from datapack files and enabling safe travel with validation and safety checks.

## Architecture

### Core Components

1. **PlanetDiscoveryService** - Automatically discovers all planets from mod datapacks
2. **PlanetTeleportationSystem** - Handles safe teleportation with validation and caching
3. **UniversalPlanetCommands** - Comprehensive command interface for planet management

### Key Features

- **Automatic Planet Discovery**: Dynamically finds all planets from `data/adastramekanized/planets/*.json`
- **Modular Design**: Each component can be used independently
- **Safety Validation**: Comprehensive safety checks before teleportation
- **Caching System**: Optimized spawn location caching for performance
- **Category Filtering**: Planets automatically categorized by characteristics
- **Fuzzy Search**: Find planets by partial name matching

## Commands

### Universal Planet Commands (`/planets`)

All commands require OP level 2.

#### Basic Commands

```bash
# List all discovered planets
/planets list

# List planets by category
/planets list habitable
/planets list airless
/planets list hostile

# Teleport to any planet (safe spawn)
/planets teleport <planet>

# Teleport specific player to planet
/planets teleport <planet> <player>

# Teleport to exact coordinates
/planets teleport <planet> <player> <x> <y> <z>
```

#### Advanced Commands

```bash
# Search for planets by name
/planets search mars
/planets search ice

# Get detailed planet information
/planets info mars
/planets info adastramekanized:moon

# List all available categories
/planets categories

# Find nearest planets to orbital distance
/planets nearest 150
/planets nearest 400 10

# System statistics
/planets stats

# Clear teleportation cache
/planets clearcache
```

## Planet Discovery

### Automatic Detection

The system automatically discovers planets by:

1. **Scanning datapack files** in `data/adastramekanized/planets/`
2. **Parsing JSON data** using the Planet codec
3. **Auto-categorizing** based on planet properties
4. **Registering** with the main planet registry

### Supported Planet Types

The system works with **any planet JSON file** in the mod, including:

- ✅ **Mars** - Thin atmosphere, night-only stars
- ✅ **Moon** - Airless, constant stars
- ✅ **Venus** - Thick toxic atmosphere, no stars
- ✅ **Mercury** - Airless, constant stars
- ✅ **Glacio** - Ice world, night-only stars
- ✅ **Binary World** - Space environment, constant stars
- ✅ **Earth Example** - Habitable, night-only stars
- ✅ **Any future planets** added to datapacks

### Planet Categories

Planets are automatically categorized:

- **habitable** / **hostile** - Based on life support requirements
- **atmospheric** / **airless** - Based on atmosphere presence
- **ice_world** / **hot_world** / **temperate** - Based on temperature
- **orbital** / **planetary** - Based on dimension type

## Teleportation System

### Safety Features

1. **Validation Checks**:
   - Player must be alive and not spectating
   - Planet must exist and be valid
   - Destination must be safe

2. **Safe Spawn Finding**:
   - Automatic surface detection
   - Air space validation
   - Fallback spawn locations

3. **Dimension Management**:
   - Automatic dimension creation
   - Fallback to Overworld if needed
   - Integration with existing dimension managers

### Teleportation Modes

- **SAFE_SPAWN** - Always find safe spawn location (default)
- **EXACT_POSITION** - Use exact coordinates if safe
- **STRICT_SURVIVAL** - Apply survival requirements (future)

### Caching System

- **Location Caching**: Remembers safe spawn points for 30 minutes
- **Performance Optimization**: Reduces calculation overhead
- **Automatic Cleanup**: Cache expires and clears on server restart

## Integration

### Existing Systems

The Universal Teleportation System integrates with:

- **PlanetRegistry** - Uses existing planet data
- **PlanetManager** - Works with server lifecycle
- **DynamicDimensionManager** - For dimension creation
- **PlanetDimensionManager** - For dimension management

### Backward Compatibility

- ✅ **Existing commands** continue to work (`/adastra`, `/planet_travel`)
- ✅ **Planet data format** unchanged
- ✅ **No breaking changes** to existing functionality

## Usage Examples

### Basic Teleportation

```bash
# Quick teleport to Mars
/planets teleport mars

# Teleport another player to the Moon
/planets teleport moon @a[limit=1]

# Go to specific coordinates on Venus
/planets teleport venus @s 100 64 200
```

### Planet Discovery

```bash
# See all planets
/planets list

# Find ice worlds
/planets list ice_world

# Search for planets with "mar" in the name
/planets search mar

# Get detailed info about Mars
/planets info mars
```

### Advanced Usage

```bash
# Find planets near Earth's orbit (150M km)
/planets nearest 150

# Find 5 closest planets to asteroid belt (400M km)
/planets nearest 400 5

# Check system status
/planets stats
```

## Performance

### Optimizations

- **Async Discovery**: Planet discovery runs on separate thread
- **Concurrent Data Structures**: Thread-safe for server environments
- **Spawn Caching**: Reduces repeated calculations
- **Lazy Loading**: Only creates dimensions when needed

### Scalability

- **Unlimited Planets**: No hard limits on planet count
- **Dynamic Adding**: New planets automatically discovered on reload
- **Memory Efficient**: Minimal memory footprint per planet

## Error Handling

### Robust Error Management

- **Graceful Fallbacks**: Overworld fallback if dimension creation fails
- **Validation Messages**: Clear error messages for invalid requests
- **Safety Checks**: Prevents teleportation to unsafe locations
- **Exception Handling**: Comprehensive error logging

### User Feedback

- **Success Messages**: Clear confirmation of successful teleportation
- **Warning Messages**: Alerts for hostile environments
- **Error Messages**: Helpful guidance for failed attempts
- **Planet Information**: Environmental details on arrival

## Future Enhancements

### Planned Features

- **GUI Interface**: Visual planet selection interface
- **Waypoints System**: Save and name custom locations
- **Group Teleportation**: Teleport multiple players together
- **Travel History**: Track visited planets
- **Fuel Requirements**: Integration with rocket fuel systems
- **Life Support Integration**: Real Mekanism oxygen requirements

### Extensibility

The modular design allows for easy extension:

- **Custom Validators**: Add new safety checks
- **Additional Filters**: Create new planet categories
- **Integration Hooks**: Connect with other mods
- **Custom Spawn Logic**: Override spawn finding behavior

## Technical Details

### File Structure

```
src/main/java/com/hecookin/adastramekanized/
├── common/planets/
│   └── PlanetDiscoveryService.java
├── common/teleportation/
│   └── PlanetTeleportationSystem.java
└── common/commands/
    └── UniversalPlanetCommands.java
```

### Dependencies

- ✅ **No external dependencies** beyond existing mod architecture
- ✅ **Minecraft 1.21.1** compatibility
- ✅ **NeoForge 21.1.209** compatibility
- ✅ **Thread-safe** for server environments

### Configuration

No additional configuration required - the system automatically:

- Discovers planets from existing datapack files
- Initializes on server startup
- Cleans up on server shutdown
- Integrates with existing planet registry

## Conclusion

The Universal Planet Teleportation System provides a comprehensive, modular solution for traveling to any planet created by the mod. With automatic discovery, safety validation, and an intuitive command interface, players can explore the entire universe of modded planets effortlessly.

The system is designed to be:
- **Future-proof**: Works with any new planets added
- **Safe**: Comprehensive validation and error handling
- **Fast**: Optimized caching and async operations
- **User-friendly**: Intuitive commands and clear feedback
- **Modular**: Each component can be used independently

Whether you're exploring a single planet or managing an entire solar system, the Universal Planet Teleportation System makes interplanetary travel seamless and safe.