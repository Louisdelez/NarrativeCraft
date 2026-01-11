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

import fr.loudo.narrativecraft.narrative.cleanup.CleanupHandler;

/**
 * Central state machine for the narrative system.
 * Manages transitions between narrative states and ensures cleanup handlers
 * are executed when exiting any active state.
 *
 * <p>Usage pattern:
 * <pre>{@code
 * NarrativeStateManager manager = ...;
 * try {
 *     manager.registerCleanupHandler(() -> resetHUD());
 *     manager.enterState(NarrativeState.DIALOGUE, context);
 *     // ... narrative logic
 * } finally {
 *     manager.exitToGameplay(); // Triggers all cleanup handlers
 * }
 * }</pre>
 */
public interface NarrativeStateManager {

    /**
     * Returns the current narrative state.
     *
     * @return the current state, never null
     */
    NarrativeState getCurrentState();

    /**
     * Checks if the system is currently in the GAMEPLAY state.
     *
     * @return true if in GAMEPLAY state
     */
    default boolean isInGameplay() {
        return getCurrentState() == NarrativeState.GAMEPLAY;
    }

    /**
     * Checks if the system is in an active narrative state (not GAMEPLAY).
     *
     * @return true if in an active state
     */
    default boolean isActive() {
        return getCurrentState().isActive();
    }

    /**
     * Attempts to enter a new narrative state.
     * Transitions are only allowed from GAMEPLAY to active states.
     *
     * @param state the target state to enter
     * @param context optional context for the state (may be null)
     * @return true if the transition was successful
     * @throws IllegalStateException if the transition is not allowed
     */
    boolean enterState(NarrativeState state, StateContext context);

    /**
     * Exits the current state and returns to GAMEPLAY.
     * Executes all registered cleanup handlers in priority order.
     * Safe to call when already in GAMEPLAY state (no-op).
     */
    void exitToGameplay();

    /**
     * Force exits to GAMEPLAY state, executing cleanup handlers.
     * Unlike exitToGameplay(), this will force the transition even
     * if an error occurs. Used for emergency cleanup scenarios.
     */
    void forceExitToGameplay();

    /**
     * Registers a cleanup handler for the current state.
     * Handlers are executed when exiting the state.
     *
     * @param handler the handler to register
     * @throws IllegalArgumentException if handler is null
     */
    void registerCleanupHandler(CleanupHandler handler);

    /**
     * Registers a cleanup handler using a simple runnable.
     * Convenience method for lambda-style handlers.
     *
     * @param name the handler name for logging
     * @param cleanup the cleanup action
     */
    void registerCleanupHandler(String name, Runnable cleanup);

    /**
     * Registers a cleanup handler with specific priority.
     *
     * @param name the handler name for logging
     * @param priority the execution priority (lower = first)
     * @param cleanup the cleanup action
     */
    void registerCleanupHandler(String name, int priority, Runnable cleanup);

    /**
     * Returns the context associated with the current state.
     *
     * @return the current context, or null if in GAMEPLAY
     */
    StateContext getCurrentContext();

    /**
     * Checks if the state was entered with a specific context type.
     *
     * @param <T> the expected context type
     * @param type the context class to check
     * @return the context if it matches, or null
     */
    <T extends StateContext> T getContextAs(Class<T> type);

    /**
     * Adds a listener to be notified of state changes.
     *
     * @param listener the listener to add
     */
    void addStateChangeListener(StateChangeListener listener);

    /**
     * Removes a state change listener.
     *
     * @param listener the listener to remove
     */
    void removeStateChangeListener(StateChangeListener listener);

    /**
     * Listener interface for state change notifications.
     */
    @FunctionalInterface
    interface StateChangeListener {
        /**
         * Called when the narrative state changes.
         *
         * @param oldState the previous state
         * @param newState the new state
         * @param context the context for the new state (may be null)
         */
        void onStateChange(NarrativeState oldState, NarrativeState newState, StateContext context);
    }
}
