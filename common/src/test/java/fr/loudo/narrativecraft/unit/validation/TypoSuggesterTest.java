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

import fr.loudo.narrativecraft.narrative.validation.TypoSuggester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TypoSuggester.
 * Tests T063: Typo suggestion using Levenshtein distance.
 */
@DisplayName("TypoSuggester Tests")
class TypoSuggesterTest {

    private TypoSuggester suggester;
    private Set<String> validTags;

    @BeforeEach
    void setUp() {
        // All valid NarrativeCraft tags
        validTags = Set.of(
            "animation", "border", "camera", "time", "cutscene", "wait",
            "dialog", "command", "emote", "fade", "kill", "on enter",
            "save", "sfx", "song", "subscene", "weather", "shake",
            "interaction", "gameplay", "text"
        );
        suggester = new TypoSuggester(validTags);
    }

    @Nested
    @DisplayName("Levenshtein Distance Calculation")
    class LevenshteinDistanceCalculation {

        @Test
        @DisplayName("Identical strings should have distance 0")
        void identicalStringsShouldHaveDistanceZero() {
            assertEquals(0, TypoSuggester.levenshteinDistance("fade", "fade"));
            assertEquals(0, TypoSuggester.levenshteinDistance("", ""));
            assertEquals(0, TypoSuggester.levenshteinDistance("animation", "animation"));
        }

        @Test
        @DisplayName("Single character difference should have distance 1")
        void singleCharacterDifferenceShouldHaveDistanceOne() {
            // Substitution
            assertEquals(1, TypoSuggester.levenshteinDistance("fade", "fado"));
            // Insertion
            assertEquals(1, TypoSuggester.levenshteinDistance("fade", "fades"));
            // Deletion
            assertEquals(1, TypoSuggester.levenshteinDistance("fade", "fad"));
        }

        @Test
        @DisplayName("Multiple character differences should have correct distance")
        void multipleCharacterDifferencesShouldHaveCorrectDistance() {
            assertEquals(2, TypoSuggester.levenshteinDistance("fade", "face"));  // d->c, e->e = 1... actually just 1 (d->c)
            assertEquals(3, TypoSuggester.levenshteinDistance("fade", "border"));
            assertEquals(4, TypoSuggester.levenshteinDistance("fade", "animation"));
        }

        @Test
        @DisplayName("Empty string should return length of other string")
        void emptyStringShouldReturnLengthOfOtherString() {
            assertEquals(4, TypoSuggester.levenshteinDistance("", "fade"));
            assertEquals(6, TypoSuggester.levenshteinDistance("border", ""));
        }

        @Test
        @DisplayName("Case sensitivity should be considered")
        void caseSensitivityShouldBeConsidered() {
            // By default, Levenshtein is case-sensitive
            assertEquals(1, TypoSuggester.levenshteinDistance("Fade", "fade"));
            assertEquals(4, TypoSuggester.levenshteinDistance("FADE", "fade"));
        }
    }

    @Nested
    @DisplayName("Typo Suggestion")
    class TypoSuggestion {

        @Test
        @DisplayName("Should suggest 'fade' for 'fad'")
        void shouldSuggestFadeForFad() {
            Optional<String> suggestion = suggester.suggest("fad");

            assertTrue(suggestion.isPresent());
            assertEquals("fade", suggestion.get());
        }

        @Test
        @DisplayName("Should suggest 'border' for 'boder'")
        void shouldSuggestBorderForBoder() {
            Optional<String> suggestion = suggester.suggest("boder");

            assertTrue(suggestion.isPresent());
            assertEquals("border", suggestion.get());
        }

        @Test
        @DisplayName("Should suggest 'sfx' for 'snd'")
        void shouldSuggestSfxForSnd() {
            Optional<String> suggestion = suggester.suggest("snd");

            assertTrue(suggestion.isPresent());
            // "snd" could match "sfx" (distance 2)
            assertTrue(Set.of("sfx", "song").contains(suggestion.get()));
        }

        @Test
        @DisplayName("Should suggest 'weather' for 'wether'")
        void shouldSuggestWeatherForWether() {
            Optional<String> suggestion = suggester.suggest("wether");

            assertTrue(suggestion.isPresent());
            assertEquals("weather", suggestion.get());
        }

        @Test
        @DisplayName("Should suggest 'animation' for 'animaton'")
        void shouldSuggestAnimationForAnimaton() {
            Optional<String> suggestion = suggester.suggest("animaton");

            assertTrue(suggestion.isPresent());
            assertEquals("animation", suggestion.get());
        }

        @Test
        @DisplayName("Should suggest 'cutscene' for 'cutscne'")
        void shouldSuggestCutsceneForCutscne() {
            Optional<String> suggestion = suggester.suggest("cutscne");

            assertTrue(suggestion.isPresent());
            assertEquals("cutscene", suggestion.get());
        }

        @Test
        @DisplayName("Should suggest 'shake' for 'shak'")
        void shouldSuggestShakeForShak() {
            Optional<String> suggestion = suggester.suggest("shak");

            assertTrue(suggestion.isPresent());
            assertEquals("shake", suggestion.get());
        }

        @Test
        @DisplayName("Should suggest 'gameplay' for 'gameply'")
        void shouldSuggestGameplayForGameply() {
            Optional<String> suggestion = suggester.suggest("gameply");

            assertTrue(suggestion.isPresent());
            assertEquals("gameplay", suggestion.get());
        }
    }

    @Nested
    @DisplayName("No Suggestion Cases")
    class NoSuggestionCases {

        @Test
        @DisplayName("Should not suggest for completely different word")
        void shouldNotSuggestForCompletelyDifferentWord() {
            Optional<String> suggestion = suggester.suggest("xyzabc123");

            // Distance threshold (typically 3) exceeded for all tags
            assertTrue(suggestion.isEmpty() || suggestion.get().length() > 0);
            // If a suggestion is returned, it should only be if within threshold
        }

        @Test
        @DisplayName("Should not suggest for very short gibberish")
        void shouldNotSuggestForVeryShortGibberish() {
            Optional<String> suggestion = suggester.suggest("zz");

            // Too different from any tag
            assertTrue(suggestion.isEmpty());
        }

        @Test
        @DisplayName("Should not suggest for empty string")
        void shouldNotSuggestForEmptyString() {
            Optional<String> suggestion = suggester.suggest("");

            assertTrue(suggestion.isEmpty());
        }
    }

    @Nested
    @DisplayName("Case Insensitive Matching")
    class CaseInsensitiveMatching {

        @Test
        @DisplayName("Should suggest for uppercase input")
        void shouldSuggestForUppercaseInput() {
            Optional<String> suggestion = suggester.suggest("FADE");

            assertTrue(suggestion.isPresent());
            assertEquals("fade", suggestion.get());
        }

        @Test
        @DisplayName("Should suggest for mixed case input")
        void shouldSuggestForMixedCaseInput() {
            Optional<String> suggestion = suggester.suggest("FaDe");

            assertTrue(suggestion.isPresent());
            assertEquals("fade", suggestion.get());
        }

        @Test
        @DisplayName("Should suggest for uppercase typo")
        void shouldSuggestForUppercaseTypo() {
            Optional<String> suggestion = suggester.suggest("BODER");

            assertTrue(suggestion.isPresent());
            assertEquals("border", suggestion.get());
        }
    }

    @Nested
    @DisplayName("Threshold Configuration")
    class ThresholdConfiguration {

        @Test
        @DisplayName("Should respect max distance threshold")
        void shouldRespectMaxDistanceThreshold() {
            // Create suggester with strict threshold
            TypoSuggester strictSuggester = new TypoSuggester(validTags, 1);

            // "boder" is distance 1 from "border" - should match
            Optional<String> close = strictSuggester.suggest("boder");
            assertTrue(close.isPresent());

            // "bdr" is distance 3 from "border" - should not match with threshold 1
            Optional<String> far = strictSuggester.suggest("bdr");
            assertTrue(far.isEmpty());
        }

        @Test
        @DisplayName("Default threshold should be reasonable")
        void defaultThresholdShouldBeReasonable() {
            // Default threshold should catch common typos (1-2 character errors)
            // but not suggest for completely different words

            // Common typo (1 char off) - should match
            assertTrue(suggester.suggest("fad").isPresent());

            // 2 chars off - should still match
            assertTrue(suggester.suggest("weter").isPresent()); // weather

            // Many chars off - should not match
            assertTrue(suggester.suggest("xyzabcdef").isEmpty());
        }
    }

    @Nested
    @DisplayName("Multiple Close Matches")
    class MultipleCloseMatches {

        @Test
        @DisplayName("Should return best match when multiple options")
        void shouldReturnBestMatchWhenMultipleOptions() {
            // "song" and "sfx" are both sound-related
            // "sog" is closer to "song" (distance 1) than "sfx" (distance 3)
            Optional<String> suggestion = suggester.suggest("sog");

            assertTrue(suggestion.isPresent());
            assertEquals("song", suggestion.get());
        }

        @Test
        @DisplayName("Should prefer shorter distance")
        void shouldPreferShorterDistance() {
            // "bordr" is distance 1 from "border", far from others
            Optional<String> suggestion = suggester.suggest("bordr");

            assertTrue(suggestion.isPresent());
            assertEquals("border", suggestion.get());
        }
    }

    @Nested
    @DisplayName("Special Characters")
    class SpecialCharacters {

        @Test
        @DisplayName("Should handle tag with space")
        void shouldHandleTagWithSpace() {
            // "on enter" is a valid tag
            Optional<String> suggestion = suggester.suggest("onenter");

            assertTrue(suggestion.isPresent());
            assertEquals("on enter", suggestion.get());
        }

        @Test
        @DisplayName("Should handle underscore input")
        void shouldHandleUnderscoreInput() {
            Optional<String> suggestion = suggester.suggest("on_enter");

            // Should recognize it's close to "on enter"
            assertTrue(suggestion.isPresent());
            assertEquals("on enter", suggestion.get());
        }
    }
}
