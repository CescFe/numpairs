# Ubiquitous Language

## Puzzle
A complete game instance containing a board and a strip of available numbers.

## Generated Puzzle
A puzzle produced by a generator rather than authored by hand.

Generated puzzles are used by generated challenges. A challenge selects one validated generated
puzzle profile and presents its player-facing initial puzzle.

## Generated Mode
A replayable generated-puzzle family identified by a stable generated-mode identity and puzzle
size.

`3 Pairs`, `4 Pairs`, and `8 Pairs` are the implemented generated modes. A mode exposes one or
more explicitly supported generated challenges. Its identity is used for exact challenge
resolution and generated-session identity; it does not imply one difficulty or one player-facing
Menu option.

## Generated Play Option
A player-facing normal-play choice that exposes supported difficulties and resolves each confirmed
new-puzzle request to one exact Generated Challenge.

`Quick` and `Classic` are the supported generated play options. A play option owns player-facing
grouping, challenge-selection policy, and its remembered difficulty. It does not own one puzzle
size, generation profile, exact session state, or Daily cadence.

## Quick
A shorter replayable Generated Play Option that selects either a 3 Pairs or 4 Pairs challenge at
the requested Low or Medium difficulty.

Every confirmed new Quick puzzle independently selects 3 Pairs with 35% probability and 4 Pairs
with 65% probability. Quick is not a difficulty tier, generated mode, Tutorial step, generation
profile, or session type. It uses the normal generated-session lifecycle.

## Classic
The replayable Generated Play Option for the original full board containing 8 solution pairs,
16 strip entries, and 16 board tiles.

Classic exposes Medium and Hard. Each difficulty resolves the matching exact 8 Pairs challenge
without weighted size selection.

## Difficulty Tier
The intended deductive challenge classification independent from generated puzzle size.

Supported tiers are:

- low
- medium
- hard

A tier has shared product meaning but does not own identical absolute profile constants for every
size. Each supported challenge calibrates the tier for its mode.

## Generated Challenge
One supported playable combination of generated mode, difficulty tier, and generated puzzle
profile.

Generated challenges are registered explicitly. Unsupported size and difficulty combinations are
absent rather than synthesized. Challenge identity owns application profile selection,
generation, retained presentation state, navigation, and exact session resolution.

A Generated Play Option may select among multiple generated challenges. Once selected, the exact
challenge remains authoritative for generation, retry, persistence, resume, and gameplay.

## Generated Puzzle Profile
The validated, immutable generation and difficulty-assessment configuration for one generated
challenge.

A profile owns puzzle size, value and result constraints, initial masking, variety targets, and
assessment policy. Android strings, player preference, lock state, navigation, and
challenge-specific learning capabilities remain outside the profile.

## Generated Session
A normal replayable generated-puzzle lifecycle identified by a stable session identity.

NumPairs owns at most one generated session slot for the application, shared by all generated
challenges. A generated session carries the mode and profile identities that resolve its exact
challenge, seed metadata, exact initial puzzle, and exact current puzzle.

Daily Challenge uses a separate Daily Session lifecycle and does not occupy this slot.

Generated Play Option identity is derivable from the exact configured challenge and is not stored
redundantly in the session snapshot.

## Resumable Generated Session
The generated session currently stored in the application-wide slot when its current puzzle is
valid, resolves to a configured challenge through its mode/profile identities, and is not solved.

An opened but untouched generated puzzle is resumable. A solved, stale, mismatched, corrupt, unsupported, or missing snapshot is not resumable.

## Generated Session Snapshot
The versioned local representation of one generated session.

The snapshot preserves stable session, mode, profile, board, tile, expression, strip-entry, and strip-item identity needed to restore the exact current puzzle. Its seed is metadata; the snapshot is not restored by regenerating from the seed.

## Daily Challenge
The one date-bound playable puzzle selected by a Daily Recipe for one device-local calendar date.

A Daily Challenge selects an existing generated challenge. It is a play cadence, not a generated
mode, difficulty tier, generated profile, or normal generated session.

## Daily Challenge Identity
The stable identity combining one canonical device-local calendar date with one Daily Recipe
version.

The canonical date uses ISO-8601 `YYYY-MM-DD`. Localized display text, the current clock value, and
derivable profile parameters are not part of the identity.

## Daily Recipe
An immutable versioned definition that selects one generated challenge and deterministically maps
a Daily Challenge identity plus candidate index to an ordered bounded sequence of generation
seeds.

The v10 Daily Recipe selects exact `4 Pairs Low`. It does not use Quick weighting or remembered
Generated Play Option difficulty. A recipe change requires a new stable version.

## Daily Session
The playable lifecycle for one Daily Challenge identity.

NumPairs owns at most one Daily Session slot, separate from the normal generated-session slot. A
Daily Session carries a stable session identity, recipe and date identity, successful candidate
metadata, exact initial puzzle, and exact current puzzle.

## Resumable Daily Session
The Daily Session currently stored in the Daily aggregate when its identity matches the current
Daily Challenge, its recipe and puzzle remain supported and valid, no completion owns its date, and
its current puzzle is not solved.

An unfinished prior-date, solved, mismatched, corrupt, completed, unsupported, or missing Daily
Session is not resumable.

## Daily Session Snapshot
The versioned local representation of one Daily Session.

The snapshot preserves stable Daily Session, Daily Challenge, candidate, board, tile, expression,
strip-entry, and strip-item identity needed to restore exact progress. Seed and candidate index are
metadata verified against the recipe; restoration never regenerates from them.

## Daily Completion
The local fact that the puzzle for one Daily Challenge identity became solved.

A completion owns its canonical date and recipe version. It contains no score, time, streak,
reward, display text, or puzzle.

## Daily Completion History
The local collection of Daily Completion records displayed by the calendar.

The history records at most one completion per local calendar date, remains independent from
normal generated sessions and preferences, and is not synchronized across installations.

## Current Puzzle
The latest committed domain state of the puzzle being played in a normal generated session or
Daily Session.

Current puzzle changes include committed strip values, operand assignments, operator assignments, and tile resets. Transient presentation state such as drafts, open selectors, dialogs, overlays, highlights, and scroll position is not part of the current puzzle.

## Tutorial
A gameplay mode that teaches the core NumPairs rules through authored content and guided player practice.

Tutorial is a learning surface, not generated replayable content, a rules helper, a hint system, or an answer reveal feature.

Quick may be recommended after Tutorial, but its generated puzzles do not replace authored
Tutorial content.

## Authored Tutorial Puzzle
A handcrafted puzzle selected for teaching clarity in Tutorial mode.

An authored tutorial puzzle may use custom tutorial composition rules and may share core game rules and UI interactions with generated modes, but its content is chosen by design rather than produced by the puzzle generator.

## Initial Puzzle
The player-facing puzzle state shown at the start of play.

For a generated puzzle, the initial puzzle is derived from a solved puzzle by hiding tile expressions and masking selected strip entries according to its selected profile.

## Solved Puzzle
The fully resolved puzzle used internally as the source of truth for generation and validation.

A solved puzzle is not exposed directly to the player.

## Board
The main area where the puzzle tiles are displayed.

## Grid
The row and column arrangement of the board.

## Tile
A single puzzle unit displayed on the board.

A tile is composed of:
- top row:
    - left operand
    - operator
    - right operand
- bottom row:
    - result

## Tile Resolution State
The validation state of a tile based on how complete its expression is and whether it matches the tile result.

A tile may be:
- unresolved
- correct
- incorrect

## Unresolved Tile
A tile whose expression is not fully known yet because at least one operand or the operator is still hidden.

## Correct Tile
A tile whose expression is fully known and whose evaluated value matches the tile result.

## Incorrect Tile
A tile whose expression is fully known but whose evaluated value does not match the tile result.

An incorrect tile is a valid gameplay state that the player can later correct.

## Expression
The arithmetic expression formed by two operands and one operator.

## Operator
The arithmetic operation used in a tile expression.

Supported operators:
- addition
- multiplication

## Strip
The horizontal area containing the available numbers for the puzzle.

## Strip Entry
A unique game element within the strip.

A strip entry has:
- a stable identity
- a current strip item

Two strip entries may display the same numeric value and still remain distinct game elements.

## Strip Entry Identity
The stable identity attached to a strip entry across strip reordering.

Tile operands reference strip entry identity rather than only a raw numeric value or a visual position.

## Strip Item
A strip value state stored in a strip entry and displayed in one strip position.

A strip item may be:
- hidden
- known from the start of the puzzle
- completed by the player during play

## Hidden Strip Item
A strip item whose value is not visible to the player at the beginning of the puzzle.

## Known Strip Item
A strip item whose value is visible to the player at the beginning of the puzzle.

## Player-entered Strip Item
A strip item whose value was entered by the player after starting from a hidden state.

## Available Number
A UI-facing shorthand for the visible numeric value exposed by a strip entry.

Domain rules operate on strip entries and strip entry identity, not on raw numbers alone.

## Visible Strip Entry
A strip entry whose current strip item exposes a numeric value to the player.

Visible strip entries can participate in operand selection.

## Operand Slot
One editable operand position in the top row of a tile expression.

A tile has:
- left operand slot
- right operand slot

## Operand Selection Choice
A domain representation of one selectable strip entry for a specific operand slot.

An operand selection choice includes:
- the strip entry identity
- the currently visible value
- operator-specific usage state
- current availability for selection

## Operand Selection Availability
The current selection status of a strip entry for a specific operand slot.

An operand selection choice may be:
- available
- exhausted
- blocked by the opposite operand already assigned in the same tile

## Solution
The correct assignment of operands and operators needed to solve the puzzle.

## Solution Pair
An unordered pair of strip entries that produces one addition tile and one multiplication tile in a generated puzzle.

The addition and multiplication tiles for a solution pair must reference the same two strip entry identities.

## Puzzle Generator
A domain service that creates generated puzzles.

The generator builds a solved puzzle first, derives the initial puzzle from it, and validates the generated aggregate before it can be shown to the player.

## Puzzle Validator
A domain validation boundary that checks shared completion facts and, for generated puzzles, the selected profile and solved-to-initial transformation. It returns typed violations rather than solving or revealing a puzzle for the player.

## Rules Helper
An informational gameplay UI surface that explains the core NumPairs rules.

The rules helper is not a hint system, tutorial engine, or answer reveal feature.
