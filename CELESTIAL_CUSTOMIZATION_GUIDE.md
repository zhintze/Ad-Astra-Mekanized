# Celestial Bodies & Sky Customization Guide

Complete guide for customizing celestial bodies, stars, and sky rendering in AdAstra Mekanized.

## Quick Reference

All customization is done through JSON files in `src/main/resources/data/adastramekanized/planets/`

**Key Files:**
- `mars.json` - Mars dimension sky configuration
- `moon.json` - Moon dimension sky configuration (if exists)
- Any custom planet JSON files

## Sky Configuration Structure

### Basic Sky Settings
```json
{
  "rendering": {
    "sky": {
      "sky_color": 15510660,           // RGB hex color for sky
      "sunrise_color": 14349555,       // RGB hex color for sunrise/sunset
      "custom_sky": true,              // Enable custom sky rendering
      "has_stars": true,               // Show stars
      "star_count": 8000,              // Number of stars (recommended: 1000-25000)
      "star_brightness": 0.8           // Star brightness (0.0-2.0)
    },
    "celestial_bodies": {
      // Celestial body configurations...
    }
  }
}
```

## Celestial Bodies Configuration

### Sun Configuration
```json
"sun": {
  "texture": "minecraft:textures/environment/sun.png",  // Texture path
  "scale": 0.6,                                         // Size (0.1-3.0)
  "color": 16769205,                                    // RGB hex tint
  "visible": true                                       // Show/hide sun
}
```

**Sun Behavior:**
- Follows `TIME_OF_DAY` movement (east to west arc)
- Always moves with day/night cycle
- Recommended scale: 0.5-1.2

### Moon Configuration
```json
"moons": [
  {
    "texture": "minecraft:textures/block/stone.png",    // Any texture
    "scale": 0.4,                                       // Size
    "color": 11184810,                                  // RGB hex tint
    "orbit_phase": 0.15,                               // Orbital position (0.0-1.0)
    "visible": true                                     // Show/hide moon
  }
]
```

**Moon Behavior:**
- Multiple moons supported (array)
- Movement: `TIME_OF_DAY_REVERSED` (opposite to sun)
- `orbit_phase` determines sky position:
  - `0.0` = one position
  - `0.5` = opposite side of sky
  - Similar values = moons appear close together

### Planet Configuration
```json
"visible_planets": [
  {
    "texture": "minecraft:textures/block/lapis_block.png", // Any texture
    "scale": 2.5,                                          // Size
    "color": 3361970,                                      // RGB hex tint
    "distance": 0.3,                                       // Position factor
    "visible": true                                        // Show/hide planet
  }
]
```

**Planet Behavior:**
- Movement: `STATIC` (fixed in sky)
- `distance` affects positioning in sky dome
- Ideal for background objects like Earth from Moon

## Color System

All colors use RGB hex format **without** the `#` prefix:

```json
"color": 16769205    // Equivalent to #FFB55D (orange-yellow)
```

**Common Colors:**
- `16777215` - Pure white (#FFFFFF)
- `16711680` - Pure red (#FF0000)
- `65280` - Pure green (#00FF00)
- `255` - Pure blue (#0000FF)
- `16769205` - Orange-yellow (#FFB55D)
- `11184810` - Gray-brown (#AAAA0A)

**Converting Colors:**
- From hex `#FFB55D`: Remove `#`, convert to decimal = `16769205`
- Online converters: Search "hex to decimal converter"

## Texture System

**Guaranteed Available Textures:**
```json
// Vanilla environment textures
"minecraft:textures/environment/sun.png"
"minecraft:textures/environment/moon_phases.png"

// Block textures (always available)
"minecraft:textures/block/stone.png"
"minecraft:textures/block/cobblestone.png"
"minecraft:textures/block/dirt.png"
"minecraft:textures/block/sand.png"
"minecraft:textures/block/red_sand.png"
"minecraft:textures/block/iron_block.png"
"minecraft:textures/block/gold_block.png"
"minecraft:textures/block/lapis_block.png"
"minecraft:textures/block/diamond_block.png"
"minecraft:textures/block/emerald_block.png"
```

**Custom Textures:**
Place in `src/main/resources/assets/adastramekanized/textures/` and reference as:
```json
"texture": "adastramekanized:textures/custom/my_star.png"
```

## Scale Guidelines

**Recommended Scales:**
- **Sun**: 0.5-1.2 (normal solar appearance)
- **Moons**: 0.2-0.8 (realistic lunar sizes)
- **Planets**: 0.5-3.0 (visible but not overwhelming)
- **Stars**: Controlled by `star_count`, not individual scale

**Scale Effects:**
- `< 0.3` - Very small, may be hard to see
- `0.3-0.8` - Good for moons and distant objects
- `0.8-1.5` - Good for suns and primary objects
- `> 2.0` - Very large, dominates sky view

## Movement Types

**Automatic Movement Assignment:**
- **Sun**: Always `TIME_OF_DAY` (moves with day/night)
- **Moons**: `TIME_OF_DAY_REVERSED` if `orbit_phase > 0`, else `STATIC`
- **Planets**: Always `STATIC` (fixed position)

**Movement Behaviors:**
- `TIME_OF_DAY`: East-to-west arc following day/night cycle
- `TIME_OF_DAY_REVERSED`: West-to-east arc (opposite sun)
- `STATIC`: Fixed position in sky

## Star Field Configuration

```json
"sky": {
  "has_stars": true,
  "star_count": 8000,         // Total stars to generate
  "star_brightness": 0.8      // Brightness multiplier
}
```

**Star Count Recommendations:**
- `1000-3000` - Light star field
- `5000-10000` - Medium density (recommended)
- `15000-25000` - Dense star field (space-like)
- `> 30000` - May impact performance

**Star Brightness:**
- `0.5` - Dim stars
- `0.8` - Normal brightness
- `1.0` - Bright stars
- `1.5+` - Very bright (may wash out other objects)

## Example Configurations

### Earth-like Sky (with Moon)
```json
"rendering": {
  "sky": {
    "sky_color": 7842047,
    "has_stars": true,
    "star_count": 3000,
    "star_brightness": 0.6
  },
  "celestial_bodies": {
    "sun": {
      "texture": "minecraft:textures/environment/sun.png",
      "scale": 1.0,
      "color": 16777215,
      "visible": true
    },
    "moons": [
      {
        "texture": "minecraft:textures/environment/moon_phases.png",
        "scale": 0.5,
        "color": 16777215,
        "orbit_phase": 0.2,
        "visible": true
      }
    ],
    "visible_planets": []
  }
}
```

### Alien World (Multiple Moons, Distant Planets)
```json
"rendering": {
  "sky": {
    "sky_color": 4718592,
    "has_stars": true,
    "star_count": 12000,
    "star_brightness": 1.2
  },
  "celestial_bodies": {
    "sun": {
      "texture": "minecraft:textures/environment/sun.png",
      "scale": 0.7,
      "color": 16744192,
      "visible": true
    },
    "moons": [
      {
        "texture": "minecraft:textures/block/iron_block.png",
        "scale": 0.3,
        "color": 12632256,
        "orbit_phase": 0.1,
        "visible": true
      },
      {
        "texture": "minecraft:textures/block/gold_block.png",
        "scale": 0.4,
        "color": 16766720,
        "orbit_phase": 0.6,
        "visible": true
      }
    ],
    "visible_planets": [
      {
        "texture": "minecraft:textures/block/lapis_block.png",
        "scale": 1.2,
        "color": 255,
        "distance": 0.4,
        "visible": true
      }
    ]
  }
}
```

### Space Station View (Tiny Sun, Many Stars, Large Planet)
```json
"rendering": {
  "sky": {
    "sky_color": 0,
    "has_stars": true,
    "star_count": 25000,
    "star_brightness": 2.0
  },
  "celestial_bodies": {
    "sun": {
      "texture": "minecraft:textures/environment/sun.png",
      "scale": 0.3,
      "color": 16777215,
      "visible": true
    },
    "moons": [],
    "visible_planets": [
      {
        "texture": "minecraft:textures/block/lapis_block.png",
        "scale": 2.5,
        "color": 3361970,
        "distance": 0.3,
        "visible": true
      },
      {
        "texture": "minecraft:textures/block/iron_block.png",
        "scale": 0.4,
        "color": 11184810,
        "distance": 0.7,
        "visible": true
      }
    ]
  }
}
```

## Testing Your Configuration

1. **Edit the JSON file** in `src/main/resources/data/adastramekanized/planets/`
2. **Restart Minecraft** (changes require restart)
3. **Teleport to dimension**:
   ```
   /tp @s ~ ~ ~ adastramekanized:mars
   /tp @s ~ ~ ~ adastramekanized:moon
   ```
4. **Test time progression**:
   ```
   /time set day
   /time set noon
   /time set night
   /time set midnight
   ```
5. **Look around** - stars and planets should be visible in all directions

## Troubleshooting

**No sky changes visible:**
- Restart Minecraft completely
- Check JSON syntax (use JSON validator)
- Verify you're in the correct dimension

**Celestial bodies not moving:**
- Sun should always move with time
- Use `/time add 1000` to test movement
- Moons move opposite to sun

**Stars not visible:**
- Ensure `"has_stars": true`
- Increase `star_count` and `star_brightness`
- Stars are more visible at night

**Objects too large/small:**
- Adjust `scale` values
- Recommended ranges in Scale Guidelines section

**Missing textures:**
- Use guaranteed vanilla textures listed above
- Check custom texture paths are correct

## Performance Notes

- **Star count** has the biggest performance impact
- Keep total celestial bodies under 10 for best performance
- Large `scale` values don't impact performance
- High `star_brightness` values don't impact performance

## File Locations

```
src/main/resources/data/adastramekanized/planets/
├── mars.json          # Mars dimension configuration
├── moon.json          # Moon dimension (if exists)
└── custom_world.json  # Your custom dimensions
```

Changes require a full Minecraft restart to take effect.