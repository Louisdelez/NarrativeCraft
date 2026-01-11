# NarrativeCraft i18n Rules

This document defines the internationalization rules for NarrativeCraft.

---

## Core Rules

### Rule 1: No Hardcoded User-Facing Text

All player-visible text must use `Translation.message()` or `Component.translatable()`.

**Wrong:**
```java
super(Component.literal("Main Screen"));
```

**Right:**
```java
super(Translation.message("screen.main.title"));
```

### Rule 2: No String Concatenation

Never concatenate translated strings. This breaks RTL languages like Arabic.

**Wrong:**
```java
Component.literal("Error: " + errorMessage)
```

**Right:**
```java
Translation.message("error.generic", errorMessage)
// Key: "narrativecraft.error.generic": "Error: %s"
```

### Rule 3: Use Placeholders

Dynamic values must use placeholder syntax (`%s`, `%d`).

**Wrong:**
```java
Translation.message("story.loaded") + " " + storyName
```

**Right:**
```java
Translation.message("story.loaded", storyName)
// Key: "narrativecraft.story.loaded": "Story loaded: %s"
```

### Rule 4: Symbols Stay Literal

Unicode symbols are universal and should NOT be translated:

```java
// Keep as literal - these are symbols, not text
Component.literal("✖")  // Close
Component.literal("◀")  // Left arrow
Component.literal("▶")  // Right arrow
Component.literal("✔")  // Checkmark
```

---

## Translation.message() API

```java
// Simple key
Translation.message("button.done")
// Returns: Component.translatable("narrativecraft.button.done")

// With placeholder
Translation.message("error.tag_unknown.what", tagName)
// Returns: Component.translatable("narrativecraft.error.tag_unknown.what", tagName)
```

The `narrativecraft.` prefix is automatically added.

---

## Key Naming Convention

```
narrativecraft.<category>.<subcategory>.<element>
```

| Category | Example |
|----------|---------|
| `screen.<id>.title` | `narrativecraft.screen.main.title` |
| `screen.<id>.<field>` | `narrativecraft.screen.dialog_options.scale` |
| `button.<action>` | `narrativecraft.button.done` |
| `error.<code>.<part>` | `narrativecraft.error.tag_unknown.what` |
| `message.<cat>.<action>` | `narrativecraft.message.story.loaded` |

See `specs/003-i18n-multi-lang/key-conventions.md` for full reference.

---

## Placeholder Types

| Format | Type | Example |
|--------|------|---------|
| `%s` | String | `"Loading: %s"` |
| `%d` | Integer | `"Found %d errors"` |
| `%1$s` | Positional | `"%1$s at line %2$d"` |

### Positional Placeholders

Use positional format (`%1$s`, `%2$d`) when:
- The phrase structure may differ between languages
- Translators need flexibility to reorder

**Example:**
```json
// English: "File example.ink has 5 errors"
"narrativecraft.error.file_errors": "File %1$s has %2$d errors"

// French: "Le fichier example.ink contient 5 erreurs"
"narrativecraft.error.file_errors": "Le fichier %1$s contient %2$d erreurs"

// German might reorder: "5 errors in file example.ink"
"narrativecraft.error.file_errors": "%2$d Fehler in Datei %1$s"
```

---

## Language Files

Location: `common/src/main/resources/assets/narrativecraft/lang/`

| File | Language |
|------|----------|
| `en_us.json` | English (source) |
| `fr_fr.json` | French |
| `de_de.json` | German |
| `es_es.json` | Spanish |
| `zh_cn.json` | Chinese (Simplified) |
| `ru_ru.json` | Russian |
| `ar_sa.json` | Arabic (RTL) |

---

## RTL Support (Arabic)

Minecraft handles RTL text direction automatically when using `Component.translatable()`.

**Requirements:**
1. Never concatenate strings
2. Use placeholders for dynamic content
3. Test UI layout with ar_sa locale

---

## CI Validation

The `i18nCheck` Gradle task validates:
1. All keys in `en_us.json` exist in other locales
2. Placeholder count matches between locales
3. Valid JSON syntax
4. No empty values

Run manually:
```bash
./gradlew :common:i18nCheck
```

Build fails if validation fails.

---

## Adding a New String

1. Add key to `en_us.json`
2. Add translations to all other locale files
3. Use `Translation.message("your.key")` in code
4. Run `./gradlew :common:i18nCheck` to validate
5. Test in-game with multiple languages

---

## Common Mistakes

| Mistake | Fix |
|---------|-----|
| Hardcoded string | Use `Translation.message()` |
| String concatenation | Use single key with `%s` placeholder |
| Missing placeholder | Ensure same `%s`/`%d` count in all locales |
| Empty translation | Add actual translated text |
| Wrong prefix | Let `Translation.message()` add prefix |
