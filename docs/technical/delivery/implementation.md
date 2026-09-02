# Implementation

Use this guide when implementing an issue on its own branch. Keep the branch focused and implement
only the issue's acceptance criteria.

## Branches

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

## Commits

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

Prefer one meaningful implementation commit when that accurately represents the work.

## Implementation Cycle

For each issue:

1. Update local `main` from `origin/main`.
2. Create the issue branch using the assigned work reference.
3. Implement only the issue acceptance criteria.
4. Validate in proportion to the change; see [validation](validation.md).
5. Review `git diff` and `git diff --check`.
6. Commit using the required convention.
7. Push the branch and prepare its Pull Request.
8. Address review feedback and any failed required checks.
9. Merge after review and required checks have passed, when the change is approved for merging.
10. Update local `main` before starting another dependent issue.

See [Pull Requests](pull-requests.md) for review and merge requirements. For dependent work, use
the integration order described in [multi-issue delivery](multi-issue-delivery.md).
