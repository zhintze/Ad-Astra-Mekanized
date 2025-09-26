# Mowzie's Mobs Configuration Guide for Ad Astra Mekanized

## Overview
This guide explains how to configure Mowzie's Mobs to work exclusively with Ad Astra Mekanized's custom planet spawning system, preventing default overworld spawning while keeping the mobs available for your space dimensions.

## Required Configuration

### Step 1: Disable Natural Spawning
Edit `config/mowziesmobs-common.toml` and set all spawn rates to 0:

```toml
[mobs.foliaath.spawn_config]
spawn_rate = 0  # Was 70

[mobs.umvuthana.spawn_config]
spawn_rate = 0  # Was 5

[mobs.grottol.spawn_config]
spawn_rate = 0  # Was 2

[mobs.lantern.spawn_config]
spawn_rate = 0  # Was 5

[mobs.naga.spawn_config]
spawn_rate = 0  # Was 20

[mobs.bluff.spawn_config]
spawn_rate = 0  # Was 10
```

### Step 2: Disable Structure Generation (Optional)
If you also want to prevent Mowzie's structures from generating in the overworld:

```toml
[mobs.frostmaw.generation_config]
generation_distance = -1  # Disables Frostmaw lairs

[mobs.umvuthi.generation_config]
generation_distance = -1  # Disables Umvuthana groves

[mobs.ferrous_wroughtnaut.generation_config]
generation_distance = -1  # Disables Wroughtnaut chambers

[mobs.sculptor.generation_config]
generation_distance = -1  # Disables Sculptor structures
```

## Available Mowzie's Mobs for Planets

### Regular Mobs
- `foliaath` - Carnivorous plant (jungle themed)
- `baby_foliaath` - Smaller carnivorous plant
- `umvuthana` - Tribal warrior (mask varies)
- `umvuthana_raptor` - Raptor-riding warrior
- `umvuthana_crane` - Crane-masked warrior
- `grottol` - Cave-dwelling creature
- `lantern` - Floating mystical light
- `naga` - Beach/coastal serpent
- `bluff` - Camouflaged cave dweller

### Boss Mobs
- `frostmaw` - Ice beast boss
- `ferrous_wroughtnaut` - Mechanical guardian boss
- `umvuthi` - Tribal chieftain boss
- `sculptor` - Mountain sculptor boss

## Using Mowzie's Mobs in PlanetGenerationRunner

### Method 1: Use Presets
```java
PlanetMaker.planet("myplanet")
    .addMowziesMobsPreset("jungle")     // Foliaaths
    .addMowziesMobsPreset("savanna")    // Umvuthana tribes
    .addMowziesMobsPreset("cave")       // Grottols and Bluffs
    .addMowziesMobsPreset("mystical")   // Lanterns
    .addMowziesMobsPreset("coastal")    // Nagas
    .addMowziesMobsPreset("frozen")     // Frostmaw (rare)
    .addMowziesMobsPreset("industrial") // Wroughtnaut (rare)
    .addMowziesMobsPreset("mixed")      // Variety pack
    .generate();
```

### Method 2: Individual Control
```java
PlanetMaker.planet("myplanet")
    .addMowziesMob("foliaath", 50, 1, 3)      // Weight 50, groups of 1-3
    .addMowziesMob("grottol", 30, 1, 1)       // Weight 30, single spawns
    .addMowziesMob("lantern", 40, 2, 4)       // Weight 40, groups of 2-4
    .generate();
```

### Method 3: Direct Spawn Definition
```java
PlanetMaker.planet("myplanet")
    .addMobSpawn("monster", "mowziesmobs:naga", 60, 1, 2)
    .addMobSpawn("ambient", "mowziesmobs:lantern", 50, 2, 4)
    .generate();
```

## Example Planets with Mowzie's Mobs

### Primal (Jungle World)
- **Theme**: Dense jungle with dangerous plants
- **Mobs**: Foliaaths, Baby Foliaaths, Lanterns, Nagas
- **Command**: `/planet teleport primal`

### Tribal (Savanna World)
- **Theme**: Vast savannas with tribal warriors
- **Mobs**: Umvuthana warriors, Umvuthi chief (rare)
- **Command**: `/planet teleport tribal`

## Spawn Weight Guidelines
- **Common**: 40-100 (frequently encountered)
- **Uncommon**: 20-40 (occasional encounters)
- **Rare**: 5-20 (infrequent spawns)
- **Very Rare**: 1-5 (boss mobs, special encounters)

## Testing Commands
After configuring and generating planets:
```
/planet teleport primal
/planet teleport tribal
```

## Troubleshooting

### Mobs Still Spawning in Overworld
- Verify all `spawn_rate` values are set to 0
- Restart the game after config changes
- Check for other mods that might force spawning

### Mobs Not Spawning on Planets
- Ensure Mowzie's Mobs is installed
- Verify mob IDs are correct (use `mowziesmobs:` prefix)
- Check spawn weights aren't too low
- Confirm planet has `disableMobGeneration(false)`

### Performance Issues
- Reduce spawn weights for heavy mobs (bosses)
- Limit group sizes for complex mobs
- Consider spreading mob types across different planets

## Integration Benefits
- Complete control over where Mowzie's mobs spawn
- Custom themed planets with appropriate creatures
- No unexpected spawns disrupting gameplay balance
- Boss mobs as rare planetary encounters