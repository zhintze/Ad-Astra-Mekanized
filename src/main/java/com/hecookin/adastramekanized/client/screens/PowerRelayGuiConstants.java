package com.hecookin.adastramekanized.client.screens;

import net.minecraft.resources.ResourceLocation;
import com.hecookin.adastramekanized.AdAstraMekanized;

/**
 * GUI coordinate constants for the Wireless Power Relay
 * Auto-generated from power_relay_gui_refined.py
 */
public class PowerRelayGuiConstants {

    // Texture information
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
        AdAstraMekanized.MOD_ID, "textures/gui/power_relay.png"
    );
    public static final int TEXTURE_WIDTH = 256;
    public static final int TEXTURE_HEIGHT = 256;

    // GUI viewport
    public static final int GUI_WIDTH = 176;
    public static final int GUI_HEIGHT = 166;

    // Background texture position
    public static final int BACKGROUND_U = 0;
    public static final int BACKGROUND_V = 0;

    // Controller slot
    public static final int CONTROLLER_SLOT_X = 80;
    public static final int CONTROLLER_SLOT_Y = 35;

    // Energy bar
    public static final int ENERGY_BAR_X = 165;
    public static final int ENERGY_BAR_Y = 17;
    public static final int ENERGY_BAR_WIDTH = 4;
    public static final int ENERGY_BAR_HEIGHT = 52;

    // Energy bar texture coordinates
    public static final int ENERGY_EMPTY_U = 176;
    public static final int ENERGY_EMPTY_V = 0;
    public static final int ENERGY_FILLED_U = 180;
    public static final int ENERGY_FILLED_V = 0;

    // Display panel
    public static final int DISPLAY_PANEL_X = 30;
    public static final int DISPLAY_PANEL_Y = 20;
    public static final int DISPLAY_PANEL_WIDTH = 120;
    public static final int DISPLAY_PANEL_HEIGHT = 50;

    // Text positions (relative to GUI, not texture)
    public static final int TITLE_Y = 6;
    public static final int ENERGY_TEXT_X = 35;
    public static final int ENERGY_TEXT_Y = 25;
    public static final int DISTRIBUTOR_TEXT_X = 35;
    public static final int DISTRIBUTOR_TEXT_Y = 40;
    public static final int POWER_TEXT_X = 35;
    public static final int POWER_TEXT_Y = 50;
    public static final int CONTROLLER_STATUS_X = 35;
    public static final int CONTROLLER_STATUS_Y = 60;

    // Player inventory (CORRECTED)
    public static final int PLAYER_INVENTORY_X = 8;
    public static final int PLAYER_INVENTORY_Y = 84;
    public static final int PLAYER_HOTBAR_Y = 142;
    public static final int INVENTORY_LABEL_X = 8;
    public static final int INVENTORY_LABEL_Y = 72;  // Corrected from 74

    // Slot overlays texture coordinates
    public static final int SLOT_HOVER_U = 176;
    public static final int SLOT_HOVER_V = 60;
    public static final int SLOT_HOVER_SIZE = 18;
    public static final int SLOT_DISABLED_U = 194;
    public static final int SLOT_DISABLED_V = 60;
    public static final int SLOT_DISABLED_SIZE = 18;

    // Button states (if used)
    public static final int BUTTON_NORMAL_U = 176;
    public static final int BUTTON_NORMAL_V = 80;
    public static final int BUTTON_HOVER_U = 194;
    public static final int BUTTON_HOVER_V = 80;
    public static final int BUTTON_PRESSED_U = 212;
    public static final int BUTTON_PRESSED_V = 80;

    // Colors
    public static final int TEXT_COLOR_DEFAULT = 0x404040;
    public static final int TEXT_COLOR_TITLE = 0x404040;
    public static final int TEXT_COLOR_ENERGY = 0x00D000;  // Mekanism green
    public static final int TEXT_COLOR_ERROR = 0xAA0000;
    public static final int TEXT_COLOR_SUCCESS = 0x00AA00;

    // Helper methods
    public static int getCenteredTextX(int containerWidth, int textWidth) {
        return (containerWidth - textWidth) / 2;
    }

    public static int calculateEnergyBarHeight(int energy, int maxEnergy) {
        if (maxEnergy <= 0) return 0;
        return (int) (ENERGY_BAR_HEIGHT * ((float) energy / maxEnergy));
    }
}
