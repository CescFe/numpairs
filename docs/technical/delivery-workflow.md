# Repository Delivery Workflow

This is the canonical workflow for planning, implementing, validating, and delivering changes to NumPairs. User instructions for a specific task take precedence over the defaults below.

## Sources Of Truth

- Use [the feature issue template](../../.github/ISSUE_TEMPLATE/feature.md) for feature and task issues.
- Use [the bug issue template](../../.github/ISSUE_TEMPLATE/bug.md) for defects.
- Use [the user-story template](../../.github/ISSUE_TEMPLATE/user-story.md) when the requested unit of work is a user story.
- Use [the Pull Request template](../../.github/pull_request_template.md) for every Pull Request.

Do not duplicate complete templates in issues or documentation. Follow the repository template and replace its prompts with task-specific content.

## Delivery Context

Treat these values as delivery-specific inputs:

- milestone URL or number
- GitHub Project URL
- Project iteration
- starting work reference
- labels or other metadata that differ from the defaults

Obtain them from the user or the current task. Do not carry a milestone, iteration, or starting reference from a previous delivery batch without verification.

Unless the task specifies otherwise, use:

- assignee: `FrancescFe`
- label for feature work: `feat`
- issue type: `Task`
- GitHub Project: `https://github.com/orgs/CescFe/projects/11`
- Project field `Size`: `xs`
- Project field `Priority`: is contextual and ranges from `P0` to `P3`. Reuse it by inspecting the most recently worked issue.
- Project field `Iteration`: is contextual. Reuse it by inspecting the most recently worked issue.
- Milestone: is contextual. Use the specified for the current delivery batch.

Resolve GitHub node IDs, option IDs, and iteration IDs through the GitHub API. Treat these identifiers as opaque and do not store previously observed values as permanent repository configuration.

## Atomic Issue Planning

For milestone delivery:

1. Read the relevant PRD or product reference completely.
2. Inspect the implemented baseline before proposing work.
3. Divide the remaining scope into independently reviewable, dependency-ordered issues.
4. Give each issue one observable outcome and one Pull Request.
5. Write each issue in English using the selected issue template.
6. Apply the required assignee, label, type, and milestone.
7. Set Project `Status` to `Ready For Dev` for each planned issue.

Do not combine unrelated product behavior, refactors, or documentation in one issue merely to reduce the number of Pull Requests.

## Delivery Context Isolation

For delivery batches with multiple atomic issues:

- Keep a lightweight coordinating context for milestone scope, dependency order, work references,
  issue and Pull Request state, and cross-issue decisions.
- Use one fresh isolated execution context for each atomic issue.
- Start an issue context only after its dependencies are merged and local `main` is current.
- Provide the issue URL or number, assigned work reference, applicable delivery inputs, dependency
  decisions, and required repository instructions and sources.
- Do not fork or copy the coordinating context or prior issue transcripts into a new issue context.
- Apply repository required-reading rules in every issue context.
- Return only a concise delivery summary to the coordinating context: issue, work reference, branch,
  Pull Request, merge result, validation evidence, and decisions or blockers that affect later issues.
- After each merge, update and compact the coordinating context before starting the next issue.
- Use the active Codex surface's fresh-context and compaction mechanisms.
- Report unavailable isolation or compaction mechanisms before starting multi-issue implementation.
- Integrate dependent issues sequentially; do not start them in parallel or from an unmerged branch.

## Work References And Branches

Work references are sequential numbers independent from GitHub issue numbers.

Use the starting reference provided by the user. If none is provided, inspect recent merged squash commits and remote branches, then use the next available reference. If the sequence is ambiguous, ask before creating a branch.

Create every implementation branch from an up-to-date `main`.

Branch format:

```text
{reference}_{descriptive_snake_case_name}
```

Example:

```text
205_document_repository_delivery_workflow
```

Never implement multiple atomic issues in the same branch.

## Commit Conventions

Commit format:

```text
{type}({reference}): {lowercase message}
```

Allowed types:

- `feat`
- `test`
- `doc`
- `ci`
- `refactor`
- `chore`

Examples:

```text
feat(205): persist onboarding progress
doc(206): document the release workflow
```

Keep the branch focused and prefer one meaningful implementation commit when that accurately represents the work.

## Pull Request Conventions

Pull Requests must:

- target `main`
- be ready for review rather than draft unless the user requests a draft
- use the repository Pull Request template
- link the issue with `Resolves #{issue_number}`
- be assigned to `FrancescFe` unless specified otherwise
- use the issue's primary label, normally `feat` for feature work
- contain only the associated issue's scope

Pull Request title format:

```text
[{reference}] {Relevant sentence-case message}
```

Merge authorization rules:

- Treat a request to complete an issue, delivery batch, or milestone end to end as authorization for
  every in-scope merge in that delivery cycle.
- Do not request additional per-Pull-Request merge confirmation within that scope.
- Do not extend merge authorization to unrelated future work.
- Do not treat implementation, Pull Request creation, or review requests as merge authorization.
- Honor an explicit review-first or no-merge instruction over broader workflow requests.

For every authorized merge, use squash and merge with this squash commit title:

```text
[{reference}] {Relevant sentence-case message}
```

- Do not merge while a required check is pending, unexpectedly skipped, cancelled, or failing.
- Stop after creating and reporting the Pull Request when merge is not authorized or the user asks
  for review first or no merge.

Before any authorized merge:

1. Re-read the associated issue's acceptance criteria.
2. Verify every criterion against the implemented change and available validation evidence.
3. Mark each fulfilled acceptance-criteria checkbox as complete in the issue.
4. Leave unmet criteria unchecked and do not merge while a required criterion remains unmet.

Do not mark criteria complete merely because implementation has ended or checks are green.

## Implementation Cycle

For each authorized issue:

1. Update local `main` from `origin/main`.
2. Create the issue branch using the assigned work reference.
3. Create a Draft Pull Request with `Resolves #<issue>` to trigger the **Pull request linked to issue** workflow (moves the issue to `In Progress`).
4. Implement only the issue acceptance criteria.
5. Validate in proportion to the change.
6. Review `git diff` and `git diff --check`.
7. Commit using the required convention.
8. Push the branch.
9. Move the Pull Request to `Ready for review`.
10. Start one terminal watcher for required GitHub checks when a merge is part of the requested cycle.
11. Review the associated issue's acceptance criteria and mark only fulfilled criteria complete.
12. If checks pass, every required criterion is fulfilled, and the delivery scope authorizes merging, squash and merge with the required title without requesting per-Pull-Request confirmation.
13. Update local `main` before starting the next issue.
14. Return the concise issue summary to the coordinating context and compact it before starting the
    next issue.

Do not start dependent implementation from an unmerged branch when the requested workflow requires sequential integration into `main`.

### Required Check Watching

After moving a Pull Request to `Ready for review`:

1. Wait approximately six and a half minutes without querying GitHub.
2. Start `gh pr checks <pr> --watch --interval 15` once.
3. Continue waiting on that terminal session using the longest practical interval.
4. Return only the final bounded summary or failure details needed for diagnosis.

- Do not restart `gh pr checks`, query individual runs, or poll the watcher at short intervals.
- After a watcher failure or interruption, use at most one bounded status query before resuming the
  watcher or addressing a failed check.

### Tool And Output Efficiency

- Group safe independent reads in one parallel call when results and failures remain attributable.
- Group related sequential operations in one call when ordering, approval scope, and failures remain
  clear.
- Keep destructive or materially different actions separate.
- Use targeted `rg` searches and bounded reads.
- Read required canonical documents in full.
- Avoid dumping unrelated files, directories, or previously read content.
- Bound tool output and suppress repetitive progress.
- Surface only final summaries and relevant failure details.
- Start long-running commands once and reuse their terminal sessions.
- Run Android validation tasks in one Gradle invocation and documented order when feasible.
- Split or rerun tasks only to diagnose a failure or preserve correctness.
- Never weaken required inspections, validation, acceptance review, approval, or merge safeguards
  for efficiency.

For Compose UI changes, the diff review must include a design-system consistency pass:

- Inspect newly introduced or configured direct Material components.
- Inspect feature-local shapes, colors, typography, and other visual styling.
- Compare each affected visual role with `NumPairsComponents` and the nearest analogous UI.
- Record any intentional deviation from an established shared component or token in the Pull Request.

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
- Treat Android Lint and IDE-only IntelliJ inspections separately; do not claim Gradle validation
  covers IDE-only warnings.

- Compile instrumented tests only; do not start an emulator or run connected-device tasks.
- For documentation-only changes, validate Markdown structure, relative links, consistency, and
  `git diff --check`.
- Skip Android build tasks for documentation-only changes unless they affect build configuration or
  executable examples.

## Milestone Completion

Before reporting a milestone ready for manual closure:

- confirm every planned issue is closed or deliberately marked `not planned`
- confirm every associated Pull Request is merged
- confirm the milestone has zero open issues
- update local `main` from `origin/main`
- run final formatting, unit-test, lint, and instrumented-test compilation checks
- confirm the worktree is clean and synchronized with `origin/main`
