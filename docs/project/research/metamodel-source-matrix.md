# Metamodel Source Matrix

Status: **VERIFIED FOR TASK 04 INPUT**

Audit date: 2026-08-17

Companion evidence: [literature and reuse audit](literature-reuse-audit.md)

## Legend

- `S1` Combemale et al.; `S2` Jason metamodel; `S3` DSML4BDI; `S4` DSML4JaCaMo; `S5` FAML; `S6` Hahn/Madrigal-Mora/Fischer. Complete metadata and exact evidence locations are in the companion audit.
- `—` means that the source is not used as evidence for that concept; it does not assert universal absence.
- Decisions use only `REUSE_DIRECT`, `ADAPT_CONCEPT`, `REFERENCE_ONLY`, `DO_NOT_REUSE`, and `OUT_OF_SCOPE`.
- “Current IR” names implemented Java records/enums or states a verified gap. It never treats a prior-work concept as if it were already implemented.

## Concept matrix

| Concept | Combemale role | Jason MM | DSML4BDI | DSML4JaCaMo | FAML/Hahn | Current IR | Decision |
|---|---|---|---|---|---|---|---|
| MAS/System | Root/domain abstract syntax | MAS container/shared sets | MAS root and organization | `MAS`; owns agents/workspaces/organizations | Generic system abstraction | `MasProjectModel` with agents, resources, organizations | `ADAPT_CONCEPT` — preserve source-root identity/provenance |
| Agent | Domain metaclass | `Agent` with belief/plan/event/rule/action structures | Core `Agent` | `Agent`; `MAS.agent [1..*]` | Generic agent | `MasAgentInstanceModel` plus per-source `AgentModel` | `ADAPT_CONCEPT` — distinguish instance from source model |
| Belief | Domain concept and constraints | Belief/BeliefBase | Belief/BeliefBase | `Belief`; agent collection `[0..*]` | Generic knowledge concept | `BeliefModel(LiteralTermModel, SourceSpan)` | `REUSE_DIRECT` |
| Goal | Domain concept and constraints | Goal set; achievement/test subgoals | Goal/GoalSet | Agent `Goal`; distinct organizational `OGoal` | Generic goal | `GoalModel`; organizational `OrganizationModel.Goal` | `ADAPT_CONCEPT` — keep agent and organization identities distinct |
| Rule | Domain concept and constraints | `Rule`/RuleSet | `Rule`/RuleSet | `Rule`; agent collection `[0..*]` | Generic behavior/knowledge references | No first-class `RuleModel` | `ADAPT_CONCEPT` — candidate gap; do not invent instances |
| Plan | Domain concept, syntax, semantics | Trigger + context + body | `Plan`/PlanLibrary | `Plan`; agent collection `[0..*]` | Generic plan/task | `PlanModel(label, trigger, optional context, steps, span)` | `REUSE_DIRECT` with documented optional-context deviation |
| Trigger | Static/behavioral syntax concept | Triggering event; belief/goal change | `Triggering_Event` | `TriggeringEvent`; `Plan.triggersPlan [1..1]` | — | `TriggerModel` with operator/type/term/span | `ADAPT_CONCEPT` — typed source semantics are normative |
| Context | Static semantics / expression | Logical applicability expression | Logical expression model | `Context`; Figure 1 `hasContext [1..1]` | — | `Optional<ContextExpr>` typed tree | `ADAPT_CONCEPT` — optional in imported Jason source |
| Step/Body | Ordered behavioral syntax | Plan body contains actions/subgoals/notes | Plan/body/logical expression | `Body`, `BodyTerm`; `hasBody [1..1]`, first/next links | Generic behavior/task | `List<PlanStepModel>` preserves source order | `ADAPT_CONCEPT` — list replaces linked action chain |
| Internal Action | Behavioral syntax | Internal action specialization | Internal action specialization | `InternalAction` subtype | — | `InternalActionStepModel` | `REUSE_DIRECT` |
| External Action | Behavioral syntax | External/basic action specialization | External action specialization | `ExternalAction` subtype | — | `ActionStepModel` | `ADAPT_CONCEPT` — current name is intentionally platform-neutral |
| Message | Interaction syntax | Message/communication notation | Agent interaction/message support | `Message` with content/broadcast attributes | Generic interaction | No first-class `MessageModel`; send actions may remain action terms | `ADAPT_CONCEPT` — gap only when evidence/rules require it |
| Workspace/resource reference | Domain relation and multiplicity | — | — | `Workspace`; `MAS.workspace [0..*]` | Environment/resource abstraction at generic level | `MasResourceReference`; `ArtifactModel.workspace` | `ADAPT_CONCEPT` — source reference and materialized environment stay separate |
| Artifact | Domain metaclass | — | — | `Artifact`; workspace containment/reference | Generic environment/resource reference | `ArtifactModel(workspace, instanceName, typeName, ...)` | `REUSE_DIRECT` |
| Artifact operation | Operation/metabehavior | External actions only | — | `AbsOperation` plus Operation/Guard/Internal/Linked specializations; `[0..*]` | Generic capability/service reference | `EnvironmentOperation(name, arity, parameterTypes, guard)` | `ADAPT_CONCEPT` — flattened conservative representation |
| Observable property | Domain attribute/state | Belief may represent environment knowledge | — | `ObsProperty`; artifact collection `[0..*]` | Generic environment state | `ObservablePropertyModel` | `REUSE_DIRECT` |
| Organization | Domain partition/view | MAS organization only at coarse level | MAS organization | `Organisation` with three specifications; `MAS.organisation [0..*]` | Generic organization | `OrganizationModel` static Moise subset | `ADAPT_CONCEPT` |
| Group | Domain metaclass/containment | — | Organization support | `Group`, recursive subgroup relation | Generic group | `OrganizationModel.Group` with parent qualified id | `REUSE_DIRECT` |
| Role | Domain metaclass/relation | — | Agent relations/organization | `Role`, role extension/link/formation relations | Generic role | `OrganizationModel.Role` | `REUSE_DIRECT` |
| Scheme | Domain metaclass/containment | — | — | `Scheme`; functional-spec collection `[0..*]` | Generic workflow/process references | `OrganizationModel.Scheme` | `REUSE_DIRECT` |
| Mission | Domain metaclass/relation | — | — | `Mission`; scheme `[1..*]` | Generic responsibility/task reference | `OrganizationModel.Mission` with cardinality/goals | `REUSE_DIRECT` |
| Organizational goal | Separate viewpoint identity | — | — | `OGoal`, distinct from agent `Goal`; mission `[1..*]` | Generic goal | `OrganizationModel.Goal` scoped by qualified id | `ADAPT_CONCEPT` — expose “organizational” in profile terminology |
| Norm | Static organizational constraint | — | — | `Norm` connects one role and one mission | Generic norm/social constraint references | `OrganizationModel.Norm` with permission/obligation, role, mission | `REUSE_DIRECT` |
| Cardinality | Abstract-syntax well-formedness | Association multiplicities | Ecore multiplicities | Explicit Ecore multiplicities; Moise min/max attributes | Generic relationship cardinalities | `OrganizationModel.Cardinality`; UML association-end multiplicities | `ADAPT_CONCEPT` — compare source constraint to UML bound, preserve unbounded |
| Mapping/correspondence | Heterogeneous modeling/weaving | — | Model-to-platform mapping for generation | Metamodel-to-JaCaMo translational mapping | Transformation-oriented | `MappingBinding`, environment and organization mapping records, staleness | `REUSE_DIRECT` — central project contribution |
| UML class/object | Target-language abstract syntax and instances | — | — | — | — | `UmlClassRef`, `UmlObjectRef`; mapping kinds `AGENT_CLASS`, `AGENT_OBJECT` | `REUSE_DIRECT` |
| UML attribute/operation | Target-language features | — | — | Artifact operations are not UML operations | — | `UmlAttributeRef`, `UmlOperationRef`; mapping kinds `BELIEF_ATTRIBUTE`, `ACTION_OPERATION` | `REUSE_DIRECT` |
| OCL constraint | Static semantics | Metamodel conformance constraints, not USE OCL | Static-semantics controls | Domain constraints stated, no UML/OCL cross-model framework described | — | `UmlConstraintRef`, `UseOclEvaluator`, `OclSnapshotResult` | `REUSE_DIRECT` — USE-specific evidence target |
| Issue/evidence | Validation feedback/tooling | Conformance checking without this repository's issue schema | Constraint feedback | No comparable issue/evidence schema described | — | `ConsistencyIssue` with status, severity, evidence, suggested fix | `REUSE_DIRECT` |
| Trace link | Heterogeneous integration and model weaving | — | Generation mappings, not diagnostic trace | Generation mapping, not diagnostic trace graph | Transformation trace is a generic concern | `TraceNode`, `TraceEdge`, `TraceRelationKind` | `REUSE_DIRECT` — project-specific diagnostic trace |
| Certainty | Analysis epistemics | — | — | — | — | `IssueCertainty {CONFIRMED, POTENTIAL, UNKNOWN}` on issues/trace edges | `REUSE_DIRECT` — required for conservative analysis |

## DSML4JaCaMo alignment summary for Task 04

| S4 partition | Direct current coverage | Known adaptation or gap |
|---|---|---|
| Agent | Agent instance/source, belief, goal, plan, trigger, context, ordered steps, internal/external/basic actions | No first-class rule, message, mental-note, or linked `Body` object; unsupported syntax stays explicit. |
| Environment | Workspace identity, artifact instance/type, operation signature/guard, observable property | Operation subclass hierarchy and port/link structure are not mirrored as a full authoring metamodel. |
| Organization | Organization, group hierarchy, role/cardinality, scheme, mission/cardinality, organizational goal, permission/obligation norm | Formation constraints, role inheritance/links, and some Moise features are bounded/unsupported and must not be inferred. |
| Cross-model USE profile | UML classes/objects/features/associations/links/OCL, mappings, issues, evidence, trace, certainty | This is the project's adaptation layer; it is not claimed to exist in S4. |

## Normative adoption rules

1. The current parser-independent Java IR is the runtime source of truth; literature supplies alignment and gap evidence.
2. A prior-work multiplicity becomes normative only after it agrees with actual Jason/JaCaMo syntax and conservative importer behavior.
3. Missing optional source data remains absent or `UNKNOWN`; no object is synthesized merely to complete a metamodel association.
4. Agent goals and Moise organizational goals have separate identities even when both use goal-like terms.
5. UML specification elements and runtime objects/links are distinct target layers.
6. Every cross-language relationship must be explicit, evidence-bearing, and staleness-aware where persisted.
7. No Ecore, Sirius, EVL, Acceleo, EuGENia, or GMF dependency follows from this matrix.

## Verification

```text
Mandatory concepts: PASS — 31/31 represented
DSML4JaCaMo partitions: PASS — agent/environment/organization mapped
Multiplicity handling: PASS — selected Figure 1 values recorded; adaptation rule explicit
Current IR alignment: PASS — implemented records and verified gaps distinguished
Decision enum: PASS — only roadmap-approved values used
Unsupported claims: none
Result: PASS
```
