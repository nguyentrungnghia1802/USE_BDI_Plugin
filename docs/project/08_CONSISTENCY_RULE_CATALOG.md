# 08. Consistency Rule Catalog

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

## 2. MVP rule catalog

| Rule ID | Mô tả | Severity | Điều kiện |
|---|---|---|---|
| ASL-001 | Parse error | Error | Jason parser thất bại |
| ASL-002 | Unsupported syntax | Warning | AST node ngoài subset |
| BDI-001 | Duplicate plan label | Error | cùng agent có label trùng |
| BDI-002 | Goal without supporting plan | Error/Warning | goal signature không có plan trigger tương ứng |
| BDI-003 | Plan missing/unknown trigger | Error | trigger không hợp lệ trong IR |
| BDI-004 | Duplicate/invalid step order | Error | index trùng hoặc không tuần tự |
| REF-001 | Referenced agent/object not found | Error | message/action receiver không resolve |
| REF-002 | Test goal/belief reference unresolved | Warning | predicate không có belief/rule/mapping phù hợp |
| MAP-001 | Agent has no UML mapping | Error | agent cần kiểm tra liên mô hình nhưng chưa map |
| MAP-002 | Action has no UML operation mapping | Error | external action chưa map |
| MAP-003 | Mapping target no longer exists | Error | mapping stale |
| SIG-001 | Arity mismatch | Error | action arguments != operation parameters sau receiver policy |
| SIG-002 | Parameter type mismatch | Error | type incompatible |
| SIG-003 | Type unknown | Warning | không đủ bằng chứng infer type |
| OWN-001 | Operation belongs to wrong owner | Error | receiver/class mapping không phù hợp owner |
| BEL-001 | Belief mapping missing/incompatible | Warning | belief cần cho cross-check nhưng chưa map |
| MSG-001 | Message receiver unknown | Error | `.send` receiver không tồn tại/không map |
| OCL-001 | Operation precondition false | Error | đánh giá trên snapshot cho kết quả false |
| OCL-002 | Operation precondition unknown | Warning | thiếu binding/snapshot |
| OCL-003 | Invariant violated after bounded effect | Error | simulated state vi phạm invariant |
| OCL-004 | Effect not specified; invariant check skipped | Info | không có effect model |
| CTX-001 | Plan context contradicts current snapshot | Warning | context evaluate false trên snapshot hiện tại |

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

## 3. Goal support semantics

Một plan hỗ trợ goal nếu:

- trigger kind là achievement goal;
- trigger operation phù hợp;
- functor/arity trùng goal signature;
- namespace phù hợp theo policy.

Không yêu cầu context luôn true; context chỉ quyết định applicable tại một snapshot.

## 4. OCL certainty policy

### Confirmed error
USE đánh giá precondition/invariant thành `false` với receiver/arguments/state xác định.

### Potential error
Phân tích effect approximation cho thấy khả năng vi phạm nhưng không có state đầy đủ.

### Unknown
Thiếu receiver, argument binding, type, effect hoặc unsupported syntax.

UI phải hiển thị rõ ba mức, không biến `UNKNOWN` thành `PASS`.

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
