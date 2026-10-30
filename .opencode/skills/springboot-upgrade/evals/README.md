# Workflow evaluations

`evals.json` defines four isolated, evidence-driven simulations: 2.x stabilization
before crossing majors, 3.x with blocked integration tests, a confirmed 4.x
transition with an alternate artifact-resolution failure, and a 2.x→3.x major
upgrade that must run every approach documented in README/AGENTS/CLAUDE files.

These evaluate the agent's decisions, command selection, validation order, and
reporting. They do not run OpenRewrite, compile a real service, verify current
release availability, or prove that a migration recipe works. Fixture versions
and command outcomes are synthetic evidence.

For each case, give a fresh agent its prompt and the referenced `scenario.md`.
Run once with `../SKILL.md` loaded and once without it. Both configurations receive
the same fixture evidence; only the with-skill run receives the self-contained
upgrade procedure and pinned commands. No separate workflow document is needed.
Save each answer as `response.md` under a separate run's `outputs/` directory.

Keep results in the sibling `../../springboot-upgrade-workspace/iteration-N/`
directory. Review outcomes against the assertions in `evals.json`, retaining
evidence for every score and labeling simulated validation clearly.

Iteration 1 records the earlier evaluation setup, in which both configurations
received an external guide. Its results describe that historical setup; future
runs should use a new iteration directory and the self-contained inputs above.
