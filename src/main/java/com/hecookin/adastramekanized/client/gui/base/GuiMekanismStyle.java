package com.hecookin.adastramekanized.client.gui.base;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

/**
 * Base GUI class following Mekanism's styling
 */
public abstract class GuiMekanismStyle<CONTAINER extends AbstractContainerMenu> extends AbstractContainerScreen<CONTAINER> {

    public static final ResourceLocation BASE_BACKGROUND = ResourceLocation.fromNamespaceAndPath(
            AdAstraMekanized.MOD_ID, "textures/gui/mekanism_style/base.png");

    public static final ResourceLocation SHADOW = ResourceLocation.fromNamespaceAndPath(
            AdAstraMekanized.MOD_ID, "textures/gui/mekanism_style/shadow.png");

    public static final ResourceLocation BLUR = ResourceLocation.fromNamespaceAndPath(
            AdAstraMekanized.MOD_ID, "textures/gui/mekanism_style/blur.png");

    // Mekanism color palette
    public static final int TEXT_COLOR = 0x404040;
    public static final int TEXT_COLOR_LIGHT = 0xFFFFFF;
    public static final int ENERGY_COLOR = 0x00D000;
    public static final int OXYGEN_COLOR = 0x3CF3FC;

    protected int relX;
    protected int relY;

    public GuiMekanismStyle(CONTAINER container, Inventory playerInventory, Component title) {
        super(container, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        this.relX = (this.width - this.imageWidth) / 2;
        this.relY = (this.height - this.imageHeight) / 2;

        this.inventoryLabelY = this.imageHeight - 94;
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
        this.titleLabelY = 6;

        // Initialize GUI elements
        addGuiElements();
    }

    /**
     * Override this to add GUI elements like gauges, progress bars, etc.
     */
    protected abstract void addGuiElements();

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        // Render main background
        guiGraphics.blit(BASE_BACKGROUND, relX, relY, 0, 0, imageWidth, imageHeight);

        // Render additional background elements
        renderBackgroundElements(guiGraphics, partialTicks, mouseX, mouseY);
    }

    /**
     * Override to render additional background elements
     */
    protected void renderBackgroundElements(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        // Default implementation - override in subclasses
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Render title with Mekanism style
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, TEXT_COLOR, false);
        // Don't render inventory label by default (Mekanism style)
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTicks);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        // Render additional overlays
        renderOverlays(guiGraphics, mouseX, mouseY, partialTicks);
    }

    /**
     * Override to render overlays like tooltips for custom elements
     */
    protected void renderOverlays(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        // Default implementation - override in subclasses
    }

    protected boolean isHovering(int x, int y, int width, int height, int mouseX, int mouseY) {
        mouseX -= relX;
        mouseY -= relY;
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}