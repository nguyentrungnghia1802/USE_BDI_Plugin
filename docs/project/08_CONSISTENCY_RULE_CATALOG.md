# 08. Consistency Rule Catalog

Status: authoritative implemented rule matrix
Verification: source-backed; see Git history and DocumentationContractTest

## 1. Rule result model

```text
Issue
  ruleId
  severity: ERROR | WARNING | INFO
  status: OPEN | SUPPRESSED | RESOLVED
  message
  agentId?
  planId?
  sourceSpan?
  umlElementRef?
  evidence[]
  suggestedFix?
  certainty: CONFIRMED | POTENTIAL | UNKNOWN
```

The authoritative rule-by-rule semantics, prerequisite behavior, traces,
limitations, fixes, and tests are specified in [static semantics](metamodel/STATIC_SEMANTICS.md).
The [rule-to-metamodel matrix](metamodel/RULE_TO_METAMODEL_MATRIX.md) provides
the compact profile/correspondence/USE trace. These documents explain the
existing Java engine and do not create an OCL/EVL or Ecore validator.

## 1.1. Research taxonomy

| Research category | Current executable phases/catalogs | IDs |
|---|---|---|
| Syntax/import conformance | `PARSE` | `ASL-001..002` |
| BDI well-formedness/reference | `IR_WELL_FORMEDNESS`, `REFERENCE` | `BDI-001..004`, `REF-001..002` |
| Cross-model correspondence | `MAPPING`, `SIGNATURE`, message resolution in `REFERENCE` | `MAP-001..003`, `SIG-001..003`, `OWN-001`, `BEL-001`, `MSG-001` |
| Snapshot/state semantics | `SNAPSHOT_OCL`, `BOUNDED_SIMULATION` | `OCL-001..004`, `CTX-001` |
| Static environment extension | separate environment catalog/service | `ENV-001..004` |
| Static organization extension | separate organization catalog/validator | `ORG-001..003` |

This taxonomy is conceptual. The deterministic `RulePhase` order and the
separate extension catalogs are unchanged.

## 2. MVP rule catalog

| Rule ID | Mô tả | Severity | Điều kiện |
|---|---|---|---|
| ASL-001 | Parse error | Error | Jason parser thất bại |
| ASL-002 | Unsupported syntax | Warning | AST node ngoài subset |
| BDI-001 | Duplicate plan label | Error | cùng agent có label trùng |
| BDI-002 | Goal without supporting plan | Error | initial/achievement goal signature không có literal `+!` plan trigger tương ứng trong aggregate index |
| BDI-003 | Plan trigger term is not a literal signature | Error | trigger term không phải `LiteralTermModel` |
| BDI-004 | Invalid retained step order | Error | known source line decreases in stored plan-step order |
| REF-001 | Referenced agent/object not found | Error | message/action receiver không resolve |
| REF-002 | Test goal/belief reference unresolved | Warning | predicate không có belief/rule/mapping phù hợp |
| MAP-001 | Agent has no UML mapping | Error | agent cần kiểm tra liên mô hình nhưng chưa map |
| MAP-002 | Action has no UML operation mapping | Error | external action chưa map |
| MAP-003 | Mapping target no longer exists | Error | mapping stale |
| SIG-001 | Arity mismatch | Error | action arguments != operation parameters sau receiver policy |
| SIG-002 | Parameter type mismatch | Error | type incompatible |
| SIG-003 | Type unknown | Warning | không đủ bằng chứng infer type |
| OWN-001 | Operation belongs to wrong owner | Error | receiver/class mapping không phù hợp owner |
| BEL-001 | Belief mapping missing | Warning | initial belief chưa có `BELIEF_ATTRIBUTE` binding |
| MSG-001 | Message receiver unknown | Error | `.send` receiver không tồn tại/không map |
| OCL-001 | Operation precondition false | Error | đánh giá trên snapshot cho kết quả false |
| OCL-002 | Operation precondition unknown | Warning | thiếu binding/snapshot |
| OCL-003 | Invariant violated after bounded effect | Error | simulated state vi phạm invariant |
| OCL-004 | Effect missing/unknown; invariant check skipped | Info | không có `soil:` effect hoặc bounded simulation trả `UNKNOWN` |
| CTX-001 | Plan context contradicts current snapshot | Warning | supported mapped context evaluate `FAIL` trên snapshot hiện tại |

## 2.1. Implementation matrix

This is the final catalog for the currently implemented MVP. The source of
truth for registration and execution is the `StandardConsistencyRules` factory
in `use-bdi-plugin`; this table is deliberately checked against that factory
by `RuleCatalogCompletenessTest`. "Implemented" means that the rule is
registered, deterministic, emits structured evidence, and is exercised by the
aggregate validation test or a targeted case-study test. It does not mean that
the rule is complete for every AgentSpeak/Jason construct or every UML/OCL
semantics.

| Rule ID | Phase | Source evaluator | Default result | Test/evidence trace |
|---|---|---|---|---|
| ASL-001 | PARSE | `parseErrors` | ERROR / CONFIRMED | `JasonAslParserAdapterTest`, `ValidationOrchestratorTest` |
| ASL-002 | PARSE | `unsupportedSyntax` | WARNING / CONFIRMED | `UnsupportedFixtureTest`, golden IR evidence |
| BDI-001 | IR_WELL_FORMEDNESS | `duplicatePlanLabels` | ERROR / CONFIRMED | `ValidationOrchestratorTest` |
| BDI-002 | IR_WELL_FORMEDNESS | `unsupportedGoals` | ERROR / CONFIRMED | `ValidationOrchestratorTest` |
| BDI-003 | IR_WELL_FORMEDNESS | `invalidTriggers` | ERROR / CONFIRMED | `ValidationOrchestratorTest` |
| BDI-004 | IR_WELL_FORMEDNESS | `invalidStepOrder` | ERROR / CONFIRMED | `ValidationOrchestratorTest` |
| REF-001 | REFERENCE | `unresolvedReferences` | ERROR / CONFIRMED or POTENTIAL | `ValidationOrchestratorTest`, `AuctionFaultInjectionTest` |
| REF-002 | REFERENCE | `unresolvedTestReferences` | WARNING / POTENTIAL | `ValidationOrchestratorTest` |
| MAP-001 | MAPPING | `unmappedAgents` | ERROR / CONFIRMED | `ValidationOrchestratorTest`, `AuctionMappingFixtureTest` |
| MAP-002 | MAPPING | `unmappedActions` | ERROR / CONFIRMED | `ValidationOrchestratorTest`, `AuctionMappingFixtureTest` |
| MAP-003 | MAPPING | `staleMappings` | ERROR / CONFIRMED | `AuctionStructuralMutantTest` |
| SIG-001 | SIGNATURE | `arityMismatches` | ERROR / CONFIRMED | `ValidationOrchestratorTest`, `AuctionFaultInjectionTest` |
| SIG-002 | SIGNATURE | `typeMismatches` | ERROR / CONFIRMED | `ValidationOrchestratorTest`, Auction baseline |
| SIG-003 | SIGNATURE | `unknownTypes` | WARNING / UNKNOWN | `ValidationOrchestratorTest`, Auction baseline |
| OWN-001 | SIGNATURE | `wrongOwners` | ERROR / CONFIRMED | `ValidationOrchestratorTest`, Auction baseline |
| BEL-001 | MAPPING | `unmappedBeliefs` | WARNING / POTENTIAL | `ValidationOrchestratorTest`, Auction baseline |
| MSG-001 | REFERENCE | `unknownMessageReceivers` | ERROR / CONFIRMED | `ValidationOrchestratorTest` |
| OCL-001 | SNAPSHOT_OCL | `failedPreconditions` | ERROR / CONFIRMED | `AuctionFaultInjectionTest` |
| OCL-002 | SNAPSHOT_OCL | `unknownPreconditions` | WARNING / UNKNOWN | `ValidationOrchestratorTest`, Auction baseline |
| CTX-001 | SNAPSHOT_OCL | `contradictingContexts` | WARNING / CONFIRMED | `ValidationOrchestratorTest` |
| OCL-003 | BOUNDED_SIMULATION | `violatedBoundedEffects` | ERROR / CONFIRMED | `ValidationOrchestratorTest` |
| OCL-004 | BOUNDED_SIMULATION | `skippedBoundedEffects` | INFO / UNKNOWN | `ValidationOrchestratorTest`, Auction baseline |

The execution skeleton is:

```text
ValidationContext
  -> parse/IR/index checks
  -> mapping/reference/signature checks
  -> read-only USE snapshot OCL checks
  -> bounded SOIL simulation when an explicit effect is available
  -> ConsistencyIssue(ruleId, severity, status, certainty, evidence)
```

The current test boundary is intentionally explicit. `ValidationOrchestratorTest`
checks that all 22 rules are registered and that every emitted issue carries
evidence. The Auction suite supplies targeted mutation evidence for `MAP-003`,
`SIG-001`, `REF-001`, and `OCL-001`; the other rules still require additional
domain-specific fixtures before they can support a broader research claim.

## 2.2. Static environment pilot catalog

These four rules are intentionally separate from `StandardConsistencyRules`,
so AgentSpeak-only projects retain the exact 22-rule configuration contract.
They consume `EnvironmentModel`, confirmed/current persisted environment
mappings, and the immutable USE projection. Persistence and GUI editing are
implemented, but evaluation remains static and candidates do not enter the
consistency validator.

| Rule ID | Check | Default result | Test/evidence trace |
|---|---|---|---|
| ENV-001 | CArtAgO artifact/operation and UML operation targets exist | ERROR / CONFIRMED | missing-operation mutant |
| ENV-002 | BDI action arity matches the annotated artifact operation | ERROR / CONFIRMED | wrong-arity mutant |
| ENV-003 | Observable property and UML attribute targets exist; dynamic value evidence is available | ERROR / CONFIRMED for missing targets, INFO / UNKNOWN without runtime values | valid baseline and wrong-property mutant |
| ENV-004 | A confirmed persisted environment mapping is stale or cannot be revalidated | ERROR / CONFIRMED when a target changed, WARNING / UNKNOWN when status remains unknown | persisted Auction stale-target and unknown-status tests |

`EnvironmentConsistencyValidator` never starts CArtAgO. An explicit static
property descriptor proves only the declaration. `EnvironmentMappingValidationService`
admits only `CONFIRMED + CURRENT` records to that validator. Candidates are
excluded and stale/unknown confirmed records produce `ENV-004`. Without
captured runtime values, `ENV-003` remains `UNKNOWN` and cannot be reported as
PASS.

## 2.3. Static organization pilot catalog

These rules remain separate from the 22 `StandardConsistencyRules` and the
environment catalog. They consume immutable `OrganizationModel`, explicit
plugin-owned mappings, and `UseModelSnapshot`; no Moise type crosses the adapter.

| Rule ID | Check | Default result | Test/evidence trace |
|---|---|---|---|
| ORG-001 | Confirmed organization role and mapped UML class both exist | ERROR / CONFIRMED for a missing source or target; INFO / UNKNOWN for a candidate | valid Auction role mappings and missing-class mutant |
| ORG-002 | Confirmed organization mission and mapped UML operation both exist | ERROR / CONFIRMED for a missing source or target; INFO / UNKNOWN for a candidate | valid Auction mission mappings and missing-operation mutant |
| ORG-003 | Confirmed role cardinality and OCL invariant exist and reviewed static bounds agree | ERROR / CONFIRMED for missing target or mismatched bounds; WARNING / UNKNOWN without reviewed bounds; INFO / UNKNOWN after a static match because enactment is unavailable | valid Auction bounds, mismatch mutant, and unavailable-evidence case |

`ORG-003` does not parse or reinterpret arbitrary OCL expression text. A human
review confirms the invariant target and normalized minimum/maximum evidence.
The rule compares that evidence with the organization IR. Static equality is
not runtime PASS: dynamic role membership and organization enactment remain
unavailable and therefore `UNKNOWN`.

## 3. Goal support semantics

Một plan hỗ trợ goal nếu:

- trigger term là literal;
- trigger kind là achievement goal (`ACHIEVE`);
- trigger operation là addition (`ADD`);
- functor/arity trùng goal signature;
- normalized functor text trùng chính xác theo current policy.

Không yêu cầu context luôn true; context chỉ quyết định applicable tại một snapshot.
Current aggregate index không có namespace field hoặc per-agent filter riêng,
vì vậy cùng functor/arity ở source khác hiện có thể được tính là support. Đây là
limitation được ghi nhận, không phải semantic được Task 05 tự ý sửa.

## 4. OCL certainty policy

### Confirmed error
USE đánh giá precondition/invariant thành `false` với receiver/arguments/state xác định.

### Potential error
Phân tích effect approximation cho thấy khả năng vi phạm nhưng không có state đầy đủ.

### Unknown
Thiếu receiver, argument binding, type, effect hoặc unsupported syntax.

UI phải hiển thị rõ ba mức, không biến `UNKNOWN` thành `PASS`.

Rules that return no issue because an optional snapshot/service is absent are
`NOT_EVALUATED`, not PASS. A bounded `NO_FINDING` is meaningful only when the
rule's documented prerequisites were available. Reports and diagrams preserve
the result model; they do not reinterpret it.

## 5. Rule execution phases

1. **Parse diagnostics**.
2. **IR well-formedness**.
3. **Reference/index checks**.
4. **Mapping checks**.
5. **Signature/type checks**.
6. **Snapshot OCL checks**.
7. **Bounded simulation checks**.

Nếu phase trước lỗi nghiêm trọng, phase sau có thể skip và sinh reason.

## 6. Suppression

Cho phép suppress issue bằng rule ID + source fingerprint với lý do. Suppression phải lưu trong project config và xuất trong report để đảm bảo minh bạch nghiên cứu.

## 7. Acceptance criteria cho mỗi rule

- mô tả chính xác;
- precondition chạy rule;
- thuật toán/pseudocode;
- severity/certainty;
- positive test;
- negative test;
- source trace;
- suggested fix;
- đánh dấu limitations.

## 8. Formalization boundary

Required triggers, portable identity, deterministic source ordering, and valid
cardinality bounds can be stated as profile-local structural constraints.
Parser diagnostics, mapping staleness, USE owner/type checks, snapshot OCL,
bounded variation, and certainty require application/service logic. The Java
engine remains executable truth; this catalog and the metamodel documents are
its normative research explanation, not a second validator.
