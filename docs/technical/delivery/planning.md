# Delivery Planning

Use this guide to define a focused NumPairs issue and confirm the delivery information that applies
to it. For work spanning several dependent issues, also see
[multi-issue delivery](multi-issue-delivery.md).

## Sources Of Truth

- Use [the feature issue template](../../../.github/ISSUE_TEMPLATE/feature.md) for feature and task issues.
- Use [the bug issue template](../../../.github/ISSUE_TEMPLATE/bug.md) for defects.
- Use [the user-story template](../../../.github/ISSUE_TEMPLATE/user-story.md) when the requested unit of work is a user story.
- Use [the Pull Request template](../../../.github/pull_request_template.md) for every Pull Request.

Product requirements, game behavior, terminology, architecture, and persistence conventions are
documented in the relevant PRD, [game rules](../../game-rules.md),
[ubiquitous language](../../ubiquitous-language.md), technical guidance, and ADRs. Consult the
documents that apply to the change rather than treating the entire documentation set as a
prerequisite for routine work.

Do not duplicate complete templates in issues or documentation. Follow the repository template
and replace its prompts with task-specific content.

## Delivery Context

Confirm these values from the current issue or delivery plan:

- milestone URL or number
- GitHub Project URL
- Project iteration
- starting work reference
- labels or other metadata that differ from the defaults

Do not carry a milestone, iteration, or starting reference from a previous delivery batch without
verifying it.

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

For a focused issue:

1. Review the product and technical references relevant to the requested behavior.
2. Inspect the implemented baseline before proposing work.
3. Define one observable outcome and the acceptance criteria that demonstrate it.
4. Keep unrelated product behavior, refactors, and documentation in separate issues.
5. Apply the required assignee, label, type, and milestone.
6. Set Project `Status` to `Ready For Dev` when the issue is ready to implement.

Write issues in English using the selected issue template. A focused standalone change does not
need a milestone breakdown.

## Work References

Work references are sequential numbers independent from GitHub issue numbers.

Use the starting reference provided for the task. If none is provided, inspect recent merged squash
commits and remote branches, then use the next available reference. If the sequence is ambiguous,
resolve it before creating a branch.
