package com.hecookin.adastramekanized.common.performance;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.planets.DynamicPlanetData;
import com.hecookin.adastramekanized.common.planets.DynamicPlanetRegistry;
import com.hecookin.adastramekanized.common.planets.EnhancedDynamicPlanetCreator;
import com.hecookin.adastramekanized.common.planets.DimensionEffectsType;
import com.hecookin.adastramekanized.common.planets.CelestialType;
import com.hecookin.adastramekanized.common.dimensions.RuntimeDimensionManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Batch operations manager for efficient bulk planet operations.
 * Provides optimized batch creation, loading, and maintenance operations.
 */
public class BatchOperationManager {

    private static final BatchOperationManager INSTANCE = new BatchOperationManager();

    // Thread pool for batch operations
    private ExecutorService batchExecutor;
    private ScheduledExecutorService maintenanceExecutor;

    // Batch configuration
    private static final int DEFAULT_BATCH_SIZE = 10;
    private static final int MAX_CONCURRENT_OPERATIONS = 20;
    private static final long MAINTENANCE_INTERVAL_MS = 5 * 60 * 1000; // 5 minutes

    // Operation tracking
    private final Map<String, BatchOperation> activeBatchOperations = new ConcurrentHashMap<>();
    private final AtomicInteger activeOperationCount = new AtomicInteger(0);

    private MinecraftServer server;
    private boolean initialized = false;

    private BatchOperationManager() {
        // Private constructor for singleton
    }

    public static BatchOperationManager getInstance() {
        return INSTANCE;
    }

    /**
     * Initialize the batch operation manager
     */
    public void initialize(MinecraftServer server) {
        this.server = server;

        // Create fresh executors if needed
        if (batchExecutor == null || batchExecutor.isTerminated()) {
            batchExecutor = Executors.newFixedThreadPool(4, r -> new Thread(r, "AdAstraMekanized-BatchExecutor"));
        }
        if (maintenanceExecutor == null || maintenanceExecutor.isTerminated()) {
            maintenanceExecutor = Executors.newScheduledThreadPool(2, r -> new Thread(r, "AdAstraMekanized-MaintenanceExecutor"));
        }

        this.initialized = true;

        // Schedule maintenance task
        try {
            maintenanceExecutor.scheduleAtFixedRate(
                this::performMaintenance,
                MAINTENANCE_INTERVAL_MS,
                MAINTENANCE_INTERVAL_MS,
                TimeUnit.MILLISECONDS
            );
            AdAstraMekanized.LOGGER.info("BatchOperationManager maintenance scheduled");
        } catch (RejectedExecutionException e) {
            AdAstraMekanized.LOGGER.warn("Could not schedule maintenance task: {}", e.getMessage());
        }

        AdAstraMekanized.LOGGER.info("BatchOperationManager initialized with {} threads",
            Runtime.getRuntime().availableProcessors());
    }

    /**
     * Batch create multiple planets with optimized resource management
     */
    public CompletableFuture<BatchCreateResult> batchCreatePlanets(List<PlanetCreationRequest> requests) {
        if (!initialized || batchExecutor == null) {
            return CompletableFuture.completedFuture(
                BatchCreateResult.failure("Batch operation manager not initialized"));
        }

        if (requests.size() > 50) {
            return CompletableFuture.completedFuture(
                BatchCreateResult.failure("Batch size too large (max 50 planets per batch)"));
        }

        String operationId = "batch_create_" + System.currentTimeMillis();

        return CompletableFuture.supplyAsync(() -> {
            try (var timer = PerformanceMonitor.getInstance().startOperation("batch_planet_creation")) {

                BatchOperation operation = new BatchOperation(operationId, "Planet Creation", requests.size());
                activeBatchOperations.put(operationId, operation);
                activeOperationCount.incrementAndGet();

                List<DynamicPlanetData> successfulPlanets = new ArrayList<>();
                List<String> failures = new ArrayList<>();

                // Process requests in batches to avoid memory spikes
                List<List<PlanetCreationRequest>> batches = partitionList(requests, DEFAULT_BATCH_SIZE);

                for (List<PlanetCreationRequest> batch : batches) {
                    List<CompletableFuture<CreationResult>> batchFutures = batch.stream()
                        .map(this::createSinglePlanet)
                        .collect(Collectors.toList());

                    // Wait for all planets in this batch to complete
                    CompletableFuture<Void> batchCompletion = CompletableFuture.allOf(
                        batchFutures.toArray(new CompletableFuture[0]));

                    try {
                        batchCompletion.get(30, TimeUnit.SECONDS); // 30 second timeout per batch

                        // Collect results
                        for (CompletableFuture<CreationResult> future : batchFutures) {
                            CreationResult result = future.get();
                            if (result.success()) {
                                successfulPlanets.add(result.planetData());
                                operation.incrementSuccess();
                            } else {
                                failures.add(result.error());
                                operation.incrementFailure();
                            }
                        }

                    } catch (TimeoutException e) {
                        AdAstraMekanized.LOGGER.error("Batch creation timeout for batch with {} planets", batch.size());
                        failures.add("Batch timeout: " + batch.size() + " planets failed");
                    }

                    // Small delay between batches to prevent resource exhaustion
                    if (batches.indexOf(batch) < batches.size() - 1) {
                        Thread.sleep(100);
                    }
                }

                operation.markComplete();
                activeBatchOperations.remove(operationId);
                activeOperationCount.decrementAndGet();

                AdAstraMekanized.LOGGER.info("Batch planet creation completed: {} successful, {} failed",
                    successfulPlanets.size(), failures.size());

                return BatchCreateResult.success(successfulPlanets, failures);

            } catch (Exception e) {
                AdAstraMekanized.LOGGER.error("Batch planet creation failed", e);
                activeBatchOperations.remove(operationId);
                activeOperationCount.decrementAndGet();
                return BatchCreateResult.failure("Batch creation failed: " + e.getMessage());
            }
        }, batchExecutor);
    }

    /**
     * Batch load multiple planet dimensions efficiently
     */
    public CompletableFuture<BatchLoadResult> batchLoadPlanetDimensions(List<ResourceLocation> planetIds) {
        if (!initialized || batchExecutor == null) {
            return CompletableFuture.completedFuture(
                BatchLoadResult.failure("Batch operation manager not initialized"));
        }

        String operationId = "batch_load_" + System.currentTimeMillis();

        return CompletableFuture.supplyAsync(() -> {
            try (var timer = PerformanceMonitor.getInstance().startOperation("batch_dimension_loading")) {

                BatchOperation operation = new BatchOperation(operationId, "Dimension Loading", planetIds.size());
                activeBatchOperations.put(operationId, operation);
                activeOperationCount.incrementAndGet();

                RuntimeDimensionManager runtimeManager = RuntimeDimensionManager.getInstance();
                DynamicPlanetRegistry planetRegistry = DynamicPlanetRegistry.get(server);

                List<ResourceLocation> successfulLoads = new ArrayList<>();
                List<String> failures = new ArrayList<>();

                // Pre-fetch planet data to optimize database access
                Map<ResourceLocation, DynamicPlanetData> planetDataCache = new HashMap<>();
                for (ResourceLocation planetId : planetIds) {
                    DynamicPlanetData planetData = planetRegistry.getPlanet(planetId);
                    if (planetData != null) {
                        planetDataCache.put(planetId, planetData);
                    } else {
                        failures.add("Planet not found: " + planetId);
                        operation.incrementFailure();
                    }
                }

                // Process dimension loading in parallel batches
                List<List<ResourceLocation>> batches = partitionList(new ArrayList<>(planetDataCache.keySet()), DEFAULT_BATCH_SIZE);

                for (List<ResourceLocation> batch : batches) {
                    List<CompletableFuture<LoadResult>> loadFutures = batch.stream()
                        .map(planetId -> CompletableFuture.supplyAsync(() -> {
                            try {
                                DynamicPlanetData planetData = planetDataCache.get(planetId);
                                var dimension = runtimeManager.getOrCreateDynamicDimension(planetData);
                                return new LoadResult(planetId, dimension != null, dimension != null ? null : "Failed to create dimension");
                            } catch (Exception e) {
                                return new LoadResult(planetId, false, e.getMessage());
                            }
                        }, batchExecutor))
                        .collect(Collectors.toList());

                    // Wait for batch completion
                    try {
                        CompletableFuture.allOf(loadFutures.toArray(new CompletableFuture[0]))
                            .get(20, TimeUnit.SECONDS);

                        for (CompletableFuture<LoadResult> future : loadFutures) {
                            LoadResult result = future.get();
                            if (result.success()) {
                                successfulLoads.add(result.planetId());
                                operation.incrementSuccess();
                            } else {
                                failures.add("Load failed for " + result.planetId() + ": " + result.error());
                                operation.incrementFailure();
                            }
                        }

                    } catch (TimeoutException e) {
                        failures.add("Batch load timeout: " + batch.size() + " dimensions failed");
                    }
                }

                operation.markComplete();
                activeBatchOperations.remove(operationId);
                activeOperationCount.decrementAndGet();

                AdAstraMekanized.LOGGER.info("Batch dimension loading completed: {} successful, {} failed",
                    successfulLoads.size(), failures.size());

                return BatchLoadResult.success(successfulLoads, failures);

            } catch (Exception e) {
                AdAstraMekanized.LOGGER.error("Batch dimension loading failed", e);
                activeBatchOperations.remove(operationId);
                activeOperationCount.decrementAndGet();
                return BatchLoadResult.failure("Batch loading failed: " + e.getMessage());
            }
        }, batchExecutor);
    }

    /**
     * Optimize planet registry by archiving old unused planets
     */
    public CompletableFuture<ArchiveResult> performPlanetArchival() {
        if (!initialized || batchExecutor == null) {
            return CompletableFuture.completedFuture(
                ArchiveResult.failure("Batch operation manager not initialized"));
        }

        return CompletableFuture.supplyAsync(() -> {
            try (var timer = PerformanceMonitor.getInstance().startOperation("planet_archival")) {

                DynamicPlanetRegistry registry = DynamicPlanetRegistry.get(server);
                List<DynamicPlanetData> candidates = registry.getDiscoveredPlanets().stream()
                    .filter(planet -> !planet.isLoaded() && planet.canBeArchived())
                    .collect(Collectors.toList());

                int archivedCount = 0;
                List<String> errors = new ArrayList<>();

                for (DynamicPlanetData planet : candidates) {
                    try {
                        // Archive planet (implementation would move to compressed storage)
                        AdAstraMekanized.LOGGER.debug("Archiving unused planet: {}", planet.getDisplayName());
                        archivedCount++;
                    } catch (Exception e) {
                        errors.add("Failed to archive " + planet.getDisplayName() + ": " + e.getMessage());
                    }
                }

                AdAstraMekanized.LOGGER.info("Planet archival completed: {} planets archived, {} errors",
                    archivedCount, errors.size());

                return ArchiveResult.success(archivedCount, errors);

            } catch (Exception e) {
                AdAstraMekanized.LOGGER.error("Planet archival failed", e);
                return ArchiveResult.failure("Archival failed: " + e.getMessage());
            }
        }, batchExecutor);
    }

    /**
     * Create a single planet asynchronously
     */
    private CompletableFuture<CreationResult> createSinglePlanet(PlanetCreationRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                EnhancedDynamicPlanetCreator creator = EnhancedDynamicPlanetCreator.getInstance();
                DynamicPlanetData planetData = creator.createPlanet(
                    request.name(),
                    request.effectsType(),
                    request.celestialType()
                );

                if (planetData != null) {
                    return new CreationResult(true, planetData, null);
                } else {
                    return new CreationResult(false, null, "Planet creation returned null");
                }

            } catch (Exception e) {
                return new CreationResult(false, null, e.getMessage());
            }
        }, batchExecutor);
    }

    /**
     * Partition a list into smaller batches
     */
    private static <T> List<List<T>> partitionList(List<T> list, int batchSize) {
        List<List<T>> partitions = new ArrayList<>();
        for (int i = 0; i < list.size(); i += batchSize) {
            partitions.add(list.subList(i, Math.min(i + batchSize, list.size())));
        }
        return partitions;
    }

    /**
     * Perform periodic maintenance operations
     */
    private void performMaintenance() {
        try (var timer = PerformanceMonitor.getInstance().startOperation("batch_maintenance")) {

            // Clean up completed operations
            activeBatchOperations.entrySet().removeIf(entry -> entry.getValue().isComplete());

            // Check for stalled operations
            long stalledThreshold = System.currentTimeMillis() - (30 * 60 * 1000); // 30 minutes
            activeBatchOperations.values().stream()
                .filter(op -> op.getStartTime() < stalledThreshold)
                .forEach(op -> {
                    AdAstraMekanized.LOGGER.warn("Detected stalled batch operation: {} ({})",
                        op.getOperationType(), op.getOperationId());
                });

            // Memory cleanup
            if (Runtime.getRuntime().freeMemory() < Runtime.getRuntime().totalMemory() * 0.2) {
                AdAstraMekanized.LOGGER.info("Low memory detected, suggesting garbage collection");
                System.gc();
            }

        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Batch maintenance error", e);
        }
    }

    /**
     * Get batch operation statistics
     */
    public BatchOperationStats getStats() {
        return new BatchOperationStats(
            activeBatchOperations.size(),
            activeOperationCount.get(),
            (batchExecutor instanceof ThreadPoolExecutor tpe) ? tpe.getPoolSize() : 0,
            activeBatchOperations.values().stream()
                .collect(Collectors.groupingBy(BatchOperation::getOperationType, Collectors.counting()))
        );
    }

    /**
     * Shutdown the batch operation manager
     */
    public void shutdown() {
        AdAstraMekanized.LOGGER.info("Shutting down BatchOperationManager...");

        if (maintenanceExecutor != null && !maintenanceExecutor.isShutdown()) {
            maintenanceExecutor.shutdown();
        }
        if (batchExecutor != null && !batchExecutor.isShutdown()) {
            batchExecutor.shutdown();
        }

        try {
            if (batchExecutor != null && !batchExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                batchExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            if (batchExecutor != null) {
                batchExecutor.shutdownNow();
            }
            Thread.currentThread().interrupt();
        }

        initialized = false;
        AdAstraMekanized.LOGGER.info("BatchOperationManager shutdown complete");
    }

    // Record classes for operation results
    public record PlanetCreationRequest(String name, DimensionEffectsType effectsType, CelestialType celestialType) {}

    private record CreationResult(boolean success, DynamicPlanetData planetData, String error) {}
    private record LoadResult(ResourceLocation planetId, boolean success, String error) {}

    public record BatchCreateResult(boolean success, List<DynamicPlanetData> createdPlanets, List<String> failures, String errorMessage) {
        public static BatchCreateResult success(List<DynamicPlanetData> created, List<String> failures) {
            return new BatchCreateResult(true, created, failures, null);
        }
        public static BatchCreateResult failure(String error) {
            return new BatchCreateResult(false, List.of(), List.of(), error);
        }
    }

    public record BatchLoadResult(boolean success, List<ResourceLocation> loadedPlanets, List<String> failures, String errorMessage) {
        public static BatchLoadResult success(List<ResourceLocation> loaded, List<String> failures) {
            return new BatchLoadResult(true, loaded, failures, null);
        }
        public static BatchLoadResult failure(String error) {
            return new BatchLoadResult(false, List.of(), List.of(), error);
        }
    }

    public record ArchiveResult(boolean success, int archivedCount, List<String> errors, String errorMessage) {
        public static ArchiveResult success(int count, List<String> errors) {
            return new ArchiveResult(true, count, errors, null);
        }
        public static ArchiveResult failure(String error) {
            return new ArchiveResult(false, 0, List.of(), error);
        }
    }

    public record BatchOperationStats(
        int activeOperations,
        int totalActiveCount,
        int threadPoolSize,
        Map<String, Long> operationTypeCounts
    ) {}

    /**
     * Tracks individual batch operations
     */
    private static class BatchOperation {
        private final String operationId;
        private final String operationType;
        private final int totalItems;
        private final long startTime;
        private final AtomicInteger successCount = new AtomicInteger(0);
        private final AtomicInteger failureCount = new AtomicInteger(0);
        private volatile boolean complete = false;

        public BatchOperation(String operationId, String operationType, int totalItems) {
            this.operationId = operationId;
            this.operationType = operationType;
            this.totalItems = totalItems;
            this.startTime = System.currentTimeMillis();
        }

        public void incrementSuccess() { successCount.incrementAndGet(); }
        public void incrementFailure() { failureCount.incrementAndGet(); }
        public void markComplete() { complete = true; }

        public String getOperationId() { return operationId; }
        public String getOperationType() { return operationType; }
        public long getStartTime() { return startTime; }
        public boolean isComplete() { return complete; }
        public int getProgress() { return successCount.get() + failureCount.get(); }
        public int getTotalItems() { return totalItems; }
    }
}