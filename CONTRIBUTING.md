# Contributing To NumPairs

Thank you for contributing to NumPairs. The project uses small, issue-driven changes so that product decisions, implementation, and review remain easy to trace.

The canonical repository guidance is:

- [AGENTS.md](AGENTS.md) for agent routing and non-negotiable safeguards
- [the delivery workflow](docs/technical/delivery-workflow.md) for issue, branch, commit, validation, Pull Request, and merge conventions
- [the code quality guidelines](docs/technical/code-quality.md) for architecture, domain modeling, implementation, and testing expectations

This guide distinguishes requesting future work from implementing an existing issue. It does not replace the canonical workflow and quality documents.

## Requesting An Issue

Use an issue to request a focused change for a future contributor. Select the appropriate template:

- [Feature request](.github/ISSUE_TEMPLATE/feature.md)
- [Bug report](.github/ISSUE_TEMPLATE/bug.md)
- [User story](.github/ISSUE_TEMPLATE/user-story.md)

Describe the context, requested outcome, and verifiable acceptance criteria without assuming that implementation has started.

Add requested future work to Project 11 with Project `Status: Backlog`. Do not place it in `Ready For Dev` or `In Progress`.

`Ready For Dev` is reserved for refined, atomic issues that maintainers or an authorized planning workflow have prepared for implementation.

## Implementing An Existing Issue

Implement an existing pending issue rather than creating a duplicate. Before editing repository files:

1. Read the complete issue and its acceptance criteria.
2. Confirm that the work is not already assigned or in progress.
3. Read [the delivery workflow](docs/technical/delivery-workflow.md).
4. Read [the code quality guidelines](docs/technical/code-quality.md) before changing production or test code.
5. Confirm the current milestone, Project iteration, and work reference.
6. Change the issue's Project `Status` to `In Progress`.
7. Create a focused branch from an up-to-date `main`.

An issue selected from `Backlog` can move directly to `In Progress` when implementation begins. A planned issue may already be in `Ready For Dev`; it must still move to `In Progress` before work starts.

Keep the implementation limited to the issue and validate it according to the delivery workflow. Before merge, verify every acceptance criterion, mark only fulfilled criteria complete, and leave the Pull Request open while a required criterion or check remains unmet.

Instrumented tests are compiled only on the development machine. Do not start an emulator or run connected-device tasks.
