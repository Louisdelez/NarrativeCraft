# NarrativeCraft Ink Scripting Guide

**Version**: 1.1.0
**Last Updated**: 2026-01-09

## Introduction

NarrativeCraft uses [Ink](https://www.inklestudios.com/ink/) as its scripting language for creating interactive narratives in Minecraft. This guide covers everything you need to know to write stories for NarrativeCraft.

## Getting Started

### Basic Story Structure

Every NarrativeCraft story starts with a scene that contains the `# on enter` tag:

```ink
=== intro ===
# on enter

Welcome to your first NarrativeCraft story!

-> continue
```

### Dialogue

Dialogue in NarrativeCraft follows a simple format:

```ink
Character Name: This is what the character says.
```

For example:

```ink
Alice: Hello! Welcome to our village.
Bob: Nice to meet you, traveler.
```

### Choices

Players can make choices that affect the story:

```ink
Alice: Would you like some tea?

* [Accept the tea]
    Alice: Here you go! It's chamomile.
    -> tea_scene

* [Decline politely]
    Alice: Perhaps another time then.
    -> continue_conversation
```

## Tags Overview

NarrativeCraft extends Ink with special tags that control game elements. Tags are written with a `#` prefix:

```ink
# fade 1.0 2.0 1.0
# camera main_angle keyframe_1
Alice: The story begins...
```

### Tag Categories

1. **Visual Effects**: `fade`, `border`, `shake`, `text`
2. **Audio**: `sfx`, `song`
3. **Camera**: `camera`, `gameplay`
4. **Scene Control**: `cutscene`, `subscene`, `animation`
5. **Game State**: `save`, `wait`, `time`, `weather`
6. **Commands**: `command`, `kill`, `interaction`

## Visual Effect Tags

### fade

Creates a screen fade effect.

```ink
# fade <fadeIn> <stay> <fadeOut> [color]
```

**Parameters:**
- `fadeIn`: Duration in seconds for fade-in
- `stay`: Duration to stay at full opacity
- `fadeOut`: Duration for fade-out
- `color` (optional): Hex color without `0x` (default: `000000` for black)

**Examples:**
```ink
# fade 1.0 2.0 1.0
// Black fade: 1s fade in, 2s stay, 1s fade out

# fade 0.5 1.0 0.5 FF0000
// Red fade effect
```

### border

Adds cinematic black bars to the screen.

```ink
# border <up> <right> <down> <left> [color] [opacity]
```

**Parameters:**
- `up`, `right`, `down`, `left`: Border size in pixels
- `color` (optional): Hex color (default: `000000`)
- `opacity` (optional): 0.0-1.0 (default: 1.0)

**Special Actions:**
```ink
# border clear          // Remove border instantly
# border out 1.0        // Fade out over 1 second
# border in 1.0         // Fade in over 1 second
```

**Examples:**
```ink
# border 50 0 50 0
// Cinematic widescreen bars (top and bottom)

# border 30 30 30 30 000000 0.8
// Semi-transparent frame around the screen
```

### shake

Shakes the screen for impact effects.

```ink
# shake <strength> <decayRate> <speed>
```

**Parameters:**
- `strength`: Intensity of the shake
- `decayRate`: How quickly the shake diminishes
- `speed`: Speed of oscillation

**Example:**
```ink
# shake 1.0 0.5 0.3
// Medium shake effect for explosions
```

### text

Displays text overlays on screen.

```ink
# text <id> create "<text>" [color]
# text <id> remove
# text <id> position <position>
# text <id> fade <in> <stay> <out>
```

**Positions:** `TOP_LEFT`, `TOP`, `TOP_RIGHT`, `MIDDLE_LEFT`, `MIDDLE`, `MIDDLE_RIGHT`, `BOTTOM_LEFT`, `BOTTOM`, `BOTTOM_RIGHT`

**Example:**
```ink
# text title create "Chapter 1: The Beginning" FFFFFF
# text title position TOP
# text title fade 1.0 3.0 1.0
```

## Audio Tags

### sfx (Sound Effects)

Plays or stops sound effects.

```ink
# sfx start <sound_id> [volume] [pitch] [loop=true/false]
# sfx stop <sound_id>
# sfx stop all
```

**Parameters:**
- `sound_id`: Minecraft sound ID (e.g., `minecraft:entity.generic.explode`)
- `volume` (optional): 0.0-1.0
- `pitch` (optional): Pitch adjustment
- `loop` (optional): Whether to loop the sound

**Examples:**
```ink
# sfx start minecraft:entity.generic.explode 0.8 1.0
// Play explosion sound at 80% volume

# sfx start minecraft:ambient.cave loop=true
// Ambient looping sound
```

### song (Music)

Plays background music.

```ink
# song start <music_id> [volume] [loop=true/false] [fadein <time>]
# song stop <music_id> [fadeout <time>]
```

**Examples:**
```ink
# song start minecraft:music.creative loop=true fadein 2.0
// Start music with 2 second fade-in

# song stop minecraft:music.creative fadeout 1.0
// Stop with 1 second fade-out
```

## Camera Tags

### camera

Sets the camera to a predefined angle.

```ink
# camera <angle_name> <keyframe_name>
```

**Example:**
```ink
# camera close_up character_face
// Switch to a close-up camera angle
```

### gameplay

Returns camera control to the player.

```ink
# gameplay
```

## Scene Control Tags

### cutscene

Starts a pre-recorded cutscene.

```ink
# cutscene start <cutscene_name>
```

**Example:**
```ink
# cutscene start intro_cinematic
// Play the intro cutscene
```

### subscene

Plays a subscene (mini-cutscene within a scene).

```ink
# subscene start <subscene_name> [loop=true/false] [block=true/false]
# subscene stop <subscene_name>
```

**Example:**
```ink
# subscene start npc_walking loop=true
// NPC walks in a loop until stopped
```

### animation

Plays character animations.

```ink
# animation start <animation_name> [loop=true/false] [block=true/false]
# animation stop <animation_name>
```

## Game State Tags

### save

Saves the current story progress.

```ink
# save
```

### wait

Pauses execution for a specified duration.

```ink
# wait <time> <unit>
```

**Units:** `second(s)`, `minute(s)`, `hour(s)`

**Examples:**
```ink
# wait 2 seconds
# wait 1 minute
```

### time

Changes the Minecraft day/night cycle.

```ink
# time set <time>
# time add <amount>
```

**Time values:** `day`, `noon`, `night`, `midnight`, or tick number

**Examples:**
```ink
# time set night
# time set 13000
```

### weather

Changes the weather.

```ink
# weather <type> [--instant]
```

**Types:** `clear`, `rain`, `thunder`

**Examples:**
```ink
# weather rain
# weather clear --instant
```

## Command Tags

### command

Executes a Minecraft command.

```ink
# command "<minecraft_command>"
```

**Security Note:** Only whitelisted commands are allowed. See the [Security section](#security).

**Examples:**
```ink
# command "effect give @p minecraft:speed 30 1"
# command "tp @p 100 64 100"
```

### kill

Removes a character from the scene.

```ink
# kill <character_name>
```

### interaction

Summons or removes interaction entities.

```ink
# interaction summon <interaction_name>
# interaction remove <interaction_name>
```

## Variables

NarrativeCraft supports Ink variables with special syntax for tag integration:

```ink
VAR player_name = "Hero"
VAR gold = 100

# text greeting create "Welcome, %player_name%!"
```

Use `%variable_name%` to insert variable values into tags.

## Knots and Stitches

Organize your story with knots (scenes) and stitches (sub-sections):

```ink
=== village ===
= enter
# on enter
Welcome to the village.
-> market

= market
The market is bustling with activity.
* [Visit the blacksmith] -> village.blacksmith
* [Leave the village] -> forest

= blacksmith
The blacksmith greets you warmly.
-> village.market
```

## Best Practices

### 1. Always Start with `# on enter`

Every scene's first tag should be `# on enter`:

```ink
=== my_scene ===
# on enter
# fade 0.5 0 0
// Your content here
```

### 2. Save Progress at Key Points

```ink
# save
Alice: Your progress has been saved.
```

### 3. Use Meaningful Names

```ink
// Good
# camera kitchen_close_up main_view
# cutscene start morning_routine

// Avoid
# camera cam1 kf1
# cutscene start cs1
```

### 4. Test Incrementally

Test your story frequently as you write to catch errors early.

## Security

NarrativeCraft restricts which Minecraft commands can be executed for security:

### Allowed Commands
- `effect`, `particle`, `playsound`, `stopsound`
- `tp`, `teleport`, `summon`, `kill`
- `time`, `weather`
- `say`, `tell`, `tellraw`, `msg`
- `give`, `clear`, `gamemode`, `xp`, `experience`
- `scoreboard`, `tag`, `attribute`, `spawnpoint`
- `function`, `advancement`

### Blocked Commands
- `op`, `deop`, `ban`, `kick`, `stop`
- `fill`, `clone`, `setblock`
- `execute`, `data`, `datapack`
- `whitelist`, `seed`

## Error Messages

NarrativeCraft provides helpful error messages when something goes wrong:

```
[WHAT] Unknown tag
  Error: 'fde' tag is invalid
[WHERE] Story: intro / Scene: start / Line: 15
  Command: fde 1.0 2.0 1.0
[WHY] The tag 'fde' is not a recognized NarrativeCraft tag
[FIX] Did you mean 'fade'?
```

## Next Steps

- See [TAG_REFERENCE.md](TAG_REFERENCE.md) for complete tag documentation
- Check [TROUBLESHOOTING.md](TROUBLESHOOTING.md) for common issues
- Explore the example stories in `example-stories/`
