package com.hecookin.adastramekanized.client.screens;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.menus.OxygenDistributorMenu;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

public class OxygenDistributorScreen extends AbstractContainerScreen<OxygenDistributorMenu> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            AdAstraMekanized.MOD_ID, "textures/gui/oxygen_distributor.png");

    // GUI dimensions
    private static final int ENERGY_BAR_X = 165;
    private static final int ENERGY_BAR_Y = 17;
    private static final int ENERGY_BAR_WIDTH = 4;
    private static final int ENERGY_BAR_HEIGHT = 52;

    private static final int OXYGEN_BAR_X = 7;
    private static final int OXYGEN_BAR_Y = 17;
    private static final int OXYGEN_BAR_WIDTH = 4;
    private static final int OXYGEN_BAR_HEIGHT = 52;

    public OxygenDistributorScreen(OxygenDistributorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // Draw main GUI background
        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        // Draw energy bar (Mekanism style - vertical green bar)
        renderEnergyBar(guiGraphics, x, y);

        // Draw oxygen bar (Mekanism style - vertical blue bar)
        renderOxygenBar(guiGraphics, x, y);
    }

    private void renderEnergyBar(GuiGraphics guiGraphics, int x, int y) {
        long energy = menu.getEnergy();
        long maxEnergy = menu.getMaxEnergy();

        // Debug: Always show energy bar background even if empty
        // Draw background (empty bar)
        guiGraphics.blit(TEXTURE,
            x + ENERGY_BAR_X, y + ENERGY_BAR_Y,
            176, 0,  // Texture coordinates for empty bar
            ENERGY_BAR_WIDTH, ENERGY_BAR_HEIGHT);

        if (maxEnergy > 0 && energy > 0) {
            int barHeight = (int) (ENERGY_BAR_HEIGHT * ((float) energy / maxEnergy));

            // Draw filled portion from bottom up (Mekanism style)
            if (barHeight > 0) {
                int yOffset = ENERGY_BAR_HEIGHT - barHeight;
                guiGraphics.blit(TEXTURE,
                    x + ENERGY_BAR_X, y + ENERGY_BAR_Y + yOffset,
                    180, yOffset,  // Texture coordinates for filled bar
                    ENERGY_BAR_WIDTH, barHeight);
            }
        }
    }

    private void renderOxygenBar(GuiGraphics guiGraphics, int x, int y) {
        long oxygen = menu.getChemicalAmount();
        long maxOxygen = menu.getChemicalCapacity();

        // Always draw background (empty bar)
        guiGraphics.blit(TEXTURE,
            x + OXYGEN_BAR_X, y + OXYGEN_BAR_Y,
            184, 0,  // Texture coordinates for empty oxygen bar
            OXYGEN_BAR_WIDTH, OXYGEN_BAR_HEIGHT);

        if (maxOxygen > 0 && oxygen > 0) {
            int barHeight = (int) (OXYGEN_BAR_HEIGHT * ((float) oxygen / maxOxygen));

            // Draw filled portion from bottom up
            if (barHeight > 0) {
                int yOffset = OXYGEN_BAR_HEIGHT - barHeight;
                guiGraphics.blit(TEXTURE,
                    x + OXYGEN_BAR_X, y + OXYGEN_BAR_Y + yOffset,
                    188, yOffset,  // Texture coordinates for filled oxygen bar
                    OXYGEN_BAR_WIDTH, barHeight);
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        // Render tooltips for bars
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // Energy bar tooltip
        if (isHovering(ENERGY_BAR_X, ENERGY_BAR_Y, ENERGY_BAR_WIDTH, ENERGY_BAR_HEIGHT, mouseX, mouseY)) {
            long energy = menu.getEnergy();
            long maxEnergy = menu.getMaxEnergy();
            Component energyText = Component.literal(String.format("%,d / %,d FE", energy, maxEnergy));
            guiGraphics.renderTooltip(this.font, energyText, mouseX, mouseY);
        }

        // Oxygen bar tooltip
        if (isHovering(OXYGEN_BAR_X, OXYGEN_BAR_Y, OXYGEN_BAR_WIDTH, OXYGEN_BAR_HEIGHT, mouseX, mouseY)) {
            long oxygen = menu.getChemicalAmount();
            long maxOxygen = menu.getChemicalCapacity();
            Component oxygenText = Component.literal(String.format("%,d / %,d mB", oxygen, maxOxygen));
            guiGraphics.renderTooltip(this.font, oxygenText, mouseX, mouseY);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 4210752, false);
    }
}