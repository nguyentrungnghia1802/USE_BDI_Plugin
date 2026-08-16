# JaCaMo Consistency Analysis Profile 1.0

Status: **NORMATIVE SPECIFICATION**

Version: `1.0.0`

Namespace URI: `https://useocl.github.io/bdi/metamodel/analysis/1.0`

Machine-readable artifact: [`use-jacamo-analysis.ecore`](use-jacamo-analysis.ecore)

Prior-work basis: [literature/reuse audit](../research/literature-reuse-audit.md) and [source matrix](../research/metamodel-source-matrix.md). Research boundary and terminology are frozen in [research positioning](../research/research-positioning.md) and [terminology](../research/terminology.md).

## 1. Purpose and conformance

The **JaCaMo Consistency Analysis Profile** is a bounded abstract-syntax specification for facts that the plugin can statically import, normalize, index, or analyze. It adapts established Jason/BDI and JaCaMo concepts; it is not a new general JaCaMo metamodel, a full DSML, an authoring model, or a runtime model.

The relationship to production code is realization, not class generation:

```text
prior-work/domain concept
        -> analysis profile concept
        -> realized by immutable Java IR and adapter output
```

The `.ecore` file is a specification/design artifact. It is not generated into Java and does not add EMF, Sirius, or any Eclipse runtime to the plugin. Task 06 owns the detailed profile-to-Java conformance proof.

## 2. Package and boundary summary

| Package | Purpose | Authority | Boundary state |
|---|---|---|---|
| `evidence` | Portable source identity, spans, and loss-preserving unsupported evidence | Project identity policy and current IR/source records | `STATIC_NORMALIZED_MODEL`; no checkout-absolute persisted identity |
| `bdi` | Supported AgentSpeak agent structure, plan expressions/steps, and term tree | Jason parser through the adapter; S2/S3/S4 vocabulary | `STATIC_NORMALIZED_MODEL`; operational semantics not modeled |
| `mas` | Static `.jcm` root, agent-instance declarations, and resource references/status | JaCaMo parser through the project adapter | `STATIC_DECLARATION`; runtime lifecycle unavailable |
| `environment` | Static CArtAgO artifact operation/property evidence | Official retained annotation metadata and explicit descriptors | `STATIC_DECLARATION`; live workspace/value state unavailable unless a reviewed fixture explicitly supplies values |
| `organization` | Bounded normalized Moise roles/groups/schemes/missions/goals/norms/cardinality | Moise parser through the adapter | `STATIC_NORMALIZED_MODEL`; enactment unavailable |

UML/OCL elements, mapping state, issues, trace links, diagrams, Swing state, report DTOs, and runtime interpreter objects are deliberately outside this domain profile. Task 07 specifies correspondence; Task 08 specifies graphical representation.

The boundary vocabulary is normative: `STATIC_DECLARATION` identifies parsed declarations, `STATIC_NORMALIZED_MODEL` identifies immutable normalized facts, and `RUNTIME_NOT_AVAILABLE` identifies lifecycle, live state, or enactment evidence that this profile and plugin do not provide.

## 3. Global structural conventions

| Concern | Normative convention |
|---|---|
| Multiplicity notation | `0..1`, `1`, or `0..*`; every relation below states it explicitly. |
| Ordered collections | Source-order or deterministic normalized order is preserved. Multi-valued containment is ordered and unique by EObject identity, as required by Ecore; structurally equal source occurrences remain distinct contained objects. Non-containment value/reference lists may be non-unique where current Java lists preserve repeated values. |
| Containment | A contained child has exactly one profile owner. Cross-references (`normalizedAgent`, parent group, role/goal/mission targets) are not containment. |
| Portable source | `projectPath` is normalized and relative to an explicit project root. Lines/columns use `0` for unknown and otherwise form nonnegative, ordered bounds. |
| Identity | Stable source identity plus structural position/qualified identity is authoritative. Display labels alone are never identity. Checkout-absolute paths are not exposed/persisted identity. |
| Duplicate handling | Agent-instance names, artifact references/signatures, and organization qualified IDs are rejected deterministically by current constructors/adapters. Other occurrence lists preserve source order for later rule/index checks. |
| Unsupported evidence | Recognized-but-not-normalized syntax becomes `UnsupportedFeature`, `UnsupportedContext`, `UnsupportedStep`, or `UnsupportedTerm`; it is not silently dropped or synthesized. Invalid top-level input may remain an adapter diagnostic outside a model that could not be constructed. |
| Runtime boundary | No association such as `currentlyEnacts`, `currentMembership`, or `currentObservableValue` exists. Optional property values are available only when explicit reviewed evidence provides them; otherwise runtime evidence is unavailable. |

## 4. `evidence` package metaclasses

| Metaclass | Purpose and prior-work/project source | Attributes | Relations, multiplicity, order, containment | Identity and boundary | Canonical example |
|---|---|---|---|---|---|
| `SourceLocatedElement` *(abstract)* | Shared profile trait for source-backed elements; implementation-only factoring realized by `model.ir.SourceSpan`, `ProjectSourceId`, and organization spans | — | `span: SourceSpan [1]`, contained | Trait has no independent identity; `STATIC_NORMALIZED_MODEL` | Auction `@run_auction` plan carries its source span |
| `ProjectSourceIdentity` | Portable identity required by persistence/trace policy; realized by `model.source.ProjectSourceId` and serializer root relativization | `projectPath: String [1]` | none | Normalized project-relative path; no `.`/`..`, leading/trailing slash, or checkout-absolute value | `auctioneer.asl` |
| `SourceSpan` | Bounded source coordinates; current `model.ir.SourceSpan`, `ProjectSourceId`, and organization `SourceSpan` | `beginLine`, `beginColumn`, `endLine`, `endColumn: Int [1]` | `source: ProjectSourceIdentity [1]`, contained | Path plus coordinates; all-zero coordinates mean unknown | `auctioneer.asl:5:1–9:*` or unknown `0:0–0:0` |
| `UnsupportedFeature` | Retains recognized evidence that the bounded profile cannot normalize; adapts current BDI and organization unsupported records | `code`, `kind`, `subject: String [1]`; `elementQualifiedId: String [0..1]` | inherits contained `span [1]` | Source position plus code/kind/structural occurrence; static evidence, never an invented substitute | Unsupported AgentSpeak term or Moise formation/link detail |

Invalid files/elements that prevent safe normalization remain typed importer diagnostics rather than malformed metamodel instances. This boundary explains why adapter diagnostic classes are not profile metaclasses.

## 5. `bdi` package metaclasses

Prior-work source is S2 Jason MM, S3 DSML4BDI, and the agent partition of S4 DSML4JaCaMo unless a row says “implementation-only.” Every class is `STATIC_NORMALIZED_MODEL` and source-backed through the inherited span.

### Agent, beliefs, goals, plans, and triggers

| Metaclass | Purpose / current realization | Attributes | Relations, multiplicity, order, containment | Identity / supported boundary | Example |
|---|---|---|---|---|---|
| `Agent` | File-level normalized AgentSpeak model; `AgentModel` | `parserVersion: String [1]`; declared belief/goal/plan counts `Int [1]`, nonnegative | `beliefs: Belief [0..*]`, `goals: Goal [0..*]`, `plans: Plan [0..*]`, `unsupportedFeatures: UnsupportedFeature [0..*]`; all ordered containment | Portable source identity. Counts permit summary-only models; materialized lists must match before detailed analysis. No invented agent name | `auctioneer.asl` model |
| `Belief` | Initial/static normalized belief; `BeliefModel` | — | `literal: LiteralTerm [1]`, contained | Source span plus occurrence; no claim about changing runtime belief base | `auction_status(draft)` |
| `Goal` | Initial/declared agent goal; `GoalModel` | — | `literal: LiteralTerm [1]`, contained | Source span plus occurrence; distinct from organization goal | `run_auction` |
| `Plan` | Labeled plan with one trigger, optional context, and ordered body; `PlanModel` | `label: String [1]` | `trigger: Trigger [1]`, `context: Context [0..1]`, `steps: Step [0..*]`; contained, with steps ordered | Source span is primary; label is a readable secondary key and duplicate labels are diagnosed. Context is optional despite S4 Figure 1 `[1..1]` because supported Jason source permits omission | `@run_auction +!run_auction : auction_status(draft) <- ...` |
| `Trigger` | Plan triggering event; `TriggerModel` | `operator: TriggerOperator [1]`, `type: TriggerType [1]` | `term: Term [1]`, contained | Plan/source occurrence; must be present in supported `PlanModel` | `+!run_auction` |

`Rule` is established prior work but is not a metaclass in version 1.0 because current Java IR does not materialize an authoritative first-class rule model. `Message` is likewise not first-class: current analysis recognizes Jason `.send` as an external action and indexes its receiver/arguments. These prior-only concepts remain visible in the coverage matrix and are not fabricated.

### Context expression hierarchy

| Metaclass | Purpose / current realization | Attributes | Relations, multiplicity, containment | Identity / boundary | Example |
|---|---|---|---|---|---|
| `Context` *(abstract)* | Typed plan applicability expression; `ContextExpr` | — | inherited `span [1]` | Structural path within plan plus span | Auction plan context root |
| `LiteralContext` | Atomic literal condition; `ContextLiteral` | — | `literal: LiteralTerm [1]`, contained | Context tree position | `auction_status(draft)` |
| `UnaryContext` | Unary logical context; `ContextUnary` | `operator: String [1]` | `operand: Context [1]`, contained | Context tree position | `not registered_bidder(X)` when supported |
| `BinaryContext` | Binary logical context; `ContextBinary` | `operator: String [1]` | `left`, `right: Context [1]`, contained and ordered by side | Context tree position | `budget(B) & auction(auction1)` |
| `UnsupportedContext` | Explicit unsupported context branch; `ContextUnsupported` | — | `feature: UnsupportedFeature [1]`, contained | Feature source identity | Recognized context expression not normalized by the current adapter |

### Ordered plan step hierarchy

| Metaclass | Purpose / current realization | Attributes | Relations, multiplicity, containment | Identity / boundary | Example |
|---|---|---|---|---|---|
| `Step` *(abstract)* | One ordered body occurrence; `PlanStepModel` | — | inherited `span [1]` | Owning plan plus zero-based source order and span; order is semantic analysis evidence | First/second action under `run_auction` |
| `ExternalAction` | Basic/environment/application action; `ActionStepModel` | — | `action: Term [1]`, contained | Step occurrence; does not assert CArtAgO execution | `submitBid(auction1,120)` or `assignCustomer(...)` |
| `InternalAction` | Jason internal action; `InternalActionStepModel` | — | `action: Term [1]`, contained | Step occurrence | `.send(counter_agent,achieve,serve(C))` |
| `GoalCall` | Achievement subgoal; `AchieveGoalStepModel` | `newFocus: Boolean [1]` | `goal: LiteralTerm [1]`, contained | Step occurrence | `!finish_auction` |
| `TestStep` | Test-goal/condition step; `TestStepModel` | — | `condition: Context [1]`, contained | Step occurrence | `?registered_bidder(B)` when supported |
| `BeliefUpdate` | Belief add/delete/replace body step; `BeliefUpdateStepModel` | `operator: UpdateOperator [1]`; `focusPolicy: FocusPolicy [1]` | `belief: LiteralTerm [1]`, contained | Step occurrence; static instruction, not observed runtime belief change | `+auction_status(open)` |
| `ConstraintStep` | Constraint/logical condition in body; `ConstraintStepModel` | — | `condition: Context [1]`, contained | Step occurrence | Supported arithmetic/logical constraint |
| `UnsupportedStep` | Recognized body item without supported normalization; `UnsupportedStepModel` | — | `feature: UnsupportedFeature [1]`, contained | Feature source identity and body order retained | Unsupported Jason body construct |

### Term hierarchy

The term tree is implementation-driven but domain-necessary: rules compare functor/signature/namespace, mappings target action/belief signatures, `.send` arguments provide message-receiver evidence, and serializers preserve normalized source meaning.

| Metaclass | Purpose / current realization | Attributes | Relations, multiplicity, order, containment | Identity / boundary | Example |
|---|---|---|---|---|---|
| `Term` *(abstract)* | Parser-independent normalized term; `TermModel` | — | inherited `span [1]` | Structural tree path plus span | Trigger/action/context term root |
| `LiteralTerm` | Literal/predicate with annotations; `LiteralTermModel` | `functor: String [1]`, `negated: Boolean [1]` | `arguments`, `annotations: Term [0..*]`, ordered containment | Tree position; rendered text is not sole identity | `auction_status(draft)` |
| `CompoundTerm` | General compound term; `CompoundTermModel` | `functor: String [1]` | `arguments: Term [0..*]`, ordered containment | Tree position | `serve(Customer)` |
| `VariableTerm` | Logic variable; `VariableTermModel` | `name: String [1]` | none beyond span | Tree position; name is lexical, not global identity | `Budget` |
| `NumberTerm` | Numeric lexical value; `NumberTermModel` | `lexicalValue: String [1]` | none beyond span | Tree position; lexical form retained | `120` |
| `StringTerm` | String literal value; `StringTermModel` | `value: String [1]` | none beyond span | Tree position | message content string |
| `ListTerm` | Ordered list with optional tail; `ListTermModel` | — | `elements: Term [0..*]` ordered; `tail: Term [0..1]`; contained | Tree position and element order | `[A,B|Tail]` |
| `SetTerm` | Set syntax normalized in deterministic parser order; `SetTermModel` | — | `elements: Term [0..*]`, ordered containment | Tree position; equality/duplicate semantics are not strengthened beyond current IR | `{a,b}` |
| `ArithmeticTerm` | Unary/binary arithmetic expression; `ArithmeticTermModel` | `operator: String [1]` | `left`, `right: Term [0..1]`, contained; supported form requires at least one operand | Tree position | `Budget-120` |
| `UnsupportedTerm` | Recognized term without supported normalization; `UnsupportedTermModel` | — | `feature: UnsupportedFeature [1]`, contained | Feature source identity | Unsupported Jason term kind |

Enumerations exactly mirror current IR: `TriggerOperator={ADD, DELETE, GOAL_STATE}`, `TriggerType={BELIEF, ACHIEVE, TEST, SIGNAL}`, `UpdateOperator={ADD, DELETE, DELETE_AND_ADD}`, and `FocusPolicy={DEFAULT, NEW_FOCUS, BEGIN_FOCUS, END_FOCUS}`.

## 6. `mas` package metaclasses

| Metaclass | Purpose and source | Attributes | Relations, multiplicity, order, containment | Identity / static-runtime meaning | Example |
|---|---|---|---|---|---|
| `MasProject` | Static `.jcm` root; S4 root adapted to `MasProjectModel` | `name: String [1]` | `agents: AgentInstance [0..*]`, `resources: ProjectResourceReference [0..*]`, `organizations: Organization [0..*]`; ordered containment | Project source plus name; current constructor rejects duplicate agent and organization IDs. `STATIC_NORMALIZED_MODEL`; never launches runtime | `mas auction { ... }` |
| `AgentInstance` | One declared agent instance; `MasAgentInstanceModel` | `name: String [1]`, `status: AgentImportStatus [1]` | inherited `span [1]`; `normalizedAgent: bdi::Agent [0..1]`, non-containment derived by project/source identity | Name unique within project; `IMPORTED`, `MISSING`, or `INVALID` preserves composition outcome. Static declaration only | `agent auctioneer : auctioneer.asl` |
| `ProjectResourceReference` | Visible `.jcm` resource that may not be semantically normalized; `MasResourceReference` | `kind: ResourceKind [1]`, `name: String [1]`, `status: ResourceStatus [1]` | `span: SourceSpan [0..1]`, contained | Kind/name/source occurrence; status is `NORMALIZED`, `MISSING`, `INVALID`, or `UNSUPPORTED`. No full workspace/institution model is invented | Auction workspace/institution/organization declarations |

`ResourceKind={WORKSPACE, ORGANIZATION, INSTITUTION}` and the status enums exactly mirror current code. S4's semantic `Workspace` is not copied into `mas`: current project composition often retains workspace/institution as visible resource references, while the separate environment package represents only artifact evidence actually normalized by the CArtAgO adapter.

## 7. `environment` package metaclasses

All content is declaration-level. The profile deliberately flattens S4's `AbsOperation` subtype hierarchy because current `EnvironmentOperation` retains name, arity, parameter types, and guard—not authoritative subtype/port/link semantics.

| Metaclass | Purpose and source | Attributes | Relations, multiplicity, order, containment | Identity / static-runtime meaning | Example |
|---|---|---|---|---|---|
| `EnvironmentModel` | Deterministic collection of statically inspected artifacts; `EnvironmentModel` | — | `artifacts: Artifact [0..*]`, ordered containment; constructor sorts and rejects duplicate references | Analysis aggregate; `STATIC_DECLARATION` | Auction environment fixture |
| `Artifact` | One artifact instance/type in a workspace; S4 `Artifact`, current `ArtifactModel` | `workspace`, `instanceName`, `typeName: String [1]` | `operations: ArtifactOperation [0..*]`, `observableProperties: ObservableProperty [0..*]`; sorted ordered containment | `workspace/instanceName`; duplicate operation/property signatures rejected. Does not imply a live artifact | Auction board artifact |
| `ArtifactOperation` | Statically inspectable operation signature; flattened S4 operation classes, current `EnvironmentOperation` | `name: String [1]`, `arity: Int [1] >=0`, `parameterTypes: String [0..*]` ordered/non-unique, `guard: String [1]` | no references | `name/arity` within artifact; parameter count equals arity. Static retained metadata only | operation used by an Auction action mapping |
| `ObservableProperty` | Explicit property descriptor and optional reviewed value evidence; S4 `ObsProperty`, current `ObservablePropertyModel` | `name: String [1]`, `arity: Int [1] >=0`, `runtimeValuesAvailable: Boolean [1]`, `runtimeValues`, `evidence: String [0..*]` ordered/non-unique | no references | `name/arity` within artifact. If values unavailable, the flag is false and rules must return UNKNOWN where values are required; supplied values count equals arity | Auction property descriptor with no live value |

The `runtimeValuesAvailable` attribute is a specification-level encoding of Java `Optional<List<String>>`; it prevents empty-but-present values from being confused with unavailable evidence. It does not add runtime capture.

## 8. `organization` package metaclasses

Prior-work source is S4's Moise partition. Current realization is the bounded `OrganizationModel` nested records. All are `STATIC_NORMALIZED_MODEL`; no runtime organization entity, membership, mission commitment, or norm-fulfilment association is present.

| Metaclass | Purpose / current realization | Attributes | Relations, multiplicity, order, containment | Identity / boundary | Example |
|---|---|---|---|---|---|
| `Organization` | One normalized organization specification; `OrganizationModel` | `id: String [1]` | `roles`, `groups`, `schemes`, `norms`, `unsupportedFeatures [0..*]`; ordered containment | Source plus `id`; qualified child IDs unique per kind | Auction organization |
| `Role` | Static role definition; `OrganizationModel.Role` | `id`, `qualifiedId: String [1]` | inherited `span [1]` | `qualifiedId` | `auctioneer`, `participant` |
| `Group` | Static group and hierarchy position; `OrganizationModel.Group` | `id`, `qualifiedId: String [1]` | `parent: Group [0..1]` non-containment; `roleCardinalities: RoleCardinality [0..*]` ordered containment | `qualifiedId`; parent reference derives from stored parent qualified ID | `auctionGroup` |
| `RoleCardinality` | Role participation bound inside a group; `OrganizationModel.RoleCardinality` | — | `role: Role [1]` non-containment; `cardinality: Cardinality [1]` containment | Owning group + role qualified ID | participant `0..300` |
| `Cardinality` | Structurally valid lower/upper bound; `OrganizationModel.Cardinality` | `minimum`, `maximum: Int [1]`; Ecore uses `-1` for unbounded | none | Contained value object; requires `minimum>=0` and `maximum=-1` or `maximum>=minimum`. Java maps unbounded to `Integer.MAX_VALUE` | `1..1`, `0..300`, `1..*` |
| `Scheme` | Functional scheme; `OrganizationModel.Scheme` | `id`, `qualifiedId: String [1]` | `goals: OrganizationalGoal [0..*]`, `missions: Mission [0..*]`; ordered containment | `qualifiedId` | Auction scheme |
| `OrganizationalGoal` | Moise goal distinct from BDI `Goal`; `OrganizationModel.Goal` | `id`, `qualifiedId: String [1]` | inherited `span [1]` | `qualifiedId`; no implicit equality with an AgentSpeak goal | organizational `auction` goal |
| `Mission` | Mission with participation bounds and goal references; `OrganizationModel.Mission` | `id`, `qualifiedId: String [1]` | `cardinality: Cardinality [1]` contained; `goals: OrganizationalGoal [0..*]` ordered/non-unique non-containment | `qualifiedId`; Java stores goal qualified IDs | `mAuctioneer`, `mParticipant` |
| `Norm` | Static permission/obligation binding a role to a mission; `OrganizationModel.Norm` | `id`, `qualifiedId: String [1]`, `type: NormType [1]` | `role: Role [1]`, `mission: Mission [1]`, non-containment | `qualifiedId`; static specification only | `n2`: participant obligation for `mParticipant` |

`NormType={PERMISSION, OBLIGATION}` mirrors current code. Formation constraints, role inheritance/links, layered `OPlan`, full Moise behavior, and enactment remain unsupported/out of scope; recognized unsupported details are retained through `UnsupportedFeature` where current normalization provides them.

## 9. Structural constraints and enforcement status

| ID | Profile constraint | Source authority | Current enforcement |
|---|---|---|---|
| MM-001 | Every supported `Plan` has exactly one valid `Trigger`. | Jason syntax/current `PlanModel`; compatible with S2/S4 | Constructor non-null plus parser/IR tests |
| MM-002 | `Plan.steps` preserves deterministic source order. | Jason plan-body semantics/current list | Immutable list, serializer/index/diagram ordering tests |
| MM-003 | `Plan.context` is optional. | Supported Jason source/current `Optional`; deliberate deviation from S4 Figure 1 | `PlanModel` optional and importer tests |
| MM-004 | Plan labels expected unique within an imported agent when labels exist; duplicates are deterministic evidence. | Current index/rule policy | Duplicate-plan index and `BDI-001` behavior, not constructor rejection |
| MM-005 | Term/context/step trees are acyclic containment trees. | Normalized AST structure | Immutable construction; Ecore containment graph has no classifier cycle that forces recursive ownership outside explicit expression recursion |
| MM-006 | Persisted/exposed source identity is project-relative and traversal-safe; coordinates may be unknown. | `ProjectSourceId` policy | Constructor/parser/relocation/serialization tests |
| MM-007 | `ArtifactOperation.arity >= 0` and equals parameter-type count. | Current adapter/model | `EnvironmentOperation` constructor/tests |
| MM-008 | Artifact reference and child signatures are deterministic and unique. | Current model | `EnvironmentModel`/`ArtifactModel` sorting and duplicate rejection |
| MM-009 | Organization cardinality has `min>=0` and upper unbounded or `upper>=min`. | Moise/current organization IR | `Cardinality` constructor/parser tests |
| MM-010 | Agent-instance names and organization IDs are unique within one `MasProject`. | Current project IR | `MasProjectModel` constructor/tests |
| MM-011 | Missing/invalid/unsupported resources remain visible through status/diagnostics and never become invented normalized models. | Adapter conservatism | project import diagnostics/status tests |
| MM-012 | Lack of live CArtAgO or Moise enactment evidence cannot produce runtime PASS. | Research/runtime boundary | ENV/ORG UNKNOWN rule tests and catalogs |

Goal-support, signature/namespace, UML mapping, OCL, issue, and trace constraints are intentionally not profile well-formedness constraints; Tasks 05 and 07 formalize those analysis/correspondence semantics.

## 10. Concept harvesting and rationale

| Harvest class | Concepts | Decision |
|---|---|---|
| Prior-work ∩ implementation | Agent, Belief, Goal, Plan, Trigger, Context, Body/Step, internal/external action, workspace reference, artifact, operation, observable property, organization, group, role, scheme, mission, organizational goal, norm, cardinality | Included with adaptations documented above |
| Prior-work only | First-class Rule, Message, MentalNotes, Body linked list, Port, operation subtype hierarchy, full Workspace, formation/link semantics, OPlan, enactment | Excluded or explicit coverage gap; no fabricated metaclass instance |
| Implementation only / finer normalization | Project/resource status, source identity/span, unsupported nodes, typed context subclasses, typed plan-step subclasses, term hierarchy, declared counts, value-evidence availability | Included because RQ1/RQ2 require conservative import, deterministic identity, signature/reference analysis, UNKNOWN semantics, or source-order evidence |

## 11. Out-of-scope package boundary

The following must not be added to this `.ecore` package family: UML class/object/attribute/operation or OCL constraint; mapping candidate/confirmed/staleness records; `ConsistencyIssue`; trace/diagram node or edge; Swing state; reports; Jason interpreter agents; JaCaMo runtime services; CArtAgO live workspaces; Moise enactment entities. Their relationships to this profile are separate application/correspondence/presentation concerns.

## 12. Versioning

The profile namespace is stable for version 1 (`.../analysis/1.0`) and the artifact metadata version is `1.0.0`. A breaking classifier/feature change requires a new namespace or an explicit migration decision. Production snapshots and reports expose this identity through `AnalysisMetamodelDescriptor`; the independent normalized Java IR/index contract remains `BdiMetamodelVersion` `0.1.0`.
