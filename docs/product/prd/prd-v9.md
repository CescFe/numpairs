# PRD - NumPairs ✨ v9 Game Feel & Personalization

> Delivered product reference for the `v9 - Game Feel & Personalization` milestone.
> References to v7 below describe the product baseline at the start of v9; the v9 success
> criteria now represent implemented behavior. v9 remains independent from, and compatible
> with, the separate future difficulty expansion represented by v8 in the product sequence.

## Product Summary

NumPairs is a native Android arithmetic deduction puzzle. Players complete hidden board expressions using numbers from an ordered strip. Each strip pair produces one addition tile and one multiplication tile, and generated play supports replayable `4 Pairs Low` and `8 Pairs Medium` modes.

v7 made the most recently opened generated puzzle reliably resumable after Android process death. The core loop is now understandable, replayable, and durable, but its moment-to-moment response remains restrained: accepted assignments have no tactile confirmation, newly correct tiles do not react, puzzle completion relies on a static overlay, and replacement puzzles appear without a designed transition.

The application also has one fixed Warm visual palette. Its color roles, typography, shapes, spacing, and elevation provide a coherent baseline, but players cannot personalize the presentation.

v9 adds accessible color personalization and focused generated-game feedback. It does not change puzzle rules, difficulty, progression, or the established interaction model.

---

## Product Goal

Make generated NumPairs play feel responsive and rewarding while giving players a persistent choice of five accessible color themes.

The milestone should add personality and tactile satisfaction without turning the calm deduction experience into a noisy arcade presentation or weakening the meaning of gameplay states.

---

## Problem Statement

The current product has four related experience gaps:

- all players see the same fixed palette even though the visual system can support controlled personalization
- accepted generated-game assignments receive no tactile acknowledgement
- making a tile correct and solving a puzzle produce limited visual reward
- a successfully generated replacement puzzle appears without a transition that communicates the change

Adding palettes directly to the current Material color scheme would also create semantic risk. The Warm implementation currently reuses primary containers for success, so changing the brand primary could silently change the meaning of completed, selected, hidden, tutorial, or error states.

Theme-aware branding introduces a separate Android constraint. In-app Compose content can react to stored preferences, but the system splash and launcher icon are resolved outside normal Compose rendering. Launcher refresh behavior varies, and Android system-themed icons use launcher-controlled colors rather than the application-selected palette.

v9 should solve the controllable product problems, investigate platform boundaries early, and expose honest fallbacks where Android retains control.

---

## Target Users

- Returning players who want NumPairs to reflect a preferred visual character
- Puzzle players who benefit from subtle confirmation that an action was accepted
- Players who expect solving a tile or puzzle to feel rewarding without delaying continued play
- Players who disable system haptics or motion and expect NumPairs to respect that choice
- Players using increased font scale, accessibility services, or launchers with themed icons
- Contributors who need stable separation between appearance palettes and gameplay semantics

---

## Current Baseline At Start Of v9

The implemented baseline is `v7 - Reliable Sessions & Replay Controls`.

That baseline includes:

- mandatory versioned onboarding and voluntary `How to play` replay
- generated `4 Pairs Low` and `8 Pairs Medium`
- one exact resumable generated session shared across generated modes
- safe generated-session replacement only after generation, validation, and storage
- completion actions for `Play another` and `Back to menu`
- a fixed Warm `darkColorScheme`
- shared Inter and JetBrains Mono typography
- shared shapes, component treatments, and semantic state guidance
- static Warm splash, launcher, and in-app color resources
- DataStore-backed local preferences and generated-session persistence
- Compose previews, unit tests, and compiled instrumented test sources

The baseline does not include:

- user-selectable themes
- persisted appearance or haptic preferences
- a personalization destination
- a theme-independent gameplay semantic color layer
- accepted-assignment haptics
- correct-tile motion
- a designed completion celebration
- a visual transition into a validated replacement puzzle
- runtime theme-aware in-app branding, splash, or launcher variants

v9 is numbered after a separate future v8 difficulty milestone. Its implementation must not assume that difficulty work already exists and must not prevent new generated profiles or mode labels from being added later.

---

## Product Principles

- Keep the board as the visual protagonist.
- Personalize appearance without changing game meaning.
- Change colors only; keep typography, shapes, spacing, and elevation stable.
- Keep semantic success, error, selection, tutorial, and hidden roles recognizable in every theme.
- Use motion and haptics to confirm player-caused transitions, not to decorate passive state.
- Keep generated play satisfying but calm, brief, and non-blocking.
- Never replay feedback merely because state was recomposed, recreated, or restored.
- Respect system motion and haptic settings.
- Keep personalization independent from onboarding and generated-session persistence.
- Preserve v7 safe replacement and completion behavior.
- Treat launcher-controlled appearance as best-effort and document fallbacks honestly.
- Keep future difficulty expansion compatible with the theme and feedback contracts.

---

## Core UX Expectations

### Personalization Entry

- Add a `Personalization` destination from the unlocked normal menu.
- Do not expose personalization as a required onboarding step.
- Keep generated-session menu actions and their order unchanged.
- Let players inspect and select any of the five themes.
- Apply a selected theme immediately across the application without resetting navigation, onboarding, or puzzle state.
- Persist the selected theme for subsequent launches.
- Let players enable or disable generated-game haptics.
- Default existing and new installations to Warm.
- Default NumPairs haptics to enabled while still respecting the Android system setting.

### Generated-Game Feedback

- Limit new v9 haptics and motion to generated gameplay.
- Keep required onboarding, voluntary `How to play`, Tutorial, and authored practice feedback unchanged.
- Provide one subtle haptic acknowledgement when a generated-game action commits an accepted strip value, operand assignment, or operator assignment.
- Do not add an error haptic.
- Animate a tile only when a player action changes it from not correct to correct.
- Do not animate already-correct initial content, restored state, recomposition, or unrelated presentation updates.
- Celebrate generated-puzzle completion once for the player action that solved it.
- Keep completion actions usable and avoid blocking navigation or replay.

### Replacement Transition

- Keep the current completed or resumable puzzle visible while its requested successor is pending.
- Do not transition away during generation, validation, or storage.
- Start the replacement transition only after the successor is safely stored and ready to present.
- Keep the current puzzle and its completion surface unchanged when replacement fails or is cancelled.
- Deduplicate replacement requests and transition playback.

---

## v9 Scope

### Theme Model And Color Ownership

Introduce an explicit theme identity with exactly five values:

1. `Warm`
2. `Frost`
3. `Obsidian`
4. `Terminal`
5. `Ember`

Separate colors into two ownership groups.

Theme-dependent appearance colors include:

- app background
- base, raised, and subtle surfaces
- primary and secondary text
- outlines and quiet separators
- brand primary and on-primary content
- brand accent and decorative brand treatments

Gameplay semantic colors include:

- success and completed state
- error and invalid state
- player selection and focus
- tutorial highlight
- hidden and placeholder state
- available and used operator-family indicators

Semantic roles must preserve their meaning and recognizable color family across themes. Exact lightness and container variants may change where required for contrast on light or dark surfaces. A theme must not reinterpret error as a brand accent, success as ordinary selection, or hidden state as completion.

Typography, shapes, spacing, elevation, interaction size, and layout remain shared and invariant.

### Theme Definitions

#### Warm

- Preserve the current warm dark baseline.
- Use deep olive-neutral backgrounds, warm cream text, and jade brand accents.
- Remain the default and safe fallback theme.

#### Frost

- Use very light cool-blue backgrounds and crisp blue surfaces.
- Use blue as the brand primary and restrained gold as a decorative brand accent.
- Do not reuse the gold accent as the tutorial semantic role without a distinct non-color cue and verified separation.

#### Obsidian

- Use deep charcoal and warm-black surfaces with restrained warm highlights.
- Preserve elegant, high-contrast presentation without collapsing hidden, surface, and border roles into the same black.

#### Terminal

- Use black and near-black surfaces with phosphor-inspired green brand accents.
- Keep JetBrains Mono limited to existing puzzle typography; the theme does not change general UI typography.
- Keep semantic success distinguishable from the brand green through container, border, icon, and text treatment.

#### Ember

- Use cream and warm-neutral surfaces with orange and restrained red-orange brand accents.
- Keep semantic error visibly distinct from decorative Ember accents through hue, container, border, icon, and text treatment.

### Persistent Personalization

- Store personalization in application-private preferences independent from onboarding and generated sessions.
- Persist stable theme identity rather than palette values.
- Fall back to Warm for a missing, unknown, or unsupported stored theme id.
- Persist the haptics-enabled preference.
- Let the Android system setting remain authoritative when system touch feedback is disabled.
- Do not persist transient selector state or animation progress.
- Do not synchronize personalization through accounts or remote services.

### Personalization Surface

- Provide an unlocked-menu entry point with accessible label and iconography.
- Present all five theme names and representative palette previews.
- Communicate the currently selected theme without relying only on color.
- Keep every theme option at least as large as the shared touch-target requirement.
- Include a generated-game haptics toggle with clear enabled and disabled semantics.
- Do not include audio, typography, shape, motion, or difficulty controls.
- Preserve screen state and the current generated session while changing preferences.

### Theme-Aware In-App Branding

- Keep the NumPairs symbol geometry unchanged.
- Tint or render in-app NumPairs branding from the active appearance palette.
- Preserve legibility and recognizable shape in all themes.
- Do not generate theme-specific logos with different typography or geometry.

### Splash And Launcher Feasibility

Resolve platform behavior before committing to one implementation.

The v9 decision is recorded in
[ADR-004](../../technical/adr/adr-004-keep-v9-platform-branding-static.md). Android resolves
the system starting window before NumPairs can asynchronously read its DataStore preference,
and component-based launcher variants do not provide sufficiently reliable refresh, recents,
shortcut, and process behavior for this release. v9 therefore keeps the existing Warm
platform splash and launcher assets, preserves the monochrome layer for launcher-controlled
system theming, and applies the selected palette to in-app branding only.

The technical investigation must:

- verify when the stored theme can be resolved relative to Android starting-window creation
- avoid blocking startup on unbounded preference work
- avoid two authoritative theme preferences solely to color the splash
- evaluate static per-theme splash resources and any safe startup selection mechanism
- evaluate adaptive launcher variants and activity-alias switching
- verify fallback, launcher refresh, process, shortcut, and system-themed-icon behavior
- document supported behavior and limitations

Required outcome:

- in-app branding follows the active theme
- startup always has a safe Warm or neutral fallback
- corrupt or unavailable theme state never blocks launch

Investigated target outcome, not supported in v9 under ADR-004:

- a previously selected theme is reflected by the splash on subsequent launches where it can be done reliably

Investigated best-effort outcome, not supported in v9 under ADR-004:

- the launcher exposes a matching adaptive icon variant where the active launcher honors component-icon changes

Android system-themed icons remain controlled by the launcher and the user's system palette. NumPairs must provide a valid monochrome layer and must not claim control over the resulting tint.

### Accepted-Assignment Haptics

- Use action-oriented platform haptic feedback rather than legacy one-shot vibration.
- Do not require the vibration permission for ordinary touch feedback.
- Keep the effect subtle because assignments are frequent.
- Trigger only after an accepted generated-game assignment is committed.
- Do not trigger for opening or dismissing a selector, editing draft text, tapping an unavailable option, resetting a tile, restoration, or state replay.
- Suppress the effect when the NumPairs preference is disabled.
- Let Android suppress it when system touch feedback is disabled.

### Correct-Tile Motion

- Detect the specific tile that transitions to correct because of a committed player action.
- Use one brief, restrained treatment such as scale, border emphasis, or color wash.
- Preserve tile size, touch targets, and board layout.
- Keep the final correct state understandable without motion.
- Do not queue repeated animation for the same state publication.
- Allow a tile that later becomes incorrect and is made correct again by the player to animate for the new transition.

### Completion Celebration

- Refine the existing completion overlay rather than replacing the completion contract.
- Coordinate a brief board-level completion response with the overlay entrance.
- Keep the treatment minimalist and avoid long confetti, full-screen obstruction, or required motion.
- Trigger the celebration once when a generated puzzle becomes solved by a player action.
- Keep `Play another` and `Back to menu` available.
- Do not trigger celebration when displaying restored or preview state.
- Let reduced or disabled system motion reach the completed visual state immediately.

### Safe New-Puzzle Transition

- Coordinate the transition at the generated-play boundary where successor readiness is known.
- Preserve the solved puzzle and completion surface while work is pending.
- Transition only to a successfully adopted successor.
- Avoid animating loading or failure into a state that implies the previous puzzle was already discarded.
- Preserve request deduplication, session identity guards, and safe failure recovery.
- Keep transition state transient and out of generated-session persistence.

---

## Accessibility And Quality

Every theme must validate:

- at least `4.5:1` contrast for small text
- at least `3:1` contrast for large text and meaningful graphics
- minimum `48dp` interactive targets where required by the shared UI contract
- readable puzzle values at supported font scales
- non-color cues for selected, used, hidden, correct, and incorrect states
- system bar icon appearance appropriate for light or dark backgrounds

Automated non-device tests should validate defined foreground/background contrast pairs for all themes. Compose tests should protect selection semantics, touch bounds, settings behavior, and generated-only feedback routing where feasible. Accessibility-check test sources may be added when compatible with the current Compose and API requirements, but local delivery must only compile instrumented tests and must not start an emulator.

Motion must:

- respect the system animation duration scale
- reach the same final state when animation duration is zero
- never be required to discover an action or understand correctness
- avoid blocking completion actions

Haptics must:

- respect the NumPairs preference
- respect the Android system setting
- use supported platform fallbacks
- never become the only confirmation of an accepted action

---

## Stages

### Stage 1 - Theme And Semantic Foundation

Outcome: the current Warm presentation is reproduced through an explicit theme model whose appearance colors are independent from stable gameplay semantic roles.

Work:

1. Introduce stable theme identity and palette ownership.
2. Separate success, error, selection, tutorial, hidden, and usage roles from brand colors.
3. Preserve the Warm baseline without intended visual or behavioral change.
4. Add deterministic contrast validation for the shared role pairs.

### Stage 2 - Five-Theme Personalization

Outcome: players can select, apply, and persist any v9 color theme and control generated-game haptics.

Work:

1. Implement Frost, Obsidian, Terminal, and Ember color schemes.
2. Validate contrast and semantic state separation for all five themes.
3. Add version-tolerant personalization preferences with Warm fallback.
4. Apply the selected theme at the application root.
5. Add the unlocked-menu Personalization destination, theme previews, and haptics toggle.
6. Apply the active palette to in-app branding.

### Stage 3 - Platform Brand Assets

Outcome: splash and launcher behavior is implemented to the reliable extent supported by Android, with verified fallbacks and documented limits.

Decision: [ADR-004](../../technical/adr/adr-004-keep-v9-platform-branding-static.md) records
that the reliable v9 extent is the existing static Warm platform splash and launcher
identity. No runtime splash or launcher component switching remains justified for this
milestone; the selected theme continues from the system splash into theme-aware in-app
branding once the preference is available.

Work:

1. Complete the startup and launcher feasibility investigation.
2. Record the durable platform decision when it affects startup or component identity.
3. Implement reliable selected-theme splash behavior if the investigation supports it.
4. Implement best-effort adaptive launcher variants if lifecycle and launcher behavior remain safe.
5. Preserve monochrome system-themed icon support and Warm or neutral fallback.

### Stage 4 - Generated Assignment And Tile Feedback

Outcome: accepted generated-game assignments feel responsive and newly correct tiles receive one restrained response.

Work:

1. Define player-caused, one-shot generated feedback transitions.
2. Add preference- and system-aware accepted-assignment haptics.
3. Add newly-correct tile motion without changing generic GameScreen behavior for Tutorial or onboarding.
4. Protect recomposition, restoration, reset, and repeated-transition behavior with tests.

### Stage 5 - Completion And Safe Replay Motion

Outcome: solving and replaying a generated puzzle feel rewarding while preserving the v7 session contract.

Work:

1. Refine the existing completion-animation backlog issue into the v9 celebration contract.
2. Coordinate board and overlay completion motion.
3. Add the successor-ready replacement transition.
4. Protect failure, cancellation, duplicate request, reduced-motion, and session-identity behavior.

### Stage 6 - End-To-End Quality And Product Alignment

Outcome: personalization and game feel are verified across themes, modes, restoration, accessibility, and documentation.

Work:

1. Add cross-theme previews and targeted Compose coverage.
2. Verify `4 Pairs Low` and `8 Pairs Medium` generated feedback.
3. Verify Tutorial and onboarding remain unaffected.
4. Verify personalization never changes or replaces generated sessions.
5. Align visual-system, UI-behavior, branding, and current-product documentation.
6. Run milestone-level formatting, unit, lint, and instrumented-test compilation validation.

---

## Out Of Scope

- Audio, music, or sound effects
- Error, rejection, warning, or invalid-state haptics
- New haptics or motion in required onboarding, `How to play`, Tutorial, or authored practice
- Theme-dependent typography, font family, font size, shapes, spacing, elevation, or layout
- Automatic theme switching based on time, wallpaper, system light/dark mode, or generated mode
- Downloadable, unlockable, paid, or progression-gated themes
- Theme editing or custom color pickers
- New puzzle rules, operators, sizes, profiles, or difficulty levels
- Scoring, timers, streaks, achievements, statistics, or progression
- Changes to generated-session ownership or persistence semantics
- Guaranteed launcher-icon refresh on every launcher
- Application control over Android system-themed icon tint
- Long, blocking, high-energy, or gameplay-obscuring animation
- Persisted animation progress or transient feedback events

---

## Success Criteria

- Warm, Frost, Obsidian, Terminal, and Ember are selectable and persist across launches.
- Warm remains the default and safe fallback.
- Themes change colors only.
- Typography, shapes, spacing, elevation, layout, rules, and touch geometry remain stable.
- Brand and surface colors can vary without changing the meaning of success, error, selection, tutorial, hidden, or usage states.
- Every defined theme role pair passes the documented contrast thresholds.
- Theme selection and haptic preferences remain independent from onboarding and generated sessions.
- In-app NumPairs branding follows the active palette.
- Splash and launcher behavior matches the documented supported extent and always has a safe fallback.
- Android system-themed icons remain valid without claiming application-controlled tint.
- Accepted generated-game assignments receive subtle haptic confirmation when both app and system settings allow it.
- A tile responds once when a player action makes it correct.
- Solving a generated puzzle produces one brief, non-blocking celebration.
- Tutorial, onboarding, and authored practice receive no new v9 feedback.
- Audio and error haptics are absent.
- Recomposition, process restoration, and unrelated state updates do not replay feedback.
- `Play another` keeps the current solved puzzle visible until a successor is generated, validated, stored, and ready.
- Failed or cancelled replacement preserves the existing puzzle and completion surface.
- Reduced or disabled system motion reaches the same usable final states without delay.
- `4 Pairs Low`, `8 Pairs Medium`, onboarding, Tutorial, generated-session restoration, and future difficulty compatibility remain coherent.
