# Data Model: 003-i18n-multi-lang

## Overview

The i18n feature uses Minecraft's native language system. No custom database or runtime data structures are needed. The "data model" consists of static JSON files loaded by Minecraft at startup.

---

## Entities

### E1: Language File

**Type:** Static JSON resource
**Location:** `assets/narrativecraft/lang/{locale}.json`

| Field | Type | Description |
|-------|------|-------------|
| (key) | String | Translation key (e.g., `narrativecraft.screen.main.title`) |
| (value) | String | Translated text with optional placeholders |

**Constraints:**
- Valid JSON syntax (UTF-8 encoded)
- All keys must be unique within file
- Placeholder format: `%s` (string), `%d` (integer), `%1$s` (positional)

**Example:**
```json
{
  "narrativecraft.screen.main.title": "Main Screen",
  "narrativecraft.button.play": "Play",
  "narrativecraft.error.tag_unknown.what": "Unknown tag: %s"
}
```

---

### E2: Translation Key

**Format:** `narrativecraft.<category>.<subcategory>.<element>`

| Segment | Description | Examples |
|---------|-------------|----------|
| `narrativecraft` | Mod namespace (auto-prefixed) | - |
| `<category>` | Functional grouping | `screen`, `button`, `error`, `tooltip` |
| `<subcategory>` | Specific feature/screen | `main`, `dialog_options`, `tag_unknown` |
| `<element>` | UI element or message part | `title`, `padding_x`, `what`, `why` |

**Validation Rules:**
- Lowercase only (a-z, 0-9, underscore, dot)
- No trailing/leading dots
- Maximum 100 characters

---

### E3: Placeholder

**Types:**

| Format | Type | Example Usage |
|--------|------|---------------|
| `%s` | String | `"Loading story: %s"` |
| `%d` | Integer | `"Found %d errors"` |
| `%1$s` | Positional string | `"%1$s at line %2$d"` |
| `%1$d` | Positional integer | `"Keyframe %1$d of %2$d"` |

**Constraints:**
- Placeholder count must match between source (en_us) and target locale
- Positional placeholders (`%1$s`) required if order differs between languages
- No unescaped `%` characters in translated text

---

## Relationships

```
┌─────────────────┐
│  Language File  │
│  (en_us.json)   │
└────────┬────────┘
         │ 1:N
         ▼
┌─────────────────┐
│ Translation Key │
│    (string)     │
└────────┬────────┘
         │ 1:N
         ▼
┌─────────────────┐
│   Placeholder   │
│  (%s, %d, ...)  │
└─────────────────┘
```

---

## File Structure

```
common/src/main/resources/assets/narrativecraft/lang/
├── en_us.json    # Source of truth (English US)
├── fr_fr.json    # French
├── de_de.json    # German
├── es_es.json    # Spanish
├── zh_cn.json    # Simplified Chinese
├── ru_ru.json    # Russian
└── ar_sa.json    # Arabic (RTL)
```

---

## Key Categories Schema

### Screen Keys
```
narrativecraft.screen.<screen_id>.title
narrativecraft.screen.<screen_id>.<element>
```

Examples:
- `narrativecraft.screen.main.title` → "Main Screen"
- `narrativecraft.screen.dialog_options.padding_x` → "Padding X"
- `narrativecraft.screen.keyframe.tick` → "Tick"

### Button Keys
```
narrativecraft.button.<action>
```

Examples:
- `narrativecraft.button.done` → "Done"
- `narrativecraft.button.credits` → "Credits"
- `narrativecraft.button.dev_env` → "Dev Environment"

### Error Keys (4-part format)
```
narrativecraft.error.<error_code>.what
narrativecraft.error.<error_code>.where
narrativecraft.error.<error_code>.why
narrativecraft.error.<error_code>.fix
```

Examples:
- `narrativecraft.error.tag_unknown.what` → "Unknown tag: %s"
- `narrativecraft.error.tag_unknown.where` → "File: %s, Line: %d"
- `narrativecraft.error.tag_unknown.why` → "Tag name not recognized"
- `narrativecraft.error.tag_unknown.fix` → "Did you mean: %s?"

### Keybind Keys
```
key.narrativecraft.<action>
key.categories.narrativecraft
```

Examples:
- `key.narrativecraft.open_menu` → "Open NarrativeCraft Menu"
- `key.categories.narrativecraft` → "NarrativeCraft"

### Message Keys
```
narrativecraft.message.<category>.<action>
```

Examples:
- `narrativecraft.message.story.loaded` → "Story loaded: %s"
- `narrativecraft.message.recording.started` → "Recording started"

### Tooltip Keys
```
narrativecraft.tooltip.<element>
```

Examples:
- `narrativecraft.tooltip.story_save` → "Save your story progress"
- `narrativecraft.tooltip.undo` → "Undo last action"

---

## State Transitions

N/A - Language files are static resources with no runtime state changes. Minecraft loads them at startup and caches in memory.

---

## Validation Rules Summary

| Rule | Enforcement |
|------|-------------|
| All en_us keys present in other locales | CI `i18nCheck` task |
| Placeholder count matches | CI `i18nCheck` task |
| Valid JSON syntax | CI `i18nCheck` task |
| UTF-8 encoding | File system / git attributes |
| No empty values | CI `i18nCheck` task |
| Key format valid | Code review (no automated check) |

---

## Estimated Counts

| Locale | Keys | Status |
|--------|------|--------|
| en_us.json | ~200 | To be created |
| fr_fr.json | ~200 | To be translated |
| de_de.json | ~200 | To be translated |
| es_es.json | ~200 | To be translated |
| zh_cn.json | ~200 | To be translated |
| ru_ru.json | ~200 | To be translated |
| ar_sa.json | ~200 | To be translated |

**Total:** ~1,400 translation strings across 7 files
