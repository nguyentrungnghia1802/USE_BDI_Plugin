# Auction Evaluation Runner Evidence

Status: implemented and packaged; evidence is scoped to the reviewed Auction
manifest, not a general correctness proof.

## Reviewed Corpus

The manifest `auction-evaluation-manifest.json` declares five cases:

- `baseline`: clean Auction inputs and the populated static snapshot;
- `STR-001-remove-bidder`: structural stale-target mutant, expected `MAP-003`;
- `SIG-001-open-arity`: signature mutant, expected `SIG-001`;
- `REF-001-bidder2`: unresolved reference mutant, expected `REF-001`;
- `OCL-001-open-precondition`: closed-state precondition mutant, expected
  `OCL-001`.

The manifest excludes `LIVE_CARTAGO` and `MOISE_ORGANIZATION_IR`. It uses the
named `auction-populated` fixture inside the private headless `MSystem`; no
JaCaMo, CArtAgO, Moise, or runtime process is started.

## Reproduction

From the repository root:

```powershell
powershell -ExecutionPolicy Bypass -File .\use-bdi-plugin\scripts\auction-evaluation.ps1
```

The script builds `use-assembly`, extracts the distribution, runs
`EvaluationRunnerMain` twice with timestamp `2026-08-11T00:00:00Z`, and compares
the JSON, CSV, and HTML SHA-256 values. Success ends with:

```text
BDI_EVALUATION_RESULT=EvaluationMetrics[totalCases=5, passed=1, detected=4, missed=0, unexpected=0, unknown=0, unsupported=0, invalidInput=0, timeouts=0, executionErrors=0]
AUCTION_EVALUATION_OK: reviewed manifest detected four mutants and preserved deterministic outputs
```

The focused suite also passes:

```text
EvaluationManifestCodecTest: 3 tests
EvaluationRunnerTest: 4 tests
```

## Fixed Run Identity

The output was generated with the fixed timestamp above. Hashes recorded by
the deterministic result are:

| Identity | SHA-256 |
| --- | --- |
| Manifest canonical identity | `050b1d420171f5778bc3950fa58d7fc3bad2a59989a6abda3820e8320c47933f` |
| Corpus identity | `a7de1f2c32e294a37b495529aac0ad97c20747f596453240ae47aa6238c907c1` |
| Configuration identity | `c987d2d2efc8e8fb1ea69b54d462d6b92ea45e93b06fa827c3338d593a8a8388` |
| `evaluation-results.json` | `9e11e35744c41a8af981748b8da8f50261ad899643741c58340e72a69a2abe0a` |
| `evaluation-results.csv` | `5860a498b44ccc1746bd5aac6172e230f723fb11f5904c6e536baf97b1f9ed36` |
| `evaluation-results.html` | `121bb2ea14098094bae96b7c80270a7d6e1f3fc07b1910684c1b5de2ec5236bf` |

The report files are under
`docs/project/evidence/auction-evaluation-run/`. The canonical output metrics
are `1 PASS + 4 DETECTED`, with no missed, unexpected, unknown, unsupported,
timeout, or execution-error case.

The baseline's underlying headless analysis retains raw out-of-scope
diagnostics and therefore has raw exit code `1`; the evaluation status is
`PASS` because the declared baseline oracle forbids only the four scoped mutant
tokens. This distinction keeps ordinary rule output visible without allowing
unrelated findings to change the reviewed corpus classification.

## Safety And Limits

Each case is copied to a unique temporary workspace and removed in `finally`.
The runner validates paths, IDs, oracle entries, evidence anchors, timeout, and
schema before execution. Tests compare source bytes before/after and verify
that the caller's USE state fingerprint is unchanged. Metrics describe these
five reviewed cases only; they do not establish statistical precision/recall,
runtime JaCaMo behavior, live CArtAgO consistency, or Moise organization
semantics.

## Task 10 repeated evaluation

The packaged script was rerun on 2026-08-17 after metamodel-aware report
integration. Both executions again produced `totalCases=5`, `passed=1`,
`detected=4`, and zero missed, unexpected, unknown, unsupported, invalid-input,
timeout, or execution-error cases. JSON/CSV/HTML hashes remained exactly the
fixed identities above. `AuctionMutantDiagramTest` also passed both tests,
linking each reviewed mutant to its real bounded trace/highlight path without
changing the external oracle or hiding raw out-of-scope findings.
