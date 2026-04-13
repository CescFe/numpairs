# ADR-002: Use Jetpack Compose for UI

## Context
We need to implement the UI layer for the NumPairs application.

Options considered:
- XML-based layouts (View system)
- Jetpack Compose

## Decision
We will use Jetpack Compose for UI development.

## Rationale
- Modern Android UI toolkit
- Declarative approach simplifies UI state management
- Faster iteration for UI-heavy development
- Better suited for dynamic layouts (grid, number strip)
- Strong integration with Kotlin

## Consequences

### Positive
- Less boilerplate code
- Easier UI previews and iteration
- Cleaner state-driven UI

### Negative
- Learning curve
- Some APIs still evolving compared to View system
- Requires modern Android tooling

## Future Considerations
If performance or specific UI limitations arise, we may:
- Mix Compose with Views if necessary