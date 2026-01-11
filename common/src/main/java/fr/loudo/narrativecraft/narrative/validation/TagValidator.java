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

import java.util.*;
import java.util.regex.Pattern;

/**
 * Validates Ink tags at load time before execution.
 *
 * Checks for:
 * - Unknown/unrecognized tags
 * - Missing required arguments
 * - Invalid argument types
 * - Invalid argument values
 *
 * Provides suggestions for typos using Levenshtein distance.
 */
public class TagValidator {

    /** All valid NarrativeCraft tag names */
    private static final Set<String> VALID_TAGS = Set.of(
        "animation", "border", "camera", "time", "cutscene", "wait",
        "dialog", "command", "emote", "fade", "kill", "on enter",
        "save", "sfx", "song", "subscene", "weather", "shake",
        "interaction", "gameplay", "text"
    );

    /** Valid time units for wait/cooldown */
    private static final Set<String> VALID_TIME_UNITS = Set.of(
        "second", "seconds", "minute", "minutes", "hour", "hours"
    );

    /** Valid weather types */
    private static final Set<String> VALID_WEATHER_TYPES = Set.of(
        "clear", "rain", "thunder"
    );

    /** Valid sound actions */
    private static final Set<String> VALID_SOUND_ACTIONS = Set.of(
        "start", "stop"
    );

    /** Pattern for hex color validation */
    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("^[0-9A-Fa-f]{6}$");

    /** Typo suggester for tag names */
    private final TypoSuggester tagSuggester;

    public TagValidator() {
        this.tagSuggester = new TypoSuggester(VALID_TAGS);
    }

    /**
     * Validates a single tag command.
     *
     * @param command The full tag command (e.g., "fade 1.0 2.0 1.0")
     * @param storyName The name of the story containing this tag
     * @param sceneName The name of the scene containing this tag
     * @param lineNumber The line number where the tag appears
     * @return ValidationResult containing any errors found
     */
    public ValidationResult validateTag(String command, String storyName, String sceneName, int lineNumber) {
        if (command == null || command.trim().isEmpty()) {
            return ValidationResult.success();
        }

        String trimmedCommand = command.trim();
        String[] parts = parseCommand(trimmedCommand);
        if (parts.length == 0) {
            return ValidationResult.success();
        }

        String tagName = parts[0].toLowerCase();
        String[] args = Arrays.copyOfRange(parts, 1, parts.length);

        // Check for unknown tag
        if (!isValidTag(tagName)) {
            Optional<String> suggestion = tagSuggester.suggest(tagName);
            return ValidationResult.failure(
                ValidationError.unknownTag(tagName, trimmedCommand, storyName, sceneName, lineNumber,
                    suggestion.orElse(null))
            );
        }

        // Validate tag-specific arguments
        return validateTagArguments(tagName, args, trimmedCommand, storyName, sceneName, lineNumber);
    }

    /**
     * Validates multiple tags and collects all errors.
     *
     * @param commands Array of tag commands
     * @param storyName The story name
     * @param sceneName The scene name
     * @param lineNumbers Array of line numbers (must match commands length)
     * @return ValidationResult containing all errors found
     */
    public ValidationResult validateTags(String[] commands, String storyName, String sceneName, int[] lineNumbers) {
        List<ValidationError> errors = new ArrayList<>();

        for (int i = 0; i < commands.length; i++) {
            ValidationResult result = validateTag(commands[i], storyName, sceneName, lineNumbers[i]);
            errors.addAll(result.getErrors());
        }

        return ValidationResult.fromErrors(errors, commands.length);
    }

    /**
     * Parses a command string into parts, respecting quoted strings.
     */
    private String[] parseCommand(String command) {
        List<String> parts = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < command.length(); i++) {
            char c = command.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ' ' && !inQuotes) {
                if (current.length() > 0) {
                    parts.add(current.toString());
                    current = new StringBuilder();
                }
            } else {
                current.append(c);
            }
        }

        if (current.length() > 0) {
            parts.add(current.toString());
        }

        return parts.toArray(new String[0]);
    }

    /**
     * Checks if a tag name is valid.
     */
    private boolean isValidTag(String tagName) {
        // Handle "on enter" as special case
        if (tagName.equals("on")) {
            return true; // Will be validated with args
        }
        return VALID_TAGS.contains(tagName);
    }

    /**
     * Validates arguments for a specific tag.
     */
    private ValidationResult validateTagArguments(String tagName, String[] args, String command,
                                                   String storyName, String sceneName, int lineNumber) {
        return switch (tagName) {
            case "fade" -> validateFade(args, command, storyName, sceneName, lineNumber);
            case "wait" -> validateWait(args, command, storyName, sceneName, lineNumber);
            case "weather" -> validateWeather(args, command, storyName, sceneName, lineNumber);
            case "shake" -> validateShake(args, command, storyName, sceneName, lineNumber);
            case "sfx", "song" -> validateSound(tagName, args, command, storyName, sceneName, lineNumber);
            case "border" -> validateBorder(args, command, storyName, sceneName, lineNumber);
            case "save", "gameplay" -> ValidationResult.success(); // No args required
            case "on" -> validateOnEnter(args, command, storyName, sceneName, lineNumber);
            default -> ValidationResult.success(); // Other tags validated at runtime
        };
    }

    /**
     * Validates fade tag: fade fadeIn staySeconds fadeOut [color]
     */
    private ValidationResult validateFade(String[] args, String command,
                                          String storyName, String sceneName, int lineNumber) {
        if (args.length < 3) {
            return ValidationResult.failure(
                ValidationError.missingArgument("fade", command, storyName, sceneName, lineNumber,
                    args.length == 0 ? "fadeInSeconds" :
                    args.length == 1 ? "staySeconds" : "fadeOutSeconds",
                    "float")
            );
        }

        // Validate all three time values
        String[] argNames = {"fadeInSeconds", "staySeconds", "fadeOutSeconds"};
        for (int i = 0; i < 3; i++) {
            if (!isValidNumber(args[i])) {
                return ValidationResult.failure(
                    ValidationError.invalidArgumentType("fade", command, storyName, sceneName, lineNumber,
                        argNames[i], "number", args[i])
                );
            }
            double value = Double.parseDouble(args[i]);
            if (value < 0) {
                return ValidationResult.failure(
                    ValidationError.invalidArgumentValue("fade", command, storyName, sceneName, lineNumber,
                        argNames[i], args[i], "must be >= 0")
                );
            }
        }

        // Validate optional color
        if (args.length >= 4 && !isValidHexColor(args[3])) {
            return ValidationResult.failure(
                ValidationError.invalidArgumentType("fade", command, storyName, sceneName, lineNumber,
                    "color", "hex color (e.g., FF0000)", args[3])
            );
        }

        return ValidationResult.success();
    }

    /**
     * Validates wait tag: wait time unit
     */
    private ValidationResult validateWait(String[] args, String command,
                                          String storyName, String sceneName, int lineNumber) {
        if (args.length < 1) {
            return ValidationResult.failure(
                ValidationError.missingArgument("wait", command, storyName, sceneName, lineNumber,
                    "timeValue", "number")
            );
        }

        if (!isValidNumber(args[0])) {
            return ValidationResult.failure(
                ValidationError.invalidArgumentType("wait", command, storyName, sceneName, lineNumber,
                    "timeValue", "number", args[0])
            );
        }

        if (args.length < 2) {
            return ValidationResult.failure(
                ValidationError.missingArgument("wait", command, storyName, sceneName, lineNumber,
                    "timeUnit", "second(s)|minute(s)|hour(s)")
            );
        }

        if (!VALID_TIME_UNITS.contains(args[1].toLowerCase())) {
            return ValidationResult.failure(
                ValidationError.invalidArgumentType("wait", command, storyName, sceneName, lineNumber,
                    "timeUnit", "second(s)|minute(s)|hour(s)", args[1])
            );
        }

        return ValidationResult.success();
    }

    /**
     * Validates weather tag: weather type [--instant]
     */
    private ValidationResult validateWeather(String[] args, String command,
                                             String storyName, String sceneName, int lineNumber) {
        if (args.length < 1) {
            return ValidationResult.failure(
                ValidationError.missingArgument("weather", command, storyName, sceneName, lineNumber,
                    "weatherType", "clear|rain|thunder")
            );
        }

        if (!VALID_WEATHER_TYPES.contains(args[0].toLowerCase())) {
            return ValidationResult.failure(
                ValidationError.invalidArgumentType("weather", command, storyName, sceneName, lineNumber,
                    "weatherType", "clear|rain|thunder", args[0])
            );
        }

        return ValidationResult.success();
    }

    /**
     * Validates shake tag: shake strength decayRate speed
     */
    private ValidationResult validateShake(String[] args, String command,
                                           String storyName, String sceneName, int lineNumber) {
        String[] argNames = {"strength", "decayRate", "speed"};

        if (args.length < 3) {
            String missing = argNames[args.length];
            return ValidationResult.failure(
                ValidationError.missingArgument("shake", command, storyName, sceneName, lineNumber,
                    missing, "float")
            );
        }

        for (int i = 0; i < 3; i++) {
            if (!isValidNumber(args[i])) {
                return ValidationResult.failure(
                    ValidationError.invalidArgumentType("shake", command, storyName, sceneName, lineNumber,
                        argNames[i], "number", args[i])
                );
            }
        }

        return ValidationResult.success();
    }

    /**
     * Validates sound tags: sfx/song start/stop soundId [volume] [pitch]
     */
    private ValidationResult validateSound(String tagName, String[] args, String command,
                                           String storyName, String sceneName, int lineNumber) {
        if (args.length < 1) {
            return ValidationResult.failure(
                ValidationError.missingArgument(tagName, command, storyName, sceneName, lineNumber,
                    "action", "start|stop")
            );
        }

        // Handle "sfx stop all" special case
        if (args[0].equalsIgnoreCase("stop") && args.length >= 2 && args[1].equalsIgnoreCase("all")) {
            return ValidationResult.success();
        }

        if (!VALID_SOUND_ACTIONS.contains(args[0].toLowerCase())) {
            return ValidationResult.failure(
                ValidationError.invalidArgumentType(tagName, command, storyName, sceneName, lineNumber,
                    "action", "start|stop", args[0])
            );
        }

        if (args.length < 2) {
            return ValidationResult.failure(
                ValidationError.missingArgument(tagName, command, storyName, sceneName, lineNumber,
                    "soundId", "namespace:category.name")
            );
        }

        return ValidationResult.success();
    }

    /**
     * Validates border tag: border up right down left [color] [opacity] OR border clear/out
     */
    private ValidationResult validateBorder(String[] args, String command,
                                            String storyName, String sceneName, int lineNumber) {
        if (args.length < 1) {
            return ValidationResult.failure(
                ValidationError.missingArgument("border", command, storyName, sceneName, lineNumber,
                    "up|clear|out", "int or action")
            );
        }

        // Handle clear/out actions
        if (args[0].equalsIgnoreCase("clear") || args[0].equalsIgnoreCase("out") ||
            args[0].equalsIgnoreCase("in")) {
            return ValidationResult.success();
        }

        // Border with pixels: border up right down left [color] [opacity]
        if (args.length < 4) {
            String[] argNames = {"up", "right", "down", "left"};
            String missing = argNames[args.length];
            return ValidationResult.failure(
                ValidationError.missingArgument("border", command, storyName, sceneName, lineNumber,
                    missing, "int (pixels)")
            );
        }

        // Validate pixel values
        String[] argNames = {"up", "right", "down", "left"};
        for (int i = 0; i < 4; i++) {
            if (!isValidInteger(args[i])) {
                return ValidationResult.failure(
                    ValidationError.invalidArgumentType("border", command, storyName, sceneName, lineNumber,
                        argNames[i], "integer", args[i])
                );
            }
        }

        // Validate optional color
        if (args.length >= 5 && !isValidHexColor(args[4])) {
            return ValidationResult.failure(
                ValidationError.invalidArgumentType("border", command, storyName, sceneName, lineNumber,
                    "color", "hex color (e.g., 000000)", args[4])
            );
        }

        // Validate optional opacity
        if (args.length >= 6) {
            if (!isValidNumber(args[5])) {
                return ValidationResult.failure(
                    ValidationError.invalidArgumentType("border", command, storyName, sceneName, lineNumber,
                        "opacity", "float (0.0-1.0)", args[5])
                );
            }
            double opacity = Double.parseDouble(args[5]);
            if (opacity < 0.0 || opacity > 1.0) {
                return ValidationResult.failure(
                    ValidationError.invalidArgumentValue("border", command, storyName, sceneName, lineNumber,
                        "opacity", args[5], "must be between 0.0 and 1.0")
                );
            }
        }

        return ValidationResult.success();
    }

    /**
     * Validates "on enter" tag
     */
    private ValidationResult validateOnEnter(String[] args, String command,
                                             String storyName, String sceneName, int lineNumber) {
        if (args.length < 1 || !args[0].equalsIgnoreCase("enter")) {
            return ValidationResult.failure(
                ValidationError.unknownTag("on " + (args.length > 0 ? args[0] : ""), command,
                    storyName, sceneName, lineNumber, "on enter")
            );
        }
        return ValidationResult.success();
    }

    // Helper methods

    private boolean isValidNumber(String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isValidInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isValidHexColor(String value) {
        return HEX_COLOR_PATTERN.matcher(value).matches();
    }

    /**
     * Returns the set of all valid tag names.
     */
    public static Set<String> getValidTags() {
        return VALID_TAGS;
    }
}
