package com.hecookin.adastramekanized.client.screens;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.menus.FuelLoaderMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

/**
 * Screen for fuel loader GUI.
 * Displays fuel tank level and bucket slots.
 */
public class FuelLoaderScreen extends AbstractContainerScreen<FuelLoaderMenu> {

    private static final ResourceLocation TEXTURE =
        ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "textures/gui/container/fuel_loader.png");

    public FuelLoaderScreen(FuelLoaderMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 177;
        this.imageHeight = 184;
        this.titleLabelY = 6;
        this.inventoryLabelY = 90;
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        graphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);

        // Render fluid tank
        renderFluidTank(graphics, x, y);
    }

    private void renderFluidTank(GuiGraphics graphics, int x, int y) {
        FluidStack fluidStack = menu.getBlockEntity().getFluidTank().getFluid();

        int capacity = menu.getBlockEntity().getFluidTank().getCapacity();
        int amount = fluidStack.getAmount();

        // Tank rendering area (in GUI coords) - matches Ad Astra position at (43, 22)
        int tankX = x + 43;
        int tankY = y + 22;
        int tankWidth = 16;
        int tankHeight = 52;

        if (fluidStack.isEmpty()) return;

        // Calculate fill height
        int fillHeight = (int) ((float) amount / capacity * tankHeight);
        int fillY = tankY + tankHeight - fillHeight;

        // Simple colored rectangle for fuel rendering
        // Orange/yellow color for fuel
        graphics.fill(tankX, fillY, tankX + tankWidth, tankY + tankHeight, 0xFFFF9933);
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);

        // Render fluid tank tooltip
        renderFluidTooltip(graphics, mouseX, mouseY);
    }

    private void renderFluidTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // Tank tooltip area - matches Ad Astra position
        int tankX = x + 43;
        int tankY = y + 22;
        int tankWidth = 16;
        int tankHeight = 52;

        if (mouseX >= tankX && mouseX <= tankX + tankWidth &&
            mouseY >= tankY && mouseY <= tankY + tankHeight) {

            FluidStack fluidStack = menu.getBlockEntity().getFluidTank().getFluid();
            int capacity = menu.getBlockEntity().getFluidTank().getCapacity();

            if (!fluidStack.isEmpty()) {
                graphics.renderTooltip(font,
                    Component.literal(fluidStack.getHoverName().getString() + ": " +
                        fluidStack.getAmount() + " / " + capacity + " mB"),
                    mouseX, mouseY);
            } else {
                graphics.renderTooltip(font,
                    Component.literal("Empty: 0 / " + capacity + " mB"),
                    mouseX, mouseY);
            }
        }
    }
}
