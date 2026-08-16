# Read-Only Graphical Concrete Syntax

Status: **NORMATIVE DERIVED REPRESENTATION PROFILE**

Version: 1.0 (2026-08-17)

This specification defines the plugin's **read-only graphical concrete
representation / graphical viewpoints**. It is not an editable DSML graphical
editor. `DiagramModel` is a deterministic, immutable projection of an already
frozen analysis snapshot; it never becomes the semantic source of AgentSpeak,
JaCaMo, UML/OCL, mappings, or findings.

Related contracts:

- [analysis metamodel](USE_JACAMO_ANALYSIS_METAMODEL.md);
- [cross-model correspondence](CROSS_MODEL_CORRESPONDENCE.md);
- [graphical viewpoints](GRAPHICAL_VIEWPOINTS.md);
- [traceability graph evidence](../evidence/traceability-graph.md).

## 1. Projection chain

```text
immutable analysis snapshot + confirmed correspondence + trace/finding evidence
                                  |
                    BdiDiagramBuilder / MasOverviewDiagramBuilder
                                  |
                    TraceabilityDiagramContributor (when supplied)
                                  v
                 one immutable typed DiagramModel
                                  |
              mode -> layer -> focus -> layout -> Swing/SVG
```

The renderer consumes only typed diagram nodes, edges, groups, selection refs,
portable source IDs, issue markers, and sorted attributes. It does not parse
AgentSpeak, invoke JaCaMo/CArtAgO/Moise, run validation/OCL, or discover a new
semantic relation.

## 2. Node and group concrete syntax

The “visual type” column names the exact `DiagramNodeType`. A repeated type is
intentional and is disambiguated by label, selection kind, layer, and
attributes; no unsupported node kind is invented.

| Metamodel/correspondence concept | Visual construct | Label | Container/layer | Edge | Status overlay | Interaction |
|---|---|---|---|---|---|---|
| `Agent` | `AGENT` node | portable source/file-derived agent label | agent group / BDI | outgoing ownership, belief, goal, message, mapping | issue or mapping target state via attached path | select/detail; focus agent |
| MAS `AgentInstance` | `AGENT` node | instance name + import-status suffix | Static MAS / BDI | `OWNS` from project and to source | status text attribute; static-only | select/detail; bounded focus if available |
| `Belief` / belief update | `BELIEF` node | rendered literal/update | agent group / BDI | `HAS_BELIEF`, `EXECUTES`, mapping/gap | issue/missing mapping badges | select/detail |
| `Goal` / `GoalCall` | `GOAL` node | rendered goal | agent group / BDI | `PURSUES_GOAL`, `EXECUTES`, `SUPPORTED_BY` | issue badge | select/detail; focus goal/plan |
| `Plan` | `PLAN` node | plan label/trigger-derived label | agent group / BDI | `OWNS`, trigger/context/ordered execution | issue badge | select/detail; focus goal/plan |
| `Trigger` | `TRIGGER` node | operator/type/term rendering | agent group / BDI | `TRIGGERED_BY` | issue badge through evidence | select/detail |
| `Context` / test / constraint | `CONTEXT` node | deterministic context rendering | agent group / BDI | `REQUIRES_CONTEXT` or ordered `EXECUTES` | issue badge | select/detail |
| `ExternalAction` / other action step | `ACTION` node | rendered action | agent group / BDI | ordered `EXECUTES`, mapping/gap | missing/stale/unknown mapping or issue | select/detail |
| `.send` `InternalAction` | `MESSAGE` node | rendered `.send` term | agent group / BDI | `SENDS_MESSAGE`, ordered `EXECUTES`, receiver mapping/gap | explicit missing/unknown receiver state | select/detail |
| `MasProject` | `MAS_PROJECT` node | project name and static status | Static MAS / MAS | `OWNS`, `USES_ARTIFACT` | static-only text | select/detail |
| static-only legend | `MAS_PROJECT` node | “Static JaCaMo project analysis…” boundary | Static MAS / MAS | `OWNS` from project | visible text, not color-only | presentation-only |
| project source/reference | `TRACE_SOURCE` node; resource declarations may use `ORGANIZATION` or `ARTIFACT` according to kind | portable path or resource name/status | BDI, Organization, or Environment | `OWNS` | explicit static/import status | select/detail where selection ref exists |
| `Organization` | `ORGANIZATION` node | organization ID | Static MAS / Organization | `OWNS` | `staticOnly=true` | select/detail; layer toggle |
| organization `Scheme`, unsupported organization evidence | `ORGANIZATION` node | scheme ID or unsupported subject | Static MAS / Organization | `OWNS` | static/unsupported label | select/detail; layer toggle |
| organization `Role` | `ROLE` node | qualified role ID | Static MAS / Organization | `OWNS`, mapping/gap | confirmation/missing state | select/detail; layer toggle |
| organization `Mission` | `MISSION` node | qualified mission ID | Static MAS / Organization | `OWNS`, mapping/gap | confirmation/missing state | select/detail; layer toggle |
| group/cardinality/goal/norm trace evidence | `TRACE_ELEMENT` when a trace graph supplies it; no dedicated Static-MAS node otherwise | trace label | Organization trace | trace-converted edges | certainty/status attributes | issue path/highlight |
| `Artifact` | `ARTIFACT` node | workspace/instance/type or declaration | Static MAS / Environment | `USES_ARTIFACT`, `HAS_OPERATION`, mapping/gap | static/missing state | select/detail; layer toggle |
| `ArtifactOperation` | `ARTIFACT_OPERATION` node | name/arity/signature | Static MAS / Environment | `HAS_OPERATION`, mapping/gap | static/mapping state | select/detail; layer toggle |
| `ObservableProperty` | `TRACE_ELEMENT` in trace contribution; property mapping in Static MAS anchors at artifact because no dedicated node type exists | trace/property label | Environment trace | mapping/evidence edges | current/stale/unknown from supplied evidence | issue path/highlight |
| UML class | `UML_CLASS` node | qualified class reference | UML/OCL | `MAPS_TO` | confirmed target or stale/unknown | select/detail; layer toggle |
| UML object | `UML_OBJECT` node | object reference | UML/OCL | `MAPS_TO` | snapshot-sensitive state | select/detail; layer toggle |
| UML attribute | `UML_ATTRIBUTE` node | qualified attribute reference | UML/OCL | `MAPS_TO` | target state | select/detail; layer toggle |
| UML operation | `UML_OPERATION` node | qualified operation signature | UML/OCL | `MAPS_TO` | target state | select/detail; layer toggle |
| OCL invariant/precondition | `OCL_CONSTRAINT` node | qualified constraint reference | UML/OCL | `CONSTRAINED_BY`, `MAPS_TO` for reviewed cardinality | certainty/target state | select/detail; layer toggle |
| missing/candidate/unresolved relation | `GAP` node | explicit missing/unknown description | source family layer | `MISSING_MAPPING` | `MISSING MAPPING` badge + dashed border | select/detail; never drawn as confirmed |
| consistency finding | `ISSUE` node with `DiagramIssueMarker` | rule ID/message label | Issues | incoming `HAS_ISSUE` | text badge for certainty | Problems-to-Diagram highlight by stable rule ID |
| trace source/element/mapping/target | `TRACE_SOURCE`, `TRACE_ELEMENT`, `TRACE_MAPPING`, `TRACE_TARGET` | portable trace label | trace-declared layer | converted trace relation | status/certainty attributes | focus/highlight only |

`DiagramGroup` is a visual container with deterministic ID and sorted unique
member IDs. Agent groups are ownership/navigation aids, not metamodel
containment objects and not persisted semantic state.

## 3. Exact edge vocabulary

Every row names one exact `DiagramEdgeType`.

| Edge type | Direction and meaning | Label/metadata contract |
|---|---|---|
| `OWNS` | project/agent/organization/scheme/instance → declared child/source/static legend | relation-specific label; optional `layer`/`staticOnly` |
| `HAS_BELIEF` | agent → initial belief | “initial-belief” |
| `PURSUES_GOAL` | agent → initial goal | “initial-goal” |
| `SUPPORTED_BY` | goal/call → matching supporting plan | support label only when exact indexed support exists |
| `TRIGGERED_BY` | plan → trigger | “trigger” |
| `REQUIRES_CONTEXT` | plan → optional context | “context” |
| `EXECUTES` | plan → plan step | visible one-based label and `order` attribute |
| `SENDS_MESSAGE` | agent → `.send` message step | message occurrence label |
| `MAPS_TO` | domain/trace/mapping node → confirmed UML/OCL target | `mappingStatus=CONFIRMED`; target state retained where available |
| `BELONGS_TO_ROLE` | typed vocabulary for a supplied role-membership projection | no new runtime membership is inferred by current builders |
| `PERFORMS_MISSION` | typed vocabulary for a supplied mission relation | no enactment/commitment is inferred by current builders |
| `USES_ARTIFACT` | static MAS/project → artifact declaration | environment layer + static-only metadata |
| `HAS_OPERATION` | artifact → statically described operation | signature label + environment layer |
| `CONSTRAINED_BY` | trace/domain evidence → selected OCL constraint | converted `EVALUATED_BY`; does not claim proof |
| `HAS_ISSUE` | semantic/evidence node → finding | preserves rule/certainty through issue marker/path |
| `MISSING_MAPPING` | source → explicit `GAP` | mapping kind/status; candidate organization relations use a gap rather than `MAPS_TO` |

No edge means arbitrary adjacency. Trace relations map exactly as follows:
`DECLARES→OWNS`; `MAPPED_BY|TARGETS|ENVIRONMENT_TARGET|ORGANIZATION_TARGET→MAPS_TO`;
`EVALUATED_BY→CONSTRAINED_BY`; `MISSING_TARGET|MISSING_MAPPING→MISSING_MAPPING`;
and `PRODUCES→HAS_ISSUE`.

### Ordered plan-step invariant

`BdiDiagramBuilder` converts the immutable zero-based list position to a
one-based `stepIndex`. Both the step node and `EXECUTES` edge store
`order=stepIndex`, and the edge label displays that number. Mode, layer, focus,
layout, canvas, and SVG export filter or render existing values; none
renumbers steps. A hidden step may create a visible gap in numbering, which is
correct evidence of filtering rather than a changed plan order.

## 4. Status and visual overlays

Color is supplementary. The canvas and SVG export use a textual badge, border
style, and fill/border palette derived from immutable node evidence:

| Semantic state | Display state/badge | Non-color distinction |
|---|---|---|
| no issue/mapping warning | `CLEAN` | no warning badge |
| issue certainty `CONFIRMED` | `CONFIRMED ISSUE` | text badge, solid border |
| issue certainty `POTENTIAL` | `POTENTIAL ISSUE` | text badge, dashed border |
| issue certainty `UNKNOWN` or unknown target/evidence | `UNKNOWN` | text badge, dashed border |
| mapping absent or `GAP` | `MISSING MAPPING` | text badge, dashed border + `MISSING_MAPPING` edge |
| confirmed target stale | `STALE MAPPING` | text badge, dashed border |
| confirmed/current | `CLEAN` plus `mappingStatus=CONFIRMED` and `targetState=CURRENT` attributes where available | `MAPS_TO` edge and mapping detail; it is not labeled “semantically proven” |

Candidate organization mappings are deliberately rendered as an explicit gap
and missing-mapping edge. Core `MappingSuggestion` candidates are not part of
the immutable analysis diagram at all until explicit confirmation.

## 5. Static-only boundary

The Static MAS projection carries the visible sentence:

> Static JaCaMo project analysis | No JaCaMo runtime | No Moise enactment | No live CArtAgO state

MAS, organization, and environment nodes also retain `staticOnly=true`.
Therefore the view does not assert lifecycle state, organization membership or
mission commitment, norm fulfilment, live artifact values, or runtime message
delivery.

## 6. Interaction and export invariants

- zoom, pan, fit, selection, focus, mode choice, and layer toggles are
  presentation-only;
- `Reset` restores `ALL`, every layer, no focus/highlight, and the full original
  `sourceModel`, then resets zoom/pan;
- selection notifies a detail listener and does not mutate semantic input;
- Problems-to-Diagram uses the stable rule ID in `DiagramIssueMarker` and walks
  only existing evidence edges;
- source and mapping details use `DiagramSelectionRef` plus optional portable
  `ProjectSourceId`; exported semantic identity never contains an absolute
  checkout path;
- SVG exports the current filtered model with the same labels, order metadata,
  badges, dash states, and deterministic layout semantics as the canvas.

Direct cross-tab source/mapping navigation under FR-DIA-007 is **RESIDUAL** by
the Task 03 scope decision. Existing Problems-to-Diagram, Diagram-to-detail,
portable selection state, and bounded agent/goal/plan focus remain supported;
this task does not broaden that UI scope.

## 7. Sirius/DSML4JaCaMo positioning

Sirius/DSML4JaCaMo is prior-work evidence that metamodel-derived graphical
viewpoints are an established approach. This thesis renderer remains a
Java2D/Swing component inside USE because it must reuse the current USE
analysis snapshot and interaction model. The concrete syntax need not be
generated automatically from Ecore, and the plugin introduces no Eclipse,
Sirius, or editable-DSML runtime dependency.
