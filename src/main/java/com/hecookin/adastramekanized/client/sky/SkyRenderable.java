package com.hecookin.adastramekanized.client.sky;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

/**
 * Represents a celestial object that can be rendered in the sky.
 * Based on Ad Astra's SkyRenderable but adapted for our use.
 */
public record SkyRenderable(
    ResourceLocation texture,        // Texture path for the celestial object
    float scale,                    // Size scale of the object
    Vec3 globalRotation,           // Global rotation (x, y, z) in degrees
    Vec3 localRotation,            // Local rotation (x, y, z) in degrees
    MovementType movementType,     // How the object moves
    boolean blend,                 // Whether to enable blending
    int color,                     // Color tint (ARGB format)
    float backLightScale          // Scale of the backlight effect
) {

    /**
     * Convenience constructor with default blending and backlight
     */
    public SkyRenderable(ResourceLocation texture, float scale, Vec3 globalRotation,
                        Vec3 localRotation, MovementType movementType, int color) {
        this(texture, scale, globalRotation, localRotation, movementType, false, color, scale * 2.0f);
    }

    /**
     * Simple constructor for static objects
     */
    public SkyRenderable(ResourceLocation texture, float scale, MovementType movementType, int color) {
        this(texture, scale, Vec3.ZERO, Vec3.ZERO, movementType, false, color, scale * 2.0f);
    }
}