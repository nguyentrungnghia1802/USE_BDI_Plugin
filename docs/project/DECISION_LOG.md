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

## 2. Cross-Cutting Safety Decisions

- Plugin-first; no USE parser/AST/core extension without a dedicated ADR.
- Jason remains syntax authority; no replacement AgentSpeak parser.
- Domain/rules consume normalized IR, never Jason AST or Swing state.
- Suggestions are not mappings until confirmed.
- OCL is PASS/FAIL/UNKNOWN and bounded effects restore state in `finally`.
- Generated reports and metrics are scoped evidence, not general proof.
- Auction is the required MVP corpus; House Building is optional.
- Current project is Jason-integrated, not full JaCaMo-integrated.

## 3. Open Decisions

| ID | Decision needed | Safe current behavior |
| --- | --- | --- |
| OD-003 | One-click serialization of the current GUI analysis | Resolved: Explorer exports the shared snapshot atomically as JSON/HTML |
| OD-004 | Closed unknown-field policy for mapping JSON | Validate required/current fields and document remaining leniency |
| OD-005 | Automatic USE state-change subscription lifecycle | Use `Refresh USE Snapshot`; stale queued refreshes are discarded |
| OD-006 | External thesis data/report/slides locations and release owner | Keep backup/tag gates open |
| OD-007 | Scope of JaCaMo integration (`.jcm`, CArtAgO, Moise, runtime traces) | Support Jason `.asl` only and make no full-JaCaMo claim |

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
| JaCaMo scope is overclaimed from Jason dependency | architecture/docs test and explicit boundary | open via OD-007 |
| External thesis artifacts are omitted | backup manifest and open release gate | open via OD-006 |

## 5. Current Validation Record

- Live-export focused tests: 13 pass.
- Plugin suite: 103 pass.
- Reactor `mvn -pl use-bdi-plugin -am test`: all four modules succeed.
- Package/parser/report/menu smoke succeeds and builds ZIP/TAR distributions.
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
