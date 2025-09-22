package com.hecookin.adastramekanized.common.planets;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.api.planets.Planet;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.nio.file.Path;

/**
 * Data container for dynamically created planets.
 * Stores all information needed to recreate and manage dynamic planets.
 */
public class DynamicPlanetData {

    private final ResourceLocation planetId;
    private final String displayName;
    private final DimensionEffectsType effectsType;
    private final CelestialType celestialType;

    // Planet properties
    private final float gravity;
    private final float temperature;
    private final float dayLength;
    private final int orbitDistance;
    private final boolean hasAtmosphere;
    private final boolean breathable;
    private final float atmospherePressure;

    // Runtime state
    private boolean isLoaded;
    private long lastAccessed;
    private long createdTime;
    private final ResourceKey<Level> dimensionKey;

    // File tracking
    private Path planetJsonFile;
    private boolean jsonFileExists;
    private boolean dimensionFolderExists;

    public DynamicPlanetData(ResourceLocation planetId, String displayName,
                           DimensionEffectsType effectsType, CelestialType celestialType,
                           float gravity, float temperature, float dayLength, int orbitDistance,
                           boolean hasAtmosphere, boolean breathable, float atmospherePressure) {
        this.planetId = planetId;
        this.displayName = displayName;
        this.effectsType = effectsType;
        this.celestialType = celestialType;
        this.gravity = gravity;
        this.temperature = temperature;
        this.dayLength = dayLength;
        this.orbitDistance = orbitDistance;
        this.hasAtmosphere = hasAtmosphere;
        this.breathable = breathable;
        this.atmospherePressure = atmospherePressure;

        // Initialize runtime state
        this.isLoaded = false;
        this.lastAccessed = System.currentTimeMillis();
        this.createdTime = System.currentTimeMillis();
        this.dimensionKey = ResourceKey.create(Registries.DIMENSION, planetId);
        this.jsonFileExists = false;
        this.dimensionFolderExists = false;
    }

    /**
     * Create DynamicPlanetData from NBT (for SavedData loading)
     */
    public static DynamicPlanetData fromNBT(CompoundTag tag) {
        ResourceLocation planetId = ResourceLocation.parse(tag.getString("planetId"));
        String displayName = tag.getString("displayName");
        DimensionEffectsType effectsType = DimensionEffectsType.valueOf(tag.getString("effectsType"));
        CelestialType celestialType = CelestialType.valueOf(tag.getString("celestialType"));

        float gravity = tag.getFloat("gravity");
        float temperature = tag.getFloat("temperature");
        float dayLength = tag.getFloat("dayLength");
        int orbitDistance = tag.getInt("orbitDistance");
        boolean hasAtmosphere = tag.getBoolean("hasAtmosphere");
        boolean breathable = tag.getBoolean("breathable");
        float atmospherePressure = tag.getFloat("atmospherePressure");

        DynamicPlanetData data = new DynamicPlanetData(planetId, displayName, effectsType, celestialType,
            gravity, temperature, dayLength, orbitDistance, hasAtmosphere, breathable, atmospherePressure);

        // Restore runtime state
        data.isLoaded = tag.getBoolean("isLoaded");
        data.lastAccessed = tag.getLong("lastAccessed");
        data.createdTime = tag.getLong("createdTime");
        data.jsonFileExists = tag.getBoolean("jsonFileExists");
        data.dimensionFolderExists = tag.getBoolean("dimensionFolderExists");

        if (tag.contains("planetJsonFile")) {
            data.planetJsonFile = Path.of(tag.getString("planetJsonFile"));
        }

        return data;
    }

    /**
     * Serialize to NBT (for SavedData saving)
     */
    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();

        tag.putString("planetId", planetId.toString());
        tag.putString("displayName", displayName);
        tag.putString("effectsType", effectsType.name());
        tag.putString("celestialType", celestialType.name());

        tag.putFloat("gravity", gravity);
        tag.putFloat("temperature", temperature);
        tag.putFloat("dayLength", dayLength);
        tag.putInt("orbitDistance", orbitDistance);
        tag.putBoolean("hasAtmosphere", hasAtmosphere);
        tag.putBoolean("breathable", breathable);
        tag.putFloat("atmospherePressure", atmospherePressure);

        tag.putBoolean("isLoaded", isLoaded);
        tag.putLong("lastAccessed", lastAccessed);
        tag.putLong("createdTime", createdTime);
        tag.putBoolean("jsonFileExists", jsonFileExists);
        tag.putBoolean("dimensionFolderExists", dimensionFolderExists);

        if (planetJsonFile != null) {
            tag.putString("planetJsonFile", planetJsonFile.toString());
        }

        return tag;
    }

    /**
     * Convert to Planet API object for compatibility
     */
    public Planet toPlanet() {
        AdAstraMekanized.LOGGER.debug("Converting DynamicPlanetData to Planet API object: {}", displayName);

        try {
            // Convert atmosphere data
            Planet.AtmosphereType atmosphereType = determineAtmosphereType();
            Planet.AtmosphereData atmosphere = new Planet.AtmosphereData(
                hasAtmosphere,
                breathable,
                atmospherePressure,
                breathable ? 0.21f : 0.0f, // Earth-like oxygen level if breathable
                atmosphereType
            );

            // Convert planet properties
            Planet.PlanetProperties properties = new Planet.PlanetProperties(
                gravity,
                temperature,
                dayLength,
                orbitDistance,
                false, // hasRings - not generated yet
                0      // moonCount - not generated yet
            );

            // Create dimension settings based on effects type
            Planet.DimensionSettings dimension = createDimensionSettings();

            // Create default generation settings
            com.hecookin.adastramekanized.api.planets.generation.PlanetGenerationSettings generation =
                com.hecookin.adastramekanized.api.planets.generation.PlanetGenerationSettings.createEarthlike();

            // Create default atmospheric rendering
            com.hecookin.adastramekanized.api.planets.atmosphere.AtmosphericRendering rendering =
                com.hecookin.adastramekanized.api.planets.atmosphere.AtmosphericRendering.createDefault();

            return new Planet(planetId, displayName, properties, atmosphere, dimension, generation, rendering);

        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Failed to convert DynamicPlanetData to Planet: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Determine atmosphere type based on planet properties
     */
    private Planet.AtmosphereType determineAtmosphereType() {
        if (!hasAtmosphere) {
            return Planet.AtmosphereType.NONE;
        }

        if (atmospherePressure < 0.3f) {
            return Planet.AtmosphereType.THIN;
        } else if (atmospherePressure > 2.0f) {
            return Planet.AtmosphereType.THICK;
        } else if (breathable) {
            return Planet.AtmosphereType.NORMAL;
        } else {
            return Planet.AtmosphereType.TOXIC;
        }
    }

    /**
     * Create dimension settings based on effects type
     */
    private Planet.DimensionSettings createDimensionSettings() {
        // Create dimension type based on effects type
        String dimensionTypeName = switch (effectsType) {
            case ROCKY -> "mars_like";
            case ICE_WORLD -> "ice_world";
            case VOLCANIC -> "volcanic";
            case GAS_GIANT -> "gas_giant";
            case MOON_LIKE -> "moon_like";
            case ASTEROID_LIKE -> "moon_like";
            case ALTERED_OVERWORLD -> "mars_like";
        };

        ResourceLocation dimensionType = ResourceLocation.fromNamespaceAndPath(
            AdAstraMekanized.MOD_ID, dimensionTypeName);

        // Use the planet chunk generator
        ResourceLocation chunkGenerator = ResourceLocation.fromNamespaceAndPath(
            AdAstraMekanized.MOD_ID, "planet");

        // Create biome source based on effects type
        ResourceLocation biomeSource = ResourceLocation.fromNamespaceAndPath(
            AdAstraMekanized.MOD_ID, effectsType.name().toLowerCase());

        // Determine sky and fog colors based on atmosphere and type
        int skyColor = determineSkyColor();
        int fogColor = determineFogColor();
        float ambientLight = determineAmbientLight();

        return new Planet.DimensionSettings(
            dimensionType,
            biomeSource,
            chunkGenerator,
            false, // isOrbital
            skyColor,
            fogColor,
            ambientLight
        );
    }

    /**
     * Determine sky color based on atmosphere and planet type
     */
    private int determineSkyColor() {
        if (!hasAtmosphere) {
            return 0x000000; // Black space
        }

        return switch (effectsType) {
            case ROCKY -> 0xD2691E; // Sandy brown
            case ICE_WORLD -> 0xE0FFFF; // Light cyan
            case VOLCANIC -> 0xFF4500; // Orange red
            case GAS_GIANT -> 0x4682B4; // Steel blue
            case MOON_LIKE -> 0x000000; // Black
            case ASTEROID_LIKE -> 0x000000; // Black
            case ALTERED_OVERWORLD -> 0x87CEEB; // Sky blue
        };
    }

    /**
     * Determine fog color based on atmosphere and planet type
     */
    private int determineFogColor() {
        if (!hasAtmosphere) {
            return 0x000000; // Black
        }

        return switch (effectsType) {
            case ROCKY -> 0xDEB887; // Burlywood
            case ICE_WORLD -> 0xF0F8FF; // Alice blue
            case VOLCANIC -> 0x8B0000; // Dark red
            case GAS_GIANT -> 0x6495ED; // Cornflower blue
            case MOON_LIKE -> 0x696969; // Dim gray
            case ASTEROID_LIKE -> 0x696969; // Dim gray
            case ALTERED_OVERWORLD -> 0xB0C4DE; // Light steel blue
        };
    }

    /**
     * Determine ambient light level based on atmosphere
     */
    private float determineAmbientLight() {
        if (!hasAtmosphere) {
            return 0.0f; // No atmosphere means no ambient light
        }

        return switch (effectsType) {
            case ROCKY -> 0.3f;
            case ICE_WORLD -> 0.4f; // Ice reflects light
            case VOLCANIC -> 0.2f; // Ash and smoke reduce light
            case GAS_GIANT -> 0.1f; // Dense atmosphere
            case MOON_LIKE -> 0.0f; // Usually no atmosphere
            case ASTEROID_LIKE -> 0.0f; // No atmosphere
            case ALTERED_OVERWORLD -> 0.9f; // Earth-like lighting
        };
    }

    /**
     * Update access timestamp for LRU tracking
     */
    public void updateAccessTime() {
        this.lastAccessed = System.currentTimeMillis();
    }

    /**
     * Check if planet data is stale and can be archived
     */
    public boolean canBeArchived() {
        long archiveThreshold = 24 * 60 * 60 * 1000; // 24 hours
        return !isLoaded && (System.currentTimeMillis() - lastAccessed) > archiveThreshold;
    }

    /**
     * Check if planet data is very old and can be deleted
     */
    public boolean canBeDeleted() {
        long deleteThreshold = 7 * 24 * 60 * 60 * 1000; // 7 days
        return !isLoaded && (System.currentTimeMillis() - lastAccessed) > deleteThreshold;
    }

    // Getters
    public ResourceLocation getPlanetId() { return planetId; }
    public String getDisplayName() { return displayName; }
    public DimensionEffectsType getEffectsType() { return effectsType; }
    public CelestialType getCelestialType() { return celestialType; }
    public float getGravity() { return gravity; }
    public float getTemperature() { return temperature; }
    public float getDayLength() { return dayLength; }
    public int getOrbitDistance() { return orbitDistance; }
    public boolean hasAtmosphere() { return hasAtmosphere; }
    public boolean isBreathable() { return breathable; }
    public float getAtmospherePressure() { return atmospherePressure; }
    public boolean isLoaded() { return isLoaded; }
    public long getLastAccessed() { return lastAccessed; }
    public long getCreatedTime() { return createdTime; }
    public ResourceKey<Level> getDimensionKey() { return dimensionKey; }
    public Path getPlanetJsonFile() { return planetJsonFile; }
    public boolean jsonFileExists() { return jsonFileExists; }
    public boolean dimensionFolderExists() { return dimensionFolderExists; }

    // Setters for runtime state
    public void setLoaded(boolean loaded) { this.isLoaded = loaded; }
    public void setPlanetJsonFile(Path planetJsonFile) { this.planetJsonFile = planetJsonFile; }
    public void setJsonFileExists(boolean exists) { this.jsonFileExists = exists; }
    public void setDimensionFolderExists(boolean exists) { this.dimensionFolderExists = exists; }

    @Override
    public String toString() {
        return String.format("DynamicPlanetData{id=%s, name='%s', type=%s, celestial=%s, loaded=%s}",
            planetId, displayName, effectsType, celestialType, isLoaded);
    }
}