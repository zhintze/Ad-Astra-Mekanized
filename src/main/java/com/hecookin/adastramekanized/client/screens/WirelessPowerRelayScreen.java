package com.hecookin.adastramekanized.client.screens;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.blockentities.machines.WirelessPowerRelayBlockEntity;
import com.hecookin.adastramekanized.common.menus.WirelessPowerRelayMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.energy.IEnergyStorage;

import static com.hecookin.adastramekanized.client.screens.PowerRelayGuiConstants.*;

/**
 * GUI screen for the Wireless Power Relay
 */
public class WirelessPowerRelayScreen extends AbstractContainerScreen<WirelessPowerRelayMenu> {

    private int hoveredSlotIndex = -1;

    public WirelessPowerRelayScreen(WirelessPowerRelayMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;
        this.inventoryLabelX = INVENTORY_LABEL_X;
        this.inventoryLabelY = INVENTORY_LABEL_Y;
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Track hovered slot for overlay rendering
        hoveredSlotIndex = -1;
        for (int i = 0; i < menu.slots.size(); i++) {
            Slot slot = menu.slots.get(i);
            if (isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY)) {
                hoveredSlotIndex = i;
                break;
            }
        }

        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);

        // Render energy bar tooltip
        if (isHovering(ENERGY_BAR_X, ENERGY_BAR_Y, ENERGY_BAR_WIDTH, ENERGY_BAR_HEIGHT, mouseX, mouseY)) {
            int energy = menu.getEnergy();
            int maxEnergy = menu.getMaxEnergy();
            graphics.renderTooltip(font,
                Component.literal(String.format("%,d / %,d FE", energy, maxEnergy)),
                mouseX, mouseY);
        }

        // Render slot overlays after everything else
        renderSlotOverlays(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        // Draw main GUI background
        graphics.blit(TEXTURE, leftPos, topPos, BACKGROUND_U, BACKGROUND_V, imageWidth, imageHeight);

        // Draw display panel area (if it has a special background in the texture)
        renderDisplayPanel(graphics);

        // Draw energy bar
        renderEnergyBar(graphics);
    }

    private void renderDisplayPanel(GuiGraphics graphics) {
        // The display panel is already part of the main background texture
        // This method is here for future enhancements like animated backgrounds
    }

    private void renderEnergyBar(GuiGraphics graphics) {
        int energy = menu.getEnergy();
        int maxEnergy = menu.getMaxEnergy();

        // Draw background (empty bar)
        graphics.blit(TEXTURE,
            leftPos + ENERGY_BAR_X, topPos + ENERGY_BAR_Y,
            ENERGY_EMPTY_U, ENERGY_EMPTY_V,
            ENERGY_BAR_WIDTH, ENERGY_BAR_HEIGHT);

        if (maxEnergy > 0 && energy > 0) {
            int barHeight = calculateEnergyBarHeight(energy, maxEnergy);

            // Draw filled portion from bottom up (Mekanism style)
            if (barHeight > 0) {
                int yOffset = ENERGY_BAR_HEIGHT - barHeight;
                graphics.blit(TEXTURE,
                    leftPos + ENERGY_BAR_X, topPos + ENERGY_BAR_Y + yOffset,
                    ENERGY_FILLED_U, ENERGY_FILLED_V + yOffset,
                    ENERGY_BAR_WIDTH, barHeight);
            }
        }
    }

    private void renderSlotOverlays(GuiGraphics graphics, int mouseX, int mouseY) {
        // Render hover overlay on the currently hovered slot
        if (hoveredSlotIndex >= 0 && hoveredSlotIndex < menu.slots.size()) {
            Slot slot = menu.slots.get(hoveredSlotIndex);
            if (!slot.hasItem()) {  // Only show overlay if slot is empty
                graphics.blit(TEXTURE,
                    leftPos + slot.x - 1, topPos + slot.y - 1,
                    SLOT_HOVER_U, SLOT_HOVER_V,
                    SLOT_HOVER_SIZE, SLOT_HOVER_SIZE);
            }
        }

        // Render disabled overlay for slots that should appear disabled
        // For now, we'll only disable empty controller slot when no controller is present
        WirelessPowerRelayBlockEntity relay = menu.getBlockEntity();
        if (relay != null && relay.getControllerSlot().getItem(0).isEmpty()) {
            // Find the controller slot (slot 0 in the menu)
            if (!menu.slots.isEmpty()) {
                Slot controllerSlot = menu.slots.get(0);
                if (controllerSlot.x == CONTROLLER_SLOT_X && controllerSlot.y == CONTROLLER_SLOT_Y) {
                    // Don't render disabled if it's being hovered
                    if (hoveredSlotIndex != 0) {
                        graphics.blit(TEXTURE,
                            leftPos + controllerSlot.x - 1, topPos + controllerSlot.y - 1,
                            SLOT_DISABLED_U, SLOT_DISABLED_V,
                            SLOT_DISABLED_SIZE, SLOT_DISABLED_SIZE);
                    }
                }
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // Title - centered
        int titleX = getCenteredTextX(imageWidth, font.width(title));
        graphics.drawString(font, title, titleX, TITLE_Y, TEXT_COLOR_TITLE, false);

        // Player inventory label
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, TEXT_COLOR_DEFAULT, false);

        // Render power info using synced data
        int energy = menu.getEnergy();
        int maxEnergy = menu.getMaxEnergy();

        String energyText = String.format("Energy: %,d / %,d FE", energy, maxEnergy);
        graphics.drawString(font, energyText, ENERGY_TEXT_X, ENERGY_TEXT_Y, TEXT_COLOR_DEFAULT, false);

        // Render distribution info using synced data
        int distributorCount = menu.getLastDistributorCount();
        int powerDistributed = menu.getLastPowerDistributed();

        String distributorText = String.format("Distributors: %d", distributorCount);
        graphics.drawString(font, distributorText, DISTRIBUTOR_TEXT_X, DISTRIBUTOR_TEXT_Y, TEXT_COLOR_DEFAULT, false);

        String powerText = String.format("Power/tick: %,d FE", powerDistributed);
        graphics.drawString(font, powerText, POWER_TEXT_X, POWER_TEXT_Y,
            powerDistributed > 0 ? TEXT_COLOR_ENERGY : TEXT_COLOR_DEFAULT, false);

        // Controller status
        WirelessPowerRelayBlockEntity relay = menu.getBlockEntity();
        boolean hasController = relay != null && !relay.getControllerSlot().getItem(0).isEmpty();
        String controllerStatus = hasController ? "Controller: Active" : "Controller: Empty";
        int statusColor = hasController ? TEXT_COLOR_SUCCESS : TEXT_COLOR_ERROR;
        graphics.drawString(font, controllerStatus, CONTROLLER_STATUS_X, CONTROLLER_STATUS_Y, statusColor, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
    }
}