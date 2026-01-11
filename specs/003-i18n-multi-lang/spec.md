# Feature Specification: Internationalization (i18n) — Complete Multi-Language Support

**Feature ID:** 003-i18n-multi-lang
**Created:** 2026-01-11
**Status:** DONE
**Completed:** 2026-01-11

---

## Overview

### Problem Statement

NarrativeCraft currently has hardcoded English text throughout the user interface, error messages, command feedback, and system notifications. This limits the mod's accessibility to non-English speaking players and content creators who represent a significant portion of the Minecraft community.

### Proposed Solution

Implement complete internationalization (i18n) support using Minecraft's native language system (`assets/<modid>/lang/*.json`). The mod will ship with complete translations in 7 languages, automatically matching the player's game language setting.

### Target Users

- **Players**: Minecraft players who prefer non-English languages
- **Content Creators**: Story authors using NarrativeCraft in their native language
- **Translators**: Community members who wish to contribute or improve translations

### Success Criteria

1. 100% of user-facing text uses translatable components (zero hardcoded strings)
2. All 7 language files pass automated CI validation (missing keys, placeholder mismatches)
3. Arabic (RTL) language displays correctly without UI breakage
4. Users can seamlessly switch languages via Minecraft settings with immediate effect
5. Adding a new language requires only creating a JSON file and passing CI checks

---

## User Scenarios & Testing

### Primary User Flows

#### Scenario 1: French Player Experience
**Actor:** Player with Minecraft set to French (fr_fr)
**Flow:**
1. Player launches Minecraft with French language setting
2. Player loads a world with NarrativeCraft
3. All NarrativeCraft UI screens display in French
4. Error messages, tooltips, and command feedback appear in French
5. Player can fully use the mod without encountering English text

**Expected Outcome:** Complete French localization with no English fallback visible

#### Scenario 2: Validation Error Display (German)
**Actor:** Story author using German (de_de)
**Flow:**
1. Author writes an Ink script with a typo in a tag name
2. Author loads the story in-game
3. Validation error displays in German using WHAT/WHERE/WHY/FIX format
4. Suggestion ("Did you mean...?") also appears in German

**Expected Outcome:** Error messages fully localized including suggestions

#### Scenario 3: Arabic RTL Support
**Actor:** Player using Arabic (ar_sa)
**Flow:**
1. Player sets Minecraft to Arabic
2. Player opens NarrativeCraft main menu
3. Text displays correctly right-to-left
4. Button labels are readable
5. Dialog choices and error messages display without layout issues

**Expected Outcome:** Functional RTL interface without text overlap or misalignment

#### Scenario 4: Command Feedback (Chinese)
**Actor:** Player using Simplified Chinese (zh_cn)
**Flow:**
1. Player executes a NarrativeCraft command
2. Command feedback message appears in Chinese
3. Error/success indicators use Chinese text

**Expected Outcome:** All command responses fully localized

### Edge Cases

- **Missing translation key**: Falls back to en_us with console warning
- **Language not supported**: Falls back to en_us gracefully
- **Dynamic text with placeholders**: Placeholders (%s, %d) work correctly in all languages
- **Very long translations**: UI gracefully handles longer German/French text

---

## Functional Requirements

### FR-001: Translatable Components Only
All user-facing text must be rendered using Minecraft's translatable components (`Component.translatable()`). No hardcoded strings in UI elements, messages, or notifications.

**Acceptance Criteria:**
- Automated scan finds zero `Component.literal()` calls with user-facing text
- All screens use translation keys for labels, buttons, and tooltips

### FR-002: Standard Language File Format
Language files must follow Minecraft's JSON format in `assets/narrativecraft/lang/` with UTF-8 encoding.

**Acceptance Criteria:**
- Files located at `common/src/main/resources/assets/narrativecraft/lang/*.json`
- Valid JSON syntax for all 7 files
- UTF-8 encoding with proper character support (Chinese, Russian, Arabic)

### FR-003: Complete Key Coverage
Every translation key in en_us.json must exist in all 6 other locale files.

**Acceptance Criteria:**
- CI check validates key presence across all locales
- Build fails if any key is missing from any locale

### FR-004: Consistent Placeholders
Placeholders (%s, %d, etc.) must be identical across all translations for each key.

**Acceptance Criteria:**
- CI validates placeholder count and order match between locales
- Build fails on placeholder mismatch

### FR-005: Fallback Behavior
If a translation key is missing at runtime, the system falls back to en_us and logs a warning.

**Acceptance Criteria:**
- Missing key displays English text (not raw key)
- Warning logged with key name for developer awareness

### FR-006: Localized Error Messages
Validation errors (WHAT/WHERE/WHY/FIX format) must be fully translatable.

**Acceptance Criteria:**
- Each error code has 4 translation keys (error.code.what/where/why/fix)
- ErrorFormatter assembles Components from translated segments
- Typo suggestions are translatable

### FR-007: Keybind Translations
Key bindings and their category must be translated.

**Acceptance Criteria:**
- Keys follow pattern: `key.narrativecraft.<action>`
- Category key: `key.categories.narrativecraft`
- All keybinds display in player's language

### FR-008: RTL Language Support
Arabic (ar_sa) must display correctly with right-to-left text.

**Acceptance Criteria:**
- No string concatenation that breaks RTL order
- UI elements remain functional
- Text is readable and properly aligned

### FR-009: CI Language Validation
Continuous integration must validate language file integrity.

**Acceptance Criteria:**
- CI job runs on every PR
- Checks: missing keys, placeholder mismatches
- Build fails on validation errors
- Optional warning for unused keys

### FR-010: Translation Documentation
Documentation must explain how to contribute translations.

**Acceptance Criteria:**
- CONTRIBUTING.md section on translation workflow
- Key naming conventions documented
- Guidelines for translators (tone, placeholders, tag names)

---

## Supported Languages

| Code | Language | Direction | Priority |
|------|----------|-----------|----------|
| en_us | English (US) | LTR | Source of truth |
| fr_fr | French | LTR | High |
| de_de | German | LTR | High |
| es_es | Spanish | LTR | High |
| zh_cn | Chinese (Simplified) | LTR | High |
| ru_ru | Russian | LTR | Medium |
| ar_sa | Arabic (Saudi) | RTL | Medium |

---

## Key Naming Conventions

### Structure
```
narrativecraft.<category>.<subcategory>.<element>
```

### Categories

| Category | Pattern | Example |
|----------|---------|---------|
| Screens | `narrativecraft.screen.<id>.<element>` | `narrativecraft.screen.main.title` |
| Buttons | `narrativecraft.button.<action>` | `narrativecraft.button.play` |
| Tooltips | `narrativecraft.tooltip.<item>` | `narrativecraft.tooltip.story_save` |
| Commands | `narrativecraft.command.<cmd>.<msg>` | `narrativecraft.command.record.start` |
| Errors | `narrativecraft.error.<code>.<part>` | `narrativecraft.error.tag_unknown.what` |
| Compat | `narrativecraft.compat.<feature>` | `narrativecraft.compat.feature_disabled` |
| Keybinds | `key.narrativecraft.<action>` | `key.narrativecraft.open_menu` |

---

## Out of Scope

- Translation of `.ink` story files (author-created narrative content)
- Advanced pluralization (ICU MessageFormat)
- Audio localization (voiceover/dubbing)
- Right-to-left UI layout restructuring (text-only RTL support)

---

## Dependencies

- Minecraft's native language system (already available)
- Existing Translation utility class in codebase
- CI infrastructure for validation checks

---

## Assumptions

1. **Translation Quality**: Initial translations will be provided; community can improve via PRs
2. **Placeholder Order**: Placeholders maintain the same order across languages (no reordering needed)
3. **Text Length**: UI can handle 20-30% longer text (German/French vs English)
4. **Minecraft Language API**: Standard `Component.translatable()` provides fallback behavior
5. **Character Support**: Minecraft's default font supports all required character sets

---

## Risks

| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| RTL layout issues | Medium | Medium | Manual testing on ar_sa; use Components only |
| Long text overflow | Low | Low | UI review for each language |
| Missing translations at release | Low | Medium | CI enforcement; en_us fallback |
| Placeholder mismatch bugs | Medium | High | CI validation on every PR |

---

## Deliverables

1. **7 Language Files**: Complete translations for all supported languages
2. **Migration**: All hardcoded strings converted to translatable components
3. **CI Validation**: Automated checks for language file integrity
4. **Documentation**: Translation guide in CONTRIBUTING.md
5. **Smoke Test Results**: Manual verification on en_us, fr_fr, ar_sa minimum

---

## Definition of Done

- [ ] i18n CI check passes on all PRs
- [ ] All 7 language files complete and validated
- [ ] Zero user-facing hardcoded strings in codebase
- [ ] Error messages fully localized (WHAT/WHERE/WHY/FIX)
- [ ] Arabic (ar_sa) smoke test passes (menus, errors, choices)
- [ ] No regression on 5 build targets
- [ ] CONTRIBUTING.md updated with translation guide
