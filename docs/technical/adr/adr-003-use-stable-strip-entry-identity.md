# ADR-003: Use Stable Strip Entry Identity for Operand Selection

## Context
The NumPairs prototype allows the player to:
- edit hidden strip numbers
- reorder player-entered strip values inside editable runs to preserve ascending order
- assign strip numbers to tile operands

At the same time, repeated numeric values are allowed in the strip, and future selector guidance needs to express operator-specific usage:
- once in an addition expression
- once in a multiplication expression

This creates an identity problem for operand selection. When a tile references a strip number, we need to decide what exactly is being referenced over time.

Options considered:
- Identity by numeric value
- Identity by fixed strip slot
- Identity by stable strip entry

## Decision
We will model operand selection with stable strip entry identity.

Each strip entry is treated as a unique game element, even if two entries display the same numeric value. Tile operands therefore reference a stable strip entry id rather than only a value or a visual index.

## Rationale

### Option 1: Identity by Numeric Value
The tile stores only the selected number, for example `6`.

#### Positive
- Simplest implementation
- Minimal state and mapping logic
- Works if repeated values are intentionally interchangeable

#### Negative
- Cannot distinguish repeated values such as two visible `25` entries
- Cannot express usage hints per unique strip element
- Teaches the wrong model if each strip entry is meant to be unique
- Breaks down as soon as future rules care which repeated value was used

#### Fit for NumPairs
Poor fit.

This conflicts with the chosen game interpretation that repeated visible values are still distinct strip elements.

### Option 2: Identity by Fixed Strip Slot
The tile stores the strip index, for example `index = 0`.

#### Positive
- Still fairly simple
- Distinguishes repeated values if they occupy different slots
- Easy to inspect and test

#### Negative
- Fails when player-entered values reorder inside editable runs
- A tile would silently end up pointing to a different visible number after strip reorder
- Makes selector hints follow positions instead of the actual strip element selected by the player
- Creates tension between "selection follows the slot" and "selection follows the chosen strip number"

#### Fit for NumPairs
Weak fit.

This would only be appropriate if strip position itself were the core identity, or if strip reordering did not exist.

### Option 3: Identity by Stable Strip Entry
The tile stores a stable strip entry id that survives visual movement inside the strip.

#### Positive
- Correctly distinguishes repeated values
- Remains correct after strip reordering
- Supports operator-specific usage hints per actual strip element
- Preserves a consistent relationship between a tile operand and the selected strip entry over time
- Scales better for future rules and validation

#### Negative
- More code and model complexity than the other two options
- Requires explicit identity in the strip model and in tile operand assignments
- Slightly higher cognitive overhead during implementation

#### Fit for NumPairs
Best fit.

This is the only option that supports all the current prototype constraints together:
- repeated strip values are allowed
- player-entered values may reorder
- each strip entry is a unique game element
- usage needs to be tracked separately for `+` and `×`

## Consequences

### Positive
- Operand selection remains semantically correct after strip reorder
- Repeated values can be modeled and hinted independently
- Domain logic can expose selector-ready usage hints without ambiguity
- Future gameplay rules can build on explicit strip entry identity instead of inferring it from UI position

### Negative
- Domain and UI state require explicit strip entry ids
- Tiles can no longer store only a raw operand value if they need to preserve identity
- The prototype carries slightly more state than a pure value-based implementation

## Implementation Notes
The current implementation reflects this decision by:
- modeling the strip as `StripEntry` objects with stable ids
- keeping `Strip` immutable, while producing a new `Strip` on updates and reorders
- preserving strip entry identity when player-entered values reorder
- storing `stripEntryId` alongside assigned operand values in tile expressions
- exposing operator-specific usage hints per visible strip entry

This means immutability and identity are treated as separate concerns:
- immutability controls how state changes
- stable identity controls what remains the same across those state changes

## Future Considerations
If the product later removes strip reordering or intentionally treats repeated values as interchangeable, this decision can be revisited.

Until then, stable strip entry identity should remain the reference model for:
- operand selection
- operand usage hints
- future validation tied to strip-origin awareness
