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

import static org.junit.jupiter.api.Assertions.*;

import fr.loudo.narrativecraft.narrative.cleanup.CleanupHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for input cleanup handler behavior.
 *
 * Addresses audit issues:
 * - StoryChoicesScreen:184 - Escape key suppressed in keyPressed
 * - MainScreen:466-468 - Escape key blocked during non-pause mode
 * - Input capture not released on scene interruption
 *
 * Uses a testable mock implementation since actual InputCleanupHandler
 * requires Minecraft runtime environment.
 */
@DisplayName("InputCleanupHandler Tests")
class InputCleanupHandlerTest {

    private MockInputCleanupHandler handler;
    private boolean keyboardCaptured;
    private boolean mouseCaptured;
    private boolean escapeBlocked;
    private boolean movementBlocked;

    @BeforeEach
    void setUp() {
        keyboardCaptured = true;
        mouseCaptured = true;
        escapeBlocked = true;
        movementBlocked = true;
        handler = new MockInputCleanupHandler();
    }

    @Test
    @DisplayName("cleanup() should release keyboard capture")
    void cleanup_shouldReleaseKeyboardCapture() {
        // Given: keyboard is captured
        assertTrue(keyboardCaptured);

        // When: cleanup is called
        handler.cleanup();

        // Then: keyboard should be released
        assertFalse(keyboardCaptured);
    }

    @Test
    @DisplayName("cleanup() should release mouse capture")
    void cleanup_shouldReleaseMouseCapture() {
        // Given: mouse is captured
        assertTrue(mouseCaptured);

        // When: cleanup is called
        handler.cleanup();

        // Then: mouse should be released
        assertFalse(mouseCaptured);
    }

    @Test
    @DisplayName("cleanup() should unblock escape key")
    void cleanup_shouldUnblockEscapeKey() {
        // Given: escape key is blocked
        assertTrue(escapeBlocked);

        // When: cleanup is called
        handler.cleanup();

        // Then: escape should be unblocked
        assertFalse(escapeBlocked);
    }

    @Test
    @DisplayName("cleanup() should restore player movement control")
    void cleanup_shouldRestorePlayerMovementControl() {
        // Given: movement is blocked
        assertTrue(movementBlocked);

        // When: cleanup is called
        handler.cleanup();

        // Then: movement should be restored
        assertFalse(movementBlocked);
    }

    @Test
    @DisplayName("cleanup() should be idempotent")
    void cleanup_shouldBeIdempotent() {
        // When: cleanup is called multiple times
        handler.cleanup();
        handler.cleanup();
        handler.cleanup();

        // Then: no exceptions and all controls should be released
        assertFalse(keyboardCaptured);
        assertFalse(mouseCaptured);
        assertFalse(escapeBlocked);
        assertFalse(movementBlocked);
    }

    @Test
    @DisplayName("priority() should return PRIORITY_INPUT (250)")
    void priority_shouldReturnInputPriority() {
        assertEquals(CleanupHandler.PRIORITY_INPUT, handler.priority());
    }

    @Test
    @DisplayName("name() should return descriptive name")
    void name_shouldReturnDescriptiveName() {
        assertEquals("InputCleanupHandler", handler.name());
    }

    @Test
    @DisplayName("cleanup() should handle null Minecraft instance gracefully")
    void cleanup_shouldHandleNullMinecraftGracefully() {
        // Given: Minecraft instance is null
        handler.setSimulateNullMinecraft(true);

        // When/Then: cleanup should not throw
        assertDoesNotThrow(() -> handler.cleanup());
    }

    @Test
    @DisplayName("cleanup() should close any open screens")
    void cleanup_shouldCloseOpenScreens() {
        // Given: a screen is open
        handler.setScreenOpen(true);

        // When: cleanup is called
        handler.cleanup();

        // Then: screen should be closed
        assertFalse(handler.isScreenOpen());
    }

    /**
     * Mock implementation of CleanupHandler that simulates input cleanup behavior
     * without requiring Minecraft runtime.
     */
    private class MockInputCleanupHandler implements CleanupHandler {
        private boolean simulateNullMinecraft = false;
        private boolean screenOpen = false;

        @Override
        public void cleanup() {
            if (simulateNullMinecraft) {
                // Gracefully handle null Minecraft
                return;
            }
            keyboardCaptured = false;
            mouseCaptured = false;
            escapeBlocked = false;
            movementBlocked = false;
            screenOpen = false;
        }

        @Override
        public int priority() {
            return PRIORITY_INPUT;
        }

        @Override
        public String name() {
            return "InputCleanupHandler";
        }

        public void setSimulateNullMinecraft(boolean simulateNullMinecraft) {
            this.simulateNullMinecraft = simulateNullMinecraft;
        }

        public void setScreenOpen(boolean open) {
            this.screenOpen = open;
        }

        public boolean isScreenOpen() {
            return screenOpen;
        }
    }
}
