# Static JaCaMo Project Analysis Service Evidence

Status: T11 evidence, validated 2026-08-11

`MasProjectAnalysisService` composes the verified JaCaMo 1.3.0 project importer
with the existing immutable current-analysis snapshot service. It does not
start a JaCaMo runtime and does not add GUI or CLI entry points yet.

Automated evidence:

- `MasProjectAnalysisServiceTest` has 3 passing tests;
- Auction `.jcm` resolves three agent instances and two AgentSpeak models;
- invalid and missing AgentSpeak sources preserve the valid model and explicit
  `JCM-002`/`JCM-004` diagnostics;
- two relocated copies serialize the same portable `MasProjectModel` and do not
  expose the temporary checkout path.

The service delegates validation to `CurrentAnalysisSnapshotService`, keeps
project diagnostics separate from BDI diagnostics, and captures a fixed caller
timestamp for deterministic snapshot tests. GUI/headless project selection is
intentionally deferred to T12.
