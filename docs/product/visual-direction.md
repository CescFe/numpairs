# NumPairs Visual Direction

## Purpose

This document defines the initial visual identity direction for NumPairs.

The goal is not to create a final brand system, but to establish a simple, reusable, and coherent visual baseline for:

- the app logo
- the Android launcher icon
- future Splash Screen usage
- documentation and repository assets

This visual direction belongs to the `v1 - Product Polish & Technical Hardening` milestone.

---

## Asset References

- Source of truth for the reusable app asset: [`ic_numpairs.xml`](../../app/src/main/res/drawable/ic_numpairs.xml)
- PNG preview used in documentation: [`numpairs-logo-preview.png`](../assets/numpairs-logo-preview.png)

The PNG is included here because standard Markdown renderers can preview PNG assets directly, while Android `VectorDrawable` XML files are not rendered as images in repository previews.

---

## Product Context

NumPairs is a native Android number puzzle game.

The core interaction is based on solving puzzle tiles by completing number expressions that match a target result.

The current prototype uses Material 3 with light/dark theme adaptation and dynamic colors on Android 12+.

Because of this, the visual identity should not depend on a fixed app color palette at this stage.

Instead, the identity should be based primarily on:

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

- light mode
- dark mode
- dynamic Material colors
- future UI refinements

At this stage, the visual system intentionally avoids depending on one fixed brand palette.

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

The v1 logo should follow a minimalist monochrome style.

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

NumPairs currently relies on Android dynamic theming.

Because of this, the visual identity should remain recognizable independently of one mandatory palette.

The logo should primarily exist as black characters on a white background in documentation and as a monochrome mark in product assets when appropriate.

The shape and composition are considered more important than palette consistency.

---

## Launcher Icon Direction

The Android launcher icon should reuse the same symbolic language as the primary logo.

An adaptive icon version should be prepared for Android.

The launcher asset should not reuse the full white documentation canvas as-is. Instead, the adaptive icon foreground should use the symbol-only mark, centered with safe padding so it remains legible across Android launcher masks.

If a monochrome launcher layer is provided, it should reuse the same simplified symbol.

---

## Splash Screen Direction

For v1, the Splash Screen should remain simple.

Recommended composition:

```
[NumPairs Symbol]

NumPairs
```

The Splash Screen should prioritize:

- clarity
- immediate recognition
- fast loading perception

A future iteration may introduce subtle animation, transitioning from an unresolved expression to a resolved one.

Animations should remain elegant and minimal.

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
