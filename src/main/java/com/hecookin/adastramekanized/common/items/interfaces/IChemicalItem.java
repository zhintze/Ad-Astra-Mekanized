package com.hecookin.adastramekanized.common.items.interfaces;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.integration.ModIntegrationManager;
import net.minecraft.world.item.ItemStack;

/**
 * Interface for items that can store chemicals (oxygen, hydrogen, etc.)
 * When Mekanism is available, delegates to Mekanism's chemical system.
 */
public interface IChemicalItem {

    default boolean hasChemical(ItemStack stack) {
        ModIntegrationManager manager = AdAstraMekanized.getIntegrationManager();
        if (manager.isMekanismAvailable()) {
            try {
                // Check with Mekanism's chemical system
                return manager.getMekanismIntegration().hasAnyChemical(stack);
            } catch (Exception ignored) {}
        }
        return false;
    }

    default long useChemical(ItemStack stack, long amount, String chemicalName) {
        ModIntegrationManager manager = AdAstraMekanized.getIntegrationManager();
        if (manager.isMekanismAvailable()) {
            try {
                // Extract chemical using Mekanism's system
                return manager.getMekanismIntegration().extractChemical(stack, chemicalName, amount);
            } catch (Exception ignored) {}
        }
        return 0;
    }
}