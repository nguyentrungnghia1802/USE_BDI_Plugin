# Plugin Technical Design

Status: verified implementation contract
Verification: source-backed; see Git history and DocumentationContractTest

## 1. Baseline

| Area | Contract |
| --- | --- |
| Host | USE 7.1.1, Java 21, Maven reactor |
| Plugin | `org.tzi.use.plugins.bdi`, manifest version `0.1.0` |
| Parser/API | JaCaMo `1.3.0` for `.jcm`; Jason `3.3.0` for `.asl`; CArtAgO `3.1` artifact API |
| Menu | `Plugins > AgentSpeak` |
| Actions | `Hello BDI Plugin`, `Import AgentSpeak...` |
| UI | Swing `ViewFrame` registered by USE `MainWindow` |
| Storage | local versioned UTF-8 JSON; no database/network API |
| Evaluation | JUnit 5, package/GUI smoke, Auction fixtures |

The verified plugin lifecycle uses `IPlugin`, `IPluginActionDelegate`,
`IPluginAction.getSession()`, `Session.hasSystem()`, `Session.system()`, and
`MainWindow.addNewViewFrame(...)`. Do not invent descriptor-level view APIs.

## 2. Module And Package Structure

```text
use-bdi-plugin/
  src/main/java/org/tzi/use/plugins/bdi/
    application/  import and project composition
    importer/     Jason/JaCaMo adapters and normalization
    model/ir/     immutable parser-independent BDI model
    model/mas/    immutable portable JaCaMo project IR
    model/environment/ immutable CArtAgO-independent environment IR
    model/mapping/ explicit mapping domain
    index/        IR-derived indexes
    use/          read-only USE/OCL adapter
    validation/   rule SPI, catalog, orchestrator, suppressions
    persistence/  versioned JSON repositories
    problems/     issue presentation models
    report/       JSON/HTML serialization
    trace/        snapshot-derived explanatory graph
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
  agent, and resource links. Resources remain explicit `UNSUPPORTED` values.
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
`ENV-001..003`. It checks CArtAgO/UML operation existence, artifact-operation
arity, and observable-property/UML-attribute targets. Existing mapping schema
`0.2.0` is unchanged; pilot environment mappings are in-memory values pending a
dedicated persistence migration. Missing runtime property values are
`UNKNOWN`, not PASS. Environment findings can contribute typed artifact,
operation/property, UML, gap, and issue nodes to the traceability graph.

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
| `scripts/performance.ps1` | `PERFORMANCE_BENCHMARK_OK` |
| `scripts/clean-clone.ps1` | `CLEAN_CLONE_REPRODUCIBILITY_OK` |
| `scripts/backup-thesis-artifacts.ps1` | `THESIS_BACKUP_OK` plus manifest |

Fixtures are separated into valid, invalid, unsupported, golden, Smart Queue,
USE, and Auction/mutant groups. Tests do not require a network, database, or
credentials. Fixed timestamps and canonical sorting protect reproducibility.

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
classes. Persisting environment mappings, live state capture, and Moise each
require a dedicated decision beyond ADR-0028.

## 10. Definition Of Done

A behavior change needs focused tests, module tests, updated requirements/
architecture/checklist, an ADR for architectural changes, `git diff --check`,
and a coherent feature-branch commit. Analysis must not leave the active USE
state changed.
