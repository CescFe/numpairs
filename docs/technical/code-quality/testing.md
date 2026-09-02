# Testing

- Add or update unit tests for new domain rules, invariants, and state transitions.
- Add a regression test for a defect when a stable automated reproduction is feasible.
- Test presentation transformations and ViewModel behavior without requiring Android UI when possible.
- Use Compose or navigation tests for behavior that depends on rendering, semantics, focus, or application routing.
- Test observable behavior and domain outcomes rather than private implementation details.
- Keep tests deterministic. Control randomness, time, persistence, and external state through explicit inputs or suitable fakes.
- Reuse focused builders, mothers, fixtures, and robots when they make intent clearer; do not create a test abstraction that obscures the scenario.
