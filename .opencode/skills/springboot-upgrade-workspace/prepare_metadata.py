"""Materialize review metadata from the versioned scenario definitions."""

import json
from pathlib import Path


workspace = Path(__file__).resolve().parent
skill = workspace.parent / "springboot-upgrade"
evals = json.loads((skill / "evals/evals.json").read_text())
for case in evals["evals"]:
    directory = workspace / "iteration-1" / f"eval-{case['id']}-{case['name']}"
    directory.mkdir(parents=True, exist_ok=True)
    metadata = {
        "eval_id": case["id"],
        "eval_name": case["name"],
        "prompt": case["prompt"],
        "assertions": case["assertions"],
    }
    (directory / "eval_metadata.json").write_text(json.dumps(metadata, indent=2) + "\n")
print("Prepared metadata for three simulated evaluations.")
