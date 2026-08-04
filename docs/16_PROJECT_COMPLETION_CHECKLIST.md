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

- [ ] Goal -> supporting plans index.
- [ ] Action -> call sites index.
- [ ] Predicate references index.
- [ ] Agent/object references index.
- [ ] Duplicate label detection.
- [ ] BDI metamodel version recorded.

## 5. UI shell

- [x] AgentSpeak menu.
- [ ] File chooser import.
- [ ] Background import task.
- [ ] BDI tree.
- [ ] Node detail/source panel.
- [ ] Problems table.
- [ ] Filtering/grouping.
- [ ] Re-import changed files.

## 6. USE adapter

- [ ] Enumerate UML classes.
- [ ] Enumerate objects in current state.
- [ ] Enumerate attributes/associations.
- [ ] Enumerate operations and parameters.
- [ ] Expose pre/postconditions.
- [ ] OCL expression evaluation wrapper.
- [ ] Stable UML element references/fingerprint.

## 7. Mapping

- [ ] Mapping entity/schema.
- [ ] Agent -> Class.
- [ ] Agent -> Object.
- [ ] Action -> Operation.
- [ ] Parameter bindings.
- [ ] Receiver binding.
- [ ] Belief mapping model.
- [ ] Suggestion scoring.
- [ ] Manual editor.
- [ ] Save/load.
- [ ] Stale mapping detection.

## 8. Consistency engine

- [ ] Rule SPI/interface.
- [ ] Phase orchestration.
- [ ] Issue/evidence model.
- [ ] ASL-001/002.
- [ ] BDI-001/002/003/004.
- [ ] REF-001/002.
- [ ] MAP-001/002/003.
- [ ] SIG-001/002/003.
- [ ] OWN-001.
- [ ] BEL-001.
- [ ] MSG-001.
- [ ] OCL-001/002/003/004.
- [ ] CTX-001.
- [ ] Suppression.

## 9. OCL integration

- [ ] Bind receiver.
- [ ] Bind operation arguments.
- [ ] Evaluate precondition on snapshot.
- [ ] PASS/FAIL/UNKNOWN result.
- [ ] Isolated/safe state mutation strategy.
- [ ] One bounded effect adapter.
- [ ] Re-check invariant.
- [ ] Restore/avoid corrupting user state.

## 10. Reporting

- [ ] JSON report.
- [ ] HTML or CSV report.
- [ ] Plugin/USE/Jason versions.
- [ ] Model and mapping hashes.
- [ ] Rule configuration.
- [ ] Issue evidence/source.
- [ ] Suppressions included.

## 11. Testing

- [x] Valid ASL fixtures.
- [x] Invalid ASL fixtures.
- [ ] Unsupported fixtures.
- [ ] Golden IR tests.
- [ ] Rule tests.
- [ ] Mapping persistence tests.
- [ ] USE model integration tests.
- [ ] OCL tests.
- [x] UI smoke test.
- [ ] Performance benchmark.
- [ ] Clean-clone reproducibility test.

## 12. Case study

- [ ] Auction UML/OCL model.
- [ ] Auction AgentSpeak files.
- [ ] Valid mapping.
- [ ] Baseline report.
- [ ] Structural mutants.
- [ ] Signature mutants.
- [ ] Reference mutants.
- [ ] OCL mutants.
- [ ] Ground truth manifest.
- [ ] Metrics table.
- [ ] Demo script.
- [ ] House Building exploratory import (optional).

## 13. Thesis evidence

- [ ] Architecture diagram.
- [ ] IR class diagram.
- [ ] BDI metamodel diagram.
- [ ] Mapping examples.
- [ ] Rule catalog final.
- [ ] UI screenshots.
- [ ] Experiment protocol.
- [ ] Precision/recall/F1.
- [ ] Performance chart/table.
- [ ] Threats to validity.
- [ ] Limitations.
- [ ] Future work.

## 14. Release

- [ ] `mvn clean verify` pass.
- [ ] Plugin install guide.
- [ ] User guide.
- [ ] Developer guide.
- [ ] Third-party notices/licenses.
- [ ] Release package tested on clean machine/profile.
- [ ] Git tag `v1.0.0-thesis-rc`.
- [ ] Backup source, data, report and slides.

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
