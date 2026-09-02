# Standard Completion Celebration Copy

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
- `KEEP_IT_UP` is reserved for a well-performing, non-record Quick Low completion.
- `CORRECTION_FREE` is reserved for a Medium or Hard completion whose authoritative correction
  count is known to be zero.
- `MEDIUM_HARD_IMPRESSIVE` is eligible only for Medium and Hard.
- `HARD_UNSTOPPABLE` is eligible only for Hard.
- Low receives the complete general pool. Its additional `KEEP_IT_UP` variant invites the player
  to try a greater challenge without framing Low itself as a lesser achievement.
- With the explicit exception of `CORRECTION_FREE`, no variant makes a factual claim beyond the
  solved state and configured difficulty. Rhetorical encouragement and narrator personification
  are expressive rather than performance data. No catalog variant implies a personal record,
  completion duration, movement count, streak, or comparative rank.
- Daily personal records and Tutorial completions keep their purpose-specific copy outside this
  catalog.

The implemented catalog contains five general variants and four context-specific variants, for a
total of nine.

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

### `YOU_ROCK`

Intent: address the player directly with a warmer, more personal celebration. Each localization
uses a natural equivalent instead of preserving the English rock idiom literally.

| Locale            | Title            | Supporting text                |
|-------------------|------------------|--------------------------------|
| English           | You rock!        | That was an impressive solve.  |
| Spanish           | ¡Eres increíble! | Menuda forma de resolverlo.    |
| Valencian/Catalan | Eres increïble!  | Quina manera de resoldre’l.    |
| German            | Du bist spitze!  | Das war wirklich stark gelöst. |

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

## Context-Specific Variants

### `CORRECTION_FREE`

Eligibility: Medium and Hard only, when the authoritative Puzzle Correction Count is known to be
zero and the completion is not presented as a personal record.

Intent: truthfully recognize solving a more demanding puzzle without rectifying any earlier
durable move. Personal-record presentation remains higher priority. Low, positive-count, and
unknown-count completions continue through the otherwise eligible standard catalog.

| Locale            | Title       | Supporting text                                          |
|-------------------|-------------|----------------------------------------------------------|
| English           | Flawless!   | You solved it without correcting a single move.          |
| Spanish           | ¡Impecable! | Lo resolviste sin corregir una sola jugada.               |
| Valencian/Catalan | Impecable!  | L’has resolt sense corregir ni una sola jugada.           |
| German            | Makellos!   | Du hast es gelöst, ohne einen einzigen Zug zu korrigieren. |

### `KEEP_IT_UP`

Eligibility: Quick Low only, when the completion is not presented as a personal record and the
attempt meets the separately defined good-performance rule.

Intent: invite a player whose Low attempt went well to consider increasing the selected difficulty.
The prompt addresses the next choice without describing Low as introductory or easy.

| Locale            | Title          | Supporting text                              |
|-------------------|----------------|----------------------------------------------|
| English           | Keep it up!    | Ready for a tougher challenge?               |
| Spanish           | ¡Sigue así!    | ¿Te atreves a subir la dificultad?           |
| Valencian/Catalan | Continua així! | T’atrevixes a pujar la dificultat?           |
| German            | Weiter so!     | Bereit für einen höheren Schwierigkeitsgrad? |

### `MEDIUM_HARD_IMPRESSIVE`

Eligibility: Medium and Hard only.

Intent: provide a playful reward for the greater deductive demand of Medium and Hard. Spanish uses
the approved cultural reference; other locales preserve its warmth and intensity with natural
copy rather than reproducing the wordplay literally.

| Locale            | Title            | Supporting text                      |
|-------------------|------------------|--------------------------------------|
| English           | Impressive!      | That took some serious thinking.     |
| Spanish           | ¡Im-presionante! | En dos palabras.                     |
| Valencian/Catalan | Impressionant!   | Això sí que ha requerit pensar.      |
| German            | Beeindruckend!   | Dafür war echtes Köpfchen gefragt.   |

### `HARD_UNSTOPPABLE`

Eligibility: Hard only.

Intent: give the strongest standard praise in the catalog through a conversational narrator voice.
The rhetorical first person is playful personification, not a claim about a real person or the
developer's performance.

| Locale            | Title                  | Supporting text                                      |
|-------------------|------------------------|------------------------------------------------------|
| English           | Nothing can stop you!  | And I thought this difficulty was impossible!        |
| Spanish           | ¡No hay quien te pare! | ¡Y yo que pensaba que esta dificultad era imposible! |
| Valencian/Catalan | No hi ha qui et pare!  | I jo que pensava que esta dificultat era impossible! |
| German            | Dich hält nichts auf!  | Und ich dachte schon, das wäre unlösbar!             |

## Good-Performance Eligibility

`KEEP_IT_UP` is a reserved contextual variant rather than an unconditional Low message. Runtime
activation still waits for authoritative elapsed-time support in normal generated play and
calibrated thresholds. Authoritative normal-play correction data is now available, but no
correction threshold is selected by this document.

The later selection rule should follow these constraints:

- Personal-record presentation always has priority. `KEEP_IT_UP` is eligible only for the standard
  non-record overlay.
- A first timed baseline, tie, or slower personal result may still qualify when the attempt meets
  the objective good-performance rule.
- Either a known good completion time or a known low correction count may qualify the attempt.
- A good-time threshold must be calibrated independently for `3 Pairs Low` and `4 Pairs Low`;
  their expected completion times are not interchangeable.
- Correction eligibility must use the authoritative correction count rather than Daily Movement
  Count or a puzzle-state inference.
- An unknown metric cannot satisfy its side of the rule. The other known metric may still qualify
  the attempt.
- Daily Low does not use this prompt because its next difficulty is selected by the Daily calendar,
  not by the player. Classic has no Low challenge.

This copy specification intentionally does not choose the numeric time or correction thresholds.
Those values require explicit calibration in the issue that activates the performance-based
selection rule.

## Localization Guidance

- Localize intent rather than word order. A locale may use a different idiom when a literal
  translation would sound unnatural.
- Keep titles concise, energetic, and suitable for the overlay headline role.
- Keep supporting text to one short sentence. `MEDIUM_HARD_IMPRESSIVE` in Spanish is the deliberate
  exception whose supporting text completes the cultural reference.
- Preserve the established player-facing terms `puzzle` in English and German, `puzle` in Spanish,
  and `puzle` in Valencian/Catalan.
- Spanish uses opening and closing exclamation marks. Valencian/Catalan follows the current app
  convention of a closing exclamation mark for these short titles.
- Valencian/Catalan copy favors the existing Valencian voice, including `teua` and `pròxim`.

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

Issue #714 originally defined and reviewed this catalog. Runtime selection, Android string
resources, the standard check badge, and Spanish resource corrections were added by dependent
implementation work. `CORRECTION_FREE` is now implemented from authoritative correction data.
Activating `KEEP_IT_UP` still depends on authoritative normal-play elapsed time plus calibrated
good-performance thresholds. Personal-record presentation remains outside this catalog.
