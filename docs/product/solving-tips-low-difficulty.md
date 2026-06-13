# Low-Difficulty Solving Tips

## Document Status

- Status: supporting product reference for Solving tips
- Applies to: current low-difficulty generated `4 Pairs`
- Related references:
  - `docs/product/solving-tips.md`
  - `docs/product/puzzle-generation.md`
  - `docs/game-rules.md`

This document records Solving tips that are strongly tied to the current low-difficulty generation model. These tips may need to be rewritten, hidden, or removed when additional difficulty levels are introduced.

---

## Low-Difficulty Assumptions

Current low-difficulty generated `4 Pairs` puzzles have constraints that make some strategies especially useful:

- strip values are in the range `2..20`
- strip values do not repeat
- strip values do not include `1`
- multiplication results are capped
- board results are distinct

These constraints are product assumptions for the first generated mode, not universal NumPairs rules.

---

## Practice Tips For Low Difficulty

The first `Practice tips` CTA should teach exactly two low-difficulty strategies.

### 1. Identify A Sum Using A Prime Result

In current low-difficulty puzzles, strip values do not include `1`.

Because of that, a prime grid result cannot be produced by multiplying two strip values. A prime result should be treated as a strong signal that the tile is an addition tile.

Recommended step copy:

> In low difficulty, the strip never includes 1, so prime results are always sums. Complete the prime result as an addition tile.

This tip should be documented and implemented as low-difficulty-specific. It should be revisited if a future difficulty allows `1`, changes the strip value range, or introduces different operations.

### 2. Narrow A Product By Checking Factors, Then Confirm The Matching Pair's Sum

For a multiplication tile, both operands must be exact factors of the visible result.

After identifying a plausible factor pair, the player should check whether the same two strip entries also explain a matching addition result elsewhere in the grid.

Recommended step copy:

> Product tiles can only use factors of their result. Find two strip values that multiply to the product, then use the same pair to complete its matching sum.

This tip is strongly aligned with current low-difficulty puzzles because the value range is small and the multiplication cap keeps factor checks approachable.

---

## Static Dialog Tips For Low Difficulty

The first low-difficulty Solving tips dialog should show concise strategy guidance. These tips should be framed as player-facing heuristics, not guarantees that inspect the current puzzle.

### Prime Results Are Addition Tiles

Current low-difficulty strip values start at `2`, so there is no `1`.

If a visible grid result is prime, it cannot be made by multiplying two strip values. The player should treat it as an addition tile.

### Large Results Are Usually Product Tiles

Current low-difficulty strip values are small, so addition results stay relatively small.

Very large grid results are strong product candidates. This should be written as a practical shortcut rather than as a formal numeric threshold.

### Products Can Be Checked By Factors

A multiplication tile can only use strip values that divide the result exactly.

If a visible strip value is not a factor of the grid result, that strip entry cannot belong to that product tile.

### Hidden Strip Values Must Fit The Sorted Strip

The strip remains sorted after hidden values are completed.

Before solving grid tiles, the player can use the visible neighbors around a hidden strip entry to narrow possible values.

### The Highest Visible Number Is A Strong Anchor

The current low-difficulty generator always reveals the highest strip value.

The player can use this visible high value to test large product candidates and to rule out addition candidates that are too small.

### Usage Badges Help Avoid Overusing Entries

Operand options show whether each strip entry has already been used for addition or multiplication.

The player can check these usage badges to avoid reusing the same strip entry too many times for the same operator.

---

## Candidate Dialog Copy

Recommended dialog title:

- `Solving tips`

Recommended CTA:

- `Practice tips`

Recommended sections and copy:

### Use The Strip

- `Hidden strip values must still fit between their visible neighbors.`
- `The highest strip value is an anchor for large products and impossible sums.`

### Find Products

- `Large results are usually product tiles.`
- `Check its factors.`

### Find Sums

- `In low difficulty, prime results are always addition tiles.`

### Use UI Clues

- `Operand choices show addition and multiplication usage.`

---

## Future Difficulty Guidance

Any future difficulty level should explicitly decide whether these tips still apply.

Examples:

- If `1` becomes available, prime-result reasoning changes because `1 x prime` becomes possible.
- If repeated strip values become common, copy must clarify strip entry identity.
- If value ranges become larger, factor-check guidance may need to avoid implying easy mental arithmetic.
- If new operations are introduced, both tips must be rewritten or removed.
