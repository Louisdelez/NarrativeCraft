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

package fr.loudo.narrativecraft.unit.state;

import static org.junit.jupiter.api.Assertions.*;

import fr.loudo.narrativecraft.narrative.state.NarrativeState;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@DisplayName("NarrativeState")
class NarrativeStateTest {

    @Nested
    @DisplayName("State Properties")
    class StatePropertiesTest {

        @Test
        @DisplayName("GAMEPLAY should not capture input, lock camera, or modify HUD")
        void gameplayStateProperties() {
            NarrativeState state = NarrativeState.GAMEPLAY;
            assertFalse(state.capturesInput());
            assertFalse(state.locksCamera());
            assertFalse(state.modifiesHud());
            assertFalse(state.isActive());
        }

        @Test
        @DisplayName("DIALOGUE should capture input and modify HUD")
        void dialogueStateProperties() {
            NarrativeState state = NarrativeState.DIALOGUE;
            assertTrue(state.capturesInput());
            assertFalse(state.locksCamera());
            assertTrue(state.modifiesHud());
            assertTrue(state.isActive());
        }

        @Test
        @DisplayName("CUTSCENE should capture input, lock camera, and modify HUD")
        void cutsceneStateProperties() {
            NarrativeState state = NarrativeState.CUTSCENE;
            assertTrue(state.capturesInput());
            assertTrue(state.locksCamera());
            assertTrue(state.modifiesHud());
            assertTrue(state.isActive());
        }

        @Test
        @DisplayName("RECORDING should not affect player control")
        void recordingStateProperties() {
            NarrativeState state = NarrativeState.RECORDING;
            assertFalse(state.capturesInput());
            assertFalse(state.locksCamera());
            assertFalse(state.modifiesHud());
            assertTrue(state.isActive());
        }

        @Test
        @DisplayName("PLAYBACK should capture input, lock camera, and modify HUD")
        void playbackStateProperties() {
            NarrativeState state = NarrativeState.PLAYBACK;
            assertTrue(state.capturesInput());
            assertTrue(state.locksCamera());
            assertTrue(state.modifiesHud());
            assertTrue(state.isActive());
        }
    }

    @Nested
    @DisplayName("State Transitions")
    class StateTransitionsTest {

        @Test
        @DisplayName("All states can transition to GAMEPLAY")
        void allStatesCanTransitionToGameplay() {
            for (NarrativeState state : NarrativeState.values()) {
                assertTrue(
                        state.canTransitionTo(NarrativeState.GAMEPLAY),
                        state + " should be able to transition to GAMEPLAY");
            }
        }

        @Test
        @DisplayName("GAMEPLAY can transition to all active states")
        void gameplayCanTransitionToActiveStates() {
            NarrativeState gameplay = NarrativeState.GAMEPLAY;
            for (NarrativeState target : NarrativeState.getActiveStates()) {
                assertTrue(gameplay.canTransitionTo(target), "GAMEPLAY should be able to transition to " + target);
            }
        }

        @Test
        @DisplayName("Active states cannot transition to other active states directly")
        void activeStatesCannotTransitionToOtherActiveStates() {
            Set<NarrativeState> activeStates = NarrativeState.getActiveStates();
            for (NarrativeState from : activeStates) {
                for (NarrativeState to : activeStates) {
                    if (from != to) {
                        assertFalse(from.canTransitionTo(to), from + " should NOT be able to transition to " + to);
                    }
                }
            }
        }

        @ParameterizedTest
        @EnumSource(NarrativeState.class)
        @DisplayName("All states can transition to themselves")
        void statesCanTransitionToSelf(NarrativeState state) {
            assertTrue(state.canTransitionTo(state), state + " should be able to transition to itself");
        }

        @Test
        @DisplayName("Null transition target should be rejected")
        void nullTransitionTargetRejected() {
            for (NarrativeState state : NarrativeState.values()) {
                assertFalse(state.canTransitionTo(null), state + " should not allow null transition");
            }
        }
    }

    @Nested
    @DisplayName("Active States Set")
    class ActiveStatesSetTest {

        @Test
        @DisplayName("Active states set should contain DIALOGUE, CUTSCENE, RECORDING, PLAYBACK")
        void activeStatesContainExpectedValues() {
            Set<NarrativeState> activeStates = NarrativeState.getActiveStates();
            assertEquals(4, activeStates.size());
            assertTrue(activeStates.contains(NarrativeState.DIALOGUE));
            assertTrue(activeStates.contains(NarrativeState.CUTSCENE));
            assertTrue(activeStates.contains(NarrativeState.RECORDING));
            assertTrue(activeStates.contains(NarrativeState.PLAYBACK));
        }

        @Test
        @DisplayName("Active states set should not contain GAMEPLAY")
        void activeStatesDoesNotContainGameplay() {
            Set<NarrativeState> activeStates = NarrativeState.getActiveStates();
            assertFalse(activeStates.contains(NarrativeState.GAMEPLAY));
        }

        @Test
        @DisplayName("Active states set should be immutable")
        void activeStatesSetIsImmutable() {
            Set<NarrativeState> activeStates = NarrativeState.getActiveStates();
            assertThrows(UnsupportedOperationException.class, () -> activeStates.add(NarrativeState.GAMEPLAY));
        }
    }

    @Nested
    @DisplayName("isActive Method")
    class IsActiveMethodTest {

        @Test
        @DisplayName("GAMEPLAY isActive returns false")
        void gameplayIsNotActive() {
            assertFalse(NarrativeState.GAMEPLAY.isActive());
        }

        @ParameterizedTest
        @EnumSource(
                value = NarrativeState.class,
                names = {"DIALOGUE", "CUTSCENE", "RECORDING", "PLAYBACK"})
        @DisplayName("Non-GAMEPLAY states isActive returns true")
        void activeStatesAreActive(NarrativeState state) {
            assertTrue(state.isActive(), state + " should be active");
        }
    }
}
