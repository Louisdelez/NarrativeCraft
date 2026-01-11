// ============================================================
// NarrativeCraft Tutorial 03: Visual Effects & Cutscenes
// ============================================================
//
// This tutorial covers:
// - Cinematic borders
// - Screen shake
// - Sound effects and music
// - Camera control
// - Time and weather
// - Cutscenes
// - Wait/delays
//
// Prerequisites: Complete 01 and 02 first
// ============================================================

VAR dramatic_moment = false

=== intro ===
# on enter

// ============================================================
// CINEMATIC BORDERS
// ============================================================
//
// # border <up> <right> <down> <left> [color] [opacity]
// Creates letterbox-style borders for cinematic feel.

// Add cinematic widescreen bars (50px top and bottom)
# border 50 0 50 0

// Fade in
# fade 1.0 0 0

Narrator: Welcome to the advanced effects tutorial.
Narrator: Notice the cinematic borders? They set the mood.

-> demonstrate_effects

=== demonstrate_effects ===

// ============================================================
// SCREEN SHAKE
// ============================================================
//
// # shake <strength> <decayRate> <speed>
// Creates camera shake for impacts and drama.

Narrator: Let's try some screen shake...

// Wait a moment for dramatic effect
# wait 1 second

// Shake! (strength: 1.0, decay: 0.5, speed: 0.3)
# shake 1.0 0.5 0.3

Narrator: Boom! That adds impact to explosions or earthquakes.

-> sound_demo

=== sound_demo ===

// ============================================================
// SOUND EFFECTS
// ============================================================
//
// # sfx start <sound_id> [volume] [pitch] [loop=true/false]
// # sfx stop <sound_id>
// # sfx stop all

Narrator: Now let's add some sound effects.

// Play an ambient sound (looping)
# sfx start minecraft:ambient.cave loop=true

Narrator: Can you hear the cave ambiance?

# wait 2 seconds

// Stop the ambient sound
# sfx stop minecraft:ambient.cave

// Play a one-shot sound
# sfx start minecraft:entity.experience_orb.pickup 0.8

Narrator: And there's a satisfying pickup sound!

-> music_demo

=== music_demo ===

// ============================================================
// MUSIC
// ============================================================
//
// # song start <music_id> [loop=true/false] [fadein <time>]
// # song stop <music_id> [fadeout <time>]

Narrator: Music sets the emotional tone...

// Start music with a fade-in
# song start minecraft:music.game.creative loop=true fadein 2.0

Narrator: Notice how the music fades in smoothly.

# wait 3 seconds

Narrator: Let's fade it out now.

# song stop minecraft:music.game.creative fadeout 2.0

# wait 2 seconds

-> time_weather_demo

=== time_weather_demo ===

// ============================================================
// TIME AND WEATHER
// ============================================================
//
// # time set <value>   (day, noon, night, midnight, or ticks)
// # weather <type>     (clear, rain, thunder)

Narrator: We can control time and weather for atmosphere.

Narrator: Let's set the mood to night...

// Change time to night (13000 ticks)
# time set night

# wait 1 second

Narrator: And add some rain...

// Add rain
# weather rain

# wait 2 seconds

Narrator: Now let's clear it up.

# weather clear
# time set day

-> camera_demo

=== camera_demo ===

// ============================================================
// CAMERA CONTROL
// ============================================================
//
// # camera <angle_name> <keyframe_name>
// # gameplay
//
// NOTE: Camera angles must be set up in the scene editor first!
// This example shows the syntax, but you need to create the
// camera angles in NarrativeCraft's editor.

Narrator: Camera control adds cinematic flair.

// Example (requires setup in editor):
// # camera close_up hero_face

Narrator: You can switch between predefined camera angles.
Narrator: Set them up in the NarrativeCraft editor first.

// Return control to player
// # gameplay

-> cutscene_demo

=== cutscene_demo ===

// ============================================================
// CUTSCENES
// ============================================================
//
// # cutscene start <cutscene_name>
//
// Cutscenes are pre-recorded sequences of actions.
// They must be created in NarrativeCraft's editor.

Narrator: Cutscenes are pre-recorded sequences.

// Example (requires recording in editor):
// # cutscene start epic_entrance

Narrator: Record cutscenes using the NarrativeCraft tools.
Narrator: Then trigger them with the cutscene tag.

-> dramatic_finale

=== dramatic_finale ===

// ============================================================
// COMBINING EFFECTS
// ============================================================
//
// The real power comes from combining multiple effects!

~ dramatic_moment = true

Narrator: Let's put it all together for a dramatic moment...

// Set the stage
# border 60 0 60 0
# time set midnight
# weather thunder

# wait 1 second

// Add atmosphere
# sfx start minecraft:ambient.cave loop=true

# fade 0.5 0 0

Narrator: A storm approaches...

# wait 1 second

// Lightning strike effect!
# fade 0 0.1 0.3 FFFFFF
# shake 1.5 0.3 0.5
# sfx start minecraft:entity.lightning_bolt.thunder

Narrator: CRACK! Thunder splits the sky!

# wait 2 seconds

// Calm down
# weather clear
# time set day
# sfx stop all
# border out 1.5

Narrator: And the storm passes...

# save

-> tutorial_complete

=== tutorial_complete ===

// Clean up borders
# border clear

# fade 0 0.5 1.0

Narrator: Congratulations! You've learned advanced effects.

{ dramatic_moment:
    Narrator: That dramatic finale really showed what's possible!
}

Narrator: Now go create your own cinematic stories!

# fade 0 1.0 2.0

-> END

// ============================================================
// WHAT YOU LEARNED
// ============================================================
//
// 1. # border - Cinematic letterbox borders
// 2. # shake - Screen shake for impact
// 3. # sfx / # song - Sound effects and music
// 4. # time / # weather - World atmosphere
// 5. # camera / # gameplay - Camera control
// 6. # cutscene - Pre-recorded sequences
// 7. # wait - Timing and pacing
// 8. Combining effects for dramatic moments
//
// See TAG_REFERENCE.md for all available tags!
// ============================================================
