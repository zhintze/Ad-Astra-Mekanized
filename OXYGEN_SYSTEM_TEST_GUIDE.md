# Oxygen Distribution System - Test Guide

## Implementation Summary

The oxygen distribution system has been completely overhauled with the following improvements:

### Key Features Implemented

1. **Dynamic Radius Expansion**
   - Starts at radius 3 blocks
   - Expands by 1 block every 10 ticks (0.5 seconds)
   - No maximum radius limit (only max blocks limit of 4,100)

2. **Blocks Owned by Others = Walls**
   - Other distributors' oxygen blocks are treated as solid walls
   - Prevents pathfinding through claimed areas
   - Ensures distributors work around each other naturally

3. **Ring-Based Priority Claiming**
   - Claims blocks closest to distributor first
   - Expands outward ring by ring
   - More realistic oxygen flow pattern

4. **100-Tick Pathfinding Cache**
   - Caches valid paths for 100 ticks (5 seconds)
   - Significantly improves performance
   - Automatically clears and refreshes

5. **GlobalOxygenManager Integration**
   - Atomic block claiming prevents race conditions
   - First-come-first-served ownership model
   - Immediate release when distributor deactivates

## Testing Instructions

### Setup Test Environment

1. **Place Multiple Distributors**
   ```
   /give @p adastramekanized:oxygen_distributor 4
   ```
   - Place 2-3 distributors about 10-15 blocks apart
   - Ensure they have overlapping potential zones

2. **Provide Resources**
   ```
   # Give yourself Mekanism oxygen tanks
   /give @p mekanism:creative_chemical_tank{mekData:{chemical:{amount:8000,id:"mekanism:oxygen"}}} 1

   # Give yourself energy cables
   /give @p mekanism:ultimate_universal_cable 64
   ```

3. **Enable Visualization**
   - Open each distributor's GUI
   - Click the "Show Oxygen" button to enable visualization
   - Choose different colors for each distributor

### Test Cases

#### Test 1: Dynamic Expansion
1. Place a single distributor
2. Provide energy and oxygen
3. Watch it expand from radius 3 outward
4. Use `/oxygen debug` to see current radius
5. **Expected**: Radius increases every 10 ticks (0.5 seconds)

#### Test 2: Boundary Respect
1. Place two distributors 10 blocks apart
2. Activate both with resources
3. Watch them expand toward each other
4. **Expected**: They form a clear boundary where they meet
5. **Expected**: Neither can claim the other's blocks

#### Test 3: Wall-Like Behavior
1. Place distributor A and let it expand
2. Place distributor B behind A's oxygen zone
3. Activate distributor B
4. **Expected**: B treats A's blocks as walls
5. **Expected**: B expands around A's zone, not through it

#### Test 4: Release and Reclaim
1. Set up two active distributors with boundaries
2. Turn OFF distributor A (via GUI)
3. **Expected**: A releases all blocks immediately
4. **Expected**: B expands into freed space within 1-2 seconds

#### Test 5: Performance Test
1. Place 4+ distributors in a large room
2. Activate all simultaneously
3. Monitor server TPS with `/forge tps`
4. **Expected**: Minimal impact on TPS
5. **Expected**: Smooth expansion without lag

### Debug Commands

```bash
# Check oxygen distributor status
/oxygen debug

# Shows:
# - Current radius
# - Oxygenated blocks count
# - Energy and oxygen levels
# - Dynamic expansion status
# - Total blocks in dimension

# Check planet oxygen status
/planet info <planet_name>
```

### Visual Indicators

- **Green blocks**: Oxygenated air (your chosen color)
- **No overlap**: Each distributor maintains exclusive zones
- **Smooth expansion**: Watch the radius grow gradually
- **Clear boundaries**: Distinct edges where distributors meet

## Expected Behavior

### Single Distributor
1. Starts with 3-block radius sphere
2. Expands by 1 radius every 0.5 seconds
3. Forms roughly spherical shape
4. Stops at 4,100 blocks or when out of oxygen

### Multiple Distributors
1. Each claims its own territory
2. Natural boundaries form where they meet
3. No fighting over blocks
4. Dead zones only where truly unreachable

### Resource Management
- **Energy**: 400 FE per distribution cycle
- **Oxygen**: 0.05 mB per block
- **Updates**: Every 20 ticks (1 second)
- **Visualization**: Updates every second

## Troubleshooting

### Issue: Distributors not expanding
- Check energy supply (needs 400+ FE)
- Check oxygen supply (needs oxygen in tank)
- Ensure not manually disabled (check GUI state)

### Issue: Overlap between distributors
- Should NOT happen with new system
- If it occurs, break and replace distributors
- Report as bug with coordinates

### Issue: Performance problems
- Check total oxygen blocks with debug command
- Reduce number of active distributors
- Check for other lag sources

## Configuration (Future)

Currently using hardcoded values:
- Initial radius: 3
- Expansion rate: +1 per 10 ticks
- Max blocks: 4,100
- Cache duration: 100 ticks

These will be made configurable in a future update.

## Technical Details

### File Changes
1. **ImprovedOxygenFloodFill.java**: New flood fill with GlobalOxygenManager checks
2. **ImprovedOxygenDistributor.java**: Unified distributor with all improvements
3. **OxygenDistributorBlockEntity.java**: Now extends ImprovedOxygenDistributor

### Algorithm
1. Check GlobalOxygenManager for block ownership
2. Treat owned blocks as impassable
3. Use priority queue for ring-based expansion
4. Cache pathfinding results for 100 ticks
5. Atomic claiming prevents conflicts