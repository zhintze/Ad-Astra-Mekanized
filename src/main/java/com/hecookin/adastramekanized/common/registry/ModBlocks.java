package com.hecookin.adastramekanized.common.registry;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.blocks.BlockProperties;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Central registry for all mod blocks.
 *
 * Organized by category for easy maintenance and debugging.
 * All blocks automatically get corresponding BlockItems.
 */
public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Registries.BLOCK, AdAstraMekanized.MOD_ID);

    // ========== METAL BLOCKS ==========

    public static final Supplier<Block> STEEL_BLOCK = registerBlock("steel_block",
            () -> new Block(BlockProperties.METAL_BLOCK));

    public static final Supplier<Block> ETRIUM_BLOCK = registerBlock("etrium_block",
            () -> new Block(BlockProperties.METAL_BLOCK));

    public static final Supplier<Block> DESH_BLOCK = registerBlock("desh_block",
            () -> new Block(BlockProperties.METAL_BLOCK));

    public static final Supplier<Block> OSTRUM_BLOCK = registerBlock("ostrum_block",
            () -> new Block(BlockProperties.METAL_BLOCK));

    public static final Supplier<Block> CALORITE_BLOCK = registerBlock("calorite_block",
            () -> new Block(BlockProperties.METAL_BLOCK));

    // ========== RAW MATERIAL BLOCKS ==========

    public static final Supplier<Block> RAW_DESH_BLOCK = registerBlock("raw_desh_block",
            () -> new Block(BlockProperties.RAW_METAL_BLOCK));

    public static final Supplier<Block> RAW_OSTRUM_BLOCK = registerBlock("raw_ostrum_block",
            () -> new Block(BlockProperties.RAW_METAL_BLOCK));

    public static final Supplier<Block> RAW_CALORITE_BLOCK = registerBlock("raw_calorite_block",
            () -> new Block(BlockProperties.RAW_METAL_BLOCK));

    // ========== SPECIAL BLOCKS ==========

    public static final Supplier<Block> CHEESE_BLOCK = registerBlock("cheese_block",
            () -> new Block(BlockProperties.CHEESE_BLOCK));

    public static final Supplier<Block> SKY_STONE = registerBlock("sky_stone",
            () -> new Block(BlockProperties.SKY_STONE));

    // ========== MOON STONES ==========

    public static final Supplier<Block> MOON_STONE = registerBlock("moon_stone",
            () -> new Block(BlockProperties.moonStone()));

    public static final Supplier<Block> MOON_COBBLESTONE = registerBlock("moon_cobblestone",
            () -> new Block(BlockProperties.moonStone()));

    public static final Supplier<Block> MOON_DEEPSLATE = registerBlock("moon_deepslate",
            () -> new Block(BlockProperties.moonStone()));

    public static final Supplier<Block> MOON_SAND = registerBlock("moon_sand",
            () -> new Block(BlockProperties.moonStone()));

    // ========== MARS STONES ==========

    public static final Supplier<Block> MARS_STONE = registerBlock("mars_stone",
            () -> new Block(BlockProperties.marsStone()));

    public static final Supplier<Block> MARS_COBBLESTONE = registerBlock("mars_cobblestone",
            () -> new Block(BlockProperties.marsStone()));

    public static final Supplier<Block> MARS_SAND = registerBlock("mars_sand",
            () -> new Block(BlockProperties.marsStone()));

    public static final Supplier<Block> CONGLOMERATE = registerBlock("conglomerate",
            () -> new Block(BlockProperties.marsStone()));

    public static final Supplier<Block> POLISHED_CONGLOMERATE = registerBlock("polished_conglomerate",
            () -> new Block(BlockProperties.marsStone()));

    // ========== VENUS STONES ==========

    public static final Supplier<Block> VENUS_STONE = registerBlock("venus_stone",
            () -> new Block(BlockProperties.venusStone()));

    public static final Supplier<Block> VENUS_COBBLESTONE = registerBlock("venus_cobblestone",
            () -> new Block(BlockProperties.venusStone()));

    public static final Supplier<Block> VENUS_SAND = registerBlock("venus_sand",
            () -> new Block(BlockProperties.venusStone()));

    public static final Supplier<Block> VENUS_SANDSTONE = registerBlock("venus_sandstone",
            () -> new Block(BlockProperties.venusStone()));

    // ========== MERCURY STONES ==========

    public static final Supplier<Block> MERCURY_STONE = registerBlock("mercury_stone",
            () -> new Block(BlockProperties.mercuryStone()));

    public static final Supplier<Block> MERCURY_COBBLESTONE = registerBlock("mercury_cobblestone",
            () -> new Block(BlockProperties.mercuryStone()));

    // ========== GLACIO STONES ==========

    public static final Supplier<Block> GLACIO_STONE = registerBlock("glacio_stone",
            () -> new Block(BlockProperties.glacioStone()));

    public static final Supplier<Block> GLACIO_COBBLESTONE = registerBlock("glacio_cobblestone",
            () -> new Block(BlockProperties.glacioStone()));

    public static final Supplier<Block> PERMAFROST = registerBlock("permafrost",
            () -> new Block(BlockProperties.glacioStone()));

    /**
     * Helper method to register a block with its corresponding BlockItem
     */
    private static <T extends Block> Supplier<T> registerBlock(String name, Supplier<T> block) {
        Supplier<T> registeredBlock = BLOCKS.register(name, block);

        // Register the corresponding BlockItem
        ModItems.ITEMS.register(name, () -> new BlockItem(registeredBlock.get(), new Item.Properties()));

        return registeredBlock;
    }

    /**
     * Register all mod blocks
     */
    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        AdAstraMekanized.LOGGER.info("Registered {} mod blocks", BLOCKS.getEntries().size());
    }
}