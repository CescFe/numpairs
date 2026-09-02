# Engineering Principles

NumPairs uses DDD-inspired domain modeling, not strict Domain-Driven Design or a prescribed Clean
Architecture implementation.

The goal is to express game concepts, rules, identities, and state transitions clearly. Patterns
such as repositories, factories, use cases, services, entities, or value objects are tools to
introduce when they solve a demonstrated problem, not mandatory layers for every feature.

Prioritize:

1. correct game behavior and preserved invariants
2. clear domain language and ownership
3. simple, testable state transitions
4. consistency with the existing codebase
5. abstraction only when its benefit is concrete

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

Design-pattern usage is not a quality target by itself. A simpler implementation is preferable when
it remains cohesive, correctly located, and easy to test.
