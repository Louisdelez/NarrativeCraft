# Implementation Plan: 003-i18n-multi-lang

**Feature:** Internationalization (i18n) — Complete Multi-Language Support
**Branch:** `003-i18n-multi-lang`
**Created:** 2026-01-11

---

## Technical Context

| Aspect | Value |
|--------|-------|
| Existing i18n utility | `Translation.message()` in `util/Translation.java` |
| Current usage | 74+ calls across 20 files |
| Hardcoded strings | ~84 to migrate |
| Language file location | `common/src/main/resources/assets/narrativecraft/lang/` |
| Target languages | 7 (en_us, fr_fr, de_de, es_es, zh_cn, ru_ru, ar_sa) |
| Estimated keys | ~200 |

---

## Phase 1: Audit & Preparation

**Objective:** Identify all player-facing text and prepare i18n architecture.

### P001: Scan Code for Hardcoded Strings
- [ ] Search for `Component.literal(` with user-visible text
- [ ] Identify strings in screen classes, commands, error messages
- [ ] Document findings in `specs/003-i18n-multi-lang/audit-results.md`

**Files to scan:**
- `common/src/main/java/fr/loudo/narrativecraft/screens/**/*.java`
- `common/src/main/java/fr/loudo/narrativecraft/narrative/**/*.java`
- `common/src/main/java/fr/loudo/narrativecraft/commands/**/*.java`

### P002: Categorize Texts
- [ ] Create inventory spreadsheet/table:
  - Category: Screen / Button / Tooltip / Error / Command / Keybind
  - Current text (English)
  - File path and line number
  - Proposed translation key

### P003: Define Key Naming Conventions
- [ ] Document in `CONTRIBUTING.md` or spec
- [ ] Pattern: `narrativecraft.<category>.<subcategory>.<element>`
- [ ] Categories: screen, button, tooltip, error, message, compat

### P004: Validate Architecture
- [ ] Confirm `Translation.message()` meets all needs
- [ ] Verify Minecraft handles fallback automatically
- [ ] Test RTL support with sample Arabic text

**Deliverable:** Audit results document with categorized list of ~84 strings

---

## Phase 2: Extract Keys (Source of Truth)

**Objective:** Create complete English language file.

### P005: Create en_us.json
- [ ] Create `common/src/main/resources/assets/narrativecraft/lang/en_us.json`
- [ ] Add all existing `Translation.message()` keys from codebase
- [ ] Ensure valid JSON and UTF-8 encoding

### P006: Extract Existing Keys
- [ ] Grep all `Translation.message("..."` calls
- [ ] Extract key suffixes (path after `narrativecraft.`)
- [ ] Add corresponding entries to `en_us.json`

### P007: Add Placeholders
- [ ] Review each key for placeholder needs
- [ ] Use `%s` for strings, `%d` for integers
- [ ] Use positional `%1$s` where order may differ

### P008: Eliminate String Concatenation
- [ ] Search for `+ "..."` patterns in translations
- [ ] Replace with single keys using placeholders
- [ ] Example: `"Error: " + msg` → `"Error: %s"` with placeholder

### P009: Verify 100% Coverage
- [ ] Run verification: all `Translation.message()` keys in JSON
- [ ] No missing keys at runtime (test in-game)

**Deliverable:** Complete `en_us.json` with ~200 keys

---

## Phase 3: Multi-Language Translations

**Objective:** Create all 6 additional language files.

### P010: Create Language Files
- [ ] `fr_fr.json` — French
- [ ] `de_de.json` — German
- [ ] `es_es.json` — Spanish
- [ ] `zh_cn.json` — Simplified Chinese
- [ ] `ru_ru.json` — Russian
- [ ] `ar_sa.json` — Arabic (RTL)

### P011: Translate All Keys
- [ ] Translate each key value (not key names)
- [ ] Preserve placeholders exactly (`%s`, `%d`)
- [ ] Maintain consistent terminology per language

### P012: Verify Placeholder Consistency
- [ ] Script to compare placeholder count between locales
- [ ] Flag any mismatches for review

### P013: Validate Encoding
- [ ] All files UTF-8 without BOM
- [ ] Valid JSON syntax (lint all files)
- [ ] Chinese/Russian/Arabic characters render correctly

**Deliverable:** 7 complete language files

---

## Phase 4: RTL & Arabic Robustness

**Objective:** Ensure Arabic displays correctly without breaking UI.

### P014: Audit for String Concatenation
- [ ] Search for any `+` operators combining translated text
- [ ] Replace with single translatable keys
- [ ] Verify no RTL-breaking patterns

### P015: Test Main Screens in Arabic
- [ ] MainScreen
- [ ] StoryChoicesScreen
- [ ] DialogCustomOptionsScreen
- [ ] CameraAngleControllerScreen

### P016: Test Error/Warning Messages in RTL
- [ ] Ink validation errors
- [ ] Command feedback
- [ ] Compat warnings

### P017: Adjust Layouts if Needed
- [ ] Check for text overflow
- [ ] Verify button alignment
- [ ] Document any Minecraft-level limitations

**Deliverable:** RTL validation report in `docs/SMOKE_RESULTS_i18n.md`

---

## Phase 5: Localized Errors & Validation Messages

**Objective:** Internationalize all error messages including WHAT/WHERE/WHY/FIX format.

### P018: Migrate Ink Errors
- [ ] `TagValidator` error messages
- [ ] `InkValidationService` messages
- [ ] All `NarrativeException` strings

### P019: Update ErrorFormatter
- [ ] Ensure `ErrorFormatter` uses `Translation.message()`
- [ ] 4-part keys: `.what`, `.where`, `.why`, `.fix`

### P020: Translate Validation Messages
- [ ] WHAT: What went wrong
- [ ] WHERE: File/line location
- [ ] WHY: Root cause explanation
- [ ] FIX: Suggested solution / "Did you mean?"

### P021: Translate Compat Warnings
- [ ] `CompatLogger` messages
- [ ] Version-specific feature warnings
- [ ] Fallback behavior notifications

**Deliverable:** Zero untranslated error messages

---

## Phase 6: CI & Automated Checks

**Objective:** Prevent i18n regressions.

### P022: Implement i18nCheck Gradle Task
- [ ] Create task in `common/build.gradle`
- [ ] Parse JSON files
- [ ] Compare keys between locales

### P023: Validation Rules
- [ ] Missing keys → FAIL
- [ ] Placeholder mismatch → FAIL
- [ ] Invalid JSON → FAIL
- [ ] Unused keys → WARN

### P024: Integrate with CI
- [ ] Add to `.github/workflows/ci.yml`
- [ ] Run on every PR

### P025: Enforce Build Failure
- [ ] `check.dependsOn i18nCheck`
- [ ] PR cannot merge if i18n fails

**Deliverable:** CI job that blocks PRs on i18n errors

---

## Phase 7: Documentation

**Objective:** Enable community translation contributions.

### P026: Translation Guide in CONTRIBUTING.md
- [ ] How to add a new language
- [ ] How to update existing translations
- [ ] PR requirements for translation changes

### P027: Style & Placeholder Rules
- [ ] Tone guidelines (formal vs informal)
- [ ] Placeholder documentation
- [ ] Special characters handling

### P028: Architecture Documentation
- [ ] Add i18n section to `MULTI_VERSION_ARCHITECTURE.md`
- [ ] Explain language file location
- [ ] Document `Translation.message()` API

### P029: New Language Checklist
- [ ] Create `docs/ADDING_NEW_LANGUAGE.md`
- [ ] Step-by-step instructions
- [ ] CI validation requirements

**Deliverable:** Complete translation documentation

---

## Phase 8: Smoke Tests

**Objective:** Verify in-game functionality.

### P030: Test Languages In-Game
- [ ] `en_us` — English baseline
- [ ] `fr_fr` — French (LTR European)
- [ ] `ar_sa` — Arabic (RTL)

### P031: Test Across Loaders
- [ ] Fabric 1.21.11
- [ ] NeoForge 1.21.11
- [ ] At least one 1.20.6 target

### P032: Document Results
- [ ] Create `docs/SMOKE_RESULTS_i18n.md`
- [ ] Screenshot evidence (optional)
- [ ] Known issues list

**Deliverable:** Signed-off smoke test results

---

## Phase 9: Closure

**Objective:** Finalize feature for release.

### P033: Final Hardcoded String Check
- [ ] Grep for remaining `Component.literal(` with text
- [ ] Exclude symbols (✖, ▶, etc.)
- [ ] Zero user-facing hardcoded strings

### P034: Verify All Languages Load
- [ ] No console errors on startup
- [ ] Language switcher works correctly
- [ ] Fallback to en_us works

### P035: Mark Feature Complete
- [ ] Update spec status to "Complete"
- [ ] Update CHANGELOG.md for next release
- [ ] Merge to main branch

---

## Definition of Done

- [ ] 7 language files complete and validated
- [ ] CI i18n check passes
- [ ] All UI, errors, commands translated
- [ ] RTL (Arabic) functional
- [ ] No regression on 5 build targets
- [ ] Documentation updated
- [ ] Smoke tests passed

---

## Task Summary

| Phase | Tasks | Estimated Effort |
|-------|-------|------------------|
| 1. Audit | P001-P004 | Research |
| 2. Extract Keys | P005-P009 | Medium |
| 3. Translations | P010-P013 | High |
| 4. RTL | P014-P017 | Medium |
| 5. Errors | P018-P021 | Medium |
| 6. CI | P022-P025 | Medium |
| 7. Docs | P026-P029 | Low |
| 8. Smoke Test | P030-P032 | Low |
| 9. Closure | P033-P035 | Low |

**Total Tasks:** 35

---

## Files to Create/Modify

### New Files
- `common/src/main/resources/assets/narrativecraft/lang/en_us.json`
- `common/src/main/resources/assets/narrativecraft/lang/fr_fr.json`
- `common/src/main/resources/assets/narrativecraft/lang/de_de.json`
- `common/src/main/resources/assets/narrativecraft/lang/es_es.json`
- `common/src/main/resources/assets/narrativecraft/lang/zh_cn.json`
- `common/src/main/resources/assets/narrativecraft/lang/ru_ru.json`
- `common/src/main/resources/assets/narrativecraft/lang/ar_sa.json`
- `docs/SMOKE_RESULTS_i18n.md`
- `docs/ADDING_NEW_LANGUAGE.md`

### Modified Files
- `common/build.gradle` (add i18nCheck task)
- `.github/workflows/ci.yml` (add i18nCheck step)
- `CONTRIBUTING.md` (add translation guide)
- `docs/MULTI_VERSION_ARCHITECTURE.md` (add i18n section)
- `CHANGELOG.md` (document feature)
- ~20 screen classes (migrate Component.literal to Translation.message)
- Error handling classes (migrate to translatable errors)

---

## Risk Mitigation

| Risk | Mitigation |
|------|------------|
| RTL layout issues | Manual testing; text-only RTL scope |
| Long text overflow | Test German (20% longer); UI review |
| Missing translations | CI enforcement; en_us fallback |
| Placeholder mismatch | CI validation on every PR |
