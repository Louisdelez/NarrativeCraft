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

import java.util.UUID;

/**
 * Base class for state context objects that carry state-specific data.
 * Subclass this for specific state requirements (e.g., DialogueContext, CutsceneContext).
 */
public class StateContext {

    private final UUID sessionId;
    private final long enteredAt;
    private final String description;

    /**
     * Creates a new state context with auto-generated session ID.
     *
     * @param description a human-readable description of this context
     */
    public StateContext(String description) {
        this(UUID.randomUUID(), description);
    }

    /**
     * Creates a new state context with a specific session ID.
     *
     * @param sessionId the unique session identifier
     * @param description a human-readable description of this context
     */
    public StateContext(UUID sessionId, String description) {
        this.sessionId = sessionId;
        this.enteredAt = System.currentTimeMillis();
        this.description = description != null ? description : "Unknown context";
    }

    /**
     * @return the unique session identifier for this context
     */
    public UUID getSessionId() {
        return sessionId;
    }

    /**
     * @return the timestamp when this context was created (entered state)
     */
    public long getEnteredAt() {
        return enteredAt;
    }

    /**
     * @return a human-readable description of this context
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return duration in milliseconds since this context was created
     */
    public long getDurationMs() {
        return System.currentTimeMillis() - enteredAt;
    }

    @Override
    public String toString() {
        return String.format("StateContext{sessionId=%s, description='%s', durationMs=%d}",
                sessionId, description, getDurationMs());
    }

    /**
     * Creates an empty context for simple state transitions.
     *
     * @return a generic state context
     */
    public static StateContext empty() {
        return new StateContext("Empty context");
    }

    /**
     * Creates a context with a simple description.
     *
     * @param description the context description
     * @return a new state context
     */
    public static StateContext of(String description) {
        return new StateContext(description);
    }
}
