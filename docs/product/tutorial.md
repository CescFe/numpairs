# Tutorial MVP Requirements

## Document Status

- Status: v3 product/UI reference for the first authored Tutorial experience
- Applies to: Tutorial mode
- Related references:
  - `docs/product/prd/prd-v3.md`
  - `docs/product/rules-helper.md`
  - `docs/product/puzzle-generation.md`
  - `docs/game-rules.md`
  - `docs/ui-behavior.md`
  - `docs/ubiquitous-language.md`

This document defines the first Tutorial MVP before implementation. It records what the authored tutorial should teach, how active learning should work, how it differs from the old prototype seed puzzle, and which tutorial ideas are intentionally deferred.

No tutorial implementation is included in this documentation ticket.

---

## Purpose

Tutorial should become an intentional learning path for first-time NumPairs players.

The first version should teach by having the player practice the core rules inside a controlled authored puzzle. It should not be only a renamed puzzle, a static rules page, a hint system, or a generated `4 Pairs` puzzle with special labeling.

Rules helper and Tutorial have different jobs:

- Rules helper explains the rules on demand while a player is already in a puzzle.
- Tutorial turns the same rules into a short guided practice experience.

---

## Source Of Truth

`docs/game-rules.md` remains the source of truth for the actual puzzle rules.

Tutorial documentation defines the authored learning experience around those rules. It does not redefine arithmetic rules, validation behavior, operand usage, or generated puzzle construction.

Documentation boundaries:

- `docs/product/prd/prd-v3.md`: defines why Tutorial belongs in v3 and what success means.
- `docs/product/tutorial.md`: defines Tutorial MVP scope, learning goals, guided steps, and future tutorial ideas.
- `docs/product/rules-helper.md`: defines the static gameplay rules helper, not tutorial progression.
- `docs/product/puzzle-generation.md`: defines generated `4 Pairs` content, not tutorial authorship.
- `docs/ui-behavior.md`: defines normal in-puzzle interaction behavior shared by gameplay modes.
- `docs/game-rules.md`: defines the core rules that Tutorial must teach.

---

## Learning Goals

The Tutorial MVP should prepare a first-time player to start generated `4 Pairs` with the core mental model in place.

The player should understand that:

- The objective is to guess all unknown elements.
- The strip contains the numbers available to solve the puzzle.
- Some strip values are hidden and must still fit an ascending list of positive integers.
- The grid contains tiles with visible results and hidden expressions.
- Each tile expression is completed with two operands and one operator.
- Operands come from visible strip entries.
- A pair of strip entries creates two related grid results: one sum and one product.
- Completing the puzzle requires solving both the hidden strip values and the hidden grid expressions.

The MVP should teach these concepts through guided interaction, not through a long text explanation.

---

## MVP Summary

The first Tutorial should be a single authored puzzle with a short guided walkthrough.

Expected shape:

- one fixed puzzle authored for teaching clarity
- normal game screen, strip, grid, operand selection, operator selection, and validation wherever possible
- lightweight guidance that highlights relevant puzzle elements and presents short copy
- player actions that complete real parts of the puzzle instead of passively reading instructions
- a final unguided section where the player finishes the remaining puzzle using normal gameplay

The authored tutorial puzzle may use the same broad `4 Pairs` puzzle shape as generated mode because that is the first replayable mode players need to learn. It is still tutorial content, not generated content.

Exact puzzle values can be finalized during implementation. The values should be selected for teaching clarity, low arithmetic load, and unambiguous examples of the rules below.

---

## Active Learning Model

Each tutorial step should combine three pieces:

- Focus: the strip entries, grid tiles, slots, or controls that matter for the current lesson.
- Copy: one concise player-facing message, preferably aligned with the existing `How to play` language.
- Action: a real player action when the concept needs practice.

Highlights should guide attention to the current lesson. They should not reveal answers by themselves.

The player should perform normal interactions where possible:

- entering a hidden strip value
- selecting operands from the strip
- choosing an operator
- completing tile expressions
- correcting incomplete or incorrect entries through existing gameplay controls

The Tutorial MVP may include non-interactive continue steps for orientation, but the core learning should come from doing the puzzle.

---

## Tutorial Step Sequence

### 1. Introduce The Objective

Focus:

- hidden strip values
- hidden tile expression slots

Copy:

- "Guess all the unknown elements."

Player action:

- continue after seeing what counts as unknown

Purpose:

- establish that both strip and grid content must be solved
- connect the tutorial to the first bullet in the current `How to play` content

### 2. Identify Screen Elements

Focus:

- strip
- grid

Copy:

- "Strip: numbers available to solve the puzzle."
- "Grid: tiles with a visible result and an unknown expression."

Player action:

- continue after each focused area, or advance through a short combined orientation step

Purpose:

- make the two main puzzle areas explicit before asking the player to solve anything

### 3. Complete A Hidden Strip Value

Focus:

- one hidden strip value
- its nearest known neighbors

Copy:

- "Guess hidden values to complete an ascending list of positive integers."

Player action:

- enter the highlighted hidden strip value

Purpose:

- teach that hidden strip values are constrained by the ordered strip
- give the player a first low-risk successful action

Content requirement:

- the authored puzzle should include at least one hidden strip value that can be deduced clearly from the surrounding visible values

### 4. Complete A First Grid Expression

Focus:

- one simple grid tile
- its visible result
- the tile's two operand slots and operator slot
- the relevant strip entries

Copy:

- "Fill each tile with two operands and one operator."

Player action:

- select the operands and operator that complete the highlighted tile

Purpose:

- teach that a visible grid result is solved by reconstructing its hidden expression
- let the player use the same controls that generated `4 Pairs` uses

Content requirement:

- the first tile should use small, obvious arithmetic
- the expression should be simple enough that the player can focus on the interaction model

### 5. Teach The Pair Relationship

Focus:

- the same two strip entries used as a pair
- the already completed sum or product tile
- the matching complementary tile

Copy:

- "Pair strip numbers so each pair creates one sum and one product that match two grid results."

Player action:

- complete the complementary tile using the same pair and the other operator

Purpose:

- teach the core `4 Pairs` relationship that one pair creates two results
- make clear that operands are not arbitrary isolated choices per tile

Content requirement:

- the authored puzzle should include one pair whose sum and product are both easy to spot
- the guided step should make the relationship visible without solving the rest of the puzzle for the player

### 6. Finish The Puzzle Normally

Focus:

- remaining hidden strip values and unresolved grid expressions

Copy:

- a short transition such as "Now finish the remaining unknowns."

Player action:

- complete the rest of the puzzle with normal gameplay interactions

Purpose:

- move from guided practice into independent solving
- confirm that the player can apply the rules without every move being narrated

Content requirement:

- remaining puzzle work should be small enough for a first tutorial
- the puzzle should avoid advanced ambiguity, large arithmetic, or distracting edge cases

---

## Authored Puzzle Requirements

The Tutorial MVP should use a deliberately authored puzzle instead of the old prototype seed.

The authored puzzle should:

- support every tutorial step above
- include hidden strip values and hidden tile expressions
- use small positive integers
- keep arithmetic easy enough for first-time players
- include at least one clear strip-order deduction
- include at least one clear pair where the same two strip entries produce one sum tile and one product tile
- leave a small amount of remaining work for the player to solve independently
- be internally valid under the core game rules

The authored puzzle does not need to follow the generated `4 Pairs` masking profile exactly. For example, generated `4 Pairs` currently has exactly 3 known strip entries and 5 hidden strip entries, but Tutorial may choose a different reveal pattern if that better supports the learning path.

---

## Difference From The Prototype Seed Puzzle

The old Tutorial seed puzzle originated as a development prototype. It should not define the product meaning of Tutorial.

The Tutorial MVP differs from that prototype by being:

- authored around explicit learning goals
- structured as a guided sequence rather than a standalone puzzle with no teaching layer
- built around short instructional copy and focused highlights
- selected for clear examples of strip completion, grid expression completion, and pair relationships
- designed to end with active independent solving
- documented as tutorial content rather than accidental seed data

The prototype seed puzzle may be useful as historical implementation context, but it should not be preserved as the default Tutorial experience unless it is intentionally re-authored to satisfy this document.

---

## Separation From Generated `4 Pairs`

Tutorial and generated `4 Pairs` share the same core rules, but they are separate product surfaces.

Tutorial should remain separate from generated `4 Pairs` in these ways:

- Tutorial content is authored by hand for learning.
- Generated `4 Pairs` content is produced by the puzzle generator for replayability.
- Tutorial should not call the generated puzzle provider to decide its teaching content.
- Tutorial should not inherit generated masking constraints unless they also serve the lesson.
- Tutorial state should remain isolated from generated mode state and generated replay identity.
- Changes to Tutorial content should not require changes to generated puzzle construction.
- Changes to generated `4 Pairs` difficulty profiles should not automatically change the Tutorial MVP.

Shared implementation can still exist where it is genuinely common, such as the game screen, core domain rules, validation behavior, and normal interaction controls.

---

## MVP Scope

In scope for the first Tutorial MVP:

- one authored tutorial puzzle
- short guided step sequence
- focused visual attention on relevant puzzle elements
- concise instructional copy tied to the current step
- active player interactions for at least strip completion, tile completion, and pair relationship practice
- normal puzzle completion after guided steps
- rules helper availability if the shared game screen already exposes it
- no changes to generated `4 Pairs` content or generation rules

Out of scope for the first Tutorial MVP:

- multiple tutorial lessons
- branching tutorial paths
- adaptive hints
- solver-backed suggestions
- answer reveal
- full onboarding framework
- user progression
- scoring
- badges, streaks, stars, or rewards
- advanced animations
- narrated walkthroughs
- step-by-step guidance for every remaining move
- in-game calculator behavior
- persistence or save-state requirements

---

## Future Tutorial Ideas

The following ideas are intentionally out of scope for the MVP. They are preserved here so they can be revisited after the first tutorial has been implemented and playtested.

Possible future iterations:

- additional worked examples for repeated values, duplicate results, or less obvious pairs
- separate lessons for strip deduction, operand selection, operator selection, and pair reasoning
- richer step-by-step guidance that reacts to incorrect player actions
- optional explanation panels that show why a pair creates both a sum and a product
- lightweight progress markers across multiple tutorial lessons
- completion feedback specific to Tutorial
- replayable tutorial variants that practice the same concept with different numbers
- accessibility-focused tutorial settings, such as slower transitions or persistent instruction text
- lightweight gamification, such as lesson completion checks or optional challenges
- onboarding copy that introduces generated `4 Pairs` after Tutorial completion

These ideas should not be treated as acceptance criteria for the first Tutorial MVP.

---

## Remaining Open Questions

- Should the first Tutorial puzzle use the same 8-entry strip and 8-tile board as generated `4 Pairs`, or may it use a smaller authored layout if the game screen supports it later?
- Should Tutorial guidance advance automatically after correct actions, or should the player explicitly tap to continue after each completed step?
- Should incorrect actions during guided steps show tutorial-specific feedback, or rely only on existing validation states for the MVP?
- Should the rules helper remain available during guided Tutorial overlays, or should only one instructional surface be open at a time?
- What exact puzzle values best support the step sequence without revealing too much too early?
