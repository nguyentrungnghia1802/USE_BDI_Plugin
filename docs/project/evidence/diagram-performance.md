# Diagram Performance Evidence

Date: 2026-08-17 (Asia/Bangkok)

Result: **PASS** as a reproducible environment sample; no universal timing
threshold or real-time claim is made.

## Protocol

`DiagramPerformanceBenchmarkTest` prepares immutable current-analysis
snapshots for Family Person, Smart Queue, Smart Home, and Auction. For each
case it measures four presentation stages independently:

1. `BdiDiagramBuilder` projection/build;
2. deterministic `BdiDiagramLayout.compute`;
3. `ALL` mode plus no-hidden-layer bounded focus projection;
4. in-memory deterministic SVG rendering of the visible projection.

The harness performs three warmups and 15 measured repetitions per case. Every
iteration must equal the reference `DiagramModel`, layout every node, reproduce
the same focused model, and produce byte-identical SVG. Swing repaint is not
timed. Input parsing/snapshot composition is prepared outside the measurements.

## Environment

| Item | Recorded value |
|---|---|
| Benchmark timestamp | `2026-08-16T22:59:32.185066100Z` |
| Fixed analysis timestamp | `2026-08-17T00:00:00Z` |
| Source identity | `0919ef74f2d929d341a48a041c69e9906e2d22b1` plus the Task 10 benchmark working tree |
| Java/JVM | `21.0.5`; Java HotSpot 64-Bit Server VM |
| OS | Windows 11 10.0 amd64 |
| CPU | Intel64 Family 6 Model 154 Stepping 3, GenuineIntel; 16 logical processors |
| Physical RAM | 16088 MiB |
| Graphics mode | non-headless JVM; no window, repaint, or interactive input used |
| Mode/layers | `ALL`; hidden layers `[]`; deterministic first-node focus |

## Observed timings

All values are milliseconds from the second of the two required script runs.

| Case | Nodes | Edges | Groups | Build median / p95 | Layout median / p95 | Focus/filter median / p95 | SVG median / p95 |
|---|---:|---:|---:|---:|---:|---:|---:|
| Family Person | 8 | 8 | 1 | 0.7061 / 1.0119 | 0.0857 / 0.2120 | 0.2805 / 0.6365 | 0.2951 / 0.5492 |
| Smart Queue | 55 | 59 | 1 | 2.2876 / 2.9715 | 0.1815 / 0.3147 | 0.6134 / 1.1979 | 0.1839 / 0.2803 |
| Smart Home | 8 | 8 | 1 | 0.2527 / 0.3722 | 0.0425 / 0.0569 | 0.1326 / 0.1456 | 0.1271 / 0.4115 |
| Auction | 43 | 48 | 2 | 1.5000 / 4.0162 | 0.1087 / 0.2744 | 0.4960 / 0.6992 | 0.1072 / 0.1658 |

The focus projection contained two nodes and one edge in every case because the
protocol selects the stable first node and its bounded neighborhood. This is a
control-path measurement, not a readability judgment or a large-graph scaling
study.

## Reproduction and determinism

```powershell
powershell -ExecutionPolicy Bypass -File .\use-bdi-plugin\scripts\diagram-performance.ps1
```

The script runs the benchmark twice. It compares case order, node/edge/group
counts, visible counts, mode/layers, per-case SVG hashes, and the aggregate
structural fingerprint while allowing environment-dependent timing samples to
differ. The recorded repeated run ended with:

```text
DIAGRAM_PERFORMANCE_OK: four canonical cases preserved structural fingerprint
19c5cc0d5d9e103db160a71c3e0ba2e45f9ef20eedfacb803b1f9f4a64135591
across two runs
```

The machine-readable current sample is generated at
`use-bdi-plugin/target/performance/diagram-performance.json`; `target` output is
deliberately not checked in. Fixed inputs and structural/SVG hashes are the
deterministic contract. Timing values are observational.

## Work log and limits

The first focused compile found an invalid three-segment `Path.resolve` call in
the new harness (`SOURCE_DEFECT`); it was replaced by chained single-segment
resolves and the test passed. The wrapper then failed before benchmarking
because its PowerShell pipeline did not retain Git identity reliably
(`TEST_DEFECT`); the native exit code and string normalization were separated,
after which the two-run gate passed. No production diagram code changed.

This one-machine, 15-sample measurement has no confidence interval, memory
profile, repaint timing, interaction study, statistical comparison, or
catastrophic-regression threshold. It demonstrates a repeatable protocol and
records current behavior; it does not prove “real-time” performance.

## Task 12 release-candidate freeze

The exact documented script was rerun on 2026-08-17 (Asia/Bangkok) against
candidate `6a84fc5ee0cff7e06c1873d0a5d7c05e52fdc0c0`, using the same four inputs,
three warmups, 15 measured repetitions, fixed analysis timestamp, `ALL` mode,
no hidden layers, and deterministic first-node focus. Java `21.0.5`, Windows
11 `10.0` amd64, 16 logical processors, and 16088 MiB physical RAM were
recorded. The second run observed:

| Case | Nodes | Edges | Groups | Build median / p95 | Layout median / p95 | Focus/filter median / p95 | SVG median / p95 |
|---|---:|---:|---:|---:|---:|---:|---:|
| Family Person | 8 | 8 | 1 | 0.5652 / 1.0951 | 0.0730 / 0.1231 | 0.2194 / 0.3826 | 0.2906 / 0.4209 |
| Smart Queue | 55 | 59 | 1 | 2.8450 / 5.2262 | 0.1893 / 0.7405 | 0.7021 / 1.6565 | 0.2015 / 0.5762 |
| Smart Home | 8 | 8 | 1 | 0.2725 / 0.5699 | 0.0413 / 0.0619 | 0.1351 / 0.3319 | 0.1320 / 0.2383 |
| Auction | 43 | 48 | 2 | 1.4296 / 4.9789 | 0.1009 / 0.4460 | 0.5172 / 0.6565 | 0.0914 / 0.1491 |

The release run ended with `DIAGRAM_PERFORMANCE_OK` and the unchanged
structural fingerprint
`19c5cc0d5d9e103db160a71c3e0ba2e45f9ef20eedfacb803b1f9f4a64135591`.
These timings remain an environment sample, not a universal performance bound.
