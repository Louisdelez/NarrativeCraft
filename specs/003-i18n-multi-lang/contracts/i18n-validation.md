# Contract: i18n Validation

## Overview

This contract defines the validation rules for language files and the expected behavior of the `i18nCheck` Gradle task.

---

## Validation API Contract

### Input
- Directory: `common/src/main/resources/assets/narrativecraft/lang/`
- Source file: `en_us.json` (source of truth)
- Target files: `fr_fr.json`, `de_de.json`, `es_es.json`, `zh_cn.json`, `ru_ru.json`, `ar_sa.json`

### Output
- Exit code 0: All validations pass
- Exit code 1: One or more validations fail
- Console output: List of errors with file, key, and description

---

## Validation Rules

### V1: Key Presence Check

**Rule:** Every key in `en_us.json` must exist in all other locale files.

**Error format:**
```
ERROR: Missing key 'narrativecraft.screen.main.title' in fr_fr.json
```

**Severity:** FAIL (blocks build)

---

### V2: Placeholder Consistency

**Rule:** For each key, placeholder count and types must match between locales.

**Checks:**
- Count of `%s` must match
- Count of `%d` must match
- Positional placeholders (`%1$s`, `%2$d`) must have same indices

**Error format:**
```
ERROR: Placeholder mismatch for 'narrativecraft.error.tag_unknown.what'
  en_us.json: 1x %s
  de_de.json: 2x %s
```

**Severity:** FAIL (blocks build)

---

### V3: Valid JSON Syntax

**Rule:** All `.json` files must be valid JSON.

**Error format:**
```
ERROR: Invalid JSON syntax in ar_sa.json at line 45
  Expected ',' or '}' but found 'EOF'
```

**Severity:** FAIL (blocks build)

---

### V4: UTF-8 Encoding

**Rule:** All files must be valid UTF-8.

**Error format:**
```
ERROR: Invalid UTF-8 encoding in zh_cn.json at byte offset 1234
```

**Severity:** FAIL (blocks build)

---

### V5: No Empty Values

**Rule:** No translation value may be an empty string.

**Error format:**
```
ERROR: Empty value for key 'narrativecraft.button.cancel' in es_es.json
```

**Severity:** FAIL (blocks build)

---

### V6: Unused Keys (Optional)

**Rule:** Keys in locale files that don't exist in `en_us.json`.

**Error format:**
```
WARNING: Unused key 'narrativecraft.deprecated.old_button' in fr_fr.json
```

**Severity:** WARN (does not block build)

---

## Gradle Task Specification

### Task Name
`i18nCheck`

### Task Group
`verification`

### Dependencies
None (can run standalone)

### Inputs
- `common/src/main/resources/assets/narrativecraft/lang/*.json`

### Outputs
- Console validation report
- Exit code

### Usage
```bash
# Run standalone
./gradlew :common:i18nCheck

# Run as part of build
./gradlew :common:check  # i18nCheck included
```

---

## CI Integration Contract

### Trigger
- On every PR to `main` or release branches
- On every push to feature branches

### Job
```yaml
- name: i18n Validation
  run: ./gradlew :common:i18nCheck
```

### Failure Behavior
- PR cannot be merged if `i18nCheck` fails
- Build status badge reflects i18n validation state

---

## Translation.message() Contract

### Existing API
```java
public static MutableComponent message(String path, Object... args)
```

### Behavior
1. Prepends `narrativecraft.` to `path`
2. Returns `Component.translatable(fullKey, args)`
3. If key missing: Minecraft shows raw key (e.g., `narrativecraft.missing.key`)

### Example Usage
```java
// Key: narrativecraft.screen.main.title
Translation.message("screen.main.title")

// Key: narrativecraft.error.tag_unknown.what with placeholder
Translation.message("error.tag_unknown.what", tagName)
```

---

## Fallback Behavior Contract

### Missing Key at Runtime
1. Minecraft displays raw translation key as text
2. Console warning logged (Minecraft's default behavior)
3. No crash or exception thrown

### Missing Locale File
1. Minecraft falls back to `en_us.json`
2. If `en_us.json` missing: raw keys displayed

### Unsupported Language
1. Player's game language not in supported list
2. Minecraft automatically falls back to `en_us`
