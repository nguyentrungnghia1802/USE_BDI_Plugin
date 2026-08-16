# Baseline Reconciliation

Status: Task 01 verified baseline
Verification time: 2026-08-17T04:14:24+07:00

## Repository And Toolchain

| Item | Verified value | Source |
| --- | --- | --- |
| Repository root | `D:/_CODE_BANK/Project_/vnu-sme-lab/use` | `git rev-parse --show-toplevel` |
| Branch used for the roadmap | `feat/metamodel-guided-roadmap` | `git status --short --branch` |
| Initial worktree | Clean | `git status --short` before Task 01 |
| Maven | 3.9.9 | `mvn -version` |
| Build JDK | Oracle JDK 21.0.5 | Maven runtime and explicit `JAVA_HOME` |
| Raw PATH Java | Oracle JRE 1.8.0_501 | `java -version`; not used for gates |

The machine PATH initially resolved `java` to Java 8 while Maven resolved JDK
21. Every recorded gate explicitly set `JAVA_HOME` and prepended
`D:\DevTools\Java\jdk-21.0.5\bin`, so the required Java 21 environment was
used without changing repository source or hiding the host configuration gap.

The root reactor declares `use-core`, `use-gui`, `use-bdi-plugin`,
`use-assembly`, `use-validator`, `use-filmstrip`, `use-rtl`, and `use-frsl`.
The Task 01 module and package gates use the four/five-module dependency closure
selected by `-pl use-bdi-plugin -am` and `-pl use-assembly -am`.

## Verified Versions And Schemas

| Contract | Verified value | Authoritative location |
| --- | --- | --- |
| USE | `7.1.1` | root/plugin POMs and `useplugin.xml` processing instruction |
| Java target | `21` for the plugin | root POM plus plugin compilation output |
| Plugin manifest | `org.tzi.use.plugins.bdi`, `0.1.0` | `useplugin.xml` and Maven JAR manifest entries |
| Jason | `3.3.0` | plugin POM and packaged parser smoke |
| JaCaMo | `1.3.0` | plugin POM and packaged class inspection |
| CArtAgO | `3.1` | plugin POM and packaged class inspection |
| Moise | `1.1` | plugin POM and packaged class/schema inspection |
| BDI metamodel/index | `0.1.0` | `BdiMetamodelVersion.CURRENT` |
| BDI mapping | `0.2.0` | `MappingDocument.CURRENT_SCHEMA_VERSION` |
| Suppression | `0.2.0` | `SuppressionJsonCodec.CURRENT_SCHEMA_VERSION` |
| Rule configuration | `0.1.0` | `RuleConfiguration.CURRENT_SCHEMA_VERSION` |
| Environment mapping | `0.1.0` | `EnvironmentMappingDocument.CURRENT_SCHEMA_VERSION` |
| Evaluation manifest/result | `0.1.0` | `EvaluationManifest.CURRENT_SCHEMA_VERSION` |
| Report | no independent schema version | report carries plugin/USE/parser/metamodel metadata from the supplied snapshot |

Canonical source-of-truth order used by this reconciliation:

1. executable source, descriptors, fixtures, and passing tests;
2. accepted ADR invariants in `DECISION_LOG.md`;
3. requirements and architecture;
4. checklist, guides, and evidence;
5. historical evidence.

## Implementation And Boundary Inventory

| Capability | Verified source boundary | Result |
| --- | --- | --- |
| AgentSpeak import | `importer/JasonAslParserAdapter`, `JasonAstToIrNormalizer` | Implemented; Jason imports confined to importer/normalizer |
| Static JaCaMo project import | `importer/JaCaMoProjectParserAdapter`, `application/MasProjectImportService` | Implemented; no runtime launch |
| Static CArtAgO inspection | `importer/CArtAgOArtifactAdapter`, `model/environment` | Implemented; no workspace/live state |
| Static Moise normalization | `importer/MoiseOrganizationParserAdapter`, `model/organization` | Implemented; no enactment |
| Immutable domain | `model/ir`, `model/mas`, `model/environment`, `model/organization`, `model/mapping` | Present; no concrete parser, Swing/AWT, or USE runtime imports |
| Index and validation | `index`, `validation` | Present; validation has no diagram/UI dependency |
| USE/OCL adapter | `use` | Read-only projection plus disposable bounded variation |
| Trace and diagram | `trace`, `diagram` | Snapshot-derived immutable evidence/presentation models |
| UI and Problems | `ui`, `problems`, plugin actions | Implemented Swing presentation boundary |
| Reports and CLI | `report`, `cli`, `evaluation` | Snapshot serialization; no validation rerun in serializers |
| Packaging | `useplugin.xml`, plugin/root/assembly POMs | Shaded plugin packaged in USE distribution |

`DiagramPackageBoundaryTest`, `VisualizationBoundaryContractTest`,
`RuleCatalogCompletenessTest`, snapshot/effect tests, report tests, and the full
213-test plugin suite make the relevant package, no-second-validator,
PASS/FAIL/UNKNOWN, and state-fingerprint boundaries executable. Source import
scans also found external Jason/JaCaMo/CArtAgO/Moise imports only in their
documented adapters. No current USE state was changed by analysis tests or
headless evaluation.

## Rule And Requirement Reconciliation

`StandardConsistencyRules.create()` registers exactly 22 IDs:
`ASL-001..002`, `BDI-001..004`, `REF-001..002`, `MAP-001..003`,
`SIG-001..003`, `OWN-001`, `BEL-001`, `MSG-001`, `OCL-001..004`, and
`CTX-001`. The separate catalogs remain `ENV-001..004` and `ORG-001..003`.
`RuleCatalogCompletenessTest` passed, unknown configured rule IDs fail fast,
and UNKNOWN outcomes remain explicit rather than becoming PASS.

Requirement statuses were checked against source plus automated evidence.
`FR-REP-004` is Implemented: Explorer exports the held
`CurrentAnalysisSnapshot` atomically through `CurrentAnalysisReportService`,
with GUI parity, escaping, overwrite/failure, and snapshot tests. Its former
entry in the traceability gap list was documentation drift and is removed.
`FR-DIA-007` remains Partial because direct cross-tab source/mapping navigation
is open. `FR-DIA-008` remains Partial because refreshed raster screenshots and
a dedicated diagram benchmark are open. `FR-ENV-004` and `FR-PLG-005` remain
Planned. `FR-REL-003` remains Planned because the release tag does not exist;
`FR-REL-004` remains Partial because no `data`, `slides`, or `presentation`
directory is available.

## Evidence Inventory

| Artifact | Baseline classification |
| --- | --- |
| `auction-evaluation-manifest.json`, `auction-evaluation.md`, `auction-evaluation-run/` | Current scoped runner/oracle/results; refreshed by the Task 01 packaged run |
| `auction-baseline.bdimap.json`, `auction-signature.bdimap.json`, `auction-reference.bdimap.json`, `auction-ocl.bdimap.json` | Current portable reviewed mapping inputs |
| `auction-ground-truth.json`, `auction-metrics.csv`, `auction-classification-metrics.md`, `auction-mapping-examples.md` | Current supporting evidence for the declared four-mutant scope |
| `auction-experiment-protocol.md` | Historical/manual protocol retained; the manifest runner is the current executable evaluation authority |
| `jacamo-parser-spike.md`, `jacamo-project-entry-points.md` | Current static adapter/entry-point evidence |
| `jacamo-project-analysis-service.md` | Historical T11 slice; its "no GUI or CLI yet" statement is superseded by the entry-point evidence |
| `cartago-artifact-spike.md`, `cartago-environment-mapping-persistence.md` | Current static environment evidence; exact persistence filename confirmed |
| `moise-organization-spike.md`, `organization-consistency-pilot.md` | Current static organization evidence |
| `limitations.md`, `threats-to-validity.md`, `future-work.md` | Current claim boundaries and residual work |
| `release-package.md` | Process remains useful; its 2026-08-09 counts are historical-only |
| `traceability-graph.md` | Historical T09 slice; current source also has environment/organization and diagram contributors |
| `auction-architecture.mmd` | Stale as a whole-system snapshot: omits static `.jcm`, environment, organization, trace, diagram, and current snapshot composition |
| `bdi-metamodel-diagram.mmd` | Valid only as a bounded AgentSpeak concept sketch; incomplete for the roadmap analysis metamodel |
| `ir-class-diagram.mmd` | Valid for the core AgentSpeak IR subset; incomplete for MAS/environment/organization/mapping/trace/diagram values |
| `performance-baseline.md` | Historical import/index sample only; dedicated diagram performance evidence is missing |
| `ui-screenshots.md` and `docs/report/images/` | Partial: older raster images exist, but refreshed export/diagram screenshots remain open |

All files named in the roadmap screenshot inventory exist. No duplicate
evidence file was created. Canonical documentation links passed
`DocumentationContractTest`.

## Commands And Results

All Maven/PowerShell commands below ran with the explicit JDK 21 environment.

| Command | Result |
| --- | --- |
| `mvn --batch-mode --no-transfer-progress -pl use-bdi-plugin -am test` | PASS; plugin 213/213, four reactor modules successful |
| `mvn --batch-mode --no-transfer-progress -pl use-assembly -am package` | PASS; five reactor modules and ZIP/TAR assembly successful |
| `powershell -ExecutionPolicy Bypass -File .\use-bdi-plugin\scripts\smoke.ps1` | PASS; `GUI_SMOKE_OK` plus parser/report/package markers |
| `powershell -ExecutionPolicy Bypass -File .\use-bdi-plugin\scripts\headless-quality-gate.ps1` | PASS; expected exits 1/3 and `HEADLESS_QUALITY_GATE_OK` |
| `powershell -ExecutionPolicy Bypass -File .\use-bdi-plugin\scripts\auction-evaluation.ps1` | PASS; two equal runs, `1 PASS + 4 DETECTED`, `AUCTION_EVALUATION_OK` |
| `mvn --batch-mode --no-transfer-progress clean verify` | PASS; core IT 1, GUI IT 121, plugin 213, assembly successful |
| `git diff --check` | PASS after reconciliation edits |

The refreshed evaluation remains deterministic within the packaged repeated
run. Its tracked input/corpus hashes differ from the older evidence bundle
because the current Windows checkout byte representation differs; semantic
outcomes, manifest identity, configuration identity, and repeat stability are
unchanged. This is scoped reproducibility evidence, not a cross-platform byte
identity claim.

## Open Items And Verdict

No Task 01 blocker or open failure remains. The release-level screenshot,
diagram-performance, tag, and complete external backup gates remain visible
and are not misclassified as baseline failures or completed work.

FINAL GATE

- Mandatory checklist: PASS
- Focused tests: PASS
- Required reactor/root tests: PASS
- Architecture boundary: PASS
- Evidence completeness: PASS
- Documentation sync: PASS
- git diff --check: PASS
- Open failures: 0
- External blockers: 0
- Result: PASS
