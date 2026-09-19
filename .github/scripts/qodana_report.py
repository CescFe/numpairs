"""Publish only new incremental findings, with bounded output and explicit run health."""

import html
import json
import os
from pathlib import Path
import sys

MAX_INPUT_BYTES = 50 * 1024 * 1024
MAX_ARTIFACT_BYTES = 10 * 1024 * 1024
MAX_ROWS = 50


def new_findings(document):
    runs = document.get("runs", [])
    if not runs:
        raise ValueError("SARIF has no runs")
    count = 0
    for run in runs:
        for invocation in run.get("invocations", []):
            if invocation.get("executionSuccessful") is False:
                raise ValueError("SARIF reports unsuccessful execution")
        results = run.get("results", [])
        if any(result.get("baselineState") not in
               {"new", "unchanged", "updated", "absent"} for result in results):
            raise ValueError("Missing/unknown baselineState: incremental comparison is unverified")
        run["results"] = [result for result in results if result["baselineState"] == "new"]
        count += len(run["results"])
    return count


def safe(value):
    return html.escape(str(value)[:300]).replace("\n", " ").replace("\r", " ").replace("|", "&#124;")


def publish(source, destination, environment):
    destination.mkdir(parents=True, exist_ok=True)
    lines = ["# IntelliJ inspections — informational pilot", "",
             f"Scan outcome: {safe(environment.get('SCAN_OUTCOME', 'unknown'))}",
             f"Scan seconds (excluding image pull): {safe(environment.get('SCAN_SECONDS') or 'unavailable')}",
             f"PR cache hit: {safe(environment.get('CACHE_HIT') or 'false')}",
             f"Merge base: {safe(environment.get('DIFF_START', 'unknown'))}",
             f"Head: {safe(environment.get('DIFF_END', 'unknown'))}", ""]
    healthy = False
    try:
        if environment.get("SCAN_OUTCOME") != "success":
            raise ValueError("Scan did not succeed; no clean-analysis claim is possible")
        if source.stat().st_size > MAX_INPUT_BYTES:
            raise ValueError("Raw SARIF exceeds the 50 MiB processing limit")
        document = json.loads(source.read_text(encoding="utf-8"))
        count = new_findings(document)
        encoded = json.dumps(document, ensure_ascii=False).encode("utf-8")
        lines.extend([f"New findings: {count}. Unchanged, updated and absent findings are excluded.", "",
                      "This is not proof of IDE parity or a successful Android project import;",
                      "review runner logs and the pilot verification checklist.", ""])
        if len(encoded) <= MAX_ARTIFACT_BYTES:
            (destination / "new-findings.sarif.json").write_bytes(encoded)
        else:
            lines.extend(["SARIF omitted: filtered report exceeds the 10 MiB artifact limit.", ""])
        lines.extend(["| Inspection | File | Line |", "| --- | --- | --- |"])
        results = [result for run in document["runs"] for result in run["results"]]
        for result in results[:MAX_ROWS]:
            locations = result.get("locations") or [{}]
            location = locations[0].get("physicalLocation", {})
            uri = location.get("artifactLocation", {}).get("uri", "unknown")
            line = location.get("region", {}).get("startLine", "")
            lines.append(f"| {safe(result.get('ruleId', 'unknown'))} | {safe(uri)} | {safe(line)} |")
        if count > MAX_ROWS:
            lines.extend(["", f"Only the first {MAX_ROWS} findings are listed here."])
        healthy = True
    except (OSError, ValueError, TypeError, KeyError, AttributeError) as error:
        lines.extend([f"**Analysis unavailable/unverified:** {safe(error)}", "",
                      "This must not be interpreted as zero findings. See the workflow logs."])
    summary = "\n".join(lines) + "\n"
    (destination / "summary.md").write_text(summary, encoding="utf-8")
    if environment.get("GITHUB_STEP_SUMMARY"):
        with open(environment["GITHUB_STEP_SUMMARY"], "a", encoding="utf-8") as output:
            output.write(summary)
    print(summary)
    return healthy


if __name__ == "__main__":
    sys.exit(0 if publish(Path(sys.argv[1]), Path(sys.argv[2]), os.environ) else 1)
