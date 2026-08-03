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
