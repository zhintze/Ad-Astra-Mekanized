package com.hecookin.adastramekanized.client.gui.element;

import com.hecookin.adastramekanized.AdAstraMekanized;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Mekanism-style slot visualization
 */
public class GuiSlot extends AbstractWidget {

    private static final ResourceLocation SLOT_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            AdAstraMekanized.MOD_ID, "textures/gui/slot.png");

    private final int slotType;

    public GuiSlot(int slotType, int x, int y) {
        super(x, y, 18, 18, Component.empty());
        this.slotType = slotType;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        // Draw slot background
        guiGraphics.blit(SLOT_TEXTURE, getX(), getY(), slotType * 18, 0, 18, 18);

        // Draw slot overlay if hovered
        if (isHovered) {
            guiGraphics.fillGradient(getX() + 1, getY() + 1, getX() + 17, getY() + 17,
                0x80FFFFFF, 0x80FFFFFF);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        // Empty implementation - narration not needed for slot visual
    }
}