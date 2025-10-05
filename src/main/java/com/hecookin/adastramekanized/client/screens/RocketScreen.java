package com.hecookin.adastramekanized.client.screens;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.menus.RocketMenu;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public class RocketScreen extends AbstractContainerScreen<RocketMenu> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            AdAstraMekanized.MOD_ID, "textures/gui/container/rocket.png");

    // Fluid bar position matching Ad Astra exactly (offset + 6 pixels from base position)
    private static final int FLUID_BAR_X = 43;  // Ad Astra renders at leftPos + 37 + 6 = leftPos + 43
    private static final int FLUID_BAR_Y = 24;  // Ad Astra renders at topPos + 55 - 31 = topPos + 24
    private static final int FLUID_BAR_WIDTH = 12;
    private static final int FLUID_BAR_HEIGHT = 46;

    public RocketScreen(RocketMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 177;
        this.imageHeight = 174;

        // Match Ad Astra exact label positions
        this.inventoryLabelX = this.imageWidth - 180;  // -3
        this.inventoryLabelY = this.imageHeight - 94;  // 80
        this.titleLabelX = this.imageWidth - 180;      // -3
        this.titleLabelY = 6;
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // Render texture at x - 8 to match Ad Astra
        graphics.blit(TEXTURE, x - 8, y, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);

        // Fluid bar uses standard x position (not offset like texture)
        drawFluidBar(graphics, x, y);
    }

    private void drawFluidBar(GuiGraphics graphics, int leftPos, int topPos) {
        FluidStack fluid = menu.getRocket().fluid();
        long capacity = menu.getRocket().fluidContainer().getTankCapacity(0);

        int x = leftPos + FLUID_BAR_X;
        int y = topPos + FLUID_BAR_Y;

        if (!fluid.isEmpty()) {
            float ratio = fluid.getAmount() / (float) capacity;

            ResourceLocation stillTexture = net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions.of(fluid.getFluid()).getStillTexture(fluid);
            TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS).apply(stillTexture);
            int color = net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions.of(fluid.getFluid()).getTintColor(fluid);

            float r = FastColor.ARGB32.red(color) / 255f;
            float g = FastColor.ARGB32.green(color) / 255f;
            float b = FastColor.ARGB32.blue(color) / 255f;

            int barHeight = (int) (FLUID_BAR_HEIGHT * ratio);
            int yOffset = FLUID_BAR_HEIGHT - barHeight;

            // Match Ad Astra's rendering exactly
            graphics.enableScissor(
                x,
                y + yOffset,
                x + FLUID_BAR_WIDTH,
                y + FLUID_BAR_HEIGHT
            );

            // Render fluid texture tiles (Ad Astra uses i from 1 to 4)
            for (int i = 1; i < 5; i++) {
                graphics.blit(
                    x + 1,
                    y + FLUID_BAR_HEIGHT - 3 - i * 12,
                    0,
                    14, 14,
                    sprite,
                    r, g, b, 1
                );
            }

            graphics.disableScissor();
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);

        // Render fluid bar tooltip
        if (isHovering(FLUID_BAR_X, FLUID_BAR_Y, FLUID_BAR_WIDTH, FLUID_BAR_HEIGHT, mouseX, mouseY)) {
            FluidStack fluid = menu.getRocket().fluid();
            long capacity = menu.getRocket().fluidContainer().getTankCapacity(0);

            String fluidName = fluid.isEmpty() ? "Empty" : fluid.getHoverName().getString();
            Component tooltip = Component.literal(String.format("%s: %,d / %,d mB",
                fluidName, fluid.getAmount(), capacity));
            graphics.renderTooltip(this.font, tooltip, mouseX, mouseY);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x2a262b, false);
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x2a262b, false);
    }
}
