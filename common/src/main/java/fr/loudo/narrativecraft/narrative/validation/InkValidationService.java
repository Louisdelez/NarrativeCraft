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

import fr.loudo.narrativecraft.api.inkAction.InkAction;
import fr.loudo.narrativecraft.api.inkAction.InkActionRegistry;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.error.ErrorFormatter;
import fr.loudo.narrativecraft.narrative.security.CommandProxy;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for validating Ink tags during story loading and execution.
 *
 * This service integrates with the existing NarrativeCraft validation system
 * to provide enhanced validation with friendly error messages and typo suggestions.
 *
 * Integration points:
 * - InkTagHandler.execute(): Runtime tag validation
 * - StoryValidation.validate(): Pre-launch validation
 */
public class InkValidationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(InkValidationService.class);

    private static InkValidationService instance;

    private final TagValidator tagValidator;
    private final CommandProxy commandProxy;
    private final ErrorFormatter errorFormatter;
    private final TypoSuggester tagSuggester;

    private InkValidationService() {
        this.tagValidator = new TagValidator();
        this.commandProxy = new CommandProxy();
        this.errorFormatter = new ErrorFormatter();
        this.tagSuggester = new TypoSuggester(TagValidator.getValidTags());
    }

    /**
     * Gets the singleton instance.
     */
    public static InkValidationService getInstance() {
        if (instance == null) {
            instance = new InkValidationService();
        }
        return instance;
    }

    /**
     * Validates a tag at runtime during story execution.
     *
     * This method is called from InkTagHandler when a tag is about to be executed.
     * It provides enhanced validation beyond what InkActionRegistry.findByCommand() offers.
     *
     * @param tag The full tag command (e.g., "fade 1.0 2.0 1.0")
     * @param storyName The current story name
     * @param scene The current scene (may be null for global context)
     * @param lineNumber Line number (0 if unknown at runtime)
     * @return ValidationResult with any errors found
     */
    public ValidationResult validateTag(String tag, String storyName, @Nullable Scene scene, int lineNumber) {
        String sceneName = scene != null ? scene.getName() : "unknown";

        // First check if it's a recognized tag
        InkAction action = InkActionRegistry.findByCommand(tag);

        if (action == null) {
            // Tag not found in registry - check if it's a typo
            String tagName = extractTagName(tag);
            return tagValidator.validateTag(tag, storyName, sceneName, lineNumber);
        }

        // If it's a command tag, validate security
        if (tagName(tag).equalsIgnoreCase("command")) {
            String commandContent = extractCommandContent(tag);
            if (commandContent != null) {
                ValidationResult securityResult = commandProxy.validateCommand(
                    commandContent, storyName, sceneName, lineNumber
                );
                if (securityResult.hasErrors()) {
                    return securityResult;
                }
            }
        }

        // Tag is valid - pass basic validation
        return ValidationResult.success();
    }

    /**
     * Validates all tags in a story file during pre-launch validation.
     *
     * This method is called from StoryValidation.validate() to enhance
     * error messages with typo suggestions and formatting.
     *
     * @param tags List of tag commands extracted from the story file
     * @param storyName The story name
     * @param sceneName The scene name
     * @param lineNumbers Array of line numbers for each tag
     * @return ValidationResult with all errors found
     */
    public ValidationResult validateAllTags(List<String> tags, String storyName, String sceneName, int[] lineNumbers) {
        List<ValidationError> allErrors = new ArrayList<>();

        for (int i = 0; i < tags.size(); i++) {
            String tag = tags.get(i);
            int lineNumber = i < lineNumbers.length ? lineNumbers[i] : 0;

            ValidationResult result = tagValidator.validateTag(tag, storyName, sceneName, lineNumber);
            allErrors.addAll(result.getErrors());
        }

        return ValidationResult.fromErrors(allErrors, tags.size());
    }

    /**
     * Validates an unknown tag and provides a helpful error message.
     *
     * This method is called when InkActionRegistry.findByCommand() returns null,
     * indicating the tag is not recognized.
     *
     * @param tag The unrecognized tag command
     * @param storyName The story name
     * @param sceneName The scene name
     * @param lineNumber The line number
     * @return ValidationError with suggestion if available
     */
    public ValidationError createUnknownTagError(String tag, String storyName, String sceneName, int lineNumber) {
        String tagName = extractTagName(tag);
        String suggestion = tagSuggester.suggest(tagName).orElse(null);

        return ValidationError.unknownTag(tagName, tag, storyName, sceneName, lineNumber, suggestion);
    }

    /**
     * Formats a validation error for console output.
     */
    public String formatErrorForConsole(ValidationError error) {
        return errorFormatter.formatForConsole(error);
    }

    /**
     * Formats a validation error for in-game chat.
     */
    public String formatErrorForChat(ValidationError error) {
        return errorFormatter.formatForChat(error);
    }

    /**
     * Formats all validation errors into a report.
     */
    public String formatErrorReport(List<ValidationError> errors) {
        return errorFormatter.formatAll(errors);
    }

    /**
     * Logs a validation error to the console.
     */
    public void logError(ValidationError error) {
        LOGGER.warn(formatErrorForConsole(error));
    }

    /**
     * Logs all validation errors to the console.
     */
    public void logErrors(List<ValidationError> errors) {
        for (ValidationError error : errors) {
            logError(error);
        }
    }

    /**
     * Gets the command proxy for security validation.
     */
    public CommandProxy getCommandProxy() {
        return commandProxy;
    }

    /**
     * Gets the tag validator.
     */
    public TagValidator getTagValidator() {
        return tagValidator;
    }

    /**
     * Gets the error formatter.
     */
    public ErrorFormatter getErrorFormatter() {
        return errorFormatter;
    }

    // Helper methods

    private String extractTagName(String tag) {
        if (tag == null || tag.isEmpty()) {
            return "";
        }
        String trimmed = tag.trim();
        int spaceIndex = trimmed.indexOf(' ');
        return spaceIndex == -1 ? trimmed : trimmed.substring(0, spaceIndex);
    }

    private String tagName(String tag) {
        return extractTagName(tag).toLowerCase();
    }

    private String extractCommandContent(String tag) {
        // Extract the command from "command \"actual command here\""
        int firstQuote = tag.indexOf('"');
        int lastQuote = tag.lastIndexOf('"');
        if (firstQuote != -1 && lastQuote > firstQuote) {
            return tag.substring(firstQuote + 1, lastQuote);
        }
        // Try without quotes
        int spaceIndex = tag.indexOf(' ');
        if (spaceIndex != -1) {
            return tag.substring(spaceIndex + 1).trim();
        }
        return null;
    }
}
