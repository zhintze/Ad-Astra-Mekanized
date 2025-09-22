package com.hecookin.adastramekanized.common.registry;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.worldgen.PlanetChunkGenerator;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Registry for custom chunk generators
 */
public class ModChunkGenerators {

    private static final DeferredRegister<MapCodec<? extends ChunkGenerator>> CHUNK_GENERATORS =
        DeferredRegister.create(Registries.CHUNK_GENERATOR, AdAstraMekanized.MOD_ID);

    // Register the PlanetChunkGenerator
    public static final Supplier<MapCodec<PlanetChunkGenerator>> PLANET_CHUNK_GENERATOR =
        CHUNK_GENERATORS.register("planet", () -> PlanetChunkGenerator.CODEC);

    /**
     * Register chunk generators with the mod event bus
     */
    public static void register(IEventBus modEventBus) {
        CHUNK_GENERATORS.register(modEventBus);
        AdAstraMekanized.LOGGER.info("Registered chunk generators");
    }
}