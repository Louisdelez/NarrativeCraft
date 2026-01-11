# Contract: Narrative State Machine

**Version**: 1.0.0
**Date**: 2026-01-09

## Overview

The Narrative State Machine manages player experience modes, ensuring safe transitions
and guaranteed cleanup on exit.

## Interface Definition

```java
package fr.loudo.narrativecraft.narrative.state;

/**
 * Central manager for narrative state transitions.
 * Thread-safe for server-side usage.
 */
public interface NarrativeStateManager {

    /**
     * Get current state for the local player (client) or specified player (server).
     *
     * @param playerId Player UUID, null for local client player
     * @return Current NarrativeState, never null (defaults to GAMEPLAY)
     */
    NarrativeState getCurrentState(@Nullable UUID playerId);

    /**
     * Transition to a new state.
     *
     * @param playerId Target player UUID
     * @param state Target state
     * @param context State-specific context data
     * @throws IllegalStateException if transition is invalid
     * @throws NullPointerException if state is null
     */
    void enterState(UUID playerId, NarrativeState state, StateContext context);

    /**
     * Exit current state and return to GAMEPLAY.
     * Executes all registered cleanup handlers in priority order.
     *
     * @param playerId Target player UUID
     */
    void exitState(UUID playerId);

    /**
     * Force reset to GAMEPLAY, clearing all handlers.
     * Use only for error recovery.
     *
     * @param playerId Target player UUID
     */
    void forceReset(UUID playerId);

    /**
     * Register a cleanup handler for the current state.
     * Handler will be called when exitState() is invoked.
     *
     * @param playerId Target player UUID
     * @param handler Cleanup handler to register
     * @throws IllegalStateException if player is in GAMEPLAY state
     */
    void registerCleanupHandler(UUID playerId, CleanupHandler handler);

    /**
     * Check if a transition is valid.
     *
     * @param from Source state
     * @param to Target state
     * @return true if transition is allowed
     */
    boolean isValidTransition(NarrativeState from, NarrativeState to);
}
```

## State Enum

```java
package fr.loudo.narrativecraft.narrative.state;

public enum NarrativeState {
    /** Default state - normal player control */
    GAMEPLAY,

    /** Dialog UI active, player input captured for choices */
    DIALOGUE,

    /** Cutscene playing, camera locked, player movement disabled */
    CUTSCENE,

    /** Recording mode - capturing player actions */
    RECORDING,

    /** Playback mode - replaying recorded actions */
    PLAYBACK
}
```

## State Context

```java
package fr.loudo.narrativecraft.narrative.state;

/**
 * Context data for state transitions.
 * Implementations are state-specific.
 */
public interface StateContext {
    /** Source that initiated the transition */
    String getSource();

    /** Timestamp of transition */
    Instant getTimestamp();
}

// Example implementations:
public record DialogueContext(
    String source,
    Instant timestamp,
    Story story,
    String dialogueId
) implements StateContext {}

public record CutsceneContext(
    String source,
    Instant timestamp,
    String cutsceneId,
    boolean skipEnabled
) implements StateContext {}
```

## Valid Transitions

| From | To | Condition |
|------|----|-----------|
| GAMEPLAY | DIALOGUE | Story loaded |
| GAMEPLAY | CUTSCENE | Cutscene available |
| GAMEPLAY | RECORDING | Editor mode |
| GAMEPLAY | PLAYBACK | Recording available |
| DIALOGUE | GAMEPLAY | Always (exit) |
| DIALOGUE | CUTSCENE | In-dialogue cutscene trigger |
| CUTSCENE | GAMEPLAY | Always (exit) |
| CUTSCENE | DIALOGUE | Post-cutscene dialogue |
| RECORDING | GAMEPLAY | Always (exit) |
| PLAYBACK | GAMEPLAY | Always (exit) |

**Invalid Transitions** (throw IllegalStateException):
- Any state → GAMEPLAY (use exitState() instead)
- RECORDING ↔ PLAYBACK (mutually exclusive)
- Any state → same state (no-op)

## Cleanup Handler Contract

```java
package fr.loudo.narrativecraft.narrative.state;

/**
 * Handler for state cleanup operations.
 * Must be idempotent and exception-safe.
 */
public interface CleanupHandler {
    /**
     * Execute cleanup operation.
     * Called automatically on state exit.
     * Must not throw exceptions (log errors internally).
     */
    void cleanup();

    /**
     * Execution priority. Lower values run first.
     * Default: 100
     *
     * Standard priorities:
     * - 10: HUD restoration
     * - 20: Camera reset
     * - 30: Input release
     * - 50: Audio stop
     * - 100: Session cleanup
     */
    default int priority() {
        return 100;
    }

    /**
     * Unique identifier for logging/debugging.
     */
    String getId();
}
```

## Usage Example

```java
// Entering dialogue state
stateManager.registerCleanupHandler(playerId, new CleanupHandler() {
    @Override
    public void cleanup() {
        hudManager.hideDialogOverlay(playerId);
    }

    @Override
    public int priority() {
        return 10; // HUD cleanup first
    }

    @Override
    public String getId() {
        return "dialogue-hud-cleanup";
    }
});

hudManager.showDialogOverlay(playerId); // Do this AFTER registering cleanup
stateManager.enterState(playerId, NarrativeState.DIALOGUE, context);

// Later, on dialogue end (or error)
stateManager.exitState(playerId); // Automatically calls cleanup handlers
```

## Error Handling

```java
// State exit always succeeds (errors logged, not thrown)
try {
    stateManager.exitState(playerId);
} catch (Exception e) {
    // This should never happen - exitState catches and logs internally
    LOGGER.error("Unexpected error in exitState", e);
    stateManager.forceReset(playerId); // Last resort
}
```

## Thread Safety

- All methods are thread-safe
- State transitions are atomic
- Cleanup handlers execute sequentially (no parallel execution)
- Player sessions are isolated (no cross-player interference)

## Metrics

The state manager exposes metrics when profiling is enabled:

| Metric | Description |
|--------|-------------|
| `narrative.state.transitions` | Counter of state transitions |
| `narrative.state.cleanup.duration` | Time spent in cleanup handlers |
| `narrative.state.cleanup.failures` | Count of cleanup handler exceptions |
