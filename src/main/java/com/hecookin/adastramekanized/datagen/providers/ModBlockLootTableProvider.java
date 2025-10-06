package com.hecookin.adastramekanized.datagen.providers;

import com.hecookin.adastramekanized.common.registry.ModBlocks;
import com.hecookin.adastramekanized.common.registry.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.Set;

public class ModBlockLootTableProvider extends BlockLootSubProvider {

    public ModBlockLootTableProvider(HolderLookup.Provider provider) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), provider);
    }

    @Override
    protected void generate() {
        // Etrium ores drop 3-6 nuggets with Fortune support
        dropEtriumOre(ModBlocks.ETRIUM_ORE.get(), ModItems.ETRIUM_NUGGET.get());
        dropEtriumOre(ModBlocks.MOON_ETRIUM_ORE.get(), ModItems.ETRIUM_NUGGET.get());
        dropEtriumOre(ModBlocks.MARS_ETRIUM_ORE.get(), ModItems.ETRIUM_NUGGET.get());
        dropEtriumOre(ModBlocks.GLACIO_ETRIUM_ORE.get(), ModItems.ETRIUM_NUGGET.get());
    }

    /**
     * Creates a loot table for etrium ore that drops 3-6 nuggets, with Fortune enchantment support.
     * Fortune increases drops using standard Minecraft ore_drops formula.
     */
    protected void dropEtriumOre(Block block, Item drop) {
        HolderLookup.RegistryLookup<Enchantment> enchantmentLookup = this.registries.lookupOrThrow(Registries.ENCHANTMENT);

        add(block, createSilkTouchDispatchTable(
            block,
            this.applyExplosionDecay(
                block,
                LootItem.lootTableItem(drop)
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(3.0F, 6.0F)))
                    .apply(ApplyBonusCount.addOreBonusCount(enchantmentLookup.getOrThrow(Enchantments.FORTUNE)))
            )
        ));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        // Only return the etrium ore blocks that we're generating loot tables for
        return Set.of(
            ModBlocks.ETRIUM_ORE.get(),
            ModBlocks.MOON_ETRIUM_ORE.get(),
            ModBlocks.MARS_ETRIUM_ORE.get(),
            ModBlocks.GLACIO_ETRIUM_ORE.get()
        );
    }
}
