package com.hecookin.adastramekanized.common.blocks.properties;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * Represents the 9 parts of a 3x3 sliding door multiblock structure.
 * The BOTTOM part acts as the controller.
 */
public enum SlidingDoorPartProperty implements StringRepresentable {
    TOP_LEFT(-1, 2),
    TOP(0, 2),
    TOP_RIGHT(1, 2),
    LEFT(-1, 1),
    CENTER(0, 1),
    RIGHT(1, 1),
    BOTTOM_LEFT(-1, 0),
    BOTTOM(0, 0),  // Controller block
    BOTTOM_RIGHT(1, 0);

    private final int xOffset;
    private final int yOffset;

    SlidingDoorPartProperty(int xOffset, int yOffset) {
        this.xOffset = xOffset;
        this.yOffset = yOffset;
    }

    public int xOffset() {
        return this.xOffset;
    }

    public int yOffset() {
        return this.yOffset;
    }

    public boolean isController() {
        return this == BOTTOM;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    @Override
    public String toString() {
        return getSerializedName();
    }
}