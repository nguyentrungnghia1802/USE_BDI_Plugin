# System Architecture

Status: canonical architecture and safety boundary
Verification: source-backed; see Git history and DocumentationContractTest

## 1. Research Architecture

```mermaid
flowchart LR
  JCM[JaCaMo .jcm] --> JP[JaCaMo 1.3.0 adapter]
  JP --> MAS[Portable MAS project IR]
  MAS --> MO[Moise 1.1 static adapter]
  MO --> ORG[Immutable organization IR]
  JP --> ASL
  CARTAGO[CArtAgO artifact class] --> CA[CArtAgO 3.1 static adapter]
  CA --> ENV[Immutable environment IR]
  ASL[AgentSpeak .asl] --> JA[Jason 3.3.0 adapter]
  JA --> IR[Immutable normalized BDI IR]
  USE[USE UML/OCL model and snapshot] --> UA[Read-only USE adapter]
  IR --> MAP[Explicit mapping model]
  UA --> MAP
  IR --> VAL[Consistency orchestrator]
  MAP --> VAL
  UA --> VAL
  ENV --> EVAL[Environment pilot rules]
  UA --> EVAL
  VAL --> SNAP[Immutable current analysis snapshot]
  SNAP --> TRACE[Derived traceability graph]
  SNAP --> DIA[Renderer-neutral BDI diagram model]
  SNAP --> GUI[BDI Explorer and Problems]
  SNAP --> REP[JSON and HTML reports]
```

The plugin is a bridge between two authorities. Jason owns AgentSpeak syntax;
USE owns UML/OCL and snapshot semantics. Plugin-owned values connect them.

## 2. Layers And Dependency Rules

| Layer | Packages/components | May depend on |
| --- | --- | --- |
| Integration/UI | plugin actions, `ui` | application services and USE GUI boundary |
| Application | `application`, report composition | importer, index, mapping, validation abstractions |
| Adapters | `importer`, `use` | Jason/JaCaMo/CArtAgO/Moise or USE concrete APIs, respectively |
| Domain | `model.ir`, `model.mas`, `model.organization`, `model.mapping`, `diagram`, issue values | Java/plugin-owned values only |
| Analysis | `index`, `validation` | normalized IR, mappings, immutable USE projection |
| Persistence | `persistence`, report exporters | versioned plugin-owned DTOs |

Hard rules:

- Jason AST and exceptions stop in the importer/normalizer boundary.
- JaCaMo project/parser types stop in `JaCaMoProjectParserAdapter`.
- CArtAgO artifact/annotation types stop in `CArtAgOArtifactAdapter`.
- Moise parser/model types stop in `MoiseOrganizationParserAdapter`.
- Swing and USE concrete classes never enter normalized IR.
- Rule evaluation consumes normalized IR and immutable projections, not parser
  AST or mutable GUI state.
- Unsupported syntax creates explicit diagnostics/features; it is never
  silently discarded.
- USE core changes require a dedicated ADR. Plugin-first is the default.

## 3. Runtime Flow

1. The user opens a `.use` model in USE.
2. `ImportBdiAction` reads the current `Session`/`MSystem`.
3. `BdiProjectConfigurationLoader` discovers optional rule and suppression
   files beside the active model.
4. A background worker parses all selected `.asl` files independently.
5. The Jason adapter normalizes successful ASTs and retains failures as
   diagnostics.
6. `BdiIndexBuilder` derives signatures, references, support, and call sites.
7. `UseUmlModelFacade` creates a deterministic read-only UML/snapshot view.
8. Suggestions remain candidates until the user confirms mapping bindings.
9. `ValidationOrchestrator` runs enabled rules by phase and applies exact
   suppressions.
10. `CurrentAnalysisSnapshotService` validates once and freezes import, USE,
    mapping, config, suppressions, issues, hashes, counts, time, and versions.
11. Explorer/Problems consumes that snapshot; `CurrentAnalysisReportService`
    serializes the same object atomically as JSON or HTML without querying
    Swing, rerunning validation, or reading live USE state.
12. `HeadlessAnalysisService` compiles an isolated USE system from explicit
    files and composes the same snapshot/report services without Swing or a
    live USE session. It verifies its private state fingerprint before/after.
13. `ImportJaCaMoAction` and the Explorer `.jcm` button create the same
    immutable project request. `BdiProjectImportWorker` runs composition off
    the EDT and publishes only the current generation on the EDT.
14. `BdiQualityGateMain --jcm` validates the explicit project input, delegates
   to `MasProjectAnalysisService`, writes the existing JSON/HTML serializers,
   and prints sorted project diagnostics with the documented exit code.
15. `BdiDiagramBuilder` reads the exact frozen snapshot plus an explicit project
    root and projects BDI structure, index-derived goal support, confirmed UML
    mappings, and explicit gaps without parsing or validating again.

Asynchronous imports carry a generation token so an older completion cannot
replace a newer selection. Manual USE refresh resolves the current session
system again, captures and validates on the EDT, rejects stale queued requests,
and verifies the state fingerprint before and after analysis.

## 4. State Ownership And Safety

| State | Owner | Policy |
| --- | --- | --- |
| Jason AST | importer invocation | transient, never exposed |
| BDI IR/index | import snapshot | immutable |
| USE projection | adapter snapshot | immutable/read-only |
| Mappings/config/suppressions | plugin/user files | versioned and validated |
| Current analysis/Problems | application snapshot service | immutable, recomputed |
| Traceability graph | trace builder | immutable, derived per snapshot, never persisted |
| Diagram model | `diagram` package/caller | immutable, portable, renderer-neutral, validated before publication, never persisted |
| Reports | GUI or headless caller | atomic serialization of supplied evidence only |

OCL results are `PASS`, `FAIL`, or `UNKNOWN`; compile/evaluation errors cannot
be converted to success. Bounded `soil:` effects execute only inside a USE
variation, and cleanup occurs in `finally`. Tests compare fingerprints before
and after analysis.

## 5. Persistence And Project Context

There is no database, server, tenant, account, or network API. Optional files:

```text
<model-directory>/
  Model.use
  .bdimap.json
  .bdi-plugin/
    rules.json
    suppressions.json
```

The active `.use` parent is the project root. Missing config files use visible
defaults; malformed versions or unknown rule IDs abort Explorer creation with
an error. `ProjectSourceId` v2 provides a case-preserving relative path and
  source range under an explicit root and rejects outside-root sources. Schema
  v2 repositories migrate v1 mappings on save and retain v1 suppression hashes
  as explicitly legacy-only entries.

## 6. Packaging And Host Lifecycle

`use-bdi-plugin` is an in-repository Maven module. USE dependencies are
`provided`; Jason 3.3.0 and required runtime dependencies are shaded without
package relocation. The assembly places the plugin JAR under `lib/plugins`.

Actions implement `IPluginActionDelegate`, access the current session through
`IPluginAction`, and add `ViewFrame` through `MainWindow.addNewViewFrame(...)`.
Replacing the JAR requires restarting USE.

## 7. Current JaCaMo Boundary

`JaCaMoProjectParserAdapter` uses the official JaCaMo 1.3.0 parser and converts
its result immediately to plugin-owned values. `MasProjectImportService`
resolves sources under an explicit project root, delegates each unique source
to `BdiImportService`, and returns portable `MasProjectModel` plus the existing
AgentSpeak snapshot. Duplicate, missing, invalid, and outside-root sources are
diagnostic outcomes rather than silent omissions.

Workspace and institution declarations remain `MasResourceReference` values
with `UNSUPPORTED` status and `JCM-005`. A referenced organization file is
passed to the official Moise 1.1 `OS.loadOSFromURI` API and normalized by
`MoiseOrganizationParserAdapter` into immutable, portable
`OrganizationModel` values. Missing, invalid, duplicate, and out-of-scope
features use `JCM-007..010`; unavailable element coordinates remain explicit
unknown source spans. ADR-0034 supersedes the earlier ADR-0032 blocker.
`MasProjectAnalysisService` composes the importer result with the existing
`CurrentAnalysisSnapshotService`. It accepts immutable project/USE/mapping/
configuration inputs, runs the shared validator once, and returns one
`MasProjectAnalysisResult` containing the project IR, snapshot, and sorted
project diagnostics. Direct `.asl` analysis remains compatible because both
paths consume the same `BdiImportSnapshot` and snapshot boundary.
`ImportJaCaMoAction` registers the single-select `.jcm` chooser under
`Plugins > AgentSpeak`; `BdiExplorerView` exposes the same action as
`Import .jcm...`. Project analysis is asynchronous, project diagnostics are
shown in the Explorer tree/status, and export uses the snapshot already held
by the view. `BdiQualityGateMain --jcm` is the non-Swing equivalent and rejects
mixed `.asl`/`.jcm` inputs before writing reports.
Separately, `CArtAgOArtifactAdapter` can normalize a supplied artifact class's
official `@OPERATION` metadata and explicit property descriptors. The plugin
does not start a JaCaMo/CArtAgO runtime, model runtime Moise semantics, dynamically
inspect CArtAgO artifacts, or consume execution traces. The GUI/headless
entry points provide static `.jcm` composition only; they do not launch a
JaCaMo runtime.

The `diagram` package is a presentation-domain boundary, not a renderer or a
second analysis model. Its records contain only plugin-owned immutable values,
portable `ProjectSourceId` v2 evidence, qualified semantic selection
references, and issue enums. Length-framed node, edge, and group identities are
deterministic; constructors reject duplicate identities, unknown endpoints,
and checkout-absolute selection references. `BdiDiagramBuilder` now projects
the exact current snapshot into this contract. `TraceabilityDiagramContributor`
projects the existing immutable `TraceabilityGraph` into the same model for
source-to-mapping-to-UML/OCL-to-issue evidence. It does not invoke validation,
OCL evaluation, or snapshot creation; it maps existing trace relations,
preserves issue metadata, keeps explicit gap nodes, deduplicates equivalent
visual edges, and replaces non-portable labels. `BdiDiagramPanel` is a
read-only Swing presentation boundary over the combined immutable model. Its
custom Java2D canvas provides deterministic type-column layout, bounded zoom,
shift/middle-button pan, fit/reset, selection, and tooltips. Layout computation
runs in a `SwingWorker`; the canvas never edits the model or reruns analysis.
T21 adds `DiagramViewMode` and a renderer-side `DiagramModeProjector` for the
BDI Plan, Agent Overview, and confirmed Mapping views. A mode filters the
already-built immutable model, preserves `EXECUTES` step-order attributes,
keeps explicit mapping gaps, and retains a selected node when it remains
visible; it never reparses, revalidates, evaluates OCL, or mutates USE state.
Issue highlighting, navigation, export, and extensions beyond these modes
remain planned separate slices and must consume these records without
reparsing.

Static environment bindings are persisted separately from `.bdimap.json` in a
typed `.cartago-map.json` document. `EnvironmentMappingFileRepository` requires
an explicit project root, uses portable `ProjectSourceId` v2 provenance, emits
deterministic UTF-8 JSON, and rejects unknown fields, unsupported versions,
duplicate mapping identities, invalid roots, and malformed records. The
`EnvironmentMappingValidationService` revalidates confirmed operation/property
targets against the current plugin-owned environment and USE snapshots. Only
`CONFIRMED + CURRENT` mappings reach `EnvironmentConsistencyValidator`; a
candidate is excluded, while a stale or unknown confirmed mapping emits
`ENV-004` with source and target evidence.

The static organization pilot consumes only `OrganizationModel`, explicit
plugin-owned organization mappings, and `UseModelSnapshot`. `ORG-001` checks a
confirmed role/class target, `ORG-002` checks a confirmed mission/operation
target, and `ORG-003` compares a Moise role cardinality with reviewer-normalized
bounds attached to a confirmed OCL-invariant mapping. The validator does not
parse OCL text heuristically. Candidate mappings and missing reviewed bounds
are `UNKNOWN`; matching static bounds are also `UNKNOWN` for enacted membership
until runtime evidence exists. `OrganizationTraceabilityGraphBuilder`
contributes portable organization/source/mapping/UML/OCL/gap/issue nodes to the
same immutable graph contract without importing Moise types.

## 8. Scoped Evaluation Evidence

`EvaluationManifestCodec` loads a versioned, closed-field manifest whose cases
declare relative inputs, expected rule IDs/certainties, forbidden findings,
evidence tokens, timeout, and an optional named state fixture. The manifest is
the reviewed oracle; `EvaluationRunner` never derives expectations from its own
findings. It rejects duplicate IDs, unknown fields, unsafe paths, missing input,
ambiguous oracle entries, and unsupported schema versions before executing a
case.

Each case is copied to a unique temporary workspace and analyzed through the
real `HeadlessAnalysisService` in a daemon worker with a bounded timeout. The
only current fixture is `auction-populated`, which uses verified USE APIs to
create a small Auction snapshot inside the private `MSystem`; it does not start
JaCaMo, CArtAgO, Moise, or a runtime. The runner records a before/after USE
fingerprint and cleans the workspace in `finally`.

The runner reports semantic outcomes (`PASS`, `DETECTED`, `MISSED`,
`UNEXPECTED`, `UNKNOWN`, `UNSUPPORTED`) separately from invalid-input,
timeout, and execution-error outcomes. Rule matching is limited to the case's
declared scope and evidence tokens, so unrelated standard diagnostics are
retained in raw analysis but cannot change the reviewed corpus oracle. The
atomic JSON/CSV/HTML output includes manifest, corpus, configuration, case
input, and trace identities. Metrics therefore describe only the versioned
Auction corpus and are not a general correctness proof.

## 9. Traceability Boundary

`TraceabilityGraphBuilder` consumes only `CurrentAnalysisSnapshot` plus an
explicit project root. It derives portable source/BDI, confirmed mapping, UML,
OCL, gap, and issue nodes with deterministic evidence edges. Issue detail is a
backward traversal over the immutable graph. Raw Jason plan annotations are
excluded from IDs and serialized labels because they may contain absolute
source URLs. Issue nodes retain rule ID, severity, status, certainty, and
evidence as typed values. `TraceabilityDiagramContributor` consumes only this
graph and maps its edges to `OWNS`, `MAPS_TO`, `CONSTRAINED_BY`,
`MISSING_MAPPING`, and `HAS_ISSUE`; a missing mapping is a graph gap, not an
inferred UML edge. Diagram selection references remain portable and do not
expose absolute source URLs.

## 10. Known Architecture Gaps

- Strict unknown-field policy for mapping JSON.
- Automatic subscription when USE state changes; manual refresh is available.
- Automatic `.jcm` mapping resolution, live CArtAgO state, Moise
  enactment/monitoring, persisted organization mappings, and runtime integration.
