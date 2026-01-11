# Contract: Cleanup Handler System

**Version**: 1.0.0
**Date**: 2026-01-09

## Overview

Cleanup handlers guarantee player state restoration after narrative events, regardless
of how the event ends (success, error, disconnection).

## Core Principle

**Register BEFORE Modify**: Every state modification must have its cleanup handler
registered BEFORE the modification occurs.

```java
// CORRECT
cleanupRegistry.register(handler);  // 1. Register cleanup
modifyPlayerState();                // 2. Then modify

// WRONG - Risk of stuck state
modifyPlayerState();                // 1. Modify
cleanupRegistry.register(handler);  // 2. Then register (too late if error occurs)
```

## Interface Definition

```java
package fr.loudo.narrativecraft.narrative.cleanup;

/**
 * Registry for cleanup handlers associated with a player session.
 */
public interface CleanupHandlerRegistry {

    /**
     * Register a cleanup handler.
     *
     * @param playerId Player UUID
     * @param handler Handler to register
     * @throws IllegalArgumentException if handler with same ID already registered
     */
    void register(UUID playerId, CleanupHandler handler);

    /**
     * Unregister a specific handler (optional cleanup path).
     *
     * @param playerId Player UUID
     * @param handlerId Handler ID to remove
     * @return true if handler was found and removed
     */
    boolean unregister(UUID playerId, String handlerId);

    /**
     * Execute all handlers for a player in priority order.
     * Clears the registry after execution.
     *
     * @param playerId Player UUID
     * @return CleanupResult with execution details
     */
    CleanupResult executeAll(UUID playerId);

    /**
     * Clear all handlers without executing (force reset scenario).
     *
     * @param playerId Player UUID
     */
    void clearAll(UUID playerId);

    /**
     * Get count of registered handlers.
     *
     * @param playerId Player UUID
     * @return Number of handlers
     */
    int getHandlerCount(UUID playerId);
}
```

## Cleanup Handler Interface

```java
package fr.loudo.narrativecraft.narrative.cleanup;

/**
 * Individual cleanup operation.
 */
@FunctionalInterface
public interface CleanupHandler {

    /**
     * Execute the cleanup operation.
     * Must be idempotent - safe to call multiple times.
     * Must not throw exceptions - catch and log internally.
     */
    void cleanup();

    /**
     * Execution priority (lower = first).
     */
    default int priority() {
        return 100;
    }

    /**
     * Unique identifier for logging and deduplication.
     */
    default String getId() {
        return this.getClass().getSimpleName() + "@" + System.identityHashCode(this);
    }

    /**
     * Description for debugging.
     */
    default String getDescription() {
        return getId();
    }
}
```

## Priority Conventions

| Priority | Category | Examples |
|----------|----------|----------|
| 0-9 | Critical | Emergency state reset |
| 10-19 | HUD | Dialog overlay, health bar, crosshair |
| 20-29 | Camera | Camera lock, angle reset, FOV |
| 30-39 | Input | Mouse capture, keyboard lock |
| 40-49 | Movement | Speed modifiers, flight, teleport |
| 50-59 | Audio | Narrative music, sound effects |
| 60-79 | Visual | Particles, shader effects, fades |
| 80-99 | Data | Temporary variables, markers |
| 100+ | Session | Story state, progress tracking |

## Cleanup Result

```java
package fr.loudo.narrativecraft.narrative.cleanup;

/**
 * Result of cleanup execution.
 */
public record CleanupResult(
    int totalHandlers,
    int successCount,
    int failureCount,
    List<CleanupFailure> failures,
    Duration totalDuration
) {
    public boolean isFullySuccessful() {
        return failureCount == 0;
    }
}

public record CleanupFailure(
    String handlerId,
    String description,
    Exception exception,
    Duration duration
) {}
```

## Built-in Handlers

### HUD Cleanup (Priority 10)

```java
public class HudCleanupHandler implements CleanupHandler {
    private final HudManager hudManager;
    private final UUID playerId;

    @Override
    public void cleanup() {
        try {
            hudManager.resetToDefault(playerId);
        } catch (Exception e) {
            LOGGER.error("HUD cleanup failed for {}", playerId, e);
            // Don't rethrow - continue with other handlers
        }
    }

    @Override
    public int priority() {
        return 10;
    }

    @Override
    public String getId() {
        return "hud-reset-" + playerId;
    }
}
```

### Camera Cleanup (Priority 20)

```java
public class CameraCleanupHandler implements CleanupHandler {
    private final CameraController cameraController;
    private final UUID playerId;

    @Override
    public void cleanup() {
        try {
            cameraController.releaseControl(playerId);
            cameraController.resetToPlayerView(playerId);
        } catch (Exception e) {
            LOGGER.error("Camera cleanup failed for {}", playerId, e);
        }
    }

    @Override
    public int priority() {
        return 20;
    }

    @Override
    public String getId() {
        return "camera-release-" + playerId;
    }
}
```

### Input Cleanup (Priority 30)

```java
public class InputCleanupHandler implements CleanupHandler {
    private final InputCaptureManager inputManager;
    private final UUID playerId;

    @Override
    public void cleanup() {
        try {
            inputManager.releaseAllCaptures(playerId);
            inputManager.enablePlayerInput(playerId);
        } catch (Exception e) {
            LOGGER.error("Input cleanup failed for {}", playerId, e);
        }
    }

    @Override
    public int priority() {
        return 30;
    }

    @Override
    public String getId() {
        return "input-release-" + playerId;
    }
}
```

## Usage Patterns

### Pattern 1: Simple Cleanup

```java
// Single cleanup action
registry.register(playerId, () -> hudManager.hideOverlay(playerId));
```

### Pattern 2: Multi-Step Cleanup

```java
// Sequential cleanup
registry.register(playerId, new CleanupHandler() {
    @Override
    public void cleanup() {
        // Step 1
        fadeScreen.cancel();
        // Step 2
        particleSystem.stopAll();
        // Step 3
        soundManager.fadeOut(500);
    }

    @Override
    public int priority() {
        return 60; // Visual category
    }
});
```

### Pattern 3: Conditional Cleanup

```java
// Only cleanup if state was modified
if (cameraWasLocked) {
    registry.register(playerId, new CameraCleanupHandler(cameraController, playerId));
}
```

### Pattern 4: Factory Method

```java
public class CleanupHandlers {
    public static CleanupHandler hudReset(HudManager hud, UUID player) {
        return new CleanupHandler() {
            @Override
            public void cleanup() {
                hud.resetToDefault(player);
            }
            @Override
            public int priority() { return 10; }
            @Override
            public String getId() { return "hud-reset-" + player; }
        };
    }
}

// Usage
registry.register(playerId, CleanupHandlers.hudReset(hudManager, playerId));
```

## Error Handling

Cleanup handlers must never propagate exceptions:

```java
@Override
public void cleanup() {
    try {
        // Actual cleanup logic
        riskyOperation();
    } catch (SpecificException e) {
        LOGGER.warn("Expected issue during cleanup: {}", e.getMessage());
        // Optionally try fallback
        tryFallback();
    } catch (Exception e) {
        LOGGER.error("Cleanup failed: {}", getDescription(), e);
        // Log and continue - don't rethrow
    }
}
```

## Testing Contract

```java
@Test
void cleanup_shouldBeIdempotent() {
    CleanupHandler handler = new MyCleanupHandler();

    // First call
    handler.cleanup();
    assertStateIsClean();

    // Second call should not throw or corrupt state
    handler.cleanup();
    assertStateIsClean();
}

@Test
void cleanup_shouldNotThrowExceptions() {
    CleanupHandler handler = new BrokenCleanupHandler(); // Simulates failure

    // Should not throw
    assertDoesNotThrow(() -> handler.cleanup());
}

@Test
void cleanup_executionOrder() {
    List<String> executionOrder = new ArrayList<>();

    registry.register(playerId, handlerWithPriority(30, () -> executionOrder.add("input")));
    registry.register(playerId, handlerWithPriority(10, () -> executionOrder.add("hud")));
    registry.register(playerId, handlerWithPriority(20, () -> executionOrder.add("camera")));

    registry.executeAll(playerId);

    assertEquals(List.of("hud", "camera", "input"), executionOrder);
}
```

## Metrics

| Metric | Type | Description |
|--------|------|-------------|
| `narrative.cleanup.handlers_registered` | Counter | Total handlers registered |
| `narrative.cleanup.handlers_executed` | Counter | Total handlers executed |
| `narrative.cleanup.failures` | Counter | Handlers that threw exceptions |
| `narrative.cleanup.duration_ms` | Histogram | Time per cleanup execution |
