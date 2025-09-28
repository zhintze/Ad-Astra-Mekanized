package com.hecookin.adastramekanized.client.screens;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.data.DistributorLinkData;
import com.hecookin.adastramekanized.common.menus.OxygenMonitorMenu;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * GUI screen for the Oxygen Network Monitor
 */
public class OxygenMonitorScreen extends AbstractContainerScreen<OxygenMonitorMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
        AdAstraMekanized.MOD_ID, "textures/gui/oxygen_monitor.png"
    );

    private static final int ENTRIES_PER_PAGE = 6;
    private int currentPage = 0;
    private int totalPages = 1;
    private int scrollY = 0;

    // Colors
    private static final int COLOR_ONLINE = 0x00FF00;
    private static final int COLOR_OFFLINE = 0xFF0000;
    private static final int COLOR_WARNING = 0xFFAA00;
    private static final int COLOR_HEADER = 0x4444FF;
    private static final int COLOR_TEXT = 0xFFFFFF;
    private static final int COLOR_DARK = 0x404040;

    public OxygenMonitorScreen(OxygenMonitorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 256;
        this.imageHeight = 200;
        this.inventoryLabelY = -999; // Hide inventory label
    }

    @Override
    protected void init() {
        super.init();

        DistributorLinkData linkData = menu.getLinkData();
        int distributorCount = linkData.getLinkCount();
        this.totalPages = Math.max(1, (distributorCount + ENTRIES_PER_PAGE - 1) / ENTRIES_PER_PAGE);

        // Add page navigation buttons if needed
        if (totalPages > 1) {
            int buttonY = topPos + imageHeight - 25;

            // Previous page button
            addRenderableWidget(Button.builder(Component.literal("<"), button -> {
                if (currentPage > 0) {
                    currentPage--;
                }
            }).pos(leftPos + 10, buttonY)
              .size(20, 20)
              .build());

            // Next page button
            addRenderableWidget(Button.builder(Component.literal(">"), button -> {
                if (currentPage < totalPages - 1) {
                    currentPage++;
                }
            }).pos(leftPos + imageWidth - 30, buttonY)
              .size(20, 20)
              .build());
        }
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        // Render background
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);

        // Render title
        Component title = Component.literal("Oxygen Network Monitor")
            .withStyle(style -> style.withColor(COLOR_HEADER).withBold(true));
        int titleX = leftPos + (imageWidth - font.width(title)) / 2;
        graphics.drawString(font, title, titleX, topPos + 8, COLOR_HEADER, false);

        // Render network summary
        DistributorLinkData linkData = menu.getLinkData();
        renderNetworkSummary(graphics, linkData, leftPos + 10, topPos + 25);

        // Render distributor list
        renderDistributorList(graphics, linkData, leftPos + 10, topPos + 50);

        // Render page indicator if multiple pages
        if (totalPages > 1) {
            Component pageText = Component.literal(String.format("Page %d/%d", currentPage + 1, totalPages));
            int pageX = leftPos + (imageWidth - font.width(pageText)) / 2;
            graphics.drawString(font, pageText, pageX, topPos + imageHeight - 20, COLOR_TEXT, false);
        }

        this.renderTooltip(graphics, mouseX, mouseY);
    }

    private void renderNetworkSummary(GuiGraphics graphics, DistributorLinkData linkData, int x, int y) {
        int totalDistributors = linkData.getLinkCount();
        int onlineCount = 0;
        int warningCount = 0;

        for (DistributorLinkData.LinkedDistributor distributor : linkData.getLinkedDistributors()) {
            if (distributor.isOnline()) {
                onlineCount++;
                if (distributor.getLastOxygen() < 1000 || distributor.getLastEnergy() < 100) {
                    warningCount++;
                }
            }
        }

        // Draw summary box
        graphics.fill(x - 2, y - 2, x + imageWidth - 18, y + 18, 0x44000000);

        // Draw summary text
        String summaryText = String.format("Network: %d distributors | %d online",
            totalDistributors, onlineCount);
        graphics.drawString(font, summaryText, x, y, COLOR_TEXT, false);

        if (warningCount > 0) {
            String warningText = String.format("(%d low resources)", warningCount);
            graphics.drawString(font, warningText, x, y + 10, COLOR_WARNING, false);
        }
    }

    private void renderDistributorList(GuiGraphics graphics, DistributorLinkData linkData, int x, int y) {
        List<DistributorLinkData.LinkedDistributor> distributors = linkData.getLinkedDistributors();

        // Calculate entries for current page
        int startIndex = currentPage * ENTRIES_PER_PAGE;
        int endIndex = Math.min(startIndex + ENTRIES_PER_PAGE, distributors.size());

        // Column headers
        graphics.drawString(font, "Position", x, y, COLOR_DARK, false);
        graphics.drawString(font, "Status", x + 80, y, COLOR_DARK, false);
        graphics.drawString(font, "Oxygen", x + 130, y, COLOR_DARK, false);
        graphics.drawString(font, "Power", x + 180, y, COLOR_DARK, false);
        y += 12;

        // Draw separator line
        graphics.fill(x, y - 2, x + imageWidth - 28, y - 1, 0x44FFFFFF);

        // Render each distributor
        for (int i = startIndex; i < endIndex; i++) {
            DistributorLinkData.LinkedDistributor distributor = distributors.get(i);
            renderDistributorEntry(graphics, distributor, x, y + (i - startIndex) * 20);
        }
    }

    private void renderDistributorEntry(GuiGraphics graphics, DistributorLinkData.LinkedDistributor distributor, int x, int y) {
        BlockPos pos = distributor.getPos();
        boolean isEnabled = distributor.isEnabled();
        boolean isOnline = distributor.isOnline();

        // Background for entry
        int bgColor = isEnabled ? (isOnline ? 0x2200FF00 : 0x22FF0000) : 0x22808080;
        graphics.fill(x - 2, y - 2, x + imageWidth - 28, y + 16, bgColor);

        // Position
        String posText = String.format("%d, %d, %d", pos.getX(), pos.getY(), pos.getZ());
        graphics.drawString(font, posText, x, y, COLOR_TEXT, false);

        // Status
        Component statusText;
        int statusColor;
        if (!isEnabled) {
            statusText = Component.literal("Disabled");
            statusColor = COLOR_DARK;
        } else if (isOnline) {
            statusText = Component.literal("Online");
            statusColor = COLOR_ONLINE;
        } else {
            statusText = Component.literal("Offline");
            statusColor = COLOR_OFFLINE;
        }
        graphics.drawString(font, statusText, x + 80, y, statusColor, false);

        // Oxygen level
        if (isOnline && isEnabled) {
            long oxygen = distributor.getLastOxygen();
            String oxygenText = formatFluid(oxygen);
            int oxygenColor = oxygen < 1000 ? COLOR_WARNING : COLOR_TEXT;
            graphics.drawString(font, oxygenText, x + 130, y, oxygenColor, false);

            // Energy level
            int energy = distributor.getLastEnergy();
            String energyText = formatEnergy(energy);
            int energyColor = energy < 100 ? COLOR_WARNING : COLOR_TEXT;
            graphics.drawString(font, energyText, x + 180, y, energyColor, false);

            // Efficiency bar
            float efficiency = distributor.getLastEfficiency();
            renderEfficiencyBar(graphics, x, y + 9, efficiency);
        } else {
            graphics.drawString(font, "---", x + 130, y, COLOR_DARK, false);
            graphics.drawString(font, "---", x + 180, y, COLOR_DARK, false);
        }

        // Custom name if set
        String customName = distributor.getCustomName();
        if (customName != null && !customName.isEmpty()) {
            Component nameText = Component.literal(customName)
                .withStyle(ChatFormatting.ITALIC);
            graphics.drawString(font, nameText, x, y + 9, 0x8888FF, false);
        }
    }

    private void renderEfficiencyBar(GuiGraphics graphics, int x, int y, float efficiency) {
        int barWidth = 60;
        int barHeight = 4;

        // Background
        graphics.fill(x, y, x + barWidth, y + barHeight, 0xFF000000);

        // Fill
        int fillWidth = (int) (barWidth * efficiency);
        int fillColor = efficiency > 0.75f ? 0xFF00FF00 :
                       efficiency > 0.5f ? 0xFFFFAA00 : 0xFFFF0000;
        graphics.fill(x + 1, y + 1, x + fillWidth - 1, y + barHeight - 1, fillColor);
    }

    private String formatFluid(long amount) {
        if (amount >= 1000000) {
            return String.format("%.1fM mB", amount / 1000000.0);
        } else if (amount >= 1000) {
            return String.format("%.1fK mB", amount / 1000.0);
        } else {
            return amount + " mB";
        }
    }

    private String formatEnergy(int amount) {
        if (amount >= 1000000) {
            return String.format("%.1fM FE", amount / 1000000.0);
        } else if (amount >= 1000) {
            return String.format("%.1fK FE", amount / 1000.0);
        } else {
            return amount + " FE";
        }
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
        // Labels are rendered in the main render method
    }
}