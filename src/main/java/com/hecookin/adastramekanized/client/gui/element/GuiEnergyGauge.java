package com.hecookin.adastramekanized.client.gui.element;

import com.hecookin.adastramekanized.AdAstraMekanized;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.text.NumberFormat;
import java.util.function.LongSupplier;

/**
 * Energy gauge following Mekanism's style
 */
public class GuiEnergyGauge extends GuiGauge {

    private static final ResourceLocation ENERGY_GAUGE_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            AdAstraMekanized.MOD_ID, "textures/gui/gauges/energy.png");

    private static final NumberFormat FORMATTER = NumberFormat.getInstance();

    private final LongSupplier energySupplier;
    private final LongSupplier maxEnergySupplier;

    public GuiEnergyGauge(int x, int y, LongSupplier energySupplier, LongSupplier maxEnergySupplier) {
        super(GaugeType.STANDARD, x, y, ENERGY_GAUGE_TEXTURE);
        this.energySupplier = energySupplier;
        this.maxEnergySupplier = maxEnergySupplier;
    }

    @Override
    public int getScaledLevel() {
        long energy = energySupplier.getAsLong();
        long maxEnergy = maxEnergySupplier.getAsLong();

        if (maxEnergy == 0 || energy == 0) {
            return 0;
        }

        return (int) ((type.height - 2) * Math.min(1.0, (double) energy / maxEnergy));
    }

    @Override
    public int getColor() {
        // Mekanism energy green
        return 0x00D000;
    }

    @Override
    public Component getTooltipText() {
        long energy = energySupplier.getAsLong();
        long maxEnergy = maxEnergySupplier.getAsLong();

        if (maxEnergy == 0) {
            return Component.literal("Energy: Empty");
        }

        String energyStr = formatEnergy(energy);
        String maxEnergyStr = formatEnergy(maxEnergy);

        return Component.literal(String.format("Energy: %s / %s FE", energyStr, maxEnergyStr));
    }

    private String formatEnergy(long energy) {
        if (energy >= 1_000_000_000) {
            return String.format("%.2fG", energy / 1_000_000_000.0);
        } else if (energy >= 1_000_000) {
            return String.format("%.2fM", energy / 1_000_000.0);
        } else if (energy >= 1_000) {
            return String.format("%.2fK", energy / 1_000.0);
        } else {
            return String.valueOf(energy);
        }
    }
}