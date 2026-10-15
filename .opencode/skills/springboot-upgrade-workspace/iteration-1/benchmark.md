# Spring Boot upgrade skill — simulated workflow evaluation

| Scenario | With skill | Workflow-only baseline |
| --- | --- | --- |
| boot2-stabilize-before-major | 6/6 | 6/6 |
| boot3-integration-blocker | 6/6 | 6/6 |
| boot4-confirmed-transition | 6/6 | 6/6 |

## Interpretation

- Three isolated decision-trace simulations, one run per scenario per configuration. No application upgrade or live Maven validation was performed.
- All 18 assertions pass in both configurations. This shows workflow adherence on these cases, not a measured improvement over the original workflow.
- Both configurations receive the original workflow and explicit simulated outcomes. These tests do not measure release discovery, recipe availability, repair implementation, or live runtime behavior.
- Only exact recipe-command arguments are graded automatically; semantic ordering, blocker handling, and reporting were reviewed against the complete outputs and fixtures.
- One sample per configuration per scenario does not establish repeatability or within-scenario variance.
- Token usage and execution duration were not provided by the subagent completion notifications. They are unavailable and excluded from metrics rather than reported as zero.
