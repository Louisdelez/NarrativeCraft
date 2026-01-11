/*
 * NarrativeCraft - Create your own stories, easily, and freely in Minecraft.
 * Copyright (c) 2025 LOUDO and contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package fr.loudo.narrativecraft.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lightweight performance profiler for NarrativeCraft subsystems.
 *
 * Enable via NarrativeCraftConfig.enableProfiling = true
 *
 * Usage:
 *   NarrativeProfiler.start("subsystem_name");
 *   // ... code to measure ...
 *   NarrativeProfiler.stop("subsystem_name");
 *
 *   // At end of session or periodically:
 *   NarrativeProfiler.logSummary();
 */
public final class NarrativeProfiler {

    private static final Logger LOGGER = LoggerFactory.getLogger("NarrativeCraft/Profiler");

    // Enable/disable flag - checked at start of each measurement
    private static volatile boolean enabled = false;

    // Thread-local storage for in-progress timings
    private static final ThreadLocal<Map<String, Long>> activeTimings = ThreadLocal.withInitial(ConcurrentHashMap::new);

    // Accumulated statistics per subsystem
    private static final Map<String, SubsystemStats> stats = new ConcurrentHashMap<>();

    // Profiling categories
    public static final String TICK_SERVER = "tick.server";
    public static final String TICK_CLIENT = "tick.client";
    public static final String RECORDING = "recording";
    public static final String PLAYBACK = "playback";
    public static final String DIALOG = "dialog";
    public static final String INK_ACTIONS = "ink_actions";
    public static final String VALIDATION = "validation";
    public static final String CLEANUP = "cleanup";
    public static final String TEXT_EFFECTS = "text_effects";
    public static final String TRIGGERS = "triggers";

    private NarrativeProfiler() {
        // Utility class
    }

    /**
     * Enable or disable profiling.
     */
    public static void setEnabled(boolean enable) {
        enabled = enable;
        if (enable) {
            LOGGER.info("[Profiler] Enabled - collecting timing data");
        } else {
            LOGGER.info("[Profiler] Disabled");
        }
    }

    /**
     * Check if profiling is enabled.
     */
    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * Start timing a subsystem. Call stop() with same name to record.
     * No-op if profiling is disabled.
     */
    public static void start(String subsystem) {
        if (!enabled) return;
        activeTimings.get().put(subsystem, System.nanoTime());
    }

    /**
     * Stop timing a subsystem and record the duration.
     * No-op if profiling is disabled or start() wasn't called.
     */
    public static void stop(String subsystem) {
        if (!enabled) return;

        Long startTime = activeTimings.get().remove(subsystem);
        if (startTime == null) return;

        long durationNanos = System.nanoTime() - startTime;
        stats.computeIfAbsent(subsystem, SubsystemStats::new).record(durationNanos);
    }

    /**
     * Record a timing directly (useful when you already have the duration).
     */
    public static void record(String subsystem, long durationNanos) {
        if (!enabled) return;
        stats.computeIfAbsent(subsystem, SubsystemStats::new).record(durationNanos);
    }

    /**
     * Get statistics for a specific subsystem.
     */
    public static SubsystemStats getStats(String subsystem) {
        return stats.get(subsystem);
    }

    /**
     * Log a summary of all collected timing data.
     */
    public static void logSummary() {
        if (stats.isEmpty()) {
            LOGGER.info("[Profiler] No data collected");
            return;
        }

        LOGGER.info("[Profiler] === Performance Summary ===");
        LOGGER.info(
                "[Profiler] {:20s} {:>10s} {:>12s} {:>12s} {:>12s}",
                "Subsystem",
                "Calls",
                "Total (ms)",
                "Avg (us)",
                "Max (us)");
        LOGGER.info("[Profiler] {}", "-".repeat(70));

        stats.entrySet().stream()
                .sorted(Map.Entry.comparingByValue((a, b) -> Long.compare(b.getTotalNanos(), a.getTotalNanos())))
                .forEach(entry -> {
                    SubsystemStats s = entry.getValue();
                    LOGGER.info(
                            "[Profiler] {:20s} {:>10d} {:>12.2f} {:>12.2f} {:>12.2f}",
                            entry.getKey(),
                            s.getCallCount(),
                            s.getTotalNanos() / 1_000_000.0,
                            s.getAverageNanos() / 1_000.0,
                            s.getMaxNanos() / 1_000.0);
                });

        LOGGER.info("[Profiler] =============================");
    }

    /**
     * Reset all collected statistics.
     */
    public static void reset() {
        stats.clear();
        LOGGER.info("[Profiler] Statistics reset");
    }

    /**
     * Statistics container for a single subsystem.
     */
    public static class SubsystemStats {
        private final String name;
        private final AtomicLong callCount = new AtomicLong(0);
        private final AtomicLong totalNanos = new AtomicLong(0);
        private volatile long maxNanos = 0;

        public SubsystemStats(String name) {
            this.name = name;
        }

        public void record(long durationNanos) {
            callCount.incrementAndGet();
            totalNanos.addAndGet(durationNanos);

            // Update max (non-atomic but acceptable for profiling)
            if (durationNanos > maxNanos) {
                maxNanos = durationNanos;
            }
        }

        public String getName() {
            return name;
        }

        public long getCallCount() {
            return callCount.get();
        }

        public long getTotalNanos() {
            return totalNanos.get();
        }

        public double getAverageNanos() {
            long count = callCount.get();
            return count > 0 ? (double) totalNanos.get() / count : 0;
        }

        public long getMaxNanos() {
            return maxNanos;
        }
    }
}
