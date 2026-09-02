# Milestone Completion

Before reporting a milestone ready for manual closure:

- confirm every planned issue is closed or deliberately marked `not planned`
- confirm every associated Pull Request is merged
- confirm the milestone has zero open issues
- update local `main` from `origin/main`
- run final formatting, unit-test, lint, and instrumented-test compilation checks appropriate to the
  milestone
- confirm the worktree is clean and synchronized with `origin/main`

For a documentation-only milestone, use the documentation validation described in
[validation](validation.md) instead of Android build tasks.
