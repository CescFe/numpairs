# Contributing To NumPairs

Thank you for contributing to NumPairs. The project uses small, issue-driven changes so that product decisions, implementation, and review remain easy to trace.

Repository automation instructions live in [AGENTS.md](AGENTS.md). This guide presents the same workflow for human contributors.

## Before Starting

Every change should have a focused GitHub issue. Select the appropriate template:

- [Feature request](.github/ISSUE_TEMPLATE/feature.md)
- [Bug report](.github/ISSUE_TEMPLATE/bug.md)
- [User story](.github/ISSUE_TEMPLATE/user-story.md)

For milestone work, confirm the current milestone, Project iteration, and starting work reference before creating a branch. These are delivery-specific values and should not be copied from an earlier milestone.

Unless a delivery batch specifies otherwise, feature tasks use:

- assignee `FrancescFe`
- label `feat`
- type `Task`
- issue fields `Priority: Medium` and `Effort: Low`
- [NumPairs Project 11](https://github.com/orgs/CescFe/projects/11)
- Project fields `Priority: P2` and `Size: xs`
- the iteration and milestone selected for the current delivery batch

Issues should be atomic: one independently reviewable outcome, one branch, and one Pull Request.

## Branches And Work References

Work references are sequential delivery numbers and are not GitHub issue numbers. Use the reference assigned to the task.

Start from an up-to-date `main`:

```bash
git switch main
git pull --ff-only
git switch -c 205_document_repository_delivery_workflow
```

Branch names use:

```text
{reference}_{descriptive_snake_case_name}
```

## Commits

Commit messages use:

```text
{type}({reference}): {lowercase message}
```

Allowed types are `feat`, `test`, `doc`, `ci`, `refactor`, and `chore`.

Examples:

```text
feat(205): add a replay entry point
doc(206): document the delivery workflow
```

Keep commits and branches limited to the associated issue.

## Local Verification

For application changes, run the relevant Android validation sequentially:

```bash
./gradlew spotlessApply testDebugUnitTest spotlessCheck compileDebugAndroidTestKotlin
```

Use `./gradlew lintDebug` for stage, milestone, and broader Android validation.

Instrumented tests are compiled only. Do not start an emulator or run connected-device tasks on the development machine.

For documentation-only work, check Markdown structure and links, review the diff, and run:

```bash
git diff --check
```

## Pull Requests

Push the issue branch and open a ready-for-review Pull Request against `main`. Complete [the Pull Request template](.github/pull_request_template.md), including:

- the closing issue reference
- a concise outcome-oriented summary
- the commands or checks used for verification
- the work reference
- the intended squash title

Assign the Pull Request to `FrancescFe` and apply the issue's primary label unless the delivery batch specifies different metadata.

Pull Request and squash commit titles use:

```text
[{reference}] {Relevant sentence-case message}
```

Example:

```text
[205] Document the repository delivery workflow
```

The project uses squash and merge. Required checks must pass before merging. A Pull Request must remain unmerged when the requester asks to review it first.

## Completing A Milestone

A milestone can be closed after:

- every planned issue is closed or deliberately marked `not planned`
- every associated Pull Request is merged
- the milestone has no open issues
- final formatting, unit-test, lint, and instrumented-test compilation checks pass on `main`
- the local worktree is clean and synchronized with `origin/main`
