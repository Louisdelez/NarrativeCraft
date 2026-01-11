# i18n Audit Results

**Audit Date:** 2026-01-11
**Branch:** `003-i18n-multi-lang`

---

## Summary

| Category | Count | Priority |
|----------|-------|----------|
| Screen Titles | 22 | High |
| Screen Labels/Fields | 18 | High |
| Button Labels | 4 | Medium |
| Debug/System Messages | 3 | Low |
| Symbol Buttons | 8+ | Keep literal |
| **Total to Migrate** | **47** | - |

---

## Screen Titles (22 strings)

| File | Line | Current Text | Proposed Key |
|------|------|--------------|--------------|
| `DialogCustomOptionsScreen.java` | 80 | "Character Custom Dialog Screen" | `screen.dialog_options.title` |
| `CharacterAdvancedScreen.java` | 46 | "Character Advanced Screen" | `screen.character_advanced.title` |
| `CharacterChangePoseScreen.java` | 46 | "Change pose camera angle screen" | `screen.character_pose.title` |
| `CharacterOptionsScreen.java` | 47 | "Character options screen" | `screen.character_options.title` |
| `StoryChoicesScreen.java` | 61, 72 | "Choice screen" | `screen.story_choices.title` |
| `CrashScreen.java` | 42 | "Crash Screen" | `screen.crash.title` |
| `FinishedStoryScreen.java` | 45 | "Finished Story Screen" | `screen.finished_story.title` |
| `InteractionOptionScreen.java` | 43 | "Interaction option screen" | `screen.interaction_option.title` |
| `KeyframeOptionScreen.java` | 68 | "Keyframe Option" | `screen.keyframe_option.title` |
| `EntryBoxScreen.java` | 43, 50 | "Entry box" | `screen.entry_box.title` |
| `EditInfoScreen.java` | 94 | "Edit info" | `screen.edit_info.title` |
| `MainScreen.java` | 106 | "Main screen" | `screen.main.title` |
| `KeyframeTriggerScreen.java` | 64, 71 | "Keyframe Trigger Screen" | `screen.keyframe_trigger.title` |
| `CameraAngleControllerScreen.java` | 53 | "Camera Angle Controller Screen" | `screen.camera_angle.title` |
| `MainScreenControllerScreen.java` | 53 | "Main Screen Controller Screen" | `screen.main_controller.title` |
| `AreaTriggerControllerScreen.java` | 51, 58 | "Area Trigger Controller Screen" | `screen.area_trigger.title` |
| `InteractionControllerScreen.java` | 56 | "Interaction Controller Screen" | `screen.interaction.title` |
| `CutsceneControllerScreen.java` | 54 | "Cutscene Controller Screen" | `screen.cutscene.title` |
| `CutsceneChangeTimeSkipScreen.java` | 45 | "Change Time Skip Screen" | `screen.time_skip.title` |
| `GenericSelectionScreen.java` | 54 | Dynamic title | Needs refactor |

---

## Screen Labels/Fields (18 strings)

| File | Line | Current Text | Proposed Key |
|------|------|--------------|--------------|
| `DialogCustomOptionsScreen.java` | 156 | "Padding X" | `screen.dialog_options.padding_x` |
| `DialogCustomOptionsScreen.java` | 169 | "Padding Y" | `screen.dialog_options.padding_y` |
| `DialogCustomOptionsScreen.java` | 182 | "Scale" | `screen.dialog_options.scale` |
| `DialogCustomOptionsScreen.java` | 195 | "Letter Spacing" | `screen.dialog_options.letter_spacing` |
| `DialogCustomOptionsScreen.java` | 208 | "Gap" | `screen.dialog_options.gap` |
| `DialogCustomOptionsScreen.java` | 221 | "Width" | `screen.dialog_options.width` |
| `DialogCustomOptionsScreen.java` | 234 | "Background Color" | `screen.dialog_options.bg_color` |
| `DialogCustomOptionsScreen.java` | 247 | "Text Color" | `screen.dialog_options.text_color` |
| `DialogCustomOptionsScreen.java` | 260 | "Bobbing Speed" | `screen.dialog_options.bobbing_speed` |
| `DialogCustomOptionsScreen.java` | 273 | "Bobbing Strength" | `screen.dialog_options.bobbing_strength` |
| `KeyframeOptionScreen.java` | 244 | "Up Down Value" | `screen.keyframe_option.up_down` |
| `KeyframeOptionScreen.java` | 302 | "Left Right Value" | `screen.keyframe_option.left_right` |
| `KeyframeOptionScreen.java` | 354 | "Rotation Value" | `screen.keyframe_option.rotation` |
| `KeyframeOptionScreen.java` | 409 | "FOV Value" | `screen.keyframe_option.fov` |
| `KeyframeTriggerScreen.java` | 99 | "Tick" | `screen.keyframe_trigger.tick` |
| `KeyframeTriggerScreen.java` | 113 | "Tags" | `screen.keyframe_trigger.tags` |
| `EditScreenCharacterAdapter.java` | 67, 74, 80 | "Day", "Month", "Year" | `screen.character.day/month/year` |
| `EditScreenCharacterAdapter.java` | 154 | "Model" | `screen.character.model` |

---

## Button Labels (4 strings)

| File | Line | Current Text | Proposed Key |
|------|------|--------------|--------------|
| `AbstractTextBoxScreen.java` | 55 | "Done" | `button.done` |
| `MainScreen.java` | 393 | "Dev Environment" | `button.dev_environment` |
| `MainScreenOptionsScreen.java` | 123 | "Credits" | `button.credits` |
| `ChooseCharacterScreen.java` | 74 | "← MAIN" / "NPC →" | `button.main` / `button.npc` |

---

## Debug/System Messages (3 strings)

| File | Line | Current Text | Proposed Key |
|------|------|--------------|--------------|
| `StorySaveIconGui.java` | 94 | "Fake save (debug)" | `debug.fake_save` |
| `KeyframeTrigger.java` | 54 | "Trigger" | `entity.trigger` |
| `CreditScreen.java` | 91 | "Tool Used" | `credits.tool_used` |

---

## Symbol Buttons (Keep as literal)

These use Unicode symbols and should NOT be translated:

| Symbol | Usage | Files |
|--------|-------|-------|
| `✖` | Close button | Multiple controller screens |
| `◀` `▶` | Navigation arrows | PickElementScreen, KeyframeOptionScreen |
| `✔` | Confirm button | KeyframeOptionScreen |
| `>` `\|\|` | Play/Pause | CutsceneControllerScreen |
| `1` `2` | Position markers | AreaTriggerControllerScreen |

---

## Existing i18n (Already Translated)

These already use `Translation.message()`:

- Command feedback messages (50+ keys)
- Validation messages
- Story loading messages
- Error messages (partial)

---

## Files Requiring Changes

### High Priority (Screen Classes)
1. `screens/options/DialogCustomOptionsScreen.java` - 11 strings
2. `screens/components/KeyframeOptionScreen.java` - 5 strings
3. `screens/keyframe/KeyframeTriggerScreen.java` - 4 strings
4. `screens/mainScreen/MainScreen.java` - 2 strings
5. `screens/controller/*.java` - 6 files, 1-2 strings each
6. `screens/characters/*.java` - 3 files
7. `screens/components/*.java` - 6 files
8. `screens/story/StoryChoicesScreen.java` - 1 string

### Medium Priority
9. `screens/storyManager/character/EditScreenCharacterAdapter.java` - 4 strings
10. `screens/mainScreen/MainScreenOptionsScreen.java` - 1 string
11. `screens/components/AbstractTextBoxScreen.java` - 1 string

### Low Priority
12. `gui/StorySaveIconGui.java` - 1 debug string
13. `narrative/keyframes/keyframeTrigger/KeyframeTrigger.java` - 1 entity name
14. `screens/credits/CreditScreen.java` - 1 string

---

## Recommendations

1. Start with `en_us.json` containing all 47 keys
2. Migrate screen titles first (most visible)
3. Then migrate labels/fields
4. Buttons and debug messages last
5. Keep symbols as `Component.literal()`
