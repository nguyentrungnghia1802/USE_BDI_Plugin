# Graphical Viewpoint Validation

Date: 2026-08-17

Task: 08 — Graphical Concrete Representation / Viewpoints in USE

Result: **PASS**

## Delivered contract

- [`GRAPHICAL_CONCRETE_SYNTAX.md`](../metamodel/GRAPHICAL_CONCRETE_SYNTAX.md)
  maps every closed node and edge type to metamodel, correspondence, trace,
  label, layer, overlay, and interaction semantics.
- [`GRAPHICAL_VIEWPOINTS.md`](../metamodel/GRAPHICAL_VIEWPOINTS.md) defines All,
  BDI Plan, Agent Overview, Mapping, Static MAS, focused issue/evidence, and
  layer-filter projections over one immutable `DiagramModel`.
- Existing metamodel, Auction architecture, traceability, user-guide, and
  screenshot-index evidence was updated rather than duplicated.
- `GraphicalViewpointContractTest` locks 25 `DiagramNodeType` values, 16
  `DiagramEdgeType` values, four mode enums, four layer enums, six visual
  states, one-based step metadata, portable SVG output, and the documented
  static/scope boundary.

## Exact boundaries preserved

- Static MAS is a separate `MasOverviewDiagramBuilder` source diagram, not an
  invented `DiagramViewMode`.
- Existing shared node types remain explicit: MAS agent instances use `AGENT`;
  schemes use `ORGANIZATION`; property/cardinality trace evidence uses
  `TRACE_ELEMENT`. No second graph model or unsupported node enum was added.
- Candidate organization mappings render as explicit gaps; core suggestions do
  not enter a diagram until confirmed.
- `CONFIRMED`, `POTENTIAL`, and `UNKNOWN` findings and missing/stale mappings
  use text badges and borders in addition to color.
- Static views state “No JaCaMo runtime | No Moise enactment | No live CArtAgO
  state”.
- FR-DIA-007 direct cross-tab source/mapping navigation remains the Task 03
  `RESIDUAL` decision. Existing selection-to-detail, Problems-to-Diagram, and
  bounded focus remain supported.
- Sirius/DSML4JaCaMo is prior-work positioning only; the implementation remains
  Java2D/Swing inside USE with no Eclipse/Sirius dependency.

## Gate results

The first 67-test focused run had 66 passing tests and one new contract failure
caused by an assertion spanning a Markdown line wrap. The assertion was made
wrap-insensitive and the complete focused suite was rerun.

```text
Focused diagram suite (final rerun):
Tests run: 67, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS

Full plugin reactor:
Tests run: 221, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS

Root clean verify: NOT REQUIRED
Reason: documentation plus a plugin-owned contract test only; no production
UI/diagram class or dependency changed.
```

Final raster capture is intentionally deferred to Task 12 after all UI-affecting
work is frozen. Task 08 evidence is deterministic SVG behavior plus automated
diagram and boundary tests; no fabricated screenshot was substituted.

Task 12 supersedes that deferral with the refreshed source-backed raster set
and capture metadata in [UI screenshot evidence](ui-screenshots.md).

## Final gate

```text
Graphical syntax spec: PASS
Viewpoint spec: PASS
Metamodel mapping: PASS (25/25 node types)
Correspondence mapping: PASS (16/16 edge types)
Static-only semantics: PASS
FR-DIA-007 decision: RESIDUAL (preserved)
Diagram tests: PASS (67/67)
Reactor: PASS (221/221 plugin tests)
Root verify if required: NOT REQUIRED
git diff --check: PASS
Open failures: 0
Result: PASS
```
