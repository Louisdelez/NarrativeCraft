# Contract: Ink Action API

**Version**: 1.0.0
**Date**: 2026-01-09

## Overview

The Ink Action API defines how narrative script tags are handled, validated, and
executed within NarrativeCraft. This contract ensures security through validation
and creator-friendly error messages.

## Core Interfaces

### InkAction

```java
package fr.loudo.narrativecraft.api.inkAction;

/**
 * Handler for an Ink script tag.
 * Implementations must be stateless and thread-safe.
 */
public interface InkAction {

    /**
     * Execute the action.
     *
     * @param context Execution context with player, story, parameters
     * @return Result indicating success/failure and any output
     */
    InkActionResult execute(InkActionContext context);

    /**
     * Get the tag name this action handles.
     * Must be lowercase, alphanumeric with underscores.
     *
     * @return Tag name (e.g., "dialog", "camera_lock")
     */
    String getTagName();

    /**
     * Get parameter definitions for validation.
     *
     * @return List of expected parameters
     */
    List<ParameterDefinition> getParameters();

    /**
     * Get documentation for this action.
     *
     * @return Documentation object
     */
    ActionDocumentation getDocumentation();

    /**
     * Check if this action requires specific permissions.
     *
     * @return Required permission, or null if none
     */
    @Nullable
    Permission getRequiredPermission();
}
```

### InkActionContext

```java
package fr.loudo.narrativecraft.api.inkAction;

/**
 * Execution context for an Ink action.
 */
public interface InkActionContext {

    /** Target player UUID */
    UUID getPlayerId();

    /** Active story */
    Story getStory();

    /** Active chapter (if any) */
    @Nullable
    Chapter getChapter();

    /** Active scene (if any) */
    @Nullable
    Scene getScene();

    /** Raw parameters from the tag */
    List<String> getRawParameters();

    /** Parsed parameters by name */
    Map<String, Object> getParsedParameters();

    /** Get specific parameter with type conversion */
    <T> T getParameter(String name, Class<T> type);

    /** Get parameter with default value */
    <T> T getParameter(String name, Class<T> type, T defaultValue);

    /** Source file and line for error reporting */
    SourceLocation getSourceLocation();

    /** State manager for state transitions */
    NarrativeStateManager getStateManager();

    /** Cleanup registry for handler registration */
    CleanupHandlerRegistry getCleanupRegistry();

    /** Logger scoped to this action execution */
    Logger getLogger();
}
```

### InkActionResult

```java
package fr.loudo.narrativecraft.api.inkAction;

/**
 * Result of action execution.
 */
public sealed interface InkActionResult {

    /** Action completed successfully */
    record Success(
        @Nullable String output,
        @Nullable Object data
    ) implements InkActionResult {}

    /** Action failed with error */
    record Failure(
        String errorCode,
        String message,
        @Nullable String suggestion,
        @Nullable Exception cause
    ) implements InkActionResult {}

    /** Action is waiting (async operation) */
    record Pending(
        String waitReason,
        Duration estimatedDuration,
        Runnable onCancel
    ) implements InkActionResult {}

    static InkActionResult success() {
        return new Success(null, null);
    }

    static InkActionResult success(String output) {
        return new Success(output, null);
    }

    static InkActionResult failure(String code, String message) {
        return new Failure(code, message, null, null);
    }

    static InkActionResult failure(String code, String message, String suggestion) {
        return new Failure(code, message, suggestion, null);
    }
}
```

## Parameter Definitions

```java
package fr.loudo.narrativecraft.api.inkAction;

/**
 * Definition of an action parameter.
 */
public record ParameterDefinition(
    String name,
    ParameterType type,
    boolean required,
    @Nullable Object defaultValue,
    @Nullable String validationPattern,
    String description
) {
    public enum ParameterType {
        STRING,
        INTEGER,
        FLOAT,
        BOOLEAN,
        ENUM,
        POSITION,  // x,y,z format
        COLOR,     // #RRGGBB or name
        DURATION,  // 1s, 500ms, 2.5s
        RESOURCE   // minecraft:resource/path
    }
}
```

## Validation Contract

### TagValidator

```java
package fr.loudo.narrativecraft.narrative.validation;

/**
 * Validates Ink tags before execution.
 */
public interface TagValidator {

    /**
     * Validate a tag.
     *
     * @param tagName Tag name from script
     * @param parameters Raw parameters
     * @param location Source location
     * @return Validation result
     */
    ValidationResult validate(
        String tagName,
        List<String> parameters,
        SourceLocation location
    );

    /**
     * Check if tag is whitelisted.
     */
    boolean isWhitelisted(String tagName);

    /**
     * Get all whitelisted tags.
     */
    Set<String> getWhitelistedTags();

    /**
     * Get suggestion for unknown tag (typo detection).
     */
    @Nullable
    String getSuggestion(String unknownTag);
}
```

### ValidationResult

```java
public record ValidationResult(
    boolean isValid,
    List<ValidationError> errors,
    List<ValidationWarning> warnings
) {
    public static ValidationResult valid() {
        return new ValidationResult(true, List.of(), List.of());
    }

    public static ValidationResult invalid(ValidationError... errors) {
        return new ValidationResult(false, List.of(errors), List.of());
    }
}

public record ValidationError(
    String code,
    String message,
    SourceLocation location,
    @Nullable String suggestion
) {}

public record ValidationWarning(
    String code,
    String message,
    SourceLocation location
) {}
```

## Error Codes

| Code | Description | Example Message |
|------|-------------|-----------------|
| `UNKNOWN_TAG` | Tag not in whitelist | "Unknown tag: #playsoundd" |
| `MISSING_PARAM` | Required parameter missing | "Tag #dialog requires 'text' parameter" |
| `INVALID_PARAM_TYPE` | Type mismatch | "Parameter 'duration' must be a number" |
| `INVALID_PARAM_VALUE` | Value out of range | "Volume must be between 0.0 and 1.0" |
| `PERMISSION_DENIED` | Insufficient permissions | "Tag #command requires operator permission" |
| `RESOURCE_NOT_FOUND` | Asset missing | "Sound file not found: custom:sounds/voice.ogg" |
| `SYNTAX_ERROR` | Malformed tag | "Expected closing parenthesis in #camera(..." |

## Error Message Format

All validation errors must follow this format:

```
[NarrativeCraft] ERROR in "<story_file>" at line <line_number>
  <error_description>
  ↳ <context_or_cause>
  ↳ Suggestion: <how_to_fix>
```

Example:
```
[NarrativeCraft] ERROR in "village_quest.ink" at line 42
  Unknown tag: #playsoundd
  ↳ Did you mean: #playsound ?
  ↳ Available sound tags: playsound, stopsound, fademusic
```

## Registration

### InkActionRegistry

```java
package fr.loudo.narrativecraft.api.inkAction;

/**
 * Registry for Ink action handlers.
 */
public interface InkActionRegistry {

    /**
     * Register an action handler.
     *
     * @param action Action to register
     * @throws IllegalArgumentException if tag name already registered
     */
    void register(InkAction action);

    /**
     * Get handler for a tag.
     *
     * @param tagName Tag name
     * @return Action handler or null if not found
     */
    @Nullable
    InkAction getAction(String tagName);

    /**
     * Get all registered actions.
     */
    Collection<InkAction> getAllActions();

    /**
     * Check if tag has a registered handler.
     */
    boolean hasAction(String tagName);
}
```

## Built-in Actions

### #dialog

```java
Tag: #dialog(text, [speaker], [emotion])

Parameters:
- text (String, required): Dialog text to display
- speaker (String, optional): Character name
- emotion (String, optional): Character emotion/animation

Example:
# dialog("Hello, traveler!", "Village Elder", "happy")
```

### #choice

```java
Tag: #choice(text, [target_knot])

Parameters:
- text (String, required): Choice text
- target_knot (String, optional): Ink knot to jump to

Example:
# choice("Accept the quest", "quest_accepted")
# choice("Decline", "quest_declined")
```

### #camera

```java
Tag: #camera(action, [params...])

Actions:
- lock: Lock camera to position
- unlock: Release camera control
- pan: Smooth camera movement
- shake: Camera shake effect

Example:
# camera("lock", 100, 65, 100, 45, 0)  // x, y, z, pitch, yaw
# camera("pan", 110, 65, 100, 2.0)     // target + duration
# camera("unlock")
```

### #sound

```java
Tag: #sound(action, sound_id, [volume], [pitch])

Actions:
- play: Play sound once
- loop: Loop sound
- stop: Stop sound

Example:
# sound("play", "minecraft:ambient.cave", 0.8, 1.0)
# sound("loop", "custom:music/village_theme")
# sound("stop", "custom:music/village_theme")
```

### #wait

```java
Tag: #wait(duration)

Parameters:
- duration (Duration, required): Time to wait

Example:
# wait("2s")
# wait("500ms")
```

### #command

```java
Tag: #command(minecraft_command)

Parameters:
- minecraft_command (String, required): Command to execute

Security:
- Only whitelisted commands allowed
- Requires operator context for restricted commands

Example:
# command("tp @p 100 65 100")  // Must be whitelisted
# command("give @p diamond 1") // Must be whitelisted
```

## Security Whitelist

Default whitelisted commands:
- `tp` - Teleport
- `give` - Give items
- `effect` - Apply effects
- `playsound` - Play sounds
- `particle` - Spawn particles
- `title` - Display titles
- `tellraw` - Send messages
- `weather` - Change weather
- `time` - Change time

Restricted (require operator):
- `gamemode`
- `difficulty`
- `gamerule`
- Any command not explicitly whitelisted

## Testing

```java
@Test
void action_shouldValidateRequiredParameters() {
    InkAction dialogAction = new DialogAction();
    ValidationResult result = validator.validate(
        "dialog",
        List.of(), // Missing required 'text'
        location
    );

    assertFalse(result.isValid());
    assertEquals("MISSING_PARAM", result.errors().get(0).code());
}

@Test
void action_shouldExecuteSuccessfully() {
    InkActionContext context = mockContext("Hello!");
    InkActionResult result = dialogAction.execute(context);

    assertInstanceOf(InkActionResult.Success.class, result);
}

@Test
void action_shouldRegisterCleanupHandler() {
    InkActionContext context = mockContext("Test");
    dialogAction.execute(context);

    verify(context.getCleanupRegistry()).register(any(), any());
}
```

## Documentation Contract

```java
public record ActionDocumentation(
    String tagName,
    String summary,
    String description,
    List<ParameterDocumentation> parameters,
    List<String> examples,
    @Nullable String seeAlso
) {}

public record ParameterDocumentation(
    String name,
    String type,
    boolean required,
    String description,
    @Nullable String defaultValue
) {}
```

All actions must provide documentation that can be auto-generated into TAG_REFERENCE.md.
