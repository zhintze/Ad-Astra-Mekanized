package com.hecookin.adastramekanized.common.planets;

import com.hecookin.adastramekanized.AdAstraMekanized;
import net.minecraft.nbt.CompoundTag;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Compressed storage for archived planet data to save memory.
 * Used for planets that haven't been accessed recently but should be preserved.
 */
public class CompressedPlanetData {

    private final byte[] compressedData;
    private final long lastAccessed;
    private final long createdTime;
    private final String displayName;
    private final DimensionEffectsType effectsType;
    private final CelestialType celestialType;

    public CompressedPlanetData(byte[] compressedData, long lastAccessed, long createdTime,
                              String displayName, DimensionEffectsType effectsType, CelestialType celestialType) {
        this.compressedData = compressedData;
        this.lastAccessed = lastAccessed;
        this.createdTime = createdTime;
        this.displayName = displayName;
        this.effectsType = effectsType;
        this.celestialType = celestialType;
    }

    /**
     * Compress a DynamicPlanetData into archived format
     */
    public static CompressedPlanetData compress(DynamicPlanetData planetData) {
        try {
            // Serialize to NBT first
            CompoundTag nbt = planetData.toNBT();
            byte[] nbtBytes = nbt.toString().getBytes();

            // Compress using GZIP
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (GZIPOutputStream gzipOut = new GZIPOutputStream(baos)) {
                gzipOut.write(nbtBytes);
            }

            byte[] compressedData = baos.toByteArray();

            // Calculate compression ratio for logging
            double compressionRatio = (double) compressedData.length / nbtBytes.length;
            AdAstraMekanized.LOGGER.debug("Compressed planet {} from {} bytes to {} bytes (ratio: {:.2f})",
                planetData.getDisplayName(), nbtBytes.length, compressedData.length, compressionRatio);

            return new CompressedPlanetData(
                compressedData,
                planetData.getLastAccessed(),
                planetData.getCreatedTime(),
                planetData.getDisplayName(),
                planetData.getEffectsType(),
                planetData.getCelestialType()
            );

        } catch (IOException e) {
            AdAstraMekanized.LOGGER.error("Failed to compress planet data for {}", planetData.getDisplayName(), e);
            throw new RuntimeException("Failed to compress planet data", e);
        }
    }

    /**
     * Decompress back to DynamicPlanetData
     */
    public DynamicPlanetData decompress() {
        try {
            // Decompress using GZIP
            ByteArrayInputStream bais = new ByteArrayInputStream(compressedData);
            try (GZIPInputStream gzipIn = new GZIPInputStream(bais)) {
                byte[] decompressedData = gzipIn.readAllBytes();
                String nbtString = new String(decompressedData);

                // Parse NBT (simplified - in real implementation you'd use proper NBT parsing)
                // For now, we'll use a placeholder approach
                AdAstraMekanized.LOGGER.debug("Decompressed planet {} from {} bytes to {} bytes",
                    displayName, compressedData.length, decompressedData.length);

                // TODO: Proper NBT deserialization - for now create a basic planet data
                // This is a simplified version that should be replaced with proper NBT parsing
                return createBasicPlanetFromMetadata();

            }
        } catch (IOException e) {
            AdAstraMekanized.LOGGER.error("Failed to decompress planet data for {}", displayName, e);
            throw new RuntimeException("Failed to decompress planet data", e);
        }
    }

    /**
     * Create a basic planet data from stored metadata (temporary implementation)
     */
    private DynamicPlanetData createBasicPlanetFromMetadata() {
        // Get default properties from the stored types
        DimensionEffectsType.PhysicalProperties physics = effectsType.getDefaultPhysics();
        DimensionEffectsType.AtmosphericProperties atmosphere = effectsType.getDefaultAtmosphere();

        // Create resource location from display name
        String id = displayName.toLowerCase().replaceAll("[^a-z0-9_]", "_");
        var planetId = AdAstraMekanized.MOD_ID + ":" + id;

        DynamicPlanetData planet = new DynamicPlanetData(
            net.minecraft.resources.ResourceLocation.parse(planetId),
            displayName,
            effectsType,
            celestialType,
            physics.gravity(),
            physics.temperature(),
            physics.dayLength(),
            100 + (int)(Math.random() * 900), // Random orbit distance
            atmosphere.hasAtmosphere(),
            atmosphere.breathable(),
            atmosphere.pressure()
        );

        // Restore timestamps
        if (lastAccessed > 0) {
            // Use reflection or setter if available to restore timestamps
            // For now, the timestamps will be reset to current time
        }

        return planet;
    }

    /**
     * Check if this archived planet is old enough to be deleted
     */
    public boolean canBeDeleted() {
        long deleteThreshold = 7 * 24 * 60 * 60 * 1000; // 7 days
        return (System.currentTimeMillis() - lastAccessed) > deleteThreshold;
    }

    /**
     * Get compressed data size for statistics
     */
    public int getCompressedSize() {
        return compressedData.length;
    }

    /**
     * Serialize to NBT for SavedData
     */
    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();

        tag.putByteArray("compressedData", compressedData);
        tag.putLong("lastAccessed", lastAccessed);
        tag.putLong("createdTime", createdTime);
        tag.putString("displayName", displayName);
        tag.putString("effectsType", effectsType.name());
        tag.putString("celestialType", celestialType.name());

        return tag;
    }

    /**
     * Load from NBT for SavedData
     */
    public static CompressedPlanetData fromNBT(CompoundTag tag) {
        byte[] compressedData = tag.getByteArray("compressedData");
        long lastAccessed = tag.getLong("lastAccessed");
        long createdTime = tag.getLong("createdTime");
        String displayName = tag.getString("displayName");
        DimensionEffectsType effectsType = DimensionEffectsType.valueOf(tag.getString("effectsType"));
        CelestialType celestialType = CelestialType.valueOf(tag.getString("celestialType"));

        return new CompressedPlanetData(compressedData, lastAccessed, createdTime,
            displayName, effectsType, celestialType);
    }

    // Getters for metadata access without decompression
    public long getLastAccessed() { return lastAccessed; }
    public long getCreatedTime() { return createdTime; }
    public String getDisplayName() { return displayName; }
    public DimensionEffectsType getEffectsType() { return effectsType; }
    public CelestialType getCelestialType() { return celestialType; }

    @Override
    public String toString() {
        return String.format("CompressedPlanetData{name='%s', type=%s, celestial=%s, size=%d bytes}",
            displayName, effectsType, celestialType, compressedData.length);
    }
}