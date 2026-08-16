# Cross-Model Correspondence Model

Status: **NORMATIVE STATIC CORRESPONDENCE PROFILE**

Version: 1.0 (2026-08-17)

Related specifications:

- [JaCaMo Consistency Analysis Profile](USE_JACAMO_ANALYSIS_METAMODEL.md)
- [Formalized static semantics](STATIC_SEMANTICS.md)
- [Correspondence/rule matrix](CORRESPONDENCE_RULE_MATRIX.md)
- [Java IR alignment](METAMODEL_TO_JAVA_ALIGNMENT.md)

## 1. Separation of concerns

The domain profile states what static JaCaMo/BDI elements exist. This
correspondence profile states which of those elements are related to which
read-only USE UML/OCL elements, with what review and evidence state. The rule
specification separately decides whether a model pair and its confirmed
correspondences produce a finding.

```text
domain element + correspondence + current USE projection
                         |
                         v
               consistency-rule input
```

A heuristic suggestion is not a semantic correspondence. A target that merely
exists is not proof that the relation is semantically correct. A static match
is not runtime behavior evidence.

## 2. Abstract correspondence contract

Each family realizes the following logical record without requiring one shared
Java superclass or persistence schema:

| Field | Multiplicity | Contract |
|---|---:|---|
| correspondence type | `1` | Closed family-specific kind; determines valid source/target shapes. |
| source metaclass/identity | `1` | Domain concept plus deterministic source key. Path-bearing BDI/environment identities are portable. |
| target USE element | `1` | Stable qualified class, object, operation, parameter, attribute, or invariant reference. |
| review state | `1` | `CANDIDATE` or `CONFIRMED`, realized exactly as described in section 3. |
| target-validation state | `1` logical | `CURRENT`, `STALE`, or `UNKNOWN`; representation differs by family. |
| provenance | `0..1` structured | Source identity/origin when the family persists structured provenance. Core BDI embeds it in source keys; organization records it as evidence. |
| expression/effect | `0..1` | Optional bounded expression metadata; never an inferred semantic proof. |
| evidence | `0..*` core, `1..*` environment/organization | Explainable heuristic/reviewer/static evidence. |
| consuming rules | `0..*` | Rules may use only bindings admitted by the family gate. |
| persistence owner | `1` logical | Core repository, environment repository, or caller/reviewer for non-persisted organization records. |

Correspondence multiplicity is many records per project. Core uniqueness is
`kind + source`, so one confirmed target exists for each core source/kind.
Environment and organization use deterministic family keys and reject duplicate
keys in their validation contexts/documents. None of these relations owns or
mutates the referenced UML/OCL element.

## 3. Review and staleness state model

### 3.1 Logical states

```text
heuristic generation -> CANDIDATE -> explicit reviewer action -> CONFIRMED
                                                              |
                                               target revalidation
                                                              v
                                              CURRENT | STALE | UNKNOWN
```

- `CANDIDATE`: explainable proposal only. It is never admitted to a semantic
  rule as though confirmed.
- `CONFIRMED`: explicit user/reviewer decision. Confirmation does not imply the
  target is current or the relation is correct forever.
- `CURRENT`: the supported static source/target checks succeeded against the
  current projection.
- `STALE`: a supported check found a changed/missing source or target.
- `UNKNOWN`: available evidence cannot establish currentness. It never becomes
  PASS.

### 3.2 Exact implementation mapping

| Family | Candidate realization | Confirmed realization | Current/stale/unknown realization |
|---|---|---|---|
| Core BDI | `MappingSuggestion`; score, reasons, optional expression | `MappingBinding`; every persisted binding is user-confirmed by contract | No status enum: empty `MappingStalenessDetector` findings means current for supported checks; `MappingStalenessReason` findings mean stale. Unavailable/unsupported proof remains unknown at the rule/evidence layer; it is not fabricated as a binding status. |
| Environment | persisted record with `EnvironmentMappingConfirmation.CANDIDATE` | same record with `CONFIRMED` | explicit `EnvironmentMappingStalenessStatus.CURRENT`, `STALE`, or `UNKNOWN` plus reasons |
| Organization | `OrganizationMappingConfirmation.CANDIDATE` | `CONFIRMED` | no stored staleness enum: validators check source/target presence; missing confirmed endpoints are confirmed errors, while candidate, unreviewed bounds, and absent runtime enactment yield UNKNOWN findings |

`MappingSuggestion.toBinding()` is a conversion primitive, not authorization.
The UI/workflow must call it only after explicit confirmation; suggestion
services never mutate a `MappingDocument` or USE state.

## 4. Core BDI ↔ UML families

All six kinds are owned by `MappingKind` and persisted in `.bdimap.json`.

| Correspondence | Source domain concept/key | Target USE element | Multiplicity and evidence | Principal consumers |
|---|---|---|---|---|
| `AGENT_CLASS` | `Agent`; `MappingSourceId.agent` | qualified UML class | agent `0..1` confirmed class binding; name/source reasons may form candidates | `MAP-001`, `MAP-003`, `OWN-001` |
| `AGENT_OBJECT` | `Agent`; same portable agent key | current UML object | agent `0..1` confirmed object binding; snapshot-sensitive | `MAP-001`, `MAP-003`, `OWN-001`, `OCL-001..002`, `CTX-001` |
| `ACTION_OPERATION` | `ExternalAction`; deterministic action call-site key | qualified UML operation signature including owner | action occurrence `0..1`; functor/name and arity are candidate evidence; optional `soil:` expression is bounded effect metadata | `MAP-002..003`, `SIG-001..003`, `OWN-001`, `OCL-001..004` |
| `PARAMETER` | action argument position | qualified operation parameter | argument position/kind `0..1`; optional expression such as `argument[0]`; receiver policy is not hidden in this relation | `MAP-003`; signature rules currently consume operation parameter order/types directly |
| `RECEIVER_OBJECT` | indexed invocation/message receiver | current UML object | receiver occurrence/name `0..1`; dynamic receivers cannot be proven by static names | `REF-001`, `MAP-003`, `MSG-001` |
| `BELIEF_ATTRIBUTE` | belief predicate signature `functor/arity` | qualified UML attribute | signature `0..1`; name compatibility is suggestion evidence only | `REF-002`, `MAP-003`, `BEL-001`, `CTX-001` |

Agent/object and receiver/object mappings are snapshot-sensitive: disappearance
of the object produces a target-missing staleness finding and `MAP-003` rather
than silently selecting another object. Action/operation matching checks owner,
arity, parameter order/type evidence in later rules; the correspondence alone
does not prove an executable effect. Missing or incompatible type evidence is
reported as failure/UNKNOWN according to the rule contract.

## 5. Environment ↔ UML families

Environment correspondence remains in the separate `.cartago-map.json` schema.

| Correspondence | Source | Target | Confirmation/currentness | Rules |
|---|---|---|---|---|
| operation mapping | BDI external-action signature plus static `ArtifactOperation` descriptor | UML operation | explicit candidate/confirmed plus current/stale/unknown; arities and parameter types retained | `ENV-001`, `ENV-002`, `ENV-004` |
| property mapping | belief signature plus static `ObservableProperty` descriptor | UML attribute | same state model; property type and optional runtime-value evidence remain distinct | `ENV-001`, `ENV-003`, `ENV-004` |

Only `confirmedCurrentMappings()` enter `EnvironmentConsistencyValidator`.
Confirmed stale/unknown records produce explicit `ENV-004` evidence. Candidates
are persisted review evidence but are not evaluated as bindings. Absence of
live observable values yields UNKNOWN where a dynamic claim would be required.

## 6. Organization ↔ UML/OCL families

Organization mappings are immutable reviewer-supplied records and currently
have no JSON repository.

| Correspondence | Source | Target | Review evidence | Rules |
|---|---|---|---|---|
| `OrganizationRoleMapping` | `organization::Role.qualifiedId` | UML class | non-empty evidence + explicit confirmation | `ORG-001` |
| `OrganizationMissionMapping` | `organization::Mission.qualifiedId` | UML operation | non-empty evidence + explicit confirmation | `ORG-002` |
| `OrganizationCardinalityMapping` | group/role cardinality | selected OCL invariant plus optional reviewer-normalized bounds | non-empty evidence; bounds are reviewed data, never parsed/inferred from arbitrary OCL text | `ORG-003` |

Candidates yield INFO/UNKNOWN and stop before target semantics. Confirmed
missing endpoints or unequal reviewed bounds are errors. Equal static bounds
still yield INFO/UNKNOWN because membership, enactment, commitment, and norm
fulfilment runtime evidence is unavailable.

## 7. Identity, provenance, and determinism

- `.bdimap.json` path-bearing source keys serialize as `ProjectSourceId` v2
  canonical IDs and resolve only against an explicit project root.
- `BELIEF_ATTRIBUTE` uses a project-independent predicate signature rather than
  a fabricated file path.
- Environment provenance stores `ProjectSourceId` v2 plus an origin string.
- Organization qualified IDs and source-span evidence provide deterministic
  static identity; they are not currently persisted by a plugin repository.
- Mapping keys and suggestion sorting are deterministic. Core JSON output is
  byte-stable for the same ordered `MappingDocument`; it intentionally
  preserves binding insertion order rather than claiming canonical sorting.
- Environment documents sort by mapping key before encoding. Trace graph
  builders sort/deduplicate their family-specific nodes and edges.
- Diagram node identity is a presentation concern and never replaces a mapping
  source or target ID.

## 8. Persistence compatibility

### 8.1 Core `.bdimap.json`

- current schema: `0.2.0`;
- exact root fields in normal output: `schemaVersion`,
  `bdiMetamodelVersion`, `useFingerprint`, `bindings`;
- exact binding output fields: `kind`, `source`, `target`, `expression`,
  `evidence`;
- decoder accepts legacy `0.1.0`, requires absolute legacy path-bearing source
  IDs, resolves them safely, and returns an in-memory `0.2.0` document;
- saving emits portable v2 source IDs;
- unknown schema versions and duplicate JSON keys are rejected;
- residual limitation: unlike the environment codec, the core decoder does
  **not** reject additional unknown root/binding fields. They are ignored by
  construction. This compatibility gap is documented, not silently described
  as strict validation, and changing it requires a schema/ADR decision.

### 8.2 Environment `.cartago-map.json`

- current schema: `0.1.0`;
- operation/property records remain typed and separate from six core kinds;
- all required fields and only the allowed fields are accepted;
- confirmation, structured provenance, staleness, and evidence are persisted;
- records are deterministically sorted and portable identities are validated
  under the project root.

### 8.3 Organization

No organization mapping repository/schema exists. Adding persistence or merging
it into either existing schema is outside this task and requires a scoped ADR.

## 9. Supported synchronization interpretation

In this project, “synchronization” means **consistency-preserving revalidation
of correspondences**:

1. load/retain explicit correspondences;
2. resolve portable source identities under the current project root;
3. project the current read-only USE model/state;
4. detect staleness and missing endpoints;
5. recompute consistency findings, trace edges, and diagrams.

It does not mean bidirectional or incremental model transformation, automatic
edit propagation between AgentSpeak and UML, automatic confirmation, runtime
JaCaMo/CArtAgO/Moise synchronization, or semantic inference from arbitrary OCL
text.

## 10. Safety invariants

1. A candidate never enters a rule requiring a confirmed binding.
2. A stale confirmed mapping emits explicit evidence; it is not treated as
   current.
3. UNKNOWN never becomes PASS.
4. Missing source/target endpoints remain explicit findings.
5. Relocation preserves path-bearing identity through `ProjectSourceId` v2.
6. Source and target keys are deterministic within their declared scope.
7. Suggestions never mutate correspondence documents or USE state.
8. Correspondence diagrams visually distinguish candidates from confirmed
   relations.
