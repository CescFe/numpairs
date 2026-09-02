# Multi-Issue Delivery

Use this workflow when a milestone or another body of work must be delivered through several
independently reviewable issues.

## Plan The Work

1. Review the relevant product references and inspect the implemented baseline.
2. Divide the remaining scope into independently reviewable, dependency-ordered issues.
3. Give each issue one observable outcome and one Pull Request.
4. Identify dependencies and define the order in which issues can be integrated.
5. Apply the required issue metadata and set Project `Status` to `Ready For Dev` for each planned
   issue.

Do not combine unrelated product behavior, refactors, or documentation in one issue merely to
reduce the number of Pull Requests.

## Integrate The Issues

- Create each implementation branch from an up-to-date `main`.
- Complete and merge dependencies before starting work that relies on them.
- Merge Pull Requests in dependency order.
- Update local `main` after each merge before starting the next dependent issue.
- Keep each branch and Pull Request limited to its issue's acceptance criteria.

See [planning](planning.md) for delivery context and work references,
[implementation](implementation.md) for branch and commit conventions, and
[Pull Requests](pull-requests.md) for review and merge requirements.
