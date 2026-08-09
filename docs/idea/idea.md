# Development Ideas After The Static CArtAgO Pilot

Status: prioritized research candidates, not implemented requirements

## Current Baseline

The project already supports Jason-based AgentSpeak import, immutable BDI IR,
explicit UML mappings, 22 standard consistency rules, snapshot OCL evaluation,
JSON/HTML evidence, headless quality gates, static `.jcm` project import,
portable traceability, and a static CArtAgO/UML consistency pilot. It does not
yet provide one end-to-end `.jcm` analysis workflow, persisted CArtAgO mappings,
Moise semantics, or runtime trace conformance.

## Priority Summary

| Rank | Idea | Value | Feasibility | Decision |
| --- | --- | --- | --- | --- |
| 1 | Unified JaCaMo project analysis entry | Very high | High | Selected |
| 2 | Persisted CArtAgO environment mappings | Very high | High | Selected |
| 3 | Static Moise organization consistency | Very high | Medium | Selected |
| 4 | Reproducible mutation and evaluation runner | High | High | Selected |
| 5 | External runtime trace conformance sidecar | Very high | Medium-low | Later |
| 6 | Dependency-aware incremental reanalysis | High | Medium | Later |
| 7 | Automatic USE session change subscription | Medium | Medium | Later |
| 8 | House Building second evaluation corpus | High | Medium | Later |

## Idea 1 - Unified JaCaMo Project Analysis Entry

**Function:** let users select one `.jcm` file and run the complete static
pipeline across its AgentSpeak sources, current USE model/state, mappings,
rules, traceability, and reports.

**Why it matters:** the `.jcm` importer currently produces project IR, while the
main GUI and CLI still center on direct `.asl` inputs. Joining these paths turns
the existing JaCaMo work into a demonstrable user workflow without starting a
runtime.

**Practical slice:** add an application service shared by GUI and CLI, preserve
partial-success diagnostics, expose resolved agent identities, and make report
identity project-relative and deterministic.

**Research value:** establishes a clear static JaCaMo project boundary and lets
the thesis compare direct AgentSpeak analysis with project-level composition.

**Constraints:** official JaCaMo parser remains authoritative; no launcher,
workspace, Moise, or runtime lifecycle is implied.

## Idea 2 - Persisted CArtAgO Environment Mappings

**Function:** save confirmed artifact-operation/UML-operation and observable-
property/UML-attribute bindings in the versioned mapping document.

**Why it matters:** the CArtAgO pilot currently uses in-memory mappings, so
reviewed environment decisions cannot survive restart, relocation, or CI use.

**Practical slice:** introduce a schema migration, typed mapping kinds, portable
source identity, strict validation, staleness detection, and deterministic
round trips. Existing BDI mappings must remain readable and unchanged.

**Research value:** makes environment consistency reproducible and auditable,
which is required before live state or larger case studies are credible.

**Constraints:** suggestions remain unconfirmed until user action; unknown or
ambiguous legacy data must fail explicitly rather than broaden a binding.

## Idea 3 - Static Moise Organization Consistency

**Function:** normalize Moise roles, groups, missions, goals, and permissions
into plugin-owned organization IR and compare selected elements with UML/OCL.

**Why it matters:** AgentSpeak and CArtAgO cover agent behavior and environment,
but the organization layer of JaCaMo is still absent. A static adapter is the
lowest-risk way to broaden JaCaMo coverage.

**Practical slice:** first verify the official parser/API and package boundary;
then implement a narrow Auction organization fixture and rules for role/class,
mission/operation, and cardinality/invariant consistency.

**Research value:** supports cross-layer claims spanning agent, environment,
organization, and UML/OCL while retaining deterministic static evaluation.

**Constraints:** no custom XML/parser guesswork, no Moise runtime, and no rule
may depend on a Moise concrete type outside the adapter.

## Idea 4 - Reproducible Mutation And Evaluation Runner

**Function:** declare controlled model/BDI/environment/organization mutants and
automatically execute the quality gate, collect findings, and compute scoped
detection metrics.

**Why it matters:** individual Auction mutants exist, but running and comparing
them manually is slow and makes thesis evidence harder to reproduce.

**Practical slice:** use a manifest with expected rule/status/evidence, isolated
temporary inputs, deterministic JSON/CSV summaries, and explicit timeout or
tool-error outcomes.

**Research value:** provides repeatable precision/recall-style evidence and
clear trace links from each mutation operator to the rules it evaluates.

**Constraints:** generated metrics describe only the declared corpus; they must
never be presented as proof of general correctness.

## Idea 5 - External Runtime Trace Conformance Sidecar

**Function:** consume a versioned event stream exported by an independently
running JaCaMo application and compare observed actions/artifact changes with
static mappings and OCL expectations.

**Why it matters:** runtime evidence can reveal deviations that static models
cannot establish, especially for CArtAgO property values and action ordering.

**Practical slice:** define an append-only JSONL protocol, correlate portable
agent/artifact identities, and produce PASS/FAIL/UNKNOWN observations without
embedding JaCaMo lifecycle control in USE.

**Research value:** bridges design-time and runtime conformance while preserving
replayability of thesis experiments.

**Why later:** timestamp ordering, identity correlation, and incomplete traces
need an ADR and stable static mappings first.

## Idea 6 - Dependency-Aware Incremental Reanalysis

**Function:** recompute only affected imports, indexes, rules, traces, and report
sections when an `.asl`, `.jcm`, mapping, config, or USE snapshot changes.

**Why it matters:** full recomputation is acceptable for Auction but will make
larger projects and live Explorer refreshes less responsive.

**Practical slice:** derive an immutable dependency graph, content hashes, and a
cache keyed by project-relative identity and analysis configuration.

**Research value:** enables performance evaluation and explains exactly why a
finding was recomputed or reused.

**Why later:** correctness and invalidation evidence are more important than
speed; a stale cache must never suppress a diagnostic.

## Idea 7 - Automatic USE Session Change Subscription

**Function:** refresh Explorer analysis when the current USE model or system
state changes, while preserving the existing manual refresh fallback.

**Why it matters:** users can currently view stale data until they press
`Refresh USE Snapshot`, which weakens the live demonstration experience.

**Practical slice:** verify official USE event APIs, debounce events, cancel
stale workers, and display snapshot identity and refresh status on the EDT.

**Research value:** improves interactive validation without changing the
read-only analysis semantics.

**Why later:** host listener lifecycle and plugin unload behavior require
careful leak, race, and state-fingerprint testing.

## Idea 8 - House Building Second Evaluation Corpus

**Function:** import a second independent AgentSpeak/JaCaMo and UML/OCL corpus,
define reviewed mappings, and execute the existing consistency pipeline.

**Why it matters:** Auction validates the MVP but cannot demonstrate how much of
the approach transfers to a structurally different multi-agent system.

**Practical slice:** establish provenance and licensing, freeze a baseline,
define a small oracle and mutants, and record unsupported constructs explicitly.

**Research value:** strengthens external-validity discussion and identifies
which normalized IR or rules are accidentally Auction-specific.

**Why later:** it should follow project-level analysis and persisted environment
mappings so the second corpus exercises the intended workflow rather than a
temporary harness.
