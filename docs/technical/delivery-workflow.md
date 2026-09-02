# Repository Delivery Workflow

This workflow describes how contributors plan, implement, validate, and deliver changes to
NumPairs. Adapt it to the size and risk of the change; issue-specific acceptance criteria and
explicit decisions for the current task take precedence.

## Sources Of Truth

- Use [the feature issue template](../../.github/ISSUE_TEMPLATE/feature.md) for feature and task issues.
- Use [the bug issue template](../../.github/ISSUE_TEMPLATE/bug.md) for defects.
- Use [the user-story template](../../.github/ISSUE_TEMPLATE/user-story.md) when the requested unit of work is a user story.
- Use [the Pull Request template](../../.github/pull_request_template.md) for every Pull Request.

Product requirements, game behavior, terminology, architecture, and persistence conventions are
documented in the relevant PRD, [game rules](../game-rules.md),
[ubiquitous language](../ubiquitous-language.md), technical guidance, and ADRs. Consult the
documents that apply to the change rather than treating the entire documentation set as a
prerequisite for routine work.

Do not duplicate complete templates in issues or documentation. Follow the repository template
and replace its prompts with task-specific content.

## Delivery Context

Treat these values as delivery-specific inputs:

- milestone URL or number
- GitHub Project URL
- Project iteration
- starting work reference
- labels or other metadata that differ from the defaults

Confirm them from the current issue or delivery plan. Do not carry a milestone, iteration, or
starting reference from a previous delivery batch without verifying it.

Unless the task specifies otherwise, use:

- assignee: `FrancescFe`
- label for feature work: `feat`
- issue type: `Task`
- GitHub Project: `https://github.com/orgs/CescFe/projects/11`
- Project field `Size`: `xs`
- Project field `Priority`: contextual, from `P0` to `P3`
- Project field `Iteration`: contextual
- Milestone: the one specified for the current delivery batch

## Atomic Issue Planning

For milestone or other multi-issue delivery:

1. Review the relevant product references and inspect the implemented baseline before proposing work.
2. Divide the remaining scope into independently reviewable, dependency-ordered issues.
3. Give each issue one observable outcome and one Pull Request.
4. Write each issue in English using the selected issue template.
5. Apply the required assignee, label, type, and milestone.
6. Set Project `Status` to `Ready For Dev` for each planned issue.

Do not combine unrelated product behavior, refactors, or documentation in one issue merely to
reduce the number of Pull Requests. A focused standalone change can be planned and implemented
without creating a milestone breakdown.

## Work References And Branches

Work references are sequential numbers independent from GitHub issue numbers.

Use the starting reference provided for the task. If none is provided, inspect recent merged squash
commits and remote branches, then use the next available reference. If the sequence is ambiguous,
resolve it before creating a branch.

Create every implementation branch from an up-to-date `main`.

Branch format:

```text
{reference}_{descriptive_snake_case_name}
```

Example:

```text
205_document_repository_delivery_workflow
```

Keep separate atomic issues on separate branches.

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

Keep the branch focused and prefer one meaningful implementation commit when that accurately
represents the work.

## Pull Request Conventions

Pull Requests should:

- target `main`
- be ready for review rather than draft once implementation and validation are complete
- use the repository Pull Request template
- link the issue with `Resolves #{issue_number}`
- be assigned to `FrancescFe` unless specified otherwise
- use the issue's primary label, normally `feat` for feature work
- contain only the associated issue's scope

Open a draft Pull Request during implementation when the issue workflow benefits from an early
issue-to-PR link. Mark it ready for review after the implementation and validation are complete.

Before merging:

1. Re-read the associated issue's acceptance criteria.
2. Verify every criterion against the implemented change and available validation evidence.
3. Mark each fulfilled acceptance-criteria checkbox as complete in the issue.
4. Leave unmet criteria unchecked and do not merge while a required criterion remains unmet.
5. Confirm that the Pull Request has been reviewed and all required checks have passed.

Merge Pull Requests with squash and merge using this squash commit title:

```text
[{reference}] {Relevant sentence-case message}
```

Do not merge while a required check is pending, unexpectedly skipped, cancelled, or failing. For
dependent issues, merge them in dependency order and update `main` before starting the next one.

## Implementation Cycle

For each issue:

1. Update local `main` from `origin/main`.
2. Create the issue branch using the assigned work reference.
3. Open a draft Pull Request when an early linked PR is useful.
4. Implement only the issue acceptance criteria.
5. Validate in proportion to the change.
6. Review `git diff` and `git diff --check`.
7. Commit using the required convention.
8. Push the branch and mark the Pull Request ready for review.
9. Address review feedback and any failed required checks.
10. Re-check the issue's acceptance criteria and mark only fulfilled criteria complete.
11. Merge after review and required checks have passed, when the change is approved for merging.
12. Update local `main` before starting another dependent issue.

## Required Checks

Monitor the required GitHub checks for a Pull Request until they complete. Investigate failures,
cancellations, or unexpected skips before merging, and rerun checks only when appropriate to the
failure or repository workflow.

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

Compile instrumented tests only; do not start an emulator or run connected-device tasks as part of
the standard validation workflow.

For documentation-only changes, validate Markdown structure, relative links, consistency, and
`git diff --check`. Skip Android build tasks unless the documentation change affects build
configuration or executable examples.

## Milestone Completion

Before reporting a milestone ready for manual closure:

- confirm every planned issue is closed or deliberately marked `not planned`
- confirm every associated Pull Request is merged
- confirm the milestone has zero open issues
- update local `main` from `origin/main`
- run final formatting, unit-test, lint, and instrumented-test compilation checks appropriate to the
  milestone
- confirm the worktree is clean and synchronized with `origin/main`
