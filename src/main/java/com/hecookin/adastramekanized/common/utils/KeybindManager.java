package com.hecookin.adastramekanized.common.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Manages keybind states for space suit and jet suit flight.
 * Client-side only for checking input states.
 */
public class KeybindManager {

    // Track if flight is enabled (could be toggled with a key later)
    private static boolean flightEnabled = true;

    /**
     * Check if suit flight is enabled (can be toggled)
     */
    public static boolean suitFlightEnabled(Player player) {
        return flightEnabled && !player.isSpectator();
    }

    /**
     * Check if jump key is being held down
     */
    @OnlyIn(Dist.CLIENT)
    public static boolean jumpDown(Player player) {
        if (player.level().isClientSide()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == player) {
                return mc.options.keyJump.isDown();
            }
        }
        // For server-side, we'd need to track this via network packets
        // For now, return false on server
        return false;
    }

    /**
     * Check if sprint key is being held down
     */
    @OnlyIn(Dist.CLIENT)
    public static boolean sprintDown(Player player) {
        if (player.level().isClientSide()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == player) {
                return mc.options.keySprint.isDown();
            }
        }
        // For server-side, we'd need to track this via network packets
        // For now, return false on server
        return false;
    }

    /**
     * Toggle flight enabled state
     */
    public static void toggleFlight() {
        flightEnabled = !flightEnabled;
    }

    /**
     * Set flight enabled state
     */
    public static void setFlightEnabled(boolean enabled) {
        flightEnabled = enabled;
    }

    /**
     * Get current flight enabled state
     */
    public static boolean isFlightEnabled() {
        return flightEnabled;
    }
}