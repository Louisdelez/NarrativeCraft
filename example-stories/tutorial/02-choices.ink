// ============================================================
// NarrativeCraft Tutorial 02: Player Choices
// ============================================================
//
// This tutorial covers:
// - Creating player choices
// - Branching narratives
// - Variables and state
// - Conditional content
// - Knots and stitches (story organization)
//
// Prerequisites: Complete 01-first-dialog.ink first
// ============================================================

// ============================================================
// VARIABLES
// ============================================================
//
// Variables store information that changes during the story.
// Declare them at the top of your file.

VAR player_mood = "neutral"
VAR met_alice = false
VAR gold = 100

=== intro ===
# on enter
# fade 1.0 0 0

Narrator: Welcome to the village square!

-> village_square

// ============================================================
// CHOICES
// ============================================================
//
// Choices let the player decide what happens next.
// Use * to create a choice. Text in [] is the button label.

=== village_square ===

The village is bustling with activity. Where would you like to go?

// Each * creates a choice option
// [Text in brackets] = what shows on the button
// Text after brackets = what happens when chosen

* [Talk to Alice at the well]
    ~ met_alice = true
    You approach Alice at the well.
    Alice: Oh, hello there! Nice to see a new face.
    -> alice_conversation

* [Visit the market]
    You head toward the busy market stalls.
    -> market

* [Just look around]
    You decide to observe the village for a moment.
    Narrator: The cobblestone streets wind between cozy homes.
    -> village_square

// ============================================================
// KNOTS AND STITCHES
// ============================================================
//
// Knots (=== name ===) are major story sections.
// Use -> to jump between knots.

=== alice_conversation ===

Alice: What brings you to our village?

* [I'm just passing through]
    ~ player_mood = "distant"
    Alice: I see. Well, travelers are always welcome here.
    -> alice_farewell

* [I'm looking for adventure!]
    ~ player_mood = "excited"
    Alice: Oh how wonderful! There's plenty of that around here.
    Alice: Have you heard about the old cave to the north?
    -> cave_hint

* [I need to earn some gold]
    ~ player_mood = "practical"
    Alice: Ah, a practical sort! The market is your best bet.
    Alice: Or... I might have a small job for you.
    -> alice_job

// ============================================================
// CONDITIONAL CONTENT
// ============================================================
//
// Use { } to show content based on conditions.
// This creates dynamic, personalized narratives.

=== alice_farewell ===

// Content changes based on variables
{ player_mood == "distant":
    Alice: Safe travels, stranger.
- else:
    Alice: Come back anytime!
}

-> village_square

=== cave_hint ===

Alice: Legend says there's treasure in that cave...
Alice: But also something dangerous. Be careful!

# save

* [Tell me more about the danger]
    Alice: Some say it's a creature. Others say it's cursed.
    Alice: I'd bring a weapon if I were you.
    -> alice_farewell

* [Thanks for the tip!]
    ~ player_mood = "brave"
    Alice: Good luck, adventurer!
    -> alice_farewell

=== alice_job ===

Alice: I need someone to deliver this letter to the blacksmith.
Alice: It's not far - just across the square.

* [I'll do it for 10 gold]
    Alice: Deal! Here's the letter.
    ~ gold = gold + 10
    Narrator: You received 10 gold! (Total: {gold})
    -> delivery_quest

* [I'll do it for free]
    ~ player_mood = "kind"
    Alice: How generous! Thank you so much.
    -> delivery_quest

* [No thanks, I'm busy]
    Alice: No worries. Maybe another time.
    -> alice_farewell

// ============================================================
// USING VARIABLES IN DIALOG
// ============================================================
//
// Use {variable_name} to insert variable values.

=== delivery_quest ===

Narrator: You now have {gold} gold.

// Check if we've met Alice before showing certain options
{ met_alice:
    Narrator: Alice waves as you head to the blacksmith.
}

-> market

=== market ===

The market is full of merchants calling out their wares.

* [Buy an apple (5 gold)]
    { gold >= 5:
        ~ gold = gold - 5
        Narrator: You bought a delicious apple. (Gold: {gold})
    - else:
        Merchant: Sorry, you don't have enough gold!
    }
    -> market

* [Return to the square]
    -> village_square

* [End the tutorial]
    -> tutorial_complete

=== tutorial_complete ===

# save

Narrator: Congratulations! You've learned about player choices.

// Recap based on what the player did
{ met_alice:
    Narrator: You met Alice and {player_mood == "kind": helped her generously|chatted with her}.
}

Narrator: Your final gold: {gold}

# fade 0 1.0 2.0

-> END

// ============================================================
// WHAT YOU LEARNED
// ============================================================
//
// 1. * [Choice text] - Creating choices
// 2. VAR name = value - Declaring variables
// 3. ~ variable = value - Changing variables
// 4. {variable} - Displaying variables
// 5. { condition: ... } - Conditional content
// 6. === name === - Knots (story sections)
// 7. -> knot_name - Jumping between sections
//
// Next: 03-cutscene.ink - Adding visual effects
// ============================================================
