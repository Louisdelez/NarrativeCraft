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

package fr.loudo.narrativecraft.unit.validation;

import static org.junit.jupiter.api.Assertions.*;

import fr.loudo.narrativecraft.narrative.validation.TagValidator;
import fr.loudo.narrativecraft.narrative.validation.ValidationError;
import fr.loudo.narrativecraft.narrative.validation.ValidationResult;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for TagValidator.
 * Tests T061: Tag validation for unknown commands, bad arguments, wrong types.
 */
@DisplayName("TagValidator Tests")
class TagValidatorTest {

    private TagValidator validator;

    @BeforeEach
    void setUp() {
        validator = new TagValidator();
    }

    @Nested
    @DisplayName("Unknown Tag Detection")
    class UnknownTagDetection {

        @Test
        @DisplayName("Should detect unknown tag name")
        void shouldDetectUnknownTag() {
            ValidationResult result = validator.validateTag("unknowntag arg1 arg2", "test_story", "test_scene", 42);

            assertFalse(result.isValid());
            assertEquals(1, result.getErrors().size());

            ValidationError error = result.getErrors().get(0);
            assertEquals(ValidationError.ErrorCode.UNKNOWN_TAG, error.getCode());
            assertEquals("unknowntag", error.getTagName());
            assertEquals(42, error.getLineNumber());
        }

        @Test
        @DisplayName("Should suggest similar tag for typo")
        void shouldSuggestSimilarTagForTypo() {
            // "sund" is a typo for "sound" or "sfx"
            ValidationResult result = validator.validateTag(
                    "sund start minecraft:entity.generic.explode", "test_story", "test_scene", 10);

            assertFalse(result.isValid());
            ValidationError error = result.getErrors().get(0);
            assertEquals(ValidationError.ErrorCode.UNKNOWN_TAG, error.getCode());
            assertNotNull(error.getSuggestion());
            assertTrue(error.getSuggestion().contains("sfx")
                    || error.getSuggestion().contains("sound")
                    || error.getSuggestion().contains("song"));
        }

        @Test
        @DisplayName("Should accept valid tag names")
        void shouldAcceptValidTagNames() {
            Set<String> validTags = Set.of(
                    "fade 1.0 2.0 1.0",
                    "sfx start minecraft:entity.generic.explode",
                    "song start minecraft:music.creative",
                    "wait 2 seconds",
                    "weather clear",
                    "shake 1.0 0.5 0.3",
                    "save",
                    "gameplay",
                    "border 10 10 10 10");

            for (String tag : validTags) {
                ValidationResult result = validator.validateTag(tag, "test_story", "test_scene", 1);
                assertTrue(
                        result.getErrors().stream()
                                .noneMatch(e -> e.getCode() == ValidationError.ErrorCode.UNKNOWN_TAG),
                        "Tag should be recognized: " + tag);
            }
        }
    }

    @Nested
    @DisplayName("Missing Argument Detection")
    class MissingArgumentDetection {

        @Test
        @DisplayName("Should detect missing required arguments for fade")
        void shouldDetectMissingArgsForFade() {
            // fade requires 3 time values
            ValidationResult result = validator.validateTag("fade 1.0", "test_story", "test_scene", 15);

            assertFalse(result.isValid());
            ValidationError error = result.getErrors().get(0);
            assertEquals(ValidationError.ErrorCode.MISSING_ARGUMENT, error.getCode());
        }

        @Test
        @DisplayName("Should detect missing required arguments for sound")
        void shouldDetectMissingArgsForSound() {
            // sfx requires action (start/stop) and sound ID
            ValidationResult result = validator.validateTag("sfx start", "test_story", "test_scene", 20);

            assertFalse(result.isValid());
            ValidationError error = result.getErrors().get(0);
            assertEquals(ValidationError.ErrorCode.MISSING_ARGUMENT, error.getCode());
        }

        @Test
        @DisplayName("Should detect missing required arguments for wait")
        void shouldDetectMissingArgsForWait() {
            // wait requires time value and unit
            ValidationResult result = validator.validateTag("wait 5", "test_story", "test_scene", 25);

            assertFalse(result.isValid());
            ValidationError error = result.getErrors().get(0);
            assertEquals(ValidationError.ErrorCode.MISSING_ARGUMENT, error.getCode());
        }

        @Test
        @DisplayName("Should detect missing required arguments for shake")
        void shouldDetectMissingArgsForShake() {
            // shake requires strength, decay_rate, speed
            ValidationResult result = validator.validateTag("shake 1.0", "test_story", "test_scene", 30);

            assertFalse(result.isValid());
            ValidationError error = result.getErrors().get(0);
            assertEquals(ValidationError.ErrorCode.MISSING_ARGUMENT, error.getCode());
        }
    }

    @Nested
    @DisplayName("Invalid Argument Type Detection")
    class InvalidArgumentTypeDetection {

        @Test
        @DisplayName("Should detect non-numeric value for fade time")
        void shouldDetectNonNumericForFadeTime() {
            ValidationResult result = validator.validateTag("fade notanumber 2.0 1.0", "test_story", "test_scene", 35);

            assertFalse(result.isValid());
            ValidationError error = result.getErrors().get(0);
            assertEquals(ValidationError.ErrorCode.INVALID_ARGUMENT_TYPE, error.getCode());
        }

        @Test
        @DisplayName("Should detect invalid time unit for wait")
        void shouldDetectInvalidTimeUnitForWait() {
            ValidationResult result = validator.validateTag("wait 5 blargs", "test_story", "test_scene", 40);

            assertFalse(result.isValid());
            ValidationError error = result.getErrors().get(0);
            assertEquals(ValidationError.ErrorCode.INVALID_ARGUMENT_TYPE, error.getCode());
        }

        @Test
        @DisplayName("Should detect invalid action for sound")
        void shouldDetectInvalidActionForSound() {
            // sfx action must be start or stop
            ValidationResult result =
                    validator.validateTag("sfx pause minecraft:entity.generic.explode", "test_story", "test_scene", 45);

            assertFalse(result.isValid());
            ValidationError error = result.getErrors().get(0);
            assertEquals(ValidationError.ErrorCode.INVALID_ARGUMENT_TYPE, error.getCode());
        }

        @Test
        @DisplayName("Should detect invalid weather type")
        void shouldDetectInvalidWeatherType() {
            ValidationResult result = validator.validateTag("weather sunny", "test_story", "test_scene", 50);

            assertFalse(result.isValid());
            ValidationError error = result.getErrors().get(0);
            assertEquals(ValidationError.ErrorCode.INVALID_ARGUMENT_TYPE, error.getCode());
        }

        @Test
        @DisplayName("Should detect invalid hex color for fade")
        void shouldDetectInvalidHexColorForFade() {
            ValidationResult result = validator.validateTag("fade 1.0 2.0 1.0 GGGGGG", "test_story", "test_scene", 55);

            assertFalse(result.isValid());
            ValidationError error = result.getErrors().get(0);
            assertEquals(ValidationError.ErrorCode.INVALID_ARGUMENT_TYPE, error.getCode());
        }
    }

    @Nested
    @DisplayName("Invalid Argument Value Detection")
    class InvalidArgumentValueDetection {

        @Test
        @DisplayName("Should detect negative time value for fade")
        void shouldDetectNegativeTimeForFade() {
            ValidationResult result = validator.validateTag("fade -1.0 2.0 1.0", "test_story", "test_scene", 60);

            assertFalse(result.isValid());
            ValidationError error = result.getErrors().get(0);
            assertEquals(ValidationError.ErrorCode.INVALID_ARGUMENT_VALUE, error.getCode());
        }

        @Test
        @DisplayName("Should detect opacity out of range for border")
        void shouldDetectOpacityOutOfRangeForBorder() {
            // opacity should be 0.0-1.0
            ValidationResult result =
                    validator.validateTag("border 10 10 10 10 000000 1.5", "test_story", "test_scene", 65);

            assertFalse(result.isValid());
            ValidationError error = result.getErrors().get(0);
            assertEquals(ValidationError.ErrorCode.INVALID_ARGUMENT_VALUE, error.getCode());
        }
    }

    @Nested
    @DisplayName("Error Context Information")
    class ErrorContextInformation {

        @Test
        @DisplayName("Error should contain story name")
        void errorShouldContainStoryName() {
            ValidationResult result = validator.validateTag("unknowntag", "my_story", "test_scene", 10);

            assertFalse(result.isValid());
            ValidationError error = result.getErrors().get(0);
            assertEquals("my_story", error.getStoryName());
        }

        @Test
        @DisplayName("Error should contain scene name")
        void errorShouldContainSceneName() {
            ValidationResult result = validator.validateTag("unknowntag", "my_story", "intro_scene", 10);

            assertFalse(result.isValid());
            ValidationError error = result.getErrors().get(0);
            assertEquals("intro_scene", error.getSceneName());
        }

        @Test
        @DisplayName("Error should contain line number")
        void errorShouldContainLineNumber() {
            ValidationResult result = validator.validateTag("unknowntag", "my_story", "test_scene", 123);

            assertFalse(result.isValid());
            ValidationError error = result.getErrors().get(0);
            assertEquals(123, error.getLineNumber());
        }

        @Test
        @DisplayName("Error should contain original command")
        void errorShouldContainOriginalCommand() {
            String command = "fade notanumber 2.0 1.0";
            ValidationResult result = validator.validateTag(command, "my_story", "test_scene", 10);

            assertFalse(result.isValid());
            ValidationError error = result.getErrors().get(0);
            assertEquals(command, error.getOriginalCommand());
        }
    }

    @Nested
    @DisplayName("Valid Tags Pass Validation")
    class ValidTagsPassValidation {

        @Test
        @DisplayName("Valid fade tag should pass")
        void validFadeTagShouldPass() {
            ValidationResult result = validator.validateTag("fade 1.0 2.0 1.0", "test_story", "test_scene", 1);
            assertTrue(result.isValid());
        }

        @Test
        @DisplayName("Valid fade tag with color should pass")
        void validFadeTagWithColorShouldPass() {
            ValidationResult result = validator.validateTag("fade 1.0 2.0 1.0 FF0000", "test_story", "test_scene", 1);
            assertTrue(result.isValid());
        }

        @Test
        @DisplayName("Valid sound tag should pass")
        void validSoundTagShouldPass() {
            ValidationResult result =
                    validator.validateTag("sfx start minecraft:entity.generic.explode", "test_story", "test_scene", 1);
            assertTrue(result.isValid());
        }

        @Test
        @DisplayName("Valid wait tag should pass")
        void validWaitTagShouldPass() {
            ValidationResult result = validator.validateTag("wait 5 seconds", "test_story", "test_scene", 1);
            assertTrue(result.isValid());
        }

        @Test
        @DisplayName("Valid weather tag should pass")
        void validWeatherTagShouldPass() {
            ValidationResult result = validator.validateTag("weather rain", "test_story", "test_scene", 1);
            assertTrue(result.isValid());
        }

        @Test
        @DisplayName("Valid shake tag should pass")
        void validShakeTagShouldPass() {
            ValidationResult result = validator.validateTag("shake 1.0 0.5 0.3", "test_story", "test_scene", 1);
            assertTrue(result.isValid());
        }

        @Test
        @DisplayName("Valid border tag should pass")
        void validBorderTagShouldPass() {
            ValidationResult result = validator.validateTag("border 10 20 10 20", "test_story", "test_scene", 1);
            assertTrue(result.isValid());
        }

        @Test
        @DisplayName("Valid save tag should pass")
        void validSaveTagShouldPass() {
            ValidationResult result = validator.validateTag("save", "test_story", "test_scene", 1);
            assertTrue(result.isValid());
        }

        @Test
        @DisplayName("Valid gameplay tag should pass")
        void validGameplayTagShouldPass() {
            ValidationResult result = validator.validateTag("gameplay", "test_story", "test_scene", 1);
            assertTrue(result.isValid());
        }
    }

    @Nested
    @DisplayName("Batch Validation")
    class BatchValidation {

        @Test
        @DisplayName("Should validate multiple tags and collect all errors")
        void shouldValidateMultipleTagsAndCollectAllErrors() {
            String[] tags = {
                "unknowntag arg",
                "fade 1.0 2.0 1.0", // valid
                "wait 5", // missing time unit
                "sfx pause sound" // invalid action
            };

            ValidationResult result = validator.validateTags(tags, "test_story", "test_scene", new int[] {1, 2, 3, 4});

            assertFalse(result.isValid());
            assertEquals(3, result.getErrors().size()); // 3 invalid tags
        }

        @Test
        @DisplayName("All valid tags should result in valid result")
        void allValidTagsShouldResultInValidResult() {
            String[] tags = {"fade 1.0 2.0 1.0", "save", "weather clear"};

            ValidationResult result = validator.validateTags(tags, "test_story", "test_scene", new int[] {1, 2, 3});

            assertTrue(result.isValid());
            assertEquals(0, result.getErrors().size());
        }
    }
}
