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

package fr.loudo.narrativecraft.narrative.state;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.cleanup.CleanupHandler;
import fr.loudo.narrativecraft.narrative.cleanup.CleanupHandlerRegistry;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Implementation of the NarrativeStateManager interface.
 * Provides thread-safe state management with guaranteed cleanup execution.
 */
public class NarrativeStateManagerImpl implements NarrativeStateManager {

    private final AtomicReference<NarrativeState> currentState = new AtomicReference<>(NarrativeState.GAMEPLAY);
    private final AtomicReference<StateContext> currentContext = new AtomicReference<>(null);
    private final CleanupHandlerRegistry cleanupRegistry = new CleanupHandlerRegistry();
    private final List<StateChangeListener> listeners = new CopyOnWriteArrayList<>();

    private volatile boolean inTransition = false;

    @Override
    public NarrativeState getCurrentState() {
        return currentState.get();
    }

    @Override
    public boolean enterState(NarrativeState state, StateContext context) {
        if (state == null) {
            throw new IllegalArgumentException("Target state cannot be null");
        }

        NarrativeState current = currentState.get();

        if (!current.canTransitionTo(state)) {
            NarrativeCraftMod.LOGGER.warn(
                    "Invalid state transition: {} -> {}. Only GAMEPLAY can enter active states.", current, state);
            throw new IllegalStateException(
                    String.format("Cannot transition from %s to %s. Must exit to GAMEPLAY first.", current, state));
        }

        if (current == state) {
            NarrativeCraftMod.LOGGER.debug("Already in state {}, ignoring transition", state);
            return true;
        }

        synchronized (this) {
            if (inTransition) {
                NarrativeCraftMod.LOGGER.warn("State transition already in progress, ignoring enter request");
                return false;
            }
            inTransition = true;
        }

        try {
            NarrativeState oldState = currentState.getAndSet(state);
            currentContext.set(context);

            NarrativeCraftMod.LOGGER.info(
                    "Narrative state changed: {} -> {} (context: {})",
                    oldState,
                    state,
                    context != null ? context.getDescription() : "none");

            notifyListeners(oldState, state, context);
            return true;
        } finally {
            inTransition = false;
        }
    }

    @Override
    public void exitToGameplay() {
        NarrativeState current = currentState.get();

        if (current == NarrativeState.GAMEPLAY) {
            NarrativeCraftMod.LOGGER.debug("Already in GAMEPLAY state, nothing to exit");
            return;
        }

        synchronized (this) {
            if (inTransition) {
                NarrativeCraftMod.LOGGER.warn("State transition already in progress, queuing exit request");
            }
            inTransition = true;
        }

        try {
            executeCleanup();

            NarrativeState oldState = currentState.getAndSet(NarrativeState.GAMEPLAY);
            StateContext oldContext = currentContext.getAndSet(null);

            long durationMs = oldContext != null ? oldContext.getDurationMs() : 0;
            NarrativeCraftMod.LOGGER.info("Exited {} state after {}ms, returned to GAMEPLAY", oldState, durationMs);

            notifyListeners(oldState, NarrativeState.GAMEPLAY, null);
        } finally {
            inTransition = false;
        }
    }

    @Override
    public void forceExitToGameplay() {
        NarrativeCraftMod.LOGGER.warn("Force exit to GAMEPLAY requested");

        try {
            executeCleanup();
        } catch (Exception e) {
            NarrativeCraftMod.LOGGER.error("Error during force cleanup: {}", e.getMessage(), e);
        }

        NarrativeState oldState = currentState.getAndSet(NarrativeState.GAMEPLAY);
        currentContext.set(null);
        inTransition = false;

        if (oldState != NarrativeState.GAMEPLAY) {
            NarrativeCraftMod.LOGGER.info("Force exited from {} to GAMEPLAY", oldState);
            notifyListeners(oldState, NarrativeState.GAMEPLAY, null);
        }
    }

    @Override
    public void registerCleanupHandler(CleanupHandler handler) {
        cleanupRegistry.register(handler);
    }

    @Override
    public void registerCleanupHandler(String name, Runnable cleanup) {
        registerCleanupHandler(CleanupHandlerRegistry.named(name, cleanup));
    }

    @Override
    public void registerCleanupHandler(String name, int priority, Runnable cleanup) {
        registerCleanupHandler(CleanupHandlerRegistry.named(name, priority, cleanup));
    }

    @Override
    public StateContext getCurrentContext() {
        return currentContext.get();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends StateContext> T getContextAs(Class<T> type) {
        StateContext ctx = currentContext.get();
        if (ctx != null && type.isInstance(ctx)) {
            return (T) ctx;
        }
        return null;
    }

    @Override
    public void addStateChangeListener(StateChangeListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    @Override
    public void removeStateChangeListener(StateChangeListener listener) {
        listeners.remove(listener);
    }

    private void executeCleanup() {
        int handlerCount = cleanupRegistry.size();
        if (handlerCount > 0) {
            NarrativeCraftMod.LOGGER.debug("Executing {} cleanup handlers", handlerCount);
            int successCount = cleanupRegistry.executeAll();
            if (successCount < handlerCount) {
                NarrativeCraftMod.LOGGER.warn(
                        "Some cleanup handlers failed: {}/{} succeeded", successCount, handlerCount);
            }
        }
    }

    private void notifyListeners(NarrativeState oldState, NarrativeState newState, StateContext context) {
        for (StateChangeListener listener : listeners) {
            try {
                listener.onStateChange(oldState, newState, context);
            } catch (Exception e) {
                NarrativeCraftMod.LOGGER.error("State change listener threw exception: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * Resets the state manager to initial state.
     * Should only be used during world unload or emergency reset.
     */
    public void reset() {
        NarrativeCraftMod.LOGGER.info("Resetting NarrativeStateManager");
        cleanupRegistry.clear();
        currentState.set(NarrativeState.GAMEPLAY);
        currentContext.set(null);
        inTransition = false;
    }

    /**
     * @return the number of pending cleanup handlers
     */
    public int getPendingCleanupCount() {
        return cleanupRegistry.size();
    }
}
