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
7. Wait for and verify automatic Project 11 intake and `Backlog` initialization, then apply the remaining Project fields and iteration.
8. Set Project `Status` to `Ready For Dev` for each planned issue.
9. Set Project `Status` to `In Progress` immediately before implementation begins on an existing issue.

Do not combine unrelated product behavior, refactors, or documentation in one issue merely to reduce the number of Pull Requests.

## Delivery Context Isolation

For a delivery batch with multiple atomic issues, keep two distinct context roles:

- a lightweight coordinating context that owns milestone scope, dependency order, work references,
  issue and Pull Request state, and cross-issue decisions
- one fresh isolated execution context for each atomic issue that owns only that issue's
  implementation cycle

Start each isolated issue context only after its dependencies are merged and local `main` is
current. Provide the issue URL or number, assigned work reference, applicable delivery inputs,
dependency decisions, and the repository instructions and sources required for that issue. Do not
fork or copy the accumulated coordinator conversation or any prior issue's implementation
transcript into the new context. Repository required-reading rules still apply inside every issue
context.

When an issue cycle ends, return only a concise delivery summary to the coordinator: issue and work
reference, branch and Pull Request, merge result, validation evidence, and any decision or blocker
that can affect later issues. After every merge, update the coordinator with that summary and
compact the coordinating context before starting the next issue. Use the active Codex surface's
fresh-context and compaction mechanisms; if either is unavailable, report that limitation before
starting multi-issue implementation instead of silently reusing the accumulated transcript.

Context isolation does not relax sequential integration. Do not run dependent issue implementation
in parallel or start it from an unmerged branch.

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

A user request to complete an issue, delivery batch, or milestone end to end authorizes every
in-scope merge in that delivery cycle. No additional per-Pull-Request merge confirmation is
required. This authorization remains limited to the requested delivery scope and does not extend
to unrelated future work.

A request limited to implementation, Pull Request creation, or review does not authorize merge.
An explicit review-first or no-merge instruction overrides a broader workflow request.

For every authorized merge, use squash and merge with this squash commit title:

```text
[{reference}] {Relevant sentence-case message}
```

Do not merge while a required check is pending, unexpectedly skipped, cancelled, or failing. If
the delivery is not authorized end to end, or the user asks to review the Pull Request first or
explicitly says not to merge, stop after creating and reporting the Pull Request.

Before any authorized merge:

1. Re-read the associated issue's acceptance criteria.
2. Verify every criterion against the implemented change and available validation evidence.
3. Mark each fulfilled acceptance-criteria checkbox as complete in the issue.
4. Leave unmet criteria unchecked and do not merge while a required criterion remains unmet.

Do not mark criteria complete merely because implementation has ended or checks are green.

## Implementation Cycle

For each authorized issue:

1. Set the issue's Project `Status` to `In Progress`.
2. Update local `main` from `origin/main`.
3. Create the issue branch using the assigned work reference.
4. Implement only the issue acceptance criteria.
5. Validate in proportion to the change.
6. Review `git diff` and `git diff --check`.
7. Commit using the required convention.
8. Push the branch.
9. Open and configure the Pull Request.
10. Start one terminal watcher for required GitHub checks when a merge is part of the requested cycle.
11. Review the associated issue's acceptance criteria and mark only fulfilled criteria complete.
12. If checks pass, every required criterion is fulfilled, and the delivery scope authorizes merging, squash and merge with the required title without requesting per-Pull-Request confirmation.
13. Update local `main` before starting the next issue.
14. Return the concise issue summary to the coordinating context and compact it before starting the
    next issue.

Do not start dependent implementation from an unmerged branch when the requested workflow requires sequential integration into `main`.

### Required Check Watching

Start `gh pr checks <pr> --watch --interval 30` once. If the command continues in the background,
wait on that same terminal session using the longest practical wait interval supported by the
surface. Keep repetitive watcher progress out of the model context and retrieve only the final
bounded summary or the failure details needed for diagnosis.

Do not repeatedly invoke `gh pr checks`, query individual run status, or poll the watcher terminal
at short intervals. A watcher failure or interruption may be followed by one bounded status query
to diagnose the terminal state before deciding whether to resume the same watcher or address a
failed check.

### Tool And Output Efficiency

- Group independent read-only discovery into one parallel tool round trip when it remains easy to
  attribute results and failures.
- Group related sequential operations into one tool call when ordering, approval scope, and failure
  attribution remain clear. Keep destructive or materially different actions separate.
- Prefer targeted `rg` searches and bounded file ranges. Read required canonical documents in full,
  but do not dump unrelated files, complete directories, or already-seen content into the context.
- Set output bounds proportional to the expected result. Suppress repetitive progress from Gradle,
  GitHub watchers, and other long-running commands; surface the final summary and relevant failure
  excerpt instead.
- Start a long-running command once and continue through its existing terminal session. Do not
  restart it merely to obtain progress.
- Pass the relevant Android validation tasks to one Gradle invocation in the documented order when
  feasible. Split or rerun tasks only when required to diagnose a failure or preserve correctness.

These efficiency rules do not remove, skip, or weaken any required inspection, validation,
acceptance-criteria review, approval, or merge safeguard.

For Compose UI changes, the diff review must include a design-system consistency pass:

- inspect newly introduced or configured direct Material components
- inspect feature-local shapes, colors, typography, and other visual styling
- compare each affected visual role with `NumPairsComponents` and the nearest analogous UI
- identify in the Pull Request any intentional deviation from an established shared component or token

## Android Validation

For application changes, run the relevant tasks sequentially:

```text
./gradlew spotlessApply testDebugUnitTest spotlessCheck compileDebugAndroidTestKotlin
```

Run `lintDebug` when completing a stage or milestone and for changes with broader Android risk.

Instrumented tests must only be compiled. Do not start an emulator or run connected-device tasks because doing so can make the development machine unusable.

For documentation-only changes, validate Markdown structure, relative links, consistency, and `git diff --check`. Android build tasks are not required unless the documentation change affects build configuration or executable examples.

## Milestone Completion

Before reporting a milestone ready for manual closure:

- confirm every planned issue is closed or deliberately marked `not planned`
- confirm every associated Pull Request is merged
- confirm the milestone has zero open issues
- update local `main` from `origin/main`
- run final formatting, unit-test, lint, and instrumented-test compilation checks
- confirm the worktree is clean and synchronized with `origin/main`

Agents must not close GitHub milestones.
