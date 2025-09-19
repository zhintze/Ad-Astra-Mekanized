# Celestial Bodies Configuration Guide

This guide explains how to easily customize celestial bodies (suns, moons, planets) that appear in the sky of your planetary dimensions.

## Configuration Location

Celestial bodies are configured in planet JSON files located in:
`src/main/resources/data/adastramekanized/planets/`

## Basic Structure

```json
{
  "rendering": {
    "sky": {
      "has_stars": true,
      "star_count": 15000,
      "star_brightness": 1.5
    },
    "celestial_bodies": {
      "sun": {
        "texture": "minecraft:textures/environment/sun.png",
        "scale": 0.8,
        "color": 16777215,
        "visible": true
      },
      "moons": [
        {
          "texture": "minecraft:textures/environment/moon_phases.png",
          "scale": 1.0,
          "color": 16777215,
          "orbit_phase": 0.5,
          "visible": true
        }
      ],
      "visible_planets": [
        {
          "texture": "minecraft:textures/block/cobblestone.png",
          "scale": 2.0,
          "color": 6737151,
          "distance": 1.0,
          "visible": true
        }
      ]
    }
  }
}
```

## Available Textures

### Vanilla Minecraft Textures (Always Available)
- **Sun**: `minecraft:textures/environment/sun.png`
- **Moon**: `minecraft:textures/environment/moon_phases.png`
- **Blocks**: `minecraft:textures/block/[block_name].png`
  - Examples: `cobblestone.png`, `stone.png`, `dirt.png`, `sand.png`
- **Items**: `minecraft:textures/item/[item_name].png`

### Custom Textures
- Add your own textures to: `src/main/resources/assets/adastramekanized/textures/celestial/`
- Reference as: `adastramekanized:textures/celestial/[filename].png`

## Celestial Body Types

### 1. Sun Configuration
```json
"sun": {
  "texture": "minecraft:textures/environment/sun.png",
  "scale": 0.8,           // Size (0.1-5.0 recommended)
  "color": 16777215,      // RGB color (white = 16777215)
  "visible": true         // Show/hide the sun
}
```
- **Movement**: Follows time of day (sunrise to sunset arc)
- **Use Cases**: Primary star, multiple suns for binary systems

### 2. Moon Configuration
```json
"moons": [
  {
    "texture": "minecraft:textures/environment/moon_phases.png",
    "scale": 1.0,
    "color": 16777215,
    "orbit_phase": 0.5,   // 0.0-1.0 (orbital position)
    "visible": true
  }
]
```
- **Movement**: Opposite to time of day (moon-like behavior)
- **orbit_phase**: Controls starting position (0.5 = opposite of sun)
- **Use Cases**: Natural satellites, multiple moons

### 3. Planet Configuration
```json
"visible_planets": [
  {
    "texture": "minecraft:textures/block/cobblestone.png",
    "scale": 2.0,
    "color": 6737151,
    "distance": 1.0,      // Affects positioning (0.5-3.0 recommended)
    "visible": true
  }
]
```
- **Movement**: Static (fixed position in sky)
- **distance**: Controls sky positioning spread
- **Use Cases**: Distant planets, space stations, asteroids

## Star Field Configuration

```json
"sky": {
  "has_stars": true,      // Enable/disable stars
  "star_count": 15000,    // Number of stars (1000-30000)
  "star_brightness": 1.5  // Brightness multiplier (0.1-3.0)
}
```

## Color Values

Colors are RGB integers. Common values:
- **White**: `16777215` (0xFFFFFF)
- **Red**: `16711680` (0xFF0000)
- **Blue**: `255` (0x0000FF)
- **Yellow**: `16776960` (0xFFFF00)
- **Orange**: `16753920` (0xFFA500)

## Example Configurations

### Earth-like Sky (from Moon)
```json
"celestial_bodies": {
  "sun": {
    "texture": "minecraft:textures/environment/sun.png",
    "scale": 0.8,
    "color": 16777215,
    "visible": true
  },
  "moons": [],
  "visible_planets": [
    {
      "texture": "minecraft:textures/block/lapis_block.png",
      "scale": 3.0,
      "color": 6737151,
      "distance": 1.0,
      "visible": true
    }
  ]
}
```

### Alien Binary Star System
```json
"celestial_bodies": {
  "sun": {
    "texture": "minecraft:textures/environment/sun.png",
    "scale": 1.2,
    "color": 16753920,  // Orange
    "visible": true
  },
  "moons": [],
  "visible_planets": [
    {
      "texture": "minecraft:textures/environment/sun.png",
      "scale": 0.8,
      "color": 16711680,  // Red second star
      "distance": 2.0,
      "visible": true
    }
  ]
}
```

### Multiple Moons
```json
"celestial_bodies": {
  "sun": {
    "texture": "minecraft:textures/environment/sun.png",
    "scale": 0.8,
    "color": 16777215,
    "visible": true
  },
  "moons": [
    {
      "texture": "minecraft:textures/environment/moon_phases.png",
      "scale": 1.0,
      "color": 16777215,
      "orbit_phase": 0.3,
      "visible": true
    },
    {
      "texture": "minecraft:textures/block/stone.png",
      "scale": 0.6,
      "color": 12632256,  // Gray
      "orbit_phase": 0.8,
      "visible": true
    }
  ],
  "visible_planets": []
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