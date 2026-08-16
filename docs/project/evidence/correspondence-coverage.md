# Cross-Model Correspondence Coverage Evaluation

Date: 2026-08-17

Profile: `JaCaMo Consistency Analysis Profile` `1.0.0`

The evaluated correspondence inventory is the closed set already specified in
[`CORRESPONDENCE_RULE_MATRIX.md`](../metamodel/CORRESPONDENCE_RULE_MATRIX.md):
six core BDI mappings, two environment mappings, and three organization
mappings. Counts describe implemented trace paths, not bidirectional
synchronization or runtime JaCaMo coverage.

## Coverage summary

| Family | Types | Model representation | Candidate path | Confirmed path | Persistence | Stale/missing/unknown | Rule trace | Diagram gap/path | Report issue evidence |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| Core BDI ↔ UML/OCL | 6 | 6 | 6 | 6 | 6 | 6 | 6 | 6 | 6 |
| Environment ↔ UML | 2 | 2 | 2 | 2 | 2 | 2 | 2 | 2 | 2 |
| Organization ↔ UML/OCL | 3 | 3 | 3 | 3 | 0 | 3 | 3 | 3 | 3 |
| **Total** | **11** | **11** | **11** | **11** | **8** | **11** | **11** | **11** | **11** |

Persistence is intentionally 8/11: organization correspondence values are
reviewer supplied and have no plugin repository. This is an explicit residual
boundary, not missing evidence hidden as PASS. Reports serialize the resulting
issues and correspondence count; they do not serialize a standalone detailed
mapping inventory. “Report issue evidence” therefore means a related finding
survives generic deterministic JSON/HTML serialization when emitted.

## Type-by-type evidence

| Correspondence | Model / confirmation | Candidate and confirmed tests | Stale/missing/UNKNOWN and rules | Persistence / trace / report |
|---|---|---|---|---|
| Agent ↔ class | `AGENT_CLASS`; suggestion separate from confirmed binding | `MappingSuggestionServiceTest`, `MappingEditorPanelTest`, Auction fixtures | `MappingStalenessDetectorTest`; `MAP-001`, `MAP-003`, `OWN-001` | `.bdimap.json`; Auction trace/diagram; generic issue report |
| Agent ↔ object | `AGENT_OBJECT`; explicit binding | suggestion/editor and Auction OCL fixtures | missing object/fingerprint paths; MAP/OWN/OCL/CTX rules | `.bdimap.json`; target/issue paths; generic issue report |
| External action ↔ operation | `ACTION_OPERATION`; explicit binding | suggestion/editor, Smart Queue/Auction mappings | missing operation/signature/effect paths; MAP/SIG/OWN/OCL | `.bdimap.json`; mapping/gap/OCL paths; mutant report evidence |
| Argument ↔ parameter | `PARAMETER`; explicit binding | mapping repository/editor tests | missing argument/parameter; `MAP-003` and signature evidence | `.bdimap.json`; mapping/target path; generic issue report |
| Receiver ↔ object | `RECEIVER_OBJECT`; explicit binding | suggestion/editor/message tests | missing receiver/object; `REF-001`, `MAP-003`, `MSG-001` | `.bdimap.json`; REF mutant gap path; mutant report evidence |
| Belief ↔ attribute | `BELIEF_ATTRIBUTE`; explicit binding | suggestion/repository/context tests | missing predicate/attribute; `REF-002`, `MAP-003`, `BEL-001`, `CTX-001` | `.bdimap.json`; belief/target path; generic issue report |
| Artifact operation ↔ operation | typed environment confirmation/currentness | environment repository and Auction environment tests | stale/unknown state; `ENV-001`, `ENV-002`, `ENV-004` | `.cartago-map.json`; environment mapping/gap trace; generic issue report |
| Observable property ↔ attribute | typed environment confirmation/currentness | environment repository and environment rule tests | no live values → UNKNOWN; `ENV-001`, `ENV-003`, `ENV-004` | `.cartago-map.json`; property/gap trace; generic issue report |
| Role ↔ class | reviewer `OrganizationRoleMapping` | candidate/confirmed `AuctionOrganizationConsistencyTest` | missing target and candidate UNKNOWN; `ORG-001` | deliberately not persisted; organization trace/diagram; generic issue report |
| Mission ↔ operation | reviewer `OrganizationMissionMapping` | candidate/confirmed organization tests | missing endpoint/candidate UNKNOWN; `ORG-002` | deliberately not persisted; mission trace/diagram; generic issue report |
| Role cardinality ↔ invariant | reviewer normalized bounds | candidate/confirmed/bounds organization tests | absent bounds/runtime → UNKNOWN, mismatch → FAIL; `ORG-003` | deliberately not persisted; OCL/cardinality trace; generic issue report |

## Explainability on the reviewed Auction mutants

`AuctionMutantDiagramTest` builds diagrams from the same manifest and real
headless snapshots used by evaluation. It proves that baseline has no scoped
mutant highlight and that each mutant keeps its reviewed certainty/token while
highlighting a bounded path:

| Mutant rule | Required highlighted evidence |
|---|---|
| `MAP-003` | source → element → mapping → target → issue |
| `SIG-001` | source → element → mapping → target → issue |
| `REF-001` | source → element → explicit gap → issue |
| `OCL-001` | source → element → mapping → target → OCL constraint → issue |

The test rejects unrelated issue nodes and checkout-absolute labels.
`AuctionTraceabilityGraphTest`, `TraceabilityDiagramContributorTest`, and
`DiagramHighlightPathTest` separately lock portable deterministic graphs,
environment/organization extensions, explicit gaps, certainty, and bounded
focus. No test infers a link absent from the frozen snapshot.

## Limitations

The inventory is intentionally small and closed. Candidate coverage proves
review/rejection semantics, not suggestion quality. Organization mappings lack
persistence. Generic reports carry issues rather than a complete mapping graph.
Trace/highlight correctness on four controlled mutants does not establish user
comprehension or usability.
