# Static Semantics of the JaCaMo Consistency Analysis Profile

Status: **NORMATIVE EXPLANATION OF CURRENT EXECUTABLE BEHAVIOR**

Profile: [JaCaMo Consistency Analysis Profile 1.0](USE_JACAMO_ANALYSIS_METAMODEL.md)

Executable authority: `StandardConsistencyRules`, `ValidationOrchestrator`,
`EnvironmentMappingValidationService`, `EnvironmentConsistencyValidator`, and
`OrganizationConsistencyValidator`.

## 1. Authority and outcome model

This specification classifies and explains the existing Java rule engine; it
does not introduce a second validator. The profile supplies abstract-syntax
vocabulary, while Java remains the executable realization. No rule is moved to
OCL or EVL, and the 22 configured standard IDs remain separate from the four
environment and three organization extension IDs.

Standard rules emit zero or more immutable `ConsistencyIssue` values. They do
not emit a per-rule PASS object. Therefore the following vocabulary is used:

| Semantic outcome | Executable representation | Interpretation |
|---|---|---|
| `NO_FINDING` | no issue after the rule's prerequisites were available | The bounded check found no violation; this is not a general correctness proof. |
| `CONFIRMED_FINDING` | issue certainty `CONFIRMED` | Available evidence establishes the declared violation within the rule boundary. |
| `POTENTIAL_FINDING` | issue certainty `POTENTIAL` | Static evidence suggests a problem but is insufficient for confirmation. |
| `UNKNOWN_FINDING` | issue certainty `UNKNOWN` | The check explicitly records unavailable/undecidable evidence. It is never PASS. |
| `NOT_EVALUATED` | no issue because a required optional service/model is absent or a construct is outside the supported subset | Callers must not reinterpret this as PASS. Input/service gates own the missing prerequisite. |

Every emitted standard issue starts `OPEN`, carries evidence and a suggested
fix, and may later become `SUPPRESSED` through the separate suppression service.
`RESOLVED` is a result-model value, not produced by these evaluators. Severity,
certainty, status, and snapshot `PASS/FAIL/UNKNOWN` are independent axes.

`ValidationOrchestrator` executes enabled rules deterministically by
`RulePhase` and rule ID, then orders findings by portable source, line, rule ID,
and message. It reads immutable inputs. Bounded SOIL evaluation is delegated to
the state-safe snapshot evaluator, which restores and fingerprint-checks the
USE state.

## 2. Research taxonomy and execution order

| Research layer | Executable phase/catalog | Rule IDs |
|---|---|---|
| A. Syntax/import conformance | `PARSE` | `ASL-001`, `ASL-002` |
| B. BDI well-formedness and reference semantics | `IR_WELL_FORMEDNESS`, `REFERENCE` | `BDI-001..004`, `REF-001..002` |
| C. Cross-model correspondence consistency | `MAPPING`, `SIGNATURE` (plus reference-phase message resolution) | `MAP-001..003`, `SIG-001..003`, `OWN-001`, `BEL-001`, `MSG-001` |
| D. Snapshot/state semantic consistency | `SNAPSHOT_OCL`, `BOUNDED_SIMULATION` | `OCL-001..004`, `CTX-001` |
| E. Static environment extension | separate environment catalog/service | `ENV-001..004` |
| F. Static organization extension | separate organization catalog/validator | `ORG-001..003` |

The taxonomy is conceptual. It does not alter the seven-value `RulePhase`
order, configuration schema, evaluator dispatch, or extension-catalog
separation.

## 3. Exact goal-support semantics

`BdiIndexBuilder` records a supporting plan only when all of these conditions
hold:

```text
trigger.term is LiteralTerm
and trigger.type == ACHIEVE
and trigger.operator == ADD
and PredicateSignature(trigger.functor, trigger.arity)
    == PredicateSignature(goal.functor, goal.arity)
```

`BDI-002` applies this lookup to initial goals and achievement-goal body steps.
Plan context truth is deliberately irrelevant to structural support; `CTX-001`
may later assess supported context expressions against a snapshot.

The current namespace policy is exact equality of the normalized functor text
and arity. There is no separate namespace field or per-agent namespace filter,
and the multi-file index is aggregate. Consequently an equal functor/arity in
another imported source can currently count as support. This limitation is
documented rather than silently strengthened in Task 05.

## 4. Standard-rule semantics

For each row, **Inputs** names profile metaclasses, current correspondence
kinds, and USE dependency. **Trace** names emitted source/UML fields. **Tests**
identify current executable coverage; the shared registry/evidence contracts
also run for every ID.

### 4.1 Layer A — syntax/import conformance

| Rule | Phase; inputs and precondition | Executable algorithm | Result and absence/unknown behavior | Trace, fix, limitation | Tests / evidence |
|---|---|---|---|---|---|
| `ASL-001` | `PARSE`; parser diagnostic (before a valid profile instance); no correspondence/USE. Diagnostic code is `ASL-001`. | Emit one issue per Jason syntax diagnostic. | `ERROR/CONFIRMED`; no matching diagnostic is `NO_FINDING`. A source that cannot normalize is not fabricated as a model. | Source span from diagnostic or unknown coordinates; no UML trace. Fix reported syntax. Limited to Jason diagnostics retained by importer. | `JasonAslParserAdapterTest`, `ValidationOrchestratorTest`, packaged diagnostic smoke; [baseline evidence](../evidence/baseline-reconciliation.md). |
| `ASL-002` | `PARSE`; `Agent`, `UnsupportedFeature`; no correspondence/USE. | Emit one issue for every retained unsupported feature across normalized agents. | `WARNING/CONFIRMED`; an empty unsupported list is `NO_FINDING`, not proof of full AgentSpeak support. | Feature span; kind/subject evidence; no UML trace. Use supported subset or extend normalizer with evidence. | `UnsupportedFixtureTest`, `IrHierarchyTest`, `ValidationOrchestratorTest`; [limitations](../evidence/limitations.md). |

### 4.2 Layer B — BDI well-formedness and references

| Rule | Phase; inputs and precondition | Executable algorithm | Result and absence/unknown behavior | Trace, fix, limitation | Tests / evidence |
|---|---|---|---|---|---|
| `BDI-001` | `IR_WELL_FORMEDNESS`; `Agent`, `Plan`; explicit nonblank labels. | Group plan labels per source agent; emit for groups with more than one occurrence. | `ERROR/CONFIRMED`; unique/blank labels give `NO_FINDING`. | First occurrence span, agent source, label, count. Rename duplicate labels. Does not compare labels across agents. | `BdiIndexTest`, `ValidationOrchestratorTest`; registry evidence. |
| `BDI-002` | `IR_WELL_FORMEDNESS`; `Goal`, `GoalCall`, `Plan`, `Trigger`; aggregate goal-support index. | For each initial/achievement goal signature, require an indexed literal `+!` plan trigger with equal functor/arity. | `ERROR/CONFIRMED`; support gives bounded `NO_FINDING`. Unsupported/non-indexed syntax is handled by `ASL-002`, not inferred as support. | Goal/call span, source and optional enclosing plan; no UML trace. Add/correct supporting plan. Aggregate index has no per-agent namespace dimension. | `BdiIndexTest`, `ValidationOrchestratorTest`; [profile coverage](METAMODEL_COVERAGE.md). |
| `BDI-003` | `IR_WELL_FORMEDNESS`; `Plan`, `Trigger`, `Term`. Constructors already require a trigger. | Emit when the trigger term is not `LiteralTerm`. | `ERROR/CONFIRMED`; literal triggers give `NO_FINDING`. This is not a missing-trigger parser check. | Plan span, agent, label, rendered term/type. Use a literal supported trigger. | `ValidationOrchestratorTest`; IR constructor tests. |
| `BDI-004` | `IR_WELL_FORMEDNESS`; ordered `Plan.steps`, `SourceSpan`. | Traverse each plan in stored order; when known current line is lower than the preceding known line, emit. Unknown-line steps do not advance the comparison. | `ERROR/CONFIRMED`; nondecreasing known lines give `NO_FINDING`. | Current step span, previous/current lines, agent/plan. Restore source order. Does not detect duplicate indices or same-line ambiguity. | `ValidationOrchestratorTest`, `BdiIndexTest`; profile ordering constraint `MM-006`. |
| `REF-001` | `REFERENCE`; indexed receiver/object terms, `RECEIVER_OBJECT`; current `UseModelSnapshot.objects`. | Non-dynamic receivers must have a receiver binding whose target object exists; named object terms must occur in current objects. | Missing receiver binding/target: `ERROR/CONFIRMED`; unmatched named object term: `ERROR/POTENTIAL`. No USE snapshot is `NOT_EVALUATED`, never PASS; dynamic receiver terms are skipped here. | Term span, optional object UML ref, origin/mapping-key evidence. Create/map/correct object. Static name heuristics can over-approximate object-like literals. | `ValidationOrchestratorTest`, `AuctionFaultInjectionTest`; [Auction evaluation](../evidence/auction-evaluation.md). |
| `REF-002` | `REFERENCE`; `TestStep`/test `Context`, initial `Belief`, `BELIEF_ATTRIBUTE`; no USE snapshot required. | A test signature is known when an initial belief has that signature or a belief mapping exists; otherwise emit. | `WARNING/POTENTIAL`; known signature gives `NO_FINDING`. First-class AgentSpeak rules are not normalized, so they cannot establish support. | Test span and optional plan; no UML target field. Add belief/rule support or mapping. | `ValidationOrchestratorTest`, `UnsupportedFixtureTest`; [profile limitation](METAMODEL_COVERAGE.md). |

### 4.3 Layer C — correspondence consistency

| Rule | Phase; inputs and precondition | Executable algorithm | Result and absence/unknown behavior | Trace, fix, limitation | Tests / evidence |
|---|---|---|---|---|---|
| `MAP-001` | `MAPPING`; `Agent`; `AGENT_CLASS` or `AGENT_OBJECT`; no USE lookup in this rule. | Emit when neither mapping kind exists for an agent source. | `ERROR/CONFIRMED`; presence is `NO_FINDING` here, with target existence delegated to `MAP-003`. | Unknown source span plus portable agent ID; map to class/object. Does not decide mapping correctness. | `ValidationOrchestratorTest`, `AuctionMappingFixtureTest`; [mapping examples](../evidence/auction-mapping-examples.md). |
| `MAP-002` | `MAPPING`; `ExternalAction`; `ACTION_OPERATION`. | Emit for each indexed external action without a binding. | `ERROR/CONFIRMED`; binding presence is `NO_FINDING` here; internal actions are outside this rule. | Action span, agent/plan, mapping source key. Confirm an operation mapping. Target checks are later rules. | `ValidationOrchestratorTest`, `AuctionMappingFixtureTest`; mapping examples. |
| `MAP-003` | `MAPPING`; all persisted BDI mapping kinds plus profile/index and current USE snapshot. | Run `MappingStalenessDetector`; emit only `SOURCE_MISSING` or `TARGET_MISSING` findings with a binding. | `ERROR/CONFIRMED`; current binding gives `NO_FINDING`. Missing USE snapshot is `NOT_EVALUATED`. Fingerprint-only differences are intentionally not stale. | Source if resolvable, UML target, detector evidence. Refresh/remove binding. | `AuctionStructuralMutantTest`, `AuctionFaultInjectionTest`; [Auction structural result](../evidence/auction-evaluation.md). |
| `SIG-001` | `SIGNATURE`; `ExternalAction`, term signature, resolved `ACTION_OPERATION`, current USE operations. | Compare action signature arity with mapped operation parameter count. | `ERROR/CONFIRMED` on inequality; equality is `NO_FINDING`. Missing snapshot/mapping/operation/signature is `NOT_EVALUATED`, with prerequisite findings owned elsewhere. | Action span, agent/plan, operation ref and counts. Correct action or mapping. | `ValidationOrchestratorTest`, `AuctionFaultInjectionTest`; Auction signature mutant. |
| `SIG-002` | `SIGNATURE`; action arguments, resolved operation parameters, current USE. Equal arity required. | Infer only String, integral/real number, and zero-arity true/false; normalize primitive names; emit on known unequal type. | `ERROR/CONFIRMED`; compatible known types give `NO_FINDING`; unknown types are handled by `SIG-003`, not PASS. | Argument span, action/plan and operation ref. Correct argument/mapping. Type system is deliberately shallow. | `ValidationOrchestratorTest`, `AuctionBaselineReportTest`; rule catalog evidence. |
| `SIG-003` | `SIGNATURE`; same prerequisites as `SIG-002`. | Emit for each equal-position argument whose static type inference is empty. | `WARNING/UNKNOWN`; known type gives `NO_FINDING`. Missing mapping/snapshot or arity mismatch is `NOT_EVALUATED` by this rule. | Argument span and operation ref with expected type. Add binding/snapshot evidence. | `ValidationOrchestratorTest`, `AuctionBaselineReportTest`; limitations. |
| `OWN-001` | `SIGNATURE`; `ExternalAction`, `AGENT_CLASS`/`AGENT_OBJECT`, resolved operation and USE class/object inheritance. | Resolve executing class; accept exact operation owner or an owner listed in its parent names; otherwise emit. | `ERROR/CONFIRMED`; compatible owner is `NO_FINDING`. Missing class/object/mapping/snapshot is `NOT_EVALUATED`, not PASS. | Action span, agent/plan, operation ref, owner evidence. Correct agent/receiver or operation mapping. | `ValidationOrchestratorTest`, `AuctionBaselineReportTest`; mapping examples. |
| `BEL-001` | `MAPPING`; initial `Belief`, `BELIEF_ATTRIBUTE`; no USE lookup. | Emit for every initial belief signature without a binding. | `WARNING/POTENTIAL`; binding presence is `NO_FINDING` only for mapping availability. | Belief span/agent, expected mapping key; no UML field. Add mapping when cross-model checking is intended. Does not test attribute existence or value compatibility. | `ValidationOrchestratorTest`, `AuctionBaselineReportTest`; mapping examples. |
| `MSG-001` | `REFERENCE`; `InternalAction` `.send`, receiver term, `RECEIVER_OBJECT`, current USE objects. | Require receiver argument, locate its indexed receiver source by source/line/rendered value, resolve mapping, and require target object. | `ERROR/CONFIRMED`; resolved object gives `NO_FINDING`. Missing USE snapshot is `NOT_EVALUATED`; dynamic receiver without evidence does not become PASS. | `.send` span, agent/plan, mapping-key/receiver evidence. Map/correct receiver. Messages remain internal-action terms, not first-class profile objects. | `ValidationOrchestratorTest`, `BdiIndexTest`; profile coverage. |

### 4.4 Layer D — snapshot and bounded-state semantics

| Rule | Phase; inputs and precondition | Executable algorithm | Result and absence/unknown behavior | Trace, fix, limitation | Tests / evidence |
|---|---|---|---|---|---|
| `OCL-001` | `SNAPSHOT_OCL`; mapped external action/operation with preconditions, `AGENT_OBJECT`, action term, snapshot evaluator. | Evaluate every operation precondition with receiver/arguments; emit results whose status is `FAIL`. | `ERROR/CONFIRMED`; evaluator `PASS` gives `NO_FINDING`; missing prerequisites are handled by `OCL-002` or `NOT_EVALUATED`. | Action span, agent/plan, operation ref, evaluator evidence. Change state/arguments/mapping. | `AuctionFaultInjectionTest`, `UseSnapshotOclEvaluatorTest`; Auction OCL mutant. |
| `OCL-002` | `SNAPSHOT_OCL`; same operation/precondition inputs. | Emit once when evaluator or receiver binding is absent; also emit each evaluator result with `UNKNOWN`. | `WARNING/UNKNOWN`; evaluable `PASS/FAIL` is no `OCL-002` finding. Compile/evaluation errors remain UNKNOWN, never PASS. | Action/operation trace and evaluator or missing-binding evidence. Provide valid snapshot/receiver/arguments. | `ValidationOrchestratorTest`, `AuctionBaselineReportTest`, `UseSnapshotOclEvaluatorTest`; limitations. |
| `CTX-001` | `SNAPSHOT_OCL`; `Plan.context`, `BELIEF_ATTRIBUTE`, `AGENT_OBJECT`, snapshot evaluator. | Translate mapped literal attributes and supported `not`/`and`/`or` trees to OCL; emit only evaluator `FAIL`. | `WARNING/CONFIRMED`; evaluator `PASS` gives `NO_FINDING`. Unmapped/unsupported context or absent evaluator/receiver is `NOT_EVALUATED`; UNKNOWN currently emits no `CTX-001` issue and must not be called PASS. | Context span, agent/plan, evaluator evidence; no direct UML ref. Fix state/belief mapping/plan choice. Translation is intentionally narrow. | `ValidationOrchestratorTest`, `UseSnapshotOclEvaluatorTest`; profile context coverage. |
| `OCL-003` | `BOUNDED_SIMULATION`; mapped action/operation, `ACTION_OPERATION.expression` beginning `soil:`, snapshot evaluator. | Run the explicit bounded SOIL effect; emit when status is `INVARIANT_VIOLATED`. | `ERROR/CONFIRMED`; safe simulation is `NO_FINDING`. Missing effect is handled by `OCL-004`; absent evaluator is `NOT_EVALUATED`. | Action/operation and simulation evidence. Review effect/invariant. Limited to explicitly supplied bounded effects. | `ValidationOrchestratorTest`, `UseSnapshotOclEvaluatorTest`; [state-safety architecture](../04_SYSTEM_ARCHITECTURE.md). |
| `OCL-004` | `BOUNDED_SIMULATION`; mapped action/operation and optional `soil:` expression. | Emit if no bounded effect exists; when evaluator exists, also emit if simulation returns `UNKNOWN`. | `INFO/UNKNOWN`; never PASS. A supplied effect without an evaluator currently yields `NOT_EVALUATED` and is a documented prerequisite gap. | Action/operation plus skip/simulation evidence. Supply a small effect or retain static-only interpretation. | `ValidationOrchestratorTest`, `AuctionBaselineReportTest`; limitations. |

## 5. Static environment extension

Only `CONFIRMED + CURRENT` persisted mappings enter `EnvironmentConsistencyValidator`.
Candidates are not findings in this catalog. Confirmed non-current persisted
mappings are handled by `ENV-004`. No CArtAgO concrete type crosses the adapter
or validator boundary.

| Rule | Inputs and executable algorithm | Result and absence/unknown behavior | Trace, fix, limitation | Tests / evidence |
|---|---|---|---|---|
| `ENV-001` | `Artifact`, `ArtifactOperation`, confirmed current operation mapping, USE operation. Require artifact, named operation, and target operation. | Missing source/target: `ERROR/CONFIRMED`; all exist is bounded `NO_FINDING`. | Mapping key and workspace/artifact plus UML operation. Select existing targets. Static declarations only. | `AuctionEnvironmentConsistencyTest`; [CArtAgO spike](../evidence/cartago-artifact-spike.md). |
| `ENV-002` | Named artifact operations and BDI action arity from operation mapping. Require at least one named operation with equal arity. | Mismatch: `ERROR/CONFIRMED`; match is bounded `NO_FINDING`. `ENV-001` returns first when source/target absent. | Environment/mapping key and UML target. Align action and operation arity. No runtime invocation. | `AuctionEnvironmentConsistencyTest`; CArtAgO spike. |
| `ENV-003` | `ObservableProperty`, confirmed current property mapping, USE attribute. Require equal property name/arity and target; then inspect optional reviewed runtime-values evidence. | Missing target: `ERROR/CONFIRMED`; values unavailable: `INFO/UNKNOWN`; available evidence produces no finding. | Environment/mapping/UML attribute. Correct targets or capture evidence. It does not compare live property values with UML state. | `AuctionEnvironmentConsistencyTest`, `AuctionEnvironmentMappingPersistenceTest`; [persistence evidence](../evidence/cartago-environment-mapping-persistence.md). |
| `ENV-004` | Confirmed persisted environment mapping after staleness revalidation. | `STALE`: `ERROR/CONFIRMED`; preserved `UNKNOWN`: `WARNING/UNKNOWN`; current is `NO_FINDING`. | Mapping/source/UML target and staleness reasons. Reconfirm after repair. Unknown never becomes current/PASS. | `AuctionEnvironmentMappingPersistenceTest`; persistence evidence. |

## 6. Static organization extension

Organization mappings remain plugin-owned and separate from the 22 standard
IDs. A candidate is reported `INFO/UNKNOWN` before target validation. No Moise
concrete type crosses the adapter, no arbitrary OCL text is parsed, and static
equality never establishes runtime enactment.

| Rule | Inputs and executable algorithm | Result and absence/unknown behavior | Trace, fix, limitation | Tests / evidence |
|---|---|---|---|---|
| `ORG-001` | `Organization.Role`, role mapping, USE class. Candidate => unknown; confirmed => require source role and target class. | Candidate: `INFO/UNKNOWN`; missing source/target: `ERROR/CONFIRMED`; valid confirmed mapping: `NO_FINDING` for static existence. | Organization source when found, mapping evidence, class target. Confirm or correct mapping. No membership/enactment claim. | `AuctionOrganizationConsistencyTest`; [organization pilot](../evidence/organization-consistency-pilot.md). |
| `ORG-002` | `Mission`, mission mapping, USE operation. Candidate => unknown; confirmed => require source mission and target operation. | Candidate: `INFO/UNKNOWN`; missing source/target: `ERROR/CONFIRMED`; valid confirmed mapping: static `NO_FINDING`. | Mission source, mapping evidence, operation target. Confirm/correct mapping. No commitment/execution claim. | `AuctionOrganizationConsistencyTest`; organization pilot. |
| `ORG-003` | `Group`, `RoleCardinality`, `Cardinality`, confirmed/candidate cardinality mapping, existing USE invariant and optional reviewer-normalized bounds. | Candidate: `INFO/UNKNOWN`; missing source/target or unequal reviewed bounds: `ERROR/CONFIRMED`; absent reviewed bounds: `WARNING/UNKNOWN`; equal static bounds: `INFO/UNKNOWN` because enactment is unavailable. | Group/role source, invariant target, expected/reviewed bounds. Confirm/review/align; capture membership for runtime checking. | `AuctionOrganizationConsistencyTest`; organization pilot and limitations. |

## 7. Invariants across reporting and presentation

- Missing receiver, binding, type, snapshot, or effect evidence is either an
  explicit `UNKNOWN_FINDING` or `NOT_EVALUATED`; neither is semantic PASS.
- OCL compile/evaluation failures remain evaluator `UNKNOWN` and feed
  `OCL-002`/`OCL-004` where the executable path supports them.
- Static CArtAgO declarations and Moise bounds cannot establish live values,
  membership, commitment, norm fulfilment, or enactment.
- `POTENTIAL` and `UNKNOWN` remain distinct from each other and from
  `CONFIRMED`.
- Reports, traceability, and diagrams serialize/project existing issue and
  snapshot evidence. They do not rerun rules or reinterpret certainty/status.

## 8. Formalization decision

Profile-local structural constraints such as required triggers, deterministic
step ordering, portable identity, and valid cardinality bounds can be stated as
metamodel well-formedness constraints. Parser diagnostics, mapping staleness,
qualified USE ownership, shallow type compatibility, snapshot OCL evaluation,
bounded state variation, and evidence certainty require application/service
logic. Accordingly, metamodel constraints and static semantics are specified
here while the existing Java rule engine remains the sole executable
realization.
