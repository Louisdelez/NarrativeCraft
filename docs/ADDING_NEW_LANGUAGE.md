# Adding a New Language to NarrativeCraft

This guide provides a step-by-step checklist for adding a new language translation to NarrativeCraft.

---

## Prerequisites

- [ ] Familiarity with the target language
- [ ] Basic understanding of JSON format
- [ ] Access to the NarrativeCraft repository

---

## Checklist

### Step 1: Create the Language File

- [ ] Navigate to `common/src/main/resources/assets/narrativecraft/lang/`
- [ ] Create a new file named `xx_yy.json` (e.g., `pt_br.json` for Brazilian Portuguese)
  - Use Minecraft's locale codes: `language_COUNTRY` format
  - Common codes: https://minecraft.wiki/w/Language

### Step 2: Copy the Structure

- [ ] Copy the entire contents of `en_us.json` to your new file
- [ ] Add a comment at the top identifying the language:
  ```json
  {
    "_comment": "NarrativeCraft Portuguese (Brazil) - Translated from en_us.json",
    ...
  }
  ```

### Step 3: Translate All Values

- [ ] Translate each value (right side of `:`)
- [ ] Do NOT modify keys (left side of `:`)
- [ ] Preserve all placeholders (`%s`, `%d`, `%1$s`, etc.)

**Example:**
```json
// en_us.json (original)
"narrativecraft.button.done": "Done"

// pt_br.json (translated)
"narrativecraft.button.done": "Concluído"
```

### Step 4: Handle Placeholders

- [ ] Keep same number of `%s` and `%d` placeholders
- [ ] For positional placeholders (`%1$s`, `%2$d`), you may reorder but keep the same indexes

**Example with placeholder:**
```json
// en_us.json
"narrativecraft.screen.scene_manager.title": "Scene Manager - Chapter %s"

// pt_br.json
"narrativecraft.screen.scene_manager.title": "Gerenciador de Cenas - Capítulo %s"
```

### Step 5: RTL Considerations (Arabic, Hebrew, etc.)

If adding an RTL (Right-to-Left) language:
- [ ] Minecraft handles text direction automatically
- [ ] Do NOT manually reverse text
- [ ] Test UI layout carefully - some elements may need adjustment
- [ ] Document any RTL-specific issues in `docs/I18N_RTL_NOTES.md`

### Step 6: Validate the Translation

- [ ] Run the i18n check:
  ```bash
  ./gradlew :common:i18nCheck
  ```
- [ ] Fix any errors reported:
  - Missing keys
  - Placeholder mismatches
  - Invalid JSON syntax

### Step 7: Test In-Game

- [ ] Change Minecraft language to your new locale
- [ ] Test the following screens:
  - Main menu
  - Story options
  - Dialog options
  - Character options
  - Error messages (if possible)
- [ ] Verify no untranslated strings appear (will show as key names)
- [ ] Check text fits in UI elements (no overflow/truncation)

### Step 8: Submit Your Translation

- [ ] Create a branch: `i18n/add-xx_yy`
- [ ] Commit your changes:
  ```bash
  git add common/src/main/resources/assets/narrativecraft/lang/xx_yy.json
  git commit -m "Add xx_yy (Language Name) translation"
  ```
- [ ] Push and create a Pull Request
- [ ] In PR description, include:
  - Native speaker status (native/fluent/intermediate)
  - Any known issues or limitations
  - Any cultural adaptations made

---

## Common Locale Codes

| Code | Language | Notes |
|------|----------|-------|
| `pt_br` | Portuguese (Brazil) | |
| `pt_pt` | Portuguese (Portugal) | |
| `ja_jp` | Japanese | |
| `ko_kr` | Korean | |
| `it_it` | Italian | |
| `nl_nl` | Dutch | |
| `pl_pl` | Polish | |
| `tr_tr` | Turkish | |
| `uk_ua` | Ukrainian | |
| `vi_vn` | Vietnamese | |
| `th_th` | Thai | |
| `he_il` | Hebrew | RTL |
| `fa_ir` | Persian/Farsi | RTL |

---

## Placeholder Reference

| Format | Type | Example | Usage |
|--------|------|---------|-------|
| `%s` | String | `"Error: %s"` | Text values |
| `%d` | Integer | `"%d errors found"` | Numbers |
| `%1$s` | Positional string | `"File %1$s at line %2$d"` | Reorderable |
| `%2$d` | Positional integer | Same as above | Reorderable |

---

## Quality Checklist

Before submitting, verify:

- [ ] All 122+ keys are translated
- [ ] No English text remains (except proper nouns like "NarrativeCraft")
- [ ] Placeholders match exactly
- [ ] JSON is valid (no syntax errors)
- [ ] Translations are natural, not literal word-for-word
- [ ] Technical terms are consistently translated
- [ ] UI text fits in buttons/labels (test in-game)

---

## Need Help?

- **Questions**: Open a GitHub issue with `[i18n]` prefix
- **Review**: Request review from native speakers in Discord
- **Reference**: Check existing translations for context

Thank you for helping make NarrativeCraft accessible to more players!
