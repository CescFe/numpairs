# Code Quality Guidelines

This document is the canonical technical quality guidance for NumPairs production and test code. Apply it in proportion to the change and prefer concrete improvements to ceremonial compliance.

## Engineering Approach

NumPairs uses DDD-inspired domain modeling, not strict Domain-Driven Design or a prescribed Clean Architecture implementation.

The goal is to express game concepts, rules, identities, and state transitions clearly. Patterns such as repositories, factories, use cases, services, entities, or value objects are tools to introduce when they solve a demonstrated problem, not mandatory layers for every feature.

Prioritize:

1. correct game behavior and preserved invariants
2. clear domain language and ownership
3. simple, testable state transitions
4. consistency with the existing codebase
5. abstraction only when its benefit is concrete

## Domain Modeling

- Use the terminology defined in [the ubiquitous language](../ubiquitous-language.md) in production code, tests, issues, and technical documentation.
- Treat [the game rules](../game-rules.md) as the behavioral source of truth. Resolve ambiguity before encoding a new rule.
- Keep game rules and invariants in `domain` rather than duplicating them in ViewModels, routes, or Composables.
- Keep the domain model independent from Android, Compose, persistence frameworks, `data`, `feature`, and `ui`.
- Model identity explicitly when behavior depends on which game element was selected. Preserve the stable strip-entry identity established by [ADR-003](adr/adr-003-use-stable-strip-entry-identity.md).
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

Do not reorganize packages merely to make the project resemble a generic architecture diagram. When a cross-cutting change revises an established boundary or introduces a durable architectural decision, add or update an ADR.

## Clean Code

- Name code after its domain responsibility rather than its implementation mechanism.
- Keep functions, classes, and files cohesive and focused on one reason to change.
- Prefer straightforward control flow, guard clauses, and explicit state transitions over deeply nested or implicit behavior.
- Prefer immutable models and copy-based state changes unless mutation has a measured and documented benefit.
- Keep public APIs as small as the supported behavior allows.
- Extract shared code only when the abstraction has a stable meaning. Similar-looking code is not automatically the same concept.
- Comments are strongly discouraged. If you need a comment, you probably need a refactor. Explain surprising constraints or tradeoffs in comments.
- Remove obsolete code introduced by the current change, but avoid unrelated cleanup or speculative refactors.
- Follow the existing formatting and naming conventions enforced by the repository tooling.

## Design Patterns And Abstractions

- Prefer an existing project pattern when it already fits the problem.
- Introduce a new pattern only when it improves at least one concrete concern: domain expression, dependency ownership, variation handling, state management, or testability.
- Use the smallest pattern that solves the problem. Avoid additional interfaces, factories, repositories, strategies, or use cases with only one trivial implementation and no boundary to protect.
- Do not hide simple domain behavior behind generic infrastructure terminology.
- Make a non-obvious local pattern understandable through names and focused documentation.
- Record cross-cutting or long-lived architectural patterns in an ADR, including the alternatives and consequences.

Design-pattern usage is not a quality target by itself. A simpler implementation is preferable when it remains cohesive, correctly located, and easy to test.

## Kotlin And Compose

- Represent closed state families with sealed types or enums when exhaustive handling prevents invalid behavior.
- Use value types or validated construction when raw primitives cannot express an important domain constraint.
- Keep mapping between domain and presentation state explicit and testable.
- Let routes and ViewModels coordinate dependencies and durable state. Prefer content Composables that receive state and emit events.
- Keep transient UI state local only when it has no domain or navigation significance.
- Keep side effects explicit and lifecycle-aware; do not trigger durable work directly from composition.
- Use shared theme tokens and components before adding feature-local visual constants.
- Put user-facing text in Android resources and preserve accessibility semantics for interactive behavior.

## Testing

- Add or update unit tests for new domain rules, invariants, and state transitions.
- Add a regression test for a defect when a stable automated reproduction is feasible.
- Test presentation transformations and ViewModel behavior without requiring Android UI when possible.
- Use Compose or navigation tests for behavior that depends on rendering, semantics, focus, or application routing.
- Test observable behavior and domain outcomes rather than private implementation details.
- Keep tests deterministic. Control randomness, time, persistence, and external state through explicit inputs or suitable fakes.
- Reuse focused builders, mothers, fixtures, and robots when they make intent clearer; do not create a test abstraction that obscures the scenario.
