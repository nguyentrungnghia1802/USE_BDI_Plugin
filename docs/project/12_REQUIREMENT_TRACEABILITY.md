# Requirement Traceability Matrix

Status: canonical implementation and test trace
Verification: source-backed; see Git history and DocumentationContractTest

This matrix maps requirement groups to executable source, tests, and detailed
documentation. It intentionally traces groups instead of every Java record.

## 1. Functional traceability

| Requirement IDs | Primary source | Primary automated evidence | Detailed docs |
| --- | --- | --- | --- |
| FR-PLG-001..006 | `useplugin.xml`, `BdiPlugin`, `HelloBdiAction`, `ImportBdiAction`, `ImportJaCaMoAction`, `BdiFileChooserSupport` | `HelloBdiActionTest`, `BdiPluginTest`, `ImportBdiActionTest`, `PluginGuiSmoke` | architecture, technical design, install/user guides, demo guide |
| FR-IMP-001..003 | `JasonAslParserAdapter`, `JasonAslImporter`, import result/diagnostics | `JasonAslParserAdapterTest`, `JasonAslImporterTest`, `AslImportReportTest` | technical design, ADR-0002..0008 |
| FR-IMP-004..006 | `JasonAstToIrNormalizer`, `model/ir`, `BdiIndexBuilder` | `AgentModelTest`, `IrHierarchyTest`, golden and index tests | architecture, technical design |
| FR-IMP-007..008 | `BdiImportService`, `BdiImportWorker`, `BdiSourceTracker`, `BdiExplorerView` | application/worker/tracker/explorer tests | architecture, developer guide |
| FR-IMP-009..011 | `JaCaMoProjectParserAdapter`, `MasProjectImportService`, `model/mas` | parser spike, golden IR, partial-success, diagnostic, and relocation tests | ADR-0026, architecture, parser-spike evidence |
| FR-IMP-012 | `MasProjectAnalysisRequest`, `MasProjectAnalysisService`, `MasProjectAnalysisResult` | Auction composition, partial-success, relocation, and immutable snapshot tests | ADR-0029, project-analysis evidence |
| FR-IMP-013 | `ImportJaCaMoAction`, `BdiProjectImportWorker`, `HeadlessAnalysisService` | Explorer project import, worker, CLI success/error/conflict tests | ADR-0030, project-entry-point evidence |
| FR-MAP-001..002 | `UseUmlModelFacade`, `UseModelFingerprint` | `UseUmlModelFacadeTest` | architecture, technical design, ADR-0012 |
| FR-MAP-003..006 | mapping domain/services/repository/editor | mapping suggestion/model/persistence/staleness/editor tests | technical design, ADR-0013/0014 |
| FR-MAP-007 | `ProjectSourceId`, mapping/suppression repositories, `SourceSpan` | identity, v1 migration, byte-stability, invalid-root, and relocation tests | ADR-0021/0022, limitations |
| FR-VAL-001..003 | `StandardConsistencyRules`, `ValidationOrchestrator`, issue model | orchestrator/rule/catalog tests | rule catalog, ADR-0014 |
| FR-VAL-004 | `RuleConfiguration`, repository/codec | rule configuration and orchestrator tests | technical design, ADR-0017 |
| FR-VAL-005 | `Suppression`, service/repository/codec | suppression repository/service/report tests | technical design, ADR-0018 |
| FR-VAL-006 | `BdiProjectConfigurationLoader`, `ImportBdiAction`, `BdiExplorerView` | loader and Explorer configuration tests | requirements, architecture, ADR-0020 |
| FR-VAL-007..008 | `BdiExplorerView`, problem collector/panel | explorer/problem tests | architecture, user guide |
| FR-OCL-001..006 | `UseSnapshotOclEvaluator`, snapshot result/status types | `UseSnapshotOclEvaluatorTest`, Auction OCL mutant test | architecture, technical design, ADR-0014 |
| FR-OCL-007 | no implementation claimed | Optional checklist status | context, requirements, future work |
| FR-UI-001..005 | `BdiExplorerView`, `LiveUseSnapshotProvider`, `ImportJaCaMoAction`, UI/problem/mapping models | UI/action/provider/project-worker tests, stale-refresh and GUI smoke | user guide, ADR-0023/0030, screenshot evidence |
| FR-REP-001..003 | report data/exporters and fingerprints | report/hash/suppression tests, Auction baseline | technical design, ADR-0015/0016/0018 |
| FR-REP-004 | Explorer export action and `CurrentAnalysisReportService` | GUI/direct parity, atomic failure, UTF-8 and HTML escaping tests | user guide, ADR-0015/0024 |
| FR-REP-005 | `ReportMain` zero-state serializer demonstration | report/package smoke tests | requirements, technical/developer guides |
| FR-REP-006 | `CurrentAnalysisSnapshotService`, immutable aggregate | Auction, malformed, deterministic-time, state-safety, and Explorer parity tests | ADR-0024, architecture, technical design |
| FR-REP-007 | `BdiQualityGateMain`, `HeadlessAnalysisService`, packaged smoke | Auction, `.jcm`, invalid/conflicting input, review-only, deterministic and process-exit tests | ADR-0025/0030, developer guide |
| FR-TRC-001..003 | `trace` graph values, builder, query, and serializer | `AuctionTraceabilityGraphTest` complete-chain, gap, deduplication, certainty, and portability tests | ADR-0027, architecture, technical design |
| FR-ENV-001..004 | `CArtAgOArtifactAdapter`, `model.environment`, environment validator/trace contributor | adapter, Auction baseline, three mutants, boundary, catalog, and package tests | ADR-0028, CArtAgO spike evidence |
| FR-ENV-005..006 | `EnvironmentMappingDocument`, typed persisted records, strict codec/repository, staleness detector, validation service | round-trip/relocation, invalid-record, candidate/unknown, BDI regression, persisted Auction, stale-target, and catalog tests | ADR-0031, environment mapping persistence evidence |
| FR-ORG-001..004 | `MoiseOrganizationParserAdapter`, `OrganizationModel`, `MasProjectImportService` | Auction normalization, missing/invalid/duplicate/unsupported, relocation, boundary, and package tests | ADR-0034, Moise organization evidence |
| FR-ORG-005 | organization mapping records, `OrganizationConsistencyValidator`, `OrganizationRuleCatalog`, organization trace contributor | Auction baseline, role/mission/cardinality mutants, candidate/UNKNOWN, dedup/serialization, boundary tests | ADR-0035, organization consistency evidence, rule catalog |
| FR-DIA-001 | `diagram` immutable values and constructor invariants | `DiagramModelTest`, `DiagramPackageBoundaryTest` ordering, duplicate, endpoint, immutability, relocation, and dependency-boundary tests | ADR-0036, architecture, technical design |
| FR-DIA-002 | `BdiDiagramBuilder` snapshot/index projection | Minimal, Smart Queue, and Auction structure, ordering, support, determinism, candidate, and state-safety tests | architecture, technical design, checklist |
| FR-DIA-003 | `BdiDiagramBuilder` plus `TraceabilityDiagramContributor` project confirmed UML/OCL targets and explicit gaps | `BdiDiagramBuilderTest`, `TraceabilityDiagramContributorTest` complete/gap/OCL-chain cases | ADR-0036/0037, architecture, technical design, checklist |
| FR-DIA-004 | `TraceNode` rule/severity fields and `TraceabilityDiagramContributor` issue markers preserve status, certainty, and evidence | `TraceabilityDiagramContributorTest` metadata and certainty cases; Auction trace tests | ADR-0037, architecture, technical design |
| FR-DIA-005..008 | no implementation claimed | Planned requirements and open diagram task sequence | architecture, product requirements, checklist |
| FR-CS-001..003 | Auction fixtures, baseline, mutant/evidence scripts | Auction case-study test suite | experiment protocol/evidence |
| FR-CS-005..008 | `EvaluationManifestCodec`, `EvaluationRunner`, `EvaluationReportWriter`, `HeadlessStateFixture`, packaged evaluation script | manifest codec/runner tests, real Auction manifest integration, deterministic repeated outputs, timeout/tool-error tests, packaged smoke | ADR-0033, Auction evaluation manifest and result evidence |
| FR-CS-004 | no required implementation | Optional checklist status | checklist, future work |
| FR-REL-001..002 | Maven POMs, smoke/clean-clone scripts | root verify and script markers | developer/install guides, release evidence |
| FR-REL-003..004 | tag/backup process | open checklist and backup manifest behavior | decision log and checklist |

## 2. Business-rule traceability

| Business rule | Enforcement |
| --- | --- |
| BR-001/002 | Package dependency rules, normalizer branches, unsupported fixture/golden tests |
| BR-003 | Mapping suggestion type and explicit editor apply/upsert flow |
| BR-004 | `ValidationContext` and rule package imports |
| BR-005 | OCL/status/certainty records and conservative rule branches |
| BR-006 | Read-only facade, variation cleanup, snapshot fingerprint tests |
| BR-007 | `Suppression` constructor and repository/service tests |
| BR-008 | `ValidationOrchestrator` configuration validation tests |
| BR-009 | JSON/HTML exporter tests including escaping/evidence |
| BR-010 | Ground-truth manifest, metrics test, threats/limitations |
| BR-011 | ADR policy and focused `IntegrationModeIT`/`ShellIT` evidence |
| BR-012 | Release checklist, package evidence, backup script/manifest |
| BR-013 | JaCaMo adapter boundary scan, runtime exclusions, parser/import tests |
| BR-018 | Moise adapter boundary scan, immutable organization IR, static-only package evidence |
| BR-019 | Organization confirmation state, reviewed cardinality evidence, `ORG-003` UNKNOWN branches, and Auction pilot tests |
| BR-020 | Immutable diagram constructors, portable semantic references, package boundary scan, and no diagram persistence/editor path |
| BR-014 | Immutable graph constructors, snapshot-only builder, and graph portability tests |
| BR-015 | Environment boundary test, UNKNOWN dynamic-state test, and package dependency smoke |
| BR-016 | Environment document confirmation filter, `ENV-004`, candidate/stale/unknown tests, and deterministic persistence evidence |

## 3. Evidence traceability

| Claim | Evidence artifact |
| --- | --- |
| Runtime pipeline and model boundaries | Mermaid architecture/IR/metamodel diagrams |
| Rule catalog completeness | `08_CONSISTENCY_RULE_CATALOG.md` plus `RuleCatalogCompletenessTest` |
| Auction mapping and baseline | mapping examples and generated baseline JSON/HTML |
| Mutant detection | ground-truth JSON, metrics CSV, fault-injection tests |
| Scoped precision/recall/F1 | classification metrics document and metrics test |
| Import/index timing | performance baseline and benchmark JSON |
| UI availability | screenshot index and `PluginGuiSmoke` |
| Package reproducibility | release-package evidence and clean-clone marker |
| Static JaCaMo import | parser-spike evidence and Auction golden MAS project IR |
| Moise organization boundary | official Moise 1.1 source/API/checksum/license evidence, Auction golden normalization, explicit diagnostics, and adapter/package boundary tests |
| Static organization consistency | Auction role/class, mission/operation, cardinality/OCL baseline and mutant tests, portable trace serialization, ADR-0035 |
| Unified issue trace | Auction traceability graph test and ADR-0027 |
| Renderer-neutral BDI diagram | Diagram invariant/relocation/package tests, minimal/Smart Queue/Auction projection tests, and ADR-0036 |
| Static environment consistency | CArtAgO adapter/mutant tests and ADR-0028 |
| Persisted environment mappings | strict environment codec/repository, relocation and stale-target tests, ADR-0031, and persistence evidence |
| Scoped Auction evaluation | versioned manifest, four portable mapping fixtures, runner tests, deterministic JSON/CSV/HTML, and packaged `AUCTION_EVALUATION_OK` smoke |
| Research limitations | threats, limitations, and future-work documents |

## 4. Known untraced end-to-end gaps

- One-click export of the current live GUI validation state.
- Host model/snapshot change subscription for a long-lived BDI Explorer.
- House Building or a second independent evaluation corpus.
- Live CArtAgO, Moise enactment/monitoring, persisted organization mappings,
  and runtime-trace integration.
- Trace/OCL evidence projection, graph layout/rendering, diagram interaction,
  export, performance evidence, and presentation fixtures.
- Complete external data/report/slides backup and release tag.

These are intentionally Partial/Planned/Optional requirements, not missing
test labels for implemented behavior.

## 5. Traceability synchronization checklist

- [x] Every requirement group has source/test/doc evidence or an explicit gap.
- [x] Business rules identify enforcement boundaries.
- [x] Evaluation claims point to scoped artifacts.
- [x] Optional and release-blocking work are distinguished.
- [x] Traceability does not use local agent instructions as project truth.
