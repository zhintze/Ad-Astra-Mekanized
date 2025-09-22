package com.hecookin.adastramekanized.common.planets;

import com.hecookin.adastramekanized.AdAstraMekanized;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * Static definitions for all 50 predefined planets.
 * Each planet has unique characteristics, seeds, and generation settings.
 */
public class StaticPlanetDefinitions {

    /**
     * Planet categories for organization and shared characteristics
     */
    public enum PlanetType {
        ROCKY_WORLD,     // Mars-like planets with varied terrain
        ICE_WORLD,       // Frozen planets with ice formations
        HOT_WORLD,       // Venus-like high temperature planets
        AIRLESS_BODY,    // Moon-like worlds without atmosphere
        EXTREME_WORLD,   // Mercury-like extreme conditions
        GAS_GIANT_MOON   // Moons orbiting gas giants
    }

    /**
     * Individual planet definition with all properties needed for generation
     */
    public static class PlanetDefinition {
        public final String name;
        public final String displayName;
        public final PlanetType type;
        public final long seed;
        public final float gravity;
        public final float temperature;
        public final boolean hasAtmosphere;
        public final boolean breathable;
        public final String defaultBlock;
        public final String surfaceBlock;
        public final List<String> ores;
        public final List<String> mobs;
        public final int skyColor;
        public final int fogColor;

        public PlanetDefinition(String name, String displayName, PlanetType type, long seed,
                               float gravity, float temperature, boolean hasAtmosphere, boolean breathable,
                               String defaultBlock, String surfaceBlock, List<String> ores, List<String> mobs,
                               int skyColor, int fogColor) {
            this.name = name;
            this.displayName = displayName;
            this.type = type;
            this.seed = seed;
            this.gravity = gravity;
            this.temperature = temperature;
            this.hasAtmosphere = hasAtmosphere;
            this.breathable = breathable;
            this.defaultBlock = defaultBlock;
            this.surfaceBlock = surfaceBlock;
            this.ores = ores;
            this.mobs = mobs;
            this.skyColor = skyColor;
            this.fogColor = fogColor;
        }

        public ResourceLocation getId() {
            return ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, name);
        }
    }

    /**
     * All 50 predefined planets with unique characteristics
     */
    public static final List<PlanetDefinition> ALL_PLANETS = List.of(
        // Rocky Worlds (15 planets)
        new PlanetDefinition("kepler_442b", "Kepler-442b", PlanetType.ROCKY_WORLD, 442442442L,
            0.8f, 15.0f, true, false, "minecraft:stone", "minecraft:grass_block",
            List.of("minecraft:iron_ore", "minecraft:copper_ore", "minecraft:gold_ore"),
            List.of("minecraft:zombie", "minecraft:skeleton"), 0x87CEEB, 0x87CEEB),

        new PlanetDefinition("proxima_centauri_b", "Proxima-Centauri-b", PlanetType.ROCKY_WORLD, 123456789L,
            1.17f, -40.0f, true, false, "minecraft:deepslate", "minecraft:coarse_dirt",
            List.of("minecraft:iron_ore", "minecraft:redstone_ore", "minecraft:diamond_ore"),
            List.of("minecraft:spider", "minecraft:creeper"), 0x8B0000, 0x8B0000),

        new PlanetDefinition("trappist_1e", "TRAPPIST-1e", PlanetType.ROCKY_WORLD, 987654321L,
            0.91f, 5.0f, true, false, "minecraft:andesite", "minecraft:podzol",
            List.of("minecraft:coal_ore", "minecraft:iron_ore", "minecraft:lapis_ore"),
            List.of("minecraft:enderman", "minecraft:witch"), 0x4B0082, 0x4B0082),

        new PlanetDefinition("gliese_667cc", "Gliese-667Cc", PlanetType.ROCKY_WORLD, 667667667L,
            1.4f, 25.0f, true, false, "minecraft:granite", "minecraft:red_sand",
            List.of("minecraft:gold_ore", "minecraft:emerald_ore", "minecraft:copper_ore"),
            List.of("minecraft:husk", "minecraft:stray"), 0xFF4500, 0xFF4500),

        new PlanetDefinition("hd_40307g", "HD-40307g", PlanetType.ROCKY_WORLD, 403074030L,
            2.3f, -15.0f, true, false, "minecraft:diorite", "minecraft:gravel",
            List.of("minecraft:iron_ore", "minecraft:coal_ore", "minecraft:redstone_ore"),
            List.of("minecraft:zombie_villager", "minecraft:phantom"), 0x2F4F4F, 0x2F4F4F),

        new PlanetDefinition("wolf_1061c", "Wolf-1061c", PlanetType.ROCKY_WORLD, 106110611L,
            1.4f, -20.0f, true, false, "minecraft:blackstone", "minecraft:soul_sand",
            List.of("minecraft:gold_ore", "minecraft:diamond_ore", "minecraft:lapis_ore"),
            List.of("minecraft:wither_skeleton", "minecraft:blaze"), 0x000000, 0x000000),

        new PlanetDefinition("k2_18b", "K2-18b", PlanetType.ROCKY_WORLD, 218218218L,
            2.2f, 0.0f, true, false, "minecraft:tuff", "minecraft:mud",
            List.of("minecraft:copper_ore", "minecraft:iron_ore", "minecraft:coal_ore"),
            List.of("minecraft:drowned", "minecraft:guardian"), 0x008B8B, 0x008B8B),

        new PlanetDefinition("ross_128b", "Ross-128b", PlanetType.ROCKY_WORLD, 128128128L,
            1.35f, -60.0f, true, false, "minecraft:calcite", "minecraft:powder_snow",
            List.of("minecraft:iron_ore", "minecraft:redstone_ore", "minecraft:emerald_ore"),
            List.of("minecraft:polar_bear", "minecraft:fox"), 0xFFFFFF, 0xFFFFFF),

        new PlanetDefinition("lhs_1140b", "LHS-1140b", PlanetType.ROCKY_WORLD, 114011401L,
            6.6f, -80.0f, false, false, "minecraft:basalt", "minecraft:blackstone",
            List.of("minecraft:diamond_ore", "minecraft:gold_ore", "minecraft:redstone_ore"),
            List.of("minecraft:magma_cube", "minecraft:ghast"), 0x8B008B, 0x8B008B),

        new PlanetDefinition("tau_ceti_e", "Tau-Ceti-e", PlanetType.ROCKY_WORLD, 828282828L,
            4.3f, 68.0f, true, false, "minecraft:sandstone", "minecraft:sand",
            List.of("minecraft:gold_ore", "minecraft:copper_ore", "minecraft:lapis_ore"),
            List.of("minecraft:husk", "minecraft:spider"), 0xFFD700, 0xFFD700),

        new PlanetDefinition("kepler_438b", "Kepler-438b", PlanetType.ROCKY_WORLD, 438438438L,
            1.4f, 3.0f, true, false, "minecraft:smooth_stone", "minecraft:dirt",
            List.of("minecraft:iron_ore", "minecraft:coal_ore", "minecraft:copper_ore"),
            List.of("minecraft:zombie", "minecraft:skeleton"), 0x90EE90, 0x90EE90),

        new PlanetDefinition("kepler_296e", "Kepler-296e", PlanetType.ROCKY_WORLD, 296296296L,
            1.5f, 12.0f, true, false, "minecraft:cobblestone", "minecraft:moss_block",
            List.of("minecraft:emerald_ore", "minecraft:diamond_ore", "minecraft:gold_ore"),
            List.of("minecraft:villager", "minecraft:iron_golem"), 0x228B22, 0x228B22),

        new PlanetDefinition("gliese_832c", "Gliese-832c", PlanetType.ROCKY_WORLD, 832832832L,
            5.4f, 22.0f, true, false, "minecraft:terracotta", "minecraft:clay",
            List.of("minecraft:copper_ore", "minecraft:iron_ore", "minecraft:redstone_ore"),
            List.of("minecraft:creeper", "minecraft:enderman"), 0xDEB887, 0xDEB887),

        new PlanetDefinition("kepler_62f", "Kepler-62f", PlanetType.ROCKY_WORLD, 626262626L,
            2.8f, -10.0f, true, false, "minecraft:prismarine", "minecraft:packed_ice",
            List.of("minecraft:lapis_ore", "minecraft:diamond_ore", "minecraft:iron_ore"),
            List.of("minecraft:guardian", "minecraft:squid"), 0x4169E1, 0x4169E1),

        new PlanetDefinition("kepler_452b", "Kepler-452b", PlanetType.ROCKY_WORLD, 452452452L,
            5.0f, 30.0f, true, false, "minecraft:red_sandstone", "minecraft:red_sand",
            List.of("minecraft:gold_ore", "minecraft:redstone_ore", "minecraft:copper_ore"),
            List.of("minecraft:husk", "minecraft:spider"), 0xCD853F, 0xCD853F),

        // Ice Worlds (10 planets)
        new PlanetDefinition("europa", "Europa", PlanetType.ICE_WORLD, 111111111L,
            1.314f, -180.0f, false, false, "minecraft:ice", "minecraft:packed_ice",
            List.of("minecraft:iron_ore", "minecraft:diamond_ore"),
            List.of("minecraft:polar_bear", "minecraft:stray"), 0xE0FFFF, 0xE0FFFF),

        new PlanetDefinition("enceladus", "Enceladus", PlanetType.ICE_WORLD, 222222222L,
            1.13f, -201.0f, false, false, "minecraft:blue_ice", "minecraft:snow_block",
            List.of("minecraft:lapis_ore", "minecraft:iron_ore"),
            List.of("minecraft:snow_golem", "minecraft:fox"), 0xB0E0E6, 0xB0E0E6),

        new PlanetDefinition("titan", "Titan", PlanetType.ICE_WORLD, 333333333L,
            1.35f, -179.0f, true, false, "minecraft:packed_ice", "minecraft:powder_snow",
            List.of("minecraft:coal_ore", "minecraft:redstone_ore"),
            List.of("minecraft:witch", "minecraft:phantom"), 0xFFA500, 0xFFA500),

        new PlanetDefinition("ganymede", "Ganymede", PlanetType.ICE_WORLD, 444444444L,
            1.428f, -183.0f, false, false, "minecraft:ice", "minecraft:frosted_ice",
            List.of("minecraft:iron_ore", "minecraft:gold_ore"),
            List.of("minecraft:zombie", "minecraft:skeleton"), 0x778899, 0x778899),

        new PlanetDefinition("callisto", "Callisto", PlanetType.ICE_WORLD, 555555555L,
            1.235f, -193.0f, false, false, "minecraft:blue_ice", "minecraft:ice",
            List.of("minecraft:diamond_ore", "minecraft:emerald_ore"),
            List.of("minecraft:stray", "minecraft:polar_bear"), 0x696969, 0x696969),

        new PlanetDefinition("triton", "Triton", PlanetType.ICE_WORLD, 666666666L,
            0.779f, -235.0f, true, false, "minecraft:packed_ice", "minecraft:blue_ice",
            List.of("minecraft:lapis_ore", "minecraft:copper_ore"),
            List.of("minecraft:enderman", "minecraft:phantom"), 0xFFB6C1, 0xFFB6C1),

        new PlanetDefinition("pluto", "Pluto", PlanetType.ICE_WORLD, 777777777L,
            0.658f, -230.0f, true, false, "minecraft:snow_block", "minecraft:powder_snow",
            List.of("minecraft:iron_ore", "minecraft:coal_ore"),
            List.of("minecraft:husk", "minecraft:spider"), 0xDDA0DD, 0xDDA0DD),

        new PlanetDefinition("charon", "Charon", PlanetType.ICE_WORLD, 888888888L,
            0.288f, -220.0f, false, false, "minecraft:ice", "minecraft:packed_ice",
            List.of("minecraft:redstone_ore", "minecraft:gold_ore"),
            List.of("minecraft:skeleton", "minecraft:stray"), 0x708090, 0x708090),

        new PlanetDefinition("miranda", "Miranda", PlanetType.ICE_WORLD, 999999999L,
            0.0794f, -213.0f, false, false, "minecraft:blue_ice", "minecraft:ice",
            List.of("minecraft:diamond_ore", "minecraft:lapis_ore"),
            List.of("minecraft:polar_bear", "minecraft:fox"), 0xF0F8FF, 0xF0F8FF),

        new PlanetDefinition("ariel", "Ariel", PlanetType.ICE_WORLD, 101010101L,
            0.269f, -214.0f, false, false, "minecraft:packed_ice", "minecraft:snow_block",
            List.of("minecraft:iron_ore", "minecraft:emerald_ore"),
            List.of("minecraft:snow_golem", "minecraft:witch"), 0xE6E6FA, 0xE6E6FA),

        // Hot Worlds (8 planets)
        new PlanetDefinition("venus", "Venus", PlanetType.HOT_WORLD, 121212121L,
            8.87f, 464.0f, true, false, "minecraft:magma_block", "minecraft:netherrack",
            List.of("minecraft:gold_ore", "minecraft:redstone_ore"),
            List.of("minecraft:blaze", "minecraft:magma_cube"), 0xFF6347, 0xFF6347),

        new PlanetDefinition("kepler_78b", "Kepler-78b", PlanetType.HOT_WORLD, 787878787L,
            1.69f, 2000.0f, false, false, "minecraft:blackstone", "minecraft:basalt",
            List.of("minecraft:diamond_ore", "minecraft:gold_ore"),
            List.of("minecraft:wither_skeleton", "minecraft:ghast"), 0x8B0000, 0x8B0000),

        new PlanetDefinition("corot_7b", "CoRoT-7b", PlanetType.HOT_WORLD, 777777L,
            4.8f, 1500.0f, false, false, "minecraft:smooth_basalt", "minecraft:magma_block",
            List.of("minecraft:redstone_ore", "minecraft:lapis_ore"),
            List.of("minecraft:blaze", "minecraft:magma_cube"), 0xFF4500, 0xFF4500),

        new PlanetDefinition("wasp_12b", "WASP-12b", PlanetType.HOT_WORLD, 121212L,
            1.4f, 2200.0f, true, false, "minecraft:nether_bricks", "minecraft:soul_sand",
            List.of("minecraft:gold_ore", "minecraft:diamond_ore"),
            List.of("minecraft:wither_skeleton", "minecraft:ghast"), 0x800080, 0x800080),

        new PlanetDefinition("hat_p_7b", "HAT-P-7b", PlanetType.HOT_WORLD, 777777777L,
            1.8f, 2860.0f, true, false, "minecraft:crying_obsidian", "minecraft:obsidian",
            List.of("minecraft:emerald_ore", "minecraft:redstone_ore"),
            List.of("minecraft:enderman", "minecraft:blaze"), 0x4B0082, 0x4B0082),

        new PlanetDefinition("kepler_10b", "Kepler-10b", PlanetType.HOT_WORLD, 101010L,
            4.56f, 1833.0f, false, false, "minecraft:gilded_blackstone", "minecraft:blackstone",
            List.of("minecraft:gold_ore", "minecraft:copper_ore"),
            List.of("minecraft:piglin", "minecraft:hoglin"), 0xFFD700, 0xFFD700),

        new PlanetDefinition("tres_2b", "TrES-2b", PlanetType.HOT_WORLD, 222222L,
            1.2f, 1060.0f, true, false, "minecraft:coal_block", "minecraft:charcoal_block",
            List.of("minecraft:coal_ore", "minecraft:diamond_ore"),
            List.of("minecraft:wither_skeleton", "minecraft:skeleton"), 0x000000, 0x000000),

        new PlanetDefinition("wasp_33b", "WASP-33b", PlanetType.HOT_WORLD, 333333L,
            2.1f, 2710.0f, true, false, "minecraft:ancient_debris", "minecraft:netherite_block",
            List.of("minecraft:ancient_debris", "minecraft:gold_ore"),
            List.of("minecraft:piglin_brute", "minecraft:zombified_piglin"), 0x8B4513, 0x8B4513),

        // Airless Bodies (8 planets)
        new PlanetDefinition("moon", "Moon", PlanetType.AIRLESS_BODY, 131313131L,
            1.622f, -173.0f, false, false, "minecraft:smooth_stone", "minecraft:light_gray_concrete",
            List.of("minecraft:iron_ore", "minecraft:gold_ore"),
            List.of("minecraft:zombie", "minecraft:skeleton"), 0x2F2F2F, 0x2F2F2F),

        new PlanetDefinition("ceres", "Ceres", PlanetType.AIRLESS_BODY, 141414141L,
            0.27f, -105.0f, false, false, "minecraft:diorite", "minecraft:white_concrete_powder",
            List.of("minecraft:iron_ore", "minecraft:coal_ore"),
            List.of("minecraft:husk", "minecraft:stray"), 0xA9A9A9, 0xA9A9A9),

        new PlanetDefinition("vesta", "Vesta", PlanetType.AIRLESS_BODY, 151515151L,
            0.25f, -60.0f, false, false, "minecraft:granite", "minecraft:gray_concrete",
            List.of("minecraft:copper_ore", "minecraft:redstone_ore"),
            List.of("minecraft:spider", "minecraft:cave_spider"), 0x808080, 0x808080),

        new PlanetDefinition("pallas", "Pallas", PlanetType.AIRLESS_BODY, 161616161L,
            0.2f, -38.0f, false, false, "minecraft:andesite", "minecraft:light_gray_concrete",
            List.of("minecraft:lapis_ore", "minecraft:emerald_ore"),
            List.of("minecraft:silverfish", "minecraft:endermite"), 0xC0C0C0, 0xC0C0C0),

        new PlanetDefinition("phobos", "Phobos", PlanetType.AIRLESS_BODY, 171717171L,
            0.0057f, -40.0f, false, false, "minecraft:cobblestone", "minecraft:gravel",
            List.of("minecraft:iron_ore", "minecraft:redstone_ore"),
            List.of("minecraft:zombie", "minecraft:spider"), 0x8B4513, 0x8B4513),

        new PlanetDefinition("deimos", "Deimos", PlanetType.AIRLESS_BODY, 181818181L,
            0.003f, -40.0f, false, false, "minecraft:stone", "minecraft:cobblestone",
            List.of("minecraft:coal_ore", "minecraft:copper_ore"),
            List.of("minecraft:skeleton", "minecraft:creeper"), 0xA0522D, 0xA0522D),

        new PlanetDefinition("io", "Io", PlanetType.AIRLESS_BODY, 191919191L,
            1.796f, -143.0f, false, false, "minecraft:yellow_terracotta", "minecraft:sulfur_block",
            List.of("minecraft:gold_ore", "minecraft:redstone_ore"),
            List.of("minecraft:blaze", "minecraft:magma_cube"), 0xFFFF00, 0xFFFF00),

        new PlanetDefinition("mimas", "Mimas", PlanetType.AIRLESS_BODY, 202020202L,
            0.0636f, -181.0f, false, false, "minecraft:ice", "minecraft:white_concrete",
            List.of("minecraft:iron_ore", "minecraft:diamond_ore"),
            List.of("minecraft:polar_bear", "minecraft:stray"), 0xF5F5F5, 0xF5F5F5),

        // Extreme Worlds (5 planets)
        new PlanetDefinition("mercury", "Mercury", PlanetType.EXTREME_WORLD, 212121212L,
            3.7f, 167.0f, false, false, "minecraft:smooth_basalt", "minecraft:basalt",
            List.of("minecraft:gold_ore", "minecraft:iron_ore"),
            List.of("minecraft:husk", "minecraft:magma_cube"), 0x8C7853, 0x8C7853),

        new PlanetDefinition("kepler_70b", "Kepler-70b", PlanetType.EXTREME_WORLD, 707070707L,
            0.44f, 7000.0f, false, false, "minecraft:end_stone", "minecraft:purpur_block",
            List.of("minecraft:diamond_ore", "minecraft:emerald_ore"),
            List.of("minecraft:enderman", "minecraft:shulker"), 0x9370DB, 0x9370DB),

        new PlanetDefinition("gj_1214b", "GJ-1214b", PlanetType.EXTREME_WORLD, 121412141L,
            6.55f, 200.0f, true, false, "minecraft:prismarine_bricks", "minecraft:dark_prismarine",
            List.of("minecraft:lapis_ore", "minecraft:copper_ore"),
            List.of("minecraft:guardian", "minecraft:elder_guardian"), 0x008080, 0x008080),

        new PlanetDefinition("hd_209458b", "HD-209458b", PlanetType.EXTREME_WORLD, 209458209L,
            0.69f, 1000.0f, true, false, "minecraft:warped_nylium", "minecraft:warped_wart_block",
            List.of("minecraft:emerald_ore", "minecraft:gold_ore"),
            List.of("minecraft:enderman", "minecraft:phantom"), 0x4B8B3B, 0x4B8B3B),

        new PlanetDefinition("wasp_76b", "WASP-76b", PlanetType.EXTREME_WORLD, 767676767L,
            0.92f, 2227.0f, true, false, "minecraft:copper_block", "minecraft:oxidized_copper",
            List.of("minecraft:copper_ore", "minecraft:gold_ore"),
            List.of("minecraft:drowned", "minecraft:guardian"), 0xB87333, 0xB87333),

        // Gas Giant Moons (4 planets)
        new PlanetDefinition("pandora", "Pandora", PlanetType.GAS_GIANT_MOON, 232323232L,
            0.84f, 15.0f, true, true, "minecraft:moss_block", "minecraft:jungle_wood",
            List.of("minecraft:emerald_ore", "minecraft:diamond_ore"),
            List.of("minecraft:ocelot", "minecraft:parrot"), 0x32CD32, 0x32CD32),

        new PlanetDefinition("endor", "Endor", PlanetType.GAS_GIANT_MOON, 242424242L,
            0.8f, 20.0f, true, true, "minecraft:dirt", "minecraft:dark_oak_wood",
            List.of("minecraft:iron_ore", "minecraft:coal_ore"),
            List.of("minecraft:wolf", "minecraft:fox"), 0x228B22, 0x228B22),

        new PlanetDefinition("yavin_4", "Yavin-4", PlanetType.GAS_GIANT_MOON, 444444L,
            1.0f, 30.0f, true, true, "minecraft:jungle_wood", "minecraft:vine",
            List.of("minecraft:gold_ore", "minecraft:lapis_ore"),
            List.of("minecraft:villager", "minecraft:iron_golem"), 0x9ACD32, 0x9ACD32),

        new PlanetDefinition("alpha_centauri_bb", "Alpha-Centauri-Bb", PlanetType.GAS_GIANT_MOON, 131313L,
            1.1f, 1200.0f, true, false, "minecraft:crimson_nylium", "minecraft:crimson_stem",
            List.of("minecraft:gold_ore", "minecraft:redstone_ore"),
            List.of("minecraft:piglin", "minecraft:strider"), 0xDC143C, 0xDC143C)
    );

    /**
     * Get planet definition by name
     */
    public static PlanetDefinition getPlanet(String name) {
        return ALL_PLANETS.stream()
            .filter(planet -> planet.name.equals(name))
            .findFirst()
            .orElse(null);
    }

    /**
     * Get all planets of a specific type
     */
    public static List<PlanetDefinition> getPlanetsByType(PlanetType type) {
        return ALL_PLANETS.stream()
            .filter(planet -> planet.type == type)
            .toList();
    }
}