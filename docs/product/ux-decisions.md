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