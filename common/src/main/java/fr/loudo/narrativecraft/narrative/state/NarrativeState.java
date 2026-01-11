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

import java.util.EnumSet;
import java.util.Set;

/**
 * Represents the possible states of the narrative system.
 * The state machine ensures only valid transitions occur and that
 * cleanup handlers are invoked when exiting any non-GAMEPLAY state.
 */
public enum NarrativeState {
    /**
     * Normal player control - no narrative elements active.
     * This is the default/safe state that all other states return to.
     */
    GAMEPLAY(false, false, false),

    /**
     * Dialog UI is active and capturing player input.
     * Player movement may be restricted, HUD may be modified.
     */
    DIALOGUE(true, false, true),

    /**
     * A cutscene is playing with locked camera.
     * Player input is fully captured, camera is controlled by the scene.
     */
    CUTSCENE(true, true, true),

    /**
     * Recording mode is active - capturing player actions.
     * Player has normal control but actions are being recorded.
     */
    RECORDING(false, false, false),

    /**
     * Recorded actions are being played back.
     * Similar to cutscene but replaying recorded content.
     */
    PLAYBACK(true, true, true);

    private final boolean capturesInput;
    private final boolean locksCamera;
    private final boolean modifiesHud;

    private static final Set<NarrativeState> ACTIVE_STATES = EnumSet.of(DIALOGUE, CUTSCENE, RECORDING, PLAYBACK);

    NarrativeState(boolean capturesInput, boolean locksCamera, boolean modifiesHud) {
        this.capturesInput = capturesInput;
        this.locksCamera = locksCamera;
        this.modifiesHud = modifiesHud;
    }

    /**
     * @return true if this state captures/blocks normal player input
     */
    public boolean capturesInput() {
        return capturesInput;
    }

    /**
     * @return true if this state locks the camera position/rotation
     */
    public boolean locksCamera() {
        return locksCamera;
    }

    /**
     * @return true if this state modifies the HUD display
     */
    public boolean modifiesHud() {
        return modifiesHud;
    }

    /**
     * @return true if this is an active narrative state (not GAMEPLAY)
     */
    public boolean isActive() {
        return this != GAMEPLAY;
    }

    /**
     * Checks if a transition from this state to the target state is valid.
     * All states can transition to GAMEPLAY (exit).
     * Only GAMEPLAY can transition to active states (enter).
     *
     * @param target the state to transition to
     * @return true if the transition is valid
     */
    public boolean canTransitionTo(NarrativeState target) {
        if (target == null) {
            return false;
        }
        if (this == target) {
            return true;
        }
        if (target == GAMEPLAY) {
            return true;
        }
        if (this == GAMEPLAY && ACTIVE_STATES.contains(target)) {
            return true;
        }
        return false;
    }

    /**
     * @return the set of states that require cleanup handlers on exit
     */
    public static Set<NarrativeState> getActiveStates() {
        return EnumSet.copyOf(ACTIVE_STATES);
    }
}
