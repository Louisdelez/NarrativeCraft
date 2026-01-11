# Tasks: 003-i18n-multi-lang

**Feature:** Internationalization (i18n) — Complete Multi-Language Support
**Branch:** `003-i18n-multi-lang`
**Generated:** 2026-01-11

---

## Summary

| Metric | Value |
|--------|-------|
| Total Tasks | 56 |
| Phases | 9 |
| Languages | 7 (en_us, fr_fr, de_de, es_es, zh_cn, ru_ru, ar_sa) |
| Parallel Opportunities | 23 tasks |

---

## Phase 1: Audit & Preparation

**Goal:** Identify all player-facing text and prepare i18n architecture.

**Completion Criteria:**
- [x] Complete inventory of hardcoded strings documented
- [x] Key naming conventions defined and validated

### Tasks

- [x] T001 Scan codebase for all player-facing text in `common/src/main/java/fr/loudo/narrativecraft/`
- [x] T002 Identify and list all `Component.literal(...)` calls with user-visible text in `common/src/main/java/`
- [x] T003 Identify all string concatenations used for UI text in `common/src/main/java/fr/loudo/narrativecraft/screens/`
- [x] T004 Categorize texts by type (UI, errors, commands, tooltips, keybinds) in `specs/003-i18n-multi-lang/audit-results.md`
- [x] T005 Define and validate i18n key naming conventions (prefix + hierarchy) in `specs/003-i18n-multi-lang/key-conventions.md`
- [x] T006 Document i18n rules (no concatenation, mandatory placeholders) in `docs/I18N_RULES.md`

**Deliverable:** `specs/003-i18n-multi-lang/audit-results.md` with categorized string inventory

---

## Phase 2: Key Extraction (Source of Truth)

**Goal:** Create complete English language file and migrate all hardcoded strings.

**Completion Criteria:**
- [x] `en_us.json` exists with all keys
- [x] Zero `Component.literal()` with user-facing text remains
- [x] All strings use `Translation.message()` or `Component.translatable()`

### Tasks

- [x] T007 Create `common/src/main/resources/assets/narrativecraft/lang/en_us.json` with initial structure
- [x] T008 [P] Extract all UI screen texts to i18n keys in `common/src/main/java/fr/loudo/narrativecraft/screens/**/*.java`
- [x] T009 [P] Extract all Ink validation error messages to i18n keys in `common/src/main/java/fr/loudo/narrativecraft/narrative/`
- [x] T010 [P] Extract all command feedback messages to i18n keys in `common-mc120/src/main/java/fr/loudo/narrativecraft/commands/`
- [x] T011 [P] Extract keybind names and categories to i18n keys in `common/src/main/java/fr/loudo/narrativecraft/keys/ModKeys.java`
- [x] T012 Replace all hardcoded `Component.literal()` with `Translation.message()` across all screen classes
- [x] T013 Verify `en_us.json` covers 100% of player-facing texts (grep validation)
- [x] T014 Remove all remaining string concatenations used for translatable text

**Deliverable:** Complete `en_us.json` with ~200 keys

---

## Phase 3: Multi-Language Translations

**Goal:** Create all 6 additional language files with complete translations.

**Completion Criteria:**
- [x] All 7 language files exist
- [x] All keys translated in each locale
- [x] Placeholder consistency validated

### Tasks

- [x] T015 [P] Create `common/src/main/resources/assets/narrativecraft/lang/fr_fr.json` with French translations
- [x] T016 [P] Create `common/src/main/resources/assets/narrativecraft/lang/de_de.json` with German translations
- [x] T017 [P] Create `common/src/main/resources/assets/narrativecraft/lang/es_es.json` with Spanish translations
- [x] T018 [P] Create `common/src/main/resources/assets/narrativecraft/lang/zh_cn.json` with Simplified Chinese translations
- [x] T019 [P] Create `common/src/main/resources/assets/narrativecraft/lang/ru_ru.json` with Russian translations
- [x] T020 [P] Create `common/src/main/resources/assets/narrativecraft/lang/ar_sa.json` with Arabic translations
- [x] T021 Translate all keys from `en_us.json` to each locale file
- [x] T022 Verify placeholder consistency (%s, %d) between all locales
- [x] T023 Verify UTF-8 encoding and valid JSON syntax for all language files

**Deliverable:** 7 complete language files

---

## Phase 4: RTL (Arabic) & UI Robustness

**Goal:** Ensure Arabic RTL displays correctly without breaking UI.

**Completion Criteria:**
- [x] No dynamic string concatenation in translatable text
- [x] Arabic UI tested and functional
- [x] RTL limitations documented

### Tasks

- [x] T024 Verify no text is dynamically concatenated in `common/src/main/java/fr/loudo/narrativecraft/`
- [x] T025 Test main UI screens in `ar_sa` locale (MainScreen, StoryChoicesScreen, DialogCustomOptionsScreen)
- [x] T026 Test dialogs, choices, and overlays in RTL mode
- [x] T027 Test error messages and warnings display in RTL mode
- [x] T028 Adjust layouts/alignments if necessary in screen classes
- [x] T029 Document RTL limitations (if any) in `docs/I18N_RTL_NOTES.md`

**Deliverable:** RTL validation report in `docs/SMOKE_RESULTS_i18n.md`

---

## Phase 5: Errors, Validation & System Messages

**Goal:** Internationalize all error messages including WHAT/WHERE/WHY/FIX format.

**Completion Criteria:**
- [x] All Ink errors use i18n keys
- [x] ErrorFormatter fully internationalized
- [x] Zero hardcoded system messages

### Tasks

- [x] T030 [P] Internationalize all Ink validation errors in `common/src/main/java/fr/loudo/narrativecraft/narrative/story/`
- [x] T031 [P] Migrate `NarrativeException` to use translation keys in `common/src/main/java/fr/loudo/narrativecraft/`
- [x] T032 [P] Migrate `ErrorFormatter` (WHAT/WHERE/WHY/FIX) to i18n in `common/src/main/java/fr/loudo/narrativecraft/util/`
- [x] T033 [P] Translate compatibility warnings in `CompatLogger` classes
- [x] T034 Verify no system message is hardcoded (grep verification)

**Deliverable:** Zero untranslated error messages

---

## Phase 6: CI & Automated Checks

**Goal:** Prevent i18n regressions with automated validation.

**Completion Criteria:**
- [x] `i18nCheck` Gradle task implemented
- [x] CI fails on missing keys or placeholder mismatches
- [x] Unused key warnings enabled

### Tasks

- [x] T035 Implement Gradle task `i18nCheck` in `common/build.gradle`
- [x] T036 [P] Add missing key detection per locale in `i18nCheck` task
- [x] T037 [P] Add placeholder inconsistency detection in `i18nCheck` task
- [x] T038 [P] Add invalid JSON detection in `i18nCheck` task
- [x] T039 Add `i18nCheck` step to `.github/workflows/ci.yml`
- [x] T040 Configure CI to fail build on i18n errors
- [x] T041 [P] Add warning for orphan/unused keys in `i18nCheck` task (optional)

**Deliverable:** CI job blocking PRs on i18n errors

---

## Phase 7: Documentation & Contributor Workflow

**Goal:** Enable community translation contributions.

**Completion Criteria:**
- [x] Translation guide in CONTRIBUTING.md
- [x] New language checklist documented
- [x] i18n section in architecture docs

### Tasks

- [x] T042 [P] Add "How to translate" guide section in `CONTRIBUTING.md`
- [x] T043 [P] Document key conventions and placeholder rules in `docs/I18N_RULES.md`
- [x] T044 [P] Add i18n section to `docs/MULTI_VERSION_ARCHITECTURE.md`
- [x] T045 [P] Add "Adding a new language" checklist in `docs/ADDING_NEW_LANGUAGE.md`
- [x] T046 Document translator workflow (en_us → other locales) in `CONTRIBUTING.md`

**Deliverable:** Complete translation documentation

---

## Phase 8: Smoke Tests & Final Validation

**Goal:** Verify in-game functionality across languages and loaders.

**Completion Criteria:**
- [x] en_us, fr_fr, ar_sa tested in-game
- [x] Tested on Fabric and NeoForge
- [x] Results documented

### Tasks

- [x] T047 [P] Test in-game with `en_us` locale on Fabric 1.21.11
- [x] T048 [P] Test in-game with `fr_fr` locale on Fabric 1.21.11
- [x] T049 [P] Test in-game with `ar_sa` locale on Fabric 1.21.11
- [x] T050 [P] Test on Fabric (at least one version: 1.21.11 or 1.20.6)
- [x] T051 [P] Test on NeoForge (at least one version: 1.21.11 or 1.20.6)
- [x] T052 Document smoke test results in `docs/SMOKE_RESULTS_i18n.md`

**Deliverable:** Signed-off smoke test results

---

## Phase 9: Closure & Final Validation

**Goal:** Lock down feature for release.

**Completion Criteria:**
- [x] Zero hardcoded player-facing text
- [x] All 7 languages load without errors
- [x] CI green with i18nCheck active
- [x] Feature marked as DONE

### Tasks

- [x] T053 Verify zero hardcoded text remains (final grep scan)
- [x] T054 Verify all 7 languages load correctly without console errors
- [x] T055 Verify CI is green with `i18nCheck` active
- [x] T056 Mark feature i18n as DONE in `specs/003-i18n-multi-lang/spec.md`

**Deliverable:** Feature complete, ready for merge

---

## Dependencies

```
Phase 1 (Audit)
    │
    ▼
Phase 2 (Key Extraction) ──────────────────┐
    │                                       │
    ▼                                       ▼
Phase 3 (Translations) ◄───────────► Phase 5 (Errors i18n)
    │                                       │
    ├───────────────────────────────────────┤
    ▼                                       ▼
Phase 4 (RTL)                         Phase 6 (CI)
    │                                       │
    └───────────────────────────────────────┤
                                            ▼
                                    Phase 7 (Docs)
                                            │
                                            ▼
                                    Phase 8 (Smoke Tests)
                                            │
                                            ▼
                                    Phase 9 (Closure)
```

---

## Parallel Execution Opportunities

### Phase 2: Key Extraction (4 parallel)
```
T008 ─┬─► T012 (after all extractions)
T009 ─┤
T010 ─┤
T011 ─┘
```

### Phase 3: Translations (6 parallel)
```
T015 (fr_fr) ─┐
T016 (de_de) ─┤
T017 (es_es) ─┼─► T021 → T022 → T023
T018 (zh_cn) ─┤
T019 (ru_ru) ─┤
T020 (ar_sa) ─┘
```

### Phase 5: Errors (4 parallel)
```
T030 ─┬─► T034 (verification)
T031 ─┤
T032 ─┤
T033 ─┘
```

### Phase 6: CI Checks (3 parallel)
```
T036 ─┬─► T039 → T040
T037 ─┤
T038 ─┘
```

### Phase 7: Documentation (4 parallel)
```
T042 ─┬─► T046 (depends on T042)
T043 ─┤
T044 ─┤
T045 ─┘
```

### Phase 8: Smoke Tests (5 parallel)
```
T047 ─┬─► T052 (documentation)
T048 ─┤
T049 ─┤
T050 ─┤
T051 ─┘
```

---

## File Paths Summary

### New Files to Create
| File | Phase | Task |
|------|-------|------|
| `common/src/main/resources/assets/narrativecraft/lang/en_us.json` | 2 | T007 |
| `common/src/main/resources/assets/narrativecraft/lang/fr_fr.json` | 3 | T015 |
| `common/src/main/resources/assets/narrativecraft/lang/de_de.json` | 3 | T016 |
| `common/src/main/resources/assets/narrativecraft/lang/es_es.json` | 3 | T017 |
| `common/src/main/resources/assets/narrativecraft/lang/zh_cn.json` | 3 | T018 |
| `common/src/main/resources/assets/narrativecraft/lang/ru_ru.json` | 3 | T019 |
| `common/src/main/resources/assets/narrativecraft/lang/ar_sa.json` | 3 | T020 |
| `specs/003-i18n-multi-lang/audit-results.md` | 1 | T004 |
| `specs/003-i18n-multi-lang/key-conventions.md` | 1 | T005 |
| `docs/I18N_RULES.md` | 1 | T006 |
| `docs/I18N_RTL_NOTES.md` | 4 | T029 |
| `docs/ADDING_NEW_LANGUAGE.md` | 7 | T045 |
| `docs/SMOKE_RESULTS_i18n.md` | 8 | T052 |

### Files to Modify
| File | Phase | Tasks |
|------|-------|-------|
| `common/build.gradle` | 6 | T035-T041 |
| `.github/workflows/ci.yml` | 6 | T039-T040 |
| `CONTRIBUTING.md` | 7 | T042, T046 |
| `docs/MULTI_VERSION_ARCHITECTURE.md` | 7 | T044 |
| Screen classes (~20 files) | 2 | T008, T012 |
| Error handling classes | 5 | T030-T033 |

---

## MVP Scope

**Recommended MVP:** Phases 1-2 + en_us only

- Complete audit (Phase 1)
- Create `en_us.json` and migrate all strings (Phase 2)
- Other languages can be added incrementally

**MVP Completion Criteria:**
- [x] `en_us.json` complete
- [x] All `Component.literal()` replaced with `Translation.message()`
- [x] Build passes on all 5 targets

---

## Definition of Done

- [x] 7 language files complete (en_us, fr_fr, de_de, es_es, zh_cn, ru_ru, ar_sa)
- [x] Zero player-facing hardcoded text
- [x] CI blocks on i18n errors
- [x] Errors, validations, and commands fully translated
- [x] RTL (ar_sa) functional
- [x] No regression on 5 build targets
- [x] Documentation updated

---

## Feature Status: DONE

**Completed:** 2026-01-11
**Total Tasks:** 56/56 (100%)
**Languages:** 7 (122 keys each)
