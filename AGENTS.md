# Repository Agent Instructions

These instructions apply to the complete NumPairs repository. User instructions for a specific task take precedence over the defaults below.

## Sources Of Truth

- Use [the feature issue template](.github/ISSUE_TEMPLATE/feature.md) for feature and task issues.
- Use [the bug issue template](.github/ISSUE_TEMPLATE/bug.md) for defects.
- Use [the user-story template](.github/ISSUE_TEMPLATE/user-story.md) when the requested unit of work is a user story.
- Use [the Pull Request template](.github/pull_request_template.md) for every Pull Request.
- Use [CONTRIBUTING.md](CONTRIBUTING.md) for the contributor-facing explanation of this workflow.

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
- issue field `Priority`: `Medium`
- issue field `Effort`: `Low`
- GitHub Project: `https://github.com/orgs/CescFe/projects/11`
- Project field `Priority`: `P2`
- Project field `Size`: `xs`

Use the iteration and milestone specified for the current delivery batch. Do not attach new work to a closed milestone unless the user explicitly requests it.

Resolve GitHub node IDs, field IDs, option IDs, and iteration IDs through the GitHub API. Treat these identifiers as opaque and do not store previously observed values as permanent repository configuration.

## Atomic Issue Planning

For milestone delivery:

1. Read the relevant PRD or product reference completely.
2. Inspect the implemented baseline before proposing work.
3. Divide the remaining scope into independently reviewable, dependency-ordered issues.
4. Give each issue one observable outcome and one Pull Request.
5. Write each issue in English using the selected issue template.
6. Apply the required assignee, label, type, issue fields, Project fields, iteration, and milestone.

Do not combine unrelated product behavior, refactors, or documentation in one issue merely to reduce the number of Pull Requests.

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
- link the issue with `Closes #{issue_number}`
- be assigned to `FrancescFe` unless specified otherwise
- use the issue's primary label, normally `feat` for feature work
- state the work reference and verification performed
- contain only the associated issue's scope

Pull Request title format:

```text
[{reference}] {Relevant sentence-case message}
```

When a merge is authorized, use squash and merge with this squash commit title:

```text
[{reference}] {Relevant sentence-case message}
```

Do not merge while a required check is pending, unexpectedly skipped, cancelled, or failing. Merge only when the user requested an end-to-end merge cycle. If the user asks to review the Pull Request first or explicitly says not to merge, stop after creating and reporting the Pull Request.

## Implementation Cycle

For each authorized issue:

1. Update local `main` from `origin/main`.
2. Create the issue branch using the assigned work reference.
3. Implement only the issue acceptance criteria.
4. Validate in proportion to the change.
5. Review `git diff` and `git diff --check`.
6. Commit using the required convention.
7. Push the branch.
8. Open and configure the Pull Request.
9. Wait for required GitHub checks when a merge is part of the requested cycle.
10. If checks pass and merge is authorized, squash and merge with the required title.
11. Update local `main` before starting the next issue.

Do not start dependent implementation from an unmerged branch when the requested workflow requires sequential integration into `main`.

## Android Validation

For application changes, run the relevant tasks sequentially:

```text
./gradlew spotlessApply testDebugUnitTest spotlessCheck compileDebugAndroidTestKotlin
```

Run `lintDebug` when completing a stage or milestone and for changes with broader Android risk.

Instrumented tests must only be compiled. Do not start an emulator or run connected-device tasks because doing so can make the development machine unusable.

For documentation-only changes, validate Markdown structure, relative links, consistency, and `git diff --check`. Android build tasks are not required unless the documentation change affects build configuration or executable examples.

## Milestone Completion

Before closing a milestone:

- confirm every planned issue is closed or deliberately marked `not planned`
- confirm every associated Pull Request is merged
- confirm the milestone has zero open issues
- update local `main` from `origin/main`
- run final formatting, unit-test, lint, and instrumented-test compilation checks
- confirm the worktree is clean and synchronized with `origin/main`

Close the milestone only when the user authorized completion and all required verification has passed.
