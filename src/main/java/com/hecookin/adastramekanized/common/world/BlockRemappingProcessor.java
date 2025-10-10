package com.hecookin.adastramekanized.common.world;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Structure processor that remaps blocks from ad_astra namespace to adastramekanized namespace.
 * Used when loading Ad Astra structures into our mod.
 */
public class BlockRemappingProcessor extends StructureProcessor {

    // Simple processor type for runtime-only usage (not serialized to disk)
    private static final StructureProcessorType<BlockRemappingProcessor> TYPE =
        () -> MapCodec.unit(new BlockRemappingProcessor());

    private static final Map<ResourceLocation, ResourceLocation> BLOCK_REMAPPING = new HashMap<>();

    static {
        // Map Ad Astra blocks to our equivalent blocks
        BLOCK_REMAPPING.put(
            ResourceLocation.parse("ad_astra:iron_plating"),
            ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "iron_plating")
        );
        BLOCK_REMAPPING.put(
            ResourceLocation.parse("ad_astra:iron_pillar"),
            ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "iron_pillar")
        );
        BLOCK_REMAPPING.put(
            ResourceLocation.parse("ad_astra:marked_iron_pillar"),
            ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "marked_iron_pillar")
        );
        // Map glowing_iron_pillar to regular iron_pillar (we don't have glowing variant yet)
        BLOCK_REMAPPING.put(
            ResourceLocation.parse("ad_astra:glowing_iron_pillar"),
            ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "iron_pillar")
        );
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return TYPE;
    }

    @Nullable
    @Override
    public StructureTemplate.StructureBlockInfo processBlock(
        LevelReader level,
        BlockPos offset,
        BlockPos pos,
        StructureTemplate.StructureBlockInfo originalBlockInfo,
        StructureTemplate.StructureBlockInfo currentBlockInfo,
        StructurePlaceSettings settings
    ) {
        Block currentBlock = currentBlockInfo.state().getBlock();
        ResourceLocation currentBlockId = BuiltInRegistries.BLOCK.getKey(currentBlock);

        // Check if this block needs remapping
        if (BLOCK_REMAPPING.containsKey(currentBlockId)) {
            ResourceLocation newBlockId = BLOCK_REMAPPING.get(currentBlockId);
            Block newBlock = BuiltInRegistries.BLOCK.get(newBlockId);

            if (newBlock != Blocks.AIR) {
                AdAstraMekanized.LOGGER.debug("Remapping block {} -> {} at {}", currentBlockId, newBlockId, pos);
                return new StructureTemplate.StructureBlockInfo(
                    currentBlockInfo.pos(),
                    newBlock.defaultBlockState(),
                    currentBlockInfo.nbt()
                );
            } else {
                AdAstraMekanized.LOGGER.warn("Failed to remap block {} - target block {} not found!", currentBlockId, newBlockId);
            }
        }

        return currentBlockInfo;
    }
}
