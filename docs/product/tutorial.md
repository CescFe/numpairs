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

The first version should teach by having the player practice the core rules inside controlled authored puzzle scenarios. It should not be only a renamed puzzle, a static rules page, a hint system, or a generated `4 Pairs` puzzle with special labeling.

Rules helper and Tutorial have different jobs:

- Rules helper explains the rules on demand while a player is already in a puzzle.
- Tutorial turns the same rules into a short guided practice experience.

---

## Source Of Truth

`docs/game-rules.md` remains the source of truth for the actual puzzle rules.

Tutorial documentation defines the authored learning experience around those rules. It does not redefine arithmetic rules, validation behavior, operand usage, or generated puzzle construction.

Tutorial-specific domain rules may be added only when they are needed for custom tutorial puzzle compositions. Those rules should be explicitly scoped to Tutorial and must not leak into generated `4 Pairs` behavior.

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

The first Tutorial should be a short guided sequence of authored puzzle scenarios.

Expected shape:

- a small set of fixed tutorial puzzle scenarios authored for teaching clarity
- custom puzzle compositions that can be smaller than generated `4 Pairs`
- normal game screen, strip, grid, operand selection, operator selection, and validation wherever possible
- lightweight guidance that highlights relevant puzzle elements and presents short copy
- player actions that complete real parts of the puzzle instead of passively reading instructions
- manual step navigation so the player can move forward and backward through the tutorial
- guided action steps where the player can perform only the action currently being taught
- a final easy `4 Pairs` puzzle where the player finishes the remaining puzzle using normal gameplay

The tutorial may use the same broad `4 Pairs` puzzle shape where that helps prepare the player for generated mode. It may also use smaller custom compositions, such as one-pair or two-pair tutorial scenarios, when those compositions teach a concept more clearly.

Exact puzzle values can be finalized during implementation. The values should be selected for teaching clarity, low arithmetic load, and unambiguous examples of the rules below.

---

## Active Learning Model

Each tutorial step should combine three pieces:

- Focus: the strip entries, grid tiles, slots, or controls that matter for the current lesson.
- Copy: one concise player-facing message, preferably aligned with the existing `How to play` language.
- Action: a real player action when the concept needs practice.

Highlights should guide attention to the current lesson. They should not reveal answers by themselves.

The MVP should use manual step navigation. Correct actions should not force an automatic advance; the player should be able to review previous steps and move forward when ready.

During guided action steps, the tutorial should expose only the action required by the current step. This avoids needing tutorial-specific error feedback for unrelated actions in the first implementation.

The player should perform normal interactions where possible:

- entering a hidden strip value
- selecting operands from the strip
- choosing an operator
- completing tile expressions
- finishing the final easy puzzle through normal gameplay controls

The Tutorial MVP may include non-interactive continue steps for orientation, but the core learning should come from doing the puzzle.

---

## Tutorial Step Sequence

### 1. Introduce The Objective

Recommended puzzle scenario:

- one-pair orientation scenario
- 2-entry strip with one known value and one hidden value
- two grid tiles representing the pair's sum and product

Focus:

- hidden strip values
- hidden tile expression slots

Copy:

- "Guess all the unknown elements."

Player action:

- manually continue after seeing what counts as unknown

Purpose:

- establish that both strip and grid content must be solved
- connect the tutorial to the first bullet in the current `How to play` content

### 2. Identify Screen Elements

Recommended puzzle scenario:

- same one-pair orientation scenario as step 1

Focus:

- strip
- grid

Copy:

- "Strip: numbers available to solve the puzzle."
- "Grid: tiles with a visible result and an unknown expression."

Player action:

- manually move forward or backward after each focused area

Purpose:

- make the two main puzzle areas explicit before asking the player to solve anything

### 3. Complete A Hidden Strip Value

Recommended puzzle scenario:

- small two-pair tutorial scenario
- at least one hidden strip value that is easy to infer from the visible order

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

Recommended puzzle scenario:

- small two-pair tutorial scenario
- one highlighted tile with obvious arithmetic

Focus:

- one simple grid tile
- its visible result
- the tile's two operand slots and operator slot
- the relevant strip entries

Copy:

- "Fill each tile with two operands and one operator."

Player action:

- select the operands and operator that complete the highlighted tile
- unrelated interactions should be unavailable during this guided action

Purpose:

- teach that a visible grid result is solved by reconstructing its hidden expression
- let the player use the same controls that generated `4 Pairs` uses

Content requirement:

- the first tile should use small, obvious arithmetic
- the expression should be simple enough that the player can focus on the interaction model

### 5. Teach The Pair Relationship

Recommended puzzle scenario:

- small two-pair tutorial scenario
- one pair whose sum and product are both easy to spot

Focus:

- the same two strip entries used as a pair
- the already completed sum or product tile
- the matching complementary tile

Copy:

- "Pair strip numbers so each pair creates one sum and one product that match two grid results."

Player action:

- complete the complementary tile using the same pair and the other operator
- unrelated interactions should be unavailable during this guided action

Purpose:

- teach the core `4 Pairs` relationship that one pair creates two results
- make clear that operands are not arbitrary isolated choices per tile

Content requirement:

- the authored puzzle should include one pair whose sum and product are both easy to spot
- the guided step should make the relationship visible without solving the rest of the puzzle for the player

### 6. Finish The Puzzle Normally

Recommended puzzle scenario:

- very easy `4 Pairs` tutorial scenario
- 8-entry strip with 5 known values and 3 hidden values
- low arithmetic load and clear pair relationships

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

## MVP Puzzle Progression

The MVP should use a small sequence of tutorial puzzle scenarios rather than forcing every teaching step into one board.

Recommended progression:

- Steps 1 and 2: one-pair orientation scenario that introduces unknowns, strip, and grid with minimal noise.
- Step 3: two-pair scenario that teaches hidden strip value deduction.
- Steps 4 and 5: two-pair scenario that teaches expression completion and the sum/product pair relationship.
- Step 6: very easy `4 Pairs` scenario with 5 known strip values and 3 hidden strip values.

This progression keeps each early lesson small while still ending on the real replayable shape players will see in generated `4 Pairs`.

---

## Authored Puzzle Requirements

The Tutorial MVP should use deliberately authored puzzle scenarios instead of the old prototype seed.

The authored puzzle scenarios should:

- support every tutorial step above
- use custom puzzle compositions where they make the lesson clearer
- include hidden strip values and hidden tile expressions
- use small positive integers
- keep arithmetic easy enough for first-time players
- include at least one clear strip-order deduction
- include at least one clear pair where the same two strip entries produce one sum tile and one product tile
- end with a very easy `4 Pairs` scenario that leaves a small amount of work for the player to solve independently
- be internally valid under the core game rules or documented tutorial-specific domain rules

The authored tutorial scenarios do not need to follow generated `4 Pairs` puzzle size or masking rules. For example, generated `4 Pairs` currently has exactly 3 known strip entries and 5 hidden strip entries, but Tutorial may use one-pair, two-pair, or easier `4 Pairs` reveal patterns if they better support the learning path.

Tutorial-specific domain rules may be introduced only for Tutorial content and should remain clearly separated from generated `4 Pairs` generation, validation, and difficulty rules.

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
- Tutorial should not inherit generated puzzle size or masking constraints unless they also serve the lesson.
- Tutorial-specific domain rules should remain scoped to Tutorial.
- Tutorial state should remain isolated from generated mode state and generated replay identity.
- Changes to Tutorial content should not require changes to generated puzzle construction.
- Changes to generated `4 Pairs` difficulty profiles should not automatically change the Tutorial MVP.

Shared implementation can still exist where it is genuinely common, such as the game screen, core domain rules, validation behavior, and normal interaction controls.

---

## MVP Scope

In scope for the first Tutorial MVP:

- a small sequence of authored tutorial puzzle scenarios
- custom one-pair and two-pair tutorial compositions when they teach concepts more clearly
- one final very easy `4 Pairs` tutorial scenario
- short guided step sequence
- focused visual attention on relevant puzzle elements
- concise instructional copy tied to the current step
- active player interactions for at least strip completion, tile completion, and pair relationship practice
- manual forward and backward step navigation
- guided steps that allow only the action currently being taught
- normal puzzle completion after guided steps
- no rules helper action inside Tutorial
- no changes to generated `4 Pairs` content or generation rules

Out of scope for the first Tutorial MVP:

- open-ended tutorial lesson libraries
- branching tutorial paths
- automatic step advancement
- adaptive hints
- solver-backed suggestions
- answer reveal
- tutorial-specific error feedback for unrelated guided-step actions
- rules helper access inside Tutorial
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

These ideas should not be treated as acceptance criteria for the first Tutorial MVP.

Future tutorial work should be considered only after the MVP confirms where players still struggle. The default direction should be to improve learning through clearer puzzle design and contextual feedback before adding heavier progression or gamification systems.

### Future Iteration Principles

Future tutorial iterations should:

- keep the player solving real puzzle states, not reading long explanations
- prefer learning by doing over separate instructional screens
- make the rule being taught visible through focused puzzle examples
- explain mistakes by referencing the rule, not by revealing the answer
- preserve normal gameplay interactions wherever possible
- stay separate from generated `4 Pairs` content and generator rules
- avoid adding rewards, scoring, or progression until there is evidence that they improve comprehension or retention

### Invisible Learning Through Puzzle Design

The strongest future direction is to make early learning feel like normal play.

Possible improvements:

- authored tutorial puzzles where the next useful move is naturally discoverable from the visible state
- examples where strip order makes one hidden value obvious without extra explanation
- early sum and product tiles that use the same pair in a way the player can infer
- gradual reduction of instructional copy as the player demonstrates understanding
- short confirmation copy after a correct action to name the rule the player just applied

This should not mean hiding the learning goal completely. The player should still understand why a move was correct so the knowledge transfers to generated `4 Pairs`.

### Concept-Focused Lessons

A later version may split Tutorial into small authored lessons instead of one walkthrough.

Candidate lesson topics:

- completing hidden strip values
- selecting operands from visible strip entries
- choosing between addition and multiplication
- recognizing that one pair creates one sum and one product
- solving repeated values or visually similar values
- finishing a full puzzle with limited guidance

Each lesson should remain short and focused. Multiple lessons should not become mandatory before a player can try generated `4 Pairs` unless playtesting shows the single MVP tutorial is not enough.

### Contextual Feedback

Future guidance may react to incorrect or incomplete actions.

Possible feedback patterns:

- if a strip value breaks ascending order, explain the ordering rule
- if a completed expression does not match the tile result, point back to the result and operator
- if a player uses unrelated operands across sum and product tiles, remind them that one pair creates both results
- if the player repeatedly tries exhausted operands, explain strip entry usage at a high level

Feedback should not reveal hidden strip values, correct pairings, exact next moves, or solver output.

### Graduated Guidance

A later Tutorial could use guidance levels that fade over time.

Possible model:

- first authored puzzle: guided highlights and required practice actions
- second authored puzzle: fewer highlights, feedback only after mistakes
- third authored puzzle: mostly normal gameplay with optional reminders

This would let Tutorial become a bridge between the first walkthrough and generated `4 Pairs` without requiring a full progression system.

### Lightweight Gamification

Gamification may help if it reinforces learning, but it is easy to overcomplicate the tutorial.

Lower-risk options:

- concept completion checks such as "Strip completed" or "First pair found"
- a final readiness message after Tutorial completion
- optional lesson completion markers if multiple lessons exist later
- optional challenge prompts after the player has already learned the rule

Higher-risk options that should remain deferred:

- scoring
- timers
- stars or grades
- streaks
- badges
- reward loops
- locked progression

Rationale:

- early tutorial pressure can distract from understanding
- reward systems add product and implementation complexity
- visible scores may imply performance evaluation instead of learning support

### Bridge To Generated `4 Pairs`

Future Tutorial completion may introduce generated `4 Pairs` more explicitly.

Possible improvements:

- post-tutorial copy explaining that generated `4 Pairs` uses the same rules with new puzzles every time
- a direct action to start generated `4 Pairs`
- an optional "practice again" action for players who want another guided attempt
- a first generated puzzle with optional reminder copy, if later research shows the transition is still difficult

This bridge should not make generated `4 Pairs` depend on tutorial completion.

### Accessibility And Pacing

Future iterations may add controls that make guidance easier to consume.

Possible improvements:

- persistent instruction text that can be reopened during a step
- manual step advancement after correct actions
- slower or reduced highlight transitions
- clearer focus order for assistive technologies
- tutorial-specific labels for highlighted areas
- a way to replay the current instruction without resetting puzzle state

### Replayable Practice Variants

Later versions may add more authored practice content without using the generated puzzle provider.

Possible improvements:

- alternate tutorial puzzles that teach the same concept with different numbers
- targeted practice puzzles for weak concepts found during playtesting
- authored examples for repeated values, duplicate results, or less obvious pair relationships
- optional review puzzles after Tutorial completion

These variants should remain authored and intentional. They should not become generated `4 Pairs` with tutorial labels unless a future product decision defines a separate guided generated-practice mode.

### Promotion Criteria

A future tutorial idea should move into milestone scope only when it has a clear reason.

Possible triggers:

- players complete the MVP tutorial but still fail to understand pair relationships in generated `4 Pairs`
- players understand grid expressions but struggle with hidden strip values
- players abandon Tutorial because instruction copy is too dense or too passive
- players need more practice before generated `4 Pairs`, but a full onboarding framework is still unnecessary
- accessibility review shows that the guided sequence needs better pacing or focus behavior

If no clear learning problem is identified, future work should favor keeping Tutorial small.

---

## Resolved MVP Decisions

- Tutorial may use custom puzzle compositions instead of the generated `4 Pairs` shape for every step.
- Custom tutorial puzzles should be based on domain rules. New domain rules may be added only when they are scoped to the Tutorial context.
- Tutorial guidance should use manual forward and backward navigation for the first version.
- Guided steps should allow only the action required by the current step, so unrelated incorrect actions are unavailable rather than handled with tutorial-specific feedback.
- Tutorial should show only one instructional surface at a time.
- The rules helper should not be shown inside Tutorial for the MVP.
- Tutorial should start with simple, small puzzle scenarios such as one-pair or two-pair puzzles.
- Tutorial should end with a very easy `4 Pairs` scenario, preferably with 5 known strip values and 3 hidden strip values.
- Exact numeric values should be chosen during implementation for clarity, small arithmetic, and low ambiguity.
