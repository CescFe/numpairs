# NumPairs Visual Direction

## Purpose

This document preserves the v1 visual identity direction for NumPairs and records how that
identity is applied by the current v9 product.

The original goal was to establish a simple, reusable, and coherent visual baseline for:

- the app logo
- the Android launcher icon
- Splash Screen usage
- documentation and repository assets

The symbol and shape decisions belong to the `v1 - Product Polish & Technical Hardening`
milestone. The current color and platform-branding contract belongs to
`v9 - Game Feel & Personalization`.

## Current v9 Branding Contract

- The NumPairs symbol geometry remains unchanged.
- In-app logo and brand treatments follow the selected Warm, Frost, Obsidian, Terminal, or
  Ember appearance palette.
- The Android system splash and adaptive, round, and legacy launcher assets remain static
  and Warm.
- The adaptive icon includes a monochrome NumPairs layer. Android and the active launcher,
  not NumPairs, choose the tint shown when system-themed icons are enabled.
- NumPairs does not switch launcher components or block startup to project the stored theme
  into platform-owned branding.

The platform boundary and its rationale are recorded in
[ADR-004](../technical/adr/adr-004-keep-v9-platform-branding-static.md).

---

## Asset References

- Source of truth for the reusable app asset: [`ic_numpairs.xml`](../../app/src/main/res/drawable/ic_numpairs.xml)
- PNG preview used in documentation: [`numpairs-logo-preview.png`](../assets/numpairs-logo-preview.png)

The PNG is included here because standard Markdown renderers can preview PNG assets directly, while Android `VectorDrawable` XML files are not rendered as images in repository previews.

---

## Product Context

NumPairs is a native Android number puzzle game.

The core interaction is based on solving puzzle tiles by completing number expressions that match a target result.

The v1 prototype used Material 3 light/dark adaptation and Android dynamic colors. The
current product instead uses five deliberate, app-owned color themes. Across that evolution,
the identity continues to be based primarily on:

- recognizable shapes
- simple composition
- strong legibility
- clear connection to the gameplay

---

## Visual Identity Principle

The NumPairs identity should be **shape-first**, not color-first.

The visual language should communicate:

- unknown values
- mathematical operators
- puzzle solving
- expression composition
- logical deduction

The identity should remain recognizable regardless of:

- the selected NumPairs color theme
- a light or dark palette
- launcher-controlled monochrome tint
- future UI refinements

Color can strengthen the in-app identity, but it must not replace the recognizable symbol.

---

## Primary Logo Direction

### Core Concept

The v1 NumPairs logo is based on the concept of an unresolved mathematical expression.

The logo intentionally avoids representing a specific solved Tile or a specific numeric result because puzzle values vary between games.

Instead, the logo focuses on the universal structure shared by all puzzles:

```text
? [+×] ?
```

The question marks represent unknown operands.

The central vertical operator element represents the two core game operations:

- addition (`+`)
- multiplication (`×`)

This creates a logo that communicates:

- unknown values
- mathematical reasoning
- multiple possible operations
- puzzle resolution mechanics

without depending on any specific puzzle example.

---

## Logo Composition

![NumPairs logo preview](../assets/numpairs-logo-preview.png)

This preview uses the exported PNG for repository rendering. The reusable in-app asset remains the vector source at [`ic_numpairs.xml`](../../app/src/main/res/drawable/ic_numpairs.xml).

The logo is composed of exactly three visual elements arranged horizontally:

1. Left operand:
   - rounded question mark (`?`)
2. Central operator group:
   - vertically stacked `+`
   - vertically stacked `×`
3. Right operand:
   - rounded question mark (`?`)

The central operand group should visually read as a single combined symbol.

The logo should feel:

- balanced
- minimal
- modern
- geometric
- highly legible

The composition should resemble a symbolic puzzle mark rather than a complete mathematical equation.

---

## Visual Style

The source logo should support a minimalist monochrome presentation. In-app instances may
use the active v9 appearance colors without changing the mark.

Recommended characteristics:

- flat design
- no gradients
- no shadows
- no outlines
- no decorative containers
- no tile borders
- no divider lines
- strong visual contrast
- rounded geometric symbol shapes

The design should work correctly in:

- black on white
- white on black
- monochrome environments
- adaptive Android contexts

---

## Typography Direction

The symbol itself acts as the primary logo mark.

If a wordmark is used alongside the symbol (`NumPairs`), it should follow these principles:

- clean
- modern
- rounded or slightly soft
- highly readable
- not overly playful
- not overly technical

The visual tone should balance:

- casual puzzle game
- polished product
- clarity-first interface

---

## Color Direction

NumPairs uses five app-owned, color-only themes: Warm, Frost, Obsidian, Terminal, and Ember.
In-app branding uses the selected theme's brand colors while the mark remains recognizable
independently of any one palette.

The logo may remain black on white in documentation and monochrome in platform assets where
appropriate.

The shape and composition are considered more important than palette consistency.

---

## Launcher Icon Direction

The Android launcher icon should reuse the same symbolic language as the primary logo.

Android packages adaptive, round, legacy, and monochrome launcher forms of the mark.

The launcher asset should not reuse the full white documentation canvas as-is. Instead, the adaptive icon foreground should use the symbol-only mark, centered with safe padding so it remains legible across Android launcher masks.

The monochrome launcher layer reuses the same simplified symbol. Android launchers control
its themed tint from the user's system palette, so that tint is not expected to match the
selected in-app NumPairs theme.

For v9, all packaged launcher variants remain static and Warm. Runtime component or
activity-alias switching is intentionally not used.

---

## Splash Screen Direction

The platform Splash Screen remains simple:

Recommended composition:

```
[NumPairs Symbol]

NumPairs
```

The Splash Screen should prioritize:

- clarity
- immediate recognition
- fast loading perception

For v9, Android shows a static Warm system splash before Compose can resolve the DataStore
theme preference. Once in-app content appears, its branding follows the selected palette.
The startup path does not synchronously wait for preferences, and it does not promise a
theme-matched platform splash.

---

## Accessibility and Legibility Requirements

The logo and icon should remain understandable at small sizes.

The design should prioritize:

- strong contrast
- simple geometry
- minimal detail
- readable symbols
- balanced spacing
- recognizable silhouette

The central operator group must remain distinguishable even at reduced sizes.
