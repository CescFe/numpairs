# Standard Completion Celebration Copy

> Status: proposed for issue [#714](https://github.com/CescFe/numpairs/issues/714).

## Purpose

This document proposes the localized copy catalog for the standard success overlay shown after a
player solves a Quick, Classic, or non-record Daily puzzle.

The catalog gives completion feedback more variety and warmth without making claims that require
performance data. It does not own runtime selection, Android resource names, visual styling,
personal-record presentation, or Tutorial completion copy.

The product tone and presentation remain governed by the
[visual design system](./visual-design-system.md) and [UX decisions](./ux-decisions.md).

## Catalog Contract

- A variant is one indivisible title and supporting-text pair. Titles and supporting text from
  different variants must never be combined.
- Stable identifiers express content identity and eligibility; they are not Android resource
  names.
- General variants are eligible for every supported Low, Medium, and Hard generated challenge.
- `MEDIUM_SHARP_THINKING` is eligible only for Medium.
- `HARD_IMPRESSIVE` is eligible only for Hard.
- Low intentionally has no exclusive variant. It receives the complete general pool without copy
  that frames Low as a beginner or lesser achievement.
- Every statement follows directly from the solved state. No variant implies a personal record,
  completion duration, movement count, correction-free attempt, streak, or comparative rank.
- Daily personal records and Tutorial completions keep their purpose-specific copy outside this
  catalog.

The proposed catalog contains six general variants and two difficulty-specific variants, for a
total of eight.

## General Variants

### `GREAT_WORK`

Intent: warm recognition that connects the result with the player's reasoning.

| Locale            | Title             | Supporting text                   |
|-------------------|-------------------|-----------------------------------|
| English           | Great work!       | Your logic paid off.              |
| Spanish           | ¡Buen trabajo!    | Tu lógica ha dado sus frutos.     |
| Valencian/Catalan | Molt bon treball! | La teua lògica ha donat resultat. |
| German            | Gut gemacht!      | Deine Logik hat sich ausgezahlt.  |

### `EXCELLENT`

Intent: direct, polished praise grounded in finding the complete solution.

| Locale            | Title         | Supporting text                           |
|-------------------|---------------|-------------------------------------------|
| English           | Excellent!    | You found the complete solution.          |
| Spanish           | ¡Excelente!   | Has dado con la solución completa.        |
| Valencian/Catalan | Excel·lent!   | Has trobat la solució completa.           |
| German            | Hervorragend! | Du hast die vollständige Lösung gefunden. |

### `NICELY_SOLVED`

Intent: acknowledge the quality of the solve while restating the completed pairing outcome.

| Locale            | Title               | Supporting text                  |
|-------------------|---------------------|----------------------------------|
| English           | Nicely solved!      | Every pair fell into place.      |
| Spanish           | ¡Muy bien resuelto! | Todas las parejas han encajado.  |
| Valencian/Catalan | Molt ben resolt!    | Totes les parelles han encaixat. |
| German            | Clever gelöst!      | Alle Paare passen zusammen.      |

### `NAILED_IT`

Intent: a slightly more playful reward that remains appropriate for a calm puzzle experience.

| Locale            | Title            | Supporting text              |
|-------------------|------------------|------------------------------|
| English           | You nailed it!   | The whole puzzle checks out. |
| Spanish           | ¡Lo has clavado! | Todo el puzle encaja.        |
| Valencian/Catalan | Ho has clavat!   | Tot el puzle encaixa.        |
| German            | Volltreffer!     | Das ganze Puzzle geht auf.   |

### `BRILLIANT`

Intent: strong but restrained praise focused on the finished solution.

| Locale            | Title       | Supporting text                         |
|-------------------|-------------|-----------------------------------------|
| English           | Brilliant!  | Your solution fits perfectly.           |
| Spanish           | ¡Brillante! | Tu solución encaja a la perfección.     |
| Valencian/Catalan | Brillant!   | La teua solució encaixa a la perfecció. |
| German            | Großartig!  | Deine Lösung passt perfekt.             |

### `KEEP_IT_UP`

Intent: forward-looking encouragement after acknowledging the solved puzzle through the overlay.

| Locale            | Title          | Supporting text                           |
|-------------------|----------------|-------------------------------------------|
| English           | Keep it up!    | The next challenge awaits.                |
| Spanish           | ¡Sigue así!    | El siguiente reto ya te espera.           |
| Valencian/Catalan | Continua així! | El pròxim repte ja t’espera.              |
| German            | Weiter so!     | Die nächste Herausforderung wartet schon. |

## Difficulty-Specific Variants

### `MEDIUM_SHARP_THINKING`

Eligibility: Medium only.

Intent: recognize the greater deductive demand of a Medium challenge without comparing the player
with anyone else.

| Locale            | Title             | Supporting text                                         |
|-------------------|-------------------|---------------------------------------------------------|
| English           | Sharp thinking!   | You mastered a demanding challenge.                     |
| Spanish           | ¡Gran deducción!  | Has superado un reto exigente.                          |
| Valencian/Catalan | Gran deducció!    | Has superat un repte exigent.                           |
| German            | Stark kombiniert! | Du hast eine anspruchsvolle Herausforderung gemeistert. |

### `HARD_IMPRESSIVE`

Eligibility: Hard only.

Intent: give the strongest standard praise in the catalog. Spanish uses the approved cultural
reference; other locales preserve the reward intensity with natural copy instead of reproducing
the wordplay literally.

| Locale            | Title            | Supporting text                                   |
|-------------------|------------------|---------------------------------------------------|
| English           | Impressive!      | You conquered a formidable puzzle.                |
| Spanish           | ¡Im-presionante! | En dos palabras.                                  |
| Valencian/Catalan | Impressionant!   | Has superat un puzle ben difícil.                 |
| German            | Beeindruckend!   | Du hast ein besonders schweres Puzzle gemeistert. |

## Localization Guidance

- Localize intent rather than word order. A locale may use a different idiom when a literal
  translation would sound unnatural.
- Keep titles concise, energetic, and suitable for the overlay headline role.
- Keep supporting text to one short sentence. `HARD_IMPRESSIVE` in Spanish is the deliberate
  exception whose supporting text completes the cultural reference.
- Preserve the established player-facing terms `puzzle` in English and German, `puzle` in Spanish,
  and `puzle` in Valencian/Catalan.
- Spanish uses opening and closing exclamation marks. Valencian/Catalan follows the current app
  convention of a closing exclamation mark for these short titles.
- Valencian/Catalan copy favors the existing Valencian voice, including `faena`, `teua`, and
  `pròxim`.

## Spanish Daily Agreement

When `Daily` stands alone as the name of the experience, Spanish treats it as feminine because the
implied noun is `partida` or `prueba`:

- `Daily completada`
- `La Daily de hoy está completada`

When an explicit noun is present, that noun controls agreement:

- `Reto Daily completado`
- `El reto Daily de hoy está completado`
- `Sesión Daily completada`

The fixed brand expression `NumPairs Daily` is not inflected.

## Deferred Implementation

Issue #714 defines and reviews this catalog only. Runtime selection, Android string resources, the
standard check badge, and Spanish resource corrections belong to the dependent implementation
issue. Performance-based variants for elapsed time, corrections, and personal records remain
outside this catalog and require their own authoritative completion data.
