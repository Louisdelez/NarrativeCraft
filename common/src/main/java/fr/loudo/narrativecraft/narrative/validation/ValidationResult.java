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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents the result of validating one or more tags.
 *
 * Contains a list of validation errors (if any) and provides
 * convenience methods for checking validity and accessing errors.
 */
public class ValidationResult {

    private final List<ValidationError> errors;
    private final int tagsValidated;

    private ValidationResult(List<ValidationError> errors, int tagsValidated) {
        this.errors = Collections.unmodifiableList(new ArrayList<>(errors));
        this.tagsValidated = tagsValidated;
    }

    /**
     * Creates a successful validation result with no errors.
     */
    public static ValidationResult success() {
        return new ValidationResult(Collections.emptyList(), 1);
    }

    /**
     * Creates a successful validation result for multiple tags.
     */
    public static ValidationResult success(int tagsValidated) {
        return new ValidationResult(Collections.emptyList(), tagsValidated);
    }

    /**
     * Creates a failed validation result with a single error.
     */
    public static ValidationResult failure(ValidationError error) {
        return new ValidationResult(Collections.singletonList(error), 1);
    }

    /**
     * Creates a failed validation result with multiple errors.
     */
    public static ValidationResult failure(List<ValidationError> errors) {
        return new ValidationResult(errors, errors.size());
    }

    /**
     * Creates a validation result from a list of errors.
     * If the list is empty, returns a success result.
     */
    public static ValidationResult fromErrors(List<ValidationError> errors, int tagsValidated) {
        return new ValidationResult(errors, tagsValidated);
    }

    /**
     * Merges multiple validation results into one.
     */
    public static ValidationResult merge(List<ValidationResult> results) {
        List<ValidationError> allErrors = new ArrayList<>();
        int totalTags = 0;

        for (ValidationResult result : results) {
            allErrors.addAll(result.getErrors());
            totalTags += result.getTagsValidated();
        }

        return new ValidationResult(allErrors, totalTags);
    }

    /**
     * Returns true if validation passed with no errors.
     */
    public boolean isValid() {
        return errors.isEmpty();
    }

    /**
     * Returns true if validation failed with at least one error.
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * Returns the list of validation errors (empty if valid).
     */
    public List<ValidationError> getErrors() {
        return errors;
    }

    /**
     * Returns the first error, or null if no errors.
     */
    public ValidationError getFirstError() {
        return errors.isEmpty() ? null : errors.get(0);
    }

    /**
     * Returns the number of errors.
     */
    public int getErrorCount() {
        return errors.size();
    }

    /**
     * Returns the number of tags that were validated.
     */
    public int getTagsValidated() {
        return tagsValidated;
    }

    /**
     * Returns errors filtered by error code.
     */
    public List<ValidationError> getErrorsByCode(ValidationError.ErrorCode code) {
        return errors.stream()
            .filter(e -> e.getCode() == code)
            .toList();
    }

    /**
     * Returns errors for a specific story.
     */
    public List<ValidationError> getErrorsForStory(String storyName) {
        return errors.stream()
            .filter(e -> storyName.equals(e.getStoryName()))
            .toList();
    }

    /**
     * Returns errors for a specific scene.
     */
    public List<ValidationError> getErrorsForScene(String storyName, String sceneName) {
        return errors.stream()
            .filter(e -> storyName.equals(e.getStoryName()) && sceneName.equals(e.getSceneName()))
            .toList();
    }

    @Override
    public String toString() {
        if (isValid()) {
            return "ValidationResult: VALID (" + tagsValidated + " tags validated)";
        } else {
            return "ValidationResult: INVALID (" + errors.size() + " errors in " + tagsValidated + " tags)";
        }
    }
}
