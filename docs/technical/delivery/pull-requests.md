# Pull Requests

Use the repository [Pull Request template](../../../.github/pull_request_template.md) for every
Pull Request. Keep each Pull Request limited to the associated issue's scope.

## Preparation

Pull Requests should:

- target `main`
- be ready for review rather than draft once implementation and validation are complete
- link the issue with `Resolves #{issue_number}`
- be assigned to `FrancescFe` unless specified otherwise
- use the issue's primary label, normally `feat` for feature work

Open a draft Pull Request during implementation when the issue workflow benefits from an early
issue-to-PR link. Mark it ready for review after the implementation and validation are complete.

## Review And Merge

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
dependent issues, merge them in dependency order as described in
[multi-issue delivery](multi-issue-delivery.md).

## Required Checks

Monitor the required GitHub checks for a Pull Request until they complete. Investigate failures,
cancellations, or unexpected skips before merging, and rerun checks only when appropriate to the
failure or repository workflow.
