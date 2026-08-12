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
| ADR-0029 | Compose static `.jcm` imports and direct AgentSpeak imports through one immutable `MasProjectAnalysisService` and keep project diagnostics separate from BDI diagnostics | project-analysis service tests |
| ADR-0030 | Route GUI and headless `.jcm` entry points through the shared project service; use a background Swing worker, reject mixed input before output, and never start JaCaMo runtime | Explorer/worker/CLI/package smoke |
| ADR-0031 | Persist CArtAgO operation/property bindings in a separate typed environment document; preserve BDI mapping schema `0.2.0` and reject unknown environment fields/versions | environment codec/repository/migration tests |
| ADR-0032 | Historical Moise blocker; superseded by ADR-0034 after official parser/API/license evidence became reproducible | original fallback and boundary tests |
| ADR-0033 | Run the reviewed Auction corpus through an isolated real headless service with named static state fixtures and a declared external oracle; normalize Jason source URLs out of mapping identity for relocation | evaluation codec/runner tests, packaged Auction evaluation smoke |
| ADR-0034 | Pin official Moise 1.1 behind one static adapter, normalize a bounded immutable organization IR, and keep enactment/rules outside this slice | organization adapter/golden/diagnostic/boundary/package tests |
| ADR-0035 | Evaluate a separate static organization catalog over confirmed plugin-owned mappings; use reviewer-normalized OCL cardinality bounds and retain runtime enactment as UNKNOWN | Auction organization baseline/mutant/trace tests |
| ADR-0036 | Define a renderer-neutral immutable diagram domain with portable semantic identity and constructor-enforced graph integrity; keep projection, rendering, and interaction in later slices | diagram invariant, relocation, and package-boundary tests |

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
| OD-007 | Scope of JaCaMo runtime integration | Static project/agent, CArtAgO artifact, and Moise organization IR are resolved; enactment, live state, and runtime traces remain open |

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
| JaCaMo scope is overclaimed from static project import | adapter boundaries, explicit resource status, no launcher/enactment claim | open via OD-007 |
| External thesis artifacts are omitted | backup manifest and open release gate | open via OD-006 |

## 5. Current Validation Record

- Headless CLI/current-snapshot/report focused tests: 10 pass.
- Plugin suite: 162 pass, including JaCaMo project import, portable
  traceability, CArtAgO adapter/environment mutants, Moise organization
  normalization, static organization rules/traceability, and boundary tests.
- Reactor `mvn -pl use-bdi-plugin -am test`: all four modules succeed.
- Package smoke verifies CArtAgO/Jason/JaCaMo/Moise static classes and
  returns `GUI_SMOKE_OK`; packaged headless smoke verifies exits 1/3.
- Root `mvn verify`: all five modules, 1 core integration test, 121 GUI
  integration tests, 162 plugin tests, and
  ZIP/TAR distributions succeed.
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

## 9. ADR-0029: Shared Static JaCaMo Project Analysis Service

**Status:** Accepted. **Date:** 2026-08-11.

The static `.jcm` importer and direct AgentSpeak importer must converge before
GUI/CLI entry points are added. `MasProjectAnalysisService` accepts immutable
project-root, project-file, USE projection, OCL evaluator, mapping, timestamp,
and `BdiProjectConfiguration` inputs. It delegates parsing to
`MasProjectImportService`, then invokes the existing
`CurrentAnalysisSnapshotService` exactly once. The service returns project IR,
sorted project diagnostics, and one immutable analysis snapshot; it does not
start JaCaMo or introduce a second validator.

Option A, selected, keeps project diagnostics separate from BDI import
diagnostics while retaining valid partial results. Option B, rejected, would
make the GUI or CLI call importer and validators directly, duplicating policy
and making snapshot/report parity untestable. Direct `.asl` behavior remains
unchanged because the new service consumes the existing `BdiImportSnapshot` and
plugin-owned snapshot types. The next slice may add entry points, but must route
both UI and headless execution through this service.

## 10. ADR-0030: Static `.jcm` GUI And Headless Entry Points

**Status:** Accepted. **Date:** 2026-08-11.

The GUI and CLI are two presentations of the same T11 application boundary.
`ImportJaCaMoAction` and `BdiExplorerView` create an immutable
`MasProjectAnalysisRequest`; `BdiProjectImportWorker` executes it outside the
Swing EDT and applies only the current generation. `BdiQualityGateMain --jcm`
performs explicit input validation, delegates to the same service, prints
sorted project diagnostics, and uses the existing snapshot-backed JSON/HTML
exporters. Mixed `--asl` and `--jcm`, missing paths, and wrong extensions are
input errors and cannot create reports.

Option A, selected, keeps one importer/validator/snapshot policy and makes
GUI/CLI parity testable. Option B, rejected, would duplicate project analysis
inside Swing or the command parser. Runtime JaCaMo startup, live CArtAgO,
Moise, subscriptions, and automatic project-resource execution remain outside
this slice. Project diagnostics are visible in the Explorer tree/status and
CLI output; the existing consistency report remains the immutable analysis
snapshot supplied by the caller.

## 11. ADR-0031: Separate Versioned CArtAgO Mapping Document

**Status:** Accepted. **Date:** 2026-08-11.

The existing `MappingDocument` is the source of truth for six BDI-to-UML
mapping kinds and schema `0.2.0`. CArtAgO operation/property mappings have
different identity, artifact/operation arity evidence, confirmation, source
provenance, and stale-target semantics. They are therefore persisted in a
separate plugin-owned environment document (for example
`.cartago-map.json`) with its own version and strict closed-field codec.

Option A, selected, keeps `MappingDocument` byte/semantic compatible and lets
the environment validator consume only confirmed, current typed records.
Option B, rejected, would add environment fields to the BDI schema and force a
cross-domain migration before the pilot has an accepted runtime contract. No
legacy environment document existed before this ADR; BDI-only files are
regression-tested unchanged. Unknown environment versions, fields, duplicate
keys, invalid roots, and malformed records fail before a file is rewritten.

Suggestions remain `CANDIDATE`; stale or unavailable targets are explicit
`STALE`/`UNKNOWN` states and never silently become valid mappings. CArtAgO
concrete types remain confined to the adapter.

## 12. ADR-0032: Blocked Moise Organization Normalization

**Status:** Superseded by ADR-0034. **Date:** 2026-08-11.

The verified JaCaMo `1.3.0` parser accepts the `.jcm` `organisation`
declaration and exposes `JaCaMoOrgParameters`, groups, and schemes as project
configuration objects. It does not parse the referenced Moise organization
file into roles, missions, goals, permissions, or cardinalities. The local
`org.jacamo:jacamo:1.3.0` POM declares `org.jacamo:moise:1.1` upstream, but this
plugin's dependency tree intentionally contains only the direct JaCaMo parser
and CArtAgO API; the Moise artifact and `ora4mas/nopl` runtime marker are absent
from the resolved and shaded package. `javap` confirms that
`jacamo.platform.Moise` is a runtime platform class, not an organization-file
parser entry point.

Option A, rejected for this slice, would add the Moise runtime based only on the
upstream POM and guess an XML/API boundary. That would change the package and
license surface without a verified parser contract. Option B, selected, keeps
the existing plugin-owned `MasResourceReference` with `UNSUPPORTED` status and
emits `JCM-005` explaining that no verified Moise parser/API is packaged and the
referenced file is not parsed. Invalid/missing/duplicate `.jcm` agent cases
retain their existing positioned diagnostics. No organization IR, rules, or
runtime claims are added until official parser/source/license evidence and a
reviewed Auction organization fixture are available.

## 13. ADR-0033: Isolated Scoped Evaluation With Named State Fixtures

**Status:** Accepted. **Date:** 2026-08-11.

The reviewed Auction evidence needs a small populated USE snapshot for its OCL
and reference cases, while the existing headless service intentionally starts
with an empty private `MSystem`. The evaluation boundary therefore accepts an
optional plugin-owned `HeadlessStateFixture` descriptor. The current
`auction-populated` fixture uses verified USE `MSystem` APIs to create the
Auctioneer/Auction/Bidder objects, required links, and a draft status inside the
private system only. It is not a general USE command interpreter and cannot
start JaCaMo, CArtAgO, Moise, or any runtime.

`EvaluationRunner` validates the versioned manifest and external oracle before
execution, copies each case into a unique temporary workspace, runs the real
`HeadlessAnalysisService` with a bounded timeout, compares state fingerprints,
and cleans up in `finally`. Semantic results are scoped to declared rule IDs and
evidence tokens; unrelated raw diagnostics remain available as evidence but do
not silently become oracle failures. Process failures are represented as
`INVALID_INPUT`, `TIMEOUT`, or `EXECUTION_ERROR`, never as semantic PASS/FAIL.

Option A, rejected, would invoke guessed USE/JaCaMo runtime commands or embed a
new script language to create state. That would cross the read-only boundary and
make the evidence non-reproducible. Option B, selected, keeps a small named
fixture interface and explicit invalid result for unknown fixture IDs.

Mapping source IDs generated by Jason may include a checkout-absolute
`url(\"...\")` annotation in a plan label. `MappingSourceId` removes only this
transport annotation while retaining the stable source span/semantic label, so
the reviewed mapping fixtures remain portable when the checkout is copied.

The resulting JSON/CSV/HTML metrics are evidence for the five declared Auction
cases (`1 PASS + 4 DETECTED` in the fixed reviewed run), not statistical or
general correctness claims. Moise organization findings and live CArtAgO remain
excluded from this evaluation manifest according to ADR-0031 and ADR-0034.

## 14. ADR-0034: Static Moise Parser Adapter And Organization IR

**Status:** Accepted; supersedes ADR-0032. **Date:** 2026-08-11.

The ADR-0032 stop condition was rechecked rather than bypassed. The official
Moise repository tag `v1.1` verifies the LGPLv3 license, schema-valid Auction
fixture, and `moise.os.OS.loadOSFromURI(String)` entry point. The official
JaCaMo Maven repository now resolves `org.jacamo:moise:1.1`; its locally
calculated JAR SHA-256 is recorded in the Moise spike evidence because that raw
repository does not publish checksum sidecars.

Option A, rejected, is a replacement XML parser or direct DOM interpretation.
It would duplicate Moise syntax authority. Option B, selected, pins the official
artifact with all Maven transitives excluded, confines `moise.*` to
`MoiseOrganizationParserAdapter`, and converts parser objects immediately to
immutable `OrganizationModel`. The bounded IR contains roles, groups, goals,
missions, permission/obligation norms, and cardinalities with stable qualified
IDs and portable source identity.

The official model does not retain element line/column positions. Unknown
coordinates are therefore explicit and are not fabricated. Missing, invalid,
duplicate, and out-of-scope organization evidence uses `JCM-007..010`.
Workspace/institution references retain `JCM-005`.

The Moise artifact is monolithic, so its own classes are present in the shaded
plugin; this does not authorize runtime use. No organization entity, board,
workspace, enactment, dynamic membership, norm-fulfillment check, or launcher is
created. Moise-to-UML/OCL rules remain a separate T15 slice over plugin-owned IR.
ADR-0035 subsequently implements that static slice without changing this parser
boundary or adding runtime enactment.

## 15. ADR-0035: Confirmed Static Organization Mappings And Reviewed OCL Bounds

**Status:** Accepted. **Date:** 2026-08-11.

The T15 pilot needs explainable role/class, mission/operation, and
cardinality/OCL checks without allowing rules to depend on Moise objects or
claiming that string matching understands arbitrary OCL semantics.

Option A, rejected, is to infer mappings and cardinality semantics from names or
regular expressions over OCL source. That would turn suggestions into bindings
and create false PASS results. Option B, selected, introduces plugin-owned
candidate/confirmed mapping records. A cardinality mapping references one class
invariant and optionally carries minimum/maximum bounds normalized by a human
reviewer. `OrganizationConsistencyValidator` compares those reviewed bounds to
the immutable organization IR and checks that all referenced UML/OCL targets
exist.

`ORG-001..003` are a separate catalog and do not alter the 22 standard rule
IDs. Candidates and unavailable reviewed bounds are `UNKNOWN`. Even matching
static bounds emit `UNKNOWN` for enacted membership because no runtime evidence
exists. The organization trace contributor uses the common immutable graph
values with portable source identity and explicit mapping/target gaps.

This slice does not persist organization mappings, parse arbitrary OCL text,
launch a runtime, monitor membership, or evaluate norm fulfillment. Those are
separate future decisions.

## 16. ADR-0036: Renderer-Neutral Immutable Diagram Domain

**Status:** Accepted. **Date:** 2026-08-13.

The visualization initiative needs one stable presentation contract before a
snapshot projector, layout library, or Swing view is selected. Option A,
rejected, is to pass Jason, JaCaMo, CArtAgO, Moise, USE UML, or Swing graph
objects directly into a renderer. That couples visualization to parser/runtime
lifecycle and makes deterministic headless testing impractical. Option B,
selected, introduces final plugin-owned diagram records with typed nodes,
edges, groups, selection references, optional portable source evidence, and
issue markers.

Node, edge, and group IDs use versioned length framing over semantic references
and typed endpoints. Labels and checkout roots do not define identity.
`ProjectSourceId` v2 remains the source authority; qualified UML and existing
trace IDs can enter through a portable `DiagramSelectionRef`. Constructors
reject absolute path references, duplicate IDs, missing edge/group endpoints,
null values, and mutable collection leakage, then sort model content by ID.

This diagram is derived presentation data under BR-020. T17 adds no snapshot
projector, source parser, validation rule, persistence schema, graph library,
Swing view, navigation behavior, or export format. Those are later vertical
slices and must not turn the diagram into an editable or semantic source of
truth.
