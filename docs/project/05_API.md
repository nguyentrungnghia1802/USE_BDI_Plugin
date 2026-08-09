# Plugin and Internal API Contracts

Status: canonical integration contract; no HTTP API is exposed
Last verified: 2026-08-09
Code baseline: `c1b11b41`

## 1. Contract sources

This project has no REST, RPC, socket, or browser API. Its executable contracts
are Java interfaces/records, the USE plugin descriptor, JSON codecs, and
command-line/script entry points. Source and tests override prose if they
disagree.

## 2. USE plugin contract

Descriptor: `use-bdi-plugin/src/main/resources/useplugin.xml`.

| Contract | Value |
| --- | --- |
| Plugin ID | `org.tzi.use.plugins.bdi` |
| Plugin name/version | `USE BDI Plugin` / `0.1.0` |
| Host declaration | USE `7.1.1` processing instruction |
| Action | `Hello BDI Plugin` |
| Action | `Import AgentSpeak...` |
| Menu | `Plugins > AgentSpeak` |

Action delegates implement USE's `IPluginActionDelegate`. They receive an
`IPluginAction`, use `getParent()` for the window and `getSession()` for current
state, and add custom content through `ViewFrame` plus
`MainWindow.addNewViewFrame(...)`.

There is no verified descriptor-level custom-view extension or hot reload.

## 3. Import contracts

### `AslImporter`

Accepts a source `Path` and returns an `AslImportResult`. Callers must expect a
structured failure result for source/parser errors rather than assuming every
failure throws.

### `BdiImportService`

Accepts an ordered list of source paths and returns `BdiImportSnapshot`:

- per-file import results;
- materialized successful `AgentModel` values;
- combined immutable `BdiIndex`.

Multi-file order is stable. Failed files do not erase successful models.

### Diagnostics

`AslDiagnostic` carries code, severity, source, message, and one-based
line/column when available. `0/0` represents unavailable source position.

## 4. Normalized IR contract

The `model/ir` package is the only supported input to index/rule code. It
exposes immutable records/interfaces for agent, belief, goal, plan, trigger,
context, plan-step, term, span, and unsupported feature models.

Consumers must not cast back to Jason AST or infer unsupported semantics from
display text. New Jason constructs require adapter normalization, explicit
unsupported representation, fixtures, and golden/test updates.

## 5. USE adapter contracts

### `UseUmlModelFacade`

Projects `MSystem` into `UseModelSnapshot`, including model structure,
constraints, objects, links, and values. The returned data is plugin-owned and
immutable.

### `UseOclEvaluator`

Compiles and evaluates a supplied expression through USE and returns
`EVALUATED`, `COMPILE_ERROR`, or `EVALUATION_ERROR` with diagnostics.

### `SnapshotOclEvaluator`

The validation-facing abstraction evaluates operation preconditions and
bounded effects, returning explicit status and evidence. Rules depend on this
abstraction, not `MSystem`.

## 6. Mapping contracts

| Type/service | Contract |
| --- | --- |
| `MappingDocument` | Immutable schema root; unique `kind + source` bindings |
| `MappingBinding` | Kind, source, target, optional expression, evidence |
| `MappingSuggestionService` | Deterministic candidates; no automatic confirmation |
| `MappingFileRepository` | UTF-8 save/load around the current schema codec |
| `MappingStalenessDetector` | Missing source/target and fingerprint review signals |
| `UseModelFingerprint` | Canonical SHA-256 of immutable USE projection |
| `MappingFingerprint` | Canonical SHA-256 of mapping content |

Repository methods may throw `IOException` for file access and
`IllegalArgumentException` for invalid schema/domain values. A failed load must
not partially modify the UI document.

## 7. Validation contracts

### `ConsistencyRule`

Each rule has a stable ID, phase, and pure evaluation over
`ValidationContext`. It returns immutable issues.

### `ValidationOrchestrator`

- validates configured IDs against supplied rules;
- orders rules deterministically by phase and ID;
- evaluates only enabled rules;
- optionally applies suppressions after evaluation;
- never changes input models/mappings.

### `ConsistencyIssue`

Required semantics include rule ID, severity, status, message, evidence, and
certainty. Source span, agent/plan ID, UML reference, and suggested fix are
optional only when the underlying evidence is unavailable.

## 8. Report contracts

`ReportExporter.exportJson(ReportData, Path)` and
`HtmlReportExporter.exportHtml(ReportData, Path)` create UTF-8 output. They
serialize the supplied counts/issues/suppressions; callers must provide a
coherent `ReportData`. Exporters do not query the current USE session or run
validation.

`ReportMain` demonstrates file generation with zero live issues/mappings. The
Auction baseline test is the verified complete analysis-to-report composition.

## 9. Script and command contracts

| Entry point | Success marker/output |
| --- | --- |
| `scripts/smoke.ps1` | `GUI_SMOKE_OK` after package/parser/report/menu probes |
| `scripts/auction-evidence.ps1` | `AUCTION_EVIDENCE_OK` |
| `scripts/performance.ps1` | `PERFORMANCE_BENCHMARK_OK` |
| `scripts/clean-clone.ps1` | `CLEAN_CLONE_REPRODUCIBILITY_OK` |
| `scripts/backup-thesis-artifacts.ps1` | `THESIS_BACKUP_OK` plus manifest |

Markers only prove the checks implemented by that script. For example,
`THESIS_BACKUP_OK` does not prove absent external slides were recovered.

## 10. Compatibility rules

- Public plugin contracts target USE 7.1.1 and Java 21.
- Jason behavior is pinned to 3.3.0.
- JSON contracts are versioned independently at 0.1.0.
- Changing rule IDs, mapping kinds, source identity, report fields, menu labels,
  or plugin IDs is a contract change requiring migration/test/doc review.
- Adding an HTTP API requires a new architecture decision; none exists today.

## 11. API synchronization checklist

- [x] Descriptor IDs, labels, and versions match `useplugin.xml`.
- [x] Java boundaries match current source packages.
- [x] Report and JSON behavior is not overclaimed.
- [x] Script success markers match current scripts/docs.
- [x] Absence of HTTP/network API is explicit.
