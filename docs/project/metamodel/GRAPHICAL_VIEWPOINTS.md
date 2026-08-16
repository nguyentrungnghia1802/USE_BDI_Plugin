# Graphical Viewpoints

Status: **NORMATIVE READ-ONLY VIEWPOINT CONTRACT**

All viewpoints are projections of one immutable `DiagramModel`. A viewpoint
does not parse, validate, evaluate OCL, confirm a mapping, mutate a model, or
persist diagram state.

## 1. Projection order

```text
source DiagramModel
  -> DiagramModeProjector
  -> DiagramNavigationProjector (hidden layers, then optional focus)
  -> deterministic layout
  -> BdiDiagramCanvas or DiagramSvgExporter
```

`Reset` returns to the original source model before presentation transforms.
Filters preserve retained node/edge IDs, labels, attributes, issue markers,
portable identities, and ordered-step metadata.

## 2. Built-in mode viewpoints

| Viewpoint | Implementation | Input | Includes | Excludes/boundary |
|---|---|---|---|---|
| All / combined | `DiagramViewMode.ALL` | current immutable source diagram | all nodes, edges, groups supplied by builders/contributor | no new semantics; layer/focus may subsequently hide content |
| BDI Plan | `DiagramViewMode.BDI_PLAN` | same model | agent/project, belief, goal, plan, trigger, context, action, message, gap, issue and edges whose endpoints remain | UML/OCL, organization, environment and generic trace nodes unless represented by an allowed retained endpoint; never reparses plan text |
| Agent Overview | `DiagramViewMode.AGENT_OVERVIEW` | same model | agent/project, beliefs, goals, plans, actions, messages, gaps, issues | trigger/context detail and non-BDI families; plan order attributes remain on retained steps |
| Mapping | `DiagramViewMode.MAPPING` | same model | endpoints of `MAPS_TO`/`MISSING_MAPPING`, plus their directly related `HAS_ISSUE` nodes | ownership/control flow not on those paths; candidates never become confirmed edges |

The enum contains exactly `ALL`, `BDI_PLAN`, `AGENT_OVERVIEW`, and `MAPPING`.
“Focused Issue” and “Static MAS” are supported viewpoints with different
implementation forms, not invented enum values.

## 3. Static MAS overview

`MasOverviewDiagramBuilder` constructs a separate read-only source diagram
from `MasProjectModel`, the frozen current-analysis snapshot, optional static
`EnvironmentModel`, and supplied confirmed/candidate organization/environment
mapping records.

It shows the project, agent instances and sources, resource declarations,
normalized organizations/roles/schemes/missions, artifacts/operations,
confirmed targets, and explicit gaps supported by current types. It does not
create dedicated nodes for every metaclass: group/cardinality/goal/norm and
observable-property detail appears only when supplied by trace evidence or in
labels/attributes supported by the current projection.

The view always exposes the static-only legend and never claims JaCaMo runtime,
Moise enactment, or live CArtAgO state.

## 4. Focused issue/evidence viewpoint

Problems-to-Diagram selects by stable rule ID. `DiagramHighlightPath` starts at
existing issue nodes and walks backward over only `HAS_ISSUE`, `MAPS_TO`,
`MISSING_MAPPING`, `CONSTRAINED_BY`, and `OWNS`. It highlights the explanatory
path without discovering a new relation or rerunning analysis.

Node focus is a bounded neighborhood plus existing issue paths (maximum issue
path depth eight). `Focus Agent` accepts an `AGENT`; `Focus Goal/Plan` accepts a
`GOAL` or `PLAN`. A focus not present after mode/layer filtering is cleared.

## 5. Layer-filter viewpoint

Layer toggles run after mode selection:

| Toggle | Exact layer/type coverage |
|---|---|
| Issues | `ISSUE` nodes or `layer=ISSUE` |
| UML/OCL | `layer=UML` plus UML class/object/attribute/operation, OCL constraint, trace target |
| Organization | `layer=ORGANIZATION` plus organization/role/mission |
| Environment | `layer=ENVIRONMENT` plus artifact/artifact operation |

An edge is visible only when both endpoints remain. Groups are rebuilt with
their retained members and disappear when empty. Hiding a layer changes no
semantic or persisted state.

## 6. Interaction matrix

| Interaction | Changes | Must not change |
|---|---|---|
| Zoom +/−, wheel zoom | canvas scale within `0.25..3.0` | diagram/evidence identity |
| Pan/scroll | viewport offset | layout/model |
| Fit | scale/offset after current layout | nodes, edges, order |
| Select | visual selection + detail callback | semantic source, mapping confirmation |
| Focus | bounded projected model | source model, rule results |
| View mode | retained typed elements | source model |
| Layer toggle | visible endpoints/groups | source model |
| Reset | presentation state back to full source | analysis snapshot |
| Export SVG | writes current filtered rendering | source model; no absolute semantic identity |

FR-DIA-007 remains **Partial / RESIDUAL**: direct cross-tab source/mapping
navigation is future work. Implemented diagram-to-detail selection,
Problems-to-Diagram highlighting, and bounded focus are the accepted scope.

## 7. Claim boundary

These viewpoints demonstrate a deterministic graphical representation derived
from the bounded metamodel, confirmed correspondences, and trace/finding
evidence. They do not demonstrate an editable DSML, automatic Ecore-to-editor
generation, bidirectional transformation, runtime monitoring, or semantic
proof from visual adjacency.
