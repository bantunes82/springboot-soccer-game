"""Check exact recipe commands and publish the recorded semantic review.

Run from any directory. This is specific to iteration-1's reviewed outputs;
changed outputs require a new semantic review. It does not execute migrations.
Missing runtime/token telemetry is omitted from benchmark metrics, not zeroed.
"""

import json
import shlex
import statistics
from datetime import datetime, timezone
from pathlib import Path


def save(path, data):
    path.write_text(json.dumps(data, indent=2) + "\n")


workspace = Path(__file__).resolve().parent
skill = workspace.parent / "springboot-upgrade"
iteration = workspace / "iteration-1"
cases = json.loads((skill / "evals/evals.json").read_text())["evals"]
reviews = json.loads((iteration / "manual_review.json").read_text())["reviews"]
skill_text = (skill / "SKILL.md").read_text()
commands = [
    line.strip() for line in skill_text.splitlines()
    if line.startswith("mvn -U org.openrewrite.maven:rewrite-maven-plugin:")
]
assert len(commands) == 3, "Skill commands changed; review the evaluation."
assert len(skill_text.splitlines()) < 500
for case in cases:
    assert all((skill / file).is_file() for file in case["files"])

runs = []
for case in cases:
    directory = iteration / f"eval-{case['id']}-{case['name']}"
    expected_command = shlex.split(commands[case["id"] - 1])
    for configuration in ("with_skill", "without_skill"):
        run_dir = directory / configuration
        response = (run_dir / "outputs/response.md").read_text()
        lines = response.splitlines()
        recipe_lines = [
            line.strip() for line in lines
            if line.strip().startswith("mvn -U org.openrewrite.maven:rewrite-maven-plugin:")
        ]
        command_passed = bool(recipe_lines) and all(
            shlex.split(line) == expected_command for line in recipe_lines
        )
        expectations = [{
            "text": case["assertions"][0],
            "passed": command_passed,
            "evidence": "Automated shlex argument comparison with the commands embedded in SKILL.md: "
                        + json.dumps(recipe_lines),
        }]
        decisions = reviews[f"{directory.name}/{configuration}"]
        assert len(decisions) == len(case["assertions"]) - 1
        for assertion, decision in zip(case["assertions"][1:], decisions):
            start, end = decision["lines"]
            assert 1 <= start <= end <= len(lines)
            evidence = f"response.md lines {start}-{end}:\n" + "\n".join(lines[start - 1:end])
            if decision.get("note"):
                evidence += "\nReview note: " + decision["note"]
            expectations.append({
                "text": assertion, "passed": decision["passed"], "evidence": evidence,
            })
        passed = sum(item["passed"] for item in expectations)
        total = len(expectations)
        summary = {"passed": passed, "failed": total - passed,
                   "total": total, "pass_rate": passed / total}
        save(run_dir / "grading.json", {
            "expectations": expectations,
            "summary": summary,
            "eval_feedback": {
                "overall": "Decision-trace simulation only. Both configurations receive explicit fixture evidence and the source workflow; these checks cannot prove live migration correctness or measure autonomous discovery."
            },
        })
        runs.append({
            "eval_id": case["id"], "eval_name": case["name"],
            "configuration": configuration, "run_number": 1,
            "result": summary, "expectations": expectations, "notes": [],
        })

run_summary = {}
for configuration in ("with_skill", "without_skill"):
    rates = [run["result"]["pass_rate"] for run in runs
             if run["configuration"] == configuration]
    run_summary[configuration] = {"pass_rate": {
        "mean": statistics.mean(rates), "stddev": statistics.stdev(rates),
        "min": min(rates), "max": max(rates),
    }}
delta = (run_summary["with_skill"]["pass_rate"]["mean"]
         - run_summary["without_skill"]["pass_rate"]["mean"])
run_summary["delta"] = {"pass_rate": f"{delta:+.2f}"}
notes = [
    "Three isolated decision-trace simulations, one run per scenario per configuration. No application upgrade or live Maven validation was performed.",
    "All 18 assertions pass in both configurations. This shows workflow adherence on these cases, not a measured improvement over the original workflow.",
    "Both configurations receive the original workflow and explicit simulated outcomes. These tests do not measure release discovery, recipe availability, repair implementation, or live runtime behavior.",
    "Only exact recipe-command arguments are graded automatically; semantic ordering, blocker handling, and reporting were reviewed against the complete outputs and fixtures.",
    "One sample per configuration per scenario does not establish repeatability or within-scenario variance.",
    "Token usage and execution duration were not provided by the subagent completion notifications. They are unavailable and excluded from metrics rather than reported as zero.",
]
benchmark = {
    "metadata": {
        "skill_name": "springboot-upgrade",
        "skill_path": ".opencode/skills/springboot-upgrade",
        "executor_model": "default subagent model (not reported by tool)",
        "analyzer_model": "github-copilot/gpt-6-astra",
        "timestamp": datetime.now(timezone.utc).isoformat(),
        "evals_run": [case["id"] for case in cases], "runs_per_configuration": 1,
    },
    "runs": runs, "run_summary": run_summary, "notes": notes,
}
save(iteration / "benchmark.json", benchmark)
markdown = ["# Spring Boot upgrade skill — simulated workflow evaluation", "",
            "| Scenario | With skill | Workflow-only baseline |",
            "| --- | --- | --- |"]
for case in cases:
    pair = [run for run in runs if run["eval_id"] == case["id"]]
    scores = [f"{run['result']['passed']}/{run['result']['total']}" for run in pair]
    markdown.append(f"| {case['name']} | {scores[0]} | {scores[1]} |")
markdown.extend(["", "## Interpretation", ""] + ["- " + note for note in notes])
(iteration / "benchmark.md").write_text("\n".join(markdown) + "\n")
print("Checked source-command fidelity and generated six grades plus benchmark.json/md.")
