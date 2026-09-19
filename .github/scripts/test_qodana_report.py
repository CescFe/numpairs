import json
from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch

import qodana_report


class QodanaReportTest(unittest.TestCase):
    def test_only_new_findings_survive(self):
        document = {"runs": [{"results": [
            {"baselineState": state} for state in ("new", "updated", "unchanged", "absent")
        ]}]}
        self.assertEqual(1, qodana_report.new_findings(document))
        self.assertEqual([{"baselineState": "new"}], document["runs"][0]["results"])

    def test_missing_comparison_is_not_clean(self):
        with self.assertRaisesRegex(ValueError, "baselineState"):
            qodana_report.new_findings({"runs": [{"results": [{"ruleId": "UnusedSymbol"}]}]})

    def test_failed_invocation_is_not_clean(self):
        with self.assertRaisesRegex(ValueError, "unsuccessful"):
            qodana_report.new_findings({"runs": [{"invocations": [{"executionSuccessful": False}]}]})

    def test_no_runs_is_not_clean(self):
        with self.assertRaises(ValueError):
            qodana_report.new_findings({"runs": []})

    def test_missing_report_still_publishes_evidence(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            self.assertFalse(qodana_report.publish(root / "missing", root / "out", {"SCAN_OUTCOME": "success"}))
            self.assertIn("unverified", (root / "out/summary.md").read_text())

    def test_failed_scan_cannot_publish_clean_report(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            self.assertFalse(qodana_report.publish(root / "missing", root / "out", {"SCAN_OUTCOME": "failure"}))
            self.assertIn("did not succeed", (root / "out/summary.md").read_text())

    def test_report_and_size_limits(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            source = root / "input.json"
            source.write_text(json.dumps({"runs": [{"results": [
                {"ruleId": "<unsafe>|id", "baselineState": "new"} for _ in range(60)
            ]}]}))
            with patch.object(qodana_report, "MAX_ARTIFACT_BYTES", 1):
                self.assertTrue(qodana_report.publish(source, root / "out", {"SCAN_OUTCOME": "success"}))
            summary = (root / "out/summary.md").read_text()
            self.assertEqual(50, summary.count("&lt;unsafe&gt;&#124;id"))
            self.assertIn("SARIF omitted", summary)
            self.assertFalse((root / "out/new-findings.sarif.json").exists())
            with patch.object(qodana_report, "MAX_INPUT_BYTES", 1):
                self.assertFalse(qodana_report.publish(source, root / "oversize", {"SCAN_OUTCOME": "success"}))

    def test_success_writes_sarif_and_step_summary(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            source = root / "input.json"
            source.write_text(json.dumps({"version": "2.1.0", "runs": [{"results": []}]}))
            self.assertTrue(qodana_report.publish(source, root / "out", {
                "SCAN_OUTCOME": "success", "GITHUB_STEP_SUMMARY": str(root / "step.md")
            }))
            self.assertEqual([], json.loads((root / "out/new-findings.sarif.json").read_text())["runs"][0]["results"])
            self.assertIn("New findings: 0", (root / "step.md").read_text())


if __name__ == "__main__":
    unittest.main()
