# Project Context

Status: canonical current-state specification
Last verified: 2026-08-09
Code baseline: `c1b11b41`

## 1. Research problem

USE validates UML/OCL structure and snapshots, while Jason/AgentSpeak models
describe beliefs, goals, plans, contexts, and actions. Without an integration
layer, a researcher must manually compare two different representations and
cannot reproducibly answer questions such as:

- Does an AgentSpeak action map to an existing UML operation with a compatible
  owner, arity, and argument type?
- Does a BDI reference resolve to a known agent or USE object?
- Is an operation precondition true in the current USE snapshot?
- Can a bounded effect be simulated without corrupting the user's state?
- Which source line, UML element, and evidence support each finding?

The thesis contribution is a plugin-first, evidence-preserving bridge. It is
not a replacement for either USE or Jason.

## 2. Product vision

Extend USE with a desktop workflow that imports real AgentSpeak sources,
normalizes them into a parser-independent BDI model, lets the user confirm
cross-model mappings, evaluates deterministic consistency rules, and presents
traceable findings in the USE GUI and reproducible reports.

## 3. Users and stakeholders

| Actor | Primary need |
| --- | --- |
| Thesis researcher | Implement and evaluate a defensible USE-BDI integration |
| Modeler | Inspect BDI structure beside the current UML/OCL model and snapshot |
| Reviewer/supervisor | Reproduce rule outcomes, mutants, metrics, and limitations |
| Plugin maintainer | Extend parsers, mappings, rules, UI, and packaging safely |
| Presentation operator | Run a stable Auction demo with exact UI and script steps |

The application is a local desktop research tool. It has no tenant, user
account, server, database, or remote authorization subsystem.

## 4. Goals

| ID | Goal | Current evidence |
| --- | --- | --- |
| G-001 | Import valid and invalid `.asl` files through Jason | Parser/import tests and fixtures |
| G-002 | Preserve source spans and unsupported syntax | IR golden tests and `ASL-002` fixture |
| G-003 | Keep domain logic independent of Jason and USE concrete APIs | Package boundaries and adapter tests |
| G-004 | Support explicit, reviewable BDI-to-UML mappings | Mapping suggestions/editor/persistence tests |
| G-005 | Produce deterministic, evidence-rich consistency findings | 22-rule catalog and rule tests |
| G-006 | Evaluate snapshot OCL without corrupting current USE state | Snapshot/effect tests and ADRs |
| G-007 | Provide reproducible thesis evidence | Auction baseline, mutants, metrics, reports |
| G-008 | Package and run as a USE 7.1.1 plugin | Assembly, smoke, and clean-clone checks |

## 5. Non-goals

- Reimplementing the AgentSpeak grammar or Jason interpreter.
- Extending USE lexer/parser/AST for BDI syntax.
- Executing a complete multi-agent system inside USE.
- Proving semantic equivalence between arbitrary AgentSpeak and UML/OCL models.
- Supporting every AgentSpeak term, context, internal action, or communication
  semantic in the MVP.
- Performing unbounded state exploration or model checking.
- Mutating the user's current USE state as a side effect of analysis.
- Providing a network API, shared database, cloud service, or multi-user access.
- Treating mapping suggestions or mutation metrics as general correctness proof.

## 6. Inputs

Required for the complete interactive workflow:

- one USE `.use` UML/OCL specification;
- optionally a current USE object/link snapshot;
- one or more Jason-compatible `.asl` files;
- user-confirmed mapping bindings when semantic checks require them.

Optional project artifacts:

- `.bdimap.json` mapping document;
- `.bdi-plugin/rules.json` enabled-rule configuration;
- `.bdi-plugin/suppressions.json` suppression records;
- bounded `soil:` effect expressions attached to action-operation mappings.

## 7. Outputs

- normalized immutable AgentSpeak IR and derived BDI indexes;
- import diagnostics with source file, line, column, and parser version;
- explainable mapping suggestions and confirmed mapping documents;
- consistency issues with rule ID, severity, status, certainty, source span,
  UML reference, evidence, and suggested fix;
- JSON/HTML reports with version and hash metadata when supplied;
- reproducible Auction case-study and benchmark artifacts under `target/`.

## 8. Trust and safety model

1. Jason is the syntax authority for AgentSpeak; unsupported normalized forms
   remain visible diagnostics instead of being silently ignored.
2. USE is the UML/OCL and snapshot authority; plugin domain code consumes a
   read-only projection.
3. Suggestions are candidates. Only explicit bindings are mappings.
4. OCL results distinguish `PASS`, `FAIL`, and `UNKNOWN`.
5. Bounded state changes execute inside a disposable USE variation and are
   restored in `finally`.
6. Reports serialize supplied analysis results; they do not create truth by
   themselves.
7. Every research claim is scoped to its fixture, oracle, and evidence.

## 9. Technical baseline

| Area | Verified baseline |
| --- | --- |
| Host | USE `7.1.1` desktop application |
| Language/build | Java 21 and Maven reactor |
| Plugin | `use-bdi-plugin`, manifest version `0.1.0` |
| AgentSpeak parser | `io.github.jason-lang:jason-interpreter:3.3.0` |
| UI | Swing action delegates and USE `ViewFrame` |
| Persistence | Versioned UTF-8 JSON files; no database |
| Packaging | Shaded plugin JAR inside the USE assembly ZIP |
| Evaluation | JUnit 5, PowerShell smoke scripts, Auction mutation fixtures |

## 10. Current implementation status

| Capability | Status | Boundary |
| --- | --- | --- |
| Plugin lifecycle and menu actions | Implemented | Restart required after JAR replacement |
| Multi-file AgentSpeak import | Implemented | Per-file partial success |
| Normalized IR and BDI indexes | Implemented | Explicit unsupported nodes/features |
| USE model/snapshot projection | Implemented | Read-only adapter |
| Mapping suggestions/editor/save/load | Implemented | Source IDs remain checkout-specific |
| Static and snapshot consistency rules | Implemented | 22 configured rule IDs |
| Rule/suppression JSON repositories | Implemented | Versioned persistence and validation tests |
| Automatic GUI project configuration | Planned | GUI does not auto-load rule/suppression files |
| JSON/HTML report exporters | Implemented | Real reports are pipeline/test driven |
| Live GUI report export | Planned | `ReportMain` is metadata/demo output, not live analysis |
| Auction case study and four mutants | Implemented | Scoped evidence only |
| House Building import | Optional | Not required for MVP |
| Release tag | Planned | `v1.0.0-thesis-rc` not created |
| Complete thesis artifact backup | Partial | Source backup works; external data/slides are absent |

## 11. Success criteria

The MVP is technically successful when:

- valid/invalid/unsupported AgentSpeak fixtures produce deterministic results;
- all 22 documented rule IDs match the source registry;
- findings preserve source and evidence rather than only a message;
- snapshot/effect checks leave the original state fingerprint unchanged;
- the Auction baseline and mutants are reproducible without live services;
- the shaded plugin is present and loadable in an extracted USE distribution;
- root `mvn clean verify`, plugin tests, and package smoke pass;
- limitations and scoped metrics remain visible in thesis evidence.

These criteria do not claim complete AgentSpeak semantics or formal proof.

## 12. Documentation map

The authoritative map and precedence rules are in [the project documentation
index](README.md). Progress is tracked in the [completion
checklist](16_PROJECT_COMPLETION_CHECKLIST.md); architectural history is in the
[decision log](DECISION_LOG.md).

## 13. Context synchronization checklist

- [x] Versions match current Maven/plugin descriptors.
- [x] Implemented, Partial, Planned, and Optional are distinguished.
- [x] Current source/state safety boundaries are explicit.
- [x] Current release blockers match the completion checklist.
- [x] No network/database/multi-user capability is implied.
- [x] Evidence claims remain scoped to the Auction corpus.
