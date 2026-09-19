# Static Analysis

Android Lint, the Kotlin compiler, and Spotless do not cover every IntelliJ inspection. Treat IDE
analysis as a complementary quality signal for changed files rather than claiming that a green
Gradle build guarantees zero IDE warnings.

- Fix a warning in source when the correction makes intent or type compatibility clearer.
- Use `@Suppress` or `@SuppressWarnings` only for a verified false positive that cannot be expressed
  cleanly in code. Keep the suppression on the narrowest declaration or expression, use the exact
  inspection id, and make the reason evident from the test or declaration.
- Do not use file-wide or broad suppression to hide unused code, ignored immutable results,
  redundant SAM constructors, or ambiguous assertion overloads.
- With JUnit 4 and Kotlin value classes, compare the unwrapped primitive or stable property when a
  nullable value-class result can make IntelliJ see boxed and unboxed `assertEquals` operands as
  inconvertible. Prefer a focused domain test assertion when the pattern recurs.
- Remove unused parameters and imports. If a framework-mandated signature makes a parameter
  intentionally unused, express that through the supported callback shape or apply a narrow
  suppression only when renaming it to `_` is unavailable.
- Omit explicit SAM constructors when the expected functional-interface type is already known.
- Assign, return, or assert the result of immutable operations such as data-class `copy`; invoking
  them without consuming the returned value does not change the original object.

The [IntelliJ inspection pilot](intellij-inspections-pilot.md) evaluates Qodana Community for Android
in pull requests. It is informational, independent of `Build and Validate`, and does not change the
required-check policy. Promotion to enforcement requires a separate decision based on measured
compatibility, useful findings, false positives, runtime, and cache cost.
