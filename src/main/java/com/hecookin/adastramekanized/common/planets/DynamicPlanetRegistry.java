package com.hecookin.adastramekanized.common.planets;

import com.hecookin.adastramekanized.AdAstraMekanized;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Central registry for all dynamically created planets.
 * Handles persistence across server restarts using Minecraft's SavedData system.
 * TODO: Fix SavedData implementation for NeoForge 1.21.1
 */
public class DynamicPlanetRegistry extends SavedData {

    private static final String DATA_NAME = AdAstraMekanized.MOD_ID + "_planet_registry";

    // Core limits and constraints
    public static final int MAX_PLANETS = 100;
    public static final int MAX_LOADED = 10;

    // Planet storage maps
    private final Map<ResourceLocation, DynamicPlanetData> discoveredPlanets = new ConcurrentHashMap<>();
    private final Map<ResourceLocation, CompressedPlanetData> archivedPlanets = new ConcurrentHashMap<>();
    private final Set<ResourceLocation> loadedDimensions = ConcurrentHashMap.newKeySet();

    // Search indices for performance
    private final Map<String, List<ResourceLocation>> nameIndex = new ConcurrentHashMap<>();
    private final Map<DimensionEffectsType, List<ResourceLocation>> typeIndex = new ConcurrentHashMap<>();
    private final Map<CelestialType, List<ResourceLocation>> celestialIndex = new ConcurrentHashMap<>();
    private final TreeMap<Long, ResourceLocation> accessTimeIndex = new TreeMap<>();

    // State tracking
    private int planetCounter = 0;
    private long lastMaintenanceTime = 0;
    private MinecraftServer server;

    public DynamicPlanetRegistry() {
        super();
    }

    /**
     * Get or create the planet registry for the server
     */
    public static DynamicPlanetRegistry get(MinecraftServer server) {
        DimensionDataStorage dataStorage = server.overworld().getDataStorage();
        return dataStorage.computeIfAbsent(
            new SavedData.Factory<>(
                DynamicPlanetRegistry::new,
                DynamicPlanetRegistry::load
            ),
            DATA_NAME
        );
    }

    /**
     * Initialize the registry with server instance
     */
    public void initialize(MinecraftServer server) {
        this.server = server;
        rebuildIndices();
        performMaintenance();
        AdAstraMekanized.LOGGER.info("DynamicPlanetRegistry initialized with {} discovered planets, {} archived",
            discoveredPlanets.size(), archivedPlanets.size());
    }

    /**
     * Register a new dynamic planet
     */
    public boolean registerPlanet(DynamicPlanetData planetData) {
        if (getTotalPlanetCount() >= MAX_PLANETS) {
            AdAstraMekanized.LOGGER.warn("Cannot register planet {}: maximum limit of {} reached",
                planetData.getDisplayName(), MAX_PLANETS);
            return false;
        }

        if (discoveredPlanets.containsKey(planetData.getPlanetId()) ||
            archivedPlanets.containsKey(planetData.getPlanetId())) {
            AdAstraMekanized.LOGGER.warn("Planet {} already exists", planetData.getPlanetId());
            return false;
        }

        discoveredPlanets.put(planetData.getPlanetId(), planetData);
        updateIndices(planetData);
        setDirty();

        AdAstraMekanized.LOGGER.info("Registered new planet: {} ({})",
            planetData.getDisplayName(), planetData.getPlanetId());
        return true;
    }

    /**
     * Get a planet by ID (checks both active and archived)
     */
    public DynamicPlanetData getPlanet(ResourceLocation planetId) {
        DynamicPlanetData planet = discoveredPlanets.get(planetId);
        if (planet != null) {
            planet.updateAccessTime();
            updateAccessTimeIndex(planet);
            setDirty();
            return planet;
        }

        // Check archived planets
        CompressedPlanetData compressed = archivedPlanets.get(planetId);
        if (compressed != null) {
            // Move back to active planets
            planet = compressed.decompress();
            planet.updateAccessTime();
            discoveredPlanets.put(planetId, planet);
            archivedPlanets.remove(planetId);
            updateIndices(planet);
            setDirty();
            AdAstraMekanized.LOGGER.debug("Restored planet {} from archive", planetId);
            return planet;
        }

        return null;
    }

    /**
     * Mark a dimension as loaded
     */
    public void markDimensionLoaded(ResourceLocation planetId) {
        loadedDimensions.add(planetId);
        DynamicPlanetData planet = discoveredPlanets.get(planetId);
        if (planet != null) {
            planet.setLoaded(true);
            planet.updateAccessTime();
            updateAccessTimeIndex(planet);
            setDirty();
        }
    }

    /**
     * Mark a dimension as unloaded
     */
    public void markDimensionUnloaded(ResourceLocation planetId) {
        loadedDimensions.remove(planetId);
        DynamicPlanetData planet = discoveredPlanets.get(planetId);
        if (planet != null) {
            planet.setLoaded(false);
            setDirty();
        }
    }

    /**
     * Get all discovered planets (active only)
     */
    public Collection<DynamicPlanetData> getDiscoveredPlanets() {
        return Collections.unmodifiableCollection(discoveredPlanets.values());
    }

    /**
     * Get all loaded planets
     */
    public List<DynamicPlanetData> getLoadedPlanets() {
        return discoveredPlanets.values().stream()
            .filter(DynamicPlanetData::isLoaded)
            .collect(Collectors.toList());
    }

    /**
     * Search planets by name pattern
     */
    public List<DynamicPlanetData> searchPlanets(String namePattern) {
        String lowerPattern = namePattern.toLowerCase();
        return discoveredPlanets.values().stream()
            .filter(planet -> planet.getDisplayName().toLowerCase().contains(lowerPattern) ||
                           planet.getPlanetId().getPath().toLowerCase().contains(lowerPattern))
            .sorted(Comparator.comparing(DynamicPlanetData::getDisplayName))
            .collect(Collectors.toList());
    }

    /**
     * Get planets by dimension effects type
     */
    public List<DynamicPlanetData> getPlanetsByType(DimensionEffectsType type) {
        List<ResourceLocation> planetIds = typeIndex.getOrDefault(type, Collections.emptyList());
        return planetIds.stream()
            .map(discoveredPlanets::get)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    /**
     * Get planets by celestial type
     */
    public List<DynamicPlanetData> getPlanetsByCelestialType(CelestialType type) {
        List<ResourceLocation> planetIds = celestialIndex.getOrDefault(type, Collections.emptyList());
        return planetIds.stream()
            .map(discoveredPlanets::get)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    /**
     * Get planets sorted by access time (most recent first)
     */
    public List<DynamicPlanetData> getPlanetsByRecentAccess(int limit) {
        return accessTimeIndex.descendingMap().values().stream()
            .map(discoveredPlanets::get)
            .filter(Objects::nonNull)
            .limit(limit)
            .collect(Collectors.toList());
    }

    /**
     * Get least recently used planets for unloading
     */
    public List<DynamicPlanetData> getLeastRecentlyUsed(int count) {
        return accessTimeIndex.values().stream()
            .map(discoveredPlanets::get)
            .filter(Objects::nonNull)
            .filter(DynamicPlanetData::isLoaded)
            .limit(count)
            .collect(Collectors.toList());
    }

    /**
     * Get total planet count (active + archived)
     */
    public int getTotalPlanetCount() {
        return discoveredPlanets.size() + archivedPlanets.size();
    }

    /**
     * Get currently loaded dimension count
     */
    public int getLoadedDimensionCount() {
        return loadedDimensions.size();
    }

    /**
     * Check if can create more planets
     */
    public boolean canCreateMorePlanets() {
        return getTotalPlanetCount() < MAX_PLANETS;
    }

    /**
     * Check if can load more dimensions
     */
    public boolean canLoadMoreDimensions() {
        return getLoadedDimensionCount() < MAX_LOADED;
    }

    /**
     * Generate next unique planet ID
     */
    public ResourceLocation generateNextPlanetId() {
        planetCounter++;
        return ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "planet_" + String.format("%03d", planetCounter));
    }

    /**
     * Perform maintenance tasks (archiving, cleanup, etc.)
     */
    public void performMaintenance() {
        long currentTime = System.currentTimeMillis();

        // Only run maintenance every 5 minutes
        if (currentTime - lastMaintenanceTime < 5 * 60 * 1000) {
            return;
        }

        int archived = 0;
        int deleted = 0;

        // Archive old planets
        List<DynamicPlanetData> toArchive = discoveredPlanets.values().stream()
            .filter(DynamicPlanetData::canBeArchived)
            .collect(Collectors.toList());

        for (DynamicPlanetData planet : toArchive) {
            CompressedPlanetData compressed = CompressedPlanetData.compress(planet);
            archivedPlanets.put(planet.getPlanetId(), compressed);
            discoveredPlanets.remove(planet.getPlanetId());
            archived++;
        }

        // Delete very old archived planets
        List<ResourceLocation> toDelete = archivedPlanets.entrySet().stream()
            .filter(entry -> entry.getValue().canBeDeleted())
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());

        for (ResourceLocation planetId : toDelete) {
            archivedPlanets.remove(planetId);
            deleted++;
        }

        if (archived > 0 || deleted > 0) {
            rebuildIndices();
            setDirty();
            AdAstraMekanized.LOGGER.info("Maintenance: archived {} planets, deleted {} old planets", archived, deleted);
        }

        lastMaintenanceTime = currentTime;
    }

    /**
     * Rebuild search indices
     */
    private void rebuildIndices() {
        nameIndex.clear();
        typeIndex.clear();
        celestialIndex.clear();
        accessTimeIndex.clear();

        for (DynamicPlanetData planet : discoveredPlanets.values()) {
            updateIndices(planet);
        }
    }

    /**
     * Update search indices for a planet
     */
    private void updateIndices(DynamicPlanetData planet) {
        // Name index
        String[] nameWords = planet.getDisplayName().toLowerCase().split("\\s+");
        for (String word : nameWords) {
            nameIndex.computeIfAbsent(word, k -> new ArrayList<>()).add(planet.getPlanetId());
        }

        // Type indices
        typeIndex.computeIfAbsent(planet.getEffectsType(), k -> new ArrayList<>()).add(planet.getPlanetId());
        celestialIndex.computeIfAbsent(planet.getCelestialType(), k -> new ArrayList<>()).add(planet.getPlanetId());

        // Access time index
        updateAccessTimeIndex(planet);
    }

    /**
     * Update access time index for a planet
     */
    private void updateAccessTimeIndex(DynamicPlanetData planet) {
        // Remove old entry if exists
        accessTimeIndex.entrySet().removeIf(entry -> entry.getValue().equals(planet.getPlanetId()));
        // Add new entry
        accessTimeIndex.put(planet.getLastAccessed(), planet.getPlanetId());
    }

    /**
     * Get registry statistics
     */
    public RegistryStats getStats() {
        int loaded = getLoadedDimensionCount();
        int active = discoveredPlanets.size();
        int archived = archivedPlanets.size();
        int total = getTotalPlanetCount();

        Map<DimensionEffectsType, Integer> typeDistribution = new EnumMap<>(DimensionEffectsType.class);
        for (DimensionEffectsType type : DimensionEffectsType.values()) {
            typeDistribution.put(type, getPlanetsByType(type).size());
        }

        Map<CelestialType, Integer> celestialDistribution = new EnumMap<>(CelestialType.class);
        for (CelestialType type : CelestialType.values()) {
            celestialDistribution.put(type, getPlanetsByCelestialType(type).size());
        }

        return new RegistryStats(loaded, active, archived, total, typeDistribution, celestialDistribution);
    }

    // SavedData implementation

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        // Save basic state
        tag.putInt("planetCounter", planetCounter);
        tag.putLong("lastMaintenanceTime", lastMaintenanceTime);

        // Save discovered planets
        ListTag discoveredList = new ListTag();
        for (DynamicPlanetData planet : discoveredPlanets.values()) {
            discoveredList.add(planet.toNBT());
        }
        tag.put("discoveredPlanets", discoveredList);

        // Save archived planets
        ListTag archivedList = new ListTag();
        for (Map.Entry<ResourceLocation, CompressedPlanetData> entry : archivedPlanets.entrySet()) {
            CompoundTag archivedTag = new CompoundTag();
            archivedTag.putString("id", entry.getKey().toString());
            archivedTag.put("data", entry.getValue().toNBT());
            archivedList.add(archivedTag);
        }
        tag.put("archivedPlanets", archivedList);

        // Save loaded dimensions
        ListTag loadedList = new ListTag();
        for (ResourceLocation id : loadedDimensions) {
            CompoundTag loadedTag = new CompoundTag();
            loadedTag.putString("id", id.toString());
            loadedList.add(loadedTag);
        }
        tag.put("loadedDimensions", loadedList);

        return tag;
    }

    public static DynamicPlanetRegistry load(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        DynamicPlanetRegistry registry = new DynamicPlanetRegistry();

        // Load basic state
        registry.planetCounter = tag.getInt("planetCounter");
        registry.lastMaintenanceTime = tag.getLong("lastMaintenanceTime");

        // Load discovered planets
        if (tag.contains("discoveredPlanets", Tag.TAG_LIST)) {
            ListTag discoveredList = tag.getList("discoveredPlanets", Tag.TAG_COMPOUND);
            for (int i = 0; i < discoveredList.size(); i++) {
                CompoundTag planetTag = discoveredList.getCompound(i);
                DynamicPlanetData planet = DynamicPlanetData.fromNBT(planetTag);
                registry.discoveredPlanets.put(planet.getPlanetId(), planet);
            }
        }

        // Load archived planets
        if (tag.contains("archivedPlanets", Tag.TAG_LIST)) {
            ListTag archivedList = tag.getList("archivedPlanets", Tag.TAG_COMPOUND);
            for (int i = 0; i < archivedList.size(); i++) {
                CompoundTag archivedTag = archivedList.getCompound(i);
                ResourceLocation id = ResourceLocation.parse(archivedTag.getString("id"));
                CompressedPlanetData compressed = CompressedPlanetData.fromNBT(archivedTag.getCompound("data"));
                registry.archivedPlanets.put(id, compressed);
            }
        }

        // Load loaded dimensions
        if (tag.contains("loadedDimensions", Tag.TAG_LIST)) {
            ListTag loadedList = tag.getList("loadedDimensions", Tag.TAG_COMPOUND);
            for (int i = 0; i < loadedList.size(); i++) {
                CompoundTag loadedTag = loadedList.getCompound(i);
                ResourceLocation id = ResourceLocation.parse(loadedTag.getString("id"));
                registry.loadedDimensions.add(id);
            }
        }

        return registry;
    }

    /**
     * Registry statistics record
     */
    public record RegistryStats(
        int loadedDimensions,
        int activePlanets,
        int archivedPlanets,
        int totalPlanets,
        Map<DimensionEffectsType, Integer> typeDistribution,
        Map<CelestialType, Integer> celestialDistribution
    ) {}
}