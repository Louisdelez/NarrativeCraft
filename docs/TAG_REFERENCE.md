# NarrativeCraft Tag Reference

**Version**: 1.1.0
**Last Updated**: 2026-01-09

Complete reference for all NarrativeCraft Ink tags.

## Table of Contents

- [animation](#animation)
- [border](#border)
- [camera](#camera)
- [command](#command)
- [cutscene](#cutscene)
- [dialog](#dialog)
- [emote](#emote)
- [fade](#fade)
- [gameplay](#gameplay)
- [interaction](#interaction)
- [kill](#kill)
- [on enter](#on-enter)
- [save](#save)
- [sfx / song](#sfx--song)
- [shake](#shake)
- [subscene](#subscene)
- [text](#text)
- [time](#time)
- [wait](#wait)
- [weather](#weather)

---

## animation

Plays character animations.

**Syntax:**
```ink
# animation start <animation_name> [loop=true/false] [unique=true/false] [block=true/false]
# animation stop <animation_name>
```

**Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| action | `start`/`stop` | Yes | Start or stop animation |
| animation_name | string | Yes | Name of the animation |
| loop | boolean | No | Loop the animation (default: false) |
| unique | boolean | No | Only one instance can play (default: false) |
| block | boolean | No | Block story until complete (default: false) |

**Examples:**
```ink
# animation start walk_cycle loop=true
# animation start wave block=true
# animation stop walk_cycle
```

---

## border

Adds cinematic black bars or colored borders to the screen.

**Syntax:**
```ink
# border <up> <right> <down> <left> [color] [opacity]
# border clear
# border in <time> [easing]
# border out <time> [easing]
```

**Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| up | int | Yes* | Top border size in pixels |
| right | int | Yes* | Right border size in pixels |
| down | int | Yes* | Bottom border size in pixels |
| left | int | Yes* | Left border size in pixels |
| color | hex | No | 6-digit hex color (default: 000000) |
| opacity | float | No | 0.0-1.0 (default: 1.0) |
| time | float | No | Fade duration for in/out |
| easing | string | No | Easing function (default: SMOOTH) |

*Required when not using `clear`, `in`, or `out`.

**Easing Options:** `LINEAR`, `SMOOTH`, `EASE_IN`, `EASE_OUT`, `EASE_IN_OUT`

**Examples:**
```ink
# border 50 0 50 0
# border 30 30 30 30 000000 0.8
# border clear
# border out 1.5 SMOOTH
```

---

## camera

Sets the camera to a predefined angle and keyframe.

**Syntax:**
```ink
# camera <angle_name> <keyframe_name>
```

**Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| angle_name | string | Yes | Camera angle name from scene |
| keyframe_name | string | Yes | Keyframe within the angle |

**Example:**
```ink
# camera close_up hero_face
```

**Note:** Camera angles and keyframes must be defined in the scene editor.

---

## command

Executes a Minecraft command (whitelisted commands only).

**Syntax:**
```ink
# command "<minecraft_command>"
```

**Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| minecraft_command | string | Yes | The command to execute (in quotes) |

**Special Variables:**
- `@p` is replaced with the player's name

**Whitelisted Commands:**
- `effect`, `particle`, `playsound`, `stopsound`, `title`, `subtitle`
- `tp`, `teleport`, `summon`, `kill`
- `time`, `weather`
- `say`, `tell`, `tellraw`, `msg`
- `give`, `clear`, `gamemode`, `xp`, `experience`
- `scoreboard`, `tag`, `attribute`, `spawnpoint`
- `function`, `advancement`

**Blocked Commands:**
- `op`, `deop`, `ban`, `ban-ip`, `kick`, `stop`
- `fill`, `clone`, `setblock`
- `execute`, `data`, `datapack`, `debug`
- `whitelist`, `seed`, `forceload`, `worldborder`

**Examples:**
```ink
# command "effect give @p minecraft:speed 60 2"
# command "tp @p 100 64 200"
# command "playsound minecraft:entity.experience_orb.pickup player @p"
```

---

## cutscene

Starts a pre-recorded cutscene.

**Syntax:**
```ink
# cutscene start <cutscene_name>
```

**Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| cutscene_name | string | Yes | Name of the cutscene |

**Example:**
```ink
# cutscene start epic_entrance
```

**Note:** Cutscenes must be recorded in the scene editor.

---

## dialog

Modifies dialog display parameters.

**Syntax:**
```ink
# dialog <parameter> [value1] [value2]
```

**Parameters:**
| Parameter | Values | Description |
|-----------|--------|-------------|
| offset | x y (float) | Dialog box offset from default position |
| scale | value (float) | Text scale multiplier |
| padding | x y (float) | Internal padding |
| width | pixels (int) | Maximum text width |
| text_color | hex | Text color |
| background_color | hex | Background color |
| gap | value (float) | Line spacing |
| letter_spacing | value (float) | Character spacing |
| no_skip | - | Disable skipping |
| manual_skip | - | Require manual skip (default) |
| auto_skip | seconds (float) | Auto-advance after delay |
| bobbing | speed strength (float) | Text bobbing animation |

**Examples:**
```ink
# dialog scale 1.5
# dialog text_color FFFF00
# dialog auto_skip 3.0
# dialog bobbing 1.0 0.5
```

---

## emote

Plays EmoteCraft emotes (requires EmoteCraft mod).

**Syntax:**
```ink
# emote play <emote_name> <character_name> [--force]
# emote stop <emote_name> <character_name>
```

**Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| action | `play`/`stop` | Yes | Play or stop emote |
| emote_name | string/UUID | Yes | Emote name or UUID |
| character_name | string | Yes | Target character |
| --force | flag | No | Force play even if already playing |

**Example:**
```ink
# emote play wave Alice
# emote stop wave Alice
```

---

## fade

Creates a screen fade effect.

**Syntax:**
```ink
# fade <fadeIn> <stay> <fadeOut> [color]
```

**Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| fadeIn | float | Yes | Fade-in duration (seconds) |
| stay | float | Yes | Duration at full opacity |
| fadeOut | float | Yes | Fade-out duration (seconds) |
| color | hex | No | 6-digit hex color (default: 000000) |

**Examples:**
```ink
# fade 1.0 2.0 1.0
# fade 0.5 0 0.5 FF0000
# fade 2.0 0 0
```

---

## gameplay

Returns camera and controls to the player.

**Syntax:**
```ink
# gameplay
```

**No parameters.**

**Example:**
```ink
# gameplay
// Player regains control
```

---

## interaction

Summons or removes interaction entities.

**Syntax:**
```ink
# interaction summon <interaction_name>
# interaction remove <interaction_name>
```

**Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| action | `summon`/`remove` | Yes | Summon or remove interaction |
| interaction_name | string | Yes | Interaction name from scene |

**Example:**
```ink
# interaction summon mysterious_door
```

---

## kill

Removes a character from the scene.

**Syntax:**
```ink
# kill <character_name>
```

**Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| character_name | string | Yes | Character to remove |

**Example:**
```ink
# kill enemy_guard
```

---

## on enter

Marks the start of a scene. Required as the first tag.

**Syntax:**
```ink
# on enter
```

**No parameters.**

**Example:**
```ink
=== intro ===
# on enter
Welcome to the story!
```

---

## save

Saves the current story progress.

**Syntax:**
```ink
# save
```

**No parameters.**

**Example:**
```ink
# save
Narrator: Your progress has been saved.
```

---

## sfx / song

Plays or stops sound effects and music.

**Syntax:**
```ink
# sfx start <sound_id> [volume] [pitch] [loop=true/false] [fadein <time>]
# sfx stop <sound_id> [fadeout <time>]
# sfx stop all

# song start <sound_id> [volume] [pitch] [loop=true/false] [fadein <time>]
# song stop <sound_id> [fadeout <time>]
```

**Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| action | `start`/`stop` | Yes | Play or stop sound |
| sound_id | string | Yes | Minecraft sound ID (namespace:path) |
| volume | float | No | Volume 0.0-1.0 |
| pitch | float | No | Pitch adjustment |
| loop | boolean | No | Loop the sound |
| fadein/fadeout | float | No | Fade duration in seconds |

**Examples:**
```ink
# sfx start minecraft:entity.generic.explode 0.8
# sfx start minecraft:ambient.cave loop=true
# sfx stop all

# song start minecraft:music.game.creative loop=true fadein 2.0
# song stop minecraft:music.game.creative fadeout 1.0
```

---

## shake

Shakes the screen for impact effects.

**Syntax:**
```ink
# shake <strength> <decayRate> <speed>
```

**Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| strength | float | Yes | Shake intensity |
| decayRate | float | Yes | How fast shake diminishes |
| speed | float | Yes | Oscillation speed |

**Example:**
```ink
# shake 1.0 0.5 0.3
```

---

## subscene

Plays a subscene (mini-cutscene within a scene).

**Syntax:**
```ink
# subscene start <subscene_name> [loop=true/false] [unique=true/false] [block=true/false]
# subscene stop <subscene_name>
```

**Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| action | `start`/`stop` | Yes | Start or stop subscene |
| subscene_name | string | Yes | Subscene name |
| loop | boolean | No | Loop the subscene |
| unique | boolean | No | Only one instance allowed |
| block | boolean | No | Block story until complete |

**Example:**
```ink
# subscene start npc_pacing loop=true
# subscene stop npc_pacing
```

---

## text

Displays text overlays on screen.

**Syntax:**
```ink
# text <id> create "<text>" [color]
# text <id> remove
# text <id> edit "<new_text>"
# text <id> position <position>
# text <id> color <hex_color>
# text <id> scale <value>
# text <id> spacing <x> <y>
# text <id> width <pixels>
# text <id> opacity <value>
# text <id> fade <in> <stay> <out>
# text <id> fadein <duration>
# text <id> fadeout <duration>
# text <id> type [--block] [end_at] [speed]
# text <id> font <font_id>
# text <id> sound <sound_id>
```

**Positions:** `TOP_LEFT`, `TOP`, `TOP_RIGHT`, `MIDDLE_LEFT`, `MIDDLE`, `MIDDLE_RIGHT`, `BOTTOM_LEFT`, `BOTTOM`, `BOTTOM_RIGHT`

**Optional Flags:**
- `--no-remove`: Don't auto-remove after fade
- `--no-drop-shadow`: Disable text shadow

**Examples:**
```ink
# text title create "Chapter 1" FFFFFF
# text title position TOP
# text title fade 1.0 3.0 1.0
# text title type --block 100 0.05
# text title remove
```

---

## time

Changes the Minecraft day/night cycle.

**Syntax:**
```ink
# time set <value>
# time add <value>
# time set <from> to <to> for <duration> <unit> [easing]
```

**Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| value | string/int | `day`(1000), `noon`(6000), `night`(13000), `midnight`(18000), or tick number |
| duration | float | Transition duration |
| unit | string | `second(s)`, `minute(s)`, `hour(s)` |
| easing | string | Easing function |

**Examples:**
```ink
# time set night
# time set 13000
# time set day to night for 5 seconds SMOOTH
```

---

## wait

Pauses execution for a specified duration.

**Syntax:**
```ink
# wait <time> <unit>
```

**Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| time | float | Yes | Duration |
| unit | string | Yes | `second(s)`, `minute(s)`, `hour(s)` |

**Examples:**
```ink
# wait 2 seconds
# wait 0.5 seconds
# wait 1 minute
```

---

## weather

Changes the weather.

**Syntax:**
```ink
# weather <type> [--instant]
```

**Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| type | string | Yes | `clear`, `rain`, `thunder` |
| --instant | flag | No | Change immediately without transition |

**Examples:**
```ink
# weather rain
# weather clear --instant
# weather thunder
```

---

## Error Codes

When a tag fails validation, you may see these error codes:

| Code | Meaning |
|------|---------|
| `UNKNOWN_TAG` | Tag name not recognized |
| `MISSING_ARGUMENT` | Required argument not provided |
| `INVALID_ARGUMENT_TYPE` | Argument has wrong type |
| `INVALID_ARGUMENT_VALUE` | Argument value out of range |
| `SECURITY_VIOLATION` | Command blocked for security |
| `RESOURCE_NOT_FOUND` | Referenced resource doesn't exist |

---

## See Also

- [INK_GUIDE.md](INK_GUIDE.md) - Scripting tutorial
- [TROUBLESHOOTING.md](TROUBLESHOOTING.md) - Common issues and solutions
