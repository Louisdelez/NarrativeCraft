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
import fr.loudo.narrativecraft.narrative.cleanup.CleanupHandlerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CleanupHandlerRegistry")
class CleanupHandlerRegistryTest {

    private CleanupHandlerRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new CleanupHandlerRegistry();
    }

    @Nested
    @DisplayName("Registration")
    class RegistrationTest {

        @Test
        @DisplayName("Should register handler and increase size")
        void registerHandler() {
            assertEquals(0, registry.size());
            registry.register(createHandler("test", 100));
            assertEquals(1, registry.size());
        }

        @Test
        @DisplayName("Should reject null handler")
        void rejectNullHandler() {
            assertThrows(IllegalArgumentException.class, () -> registry.register(null));
        }

        @Test
        @DisplayName("Should allow multiple handlers")
        void allowMultipleHandlers() {
            registry.register(createHandler("first", 100));
            registry.register(createHandler("second", 200));
            registry.register(createHandler("third", 300));
            assertEquals(3, registry.size());
        }
    }

    @Nested
    @DisplayName("Unregistration")
    class UnregistrationTest {

        @Test
        @DisplayName("Should unregister existing handler")
        void unregisterExistingHandler() {
            CleanupHandler handler = createHandler("test", 100);
            registry.register(handler);
            assertEquals(1, registry.size());

            assertTrue(registry.unregister(handler));
            assertEquals(0, registry.size());
        }

        @Test
        @DisplayName("Should return false for non-existent handler")
        void unregisterNonExistentHandler() {
            CleanupHandler handler = createHandler("test", 100);
            assertFalse(registry.unregister(handler));
        }

        @Test
        @DisplayName("Should handle null unregister gracefully")
        void unregisterNull() {
            assertFalse(registry.unregister(null));
        }
    }

    @Nested
    @DisplayName("Execution")
    class ExecutionTest {

        @Test
        @DisplayName("Should execute all handlers")
        void executeAllHandlers() {
            AtomicInteger counter = new AtomicInteger(0);

            registry.register(() -> counter.incrementAndGet());
            registry.register(() -> counter.incrementAndGet());
            registry.register(() -> counter.incrementAndGet());

            int result = registry.executeAll();

            assertEquals(3, result);
            assertEquals(3, counter.get());
        }

        @Test
        @DisplayName("Should execute handlers in priority order")
        void executeInPriorityOrder() {
            List<String> executionOrder = new ArrayList<>();

            registry.register(CleanupHandlerRegistry.named("third", 300, () -> executionOrder.add("third")));
            registry.register(CleanupHandlerRegistry.named("first", 100, () -> executionOrder.add("first")));
            registry.register(CleanupHandlerRegistry.named("second", 200, () -> executionOrder.add("second")));

            registry.executeAll();

            assertEquals(List.of("first", "second", "third"), executionOrder);
        }

        @Test
        @DisplayName("Should clear handlers after execution")
        void clearAfterExecution() {
            registry.register(createHandler("test", 100));
            assertEquals(1, registry.size());

            registry.executeAll();

            assertEquals(0, registry.size());
            assertTrue(registry.isEmpty());
        }

        @Test
        @DisplayName("Should continue execution even if handler throws")
        void continueOnException() {
            AtomicInteger counter = new AtomicInteger(0);

            registry.register(CleanupHandlerRegistry.named("first", 100, counter::incrementAndGet));
            registry.register(CleanupHandlerRegistry.named("failing", 200, () -> {
                throw new RuntimeException("Test exception");
            }));
            registry.register(CleanupHandlerRegistry.named("third", 300, counter::incrementAndGet));

            int result = registry.executeAll();

            assertEquals(2, result);
            assertEquals(2, counter.get());
        }

        @Test
        @DisplayName("Should return 0 when no handlers registered")
        void returnZeroWhenEmpty() {
            assertEquals(0, registry.executeAll());
        }
    }

    @Nested
    @DisplayName("Clear")
    class ClearTest {

        @Test
        @DisplayName("Should clear all handlers without execution")
        void clearWithoutExecution() {
            AtomicInteger counter = new AtomicInteger(0);

            registry.register(() -> counter.incrementAndGet());
            registry.register(() -> counter.incrementAndGet());

            registry.clear();

            assertEquals(0, registry.size());
            assertEquals(0, counter.get());
        }
    }

    @Nested
    @DisplayName("Named Handler Factory")
    class NamedHandlerFactoryTest {

        @Test
        @DisplayName("Should create handler with default priority")
        void createWithDefaultPriority() {
            CleanupHandler handler = CleanupHandlerRegistry.named("test", () -> {});

            assertEquals("test", handler.name());
            assertEquals(CleanupHandler.DEFAULT_PRIORITY, handler.priority());
        }

        @Test
        @DisplayName("Should create handler with custom priority")
        void createWithCustomPriority() {
            CleanupHandler handler = CleanupHandlerRegistry.named("test", 42, () -> {});

            assertEquals("test", handler.name());
            assertEquals(42, handler.priority());
        }

        @Test
        @DisplayName("Should execute the provided action")
        void executeProvidedAction() {
            AtomicInteger counter = new AtomicInteger(0);
            CleanupHandler handler = CleanupHandlerRegistry.named("test", counter::incrementAndGet);

            handler.cleanup();

            assertEquals(1, counter.get());
        }
    }

    @Nested
    @DisplayName("CleanupHandler Interface Defaults")
    class InterfaceDefaultsTest {

        @Test
        @DisplayName("Default priority should be DEFAULT_PRIORITY constant")
        void defaultPriority() {
            CleanupHandler handler = () -> {};
            assertEquals(CleanupHandler.DEFAULT_PRIORITY, handler.priority());
        }

        @Test
        @DisplayName("Default name should be class simple name")
        void defaultName() {
            CleanupHandler handler = () -> {};
            assertNotNull(handler.name());
        }

        @Test
        @DisplayName("Priority constants should be properly ordered")
        void priorityConstantsOrdered() {
            assertTrue(CleanupHandler.PRIORITY_HUD < CleanupHandler.PRIORITY_CAMERA);
            assertTrue(CleanupHandler.PRIORITY_CAMERA < CleanupHandler.PRIORITY_INPUT);
            assertTrue(CleanupHandler.PRIORITY_INPUT < CleanupHandler.PRIORITY_AUDIO);
            assertTrue(CleanupHandler.PRIORITY_AUDIO < CleanupHandler.DEFAULT_PRIORITY);
        }
    }

    private CleanupHandler createHandler(String name, int priority) {
        return new CleanupHandler() {
            @Override
            public void cleanup() {}

            @Override
            public int priority() {
                return priority;
            }

            @Override
            public String name() {
                return name;
            }
        };
    }
}
