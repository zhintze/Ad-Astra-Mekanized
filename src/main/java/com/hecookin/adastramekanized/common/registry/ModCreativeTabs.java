package com.hecookin.adastramekanized.common.registry;

import com.hecookin.adastramekanized.AdAstraMekanized;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Creative mode tabs for Ad Astra Mekanized.
 *
 * Organized by category for easy item discovery:
 * - Materials: All ingots, nuggets, plates, rods, raw materials
 * - Building Blocks: All planet stones, metal blocks, processed blocks
 * - Industrial: All factory blocks, plating, machinery
 * - Decorative: Flags, special decorative items
 */
public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AdAstraMekanized.MOD_ID);

    // ========== MATERIALS TAB ==========
    public static final Supplier<CreativeModeTab> MATERIALS_TAB = CREATIVE_MODE_TABS.register("materials",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("creativetab.adastramekanized.materials"))
                    .icon(() -> new ItemStack(ModItems.DESH_INGOT.get()))
                    .displayItems((parameters, output) -> {
                        // Metal Ingots
                        output.accept(ModItems.STEEL_INGOT.get());
                        output.accept(ModItems.ETRIUM_INGOT.get());
                        output.accept(ModItems.DESH_INGOT.get());
                        output.accept(ModItems.OSTRUM_INGOT.get());
                        output.accept(ModItems.CALORITE_INGOT.get());

                        // Metal Nuggets
                        output.accept(ModItems.STEEL_NUGGET.get());
                        output.accept(ModItems.ETRIUM_NUGGET.get());
                        output.accept(ModItems.DESH_NUGGET.get());
                        output.accept(ModItems.OSTRUM_NUGGET.get());
                        output.accept(ModItems.CALORITE_NUGGET.get());

                        // Raw Materials
                        output.accept(ModItems.RAW_DESH.get());
                        output.accept(ModItems.RAW_OSTRUM.get());
                        output.accept(ModItems.RAW_CALORITE.get());

                        // Processed Materials - Plates
                        output.accept(ModItems.IRON_PLATE.get());
                        output.accept(ModItems.STEEL_PLATE.get());
                        output.accept(ModItems.ETRIUM_PLATE.get());
                        output.accept(ModItems.DESH_PLATE.get());
                        output.accept(ModItems.OSTRUM_PLATE.get());
                        output.accept(ModItems.CALORITE_PLATE.get());

                        // Processed Materials - Rods
                        output.accept(ModItems.IRON_ROD.get());
                        output.accept(ModItems.STEEL_ROD.get());
                        output.accept(ModItems.ETRIUM_ROD.get());

                        // Special Materials
                        output.accept(ModItems.ETRIONIC_CORE.get());
                        output.accept(ModItems.ICE_SHARD.get());
                        output.accept(ModItems.CHEESE.get());
                    })
                    .build());

    // ========== BUILDING BLOCKS TAB ==========
    public static final Supplier<CreativeModeTab> BUILDING_BLOCKS_TAB = CREATIVE_MODE_TABS.register("building_blocks",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("creativetab.adastramekanized.building_blocks"))
                    .icon(() -> new ItemStack(ModBlocks.DESH_BLOCK.get()))
                    .displayItems((parameters, output) -> {
                        // Metal Blocks
                        output.accept(ModBlocks.STEEL_BLOCK.get());
                        output.accept(ModBlocks.ETRIUM_BLOCK.get());
                        output.accept(ModBlocks.DESH_BLOCK.get());
                        output.accept(ModBlocks.OSTRUM_BLOCK.get());
                        output.accept(ModBlocks.CALORITE_BLOCK.get());

                        // Raw Material Blocks
                        output.accept(ModBlocks.RAW_DESH_BLOCK.get());
                        output.accept(ModBlocks.RAW_OSTRUM_BLOCK.get());
                        output.accept(ModBlocks.RAW_CALORITE_BLOCK.get());

                        // Special Blocks
                        output.accept(ModBlocks.CHEESE_BLOCK.get());

                        // Moon Stones
                        output.accept(ModBlocks.MOON_STONE.get());
                        output.accept(ModBlocks.MOON_STONE_STAIRS.get());
                        output.accept(ModBlocks.MOON_STONE_SLAB.get());
                        output.accept(ModBlocks.MOON_STONE_WALL.get());
                        output.accept(ModBlocks.MOON_COBBLESTONE.get());
                        output.accept(ModBlocks.MOON_COBBLESTONE_STAIRS.get());
                        output.accept(ModBlocks.MOON_COBBLESTONE_SLAB.get());
                        output.accept(ModBlocks.MOON_COBBLESTONE_WALL.get());
                        output.accept(ModBlocks.MOON_DEEPSLATE.get());
                        output.accept(ModBlocks.MOON_SAND.get());

                        // Mars Stones
                        output.accept(ModBlocks.MARS_STONE.get());
                        output.accept(ModBlocks.MARS_STONE_STAIRS.get());
                        output.accept(ModBlocks.MARS_STONE_SLAB.get());
                        output.accept(ModBlocks.MARS_STONE_WALL.get());
                        output.accept(ModBlocks.MARS_COBBLESTONE.get());
                        output.accept(ModBlocks.MARS_COBBLESTONE_STAIRS.get());
                        output.accept(ModBlocks.MARS_COBBLESTONE_SLAB.get());
                        output.accept(ModBlocks.MARS_COBBLESTONE_WALL.get());
                        output.accept(ModBlocks.MARS_SAND.get());
                        output.accept(ModBlocks.CONGLOMERATE.get());
                        output.accept(ModBlocks.POLISHED_CONGLOMERATE.get());

                        // Venus Stones
                        output.accept(ModBlocks.VENUS_STONE.get());
                        output.accept(ModBlocks.VENUS_STONE_STAIRS.get());
                        output.accept(ModBlocks.VENUS_STONE_SLAB.get());
                        output.accept(ModBlocks.VENUS_STONE_WALL.get());
                        output.accept(ModBlocks.VENUS_COBBLESTONE.get());
                        output.accept(ModBlocks.VENUS_COBBLESTONE_STAIRS.get());
                        output.accept(ModBlocks.VENUS_COBBLESTONE_SLAB.get());
                        output.accept(ModBlocks.VENUS_COBBLESTONE_WALL.get());
                        output.accept(ModBlocks.VENUS_SAND.get());
                        output.accept(ModBlocks.VENUS_SANDSTONE.get());

                        // Mercury Stones
                        output.accept(ModBlocks.MERCURY_STONE.get());
                        output.accept(ModBlocks.MERCURY_STONE_STAIRS.get());
                        output.accept(ModBlocks.MERCURY_STONE_SLAB.get());
                        output.accept(ModBlocks.MERCURY_STONE_WALL.get());
                        output.accept(ModBlocks.MERCURY_COBBLESTONE.get());
                        output.accept(ModBlocks.MERCURY_COBBLESTONE_STAIRS.get());
                        output.accept(ModBlocks.MERCURY_COBBLESTONE_SLAB.get());
                        output.accept(ModBlocks.MERCURY_COBBLESTONE_WALL.get());

                        // Glacio Stones
                        output.accept(ModBlocks.GLACIO_STONE.get());
                        output.accept(ModBlocks.GLACIO_STONE_STAIRS.get());
                        output.accept(ModBlocks.GLACIO_STONE_SLAB.get());
                        output.accept(ModBlocks.GLACIO_STONE_WALL.get());
                        output.accept(ModBlocks.GLACIO_COBBLESTONE.get());
                        output.accept(ModBlocks.GLACIO_COBBLESTONE_STAIRS.get());
                        output.accept(ModBlocks.GLACIO_COBBLESTONE_SLAB.get());
                        output.accept(ModBlocks.GLACIO_COBBLESTONE_WALL.get());
                        output.accept(ModBlocks.PERMAFROST.get());

                        // Sky Stone
                        output.accept(ModBlocks.SKY_STONE.get());
                        output.accept(ModBlocks.SKY_STONE_STAIRS.get());
                        output.accept(ModBlocks.SKY_STONE_SLAB.get());
                        output.accept(ModBlocks.SKY_STONE_WALL.get());
                    })
                    .build());

    // ========== INDUSTRIAL TAB ==========
    public static final Supplier<CreativeModeTab> INDUSTRIAL_TAB = CREATIVE_MODE_TABS.register("industrial",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("creativetab.adastramekanized.industrial"))
                    .icon(() -> new ItemStack(ModBlocks.STEEL_BLOCK.get())) // Will update when factory blocks are added
                    .displayItems((parameters, output) -> {
                        // Industrial blocks will be added here in future batches
                        // Factory blocks, plating, pillars, sliding doors, etc.
                    })
                    .build());

    // ========== DECORATIVE TAB ==========
    public static final Supplier<CreativeModeTab> DECORATIVE_TAB = CREATIVE_MODE_TABS.register("decorative",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("creativetab.adastramekanized.decorative"))
                    .icon(() -> new ItemStack(ModBlocks.CHEESE_BLOCK.get())) // Will update when flags are added
                    .displayItems((parameters, output) -> {
                        // Flags and other decorative items will be added here in future batches
                    })
                    .build());

    /**
     * Register all creative mode tabs
     */
    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
        AdAstraMekanized.LOGGER.info("Registered {} creative mode tabs", CREATIVE_MODE_TABS.getEntries().size());
    }
}