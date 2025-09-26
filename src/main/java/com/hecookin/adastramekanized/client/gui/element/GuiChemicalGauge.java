package com.hecookin.adastramekanized.client.gui.element;

import com.hecookin.adastramekanized.AdAstraMekanized;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.LongSupplier;

/**
 * Chemical gauge for oxygen/gas display following Mekanism's style
 */
public class GuiChemicalGauge extends GuiGauge {

    private static final ResourceLocation CHEMICAL_GAUGE_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            AdAstraMekanized.MOD_ID, "textures/gui/gauges/chemical.png");

    private final LongSupplier amountSupplier;
    private final LongSupplier capacitySupplier;
    private final String chemicalName;
    private final int chemicalColor;

    public GuiChemicalGauge(int x, int y, LongSupplier amountSupplier, LongSupplier capacitySupplier,
                            String chemicalName, int color) {
        super(GaugeType.STANDARD, x, y, CHEMICAL_GAUGE_TEXTURE);
        this.amountSupplier = amountSupplier;
        this.capacitySupplier = capacitySupplier;
        this.chemicalName = chemicalName;
        this.chemicalColor = color;
    }

    @Override
    public int getScaledLevel() {
        long amount = amountSupplier.getAsLong();
        long capacity = capacitySupplier.getAsLong();

        if (capacity == 0 || amount == 0) {
            return 0;
        }

        return (int) ((type.height - 2) * Math.min(1.0, (double) amount / capacity));
    }

    @Override
    public int getColor() {
        return chemicalColor;
    }

    @Override
    public Component getTooltipText() {
        long amount = amountSupplier.getAsLong();
        long capacity = capacitySupplier.getAsLong();

        if (capacity == 0) {
            return Component.literal(chemicalName + ": Empty");
        }

        return Component.literal(String.format("%s: %,d / %,d mB", chemicalName, amount, capacity));
    }
}