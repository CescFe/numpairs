# PRD - NumPairs (v1 Historical Snapshot)

> Historical note: this document captures the original product polish & technical hardening scope. It remains as a historical snapshot and was later superseded.

## Product Summary

NumPairs is a native Android arithmetic deduction puzzle.

Players use numbers from a strip to complete hidden expressions on a board. Each pair of strip entries generates one addition result and one multiplication result, and the player must resolve the puzzle by deducing missing numbers, selecting operators, and matching pairings correctly.

---

## Product Goal

Turn the first playable prototype into a stable, clear, and maintainable foundation ready for feature expansion.

---

## Problem Statement

The prototype already proves the core puzzle loop, but it still lacks enough polish, architectural clarity, and documentation consistency to support confident iteration.

v1 should make the game:

- easier to understand
- easier to maintain
- more trustworthy in its interaction feedback
- better prepared for future flows and feature growth

---

## Target Users

- Casual puzzle players
- Players who enjoy mental math and deduction
- Mobile-first users looking for short, understandable puzzle sessions

---

## Current Baseline At Start Of v1

The product entering v1 is still the `v0 - Playable Prototype`.

That baseline currently includes:

- a single playable Android screen
- an 8-tile result board
- an 8-entry number strip
- hidden, known, and player-entered strip states
- direct tile editing through contextual selectors
- local incorrect-tile feedback for fully known wrong expressions
- a whole-puzzle completion flow, including invalid completed puzzle outcomes
- responsive Compose UI foundations

That baseline does not yet include:

- branded app identity
- splash screen flow
- formal app navigation orchestration
- persistence or session continuity
- scoring systems
- multiple puzzles or difficulty selection

---

## Product Principles

- Clarity over cleverness
- Mobile-first direct manipulation
- Validation should inform players without blocking experimentation
- Architecture should support future screens and features without over-engineering
- Documentation should mirror the real product and the intended milestone scope

---

## Core UX Expectations

- The strip and the board should be understandable at a glance
- Single tap is the primary interaction across the strip and board
- Editing a grid slot must not require prior strip selection
- Incorrect tile feedback appears only when a tile is fully known and incorrect
- Invalid completed puzzle states should be explained clearly enough for the player to recover
- Reset interactions should be discoverable, reliable, and safe
- Accessibility semantics, labels, and contrast should improve comprehension and assistive technology support
- App launch should feel intentional and branded, including launcher assets and splash behavior

These expectations align with the detailed interaction rules defined in `docs/ui-behavior.md`.

---

## v1 Scope

### Product And UX

- Improve feedback for incorrect tile states
- Improve feedback for invalid completed puzzle states
- Preserve or refine correct-completion feedback so success remains clear
- Add reliable reset tile interaction
- Design a first-pass NumPairs logo and Android app icon
- Add a branded splash screen using the approved logo
- Improve UI clarity and accessibility semantics, including content descriptions and contrast

### Engineering Quality

- Introduce `AppNavigation` to orchestrate startup and future navigation flow
- Reduce `MainActivity` responsibilities
- Refactor code for readability and maintainability
- Review and tighten MVVM boundaries

### Testing

- Increase high-value coverage for product-critical behavior
- Remove redundant or low-signal tests

### Documentation

- Add `docs/product/prd/prd-v1.md` as the canonical PRD
- Keep `docs/product/prd/prd-v0.md` as the historical prototype snapshot
- Align roadmap, README, and supporting product docs with the NumPairs naming and v1 scope

---

## Out Of Scope

- Scoring system
- Persistence or save state
- Difficulty modes
- Multiple puzzles
- Content browsing or puzzle selection flows
- Multiscreen product flows beyond the startup flow needed for splash-to-game entry
- Accounts, backend services, or social features

---

## Success Criteria

- The codebase is clean, readable, maintainable, and ready for the next iteration
- Architecture is consistent and intentional, with clear MVVM boundaries
- Players clearly understand when a tile is incorrect and when a completed puzzle is still invalid
- Reset interactions work reliably
- The app has a coherent first impression through branded iconography and splash behavior
- The UI is more polished and accessible
- Documentation reflects the real system and the intended v1 product baseline

---

## Documentation Alignment Notes

- `README.md` is the lightweight repository overview and should point to this PRD as the canonical product document
- `docs/product/roadmap.md` defines milestone sequencing and should stay aligned with the v1 scope summarized here
- `docs/ui-behavior.md` defines detailed interaction behavior for strip, tile, selector, and validation flows
- `docs/product/ux-decisions.md` captures layout and visual rationale that supports this PRD
- `docs/game-rules.md` and `docs/ubiquitous-language.md` define the rules and terminology used throughout the product docs

No intentional product-level divergence is documented between this PRD and the supporting product documents listed above. If implementation changes create a mismatch, the corresponding product docs should be updated together.
