# Low-Difficulty Solving Tips

## Document Status

- Status: supporting product reference for Solving tips
- Applies to: generated `4 Pairs Low`

This document records Solving tips that depend on the generated `4 Pairs Low` profile. The profile configuration lives in `docs/product/puzzle-generation.md`.

---

## Low-Difficulty Assumptions

`4 Pairs Low` has constraints that make some strategies especially useful. The exact generation profile is documented in `docs/product/puzzle-generation.md`; the player-facing implications are:

- `1` is excluded, so prime board results are strong addition-tile signals
- strip values are distinct and small enough for factor checks to stay approachable
- multiplication results are capped, so large results are usually product candidates
- board results are distinct, reducing duplicate-result ambiguity
- the highest strip value is visible, giving players an anchor for large products and impossible sums

These are profile-specific assumptions, not universal NumPairs rules.

---

## Practice Tips For Low Difficulty

The first `Practice tips` CTA should teach exactly two low-difficulty strategies.

### 1. Identify A Sum Using A Prime Result

In `4 Pairs Low`, strip values do not include `1`.

Because of that, a prime grid result cannot be produced by multiplying two strip values. A prime result should be treated as a strong signal that the tile is an addition tile.

Recommended step copy:

> In low difficulty, the strip never includes 1, so prime results are always sums. Complete the prime result as an addition tile.

This tip is low-difficulty-specific. It should be revisited if a future profile allows `1`, changes the strip value range, or introduces different operations.

### 2. Narrow A Product By Checking Factors, Then Confirm The Matching Pair's Sum

For a multiplication tile, both operands must be exact factors of the visible result.

After identifying a plausible factor pair, the player should check whether the same two strip entries also explain a matching addition result elsewhere in the grid.

Recommended step copy:

> Product tiles can only use factors of their result. Find two strip values that multiply to the product, then use the same pair to complete its matching sum.

This tip is strongly aligned with `4 Pairs Low` because the value range is small and the multiplication cap keeps factor checks approachable.

---

## Static Dialog Tips For Low Difficulty

The first low-difficulty Solving tips dialog should show concise strategy guidance. These tips should be framed as player-facing heuristics, not guarantees that inspect the current puzzle.

### Prime Results Are Addition Tiles

`4 Pairs Low` strip values start at `2`, so there is no `1`.

If a visible grid result is prime, it cannot be made by multiplying two strip values. The player should treat it as an addition tile.

### Large Results Are Usually Product Tiles

`4 Pairs Low` strip values are small, so addition results stay relatively small.

Very large grid results are strong product candidates. This should be written as a practical shortcut rather than as a formal numeric threshold.

### Products Can Be Checked By Factors

A multiplication tile can only use strip values that divide the result exactly.

If a visible strip value is not a factor of the grid result, that strip entry cannot belong to that product tile.

### Hidden Strip Values Must Fit The Sorted Strip

The strip remains sorted after hidden values are completed.

Before solving grid tiles, the player can use the visible neighbors around a hidden strip entry to narrow possible values.

### The Highest Visible Number Is A Strong Anchor

`4 Pairs Low` always reveals the highest strip value.

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

Any future difficulty profile should explicitly decide whether these tips still apply.

Examples:

- If `1` becomes available, prime-result reasoning changes because `1 x prime` becomes possible.
- If repeated strip values become common, copy must clarify strip entry identity.
- If value ranges become larger, factor-check guidance may need to avoid implying easy mental arithmetic.
- If new operations are introduced, both tips must be rewritten or removed.
