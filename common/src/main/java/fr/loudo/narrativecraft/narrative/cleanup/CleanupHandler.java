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

/**
 * Interface for cleanup handlers that restore player state when exiting narrative states.
 * Handlers are invoked in priority order (lowest first) to ensure proper restoration
 * sequence (e.g., HUD before camera before input).
 *
 * <p>Implementation requirements:
 * <ul>
 *   <li>cleanup() must be idempotent - safe to call multiple times</li>
 *   <li>cleanup() must not throw exceptions - log errors and continue</li>
 *   <li>cleanup() should complete quickly - no blocking operations</li>
 * </ul>
 *
 * <p>Standard priority ranges:
 * <ul>
 *   <li>0-99: HUD/UI restoration</li>
 *   <li>100-199: Camera restoration</li>
 *   <li>200-299: Input restoration</li>
 *   <li>300-399: Audio restoration</li>
 *   <li>400+: Custom handlers</li>
 * </ul>
 */
@FunctionalInterface
public interface CleanupHandler {

    /**
     * Default priority for handlers without explicit priority.
     */
    int DEFAULT_PRIORITY = 500;

    /**
     * Priority for HUD cleanup handlers.
     */
    int PRIORITY_HUD = 50;

    /**
     * Priority for camera cleanup handlers.
     */
    int PRIORITY_CAMERA = 150;

    /**
     * Priority for input cleanup handlers.
     */
    int PRIORITY_INPUT = 250;

    /**
     * Priority for audio cleanup handlers.
     */
    int PRIORITY_AUDIO = 350;

    /**
     * Performs cleanup to restore player state.
     * This method is called when exiting a narrative state, either normally
     * or due to an error/interruption.
     *
     * <p>Implementations must:
     * <ul>
     *   <li>Be idempotent (safe to call multiple times)</li>
     *   <li>Not throw exceptions (catch and log internally)</li>
     *   <li>Complete quickly without blocking</li>
     * </ul>
     */
    void cleanup();

    /**
     * Returns the priority of this handler. Lower values execute first.
     * This ensures proper cleanup order (e.g., HUD before camera).
     *
     * @return the priority value (lower = runs first)
     */
    default int priority() {
        return DEFAULT_PRIORITY;
    }

    /**
     * Returns a descriptive name for this handler for logging purposes.
     *
     * @return the handler name
     */
    default String name() {
        return getClass().getSimpleName();
    }
}
