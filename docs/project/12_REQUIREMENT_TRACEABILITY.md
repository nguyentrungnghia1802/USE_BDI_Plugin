# Requirement Traceability Matrix

Status: canonical implementation and test trace
Verification: source-backed; see Git history and DocumentationContractTest

This matrix maps requirement groups to executable source, tests, and detailed
documentation. It intentionally traces groups instead of every Java record.

## 1. Functional traceability

| Requirement IDs | Primary source | Primary automated evidence | Detailed docs |
| --- | --- | --- | --- |
| FR-PLG-001..005 | `useplugin.xml`, `BdiPlugin`, `HelloBdiAction`, `ImportBdiAction` | `HelloBdiActionTest`, `ImportBdiActionTest`, `PluginGuiSmoke` | `02`, install/user guides |
| FR-IMP-001..003 | `JasonAslParserAdapter`, `JasonAslImporter`, import result/diagnostics | `JasonAslParserAdapterTest`, `JasonAslImporterTest`, `AslImportReportTest` | `03`, `05`, ADR-0002..0008 |
| FR-IMP-004..006 | `JasonAstToIrNormalizer`, `model/ir`, `BdiIndexBuilder` | `AgentModelTest`, `IrHierarchyTest`, golden and index tests | `03`, technical design |
| FR-IMP-007..008 | `BdiImportService`, `BdiImportWorker`, `BdiSourceTracker`, `BdiExplorerView` | application/worker/tracker/explorer tests | `02`, `03`, developer guide |
| FR-MAP-001..002 | `UseUmlModelFacade`, `UseModelFingerprint` | `UseUmlModelFacadeTest` | `02`, `05`, ADR-0012 |
| FR-MAP-003..006 | mapping domain/services/repository/editor | mapping suggestion/model/persistence/staleness/editor tests | `03`, `04`, `05`, ADR-0013/0014 |
| FR-MAP-007 | `MappingSourceId`, `SourceSpan` | portability behavior documented in fixture/tests | `04`, `09`, limitations |
| FR-VAL-001..003 | `StandardConsistencyRules`, `ValidationOrchestrator`, issue model | orchestrator/rule/catalog tests | rule catalog, ADR-0014 |
| FR-VAL-004 | `RuleConfiguration`, repository/codec | rule configuration and orchestrator tests | `04`, `05`, ADR-0017 |
| FR-VAL-005 | `Suppression`, service/repository/codec | suppression repository/service/report tests | `04`, `05`, ADR-0018 |
| FR-VAL-006 | `BdiProjectConfigurationLoader`, `ImportBdiAction`, `BdiExplorerView` | loader and Explorer configuration tests | `01`, `02`, ADR-0020 |
| FR-VAL-007..008 | `BdiExplorerView`, problem collector/panel | explorer/problem tests | `03`, user guide |
| FR-OCL-001..006 | `UseSnapshotOclEvaluator`, snapshot result/status types | `UseSnapshotOclEvaluatorTest`, Auction OCL mutant test | `02`, `03`, `11`, ADR-0014 |
| FR-OCL-007 | no implementation claimed | Optional checklist status | `00`, `01`, future work |
| FR-UI-001..004 | `BdiExplorerView`, UI/problem/mapping models | UI and action tests plus GUI smoke | user guide, screenshot evidence |
| FR-REP-001..003 | report data/exporters and fingerprints | report/hash/suppression tests, Auction baseline | `04`, `05`, ADR-0015/0016/0018 |
| FR-REP-004 | Missing live GUI composition | Explicit Planned status and limitation contract | `01`, `05`, `09` |
| FR-REP-005 | `ReportMain` zero-state serializer demonstration | report/package smoke tests | `01`, `05`, developer guide |
| FR-CS-001..003 | Auction fixtures, baseline, mutant/evidence scripts | Auction case-study test suite | experiment protocol/evidence |
| FR-CS-004 | no required implementation | Optional checklist status | roadmap, future work |
| FR-REL-001..002 | Maven POMs, smoke/clean-clone scripts | root verify and script markers | `07`, `08`, release evidence |
| FR-REL-003..004 | tag/backup process | open checklist and backup manifest behavior | `08`, `09`, checklist |

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
| Research limitations | threats, limitations, and future-work documents |

## 4. Known untraced end-to-end gaps

- One-click export of the current live GUI validation state.
- Portable relative source identity and migration of existing mapping/
  suppression artifacts.
- Host model/snapshot change subscription for a long-lived BDI Explorer.
- House Building or a second independent evaluation corpus.
- Complete external data/report/slides backup and release tag.

These are intentionally Partial/Planned/Optional requirements, not missing
test labels for implemented behavior.

## 5. Traceability synchronization checklist

- [x] Every requirement group has source/test/doc evidence or an explicit gap.
- [x] Business rules identify enforcement boundaries.
- [x] Evaluation claims point to scoped artifacts.
- [x] Optional and release-blocking work are distinguished.
- [x] Traceability does not use local agent instructions as project truth.
