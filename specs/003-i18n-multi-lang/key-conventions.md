# i18n Key Naming Conventions

**Version:** 1.0
**Created:** 2026-01-11

---

## Key Structure

```
narrativecraft.<category>.<subcategory>.<element>
```

The `narrativecraft.` prefix is automatically added by `Translation.message()`.

---

## Categories

### Screen Keys

**Pattern:** `screen.<screen_id>.<element>`

| Key | Usage |
|-----|-------|
| `screen.<id>.title` | Screen title (passed to super constructor) |
| `screen.<id>.<field>` | Field labels, section headers |

**Examples:**
```json
"narrativecraft.screen.main.title": "Main Screen",
"narrativecraft.screen.dialog_options.title": "Dialog Options",
"narrativecraft.screen.dialog_options.padding_x": "Padding X",
"narrativecraft.screen.dialog_options.scale": "Scale"
```

### Button Keys

**Pattern:** `button.<action>`

**Examples:**
```json
"narrativecraft.button.done": "Done",
"narrativecraft.button.cancel": "Cancel",
"narrativecraft.button.save": "Save",
"narrativecraft.button.credits": "Credits",
"narrativecraft.button.dev_environment": "Dev Environment"
```

### Error Keys (WHAT/WHERE/WHY/FIX)

**Pattern:** `error.<code>.<part>`

| Part | Description |
|------|-------------|
| `.what` | What happened |
| `.where` | Location (file, line) |
| `.why` | Root cause |
| `.fix` | Suggested fix |

**Examples:**
```json
"narrativecraft.error.tag_unknown.what": "Unknown tag: %s",
"narrativecraft.error.tag_unknown.where": "File: %s, Line: %d",
"narrativecraft.error.tag_unknown.why": "Tag name not recognized by NarrativeCraft",
"narrativecraft.error.tag_unknown.fix": "Did you mean: %s?"
```

### Message Keys

**Pattern:** `message.<category>.<action>`

**Examples:**
```json
"narrativecraft.message.story.loaded": "Story loaded: %s",
"narrativecraft.message.recording.started": "Recording started",
"narrativecraft.message.playback.finished": "Playback finished"
```

### Tooltip Keys

**Pattern:** `tooltip.<element>`

**Examples:**
```json
"narrativecraft.tooltip.save_story": "Save your story progress",
"narrativecraft.tooltip.delete": "Delete this item"
```

### Keybind Keys

**Pattern:** `key.narrativecraft.<action>`

Note: Keybinds use standard Minecraft format without the mod prefix.

**Examples:**
```json
"key.narrativecraft.open_menu": "Open NarrativeCraft Menu",
"key.narrativecraft.toggle_recording": "Toggle Recording",
"key.categories.narrativecraft": "NarrativeCraft"
```

### Credits Keys

**Pattern:** `credits.<section>`

**Examples:**
```json
"narrativecraft.credits.tool_used": "Tools Used",
"narrativecraft.credits.special_thanks": "Special Thanks"
```

### Debug Keys

**Pattern:** `debug.<item>`

**Examples:**
```json
"narrativecraft.debug.fake_save": "Fake save (debug)"
```

### Entity Keys

**Pattern:** `entity.<type>`

**Examples:**
```json
"narrativecraft.entity.trigger": "Trigger"
```

---

## Placeholder Rules

### Types

| Format | Type | Example |
|--------|------|---------|
| `%s` | String | `"Loading: %s"` |
| `%d` | Integer | `"Found %d errors"` |
| `%1$s` | Positional string | `"%1$s at line %2$d"` |
| `%1$d` | Positional integer | `"Keyframe %1$d of %2$d"` |

### Guidelines

1. **Same count across locales**: If `en_us` has 2 placeholders, all locales must have 2
2. **Use positional for reordering**: Some languages need different word order
3. **Never concatenate**: Use single key with placeholders instead

**Wrong:**
```java
Component.literal("Error: " + message)  // BAD - breaks RTL
```

**Right:**
```java
Translation.message("error.generic", message)  // GOOD
// Key: "narrativecraft.error.generic": "Error: %s"
```

---

## Naming Rules

1. **Lowercase only**: `screen.main.title` not `screen.Main.Title`
2. **Underscores for multi-word**: `dialog_options` not `dialogOptions`
3. **Dots for hierarchy**: `screen.main.title` not `screen_main_title`
4. **Action verbs for buttons**: `button.save`, `button.delete`
5. **Nouns for labels**: `screen.dialog.scale`, `screen.dialog.width`
6. **Max 5 segments**: Avoid deeply nested keys

---

## Quick Reference

| Category | Pattern | Example |
|----------|---------|---------|
| Screen title | `screen.<id>.title` | `screen.main.title` |
| Screen field | `screen.<id>.<field>` | `screen.dialog_options.scale` |
| Button | `button.<action>` | `button.done` |
| Error | `error.<code>.<part>` | `error.tag_unknown.what` |
| Message | `message.<cat>.<action>` | `message.story.loaded` |
| Tooltip | `tooltip.<element>` | `tooltip.save_story` |
| Keybind | `key.narrativecraft.<action>` | `key.narrativecraft.open_menu` |
| Credits | `credits.<section>` | `credits.tool_used` |
| Debug | `debug.<item>` | `debug.fake_save` |
| Entity | `entity.<type>` | `entity.trigger` |
