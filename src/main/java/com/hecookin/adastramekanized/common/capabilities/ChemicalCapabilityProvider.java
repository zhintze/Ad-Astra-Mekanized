package com.hecookin.adastramekanized.common.capabilities;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.integration.ModIntegrationManager;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import java.lang.reflect.Method;

/**
 * Provides chemical storage capabilities for items when Mekanism is available.
 * This allows our space suits to be filled in Mekanism's chemical tanks.
 */
public class ChemicalCapabilityProvider {

    /**
     * Attach chemical capabilities to an ItemStack when Mekanism is available
     */
    public static ICapabilityProvider<ItemStack, Void, ?> createForArmor(long capacity, String... acceptedChemicals) {
        ModIntegrationManager manager = AdAstraMekanized.getIntegrationManager();

        if (manager.isMekanismAvailable()) {
            try {
                // Use Mekanism's chemical system via reflection
                return createMekanismCapability(capacity, acceptedChemicals);
            } catch (Exception e) {
                AdAstraMekanized.LOGGER.warn("Failed to create Mekanism chemical capability: {}", e.getMessage());
            }
        }

        // Return null if Mekanism is not available
        return null;
    }

    private static ICapabilityProvider<ItemStack, Void, ?> createMekanismCapability(long capacity, String... acceptedChemicals) throws Exception {
        // This would use reflection to create a Mekanism chemical tank capability
        // For now, we'll need to handle this through Mekanism's API when available

        Class<?> chemicalTanksBuilderClass = Class.forName("mekanism.common.capabilities.chemical.ChemicalTanksBuilder");
        Object builder = chemicalTanksBuilderClass.getMethod("builder").invoke(null);

        // Get the MekanismChemicals class for oxygen/hydrogen references
        Class<?> mekanismChemicalsClass = Class.forName("mekanism.common.registries.MekanismChemicals");

        // Add internal storage for each accepted chemical
        for (String chemicalName : acceptedChemicals) {
            Object chemical = null;
            if ("oxygen".equalsIgnoreCase(chemicalName)) {
                chemical = mekanismChemicalsClass.getField("OXYGEN").get(null);
            } else if ("hydrogen".equalsIgnoreCase(chemicalName)) {
                chemical = mekanismChemicalsClass.getField("HYDROGEN").get(null);
            }

            if (chemical != null) {
                // Create a predicate that accepts the specific chemical
                final Object finalChemical = chemical;  // Make it final for lambda
                Class<?> predicateClass = Class.forName("java.util.function.Predicate");
                Method addStorageMethod = builder.getClass().getMethod("addInternalStorage",
                    long.class, long.class, predicateClass);

                // Create predicate using lambda equivalent
                Object predicate = java.lang.reflect.Proxy.newProxyInstance(
                    predicateClass.getClassLoader(),
                    new Class<?>[] { predicateClass },
                    (proxy, method, args) -> {
                        if (method.getName().equals("test")) {
                            // Check if the chemical matches
                            return args[0] != null && args[0].equals(finalChemical);
                        }
                        return false;
                    }
                );

                builder = addStorageMethod.invoke(builder, capacity, capacity, predicate);
            }
        }

        // Build and return the capability
        Method buildMethod = builder.getClass().getMethod("build");
        Object chemicalTanks = buildMethod.invoke(builder);

        // Wrap in a capability provider
        return new ICapabilityProvider<ItemStack, Void, Object>() {
            @Override
            public Object getCapability(ItemStack stack, Void context) {
                return chemicalTanks;
            }
        };
    }
}