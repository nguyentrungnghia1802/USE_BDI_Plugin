# Rule-to-Metamodel Matrix

Status: **NORMATIVE TRACE MATRIX FOR CURRENT RULES**

Detailed behavior: [static semantics](STATIC_SEMANTICS.md)

The “correspondence concepts” column names current executable mapping inputs;
Task 07 formalizes them as a correspondence specification. `No` under runtime
evidence means the rule is intentionally static. `Optional/required` never
implies that absent evidence is PASS.

| Rule | Execution phase/catalog | Metamodel concepts | Correspondence concepts | USE concepts | Runtime evidence needed? | Formalization type |
|---|---|---|---|---|---|---|
| `ASL-001` | `PARSE` | pre-model parser diagnostic, `SourceSpan` | none | none | No | `PARSER_DIAGNOSTIC` |
| `ASL-002` | `PARSE` | `Agent`, `UnsupportedFeature` | none | none | No | `PARSER_DIAGNOSTIC` |
| `BDI-001` | `IR_WELL_FORMEDNESS` | `Agent`, `Plan`, ordered containment | none | none | No | `METAMODEL_WELL_FORMEDNESS` |
| `BDI-002` | `IR_WELL_FORMEDNESS` | `Goal`, `GoalCall`, `Plan`, `Trigger`, `LiteralTerm` | none | none | No | `REFERENCE_RESOLUTION` |
| `BDI-003` | `IR_WELL_FORMEDNESS` | `Plan`, `Trigger`, `Term`, `LiteralTerm` | none | none | No | `METAMODEL_WELL_FORMEDNESS` |
| `BDI-004` | `IR_WELL_FORMEDNESS` | `Plan`, ordered `Step`, `SourceSpan` | none | none | No | `METAMODEL_WELL_FORMEDNESS` |
| `REF-001` | `REFERENCE` | action/internal-action receiver and recursive `Term` occurrences | `RECEIVER_OBJECT` | current objects | Static snapshot required | `REFERENCE_RESOLUTION` |
| `REF-002` | `REFERENCE` | `TestStep`, `Context`, `Belief`, predicate signature | `BELIEF_ATTRIBUTE` | attribute target not dereferenced by this rule | No | `REFERENCE_RESOLUTION` |
| `MAP-001` | `MAPPING` | `Agent` | `AGENT_CLASS`, `AGENT_OBJECT` | targets checked separately | No | `CROSS_MODEL_CONSISTENCY` |
| `MAP-002` | `MAPPING` | `ExternalAction`, `Term` signature | `ACTION_OPERATION` | target checked separately | No | `CROSS_MODEL_CONSISTENCY` |
| `MAP-003` | `MAPPING` | all source-bearing BDI concepts used by mapping source IDs | all six BDI `MappingKind` values and staleness state | classes, objects, attributes, operations | Static snapshot required | `CROSS_MODEL_CONSISTENCY` |
| `SIG-001` | `SIGNATURE` | `ExternalAction`, `LiteralTerm`/`CompoundTerm` arguments | `ACTION_OPERATION` | operation parameters | Static snapshot required | `CROSS_MODEL_CONSISTENCY` |
| `SIG-002` | `SIGNATURE` | action `Term` arguments (`StringTerm`, `NumberTerm`, boolean literal) | `ACTION_OPERATION` | parameter types | Static snapshot required | `CROSS_MODEL_CONSISTENCY` |
| `SIG-003` | `SIGNATURE` | action `Term` arguments with unknown static type | `ACTION_OPERATION` | parameter types | Optional snapshot/type evidence; absent remains UNKNOWN/not evaluated | `CROSS_MODEL_CONSISTENCY` |
| `OWN-001` | `SIGNATURE` | `Agent`, `ExternalAction` | `AGENT_CLASS`, `AGENT_OBJECT`, `ACTION_OPERATION` | object class, class parents, operation owner | Static snapshot required | `CROSS_MODEL_CONSISTENCY` |
| `BEL-001` | `MAPPING` | initial `Belief`, `LiteralTerm` signature | `BELIEF_ATTRIBUTE` | target checked separately | No | `CROSS_MODEL_CONSISTENCY` |
| `MSG-001` | `REFERENCE` | `.send` as `InternalAction`, receiver `Term` | `RECEIVER_OBJECT` | current objects | Static snapshot required; dynamic evidence otherwise absent | `REFERENCE_RESOLUTION` |
| `OCL-001` | `SNAPSHOT_OCL` | `ExternalAction`, action arguments | `AGENT_OBJECT`, `ACTION_OPERATION` | current object, operation preconditions, snapshot state | Yes: immutable current snapshot | `SNAPSHOT_SEMANTIC` |
| `OCL-002` | `SNAPSHOT_OCL` | `ExternalAction`, action arguments | `AGENT_OBJECT`, `ACTION_OPERATION` | receiver, preconditions, compiled/evaluated OCL | Yes; absence/error is UNKNOWN | `SNAPSHOT_SEMANTIC` |
| `CTX-001` | `SNAPSHOT_OCL` | `Plan`, supported `Context` tree, literal predicates | `AGENT_OBJECT`, `BELIEF_ATTRIBUTE` | current object attributes and expression evaluation | Yes; unsupported/absent path is not PASS | `SNAPSHOT_SEMANTIC` |
| `OCL-003` | `BOUNDED_SIMULATION` | `ExternalAction` | `ACTION_OPERATION` with explicit `soil:` effect | disposable USE variation and invariants | Yes: bounded disposable variation | `BOUNDED_SIMULATION` |
| `OCL-004` | `BOUNDED_SIMULATION` | `ExternalAction` | `ACTION_OPERATION`, optional `soil:` effect | optional evaluator/variation | Yes when effect supplied; absence is UNKNOWN/not evaluated | `BOUNDED_SIMULATION` |
| `ENV-001` | environment catalog | `Artifact`, `ArtifactOperation` | confirmed/current environment operation mapping | operation target | No | `STATIC_ENVIRONMENT` |
| `ENV-002` | environment catalog | `ArtifactOperation`, BDI action arity evidence | environment operation mapping | operation target identity retained | No | `STATIC_ENVIRONMENT` |
| `ENV-003` | environment catalog | `ObservableProperty` | confirmed/current environment property mapping | attribute target | Optional reviewed values; absence is UNKNOWN | `STATIC_ENVIRONMENT` |
| `ENV-004` | environment validation service | `Artifact`, `ArtifactOperation`, `ObservableProperty`, source identity | persisted confirmation and staleness state | operation/attribute targets | No live runtime; unknown staleness stays UNKNOWN | `STATIC_ENVIRONMENT` |
| `ORG-001` | organization catalog | `Organization`, `Role` | role-to-class mapping and confirmation | class target | No; membership/enactment unavailable | `STATIC_ORGANIZATION` |
| `ORG-002` | organization catalog | `Organization`, `Scheme`, `Mission` | mission-to-operation mapping and confirmation | operation target | No; commitment/execution unavailable | `STATIC_ORGANIZATION` |
| `ORG-003` | organization catalog | `Group`, `RoleCardinality`, `Cardinality` | cardinality-to-invariant mapping, confirmation, reviewer-normalized bounds | class invariant target | Runtime membership required for enactment; unavailable => UNKNOWN | `STATIC_ORGANIZATION` |

## Matrix invariants

- The standard registry has exactly 22 IDs and retains its configuration
  contract.
- `ENV-001..004` and `ORG-001..003` are separate extension catalogs.
- UML/OCL, mapping, issue, and runtime concepts are not moved into the JaCaMo
  domain Ecore package.
- A matrix cell saying a prerequisite is required does not convert absence to
  `NO_FINDING` or PASS; [the static-semantics outcome model](STATIC_SEMANTICS.md#1-authority-and-outcome-model)
  governs that boundary.
