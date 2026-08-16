# Analysis Metamodel Coverage Evaluation

Date: 2026-08-17

Profile: `JaCaMo Consistency Analysis Profile` `1.0.0`

Corpus: the four canonical demos (Family Person, Smart Queue, Smart Home, and
Auction), the reviewed five-case Auction evaluation corpus, and focused
negative/adapter fixtures identified below.

## Counted domain coverage

The Ecore artifact contains 48 in-profile EClasses. Counts are unweighted and
are reported by package and declared support class; they are not a percentage
claim about semantic completeness.

| Package | EClasses | Supported | Declaration-only | Partial | Out of scope |
|---|---:|---:|---:|---:|---:|
| Evidence/source identity | 4 | 4 | 0 | 0 | 0 |
| BDI | 28 | 28 | 0 | 0 | 0 |
| MAS project | 3 | 2 | 1 | 0 | 0 |
| Environment | 4 | 0 | 3 | 1 | 0 |
| Organization | 9 | 9 | 0 | 0 | 0 |
| **Total** | **48** | **43** | **4** | **1** | **0** |

UML/OCL, correspondence lifecycle, issues, trace/diagram state, report DTOs,
and runtime JaCaMo/CArtAgO/Moise behavior are intentionally outside this domain
profile and are counted separately in the normative
[`METAMODEL_COVERAGE.md`](../metamodel/METAMODEL_COVERAGE.md). No excluded
concept is added to the denominator to improve or depress the result.

## Per-metaclass realization and evidence anchors

The table identifies every profile EClass. Detailed feature/cardinality,
identity, producer, Java type, test, diagram, and rule references remain in the
normative [`METAMODEL_TO_JAVA_ALIGNMENT.md`](../metamodel/METAMODEL_TO_JAVA_ALIGNMENT.md)
and [`METAMODEL_COVERAGE.md`](../metamodel/METAMODEL_COVERAGE.md); this evaluation
does not duplicate those 48 feature-level rows.

Fixture keys: `ALL` = all canonical demos; `SQ` = Smart Queue; `AUC` = Auction;
`FP` = Family Person; `SH` = Smart Home; `NEG` = focused unsupported/typed-IR
fixtures; `CART` = the static CArtAgO adapter fixture. Test names are executable
anchors, not manual ticks.

| Metaclass(es) | Status | Fixture / producer | Java and executable test anchor | Rule / diagram evidence |
|---|---|---|---|---|
| `SourceLocatedElement`, `ProjectSourceIdentity`, `SourceSpan` | supported | ALL; all adapters/project normalization | `SourceSpan`, `ProjectSourceId`; `ProjectSourceIdTest`, `MetamodelIrAlignmentContractTest` | all source-bound rules; portable source nodes |
| `UnsupportedFeature` | supported | NEG + AUC `.jcm`; Jason/Moise normalizers | BDI/organization unsupported records; `UnsupportedFixtureTest`, `MoiseOrganizationParserAdapterTest` | `ASL-002`/project diagnostics; gap evidence |
| `Agent`, `Belief`, `Goal`, `Plan`, `Trigger` | supported | FP/SQ/AUC; Jason normalizer | corresponding `*Model` records; `AgentModelTest`, `IrHierarchyTest`, importer tests | BDI/MAP families; agent/belief/goal/plan/trigger paths |
| `Context`, `LiteralContext`, `UnaryContext`, `BinaryContext` | supported | SQ/AUC; Jason normalizer | sealed `ContextExpr` hierarchy; `IrHierarchyTest`, context/validation tests | `CTX-001`, `REF-002`, `OCL-002`; context nodes |
| `UnsupportedContext` | supported | NEG `relational-context.asl`; Jason normalizer | `ContextUnsupported`; `UnsupportedFixtureTest`, golden fixture | `ASL-002`, downstream UNKNOWN; unsupported evidence |
| `Step`, `ExternalAction`, `InternalAction`, `GoalCall`, `TestStep`, `BeliefUpdate`, `ConstraintStep` | supported | FP/SQ/AUC + typed NEG; Jason normalizer | sealed `PlanStepModel` hierarchy; `IrHierarchyTest`, `BdiIndexTest`, `BdiDiagramBuilderTest` | BDI/MAP/SIG/REF/MSG/ENV families; ordered body paths |
| `UnsupportedStep` | supported | NEG typed fixture; Jason normalizer | `UnsupportedStepModel`; `IrHierarchyTest`, alignment contract | `ASL-002`/`BDI-004`; explicit unsupported step |
| `Term`, `LiteralTerm`, `CompoundTerm`, `VariableTerm`, `NumberTerm`, `StringTerm` | supported | FP/SQ/AUC; Jason normalizer | sealed `TermModel` hierarchy; `IrHierarchyTest`, parser/index tests | reference/signature/type/context evidence and labels |
| `ListTerm`, `SetTerm`, `ArithmeticTerm`, `UnsupportedTerm` | supported | NEG typed/parser fixtures; Jason normalizer | corresponding term records; `IrHierarchyTest`, alignment contract | recursive index/type evidence; unsupported stays explicit |
| `MasProject`, `AgentInstance` | supported | FP/SH/AUC `.jcm`; JaCaMo adapter/import service | `MasProjectModel`, `MasAgentInstanceModel`; `MasProjectImportServiceTest`, `MasProjectAnalysisServiceTest` | project prerequisites; MAS/agent nodes |
| `ProjectResourceReference` | declaration-only | FP/SH/AUC `.jcm`; JaCaMo adapter | `MasResourceReference`; MAS project golden/relocation tests | resource diagnostics; static resource nodes |
| `EnvironmentModel`, `Artifact`, `ArtifactOperation` | declaration-only | CART plus SH/AUC declarations; CArtAgO adapter | environment records; `CArtAgOArtifactAdapterTest`, environment rule tests | `ENV-001..004`; environment/artifact/operation layer |
| `ObservableProperty` | partial | CART explicit descriptor; CArtAgO adapter | `ObservablePropertyModel`; adapter/environment tests | `ENV-003..004`; no live value stream |
| `Organization`, `Role`, `Group`, `RoleCardinality`, `Cardinality` | supported static subset | FP/SH/AUC organization XML; Moise adapter | nested organization records; Moise golden and `AuctionOrganizationConsistencyTest` | `ORG-001`, `ORG-003`; organization/role/group/cardinality paths |
| `Scheme`, `OrganizationalGoal`, `Mission`, `Norm` | supported static subset | FP/SH/AUC organization XML; Moise adapter | nested organization records; Moise golden and organization tests | `ORG-002..003`/structural evidence; scheme/goal/mission/norm projection |

## Implementation conformance dimensions

| Dimension | Result | Interpretation |
|---|---:|---|
| EClass inventory classified | 48/48 | every EClass appears in the tables above and in the normative coverage matrix |
| Java alignment rows | 48/48 | 47 concrete/nested Java types load; abstract `SourceLocatedElement` is deliberately flattened |
| Unresolved alignment gaps | 0 | adaptations are named `FLATTENED`, `DERIVED`, `DIAGNOSTIC_ONLY`, or `DECLARATION_ONLY` |
| Producer ownership identified | 48/48 | Jason, JaCaMo, CArtAgO, Moise, or project/source normalization owns each row |
| Executable test anchor identified | 48/48 | anchors include canonical, negative, adapter, alignment, and rule tests |

This is structural/trace coverage, not “100% semantic coverage.” In particular,
the single partial observable-property concept has no live values; environment
entries are declaration-only; organization evidence is static and has no
enactment; unsupported term/context/step variants preserve loss evidence rather
than claiming complete AgentSpeak normalization.

## Reproduction

`MetamodelProfileArtifactTest` verifies the 48-class/five-package Ecore
inventory. `MetamodelIrAlignmentContractTest` verifies every alignment name,
the 47 loadable Java realizations, zero `GAP` rows, and canonical producer
fixtures. The full Task 10 focused and reactor commands are recorded in the
task evidence and were run against this same profile descriptor.
