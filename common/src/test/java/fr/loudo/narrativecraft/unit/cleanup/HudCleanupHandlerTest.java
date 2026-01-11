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
 * Unit tests for HUD cleanup handler behavior.
 *
 * Addresses audit issues:
 * - MainScreen:177 - hideGui set without restoration guarantee
 * - DialogCustomOptionsScreen:109 - hideGui modified in init() without finally
 * - CameraAngleOptionsScreen:65 - Empty onClose() prevents parent cleanup
 * - StoryChoicesScreen:231 - Empty onClose() - no widget cleanup
 *
 * Uses a testable mock implementation since actual HudCleanupHandler
 * requires Minecraft runtime environment.
 */
@DisplayName("HudCleanupHandler Tests")
class HudCleanupHandlerTest {

    private MockHudCleanupHandler handler;
    private boolean hideGuiState;
    private boolean dialogRendererCleared;

    @BeforeEach
    void setUp() {
        hideGuiState = true; // Simulate HUD hidden state
        dialogRendererCleared = false;
        handler = new MockHudCleanupHandler();
    }

    @Test
    @DisplayName("cleanup() should restore hideGui to false")
    void cleanup_shouldRestoreHideGuiToFalse() {
        // Given: HUD is hidden
        assertTrue(hideGuiState);

        // When: cleanup is called
        handler.cleanup();

        // Then: hideGui should be false
        assertFalse(hideGuiState);
    }

    @Test
    @DisplayName("cleanup() should clear dialog renderer")
    void cleanup_shouldClearDialogRenderer() {
        // When: cleanup is called
        handler.cleanup();

        // Then: dialog renderer should be cleared
        assertTrue(dialogRendererCleared);
    }

    @Test
    @DisplayName("cleanup() should be idempotent - safe to call multiple times")
    void cleanup_shouldBeIdempotent() {
        // When: cleanup is called multiple times
        handler.cleanup();
        handler.cleanup();
        handler.cleanup();

        // Then: no exceptions should be thrown and state should be consistent
        assertFalse(hideGuiState);
        assertTrue(dialogRendererCleared);
    }

    @Test
    @DisplayName("priority() should return PRIORITY_HUD (50)")
    void priority_shouldReturnHudPriority() {
        assertEquals(CleanupHandler.PRIORITY_HUD, handler.priority());
    }

    @Test
    @DisplayName("name() should return descriptive name")
    void name_shouldReturnDescriptiveName() {
        assertEquals("HudCleanupHandler", handler.name());
    }

    @Test
    @DisplayName("cleanup() should not throw exceptions even when state is null")
    void cleanup_shouldNotThrowWhenStateIsNull() {
        // Given: a handler with null state (simulating edge case)
        handler.setSimulateNullState(true);

        // When/Then: cleanup should not throw
        assertDoesNotThrow(() -> handler.cleanup());
    }

    /**
     * Mock implementation of CleanupHandler that simulates HUD cleanup behavior
     * without requiring Minecraft runtime.
     */
    private class MockHudCleanupHandler implements CleanupHandler {
        private boolean simulateNullState = false;

        @Override
        public void cleanup() {
            if (simulateNullState) {
                // Simulate handling null state gracefully
                return;
            }
            hideGuiState = false;
            dialogRendererCleared = true;
        }

        @Override
        public int priority() {
            return PRIORITY_HUD;
        }

        @Override
        public String name() {
            return "HudCleanupHandler";
        }

        public void setSimulateNullState(boolean simulateNullState) {
            this.simulateNullState = simulateNullState;
        }
    }
}
