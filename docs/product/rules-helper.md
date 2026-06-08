# Rules Helper Requirements

## Document Status

- Status: v3 product/UI reference for the gameplay rules helper
- Applies to: generated `4 Pairs` gameplay mode
- Related references:
  - `docs/product/prd/prd-v3.md`
  - `docs/game-rules.md`
  - `docs/ui-behavior.md`
  - `docs/ubiquitous-language.md`

This document defines the first rules helper requirements before UI implementation. It records what the helper should explain, where it appears, how it opens and closes, and what it must not become.

---

## Purpose

The rules helper should let players understand or refresh the core NumPairs rules without leaving the current puzzle.

It exists to explain how the game works. It is not a hint system, a solver, or a tutorial engine.

---

## Source Of Truth

`docs/game-rules.md` remains the source of truth for the actual puzzle rules.

The helper should summarize those rules in concise, player-facing language. If a future implementation needs to change the rules themselves, `docs/game-rules.md` must be updated first and this helper reference should be aligned afterward.

Documentation boundaries:

- `docs/product/prd/prd-v3.md`: defines why rules help belongs in v3 and what success means.
- `docs/game-rules.md`: defines the actual rules of NumPairs.
- `docs/ui-behavior.md`: defines how the helper is reached and dismissed from gameplay UI.
- `docs/product/rules-helper.md`: defines helper-specific product, content, and presentation decisions.

---

## Resolved Decisions

- The helper should appear inside generated `4 Pairs` gameplay only for the first implementation, not in the menu.
- Tutorial should not expose the helper for the MVP because Tutorial already has its own guided instructional surface.
- Tutorial behavior is independent of the helper. Persistent tutorial instructions, tutorial puzzle sizing, multistep tutorial structure, and future tutorial evolution belong to tutorial documentation, not this helper scope.
- There should not be a separate pre-puzzle `How to play` screen for this helper. The future authored tutorial should act as the deeper how-to-play experience.
- The helper should primarily describe rules without concrete numeric examples. A small example may be added only if a rule is otherwise unclear.
- Helper copy can start as simple Android string resources.
- The helper should close through a visible close icon, outside tap, or system back, and puzzle state should remain intact.

---

## Availability

The helper should be available from generated `4 Pairs` gameplay through a game top app bar action.

Initial availability:

- Tutorial: excluded for the MVP
- generated `4 Pairs`: included
- Menu: excluded for the first implementation

Rationale:

- Tutorial should show only one instructional surface at a time, and the guided Tutorial content is the primary instructional surface for that mode.
- Adding the helper to generated `4 Pairs` keeps the first implementation focused on on-demand help for replayable gameplay.
- `MenuScreen` has its own top bar and does not currently host gameplay context, so adding menu-level help would expand the surface area without being required for v3.

The helper should not be tied to generated puzzle internals or tutorial-specific state. It should be reusable from gameplay modes that opt into the shared game screen.

---

## Entry Point

The helper should open from an icon button in the game screen top app bar.

Recommended icon:

- help icon, such as `?` in a circle or the closest available Material/Lucide equivalent in the project icon approach

Expected accessibility:

- content description should identify the action as rules help
- test tag should be stable enough for UI tests

The helper action should not replace the existing back navigation. The top app bar should continue to show the mode title and the back button.

---

## Presentation Pattern

Use a modal dialog for the first implementation.

Decision:

- Do not use a separate screen.
- Do not use a small anchored popup.
- Do not use a bottom sheet for the first implementation.

Rationale:

- The helper contains readable explanatory content, not contextual slot choices.
- A modal dialog keeps the current puzzle visible as background context while clearly pausing interaction with it.
- A small anchored popup is likely too constrained for rules text and accessibility.
- A bottom sheet is already used for operand selection, so using a dialog avoids confusing rules help with selection workflows.
- A dialog can be implemented with low disruption to the current `GameScreen` structure.

The dialog content should be vertically scrollable if needed on small screens.

---

## Closing Behavior

The helper should close without changing puzzle state.

Supported close paths:

- tap a visible close icon, such as `X`
- tap outside the dialog
- use system back while the helper is open

System back behavior:

- if the helper is open, back closes the helper first
- if the helper is closed, back keeps the existing route behavior

Opening and closing the helper must not reset:

- strip entries
- tile assignments
- active puzzle outcome
- completion state
- generated puzzle identity

---

## Content Scope

The initial helper should explain:

- strip numbers
- hidden strip values
- board tiles
- operands
- operators
- the relationship between one pair, one sum, and one product
- completion validation at a high level

Recommended content shape:

- short title
- concise grouped sections or bullet-style rows
- plain player-facing language
- no implementation terms such as solver, provider, generator, or validation service

Examples with concrete numbers are not required for the first version. They may be used only if the rule cannot be explained clearly without one.

---

## Non-Goals

The helper must not:

- reveal puzzle-specific answers
- reveal hidden strip values
- identify correct pairings for the current puzzle
- suggest the next move
- call or expose the solver
- auto-fill strip entries or tiles
- change validation behavior
- replace the authored Tutorial experience

The helper and Tutorial are separate product surfaces. Tutorial design, tutorial puzzle size, tutorial step structure, and future tutorial iteration depth are out of scope for this helper documentation.

---

## Content Implementation Guidance

For the first implementation, helper copy can live as simple Android string resources.

Rationale:

- The first helper is static and short.
- A separate content model would add complexity before there is real reuse pressure.
- Android string resources keep localization and accessibility paths open.

If future helper content becomes mode-specific, localized at larger scale, or shared by non-Android surfaces, this decision can be revisited.

---

## Remaining Open Questions

- Exact helper copy and tone should be finalized during implementation.
- Should the helper action be hidden, disabled, or left unavailable when the success overlay is visible?
- If another gameplay modal is already open, should opening the helper be blocked, or should the existing modal close first?
- Should the helper include any simple visual formatting, such as icons for strip, board, `+`, and `x`, or remain text-only initially?
- Should future modes be able to add mode-specific helper sections after the shared core rules?
- What accessibility labels and heading semantics should be used inside the dialog?
