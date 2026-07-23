# PRD - NumPairs 🎓 v6 Guided First Run

> Active product reference for the Guided First Run milestone. This revision replaces the original mandatory-validation contract while preserving the implemented v5 baseline and local first-run boundary.

## Product Summary

NumPairs is a native Android arithmetic deduction puzzle. Players complete hidden board expressions using numbers from an ordered strip. Each strip pair produces one addition tile and one multiplication tile, and the puzzle is solved by recovering hidden strip values, expressions, and pair relationships.

v3 introduced authored Tutorial content, guided interactions, rules help, and optional solving tips. v5 expanded replayable play to generated `4 Pairs Low` and `8 Pairs Medium` modes.

Guided First Run makes Tutorial the recommended route before the normal menu on a fresh installation. Its purpose is focused: reduce the risk that a new player accidentally starts generated play without understanding the interface, familiarize the player with the strip and tiles, and teach the basic pair rules. Tutorial completion is not a certification gate; a player may explicitly skip after acknowledging the recommendation.

---

## Product Goal

Give first-time players a concise observe-then-practice introduction to the strip, tiles, and sum/product pair rule before they choose a generated mode, while preserving an explicit and informed route to skip Tutorial.

---

## Problem Statement

Opening the normal menu on a fresh installation lets an inexperienced player enter generated play before understanding the puzzle surface or rules. Conversely, forcing every player to complete a validation puzzle treats onboarding as certification and creates unnecessary friction for experienced players.

Guided First Run should therefore:

- present Tutorial before Menu by default on a fresh installation
- explain the minimum interface and rules through focused puzzle states
- demonstrate how those rules combine in one reasoned worked example
- let the player apply the complete mental model through normal interactions
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

- Give each explanatory step one concise concept and one matching visual focus
- Use one short worked example to connect the concepts before independent practice
- Use Tutorial to introduce the product, not to certify the player
- Recommend learning without removing informed player choice
- Show the complete puzzle surface while introducing its parts progressively
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
- Completing the independent Tutorial practice shows a completion confirmation before opening Menu
- Closing or restarting the application cannot expose Menu while first-run Tutorial remains unresolved
- Completed and skipped players reach Menu on later launches
- Completed or skipped players can voluntarily replay the same Tutorial from its beginning
- Voluntary replay never relocks Menu or changes the stored first-run outcome

---

## Guided First-Run Flow

### Entry

The fresh-install route is:

```text
Splash -> Tutorial -> Menu
```

Menu is reached when the player continues from the completed independent practice puzzle or confirms `Skip anyway`. There is no separate final-validation route.

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

Learn Basics has two parts: a non-interactive explanation with one worked example, followed by independent practice.

### Part 1 - Explain And Demonstrate

Keep the complete authored puzzle visible in this player-facing initial state throughout the explanation:

```text
strip: 2, ?, 4, 5
tiles: ? ? ? = 5
       ? ? ? = 6
       ? ? ? = 9
       ? ? ? = 20
```

Every explanatory step disables strip, operand, operator, and reset interaction. The player advances or returns manually with accessible `Back` and `Next` actions below the current copy. The controls keep stable layout and touch targets. The first explanation omits `Back` entirely while keeping `Next` in its established right-hand position.

Use one short message and one matching focus per step:

1. Show the whole puzzle without highlighting any element and explain that the objective is to discover every hidden number and symbol.
2. Highlight the complete strip surface and all its entries; explain that its positive integers are ordered from lowest to highest, may repeat, and may be hidden.
3. Highlight one whole tile and its three hidden expression slots; explain that its visible result is produced by two operands and one operator in the hidden expression.
4. Highlight strip entries `4` and `5` with result tiles `9` and `20`; explain that each pair completes two tiles, one sum and one product.

Continue with a deterministic, manually paced worked example. Its first step keeps the same unresolved state visible without highlighting any element, then five more steps apply these transitions in order:

1. `4 × 5 = 20`: the larger product is resolved first.
2. `4 + 5 = 9`: the same pair also completes its sum.
3. Reveal and exclusively highlight strip value `3`: with `2`, it produces the remaining results `5` and `6`.
4. `2 × 3 = 6`: resolve the remaining product.
5. `2 + 3 = 5`: resolve the remaining sum.

The puzzle remains non-interactive throughout all six worked-example steps. The player controls every transition with the same accessible `Back` and `Next` actions used by the preceding explanation. Moving backward restores the previous authored snapshot, and moving forward applies the next deduction. No worked-example state changes on a timer, so every player has enough time to read the reasoning and inspect the highlighted result before continuing.

### Part 2 - Solve A Two-Pair Puzzle

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

Tell the player that it is their turn, that tapping any unknown starts an interaction, and that strip values may repeat. Present multiplication as a useful starting heuristic rather than a universal rule. Initially focus the known `2` and `3` entries with result tile `6` without restricting the player's first action; remove that cue after the player commits the first puzzle change.

Allow normal strip, operand, operator, correction, reset, and validation behavior from the beginning.

Show an accessible `Back` action and no `Next` action during independent practice. `Back` returns to the final worked-example step so the player can review any previous explanation. Returning to practice restores committed in-session puzzle progress and keeps its initial cue dismissed if the player had already changed the puzzle. Revisiting the final worked-example boundary does not record its checkpoint again.

The two strip entries that display `2` remain distinct stable entries. Because exchanging those equal-valued entries produces an equivalent player-visible solution, Tutorial completion must accept either valid stable-entry assignment as long as all shared puzzle rules are satisfied.

Solving the puzzle shows a dedicated Tutorial success overlay with:

- title: `Tutorial completed!`
- message: `You now know the essentials for playing NumPairs`
- one primary action: `Continue`

The overlay reuses the established success visual role without offering the generated-puzzle actions to play another puzzle or return to Menu. Tapping its scrim does not dismiss it.

During required first-run playback, `Continue` persists the completed outcome and opens Menu. During voluntary playback, it leaves onboarding state unchanged and returns to the playback origin: Menu for `How to play`, or the unchanged in-progress `4 Pairs` puzzle for the in-game rules-helper entry.

---

## Local First-Run State

- Persist state locally on the device
- Distinguish unresolved, Tutorial-completed, and Tutorial-skipped outcomes
- Treat absent local state as an unresolved first run
- Preserve Menu access after Tutorial completion or explicit skip
- Record stable, meaning-based Tutorial checkpoints rather than list positions
- Record the explanation boundary only after the worked example completes
- Resume independent practice after a completed worked example
- Restart the current explanation or practice boundary if the application closes before that boundary is completed
- Do not persist partial strip or tile edits within an incomplete step
- Keep onboarding state independent from Tutorial replay state and generated sessions
- Treat uninstalling or clearing all application data as a new local first run
- Do not reset onboarding when only the application cache is cleared

---

## Optional Learning And Replay

- Keep `How to play` available from the unlocked normal menu
- Start voluntary replay from Step 1 regardless of stored first-run checkpoints
- Use the same two-part Learn Basics Tutorial for first run, Menu replay, and the in-game `Play tutorial` entry
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
- Automatic answer reveal or completion outside the authored non-interactive worked example
- New generated puzzle modes, sizes, or difficulty profiles
- Changes to core NumPairs rules or operators
- Changes to generated puzzle construction
- Persistence of partial edits inside an incomplete Tutorial step
- User accounts, cloud synchronization, analytics, or remote onboarding configuration
- Scoring, timers, streaks, achievements, or progression systems
- Character-led instruction, narration, or decorative advanced Tutorial animation

---

## Success Criteria

- A fresh installation starts Tutorial instead of Menu
- A new player sees the complete puzzle while focused steps explain the objective, strip, tile anatomy, and one-sum/one-product pair rule
- Each worked-example step keeps its deduction and resulting puzzle state visible until the player manually continues
- Independent practice teaches the real interaction model through unrestricted player actions
- `Skip tutorial` is available from Step 1 and always requires explicit confirmation
- Confirmed skip opens Menu without final validation
- Solving the authored repeated-value practice puzzle shows the Tutorial completion overlay, and `Continue` opens Menu
- Completion and skip outcomes persist reliably across application restarts
- Interrupted unresolved playback resumes after completed steps
- Completed and skipped players retain Menu access
- Voluntary replay uses the same Tutorial without relocking Menu or changing first-run state
- Generated mode behavior remains unchanged
