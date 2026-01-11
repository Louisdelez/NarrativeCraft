# Quickstart: i18n Implementation

## Prerequisites

- Java 21 installed
- Project builds successfully: `./gradlew :common:build`
- Understanding of Minecraft's translation system

---

## Step 1: Create English Source File

Create `common/src/main/resources/assets/narrativecraft/lang/en_us.json`:

```json
{
  "narrativecraft.screen.main.title": "Main Screen",
  "narrativecraft.button.done": "Done",
  "narrativecraft.error.tag_unknown.what": "Unknown tag: %s"
}
```

---

## Step 2: Migrate Hardcoded Strings

### Before (hardcoded)
```java
super(Component.literal("Main screen"));
```

### After (translatable)
```java
super(Translation.message("screen.main.title"));
```

---

## Step 3: Create Locale Files

Copy `en_us.json` structure to each locale:
- `fr_fr.json`
- `de_de.json`
- `es_es.json`
- `zh_cn.json`
- `ru_ru.json`
- `ar_sa.json`

Translate values (not keys).

---

## Step 4: Add CI Validation

In `common/build.gradle`:

```groovy
task i18nCheck {
    group = 'verification'
    description = 'Validates translation files'

    doLast {
        def langDir = file('src/main/resources/assets/narrativecraft/lang')
        def slurper = new groovy.json.JsonSlurper()

        def enFile = new File(langDir, 'en_us.json')
        if (!enFile.exists()) {
            throw new GradleException("en_us.json not found")
        }

        def enKeys = slurper.parse(enFile).keySet()
        def locales = ['fr_fr', 'de_de', 'es_es', 'zh_cn', 'ru_ru', 'ar_sa']
        def errors = []

        locales.each { locale ->
            def file = new File(langDir, "${locale}.json")
            if (!file.exists()) {
                errors << "Missing file: ${locale}.json"
                return
            }

            def keys = slurper.parse(file).keySet()
            def missing = enKeys - keys
            missing.each { key ->
                errors << "Missing key '${key}' in ${locale}.json"
            }
        }

        if (!errors.isEmpty()) {
            errors.each { println "ERROR: $it" }
            throw new GradleException("i18n validation failed with ${errors.size()} errors")
        }

        println "i18n validation passed: ${enKeys.size()} keys across ${locales.size() + 1} locales"
    }
}

check.dependsOn i18nCheck
```

---

## Step 5: Test Locally

```bash
# Validate translations
./gradlew :common:i18nCheck

# Build all targets
./gradlew build
```

---

## Quick Reference

### Translation.message() Usage

```java
// Simple key
Translation.message("button.done")
// → Component.translatable("narrativecraft.button.done")

// With placeholder
Translation.message("error.tag_unknown.what", "fade_in")
// → Component.translatable("narrativecraft.error.tag_unknown.what", "fade_in")
// → "Unknown tag: fade_in"
```

### Key Naming Pattern

```
narrativecraft.screen.<id>.title       # Screen titles
narrativecraft.screen.<id>.<element>   # Screen elements
narrativecraft.button.<action>         # Buttons
narrativecraft.error.<code>.what       # Error: what happened
narrativecraft.error.<code>.where      # Error: location
narrativecraft.error.<code>.why        # Error: reason
narrativecraft.error.<code>.fix        # Error: suggestion
narrativecraft.message.<category>      # Chat messages
narrativecraft.tooltip.<item>          # Tooltips
key.narrativecraft.<action>            # Keybinds
key.categories.narrativecraft          # Keybind category
```

### Placeholder Types

| Format | Type | Example |
|--------|------|---------|
| `%s` | String | `"Story: %s"` |
| `%d` | Integer | `"Found %d errors"` |
| `%1$s` | Positional | `"%1$s on line %2$d"` |

---

## Common Mistakes

### Wrong: String concatenation
```java
// BAD - breaks RTL
Component.literal("Error: " + message)
```

### Right: Use placeholders
```java
// GOOD
Translation.message("error.generic", message)
```

### Wrong: Translate symbols
```java
// BAD - symbols are universal
Translation.message("button.close_x")  // "✖"
```

### Right: Keep symbols literal
```java
// GOOD
Component.literal("✖")
```

---

## Validation Errors

| Error | Fix |
|-------|-----|
| Missing key in locale | Add key to the locale file |
| Placeholder mismatch | Ensure same %s/%d count |
| Invalid JSON | Fix syntax (missing comma, quote) |
| Empty value | Add translation text |
