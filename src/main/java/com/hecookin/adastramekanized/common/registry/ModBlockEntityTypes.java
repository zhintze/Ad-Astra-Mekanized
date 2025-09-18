package com.hecookin.adastramekanized.common.registry;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.blockentities.machines.OxygenProcessingStationBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntityTypes {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
        DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, AdAstraMekanized.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<OxygenProcessingStationBlockEntity>> OXYGEN_PROCESSING_STATION =
        BLOCK_ENTITY_TYPES.register("oxygen_processing_station",
            () -> createBlockEntityType(
                OxygenProcessingStationBlockEntity::new,
                ModBlocks.OXYGEN_PROCESSING_STATION.get()));

    /**
     * Helper method to create a BlockEntityType
     */
    private static <T extends BlockEntity> BlockEntityType<T> createBlockEntityType(
            BlockEntityType.BlockEntitySupplier<T> supplier, Block... blocks) {
        return BlockEntityType.Builder.of(supplier, blocks).build(null);
    }
}