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

import org.jetbrains.annotations.Nullable;

/**
 * Represents a validation error with full context information.
 *
 * Contains the four components needed for friendly error messages:
 * - WHAT: The error code and description
 * - WHERE: Story name, scene name, line number
 * - WHY: The reason/cause explanation
 * - FIX: Suggestion for how to fix the error
 */
public class ValidationError {

    /**
     * Error codes for different validation failure types.
     */
    public enum ErrorCode {
        UNKNOWN_TAG("Unknown tag"),
        MISSING_ARGUMENT("Missing required argument"),
        INVALID_ARGUMENT_TYPE("Invalid argument type"),
        INVALID_ARGUMENT_VALUE("Invalid argument value"),
        SECURITY_VIOLATION("Security violation"),
        RESOURCE_NOT_FOUND("Resource not found");

        private final String description;

        ErrorCode(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    private final ErrorCode code;
    private final String tagName;
    private final String originalCommand;
    private final String storyName;
    private final String sceneName;
    private final int lineNumber;
    private final String reason;
    @Nullable
    private final String suggestion;
    @Nullable
    private final String expectedType;
    @Nullable
    private final String actualValue;
    @Nullable
    private final String argumentName;

    private ValidationError(Builder builder) {
        this.code = builder.code;
        this.tagName = builder.tagName;
        this.originalCommand = builder.originalCommand;
        this.storyName = builder.storyName;
        this.sceneName = builder.sceneName;
        this.lineNumber = builder.lineNumber;
        this.reason = builder.reason;
        this.suggestion = builder.suggestion;
        this.expectedType = builder.expectedType;
        this.actualValue = builder.actualValue;
        this.argumentName = builder.argumentName;
    }

    // Factory methods for common error types

    /**
     * Creates an error for an unknown/unrecognized tag.
     */
    public static ValidationError unknownTag(String tagName, String originalCommand,
                                              String storyName, String sceneName,
                                              int lineNumber, @Nullable String suggestion) {
        return new Builder()
            .code(ErrorCode.UNKNOWN_TAG)
            .tagName(tagName)
            .originalCommand(originalCommand)
            .storyName(storyName)
            .sceneName(sceneName)
            .lineNumber(lineNumber)
            .reason("The tag '" + tagName + "' is not a recognized NarrativeCraft tag")
            .suggestion(suggestion != null ? "Did you mean '" + suggestion + "'?" : null)
            .build();
    }

    /**
     * Creates an error for a missing required argument.
     */
    public static ValidationError missingArgument(String tagName, String originalCommand,
                                                   String storyName, String sceneName,
                                                   int lineNumber, String argumentName,
                                                   String expectedType) {
        return new Builder()
            .code(ErrorCode.MISSING_ARGUMENT)
            .tagName(tagName)
            .originalCommand(originalCommand)
            .storyName(storyName)
            .sceneName(sceneName)
            .lineNumber(lineNumber)
            .argumentName(argumentName)
            .expectedType(expectedType)
            .reason("The tag '" + tagName + "' requires the argument '" + argumentName + "'")
            .suggestion("Add the '" + argumentName + "' argument (type: " + expectedType + ")")
            .build();
    }

    /**
     * Creates an error for an invalid argument type.
     */
    public static ValidationError invalidArgumentType(String tagName, String originalCommand,
                                                       String storyName, String sceneName,
                                                       int lineNumber, String argumentName,
                                                       String expectedType, String actualValue) {
        return new Builder()
            .code(ErrorCode.INVALID_ARGUMENT_TYPE)
            .tagName(tagName)
            .originalCommand(originalCommand)
            .storyName(storyName)
            .sceneName(sceneName)
            .lineNumber(lineNumber)
            .argumentName(argumentName)
            .expectedType(expectedType)
            .actualValue(actualValue)
            .reason("Expected " + expectedType + " but got '" + actualValue + "'")
            .suggestion("Change '" + actualValue + "' to a valid " + expectedType)
            .build();
    }

    /**
     * Creates an error for an invalid argument value (correct type, wrong value).
     */
    public static ValidationError invalidArgumentValue(String tagName, String originalCommand,
                                                        String storyName, String sceneName,
                                                        int lineNumber, String argumentName,
                                                        String actualValue, String constraint) {
        return new Builder()
            .code(ErrorCode.INVALID_ARGUMENT_VALUE)
            .tagName(tagName)
            .originalCommand(originalCommand)
            .storyName(storyName)
            .sceneName(sceneName)
            .lineNumber(lineNumber)
            .argumentName(argumentName)
            .actualValue(actualValue)
            .reason("The value '" + actualValue + "' is invalid: " + constraint)
            .suggestion("Use a value that " + constraint)
            .build();
    }

    /**
     * Creates an error for a security violation (e.g., blocked command).
     */
    public static ValidationError securityViolation(String tagName, String originalCommand,
                                                     String storyName, String sceneName,
                                                     int lineNumber, String reason) {
        return new Builder()
            .code(ErrorCode.SECURITY_VIOLATION)
            .tagName(tagName)
            .originalCommand(originalCommand)
            .storyName(storyName)
            .sceneName(sceneName)
            .lineNumber(lineNumber)
            .reason(reason)
            .suggestion("Use only whitelisted commands")
            .build();
    }

    /**
     * Creates an error for a resource not found (e.g., missing scene, animation).
     */
    public static ValidationError resourceNotFound(String tagName, String originalCommand,
                                                    String storyName, String sceneName,
                                                    int lineNumber, String resourceType,
                                                    String resourceName) {
        return new Builder()
            .code(ErrorCode.RESOURCE_NOT_FOUND)
            .tagName(tagName)
            .originalCommand(originalCommand)
            .storyName(storyName)
            .sceneName(sceneName)
            .lineNumber(lineNumber)
            .reason("The " + resourceType + " '" + resourceName + "' was not found")
            .suggestion("Check that the " + resourceType + " exists and the name is spelled correctly")
            .build();
    }

    // Getters

    public ErrorCode getCode() {
        return code;
    }

    public String getTagName() {
        return tagName;
    }

    public String getOriginalCommand() {
        return originalCommand;
    }

    public String getStoryName() {
        return storyName;
    }

    public String getSceneName() {
        return sceneName;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getReason() {
        return reason;
    }

    @Nullable
    public String getSuggestion() {
        return suggestion;
    }

    @Nullable
    public String getExpectedType() {
        return expectedType;
    }

    @Nullable
    public String getActualValue() {
        return actualValue;
    }

    @Nullable
    public String getArgumentName() {
        return argumentName;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(code.getDescription()).append("] ");
        sb.append("in ").append(storyName).append("/").append(sceneName);
        sb.append(" at line ").append(lineNumber);
        sb.append(": ").append(reason);
        if (suggestion != null) {
            sb.append(" - ").append(suggestion);
        }
        return sb.toString();
    }

    /**
     * Builder for ValidationError.
     */
    public static class Builder {
        private ErrorCode code;
        private String tagName;
        private String originalCommand;
        private String storyName;
        private String sceneName;
        private int lineNumber;
        private String reason;
        private String suggestion;
        private String expectedType;
        private String actualValue;
        private String argumentName;

        public Builder code(ErrorCode code) {
            this.code = code;
            return this;
        }

        public Builder tagName(String tagName) {
            this.tagName = tagName;
            return this;
        }

        public Builder originalCommand(String originalCommand) {
            this.originalCommand = originalCommand;
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

        public Builder reason(String reason) {
            this.reason = reason;
            return this;
        }

        public Builder suggestion(String suggestion) {
            this.suggestion = suggestion;
            return this;
        }

        public Builder expectedType(String expectedType) {
            this.expectedType = expectedType;
            return this;
        }

        public Builder actualValue(String actualValue) {
            this.actualValue = actualValue;
            return this;
        }

        public Builder argumentName(String argumentName) {
            this.argumentName = argumentName;
            return this;
        }

        public ValidationError build() {
            return new ValidationError(this);
        }
    }
}
