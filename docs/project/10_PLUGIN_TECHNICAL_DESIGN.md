# Plugin Technical Design

Status: verified implementation contract
Verification: source-backed; see Git history and DocumentationContractTest

## 1. Baseline

| Area | Contract |
| --- | --- |
| Host | USE 7.1.1, Java 21, Maven reactor |
| Plugin | `org.tzi.use.plugins.bdi`, manifest version `0.1.0` |
| Parser | `io.github.jason-lang:jason-interpreter:3.3.0` |
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
    importer/     Jason adapter and normalization
    model/ir/     immutable parser-independent BDI model
    model/mapping/ explicit mapping domain
    index/        IR-derived indexes
    use/          read-only USE/OCL adapter
    validation/   rule SPI, catalog, orchestrator, suppressions
    persistence/  versioned JSON repositories
    problems/     issue presentation models
    report/       JSON/HTML serialization
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

## 4. Mapping And Validation Contracts

Mapping schema `0.2.0` supports `AGENT_CLASS`, `AGENT_OBJECT`,
`ACTION_OPERATION`, `PARAMETER`, `RECEIVER_OBJECT`, and `BELIEF_ATTRIBUTE`.
`kind + source` is unique. Suggestions are deterministic and explainable but
never auto-confirmed.

Each `ConsistencyRule` has a stable ID and phase. The orchestrator validates
enabled IDs, sorts by phase/ID, evaluates immutable context, and applies exact
source-fingerprint suppressions. The authoritative 22-rule matrix is in
[the rule catalog](08_CONSISTENCY_RULE_CATALOG.md).

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

## 7. Build, Test, And Script Contracts

```powershell
mvn --batch-mode --no-transfer-progress -pl use-bdi-plugin -am test
mvn --batch-mode --no-transfer-progress -pl use-assembly -am package
mvn --batch-mode --no-transfer-progress clean verify
```

| Script | Bounded success marker |
| --- | --- |
| `scripts/smoke.ps1` | `GUI_SMOKE_OK` |
| `scripts/auction-evidence.ps1` | `AUCTION_EVIDENCE_OK` |
| `scripts/performance.ps1` | `PERFORMANCE_BENCHMARK_OK` |
| `scripts/clean-clone.ps1` | `CLEAN_CLONE_REPRODUCIBILITY_OK` |
| `scripts/backup-thesis-artifacts.ps1` | `THESIS_BACKUP_OK` plus manifest |

Fixtures are separated into valid, invalid, unsupported, golden, Smart Queue,
USE, and Auction/mutant groups. Tests do not require a network, database, or
credentials. Fixed timestamps and canonical sorting protect reproducibility.

## 8. Extension Rules

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

For a new JaCaMo layer, create a separate adapter and plugin-owned IR. Do not
make current rules depend directly on `.jcm`, CArtAgO, Moise, or runtime classes.

## 9. Definition Of Done

A behavior change needs focused tests, module tests, updated requirements/
architecture/checklist, an ADR for architectural changes, `git diff --check`,
and a coherent feature-branch commit. Analysis must not leave the active USE
state changed.
