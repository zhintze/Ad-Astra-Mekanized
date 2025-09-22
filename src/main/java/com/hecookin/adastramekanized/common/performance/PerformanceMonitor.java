package com.hecookin.adastramekanized.common.performance;

import com.hecookin.adastramekanized.AdAstraMekanized;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Performance monitoring system for tracking operation times and identifying bottlenecks.
 */
public class PerformanceMonitor {

    private static final PerformanceMonitor INSTANCE = new PerformanceMonitor();

    // Performance tracking maps
    private final Map<String, AtomicLong> operationCounts = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> totalOperationTime = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> maxOperationTime = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> minOperationTime = new ConcurrentHashMap<>();

    // Memory tracking
    private final AtomicLong totalMemoryAllocations = new AtomicLong(0);
    private final AtomicLong peakMemoryUsage = new AtomicLong(0);

    private PerformanceMonitor() {
        // Private constructor for singleton
    }

    public static PerformanceMonitor getInstance() {
        return INSTANCE;
    }

    /**
     * Start timing an operation
     */
    public OperationTimer startOperation(String operationName) {
        return new OperationTimer(operationName);
    }

    /**
     * Record operation completion
     */
    void recordOperation(String operationName, long durationMs) {
        operationCounts.computeIfAbsent(operationName, k -> new AtomicLong(0)).incrementAndGet();
        totalOperationTime.computeIfAbsent(operationName, k -> new AtomicLong(0)).addAndGet(durationMs);

        // Update max time
        maxOperationTime.compute(operationName, (k, current) -> {
            if (current == null) return new AtomicLong(durationMs);
            long currentMax = current.get();
            if (durationMs > currentMax) {
                current.set(durationMs);
            }
            return current;
        });

        // Update min time
        minOperationTime.compute(operationName, (k, current) -> {
            if (current == null) return new AtomicLong(durationMs);
            long currentMin = current.get();
            if (durationMs < currentMin) {
                current.set(durationMs);
            }
            return current;
        });

        // Log slow operations
        if (durationMs > 1000) { // Operations taking more than 1 second
            AdAstraMekanized.LOGGER.warn("Slow operation detected: {} took {}ms", operationName, durationMs);
        }
    }

    /**
     * Record memory allocation
     */
    public void recordMemoryAllocation(long bytes) {
        totalMemoryAllocations.addAndGet(bytes);

        // Update peak memory if current usage is higher
        Runtime runtime = Runtime.getRuntime();
        long currentUsage = runtime.totalMemory() - runtime.freeMemory();
        long currentPeak = peakMemoryUsage.get();
        if (currentUsage > currentPeak) {
            peakMemoryUsage.set(currentUsage);
        }
    }

    /**
     * Get performance statistics for an operation
     */
    public OperationStats getOperationStats(String operationName) {
        long count = operationCounts.getOrDefault(operationName, new AtomicLong(0)).get();
        long totalTime = totalOperationTime.getOrDefault(operationName, new AtomicLong(0)).get();
        long maxTime = maxOperationTime.getOrDefault(operationName, new AtomicLong(0)).get();
        long minTime = minOperationTime.getOrDefault(operationName, new AtomicLong(Long.MAX_VALUE)).get();

        if (minTime == Long.MAX_VALUE) minTime = 0;

        long avgTime = count > 0 ? totalTime / count : 0;

        return new OperationStats(operationName, count, totalTime, avgTime, maxTime, minTime);
    }

    /**
     * Get all tracked operation names
     */
    public Set<String> getTrackedOperations() {
        return new HashSet<>(operationCounts.keySet());
    }

    /**
     * Get memory statistics
     */
    public MemoryStats getMemoryStats() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();

        return new MemoryStats(
            usedMemory,
            totalMemory,
            maxMemory,
            peakMemoryUsage.get(),
            totalMemoryAllocations.get()
        );
    }

    /**
     * Clear all performance statistics
     */
    public void clearStats() {
        operationCounts.clear();
        totalOperationTime.clear();
        maxOperationTime.clear();
        minOperationTime.clear();
        totalMemoryAllocations.set(0);
        peakMemoryUsage.set(0);

        AdAstraMekanized.LOGGER.info("Performance statistics cleared");
    }

    /**
     * Generate a performance report
     */
    public String generatePerformanceReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== Ad Astra Mekanized Performance Report ===\n");

        // Memory stats
        MemoryStats memStats = getMemoryStats();
        report.append("\nMemory Usage:\n");
        report.append(String.format("  Current: %d MB\n", memStats.usedMemory() / (1024 * 1024)));
        report.append(String.format("  Peak: %d MB\n", memStats.peakUsage() / (1024 * 1024)));
        report.append(String.format("  Max Available: %d MB\n", memStats.maxMemory() / (1024 * 1024)));
        report.append(String.format("  Total Allocations: %d MB\n", memStats.totalAllocations() / (1024 * 1024)));

        // Operation stats
        report.append("\nOperation Statistics:\n");
        getTrackedOperations().stream()
            .sorted()
            .forEach(opName -> {
                OperationStats stats = getOperationStats(opName);
                report.append(String.format("  %s: %d calls, avg %dms, max %dms, total %dms\n",
                    stats.operationName(), stats.count(), stats.averageTime(),
                    stats.maxTime(), stats.totalTime()));
            });

        return report.toString();
    }

    /**
     * Timer for tracking operation duration
     */
    public class OperationTimer implements AutoCloseable {
        private final String operationName;
        private final long startTime;

        public OperationTimer(String operationName) {
            this.operationName = operationName;
            this.startTime = System.currentTimeMillis();
        }

        @Override
        public void close() {
            long duration = System.currentTimeMillis() - startTime;
            recordOperation(operationName, duration);
        }
    }

    /**
     * Operation statistics record
     */
    public record OperationStats(
        String operationName,
        long count,
        long totalTime,
        long averageTime,
        long maxTime,
        long minTime
    ) {}

    /**
     * Memory statistics record
     */
    public record MemoryStats(
        long usedMemory,
        long totalMemory,
        long maxMemory,
        long peakUsage,
        long totalAllocations
    ) {}
}