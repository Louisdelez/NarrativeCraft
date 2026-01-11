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

package fr.loudo.narrativecraft.integration;

import static org.junit.jupiter.api.Assertions.*;

import fr.loudo.narrativecraft.narrative.cleanup.CleanupHandler;
import fr.loudo.narrativecraft.narrative.state.NarrativeState;
import fr.loudo.narrativecraft.narrative.state.NarrativeStateManagerImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for state machine transitions and cleanup handler execution.
 *
 * Addresses audit issues:
 * - State recovery from all narrative states to GAMEPLAY
 * - Cleanup handler priority ordering
 * - Cleanup execution on normal and forced exit
 * - Re-entrancy prevention during transitions
 */
@DisplayName("State Transition Integration Tests")
class StateTransitionTest {

    private NarrativeStateManagerImpl stateManager;
    private List<String> cleanupOrder;

    @BeforeEach
    void setUp() {
        stateManager = new NarrativeStateManagerImpl();
        cleanupOrder = new ArrayList<>();
    }

    @Nested
    @DisplayName("Valid State Transitions")
    class ValidTransitions {

        @Test
        @DisplayName("GAMEPLAY -> DIALOGUE -> GAMEPLAY should execute cleanup")
        void gameplayToDialogueToGameplay_shouldExecuteCleanup() {
            // Given: register a cleanup handler
            stateManager.registerCleanupHandler("TestCleanup", () -> cleanupOrder.add("cleanup"));

            // When: enter DIALOGUE state
            boolean entered = stateManager.enterState(NarrativeState.DIALOGUE, null);
            assertTrue(entered);
            assertEquals(NarrativeState.DIALOGUE, stateManager.getCurrentState());

            // And: exit to GAMEPLAY
            stateManager.exitToGameplay();

            // Then: should be in GAMEPLAY and cleanup should have run
            assertEquals(NarrativeState.GAMEPLAY, stateManager.getCurrentState());
            assertEquals(1, cleanupOrder.size());
            assertEquals("cleanup", cleanupOrder.get(0));
        }

        @Test
        @DisplayName("GAMEPLAY -> CUTSCENE -> GAMEPLAY should execute cleanup")
        void gameplayToCutsceneToGameplay_shouldExecuteCleanup() {
            stateManager.registerCleanupHandler("TestCleanup", () -> cleanupOrder.add("cleanup"));

            stateManager.enterState(NarrativeState.CUTSCENE, null);
            assertEquals(NarrativeState.CUTSCENE, stateManager.getCurrentState());

            stateManager.exitToGameplay();
            assertEquals(NarrativeState.GAMEPLAY, stateManager.getCurrentState());
            assertEquals(1, cleanupOrder.size());
        }

        @Test
        @DisplayName("GAMEPLAY -> PLAYBACK -> GAMEPLAY should execute cleanup")
        void gameplayToPlaybackToGameplay_shouldExecuteCleanup() {
            stateManager.registerCleanupHandler("TestCleanup", () -> cleanupOrder.add("cleanup"));

            stateManager.enterState(NarrativeState.PLAYBACK, null);
            assertEquals(NarrativeState.PLAYBACK, stateManager.getCurrentState());

            stateManager.exitToGameplay();
            assertEquals(NarrativeState.GAMEPLAY, stateManager.getCurrentState());
            assertEquals(1, cleanupOrder.size());
        }

        @Test
        @DisplayName("GAMEPLAY -> RECORDING -> GAMEPLAY should execute cleanup")
        void gameplayToRecordingToGameplay_shouldExecuteCleanup() {
            stateManager.registerCleanupHandler("TestCleanup", () -> cleanupOrder.add("cleanup"));

            stateManager.enterState(NarrativeState.RECORDING, null);
            assertEquals(NarrativeState.RECORDING, stateManager.getCurrentState());

            stateManager.exitToGameplay();
            assertEquals(NarrativeState.GAMEPLAY, stateManager.getCurrentState());
            assertEquals(1, cleanupOrder.size());
        }
    }

    @Nested
    @DisplayName("Invalid State Transitions")
    class InvalidTransitions {

        @Test
        @DisplayName("DIALOGUE -> CUTSCENE should throw IllegalStateException")
        void dialogueToCutscene_shouldThrow() {
            stateManager.enterState(NarrativeState.DIALOGUE, null);

            assertThrows(IllegalStateException.class, () -> stateManager.enterState(NarrativeState.CUTSCENE, null));
        }

        @Test
        @DisplayName("CUTSCENE -> DIALOGUE should throw IllegalStateException")
        void cutsceneToDialogue_shouldThrow() {
            stateManager.enterState(NarrativeState.CUTSCENE, null);

            assertThrows(IllegalStateException.class, () -> stateManager.enterState(NarrativeState.DIALOGUE, null));
        }

        @Test
        @DisplayName("PLAYBACK -> RECORDING should throw IllegalStateException")
        void playbackToRecording_shouldThrow() {
            stateManager.enterState(NarrativeState.PLAYBACK, null);

            assertThrows(IllegalStateException.class, () -> stateManager.enterState(NarrativeState.RECORDING, null));
        }
    }

    @Nested
    @DisplayName("Cleanup Handler Priority")
    class CleanupPriority {

        @Test
        @DisplayName("Cleanup handlers should execute in priority order (lowest first)")
        void cleanupHandlers_shouldExecuteInPriorityOrder() {
            // Register handlers in random priority order
            stateManager.registerCleanupHandler(
                    "Audio", CleanupHandler.PRIORITY_AUDIO, () -> cleanupOrder.add("audio"));
            stateManager.registerCleanupHandler("HUD", CleanupHandler.PRIORITY_HUD, () -> cleanupOrder.add("hud"));
            stateManager.registerCleanupHandler(
                    "Camera", CleanupHandler.PRIORITY_CAMERA, () -> cleanupOrder.add("camera"));
            stateManager.registerCleanupHandler(
                    "Input", CleanupHandler.PRIORITY_INPUT, () -> cleanupOrder.add("input"));

            // Enter and exit a state
            stateManager.enterState(NarrativeState.DIALOGUE, null);
            stateManager.exitToGameplay();

            // Verify execution order: HUD (50) -> Camera (150) -> Input (250) -> Audio (350)
            assertEquals(4, cleanupOrder.size());
            assertEquals("hud", cleanupOrder.get(0));
            assertEquals("camera", cleanupOrder.get(1));
            assertEquals("input", cleanupOrder.get(2));
            assertEquals("audio", cleanupOrder.get(3));
        }

        @Test
        @DisplayName("Custom priority handlers should execute in correct position")
        void customPriorityHandlers_shouldExecuteInCorrectPosition() {
            stateManager.registerCleanupHandler("First", 10, () -> cleanupOrder.add("first"));
            stateManager.registerCleanupHandler("Middle", 100, () -> cleanupOrder.add("middle"));
            stateManager.registerCleanupHandler("Last", 500, () -> cleanupOrder.add("last"));

            stateManager.enterState(NarrativeState.CUTSCENE, null);
            stateManager.exitToGameplay();

            assertEquals(3, cleanupOrder.size());
            assertEquals("first", cleanupOrder.get(0));
            assertEquals("middle", cleanupOrder.get(1));
            assertEquals("last", cleanupOrder.get(2));
        }
    }

    @Nested
    @DisplayName("Force Exit Behavior")
    class ForceExit {

        @Test
        @DisplayName("forceExitToGameplay() should execute cleanup even if exception occurs")
        void forceExit_shouldExecuteCleanupEvenOnException() {
            // Register a handler that throws and one that doesn't
            stateManager.registerCleanupHandler("Failing", 50, () -> {
                cleanupOrder.add("failing");
                throw new RuntimeException("Simulated failure");
            });
            stateManager.registerCleanupHandler("Succeeding", 100, () -> cleanupOrder.add("succeeding"));

            stateManager.enterState(NarrativeState.DIALOGUE, null);

            // Force exit should not throw and should run both handlers
            assertDoesNotThrow(() -> stateManager.forceExitToGameplay());
            assertEquals(NarrativeState.GAMEPLAY, stateManager.getCurrentState());

            // Both handlers should have been attempted
            assertEquals(2, cleanupOrder.size());
            assertTrue(cleanupOrder.contains("failing"));
            assertTrue(cleanupOrder.contains("succeeding"));
        }

        @Test
        @DisplayName("forceExitToGameplay() should reset transition lock")
        void forceExit_shouldResetTransitionLock() {
            stateManager.enterState(NarrativeState.CUTSCENE, null);
            stateManager.forceExitToGameplay();

            // Should be able to enter a new state after force exit
            boolean entered = stateManager.enterState(NarrativeState.DIALOGUE, null);
            assertTrue(entered);
            assertEquals(NarrativeState.DIALOGUE, stateManager.getCurrentState());
        }
    }

    @Nested
    @DisplayName("Cleanup Handler Clearing")
    class CleanupClearing {

        @Test
        @DisplayName("Cleanup handlers should be cleared after execution")
        void cleanupHandlers_shouldBeClearedAfterExecution() {
            AtomicInteger callCount = new AtomicInteger(0);
            stateManager.registerCleanupHandler("Counter", () -> callCount.incrementAndGet());

            // First transition
            stateManager.enterState(NarrativeState.DIALOGUE, null);
            stateManager.exitToGameplay();
            assertEquals(1, callCount.get());

            // Second transition - handler should have been cleared
            stateManager.enterState(NarrativeState.CUTSCENE, null);
            stateManager.exitToGameplay();
            assertEquals(1, callCount.get()); // Still 1, not 2
        }

        @Test
        @DisplayName("getPendingCleanupCount() should reflect registered handlers")
        void getPendingCleanupCount_shouldReflectRegisteredHandlers() {
            assertEquals(0, stateManager.getPendingCleanupCount());

            stateManager.registerCleanupHandler("One", () -> {});
            assertEquals(1, stateManager.getPendingCleanupCount());

            stateManager.registerCleanupHandler("Two", () -> {});
            assertEquals(2, stateManager.getPendingCleanupCount());

            // Enter and exit to clear handlers
            stateManager.enterState(NarrativeState.DIALOGUE, null);
            stateManager.exitToGameplay();

            assertEquals(0, stateManager.getPendingCleanupCount());
        }
    }

    @Nested
    @DisplayName("Reset Behavior")
    class ResetBehavior {

        @Test
        @DisplayName("reset() should return to GAMEPLAY without executing cleanup")
        void reset_shouldReturnToGameplayWithoutCleanup() {
            stateManager.registerCleanupHandler("ShouldNotRun", () -> cleanupOrder.add("ran"));
            stateManager.enterState(NarrativeState.CUTSCENE, null);

            stateManager.reset();

            assertEquals(NarrativeState.GAMEPLAY, stateManager.getCurrentState());
            assertTrue(cleanupOrder.isEmpty()); // Cleanup was NOT executed
            assertEquals(0, stateManager.getPendingCleanupCount());
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Entering same state should be no-op")
        void enteringSameState_shouldBeNoOp() {
            stateManager.enterState(NarrativeState.DIALOGUE, null);
            stateManager.registerCleanupHandler("Test", () -> cleanupOrder.add("ran"));

            // Entering same state should return true but not trigger cleanup
            boolean result = stateManager.enterState(NarrativeState.DIALOGUE, null);
            assertTrue(result);
            assertTrue(cleanupOrder.isEmpty());
            assertEquals(1, stateManager.getPendingCleanupCount());
        }

        @Test
        @DisplayName("exitToGameplay() when already in GAMEPLAY should be no-op")
        void exitWhenAlreadyInGameplay_shouldBeNoOp() {
            assertEquals(NarrativeState.GAMEPLAY, stateManager.getCurrentState());

            // Should not throw or cause issues
            assertDoesNotThrow(() -> stateManager.exitToGameplay());
            assertEquals(NarrativeState.GAMEPLAY, stateManager.getCurrentState());
        }

        @Test
        @DisplayName("null target state should throw IllegalArgumentException")
        void nullTargetState_shouldThrow() {
            assertThrows(IllegalArgumentException.class, () -> stateManager.enterState(null, null));
        }
    }
}
