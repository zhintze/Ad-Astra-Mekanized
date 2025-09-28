package com.hecookin.adastramekanized.common.registry;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.blockentities.machines.OxygenDistributorBlockEntity;
import com.hecookin.adastramekanized.common.blockentities.machines.ImprovedOxygenDistributor;
import com.hecookin.adastramekanized.common.blockentities.machines.WirelessPowerRelayBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntityTypes {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
        DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, AdAstraMekanized.MOD_ID);

    // Keeping old one temporarily for compatibility
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<OxygenDistributorBlockEntity>> OXYGEN_DISTRIBUTOR =
        BLOCK_ENTITY_TYPES.register("oxygen_distributor",
            () -> createBlockEntityType(
                OxygenDistributorBlockEntity::new,
                ModBlocks.OXYGEN_DISTRIBUTOR.get()));

    // Improved oxygen distributor with Mekanism integration
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ImprovedOxygenDistributor>> MEKANISM_OXYGEN_DISTRIBUTOR =
        BLOCK_ENTITY_TYPES.register("mekanism_oxygen_distributor",
            () -> createBlockEntityType(
                ImprovedOxygenDistributor::new,
                ModBlocks.OXYGEN_DISTRIBUTOR.get()));

    // Wireless Power Relay block entity
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WirelessPowerRelayBlockEntity>> WIRELESS_POWER_RELAY =
        BLOCK_ENTITY_TYPES.register("wireless_power_relay",
            () -> createBlockEntityType(
                WirelessPowerRelayBlockEntity::new,
                ModBlocks.WIRELESS_POWER_RELAY.get()));

    /**
     * Helper method to create a BlockEntityType
     */
    private static <T extends BlockEntity> BlockEntityType<T> createBlockEntityType(
            BlockEntityType.BlockEntitySupplier<T> supplier, Block... blocks) {
        return BlockEntityType.Builder.of(supplier, blocks).build(null);
    }
}