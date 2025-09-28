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
        AdAstraMekanized.MOD_ID, "textures/gui/wireless_power_relay.png"
    );

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
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        // Render energy bar
        WirelessPowerRelayBlockEntity relay = menu.getBlockEntity();
        IEnergyStorage energy = relay.getEnergyStorage();
        
        if (energy.getMaxEnergyStored() > 0) {
            int energyHeight = (int) (52 * ((float) energy.getEnergyStored() / energy.getMaxEnergyStored()));
            graphics.blit(TEXTURE, leftPos + 10, topPos + 17 + (52 - energyHeight), 
                176, 52 - energyHeight, 16, energyHeight);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, (imageWidth - font.width(title)) / 2, 6, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);

        // Render power info
        WirelessPowerRelayBlockEntity relay = menu.getBlockEntity();
        IEnergyStorage energy = relay.getEnergyStorage();
        
        String energyText = String.format("%d / %d FE", 
            energy.getEnergyStored(), energy.getMaxEnergyStored());
        graphics.drawString(font, energyText, 30, 20, 0x404040, false);

        // Render distribution info
        int distributorCount = relay.getLastDistributorCount();
        int powerDistributed = relay.getLastPowerDistributed();
        
        graphics.drawString(font, "Distributors: " + distributorCount, 30, 40, 0x404040, false);
        graphics.drawString(font, "Power/tick: " + powerDistributed + " FE", 30, 50, 0x404040, false);

        // Controller status
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