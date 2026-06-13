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

This tip should be documented and implemented as low-difficulty-specific. It should be revisited if a future difficulty allows `1`, changes the strip value range, or introduces different operations.

### 2. Narrow A Product By Checking Factors, Then Confirm The Matching Pair's Sum

For a multiplication tile, both operands must be exact factors of the visible result.

After identifying a plausible factor pair, the player should check whether the same two strip entries also explain a matching addition result elsewhere in the grid.

This tip is strongly aligned with current low-difficulty puzzles because the value range is small and the multiplication cap keeps factor checks approachable.

---

## Future Difficulty Guidance

Any future difficulty level should explicitly decide whether these tips still apply.

Examples:

- If `1` becomes available, prime-result reasoning changes because `1 x prime` becomes possible.
- If repeated strip values become common, copy must clarify strip entry identity.
- If value ranges become larger, factor-check guidance may need to avoid implying easy mental arithmetic.
- If new operations are introduced, both tips must be rewritten or removed.
