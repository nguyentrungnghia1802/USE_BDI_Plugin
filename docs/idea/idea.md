# JaCaMo Research Roadmap After T11-T16

Status: prioritized research candidates, not implemented requirements

## Current Baseline

T11-T16 are complete. The project supports Jason-based AgentSpeak import,
immutable BDI IR, explicit UML mappings, 22 standard consistency rules,
snapshot OCL evaluation, JSON/HTML evidence, shared GUI/headless `.jcm`
analysis, portable traceability, persisted CArtAgO mappings, bounded Moise IR,
static organization consistency, and a reproducible Auction evaluation runner.
It does not provide JaCaMo runtime lifecycle, live environment/organization
state, persisted organization mappings, or runtime trace conformance.

## Priority Summary

| Rank | Idea | Value | Feasibility | Decision |
| --- | --- | --- | --- | --- |
| 1 | Unified JaCaMo project analysis entry | Very high | High | Completed in T11-T12 |
| 2 | Persisted CArtAgO environment mappings | Very high | High | Completed in T13 |
| 3 | Static Moise organization consistency | Very high | Medium | Completed in T14-T15 |
| 4 | Reproducible mutation and evaluation runner | High | High | Completed in T16 |
| 5 | External runtime trace conformance sidecar | Very high | Medium-low | Later |
| 6 | Dependency-aware incremental reanalysis | High | Medium | Later |
| 7 | Automatic USE session change subscription | Medium | Medium | Later |
| 8 | House Building second evaluation corpus | High | Medium | Later |

## Idea 1 - Unified JaCaMo Project Analysis Entry

**Status:** implemented in T11-T12 through the shared project analysis service,
Explorer action, and `BdiQualityGateMain --jcm` entry point.

**Function:** let users select one `.jcm` file and run the complete static
pipeline across its AgentSpeak sources, current USE model/state, mappings,
rules, traceability, and reports.

**Verified outcome:** the `.jcm` importer now feeds the same immutable analysis
snapshot used by direct `.asl` input, without starting a runtime.

**Practical slice:** add an application service shared by GUI and CLI, preserve
partial-success diagnostics, expose resolved agent identities, and make report
identity project-relative and deterministic.

**Research value:** establishes a clear static JaCaMo project boundary and lets
the thesis compare direct AgentSpeak analysis with project-level composition.

**Constraints:** official JaCaMo parser remains authoritative; no launcher,
workspace, Moise, or runtime lifecycle is implied.

## Idea 2 - Persisted CArtAgO Environment Mappings

**Status:** implemented in T13 with a separate strict, portable
`.cartago-map.json` document and explicit stale/unknown revalidation.

**Function:** save confirmed artifact-operation/UML-operation and observable-
property/UML-attribute bindings in the versioned mapping document.

**Verified outcome:** reviewed environment decisions survive restart and
checkout relocation; candidate, stale, and unknown states remain explicit.

**Practical slice:** introduce a schema migration, typed mapping kinds, portable
source identity, strict validation, staleness detection, and deterministic
round trips. Existing BDI mappings must remain readable and unchanged.

**Research value:** makes environment consistency reproducible and auditable,
which is required before live state or larger case studies are credible.

**Constraints:** suggestions remain unconfirmed until user action; unknown or
ambiguous legacy data must fail explicitly rather than broaden a binding.

## Idea 3 - Static Moise Organization Consistency

**Status:** implemented in T14-T15 using the official Moise 1.1 parser boundary,
plugin-owned organization IR, `ORG-001..003`, and portable trace evidence.

**Function:** normalize Moise roles, groups, missions, goals, and permissions
into plugin-owned organization IR and compare selected elements with UML/OCL.

**Verified outcome:** the Auction organization layer is available for bounded
static role/class, mission/operation, and reviewed cardinality/OCL checks while
runtime enactment remains `UNKNOWN`.

**Practical slice:** first verify the official parser/API and package boundary;
then implement a narrow Auction organization fixture and rules for role/class,
mission/operation, and cardinality/invariant consistency.

**Research value:** supports cross-layer claims spanning agent, environment,
organization, and UML/OCL while retaining deterministic static evaluation.

**Constraints:** no custom XML/parser guesswork, no Moise runtime, and no rule
may depend on a Moise concrete type outside the adapter.

## Idea 4 - Reproducible Mutation And Evaluation Runner

**Status:** implemented in T16 with a reviewed manifest, isolated workspaces,
deterministic JSON/CSV/HTML output, and explicit semantic/process outcomes.

**Function:** declare controlled model/BDI/environment/organization mutants and
automatically execute the quality gate, collect findings, and compute scoped
detection metrics.

**Verified outcome:** one Auction baseline and four declared mutants run through
the real headless service and reproduce `1 PASS + 4 DETECTED` byte-stably.

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
