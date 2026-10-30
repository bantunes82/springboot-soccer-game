# Skill Benchmark: springboot-upgrade (iteration 3)

Evals 1-5, 1 run per configuration. Baseline = previous SKILL.md (git HEAD).

| Metric | With Skill (new) | Old Skill | Delta |
|---|---|---|---|
| Pass rate | 98% | 91% | +0.07 |

| Eval | New | Old |
|---|---|---|
| 1. boot2-stabilize-before-major | 7/7 | 6/7 |
| 2. boot3-integration-blocker | 6/6 | 6/6 |
| 3. boot4-confirmed-transition | 6/6 | 6/6 |
| 4. boot3-all-documented-approaches | 10/11 | 9/11 |
| 5. boot3-smoke-interfaces | 9/9 | 8/9 |

## Notes
- Baseline is the previous committed SKILL.md (git HEAD); one run per configuration per eval.
- Timing/token telemetry was not available from subagent notifications, so it is omitted rather than reported as 0.
- Discriminating assertions: eval-1 #6 and eval-4 #8/#9 (recipe vs agent change attribution, recipe-only diff snapshot). Both pass only with the new skill.
- Evals 2 and 3 do not discriminate (both 100%), though the new skill still splits recipe/agent changes there.
- eval-4 #10 (smoke checks beyond readiness and /v1/items) failed with the new skill but passed with the old one; this is likely run-to-run variation, since the edit did not touch section 3a.
- eval-5 old skill failed #9 by stating the upgrade as fact in its final report.
