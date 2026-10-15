# Workflow evaluations

`evals.json` defines three isolated, evidence-driven simulations: 2.x stabilization
before crossing majors, 3.x with blocked integration tests, and a confirmed 4.x
transition with an alternate artifact-resolution failure.

These evaluate the agent's decisions, command selection, validation order, and
reporting. They do not run OpenRewrite, compile a real service, verify current
release availability, or prove that a migration recipe works. Fixture versions
and command outcomes are synthetic evidence.

For each case, give a fresh agent its prompt, the referenced `scenario.md`, and a
copy of the repository's `springboot-upgrade-workflow.md`. Run once with
`../SKILL.md` loaded and once without it. Both configurations receive the same
workflow and fixture evidence so the comparison tests the skill's added guidance.
Save each answer as `response.md` under a separate run's `outputs/` directory.

Keep results in the sibling `../../springboot-upgrade-workspace/iteration-N/`
directory. Review outcomes against the assertions in `evals.json`, retaining
evidence for every score and labeling simulated validation clearly.
