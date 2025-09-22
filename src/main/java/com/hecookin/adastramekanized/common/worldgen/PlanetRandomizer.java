package com.hecookin.adastramekanized.common.worldgen;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.planets.DimensionEffectsType;
import com.hecookin.adastramekanized.common.planets.CelestialType;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Weighted randomization engine for procedural planet generation.
 * Provides deterministic or random selection of planet attributes based on configurable weights.
 */
public class PlanetRandomizer {

    private final Random random;
    private final long seed;
    private final boolean useSeededGeneration;

    /**
     * Create a new randomizer with optional seed
     */
    public PlanetRandomizer(long worldSeed, boolean useSeededGeneration) {
        this.seed = worldSeed;
        this.useSeededGeneration = useSeededGeneration;
        this.random = useSeededGeneration ? new Random(worldSeed) : ThreadLocalRandom.current();

        AdAstraMekanized.LOGGER.debug("PlanetRandomizer initialized with seed: {} (seeded: {})",
            worldSeed, useSeededGeneration);
    }

    /**
     * Create a new randomizer for a specific planet index (deterministic per planet)
     */
    public PlanetRandomizer forPlanet(int planetIndex) {
        if (useSeededGeneration) {
            // Create unique but deterministic seed for each planet
            long planetSeed = seed + (planetIndex * 31L) + 0x5D29C4B1L;
            return new PlanetRandomizer(planetSeed, true);
        } else {
            return this; // Use same random instance for non-seeded generation
        }
    }

    // ========== WEIGHTED SELECTION ALGORITHMS ==========

    /**
     * Select an item from a weighted map using cumulative probability
     */
    public <T> T selectWeighted(Map<T, Float> weightedOptions) {
        if (weightedOptions.isEmpty()) {
            throw new IllegalArgumentException("Cannot select from empty weighted options");
        }

        // Calculate total weight
        float totalWeight = weightedOptions.values().stream()
            .reduce(0.0f, Float::sum);

        if (totalWeight <= 0) {
            throw new IllegalArgumentException("Total weight must be positive, got: " + totalWeight);
        }

        // Generate random value in [0, totalWeight)
        float randomValue = random.nextFloat() * totalWeight;

        // Find the selected item using cumulative weights
        float cumulativeWeight = 0.0f;
        for (Map.Entry<T, Float> entry : weightedOptions.entrySet()) {
            cumulativeWeight += entry.getValue();
            if (randomValue <= cumulativeWeight) {
                return entry.getKey();
            }
        }

        // Fallback (should never happen with proper weights)
        return weightedOptions.keySet().iterator().next();
    }

    /**
     * Select multiple items from a weighted map without replacement
     */
    public <T> List<T> selectMultipleWeighted(Map<T, Float> weightedOptions, int count) {
        if (count <= 0) {
            return Collections.emptyList();
        }

        List<T> selected = new ArrayList<>();
        Map<T, Float> remainingOptions = new HashMap<>(weightedOptions);

        for (int i = 0; i < count && !remainingOptions.isEmpty(); i++) {
            T selectedItem = selectWeighted(remainingOptions);
            selected.add(selectedItem);
            remainingOptions.remove(selectedItem); // Remove to prevent duplicates
        }

        return selected;
    }

    /**
     * Select from a list with equal probability
     */
    public <T> T selectRandom(List<T> options) {
        if (options.isEmpty()) {
            throw new IllegalArgumentException("Cannot select from empty list");
        }
        return options.get(random.nextInt(options.size()));
    }

    /**
     * Select multiple items from a list without replacement
     */
    public <T> List<T> selectMultipleRandom(List<T> options, int count) {
        if (count <= 0) {
            return Collections.emptyList();
        }

        List<T> shuffled = new ArrayList<>(options);
        Collections.shuffle(shuffled, random);
        return shuffled.subList(0, Math.min(count, shuffled.size()));
    }

    // ========== PLANET ATTRIBUTE GENERATION ==========

    /**
     * Generate random dimension effects type based on configured weights
     */
    public DimensionEffectsType generateEffectsType() {
        return selectWeighted(PlanetGenerationConfig.EFFECTS_TYPE_WEIGHTS);
    }

    /**
     * Generate random celestial type based on configured weights
     */
    public CelestialType generateCelestialType() {
        return selectWeighted(PlanetGenerationConfig.CELESTIAL_TYPE_WEIGHTS);
    }

    /**
     * Generate random gravity within configured range
     */
    public float generateGravity() {
        return randomFloat(PlanetGenerationConfig.MIN_GRAVITY, PlanetGenerationConfig.MAX_GRAVITY);
    }

    /**
     * Generate random temperature within configured range
     */
    public float generateTemperature() {
        return randomFloat(PlanetGenerationConfig.MIN_TEMPERATURE, PlanetGenerationConfig.MAX_TEMPERATURE);
    }

    /**
     * Generate random day length within configured range
     */
    public float generateDayLength() {
        return randomFloat(PlanetGenerationConfig.MIN_DAY_LENGTH, PlanetGenerationConfig.MAX_DAY_LENGTH);
    }

    /**
     * Generate random orbit distance within configured range
     */
    public int generateOrbitDistance() {
        return randomInt(PlanetGenerationConfig.MIN_PLANET_DISTANCE, PlanetGenerationConfig.MAX_ORBIT_DISTANCE);
    }

    /**
     * Generate random atmosphere pressure within configured range
     */
    public float generateAtmospherePressure() {
        return randomFloat(PlanetGenerationConfig.MIN_ATMOSPHERE_PRESSURE, PlanetGenerationConfig.MAX_ATMOSPHERE_PRESSURE);
    }

    // ========== ATMOSPHERE GENERATION ==========

    /**
     * Generate atmosphere properties based on configured chances
     */
    public AtmosphereProperties generateAtmosphere() {
        float roll = random.nextFloat() * 100.0f;
        float cumulative = 0.0f;

        cumulative += PlanetGenerationConfig.BREATHABLE_ATMOSPHERE_CHANCE;
        if (roll <= cumulative) {
            return new AtmosphereProperties(true, true, generateAtmospherePressure());
        }

        cumulative += PlanetGenerationConfig.TOXIC_ATMOSPHERE_CHANCE;
        if (roll <= cumulative) {
            return new AtmosphereProperties(true, false, generateAtmospherePressure());
        }

        cumulative += PlanetGenerationConfig.THIN_ATMOSPHERE_CHANCE;
        if (roll <= cumulative) {
            float thinPressure = randomFloat(0.1f, 0.5f); // Thin atmosphere
            return new AtmosphereProperties(true, false, thinPressure);
        }

        // No atmosphere
        return new AtmosphereProperties(false, false, 0.0f);
    }

    // ========== ORE GENERATION ==========

    /**
     * Generate ore configuration for a planet based on chances and weights
     */
    public List<OreSpawnConfig> generateOreConfiguration() {
        List<OreSpawnConfig> oreConfigs = new ArrayList<>();

        // Process each ore category
        addOresIfChance(oreConfigs, getCommonOres(), PlanetGenerationConfig.COMMON_ORE_CHANCE);
        addOresIfChance(oreConfigs, getUncommonOres(), PlanetGenerationConfig.UNCOMMON_ORE_CHANCE);
        addOresIfChance(oreConfigs, getRareOres(), PlanetGenerationConfig.RARE_ORE_CHANCE);
        addOresIfChance(oreConfigs, getModOres(), PlanetGenerationConfig.MOD_ORE_CHANCE);

        return oreConfigs;
    }

    private void addOresIfChance(List<OreSpawnConfig> oreConfigs, List<String> oreTypes, float chance) {
        if (random.nextFloat() * 100.0f <= chance) {
            for (String oreType : oreTypes) {
                PlanetGenerationConfig.OreConfig config = PlanetGenerationConfig.ORE_CONFIGURATIONS.get(oreType);
                if (config != null) {
                    int veinSize = randomInt(config.minVeinSize(), config.maxVeinSize());
                    int veinsPerChunk = randomInt(config.minVeinsPerChunk(), config.maxVeinsPerChunk());
                    oreConfigs.add(new OreSpawnConfig(oreType, veinSize, veinsPerChunk, config.weight()));
                }
            }
        }
    }

    private List<String> getCommonOres() {
        return Arrays.asList("minecraft:iron_ore", "minecraft:copper_ore", "minecraft:coal_ore");
    }

    private List<String> getUncommonOres() {
        return Arrays.asList("minecraft:gold_ore", "minecraft:redstone_ore", "minecraft:lapis_ore");
    }

    private List<String> getRareOres() {
        return Arrays.asList("minecraft:diamond_ore", "minecraft:emerald_ore");
    }

    private List<String> getModOres() {
        return Arrays.asList("adastramekanized:desh_ore", "adastramekanized:ostrum_ore");
    }

    // ========== MOB GENERATION ==========

    /**
     * Generate mob spawning configuration for a planet
     */
    public MobSpawnConfig generateMobConfiguration() {
        float roll = random.nextFloat() * 100.0f;
        float spawnRate = randomFloat(PlanetGenerationConfig.MIN_MOB_SPAWN_RATE,
                                    PlanetGenerationConfig.MAX_MOB_SPAWN_RATE);

        if (roll <= PlanetGenerationConfig.HOSTILE_MOBS_CHANCE) {
            // Hostile planet
            List<String> selectedMobs = selectMultipleRandom(PlanetGenerationConfig.HOSTILE_MOB_TYPES,
                randomInt(1, 3)); // 1-3 hostile mob types
            return new MobSpawnConfig(MobType.HOSTILE, selectedMobs, spawnRate);
        }

        roll -= PlanetGenerationConfig.HOSTILE_MOBS_CHANCE;
        if (roll <= PlanetGenerationConfig.PASSIVE_MOBS_CHANCE) {
            // Peaceful planet
            List<String> selectedMobs = selectMultipleRandom(PlanetGenerationConfig.PASSIVE_MOB_TYPES,
                randomInt(1, 4)); // 1-4 passive mob types
            return new MobSpawnConfig(MobType.PASSIVE, selectedMobs, spawnRate);
        }

        // No mobs (lifeless planet)
        return new MobSpawnConfig(MobType.NONE, Collections.emptyList(), 0.0f);
    }

    // ========== UTILITY METHODS ==========

    /**
     * Generate random float within range [min, max]
     */
    public float randomFloat(float min, float max) {
        return min + random.nextFloat() * (max - min);
    }

    /**
     * Generate random int within range [min, max] (inclusive)
     */
    public int randomInt(int min, int max) {
        return min + random.nextInt(max - min + 1);
    }

    /**
     * Generate random boolean with given probability
     */
    public boolean randomBoolean(float probability) {
        return random.nextFloat() <= probability;
    }

    /**
     * Get the current random instance (for external use if needed)
     */
    public Random getRandom() {
        return random;
    }

    // ========== CONFIGURATION RECORDS ==========

    /**
     * Atmosphere properties for a generated planet
     */
    public record AtmosphereProperties(
        boolean hasAtmosphere,
        boolean breathable,
        float pressure
    ) {}

    /**
     * Ore spawn configuration for a specific ore type
     */
    public record OreSpawnConfig(
        String oreType,         // e.g., "minecraft:iron_ore"
        int veinSize,          // Size of ore veins
        int veinsPerChunk,     // Number of veins per chunk
        float weight           // Relative spawn weight
    ) {}

    /**
     * Mob spawning configuration for a planet
     */
    public record MobSpawnConfig(
        MobType mobType,        // Type of mobs on this planet
        List<String> mobTypes,  // Specific mob entity types
        float spawnRate         // Spawn rate multiplier
    ) {}

    /**
     * Mob presence categories
     */
    public enum MobType {
        HOSTILE,    // Dangerous mobs spawn
        PASSIVE,    // Peaceful mobs spawn
        NONE        // No mobs spawn (lifeless planet)
    }
}