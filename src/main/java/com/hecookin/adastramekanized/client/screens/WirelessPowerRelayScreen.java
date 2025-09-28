package com.hecookin.adastramekanized.client.screens;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.blockentities.machines.WirelessPowerRelayBlockEntity;
import com.hecookin.adastramekanized.common.menus.WirelessPowerRelayMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.energy.IEnergyStorage;

/**
 * GUI screen for the Wireless Power Relay
 */
public class WirelessPowerRelayScreen extends AbstractContainerScreen<WirelessPowerRelayMenu> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
        AdAstraMekanized.MOD_ID, "textures/gui/oxygen_distributor.png"
    );

    // Use oxygen distributor's energy bar position
    private static final int ENERGY_BAR_X = 165;
    private static final int ENERGY_BAR_Y = 17;
    private static final int ENERGY_BAR_WIDTH = 4;
    private static final int ENERGY_BAR_HEIGHT = 52;

    public WirelessPowerRelayScreen(WirelessPowerRelayMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
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
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        // Draw main GUI background
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        // Draw energy bar (same style as oxygen distributor)
        renderEnergyBar(graphics);
    }

    private void renderEnergyBar(GuiGraphics graphics) {
        int energy = menu.getEnergy();
        int maxEnergy = menu.getMaxEnergy();

        // Draw background (empty bar)
        graphics.blit(TEXTURE,
            leftPos + ENERGY_BAR_X, topPos + ENERGY_BAR_Y,
            176, 0,  // Texture coordinates for empty bar
            ENERGY_BAR_WIDTH, ENERGY_BAR_HEIGHT);

        if (maxEnergy > 0 && energy > 0) {
            int barHeight = (int) (ENERGY_BAR_HEIGHT * ((float) energy / maxEnergy));

            // Draw filled portion from bottom up (Mekanism style)
            if (barHeight > 0) {
                int yOffset = ENERGY_BAR_HEIGHT - barHeight;
                graphics.blit(TEXTURE,
                    leftPos + ENERGY_BAR_X, topPos + ENERGY_BAR_Y + yOffset,
                    180, yOffset,  // Texture coordinates for filled bar
                    ENERGY_BAR_WIDTH, barHeight);
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, (imageWidth - font.width(title)) / 2, 6, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);

        // Render power info using synced data
        int energy = menu.getEnergy();
        int maxEnergy = menu.getMaxEnergy();

        String energyText = String.format("%d / %d FE",
            energy, maxEnergy);
        graphics.drawString(font, energyText, 30, 20, 0x404040, false);

        // Render distribution info using synced data
        int distributorCount = menu.getLastDistributorCount();
        int powerDistributed = menu.getLastPowerDistributed();

        graphics.drawString(font, "Distributors: " + distributorCount, 30, 40, 0x404040, false);
        graphics.drawString(font, "Power/tick: " + powerDistributed + " FE", 30, 50, 0x404040, false);

        // Controller status - still need to access block entity for slot
        WirelessPowerRelayBlockEntity relay = menu.getBlockEntity();
        boolean hasController = !relay.getControllerSlot().getItem(0).isEmpty();
        String controllerStatus = hasController ? "Controller: Inserted" : "Controller: Empty";
        int statusColor = hasController ? 0x00AA00 : 0xAA0000;
        graphics.drawString(font, controllerStatus, 30, 60, statusColor, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
    }
}