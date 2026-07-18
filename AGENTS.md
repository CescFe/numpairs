# Repository Agent Instructions

These instructions apply to the complete NumPairs repository. User instructions for a specific task take precedence over repository defaults.

## Required Reading

- Read [the delivery workflow](docs/technical/delivery-workflow.md) completely before planning issues, changing repository files, validating work, opening a Pull Request, or merging.
- Read [the code quality guidelines](docs/technical/code-quality.md) completely before changing production or test code.
- For game-rule or domain-model changes, also read [the ubiquitous language](docs/ubiquitous-language.md) and [the game rules](docs/game-rules.md).
- Review the relevant [Architectural Decision Records](docs/technical/adr/) before changing an established architectural boundary or decision.

Use the linked documents as the canonical sources rather than copying their complete rules into this file.

## Repository Templates

- Use [the feature issue template](.github/ISSUE_TEMPLATE/feature.md) for feature and task issues.
- Use [the bug issue template](.github/ISSUE_TEMPLATE/bug.md) for defects.
- Use [the user-story template](.github/ISSUE_TEMPLATE/user-story.md) when the requested unit of work is a user story.
- Use [the Pull Request template](.github/pull_request_template.md) for every Pull Request.

Follow the selected template and replace its prompts with task-specific content.

## Non-Negotiable Safeguards

- Keep each implementation limited to one atomic issue, one branch, and one Pull Request.
- Preserve unrelated and user-authored work already present in the worktree.
- Verify the associated issue's acceptance criteria and mark only fulfilled criteria complete before merge.
- Treat a user request to complete an issue, delivery batch, or milestone end to end as merge authorization for every in-scope Pull Request; no per-PR confirmation is required.
- Outside that authorized scope, or when the user requests review, Pull Request creation, or no merge, do not merge. Never merge while a required check is not successful.
- Compile instrumented tests when required, but do not start an emulator or run connected-device tasks.
- Resolve current GitHub Project field and option identifiers dynamically; do not treat opaque IDs as permanent configuration.
- Do not introduce an architectural pattern, dependency, or refactor solely to satisfy a generic best practice.
