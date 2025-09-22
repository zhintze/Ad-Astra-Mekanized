package com.hecookin.adastramekanized.common.worldgen;

import com.hecookin.adastramekanized.common.planets.DimensionEffectsType;
import com.hecookin.adastramekanized.common.planets.CelestialType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;

import java.util.*;

/**
 * Central configuration for procedural planet generation at world start.
 * All planet attributes, weights, and options are configurable here.
 */
public class PlanetGenerationConfig {

    // ========== BASIC PLANET SETTINGS ==========

    /** Number of random planets to generate per world */
    public static int PLANET_COUNT = 10;

    /** Use world seed for deterministic generation */
    public static boolean USE_SEED_BASED_GENERATION = true;

    /** Minimum safe distance between planets (in millions of km) */
    public static int MIN_PLANET_DISTANCE = 50;

    /** Maximum orbit distance (in millions of km) */
    public static int MAX_ORBIT_DISTANCE = 2000;

    // ========== DIMENSION EFFECTS DISTRIBUTION ==========

    /** Weights for different dimension effect types (higher = more likely) */
    public static final Map<DimensionEffectsType, Float> EFFECTS_TYPE_WEIGHTS = Map.of(
        DimensionEffectsType.ROCKY, 35.0f,           // Mars-like worlds
        DimensionEffectsType.MOON_LIKE, 25.0f,       // Airless rocky worlds
        DimensionEffectsType.ICE_WORLD, 20.0f,       // Frozen worlds
        DimensionEffectsType.VOLCANIC, 15.0f,        // Hot lava worlds
        DimensionEffectsType.GAS_GIANT, 5.0f         // Rare gas giants
    );

    // ========== CELESTIAL BODY DISTRIBUTION ==========

    /** Weights for different celestial configurations */
    public static final Map<CelestialType, Float> CELESTIAL_TYPE_WEIGHTS = Map.of(
        CelestialType.SINGLE_SUN, 40.0f,             // Most common
        CelestialType.SUN_AND_EARTH, 25.0f,          // Earth visible
        CelestialType.TWO_MOONS, 20.0f,              // Dual moon system
        CelestialType.BINARY_STAR, 10.0f,            // Binary star system
        CelestialType.RING_SYSTEM, 5.0f              // Rare ring system
    );

    // ========== PHYSICAL ATTRIBUTE RANGES ==========

    /** Gravity multiplier ranges (1.0 = Earth gravity) */
    public static final float MIN_GRAVITY = 0.1f;
    public static final float MAX_GRAVITY = 3.0f;

    /** Temperature ranges in Celsius */
    public static final float MIN_TEMPERATURE = -200.0f;
    public static final float MAX_TEMPERATURE = 500.0f;

    /** Day length ranges in hours */
    public static final float MIN_DAY_LENGTH = 6.0f;
    public static final float MAX_DAY_LENGTH = 72.0f;

    /** Atmosphere pressure ranges (1.0 = Earth atmosphere) */
    public static final float MIN_ATMOSPHERE_PRESSURE = 0.0f;
    public static final float MAX_ATMOSPHERE_PRESSURE = 5.0f;

    // ========== ORE GENERATION CONFIGURATION ==========

    /** Available ores with generation weights and vein parameters */
    public static final Map<String, OreConfig> ORE_CONFIGURATIONS = Map.of(
        // Common ores
        "minecraft:iron_ore", new OreConfig(30.0f, 8, 20, 5, 15),
        "minecraft:copper_ore", new OreConfig(25.0f, 6, 16, 4, 12),
        "minecraft:coal_ore", new OreConfig(35.0f, 10, 25, 6, 18),

        // Uncommon ores
        "minecraft:gold_ore", new OreConfig(8.0f, 4, 12, 2, 6),
        "minecraft:redstone_ore", new OreConfig(12.0f, 5, 14, 3, 8),
        "minecraft:lapis_ore", new OreConfig(6.0f, 3, 10, 2, 5),

        // Rare ores
        "minecraft:diamond_ore", new OreConfig(2.0f, 2, 8, 1, 3),
        "minecraft:emerald_ore", new OreConfig(1.0f, 1, 6, 1, 2),

        // Mod-specific ores (TODO: Add Ad Astra/Mekanism ores)
        "adastramekanized:desh_ore", new OreConfig(15.0f, 6, 18, 3, 10),
        "adastramekanized:ostrum_ore", new OreConfig(5.0f, 3, 12, 2, 6)
        // TODO: Add more mod ores as they're implemented
    );

    /** Percentage chance each planet has specific ore types */
    public static final float COMMON_ORE_CHANCE = 90.0f;      // Iron, copper, coal
    public static final float UNCOMMON_ORE_CHANCE = 60.0f;    // Gold, redstone, lapis
    public static final float RARE_ORE_CHANCE = 25.0f;        // Diamond, emerald
    public static final float MOD_ORE_CHANCE = 40.0f;         // Mod-specific ores

    // ========== MOB SPAWNING CONFIGURATION ==========

    /** Distribution of mob presence on planets */
    public static final float HOSTILE_MOBS_CHANCE = 45.0f;    // Dangerous planets
    public static final float PASSIVE_MOBS_CHANCE = 30.0f;    // Peaceful planets
    public static final float NO_MOBS_CHANCE = 25.0f;         // Lifeless planets

    /** Available hostile mobs for planets */
    public static final List<String> HOSTILE_MOB_TYPES = Arrays.asList(
        "minecraft:zombie",
        "minecraft:skeleton",
        "minecraft:spider",
        "minecraft:creeper",
        "minecraft:enderman"
        // TODO: Add space-themed hostile mobs
    );

    /** Available passive mobs for planets */
    public static final List<String> PASSIVE_MOB_TYPES = Arrays.asList(
        "minecraft:cow",
        "minecraft:sheep",
        "minecraft:pig",
        "minecraft:chicken"
        // TODO: Add space-themed passive mobs
    );

    /** Mob spawn density multipliers */
    public static final float MIN_MOB_SPAWN_RATE = 0.5f;
    public static final float MAX_MOB_SPAWN_RATE = 2.0f;

    // ========== NAME GENERATION COMPONENTS ==========

    /** Scientific/space exploration inspired prefixes */
    public static final List<String> NAME_PREFIXES = Arrays.asList(
        // Real space missions/telescopes
        "Kepler", "Hubble", "Webb", "Spitzer", "Cassini", "Voyager", "Pioneer",
        "Galileo", "Juno", "Perseverance", "Curiosity", "Opportunity", "Spirit",

        // Astronomical objects
        "Proxima", "Centauri", "Vega", "Altair", "Rigel", "Betelgeuse", "Sirius",
        "Polaris", "Arcturus", "Capella", "Aldebaran", "Antares",

        // Sci-fi inspired
        "Nova", "Stellar", "Cosmic", "Nebula", "Quantum", "Photon", "Ion",
        "Plasma", "Fusion", "Solar", "Lunar", "Terra", "Astro", "Galactic",

        // Explorer/scientist names
        "Newton", "Einstein", "Hawking", "Sagan", "Tycho", "Copernicus",
        "Galilei", "Tesla", "Darwin", "Mendel", "Curie", "Faraday"
    );

    /** Number/designation patterns */
    public static final List<String> NUMBER_PATTERNS = Arrays.asList(
        // Standard patterns
        "-%d", "-%03d", " %d", " %02d",

        // Letter combinations
        "-%da", "-%db", "-%dc", "-%dd", "-%de", "-%df",
        " Prime", " Alpha", " Beta", " Gamma", " Delta", " Epsilon"
    );

    /** Suffix modifiers for variety */
    public static final List<String> NAME_SUFFIXES = Arrays.asList(
        "", " Major", " Minor", " North", " South", " Prime", " Secondary",
        " A", " B", " C", " I", " II", " III", " IV", " V"
    );

    // ========== ATMOSPHERE CONFIGURATION ==========

    /** Atmosphere composition chances */
    public static final float BREATHABLE_ATMOSPHERE_CHANCE = 15.0f;  // Rare Earth-like
    public static final float TOXIC_ATMOSPHERE_CHANCE = 35.0f;       // Dangerous but present
    public static final float THIN_ATMOSPHERE_CHANCE = 30.0f;        // Mars-like
    public static final float NO_ATMOSPHERE_CHANCE = 20.0f;          // Moon-like

    // ========== CONFIGURATION RECORDS ==========

    /**
     * Configuration for ore generation on planets
     */
    public record OreConfig(
        float weight,           // Selection weight vs other ores
        int minVeinSize,        // Minimum vein size
        int maxVeinSize,        // Maximum vein size
        int minVeinsPerChunk,   // Minimum veins per chunk
        int maxVeinsPerChunk    // Maximum veins per chunk
    ) {}

    // ========== VALIDATION METHODS ==========

    /**
     * Validate that all configuration weights sum to reasonable values
     */
    public static boolean validateConfiguration() {
        // Validate effects weights
        float totalEffectsWeight = EFFECTS_TYPE_WEIGHTS.values().stream()
            .reduce(0.0f, Float::sum);
        if (totalEffectsWeight <= 0) {
            return false;
        }

        // Validate celestial weights
        float totalCelestialWeight = CELESTIAL_TYPE_WEIGHTS.values().stream()
            .reduce(0.0f, Float::sum);
        if (totalCelestialWeight <= 0) {
            return false;
        }

        // Validate mob chances
        float totalMobChances = HOSTILE_MOBS_CHANCE + PASSIVE_MOBS_CHANCE + NO_MOBS_CHANCE;
        if (Math.abs(totalMobChances - 100.0f) > 0.1f) {
            return false;
        }

        return true;
    }

    // ========== TODO: FUTURE ENHANCEMENTS ==========

    // TODO: Add biome-specific configurations
    // TODO: Add structure generation weights (villages, dungeons, etc.)
    // TODO: Add weather pattern configurations
    // TODO: Add resource scarcity/abundance modifiers
    // TODO: Add planet age/maturity factors affecting terrain
    // TODO: Add gravitational effects on terrain generation
    // TODO: Add seasonal variation configurations
    // TODO: Add technological artifact spawn chances
    // TODO: Add inter-planetary relationship configurations (trade routes, etc.)
    // TODO: Add planet classification system (habitable, resource-rich, dangerous, etc.)
}