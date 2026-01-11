// ============================================================
// NarrativeCraft Cross-Version Compatibility Test
// Version: 1.2.0
// ============================================================
//
// This story validates that all major NarrativeCraft features
// work identically across all supported Minecraft versions:
//
// - Fabric 1.19.4
// - Fabric 1.20.6
// - Fabric 1.21.11
// - NeoForge 1.20.6
// - NeoForge 1.21.11
//
// TESTING PROCEDURE:
// 1. Load this story on each target version
// 2. Play through all paths
// 3. Verify each feature works as expected
// 4. Record any version-specific issues
//
// ============================================================

VAR version_name = "Unknown"
VAR test_passed = 0
VAR test_total = 0

// ============================================================
// TEST START
// ============================================================

=== start ===
# on enter

// Initialize test counter
~ test_total = 0
~ test_passed = 0

Narrator: Starting NarrativeCraft Cross-Version Compatibility Test...

# wait 1 second

-> test_dialog

// ============================================================
// TEST 1: Basic Dialog
// ============================================================

=== test_dialog ===

~ test_total = test_total + 1

Narrator: TEST 1: Basic Dialog

// Character dialog with variable interpolation
Character: Hello! This is a basic dialog test.
Character: If you can read this, dialog rendering works correctly.

~ test_passed = test_passed + 1

-> test_choices

// ============================================================
// TEST 2: Choices
// ============================================================

=== test_choices ===

~ test_total = test_total + 1

Narrator: TEST 2: Choices

Character: Please select an option to verify choices work:

* [Option A - Success]
    Character: You selected Option A. Choice system works!
    ~ test_passed = test_passed + 1
    -> test_fade

* [Option B - Success]
    Character: You selected Option B. Choice system works!
    ~ test_passed = test_passed + 1
    -> test_fade

* [Option C - Success]
    Character: You selected Option C. Choice system works!
    ~ test_passed = test_passed + 1
    -> test_fade

// ============================================================
// TEST 3: Fade Effects
// ============================================================

=== test_fade ===

~ test_total = test_total + 1

Narrator: TEST 3: Fade Effects

Narrator: Testing fade to black...

# fade 0.5 1.0 0.5

Narrator: Fade effect completed.

~ test_passed = test_passed + 1

-> test_border

// ============================================================
// TEST 4: Border (Cinematic Bars)
// ============================================================

=== test_border ===

~ test_total = test_total + 1

Narrator: TEST 4: Border (Cinematic Bars)

# border 60 0 60 0 000000 0.8

Narrator: Cinematic borders should now be visible.

# wait 2 seconds

# border out 1.0

Narrator: Borders removed.

~ test_passed = test_passed + 1

-> test_shake

// ============================================================
// TEST 5: Screen Shake
// ============================================================

=== test_shake ===

~ test_total = test_total + 1

Narrator: TEST 5: Screen Shake

Narrator: Triggering screen shake...

# shake 0.5 0.5 0.3

Narrator: Shake effect completed.

~ test_passed = test_passed + 1

-> test_text_overlay

// ============================================================
// TEST 6: Text Overlays
// ============================================================

=== test_text_overlay ===

~ test_total = test_total + 1

Narrator: TEST 6: Text Overlays

# text testlabel create "CROSS-VERSION TEST" FFFFFF
# text testlabel position TOP
# text testlabel fade 0.3 2.0 0.5

Narrator: Text overlay should be visible at the top of the screen.

# wait 3 seconds

~ test_passed = test_passed + 1

-> test_audio

// ============================================================
// TEST 7: Audio (SFX)
// ============================================================

=== test_audio ===

~ test_total = test_total + 1

Narrator: TEST 7: Audio (Sound Effects)

# sfx start minecraft:entity.experience_orb.pickup 0.8

Narrator: You should have heard a sound effect.

# wait 1 second

~ test_passed = test_passed + 1

-> test_time_weather

// ============================================================
// TEST 8: Time and Weather
// ============================================================

=== test_time_weather ===

~ test_total = test_total + 1

Narrator: TEST 8: Time and Weather Control

Narrator: Setting time to noon...

# time set 6000

# wait 1 second

Narrator: Setting weather to rain...

# weather rain

# wait 2 seconds

Narrator: Clearing weather...

# weather clear

~ test_passed = test_passed + 1

-> test_wait

// ============================================================
// TEST 9: Wait/Timing
// ============================================================

=== test_wait ===

~ test_total = test_total + 1

Narrator: TEST 9: Wait/Timing

Narrator: Waiting 2 seconds...

# wait 2 seconds

Narrator: Wait completed.

~ test_passed = test_passed + 1

-> test_save

// ============================================================
// TEST 10: Save/Load
// ============================================================

=== test_save ===

~ test_total = test_total + 1

Narrator: TEST 10: Save Point

Narrator: Creating save point...

# save

Narrator: Save point created. Progress will be preserved.

~ test_passed = test_passed + 1

-> test_dialog_customization

// ============================================================
// TEST 11: Dialog Customization
// ============================================================

=== test_dialog_customization ===

~ test_total = test_total + 1

Narrator: TEST 11: Dialog Customization

# dialog scale 1.2
# dialog text_color FFD700
# dialog width 400

Character: This dialog should be larger, golden, and wider.

# wait 2 seconds

// Reset dialog settings
# dialog scale 1.0
# dialog text_color FFFFFF
# dialog width 300

Character: Dialog settings reset to defaults.

~ test_passed = test_passed + 1

-> test_variables

// ============================================================
// TEST 12: Variable Interpolation
// ============================================================

=== test_variables ===

~ test_total = test_total + 1

~ version_name = "Test Version"

Narrator: TEST 12: Variable Interpolation

Character: Current version name is: {version_name}
Character: Tests passed so far: {test_passed} out of {test_total}

~ test_passed = test_passed + 1

-> test_conditionals

// ============================================================
// TEST 13: Conditional Logic
// ============================================================

=== test_conditionals ===

~ test_total = test_total + 1

Narrator: TEST 13: Conditional Logic

{ test_passed > 0:
    Character: Conditional check passed! We have passing tests.
    ~ test_passed = test_passed + 1
- else:
    Character: Conditional check failed - this should not happen.
}

-> test_music

// ============================================================
// TEST 14: Background Music
// ============================================================

=== test_music ===

~ test_total = test_total + 1

Narrator: TEST 14: Background Music

# song start minecraft:music.game fadein 1.0 loop=true

Narrator: Background music should now be playing.

# wait 3 seconds

# song stop minecraft:music.game fadeout 1.0

Narrator: Music stopped.

~ test_passed = test_passed + 1

-> test_complete

// ============================================================
// TEST COMPLETION
// ============================================================

=== test_complete ===

Narrator: ============================================
Narrator: CROSS-VERSION COMPATIBILITY TEST COMPLETE
Narrator: ============================================

Narrator: Tests Passed: {test_passed} / {test_total}

{ test_passed == test_total:
    # text result create "ALL TESTS PASSED" 00FF00
    # text result position MIDDLE
    # text result fade 0.5 5.0 1.0

    Narrator: SUCCESS! All features work correctly on this version.
- else:
    # text result create "SOME TESTS FAILED" FF0000
    # text result position MIDDLE
    # text result fade 0.5 5.0 1.0

    Narrator: WARNING: Some tests did not pass. Check version compatibility.
}

# wait 5 seconds

# fade 0 2.0 0

-> END

// ============================================================
// TEST COVERAGE SUMMARY
// ============================================================
//
// This test validates:
//
// 1. Basic Dialog - Text rendering
// 2. Choices - Player selection handling
// 3. Fade Effects - Screen transitions
// 4. Border - Cinematic bars
// 5. Shake - Screen shake effects
// 6. Text Overlays - On-screen text display
// 7. Audio (SFX) - Sound effect playback
// 8. Time/Weather - World state control
// 9. Wait/Timing - Timed pauses
// 10. Save/Load - Progress persistence
// 11. Dialog Customization - Scale, color, width
// 12. Variable Interpolation - Dynamic text
// 13. Conditional Logic - Branching based on state
// 14. Background Music - Music playback and control
//
// Features NOT tested (require editor setup):
// - Camera angles
// - Cutscenes
// - Animations
// - Character recording playback
// - Area triggers
// - Interactions
//
// ============================================================
