# Task 10 Research Evaluation Validation

Date: 2026-08-17

Result: **PASS**

## Delivered evaluation

- [`metamodel-coverage.md`](metamodel-coverage.md) classifies all 48 profile
  EClasses: 43 supported, four declaration-only, one partial, and zero
  in-profile out-of-scope. It traces every class to producer, Java/test anchor,
  fixture basis, rules, and diagram evidence without treating the count as
  semantic completeness.
- [`correspondence-coverage.md`](correspondence-coverage.md) evaluates the
  closed 11-type inventory: six core, two environment, and three organization
  correspondences. All have model, candidate/confirmed, rule, missing/unknown,
  diagram/trace, and issue-report paths; eight have persistence and the three
  organization types explicitly do not.
- The five-case Auction oracle remained unchanged: one baseline passed and all
  four reviewed mutants were detected with zero missed or process-error cases.
  Repeated JSON/CSV/HTML hashes remained stable, and real snapshot diagrams
  preserved each mutant's bounded evidence path and certainty.
- [`diagram-performance.md`](diagram-performance.md) closes the dedicated
  presentation benchmark gap for Family Person, Smart Queue, Smart Home, and
  Auction. It records build/layout/focus/SVG measurements while using exact
  model/SVG equality and a repeated structural fingerprint as the deterministic
  contract.
- Threats, limitations, future work, import performance, classification,
  Auction runner, and traceability evidence were updated in place.

## Commands and results

```text
Focused metamodel/correspondence/mutant/trace/performance suite:
Tests run: 23, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS

mvn --batch-mode --no-transfer-progress -pl use-bdi-plugin -am test
Tests run: 222, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS

powershell -ExecutionPolicy Bypass -File .\use-bdi-plugin\scripts\auction-evaluation.ps1
EvaluationMetrics[totalCases=5, passed=1, detected=4, missed=0, unexpected=0,
unknown=0, unsupported=0, invalidInput=0, timeouts=0, executionErrors=0]
AUCTION_EVALUATION_OK (two deterministic packaged executions)

powershell -ExecutionPolicy Bypass -File .\use-bdi-plugin\scripts\performance.ps1
PERFORMANCE_BENCHMARK_OK (Smart Queue; 2 warmups, 7 measurements)

powershell -ExecutionPolicy Bypass -File .\use-bdi-plugin\scripts\diagram-performance.ps1
DIAGRAM_PERFORMANCE_OK (4 cases; 3 warmups, 15 measurements each;
structural fingerprint stable across two runs)

mvn --batch-mode --no-transfer-progress clean verify
use: SUCCESS
use-core: SUCCESS (1 integration test)
use-gui: SUCCESS (121 integration tests)
USE BDI Plugin: SUCCESS (222 tests)
use-assembly: SUCCESS
BUILD SUCCESS
```

`git diff --check`, local-link validation, required-file checks, placeholder
scans, and the thesis evidence contract all passed. The evaluation runner and
snapshot/GUI suites retained before/after state fingerprints and isolated
temporary workspaces.

## Failure and correction log

The new diagram harness first failed test compilation because its fixture
locator called a nonexistent three-argument `Path.resolve` overload
(`SOURCE_DEFECT`). Chained single-segment resolves corrected the cause. The
wrapper then failed before running because Git identity was read through an
unreliable PowerShell pipeline (`TEST_DEFECT`); capturing the native exit code
and normalizing the returned string separately corrected it. Focused,
two-run benchmark, plugin, and root gates were rerun after correction. No test,
oracle, expected mutant result, or timing threshold was weakened.

## Final gate

```text
Metamodel coverage: PASS (48/48 classified; no semantic-completeness claim)
Correspondence coverage: PASS (11/11 traced; persistence 8/11 by design)
Auction baseline/mutants: PASS (1 PASS + 4 DETECTED; 0 missed/errors)
Trace/diagram evidence: PASS (four bounded real-snapshot mutant paths)
Diagram performance: PASS (four canonical cases; reproducible protocol)
Determinism: PASS (report hashes, model/SVG equality, structural fingerprint)
State safety: PASS
Threats/limitations: PASS
Reactor: PASS (222/222 plugin tests)
Root verify if required: PASS (all 5 modules)
git diff --check: PASS
Open failures: 0
Result: PASS
```
