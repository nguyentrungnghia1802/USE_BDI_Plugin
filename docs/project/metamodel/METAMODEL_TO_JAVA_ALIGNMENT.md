# JaCaMo Analysis Profile to Java IR Alignment

Status: **NORMATIVE CONFORMANCE MATRIX — ZERO UNRESOLVED GAPS**

Profile: [JaCaMo Consistency Analysis Profile 1.0](USE_JACAMO_ANALYSIS_METAMODEL.md)

This document proves realization, not EMF object identity. Production records
do not implement `EObject`; the versioned Ecore remains a specification
artifact and the immutable plugin-owned Java values remain executable truth.

## 1. Status vocabulary

| Status | Meaning |
|---|---|
| `EXACT` | Java type/feature has the same concept and shape. |
| `REALIZED_WITH_DIFFERENT_NAME` | Concept is direct but Java follows an established project name. |
| `FLATTENED` | One profile concept/reference is represented across records, strings, optionals, or nested values. |
| `DERIVED` | Relation/value is computed from immutable aggregate identity rather than stored as an object reference. |
| `DIAGNOSTIC_ONLY` | Invalid/unsupported evidence exists outside an instance that could not safely normalize. |
| `DECLARATION_ONLY` | Java retains static declaration evidence with no runtime claim. |
| `OUT_OF_SCOPE` | Technical/application concern deliberately outside the domain profile. |

No row has unresolved `GAP` status. Deliberate differences are classified and
tested below.

## 2. Metaclass alignment matrix

Package abbreviations: `ir` = `org.tzi.use.plugins.bdi.model.ir`, `mas` =
`...model.mas`, `env` = `...model.environment`, `org` =
`...model.organization`, and `source` = `...model.source`.

### 2.1 Evidence package

| Metaclass/feature | Java package/type | Producer | Cardinality realization | Identity | Tests | Status | Notes |
|---|---|---|---|---|---|---|---|
| `SourceLocatedElement` | factored across `ir.SourceSpan` fields and `org.OrganizationModel.SourceSpan` | all source adapters | Every source-backed normalized BDI/organization record requires a nonnull span; MAS resources use optional source identity | portable identity is derived at persistence/trace boundaries | `IrHierarchyTest`, organization golden tests | `FLATTENED` | A Java marker interface would add no semantic value. Environment declarations are not profile `SourceLocatedElement` subclasses. |
| `ProjectSourceIdentity` | `source.ProjectSourceId` | project/import services and adapter root relativization | exactly one canonical project path when exposed/persisted; MAS resource source may be absent | v2 normalized project-relative path | `ProjectSourceIdTest`, MAS relocation test | `REALIZED_WITH_DIFFERENT_NAME` | In-session `ir.SourceSpan` may hold an absolute `Path`; it is not persisted semantic identity. |
| `SourceSpan` | `ir.SourceSpan`; `org.OrganizationModel.SourceSpan` | Jason and Moise normalizers | source `[1]`, four nonnegative coordinates; all zero represents unknown | source identity plus coordinates | source-location, relocation, organization tests | `FLATTENED` | Two Java records avoid coupling BDI and organization packages; both preserve unknown coordinates explicitly. |
| `UnsupportedFeature` | `ir.UnsupportedFeature`; `org.OrganizationModel.UnsupportedFeature`; MAS diagnostic/resource status at project boundary | Jason/Moise normalizers and project service | BDI/organization lists `[0..*]`; top-level invalid/project-only evidence may remain diagnostic | span + code/kind/subject or qualified element | unsupported fixture, Moise boundary, MAS project tests | `FLATTENED` | Loss-preserving evidence exists without forcing malformed top-level model instances. |

### 2.2 BDI package

| Metaclass/feature | Java package/type | Producer | Cardinality realization | Identity | Tests | Status | Notes |
|---|---|---|---|---|---|---|---|
| `Agent` | `ir.AgentModel` | `JasonAslParserAdapter` → `JasonAstToIrNormalizer` | beliefs/goals/plans/unsupported lists are nonnull immutable `[0..*]`; declared counts nonnegative | source file identity, not invented agent name | importer, IR/golden, canonical fixture contract | `REALIZED_WITH_DIFFERENT_NAME` | Parser version and declared counts support summary/partial evidence. |
| `Belief` | `ir.BeliefModel` | Jason normalizer | literal `[1]`, span `[1]` | source occurrence | IR hierarchy/index tests | `REALIZED_WITH_DIFFERENT_NAME` | Static initial belief only. |
| `Goal` | `ir.GoalModel` | Jason normalizer | literal `[1]`, span `[1]` | source occurrence | IR hierarchy/index tests | `REALIZED_WITH_DIFFERENT_NAME` | Distinct from organization goal. |
| `Plan` | `ir.PlanModel` | Jason normalizer | trigger `[1]`, context `Optional` `[0..1]`, immutable ordered steps `[0..*]` | source occurrence; label secondary | IR hierarchy, index, validation tests | `REALIZED_WITH_DIFFERENT_NAME` | Constructor rejects null; duplicate labels are diagnosed by index/rule rather than constructor. |
| `Trigger` | `ir.TriggerModel` | Jason normalizer | operator/type/term/span all `[1]` | owning plan + span | IR hierarchy/index tests | `REALIZED_WITH_DIFFERENT_NAME` | Nonliteral term can survive for `BDI-003` evidence. |
| `Context` | sealed `ir.ContextExpr` | Jason normalizer | plan owns `Optional<ContextExpr>`; recursive children nonnull | tree position + span | `IrHierarchyTest`, Smart Queue fixture | `REALIZED_WITH_DIFFERENT_NAME` | Abstract interface realizes profile abstract class. |
| `LiteralContext` | `ir.ContextLiteral` | Jason normalizer | literal `[1]`, span `[1]` | context tree position | hierarchy/context rule tests | `REALIZED_WITH_DIFFERENT_NAME` | — |
| `UnaryContext` | `ir.ContextUnary` | Jason normalizer | operator `[1]`, operand `[1]` | context tree position | hierarchy/context tests | `REALIZED_WITH_DIFFERENT_NAME` | — |
| `BinaryContext` | `ir.ContextBinary` | Jason normalizer | operator/left/right `[1]`; side fields preserve order | context tree position | hierarchy/context tests | `REALIZED_WITH_DIFFERENT_NAME` | — |
| `UnsupportedContext` | `ir.ContextUnsupported` | Jason normalizer | feature `[1]`, span retained through feature/context | tree position + evidence | unsupported fixture/hierarchy tests | `REALIZED_WITH_DIFFERENT_NAME` | Downstream translation does not invent a predicate. |
| `Step` | sealed `ir.PlanStepModel` | Jason normalizer | plan list `[0..*]`, immutable and ordered | owning plan + zero-based list position + span | hierarchy/index/order tests | `REALIZED_WITH_DIFFERENT_NAME` | Abstract interface; index exposes one-based call-site step number as derived technical API. |
| `ExternalAction` | `ir.ActionStepModel` | Jason normalizer | action term `[1]`, span `[1]` | step occurrence | hierarchy/index/Smart Queue tests | `REALIZED_WITH_DIFFERENT_NAME` | “Action” Java name predates canonical profile name. |
| `InternalAction` | `ir.InternalActionStepModel` | Jason normalizer | action term `[1]` | step occurrence | hierarchy/index tests | `REALIZED_WITH_DIFFERENT_NAME` | `.send` remains an internal-action term. |
| `GoalCall` | `ir.AchieveGoalStepModel` | Jason normalizer | goal literal `[1]`, `newFocus` boolean `[1]` | step occurrence | hierarchy/index/goal-support tests | `REALIZED_WITH_DIFFERENT_NAME` | Established Java name describes achievement-goal semantics. |
| `TestStep` | `ir.TestStepModel` | Jason normalizer | condition `[1]` | step occurrence | hierarchy/index tests | `REALIZED_WITH_DIFFERENT_NAME` | Model suffix only. |
| `BeliefUpdate` | `ir.BeliefUpdateStepModel` | Jason normalizer | operator/focus/belief/span `[1]` | step occurrence | hierarchy/index tests | `REALIZED_WITH_DIFFERENT_NAME` | Static instruction, not runtime belief state. |
| `ConstraintStep` | `ir.ConstraintStepModel` | Jason normalizer | condition `[1]` | step occurrence | hierarchy/index tests | `REALIZED_WITH_DIFFERENT_NAME` | — |
| `UnsupportedStep` | `ir.UnsupportedStepModel` | Jason normalizer | feature `[1]` | step occurrence + evidence | unsupported/hierarchy tests | `REALIZED_WITH_DIFFERENT_NAME` | Preserves order and explicit unsupported evidence. |
| `Term` | sealed `ir.TermModel` | Jason normalizer | every concrete term requires span; recursive lists immutable | tree position + span | `IrHierarchyTest`, golden serializer | `REALIZED_WITH_DIFFERENT_NAME` | Abstract interface realizes profile abstract class. |
| `LiteralTerm` | `ir.LiteralTermModel` | Jason normalizer | functor/negation `[1]`; ordered arguments/annotations `[0..*]` | tree position | hierarchy/index tests | `REALIZED_WITH_DIFFERENT_NAME` | Repeated structurally equal occurrences remain distinct values by containment position. |
| `CompoundTerm` | `ir.CompoundTermModel` | Jason normalizer | functor `[1]`, ordered arguments `[0..*]` | tree position | hierarchy/index tests | `REALIZED_WITH_DIFFERENT_NAME` | — |
| `VariableTerm` | `ir.VariableTermModel` | Jason normalizer | variable name `[1]` | tree position | hierarchy/type tests | `REALIZED_WITH_DIFFERENT_NAME` | Unknown static type remains explicit. |
| `NumberTerm` | `ir.NumberTermModel` | Jason normalizer | lexical value `[1]` | tree position | hierarchy/type tests | `REALIZED_WITH_DIFFERENT_NAME` | Lexical preservation supports deterministic rendering. |
| `StringTerm` | `ir.StringTermModel` | Jason normalizer | value `[1]` | tree position | hierarchy/type tests | `REALIZED_WITH_DIFFERENT_NAME` | — |
| `ListTerm` | `ir.ListTermModel` | Jason normalizer | ordered elements `[0..*]`, tail `Optional` `[0..1]` | tree position | hierarchy/normalizer tests | `REALIZED_WITH_DIFFERENT_NAME` | — |
| `SetTerm` | `ir.SetTermModel` | Jason normalizer | ordered retained elements `[0..*]` | tree position | hierarchy/normalizer tests | `REALIZED_WITH_DIFFERENT_NAME` | Retained deterministic order does not claim runtime set order. |
| `ArithmeticTerm` | `ir.ArithmeticTermModel` | Jason normalizer | operator `[1]`, left/right independently optional | tree position | hierarchy/normalizer tests | `REALIZED_WITH_DIFFERENT_NAME` | Optional operands preserve partial supported evidence. |
| `UnsupportedTerm` | `ir.UnsupportedTermModel` | Jason normalizer | feature `[1]` | tree position + evidence | unsupported/hierarchy tests | `REALIZED_WITH_DIFFERENT_NAME` | Never silently converted to a supported term. |

`Rule` and `Message` are deliberately not v1 classifiers: AgentSpeak rules
have no first-class Java IR record, and messages are `.send` internal-action
terms. This is `OUT_OF_SCOPE`/flattened behavior already recorded in profile
coverage, not an alignment gap.

### 2.3 MAS package

| Metaclass/feature | Java package/type | Producer | Cardinality realization | Identity | Tests | Status | Notes |
|---|---|---|---|---|---|---|---|
| `MasProject` | `mas.MasProjectModel` | `JaCaMoProjectParserAdapter` → `MasProjectImportService` | source/name `[1]`; immutable agents/resources/organizations `[0..*]`; duplicate agent/org IDs rejected | `ProjectSourceId` + name | MAS analysis/relocation/serializer tests | `REALIZED_WITH_DIFFERENT_NAME` | Static normalized model only. |
| `AgentInstance` | `mas.MasAgentInstanceModel` | project adapter/import service | name/source/status `[1]`; normalized agent association is derived in aggregate analysis by source | name unique within project + portable source | MAS import/analysis tests | `REALIZED_WITH_DIFFERENT_NAME` | `normalizedAgent [0..1]` is not stored to avoid a mutable/cyclic project graph. |
| `ProjectResourceReference` | `mas.MasResourceReference` | project adapter/import service | kind/name/status `[1]`; source `Optional [0..1]` | kind + name + optional portable source | JaCaMo parser/MAS service tests | `REALIZED_WITH_DIFFERENT_NAME` | Workspace/institution/unsupported organization declarations remain visible without full workspace semantics. |

Project parse/import diagnostics are `DIAGNOSTIC_ONLY` application evidence.
They remain outside a project instance when normalization is unsafe and preserve
partial success for other agents/resources.

### 2.4 Environment package

| Metaclass/feature | Java package/type | Producer | Cardinality realization | Identity | Tests | Status | Notes |
|---|---|---|---|---|---|---|---|
| `EnvironmentModel` | `env.EnvironmentModel` | `CArtAgOArtifactAdapter` plus explicit descriptor composition | immutable sorted artifacts `[0..*]`; duplicate references rejected | artifact reference collection | adapter/environment tests | `EXACT` | Declaration-only aggregate; no live workspace. |
| `Artifact` | `env.ArtifactModel` | CArtAgO adapter/composition | workspace/instance/type `[1]`; sorted operation/property lists `[0..*]`; duplicate signatures rejected | `workspace/instanceName` | adapter/environment tests | `REALIZED_WITH_DIFFERENT_NAME` | Workspace is an identity string, not a semantic runtime object. |
| `ArtifactOperation` | `env.EnvironmentOperation` | CArtAgO retained `@OPERATION` metadata adapter | name/arity/guard `[1]`, parameter type list size equals arity | name/arity within artifact | `CArtAgOArtifactAdapterTest`, environment mutants | `REALIZED_WITH_DIFFERENT_NAME` | Static annotation metadata only. |
| `ObservableProperty` | `env.ObservablePropertyModel` | explicit static descriptor/fixture composition | name/arity `[1]`; runtime values `Optional [0..1]` with size=arity; evidence `[0..*]` | name/arity within artifact | environment baseline/unknown/persistence tests | `FLATTENED` | Profile boolean `runtimeValuesAvailable` derives from `Optional.isPresent`; absent remains UNKNOWN. |

### 2.5 Organization package

| Metaclass/feature | Java package/type | Producer | Cardinality realization | Identity | Tests | Status | Notes |
|---|---|---|---|---|---|---|---|
| `Organization` | `org.OrganizationModel` | `MoiseOrganizationParserAdapter` | source/span/id `[1]`; roles/groups/schemes/norms/unsupported `[0..*]`; qualified duplicates rejected | organization ID + portable source | Moise golden/boundary, Auction organization tests | `REALIZED_WITH_DIFFERENT_NAME` | Static normalized specification only. |
| `Role` | `org.OrganizationModel.Role` | Moise adapter | id/qualifiedId/span `[1]` | qualified ID | organization tests | `FLATTENED` | Nested record keeps bounded organization domain cohesive. |
| `Group` | `org.OrganizationModel.Group` | Moise adapter | id/qualifiedId/span `[1]`; role cardinalities `[0..*]`; parent stored as nullable qualified ID | qualified ID | organization tests | `FLATTENED` | Profile parent reference is derived from `parentQualifiedId`; unresolved parent stays adapter evidence. |
| `RoleCardinality` | `org.OrganizationModel.RoleCardinality` | Moise adapter | role qualified ID/cardinality `[1]` | owning group + role ID | organization tests | `FLATTENED` | Profile role reference derives from the stable string ID. |
| `Cardinality` | `org.OrganizationModel.Cardinality` | Moise adapter | minimum/maximum `[1]`; constructor requires `0 <= min <= max`; unbounded uses `Integer.MAX_VALUE` | value object | organization constructor/rule tests | `EXACT` | Profile enum-free integer representation maps unbounded sentinel explicitly. |
| `Scheme` | `org.OrganizationModel.Scheme` | Moise adapter | id/qualifiedId/span `[1]`; unique goals/missions `[0..*]` | qualified ID | Moise/Auction tests | `FLATTENED` | Nested record. |
| `OrganizationalGoal` | `org.OrganizationModel.Goal` | Moise adapter | id/qualifiedId/span `[1]` | qualified ID | Moise/Auction tests | `REALIZED_WITH_DIFFERENT_NAME` | Java nesting disambiguates it from BDI `GoalModel`. |
| `Mission` | `org.OrganizationModel.Mission` | Moise adapter | id/qualifiedId/cardinality/span `[1]`; ordered goal qualified IDs `[0..*]` | qualified ID | Moise/Auction tests | `FLATTENED` | Profile goal references derive from stable qualified-ID strings. |
| `Norm` | `org.OrganizationModel.Norm` | Moise adapter | id/qualifiedId/type/role ID/mission ID/span `[1]` | qualified ID | Moise/Auction tests | `FLATTENED` | Role/mission references derive from qualified IDs; no fulfilment state. |

## 3. Enum alignment

| Profile enum | Java realization | Status | Note |
|---|---|---|---|
| `TriggerOperator` | `ir.TriggerModel.TriggerOperator` | `EXACT` | `ADD`, `DELETE`, `GOAL_STATE`. |
| `TriggerType` | `ir.TriggerModel.TriggerType` | `EXACT` | `BELIEF`, `ACHIEVE`, `TEST`, `SIGNAL`. |
| `UpdateOperator` | `ir.BeliefUpdateStepModel.UpdateOperator` | `EXACT` | Same literals. |
| `FocusPolicy` | `ir.BeliefUpdateStepModel.FocusPolicy` | `EXACT` | Same literals. |
| `AgentImportStatus` | `mas.MasAgentImportStatus` | `REALIZED_WITH_DIFFERENT_NAME` | Same literals. |
| `ResourceKind` | `mas.MasResourceKind` | `REALIZED_WITH_DIFFERENT_NAME` | Static declaration kinds. |
| `ResourceStatus` | `mas.MasResourceStatus` | `REALIZED_WITH_DIFFERENT_NAME` | Same bounded status role. |
| `NormType` | `org.OrganizationModel.NormType` | `EXACT` | Permission/obligation only. |

## 4. Feature-level deviations and multiplicity decisions

| Profile feature | Java realization | Decision |
|---|---|---|
| `SourceLocatedElement.span [1]` | nonnull spans on normalized BDI/organization source elements | The profile models normalized instances. Files that cannot normalize produce diagnostics outside malformed objects. |
| `Agent` declared counts vs contained lists | independent nonnegative count fields plus immutable materialized lists | Summary/partial evidence is permitted; detailed consumers verify counts where required rather than fabricating nodes. |
| `Plan.trigger [1]` | constructor-nonnull `TriggerModel` | Exact for normalized plans; parser failure remains diagnostic-only. |
| ordered multi-valued containment | `List.copyOf` and source traversal order; environment values sort by stable signature | BDI/MAS preserve source order; environment uses deterministic canonical order because source reflection order is not authority. |
| uniqueness | duplicate agent/org/artifact/signature/qualified IDs rejected; duplicate plan labels diagnosed | Matches the profile's source-authority notes without losing plan evidence. |
| `AgentInstance.normalizedAgent [0..1]` | derived by aggregate source association | Avoids duplicated/cyclic ownership and preserves immutable separately reusable agent models. |
| `ObservableProperty.runtimeValuesAvailable` | `runtimeValues.isPresent()` | Exact information content with a Java `Optional`; empty-present remains distinguishable from unavailable. |
| organization cross-references | qualified-ID strings resolved by validators/builders | Stable, portable, serialization-friendly representation; no runtime EObject graph required. |
| unknown coordinates | all-zero spans; MAS resource source may be absent | Unknown is explicit and never replaced by a fake positive line/column. |

Null is forbidden for record fields and collection containers. Empty immutable
collections realize `[0..*]`. Optionals or documented nullable qualified IDs
realize `[0..1]`. All differences above preserve the profile's evidence and
static/runtime boundary.

## 5. Producer and parser boundary

```text
AgentSpeak text
  -> JasonAslParserAdapter / JasonAstToIrNormalizer
  -> model.ir + BdiIndex

.jcm
  -> JaCaMoProjectParserAdapter / MasProjectImportService
  -> model.mas (+ normalized agent/organization aggregates)

CArtAgO class metadata
  -> CArtAgOArtifactAdapter
  -> model.environment

Moise XML
  -> MoiseOrganizationParserAdapter
  -> model.organization
```

Jason, JaCaMo, CArtAgO, and Moise concrete imports occur only under the
`importer` adapter package. Model, index, validation, trace, diagram, report,
and USE-boundary packages consume plugin-owned Java values. Input order,
partial success, unsupported evidence, and unknown coordinates are retained;
canonical collections are deterministic.

## 6. Identity conformance

- `ProjectSourceId` v2 is the persisted/exposed portable identity authority.
- Local absolute `Path` in the in-memory AgentSpeak `SourceSpan` locates the
  checkout file for navigation; serializers, mapping sources, trace, reports,
  and relocation comparisons normalize it against an explicit project root.
- Unknown source positions are encoded as zero coordinates or absent optional
  source, never guessed.
- Mapping source IDs reuse portable source/signature/structural positions.
- Diagram node identity is a separate presentation concern and is not added to
  the domain profile.
- `ProjectSourceIdTest`, MAS relocation, trace relocation, mapping relocation,
  and diagram portability tests remain the executable evidence.

## 7. Canonical fixture conformance

| Fixture | Produced concepts/evidence | Boundary demonstrated | Executable evidence |
|---|---|---|---|
| minimal direct ASL | agent, initial belief/goal, literal achievement trigger, optional context, plan, internal-action step | Jason types stop at adapter; normalized model/index are immutable | `MetamodelIrAlignmentContractTest`, importer/index tests |
| Smart Queue | nested contexts, ordered internal/external/goal/test/belief-update steps, terms, source spans, confirmed agent/action mapping sidecar | static normalized BDI + separate correspondence; no runtime claim | alignment contract, canonical demo and mapping tests |
| Auction `.jcm` | MAS root, three agent instances over two sources, resource references, normalized organization, partial unsupported project diagnostics | static JaCaMo/Moise composition; portable relocation | alignment contract, MAS analysis/relocation and Auction tests |
| Smart Home | `.jcm` workspace/organization declarations and BDI mapping sidecar | workspace remains `ProjectResourceReference`; it is not fabricated as an `Artifact` | project/demo tests |
| explicit CArtAgO adapter fixture | artifact operation metadata and optional property descriptor evidence | declaration-only environment IR; no live values | `CArtAgOArtifactAdapterTest`, Auction environment tests |

Smart Home does not contain authoritative artifact class metadata, so it is not
used to claim `ArtifactOperation`/`ObservableProperty` production. The explicit
adapter/environment fixtures own that proof.

Smart Queue contains `.print` internal actions and indexed action/object
references but no `.send` occurrence. Message-receiver normalization is proven
by `BdiIndexTest` and `ValidationOrchestratorTest`; the fixture matrix does not
fabricate a message concept absent from the demo.

## 8. Reverse audit: implementation-only concerns

| Java concern | Classification | Rationale |
|---|---|---|
| `AslDiagnostic`, `MasProjectDiagnostic`, import reports/results | `DIAGNOSTIC_ONLY` | Application evidence for inputs that may not form valid profile instances and for partial success. |
| `BdiIndex`, signatures, call sites, duplicate-label records | `DERIVED` | Immutable analysis acceleration/evidence, not source abstract syntax. |
| mapping, issue, suppression, trace, diagram, report, Swing values | `OUT_OF_SCOPE` | Separate correspondence/analysis/presentation/application concerns formalized by later tasks. |
| serializers and version constants | `OUT_OF_SCOPE` technical realization | Persistence/metadata mechanism rather than JaCaMo domain concepts. |

Every semantically relevant domain IR type is covered by the metaclass/enum
tables or an explicit profile exclusion. No unresolved alignment gap remains.
