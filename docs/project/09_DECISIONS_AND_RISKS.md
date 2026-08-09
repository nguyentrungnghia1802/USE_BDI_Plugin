# Decisions and Risks

Status: canonical decision index and active risk register
Verification: source-backed; see Git history and DocumentationContractTest

The full accepted decision text and evidence remain in
[DECISION_LOG.md](DECISION_LOG.md). This document provides current ownership,
open decisions, debt, and risks. It does not supersede ADR detail.

## 1. Decision policy

A material decision records status, context, decision, consequences, and
validation evidence. Accepted decisions are never silently reversed; a later
ADR must explicitly supersede them.

Changes to USE core, parser boundaries, source identity, persistence schema,
rule semantics, state mutation, report identity, plugin lifecycle, or release
architecture require ADR review.

## 2. Accepted ADR index

| ADR | Accepted decision |
| --- | --- |
| ADR-0001 | Use the verified USE plugin action lifecycle, session APIs, and `ViewFrame` boundary |
| ADR-0002 | Reuse Jason parser and shade required runtime dependencies |
| ADR-0003 | Represent syntax/import diagnostics explicitly |
| ADR-0004 | Preserve ordered multi-file results and failures |
| ADR-0005 | Keep prototypes under fixture/case-study boundaries |
| ADR-0006 | Preserve partial success across files |
| ADR-0007 | Extract source locations at the Jason adapter boundary |
| ADR-0008 | Record parser version metadata |
| ADR-0009 | Use metadata-only normalized `AgentModel` root |
| ADR-0010 | Normalize Jason AST into plugin-owned immutable IR |
| ADR-0011 | Build IR-only indexes and a Swing Explorer slice |
| ADR-0012 | Add Problems/re-import and read-only USE projection |
| ADR-0013 | Use conservative explicit mappings, suggestions, editor, and JSON |
| ADR-0014 | Use deterministic static rule phases and stale mapping policy |
| ADR-0015 | Preserve issue evidence in escaped HTML reports |
| ADR-0016 | Use canonical SHA-256 model/mapping report identities |
| ADR-0017 | Use versioned rule configuration and fail fast on unknown IDs |
| ADR-0018 | Use source-fingerprint suppressions and include them in reports |
| ADR-0019 | Return invalid-spec results only in integration-test mode; preserve normal CLI exit |
| ADR-0020 | Discover project configuration beside the active `.use` model and fail visibly on invalid input |

## 3. Open decisions

| ID | Decision needed | Trigger | Safe current behavior |
| --- | --- | --- | --- |
| OD-001 | Relative project-root source identity and migration | Before portable checked-in mappings/suppressions | Keep absolute identity and report staleness |
| OD-003 | Live GUI analysis report/export action | Before user-facing report acceptance | Use case-study/application composition |
| OD-004 | Closed unknown-field policy for mapping JSON | Before schema evolution | Validate required/current values; document leniency |
| OD-005 | USE snapshot refresh/subscription lifecycle | Before long-lived view consistency claim | Re-open/re-import view for a new projection |
| OD-006 | Complete external thesis artifact locations | Before release tag | Keep backup gate open and manifest missing inputs |
| OD-007 | House Building scope | Only if time/evaluation requires second corpus | Treat as optional |

Open decisions must not be guessed in implementation or marked complete from a
supporting class alone.

## 4. Technical debt ledger

| ID | Debt | Impact | Control/next action |
| --- | --- | --- | --- |
| TD-001 | Absolute source IDs in mappings, suppressions, reports | Checkout relocation causes staleness | Design project-root identity migration (OD-001) |
| TD-003 | No live GUI report export | User may confuse `ReportMain` with real analysis | Add export action composed from current snapshot (OD-003) |
| TD-004 | Mapping codec permits unknown fields | Typos may be ignored | Add closed-field validation or versioned extension policy |
| TD-005 | View captures a USE projection without host change subscription | Long-lived view may analyze an older snapshot | Add refresh/subscription contract or reopen guidance |
| TD-006 | Literal type inference is narrow | Variables/complex terms produce warnings/unknowns | Extend only with fixtures and conservative semantics |
| TD-007 | Mutation corpus has four labeled instances | Metrics do not generalize | Add independent models/mutants and blinded oracle review |
| TD-008 | Performance workload is a small Smart Queue fixture | Does not predict large model behavior | Add Auction/larger corpus benchmark |
| TD-009 | Release tag and full external backup are absent | Release cannot be called final | Resolve OD-006, rerun gates, tag |

## 5. Risk register

| ID | Risk | Likelihood | Impact | Controls | Status |
| --- | --- | --- | --- | --- | --- |
| R-001 | Unsupported AgentSpeak syntax is misrepresented | Medium | High | Jason authority, unsupported IR, `ASL-002`, golden fixtures | Controlled/residual |
| R-002 | Suggestion is mistaken for semantic proof | High | High | Explicit confirmation, evidence text, docs/UI wording | Open residual |
| R-003 | Wrong/moved source invalidates identity | High | Medium | fingerprints, stale checks, portability warning | Open (OD-001) |
| R-004 | Analysis corrupts current USE state | Low | Critical | read-only facade, variation/finally, fingerprint tests | Release-blocking if observed |
| R-005 | OCL information gap becomes false PASS | Medium | High | PASS/FAIL/UNKNOWN contract and evidence | Controlled/residual |
| R-006 | GUI displays stale USE projection | Medium | Medium | view/import lifecycle guidance | Open (OD-005) |
| R-007 | Rule/config docs drift from source registry | Medium | High | catalog, project-loader, and documentation contract tests | Controlled |
| R-008 | Shaded dependencies/license evidence incomplete | Low | High | package smoke, runtime tree, embedded notices | Controlled |
| R-009 | Report output is mistaken for live analysis | Medium | High | explicit `ReportMain` limitation | Open (OD-003) |
| R-010 | Mapping JSON typo is silently tolerated | Medium | Medium | required/domain validation and documented debt | Open (OD-004) |
| R-011 | Four-mutant metrics are overgeneralized | High | High | threats/limitations and scoped wording | Controlled/residual |
| R-012 | GUI smoke is skipped on headless CI | Medium | Medium | separate desktop smoke marker/manual acceptance | Open residual |
| R-013 | Java launcher uses Java 8 while Maven uses 21 | Medium | Medium | verify both commands, set launcher JDK | Open environment risk |
| R-014 | External thesis artifacts are omitted from backup | High | High | manifest missing paths and open release gate | Open (OD-006) |
| R-015 | Documentation drifts after agent instructions are untracked | Medium | High | stable verification marker, Git history, canonical sync tests, and PR discipline | Controlled/residual |

## 6. Risk acceptance rules

- `UNKNOWN` is an acceptable conservative result; silent loss is not.
- A state-corruption finding blocks release immediately.
- A failing/absent GUI smoke must be reported separately from headless tests.
- Metrics must name corpus size, oracle, and interpretation boundary.
- A release tag must not be created while required backup material is missing.
- Closing a risk requires source/test/evidence, not only prose.

## 7. Decision and risk synchronization checklist

- [x] Accepted ADRs are indexed through ADR-0019.
- [x] Open implementation gaps have decision IDs and safe defaults.
- [x] Technical debt distinguishes available libraries from live workflows.
- [x] Release, portability, state, evaluation, and environment risks are active.
- [x] No accepted ADR was silently superseded.
