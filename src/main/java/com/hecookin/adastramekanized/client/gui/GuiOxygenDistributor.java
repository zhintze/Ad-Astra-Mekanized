package com.hecookin.adastramekanized.client.gui;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.menus.OxygenDistributorMenu;
import com.hecookin.adastramekanized.common.networking.ModNetworking;
import com.hecookin.adastramekanized.common.networking.packets.OxygenDistributorButtonPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

/**
 * Ad Astra-style GUI for the oxygen distributor
 */
public class GuiOxygenDistributor extends AbstractContainerScreen<OxygenDistributorMenu> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
        AdAstraMekanized.MOD_ID, "textures/gui/oxygen_distributor.png");
    private static final ResourceLocation SLOT_TEXTURE = ResourceLocation.fromNamespaceAndPath(
        AdAstraMekanized.MOD_ID, "textures/gui/slot_steel.png");
    private static final ResourceLocation CHEMICAL_TEXTURE = ResourceLocation.fromNamespaceAndPath(
        AdAstraMekanized.MOD_ID, "textures/gui/gauges/chemical.png");

    private Button powerButton;
    private Button visibilityButton;
    private boolean oxygenBlockVisibility = false;

    //flips coordinates that come directly from aseprite's inverted y axis
    /*int asepriteYcoordFlipFormula (int height, int yAxis) {
        return height - 1 - yAxis;
    }*/


    public GuiOxygenDistributor(OxygenDistributorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 177;
        this.imageHeight = 244;
        // Ad Astra specific title positioning - no title rendered for this machine
        this.titleLabelX = 15; // Hide title by moving it off screen

        //this.titleLabelY = asepriteYcoordFlipFormula(this.imageHeight,65); // Hide title by moving it off screen
        this.titleLabelY = 66;

        // Ad Astra player inventory label position
        this.inventoryLabelY = 152; // Just above the inventory slots at Y=162


    }

    @Override
    protected void init() {
        super.init();

        // Add power on/off button at position (17, 95)
        this.powerButton = this.addRenderableWidget(
            Button.builder(
                Component.literal(menu.getBlockEntity().isActive() ? "O" : "X"),
                button -> {
                    // Toggle machine power state
                    boolean currentState = menu.getBlockEntity().isActive();
                    boolean newState = !currentState;

                    // Check if we have resources before trying to activate
                    if (newState && (menu.getEnergy() < 400 || menu.getChemicalAmount() == 0)) {
                        // Cannot activate without resources - button stays in OFF state
                        return;
                    }

                    ModNetworking.sendToServer(new OxygenDistributorButtonPacket(
                        menu.getBlockEntity().getBlockPos(),
                        OxygenDistributorButtonPacket.ButtonType.POWER,
                        newState
                    ));
                    button.setMessage(Component.literal(newState ? "O" : "X"));
                })
                .pos(this.leftPos + 17, this.topPos + 75)
                .size(20, 20)
                .build()
        );

        // Add visibility toggle button at position (17, 130)
        this.visibilityButton = this.addRenderableWidget(
            Button.builder(
                Component.literal("Show"),
                button -> {
                    // Toggle oxygen block visibility
                    oxygenBlockVisibility = !oxygenBlockVisibility;
                    ModNetworking.sendToServer(new OxygenDistributorButtonPacket(
                        menu.getBlockEntity().getBlockPos(),
                        OxygenDistributorButtonPacket.ButtonType.VISIBILITY,
                        oxygenBlockVisibility
                    ));
                    button.setMessage(Component.literal(oxygenBlockVisibility ? "H" : "S"));

                    //TODO: Add phantom see-through blocks to indicate oxygenated blocks
                })
                .pos(this.leftPos + 17, this.topPos + 120)
                .size(20, 20)
                .build()
        );
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // Render inventory label with Ad Astra's text color
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x2a262b, false);
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x2a262b, false);

    }



    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        // Render the main GUI texture background exactly like Ad Astra
        int left = (this.width - this.imageWidth) / 2;
        int top = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(TEXTURE, left, top, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);

        // Render slot outlines for inventory slots
        renderSlotBackgrounds(guiGraphics);

        // Render status text at Ad Astra's exact positions (must be before bars)
        renderStatusText(guiGraphics, left, top);

        // Render energy gauge slightly adjusted position for alignment
        // Original is 147, moving left by 1 pixel for better alignment
        renderEnergyGauge(guiGraphics, left + 148, top + 82);

        // Render oxygen gauge slightly adjusted for alignment
        // Moving left by 2 pixels for better visual alignment
        renderOxygenGauge(guiGraphics, left + 51, top + 82);
    }

    private void renderSlotBackgrounds(GuiGraphics graphics) {
        // Render slot texture for each slot in the container
        for (int i = 0; i < this.menu.slots.size(); i++) {
            var slot = this.menu.slots.get(i);
            // Only render for player inventory slots (not machine slots)
            if (slot.container == this.minecraft.player.getInventory()) {
                graphics.blit(SLOT_TEXTURE, this.leftPos + slot.x - 1, this.topPos + slot.y - 1,
                    0, 0, 18, 18, 18, 18);
            }
        }
    }

    private void renderStatusText(GuiGraphics guiGraphics, int leftPos, int topPos) {
        // Render status information at Ad Astra's exact positions
        int textColor = 0x68d975; // Green color like Ad Astra

        // Get actual machine state
        boolean isActive = menu.getBlockEntity().isActive();
        boolean hasResources = menu.getEnergy() >= 400 && menu.getChemicalAmount() > 0;

        // Energy per tick at position (11, 9) - shows actual consumption when active
        // 400 FE every 100 ticks = 4 FE/t average
        float energyConsumption = (isActive && hasResources) ? 4.0f : 0;
        Component energyText = Component.literal(String.format("Energy: %.1f FE/t", energyConsumption));
        guiGraphics.drawString(this.font, energyText, leftPos + 11, topPos + 9, textColor);

        // Get oxygenated block count for both displays
        int oxygenatedBlocks = menu.getBlockEntity().getOxygenatedBlockCount();

        // Oxygen per tick at position (11, 20) - shows dynamic consumption based on oxygenated blocks
        // Shows actual consumption: oxygenated blocks × 0.25 mB per block / 100 ticks
        float oxygenUsage = (isActive && hasResources && oxygenatedBlocks > 0)
            ? (oxygenatedBlocks * 0.25f / 100.0f) // blocks × 0.25 mB / 100 ticks
            : 0;
        Component oxygenText = Component.literal(String.format("Oxygen: %.3f mB/t", oxygenUsage));
        guiGraphics.drawString(this.font, oxygenText, leftPos + 11, topPos + 20, textColor);

        // Blocks distributed at position (11, 31) - show actual oxygenated block count
        Component blocksText = Component.literal(String.format("Blocks: %d/50", oxygenatedBlocks));
        guiGraphics.drawString(this.font, blocksText, leftPos + 11, topPos + 31, textColor);

        // Active/Inactive status at position (11, 42) - green when active, red when inactive
        String statusText = isActive ? "Active" : "Inactive";
        int statusColor = isActive ? 0x68d975 : 0xd95763; // Green when active, red when inactive
        Component statusComponent = Component.literal(statusText);
        guiGraphics.drawString(this.font, statusComponent, leftPos + 11, topPos + 42, statusColor);
    }

    private void renderEnergyGauge(GuiGraphics guiGraphics, int x, int y) {
        long energy = menu.getEnergy();
        long maxEnergy = menu.getMaxEnergy();

        // Ad Astra's actual energy bar dimensions from GuiUtils
        int gaugeWidth = 11;  // ENERGY_BAR_WIDTH
        int gaugeHeight = 45; // ENERGY_BAR_HEIGHT

        // The texture at these positions already has the bar outline
        // We just need to fill the interior
        if (maxEnergy > 0 && energy > 0) {
            float ratio = (float) energy / maxEnergy;
            int scaledHeight = (int) (gaugeHeight * ratio);
            if (scaledHeight > 0) {
                // Draw energy fill from bottom up with 45% alpha red
                // Alpha 45% = 0.45 * 255 = 114.75 ≈ 0x73
                int yStart = y + gaugeHeight - scaledHeight;
                guiGraphics.fill(x, yStart, x + gaugeWidth, y + gaugeHeight, 0x7348de00);
            }
        }
    }

    private void renderOxygenGauge(GuiGraphics guiGraphics, int x, int y) {
        long oxygen = menu.getChemicalAmount();
        long maxOxygen = menu.getChemicalCapacity();

        // Ad Astra's fluid bar dimensions (same as energy)
        int gaugeWidth = 12;
        int gaugeHeight = 46;

        // The texture at these positions already has the bar outline
        if (maxOxygen > 0 && oxygen > 0) {
            float ratio = (float) oxygen / maxOxygen;
            // Constrain the scaled height to not exceed the gauge bounds
            int scaledHeight = Math.min(gaugeHeight, (int) (gaugeHeight * ratio));
            if (scaledHeight > 0) {
                // Hardcoded Mekanism oxygen color from ChemicalConstants
                // OXYGEN("oxygen", 0xFF6CE2FF, ...)
                // Using 80% opacity for a more translucent chemical look
                int oxygenColor = 0xCC6CE2FF;

                int yStart = y + gaugeHeight - scaledHeight;

                // Simple fill approach like energy gauge - don't use the template texture
                guiGraphics.fill(x, yStart, x + gaugeWidth, y + gaugeHeight, oxygenColor);

                // Add subtle highlight on the left edge for depth
                guiGraphics.fill(x, yStart, x + 1, y + gaugeHeight, 0x40FFFFFF);

                // Add subtle shadow on the right edge for depth
                guiGraphics.fill(x + gaugeWidth - 1, yStart, x + gaugeWidth, y + gaugeHeight, 0x40000000);

                // Add a bright edge at the top of the liquid
                guiGraphics.fill(x, yStart, x + gaugeWidth, yStart + 1, 0x60FFFFFF);
            }
        }
    }


    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        // Render tooltips - need to calculate proper positions
        int left = (this.width - this.imageWidth) / 2;
        int top = (this.height - this.imageHeight) / 2;

        // Energy tooltip at adjusted position (148, 82) with bar size 11x45
        if (mouseX >= left + 148 && mouseX < left + 148 + 11 &&
            mouseY >= top + 82 && mouseY < top + 82 + 45) {
            long energy = menu.getEnergy();
            long maxEnergy = menu.getMaxEnergy();
            Component text = Component.literal(String.format("Energy: %,d / %,d FE", energy, maxEnergy));
            guiGraphics.renderTooltip(this.font, text, mouseX, mouseY);
        }

        // Oxygen tooltip at dynamically adjusted position (51, 82) with bar size 11x46
        if (mouseX >= left + 51 && mouseX < left + 51 + 11 &&
            mouseY >= top + 82 && mouseY < top + 82 + 46) {
            long oxygen = menu.getChemicalAmount();
            long maxOxygen = menu.getChemicalCapacity();
            Component text = Component.literal(String.format("Oxygen: %,d / %,d mB", oxygen, maxOxygen));
            guiGraphics.renderTooltip(this.font, text, mouseX, mouseY);
        }
    }
}