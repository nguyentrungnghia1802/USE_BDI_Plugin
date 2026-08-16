# JaCaMo Consistency Analysis Profile Coverage

Status: **VERSION 1.0 COVERAGE CONTRACT**

Specification: [JaCaMo Consistency Analysis Profile](USE_JACAMO_ANALYSIS_METAMODEL.md)

Allowed status values:

- `SUPPORTED`: materialized and analyzed at the stated profile meaning.
- `PARTIAL`: materialized for a documented subset or with a deliberate representation adaptation.
- `DECLARATION_ONLY`: static declaration metadata only; no runtime semantic claim.
- `UNSUPPORTED_EXPLICIT`: prior/source concept is not materialized as a first-class profile concept; evidence remains explicit where recognized.
- `OUT_OF_SCOPE`: deliberately excluded concern, usually runtime or a separate correspondence/presentation layer.

## Evidence package

| Metaclass | Prior work | Current source producer | Current IR | Rules using it | Diagram projection | Status |
|---|---|---|---|---|---|---|
| `SourceLocatedElement` | Language elements require locatable syntax/evidence (S1) | All source adapters | Factored across `SourceSpan`, `ProjectSourceId`, organization spans | All source-bound standard/ENV/ORG findings | Portable source selection/labels | `SUPPORTED` |
| `ProjectSourceIdentity` | Heterogeneous-model traceability concern (S1) | project-root/source normalization | `ProjectSourceId`; serializers relativize BDI spans | `MAP-003`, `ENV-004`, suppression/staleness identity | Source and resource nodes | `SUPPORTED` |
| `SourceSpan` | Source-level concrete-syntax evidence (S1–S4) | Jason/JaCaMo/Moise adapters | BDI and organization `SourceSpan`, `ProjectSourceId` | `ASL-002`, BDI/reference/mapping diagnostics | Selection references and tooltips | `SUPPORTED` |
| `UnsupportedFeature` | Prior metamodels include constructs beyond bounded subset | Jason/Moise normalizers | BDI and organization unsupported records | `ASL-002`; project/organization diagnostics | Unsupported/gap evidence nodes where projected | `SUPPORTED` |

## BDI package

| Metaclass | Prior work | Current source producer | Current IR | Rules using it | Diagram projection | Status |
|---|---|---|---|---|---|---|
| `Agent` | S2/S3/S4 `Agent` | Jason adapter/normalizer | `AgentModel` | `MAP-001`, BDI aggregation; parser/unsupported prerequisites | Agent/source group | `SUPPORTED` |
| `Belief` | S2/S3/S4 `Belief` | Jason normalizer | `BeliefModel` | `BEL-001`, `REF-002`, `CTX-001` evidence | Belief node/signature | `SUPPORTED` |
| `Goal` | S2/S3/S4 agent `Goal` | Jason normalizer | `GoalModel` | `BDI-002`; goal support index | Goal node/focus | `SUPPORTED` |
| `Plan` | S2/S3/S4 `Plan` | Jason normalizer | `PlanModel` | `BDI-001..004`, context/reference/mapping/signature/OCL families | Plan node and ordered body path | `SUPPORTED` |
| `Trigger` | S2/S3/S4 triggering event | Jason normalizer | `TriggerModel` | `BDI-002`, `BDI-003`, goal-support index | Trigger relation/signature | `SUPPORTED` |
| `Context` | S2/S3/S4 context/logical expression | Jason normalizer | `ContextExpr` | `REF-002`, `CTX-001`, `OCL-002` prerequisites | Context node/evidence | `SUPPORTED` |
| `LiteralContext` | Current typed refinement of prior context | Jason normalizer | `ContextLiteral` | `REF-002`, `CTX-001` | Context literal | `SUPPORTED` |
| `UnaryContext` | Current typed refinement | Jason normalizer | `ContextUnary` | `CTX-001` where evaluable | Context expression | `SUPPORTED` |
| `BinaryContext` | Current typed refinement | Jason normalizer | `ContextBinary` | `CTX-001` where evaluable | Context expression | `SUPPORTED` |
| `UnsupportedContext` | Loss-preserving subset boundary | Jason normalizer | `ContextUnsupported` | `ASL-002`; downstream UNKNOWN/skip | Explicit unsupported evidence | `SUPPORTED` |
| `Step` | S2 body elements; S4 Body/Action chain | Jason normalizer | `PlanStepModel` | `BDI-004`; body-wide reference/mapping checks | Ordered `EXECUTES` path | `SUPPORTED` |
| `ExternalAction` | S2/S3/S4 external/basic action | Jason normalizer | `ActionStepModel` | `MAP-002`, `SIG-001..003`, `REF-001`, OCL rules, `ENV-002` | Action and mapping target path | `SUPPORTED` |
| `InternalAction` | S2/S3/S4 internal action | Jason normalizer | `InternalActionStepModel` | `REF-001`, `MSG-001` for `.send`; signature indexing | Internal action/message path | `SUPPORTED` |
| `GoalCall` | S2 plan subgoal | Jason normalizer | `AchieveGoalStepModel` | `BDI-002`, `REF-002` as applicable | Goal-call edge | `SUPPORTED` |
| `TestStep` | Jason test goal/body element | Jason normalizer | `TestStepModel` | `REF-002`, `CTX-001` | Test/context path | `SUPPORTED` |
| `BeliefUpdate` | Jason belief change/body element | Jason normalizer | `BeliefUpdateStepModel` | belief/reference indexes; bounded analysis prerequisites | Belief-update step | `SUPPORTED` |
| `ConstraintStep` | Current typed body constraint | Jason normalizer | `ConstraintStepModel` | `CTX-001` where evaluable | Constraint/context path | `SUPPORTED` |
| `UnsupportedStep` | Loss-preserving subset boundary | Jason normalizer | `UnsupportedStepModel` | `ASL-002`, `BDI-004` ordering evidence | Unsupported step/gap | `SUPPORTED` |
| `Term` | S2/S3 logical/action expressions; implementation refinement | Jason normalizer | `TermModel` | reference/signature/type/message/context rules | Labels/signatures/evidence | `SUPPORTED` |
| `LiteralTerm` | S2/S3 belief/goal/action literal | Jason normalizer | `LiteralTermModel` | `BDI-002`, `REF-001..002`, `MAP-002`, `SIG-001..003`, `BEL-001`, `MSG-001` | Signature-bearing nodes/labels | `SUPPORTED` |
| `CompoundTerm` | Implementation refinement needed for expressions | Jason normalizer | `CompoundTermModel` | reference/signature argument indexing | Detail/labels where projected | `SUPPORTED` |
| `VariableTerm` | AgentSpeak term kind | Jason normalizer | `VariableTermModel` | type inference and UNKNOWN evidence | Detail only | `SUPPORTED` |
| `NumberTerm` | AgentSpeak term kind | Jason normalizer | `NumberTermModel` | type/signature inference | Detail only | `SUPPORTED` |
| `StringTerm` | AgentSpeak term kind | Jason normalizer | `StringTermModel` | type/signature/message evidence | Detail only | `SUPPORTED` |
| `ListTerm` | AgentSpeak term kind | Jason normalizer | `ListTermModel` | recursive reference/type indexing | Detail only | `SUPPORTED` |
| `SetTerm` | AgentSpeak term kind | Jason normalizer | `SetTermModel` | recursive reference/type indexing | Detail only | `SUPPORTED` |
| `ArithmeticTerm` | AgentSpeak expression kind | Jason normalizer | `ArithmeticTermModel` | type/context analysis where supported | Detail only | `SUPPORTED` |
| `UnsupportedTerm` | Loss-preserving subset boundary | Jason normalizer | `UnsupportedTermModel` | `ASL-002`; downstream UNKNOWN | Unsupported evidence | `SUPPORTED` |
| `Rule` *(not a v1 classifier)* | S2/S3/S4 `Rule` | Jason parser sees source, normalizer has no first-class record | none | `REF-002` cannot rely on explicit rule instances | none | `UNSUPPORTED_EXPLICIT` |
| `Message` *(not a v1 classifier)* | S2/S4 `Message` | `.send` normalized as internal action | `.send` term plus action/reference index | `MSG-001`, `REF-001` | action/message evidence path | `PARTIAL` |

## MAS and environment packages

| Metaclass | Prior work | Current source producer | Current IR | Rules using it | Diagram projection | Status |
|---|---|---|---|---|---|---|
| `MasProject` | S4 `MAS`; S5/S6 generic MAS | JaCaMo project adapter | `MasProjectModel` | project diagnostics and downstream ENV/ORG prerequisites | MAS root | `SUPPORTED` |
| `AgentInstance` | S4 MAS→Agent; JaCaMo declaration | project adapter/import service | `MasAgentInstanceModel`; normalized `AgentModel` linked by analysis aggregate/source | project/reference availability; `MAP-001` on normalized model | Agent instance and source relation | `SUPPORTED` |
| `ProjectResourceReference` | S4 workspace/organization partitions | project adapter | `MasResourceReference` | project diagnostics; ENV/ORG availability | Resource/status node | `DECLARATION_ONLY` |
| `EnvironmentModel` | S4 artifact partition | CArtAgO adapter/application composition | `EnvironmentModel` | `ENV-001..004` | Environment layer | `DECLARATION_ONLY` |
| `Artifact` | S4 `Artifact` | CArtAgO adapter | `ArtifactModel` | `ENV-001..004` | Artifact node | `DECLARATION_ONLY` |
| `ArtifactOperation` | S4 `AbsOperation` hierarchy | retained `@OPERATION` metadata adapter | `EnvironmentOperation` | `ENV-001`, `ENV-002` | Operation and mapping/gap path | `DECLARATION_ONLY` |
| `ObservableProperty` | S4 `ObsProperty` | explicit descriptor/fixture | `ObservablePropertyModel` | `ENV-003`, `ENV-004` | Property/evidence path when present | `PARTIAL` |
| `Workspace` *(not a semantic v1 classifier)* | S4 `Workspace` | `.jcm` parser | resource kind/name; artifact has workspace string | resource diagnostics/ENV identity | Workspace resource/layer label | `DECLARATION_ONLY` |
| `Port` / artifact links *(not v1 classifiers)* | S4 `Port` | not normalized | none | none | none | `OUT_OF_SCOPE` |
| Operation subtypes *(not v1 classifiers)* | S4 Operation/Guard/Internal/Linked | current adapter flattens authoritative metadata | `EnvironmentOperation` | `ENV-001..002` use signature/guard only | operation node | `PARTIAL` |
| Live workspace/property value stream | CArtAgO runtime concern | no production source | optional reviewed fixture values only | `ENV-003` returns UNKNOWN without values | static-only legend | `OUT_OF_SCOPE` |

## Organization package

| Metaclass | Prior work | Current source producer | Current IR | Rules using it | Diagram projection | Status |
|---|---|---|---|---|---|---|
| `Organization` | S4 `Organisation` | Moise adapter | `OrganizationModel` | `ORG-001..003` prerequisites | Organization root | `SUPPORTED` |
| `Role` | S4 `Role` | Moise adapter | `OrganizationModel.Role` | `ORG-001`, `ORG-003` | Role node/mapping | `SUPPORTED` |
| `Group` | S4 `Group` | Moise adapter | `OrganizationModel.Group` | `ORG-003` grouping/cardinality context | Group node/containment | `SUPPORTED` |
| `RoleCardinality` | S4 role/formation bounds | Moise adapter | `OrganizationModel.RoleCardinality` | `ORG-003` | Cardinality evidence | `SUPPORTED` |
| `Cardinality` | S4 multiplicities/Moise bounds | Moise adapter | `OrganizationModel.Cardinality` | `ORG-003` | Bound label/evidence | `SUPPORTED` |
| `Scheme` | S4 `Scheme` | Moise adapter | `OrganizationModel.Scheme` | `ORG-002` mission lookup; `ORG-003` context | Scheme node | `SUPPORTED` |
| `OrganizationalGoal` | S4 `OGoal` | Moise adapter | `OrganizationModel.Goal` | mission/reference structural evidence | Goal node | `SUPPORTED` |
| `Mission` | S4 `Mission` | Moise adapter | `OrganizationModel.Mission` | `ORG-002`, `ORG-003` | Mission node/mapping | `SUPPORTED` |
| `Norm` | S4 `Norm` | Moise adapter | `OrganizationModel.Norm` | normalized structural evidence; no runtime fulfilment rule | Norm relation/detail where projected | `SUPPORTED` |
| Formation constraints / role links / OPlan | S4 classes | recognized selectively; bounded normalizer does not materialize full semantics | organization `UnsupportedFeature` where provided | no standard semantic rule | unsupported/static evidence only | `UNSUPPORTED_EXPLICIT` |
| Enactment/membership/commitment | Moise runtime concern | no production source | none | `ORG-003` explicitly remains UNKNOWN after static match | static-only legend | `OUT_OF_SCOPE` |

## Separate-layer exclusions

| Concept family | Why absent from domain profile | Owner | Status |
|---|---|---|---|
| UML classes, objects, attributes, operations, associations, links, OCL | Heterogeneous target language, not JaCaMo domain abstract syntax | Task 07 correspondence specification and `use` projection | `OUT_OF_SCOPE` |
| Mapping candidate/confirmed/staleness | Relationship/lifecycle between models | Task 07 and mapping packages | `OUT_OF_SCOPE` |
| Consistency issue, severity/status/certainty | Analysis result | Task 05 and validation packages | `OUT_OF_SCOPE` |
| Trace nodes/edges | Explanation relation derived from analysis | Task 07/08 and trace package | `OUT_OF_SCOPE` |
| Diagram nodes/edges/view state | Read-only presentation | Task 08 and diagram/UI packages | `OUT_OF_SCOPE` |
| Report/CLI DTOs | Serialization/application boundary | report/CLI/evaluation packages | `OUT_OF_SCOPE` |
| Jason interpreter state / JaCaMo runtime services | Execution rather than static analysis | none in project scope | `OUT_OF_SCOPE` |

## Coverage totals

The Ecore artifact defines 48 EClasses (including four abstract factoring classes) and eight EEnums across five packages. Every EClass is represented above. Prior-only concepts that might otherwise be mistaken for coverage are separately classified. The profile does not convert missing concepts into synthetic instances.
