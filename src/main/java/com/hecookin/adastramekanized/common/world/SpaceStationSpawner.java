package com.hecookin.adastramekanized.common.world;

import com.hecookin.adastramekanized.AdAstraMekanized;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

/**
 * Handles automatic space station structure placement in Earth's Orbit dimension.
 * Spawns a single space station at origin (0, 100, 0) when the dimension is first accessed.
 * Blocks are automatically remapped from ad_astra namespace to adastramekanized.
 */
public class SpaceStationSpawner extends SavedData {

    private static final String DATA_NAME = AdAstraMekanized.MOD_ID + "_space_station_spawned";
    private static final ResourceLocation EARTH_ORBIT_DIM =
        ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "earth_orbit");
    private static final ResourceLocation SPACE_STATION_STRUCTURE =
        ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "space_station");

    // Station spawn coordinates (centered at origin, Y=100)
    private static final BlockPos STATION_CENTER = new BlockPos(0, 100, 0);

    private boolean stationSpawned = false;

    public SpaceStationSpawner() {
        super();
    }

    public static SpaceStationSpawner load(CompoundTag tag, HolderLookup.Provider registries) {
        SpaceStationSpawner spawner = new SpaceStationSpawner();
        spawner.stationSpawned = tag.getBoolean("StationSpawned");
        return spawner;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putBoolean("StationSpawned", stationSpawned);
        return tag;
    }

    public static SpaceStationSpawner get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
            new SavedData.Factory<>(SpaceStationSpawner::new, SpaceStationSpawner::load),
            DATA_NAME
        );
    }

    /**
     * Reset the spawn tracker to allow structures to spawn again.
     * Useful for debugging or if structures failed to spawn.
     */
    public static void resetSpawnTracker(ServerLevel level) {
        SpaceStationSpawner spawner = get(level);
        spawner.stationSpawned = false;
        spawner.setDirty();
        AdAstraMekanized.LOGGER.info("Reset space station spawn tracker for dimension: {}", level.dimension().location());
    }

    /**
     * Attempts to spawn the space station if it hasn't been spawned yet.
     * Should be called when the dimension first loads or when a player enters.
     */
    public static void trySpawnStation(ServerLevel level) {
        // Only spawn in Earth's Orbit dimension
        if (!level.dimension().location().equals(EARTH_ORBIT_DIM)) {
            AdAstraMekanized.LOGGER.debug("Not Earth's Orbit dimension, skipping spawn");
            return;
        }

        SpaceStationSpawner spawner = get(level);
        if (spawner.stationSpawned) {
            AdAstraMekanized.LOGGER.debug("Space station already spawned, skipping");
            return;
        }

        AdAstraMekanized.LOGGER.info("Starting space station spawn sequence...");

        try {
            // === SPAWN MAIN SPACE STATION ===
            AdAstraMekanized.LOGGER.info("Loading space station structure: {}", SPACE_STATION_STRUCTURE);

            // Load space station structure
            StructureTemplate stationStructure = level.getStructureManager().getOrCreate(SPACE_STATION_STRUCTURE);
            if (stationStructure == null) {
                AdAstraMekanized.LOGGER.error("Failed to load space station structure: {}", SPACE_STATION_STRUCTURE);
                return;
            }

            // Check if structure has valid size
            if (stationStructure.getSize().getX() == 0 || stationStructure.getSize().getY() == 0 || stationStructure.getSize().getZ() == 0) {
                AdAstraMekanized.LOGGER.error("Space station structure has invalid size (0x0x0)! The NBT file may be corrupted or incompatible.");
                AdAstraMekanized.LOGGER.error("Trying to locate structure file to verify it exists...");

                // Try to find the actual file
                try {
                    var resourcePath = level.getServer().getResourceManager().getResource(
                        ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "structures/space_station.nbt")
                    );
                    AdAstraMekanized.LOGGER.info("Structure file found in resources: {}", resourcePath.isPresent());
                } catch (Exception e) {
                    AdAstraMekanized.LOGGER.error("Error checking structure file", e);
                }
                return;
            }

            // Calculate position (center structure at origin)
            BlockPos stationPos = STATION_CENTER.offset(
                -stationStructure.getSize().getX() / 2,
                0,
                -stationStructure.getSize().getZ() / 2
            );

            AdAstraMekanized.LOGGER.info("Spawning space station at {} (size: {})", stationPos, stationStructure.getSize());

            // Add chunk ticket to keep area loaded during placement
            ChunkPos centerChunk = new ChunkPos(stationPos);
            level.getChunkSource().addRegionTicket(TicketType.PORTAL, centerChunk, 5, stationPos);

            // Place main station with block remapping
            StructurePlaceSettings settings = new StructurePlaceSettings();
            settings.addProcessor(new BlockRemappingProcessor());
            stationStructure.placeInWorld(level, stationPos, stationPos, settings, level.random, 2);

            // Protect all space station blocks
            SpaceStationBlockProtection protection = SpaceStationBlockProtection.get(level);
            int blocksProtected = 0;

            // Iterate through structure and protect all non-air blocks
            for (StructureTemplate.StructureBlockInfo blockInfo : stationStructure.filterBlocks(stationPos, settings, net.minecraft.world.level.block.Blocks.STRUCTURE_VOID)) {
                BlockPos blockPos = blockInfo.pos();
                if (!level.getBlockState(blockPos).isAir()) {
                    protection.protectBlock(blockPos);
                    blocksProtected++;
                }
            }

            AdAstraMekanized.LOGGER.info("Protected {} space station blocks from destruction", blocksProtected);

            // Mark as spawned and save
            spawner.stationSpawned = true;
            spawner.setDirty();

            AdAstraMekanized.LOGGER.info("Space station successfully spawned in Earth's Orbit at {}", stationPos);

        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Failed to spawn space station", e);
        }
    }
}
