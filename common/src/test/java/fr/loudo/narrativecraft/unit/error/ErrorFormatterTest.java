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

package fr.loudo.narrativecraft.unit.error;

import fr.loudo.narrativecraft.narrative.error.ErrorFormatter;
import fr.loudo.narrativecraft.narrative.validation.ValidationError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ErrorFormatter.
 * Tests T062: Error message formatting with 4-component format (what, where, why, fix).
 */
@DisplayName("ErrorFormatter Tests")
class ErrorFormatterTest {

    private ErrorFormatter formatter;

    @BeforeEach
    void setUp() {
        formatter = new ErrorFormatter();
    }

    @Nested
    @DisplayName("Four Component Format")
    class FourComponentFormat {

        @Test
        @DisplayName("Formatted error should contain WHAT component")
        void formattedErrorShouldContainWhatComponent() {
            ValidationError error = ValidationError.unknownTag("unknowntag", "fade 1.0 2.0 1.0", "test_story", "test_scene", 42, null);

            String formatted = formatter.format(error);

            assertTrue(formatted.contains("[WHAT]") || formatted.toLowerCase().contains("error:"),
                "Should contain WHAT component identifying the error");
            assertTrue(formatted.contains("unknown") || formatted.contains("unrecognized"),
                "Should describe unknown tag");
        }

        @Test
        @DisplayName("Formatted error should contain WHERE component")
        void formattedErrorShouldContainWhereComponent() {
            ValidationError error = ValidationError.unknownTag("unknowntag", "unknowntag arg", "my_story", "intro_scene", 42, null);

            String formatted = formatter.format(error);

            assertTrue(formatted.contains("[WHERE]") || formatted.contains("my_story"),
                "Should contain WHERE component with story name");
            assertTrue(formatted.contains("intro_scene"),
                "Should contain scene name");
            assertTrue(formatted.contains("42") || formatted.contains("line 42"),
                "Should contain line number");
        }

        @Test
        @DisplayName("Formatted error should contain WHY component")
        void formattedErrorShouldContainWhyComponent() {
            ValidationError error = ValidationError.unknownTag("unknowntag", "unknowntag arg", "test_story", "test_scene", 10, null);

            String formatted = formatter.format(error);

            assertTrue(formatted.contains("[WHY]") || formatted.toLowerCase().contains("because") || formatted.toLowerCase().contains("reason"),
                "Should contain WHY component explaining the cause");
        }

        @Test
        @DisplayName("Formatted error should contain FIX component")
        void formattedErrorShouldContainFixComponent() {
            ValidationError error = ValidationError.unknownTag("sund", "sund start sound", "test_story", "test_scene", 10, "sfx");

            String formatted = formatter.format(error);

            assertTrue(formatted.contains("[FIX]") || formatted.toLowerCase().contains("suggestion") || formatted.toLowerCase().contains("try"),
                "Should contain FIX component with suggestion");
            assertTrue(formatted.contains("sfx"),
                "Should contain the suggested correction");
        }
    }

    @Nested
    @DisplayName("Error Type Formatting")
    class ErrorTypeFormatting {

        @Test
        @DisplayName("Unknown tag error should have clear message")
        void unknownTagErrorShouldHaveClearMessage() {
            ValidationError error = ValidationError.unknownTag("badtag", "badtag arg1", "test_story", "test_scene", 10, null);

            String formatted = formatter.format(error);

            assertTrue(formatted.contains("badtag"));
            assertTrue(formatted.toLowerCase().contains("unknown") || formatted.toLowerCase().contains("unrecognized"));
        }

        @Test
        @DisplayName("Missing argument error should list expected arguments")
        void missingArgumentErrorShouldListExpectedArguments() {
            ValidationError error = ValidationError.missingArgument("fade", "fade 1.0", "test_story", "test_scene", 15,
                "staySeconds", "float");

            String formatted = formatter.format(error);

            assertTrue(formatted.contains("fade"));
            assertTrue(formatted.toLowerCase().contains("missing") || formatted.toLowerCase().contains("required"));
            assertTrue(formatted.contains("staySeconds") || formatted.contains("stay"));
        }

        @Test
        @DisplayName("Invalid type error should show expected vs actual")
        void invalidTypeErrorShouldShowExpectedVsActual() {
            ValidationError error = ValidationError.invalidArgumentType("fade", "fade notanumber 2.0 1.0", "test_story", "test_scene", 20,
                "fadeInSeconds", "number", "notanumber");

            String formatted = formatter.format(error);

            assertTrue(formatted.contains("fade"));
            assertTrue(formatted.contains("notanumber") || formatted.toLowerCase().contains("invalid"));
            assertTrue(formatted.toLowerCase().contains("number") || formatted.toLowerCase().contains("numeric"));
        }

        @Test
        @DisplayName("Invalid value error should show constraints")
        void invalidValueErrorShouldShowConstraints() {
            ValidationError error = ValidationError.invalidArgumentValue("fade", "fade -1.0 2.0 1.0", "test_story", "test_scene", 25,
                "fadeInSeconds", "-1.0", "must be >= 0");

            String formatted = formatter.format(error);

            assertTrue(formatted.contains("fade"));
            assertTrue(formatted.contains("-1.0") || formatted.toLowerCase().contains("negative"));
            assertTrue(formatted.toLowerCase().contains("0") || formatted.toLowerCase().contains("positive"));
        }
    }

    @Nested
    @DisplayName("Batch Error Formatting")
    class BatchErrorFormatting {

        @Test
        @DisplayName("Multiple errors should be formatted together")
        void multipleErrorsShouldBeFormattedTogether() {
            List<ValidationError> errors = List.of(
                ValidationError.unknownTag("badtag1", "badtag1 arg", "test_story", "scene1", 10, null),
                ValidationError.unknownTag("badtag2", "badtag2 arg", "test_story", "scene2", 20, null),
                ValidationError.missingArgument("fade", "fade 1.0", "test_story", "scene3", 30, "staySeconds", "float")
            );

            String formatted = formatter.formatAll(errors);

            assertTrue(formatted.contains("badtag1"));
            assertTrue(formatted.contains("badtag2"));
            assertTrue(formatted.contains("fade"));
            assertTrue(formatted.contains("10") || formatted.contains("line 10"));
            assertTrue(formatted.contains("20") || formatted.contains("line 20"));
            assertTrue(formatted.contains("30") || formatted.contains("line 30"));
        }

        @Test
        @DisplayName("Should include error count header")
        void shouldIncludeErrorCountHeader() {
            List<ValidationError> errors = List.of(
                ValidationError.unknownTag("badtag1", "badtag1 arg", "test_story", "scene1", 10, null),
                ValidationError.unknownTag("badtag2", "badtag2 arg", "test_story", "scene2", 20, null)
            );

            String formatted = formatter.formatAll(errors);

            assertTrue(formatted.contains("2") || formatted.toLowerCase().contains("errors"),
                "Should indicate number of errors found");
        }

        @Test
        @DisplayName("Empty error list should return success message")
        void emptyErrorListShouldReturnSuccessMessage() {
            List<ValidationError> errors = List.of();

            String formatted = formatter.formatAll(errors);

            assertTrue(formatted.toLowerCase().contains("no errors") || formatted.toLowerCase().contains("valid") || formatted.toLowerCase().contains("success"));
        }
    }

    @Nested
    @DisplayName("Console Output Format")
    class ConsoleOutputFormat {

        @Test
        @DisplayName("Console format should be readable")
        void consoleFormatShouldBeReadable() {
            ValidationError error = ValidationError.unknownTag("badtag", "badtag arg", "my_story", "intro", 42, "border");

            String formatted = formatter.formatForConsole(error);

            // Should contain clear structure for log output
            assertFalse(formatted.isEmpty());
            assertTrue(formatted.split("\n").length >= 1, "Should have at least one line");
        }

        @Test
        @DisplayName("Console format should not have excessive whitespace")
        void consoleFormatShouldNotHaveExcessiveWhitespace() {
            ValidationError error = ValidationError.unknownTag("badtag", "badtag arg", "my_story", "intro", 42, null);

            String formatted = formatter.formatForConsole(error);

            assertFalse(formatted.contains("    \n    "), "Should not have blank lines with only spaces");
        }
    }

    @Nested
    @DisplayName("Chat Message Format")
    class ChatMessageFormat {

        @Test
        @DisplayName("Chat format should be concise")
        void chatFormatShouldBeConcise() {
            ValidationError error = ValidationError.unknownTag("badtag", "badtag arg", "my_story", "intro", 42, "border");

            String formatted = formatter.formatForChat(error);

            // Chat messages should be single line or very short
            assertTrue(formatted.length() < 300, "Chat message should be concise");
        }

        @Test
        @DisplayName("Chat format should include key information")
        void chatFormatShouldIncludeKeyInformation() {
            ValidationError error = ValidationError.unknownTag("badtag", "badtag arg", "my_story", "intro", 42, "border");

            String formatted = formatter.formatForChat(error);

            // Must contain: error type, location hint, suggestion if available
            assertTrue(formatted.contains("badtag") || formatted.toLowerCase().contains("unknown"));
            assertTrue(formatted.contains("42") || formatted.contains("intro"));
        }
    }
}
