# 16. Project Completion Checklist

> Đây là nguồn sự thật cho tiến độ. Sau mỗi phiên làm việc, cập nhật checkbox, ghi commit và bằng chứng test.

## 0. Repository baseline

- [x] Xác nhận `java -version` là Java 21.
- [x] Chạy `mvn clean test` tại root.
- [x] Chạy USE GUI từ clean build.
- [x] Ghi USE commit hash và version.
- [x] Tạo branch `thesis/bdi-plugin`.
- [x] Di chuyển prototype files khỏi root vào case study/fixtures.
- [x] Tạo module `use-bdi-plugin`.

## 1. Plugin spike

- [x] Xác định plugin descriptor/manifest.
- [x] Xác định lifecycle interface.
- [x] Xác định cách thêm menu.
- [x] Xác định cách thêm custom view/panel.
- [x] Xác định cách lấy current model/session/state.
- [x] Xác định classloader/dependency packaging.
- [x] Hello plugin chạy trong distribution.
- [x] Ghi kết quả vào `DECISION_LOG.md`.

## 2. Jason importer

- [x] Thêm dependency Jason pin version.
- [x] Parse `.asl` hợp lệ.
- [x] Bắt syntax error.
- [x] Multi-file import.
- [x] Partial success policy.
- [x] Source location extraction.
- [x] Parser version in report.

## 3. Intermediate representation

- [x] `AgentModel`.
- [x] `BeliefModel`.
- [x] `GoalModel`.
- [x] `PlanModel`.
- [x] `TriggerModel`.
- [x] `ContextExpr` tree.
- [x] `PlanStepModel` hierarchy.
- [x] `TermModel` hierarchy.
- [x] `SourceSpan`.
- [x] `UnsupportedFeature`.
- [x] Golden serialization tests.

## 4. BDI index and metamodel

- [x] Goal -> supporting plans index.
- [x] Action -> call sites index.
- [x] Predicate references index.
- [x] Agent/object references index.
- [x] Duplicate label detection.
- [x] BDI metamodel version recorded.

## 5. UI shell

- [x] AgentSpeak menu.
- [x] File chooser import.
- [x] Background import task.
- [x] BDI tree.
- [x] Node detail/source panel.
- [x] Problems table.
- [x] Filtering/grouping.
- [x] Re-import changed files.

## 6. USE adapter

- [x] Enumerate UML classes.
- [x] Enumerate objects in current state.
- [x] Enumerate attributes/associations.
- [x] Enumerate operations and parameters.
- [x] Expose pre/postconditions.
- [x] OCL expression evaluation wrapper.
- [x] Stable UML element references/fingerprint.

## 7. Mapping

- [x] Mapping entity/schema.
- [x] Agent -> Class.
- [x] Agent -> Object.
- [x] Action -> Operation.
- [x] Parameter bindings.
- [x] Receiver binding.
- [x] Belief mapping model.
- [x] Suggestion scoring.
- [x] Manual editor.
- [x] Save/load.
- [x] Stale mapping detection.

## 8. Consistency engine

- [x] Rule SPI/interface.
- [x] Phase orchestration.
- [x] Issue/evidence model.
- [x] ASL-001/002.
- [x] BDI-001/002/003/004.
- [x] REF-001/002.
- [x] MAP-001/002/003.
- [x] SIG-001/002/003.
- [x] OWN-001.
- [x] BEL-001.
- [x] MSG-001.
- [x] OCL-001/002/003/004.
- [x] CTX-001.
- [x] Suppression.

## 9. OCL integration

- [x] Bind receiver.
- [x] Bind operation arguments.
- [x] Evaluate precondition on snapshot.
- [x] PASS/FAIL/UNKNOWN result.
- [x] Isolated/safe state mutation strategy.
- [x] One bounded effect adapter.
- [x] Re-check invariant.
- [x] Restore/avoid corrupting user state.

## 10. Reporting

- [x] JSON report. (generated to `docs/bdi-report.json` by `ReportMain`)
- [x] HTML or CSV report.
- [x] Plugin/USE/Jason versions. (included in report metadata)
- [x] Model and mapping hashes. (SHA-256 identities are carried by report metadata)
- [x] Rule configuration. (versioned `rules.json` filters enabled rule IDs)
- [x] Issue evidence/source. (HTML and JSON exports include rule, status, certainty, source, message, and evidence)
- [x] Suppressions included. (persisted, applied, and exported with reasons)

## 11. Testing

- [x] Valid ASL fixtures.
- [x] Invalid ASL fixtures.
- [x] Unsupported fixtures. (relational-context fixture is retained as ASL-002 evidence)
- [x] Golden IR tests. (minimal and unsupported-context IR JSON fixtures)
- [x] Rule tests.
- [x] Mapping persistence tests.
- [x] USE model integration tests.
- [x] OCL tests.
- [x] UI smoke test.
- [x] Performance benchmark. (import -> IR -> BDI index baseline)
- [x] Clean-clone reproducibility test. (exact HEAD -> USE distribution package)

## 12. Case study

- [x] Auction UML/OCL model. (first compilable Auction fixture)
- [x] Auction AgentSpeak files. (auctioneer and bidder fixtures)
- [x] Valid mapping. (confirmed Auction class/object/action/parameter/belief links)
- [x] Baseline report. (deterministic JSON/HTML Auction report)
- [x] Structural mutants. (remove-Bidder Auction fixture detects 9 stale mapping targets)
- [x] Signature mutants. (open/0 versus mutated open(flag:String) detects SIG-001)
- [x] Reference mutants. (bidder2 reference detects 4 REF-001 findings)
- [x] OCL mutants. (draft snapshot versus #closed precondition detects OCL-001)
- [x] Ground truth manifest. (four Auction mutant outcomes)
- [x] Metrics table. (targeted baseline/mutant deltas in CSV)
- [x] Demo script. (reproducible Auction evidence command)
- [ ] House Building exploratory import (optional).

## 13. Thesis evidence

- [x] Architecture diagram. (Mermaid runtime pipeline)
- [x] IR class diagram. (normalized Java-owned IR hierarchy)
- [x] BDI metamodel diagram. (agent/plan/index domain view)
- [x] Mapping examples. (Auction confirmed and mutant links)
- [x] Rule catalog final. (22-rule implementation matrix and catalog test)
- [x] UI screenshots. (tracked USE/BDI Explorer/Problems evidence and smoke path)
- [x] Experiment protocol. (reproducible Auction baseline/mutant procedure)
- [x] Precision/recall/F1. (scoped four-mutant detection metrics)
- [x] Performance chart/table. (Smart Queue import/index benchmark evidence)
- [x] Threats to validity. (construct, internal, external, and conclusion risks)
- [x] Limitations. (known subset, mapping, OCL, metrics, and build boundaries)
- [x] Future work. (reference precision, corpus, integration, evaluation, release)

## 14. Release

- [x] `mvn clean verify` pass. (2026-08-09: use-core, use-gui 121 ShellIT/integration tests, plugin 74 tests, and assembly all passed)
- [x] Plugin install guide.
- [x] User guide. (`USER_GUIDE.md` covers build, GUI clicks, Auction demo, and troubleshooting)
- [x] Developer guide. (`DEVELOPER_GUIDE.md` records module/API boundaries, tests, and extension rules)
- [x] Third-party notices/licenses. (`THIRD_PARTY_NOTICES.md` matches the checked POM/runtime tree and embedded notice)
- [x] Release package tested on clean machine/profile. (clean-clone package from committed `594b2b07` passed with `CLEAN_CLONE_REPRODUCIBILITY_OK`)
- [ ] Git tag `v1.0.0-thesis-rc`.
- [ ] Backup source, data, report and slides.

## Thesis evidence and install-guide bundle - 2026-08-09

- `08_CONSISTENCY_RULE_CATALOG.md` now contains the implementation matrix for
  all 22 registered `StandardConsistencyRules`, including phase, evaluator,
  default severity/certainty, and test/evidence trace. `RuleCatalogCompletenessTest`
  checks that the source registry and documented IDs remain aligned.
- `evidence/ui-screenshots.md` indexes the tracked USE class diagram, import
  entry point, BDI Explorer, detail panel, and Problems screenshots. It also
  records the exact `Plugins > AgentSpeak` click path and points to the
  `PluginGuiSmoke`/`smoke.ps1` verification.
- `evidence/auction-experiment-protocol.md` defines the four-mutant Auction
  procedure, oracle, commands, expected markers, and interpretation boundary.
- `evidence/auction-classification-metrics.md` reports mutation-instance
  precision `1.000`, recall `1.000`, and F1 `1.000` for TP=4, FP=0, FN=0. This
  is explicitly scoped to the four labeled mutants and does not estimate TN
  or rule-level generalization. `AuctionMetricsEvidenceTest` recomputes it
  from the tracked CSV.
- `evidence/performance-baseline.md` records the latest seven-iteration
  Smart Queue import/index sample: minimum `3.1334 ms`, median `6.1485 ms`,
  p95 `7.7725 ms`, with a Mermaid chart and reproduction command.
- `evidence/threats-to-validity.md`, `limitations.md`, and `future-work.md`
  capture the evaluation boundary and next research steps. `PLUGIN_INSTALL_GUIDE.md`
  documents assembly, runtime launch, plugin placement, UI verification, and
  clean-clone validation.
- `mvn --batch-mode --no-transfer-progress -pl use-bdi-plugin "-Dtest=RuleCatalogCompletenessTest,AuctionMetricsEvidenceTest,ThesisEvidenceArtifactTest,AuctionEvidenceArtifactTest" test` passed with 4 tests.
- `mvn --batch-mode --no-transfer-progress -pl use-bdi-plugin -am test` passed with 73 tests, including the full Auction, UI, OCL, catalog, and metrics suite.
- `powershell -ExecutionPolicy Bypass -File .\use-bdi-plugin\scripts\performance.ps1` passed with marker `PERFORMANCE_BENCHMARK_OK`.
- `powershell -ExecutionPolicy Bypass -File .\use-bdi-plugin\scripts\smoke.ps1` passed the assembled package, shaded Jason parser/report checks, and direct extracted-distribution GUI probe with `GUI_SMOKE_OK`.
- The earlier root verify failure was caused by invalid-specification fixtures
  terminating `Main.main` during `-it`; ADR-0019 records the focused fix and
  regression test. The current release status is recorded below.

## Reporting evidence - 2026-08-09

- `ReportData` now carries immutable `ConsistencyIssue` records while retaining
  the existing summary constructor used by `ReportMain`.
- `HtmlReportExporter` writes a metadata summary and an escaped consistency
  issue table with rule ID, severity, status, certainty, source location,
  message, and evidence. `ReportExporter` includes the same issue fields in
  the JSON `issues` array.
- `HtmlReportExporterTest` verifies source/evidence rendering and HTML escaping
  for an injection-shaped message; `ReportExporterTest` verifies JSON issue
  serialization and escaped quotes.
- `mvn -pl use-bdi-plugin test` passed with 47 tests. No USE core source was
  changed and the user's pre-existing `docs/agent/PROMPT_START_PROJECT.md`
  change was not staged.

## Reporting identity evidence - 2026-08-09

- `UseModelFingerprint` supplies the existing SHA-256 identity for the
  immutable `UseModelSnapshot`; `MappingFingerprint` adds a deterministic
  SHA-256 identity for `MappingDocument` using schema/metamodel/USE metadata
  and bindings sorted by `MappingBinding.key()`.
- Mapping canonicalization includes binding targets, optional expressions, and
  evidence with explicit markers/counts. `ReportData` validates supplied
  hashes as 64-character hexadecimal SHA-256 values while preserving the
  summary constructor used by `ReportMain`.
- JSON exports write `modelHash` and `mappingHash`; HTML exports show them as
  `Model Hash` and `Mapping Hash`. Exporters serialize supplied values only and
  do not inspect or mutate USE state.
- `MappingFingerprintTest`, `ReportExporterTest`, and
  `HtmlReportExporterTest` cover stable ordering, content changes, and both
  report formats. `mvn -pl use-bdi-plugin clean test` passed with 49 tests.
- The demo `ReportMain` still emits null hashes until a live model/mapping
  analysis pipeline supplies the computed values; that integration is not
  claimed by this checklist item.

## Rule configuration evidence - 2026-08-09

- `use-bdi-plugin/.bdi-plugin/rules.json` is a tracked schema `0.1.0` example
  enabling all 22 standard rule IDs, so the default behavior remains unchanged.
- `RuleConfiguration` stores an immutable sorted enabled-rule set.
  `RuleConfigurationRepository` persists deterministic UTF-8 JSON and rejects
  unknown fields, duplicate IDs, malformed values, and unsupported schema
  versions.
- `ValidationOrchestrator` accepts the configuration, rejects IDs absent from
  the supplied rule set, and evaluates only enabled rules. Its no-argument
  constructor still enables every standard rule.
- `RuleConfigurationRepositoryTest` covers round-trip ordering and malformed
  configuration; `ValidationOrchestratorTest` covers filtering and unknown-ID
  rejection. `mvn -pl use-bdi-plugin test` passed with 53 tests.
- Automatic discovery of `rules.json` from a project context is not claimed;
  the repository still lacks the authoritative `00_PROJECT_CONTEXT.md` and
  the application can inject a loaded configuration explicitly.

## Suppression evidence - 2026-08-09

- `use-bdi-plugin/.bdi-plugin/suppressions.json` now uses schema `0.1.0` with
  an explicit empty `suppressions` array; the old placeholder entry is not
  treated as a real suppression.
- `IssueFingerprint` hashes normalized source path and source-span positions.
  `SuppressionService` matches `ruleId + sourceFingerprint`, changes only
  matching `OPEN` issues to `SUPPRESSED`, and appends the configured reason to
  issue evidence.
- `SuppressionRepository` provides deterministic UTF-8 persistence with
  duplicate/unknown-field/schema validation. `ReportExporter` and
  `HtmlReportExporter` include rule, source fingerprint, and reason; `ReportMain`
  loads the project file when generating reports.
- `SuppressionRepositoryTest`, `SuppressionServiceTest`,
  `ValidationOrchestratorTest`, `ReportExporterTest`, and
  `HtmlReportExporterTest` cover persistence, matching, status/evidence, and
  JSON/HTML output. `mvn -pl use-bdi-plugin test` passed with 58 tests.
- Source fingerprints currently include normalized absolute paths because the
  domain does not yet receive a verified project-root context; moving a
  checkout can therefore require regenerating suppression entries.

## Unsupported fixture evidence - 2026-08-09

- `fixtures/asl/unsupported/relational-context.asl` is valid Jason 3.3.0
  input with `Counter > 0` in a plan context.
- The normalizer checks Jason `RelExpr` before its `Literal` superclass path,
  retaining a `ContextUnsupported` node and `ASL-002` source evidence instead
  of silently treating the relation as a literal.
- `UnsupportedFixtureTest` verifies successful import, line-4 evidence,
  `BdiProblemCollector` WARNING projection, and no `ASL-001` syntax problem.
- `mvn -pl use-bdi-plugin -Dtest=UnsupportedFixtureTest test` passed. No USE
  core source was changed.

## Golden IR evidence - 2026-08-09

- `AgentModelJsonSerializerTest` now compares both the existing minimal IR
  golden and `fixtures/expected/unsupported-relational-context-agent-model.json`.
- The unsupported golden preserves portable source paths, the
  `ContextUnsupported` node, `ASL-002` feature kind/subject, source spans, and
  deterministic ordering. The test serializes each model twice.
- `mvn -pl use-bdi-plugin -Dtest=AgentModelJsonSerializerTest test` passed with
  2 tests. This does not claim a broader golden corpus for Auction fixtures.

## Performance benchmark evidence - 2026-08-09

- `BdiPerformanceBenchmarkTest` measures the actual `BdiImportService` pipeline
  on `fixtures/smartqueue/Smart_manager_agent.asl`: Jason parse, materialized
  normalized IR, and `BdiIndexBuilder` indexing are included in each sample.
- The harness warms up twice, measures seven iterations with `System.nanoTime`,
  verifies 9 beliefs, 1 goal, 5 plans, materialization, action/predicate and
  agent/object indexes on every iteration, and writes
  `use-bdi-plugin/target/performance/bdi-import-index.json`.
- The recorded run on 2026-08-09 used Jason `3.3.0` and BDI metamodel `0.1.0`:
  minimum `3.5921 ms`, median `4.8261 ms`, p95 `10.1224 ms`.
- Reproduce with
  `powershell -ExecutionPolicy Bypass -File .\use-bdi-plugin\scripts\performance.ps1`.
  These numbers are an environment comparison baseline, not a hard timing
  gate, and do not claim Auction-scale performance data.

## Clean-clone reproducibility evidence - 2026-08-09

- `use-bdi-plugin/scripts/clean-clone.ps1` clones the exact current `HEAD`
  into a generated temp directory and checks the clone is clean before and
  after building. It does not copy unstaged working-tree files.
- The clone runs `mvn --batch-mode --no-transfer-progress -pl use-assembly -am
  package`, then verifies `use-bdi-plugin-7.1.1.jar` contains the BDI plugin,
  Jason `Agent` class, and third-party notices. It also verifies the same
  plugin JAR is present at `use-7.1.1/lib/plugins/` in the assembled ZIP.
- The script passed with marker
  `CLEAN_CLONE_REPRODUCIBILITY_OK`. Temporary clone cleanup is path-checked;
  `-KeepClone` is available for diagnostics. This evidence does not claim the
  separate root `mvn clean verify`/Failsafe baseline limitation is resolved.

## Auction UML/OCL evidence - 2026-08-09

- `use-bdi-plugin/src/test/resources/fixtures/casestudy/auction/Auction.use`
  is a compilable first Auction case-study model. It contains 4 classes,
  4 associations, 7 attributes, 4 operations, and 5 class invariants.
- `AuctionModelFixtureTest` compiles the real fixture with `USECompiler`,
  verifies 7 operation preconditions and 2 postconditions through
  `UseUmlModelFacade`, then creates 3 objects and 2 links and verifies the
  immutable snapshot fingerprint changes.
- `mvn -pl use-bdi-plugin -Dtest=AuctionModelFixtureTest test` passed with
  1 test.

## Auction AgentSpeak evidence - 2026-08-09

- `use-bdi-plugin/src/test/resources/fixtures/casestudy/auction/auctioneer.asl`
  and `bidder.asl` are valid Jason 3.3.0 fixtures aligned with the Auction
  lifecycle vocabulary. They cover initial beliefs/goals, plan contexts,
  belief updates, achieve goals, external actions, and `.print`.
- `AuctionAgentSpeakFixtureTest` imports both files through
  `BdiImportService` with no diagnostics, verifies materialized model counts
  of 2 beliefs, 1 goal, and 3 plans for `auctioneer`, and 2 beliefs, 1 goal,
  and 1 plan for `bidder`.
- The test also verifies one indexed external call for each of `open/0`,
  `placeBid/2`, `close/0`, and `submitBid/2`, plus one indexed internal
  `.print/1` call, and verifies goal-to-supporting-plan lookups.
- `mvn -pl use-bdi-plugin -Dtest=AuctionAgentSpeakFixtureTest test` passed
  with 1 test. No mapping, mutant, ground-truth, or report claim is made by
  this fixture/import slice.

## Auction valid-mapping evidence - 2026-08-09

- `AuctionMappingFixtureTest` selects exact-name/arity suggestions from the
  imported Auction models and populated `Auction.use` snapshot for 14 confirmed
  bindings: two agent classes, two agent objects, four action operations, four
  positional parameters, and two supported belief attributes.
- The test persists and reloads the document through
  `MappingFileRepository`, verifies the current USE fingerprint and BDI
  metamodel metadata, and confirms no `MappingStalenessDetector` findings.
- The configured validation run produces no `MAP-001`, `MAP-002`, or `MAP-003`
  issue for the confirmed case-study mapping. Relational beliefs without a UML
  attribute target are intentionally not forced into `BELIEF_ATTRIBUTE` links.
- Auction plan labels are explicit because unlabeled plans with the same step
  position would otherwise share the current `MappingSourceId` key
  (`source + plan label + step`) and `MappingDocument.upsert` would replace a
  prior action binding.
- `mvn -pl use-bdi-plugin '-Dtest=AuctionAgentSpeakFixtureTest,AuctionMappingFixtureTest' test`
  passed with 2 tests. The runtime mapping file is temporary because source IDs
  currently contain normalized absolute paths; no checkout-specific JSON is
  claimed as a portable fixture.

## Auction baseline-report evidence - 2026-08-09

- `AuctionBaselineReportTest` runs the actual Auction import, populated USE
  snapshot, confirmed 14-binding mapping, and `ValidationOrchestrator`, then
  exports both JSON and HTML reports to
  `use-bdi-plugin/target/case-study/auction/`.
- The report metadata contains project `Auction-Case-Study`, plugin `0.1.0`,
  USE `USE-7.1.1`, Jason `3.3.0` in notes, 14 mappings, a 64-character model
  hash, a 64-character mapping hash, and fixed timestamp
  `2026-08-09T00:00:00Z`.
- The locked baseline has 27 findings with histogram `REF-001=10`,
  `BEL-001=2`, `OCL-002=4`, `OCL-004=4`, `OWN-001=3`, `SIG-002=2`, and
  `SIG-003=2`. This records the current pre-mutant behavior, including
  potential/unknown findings; it does not silently discard them.
- Repeated JSON and HTML exports are byte-identical within the same checkout.
  `powershell -ExecutionPolicy Bypass -File
  .\use-bdi-plugin\scripts\auction-baseline.ps1` passed with marker
  `AUCTION_BASELINE_REPORT_OK` and verified both output files.
- The generated report is intentionally under `target/`, not committed as a
  portable fixture, because source spans and mapping source IDs currently use
  normalized absolute paths.

## Auction structural-mutant evidence - 2026-08-09

- `use-bdi-plugin/src/test/resources/fixtures/casestudy/auction/mutants/structural-remove-bidder.use`
  is a separate valid USE model. It removes `Bidder`, the dependent
  `submitBid`/`placeBid` structure, related associations, and Bidder
  constraints while preserving the original Auction fixture.
- `AuctionStructuralMutantTest` keeps the original AgentSpeak import and
  confirmed 14-binding mapping, then loads the mutant with `USECompiler` and
  projects it through `UseUmlModelFacade`.
- `MappingStalenessDetector` reports nine `TARGET_MISSING` bindings grouped as
  `AGENT_CLASS=1`, `AGENT_OBJECT=1`, `ACTION_OPERATION=2`, `PARAMETER=4`, and
  `BELIEF_ATTRIBUTE=1`; the changed USE fingerprint is retained as review
  evidence. `ValidationOrchestrator` turns those nine target findings into
  nine confirmed `MAP-003` issues.
- `powershell -ExecutionPolicy Bypass -File
  .\use-bdi-plugin\scripts\auction-structural-mutant.ps1` passed with marker
  `AUCTION_STRUCTURAL_MUTANT_OK`. The unchanged AgentSpeak files produce no
  `ASL-001` issue. This entry records the structural member of the complete
  Auction fault-injection bundle documented below.

## Auction fault-injection and thesis-evidence bundle - 2026-08-09

- `AuctionFaultInjectionTest` passes three additional mutant scenarios:
  `SIG-001-open-arity` changes `Auction::open()` to
  `Auction::open(flag:String)` and produces one `SIG-001`; `REF-001-bidder2`
  changes the object reference to absent `bidder2` and produces four targeted
  `REF-001` findings; `OCL-001-open-precondition` changes the open precondition
  to `#closed` and produces one confirmed `OCL-001` for `auction1` in `draft`.
- `AuctionEvidenceArtifactTest` verifies
  `docs/project/evidence/auction-ground-truth.json`,
  `auction-metrics.csv`, the three Mermaid diagrams, and the mapping examples.
  The manifest also includes the previous structural mutant with nine
  `MAP-003` findings and the 27-issue/14-binding baseline metadata.
- `powershell -ExecutionPolicy Bypass -File
  .\use-bdi-plugin\scripts\auction-evidence.ps1` passed with marker
  `AUCTION_EVIDENCE_OK`; the nested baseline and structural scripts passed as
  `AUCTION_BASELINE_REPORT_OK` and `AUCTION_STRUCTURAL_MUTANT_OK`.
- The metrics table reports targeted mutant deltas only. It does not claim
  thesis precision/recall/F1, because the current conservative `REF-001`,
  signature, and OCL policies still require a broader labeled corpus.

## Phase 0 evidence - 2026-08-03

- Baseline: USE `7.1.1`, commit
  `a455e90e7e68c10c53c04b86647c1ce79ff4610c`, branch
  `thesis/bdi-plugin`, Java `21.0.5`, Maven `3.9.9`.
- Root gate: `mvn clean test` passed.
- Module/package gate: `mvn -pl use-bdi-plugin -am package` passed; one unit
  test passed.
- Distribution/UI gate: `use-bdi-plugin/scripts/smoke.ps1` passed and found
  `Plugins > AgentSpeak > Hello BDI Plugin` in a started USE GUI.
- Known baseline limitation: root `verify` is blocked by the existing
  `use-gui` `ShellIT` fork exiting without a Failsafe handshake. See
  `DECISION_LOG.md` for the command output interpretation and accepted Phase 0
  gates.
- Documentation gap: `00_PROJECT_CONTEXT.md` is absent and remains open.

## Phase 1 importer slice evidence - 2026-08-03

- Jason `3.3.0` is pinned in `use-bdi-plugin/pom.xml` and shaded into the plugin
  JAR with its runtime dependencies; USE dependencies remain `provided`.
- `JasonAslParserAdapter` initializes an offline Jason agent, disables the web
  mind inspector, parses one file, and returns a Jason-independent
  `AslParseSummary`.
- `minimal.asl` is a valid fixture with one initial belief, one initial goal,
  and one plan; `JasonAslParserAdapterTest` verifies all three counts and the
  Maven-filtered parser version.
- `mvn -pl use-bdi-plugin clean package` passed with three tests. The formal
  report, syntax diagnostics, source locations, multi-file import, and BDI IR
  remain deliberately open.
- `use-bdi-plugin/scripts/smoke.ps1` parsed the fixture through the shaded JAR
  from the assembled distribution and passed the existing USE GUI menu smoke.

## Phase 1 syntax diagnostic slice evidence - 2026-08-04

- Invalid fixture `missing-plan-body.asl` deterministically fails at the `;`
  token on line 3, column 8.
- `JasonAslParserAdapter` catches Jason `ParseException` and exposes a
  Jason-independent `AslDiagnostic` with rule ID `ASL-001`, `ERROR` severity,
  normalized source path, one-based position, and parser message.
- Missing-file errors remain distinct and do not carry a syntax diagnostic.
- `mvn -pl use-bdi-plugin test` passed with four tests.
- `use-bdi-plugin/scripts/smoke.ps1` rebuilt the distribution, reproduced
  `ASL-001` at line 3, column 8 through the shaded plugin JAR, and passed the
  existing USE GUI menu smoke.
- IR `SourceSpan`, unsupported syntax/`ASL-002`, multi-file partial success, and
  the consistency-rule SPI remain open tasks.

## Phase 1 multi-file importer slice evidence - 2026-08-04

- `AslImporter` accepts an ordered `List<Path>` and returns Java-only
  `AslImportResult`; no Jason AST type crosses the adapter boundary.
- `JasonAslImporter` parses each source independently, preserves input order,
  and returns immutable per-file summaries with aggregate belief, goal, and
  plan counts.
- `review-agent.asl` provides a second valid fixture. Together with
  `minimal.asl`, the importer reports two files, three beliefs, two goals, and
  two plans.
- The initial multi-file slice used fail-fast behavior as an interim boundary;
  ADR-0006 supersedes that behavior with a partial result containing successful
  summaries and per-file diagnostics.
- `mvn -pl use-bdi-plugin test` passed with seven tests.
- `use-bdi-plugin/scripts/smoke.ps1` rebuilt the distribution, imported both
  valid fixtures through the shaded JAR with the expected aggregate counts,
  preserved the `ASL-001` regression check, and passed the USE GUI menu smoke.

## Phase 0 repository fixture boundary evidence - 2026-08-04

- Repository inventory found one actual root prototype, `Smart_manager_agent.asl`;
  it is now tracked at
  `use-bdi-plugin/src/test/resources/fixtures/smartqueue/Smart_manager_agent.asl`.
- The migrated prototype parses through Jason 3.3.0 with nine beliefs, one
  initial goal, and five plans. `JasonAslParserAdapterTest` protects this
  fixture path and parse contract.
- `use-bdi-plugin/scripts/smoke.ps1` fails if the prototype returns to root or
  the migrated fixture disappears. The SmartQueue `.use/.cmd/.clt` names
  referenced by older technical notes are absent from this checkout and were
  not invented or marked as migrated.
- `mvn -pl use-bdi-plugin test` passed with eight tests.
- `use-bdi-plugin/scripts/smoke.ps1` confirmed root absence and fixture presence,
  then passed the package, multi-file, diagnostic, third-party-notice, and USE
  GUI menu gates.

## Phase 1 partial-success importer slice evidence - 2026-08-04

- `AslImportResult` now carries immutable ordered successful summaries and
  immutable per-file diagnostics; aggregate counts still derive only from
  successful summaries.
- `JasonAslImporter` attempts every source in the input order. A valid-invalid-
  valid sequence keeps both valid summaries and reports the invalid file's
  `ASL-001` diagnostic without dropping later input.
- Missing or otherwise non-parser import failures are reported as
  `ASL-IMPORT-001` with normalized source and unknown position `0/0`.
- `mvn -pl use-bdi-plugin test` covers empty input, all-valid multi-file input,
  syntax partial success, missing-file partial success, parser diagnostics, and
  the migrated Smart Queue fixture.
- `use-bdi-plugin/scripts/smoke.ps1` validates the same partial result through
  the shaded plugin JAR in the assembled distribution and passes the USE GUI
  menu smoke.

## Phase 1 source-location extraction slice evidence - 2026-08-04

- Jason 3.3.0 `Term.getSrcInfo()` and `SourceInfo` provide the source file plus
  begin/end lines for parsed terms; declaration columns are not available from
  this metadata and are not synthesized.
- `JasonAslParserAdapter` extracts immutable Java-only locations for initial
  beliefs, initial goals, and plans, normalizes the source path, and orders the
  locations by source line. Missing metadata is retained as an explicit `0/0`
  location instead of being silently dropped.
- `minimal.asl` is protected by assertions for `ready` at line `1`, `start` at
  line `2`, and `+!start` spanning lines `4-5`; the location list is immutable.
- `mvn -pl use-bdi-plugin test` passed with nine tests, and the packaged smoke
  verifies the same line locations through the shaded plugin JAR.
- The full normalized IR `SourceSpan` model remains a separate open task.

## Phase 1 parser-version report slice evidence - 2026-08-04

- `AslImportReport` wraps the immutable `AslImportResult` instead of copying its
  summaries or diagnostics, and exposes distinct parser versions in encounter
  order from successful file summaries.
- An empty or all-failed import reports no parser version because no parser
  successfully produced a summary; failed files are not assigned a guessed
  version.
- `AslImportReportTest` verifies repeated and distinct versions, encounter
  ordering, empty input, result identity, and immutable metadata.
- `mvn -pl use-bdi-plugin test` passed with eleven tests. Packaged smoke reports
  `3.3.0` through the shaded Jason plugin JAR.
- This slice does not claim JSON/HTML export, plugin/USE version metadata, model
  hashes, or report persistence; those remain in the reporting checklist.

## Phase 1 AgentModel root IR slice evidence - 2026-08-04

- `AgentModel` is an immutable Java-only root IR value containing the normalized
  source path, parser version, and belief, goal, and plan counts. It does not
  invent an AgentSpeak agent name when the source has no such identity field.
- `AslAgentModelNormalizer` maps one `AslParseSummary` or the ordered successful
  summaries in `AslImportResult` into root models; failed files remain
  diagnostics and do not produce an `AgentModel`.
- `mvn -pl use-bdi-plugin test` passed with thirteen tests, including the two
  root-model normalization and immutable-order assertions.
- `use-bdi-plugin/scripts/smoke.ps1` passed `AGENT_MODEL_SMOKE_OK` from the
  shaded plugin JAR in the assembled USE distribution, alongside the existing
  importer, diagnostic, and GUI menu gates.
- This entry records the earlier metadata-only root boundary; the following IR
  tree slice supersedes its open-item status with implementation evidence.

## Phase 1 normalized IR tree slice evidence - 2026-08-04

- The `model/ir` package now contains immutable `BeliefModel`, `GoalModel`,
  `PlanModel`, `TriggerModel`, `ContextExpr`, `PlanStepModel`, and `TermModel`
  hierarchies. Lists and optionals are defensively copied.
- `SourceSpan` preserves normalized source path and begin/end lines. Jason does
  not expose declaration columns through `SourceInfo`, so columns remain an
  explicit `0` rather than invented data.
- `JasonAslParserAdapter.parseModel` uses package-private
  `JasonAstToIrNormalizer` to materialize the tree. Domain IR classes do not
  import Jason classes. Generated Jason labels such as `p__N[...]` are omitted
  because their counter is process-dependent and they are not source labels.
- `UnsupportedFeature` records `ASL-002`, kind, subject, and source span in the
  root model instead of dropping an unknown term or plan step. The ASL-002
  rule/Problems integration and unsupported fixture remain separate open
  testing/analysis tasks.
- `AgentModelJsonSerializer` emits deterministic JSON and relativizes source
  paths when a source root is supplied. `minimal-agent-model.json` is a golden
  fixture covering belief, goal, trigger, context, internal action, strings,
  and source spans.
- `mvn -pl use-bdi-plugin test` passed with eighteen tests; the Smart Queue
  fixture materialized 9 beliefs, 1 goal, 5 plans, and no unsupported nodes.
- `mvn -pl use-bdi-plugin clean package` passed with Jason `3.3.0` shaded.
- `use-bdi-plugin/scripts/smoke.ps1` passed the full assembly, including
  `IR_TREE_SMOKE_OK`, partial import, diagnostics, third-party notices, and
  `Plugins > AgentSpeak > Hello BDI Plugin`. Windows left the temporary smoke
  directory locked during cleanup, but all gates completed successfully.

## Phase 2 BDI index and explorer slice evidence - 2026-08-04

- `BdiIndexBuilder` builds an immutable Java-only snapshot with predicate
  signatures, goal-to-supporting-plan lookup, action call sites, predicate
  occurrences, syntactic agent/object references, duplicate explicit-label
  evidence, and metamodel version `0.1.0`.
- Supporting plans use the documented `ACHIEVE + ADD` trigger and matching
  functor/arity. Jason internal `.send` receivers are indexed as agent
  references; named terms in predicate/action arguments are retained as
  object references but are not claimed to be resolved to USE objects.
- `BdiImportService` materializes each selected source independently and keeps
  successful models plus per-file diagnostics. `BdiImportWorker` runs the
  service through `SwingWorker`; `BdiExplorerView` renders file, belief, goal,
  plan, and ordered-step nodes and shows source spans/excerpts on selection.
- `ImportBdiAction` adds `Plugins > AgentSpeak > Import AgentSpeak...` with a
  multi-select `.asl` chooser and opens the view through USE's verified
  `ViewFrame`/`MainWindow.addNewViewFrame` API.
- `mvn -pl use-bdi-plugin -am test` passed with 26 tests, including index,
  partial-import, worker, tree/source-detail, chooser, and existing parser/IR
  regression coverage.
- Packaged distribution smoke passed `BDI_INDEX_SMOKE_OK` through the shaded
  plugin JAR and `GUI_SMOKE_OK` for both AgentSpeak menu actions. Windows left
  the temporary smoke directory locked during cleanup; this does not affect
  the build or smoke assertions. Problems table, filtering, re-import, USE
  adapter, mapping, and rules remain open tasks.

## Phase 2 Problems/re-import and USE adapter slice evidence - 2026-08-04

- `BdiProblemCollector` converts retained `AslDiagnostic`, `UnsupportedFeature`
  (`ASL-002`), and duplicate plan-label evidence into immutable problem rows.
  `BdiProblemTableModel` supports text/severity filtering and deterministic
  grouping by problem group, source, or code; `BdiProblemPanel` exposes those
  controls in the explorer's `Problems` tab.
- `BdiSourceTracker` normalizes selected paths and records existence, size, and
  last-modified stamps after an import. `BdiExplorerView` re-imports the complete
  selected set when any source changes, so unchanged successful models are not
  silently dropped; an import-generation token ignores stale worker callbacks.
- `UseUmlModelFacade` reads the verified USE 7.1.1 APIs for classes, direct
  attributes, associations and ends, operations/parameters, class invariants,
  operation pre/postconditions, current objects/attribute values, and links.
  The facade returns plugin-owned immutable records and does not mutate the
  current USE model or system state.
- `UseOclEvaluator` wraps the actual `OCLCompiler.compileExpression(...)` and
  `Evaluator.eval(...)` APIs and reports `EVALUATED`, `COMPILE_ERROR`, or
  `EVALUATION_ERROR` with diagnostics. `UseModelFingerprint` hashes a canonical
  sorted projection with SHA-256; each reference record exposes a stable
  qualified reference string.
- Fixture `fixtures/use/QueueModel.use` covers classes, attributes, an
  association, objects/links, operations/parameters, pre/postconditions, and a
  class invariant. `mvn -pl use-bdi-plugin test` passed with 32 tests, including
  Problems collector/table, source tracker, UI re-import, facade projection,
  fingerprint, and OCL status tests.
- This slice does not claim mapping entities, mapping suggestions/editor,
  consistency-rule orchestration, or project-wide OCL checks; those remain the
  next open checklist tasks. The repository still lacks the requested
  `docs/00_PROJECT_CONTEXT.md` authoritative file.

## Phase 2 mapping slice evidence - 2026-08-04

- `model/mapping/MappingDocument` is the immutable schema root. It records
  schema version `0.1.0`, BDI metamodel version, USE fingerprint, and binding
  values keyed by mapping kind plus normalized source identity. `MappingBinding`
  supports optional expressions and evidence without depending on Swing, Jason,
  or USE concrete classes.
- `MappingSuggestionService` produces deterministic, explainable candidates for
  Agent -> Class/Object, Action -> Operation, positional parameter bindings,
  `.send` receiver -> USE object, and initial belief -> UML attribute. It uses
  normalized names, operation arity, signatures, and evidence reasons; it does
  not claim semantic resolution or mutate the USE state.
- `MappingEditorPanel` is available as the `Mapping` tab beside `Explorer` and
  `Problems`. It supports Add/update, Apply selected suggestion, Remove, and
  exposes Save/Load actions for the mapping document.
- `MappingFileRepository` and the dependency-free `MappingJsonCodec` provide
  deterministic UTF-8 `.bdimap.json` round trips with schema validation and
  explicit malformed/unknown-enum errors.
- `mvn -pl use-bdi-plugin -am test` passed with 41 tests, including suggestion
  scoring, BDI/USE-to-editor wiring, binding replacement, JSON round
  trip/rejection, and Swing apply/save/load coverage. The plugin compile/package
  path remains shaded and plugin-first; no USE core source was modified.
- At this mapping milestone, stale detection, semantic resolution beyond the
  conservative candidate policy, consistency rules, and project-wide OCL checks
  were intentionally deferred; ADR-0014 records the subsequent static-rule
  implementation and its remaining limits.

## Release documentation and backup slice - 2026-08-09

- `USER_GUIDE.md` documents the exact USE GUI path for loading `Auction.use`,
  opening `View > Create View > Object diagram`, importing both AgentSpeak
  fixtures, reviewing `Explorer`/`Problems`/`Mapping`, and reproducing the
  Auction evidence bundle.
- `DEVELOPER_GUIDE.md` records the verified plugin package boundaries, Jason
  runtime tree, read-only USE access policy, test commands, UI lifecycle, and
  release checks. It does not introduce a USE core change or unverified API.
- `THIRD_PARTY_NOTICES.md` records USE GPLv2 from the checked-in `COPYING` and
  the exact Jason 3.3.0, JADE 4.3, and GlassFish JSON 1.1.4 runtime evidence.
  `ReleaseArtifactTest` protects the three Maven coordinates and the guide
  markers.
- `ReleaseArtifactTest` and `mvn --batch-mode --no-transfer-progress
  -pl use-bdi-plugin -am test` passed with 74 tests. The new test verifies the
  release guides, package evidence, and backup procedure.
- `mvn --batch-mode --no-transfer-progress -pl use-assembly -am package`
  passed. `smoke.ps1` passed parser/report/package checks and the direct GUI
  probe with `GUI_SMOKE_OK`.
- `backup-thesis-artifacts.ps1` passed with `THESIS_BACKUP_OK`, creating a ZIP
  for the committed `HEAD` and a manifest. The manifest records that `data`,
  `slides`, and `presentation` directories are absent from this checkout;
  therefore the full source/data/report/slides item remains open.
- The clean-clone release check passed from committed `HEAD` `594b2b07` with
  `CLEAN_CLONE_REPRODUCIBILITY_OK`. Root `mvn clean verify` now also passes;
  the tag remains pending because the full backup item lacks external
  slide/data input.

## Root verification repair - 2026-08-09

- `Main.main` now returns instead of calling `System.exit(1)` when
  specification compilation fails in explicit `Options.integrationTestMode`.
  Normal CLI invalid-specification behavior is unchanged.
- `ShellIT` normalizes the absolute fixture model path in captured diagnostics
  to the model basename before comparing portable `.in` expectations.
- `IntegrationModeIT` covers invalid `t053.use`; `ShellIT` covers all 120
  shell fixtures. `mvn --batch-mode --no-transfer-progress clean verify`
  passed with 1 `use-core` integration test, 121 `use-gui` integration tests,
  74 plugin tests, and a successful `use-assembly` package.
- The change is limited to test-mode error return and test-output portability;
  it does not alter normal USE CLI exit behavior, plugin lifecycle, or USE
  model state semantics.
