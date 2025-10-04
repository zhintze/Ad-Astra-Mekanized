package com.hecookin.adastramekanized.common.blocks.properties;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

/**
 * Defines the 9 parts of a 3x3 launch pad multiblock
 */
public enum LaunchPadPartProperty implements StringRepresentable {
    CENTER(0, 0, true),
    NORTH(-1, 0, false),
    SOUTH(1, 0, false),
    EAST(0, 1, false),
    WEST(0, -1, false),
    NORTH_EAST(-1, 1, false),
    NORTH_WEST(-1, -1, false),
    SOUTH_EAST(1, 1, false),
    SOUTH_WEST(1, -1, false);

    private final int xOffset;
    private final int yOffset;
    private final boolean isController;

    LaunchPadPartProperty(int xOffset, int yOffset, boolean isController) {
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.isController = isController;
    }

    public int xOffset() {
        return xOffset;
    }

    public int yOffset() {
        return yOffset;
    }

    public boolean isController() {
        return isController;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name().toLowerCase();
    }
}
