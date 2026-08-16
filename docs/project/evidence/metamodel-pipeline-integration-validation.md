# Metamodel-Aware Pipeline Integration Validation

Date: 2026-08-17

Task: 09 — Metamodel-Aware Pipeline Integration

Result: **PASS**

## Audited baseline and decision

- `BdiMetamodelVersion.CURRENT` (`0.1.0`) already identifies the normalized
  Java BDI IR/index contract used by `.bdimap`; it is not the broader analysis
  profile identity and was not renamed or duplicated.
- `AnalysisMetamodelDescriptor.current()` is the plugin-owned, non-EMF
  descriptor for the JaCaMo Consistency Analysis Profile. Its stable ID is
  `https://useocl.github.io/bdi/metamodel/analysis/1.0`, its version is `1.0.0`,
  and its profile name matches the Ecore artifact.
- ADR-0045 records the material snapshot/report contract decision. Historical
  reports that lack the new additive fields remain historical/unknown rather
  than being assigned guessed metadata.
- [`METAMODEL_VERSIONING.md`](../metamodel/METAMODEL_VERSIONING.md) defines
  semantic change classes, independent mapping/report/plugin version axes,
  migration policy, and evidence compatibility.

## Runtime and report integration

- `CurrentAnalysisSnapshot` owns one `AnalysisVersionMetadata`, requires the
  current descriptor, and remains immutable.
- `CurrentAnalysisSnapshotService` composes the snapshot once and invokes the
  validation function exactly once. Report, trace, and diagram packages do not
  invoke `ValidationOrchestrator`, access `UseUmlModelFacade`/`MSystem`, or
  mutate USE state.
- `CurrentAnalysisReportService` copies already-computed snapshot metadata.
  JSON and HTML exports include analysis metamodel ID/version/profile name,
  BDI IR metamodel version, sorted unique parser versions, plugin version, USE
  version, and supplied source/mapping hashes.
- Direct `.asl`, `.jcm`, invalid-source, GUI-current-snapshot, mapping,
  environment, organization, diagram, and state-fingerprint paths remain on
  the same immutable pipeline. No EMF runtime dependency, second parser,
  second validator, report-side validation, or diagram-side validation was
  introduced.

## Reproducible gate results

Commands were run from the repository root on 2026-08-17 (Asia/Bangkok).

```text
Focused integration/compatibility suite:
Tests run: 73, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS

mvn --batch-mode --no-transfer-progress -pl use-bdi-plugin -am test
Tests run: 221, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS

powershell -ExecutionPolicy Bypass -File .\use-bdi-plugin\scripts\headless-quality-gate.ps1
HEADLESS_QUALITY_GATE_OK: packaged exits 1/3 and Auction JSON/HTML reports verified

powershell -ExecutionPolicy Bypass -File .\use-bdi-plugin\scripts\auction-evaluation.ps1
EvaluationMetrics[totalCases=5, passed=1, detected=4, missed=0, unexpected=0,
unknown=0, unsupported=0, invalidInput=0, timeouts=0, executionErrors=0]
AUCTION_EVALUATION_OK: reviewed manifest detected four mutants and preserved deterministic outputs

mvn --batch-mode --no-transfer-progress clean verify
use: SUCCESS
use-core: SUCCESS (1 integration test)
use-gui: SUCCESS (121 integration tests)
USE BDI Plugin: SUCCESS (221 tests)
use-assembly: SUCCESS
BUILD SUCCESS
```

Static boundary scans found zero EMF `EObject`/runtime imports in production,
zero validation/USE-runtime imports in report, trace, or diagram packages, and
one snapshot-composition `validator.apply` call. `git diff --check` passed with
no whitespace errors.

The final documentation-only recheck initially failed to launch twice: first
because PowerShell parsed the unquoted comma in Maven's test selector, then
because the reactor applied the plugin-only selector to upstream modules. Both
were classified as invocation defects. Quoting both Maven properties and using
`surefire.failIfNoSpecifiedTests=false` corrected the command; the two selected
contract tests then passed in the four-module reactor. No source assertion was
weakened or skipped.

## Final gate

```text
Metamodel descriptor: PASS
Versioning spec: PASS
Snapshot integration: PASS
Report integration: PASS
Determinism: PASS
Direct ASL: PASS
JCM: PASS
State safety: PASS
Reactor: PASS (221/221 plugin tests)
Headless: PASS
Auction evaluation: PASS (5 cases; 1 baseline + 4 detected mutants)
Root verify: PASS (all 5 modules)
git diff --check: PASS
Open failures: 0
Result: PASS
```
