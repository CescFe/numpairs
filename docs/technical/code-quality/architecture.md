# Architecture

## Domain Modeling

- Use the terminology defined in [the ubiquitous language](../../ubiquitous-language.md) in production code, tests, issues, and technical documentation.
- Treat [the game rules](../../game-rules.md) as the behavioral source of truth. Resolve ambiguity before encoding a new rule.
- Keep game rules and invariants in `domain` rather than duplicating them in ViewModels, routes, or Composables.
- Keep the domain model independent from Android, Compose, persistence frameworks, `data`, `feature`, and `ui`.
- Model identity explicitly when behavior depends on which game element was selected. Preserve the stable strip-entry identity established by [ADR-003](../adr/adr-003-use-stable-strip-entry-identity.md).
- Prefer typed states, outcomes, and violations when callers must distinguish business cases. Do not replace meaningful domain distinctions with loosely related booleans, nullable values, or display strings.
- Use a domain service when behavior spans concepts and does not naturally belong to one model. Do not create service or use-case wrappers that only forward a call.
- Keep invariants in one authoritative location and test them there.

## Architectural Boundaries

Respect the responsibilities already established in the repository:

- `domain`: puzzle models, rules, assignments, validation, profiles, and generation logic
- `data`: persistence, platform-backed state, and seed data
- `feature`: feature coordination, presentation state, routes, and feature-specific UI
- `ui/theme`: shared visual tokens and reusable design-system components
- `ui/navigation`: application composition and navigation between features

Dependencies should preserve these boundaries:

- `domain` must remain platform-independent.
- `data` must not depend on feature or UI code.
- Features may coordinate domain, data, and shared UI concerns, but business rules must remain in the domain.
- Composables must not become an alternative domain or persistence layer.
- Platform and persistence types should be translated at their boundary instead of leaking into domain APIs.

Do not reorganize packages merely to make the project resemble a generic architecture diagram. When
a cross-cutting change revises an established boundary or introduces a durable architectural
decision, add or update an ADR.
