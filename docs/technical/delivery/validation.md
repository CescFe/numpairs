# Validation

Validate changes in proportion to their risk and record relevant evidence in the Pull Request.
Documentation and application changes have different validation requirements.

## All Changes

- Review `git diff` for scope, correctness, and consistency.
- Run `git diff --check`.
- Re-check the issue's acceptance criteria against the implemented result.

## Android Validation

For application changes, run the relevant tasks sequentially:

```text
./gradlew spotlessApply testDebugUnitTest spotlessCheck compileDebugAndroidTestKotlin lintDebug
```

- Run `lintDebug` for every Android or Kotlin production or test change, stage or milestone
  completion, and other change with broader Android risk.
- Inspect each affected module's `build/reports/lint-results-debug.*`, even when the task succeeds;
  success can still include warnings.
- For warnings in changed files, fix each safe warning or record the warning and why it remains.
- Do not expand the atomic issue for warnings outside the current diff; report material
  pre-existing findings separately.
- Treat Android Lint and IDE-only IntelliJ inspections separately; Gradle validation does not cover
  IDE-only warnings.

Compile instrumented tests only; do not start an emulator or run connected-device tasks as part of
the standard validation workflow.

For Compose UI changes, include a design-system consistency pass:

- inspect newly introduced or configured direct Material components
- inspect feature-local shapes, colors, typography, and other visual styling
- compare each affected visual role with `NumPairsComponents` and the nearest analogous UI
- record any intentional deviation from an established shared component or token in the Pull Request

## Documentation-Only Validation

For documentation-only changes, validate Markdown structure, relative links, consistency, and
`git diff --check`. Skip Android build tasks unless the documentation change affects build
configuration or executable examples.
