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
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for cleanup scenarios.
 *
 * Tests scenarios from Phase 4 T048-T056:
 * - Dialogue abort
 * - Cutscene abort
 * - Screen close
 * - Player disconnect
 * - Exception during narrative
 */
@DisplayName("Cleanup Integration Tests")
class CleanupIntegrationTest {

    private NarrativeStateManagerImpl stateManager;
    private List<String> cleanupOrder;
    private AtomicBoolean hudRestored;
    private AtomicBoolean cameraRestored;
    private AtomicBoolean inputRestored;
    private AtomicBoolean audioStopped;

    @BeforeEach
    void setUp() {
        stateManager = new NarrativeStateManagerImpl();
        cleanupOrder = new ArrayList<>();
        hudRestored = new AtomicBoolean(false);
        cameraRestored = new AtomicBoolean(false);
        inputRestored = new AtomicBoolean(false);
        audioStopped = new AtomicBoolean(false);

        // Register mock handlers matching production priorities
        stateManager.registerCleanupHandler("HUD", CleanupHandler.PRIORITY_HUD, () -> {
            cleanupOrder.add("hud");
            hudRestored.set(true);
        });
        stateManager.registerCleanupHandler("Camera", CleanupHandler.PRIORITY_CAMERA, () -> {
            cleanupOrder.add("camera");
            cameraRestored.set(true);
        });
        stateManager.registerCleanupHandler("Input", CleanupHandler.PRIORITY_INPUT, () -> {
            cleanupOrder.add("input");
            inputRestored.set(true);
        });
        stateManager.registerCleanupHandler("Audio", CleanupHandler.PRIORITY_AUDIO, () -> {
            cleanupOrder.add("audio");
            audioStopped.set(true);
        });
    }

    @Nested
    @DisplayName("Dialogue Abort Scenarios")
    class DialogueAbort {

        @Test
        @DisplayName("Normal dialogue exit should restore all state")
        void normalExit_shouldRestoreAllState() {
            // Enter dialogue state
            stateManager.enterState(NarrativeState.DIALOGUE, null);

            // Exit normally
            stateManager.exitToGameplay();

            // Verify all state restored
            assertTrue(hudRestored.get(), "HUD should be restored");
            assertTrue(cameraRestored.get(), "Camera should be restored");
            assertTrue(inputRestored.get(), "Input should be restored");
            assertTrue(audioStopped.get(), "Audio should be stopped");
            assertEquals(NarrativeState.GAMEPLAY, stateManager.getCurrentState());
        }

        @Test
        @DisplayName("Dialogue abort (force exit) should restore all state")
        void forceExit_shouldRestoreAllState() {
            stateManager.enterState(NarrativeState.DIALOGUE, null);

            // Force exit (simulating abort)
            stateManager.forceExitToGameplay();

            assertTrue(hudRestored.get());
            assertTrue(cameraRestored.get());
            assertTrue(inputRestored.get());
            assertTrue(audioStopped.get());
            assertEquals(NarrativeState.GAMEPLAY, stateManager.getCurrentState());
        }
    }

    @Nested
    @DisplayName("Cutscene Abort Scenarios")
    class CutsceneAbort {

        @Test
        @DisplayName("Cutscene abort should restore camera control")
        void cutsceneAbort_shouldRestoreCameraControl() {
            stateManager.enterState(NarrativeState.CUTSCENE, null);

            stateManager.exitToGameplay();

            assertTrue(cameraRestored.get(), "Camera must be restored after cutscene abort");
            assertEquals(NarrativeState.GAMEPLAY, stateManager.getCurrentState());
        }

        @Test
        @DisplayName("Force exit during cutscene should not block future states")
        void forceExitDuringCutscene_shouldNotBlockFutureStates() {
            stateManager.enterState(NarrativeState.CUTSCENE, null);
            stateManager.forceExitToGameplay();

            // Should be able to enter a new state
            boolean entered = stateManager.enterState(NarrativeState.DIALOGUE, null);
            assertTrue(entered, "Should be able to enter new state after force exit");
        }
    }

    @Nested
    @DisplayName("Exception During Narrative")
    class ExceptionDuringNarrative {

        @Test
        @DisplayName("Handler exception should not prevent other handlers from running")
        void handlerException_shouldNotPreventOtherHandlers() {
            // Reset and add a failing handler
            stateManager.reset();
            cleanupOrder.clear();

            stateManager.registerCleanupHandler("FailingFirst", 25, () -> {
                cleanupOrder.add("failing");
                throw new RuntimeException("Test exception");
            });
            stateManager.registerCleanupHandler("SucceedingSecond", 75, () -> {
                cleanupOrder.add("succeeding");
            });

            stateManager.enterState(NarrativeState.DIALOGUE, null);
            stateManager.forceExitToGameplay();

            // Both handlers should have been attempted
            assertEquals(2, cleanupOrder.size());
            assertTrue(cleanupOrder.contains("failing"));
            assertTrue(cleanupOrder.contains("succeeding"));
            assertEquals(NarrativeState.GAMEPLAY, stateManager.getCurrentState());
        }
    }

    @Nested
    @DisplayName("Cleanup Order Guarantee")
    class CleanupOrderGuarantee {

        @Test
        @DisplayName("Cleanup order should be HUD -> Camera -> Input -> Audio")
        void cleanupOrder_shouldBeCorrect() {
            stateManager.enterState(NarrativeState.DIALOGUE, null);
            stateManager.exitToGameplay();

            assertEquals(4, cleanupOrder.size());
            assertEquals("hud", cleanupOrder.get(0));
            assertEquals("camera", cleanupOrder.get(1));
            assertEquals("input", cleanupOrder.get(2));
            assertEquals("audio", cleanupOrder.get(3));
        }
    }

    @Nested
    @DisplayName("Idempotency Tests")
    class IdempotencyTests {

        @Test
        @DisplayName("Multiple exits should not cause errors")
        void multipleExits_shouldNotCauseErrors() {
            stateManager.enterState(NarrativeState.DIALOGUE, null);

            // Call exit multiple times
            assertDoesNotThrow(() -> {
                stateManager.exitToGameplay();
                stateManager.exitToGameplay();
                stateManager.exitToGameplay();
            });

            assertEquals(NarrativeState.GAMEPLAY, stateManager.getCurrentState());
        }

        @Test
        @DisplayName("Exit from GAMEPLAY should be no-op")
        void exitFromGameplay_shouldBeNoOp() {
            assertEquals(NarrativeState.GAMEPLAY, stateManager.getCurrentState());

            assertDoesNotThrow(() -> stateManager.exitToGameplay());
            assertEquals(NarrativeState.GAMEPLAY, stateManager.getCurrentState());
            assertTrue(cleanupOrder.isEmpty(), "No cleanup should run when already in GAMEPLAY");
        }
    }

    @Nested
    @DisplayName("Reset Scenarios")
    class ResetScenarios {

        @Test
        @DisplayName("Reset should clear state without running cleanup")
        void reset_shouldClearStateWithoutCleanup() {
            stateManager.enterState(NarrativeState.CUTSCENE, null);

            stateManager.reset();

            assertEquals(NarrativeState.GAMEPLAY, stateManager.getCurrentState());
            assertTrue(cleanupOrder.isEmpty(), "Reset should not run cleanup handlers");
            assertEquals(0, stateManager.getPendingCleanupCount());
        }
    }
}
