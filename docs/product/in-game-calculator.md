# In-Game Calculator Proposal

## Document Status

- Status: future product/UX proposal
- Suggested milestone: post-v2 gameplay usability polish
- Related references:
  - `docs/product/prd/prd-v2.md`
  - `docs/ui-behavior.md`
  - `docs/product/ux-decisions.md`

This document captures a potential future improvement for generated `4 Pairs` mode. It is not part of the v2 acceptance baseline.

---

## Problem

Generated `4 Pairs` puzzles require small arithmetic checks while solving.

Players can do those calculations mentally, on paper, or with an external calculator, but leaving the app flow adds friction. A lightweight in-game calculator could support replayable puzzle solving without changing the puzzle rules.

---

## Product Position

The calculator should be treated as a convenience tool, not as a hint system or solver.

It should:

- help players perform arithmetic they choose to enter
- remain independent from puzzle state
- avoid suggesting solution pairs
- avoid reading or auto-filling tile or strip values
- avoid exposing solver output

This keeps the feature aligned with the v2 boundary that player-facing solvers, hints, and solution reveal are out of scope.

---

## Recommended UX

The calculator should be accessible from the game screen top app bar with a calculator icon button on the right side.

Recommended presentation:

- Use a modal bottom sheet rather than a separate screen.
- Keep the current puzzle visible behind the modal context.
- Let system back close the calculator before leaving the game route.
- Block puzzle interaction while the calculator is open.

Rationale:

- A bottom sheet fits mobile usage better than a small top-right popup.
- It preserves puzzle context without creating another navigation destination.
- It gives enough room for a stable keypad layout.

---

## Functional Scope

Initial calculator controls:

- digits `0` through `9`
- addition
- subtraction
- multiplication
- division
- equals
- clear
- backspace

The original idea only requires digits and the five main operation symbols, but `clear` and `backspace` should be included from the first implementation because they are necessary for comfortable correction.

Non-goals for the first version:

- memory functions
- history
- parentheses
- percentages
- scientific operations
- direct insertion into the puzzle
- automatic pair suggestions

---

## Mode Scope

Start with generated `4 Pairs` mode only.

Tutorial should not receive the calculator by default unless a later product decision says the tutorial should also teach or expose it.

Implementation should make the tool opt-in from the route or mode configuration so the reusable game screen is not forced to show it everywhere.

---

## State Behavior

Recommended state behavior:

- Keep the current calculator expression while the player remains in the same puzzle.
- Clear calculator state when a new generated puzzle starts.
- Close the calculator when puzzle completion actions are shown.
- Do not persist calculator state across app restarts.

---

## Display Behavior

The calculator should show:

- one expression line
- one result line

Division behavior:

- Show integer results without a decimal suffix when division is exact.
- Show a short decimal result when division is not exact.
- Show an error state for division by zero.

The first implementation should prefer predictable, limited formatting over complex expression parsing.

---

## Open Questions

- Should the calculator be available in Tutorial later, or only in generated modes?
- Should the calculator preserve its expression if the bottom sheet is closed and reopened during the same puzzle?
- What decimal precision is enough for this game?
- Should invalid expressions show inline errors immediately or only after pressing equals?
- Should physical keyboard input be supported?
- Should the calculator button be hidden once the completion overlay is visible?
- Should future puzzle modes be able to configure which operations are shown?

---

## Suggested Implementation Direction

- Add a top app bar action slot to `GameScreen`.
- Add a calculator action from `FourPairsRoute` only.
- Render the calculator as a modal bottom sheet owned by the game UI layer.
- Keep calculator state separate from puzzle domain state.
- Keep tests focused on visibility, basic input/output behavior, close behavior, and mode-specific availability.
