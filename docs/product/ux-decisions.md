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
