# Potential Future Features for Ad Astra Mekanized

## Oxygen Network Monitor (Wall-mounted Display) - INCOMPLETE
**Status**: Partially implemented, removed from creative tab/JEI
**Date Removed**: 2025-09-28

### Description
The Oxygen Network Monitor was intended to be a thin, wall-mounted display block (similar to RFTools screens) that would show real-time status of linked oxygen distributors when paired with an Oxygen Network Controller.

### Implementation Status
- ✅ Block and BlockEntity classes created
- ✅ Wall-mounting mechanics implemented (thin hitbox like RFTools screens)
- ✅ Basic pairing with controller via sneak+right-click
- ❌ GUI/Screen rendering not implemented
- ❌ Real-time data synchronization not complete
- ❌ Visual display of distributor statuses not implemented

### Files Involved
- `/src/main/java/com/hecookin/adastramekanized/common/blocks/OxygenNetworkMonitorBlock.java`
- `/src/main/java/com/hecookin/adastramekanized/common/blockentities/OxygenNetworkMonitorBlockEntity.java`
- `/src/main/java/com/hecookin/adastramekanized/client/screens/OxygenNetworkMonitorScreen.java` (partial)
- `/src/main/java/com/hecookin/adastramekanized/common/menus/OxygenNetworkMonitorMenu.java` (partial)

### Reason for Removal
The feature was incomplete and would require significant additional work:
1. Complex GUI rendering for displaying multiple distributor statuses
2. Network packet system for real-time updates
3. Screen refresh logic and performance optimization
4. Visual design for the display interface

### Future Implementation Notes
If reimplementing this feature:
1. Consider using a simpler display format (text-based rather than graphical)
2. Implement efficient network sync to avoid performance issues
3. Add configuration for update frequency
4. Consider integration with other monitoring mods (like RFTools screens)