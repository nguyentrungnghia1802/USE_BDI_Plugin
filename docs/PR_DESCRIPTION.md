Feature: Reporting — JSON + HTML exporter, smoke verification, suppressions placeholder

What I changed
- Added `HtmlReportExporter` and accompanying unit test to produce a simple HTML view of the report.
- Extended `ReportMain` to write both `docs/bdi-report.json` and `docs/bdi-report.html`.
- Added a placeholder suppression config at `use-bdi-plugin/.bdi-plugin/suppressions.json`.
- Updated smoke script `use-bdi-plugin/scripts/smoke.ps1` to verify the HTML report is generated.

Why
- Provide a human-friendly HTML summary alongside the machine-readable JSON report for release evidence and quick inspection.
- Start a place to persist suppressions for future UI and export integration.

How to test
1. Run unit tests: `mvn -pl use-bdi-plugin test`
2. Run report generator: `mvn -pl use-bdi-plugin exec:java -Dexec.mainClass=org.tzi.use.plugins.bdi.report.ReportMain`
3. Run smoke script (Windows PowerShell): `use-bdi-plugin\scripts\smoke.ps1`

Files of interest
- `use-bdi-plugin/src/main/java/org/tzi/use/plugins/bdi/report/HtmlReportExporter.java`
- `use-bdi-plugin/src/test/java/org/tzi/use/plugins/bdi/report/HtmlReportExporterTest.java`
- `use-bdi-plugin/.bdi-plugin/suppressions.json`
- `use-bdi-plugin/scripts/smoke.ps1` (HTML check)
