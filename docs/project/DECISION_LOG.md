# Decision Log

Status: accepted architecture invariants and open decisions
Verification: source-backed; see Git history and DocumentationContractTest

Accepted decisions are not silently reversed. A material change to USE core,
parser boundaries, source identity, persistence schema, rule semantics, state
mutation, report identity, plugin lifecycle, or release architecture requires a
new ADR that explicitly supersedes the affected entry.

## 1. Accepted ADR Invariants

| ADR | Invariant that future work must preserve | Primary evidence |
| --- | --- | --- |
| ADR-0001 | Use an in-repository plugin module, verified action lifecycle, session APIs, and programmatic `ViewFrame`; package it through `use-assembly` | plugin descriptor, action tests, assembly |
| ADR-0002 | Pin Jason 3.3.0 behind an adapter and shade required runtime dependencies without relocation | POM, parser/package smoke, notices |
| ADR-0003 | Convert Jason syntax failures to Java-only `ASL-001` diagnostics with real positions when available | parser diagnostic tests |
| ADR-0004 | Preserve deterministic ordered multi-file results; the early fail-fast policy was superseded by ADR-0006 | importer tests |
| ADR-0005 | Keep prototypes under fixture/case-study boundaries, never repository root | fixture tests and smoke |
| ADR-0006 | Attempt every valid source, retain partial successes, and expose non-syntax import failures as `ASL-IMPORT-001` | multi-file tests |
| ADR-0007 | Extract declaration source locations at the Jason boundary; unavailable coordinates remain explicit | source-location tests |
| ADR-0008 | Derive parser-version metadata from successful imports and the pinned Maven property | report/parser tests |
| ADR-0009 | Use a plugin-owned `AgentModel` root; do not invent an agent name absent from source evidence | IR tests |
| ADR-0010 | Normalize Jason AST to immutable sealed IR, retain unsupported constructs as `ASL-002`, and keep golden output portable | hierarchy/golden tests |
| ADR-0011 | Build IR-only deterministic indexes and import in a background Explorer worker; static support does not claim runtime applicability | index/Explorer tests |
| ADR-0012 | Present Problems/re-import from immutable snapshots; project USE state read-only and preserve explicit OCL failure statuses | Problems/facade/OCL tests |
| ADR-0013 | Use conservative, explainable mapping candidates and six versioned mapping kinds; only the user confirms bindings | mapping/editor/persistence tests |
| ADR-0014 | Run deterministic rule phases; missing targets are errors, uncertain runtime references/types remain potential/unknown, and state fingerprints alone do not invalidate mappings | rule/staleness tests |
| ADR-0015 | JSON/HTML exporters serialize supplied issue evidence, escape output, and never run validation or query the live session | exporter tests |
| ADR-0016 | Compute canonical lowercase SHA-256 model/mapping identities outside exporters; binding order is non-semantic | fingerprint/report tests |
| ADR-0017 | Use versioned explicit `enabledRules`; reject configured IDs absent from the active catalog | config/orchestrator tests |
| ADR-0018 | Suppress only exact rule/source-fingerprint pairs with a reason and retain suppression transparency in reports | suppression/report tests |
| ADR-0019 | Invalid specifications return only in explicit integration-test mode; normal CLI exit behavior remains unchanged | 121 GUI integration tests |
| ADR-0020 | Discover rules/suppressions only beside the active file-backed `.use` model; visible defaults for absence and visible failure for invalid input | loader/action/Explorer tests |
| ADR-0021 | Represent portable source evidence as a case-preserving, project-relative `ProjectSourceId` v2 with explicit coordinates; reject sources outside an explicit root and retain v1 mapping behavior until migration | `ProjectSourceIdTest` |
| ADR-0022 | Persist mappings and suppressions as schema `0.2.0` under an explicit existing project root; migrate v1 mappings to portable IDs on save, but retain irreversible v1 suppression hashes as legacy-only entries so relocation cannot broaden suppression | repository migration and relocation tests |
| ADR-0023 | Refresh USE state manually through a plugin-owned provider that resolves the current session system per capture; run capture/validation on the EDT, discard stale generations, and verify the state fingerprint before/after analysis without claiming host event subscription | Explorer/provider/evaluator refresh tests |
| ADR-0024 | Compose Problems/export/headless inputs through one immutable application-owned `CurrentAnalysisSnapshot`; validation runs once per composition, caller supplies time, and ADR-0016 hashes plus counts/version/config/suppression evidence are constructor-validated | current-analysis Auction/malformed/Explorer tests |
| ADR-0025 | Headless quality gate requires explicit `.use`, one-or-more `.asl`, and JSON/HTML output paths; mapping/rules/suppressions are optional explicit files, timestamp defaults to epoch, no CWD discovery occurs, and exits are 0 clean, 1 confirmed findings, 2 potential/unknown-only, 3 invalid input/config, 4 infrastructure/output failure | CLI integration/process smoke and deterministic-report tests |
| ADR-0026 | Use the official JaCaMo 1.3.0 parser/model behind an adapter, paired with Jason 3.3.0; shade only the JaCaMo artifact and exclude its runtime transitives | parser spike, dependency evidence, package smoke |
| ADR-0027 | Derive an immutable traceability graph from `CurrentAnalysisSnapshot`; use project-relative source and qualified UML identities, preserve issue certainty, and represent missing mappings as explicit gaps | Auction graph/query/portability tests |
| ADR-0028 | Inspect CArtAgO 3.1 `@OPERATION` metadata behind an adapter, model observable properties from explicit static descriptors, keep pilot mappings in memory, and return UNKNOWN without runtime state evidence | adapter/mutant/catalog/package tests |

## 2. Cross-Cutting Safety Decisions

- Plugin-first; no USE parser/AST/core extension without a dedicated ADR.
- Jason remains syntax authority; no replacement AgentSpeak parser.
- Domain/rules consume normalized IR, never Jason AST or Swing state.
- Suggestions are not mappings until confirmed.
- OCL is PASS/FAIL/UNKNOWN and bounded effects restore state in `finally`.
- Generated reports and metrics are scoped evidence, not general proof.
- Auction is the required MVP corpus; House Building is optional.
- Current project includes static JaCaMo project import, not full runtime integration.

## 3. Open Decisions

| ID | Decision needed | Safe current behavior |
| --- | --- | --- |
| OD-003 | One-click serialization of the current GUI analysis | Resolved: Explorer exports the shared snapshot atomically as JSON/HTML |
| OD-004 | Closed unknown-field policy for mapping JSON | Validate required/current fields and document remaining leniency |
| OD-005 | Automatic USE state-change subscription lifecycle | Use `Refresh USE Snapshot`; stale queued refreshes are discarded |
| OD-006 | External thesis data/report/slides locations and release owner | Keep backup/tag gates open |
| OD-007 | Scope of JaCaMo integration (`.jcm`, CArtAgO, Moise, runtime traces) | Static project/agent IR and CArtAgO artifact pilot resolved by ADR-0026/0028; Moise and runtime remain open |

## 4. Active Risks

| Risk | Control | Residual status |
| --- | --- | --- |
| Unsupported AgentSpeak is misrepresented | Jason authority, unsupported IR, diagnostics, golden fixtures | controlled |
| Legacy suppression hashes become stale after checkout relocation | versioned legacy marker prevents broad matching; recreate reviewed entries as v2 when appropriate | accepted migration limitation |
| OCL information gap becomes false PASS | explicit PASS/FAIL/UNKNOWN | controlled |
| Analysis mutates current USE state | read-only facade, disposable variation, fingerprint tests | controlled |
| Open Explorer does not update automatically after USE state changes | visible manual refresh with state-safety check | automatic subscription open via OD-005 |
| Mapping JSON typo is tolerated | domain validation and tests | open via OD-004 |
| Report is mistaken for live analysis | GUI export is snapshot-backed; `ReportMain` remains explicitly demo-only | mitigated |
| JaCaMo scope is overclaimed from static project import | adapter boundary, runtime dependency exclusions, explicit resource status | open via OD-007 |
| External thesis artifacts are omitted | backup manifest and open release gate | open via OD-006 |

## 5. Current Validation Record

- Headless CLI/current-snapshot/report focused tests: 10 pass.
- Plugin suite: 121 pass, including JaCaMo project import, portable
  traceability, CArtAgO adapter/environment mutants, and boundary tests.
- Reactor `mvn -pl use-bdi-plugin -am test`: all four modules succeed.
- Package smoke verifies CArtAgO/Jason/JaCaMo classes, excludes Moise, and
  returns `GUI_SMOKE_OK`; packaged headless smoke verifies exits 1/3.
- Root `mvn clean verify`: all five modules, 121 GUI integration tests, and
  ZIP/TAR distributions succeed.
- Root `mvn clean verify` passed for the preceding host-refresh commit; current
  application changes do not alter host or assembly wiring.
- Auction evidence covers baseline plus signature, reference, OCL, and structural
  mutants with scoped metrics.
- Documentation contract checks the compact inventory, links, versions,
  descriptor labels, open risks, JaCaMo boundary, and absence of deleted
  duplicate files.

Exact dates, commits, command output, and historical implementation details are
available from Git history, CI, and `docs/project/evidence/`; they are not
duplicated here.

## 6. ADR-0026: Official JaCaMo Parser With A Static-Only Package

**Status:** Accepted. **Date:** 2026-08-10.

The `.jcm` syntax authority is the official `org.jacamo:jacamo:1.3.0`
artifact. Its published POM depends on Jason 3.3.0, matching ADR-0002. The
plugin calls `JaCaMoProjectParser(Reader).parse(String)` only through an
adapter and immediately converts the result to plugin-owned values.

Option A, selected, shades the JaCaMo parser/model JAR while excluding every
transitive runtime dependency. The existing explicit Jason 3.3.0 dependency
supplies the parser's shared MAS model. This keeps the USE plugin small and
prevents accidental runtime claims. Option B, rejected for this slice, packages
the full JaCaMo dependency graph and launcher; it introduces CArtAgO, Moise,
REST, Gradle Tooling and runtime lifecycle/classloader risk without serving
static project import.

The accepted boundary does not parse CArtAgO Java artifacts, normalize Moise,
or launch a MAS. Environment and organization declarations must remain visible
as unsupported resource references until dedicated adapters are accepted.
JaCaMo and Jason contain seven overlapping project-template resources in the
shaded JAR; no parser classes overlap, and these templates are outside the
static import contract. Package smoke continues to verify both parser classes.
`docs/project/evidence/jacamo-parser-spike.md` records the source signatures,
artifact provenance, dependency result, checksums, and fallback.

## 7. ADR-0027: Snapshot-Derived Traceability Graph

**Status:** Accepted. **Date:** 2026-08-10.

Traceability is an immutable explanatory projection of one
`CurrentAnalysisSnapshot`, not another persistence model or mutable source of
truth. `TraceabilityGraphBuilder` receives the explicit project root required
by ADR-0021, derives stable typed nodes and evidence-bearing edges, and uses
qualified UML references for model targets. Jason plan labels are not used as
portable IDs because their source annotations can contain checkout-absolute
URLs; the portable source span is the BDI element identity.

Missing confirmed mappings create a typed `GAP` node and `MISSING_MAPPING`
edge. OCL issues retain their `OPEN` lifecycle status and `UNKNOWN` certainty
through the issue node and chain edges. The deterministic debug serializer
omits raw evidence text and absolute paths. Graph visualization, persistence,
CArtAgO, Moise, and runtime traces remain outside this MVP slice.

## 8. ADR-0028: Static CArtAgO Artifact Consistency Pilot

**Status:** Accepted. **Date:** 2026-08-10.

The plugin pins official `org.jacamo:cartago:3.1`. The adapter reflects the
runtime-retained `cartago.OPERATION` annotation and immediately emits
plugin-owned environment values. No CArtAgO type crosses into environment
models, mappings, rules, or traceability. The packaged CArtAgO JAR lets user
artifact classes link to the API, but the plugin never creates a workspace or
calls `CartagoService`; all transitives remain excluded.

CArtAgO observable properties are created imperatively through
`Artifact.defineObsProperty`, not a declaration annotation. This pilot accepts
explicit static property descriptors as evidence and does not parse Java
source or instantiate a runtime workspace. Missing dynamic values therefore
produce `ENV-003` with `UNKNOWN`, never PASS.

Adding persisted environment kinds to mapping schema `0.2.0` would require a
migration and compatibility policy. Rejected for this pilot: reusing existing
mapping kinds or silently extending their meaning. Selected: separate immutable
in-memory operation/property mappings until a later persistence ADR. Moise,
runtime execution, live property capture, and generic Java parsing remain out
of scope.
