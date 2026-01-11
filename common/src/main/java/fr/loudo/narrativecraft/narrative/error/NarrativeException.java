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
import org.jetbrains.annotations.Nullable;

/**
 * Structured exception for NarrativeCraft errors.
 *
 * Provides detailed context information for debugging and user feedback:
 * - Error code for programmatic handling
 * - Human-readable message
 * - Location information (story, scene, line)
 * - Suggestion for fixing the error
 */
public class NarrativeException extends RuntimeException {

    /**
     * Error categories for NarrativeCraft exceptions.
     */
    public enum ErrorCategory {
        VALIDATION("Validation Error"),
        LOADING("Loading Error"),
        EXECUTION("Execution Error"),
        STATE("State Error"),
        SECURITY("Security Error"),
        RESOURCE("Resource Error"),
        INTERNAL("Internal Error");

        private final String displayName;

        ErrorCategory(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private final ErrorCategory category;
    private final String errorCode;

    @Nullable
    private final String storyName;

    @Nullable
    private final String sceneName;

    private final int lineNumber;

    @Nullable
    private final String originalCommand;

    @Nullable
    private final String suggestion;

    private NarrativeException(Builder builder) {
        super(builder.message, builder.cause);
        this.category = builder.category;
        this.errorCode = builder.errorCode;
        this.storyName = builder.storyName;
        this.sceneName = builder.sceneName;
        this.lineNumber = builder.lineNumber;
        this.originalCommand = builder.originalCommand;
        this.suggestion = builder.suggestion;
    }

    // Factory methods

    /**
     * Creates a validation exception from a ValidationError.
     */
    public static NarrativeException fromValidationError(ValidationError error) {
        return new Builder()
                .category(ErrorCategory.VALIDATION)
                .errorCode(error.getCode().name())
                .message(error.getReason())
                .storyName(error.getStoryName())
                .sceneName(error.getSceneName())
                .lineNumber(error.getLineNumber())
                .originalCommand(error.getOriginalCommand())
                .suggestion(error.getSuggestion())
                .build();
    }

    /**
     * Creates a loading error (e.g., story file not found).
     */
    public static NarrativeException loadingError(String message, String storyName) {
        return new Builder()
                .category(ErrorCategory.LOADING)
                .errorCode("LOAD_FAILED")
                .message(message)
                .storyName(storyName)
                .build();
    }

    /**
     * Creates a loading error with cause.
     */
    public static NarrativeException loadingError(String message, String storyName, Throwable cause) {
        return new Builder()
                .category(ErrorCategory.LOADING)
                .errorCode("LOAD_FAILED")
                .message(message)
                .storyName(storyName)
                .cause(cause)
                .build();
    }

    /**
     * Creates an execution error (runtime error during narrative).
     */
    public static NarrativeException executionError(
            String message, String storyName, String sceneName, String command) {
        return new Builder()
                .category(ErrorCategory.EXECUTION)
                .errorCode("EXEC_FAILED")
                .message(message)
                .storyName(storyName)
                .sceneName(sceneName)
                .originalCommand(command)
                .build();
    }

    /**
     * Creates an execution error with cause.
     */
    public static NarrativeException executionError(
            String message, String storyName, String sceneName, String command, Throwable cause) {
        return new Builder()
                .category(ErrorCategory.EXECUTION)
                .errorCode("EXEC_FAILED")
                .message(message)
                .storyName(storyName)
                .sceneName(sceneName)
                .originalCommand(command)
                .cause(cause)
                .build();
    }

    /**
     * Creates a state error (invalid state transition).
     */
    public static NarrativeException stateError(String message, String currentState, String attemptedAction) {
        return new Builder()
                .category(ErrorCategory.STATE)
                .errorCode("STATE_INVALID")
                .message(message + " (current: " + currentState + ", attempted: " + attemptedAction + ")")
                .build();
    }

    /**
     * Creates a security error (blocked command, etc.).
     */
    public static NarrativeException securityError(String message, String command, String storyName, String sceneName) {
        return new Builder()
                .category(ErrorCategory.SECURITY)
                .errorCode("SECURITY_BLOCKED")
                .message(message)
                .storyName(storyName)
                .sceneName(sceneName)
                .originalCommand(command)
                .suggestion("Use only whitelisted commands")
                .build();
    }

    /**
     * Creates a resource not found error.
     */
    public static NarrativeException resourceNotFound(
            String resourceType, String resourceName, String storyName, String sceneName) {
        return new Builder()
                .category(ErrorCategory.RESOURCE)
                .errorCode("RESOURCE_NOT_FOUND")
                .message("The " + resourceType + " '" + resourceName + "' was not found")
                .storyName(storyName)
                .sceneName(sceneName)
                .suggestion("Check that the " + resourceType + " exists and the name is spelled correctly")
                .build();
    }

    /**
     * Creates an internal error (unexpected condition).
     */
    public static NarrativeException internalError(String message, Throwable cause) {
        return new Builder()
                .category(ErrorCategory.INTERNAL)
                .errorCode("INTERNAL_ERROR")
                .message(message)
                .cause(cause)
                .build();
    }

    // Getters

    public ErrorCategory getCategory() {
        return category;
    }

    public String getErrorCode() {
        return errorCode;
    }

    @Nullable
    public String getStoryName() {
        return storyName;
    }

    @Nullable
    public String getSceneName() {
        return sceneName;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    @Nullable
    public String getOriginalCommand() {
        return originalCommand;
    }

    @Nullable
    public String getSuggestion() {
        return suggestion;
    }

    /**
     * Returns a formatted location string (e.g., "story/scene:42").
     */
    public String getLocation() {
        StringBuilder sb = new StringBuilder();
        if (storyName != null) {
            sb.append(storyName);
            if (sceneName != null) {
                sb.append("/").append(sceneName);
            }
        }
        if (lineNumber > 0) {
            sb.append(":").append(lineNumber);
        }
        return sb.length() > 0 ? sb.toString() : "<unknown>";
    }

    /**
     * Returns a user-friendly error message.
     */
    public String getUserFriendlyMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(category.getDisplayName()).append("] ");
        sb.append(getMessage());

        if (storyName != null || lineNumber > 0) {
            sb.append("\n  Location: ").append(getLocation());
        }

        if (originalCommand != null) {
            sb.append("\n  Command: ").append(originalCommand);
        }

        if (suggestion != null) {
            sb.append("\n  Suggestion: ").append(suggestion);
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return getUserFriendlyMessage();
    }

    /**
     * Builder for NarrativeException.
     */
    public static class Builder {
        private ErrorCategory category = ErrorCategory.INTERNAL;
        private String errorCode = "UNKNOWN";
        private String message = "An unknown error occurred";
        private String storyName;
        private String sceneName;
        private int lineNumber = -1;
        private String originalCommand;
        private String suggestion;
        private Throwable cause;

        public Builder category(ErrorCategory category) {
            this.category = category;
            return this;
        }

        public Builder errorCode(String errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder storyName(String storyName) {
            this.storyName = storyName;
            return this;
        }

        public Builder sceneName(String sceneName) {
            this.sceneName = sceneName;
            return this;
        }

        public Builder lineNumber(int lineNumber) {
            this.lineNumber = lineNumber;
            return this;
        }

        public Builder originalCommand(String originalCommand) {
            this.originalCommand = originalCommand;
            return this;
        }

        public Builder suggestion(String suggestion) {
            this.suggestion = suggestion;
            return this;
        }

        public Builder cause(Throwable cause) {
            this.cause = cause;
            return this;
        }

        public NarrativeException build() {
            return new NarrativeException(this);
        }
    }
}
