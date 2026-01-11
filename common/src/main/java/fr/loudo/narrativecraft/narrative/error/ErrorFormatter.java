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

package fr.loudo.narrativecraft.narrative.error;

import fr.loudo.narrativecraft.narrative.validation.ValidationError;
import java.util.List;

/**
 * Formats validation errors into user-friendly messages.
 *
 * Uses a 4-component format for maximum clarity:
 * - WHAT: What went wrong (error type and description)
 * - WHERE: Location in the story/scene/line
 * - WHY: Reason/cause of the error
 * - FIX: Suggestion for how to fix it
 */
public class ErrorFormatter {

    private static final String DIVIDER = "----------------------------------------";

    /**
     * Formats a single validation error with full detail.
     *
     * @param error The validation error to format
     * @return A human-readable error message
     */
    public String format(ValidationError error) {
        StringBuilder sb = new StringBuilder();

        // [WHAT] Error type and summary
        sb.append("[WHAT] ").append(error.getCode().getDescription()).append("\n");
        sb.append("  Error: ").append(error.getTagName()).append(" tag is invalid\n");

        // [WHERE] Location information
        sb.append("[WHERE] ");
        sb.append("Story: ").append(error.getStoryName());
        sb.append(" / Scene: ").append(error.getSceneName());
        sb.append(" / Line: ").append(error.getLineNumber()).append("\n");
        if (error.getOriginalCommand() != null) {
            sb.append("  Command: ").append(error.getOriginalCommand()).append("\n");
        }

        // [WHY] Reason/cause
        sb.append("[WHY] ").append(error.getReason()).append("\n");

        // [FIX] Suggestion
        if (error.getSuggestion() != null) {
            sb.append("[FIX] ").append(error.getSuggestion()).append("\n");
        }

        return sb.toString();
    }

    /**
     * Formats multiple validation errors into a report.
     *
     * @param errors List of validation errors
     * @return A formatted error report
     */
    public String formatAll(List<ValidationError> errors) {
        if (errors.isEmpty()) {
            return "No errors found. All tags are valid.";
        }

        StringBuilder sb = new StringBuilder();

        // Header
        sb.append(DIVIDER).append("\n");
        sb.append("NarrativeCraft Validation Report\n");
        sb.append("Found ").append(errors.size()).append(" error(s)\n");
        sb.append(DIVIDER).append("\n\n");

        // Each error
        for (int i = 0; i < errors.size(); i++) {
            sb.append("Error ")
                    .append(i + 1)
                    .append(" of ")
                    .append(errors.size())
                    .append(":\n");
            sb.append(format(errors.get(i)));
            sb.append("\n");
        }

        sb.append(DIVIDER).append("\n");

        return sb.toString();
    }

    /**
     * Formats an error for console/log output.
     * Single-line format optimized for log readability.
     *
     * @param error The validation error to format
     * @return A concise log-friendly message
     */
    public String formatForConsole(ValidationError error) {
        StringBuilder sb = new StringBuilder();

        sb.append("[NarrativeCraft] ");
        sb.append(error.getCode().getDescription()).append(" in ");
        sb.append(error.getStoryName()).append("/").append(error.getSceneName());
        sb.append(":").append(error.getLineNumber());
        sb.append(" - ").append(error.getReason());

        if (error.getSuggestion() != null) {
            sb.append(" | Suggestion: ").append(error.getSuggestion());
        }

        return sb.toString();
    }

    /**
     * Formats an error for in-game chat display.
     * Very concise format suitable for chat messages.
     *
     * @param error The validation error to format
     * @return A chat-friendly message
     */
    public String formatForChat(ValidationError error) {
        StringBuilder sb = new StringBuilder();

        // Keep it short for chat
        sb.append("[Tag Error] ");
        sb.append(error.getTagName());
        sb.append(" at ").append(error.getSceneName()).append(":").append(error.getLineNumber());

        if (error.getSuggestion() != null) {
            // Extract just the key suggestion
            String suggestion = error.getSuggestion();
            if (suggestion.length() > 50) {
                suggestion = suggestion.substring(0, 47) + "...";
            }
            sb.append(" - ").append(suggestion);
        }

        return sb.toString();
    }

    /**
     * Formats an error with ANSI colors for terminal output.
     *
     * @param error The validation error to format
     * @return A colorized message (if terminal supports ANSI)
     */
    public String formatWithColors(ValidationError error) {
        final String RESET = "\u001B[0m";
        final String RED = "\u001B[31m";
        final String YELLOW = "\u001B[33m";
        final String CYAN = "\u001B[36m";
        final String WHITE = "\u001B[37m";

        StringBuilder sb = new StringBuilder();

        // [WHAT] in red
        sb.append(RED)
                .append("[WHAT] ")
                .append(error.getCode().getDescription())
                .append(RESET)
                .append("\n");
        sb.append("  Error: ").append(error.getTagName()).append(" tag is invalid\n");

        // [WHERE] in cyan
        sb.append(CYAN).append("[WHERE] ").append(RESET);
        sb.append("Story: ").append(error.getStoryName());
        sb.append(" / Scene: ").append(error.getSceneName());
        sb.append(" / Line: ").append(error.getLineNumber()).append("\n");
        if (error.getOriginalCommand() != null) {
            sb.append("  Command: ")
                    .append(WHITE)
                    .append(error.getOriginalCommand())
                    .append(RESET)
                    .append("\n");
        }

        // [WHY] in yellow
        sb.append(YELLOW)
                .append("[WHY] ")
                .append(error.getReason())
                .append(RESET)
                .append("\n");

        // [FIX] in white
        if (error.getSuggestion() != null) {
            sb.append(WHITE)
                    .append("[FIX] ")
                    .append(error.getSuggestion())
                    .append(RESET)
                    .append("\n");
        }

        return sb.toString();
    }

    /**
     * Creates a summary line for multiple errors (for quick overview).
     *
     * @param errors List of validation errors
     * @return A one-line summary
     */
    public String formatSummary(List<ValidationError> errors) {
        if (errors.isEmpty()) {
            return "Validation passed: no errors";
        }

        // Count by type
        long unknownTags = errors.stream()
                .filter(e -> e.getCode() == ValidationError.ErrorCode.UNKNOWN_TAG)
                .count();
        long missingArgs = errors.stream()
                .filter(e -> e.getCode() == ValidationError.ErrorCode.MISSING_ARGUMENT)
                .count();
        long invalidTypes = errors.stream()
                .filter(e -> e.getCode() == ValidationError.ErrorCode.INVALID_ARGUMENT_TYPE)
                .count();
        long invalidValues = errors.stream()
                .filter(e -> e.getCode() == ValidationError.ErrorCode.INVALID_ARGUMENT_VALUE)
                .count();

        StringBuilder sb = new StringBuilder();
        sb.append("Validation failed: ").append(errors.size()).append(" error(s) (");

        boolean first = true;
        if (unknownTags > 0) {
            sb.append(unknownTags).append(" unknown");
            first = false;
        }
        if (missingArgs > 0) {
            if (!first) sb.append(", ");
            sb.append(missingArgs).append(" missing args");
            first = false;
        }
        if (invalidTypes > 0) {
            if (!first) sb.append(", ");
            sb.append(invalidTypes).append(" invalid types");
            first = false;
        }
        if (invalidValues > 0) {
            if (!first) sb.append(", ");
            sb.append(invalidValues).append(" invalid values");
        }

        sb.append(")");
        return sb.toString();
    }
}
