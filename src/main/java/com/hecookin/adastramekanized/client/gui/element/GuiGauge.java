package com.hecookin.adastramekanized.client.gui.element;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Mekanism-style gauge element for displaying resource levels
 */
public abstract class GuiGauge extends AbstractWidget {

    public enum GaugeType {
        STANDARD(18, 60, 176, 0, 18, 60),
        SMALL(18, 30, 176, 60, 18, 30),
        MEDIUM(18, 42, 176, 90, 18, 42),
        WIDE(22, 60, 194, 0, 22, 60);

        public final int width;
        public final int height;
        public final int barX;
        public final int barY;
        public final int overlayWidth;
        public final int overlayHeight;

        GaugeType(int width, int height, int barX, int barY, int overlayWidth, int overlayHeight) {
            this.width = width;
            this.height = height;
            this.barX = barX;
            this.barY = barY;
            this.overlayWidth = overlayWidth;
            this.overlayHeight = overlayHeight;
        }
    }

    protected final ResourceLocation texture;
    protected final GaugeType type;
    protected final int relX;
    protected final int relY;

    // For tooltip
    protected Component tooltip;

    public GuiGauge(GaugeType type, int x, int y, ResourceLocation texture) {
        super(x, y, type.width, type.height, Component.empty());
        this.type = type;
        this.relX = x;
        this.relY = y;
        this.texture = texture;
    }

    /**
     * Get the scaled level for rendering (0 to height-2)
     */
    public abstract int getScaledLevel();

    /**
     * Get the color for the gauge content
     */
    public abstract int getColor();

    /**
     * Get tooltip text
     */
    public abstract Component getTooltipText();

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        RenderSystem.setShaderTexture(0, texture);

        // Draw gauge background
        guiGraphics.blit(texture, getX(), getY(), 0, 0, type.width, type.height);

        // Calculate fill level
        int scaledLevel = getScaledLevel();
        if (scaledLevel > 0) {
            // Draw from bottom up
            int yOffset = type.height - 2 - scaledLevel;
            guiGraphics.blit(texture,
                getX() + 1, getY() + 1 + yOffset,
                type.barX + 1, type.barY + 1 + yOffset,
                type.width - 2, scaledLevel);

            // Apply color overlay
            RenderSystem.setShaderColor(
                ((getColor() >> 16) & 0xFF) / 255.0f,
                ((getColor() >> 8) & 0xFF) / 255.0f,
                (getColor() & 0xFF) / 255.0f,
                1.0f);

            // Render colored overlay
            guiGraphics.blit(texture,
                getX() + 1, getY() + 1 + yOffset,
                type.barX + type.width + 1, type.barY + 1 + yOffset,
                type.width - 2, scaledLevel);

            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        }

        // Draw gauge overlay/frame
        guiGraphics.blit(texture,
            getX(), getY(),
            type.barX + type.width * 2, type.barY,
            type.width, type.height);

        // Store tooltip for rendering
        if (isHovered) {
            this.tooltip = getTooltipText();
        }
    }

    public Component getTooltipComponent() {
        return tooltip;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        // Empty implementation - narration not needed for gauge
    }
}