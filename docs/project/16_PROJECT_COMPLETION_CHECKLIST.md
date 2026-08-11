# Project Completion Checklist

Status: current gates only; historical execution detail belongs to Git history
and `evidence/`
Verification: source-backed; see Git history and DocumentationContractTest

Use this file to select the highest-priority incomplete task whose dependencies
are complete. Do not append per-day implementation diaries.

## 1. Research MVP

- [x] Plugin module builds and is packaged in the USE assembly.
- [x] USE menu actions and `ViewFrame` lifecycle are verified.
- [x] Jason 3.3.0 parses valid/invalid AgentSpeak fixtures.
- [x] Multi-file import preserves partial success and structured diagnostics.
- [x] Jason AST is normalized to immutable plugin-owned IR.
- [x] Unsupported supported-by-Jason syntax remains visible as `ASL-002`.
- [x] Source spans, parser version, golden IR, and deterministic indexes exist.
- [x] Explorer imports off the EDT and protects against stale completions.
- [x] Problems, details, and re-import behavior are available in the GUI.
- [x] USE UML/OCL model and snapshot are projected read-only.
- [x] Mapping suggestions require explicit user confirmation.
- [x] Mapping persistence, fingerprints, and staleness checks exist.
- [x] All 22 documented rules are registered and configuration-selectable.
- [x] Suppressions require rule ID plus exact source fingerprint and reason.
- [x] OCL/effect checks distinguish PASS/FAIL/UNKNOWN and restore state.
- [x] Project rule/suppression files auto-load beside the active `.use` model.
- [x] Project-relative source identity v2 is defined and relocation-tested.
- [x] Mapping/suppression schema v2 migrates v1 safely under an explicit root.
- [x] Explorer manually refreshes current USE state without reparsing AgentSpeak.
- [x] Problems uses one immutable current-analysis application snapshot.
- [x] JSON/HTML exporters preserve issues, evidence, hashes, and suppressions.
- [x] Explorer exports its exact current analysis as atomic JSON/HTML output.
- [x] Headless gate composes the same snapshot and distinguishes CI outcomes.
- [x] Official JaCaMo 1.3.0 parses Auction `.jcm` into portable MAS IR.
- [x] `.jcm` agent import preserves partial success and explicit unsupported resources.
- [x] `.jcm` projects compose through the shared immutable analysis snapshot service.
- [x] Explorer and headless CLI expose the shared `.jcm` project analysis path.
- [x] Official Moise 1.1 normalizes the bounded Auction organization subset
  into immutable portable IR with explicit diagnostics.
- [x] Typed CArtAgO environment mappings persist in a strict portable document and revalidate stale targets.
- [x] Snapshot-derived traceability explains Auction issues with portable IDs and explicit mapping gaps.
- [x] Static CArtAgO artifact pilot detects operation/arity/property mutants and preserves dynamic UNKNOWN.
- [x] Auction baseline, four mutants, oracle, metrics, and diagrams are tracked.
- [x] Versioned Auction evaluation manifest runs the real headless service in isolated workspaces and classifies one PASS plus four DETECTED cases with deterministic reports.
- [x] Performance, package, GUI smoke, and clean-clone evidence exist.
- [x] User, developer, install, license, limitation, and threat guides exist.

## 2. Quality Gates

- [x] `mvn --batch-mode --no-transfer-progress -pl use-bdi-plugin -am test`.
- [x] `mvn --batch-mode --no-transfer-progress clean verify`.
- [x] Extracted assembly contains the shaded plugin, JaCaMo/Jason parsers,
  CArtAgO artifact API, Moise OS parser/schema, and third-party notices.
- [x] Package/parser/report/menu smoke returns `GUI_SMOKE_OK`.
- [x] Documentation links, stable facts, and evidence inventory are tested.
- [x] Canonical documentation uses the compact inventory; redundant generic
  project documents are rejected by `DocumentationContractTest`.
- [x] Analysis does not leave current USE state changed.
- [x] No Jason/USE/Swing concrete types cross into normalized IR.
- [x] No unsupported syntax or OCL uncertainty is silently ignored.
- [x] Packaged scoped Auction evaluation repeats byte-stable JSON/CSV/HTML output, separates process failures, and ends with `AUCTION_EVALUATION_OK`.

Latest validated baseline after the static Moise organization slice:

- focused T14 organization/project-import tests: 9 pass;
- focused documentation/Explorer regression tests: 11 pass;
- plugin tests: 147 pass;
- `mvn -pl use-bdi-plugin -am test`: all four reactor modules succeed;
- package smoke verifies CArtAgO/Jason/JaCaMo/Moise static classes and
  returns `GUI_SMOKE_OK`;
- packaged process smoke verifies Auction JSON/HTML plus exits 1 and 3;
- root `mvn verify`: all five modules, 1 USE core integration test, 121 GUI
  integration tests, 147 plugin tests, and ZIP/TAR assembly succeed;
- packaged `auction-evaluation.ps1` returns `AUCTION_EVALUATION_OK` twice and
  compares deterministic JSON/CSV/HTML outputs.

T11 project-analysis evidence:

- `MasProjectAnalysisServiceTest`: 3 focused tests pass;
- valid Auction composition, partial invalid/missing sources, and relocation
  identity are covered;

T12 project-entry-point evidence:

- `BdiQualityGateMainTest`, `BdiExplorerViewTest`, `BdiImportWorkerTest`, and
  `ImportBdiActionTest`: 22 focused tests pass;
- GUI action/menu and Explorer button select one `.jcm`, show resolved agents
  and sorted project diagnostics, and export the held immutable snapshot;
- CLI `--jcm` shares T11 composition, rejects conflicts/missing/wrong-extension
  input with exit 3, and preserves deterministic JSON/HTML output behavior;
- package smoke and root verification are required before release integration.

T13 environment-mapping evidence:

- `EnvironmentMappingFileRepositoryTest`: 4 tests cover typed operation/property
  round-trip, deterministic bytes, checkout relocation, confirmation filtering,
  unknown status, malformed/unknown/duplicate records, invalid roots, and the
  unchanged `.bdimap.json` repository contract;
- `AuctionEnvironmentMappingPersistenceTest`: 2 tests prove the persisted path
  preserves the `ENV-003` dynamic `UNKNOWN` oracle and emits `ENV-004` for a
  removed UML target without passing it silently to the legacy rules;
- `RuleCatalogCompletenessTest` includes the separate `ENV-004` catalog entry;
- [CArtAgO environment mapping persistence evidence](evidence/cartago-environment-mapping-persistence.md)
  records schema, source identity, rejection policy, and limitations.

T14 Moise organization evidence:

- official Moise tag `v1.1`, API, LGPLv3 license, dependency, local SHA-256,
  schema, and Auction fixture are recorded in the spike evidence;
- `MoiseOrganizationParserAdapterTest`: 4 tests cover deterministic roles,
  groups, schemes, goals, missions, norms/cardinalities, explicit missing/
  invalid/duplicate/unsupported outcomes, and the adapter-only type boundary;
- `MasProjectImportServiceTest`: 5 tests cover normalized golden project IR,
  relocation, partial failure, and direct AgentSpeak compatibility;
- ADR-0034 supersedes ADR-0032 and explicitly excludes enactment/runtime/rules.

T16 evaluation evidence:

- `EvaluationManifestCodecTest`: canonical round-trip, unknown-field,
  duplicate-key, duplicate-ID, path-traversal, missing-oracle, and malformed
  JSON-number validation pass;
- `EvaluationRunnerTest`: real headless analysis, temporary-workspace
  isolation, input immutability, fixed-timestamp report stability, missing input,
  timeout, tool-error, and reviewed-manifest integration pass;
- the reviewed manifest declares five Auction cases and the real integration
  result is `1 PASS + 4 DETECTED`, with zero missed, unexpected, unknown,
  unsupported, timeout, or execution-error cases;
- [Auction evaluation evidence](evidence/auction-evaluation.md) records the
  manifest/corpus/configuration hashes, deterministic JSON/CSV/HTML outputs,
  state-fixture boundary, excluded `LIVE_CARTAGO`/`MOISE_ORGANIZATION_IR`
  layers, and the packaged `AUCTION_EVALUATION_OK` smoke command.

The isolated root run avoids local Java language-server writes/locks in Maven
`target`; this is an environment race, not a source exception.

## 3. Open Must Tasks

- [ ] Create reviewed Git tag `v1.0.0-thesis-rc` after branch integration.
- [ ] Complete and verify backup of source, data, generated report, and thesis
  slides; current source backup cannot fabricate absent external artifacts.

These are release-owner tasks. Do not create a tag or claim a complete backup
without the user's release decision and the external artifact locations.

## 4. Optional Task

- [ ] Import House Building as an exploratory second corpus.

House Building is not required for the Auction MVP and must not delay the
release gates.

## 5. Accepted Residual Gaps

The following are future work, not hidden completion claims:

- OD-004: strict mapping JSON unknown-field policy;
- OD-005: automatic subscription after USE state changes; manual refresh exists;
- OD-006: external thesis artifact locations and release ownership;
- Live CArtAgO, Moise enactment/monitoring, organization consistency rules,
  and runtime integration.
- General/statistical correctness beyond the five declared Auction evaluation
  cases; the runner is scoped evidence, not a quality-proof generator.

The prioritized development candidates are maintained in
[the idea backlog](../idea/idea.md).

## 6. Evidence Map

| Claim | Primary evidence |
| --- | --- |
| Import/IR/index | importer, IR, golden, and index tests |
| JaCaMo project import | parser spike, MAS golden, diagnostics, relocation, package smoke |
| Static Moise organization import | official source/API/license/checksum, organization golden/diagnostic/boundary tests, ADR-0034 |
| Unified traceability | Auction complete-chain, explicit-gap, certainty, deduplication, and portability tests |
| CArtAgO environment pilot | official annotation adapter, three mutants, UNKNOWN state, trace, catalog, package smoke |
| Persisted environment mappings | typed document codec/repository, relocation, candidate/unknown, stale-target, BDI regression, and catalog tests |
| Plugin GUI | action/Explorer tests and `scripts/smoke.ps1` |
| Mapping/rules | mapping, orchestrator, catalog, config, suppression tests |
| USE/OCL safety | facade/evaluator tests and fingerprint assertions |
| Auction evaluation | `docs/project/evidence/` and case-study tests |
| Packaging | release tests, assembly, smoke, clean-clone script |
| Documentation | `DocumentationContractTest`, traceability, Git history |

## 7. Per-Task Completion Rule

A task is complete only when:

1. acceptance criteria identify affected requirements;
2. the smallest vertical slice is implemented within accepted boundaries;
3. success, failure, and unknown/unsupported behavior have tests;
4. focused/module tests pass, with root verification when integration changes;
5. relevant docs, checklist, traceability, and ADRs are synchronized;
6. `git diff --check` passes and unrelated work is preserved;
7. the coherent change is committed on a feature branch.
