# Oxygen Distributor Testing Guide

## Test Setup Commands

```bash
# 1. Build the mod
./gradlew clean build

# 2. Launch test client
./gradlew runClient
```

## In-Game Testing Sequence

### Phase 1: Basic Functionality
```
# Place oxygen distributor
# Connect Mekanism oxygen pipes
# Fill with oxygen from creative tank

# Verify GUI displays:
- Power consumption
- Oxygen consumption rate
- Blocks filled (should be 0 initially)
```

### Phase 2: Adjacent Air Block Requirement
```
# Test 1: Surrounded distributor (should NOT work)
- Surround distributor with solid blocks
- Activate distributor
- Check /oxygen debug
- Expected: 0 blocks filled, no oxygen distribution

# Test 2: With adjacent air (should work)
- Break one adjacent block
- Activate distributor
- Check /oxygen debug
- Expected: Oxygen fills up to 100 blocks
```

### Phase 3: Visual Rendering
```
# With distributor active:
1. Open GUI
2. Click "Show/Hide Oxygen" button
3. Verify yellow-greenish translucent blocks appear
4. Click button again to hide
5. Verify blocks disappear but oxygen still works

# Visual settings:
- Color: Yellow-greenish (R:0.7, G:1.0, B:0.3)
- Transparency: 15% (Alpha: 0.15)
```

### Phase 4: Oxygen Protection
```
# Test oxygen damage prevention:
/oxygen debug
- Stand in oxygenated zone
- Verify no oxygen damage taken
- Step outside zone
- Verify oxygen damage resumes
```

### Phase 5: Smart Flood Fill
```
# Test wall blocking:
- Build walls around distributor
- Activate distributor
- Verify oxygen doesn't bleed through walls
- Create doorway
- Verify oxygen flows through opening

# Test special blocks:
- Place stairs, slabs, doors in path
- Verify oxygen flows correctly around/through them
```

## Debug Commands

```
/oxygen status              # Show player oxygen status
/oxygen debug               # Show detailed position info
/oxygen zones               # List all oxygen zones
```

## Expected Console Output

When working correctly:
```
[OxygenManager] Zone 0: Center BlockPos{x=X, y=Y, z=Z}, Size: 100
[OxygenDistributor] Distributed oxygen to 100 blocks
[OxygenZoneRenderer] Rendering enabled: true
[OxygenVisualization] Syncing 100 positions to client
```

## Verification Checklist

- [ ] Distributor accepts oxygen from Mekanism pipes
- [ ] GUI shows correct power/oxygen consumption
- [ ] Requires adjacent air block to start
- [ ] Fills exactly 100 blocks maximum
- [ ] Respects walls (no bleeding through)
- [ ] Visual rendering toggles on/off
- [ ] Yellow-greenish tint visible when enabled
- [ ] Player protected from oxygen damage in zone
- [ ] Network sync works (multiplayer test)