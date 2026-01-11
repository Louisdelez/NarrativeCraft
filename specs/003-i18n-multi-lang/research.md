# Research: 003-i18n-multi-lang

## Summary

Research completed on 2026-01-11 to resolve technical unknowns for i18n implementation.

---

## R1: Existing i18n Infrastructure

### Decision
Use the existing `Translation.message()` utility class for all new translations.

### Rationale
- `Translation.java` already exists at `common/src/main/java/fr/loudo/narrativecraft/util/Translation.java`
- 74+ usages across 20+ files already use `Translation.message(path, args...)`
- Wraps `Component.translatable()` with automatic `narrativecraft.` prefix
- Consistent with Minecraft's native language system

### Alternatives Considered
- Direct `Component.translatable()` calls - Rejected: No namespace prefix consistency
- Custom i18n library - Rejected: Over-engineering, Minecraft's system sufficient

---

## R2: Current Translation Coverage

### Decision
~84 hardcoded strings need migration; commands and keybinds already i18n-ready.

### Findings by Category

| Category | Count | Status | Priority |
|----------|-------|--------|----------|
| Screen Titles | 19 | Hardcoded | High |
| Screen Labels/Fields | 23 | Hardcoded | High |
| Button Labels | 27 | Hardcoded | High |
| Error/Crash Messages | 4 | Hardcoded | Medium |
| Debug Messages | 2 | Hardcoded | Low |
| Credits Text | 1 | Hardcoded | Low |
| Symbol Buttons (✖, ◀, ▶) | 8+ | Keep literal | N/A |
| Commands | 50+ | Already i18n | Done |
| Keybinds | 6 | Already i18n | Done |

### Key Files Requiring Migration

**High Priority (Screen Titles):**
- `MainScreen.java` - "Main screen"
- `CharacterAdvancedScreen.java` - "Character Advanced Screen"
- `DialogCustomOptionsScreen.java` - "Character Custom Dialog Screen"
- `CameraAngleControllerScreen.java` - "Camera Angle Controller Screen"
- `CutsceneControllerScreen.java` - "Cutscene Controller Screen"
- `KeyframeTriggerScreen.java` - "Keyframe Trigger Screen"
- `StoryChoicesScreen.java` - "Choice screen"
- ... (12 more screen classes)

**High Priority (Labels):**
- `DialogCustomOptionsScreen.java` - 10 field labels (Padding X/Y, Scale, etc.)
- `KeyframeTriggerScreen.java` - Tick, Tags labels
- `KeyframeOptionScreen.java` - Up Down Value, Left Right Value, etc.
- `CutsceneChangeTimeSkipScreen.java` - "Number" label

**Medium Priority (Buttons):**
- `MainScreen.java` - "Dev Environment"
- `MainScreenOptionsScreen.java` - "Credits"
- `AbstractTextBoxScreen.java` - "Done"

### Rationale
Symbol buttons (✖, ◀, ▶, ✔) should remain `Component.literal()` as they are universal Unicode symbols, not translatable text.

---

## R3: Language File Location

### Decision
Place language files at `common/src/main/resources/assets/narrativecraft/lang/`

### Rationale
- Standard Minecraft resource location
- Shared via `common/` module for all 5 build targets
- Automatically loaded by Minecraft's language system
- UTF-8 encoding required for CJK/Cyrillic/Arabic characters

### Files to Create
```
common/src/main/resources/assets/narrativecraft/lang/
├── en_us.json  (source of truth)
├── fr_fr.json
├── de_de.json
├── es_es.json
├── zh_cn.json
├── ru_ru.json
└── ar_sa.json
```

---

## R4: Key Naming Convention

### Decision
Use hierarchical dot-notation: `narrativecraft.<category>.<subcategory>.<element>`

### Rationale
- Consistent with Minecraft modding conventions
- Enables grouping and easy maintenance
- Prefix already enforced by `Translation.message()`

### Categories

| Prefix | Usage | Example |
|--------|-------|---------|
| `screen.<id>.title` | Screen titles | `narrativecraft.screen.main.title` |
| `screen.<id>.<element>` | Screen elements | `narrativecraft.screen.dialog_options.padding_x` |
| `button.<action>` | Button labels | `narrativecraft.button.done` |
| `tooltip.<item>` | Hover tooltips | `narrativecraft.tooltip.save_story` |
| `error.<code>.<part>` | Error messages | `narrativecraft.error.tag_unknown.what` |
| `message.<category>` | Chat messages | `narrativecraft.message.story_loaded` |
| `credits.<section>` | Credits text | `narrativecraft.credits.tools_used` |

---

## R5: RTL Handling for Arabic

### Decision
Use Minecraft's native RTL support; avoid string concatenation.

### Rationale
- Minecraft's `Component.translatable()` handles RTL text direction
- No custom BiDi algorithm needed
- Main risk: string concatenation breaking RTL order

### Mitigation
- All multi-part messages must use placeholders (`%s`, `%d`)
- Never concatenate translated strings with `+`
- CI validation will check placeholder consistency

---

## R6: CI Validation Approach

### Decision
Create Gradle task `i18nCheck` using simple JSON comparison.

### Rationale
- No external dependencies needed
- JSON parsing built into Java/Gradle
- Fast execution (< 1 second)

### Validation Rules
1. All keys in `en_us.json` must exist in every other locale
2. Placeholder count/order must match between locales
3. Valid UTF-8 JSON encoding required
4. No empty string values allowed

### Implementation
```groovy
// buildSrc or common/build.gradle
task i18nCheck {
    doLast {
        def langDir = file('src/main/resources/assets/narrativecraft/lang')
        def enKeys = new JsonSlurper().parse(new File(langDir, 'en_us.json'))
        // Compare keys and placeholders across all locales
    }
}
```

---

## R7: Estimated Key Count

### Decision
Target: ~150-200 translation keys initially.

### Breakdown
| Source | Estimated Keys |
|--------|----------------|
| Existing `Translation.message()` calls | 74 |
| Screen titles to migrate | 19 |
| Labels/fields to migrate | 23 |
| Buttons to migrate | 15 |
| Error messages (WHAT/WHERE/WHY/FIX) | 40+ |
| Keybind names/categories | 10 |
| Tooltips | 20+ |
| **Total** | ~200 |

---

## Open Questions Resolved

| Question | Resolution |
|----------|------------|
| Where do lang files go? | `common/src/main/resources/assets/narrativecraft/lang/` |
| How to prefix keys? | `Translation.message()` auto-prefixes `narrativecraft.` |
| What about symbols (✖, ▶)? | Keep as `Component.literal()` - not translatable |
| RTL implementation? | Minecraft handles it; avoid concatenation |
| CI validation? | Gradle task comparing JSON keys/placeholders |

---

## Recommendations

1. **Phase 1**: Extract keys from existing `Translation.message()` calls to `en_us.json`
2. **Phase 2**: Migrate 84 hardcoded strings to use `Translation.message()`
3. **Phase 3**: Create 6 additional locale files with translations
4. **Phase 4**: Add `i18nCheck` Gradle task and CI integration
5. **Phase 5**: RTL testing on ar_sa
