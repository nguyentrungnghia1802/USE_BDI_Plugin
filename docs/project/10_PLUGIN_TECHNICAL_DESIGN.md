# Plugin Technical Design

Status: verified implementation contract
Verification: source-backed; see Git history and DocumentationContractTest

## 1. Baseline

| Area | Contract |
| --- | --- |
| Host | USE 7.1.1, Java 21, Maven reactor |
| Plugin | `org.tzi.use.plugins.bdi`, manifest version `0.1.0` |
| Parser/API | JaCaMo `1.3.0` for `.jcm`; Jason `3.3.0` for `.asl`; CArtAgO `3.1`; Moise `1.1` for static organization XML |
| Menu | `Plugins > AgentSpeak` |
| Actions | `Hello BDI Plugin`, `Import AgentSpeak...` |
| UI | Swing `ViewFrame` registered by USE `MainWindow` |
| Storage | local versioned UTF-8 JSON; no database/network API |
| Evaluation | JUnit 5, package/GUI smoke, Auction fixtures |

The verified plugin lifecycle uses `IPlugin`, `IPluginActionDelegate`,
`IPluginAction.getSession()`, `Session.hasSystem()`, `Session.system()`, and
`MainWindow.addNewViewFrame(...)`. Do not invent descriptor-level view APIs.

Plugin-owned Swing file dialogs use `BdiFileChooserSupport`. On the preferred
Windows checkout they start at `D:\_CODE_BANK\Project_\vnu-sme-lab\use`; a
moved checkout falls back to the process working directory. This helper is
used by AgentSpeak/JaCaMo import, mapping load/save, and current-analysis
export. During plugin initialization, `BdiPlugin` also applies the same path
through USE's verified `Options.setLastDirectory(...)` API, so the core
specification chooser starts at the same location after the plugin loads.

## 2. Module And Package Structure

```text
use-bdi-plugin/
  src/main/java/org/tzi/use/plugins/bdi/
    application/  import and project composition
    importer/     Jason/JaCaMo adapters and normalization
    model/ir/     immutable parser-independent BDI model
    model/mas/    immutable portable JaCaMo project IR
    model/organization/ immutable Moise-independent organization IR
    model/environment/ immutable CArtAgO-independent environment IR
    model/mapping/ explicit mapping domain
    index/        IR-derived indexes
    use/          read-only USE/OCL adapter
    validation/   rule SPI, catalog, orchestrator, suppressions
    persistence/  versioned JSON repositories
    problems/     issue presentation models
    report/       JSON/HTML serialization
    trace/        snapshot-derived explanatory graph
    diagram/      immutable renderer-neutral visualization values
    ui/           Explorer, mapping, and Problems Swing views
```

## 3. Import And IR Contracts

- `AslImporter` attempts every validated source and preserves input order.
- Syntax failures use `ASL-001`; other import failures use `ASL-IMPORT-001`.
- Successful files produce immutable `AgentModel` trees and source spans.
- Unnormalized Jason constructs produce `ASL-002` and explicit unsupported
  nodes/features.
- `BdiImportService` combines per-file results, models, and one immutable
  `BdiIndex`; failed files do not erase successful files.
- Jason classes are package-boundary implementation details.
- `JaCaMoProjectParserAdapter` is the only production class that imports
  `jacamo.*`; it emits parser-independent descriptors.
- `MasProjectImportService` deduplicates instance names, imports each unique
  source through `BdiImportService`, and assigns `IMPORTED`, `INVALID`, or
  `MISSING` per instance.
- `MasProjectModel` uses `ProjectSourceId` for relocation-stable project,
  agent, resource, and organization links. Workspace/institution resources
  remain explicit `UNSUPPORTED` values.
- `MoiseOrganizationParserAdapter` is the only production class importing
  `moise.*`. It calls the official `OS.loadOSFromURI` entry point and
  immediately converts roles, groups, schemes, goals, missions, permission/
  obligation norms, and cardinalities into immutable `OrganizationModel`.
  The API does not expose line/column coordinates, so source spans use portable
  file identity with explicit unknown positions.
- Organization parse outcomes are `NORMALIZED`, `MISSING`, or `INVALID`.
  Unsupported role/group/goal/mission/norm details remain
  `UnsupportedFeature` plus `JCM-010`; they are never silently discarded.
- `MasProjectAnalysisRequest` is the immutable input boundary for one `.jcm`
  analysis. `MasProjectAnalysisService` delegates import to
  `MasProjectImportService`, then reuses `CurrentAnalysisSnapshotService`
  rather than duplicating validation/report composition. Its result keeps
  project diagnostics separate from BDI import diagnostics and sorts project
  diagnostics deterministically.
- `ImportJaCaMoAction` provides a single-select `.jcm` chooser under
  `Plugins > AgentSpeak`; `BdiExplorerView` exposes the same operation as
  `Import .jcm...`. `BdiProjectImportWorker` keeps composition off the EDT and
  applies only the current generation. Project diagnostics are visible in the
  tree and status text; the view exports the snapshot already displayed.
- `CArtAgOArtifactAdapter` reflects only official runtime-retained `@OPERATION`
  metadata. Observable properties use explicit descriptors because CArtAgO
  creates them imperatively through `defineObsProperty`; no Java parser or
  workspace runtime is invoked.

## 4. Mapping And Validation Contracts

Mapping schema `0.2.0` supports `AGENT_CLASS`, `AGENT_OBJECT`,
`ACTION_OPERATION`, `PARAMETER`, `RECEIVER_OBJECT`, and `BELIEF_ATTRIBUTE`.
`kind + source` is unique. Suggestions are deterministic and explainable but
never auto-confirmed.

Each `ConsistencyRule` has a stable ID and phase. The orchestrator validates
enabled IDs, sorts by phase/ID, evaluates immutable context, and applies exact
source-fingerprint suppressions. The authoritative 22-rule matrix is in
[the rule catalog](08_CONSISTENCY_RULE_CATALOG.md).

The optional environment pilot uses a separate immutable context and
`ENV-001..004`. It checks CArtAgO/UML operation existence, artifact-operation
arity, observable-property/UML-attribute targets, and stale/unknown confirmed
bindings. Existing mapping schema `0.2.0` is unchanged. Persisted environment
records use the separate `EnvironmentMappingDocument` schema `0.1.0`, typed
operation/property records, portable `ProjectSourceId` v2 provenance, explicit
confirmation, evidence, and staleness state. `EnvironmentMappingJsonCodec` is
strict about closed fields and deterministic field order; the repository
requires an explicit project root and cannot overwrite a destination after
validation failure. Missing runtime property values are `UNKNOWN`, not PASS.
Candidates do not enter rules. Only confirmed current records are converted to
the existing environment mappings; stale or unknown confirmed records produce
`ENV-004` with auditable evidence. Environment findings can contribute typed
artifact, operation/property, UML, gap, and issue nodes to the traceability
graph.

OCL checks preserve certainty:

- model/signature checks use immutable USE references;
- snapshot preconditions return explicit status/evidence;
- bounded effects require `soil:` and disposable state variation;
- missing evidence yields potential/unknown, never fabricated PASS.

`CurrentAnalysisSnapshotService` is the application composition boundary. It
invokes the configured validator exactly once and returns immutable import/USE/
mapping/config/suppression/issue evidence with caller-supplied time, derived
counts, ADR-0016 model/mapping hashes, and parser/plugin/USE/metamodel versions.
The constructor rejects count, hash, parser-version, metamodel, and OCL-without-
USE inconsistencies. Problems reads this aggregate; Swing controls, `MSystem`,
Jason AST, and mutable lists do not cross into it.

`TraceabilityGraphBuilder` derives typed nodes and edges from that immutable
snapshot. `ProjectSourceId` v2 identifies source and BDI elements, confirmed
bindings connect them to qualified UML/OCL targets, and missing bindings create
explicit gap nodes. `detailForIssue` returns the stable predecessor closure for
one issue. The debug JSON serializer intentionally excludes raw evidence text
and absolute source URLs; it is not a replacement analysis report or a
persistence format.

## 5. Persistence Contracts

`ProjectSourceId` v2 is the portable source value used by persistence. Its
canonical form is length-delimited, contains a normalized project-relative path
plus begin/end line and column, preserves case, and requires an explicit root.
Mapping and suppression repositories require an existing absolute project root;
they never infer one from the process working directory.

Current mapping and suppression schema: `0.2.0`. Rule configuration remains
`0.1.0`.

```json
{"schemaVersion":"0.1.0","enabledRules":["ASL-001","MAP-003"]}
```

```json
{"schemaVersion":"0.2.0","suppressions":[{"ruleId":"REF-001","identityVersion":"bdi-source-v2","sourceFingerprint":"<sha256>","sourceId":"<canonical-project-source-id>","reason":"reviewed"}]}
```

Loading schema `0.1.0` mappings converts absolute path-bearing bindings at the
repository boundary and writes deterministic v2 on the next save. A v1
suppression hash is irreversible, so migration preserves it as
`identityVersion: bdi-source-v1` with `sourceId: null`; it remains exact for the
original checkout and cannot suppress a relocated issue by accident.

Repositories reject malformed input, unsupported versions, invalid values, and
duplicates. Rule/suppression codecs reject unknown fields. The mapping decoder
does not reject every unknown object field; OD-004 keeps strict closed-schema
validation open. Generated reports contain supplied metadata, issues,
suppressions, and optional canonical model/mapping SHA-256 identities.

`CurrentAnalysisReportService` converts one `CurrentAnalysisSnapshot` to one
`ReportData` shared by JSON and HTML. GUI writes use a sibling temporary file
and move only a completed serialization into place. Existing targets require
explicit overwrite confirmation; exporters never parse, validate, or inspect
the current `MSystem`.

## 6. GUI Project Composition

`BdiProjectConfigurationLoader` resolves the parent of the active USE model
filename and loads:

```text
.bdi-plugin/rules.json
.bdi-plugin/suppressions.json
```

No current-working-directory fallback is allowed. Missing files select standard
rules/empty suppressions and display that origin. Invalid files are shown as an
error before `BdiExplorerView` opens.

`LiveUseSnapshotProvider` resolves the current `Session.system()` for each
manual refresh and returns a projection/evaluator pair from that same system.
`BdiExplorerView` runs refresh on the EDT, uses a generation token to discard
stale queued requests, reuses the existing BDI import, and compares USE state
fingerprints before/after validation. Refresh failures remain visible in status
text and cannot be converted into a successful analysis result.

## 7. Headless Quality Gate

`BdiQualityGateMain` accepts explicit `--use`, either repeatable `--asl` or one
`--jcm`, optional `--mapping`/`--rules`/`--suppressions`, and one-or-more
`--json`/`--html` outputs. It does not discover project files from the process
CWD and rejects mixed `.asl`/`.jcm` input before report creation. The `.jcm`
path delegates to `MasProjectAnalysisService`; direct `.asl` remains a
compatibility path. The timestamp defaults to `Instant.EPOCH` or is supplied
by `--timestamp` for byte-stable reports. Project diagnostics are printed in
sorted order with source paths and the result uses the same documented exit
codes as direct analysis.

The runner compiles a private `MSystem`, uses existing import/projection/
orchestrator/current-snapshot/report services, and compares state fingerprints.
Exit codes are 0 clean, 1 confirmed findings, 2 potential/unknown-only, 3
invalid input/config, and 4 infrastructure/output failure. A parser-invalid
`.asl` is valid analysis input: it produces `ASL-001`, a report, and exit 1.

## 8. Build, Test, And Script Contracts

```powershell
mvn --batch-mode --no-transfer-progress -pl use-bdi-plugin -am test
mvn --batch-mode --no-transfer-progress -pl use-assembly -am package
mvn --batch-mode --no-transfer-progress clean verify
```

| Script | Bounded success marker |
| --- | --- |
| `scripts/smoke.ps1` | `GUI_SMOKE_OK` |
| `scripts/headless-quality-gate.ps1` | `HEADLESS_QUALITY_GATE_OK` |
| `scripts/auction-evidence.ps1` | `AUCTION_EVIDENCE_OK` |
| `scripts/auction-evaluation.ps1` | `AUCTION_EVALUATION_OK` |
| `scripts/performance.ps1` | `PERFORMANCE_BENCHMARK_OK` |
| `scripts/clean-clone.ps1` | `CLEAN_CLONE_REPRODUCIBILITY_OK` |
| `scripts/backup-thesis-artifacts.ps1` | `THESIS_BACKUP_OK` plus manifest |

Fixtures are separated into valid, invalid, unsupported, golden, Smart Queue,
USE, and Auction/mutant groups. Tests do not require a network, database, or
credentials. Fixed timestamps and canonical sorting protect reproducibility.

### 8.1 Reviewed Evaluation Runner

The T16 evaluation slice is implemented by
`EvaluationManifestCodec`, `EvaluationRunner`, `EvaluationReportWriter`, and
`EvaluationRunnerMain`. The manifest at
`docs/project/evidence/auction-evaluation-manifest.json` is the versioned
reviewed oracle for the Auction corpus. It declares five cases: one clean
baseline and four controlled mutants for structural, signature, reference, and
OCL findings. Relative input paths and evidence anchors are resolved below the
explicit checkout root only.

The runner validates every case before execution, copies source and optional
mapping files to an isolated temporary directory, and invokes the real
`HeadlessAnalysisService`. A case may request a named `HeadlessStateFixture`; the
current `auction-populated` fixture creates only the objects/links required by
the reviewed snapshot and never starts a JaCaMo/CArtAgO/Moise runtime. The
runner compares USE state fingerprints, sanitizes temporary paths from evidence,
and removes the workspace in a `finally` block.

`EvaluationStatus` distinguishes semantic and process outcomes. `PASS` is a
case with no declared finding; `DETECTED` contains all required findings with
the declared certainty; `MISSED`, `UNEXPECTED`, and `UNKNOWN` are semantic
oracle deviations. `UNSUPPORTED`, `INVALID_INPUT`, `TIMEOUT`, and
`EXECUTION_ERROR` are not semantic detections. Evidence tokens scope matching
to the reviewed oracle without hiding out-of-scope raw diagnostics.

`EvaluationReportWriter` emits deterministic `evaluation-results.json`,
`evaluation-results.csv`, and `evaluation-results.html` with fixed timestamp
support and SHA-256 manifest/corpus/configuration/input identities. Run the
packaged check with:

```powershell
powershell -ExecutionPolicy Bypass -File .\use-bdi-plugin\scripts\auction-evaluation.ps1
```

The script builds the assembly, launches `EvaluationRunnerMain` from the
extracted shaded distribution, repeats the run, compares all three output
hashes, and ends with `AUCTION_EVALUATION_OK` only for the reviewed `1 PASS + 4
DETECTED` result. This evidence is bounded to the declared Auction cases.

## 9. Extension Rules

For a new AgentSpeak construct:

1. verify Jason 3.3.0 AST behavior in an importer test;
2. normalize it or emit explicit unsupported evidence;
3. update golden IR/index fixtures;
4. keep Jason types out of domain/rules.

For a new rule:

1. assign a stable catalog ID and phase;
2. implement against `ValidationContext` only;
3. test positive, negative, and unknown/unsupported evidence;
4. update rule catalog and traceability.

For a new JaCaMo layer, extend a separate adapter and plugin-owned IR. Current
standard rules must not depend directly on `.jcm`, CArtAgO, Moise, or runtime
classes. Static organization normalization is bounded by ADR-0034. The separate
`ORG-001..003` pilot consumes only confirmed plugin-owned mappings and immutable
snapshots under ADR-0035. It compares reviewer-normalized cardinality bounds
rather than guessing OCL semantics from expression text. Live CArtAgO state,
organization enactment/monitoring, persisted organization mappings, and runtime
traces remain separate changes and must preserve PASS/FAIL/UNKNOWN semantics.

For diagram visualization, `DiagramModel` owns only immutable nodes, edges,
groups, portable semantic selection references, optional `ProjectSourceId` v2
evidence, and issue markers. IDs use versioned length framing over node/edge
types and semantic references rather than labels or absolute paths. The model
rejects duplicate IDs, missing edge/group endpoints, null members, and mutable
collection leakage before sorting content by ID. No adapter, USE concrete,
runtime, AWT, or Swing type may cross this package boundary. ADR-0036 limits
this slice to the domain contract; builders and renderers are separate tasks.

`BdiDiagramBuilder.build(CurrentAnalysisSnapshot, Path)` is the T18 projection
boundary. It reads normalized agent models, reuses `BdiIndex` supporting-plan
relations and confirmed `MappingDocument` bindings, and emits one source group
per imported model. Initial beliefs/goals, plans, triggers, contexts, and typed
ordered steps retain portable source identity; `EXECUTES` edges carry one-based
order metadata. Agent, external-action, belief, and message-receiver mappings
become `MAPS_TO` edges only when confirmed. Missing required mappings become
typed `GAP` nodes, while absent candidate suggestions cannot enter the snapshot
or diagram. UML target state is read as `CURRENT`, `STALE`, or `UNKNOWN` from
the immutable USE projection. Parameter-level and OCL/issue evidence projection
is supplied by `TraceabilityDiagramContributor`, which accepts only the existing
`TraceabilityGraph`. The contributor maps trace node kinds to diagram node types,
preserves issue rule ID/severity/status/certainty/evidence, retains explicit
mapping gaps, deduplicates equivalent visual edges, and sanitizes non-portable
labels. It never reruns validation or OCL evaluation, reads live USE state, or
mutates the analysis snapshot.

The T20/T21 Swing boundary is `BdiDiagramPanel` plus its private Java2D canvas. It
uses no graph library or web server. A `SwingWorker` computes a deterministic
renderer-owned layout from sorted immutable nodes; the EDT only publishes the
result, paints, and handles bounded zoom, pan, fit/reset, selection, and
tooltips. Controls are presentation-only, and the panel accepts a new
`DiagramModel` rather than exposing mutable semantic state. T21 adds a
`DiagramViewMode` selector and `DiagramModeProjector`: BDI Plan filters to the
Goal/Plan/Trigger/Context/Action path while retaining ordered `EXECUTES`
attributes, Agent Overview keeps agent goals/plans/beliefs/messages/actions,
and Mapping keeps confirmed `MAPS_TO` relations, `MISSING_MAPPING` gaps, and
related issue evidence. Switching mode reuses the current model and preserves
selection when the node remains visible; it does not parse, validate, run OCL,
or change USE state. T22 adds `DiagramVisualStateResolver` and
`DiagramHighlightPath` as presentation-only consumers of node attributes,
issue markers, and evidence edges. Confirmed/potential/unknown issue states
and missing/stale mapping states use both a visible badge and a distinct
border treatment, while selection remains an independent overlay. Selecting a
Problems row focuses the matching rule ID in Diagram and selects its issue
node; node details expose source, status, mapping state, and evidence. A new
immutable projection reuses the active rule ID so mapping refresh never leaves
stale highlight state. UNKNOWN is explicitly labeled UNKNOWN, never PASS.
Navigation beyond this issue-to-evidence path, layout extensions beyond these
modes, and export remain future slices.

T23 adds `MasOverviewDiagramBuilder` as a static JaCaMo projection boundary.
For a project import it receives the normalized `MasProjectModel` and the
exact `CurrentAnalysisSnapshot`; optional `EnvironmentModel` and typed
organization/environment mapping lists allow available CArtAgO and UML/OCL
evidence to be shown without inventing a runtime model. The builder emits
stable `MAS_PROJECT`, `AGENT`, `TRACE_SOURCE`, `ORGANIZATION`, `ROLE`,
`MISSION`, `ARTIFACT`, `ARTIFACT_OPERATION`, UML/OCL, and `GAP` nodes plus
typed edges. Unsupported `.jcm` resources and missing mapping sources are
explicitly labeled. A `staticOnly` legend and per-node layer attributes make
the no-runtime boundary visible; `BdiDiagramCanvas` uses those attributes for
layer colors and tooltips. Repeated source references are included once in the
MAS group, while the immutable node and edge identities remain unchanged.
`MasOverviewDiagramBuilderTest` covers Auction projection, environment and
organization mapping edges, explicit missing-operation gaps, determinism, and
static-only metadata; `BdiExplorerViewTest` covers the actual `.jcm` UI path.

T24 adds `DiagramLayer` and `DiagramNavigationProjector` after
`DiagramModeProjector` in the presentation pipeline. The layer projector
removes only nodes classified as Issues, UML/OCL, Organization, or Environment
and then drops edges/groups whose endpoints are no longer visible. Optional
focus keeps the selected node, all directly incident predecessors/successors,
and shortest existing issue-evidence paths with a fixed depth bound of eight.
It never changes `BdiDiagramPanel.sourceModelForTest()` or any analysis value.
The panel owns only stable selection/focus IDs and toggle state. Reset clears
focus/highlight, restores `ALL` and all layers, and republishes the original
source model. Organization and Environment toggles are disabled when their
layers are absent, which is the expected direct `.asl` behavior. Focus requests
fit the completed asynchronous layout for presentation. Unit tests cover layer
filtering, bounded focus, hidden/missing focus, reset, and source immutability;
Explorer tests cover both direct `.asl` and Auction `.jcm` control behavior.

T25 treats `use-bdi-plugin/demo` as executable presentation input.
`CanonicalDemoDiagramTest` resolves each bundle from the repository, loads its
portable `.bdimap.json`, and calls the same `BdiDiagramBuilder`,
`MasOverviewDiagramBuilder`, and `TraceabilityDiagramContributor` boundaries
used by Explorer. Family asserts a compact goal-plan-action-operation path;
Smart Queue asserts decision context plus its confirmed manager operation;
Smart Home asserts separate BDI and static environment layers; Auction asserts
bounded agent/organization/environment plus BDI, UML/OCL, and issue content.
`DocumentationContractTest` requires every canonical walkthrough to document
the Diagram tab and Fit control. These tests do not launch Jason or JaCaMo.

T26 reuses `auction-evaluation-manifest.json` as the reviewed visual oracle.
`AuctionMutantDiagramTest` copies each declared case into an isolated temporary
workspace, invokes `HeadlessAnalysisService` with its static state fixture,
builds `TraceabilityGraph`, and projects it through
`TraceabilityDiagramContributor`. Highlight traversal follows only incoming
`OWNS`, `MAPS_TO`, `MISSING_MAPPING`, `CONSTRAINED_BY`, and `HAS_ISSUE` edges.
This retains the source-to-issue chain while preventing a shared source node
from fabricating sibling issue highlights. Scoped baseline assertions use the
same reviewed evidence tokens as evaluation; no validation or rendering rule
is specialized for a mutant ID.

T27 implements `DiagramSvgExporter` without a new dependency. It accepts only
the current visible `DiagramModel`, highlight IDs, and optional selected node;
therefore hidden layers, view modes, and focus are reflected exactly while the
unfiltered `sourceModel` is not exported. It reuses `BdiDiagramLayout` and
`DiagramPalette`, emits deterministic UTF-8 XML with escaped labels and a
static arrow marker, replaces labels containing absolute paths/URLs, and writes
through a temporary sibling followed by an atomic move when supported.
`BdiDiagramPanel` owns the save chooser and overwrite confirmation. Empty
models and unconfirmed overwrite fail without changing analysis state.

T28 adds `VisualizationBoundaryContractTest` as a release architecture guard.
It scans production imports so projection/rendering cannot call Jason/JaCaMo,
the AgentSpeak importer, USE UML runtime types, validators, or the OCL evaluator,
and validation cannot depend back on diagram/UI. Immutable issue status and
certainty values may cross into presentation. `RuleCatalogCompletenessTest` continues to
lock the exact 22 standard IDs plus the separate `ENV-001..004` and
`ORG-001..003` catalogs. Behavioral confidence comes from the existing focused
tests and the module, root, package smoke, Auction evidence, and deterministic
evaluation gates rather than a duplicate visualization validator.

The post-T28 Explorer usability hardening keeps the same immutable presentation
boundary. Toolbars use explicit rows rather than relying on `FlowLayout` wrapping,
tabs scroll when the host view is narrow, Mapping uses a vertical split, and
Problems exposes a visible filtered/total count. Compact diagrams auto-fit only
after their asynchronous layout is published; larger diagrams remain scrollable,
and Fit/Reset clear stale viewport positions. Import state is mode-specific: a
successful direct `.asl` import owns re-import tracking, while a successful `.jcm`
import clears it. Failed imports retain the previous analysis, and mapping edits
before the first successful import cannot manufacture an empty exportable analysis.
These changes remain UI/application orchestration only and do not alter parser,
IR, validation, OCL, or current USE state.

## 10. Definition Of Done

A behavior change needs focused tests, module tests, updated requirements/
architecture/checklist, an ADR for architectural changes, `git diff --check`,
and a coherent feature-branch commit. Analysis must not leave the active USE
state changed.
