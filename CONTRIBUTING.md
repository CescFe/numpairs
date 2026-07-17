# Contributing To NumPairs

Thank you for contributing to NumPairs. The project uses small, issue-driven changes so that product decisions, implementation, and review remain easy to trace.

Repository automation instructions live in [AGENTS.md](AGENTS.md). This guide presents the same workflow for human contributors.

## Requesting An Issue

Use an issue to request a focused change for a future contributor. Select the appropriate template:

- [Feature request](.github/ISSUE_TEMPLATE/feature.md)
- [Bug report](.github/ISSUE_TEMPLATE/bug.md)
- [User story](.github/ISSUE_TEMPLATE/user-story.md)

Describe the context, requested outcome, and verifiable acceptance criteria without assuming that implementation has started. Add the issue to Project 11 with Project `Status: Backlog`, not `Ready For Dev` or `In Progress`.

`Ready For Dev` is reserved for refined, atomic work that maintainers or an authorized planning workflow have prepared for implementation.

## Implementing An Existing Issue

Contributors implement an existing issue rather than creating a duplicate. Before editing code:

1. Read the complete issue and its acceptance criteria.
2. Confirm that the work is not already assigned or in progress.
3. Confirm the current milestone, Project iteration, and work reference.
4. Change the issue's Project `Status` to `In Progress`.
5. Create a focused branch from an up-to-date `main`.

An issue selected from `Backlog` can move directly to `In Progress` when a contributor begins implementation. A planned issue may already be in `Ready For Dev`; it must still move to `In Progress` before work starts.

Milestone, iteration, and starting work reference are delivery-specific values and should not be copied from an earlier delivery batch.

Unless a delivery batch specifies otherwise, feature tasks use:

- assignee `FrancescFe`
- label `feat`
- type `Task`
- issue fields `Priority: Medium` and `Effort: Low`
- [NumPairs Project 11](https://github.com/orgs/CescFe/projects/11)
- Project field `Size: xs`
- the iteration and milestone selected for the current delivery batch

Project `Priority` can be `P0`, `P1`, `P2`, or `P3`. Unless the delivery batch specifies a priority, use the same Project `Priority` as the most recently worked issue. Ask when there is no previous issue from which to inherit it.

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

Before merging, re-read every acceptance criterion in the associated issue. Check the box for each criterion that the implementation and validation evidence satisfy. Leave unmet criteria unchecked, and do not merge while a required criterion remains unmet.

## Completing A Milestone

A milestone can be closed after:

- every planned issue is closed or deliberately marked `not planned`
- every associated Pull Request is merged
- the milestone has no open issues
- final formatting, unit-test, lint, and instrumented-test compilation checks pass on `main`
- the local worktree is clean and synchronized with `origin/main`
