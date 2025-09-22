package com.hecookin.adastramekanized.common.planets;

import com.hecookin.adastramekanized.AdAstraMekanized;
import net.minecraft.resources.ResourceLocation;

/**
 * Enumeration of dimension effects templates for dynamic planets.
 * Each type corresponds to a pre-registered DimensionSpecialEffects class.
 */
public enum DimensionEffectsType {
    MOON_LIKE("moon_like", "Airless worlds with stark landscapes, low gravity, excellent stargazing", 20),
    ROCKY("rocky", "Mars-like worlds with thin atmosphere, dust storms, and rocky terrain", 28),
    GAS_GIANT("gas_giant", "Thick atmosphere worlds with floating islands, intense storms, and high pressure", 15),
    ICE_WORLD("ice_world", "Frozen worlds with snow storms, ice formations, and extremely cold temperatures", 18),
    VOLCANIC("volcanic", "Lava worlds with ash storms, extreme heat, and volcanic activity", 8),
    ASTEROID_LIKE("asteroid_like", "Small rocky bodies with minimal gravity, jagged terrain, and no atmosphere", 9),
    ALTERED_OVERWORLD("altered_overworld", "Earth-like worlds with modified biomes, breathable atmosphere, and familiar terrain", 2);

    private final String id;
    private final String description;
    private final int chanceWeight;
    private final ResourceLocation resourceLocation;

    public static final int TOTAL_WEIGHT = 100;

    DimensionEffectsType(String id, String description, int chanceWeight) {
        this.id = id;
        this.description = description;
        this.chanceWeight = chanceWeight;
        this.resourceLocation = ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, id);
    }

    /**
     * Get the ResourceLocation for this dimension effects type
     */
    public ResourceLocation getResourceLocation() {
        return resourceLocation;
    }

    /**
     * Get the string ID for this dimension effects type
     */
    public String getId() {
        return id;
    }

    /**
     * Get the human-readable description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get the chance weight for this dimension effects type
     */
    public int getChanceWeight() {
        return chanceWeight;
    }

    /**
     * Get dimension effects type by string ID
     */
    public static DimensionEffectsType fromId(String id) {
        for (DimensionEffectsType type : values()) {
            if (type.getId().equals(id)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown dimension effects type: " + id);
    }

    /**
     * Get a random dimension effects type for procedural generation based on weights
     */
    public static DimensionEffectsType getRandomType() {
        int totalWeight = 0;
        for (DimensionEffectsType type : values()) {
            totalWeight += type.getChanceWeight();
        }

        int randomValue = (int) (Math.random() * totalWeight);
        int currentWeight = 0;

        for (DimensionEffectsType type : values()) {
            currentWeight += type.getChanceWeight();
            if (randomValue < currentWeight) {
                return type;
            }
        }

        return ROCKY;
    }

    /**
     * Get default atmospheric properties for this dimension type
     */
    public AtmosphericProperties getDefaultAtmosphere() {
        return switch (this) {
            case MOON_LIKE -> new AtmosphericProperties(false, false, 0.0f, 0.0f);
            case ROCKY -> new AtmosphericProperties(true, false, 0.006f, 0.001f);
            case GAS_GIANT -> new AtmosphericProperties(true, false, 2.5f, 0.0f);
            case ICE_WORLD -> new AtmosphericProperties(true, false, 0.3f, 0.15f);
            case VOLCANIC -> new AtmosphericProperties(true, false, 1.2f, 0.0f);
            case ASTEROID_LIKE -> new AtmosphericProperties(false, false, 0.0f, 0.0f);
            case ALTERED_OVERWORLD -> new AtmosphericProperties(true, true, 1.0f, 0.21f);
        };
    }

    /**
     * Get default physical properties for this dimension type
     */
    public PhysicalProperties getDefaultPhysics() {
        return switch (this) {
            case MOON_LIKE -> new PhysicalProperties(0.165f, -173.0f, 708.0f);
            case ROCKY -> new PhysicalProperties(0.379f, -65.0f, 24.6f);
            case GAS_GIANT -> new PhysicalProperties(2.5f, -145.0f, 16.0f);
            case ICE_WORLD -> new PhysicalProperties(0.8f, -220.0f, 32.0f);
            case VOLCANIC -> new PhysicalProperties(1.2f, 400.0f, 18.0f);
            case ASTEROID_LIKE -> new PhysicalProperties(0.08f, -180.0f, 400.0f);
            case ALTERED_OVERWORLD -> new PhysicalProperties(0.98f, 15.0f, 24.0f);
        };
    }

    /**
     * Atmospheric properties record
     */
    public record AtmosphericProperties(boolean hasAtmosphere, boolean breathable,
                                      float pressure, float oxygenLevel) {}

    /**
     * Physical properties record
     */
    public record PhysicalProperties(float gravity, float temperature, float dayLength) {}

    @Override
    public String toString() {
        return String.format("%s (%s)", id, description);
    }
}