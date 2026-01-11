// ============================================================
// NarrativeCraft Showcase: Advanced Complex Scene
// ============================================================
//
// This showcase demonstrates advanced NarrativeCraft features:
// - Complex dialog with character switching
// - Branching narratives with state tracking
// - Visual effects (fade, border, shake)
// - Audio integration (sfx, music)
// - Camera control references
// - Time/weather manipulation
// - Dialog UI customization
// - Cutscene references
// - Animation references
// - Save points and progression
//
// Also includes COMMENTED error examples to show error messages!
//
// NOTE: Some features (camera, cutscene, animation) require
// setup in the NarrativeCraft editor first.
// ============================================================

// ============================================================
// VARIABLES - Track player state across the scene
// ============================================================

VAR player_name = "Traveler"
VAR trust_level = 0           // -100 to +100 scale
VAR has_sword = false
VAR has_key = false
VAR discovered_secret = false
VAR time_remaining = 5        // Minutes until event
VAR gold = 50
VAR dramatic_music_playing = false

// ============================================================
// SCENE START
// ============================================================

=== intro ===
# on enter

// ============================================================
// ESTABLISHING ATMOSPHERE
// ============================================================
//
// Combine multiple effects for immersive scene transitions.
// Order matters! Set up atmosphere before dialog.

// Cinematic widescreen bars
# border 60 0 60 0 000000 0.9

// Set moody evening time
# time set 12000

// Light rain for atmosphere
# weather rain

// Dramatic fade in from black
# fade 2.0 0 0

// Start ambient music with slow fade-in
# song start minecraft:music.game fadein 3.0 loop=true
~ dramatic_music_playing = true

// ============================================================
// DIALOG CUSTOMIZATION
// ============================================================
//
// Customize dialog appearance for this scene.
// These persist until changed again.

// Slightly larger text for cinematic feel
# dialog scale 1.2

// Yellowish text color (warm candlelight theme)
# dialog text_color FFD700

// Wider dialog box
# dialog width 400

-> castle_entrance

// ============================================================
// MAIN SCENE: Castle Entrance
// ============================================================

=== castle_entrance ===

// ============================================================
// CHARACTER INTRODUCTION WITH TEXT OVERLAY
// ============================================================

// Display location title
# text location create "The Castle of Shadows" FFFFFF
# text location position TOP
# text location fade 0.5 2.0 1.0

# wait 2 seconds

// Narration sets the scene
Narrator: The ancient castle looms before you, its towers piercing the storm clouds.

Narrator: A hooded figure emerges from the shadows...

// ============================================================
// ERROR EXAMPLE: Unknown Tag (COMMENTED)
// ============================================================
//
// Uncomment the line below to see NarrativeCraft's error message:
//
// # fde 1.0 2.0 1.0
//
// This will produce:
// [WHAT] Unknown tag
//   Error: 'fde' tag is invalid
// [WHERE] Story: showcase / Scene: complex-scene / Line: X
//   Command: fde 1.0 2.0 1.0
// [WHY] The tag 'fde' is not a recognized NarrativeCraft tag
// [FIX] Did you mean 'fade'?

// ============================================================
// MULTI-CHARACTER DIALOG
// ============================================================

Mysterious Figure: Halt, {player_name}. What brings you to Castle Shadows?

* [I seek the artifact] -> seek_artifact
* [I'm here to rescue the princess] -> rescue_mission
* [Just passing through...] -> suspicious_response
* [Remain silent] -> silent_response

// ============================================================
// BRANCHING PATH: Seek Artifact
// ============================================================

=== seek_artifact ===

~ trust_level = trust_level + 10

Mysterious Figure: Ah, a treasure hunter. Bold... or foolish.

// Sound effect for dramatic reveal
# sfx start minecraft:block.chest.open 0.8

Mysterious Figure: The artifact you seek lies deep within.

// The figure removes their hood
# shake 0.3 0.8 0.2

Marcus: I am Marcus, guardian of this place.

Marcus: Few who enter ever leave. But perhaps...

// ============================================================
// ERROR EXAMPLE: Missing Argument (COMMENTED)
// ============================================================
//
// Uncomment to see missing argument error:
//
// # fade 1.0
//
// Error message:
// [WHAT] Missing required argument
// [WHERE] Story: showcase / Scene: complex-scene / Line: X
//   Command: fade 1.0
// [WHY] The tag 'fade' requires the argument 'staySeconds'
// [FIX] Add the 'staySeconds' argument (type: float)

-> marcus_offer

// ============================================================
// BRANCHING PATH: Rescue Mission
// ============================================================

=== rescue_mission ===

~ trust_level = trust_level + 5

Mysterious Figure: The princess? How noble.

// Dramatic thunder
# sfx start minecraft:entity.lightning_bolt.thunder 0.7
# shake 0.5 0.5 0.3

Mysterious Figure: She is held in the highest tower.

// Figure reveals themselves
Marcus: I am Marcus. I guard the castle... but I have no love for its lord.

Marcus: Perhaps we can help each other.

-> marcus_offer

// ============================================================
// BRANCHING PATH: Suspicious Response
// ============================================================

=== suspicious_response ===

~ trust_level = trust_level - 20

Mysterious Figure: "Passing through"? Through a castle surrounded by cliffs?

// Tension building
# sfx start minecraft:entity.guardian.ambient 0.5

Mysterious Figure: You lie poorly, stranger.

// Reveal
Marcus: I am Marcus, and I don't take kindly to liars.

{ trust_level < -10:
    Marcus: Give me one reason not to call the guards.
    -> confrontation
- else:
    Marcus: But... you don't seem like a threat. State your true purpose.
    -> second_chance
}

// ============================================================
// BRANCHING PATH: Silent Response
// ============================================================

=== silent_response ===

// Silence is mysterious
Mysterious Figure: ...

# wait 2 seconds

Mysterious Figure: The silent type. Interesting.

// Remove hood
Marcus: I am Marcus. Your silence speaks volumes.

~ trust_level = trust_level + 15

Marcus: A wise person knows when words are unnecessary.

-> marcus_offer

// ============================================================
// CONFRONTATION PATH
// ============================================================

=== confrontation ===

// Hostile music change
# song stop minecraft:music.game fadeout 1.0
# wait 1 second
# song start minecraft:music.nether.basalt_deltas loop=true fadein 0.5
~ dramatic_music_playing = true

Marcus: Guards! We have an intruder!

// Screen shake for alarm
# shake 0.8 0.4 0.4
# sfx start minecraft:block.bell.use 1.0

* [Fight!] -> fight_scene
* [Run!] -> escape_attempt
* [Wait! I'll tell the truth!] -> second_chance

// ============================================================
// SECOND CHANCE PATH
// ============================================================

=== second_chance ===

~ trust_level = trust_level + 5

Marcus: Speak then. And speak honestly.

* [I seek the artifact] -> seek_artifact
* [I'm here to rescue the princess] -> rescue_mission
* [I have a message for the lord] -> messenger_lie

// ============================================================
// MARCUS OFFER - Main junction point
// ============================================================

=== marcus_offer ===

// ============================================================
// CONDITIONAL CONTENT BASED ON TRUST
// ============================================================

{ trust_level >= 20:
    Marcus: I sense you are trustworthy, {player_name}.
    Marcus: I will help you... if you help me first.
- trust_level >= 0:
    Marcus: You seem... acceptable. But I need proof of your intentions.
- else:
    Marcus: I don't trust you. But perhaps you can earn that trust.
}

Marcus: The lord of this castle holds something precious to me.

Marcus: A locket, stolen from my family. Retrieve it, and I'll help you.

// ============================================================
// SAVE POINT
// ============================================================
//
// Critical decision point - save player progress

# save

// ============================================================
// ERROR EXAMPLE: Invalid Argument Type (COMMENTED)
// ============================================================
//
// Uncomment to see type error:
//
// # wait five seconds
//
// Error message:
// [WHAT] Invalid argument type
// [WHERE] Story: showcase / Scene: complex-scene / Line: X
//   Command: wait five seconds
// [WHY] Expected number but got 'five'
// [FIX] Change 'five' to a valid number

* [I accept your quest] -> accept_quest
* [What's in it for me?] -> negotiate
* [I work alone] -> refuse_help

// ============================================================
// QUEST ACCEPTED
// ============================================================

=== accept_quest ===

~ trust_level = trust_level + 25

Marcus: Excellent. The lord keeps the locket in his study.

// Give directions
# text marker create "Study: North Tower, 3rd Floor" FFFF00
# text marker position BOTTOM_RIGHT
# text marker fade 0.3 5.0 0.5

Marcus: Take this key. It opens the servant's passage.

# sfx start minecraft:entity.item.pickup 0.7
~ has_key = true

Narrator: You received a Servant's Key.

Marcus: Avoid the guards. They patrol every few minutes.

// ============================================================
// CAMERA/CUTSCENE REFERENCES
// ============================================================
//
// These require setup in the NarrativeCraft editor.
// Uncomment when your camera angles and cutscenes are ready:
//
// # camera wide_shot castle_overview
// # cutscene start marcus_points_direction

Marcus: Go now. May fortune favor you.

// Remove cinematic borders for exploration
# border out 1.5

// Return to normal dialog settings
# dialog scale 1.0
# dialog text_color FFFFFF

-> castle_interior

// ============================================================
// NEGOTIATE PATH
// ============================================================

=== negotiate ===

~ trust_level = trust_level - 5

Marcus: You want payment? Fine.

Marcus: Bring me the locket, and I'll give you 200 gold and access to the artifact chamber.

* [Deal]
    ~ trust_level = trust_level + 10
    -> accept_quest
* [Not enough. I want the artifact itself.]
    Marcus: ...You have nerve. Very well. The artifact AND 100 gold.
    ~ trust_level = trust_level + 5
    -> accept_quest
* [Forget it. I'll find my own way.]
    -> refuse_help

// ============================================================
// REFUSE HELP PATH
// ============================================================

=== refuse_help ===

~ trust_level = trust_level - 15

Marcus: A mistake, but your choice to make.

Marcus: The castle is treacherous. Don't say I didn't warn you.

// Cold wind effect
# sfx start minecraft:item.elytra.flying 0.3

Marcus: Good luck, {player_name}. You'll need it.

# border out 1.0

-> castle_interior

// ============================================================
// FIGHT SCENE (from confrontation)
// ============================================================

=== fight_scene ===

// Dramatic effects
# shake 1.5 0.3 0.5
# fade 0 0.1 0.2 FF0000

Marcus: So be it!

// ============================================================
// ERROR EXAMPLE: Invalid Argument Value (COMMENTED)
// ============================================================
//
// Uncomment to see value validation error:
//
// # border 10 10 10 10 000000 2.5
//
// Error message:
// [WHAT] Invalid argument value
// [WHERE] Story: showcase / Scene: complex-scene / Line: X
//   Command: border 10 10 10 10 000000 2.5
// [WHY] The value '2.5' is invalid: must be between 0.0 and 1.0
// [FIX] Use a value that must be between 0.0 and 1.0

{ has_sword:
    Narrator: You draw your sword!
    // Combat would go here - simplified for demo
    # shake 1.0 0.5 0.3
    # sfx start minecraft:entity.player.attack.sweep 0.8
    Narrator: After a fierce battle, Marcus retreats.
    ~ trust_level = -50
    -> castle_interior
- else:
    Narrator: Without a weapon, you're quickly overpowered.
    -> game_over_captured
}

// ============================================================
// ESCAPE ATTEMPT
// ============================================================

=== escape_attempt ===

Narrator: You turn and run!

# sfx start minecraft:entity.player.hurt 0.5
# shake 0.5 0.6 0.4

Marcus: After them!

// Dramatic chase effect
# fade 0 0.5 0.5

-> castle_interior_hunted

// ============================================================
// MESSENGER LIE PATH
// ============================================================

=== messenger_lie ===

~ trust_level = trust_level - 10

Marcus: A messenger? Show me the message.

* [I... lost it]
    Marcus: How convenient.
    ~ trust_level = trust_level - 20
    -> confrontation
* [It's verbal, for the lord's ears only]
    Marcus: Hmm. The lord doesn't accept visitors.
    Marcus: But perhaps I can arrange an... audience.
    ~ trust_level = trust_level + 5
    -> marcus_offer

// ============================================================
// CASTLE INTERIOR
// ============================================================

=== castle_interior ===

// Change atmosphere for interior
# weather clear
# time set 13500

{ has_key:
    Narrator: Using the servant's key, you enter the castle through a hidden passage.
    # sfx start minecraft:block.wooden_door.open 0.6
- else:
    Narrator: You find a window and climb inside. The castle is dark and cold.
}

// Ambient cave sounds for atmosphere
# sfx start minecraft:ambient.cave loop=true

Narrator: Torches flicker along stone walls. The air smells of dust and secrets.

// ============================================================
// EXPLORATION CHOICE
// ============================================================

{ has_key:
    * [Head to the study (North Tower)] -> study_approach
}
* [Explore the main hall] -> main_hall
* [Search for another way] -> alternative_route

// ============================================================
// CASTLE INTERIOR (HUNTED)
// ============================================================

=== castle_interior_hunted ===

# sfx start minecraft:ambient.cave loop=true

Narrator: You barely escape into the castle. Guards are searching for you.

// Urgency text
# text warning create "GUARDS ALERTED" FF0000
# text warning position TOP_RIGHT
# text warning fade 0.2 3.0 0.5

~ time_remaining = 3

* [Hide in the shadows] -> hide_attempt
* [Keep moving] -> main_hall

// ============================================================
// STUDY APPROACH
// ============================================================

=== study_approach ===

# sfx stop minecraft:ambient.cave fadeout 0.5

Narrator: You climb the North Tower's spiral staircase.

// Progress indicator
# text floor create "Floor 2 of 3" AAAAAA
# text floor position BOTTOM_LEFT
# text floor fade 0.2 2.0 0.3

# wait 1 second

# text floor edit "Floor 3 of 3"
# text floor fade 0.2 2.0 0.3

Narrator: The study door stands before you.

# sfx start minecraft:block.wooden_door.open 0.5

-> study_scene

// ============================================================
// STUDY SCENE - Quest objective
// ============================================================

=== study_scene ===

// Rich atmosphere
# border 30 30 30 30 1a0a00 0.8

Narrator: The study is filled with ancient books and strange artifacts.

Narrator: On the desk... a glinting locket!

# sfx start minecraft:block.amethyst_block.chime 0.7

* [Take the locket] -> take_locket
* [Search for more items first] -> search_study

// ============================================================
// TAKE LOCKET
// ============================================================

=== take_locket ===

# sfx start minecraft:entity.item.pickup 0.8

Narrator: You take Marcus's family locket.

// ============================================================
// ANIMATION REFERENCE (COMMENTED)
// ============================================================
//
// If you have animations set up in the editor:
//
// # animation start pick_up_item block=true

~ discovered_secret = true

// Success save
# save

Narrator: Now to return to Marcus...

# border out 1.0

-> return_to_marcus

// ============================================================
// SEARCH STUDY
// ============================================================

=== search_study ===

Narrator: You search the study carefully.

{ not discovered_secret:
    Narrator: Behind a loose brick, you find a hidden compartment!
    # sfx start minecraft:block.stone.break 0.5
    Narrator: Inside: a map showing a secret passage to the artifact chamber!
    ~ discovered_secret = true
    ~ gold = gold + 25
    Narrator: (Found 25 gold and a Secret Map)
}

* [Take the locket] -> take_locket
* [Leave without the locket] -> leave_study

// ============================================================
// LEAVE STUDY
// ============================================================

=== leave_study ===

Narrator: You decide to leave the locket. Perhaps it's a trap.

# border out 1.0

-> castle_interior

// ============================================================
// RETURN TO MARCUS
// ============================================================

=== return_to_marcus ===

// Return to entrance atmosphere
# time set 12500
# weather rain

Narrator: You return to find Marcus waiting.

Marcus: You have it! My family's locket!

# sfx start minecraft:entity.player.levelup 0.7

{ trust_level >= 20:
    Marcus: I knew I could trust you. Here, as promised.
    ~ gold = gold + 200
    Narrator: (Received 200 gold)
    Marcus: The artifact chamber is through the chapel. Here's the key.
- trust_level >= 0:
    Marcus: Well done. You've proven yourself.
    ~ gold = gold + 100
    Narrator: (Received 100 gold)
    Marcus: The artifact chamber... I'll show you the way.
- else:
    Marcus: ...You actually did it. I'm... surprised.
    ~ gold = gold + 50
    Narrator: (Received 50 gold)
    Marcus: The artifact is in the chapel basement. Don't make me regret this.
}

// ============================================================
// FINAL SCENE TRANSITION
// ============================================================

# save

// Dramatic music swell
# song stop minecraft:music.game fadeout 2.0
# song stop minecraft:music.nether.basalt_deltas fadeout 2.0

# wait 1 second

// Epic finale music
# song start minecraft:music.end fadein 2.0

// Cinematic borders
# border 60 0 60 0

# fade 0 1.0 2.0

-> artifact_chamber

// ============================================================
// ARTIFACT CHAMBER - Story Climax
// ============================================================

=== artifact_chamber ===
# on enter
# fade 2.0 0 0

// Awe-inspiring atmosphere
# sfx start minecraft:block.beacon.activate 0.8
# shake 0.3 0.9 0.2

Narrator: The Artifact of Shadows pulses with ancient power.

{ discovered_secret:
    Narrator: Thanks to the secret map, you knew exactly where to look.
}

Narrator: Your journey through Castle Shadows ends here.

// Final statistics
# text stats create "Quest Complete! Trust: {trust_level} | Gold: {gold}" 00FF00
# text stats position BOTTOM
# text stats fade 0.5 5.0 1.0

# wait 5 seconds

// Fade to credits
# fade 0 2.0 0

-> END

// ============================================================
// GAME OVER STATES
// ============================================================

=== game_over_captured ===

# song stop minecraft:music.game fadeout 1.0
# song stop minecraft:music.nether.basalt_deltas fadeout 1.0

# fade 0 0.5 0 000000

Narrator: The guards overpower you. You are thrown into the dungeon.

# text gameover create "CAPTURED" FF0000
# text gameover position MIDDLE
# text gameover scale 2.0
# text gameover fade 0.5 3.0 1.0

# wait 4 seconds

-> END

=== hide_attempt ===

Narrator: You press yourself into the shadows, holding your breath.

# wait 2 seconds

// Random chance simulation
{ time_remaining > 2:
    # sfx start minecraft:entity.guardian.ambient 0.3
    Narrator: Footsteps pass... then fade away. Safe, for now.
    ~ time_remaining = time_remaining - 1
    -> main_hall
- else:
    # sfx start minecraft:entity.guardian.attack 0.6
    # shake 0.5 0.5 0.3
    Narrator: A guard spots you!
    -> game_over_captured
}

=== main_hall ===

// Placeholder for main hall exploration
Narrator: The main hall stretches before you. Portraits of stern nobles line the walls.

* [Continue searching] -> castle_interior
* [Look for an exit] -> refuse_help

=== alternative_route ===

// Placeholder for alternative exploration
Narrator: You search for another way through the castle.

{ discovered_secret:
    Narrator: The secret map reveals a hidden passage!
    -> study_approach
- else:
    Narrator: Without more information, you're wandering blind.
    -> castle_interior
}

// ============================================================
// END OF SHOWCASE
// ============================================================
//
// This complex scene demonstrated:
//
// 1. DIALOG FEATURES:
//    - Multi-character conversations
//    - Character name: Text format
//    - Variable interpolation ({player_name})
//
// 2. BRANCHING NARRATIVES:
//    - Multiple choice points
//    - Trust level tracking
//    - Conditional content based on state
//    - Multiple endings (success/capture)
//
// 3. VISUAL EFFECTS:
//    - # fade with timing and color
//    - # border for cinematic bars
//    - # shake for impact
//    - # text for overlays
//
// 4. AUDIO:
//    - # sfx for sound effects
//    - # song for music with fade in/out
//    - Looping ambient sounds
//
// 5. WORLD CONTROL:
//    - # time set for day/night
//    - # weather for atmosphere
//
// 6. UI CUSTOMIZATION:
//    - # dialog scale/text_color/width
//
// 7. SAVE POINTS:
//    - # save at critical moments
//
// 8. TIMING:
//    - # wait for pacing
//
// 9. ERROR EXAMPLES (commented):
//    - Unknown tag (typo)
//    - Missing argument
//    - Invalid argument type
//    - Invalid argument value
//
// For camera and cutscene features, set them up in the
// NarrativeCraft editor first, then uncomment the related tags.
//
// See docs/TAG_REFERENCE.md for complete tag documentation.
// ============================================================
