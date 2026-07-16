# PRD - NumPairs 🎓 v6 Guided First Run

> Active product reference for the v6 milestone. The implemented product entering this milestone is the completed v5 baseline.

## Product Summary

NumPairs is a native Android arithmetic deduction puzzle. Players complete hidden board expressions using numbers from an ordered strip. Each strip pair produces one addition tile and one multiplication tile, and the puzzle is solved by recovering hidden strip values, expressions, and pair relationships.

v3 introduced authored Tutorial content, guided interactions, rules help, and optional solving tips. v5 expanded replayable play to the generated `4 Pairs Low` and `8 Pairs Medium` modes.

v6 turns the essential learning path into a mandatory guided first-run experience. New players should practice the minimum required interactions and then demonstrate basic understanding in a small authored validation puzzle before the normal menu is unlocked. Broader explanations, strategies, and solving tips remain optional.

---

## Product Goal

Ensure that a first-time player understands the minimum NumPairs interaction model and pair relationship before entering normal play.

The first-run experience should teach through real puzzle actions, require a small independent demonstration of understanding, and remain short enough that experienced players can reach validation without completing all optional instruction.

---

## Problem Statement

The v5 application provides several learning surfaces, but all of them are optional. A fresh launch opens the normal menu, where a player can enter a generated puzzle without first learning how strip numbers, tile expressions, and complementary sum/product pairs work.

This creates several onboarding risks:

- new players can reach generated play before understanding the objective
- the existing Tutorial entry depends on the player choosing to learn first
- acknowledging that the player already knows the rules would not prove that they understand the interaction model
- Tutorial completion is not a persisted prerequisite for normal play
- forcing the complete existing learning experience would also burden experienced players with optional strategy content
- interrupted first-run progress has no product-level resume contract

v6 should address these risks by requiring a concise learning core, allowing an early route to practical validation, and persisting completion locally.

---

## Target Users

- First-time players who have not previously seen or played NumPairs
- Casual puzzle players who benefit from learning one mechanic at a time
- Players unfamiliar with the relationship between each pair's sum and product
- Experienced puzzle players who can demonstrate understanding without completing the full guided path
- Returning players who want to replay the introduction or optional learning content

---

## Current Baseline At Start Of v6

The product entering v6 is the implemented `v5 - Bigger Challenges with 8 Pairs` baseline.

That baseline includes:

- a branded Android launch experience with splash support
- a `Splash -> Menu` startup flow
- menu entry points for Tutorial, generated `4 Pairs Low`, and generated `8 Pairs Medium`
- an authored `Learn basics` Tutorial using real strip and tile interactions
- guided Tutorial steps with focused highlights and restricted interactions
- automatic Tutorial progression after the required action is completed
- a small authored two-pair Tutorial composition
- static rules help for core game rules
- optional `Solving tips` and authored `Practice tips` content for `4 Pairs Low`
- generated and validated `4 Pairs Low` and `8 Pairs Medium` puzzles
- shared gameplay interactions for entering strip values and completing tile expressions
- local preferences used by existing discovery behavior
- a fixed NumPairs visual system shared across menu, Tutorial, and gameplay surfaces

The baseline does not yet include:

- first-launch routing into a mandatory learning experience
- a persisted onboarding completion requirement
- versioned onboarding state
- resumption from a completed onboarding checkpoint
- an `I know how to play` route backed by practical validation
- a dedicated authored final validation puzzle
- a product distinction between mandatory first-run learning and voluntary Tutorial replay
- an explicit upgrade policy for installations that predate v6

---

## Product Principles

- Teach through real player actions rather than passive explanations
- Require the essential learning core, not all available learning content
- Validate understanding through play rather than self-declaration
- Reveal only the information and available actions relevant to the current stage
- Preserve normal gameplay controls and visual language wherever possible
- Keep the first-run path short and focused
- Keep basic onboarding, static rules help, and advanced solving guidance distinct
- Persist only the onboarding state needed to provide a reliable first-run contract
- Do not change generated puzzle rules or difficulty profiles to support onboarding
- Treat future onboarding revisions as explicit product decisions rather than silently replaying them for every user

---

## Core UX Expectations

- A fresh installation enters guided onboarding before the normal menu
- The player performs the same operand and operator interactions used in normal gameplay
- The first two learning stages cannot be skipped
- Guidance advances only after the required action is completed correctly
- `I know how to play` becomes available only after the player completes a full sum/product pair
- Choosing the early exit leads to validation and does not complete onboarding by itself
- The final validation uses a small authored `2 Pairs` puzzle without direct step guidance
- Only solving the final validation marks onboarding as completed
- Restarting the application cannot bypass required onboarding
- Completed players reach the normal menu on subsequent launches
- Completed players can voluntarily replay basic onboarding and optional advanced learning
- Replaying learning content does not relock normal play

---

## v6 Scope

### Guided First-Run Flow

The first-run experience should contain four focused authored stages.

#### Stage 1 - Place A Number

- Use a minimal authored puzzle composition focused on one operand placement
- Make only the number required for the lesson selectable
- Highlight the relevant number first and the destination operand slot when appropriate
- Require the player to perform the normal operand-selection interaction
- Advance only after the correct number has been placed
- Keep arithmetic and explanatory copy minimal so the interaction itself is the lesson

#### Stage 2 - Complete A Complementary Pair

- Use an authored pair with an obvious addition and multiplication result
- Guide the player through completing one expression
- Show that the same two strip entries must also form the complementary expression with the other operator
- Require the player to complete both the addition and multiplication tiles
- Treat the pair as complete only when both expressions use the intended strip entries and operators
- Make `I know how to play` available only after this stage is completed

#### Stage 3 - Deduce A Hidden Strip Value

- Use a small authored scenario with one hidden strip value that is easy to infer
- Connect the visible result, its expression, and the ordered strip clearly
- Require the player to enter the hidden strip value through the normal strip interaction
- Reinforce that solving expressions and completing the strip are connected parts of the same puzzle
- Keep this stage on the full guided path but allow experienced players to bypass it through the validated early exit

#### Stage 4 - Complete Final Validation

- Use a small authored `2 Pairs` puzzle
- Allow the standard interaction model without direct target highlighting or step-by-step instructions
- Keep arithmetic simple and avoid advanced ambiguity or strategy requirements
- Require the player to solve the complete puzzle
- Use the solved state as the only successful onboarding completion condition

### Required Core And Early Exit

- Stages 1 and 2 form the mandatory guided core
- The player cannot skip or dismiss the mandatory core to reach the normal menu
- `I know how to play` appears only after the player has correctly completed the complementary pair in Stage 2
- Selecting `I know how to play` skips Stage 3 and starts Stage 4
- Selecting the early exit does not write onboarding completion
- A player who does not select the early exit continues through Stage 3 before entering Stage 4
- Both paths converge on the same final validation and completion rule
- Back navigation or application restart must not provide an alternative route to the normal menu while onboarding is incomplete

### Final Validation Puzzle

- Author the validation puzzle specifically for learning confirmation rather than generating it dynamically
- Use exactly two strip pairs and four board tiles
- Include enough unknown information to exercise normal strip and expression interactions
- Avoid direct instructional highlights during normal validation progress
- Do not reveal the intended pairings, hidden values, or next move
- Preserve ordinary reset, correction, validation, and completion behavior where applicable
- Mark onboarding complete only after the puzzle reaches the solved state

### Local Onboarding State

- Persist onboarding state locally on the device
- Store completion as a versioned value rather than an unversioned Boolean
- Record the last fully completed onboarding stage as a resumable checkpoint
- Resume at the next required stage after application restart
- Restart the current stage if the application closes before that stage is completed
- Do not require persistence of partial strip or tile edits within an incomplete stage
- Keep onboarding state independent from Tutorial replay state and generated puzzle sessions
- Route users whose required onboarding version is complete directly to the normal menu
- Do not clear completed onboarding when the player voluntarily replays learning content
- Treat uninstalling or clearing application data as a new local first run

Upgrade behavior for v6:

- fresh installations start with onboarding version 1 incomplete
- installations identified as upgrades from a pre-v6 version initialize onboarding version 1 as completed
- upgraded players retain immediate menu access and can enter the guided introduction voluntarily
- future onboarding versions must define their own migration policy instead of automatically invalidating prior completion

The exact platform mechanism used to distinguish a fresh installation from an upgrade is an implementation decision, but it must satisfy this product behavior reliably.

### Optional Learning And Replay

- Add or refine a `How to play` entry point in the normal menu
- Let completed players start the guided introduction voluntarily from its beginning
- Let voluntary replay use the same authored stages and interactions as first-run onboarding
- Keep the menu unlocked before, during, and after voluntary replay
- Preserve `Solving tips` and `Practice tips` as optional advanced learning content
- Keep the static rules helper available as an on-demand rules reference in supported gameplay modes
- Keep basic guided onboarding, advanced strategy practice, and rules reference content as distinct product surfaces

---

## Suggested Implementation Phases

1. Finalize the authored scenarios and completion conditions for all four stages.
2. Introduce versioned local onboarding state and the pre-v6 upgrade policy.
3. Route fresh installations into onboarding and completed installations into the menu.
4. Implement the mandatory guided core and resumable stage progression.
5. Implement `I know how to play` and both paths into final validation.
6. Implement the authored `2 Pairs` validation puzzle and completion persistence.
7. Add `How to play` and voluntary replay without changing the completed state.
8. Validate the complete first-run, restart, upgrade, and replay experience in supported languages.

---

## Out Of Scope

- Contextual help triggered by repeated errors or inactivity
- Proactive action discovery intended to show Help or Hint actions
- Solver-backed hints or next-move suggestions
- Puzzle-specific answer reveal
- Automatic strip entry, operand placement, or puzzle completion
- Adaptive onboarding or difficulty based on player behavior
- New generated puzzle modes, sizes, or difficulty profiles
- Changes to core NumPairs rules or operators
- Changes to generated `4 Pairs Low` or `8 Pairs Medium` construction
- Persistence of partial edits inside an incomplete onboarding stage
- Persistence of generated puzzle sessions
- A full Settings screen created only to reset onboarding
- Requiring pre-v6 players to complete onboarding after upgrading
- Reworking all existing advanced Tutorial or solving-tips content
- Scoring, timers, streaks, achievements, or progression systems
- User accounts, cloud synchronization, analytics, or remote onboarding configuration
- Advanced tutorial animations or character-led instruction

---

## Success Criteria

- A fresh installation starts with guided onboarding instead of the normal menu
- The player physically completes the essential operand-placement and complementary-pair interactions
- The first two learning stages cannot be skipped
- `I know how to play` is unavailable until the mandatory complementary pair is complete
- The early-exit path skips optional guidance without bypassing final validation
- Onboarding is marked complete only after the final authored `2 Pairs` puzzle is solved
- Completed stages and final completion persist reliably across application restarts
- An interrupted player resumes without repeating fully completed required stages
- Completed players reach the normal menu on subsequent launches
- Existing players upgrading from a pre-v6 version retain menu access
- Players can voluntarily replay basic onboarding and access optional advanced learning
- Voluntary replay does not relock the menu or clear prior completion
- Generated `4 Pairs Low` and `8 Pairs Medium` behavior remains unchanged
- A first-time tester can complete their first generated `4 Pairs Low` puzzle without verbal explanation
