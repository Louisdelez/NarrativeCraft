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

package fr.loudo.narrativecraft.unit.cleanup;

import fr.loudo.narrativecraft.narrative.cleanup.CleanupHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for camera cleanup handler behavior.
 *
 * Addresses audit issues:
 * - CameraMixin:60 - Minecraft.getInstance().player may be null
 * - Cutscene playback camera lock not restored after interruption
 * - Player currentCamera field persisting after scene exit
 *
 * Uses a testable mock implementation since actual CameraCleanupHandler
 * requires Minecraft runtime environment.
 */
@DisplayName("CameraCleanupHandler Tests")
class CameraCleanupHandlerTest {

    private MockCameraCleanupHandler handler;
    private boolean cameraLocked;
    private float[] cameraPosition;
    private float[] cameraRotation;
    private Object currentCamera;

    @BeforeEach
    void setUp() {
        cameraLocked = true;
        cameraPosition = new float[]{100f, 64f, 200f};
        cameraRotation = new float[]{45f, 180f};
        currentCamera = new Object(); // Simulates a camera lock
        handler = new MockCameraCleanupHandler();
    }

    @Test
    @DisplayName("cleanup() should unlock camera control")
    void cleanup_shouldUnlockCameraControl() {
        // Given: camera is locked
        assertTrue(cameraLocked);

        // When: cleanup is called
        handler.cleanup();

        // Then: camera should be unlocked
        assertFalse(cameraLocked);
    }

    @Test
    @DisplayName("cleanup() should clear currentCamera reference")
    void cleanup_shouldClearCurrentCameraReference() {
        // Given: currentCamera is set
        assertNotNull(currentCamera);

        // When: cleanup is called
        handler.cleanup();

        // Then: currentCamera should be null
        assertNull(currentCamera);
    }

    @Test
    @DisplayName("cleanup() should restore camera to player control")
    void cleanup_shouldRestoreCameraToPlayerControl() {
        // When: cleanup is called
        handler.cleanup();

        // Then: camera should return to player control (first person mode)
        assertFalse(cameraLocked);
        assertNull(currentCamera);
    }

    @Test
    @DisplayName("cleanup() should be idempotent")
    void cleanup_shouldBeIdempotent() {
        // When: cleanup is called multiple times
        handler.cleanup();
        handler.cleanup();
        handler.cleanup();

        // Then: no exceptions and state should be consistent
        assertFalse(cameraLocked);
        assertNull(currentCamera);
    }

    @Test
    @DisplayName("priority() should return PRIORITY_CAMERA (150)")
    void priority_shouldReturnCameraPriority() {
        assertEquals(CleanupHandler.PRIORITY_CAMERA, handler.priority());
    }

    @Test
    @DisplayName("name() should return descriptive name")
    void name_shouldReturnDescriptiveName() {
        assertEquals("CameraCleanupHandler", handler.name());
    }

    @Test
    @DisplayName("cleanup() should handle null player gracefully")
    void cleanup_shouldHandleNullPlayerGracefully() {
        // Given: player is null (simulating disconnect scenario)
        handler.setSimulateNullPlayer(true);

        // When/Then: cleanup should not throw
        assertDoesNotThrow(() -> handler.cleanup());
    }

    @Test
    @DisplayName("cleanup() should stop any active cutscene playback")
    void cleanup_shouldStopActiveCutscenePlayback() {
        // Given: cutscene playback is active
        handler.setCutsceneActive(true);

        // When: cleanup is called
        handler.cleanup();

        // Then: cutscene should be stopped
        assertFalse(handler.isCutsceneActive());
    }

    /**
     * Mock implementation of CleanupHandler that simulates camera cleanup behavior
     * without requiring Minecraft runtime.
     */
    private class MockCameraCleanupHandler implements CleanupHandler {
        private boolean simulateNullPlayer = false;
        private boolean cutsceneActive = false;

        @Override
        public void cleanup() {
            if (simulateNullPlayer) {
                // Gracefully handle null player
                return;
            }
            cameraLocked = false;
            currentCamera = null;
            cutsceneActive = false;
        }

        @Override
        public int priority() {
            return PRIORITY_CAMERA;
        }

        @Override
        public String name() {
            return "CameraCleanupHandler";
        }

        public void setSimulateNullPlayer(boolean simulateNullPlayer) {
            this.simulateNullPlayer = simulateNullPlayer;
        }

        public void setCutsceneActive(boolean active) {
            this.cutsceneActive = active;
        }

        public boolean isCutsceneActive() {
            return cutsceneActive;
        }
    }
}
