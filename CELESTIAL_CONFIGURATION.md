# Celestial Bodies Configuration Guide

This comprehensive guide explains how to configure celestial bodies (suns, moons, planets), stars, and sky rendering in Ad Astra Mekanized planetary dimensions.

## Quick Reference

All customization is done through JSON files in `src/main/resources/data/adastramekanized/planets/`

**Key Files:**
- `mars.json` - Mars dimension sky configuration
- `moon.json` - Moon dimension sky configuration (if exists)
- Any custom planet JSON files

## Configuration Structure

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
    "horizontal_position": 0.4,                        // Horizontal position in sky (-1.0 to 1.0)
    "vertical_position": 0.15,                         // Vertical position in sky (-1.0 to 1.0)
    "moves_with_time": true,                           // Whether moon moves with time
    "visible": true                                     // Show/hide moon
  }
]
```

**Moon Behavior:**
- Multiple moons supported (array)
- Movement: Configurable via `moves_with_time`
  - `true` = `TIME_OF_DAY` (same direction as sun, east to west arc)
  - `false` = `STATIC` (fixed position)
- `horizontal_position` determines horizontal sky position (-1.0 to 1.0)
- `vertical_position` determines vertical sky position (-1.0 to 1.0)

### Planet Configuration
```json
"visible_planets": [
  {
    "texture": "minecraft:textures/block/lapis_block.png", // Any texture
    "scale": 2.5,                                          // Size
    "color": 3361970,                                      // RGB hex tint
    "horizontal_position": 0.3,                            // Horizontal position in sky (-1.0 to 1.0)
    "vertical_position": 0.2,                              // Vertical position in sky (-1.0 to 1.0)
    "moves_with_time": true,                               // Whether planet moves with time
    "visible": true                                        // Show/hide planet
  }
]
```

**Planet Behavior:**
- Movement: Configurable via `moves_with_time`
  - `true` = `TIME_OF_DAY` (east to west arc following day/night cycle)
  - `false` = `STATIC` (fixed position)
- `horizontal_position` affects horizontal positioning in sky dome (-1.0 to 1.0)
- `vertical_position` controls vertical height: `-1.0` = below horizon, `0.0` = horizon level, `1.0` = overhead

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

// Our custom celestial textures (copied from Ad Astra)
"adastramekanized:textures/celestial/earth.png"
"adastramekanized:textures/celestial/mars.png"
"adastramekanized:textures/celestial/venus.png"
"adastramekanized:textures/celestial/mercury.png"
"adastramekanized:textures/celestial/glacio.png"
"adastramekanized:textures/celestial/moon.png"
"adastramekanized:textures/celestial/phobos.png"      // Mars moon
"adastramekanized:textures/celestial/deimos.png"      // Mars moon

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
- **Sun**: Always `TIME_OF_DAY` (east to west arc with day/night cycle)
- **Moons**: Determined by `moves_with_time` boolean
  - `true` = `TIME_OF_DAY` (same direction as sun, east to west arc)
  - `false` = `STATIC` (fixed position)
- **Planets**: Determined by `moves_with_time` boolean
  - `true` = `TIME_OF_DAY` (same direction as sun, east to west arc)
  - `false` = `STATIC` (fixed position)

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
        "texture": "adastramekanized:textures/celestial/earth.png",
        "scale": 2.5,
        "color": 3361970,
        "distance": 0.3,
        "elevation": 0.2,
        "visible": true
      },
      {
        "texture": "adastramekanized:textures/celestial/mars.png",
        "scale": 0.4,
        "color": 16744192,
        "distance": 0.7,
        "elevation": -0.3,
        "visible": true
      }
    ]
  }
}
```

### Authentic Mars Sky (Real Phobos & Deimos Moons)
```json
"rendering": {
  "sky": {
    "sky_color": 15510660,
    "has_stars": true,
    "star_count": 8000,
    "star_brightness": 0.8
  },
  "celestial_bodies": {
    "sun": {
      "texture": "minecraft:textures/environment/sun.png",
      "scale": 0.6,
      "color": 16769205,
      "visible": true
    },
    "moons": [
      {
        "texture": "adastramekanized:textures/celestial/phobos.png",
        "scale": 0.3,
        "color": 11184810,
        "orbit_phase": 0.15,
        "visible": true
      },
      {
        "texture": "adastramekanized:textures/celestial/deimos.png",
        "scale": 0.2,
        "color": 9474192,
        "orbit_phase": 0.25,
        "visible": true
      }
    ],
    "visible_planets": [
      {
        "texture": "adastramekanized:textures/celestial/earth.png",
        "scale": 0.8,
        "color": 3361970,
        "distance": 0.6,
        "elevation": 0.15,
        "visible": true
      }
    ]
  }
}
```

### No Celestial Bodies (Empty Space)
```json
"celestial_bodies": {
  "sun": {
    "visible": false
  },
  "moons": [],
  "visible_planets": []
}
```

## Testing and Validation

### Test Configurations Created

#### 1. Moon Test Configuration (`moon.json`)

**Objective**: Test star systems, Earth view, and tiny sun

**Configuration:**
- **Tiny Sun**: Scale 0.3 (very small compared to normal 0.8-1.0)
- **Spectacular Star Field**: 25,000 stars with 2.0 brightness
- **Multiple "Planets" (Star Systems)**:
  - Large blue "Earth" (lapis block texture, scale 2.5)
  - Small metallic star system (iron block texture, scale 0.4)
  - Medium golden star system (gold block texture, scale 0.6)
- **Black Sky**: No atmosphere, pure space view

**Expected Results:**
- Tiny sun that moves across sky in normal day/night cycle
- Massive star field visible in black sky
- Three different "star systems" appearing as static objects
- Earth-like blue planet dominating the sky view

#### 2. Mars Test Configuration (`mars.json`)

**Objective**: Test close twin moons, no night planets, always bright

**Configuration:**
- **Always Bright**: Ambient light increased to 0.9 (vs normal 0.6)
- **Twin Moons Close Together**:
  - Moon 1: Stone texture, scale 0.4, orbit phase 0.15
  - Moon 2: Cobblestone texture, scale 0.3, orbit phase 0.25
  - Only 0.1 phase difference = very close in sky
- **No Night Planets**: Empty `visible_planets` array
- **Distinct Textures**: Different block textures to make moons easily distinguishable

**Expected Results:**
- Two moons appearing very close together in the sky
- Different textures making them clearly distinguishable
- Moons move opposite to sun (night sky behavior)
- High ambient light keeping Mars bright even at night
- No static planets cluttering the sky

### Test Verification Points

#### Moon Dimension Tests
1. ✅ **Tiny Sun**: Sun should appear much smaller than normal
2. ✅ **Star Density**: Sky should be filled with many bright stars
3. ✅ **Earth Visibility**: Large blue planet should dominate part of sky
4. ✅ **Multiple Star Systems**: Three different colored objects at different distances
5. ✅ **Fixed Positioning**: Planets should not follow camera movement
6. ✅ **Sun Movement**: Only the sun should move with time of day

#### Mars Dimension Tests
1. ✅ **Close Moons**: Two moons should appear near each other
2. ✅ **Moon Distinction**: Different textures should make moons easily distinguishable
3. ✅ **Brightness**: Mars should remain well-lit even during night
4. ✅ **Moon Movement**: Moons should move together across sky (opposite to sun)
5. ✅ **No Static Objects**: No planets should clutter the sky view
6. ✅ **Clean Sky**: Only sun and two moons visible

### How to Test

1. **Start Minecraft with the mod**
2. **Teleport to Moon**: Use command or teleporter
   ```
   /tp @s ~ ~ ~ adastramekanized:moon
   ```
3. **Verify Moon sky features**: Look up and check all Moon test points
4. **Teleport to Mars**:
   ```
   /tp @s ~ ~ ~ adastramekanized:mars
   ```
5. **Verify Mars sky features**: Look up and check all Mars test points
6. **Test time progression**: Use `/time set` commands to test day/night cycle
   ```
   /time set day
   /time set noon
   /time set night
   /time set midnight
   ```

### Expected Behavior Validation

#### Movement Types Working Correctly
- **Sun**: Should move in arc across sky with time progression
- **Moons**: Should move opposite to sun (visible mainly at night)
- **Planets/Star Systems**: Should remain fixed in sky regardless of time

#### Texture System Working
- **No Missing Textures**: All celestial bodies should render with proper textures
- **Fallback System**: No ender dragon textures or error sprites
- **Variety**: Different textures should be clearly visible and distinct

#### Positioning System Working
- **No Camera Following**: Celestial bodies should stay fixed in sky dome
- **Proper Spacing**: Multiple objects should be spread across sky, not overlapping
- **Size Scaling**: Different scale values should produce visibly different sizes

#### Configuration Flexibility Demonstrated
- **Easy Customization**: JSON changes should immediately affect sky rendering
- **Multiple Object Types**: Suns, moons, and planets all working simultaneously
- **Color Tinting**: Different colors should be visible on celestial objects

### Performance Validation

- **High Star Count**: 25,000 stars should render without significant lag
- **Multiple Objects**: Up to 4 celestial bodies should render smoothly
- **Real-time Updates**: Sky should update smoothly during time progression

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

## Tips

1. **Scale Values**:
   - Small objects: 0.3-0.8
   - Normal size: 0.8-1.5
   - Large objects: 1.5-3.0
   - Massive objects: 3.0-5.0

2. **Positioning**:
   - Use `orbit_phase` for moons to prevent overlapping
   - Use `distance` for planets to spread them across the sky

3. **Performance**:
   - Limit total celestial bodies to 10 or fewer per planet
   - Higher star counts (>20000) may impact performance

4. **Testing**:
   - Reload the game after changing configurations
   - Use `/tp @s ~ ~ ~ adastramekanized:planet_name` to test different planets

## Movement Types

- **Sun**: Moves in an arc across the sky following day/night cycle
- **Moons**: Move opposite to the sun (night sky behavior)
- **Planets**: Static positioning (always visible in same location)

This system allows for realistic astronomy simulation or completely fantastical alien skies!

## File Locations

```
src/main/resources/data/adastramekanized/planets/
├── mars.json          # Mars dimension configuration
├── moon.json          # Moon dimension (if exists)
└── custom_world.json  # Your custom dimensions
```

Changes require a full Minecraft restart to take effect.

---

All configurations demonstrate the full range of celestial customization:

1. **Size Range**: From tiny (0.3) to large (2.5) scale values
2. **Texture Variety**: Sun, moon, and multiple block textures
3. **Movement Types**: Static, time-based, and reverse time-based movement
4. **Color Customization**: Different color tints applied successfully
5. **Star Field Control**: High density and brightness star fields
6. **Ambient Lighting**: Custom brightness levels for always-bright worlds

These configurations validate that the celestial body system is working correctly and provides the full range of customization options as designed.