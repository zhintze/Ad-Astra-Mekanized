package com.hecookin.adastramekanized.common.worldgen;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.planets.DimensionEffectsType;

import java.util.*;

/**
 * Procedural name generator for planets inspired by real space exploration,
 * astronomy, and science fiction. Creates unique, memorable planet names
 * that fit the space exploration theme.
 */
public class NameGenerator {

    private final PlanetRandomizer randomizer;
    private final Set<String> usedNames;

    public NameGenerator(PlanetRandomizer randomizer) {
        this.randomizer = randomizer;
        this.usedNames = new HashSet<>();
    }

    /**
     * Generate a unique planet name that hasn't been used yet
     */
    public String generateUniquePlanetName(int planetIndex, DimensionEffectsType effectsType) {
        String name;
        int attempts = 0;
        final int maxAttempts = 100;

        do {
            name = generatePlanetName(planetIndex, effectsType);
            attempts++;

            if (attempts >= maxAttempts) {
                // Fallback to guaranteed unique name
                name = generateFallbackName(planetIndex);
                break;
            }
        } while (usedNames.contains(name));

        usedNames.add(name);
        AdAstraMekanized.LOGGER.debug("Generated unique planet name: {} (attempts: {})", name, attempts);
        return name;
    }

    /**
     * Generate a planet name based on index and type
     */
    private String generatePlanetName(int planetIndex, DimensionEffectsType effectsType) {
        // Create deterministic randomizer for this specific planet
        PlanetRandomizer planetRandomizer = randomizer.forPlanet(planetIndex);

        NameStyle style = selectNameStyle(planetRandomizer, effectsType);

        return switch (style) {
            case SCIENTIFIC_DESIGNATION -> generateScientificName(planetRandomizer, planetIndex);
            case SPACE_MISSION -> generateSpaceMissionName(planetRandomizer, planetIndex);
            case ASTRONOMICAL_OBJECT -> generateAstronomicalName(planetRandomizer, planetIndex);
            case CLASSICAL_MYTHOLOGY -> generateMythologicalName(planetRandomizer, planetIndex);
            case SCI_FI_THEMED -> generateSciFiName(planetRandomizer, planetIndex);
            case EXPLORER_TRIBUTE -> generateExplorerName(planetRandomizer, planetIndex);
        };
    }

    /**
     * Select naming style based on planet type and randomization
     */
    private NameStyle selectNameStyle(PlanetRandomizer planetRandomizer, DimensionEffectsType effectsType) {
        // Bias certain name styles based on planet type
        Map<NameStyle, Float> styleWeights = new HashMap<>();

        switch (effectsType) {
            case ROCKY -> {
                styleWeights.put(NameStyle.SCIENTIFIC_DESIGNATION, 25.0f);
                styleWeights.put(NameStyle.ASTRONOMICAL_OBJECT, 25.0f);
                styleWeights.put(NameStyle.CLASSICAL_MYTHOLOGY, 20.0f);
                styleWeights.put(NameStyle.SPACE_MISSION, 15.0f);
                styleWeights.put(NameStyle.SCI_FI_THEMED, 10.0f);
                styleWeights.put(NameStyle.EXPLORER_TRIBUTE, 5.0f);
            }
            case ICE_WORLD -> {
                styleWeights.put(NameStyle.CLASSICAL_MYTHOLOGY, 30.0f);
                styleWeights.put(NameStyle.SCI_FI_THEMED, 25.0f);
                styleWeights.put(NameStyle.ASTRONOMICAL_OBJECT, 20.0f);
                styleWeights.put(NameStyle.SCIENTIFIC_DESIGNATION, 15.0f);
                styleWeights.put(NameStyle.SPACE_MISSION, 5.0f);
                styleWeights.put(NameStyle.EXPLORER_TRIBUTE, 5.0f);
            }
            case VOLCANIC -> {
                styleWeights.put(NameStyle.CLASSICAL_MYTHOLOGY, 35.0f);
                styleWeights.put(NameStyle.SCI_FI_THEMED, 25.0f);
                styleWeights.put(NameStyle.ASTRONOMICAL_OBJECT, 15.0f);
                styleWeights.put(NameStyle.SCIENTIFIC_DESIGNATION, 15.0f);
                styleWeights.put(NameStyle.SPACE_MISSION, 5.0f);
                styleWeights.put(NameStyle.EXPLORER_TRIBUTE, 5.0f);
            }
            case GAS_GIANT -> {
                styleWeights.put(NameStyle.ASTRONOMICAL_OBJECT, 30.0f);
                styleWeights.put(NameStyle.CLASSICAL_MYTHOLOGY, 25.0f);
                styleWeights.put(NameStyle.SCIENTIFIC_DESIGNATION, 20.0f);
                styleWeights.put(NameStyle.SCI_FI_THEMED, 15.0f);
                styleWeights.put(NameStyle.SPACE_MISSION, 5.0f);
                styleWeights.put(NameStyle.EXPLORER_TRIBUTE, 5.0f);
            }
            case ASTEROID_LIKE -> {
                styleWeights.put(NameStyle.SCIENTIFIC_DESIGNATION, 40.0f);
                styleWeights.put(NameStyle.SPACE_MISSION, 20.0f);
                styleWeights.put(NameStyle.ASTRONOMICAL_OBJECT, 15.0f);
                styleWeights.put(NameStyle.SCI_FI_THEMED, 15.0f);
                styleWeights.put(NameStyle.CLASSICAL_MYTHOLOGY, 5.0f);
                styleWeights.put(NameStyle.EXPLORER_TRIBUTE, 5.0f);
            }
            case ALTERED_OVERWORLD -> {
                styleWeights.put(NameStyle.CLASSICAL_MYTHOLOGY, 30.0f);
                styleWeights.put(NameStyle.EXPLORER_TRIBUTE, 25.0f);
                styleWeights.put(NameStyle.ASTRONOMICAL_OBJECT, 20.0f);
                styleWeights.put(NameStyle.SCI_FI_THEMED, 15.0f);
                styleWeights.put(NameStyle.SCIENTIFIC_DESIGNATION, 5.0f);
                styleWeights.put(NameStyle.SPACE_MISSION, 5.0f);
            }
            default -> {
                // Default distribution for moon-like and other types
                styleWeights.put(NameStyle.SCIENTIFIC_DESIGNATION, 30.0f);
                styleWeights.put(NameStyle.SPACE_MISSION, 25.0f);
                styleWeights.put(NameStyle.ASTRONOMICAL_OBJECT, 20.0f);
                styleWeights.put(NameStyle.CLASSICAL_MYTHOLOGY, 10.0f);
                styleWeights.put(NameStyle.SCI_FI_THEMED, 10.0f);
                styleWeights.put(NameStyle.EXPLORER_TRIBUTE, 5.0f);
            }
        }

        return planetRandomizer.selectWeighted(styleWeights);
    }

    // ========== NAME GENERATION METHODS ==========

    /**
     * Generate scientific designation names (e.g., "Kepler-442b", "HD 40307g")
     */
    private String generateScientificName(PlanetRandomizer planetRandomizer, int planetIndex) {
        List<String> catalogPrefixes = Arrays.asList(
            "Kepler", "HD", "TOI", "TrES", "WASP", "HAT-P", "XO", "CoRoT",
            "K2", "TESS", "Gliese", "Wolf", "Ross", "Lalande", "BD"
        );

        String prefix = planetRandomizer.selectRandom(catalogPrefixes);
        int catalogNumber = planetRandomizer.randomInt(100, 9999);

        // Planet designation (b, c, d, etc. where b is first planet)
        char planetLetter = (char) ('b' + (planetIndex % 10));

        return String.format("%s-%d%c", prefix, catalogNumber, planetLetter);
    }

    /**
     * Generate space mission inspired names
     */
    private String generateSpaceMissionName(PlanetRandomizer planetRandomizer, int planetIndex) {
        List<String> missionPrefixes = Arrays.asList(
            "Voyager", "Pioneer", "Cassini", "Galileo", "Juno", "Perseverance",
            "Curiosity", "Opportunity", "Spirit", "Pathfinder", "Viking",
            "Mariner", "Apollo", "Artemis", "Orion", "Discovery", "Endeavour"
        );

        String prefix = planetRandomizer.selectRandom(missionPrefixes);
        String pattern = planetRandomizer.selectRandom(PlanetGenerationConfig.NUMBER_PATTERNS);

        return prefix + String.format(pattern, planetIndex + 1);
    }

    /**
     * Generate astronomical object inspired names
     */
    private String generateAstronomicalName(PlanetRandomizer planetRandomizer, int planetIndex) {
        List<String> starNames = Arrays.asList(
            "Proxima", "Centauri", "Vega", "Altair", "Rigel", "Betelgeuse", "Sirius",
            "Polaris", "Arcturus", "Capella", "Aldebaran", "Antares", "Canopus",
            "Deneb", "Procyon", "Achernar", "Hadar", "Mimosa", "Shaula", "Elnath"
        );

        String starName = planetRandomizer.selectRandom(starNames);
        String suffix = planetRandomizer.selectRandom(PlanetGenerationConfig.NAME_SUFFIXES);

        return starName + suffix;
    }

    /**
     * Generate classical mythology inspired names
     */
    private String generateMythologicalName(PlanetRandomizer planetRandomizer, int planetIndex) {
        // Mix of Roman, Greek, Norse, and other mythologies
        List<String> mythNames = Arrays.asList(
            // Roman gods/goddesses
            "Minerva", "Vulcan", "Ceres", "Vesta", "Janus", "Terminus", "Fortuna",
            "Aurora", "Luna", "Stella", "Celesta", "Astra",

            // Greek mythology
            "Helios", "Selene", "Eos", "Nyx", "Aether", "Cosmos", "Gaia",
            "Urania", "Theia", "Rhea", "Metis", "Hebe",

            // Norse mythology
            "Asgard", "Midgard", "Valhalla", "Bifrost", "Yggdrasil", "Ragnar",
            "Freya", "Balder", "Heimdall", "Vidar",

            // Other mythologies
            "Avalon", "Elysium", "Arcadia", "Atlantis", "Lemuria", "Shangri",
            "Agartha", "Hyperborea", "Thule"
        );

        String baseName = planetRandomizer.selectRandom(mythNames);

        // Occasionally add a modifier
        if (planetRandomizer.randomBoolean(0.3f)) {
            String modifier = planetRandomizer.selectRandom(PlanetGenerationConfig.NAME_SUFFIXES);
            return baseName + modifier;
        }

        return baseName;
    }

    /**
     * Generate sci-fi themed names
     */
    private String generateSciFiName(PlanetRandomizer planetRandomizer, int planetIndex) {
        List<String> sciFiPrefixes = Arrays.asList(
            "Neo", "Cyber", "Quantum", "Plasma", "Ion", "Photon", "Neutron",
            "Fusion", "Solar", "Stellar", "Cosmic", "Galactic", "Void",
            "Nexus", "Matrix", "Vector", "Synth", "Chrome", "Neon", "Crystal"
        );

        List<String> sciFiSuffixes = Arrays.asList(
            "Prime", "Alpha", "Beta", "Gamma", "Delta", "Omega", "Zero",
            "One", "Core", "Station", "Base", "Outpost", "Colony", "Haven",
            "Forge", "Nexus", "Gate", "Portal", "Sphere", "Ring"
        );

        String prefix = planetRandomizer.selectRandom(sciFiPrefixes);
        String suffix = planetRandomizer.selectRandom(sciFiSuffixes);

        return prefix + " " + suffix;
    }

    /**
     * Generate explorer/scientist tribute names
     */
    private String generateExplorerName(PlanetRandomizer planetRandomizer, int planetIndex) {
        List<String> explorerNames = Arrays.asList(
            "Newton", "Einstein", "Hawking", "Sagan", "Tycho", "Copernicus",
            "Galilei", "Tesla", "Darwin", "Mendel", "Curie", "Faraday",
            "Kepler", "Hubble", "Feynman", "Bohr", "Heisenberg", "Schrödinger",
            "Planck", "Maxwell", "Lorentz", "Dirac", "Pauli", "Fermi"
        );

        String explorerName = planetRandomizer.selectRandom(explorerNames);

        // Add designation or modifier
        if (planetRandomizer.randomBoolean(0.7f)) {
            String pattern = planetRandomizer.selectRandom(PlanetGenerationConfig.NUMBER_PATTERNS);
            return explorerName + String.format(pattern, planetIndex + 1);
        } else {
            String modifier = planetRandomizer.selectRandom(PlanetGenerationConfig.NAME_SUFFIXES);
            return explorerName + modifier;
        }
    }

    /**
     * Generate fallback name when all else fails (guaranteed unique)
     */
    private String generateFallbackName(int planetIndex) {
        return String.format("Planet-%03d", planetIndex + 1);
    }

    /**
     * Generate a short description/classification for the planet name
     */
    public String generatePlanetDescription(String planetName, DimensionEffectsType effectsType) {
        String typeDescription = switch (effectsType) {
            case ROCKY -> "rocky world";
            case ICE_WORLD -> "frozen planet";
            case VOLCANIC -> "volcanic world";
            case GAS_GIANT -> "gas giant";
            case MOON_LIKE -> "airless world";
            case ASTEROID_LIKE -> "asteroid-like body";
            case ALTERED_OVERWORLD -> "habitable world";
        };

        return String.format("%s - A distant %s", planetName, typeDescription);
    }

    /**
     * Get all used names (for debugging/validation)
     */
    public Set<String> getUsedNames() {
        return new HashSet<>(usedNames);
    }

    /**
     * Clear used names (for testing or reset)
     */
    public void clearUsedNames() {
        usedNames.clear();
    }

    // ========== NAMING STYLES ==========

    /**
     * Different naming style categories for variety
     */
    private enum NameStyle {
        SCIENTIFIC_DESIGNATION,  // Kepler-442b, HD 40307g
        SPACE_MISSION,          // Voyager-1, Cassini-7
        ASTRONOMICAL_OBJECT,    // Proxima, Vega, Rigel
        CLASSICAL_MYTHOLOGY,    // Minerva, Helios, Asgard
        SCI_FI_THEMED,         // Neo Prime, Quantum Core
        EXPLORER_TRIBUTE       // Newton-3, Einstein Beta
    }
}