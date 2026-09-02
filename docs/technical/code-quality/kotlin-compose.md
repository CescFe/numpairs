# Kotlin And Compose

- Represent closed state families with sealed types or enums when exhaustive handling prevents invalid behavior.
- Use value types or validated construction when raw primitives cannot express an important domain constraint.
- Keep mapping between domain and presentation state explicit and testable.
- Let routes and ViewModels coordinate dependencies and durable state. Prefer content Composables that receive state and emit events.
- Keep transient UI state local only when it has no domain or navigation significance.
- Keep side effects explicit and lifecycle-aware; do not trigger durable work directly from composition.
- Use shared theme tokens and components before adding feature-local visual constants.
- Before using or styling a Material component directly, inspect `NumPairsComponents` and the nearest analogous UI for an established NumPairs visual role.
- An action specified as a primary CTA must use `NumPairsComponents.PrimaryCtaButton`; do not substitute a raw Material 3 `Button` for that established role.
- If a shared component cannot express the required behavior, extend its API when the shared role still applies or record in the Pull Request why a direct Material or feature-local implementation is more appropriate.
- Direct Material components remain appropriate when no established NumPairs role applies. Do not introduce a shared wrapper solely for generic uniformity.
- Put user-facing text in Android resources and preserve accessibility semantics for interactive behavior.
