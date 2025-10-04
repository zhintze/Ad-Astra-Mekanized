package com.hecookin.adastramekanized.client.screens;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.menus.NasaWorkbenchMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class NasaWorkbenchScreen extends AbstractContainerScreen<NasaWorkbenchMenu> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "textures/gui/container/nasa_workbench.png");
    private static final ResourceLocation SLOT_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "textures/gui/slot_steel.png");

    public NasaWorkbenchScreen(NasaWorkbenchMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 177;
        this.imageHeight = 244;
        this.titleLabelY = 6;
        this.inventoryLabelY = 152;
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        graphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);

        // Render slot backgrounds
        renderSlotBackgrounds(graphics);
    }

    private void renderSlotBackgrounds(GuiGraphics graphics) {
        // Render slot texture for each slot in the container
        for (int i = 0; i < this.menu.slots.size(); i++) {
            var slot = this.menu.slots.get(i);
            // Render for all slots (both workbench and player inventory)
            graphics.blit(SLOT_TEXTURE, this.leftPos + slot.x - 1, this.topPos + slot.y - 1,
                0, 0, 18, 18, 18, 18);
        }
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
