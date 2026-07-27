# Initial Layout Decisions

## Strip Position

The strip is displayed above the board so players first see the available numbers before interacting with the puzzle.

### Rationale
- Clear top-to-bottom gameplay flow: available inputs first, puzzle board second
- Better onboarding for first-time users
- Stronger visual hierarchy

### Potential Concerns
- Bottom placement could be more comfortable for one-handed thumb interaction
- On tall devices, the strip may feel visually distant from the board
- Future usability testing may reveal better ergonomics with alternative placements

---

## Strip Layout

The strip is rendered as a single compact row of 8 number chips.

### Rationale
- Keeps the full set of available numbers visible at a glance
- Reduces vertical space consumption
- Improves scanability before interacting with the board

### Potential Concerns
- Three-digit numbers reduce the visual space available to each chip
- Very narrow devices may require revisiting chip spacing or typography
- A single-row constraint leaves less room for future decorative styling

---

## Tile Reading Order

Tiles display operands and operator on the top row, and the result on the bottom row.

### Rationale
- More natural left-to-right, top-to-bottom reading flow
- Easier onboarding
- Clear cause → result relationship

### Potential Concerns
- Users familiar with classic style layouts may expect the result first
- The result area may receive less visual emphasis when placed below the expression
- Alternative layouts may perform better depending on visual styling and spacing

---

## Tile Emphasis

The bottom row result is visually emphasized with larger typography than the top row expression.

### Rationale
- Makes the outcome of each arithmetic expression faster to parse
- Reinforces the top-to-bottom reading flow from expression to result
- Helps the result remain legible even when tiles stay width-bounded

### Potential Concerns
- A stronger result may visually dominate the expression too much
- Tight vertical space could make some tiles feel heavy
- Further tuning may be needed once interaction states are added

---

## Responsive Board Layout

The board adapts the number of visual columns to the available width, but tile width is kept within a bounded range instead of stretching to fill all free space.

### Rationale
- Prevents tiles from becoming awkwardly wide in landscape or on larger screens
- Preserves a more card-like visual proportion
- Keeps the board centered while still adapting to smaller widths

### Potential Concerns
- The rendered board may not mirror any future logical grouping rules one-to-one
- Some users may perceive reflow across orientations as a stronger layout change
- Larger screens may expose more empty horizontal space around centered rows

---

## Invalid Tile Feedback

When a tile becomes fully known but incorrect, the UI marks the tile using a combined error treatment instead of blocking interaction or crashing.

Current visual direction:
- subtle error-tinted tile container
- error-colored tile border
- error-colored expression row
- normal result styling

### Rationale
- Makes incorrect tiles noticeable without overwhelming the board
- Keeps the error attached to the player-entered expression instead of the target result
- Preserves readability of the tile result while still making the mismatch obvious
- Avoids relying on a single visual cue

### Potential Concerns
- Some themes may need tuning if the error tint is too subtle or too strong
- Color-only feedback would be insufficient on its own, so accessibility semantics should remain in place
- Future hinting or puzzle-completion states may require a clearer distinction from simple incorrectness

---

## v10 Quick And Daily Entry Points

### Decision

- Present `Quick` as the player-facing name for the generated `3 Pairs Low` challenge.
- Launch Quick from one full-width primary CTA without a redundant one-option difficulty selector.
- Present Daily Challenge as one unified split primary CTA at the top of the unlocked Menu.
- Use the Daily primary region for start, continue, or completed-today behavior.
- Use the trailing Daily region as an always-available completion-calendar action.

### Rationale

- Quick communicates session length more clearly than exposing only the pair count.
- Keeping `3 Pairs Low` in accessibility and gameplay copy preserves structural and difficulty
  meaning.
- A direct Quick action protects the promise of immediate short play.
- Daily has two stable actions regardless of state: act on today or inspect history.
- Reusing the split primary CTA role preserves Menu hierarchy and avoids an extra Daily landing
  screen.

### Potential Concerns

- Additional primary actions increase Menu height on compact or text-scaled devices.
- Daily and normal Resume may both be visible and need clearly distinct labels.
- The Daily calendar icon needs an explicit localized action description because icon meaning
  alone is insufficient.

---

## v11 Quick And Classic Generated Play

### Decision

- Replace the direct Quick, 4 Pairs, and 8 Pairs normal-play actions with Quick and Classic split
  primary CTAs.
- Let Quick expose Low and Medium and select the matching 3 Pairs challenge 35% of the time and
  4 Pairs challenge 65% of the time.
- Let Classic expose Medium and Hard over the original 8 Pairs board.
- Keep pair counts as structural and accessibility information rather than separate Menu modes.
- Resolve a random Quick size only after the player confirms a new puzzle.

### Rationale

- Two intent-based choices reduce Menu complexity.
- Quick communicates a shorter session relative to the original full-board Classic experience.
- Difficulty remains explicit while puzzle-size variety happens automatically.
- Delayed selection prevents dismissed replacement dialogs from creating meaningless random
  choices.
- Keeping exact size identities internal preserves reliable Resume and profile calibration.

### Potential Concerns

- Quick no longer promises one exact board size, so copy must not imply that it always contains
  3 Pairs.
- Independent weighted selections can produce visible runs of one size; v11 intentionally adds no
  streak smoothing.
- Medium solving tips must not claim Low-only profile constraints.

---

## v10 Daily Completion History

### Decision

- Keep normal generated and Daily resumability visibly and persistently independent.
- Show past completion dates in a local monthly calendar.
- Keep past dates informational and non-interactive.
- Treat past dates without completion as neutral rather than missed or failed.
- Share only current-date Daily completion through concise text.

### Rationale

- Independent entry points reflect the two independent persistence slots.
- A calendar gives Daily completion durable meaning without introducing a streak or score.
- Non-interactive past dates keep v10 out of catch-up and backfill behavior.
- Neutral history avoids punishing players for days before installation or voluntary breaks.
- Text-only sharing celebrates participation without revealing puzzle content.

### Potential Concerns

- Device-clock changes can alter which date is considered today; v10 explicitly trusts local time.
- Clearing application data or reinstalling resets the calendar because no account or backup owns
  it.
- Sharing without a score or time is intentionally ceremonial rather than competitive.
