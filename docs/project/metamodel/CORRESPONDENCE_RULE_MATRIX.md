# Correspondence-to-Rule Matrix

Status: **NORMATIVE TRACEABILITY MATRIX**

The matrix preserves the three existing correspondence owners. “Persisted?”
means persisted by the plugin itself; it does not imply that a candidate is a
confirmed semantic binding.

| Correspondence | Existing mapping kind/type | Persisted? | Rules | Suggestion source | Confirmation required | Staleness |
|---|---|---:|---|---|---|---|
| Agent ↔ class | `MappingKind.AGENT_CLASS` / `MappingBinding` | yes, `.bdimap.json` 0.2.0 | `MAP-001`, `MAP-003`, `OWN-001` | normalized source filename ↔ class name | yes; `MappingSuggestion` must be explicitly converted/accepted | core detector: version, fingerprint, source, target findings |
| Agent ↔ object | `MappingKind.AGENT_OBJECT` / `MappingBinding` | yes | `MAP-001`, `MAP-003`, `OWN-001`, `OCL-001..002`, `CTX-001` | normalized source filename ↔ current object name | yes | missing snapshot object is `TARGET_MISSING`; fingerprint may also change |
| External action ↔ operation | `MappingKind.ACTION_OPERATION` / `MappingBinding` | yes | `MAP-002..003`, `SIG-001..003`, `OWN-001`, `OCL-001..004` | normalized functor/name + arity; optional explicit `soil:` effect | yes | missing source/operation, fingerprint/metamodel changes |
| Argument ↔ parameter | `MappingKind.PARAMETER` / `MappingBinding` | yes | `MAP-003`; signature rules use operation signature directly | argument position and operation parameter order | yes | missing argument/parameter plus document-level checks |
| Receiver ↔ object | `MappingKind.RECEIVER_OBJECT` / `MappingBinding` | yes | `REF-001`, `MAP-003`, `MSG-001` | normalized indexed receiver ↔ current object name | yes | missing receiver/object plus fingerprint change |
| Belief ↔ attribute | `MappingKind.BELIEF_ATTRIBUTE` / `MappingBinding` | yes | `REF-002`, `MAP-003`, `BEL-001`, `CTX-001` | normalized initial-belief functor ↔ attribute name | yes | missing signature/attribute; predicate key is project-independent |
| Artifact operation ↔ operation | `PersistedEnvironmentOperationMapping` | yes, `.cartago-map.json` 0.1.0 | `ENV-001`, `ENV-002`, `ENV-004` | caller-supplied static operation/arity/type evidence; no core auto-confirm path | yes, explicit enum | explicit `CURRENT`, `STALE`, `UNKNOWN`; only confirmed-current reaches semantic validator |
| Observable property ↔ attribute | `PersistedEnvironmentPropertyMapping` | yes, `.cartago-map.json` 0.1.0 | `ENV-001`, `ENV-003`, `ENV-004` | caller-supplied descriptor/type evidence | yes, explicit enum | explicit state; unavailable dynamic values remain UNKNOWN |
| Role ↔ class | `OrganizationRoleMapping` | no plugin repository | `ORG-001` | reviewer-supplied mapping/evidence | yes, `OrganizationMappingConfirmation` | source/target checked at evaluation; candidate is INFO/UNKNOWN |
| Mission ↔ operation | `OrganizationMissionMapping` | no plugin repository | `ORG-002` | reviewer-supplied mapping/evidence | yes | source/target checked at evaluation; candidate is INFO/UNKNOWN |
| Role cardinality ↔ OCL invariant | `OrganizationCardinalityMapping` | no plugin repository | `ORG-003` | reviewer selects invariant and optionally records normalized bounds | yes | missing endpoints/mismatched reviewed bounds fail; absent bounds/runtime enactment are UNKNOWN |

## Rule admission summary

| Family | Candidate admitted? | Confirmed but stale/unknown admitted? | Current confirmed admitted? |
|---|---:|---:|---:|
| core BDI | no; suggestions live outside `MappingDocument` | binding remains visible so `MAP-003` and dependent rules can report explicit problems | yes |
| environment | no | no semantic validation; emits `ENV-004` | yes |
| organization | no semantic target validation; emits INFO/UNKNOWN review finding | confirmed endpoints are checked; unavailable reviewed/runtime evidence stays explicit | yes, within static rule limits |

See [the correspondence specification](CROSS_MODEL_CORRESPONDENCE.md) for
identity, persistence, synchronization, and claim boundaries.
