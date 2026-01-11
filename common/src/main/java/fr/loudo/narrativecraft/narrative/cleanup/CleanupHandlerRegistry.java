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

package fr.loudo.narrativecraft.narrative.cleanup;

import fr.loudo.narrativecraft.NarrativeCraftMod;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Registry for cleanup handlers that manages registration and execution.
 * Handlers are executed in priority order (lowest first) when cleanup is triggered.
 *
 * <p>Thread-safety: This class uses CopyOnWriteArrayList for the handler list
 * to allow concurrent registration and iteration. The executeAll method
 * creates a snapshot for safe iteration.
 */
public class CleanupHandlerRegistry {

    private final List<CleanupHandler> handlers = new CopyOnWriteArrayList<>();

    /**
     * Registers a cleanup handler to be executed on state exit.
     *
     * @param handler the handler to register
     * @throws IllegalArgumentException if handler is null
     */
    public void register(CleanupHandler handler) {
        if (handler == null) {
            throw new IllegalArgumentException("CleanupHandler cannot be null");
        }
        handlers.add(handler);
        NarrativeCraftMod.LOGGER.debug("Registered cleanup handler: {} (priority: {})",
                handler.name(), handler.priority());
    }

    /**
     * Unregisters a cleanup handler.
     *
     * @param handler the handler to unregister
     * @return true if the handler was found and removed
     */
    public boolean unregister(CleanupHandler handler) {
        if (handler == null) {
            return false;
        }
        boolean removed = handlers.remove(handler);
        if (removed) {
            NarrativeCraftMod.LOGGER.debug("Unregistered cleanup handler: {}", handler.name());
        }
        return removed;
    }

    /**
     * Executes all registered handlers in priority order (lowest first).
     * Each handler is executed in a try-catch to ensure all handlers run
     * even if one fails. Handlers are cleared after execution.
     *
     * @return the number of handlers that executed successfully
     */
    public int executeAll() {
        if (handlers.isEmpty()) {
            return 0;
        }

        List<CleanupHandler> sorted = new ArrayList<>(handlers);
        sorted.sort(Comparator.comparingInt(CleanupHandler::priority));

        int successCount = 0;
        int totalCount = sorted.size();

        NarrativeCraftMod.LOGGER.debug("Executing {} cleanup handlers", totalCount);

        for (CleanupHandler handler : sorted) {
            try {
                handler.cleanup();
                successCount++;
                NarrativeCraftMod.LOGGER.debug("Cleanup handler {} completed successfully", handler.name());
            } catch (Exception e) {
                NarrativeCraftMod.LOGGER.error("Cleanup handler {} failed: {}",
                        handler.name(), e.getMessage(), e);
            }
        }

        handlers.clear();

        if (successCount < totalCount) {
            NarrativeCraftMod.LOGGER.warn("Cleanup completed with {} failures out of {} handlers",
                    totalCount - successCount, totalCount);
        } else {
            NarrativeCraftMod.LOGGER.debug("All {} cleanup handlers completed successfully", totalCount);
        }

        return successCount;
    }

    /**
     * Clears all registered handlers without executing them.
     * Use this only for reset scenarios, not normal cleanup flow.
     */
    public void clear() {
        int count = handlers.size();
        handlers.clear();
        if (count > 0) {
            NarrativeCraftMod.LOGGER.debug("Cleared {} cleanup handlers without execution", count);
        }
    }

    /**
     * @return the current number of registered handlers
     */
    public int size() {
        return handlers.size();
    }

    /**
     * @return true if no handlers are registered
     */
    public boolean isEmpty() {
        return handlers.isEmpty();
    }

    /**
     * Creates a named cleanup handler with default priority.
     *
     * @param name the handler name for logging
     * @param action the cleanup action to perform
     * @return a new CleanupHandler
     */
    public static CleanupHandler named(String name, Runnable action) {
        return named(name, CleanupHandler.DEFAULT_PRIORITY, action);
    }

    /**
     * Creates a named cleanup handler with specified priority.
     *
     * @param name the handler name for logging
     * @param priority the execution priority (lower = first)
     * @param action the cleanup action to perform
     * @return a new CleanupHandler
     */
    public static CleanupHandler named(String name, int priority, Runnable action) {
        return new CleanupHandler() {
            @Override
            public void cleanup() {
                action.run();
            }

            @Override
            public int priority() {
                return priority;
            }

            @Override
            public String name() {
                return name;
            }
        };
    }
}
