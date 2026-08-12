# Product Requirements

Status: canonical requirement baseline synchronized with implementation
Verification: source-backed; see Git history and DocumentationContractTest

## 1. Status terminology

| Status | Meaning |
| --- | --- |
| Implemented | Runtime behavior and relevant automated evidence exist |
| Partial | A useful subset exists, but the documented end-to-end behavior does not |
| Planned | No complete runtime behavior is verified |
| Optional | Explicitly outside the MVP completion gate |
| Deprecated | Retained only for compatibility and must not be extended |

A source file or class name alone is not evidence that a requirement is
implemented.

## 2. Functional requirements

### 2.1 Plugin integration

| ID | Requirement | Status |
| --- | --- | --- |
| FR-PLG-001 | Package a USE 7.1.1 plugin descriptor and shaded runtime JAR | Implemented |
| FR-PLG-002 | Expose `Hello BDI Plugin`, `Import AgentSpeak...`, and `Import JaCaMo Project...` under `Plugins > AgentSpeak` | Implemented |
| FR-PLG-003 | Open a USE `ViewFrame` and use the current `Session`/`MSystem` when available | Implemented |
| FR-PLG-004 | Keep import available without a loaded UML model and degrade model-dependent checks safely | Implemented |
| FR-PLG-005 | Reload a changed plugin without restarting USE | Planned |
| FR-PLG-006 | Start plugin-owned and USE file choosers at the preferred repository root with a safe moved-checkout fallback | Implemented |

### 2.2 AgentSpeak import and normalized model

| ID | Requirement | Status |
| --- | --- | --- |
| FR-IMP-001 | Parse one or more `.asl` sources through pinned Jason 3.3.0 | Implemented |
| FR-IMP-002 | Preserve per-file success/failure and continue after an invalid file | Implemented |
| FR-IMP-003 | Emit structured syntax/import diagnostics with source location when available | Implemented |
| FR-IMP-004 | Normalize beliefs, goals, plans, triggers, contexts, steps, and terms into immutable Java-owned IR | Implemented |
| FR-IMP-005 | Retain unsupported syntax as `ASL-002` evidence or unsupported IR nodes | Implemented |
| FR-IMP-006 | Build goal, action, predicate, reference, and duplicate-label indexes | Implemented |
| FR-IMP-007 | Run import in a background worker and ignore stale cancelled callbacks | Implemented |
| FR-IMP-008 | Re-import tracked sources when their content changes | Implemented |
| FR-IMP-009 | Parse a `.jcm` through JaCaMo 1.3.0 into portable project/agent/resource IR | Implemented |
| FR-IMP-010 | Resolve project-relative agent sources and preserve valid agents after missing, invalid, or duplicate declarations | Implemented |
| FR-IMP-011 | Retain workspace/institution declarations as explicit unsupported resources and expose organization normalization status | Implemented |
| FR-IMP-012 | Compose a `.jcm` project and its resolved AgentSpeak sources into the shared immutable analysis snapshot | Implemented |
| FR-IMP-013 | Analyze one selected `.jcm` through the Explorer action or headless `--jcm` input without starting JaCaMo | Implemented |

### 2.3 USE projection and mapping

| ID | Requirement | Status |
| --- | --- | --- |
| FR-MAP-001 | Project classes, attributes, associations, operations, constraints, objects, links, and values from current USE state | Implemented |
| FR-MAP-002 | Produce deterministic model fingerprints and stable qualified UML references | Implemented |
| FR-MAP-003 | Suggest agent/class/object, action/operation, argument/parameter, receiver/object, and belief/attribute mappings | Implemented |
| FR-MAP-004 | Require explicit user confirmation before a suggestion becomes a binding | Implemented |
| FR-MAP-005 | Add, update, remove, save, and load versioned `.bdimap.json` bindings | Implemented |
| FR-MAP-006 | Detect missing sources, missing targets, and fingerprint staleness | Implemented |
| FR-MAP-007 | Keep mapping/source identities portable after moving the checkout | Implemented |

### 2.4 Consistency analysis

| ID | Requirement | Status |
| --- | --- | --- |
| FR-VAL-001 | Evaluate the 22 IDs listed in the consistency rule catalog | Implemented |
| FR-VAL-002 | Execute rules in deterministic parse, IR, reference, mapping, signature, ownership, belief/message/context, and OCL phases | Implemented |
| FR-VAL-003 | Return immutable issues with severity, status, certainty, source, UML target, evidence, and suggested fix | Implemented |
| FR-VAL-004 | Enable a configured subset of known rules and fail fast on unknown IDs | Implemented |
| FR-VAL-005 | Persist and apply source-fingerprint suppressions with a reason | Implemented |
| FR-VAL-006 | Auto-load rules and suppressions from `.bdi-plugin` beside the active `.use` model | Implemented |
| FR-VAL-007 | Refresh Problems after import and confirmed mapping changes | Implemented |
| FR-VAL-008 | Never silently classify an unavailable semantic fact as PASS | Implemented |

### 2.5 OCL and bounded effects

| ID | Requirement | Status |
| --- | --- | --- |
| FR-OCL-001 | Bind receiver and operation arguments against a copied USE variable environment | Implemented |
| FR-OCL-002 | Evaluate operation preconditions on the current snapshot | Implemented |
| FR-OCL-003 | Distinguish `PASS`, `FAIL`, and `UNKNOWN` with evidence | Implemented |
| FR-OCL-004 | Accept bounded effects only through the explicit `soil:` mapping expression | Implemented |
| FR-OCL-005 | Execute bounded effects inside a disposable variation and re-check invariants | Implemented |
| FR-OCL-006 | Restore the original state even when compile/evaluation/execution fails | Implemented |
| FR-OCL-007 | Perform unbounded plan/model checking | Optional |

### 2.6 GUI and reporting

| ID | Requirement | Status |
| --- | --- | --- |
| FR-UI-001 | Display imported sources, beliefs, goals, plans, and ordered steps in a BDI tree | Implemented |
| FR-UI-002 | Display selected-node details, source span, and source excerpt | Implemented |
| FR-UI-003 | Display filterable/groupable Problems and mapping editor tabs | Implemented |
| FR-UI-004 | Surface partial import and unsupported syntax without crashing the view | Implemented |
| FR-UI-005 | Select a `.jcm` project, inspect resolved agents/project diagnostics, and retain the immutable result in Explorer | Implemented |
| FR-REP-001 | Export supplied analysis results as deterministic UTF-8 JSON and escaped HTML | Implemented |
| FR-REP-002 | Include plugin/USE/Jason metadata, issues, evidence, and suppressions | Implemented |
| FR-REP-003 | Include canonical model and mapping SHA-256 values when supplied | Implemented |
| FR-REP-004 | Export the current live GUI analysis with one user action | Implemented |
| FR-REP-005 | Generate a zero-state serializer smoke through `ReportMain` without claiming live analysis | Implemented |
| FR-REP-006 | Compose one immutable current analysis snapshot for Problems, GUI export, and headless gates | Implemented |
| FR-REP-007 | Run direct `.asl` or `.jcm` analysis headlessly with deterministic reports and distinct CI exit semantics | Implemented |

### 2.7 Traceability

| ID | Requirement | Status |
| --- | --- | --- |
| FR-TRC-001 | Derive an immutable source-to-mapping-to-UML/OCL-to-issue graph from one current-analysis snapshot | Implemented |
| FR-TRC-002 | Preserve issue status/certainty and represent absent confirmed mappings as explicit gaps | Implemented |
| FR-TRC-003 | Query one issue chain deterministically without exposing checkout-absolute paths | Implemented |

### 2.8 Environment consistency pilot

| ID | Requirement | Status |
| --- | --- | --- |
| FR-ENV-001 | Normalize CArtAgO 3.1 artifact operations into immutable plugin-owned environment IR | Implemented |
| FR-ENV-002 | Validate mapped artifact operation existence and arity against USE operation targets | Implemented |
| FR-ENV-003 | Validate declared observable-property/UML-attribute targets and return UNKNOWN without dynamic value evidence | Implemented |
| FR-ENV-004 | Run CArtAgO workspaces or consume live artifact state | Planned |
| FR-ENV-005 | Persist typed CArtAgO operation/property mappings in a portable deterministic `.cartago-map.json` document | Implemented |
| FR-ENV-006 | Revalidate confirmed environment targets and preserve explicit candidate/stale/unknown status | Implemented |

### 2.9 Static organization import

| ID | Requirement | Status |
| --- | --- | --- |
| FR-ORG-001 | Parse a referenced Moise 1.1 organization specification through the official API without starting a runtime | Implemented |
| FR-ORG-002 | Normalize roles, groups, goals, missions, norms, and cardinalities into immutable portable organization IR | Implemented |
| FR-ORG-003 | Report missing, invalid, duplicate, and unsupported organization evidence explicitly | Implemented |
| FR-ORG-004 | Preserve unavailable source coordinates as unknown rather than inventing positions | Implemented |
| FR-ORG-005 | Evaluate confirmed role/class, mission/operation, and reviewed cardinality/OCL mappings with explicit UNKNOWN runtime semantics | Implemented |

### 2.10 Diagram visualization

| ID | Requirement | Status |
| --- | --- | --- |
| FR-DIA-001 | Provide an immutable renderer-neutral diagram domain with portable deterministic identities and validated node, edge, and group references | Implemented |
| FR-DIA-002 | Derive a read-only BDI structure diagram from the current immutable analysis snapshot without reparsing AgentSpeak | Implemented |
| FR-DIA-003 | Project confirmed mappings, UML/OCL targets, and explicit mapping gaps into the diagram | Implemented |
| FR-DIA-004 | Preserve issue status, certainty, and evidence associations in diagram projections | Implemented |
| FR-DIA-005 | Project static JaCaMo, CArtAgO, and Moise structures without implying runtime behavior | Planned |
| FR-DIA-006 | Render diagrams in USE with deterministic layout, zoom, pan, fit, selection, and highlighting | Implemented |
| FR-DIA-007 | Navigate diagram selections to existing Explorer, Problems, source, and mapping details | Planned |
| FR-DIA-008 | Provide validated diagram demos, mutants, screenshots, and performance evidence | Planned |

### 2.11 Case study and release

| ID | Requirement | Status |
| --- | --- | --- |
| FR-CS-001 | Provide a compilable Auction UML/OCL model and valid AgentSpeak pair | Implemented |
| FR-CS-002 | Provide structural, signature, reference, and OCL mutants with an oracle | Implemented |
| FR-CS-003 | Produce reproducible baseline reports, metrics, diagrams, and limitation evidence | Implemented |
| FR-CS-004 | Import the House Building case study | Optional |
| FR-CS-005 | Execute the reviewed Auction baseline and mutants through the real headless analysis service in isolated temporary workspaces | Implemented |
| FR-CS-006 | Emit deterministic JSON, CSV, and HTML evaluation results containing manifest, corpus, configuration, input, and trace identities | Implemented |
| FR-CS-007 | Classify detected, missed, unexpected, PASS, UNKNOWN, unsupported, timeout, and execution-error outcomes with corpus-scoped metrics | Implemented |
| FR-CS-008 | Reject malformed or unsafe manifests before execution and prove source fixtures and the current USE state remain unchanged | Implemented |
| FR-REL-001 | Pass root `mvn clean verify` and plugin/package smoke checks | Implemented |
| FR-REL-002 | Verify packaging from an exact clean committed tree | Implemented |
| FR-REL-003 | Create `v1.0.0-thesis-rc` | Planned |
| FR-REL-004 | Back up source, data, report, and slides in one complete manifest | Partial |

## 3. Business rules

| ID | Rule |
| --- | --- |
| BR-001 | Jason AST types stop at the importer/normalizer boundary. |
| BR-002 | Unsupported syntax produces evidence and is never silently dropped. |
| BR-003 | Mapping suggestions are not confirmed mappings. |
| BR-004 | Rule evaluation consumes normalized IR, mappings, and immutable USE projections. |
| BR-005 | Missing runtime/model information results in an issue or `UNKNOWN`, not an invented PASS. |
| BR-006 | Analysis must not permanently mutate the current USE state. |
| BR-007 | Suppression requires rule ID, source fingerprint, and a non-empty reason. |
| BR-008 | Unknown configured rule IDs fail fast. |
| BR-009 | Reports preserve evidence and escaping; they do not execute or reinterpret rules. |
| BR-010 | Evaluation metrics remain scoped to the labeled Auction mutants. |
| BR-011 | USE core changes require an accepted ADR and focused regression evidence. |
| BR-012 | Release completion requires tests, docs, package evidence, tag, and complete artifact backup. |
| BR-013 | JaCaMo parser/model types stop at the project adapter; static import does not imply runtime support. |
| BR-014 | The traceability graph is derived evidence, never a mutable or persisted source of truth. |
| BR-015 | CArtAgO concrete APIs stop at the adapter; static declarations cannot imply runtime-state consistency. |
| BR-016 | Environment mapping suggestions remain candidates; only confirmed current bindings enter environment rules, while stale and unknown states remain explicit. |
| BR-017 | Evaluation oracles are declared outside the runner; missing/ambiguous input or evidence is invalid, unsupported, or unknown and never becomes a semantic PASS. |
| BR-018 | Moise concrete classes stop at the organization adapter; static normalization does not imply enactment or runtime consistency. |
| BR-019 | Organization suggestions remain candidates; static cardinality checks require a confirmed invariant target plus reviewer-normalized bounds, and matching bounds remain UNKNOWN without enactment evidence. |
| BR-020 | A diagram is an immutable derived presentation, never a parser, validator, editable semantic model, persisted source of truth, or runtime claim. |

## 4. Core acceptance criteria

1. Loading the shaded JAR exposes both AgentSpeak actions in USE.
2. Importing valid-invalid-valid sources returns two models plus one structured
   syntax diagnostic without aborting the complete import.
3. A valid but unsupported relational context is retained as `ASL-002` and a
   dedicated IR node.
4. Import with a current USE system creates mapping candidates and rule results
   without modifying that system.
5. A confirmed mapping round-trips through `.bdimap.json` deterministically.
6. Missing mapped targets produce `MAP-003`; signature, reference, and OCL
   mutants produce their scoped expected rule IDs.
7. OCL/effect checks restore the original snapshot fingerprint.
8. JSON/HTML outputs preserve metadata, issues, evidence, hashes, and
   suppressions while escaping untrusted text.
9. The Auction evidence command ends with `AUCTION_EVIDENCE_OK`.
10. The assembled distribution contains the shaded plugin, Jason runtime, and
    third-party notices.
11. Root verification completes all module and integration tests.
12. Unknown/unsupported behavior remains visible in docs and reports.
13. Missing project configuration uses visible defaults; malformed or unknown-rule configuration prevents the Explorer from opening with an explicit error.
14. Copying a relative Auction `.jcm` project to another checkout produces the
    same portable project IR and imports the same resolved AgentSpeak sources.
15. Selecting Auction `.jcm` in Explorer and passing it to CLI `--jcm` use the
     same application snapshot semantics; project diagnostics remain visible.
16. Conflicting `--asl` and `--jcm`, missing files, wrong extensions, and
     cancelled GUI work do not create misleading analysis output.
17. A fixed-timestamp run of the reviewed Auction manifest produces one PASS,
    four DETECTED cases, and byte-stable JSON/CSV/HTML on repetition.
18. Duplicate IDs, unknown fields, path traversal, missing oracle entries,
    malformed numbers, missing files, timeouts, and tool failures have explicit
    diagnostics and do not contribute semantic detection counts.
19. The evaluation runner applies only named static state fixtures inside its
    private `MSystem`; the input files and caller's USE state are unchanged.
20. Evaluation metrics identify the declared corpus and excluded layers and are
    documented as evidence for this reviewed corpus, not general correctness.

## 5. Non-functional requirements

### Correctness and determinism

- Immutable domain values and stable ordering for mappings, rules, and reports.
- Repeated serialization of the same input is byte-identical where promised.
- Diagnostics and findings retain source/evidence whenever available.

### Safety

- No normal analysis path may commit a mutation to the user's USE state.
- Parser input, mapping JSON, rule JSON, and suppression JSON are validated.
- HTML output escapes issue text and evidence.
- No network listener is started by offline AgentSpeak import.

### Performance and responsiveness

- Import runs outside the Swing event-dispatch thread.
- Stale worker callbacks cannot replace a newer import result.
- The benchmark records rather than hard-codes environment-dependent timing.

### Compatibility and portability

- Build target is Java 21; host model is USE 7.1.1.
- Jason and runtime dependencies are pinned and shaded.
- New persisted source identities are project-relative. Irreversible legacy
  suppression hashes remain explicitly versioned and may become stale after
  relocation rather than matching more broadly.

### Maintainability

- Plugin-first boundaries and adapters isolate USE/Jason concrete APIs.
- Every rule ID has catalog and test traceability.
- Material architecture changes require a new or superseding ADR.

## 6. Error behavior

| Condition | Required behavior |
| --- | --- |
| Invalid `.asl` syntax | `ASL-001` diagnostic; other files continue |
| Import/file failure without parser location | `ASL-IMPORT-001` with unknown position |
| Supported Jason syntax not represented in IR | `ASL-002` and unsupported node/feature |
| No USE model loaded | Import/Explorer still work; model-dependent checks are absent/unknown |
| Malformed mapping/config/suppression JSON | Reject with descriptive exception; do not partially apply |
| Unknown configured rule | Fail configuration validation immediately |
| OCL compile/evaluation failure | Preserve status/evidence as `UNKNOWN` path |
| Bounded SOIL failure | Restore variation and report failure/unknown evidence |
| Cancelled/stale import worker | Ignore stale callback; retain newest generation |
| Conflicting or invalid project input | Reject before report creation with exit 3 and an actionable input diagnostic |
| Report path/write failure | Propagate/report I/O failure; do not claim success |
| Invalid `.jcm` syntax | `JCM-001` with parser position when available; no partial project is fabricated |
| Missing/invalid/duplicate `.jcm` agent | `JCM-002`/`JCM-004`/`JCM-003`; independent valid sources remain imported |
| JaCaMo workspace/institution resource | Retain `UNSUPPORTED` reference and `JCM-005` warning |
| Moise organization resource | Normalize or emit `JCM-007..010` with source evidence; never silently ignore |

## 7. Requirement synchronization checklist

- [x] Statuses were verified against source and tests.
- [x] Partial GUI configuration/report integration is explicit.
- [x] Release and optional case-study boundaries match the completion checklist.
- [x] Acceptance criteria have source/test traceability in
  [the traceability matrix](12_REQUIREMENT_TRACEABILITY.md).
- [x] No web/database/multi-user requirements were copied from the reference
  project.
