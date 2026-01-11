// ============================================================
// NarrativeCraft Tutorial 01: Your First Dialog
// ============================================================
//
// This tutorial covers:
// - Basic scene structure
// - Dialog formatting (Character: Text)
// - The # on enter tag
// - Using # fade for transitions
// - Using # save to save progress
//
// To use this example:
// 1. Create a new scene in NarrativeCraft
// 2. Copy this content into your scene's .ink file
// 3. Build and play the story
// ============================================================

=== intro ===

// Every scene MUST start with # on enter
// This tells NarrativeCraft this is the beginning of the scene
# on enter

// Fade in from black (1 second fade, 0 stay, 0 fade out)
# fade 1.0 0 0

// ============================================================
// DIALOG BASICS
// ============================================================
//
// Dialog format: CharacterName: What they say.
// The character name appears as a speaker label in-game.

Narrator: Welcome to your first NarrativeCraft story!

Narrator: In this tutorial, you'll learn the basics of creating interactive narratives in Minecraft.

// ============================================================
// CHARACTER DIALOG
// ============================================================
//
// You can have multiple characters speak in sequence.
// Just change the name before the colon.

Alice: Hello, traveler! I'm Alice.

Bob: And I'm Bob! We'll be your guides today.

Alice: NarrativeCraft uses a language called Ink for writing stories.

Bob: It's designed to make dialog easy to write - just like a screenplay!

// ============================================================
// SAVING PROGRESS
// ============================================================
//
// Use # save to save the player's progress.
// They can continue from this point if they quit.

# save

Narrator: Your progress has been saved.

// ============================================================
// FADE TRANSITIONS
// ============================================================
//
// # fade <fadeIn> <stay> <fadeOut> [color]
// - fadeIn: Time to fade TO the color
// - stay: Time to stay at the color
// - fadeOut: Time to fade FROM the color
// - color: Optional hex color (default: black)

Narrator: Let's try a fade to end this tutorial...

// Fade out for 2 seconds, stay black for 1 second
# fade 0 1.0 2.0

Narrator: Scene complete! You've learned the basics.

-> END

// ============================================================
// WHAT YOU LEARNED
// ============================================================
//
// 1. # on enter - Required at the start of every scene
// 2. Character: Text - How to write dialog
// 3. # fade - Screen fade transitions
// 4. # save - Saving player progress
// 5. -> END - Ending the story
//
// Next: 02-choices.ink - Adding player choices
// ============================================================
