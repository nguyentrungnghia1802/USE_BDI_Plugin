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
- [x] Snapshot-derived traceability explains Auction issues with portable IDs and explicit mapping gaps.
- [x] Static CArtAgO artifact pilot detects operation/arity/property mutants and preserves dynamic UNKNOWN.
- [x] Auction baseline, four mutants, oracle, metrics, and diagrams are tracked.
- [x] Performance, package, GUI smoke, and clean-clone evidence exist.
- [x] User, developer, install, license, limitation, and threat guides exist.

## 2. Quality Gates

- [x] `mvn --batch-mode --no-transfer-progress -pl use-bdi-plugin -am test`.
- [x] `mvn --batch-mode --no-transfer-progress clean verify`.
- [x] Extracted assembly contains the shaded plugin, JaCaMo/Jason parsers, and
  CArtAgO artifact API without Moise or other excluded runtime transitives.
- [x] Package/parser/report/menu smoke returns `GUI_SMOKE_OK`.
- [x] Documentation links, stable facts, and evidence inventory are tested.
- [x] Canonical documentation uses the compact inventory; redundant generic
  project documents are rejected by `DocumentationContractTest`.
- [x] Analysis does not leave current USE state changed.
- [x] No Jason/USE/Swing concrete types cross into normalized IR.
- [x] No unsupported syntax or OCL uncertainty is silently ignored.

Latest validated baseline after the static CArtAgO environment pilot:

- focused CLI/current-snapshot/report tests: 10 pass;
- plugin tests: 121 pass;
- `mvn -pl use-bdi-plugin -am test`: all four reactor modules succeed;
- package smoke verifies CArtAgO/Jason/JaCaMo classes, excludes Moise, and
  returns `GUI_SMOKE_OK`;
- packaged process smoke verifies Auction JSON/HTML plus exits 1 and 3;
- root `mvn clean verify`: all five modules, 121 GUI integration tests, and
  ZIP/TAR assembly succeed.

T11 project-analysis evidence:

- `MasProjectAnalysisServiceTest`: 3 focused tests pass;
- valid Auction composition, partial invalid/missing sources, and relocation
  identity are covered;
- GUI/CLI `.jcm` entry-point wiring remains the next planned slice.

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
- JaCaMo GUI/CLI composition, persisted/live CArtAgO, Moise, and runtime integration.

The prioritized development candidates are maintained in
[the idea backlog](../idea/idea.md).

## 6. Evidence Map

| Claim | Primary evidence |
| --- | --- |
| Import/IR/index | importer, IR, golden, and index tests |
| JaCaMo project import | parser spike, MAS golden, diagnostics, relocation, package smoke |
| Unified traceability | Auction complete-chain, explicit-gap, certainty, deduplication, and portability tests |
| CArtAgO environment pilot | official annotation adapter, three mutants, UNKNOWN state, trace, catalog, package smoke |
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
