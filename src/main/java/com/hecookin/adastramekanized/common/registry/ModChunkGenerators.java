package com.hecookin.adastramekanized.common.registry;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registry for custom chunk generators.
 * Currently empty as planets use vanilla noise-based generation configured through data files.
 * Kept for future custom generation needs.
 */
public class ModChunkGenerators {

    private static final DeferredRegister<MapCodec<? extends ChunkGenerator>> CHUNK_GENERATORS =
        DeferredRegister.create(Registries.CHUNK_GENERATOR, AdAstraMekanized.MOD_ID);

    // No custom chunk generators currently registered
    // Planets use vanilla noise chunk generator with custom noise settings

    /**
     * Register chunk generators with the mod event bus
     */
    public static void register(IEventBus modEventBus) {
        CHUNK_GENERATORS.register(modEventBus);
        AdAstraMekanized.LOGGER.info("Registered chunk generators");
    }
}