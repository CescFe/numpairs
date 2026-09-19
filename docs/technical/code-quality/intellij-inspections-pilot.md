# IntelliJ inspections PR pilot

Implementation for [#685](https://github.com/CescFe/numpairs/issues/685). The pilot is informational:
findings do not fail a quality gate. Infrastructure failures remain visible as failures of this
independent workflow, not as successful scans with zero warnings. `Build and Validate` and the
repository ruleset are unchanged.

## Configuration and execution

`qodana.yaml` owns the focused inspection profile and JDK 21 selection. The workflow invokes
Community for Android `jetbrains/qodana-jvm-android:2026.2` directly, pinned to the registry manifest
digest (also recorded in the configuration). Keep both image references synchronized when upgrading.
The Docker entrypoint avoids a second downloaded CLI and does not post PR comments or annotations.

PRs targeting `main` run after opening, reopening, synchronization, or leaving draft status. Moving
back to draft cancels the previous run and skips analysis. Checkout uses the actual PR head SHA, not
GitHub's synthetic merge commit. Both commits must exist locally; `git merge-base` must succeed.
Failure never falls back to a whole-project scan. The runner receives explicit `--diff-start` and
`--diff-end`. Configuration and the reporting script are copied outside the checkout because the
incremental runner can switch revisions, including revisions before these files existed.

Only SARIF results with `baselineState: new` are published. Existing, resolved, and updated existing
findings are omitted. Missing/unknown comparison states make the report unverified rather than
silently accepting a full scan. A successful empty SARIF does not by itself prove successful Gradle
import or IDE parity: inspect Qodana's sanity diagnostics and logs as part of verification.

The PR's **Pilot IntelliJ Inspections** workflow has a job summary and a downloadable
`intellij-pilot-pr-<number>` artifact. The summary lists at most 50 finding locations. Raw SARIF input
is limited to 50 MiB; filtered SARIF is uploaded only if at most 10 MiB. An oversized report produces
an explicit notice, not silent truncation. The summary remains available. Retention is seven days;
HTML reports, source snapshots, and complete runner logs are not uploaded as artifacts.

## Inspection scope and representative cases

The profile starts empty and enables only the following candidates; Qodana sanity checks are not
disabled. Formatting, unused imports and general Android Lint checks remain in their existing jobs.
Availability in Inspectopedia does **not** establish support in the pinned Community Android image.

| Recent warning | Candidate inspection | Verification status |
| --- | --- | --- |
| Explicit SAM in `DailyChallengeRoute` and `PersonalizationNavigationTest` | `RedundantSamConstructor` | Pending actual analysis |
| Receiver-only `let` in `AppNavigation` | `SimpleRedundantLet`, `ComplexRedundantLet` | Pending actual analysis |
| Unused declaration such as `DailyAggregate.completedChallengeIds` | `UnusedSymbol` | Pending confirmation of Kotlin support in this image |
| Unused `when` subject variable in `GeneratedModeRoute` | `UnusedSymbol` | Coverage not established; compiler diagnostic may differ |
| Flow-implied `activeReplacementTransition == null` in `GeneratedModeRoute` | `ConstantConditionIf` | Not equivalent: this inspection covers literal constant conditions, not general Kotlin data-flow reasoning |
| Ignored immutable `copy` return value | `UnusedDataClassCopyResult` | Pending actual analysis |

The historical correction `c522e181c1d8e324ee9e8fa9afb7b00d845da596` and its parent
`efdf9b2aecbc2fb8f48855ca1c93cf023c506fc1` contain the SAM, scope-function, unused-variable and
flow-condition examples. `acf204e83144c8e6893101cd56cc527850825f4f` contains additional unused-symbol
and ignored-copy corrections. JUnit value-class assertion compatibility is outside this initial
profile; do not treat its absence as a reproduced warning or broadly suppress it.

To complete reproduction, use disposable copies/checkouts of the historical revisions, mounting the
current pilot configuration externally. First scan the pre-fix snapshot to establish which warnings
the runner actually sees, then the fixed snapshot. A forward incremental scan of a correction should
report **no new** instance of the removed warning; alone, that does not prove detection. To test new
warning detection, compare the fixed snapshot to an isolated reintroduction of the historical change.
Do not add deliberately defective production/test code or make reproduction commits on this branch.
Record inspection IDs, locations and relevant sanity/import diagnostics, including missing warnings.

## Resource, cache and security policy

- The workflow allows 30 minutes overall, at most five minutes for image download and twenty for
  analysis. Docker is capped at two CPUs and 6 GiB memory. Timeouts stop the named container before
  reporting. Incremental analysis can import/analyze both revisions; it is not guaranteed to be cheap.
- Qodana's `/data/cache` is cached per PR, runner OS, image version and configuration/dependency hash.
  A successful first run populates one immutable cache; later runs with the same key restore it.
  There are no cross-PR restore prefixes and failed scans do not save caches. Cache size and results
  size are printed in the publication step. Normal GitHub cache eviction still applies.
- Logs expose image-pull duration; summaries record scan duration excluding pull, cache-hit state,
  and comparison SHAs. Step timings provide total runner usage. Actual warm/cold cost is pending.
- Community Android can run locally without a Qodana Cloud token. No `QODANA_TOKEN`, Cloud upload,
  paid linter or Code Scanning upload is configured. Recheck licensing when changing images.
- The workflow uses `pull_request`, not `pull_request_target`, read-only repository permissions,
  no persisted checkout credentials, no repository secrets, and no automatic fixes or pushes.
  As with a Gradle build, PR code executes on an ephemeral runner; retain GitHub's fork approval policy.

## Evidence and acceptance status

Local attempt on 2026-09-19: `docker pull jetbrains/qodana-jvm-android:2026.2` was stopped by an explicit
120-second timeout (exit 124) before completion. **Qodana did not run.** This supplies no measurement
of analysis runtime, cache savings, AGP/Gradle/Kotlin compatibility, or warning reproduction. In
particular, compatibility with this project's AGP 9.3.1, Gradle 9.7.1 and Kotlin 2.4.10 is unverified.

Before closing #685, attach actual PR run URLs and record:

1. Cold-run image download, import/sanity status, scan seconds, total duration, cache/artifact sizes.
2. A subsequent PR update with the same configuration: cache hit and measured warm-run duration.
3. New-versus-existing warning behavior and each historical case above, including unsupported IDs
   and divergent results. Verify a clean comparison and an infrastructure-failure report too.
4. Community execution without Cloud credentials and actual runner usage under the configured caps.

Create a separate evaluation issue after collecting this evidence. It should decide whether to keep,
tune or remove the pilot, and only then consider blocking severities, a required check, upgrade
ownership and an accepted CI budget. This implementation does not authorize that promotion and the
remaining empirical acceptance criteria must not be marked complete based on configuration alone.

References: [incremental analysis](https://www.jetbrains.com/help/qodana/analyze-pr.html),
[inspection profiles](https://www.jetbrains.com/help/qodana/inspection-profiles.html),
[configuration and Community tokens](https://www.jetbrains.com/help/qodana/configuration-reference.html),
[runner options](https://www.jetbrains.com/help/qodana/docker-image-configuration.html).
