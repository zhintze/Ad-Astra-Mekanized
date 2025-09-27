# Oxygen Distribution Sphere Calculation

## 10-Block Radius Sphere

For a sphere with radius 10 blocks from the oxygen distributor:

### Mathematical Calculation
- Volume of sphere = (4/3) × π × r³
- Volume = (4/3) × π × 10³
- Volume ≈ 4,189 cubic units

### Minecraft Block Count
In Minecraft, we count discrete blocks. A block at position (x, y, z) is included if:
- √(x² + y² + z²) ≤ 10

### Actual Block Count
For a 10-block radius:
- **Approximate blocks**: ~4,100-4,200 blocks
- This creates a sphere with diameter of 21 blocks (10 radius + center + 10 radius)

### Starting Configuration
For initial testing with 100 blocks total (as currently configured):
- This approximates to a radius of ~2.88 blocks
- Creates a small bubble around the distributor

### Recommended Progression
1. **Initial (current)**: 100 blocks (~3 block radius)
2. **Small**: 500 blocks (~5 block radius)
3. **Medium**: 1,500 blocks (~7 block radius)
4. **Large**: 4,100 blocks (10 block radius)
5. **Maximum**: 10,000+ blocks (~13+ block radius)

### Update Configuration
To achieve a 10-block radius, we need to update:
- `MAX_OXYGEN_BLOCKS` from 100 to 4100
- Adjust oxygen consumption rate accordingly