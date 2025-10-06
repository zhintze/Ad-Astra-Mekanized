package com.hecookin.adastramekanized.datagen;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.datagen.providers.ModBlockLootTableProvider;
import com.hecookin.adastramekanized.datagen.providers.ModRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Main entry point for all data generation in Ad Astra Mekanized.
 * This class handles registration of all data providers when running the data generation task.
 */
@EventBusSubscriber(modid = AdAstraMekanized.MOD_ID)
public class ModDataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        AdAstraMekanized.LOGGER.info("Starting Ad Astra Mekanized data generation...");

        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        // Add recipe provider
        generator.addProvider(
            event.includeServer(),
            new ModRecipeProvider(packOutput, lookupProvider)
        );

        // Add loot table provider
        generator.addProvider(
            event.includeServer(),
            new LootTableProvider(
                packOutput,
                Collections.emptySet(),
                List.of(
                    new LootTableProvider.SubProviderEntry(
                        ModBlockLootTableProvider::new,
                        LootContextParamSets.BLOCK
                    )
                ),
                lookupProvider
            )
        );

        AdAstraMekanized.LOGGER.info("Registered data providers for Ad Astra Mekanized");
    }
}