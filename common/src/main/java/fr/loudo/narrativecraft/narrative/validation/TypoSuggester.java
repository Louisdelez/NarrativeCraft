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

package fr.loudo.narrativecraft.narrative.validation;

import java.util.Optional;
import java.util.Set;

/**
 * Suggests corrections for typos in tag names using Levenshtein distance.
 *
 * This helps provide "Did you mean X?" suggestions when users
 * make typos in their Ink scripts.
 */
public class TypoSuggester {

    /** Default maximum edit distance for suggestions */
    private static final int DEFAULT_MAX_DISTANCE = 3;

    private final Set<String> validValues;
    private final int maxDistance;

    /**
     * Creates a TypoSuggester with default max distance.
     *
     * @param validValues The set of valid values to suggest from
     */
    public TypoSuggester(Set<String> validValues) {
        this(validValues, DEFAULT_MAX_DISTANCE);
    }

    /**
     * Creates a TypoSuggester with custom max distance.
     *
     * @param validValues The set of valid values to suggest from
     * @param maxDistance Maximum edit distance for suggestions
     */
    public TypoSuggester(Set<String> validValues, int maxDistance) {
        this.validValues = validValues;
        this.maxDistance = maxDistance;
    }

    /**
     * Suggests a correction for the given input.
     *
     * @param input The potentially misspelled input
     * @return An Optional containing the best match, or empty if no good match found
     */
    public Optional<String> suggest(String input) {
        if (input == null || input.isEmpty()) {
            return Optional.empty();
        }

        String normalizedInput = normalizeInput(input);
        String bestMatch = null;
        int bestDistance = Integer.MAX_VALUE;

        for (String valid : validValues) {
            String normalizedValid = valid.toLowerCase();
            int distance = levenshteinDistance(normalizedInput, normalizedValid);

            if (distance < bestDistance && distance <= maxDistance) {
                bestDistance = distance;
                bestMatch = valid;
            }
        }

        return Optional.ofNullable(bestMatch);
    }

    /**
     * Normalizes input for comparison (lowercase, remove underscores).
     */
    private String normalizeInput(String input) {
        return input.toLowerCase().replace("_", " ");
    }

    /**
     * Calculates the Levenshtein distance between two strings.
     *
     * The Levenshtein distance is the minimum number of single-character edits
     * (insertions, deletions, substitutions) required to change one string into another.
     *
     * @param s1 First string
     * @param s2 Second string
     * @return The edit distance between the strings
     */
    public static int levenshteinDistance(String s1, String s2) {
        if (s1 == null) s1 = "";
        if (s2 == null) s2 = "";

        int m = s1.length();
        int n = s2.length();

        // Handle edge cases
        if (m == 0) return n;
        if (n == 0) return m;

        // Create distance matrix
        int[][] dp = new int[m + 1][n + 1];

        // Initialize first column (deletions)
        for (int i = 0; i <= m; i++) {
            dp[i][0] = i;
        }

        // Initialize first row (insertions)
        for (int j = 0; j <= n; j++) {
            dp[0][j] = j;
        }

        // Fill in the rest of the matrix
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;

                dp[i][j] = Math.min(
                    Math.min(
                        dp[i - 1][j] + 1,      // deletion
                        dp[i][j - 1] + 1       // insertion
                    ),
                    dp[i - 1][j - 1] + cost    // substitution
                );
            }
        }

        return dp[m][n];
    }

    /**
     * Checks if the input is within the maximum distance of any valid value.
     *
     * @param input The input to check
     * @return true if a suggestion is available
     */
    public boolean hasSuggestion(String input) {
        return suggest(input).isPresent();
    }

    /**
     * Returns the set of valid values this suggester uses.
     */
    public Set<String> getValidValues() {
        return validValues;
    }

    /**
     * Returns the maximum distance threshold.
     */
    public int getMaxDistance() {
        return maxDistance;
    }
}
