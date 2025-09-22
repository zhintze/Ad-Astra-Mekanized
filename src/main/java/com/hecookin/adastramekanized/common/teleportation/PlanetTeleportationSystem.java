package com.hecookin.adastramekanized.common.teleportation;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.api.planets.Planet;
import com.hecookin.adastramekanized.common.performance.PerformanceMonitor;
import com.hecookin.adastramekanized.common.planets.PlanetDiscoveryService;
import com.hecookin.adastramekanized.common.planets.DynamicPlanetData;
import com.hecookin.adastramekanized.common.planets.DynamicPlanetRegistry;
import com.hecookin.adastramekanized.common.dimensions.PlanetDimensionManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Comprehensive planet teleportation system with modular architecture.
 *
 * Provides safe, validated teleportation to any planet discovered by the mod,
 * with automatic dimension management, spawn point calculation, and safety checks.
 */
public class PlanetTeleportationSystem {

    private static final PlanetTeleportationSystem INSTANCE = new PlanetTeleportationSystem();

    private final Map<ResourceLocation, TeleportationCache> teleportCache = new ConcurrentHashMap<>();
    private final Set<UUID> activeTeleportations = ConcurrentHashMap.newKeySet();
    private MinecraftServer server;
    private boolean initialized = false;

    private PlanetTeleportationSystem() {
        // Private constructor for singleton
    }

    public static PlanetTeleportationSystem getInstance() {
        return INSTANCE;
    }

    /**
     * Initialize the teleportation system
     */
    public void initialize(MinecraftServer server) {
        this.server = server;
        this.initialized = true;
        AdAstraMekanized.LOGGER.info("PlanetTeleportationSystem initialized");
    }

    /**
     * Teleport a player to any planet discovered by the mod
     */
    public CompletableFuture<TeleportResult> teleportToAnyPlanet(ServerPlayer player, ResourceLocation planetId) {
        return teleportToAnyPlanet(player, planetId, null, TeleportMode.SAFE_SPAWN);
    }

    /**
     * Teleport a player to a dynamic planet (created via commands)
     */
    public CompletableFuture<TeleportResult> teleportToDynamicPlanet(ServerPlayer player, ResourceLocation planetId) {
        return teleportToDynamicPlanet(player, planetId, null, TeleportMode.SAFE_SPAWN);
    }

    /**
     * Teleport a player to a specific location on a dynamic planet
     */
    public CompletableFuture<TeleportResult> teleportToDynamicPlanet(ServerPlayer player, ResourceLocation planetId,
                                                                    Vec3 targetPosition, TeleportMode mode) {
        if (!initialized) {
            return CompletableFuture.completedFuture(
                TeleportResult.failure("Teleportation system not initialized"));
        }

        if (activeTeleportations.contains(player.getUUID())) {
            return CompletableFuture.completedFuture(
                TeleportResult.failure("Player is already teleporting"));
        }

        return CompletableFuture.supplyAsync(() -> {
            try (var timer = PerformanceMonitor.getInstance().startOperation("planet_teleportation")) {
                activeTeleportations.add(player.getUUID());
                try {
                    return performDynamicPlanetTeleportation(player, planetId, targetPosition, mode);
                } finally {
                    activeTeleportations.remove(player.getUUID());
                }
            }
        });
    }

    /**
     * Teleport a player to a specific location on any planet
     */
    public CompletableFuture<TeleportResult> teleportToAnyPlanet(ServerPlayer player, ResourceLocation planetId,
                                                                 Vec3 targetPosition, TeleportMode mode) {
        if (!initialized) {
            return CompletableFuture.completedFuture(
                TeleportResult.failure("Teleportation system not initialized"));
        }

        if (activeTeleportations.contains(player.getUUID())) {
            return CompletableFuture.completedFuture(
                TeleportResult.failure("Player is already teleporting"));
        }

        return CompletableFuture.supplyAsync(() -> {
            activeTeleportations.add(player.getUUID());
            try {
                return performTeleportation(player, planetId, targetPosition, mode);
            } finally {
                activeTeleportations.remove(player.getUUID());
            }
        });
    }

    /**
     * Core teleportation logic for dynamic planets
     */
    private TeleportResult performDynamicPlanetTeleportation(ServerPlayer player, ResourceLocation planetId,
                                                           Vec3 targetPosition, TeleportMode mode) {
        try {
            // Step 1: Get dynamic planet data
            DynamicPlanetData planetData = getDynamicPlanetData(planetId);
            if (planetData == null) {
                return TeleportResult.failure("Dynamic planet not found: " + planetId);
            }

            // Step 2: Validate teleportation safety for dynamic planet
            TeleportValidation validation = validateDynamicPlanetTeleportation(player, planetData, mode);
            if (!validation.isValid()) {
                return TeleportResult.failure(validation.getFailureReason());
            }

            // Step 3: Ensure dynamic dimension exists (may use Overworld as safe fallback)
            ServerLevel targetLevel = ensureDynamicDimensionExists(planetData);
            if (targetLevel == null) {
                return TeleportResult.failure("Failed to create or access dimension for " + planetData.getDisplayName());
            }

            // Check if we're using Overworld as fallback
            boolean usingFallback = targetLevel.dimension().equals(Level.OVERWORLD);

            // Step 4: Calculate safe spawn position
            Vec3 finalPosition = calculateTeleportPosition(targetLevel, planetData, targetPosition, mode);
            if (finalPosition == null) {
                return TeleportResult.failure("Could not find safe spawn location on " + planetData.getDisplayName());
            }

            // Step 5: Perform the teleportation
            boolean success = executeDynamicPlanetTeleportation(player, targetLevel, finalPosition, planetData);
            if (!success) {
                return TeleportResult.failure("Teleportation execution failed");
            }

            // Step 6: Cache the teleportation for future use
            updateTeleportationCache(planetId, targetLevel, finalPosition);

            // Step 7: Send feedback to player
            sendDynamicPlanetTeleportationFeedback(player, planetData, finalPosition, mode, usingFallback);

            return TeleportResult.successDynamic(planetData, finalPosition, targetLevel.dimension());

        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Dynamic planet teleportation failed for player {} to planet {}",
                player.getName().getString(), planetId, e);
            return TeleportResult.failure("Teleportation failed: " + e.getMessage());
        }
    }

    /**
     * Core teleportation logic
     */
    private TeleportResult performTeleportation(ServerPlayer player, ResourceLocation planetId,
                                               Vec3 targetPosition, TeleportMode mode) {
        try {
            // Step 1: Discover and validate the planet
            Planet planet = discoverPlanet(planetId);
            if (planet == null) {
                return TeleportResult.failure("Planet not found: " + planetId);
            }

            // Step 2: Validate teleportation safety
            TeleportValidation validation = validateTeleportation(player, planet, mode);
            if (!validation.isValid()) {
                return TeleportResult.failure(validation.getFailureReason());
            }

            // Step 3: Ensure dimension exists
            ServerLevel targetLevel = ensureDimensionExists(planet);
            if (targetLevel == null) {
                return TeleportResult.failure("Failed to create or access dimension for " + planet.displayName());
            }

            // Step 4: Calculate safe spawn position
            Vec3 finalPosition = calculateTeleportPosition(targetLevel, planet, targetPosition, mode);
            if (finalPosition == null) {
                return TeleportResult.failure("Could not find safe spawn location on " + planet.displayName());
            }

            // Step 5: Perform the teleportation
            boolean success = executeTeleportation(player, targetLevel, finalPosition, planet);
            if (!success) {
                return TeleportResult.failure("Teleportation execution failed");
            }

            // Step 6: Cache the teleportation for future use
            updateTeleportationCache(planetId, targetLevel, finalPosition);

            // Step 7: Send feedback to player
            sendTeleportationFeedback(player, planet, finalPosition, mode);

            return TeleportResult.success(planet, finalPosition, targetLevel.dimension());

        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Teleportation failed for player {} to planet {}",
                player.getName().getString(), planetId, e);
            return TeleportResult.failure("Teleportation failed: " + e.getMessage());
        }
    }

    /**
     * Get dynamic planet data from the registry
     */
    private DynamicPlanetData getDynamicPlanetData(ResourceLocation planetId) {
        if (server == null) {
            return null;
        }

        try {
            DynamicPlanetRegistry registry = DynamicPlanetRegistry.get(server);
            return registry.getPlanet(planetId);
        } catch (Exception e) {
            AdAstraMekanized.LOGGER.warn("Failed to get dynamic planet data for {}: {}", planetId, e.getMessage());
            return null;
        }
    }

    /**
     * Validate teleportation safety for dynamic planets
     */
    private TeleportValidation validateDynamicPlanetTeleportation(ServerPlayer player, DynamicPlanetData planetData, TeleportMode mode) {
        // Check basic requirements
        if (player.isDeadOrDying()) {
            return TeleportValidation.invalid("Cannot teleport dead players");
        }

        if (player.isSpectator()) {
            return TeleportValidation.invalid("Spectators cannot teleport to planets");
        }

        // Check planet-specific requirements based on mode
        if (mode == TeleportMode.STRICT_SURVIVAL) {
            if (!planetData.hasAtmosphere() && !hasLifeSupport(player)) {
                return TeleportValidation.invalid("Life support required for " + planetData.getDisplayName());
            }

            if (!planetData.isBreathable() && !hasLifeSupport(player)) {
                return TeleportValidation.invalid("Breathable atmosphere or life support required for " + planetData.getDisplayName());
            }

            if (planetData.getGravity() > 3.0f && !hasGravityProtection(player)) {
                return TeleportValidation.invalid("Gravity protection required for " + planetData.getDisplayName());
            }
        }

        return TeleportValidation.valid();
    }

    /**
     * Ensure dynamic planet dimension exists using new runtime dimension manager
     */
    private ServerLevel ensureDynamicDimensionExists(DynamicPlanetData planetData) {
        AdAstraMekanized.LOGGER.info("Checking dimension for planet: {} ({})",
            planetData.getDisplayName(), planetData.getPlanetId());

        try {
            // First try to find existing dimension from datapack
            ResourceLocation dimensionId = planetData.getPlanetId();
            ResourceKey<Level> dimensionKey = ResourceKey.create(Registries.DIMENSION, dimensionId);

            ServerLevel existingLevel = server.getLevel(dimensionKey);
            if (existingLevel != null) {
                AdAstraMekanized.LOGGER.info("Found existing dimension for planet: {}", planetData.getDisplayName());
                return existingLevel;
            }

            // Runtime dimension creation is disabled in NeoForge 1.21.1 - use Overworld as fallback
            AdAstraMekanized.LOGGER.warn("Dimension {} not found in datapacks, using Overworld as fallback for planet {}",
                dimensionId, planetData.getDisplayName());
            return server.getLevel(Level.OVERWORLD);

        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Failed to access dimension for planet {}: {}",
                planetData.getDisplayName(), e.getMessage(), e);
            // Return Overworld as ultimate fallback
            return server.getLevel(Level.OVERWORLD);
        }
    }

    /**
     * Calculate safe teleportation position for dynamic planets
     */
    private Vec3 calculateTeleportPosition(ServerLevel level, DynamicPlanetData planetData, Vec3 requestedPosition, TeleportMode mode) {
        if (requestedPosition != null && mode != TeleportMode.SAFE_SPAWN) {
            // Use requested position with safety checks
            BlockPos blockPos = BlockPos.containing(requestedPosition);
            if (isSafeLocation(level, blockPos)) {
                return requestedPosition;
            }
        }

        // Find safe spawn location
        return findSafeSpawnLocationForDynamicPlanet(level, planetData);
    }

    /**
     * Find safe spawn location for dynamic planets
     */
    private Vec3 findSafeSpawnLocationForDynamicPlanet(ServerLevel level, DynamicPlanetData planetData) {
        // Start from world spawn
        BlockPos spawn = level.getSharedSpawnPos();

        // Try cache first
        TeleportationCache cache = teleportCache.get(planetData.getPlanetId());
        if (cache != null && cache.isValid() && isSafeLocation(level, cache.getSpawnLocation())) {
            return Vec3.atCenterOf(cache.getSpawnLocation());
        }

        // Search for safe location around spawn
        for (int radius = 0; radius <= 50; radius += 10) {
            for (int angle = 0; angle < 360; angle += 45) {
                double radians = Math.toRadians(angle);
                int x = spawn.getX() + (int)(radius * Math.cos(radians));
                int z = spawn.getZ() + (int)(radius * Math.sin(radians));

                BlockPos testPos = findSurfaceAt(level, x, z);
                if (testPos != null && isSafeLocation(level, testPos)) {
                    return Vec3.atCenterOf(testPos);
                }
            }
        }

        // Fallback to spawn height
        return new Vec3(spawn.getX() + 0.5, Math.max(spawn.getY(), 100), spawn.getZ() + 0.5);
    }

    /**
     * Execute teleportation for dynamic planets
     */
    private boolean executeDynamicPlanetTeleportation(ServerPlayer player, ServerLevel targetLevel, Vec3 position, DynamicPlanetData planetData) {
        try {
            // Store original dimension for potential rollback
            ResourceKey<Level> originalDimension = player.level().dimension();
            Vec3 originalPosition = player.position();

            // Perform teleportation
            player.teleportTo(targetLevel, position.x, position.y, position.z,
                            player.getYRot(), player.getXRot());

            // Apply planet-specific effects
            applyDynamicPlanetEffects(player, planetData);

            AdAstraMekanized.LOGGER.info("Successfully teleported {} to {} at ({}, {}, {})",
                player.getName().getString(), planetData.getDisplayName(),
                position.x, position.y, position.z);

            return true;
        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Failed to execute dynamic planet teleportation", e);
            return false;
        }
    }

    /**
     * Apply dynamic planet-specific effects to the player
     */
    private void applyDynamicPlanetEffects(ServerPlayer player, DynamicPlanetData planetData) {
        // Send informational messages about planet conditions

        if (!planetData.hasAtmosphere()) {
            player.sendSystemMessage(Component.literal("§c⚠ Warning: This planet has no atmosphere!"));
        } else if (!planetData.isBreathable()) {
            player.sendSystemMessage(Component.literal("§e⚠ Warning: This planet's atmosphere is not breathable!"));
        }

        if (planetData.getGravity() != 1.0f) {
            player.sendSystemMessage(Component.literal("§b⚠ Gravity: " +
                String.format("%.2f", planetData.getGravity()) + "x Earth normal"));
        }

        if (planetData.getTemperature() < -50.0f) {
            player.sendSystemMessage(Component.literal("§9⚠ Warning: Extremely cold temperatures (" +
                String.format("%.1f", planetData.getTemperature()) + "°C)"));
        } else if (planetData.getTemperature() > 100.0f) {
            player.sendSystemMessage(Component.literal("§c⚠ Warning: Extremely hot temperatures (" +
                String.format("%.1f", planetData.getTemperature()) + "°C)"));
        }
    }

    /**
     * Send feedback to player after dynamic planet teleportation
     */
    private void sendDynamicPlanetTeleportationFeedback(ServerPlayer player, DynamicPlanetData planetData, Vec3 position, TeleportMode mode, boolean usingFallback) {
        if (usingFallback) {
            player.sendSystemMessage(Component.literal("§e⚠ Teleported to Overworld (planet dimension unavailable)"));
            player.sendSystemMessage(Component.literal("§7Planet: " + planetData.getDisplayName() + " - dimension files generated for future use"));
        } else {
            player.sendSystemMessage(Component.literal("§a✓ Welcome to " + planetData.getDisplayName() + "!"));
        }

        if (mode == TeleportMode.SAFE_SPAWN) {
            player.sendSystemMessage(Component.literal("§7Landed at safe spawn location"));
        }

        // Additional planet info
        String coords = String.format("%.1f, %.1f, %.1f", position.x, position.y, position.z);
        player.sendSystemMessage(Component.literal("§7Location: " + coords));

        if (!usingFallback) {
            // Planet type information (only show if we're on the actual planet)
            player.sendSystemMessage(Component.literal("§7Planet Type: " + planetData.getEffectsType().getDescription()));
            player.sendSystemMessage(Component.literal("§7Celestial Configuration: " + planetData.getCelestialType().getDescription()));
        }
    }

    /**
     * Discover a planet using the discovery service
     */
    private Planet discoverPlanet(ResourceLocation planetId) {
        PlanetDiscoveryService discoveryService = PlanetDiscoveryService.getInstance();

        // First check discovered planets
        Optional<Planet> discovered = discoveryService.getAllModPlanets().stream()
            .filter(planet -> planet.id().equals(planetId))
            .findFirst();

        if (discovered.isPresent()) {
            return discovered.get();
        }

        // Fallback: search by partial name match
        String searchTerm = planetId.getPath();
        List<Planet> matches = discoveryService.searchPlanets(searchTerm);
        if (!matches.isEmpty()) {
            AdAstraMekanized.LOGGER.info("Found planet by search: {} -> {}", searchTerm, matches.get(0).displayName());
            return matches.get(0);
        }

        return null;
    }

    /**
     * Validate teleportation safety and requirements
     */
    private TeleportValidation validateTeleportation(ServerPlayer player, Planet planet, TeleportMode mode) {
        // Check basic requirements
        if (player.isDeadOrDying()) {
            return TeleportValidation.invalid("Cannot teleport dead players");
        }

        if (player.isSpectator()) {
            return TeleportValidation.invalid("Spectators cannot teleport to planets");
        }

        // Check planet-specific requirements based on mode
        if (mode == TeleportMode.STRICT_SURVIVAL) {
            if (planet.atmosphere().requiresLifeSupport() && !hasLifeSupport(player)) {
                return TeleportValidation.invalid("Life support required for " + planet.displayName());
            }

            if (planet.properties().gravity() > 3.0f && !hasGravityProtection(player)) {
                return TeleportValidation.invalid("Gravity protection required for " + planet.displayName());
            }
        }

        return TeleportValidation.valid();
    }

    /**
     * Ensure the planet's dimension exists and is accessible with enhanced validation
     */
    private ServerLevel ensureDimensionExists(Planet planet) {
        ResourceLocation dimensionId = planet.getDimensionLocation();
        ResourceKey<Level> dimensionKey = ResourceKey.create(Registries.DIMENSION, dimensionId);

        AdAstraMekanized.LOGGER.info("Checking dimension for planet: {} at {}", planet.displayName(), dimensionKey.location());

        try {
            // Only try to get existing dimension from datapack - no runtime creation
            ServerLevel existingLevel = server.getLevel(dimensionKey);
            if (existingLevel != null) {
                AdAstraMekanized.LOGGER.info("Found existing dimension: {}", dimensionKey.location());
                return existingLevel;
            }

            // Runtime dimension creation is disabled in NeoForge 1.21.1 - use Overworld as fallback
            AdAstraMekanized.LOGGER.warn("Dimension {} not found in datapacks, using Overworld as fallback for planet {}",
                dimensionKey.location(), planet.displayName());
            return server.getLevel(Level.OVERWORLD);

        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Failed to access dimension for planet {}: {}",
                planet.displayName(), e.getMessage(), e);
            // Return Overworld as ultimate fallback
            return server.getLevel(Level.OVERWORLD);
        }
    }

    /**
     * Calculate safe teleportation position
     */
    private Vec3 calculateTeleportPosition(ServerLevel level, Planet planet, Vec3 requestedPosition, TeleportMode mode) {
        if (requestedPosition != null && mode != TeleportMode.SAFE_SPAWN) {
            // Use requested position with safety checks
            BlockPos blockPos = BlockPos.containing(requestedPosition);
            if (isSafeLocation(level, blockPos)) {
                return requestedPosition;
            }
        }

        // Find safe spawn location
        return findSafeSpawnLocation(level, planet);
    }

    /**
     * Find a safe spawn location on the planet
     */
    private Vec3 findSafeSpawnLocation(ServerLevel level, Planet planet) {
        // Start from world spawn
        BlockPos spawn = level.getSharedSpawnPos();

        // Try cache first
        TeleportationCache cache = teleportCache.get(planet.id());
        if (cache != null && cache.isValid() && isSafeLocation(level, cache.getSpawnLocation())) {
            return Vec3.atCenterOf(cache.getSpawnLocation());
        }

        // Search for safe location around spawn
        for (int radius = 0; radius <= 50; radius += 10) {
            for (int angle = 0; angle < 360; angle += 45) {
                double radians = Math.toRadians(angle);
                int x = spawn.getX() + (int)(radius * Math.cos(radians));
                int z = spawn.getZ() + (int)(radius * Math.sin(radians));

                BlockPos testPos = findSurfaceAt(level, x, z);
                if (testPos != null && isSafeLocation(level, testPos)) {
                    return Vec3.atCenterOf(testPos);
                }
            }
        }

        // Fallback to spawn height
        return new Vec3(spawn.getX() + 0.5, Math.max(spawn.getY(), 100), spawn.getZ() + 0.5);
    }

    /**
     * Find surface level at given coordinates
     */
    private BlockPos findSurfaceAt(ServerLevel level, int x, int z) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, level.getMaxBuildHeight(), z);

        // Move down until we find solid ground
        while (pos.getY() > level.getMinBuildHeight()) {
            BlockState state = level.getBlockState(pos);
            if (!state.isAir() && state.canOcclude()) {
                // Found solid ground, return position above it
                return pos.above().immutable();
            }
            pos.move(0, -1, 0);
        }

        return null;
    }

    /**
     * Check if a location is safe for teleportation
     */
    private boolean isSafeLocation(ServerLevel level, BlockPos pos) {
        // Check if there's enough space for the player
        BlockState ground = level.getBlockState(pos.below());
        BlockState feet = level.getBlockState(pos);
        BlockState head = level.getBlockState(pos.above());

        // Need solid ground and air space for player
        return !ground.isAir() && ground.canOcclude() &&
               feet.isAir() && head.isAir() &&
               pos.getY() > level.getMinBuildHeight() &&
               pos.getY() < level.getMaxBuildHeight() - 1;
    }

    /**
     * Execute the actual teleportation
     */
    private boolean executeTeleportation(ServerPlayer player, ServerLevel targetLevel, Vec3 position, Planet planet) {
        try {
            // Store original dimension for potential rollback
            ResourceKey<Level> originalDimension = player.level().dimension();
            Vec3 originalPosition = player.position();

            // Perform teleportation
            player.teleportTo(targetLevel, position.x, position.y, position.z,
                            player.getYRot(), player.getXRot());

            // Apply planet-specific effects
            applyPlanetEffects(player, planet);

            AdAstraMekanized.LOGGER.info("Successfully teleported {} to {} at ({}, {}, {})",
                player.getName().getString(), planet.displayName(),
                position.x, position.y, position.z);

            return true;
        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Failed to execute teleportation", e);
            return false;
        }
    }

    /**
     * Apply planet-specific effects to the player
     */
    private void applyPlanetEffects(ServerPlayer player, Planet planet) {
        // This could be expanded to apply environmental effects
        // For now, just send informational messages

        if (!planet.isHabitable()) {
            player.sendSystemMessage(Component.literal("§c⚠ Warning: This planet is hostile to life!"));
        }

        if (planet.atmosphere().requiresLifeSupport()) {
            player.sendSystemMessage(Component.literal("§e⚠ Life support recommended on this planet"));
        }

        if (planet.properties().gravity() != 1.0f) {
            player.sendSystemMessage(Component.literal("§b⚠ Gravity: " +
                String.format("%.2f", planet.properties().gravity()) + "x Earth normal"));
        }
    }

    /**
     * Update teleportation cache
     */
    private void updateTeleportationCache(ResourceLocation planetId, ServerLevel level, Vec3 position) {
        BlockPos blockPos = BlockPos.containing(position);
        teleportCache.put(planetId, new TeleportationCache(level.dimension(), blockPos, System.currentTimeMillis()));
    }

    /**
     * Send feedback to player after teleportation
     */
    private void sendTeleportationFeedback(ServerPlayer player, Planet planet, Vec3 position, TeleportMode mode) {
        player.sendSystemMessage(Component.literal("§a✓ Welcome to " + planet.displayName() + "!"));

        if (mode == TeleportMode.SAFE_SPAWN) {
            player.sendSystemMessage(Component.literal("§7Landed at safe spawn location"));
        }

        // Additional planet info
        String coords = String.format("%.1f, %.1f, %.1f", position.x, position.y, position.z);
        player.sendSystemMessage(Component.literal("§7Location: " + coords));
    }

    /**
     * Check if player has life support equipment (placeholder)
     */
    private boolean hasLifeSupport(ServerPlayer player) {
        // TODO: Implement actual life support checking with Mekanism integration
        return false;
    }

    /**
     * Check if player has gravity protection (placeholder)
     */
    private boolean hasGravityProtection(ServerPlayer player) {
        // TODO: Implement actual gravity protection checking
        return false;
    }

    /**
     * Get all teleportable planets (static planets)
     */
    public List<Planet> getAllTeleportablePlanets() {
        return new ArrayList<>(PlanetDiscoveryService.getInstance().getAllModPlanets());
    }

    /**
     * Get all dynamic planets available for teleportation
     */
    public List<DynamicPlanetData> getAllDynamicPlanets() {
        if (server == null) {
            return new ArrayList<>();
        }

        try {
            DynamicPlanetRegistry registry = DynamicPlanetRegistry.get(server);
            return new ArrayList<>(registry.getDiscoveredPlanets());
        } catch (Exception e) {
            AdAstraMekanized.LOGGER.warn("Failed to get dynamic planets: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Get combined list of all teleportable destinations (static + dynamic)
     */
    public List<String> getAllTeleportableDestinations() {
        List<String> destinations = new ArrayList<>();

        // Add static planets
        getAllTeleportablePlanets().forEach(planet ->
            destinations.add(planet.id().toString() + " (static)")
        );

        // Add dynamic planets
        getAllDynamicPlanets().forEach(planetData ->
            destinations.add(planetData.getPlanetId().toString() + " (dynamic)")
        );

        return destinations;
    }

    /**
     * Get teleportable planets by category
     */
    public List<Planet> getTeleportablePlanetsByCategory(String category) {
        return new ArrayList<>(PlanetDiscoveryService.getInstance().getPlanetsByCategory(category));
    }

    /**
     * Clear teleportation cache
     */
    public void clearCache() {
        teleportCache.clear();
        AdAstraMekanized.LOGGER.info("Teleportation cache cleared");
    }

    /**
     * Get system statistics
     */
    public TeleportStats getStats() {
        int staticPlanets = PlanetDiscoveryService.getInstance().getAllModPlanets().size();
        int dynamicPlanets = getAllDynamicPlanets().size();

        return new TeleportStats(
            teleportCache.size(),
            activeTeleportations.size(),
            staticPlanets,
            dynamicPlanets,
            staticPlanets + dynamicPlanets,
            initialized
        );
    }

    /**
     * Teleportation modes
     */
    public enum TeleportMode {
        SAFE_SPAWN,         // Always find safe spawn location
        EXACT_POSITION,     // Use exact coordinates if safe
        STRICT_SURVIVAL     // Apply survival requirements and checks
    }

    /**
     * Teleportation validation result
     */
    public static class TeleportValidation {
        private final boolean valid;
        private final String failureReason;

        private TeleportValidation(boolean valid, String failureReason) {
            this.valid = valid;
            this.failureReason = failureReason;
        }

        public static TeleportValidation valid() {
            return new TeleportValidation(true, null);
        }

        public static TeleportValidation invalid(String reason) {
            return new TeleportValidation(false, reason);
        }

        public boolean isValid() { return valid; }
        public String getFailureReason() { return failureReason; }
    }

    /**
     * Teleportation cache entry
     */
    private static class TeleportationCache {
        private final ResourceKey<Level> dimension;
        private final BlockPos spawnLocation;
        private final long timestamp;
        private static final long CACHE_DURATION = 30 * 60 * 1000; // 30 minutes

        public TeleportationCache(ResourceKey<Level> dimension, BlockPos spawnLocation, long timestamp) {
            this.dimension = dimension;
            this.spawnLocation = spawnLocation;
            this.timestamp = timestamp;
        }

        public boolean isValid() {
            return System.currentTimeMillis() - timestamp < CACHE_DURATION;
        }

        public ResourceKey<Level> getDimension() { return dimension; }
        public BlockPos getSpawnLocation() { return spawnLocation; }
    }

    /**
     * Teleportation result
     */
    public static class TeleportResult {
        private final boolean success;
        private final String message;
        private final Planet planet;
        private final DynamicPlanetData dynamicPlanet;
        private final Vec3 position;
        private final ResourceKey<Level> dimension;
        private final boolean isDynamic;

        private TeleportResult(boolean success, String message, Planet planet, DynamicPlanetData dynamicPlanet,
                             Vec3 position, ResourceKey<Level> dimension, boolean isDynamic) {
            this.success = success;
            this.message = message;
            this.planet = planet;
            this.dynamicPlanet = dynamicPlanet;
            this.position = position;
            this.dimension = dimension;
            this.isDynamic = isDynamic;
        }

        public static TeleportResult success(Planet planet, Vec3 position, ResourceKey<Level> dimension) {
            return new TeleportResult(true, "Teleportation successful", planet, null, position, dimension, false);
        }

        public static TeleportResult successDynamic(DynamicPlanetData planetData, Vec3 position, ResourceKey<Level> dimension) {
            return new TeleportResult(true, "Dynamic planet teleportation successful", null, planetData, position, dimension, true);
        }

        public static TeleportResult failure(String message) {
            return new TeleportResult(false, message, null, null, null, null, false);
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Planet getPlanet() { return planet; }
        public DynamicPlanetData getDynamicPlanet() { return dynamicPlanet; }
        public Vec3 getPosition() { return position; }
        public ResourceKey<Level> getDimension() { return dimension; }
        public boolean isDynamic() { return isDynamic; }

        /**
         * Get display name regardless of planet type
         */
        public String getDestinationName() {
            if (isDynamic && dynamicPlanet != null) {
                return dynamicPlanet.getDisplayName();
            } else if (planet != null) {
                return planet.displayName();
            }
            return "Unknown";
        }
    }

    /**
     * System statistics
     */
    public record TeleportStats(
        int cachedLocations,
        int activeTeleportations,
        int staticPlanets,
        int dynamicPlanets,
        int totalAvailablePlanets,
        boolean initialized
    ) {
        public String getStatusSummary() {
            return String.format("Teleportation System: %s | Destinations: %d static + %d dynamic = %d total | Cache: %d | Active: %d",
                initialized ? "Ready" : "Not Initialized",
                staticPlanets, dynamicPlanets, totalAvailablePlanets,
                cachedLocations, activeTeleportations);
        }
    }
}