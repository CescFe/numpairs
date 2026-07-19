# PRD - NumPairs 🎓 v6 Guided First Run

> Active product reference for the Guided First Run milestone. This revision replaces the original mandatory-validation contract while preserving the implemented v5 baseline and versioned local first-run boundary.

## Product Summary

NumPairs is a native Android arithmetic deduction puzzle. Players complete hidden board expressions using numbers from an ordered strip. Each strip pair produces one addition tile and one multiplication tile, and the puzzle is solved by recovering hidden strip values, expressions, and pair relationships.

v3 introduced authored Tutorial content, guided interactions, rules help, and optional solving tips. v5 expanded replayable play to generated `4 Pairs Low` and `8 Pairs Medium` modes.

Guided First Run makes Tutorial the recommended route before the normal menu on a fresh installation. Its purpose is focused: reduce the risk that a new player accidentally starts generated play without learning the interface, familiarize the player with the strip and tiles, and teach the basic pair rules. Tutorial completion is not a certification gate; a player may explicitly skip after acknowledging the recommendation.

---

## Product Goal

Give first-time players a concise, action-led introduction to the strip, tiles, and sum/product pair rule before they choose a generated mode, while preserving an explicit and informed route to skip Tutorial.

---

## Problem Statement

Opening the normal menu on a fresh installation lets an inexperienced player enter generated play before understanding the puzzle surface or rules. Conversely, forcing every player to complete a validation puzzle treats onboarding as certification and creates unnecessary friction for experienced players.

Guided First Run should therefore:

- present Tutorial before Menu by default on a fresh installation
- teach the minimum interface and rules through real actions
- make the recommendation clear without making completion mandatory
- require explicit confirmation before an incomplete Tutorial is skipped
- keep the same Tutorial safely replayable after Menu is unlocked

---

## Target Users

- First-time players who have not previously seen or played NumPairs
- Casual puzzle players who benefit from learning one mechanic at a time
- Players unfamiliar with the relationship between each pair's sum and product
- Experienced players who want to reach Menu without completing introductory practice
- Returning players who want to replay the basic Tutorial

---

## Product Principles

- Teach through real player actions rather than passive explanation
- Use Tutorial to introduce the product, not to certify the player
- Recommend learning without removing informed player choice
- Introduce the strip before adding tile complexity
- Keep copy concise and tied to the current authored puzzle state
- Preserve normal gameplay controls and visual language wherever possible
- Keep basic Tutorial, static rules help, and advanced solving guidance distinct
- Persist only the state needed for a reliable local first-run contract
- Do not change generated puzzle rules or difficulty profiles to support Tutorial

---

## Core UX Expectations

- A fresh installation routes from Splash into Tutorial before Menu
- A low-emphasis `Skip tutorial` action is available from the first step and remains available throughout required first-run playback
- Requesting skip opens a confirmation dialog instead of unlocking Menu immediately
- The dialog recommends continuing Tutorial and offers an explicit `Skip anyway` alternative
- Confirming skip opens Menu without a final check
- Completing the three Tutorial steps opens Menu without a separate validation stage
- Closing or restarting the application cannot expose Menu while first-run Tutorial remains unresolved
- Completed, skipped, upgraded, and legacy-completed players reach Menu on later launches
- Completed or skipped players can voluntarily replay the same Tutorial from its beginning
- Voluntary replay never relocks Menu or changes the stored first-run outcome

---

## Guided First-Run Flow

### Entry

The fresh-install route is:

```text
Splash -> Tutorial -> Menu
```

Menu is reached when the player either completes Step 3 or confirms `Skip anyway`. There is no separate final-validation route.

### Skip Confirmation

Every required Tutorial step exposes one visually low-emphasis `Skip tutorial` action. It should remain in a stable location and preserve an accessible touch target.

Requesting skip opens a confirmation dialog with this content intent:

- title: ask whether the player wants to skip Tutorial
- message: recommend Tutorial for a first game, summarize that it teaches the strip, tiles, and basic rules, and state that it remains available later from `How to play`
- recommended primary action: `Continue tutorial`
- explicit lower-emphasis action: `Skip anyway`

Dismissing the dialog or selecting `Continue tutorial` preserves the current step and puzzle state. Selecting `Skip anyway` persists the skipped first-run outcome and opens Menu immediately.

---

## Tutorial Content

Learn basics contains exactly three authored steps.

### Step 1 - Introduce The Strip

Show the strip without board tiles:

```text
2, ?, 4, 5
```

Explain that the strip contains positive integers ordered from lowest to highest and that repeated values are allowed. Avoid describing the strip as strictly increasing.

Required action:

- tap the hidden strip entry
- enter `3`

While the entry editor is open, replace generic range guidance with concise Tutorial guidance asking the player to enter `3`. If the player provides invalid input, feedback must remain truthful about the actual validation problem. Tutorial copy must not imply that `3` is the only value allowed by the general ascending-order rule.

### Step 2 - Introduce Tiles And Pairs

Preserve the visually completed strip:

```text
2, 3, 4, 5
```

Reveal four tiles in this authored state:

```text
? ? ? = 5
2 × 3 = 6
4 + 5 = 9
4 × 5 = 20
```

Explain that a tile has two operands, one operator, and a visible result. Explain that strip entries form pairs and that each pair creates one addition result and one multiplication result.

Required action:

- complete the unresolved tile as `2 + 3 = 5` using the normal operand and operator interactions

The resolved `2 × 3 = 6` tile acts as the complementary example for the same pair.

### Step 3 - Solve A Two-Pair Puzzle

Start a clean authored two-pair puzzle with this strip:

```text
?, ?, 2, 3
```

The completed strip is:

```text
1, 2, 2, 3
```

Show unresolved tiles with results in this order:

```text
3, 2, 5, 6
```

These results correspond to one addition and one multiplication for the `1`/`2` pair and one addition and one multiplication for the other `2`/`3` pair.

Explain that the player should solve the complete puzzle and remind them that the strip may contain repeated values. Allow normal strip, operand, operator, correction, reset, and validation behavior.

The two strip entries that display `2` remain distinct stable entries. Because exchanging those equal-valued entries produces an equivalent player-visible solution, Tutorial completion must accept either valid stable-entry assignment as long as all shared puzzle rules are satisfied.

Solving the puzzle completes Tutorial. Required first-run playback persists the completed outcome and opens Menu; voluntary playback shows ordinary Tutorial completion without changing onboarding state.

---

## Local First-Run State

- Persist state locally on the device
- Keep completion versioned so future revisions define an explicit migration policy
- Distinguish unresolved, Tutorial-completed, and Tutorial-skipped outcomes
- Preserve immediate Menu access for installations identified as pre-v6 upgrades
- Preserve Menu access for installations that already completed the legacy required onboarding contract
- Record the last fully completed Tutorial step as a resumable checkpoint
- Resume at the next step after application restart
- Restart the current step if the application closes before that step is completed
- Do not persist partial strip or tile edits within an incomplete step
- Keep onboarding state independent from Tutorial replay state and generated sessions
- Treat uninstalling or clearing application data as a new local first run

Future onboarding versions must define their own migration policy instead of automatically invalidating prior resolution.

---

## Optional Learning And Replay

- Keep `How to play` available from the unlocked normal menu
- Start voluntary replay from Step 1 regardless of stored first-run checkpoints
- Use the same three authored Tutorial steps for first run, Menu replay, and the in-game `Play tutorial` entry
- Allow voluntary playback to return to the already-unlocked Menu or underlying generated puzzle at any time
- Never clear, downgrade, or otherwise change a resolved first-run outcome during replay
- Preserve Solving Tips Practice as separate advanced learning content
- Keep the static rules helper as an on-demand rules reference in supported generated modes

---

## Out Of Scope

- A mandatory final check or certification puzzle
- Adaptive onboarding based on player behavior
- Contextual help triggered by repeated errors or inactivity
- Solver-backed hints or next-move suggestions
- Puzzle-specific answer reveal
- Automatic strip entry, operand placement, or puzzle completion
- New generated puzzle modes, sizes, or difficulty profiles
- Changes to core NumPairs rules or operators
- Changes to generated puzzle construction
- Persistence of partial edits inside an incomplete Tutorial step
- User accounts, cloud synchronization, analytics, or remote onboarding configuration
- Scoring, timers, streaks, achievements, or progression systems
- Advanced Tutorial animations or character-led instruction

---

## Success Criteria

- A fresh installation starts Tutorial instead of Menu
- A new player encounters the strip before tile interaction is introduced
- Tutorial teaches strip ordering, repeated values, tile anatomy, and the one-sum/one-product pair rule through real actions
- `Skip tutorial` is available from Step 1 and always requires explicit confirmation
- Confirmed skip opens Menu without final validation
- Solving the Step 3 authored puzzle completes Tutorial and opens Menu
- Completion and skip outcomes persist reliably across application restarts
- Interrupted unresolved playback resumes after completed steps
- Existing completed and upgraded players retain Menu access
- Voluntary replay uses the same Tutorial without relocking Menu or changing first-run state
- Generated mode behavior remains unchanged
