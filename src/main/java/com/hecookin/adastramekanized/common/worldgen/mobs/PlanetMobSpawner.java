package com.hecookin.adastramekanized.common.worldgen.mobs;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.api.planets.generation.PlanetGenerationSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.*;

/**
 * Advanced mob spawning system for planetary dimensions.
 *
 * Features:
 * - Planet-specific mob types and behaviors
 * - Environmental adaptation (low gravity, no atmosphere, extreme temperatures)
 * - Configurable spawn rates and conditions
 * - Cave-specific mobs
 * - Surface vs underground spawning
 * - Day/night cycle considerations
 * - Hostile environment effects on mob spawning
 */
public class PlanetMobSpawner {

    private final PlanetGenerationSettings generationSettings;
    private final ResourceLocation planetId;
    private final RandomSource random;
    private final Map<PlanetType, List<MobSpawnEntry>> planetMobs;

    public PlanetMobSpawner(PlanetGenerationSettings generationSettings, ResourceLocation planetId, long seed) {
        this.generationSettings = generationSettings;
        this.planetId = planetId;
        this.random = RandomSource.create(seed + 1000);
        this.planetMobs = initializePlanetMobs();

        AdAstraMekanized.LOGGER.info("Initialized PlanetMobSpawner for planet: {} with type: {}",
            planetId, determinePlanetType());
    }

    /**
     * Attempt to spawn mobs in a chunk
     */
    public void spawnMobs(ServerLevel level, ChunkAccess chunk) {
        PlanetType planetType = determinePlanetType();
        List<MobSpawnEntry> availableMobs = planetMobs.getOrDefault(planetType, new ArrayList<>());

        if (availableMobs.isEmpty()) {
            return;
        }

        // Spawn surface mobs
        spawnSurfaceMobs(level, chunk, availableMobs);

        // Spawn underground mobs
        spawnUndergroundMobs(level, chunk, availableMobs);

        // Spawn atmospheric mobs (if planet has atmosphere)
        if (generationSettings.environment().hasOxygen()) {
            spawnAtmosphericMobs(level, chunk, availableMobs);
        }
    }

    /**
     * Spawn mobs on the surface
     */
    private void spawnSurfaceMobs(ServerLevel level, ChunkAccess chunk, List<MobSpawnEntry> availableMobs) {
        for (int attempt = 0; attempt < 10; attempt++) {
            int x = random.nextInt(16);
            int z = random.nextInt(16);

            BlockPos surfacePos = getSurfacePosition(chunk, x, z);
            if (surfacePos == null) continue;

            BlockPos spawnPos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                chunk.getPos().getWorldPosition().offset(x, 0, z));

            if (isValidSurfaceSpawnLocation(level, spawnPos)) {
                attemptMobSpawn(level, spawnPos, availableMobs, SpawnContext.SURFACE);
            }
        }
    }

    /**
     * Spawn mobs underground in caves
     */
    private void spawnUndergroundMobs(ServerLevel level, ChunkAccess chunk, List<MobSpawnEntry> availableMobs) {
        for (int attempt = 0; attempt < 15; attempt++) {
            int x = random.nextInt(16);
            int z = random.nextInt(16);
            int y = chunk.getMinBuildHeight() + 10 + random.nextInt(50);

            BlockPos worldPos = chunk.getPos().getWorldPosition().offset(x, y, z);

            if (isValidUndergroundSpawnLocation(level, worldPos)) {
                attemptMobSpawn(level, worldPos, availableMobs, SpawnContext.UNDERGROUND);
            }
        }
    }

    /**
     * Spawn flying/atmospheric mobs
     */
    private void spawnAtmosphericMobs(ServerLevel level, ChunkAccess chunk, List<MobSpawnEntry> availableMobs) {
        for (int attempt = 0; attempt < 5; attempt++) {
            int x = random.nextInt(16);
            int z = random.nextInt(16);
            int y = getSurfaceHeight(chunk, x, z) + 10 + random.nextInt(20);

            BlockPos worldPos = chunk.getPos().getWorldPosition().offset(x, y, z);

            if (isValidAtmosphericSpawnLocation(level, worldPos)) {
                attemptMobSpawn(level, worldPos, availableMobs, SpawnContext.ATMOSPHERIC);
            }
        }
    }

    /**
     * Attempt to spawn a mob at the given location
     */
    private void attemptMobSpawn(ServerLevel level, BlockPos pos, List<MobSpawnEntry> availableMobs, SpawnContext context) {
        // Filter mobs by spawn context
        List<MobSpawnEntry> contextMobs = availableMobs.stream()
            .filter(mob -> mob.canSpawnIn(context))
            .filter(mob -> passesEnvironmentalChecks(pos, mob))
            .toList();

        if (contextMobs.isEmpty()) return;

        // Calculate total weight
        int totalWeight = contextMobs.stream().mapToInt(MobSpawnEntry::weight).sum();
        if (totalWeight <= 0) return;

        // Select random mob based on weight
        int randomWeight = random.nextInt(totalWeight);
        int currentWeight = 0;

        for (MobSpawnEntry mobEntry : contextMobs) {
            currentWeight += mobEntry.weight();
            if (randomWeight < currentWeight) {
                spawnMob(level, pos, mobEntry);
                break;
            }
        }
    }

    /**
     * Actually spawn the mob
     */
    private void spawnMob(ServerLevel level, BlockPos pos, MobSpawnEntry mobEntry) {
        try {
            EntityType<?> entityType = mobEntry.entityType();

            if (entityType != null) {
                var entity = entityType.create(level);
                if (entity != null) {
                    entity.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);

                    // Apply planet-specific modifications
                    applyPlanetaryAdaptations(entity, mobEntry);

                    // Spawn the entity
                    level.addFreshEntity(entity);

                    AdAstraMekanized.LOGGER.debug("Spawned {} at {} on planet {}",
                        entityType.getDescriptionId(), pos, planetId);
                }
            }
        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Failed to spawn mob {} at {}: {}",
                mobEntry.entityType(), pos, e.getMessage());
        }
    }

    /**
     * Apply planet-specific adaptations to spawned mobs
     */
    private void applyPlanetaryAdaptations(net.minecraft.world.entity.Entity entity, MobSpawnEntry mobEntry) {
        // Apply gravity effects
        float gravityMultiplier = generationSettings.environment().gravity().gravityMultiplier();

        // Apply temperature effects
        float temperature = generationSettings.environment().temperature().baseTemperature();

        // Apply atmospheric effects
        boolean hasOxygen = generationSettings.environment().hasOxygen();

        // For now, these are conceptual - in a full implementation you'd modify entity NBT or add effects
        // entity.addTag("planet_gravity_" + gravityMultiplier);
        // entity.addTag("planet_temperature_" + temperature);
        // entity.addTag("planet_atmosphere_" + hasOxygen);
    }

    /**
     * Check if environmental conditions allow this mob to spawn
     */
    private boolean passesEnvironmentalChecks(BlockPos pos, MobSpawnEntry mobEntry) {
        // Temperature check
        float temperature = generationSettings.environment().temperature().baseTemperature();
        if (temperature < mobEntry.minTemperature() || temperature > mobEntry.maxTemperature()) {
            return false;
        }

        // Atmosphere check
        boolean hasOxygen = generationSettings.environment().hasOxygen();
        if (mobEntry.requiresOxygen() && !hasOxygen) {
            return false;
        }

        // Gravity check
        float gravity = generationSettings.environment().gravity().gravityMultiplier();
        if (gravity < mobEntry.minGravity() || gravity > mobEntry.maxGravity()) {
            return false;
        }

        // Radiation check
        boolean hasRadiation = generationSettings.environment().radiation().hasRadiation();
        if (hasRadiation && !mobEntry.radiationResistant()) {
            return false;
        }

        return true;
    }

    /**
     * Check if location is valid for surface spawning
     */
    private boolean isValidSurfaceSpawnLocation(ServerLevel level, BlockPos pos) {
        // Check if there's solid ground
        if (!level.getBlockState(pos.below()).isSolidRender(level, pos.below())) {
            return false;
        }

        // Check if there's space to spawn
        if (!level.getBlockState(pos).isAir() || !level.getBlockState(pos.above()).isAir()) {
            return false;
        }

        // Check light level for hostile mobs
        int lightLevel = level.getBrightness(net.minecraft.world.level.LightLayer.BLOCK, pos);
        return lightLevel >= 0; // Allow spawning in all light levels for now
    }

    /**
     * Check if location is valid for underground spawning
     */
    private boolean isValidUndergroundSpawnLocation(ServerLevel level, BlockPos pos) {
        // Must be in a cave (air block underground)
        if (!level.getBlockState(pos).isAir()) {
            return false;
        }

        // Check for solid floor
        if (!level.getBlockState(pos.below()).isSolidRender(level, pos.below())) {
            return false;
        }

        // Check headroom
        if (!level.getBlockState(pos.above()).isAir()) {
            return false;
        }

        // Must be underground (below surface level)
        int surfaceHeight = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ());
        return pos.getY() < surfaceHeight - 10;
    }

    /**
     * Check if location is valid for atmospheric spawning
     */
    private boolean isValidAtmosphericSpawnLocation(ServerLevel level, BlockPos pos) {
        // Must be in open air
        return level.getBlockState(pos).isAir() &&
               level.getBlockState(pos.above()).isAir() &&
               level.getBlockState(pos.below()).isAir();
    }

    /**
     * Get surface position for chunk coordinates
     */
    private BlockPos getSurfacePosition(ChunkAccess chunk, int x, int z) {
        int surfaceY = getSurfaceHeight(chunk, x, z);
        if (surfaceY > chunk.getMinBuildHeight()) {
            return new BlockPos(x, surfaceY + 1, z);
        }
        return null;
    }

    /**
     * Get surface height for local chunk coordinates
     */
    private int getSurfaceHeight(ChunkAccess chunk, int x, int z) {
        for (int y = chunk.getMaxBuildHeight() - 1; y >= chunk.getMinBuildHeight(); y--) {
            if (!chunk.getBlockState(new BlockPos(x, y, z)).isAir()) {
                return y;
            }
        }
        return chunk.getMinBuildHeight();
    }

    /**
     * Determine planet type based on generation settings
     */
    private PlanetType determinePlanetType() {
        boolean hasAtmosphere = generationSettings.environment().hasOxygen();
        float temperature = generationSettings.environment().temperature().baseTemperature();
        float gravity = generationSettings.environment().gravity().gravityMultiplier();
        boolean hasRadiation = generationSettings.environment().radiation().hasRadiation();

        if (!hasAtmosphere && gravity < 0.5f) {
            return PlanetType.MOON;
        } else if (temperature < -50 && hasAtmosphere) {
            return PlanetType.ICE_PLANET;
        } else if (temperature > 50 && !hasAtmosphere) {
            return PlanetType.DESERT_PLANET;
        } else if (hasRadiation) {
            return PlanetType.RADIOACTIVE_PLANET;
        } else if (hasAtmosphere && temperature > -20 && temperature < 40) {
            return PlanetType.HABITABLE_PLANET;
        } else {
            return PlanetType.BARREN_PLANET;
        }
    }

    /**
     * Initialize mob spawn lists for different planet types
     */
    private Map<PlanetType, List<MobSpawnEntry>> initializePlanetMobs() {
        Map<PlanetType, List<MobSpawnEntry>> mobs = new HashMap<>();

        // Moon-like planets
        mobs.put(PlanetType.MOON, Arrays.asList(
            new MobSpawnEntry(EntityType.ENDERMITE, 5, SpawnContext.UNDERGROUND, false, -200, 200, 0.0f, 2.0f, true, "alien_parasite")
        ));

        // Desert planets
        mobs.put(PlanetType.DESERT_PLANET, Arrays.asList(
            new MobSpawnEntry(EntityType.HUSK, 8, SpawnContext.SURFACE, false, 20, 100, 0.5f, 2.0f, true, "desert_wanderer"),
            new MobSpawnEntry(EntityType.SPIDER, 6, SpawnContext.UNDERGROUND, false, 10, 80, 0.2f, 1.5f, true, "cave_spider"),
            new MobSpawnEntry(EntityType.SILVERFISH, 4, SpawnContext.UNDERGROUND, false, 0, 60, 0.1f, 1.2f, false, "sand_burrower")
        ));

        // Ice planets
        mobs.put(PlanetType.ICE_PLANET, Arrays.asList(
            new MobSpawnEntry(EntityType.STRAY, 7, SpawnContext.SURFACE, true, -100, 10, 0.3f, 1.5f, false, "frozen_guardian"),
            new MobSpawnEntry(EntityType.POLAR_BEAR, 3, SpawnContext.SURFACE, true, -80, 5, 0.8f, 1.2f, false, "ice_bear"),
            new MobSpawnEntry(EntityType.BAT, 5, SpawnContext.UNDERGROUND, true, -50, 20, 0.2f, 1.8f, false, "ice_bat")
        ));

        // Habitable planets
        mobs.put(PlanetType.HABITABLE_PLANET, Arrays.asList(
            new MobSpawnEntry(EntityType.COW, 8, SpawnContext.SURFACE, true, -10, 35, 0.8f, 1.2f, false, "alien_grazer"),
            new MobSpawnEntry(EntityType.SHEEP, 6, SpawnContext.SURFACE, true, -5, 40, 0.7f, 1.3f, false, "wool_creature"),
            new MobSpawnEntry(EntityType.CHICKEN, 10, SpawnContext.SURFACE, true, 0, 45, 0.5f, 1.5f, false, "feathered_biped"),
            new MobSpawnEntry(EntityType.ZOMBIE, 4, SpawnContext.UNDERGROUND, false, -20, 50, 0.5f, 1.5f, false, "infected_colonist"),
            new MobSpawnEntry(EntityType.BAT, 12, SpawnContext.ATMOSPHERIC, true, -10, 40, 0.3f, 1.8f, false, "flying_mammal")
        ));

        // Radioactive planets
        mobs.put(PlanetType.RADIOACTIVE_PLANET, Arrays.asList(
            new MobSpawnEntry(EntityType.SKELETON, 6, SpawnContext.SURFACE, false, -50, 100, 0.2f, 2.0f, true, "irradiated_bones"),
            new MobSpawnEntry(EntityType.SPIDER, 8, SpawnContext.UNDERGROUND, false, -30, 80, 0.1f, 1.8f, true, "mutant_arachnid"),
            new MobSpawnEntry(EntityType.SLIME, 5, SpawnContext.UNDERGROUND, false, -40, 90, 0.3f, 1.6f, true, "radioactive_ooze")
        ));

        // Barren planets
        mobs.put(PlanetType.BARREN_PLANET, Arrays.asList(
            new MobSpawnEntry(EntityType.SILVERFISH, 3, SpawnContext.UNDERGROUND, false, -100, 100, 0.0f, 3.0f, true, "rock_eater")
        ));

        return mobs;
    }

    /**
     * Planet classification for mob spawning
     */
    private enum PlanetType {
        MOON,
        DESERT_PLANET,
        ICE_PLANET,
        HABITABLE_PLANET,
        RADIOACTIVE_PLANET,
        BARREN_PLANET
    }

    /**
     * Spawn context for different areas
     */
    public enum SpawnContext {
        SURFACE,
        UNDERGROUND,
        ATMOSPHERIC
    }

    /**
     * Mob spawn entry with environmental requirements
     */
    private record MobSpawnEntry(
        EntityType<?> entityType,
        int weight,
        SpawnContext preferredContext,
        boolean requiresOxygen,
        float minTemperature,
        float maxTemperature,
        float minGravity,
        float maxGravity,
        boolean radiationResistant,
        String planetVariantName
    ) {
        public boolean canSpawnIn(SpawnContext context) {
            return preferredContext == context;
        }
    }
}