# 14. Roadmap to December 2026

**Mốc bắt đầu:** 03/08/2026.  
**Mốc code freeze mục tiêu:** 30/11/2026.  
**Tháng 12:** buffer, viết luận văn, sửa theo góp ý và chuẩn bị bảo vệ.

## Phase 0 — Baseline và plugin spike (03/08–09/08)

### Deliverables
- build toàn bộ USE bằng Java 21/Maven;
- chạy GUI và test mẫu;
- branch riêng;
- tài liệu chính xác về plugin lifecycle/API/classloader;
- module `use-bdi-plugin` rỗng build được;
- Hello plugin/menu xuất hiện.

### Exit criteria
- clean clone -> build -> start USE -> load plugin theo README nội bộ.

## Phase 1 — Jason import và IR (10/08–23/08)

### Deliverables
- thêm `jason-interpreter`;
- import một file `.asl`;
- error diagnostics;
- normalize Belief, Goal, Plan, Trigger, Context, Steps;
- source location;
- unit/golden tests.

### Demo
CLI/test hoặc UI đơn giản in cây IR của `minimal.asl` và `auctioneer.asl`.

## Phase 2 — BDI Explorer và indexing (24/08–06/09)

### Deliverables
- BDI project repository;
- goal-plan/action/reference index;
- tree view;
- Problems table ban đầu;
- rules BDI-001..004.

## Phase 3 — USE model adapter và mapping (07/09–27/09)

### Deliverables
- đọc classes, objects, attributes, operations từ current model/state;
- mapping data model;
- mapping suggestions;
- manual confirmation/editor;
- save/reload `.bdimap.json`;
- rules MAP/SIG/OWN cơ bản.

### MVP Gate 1
Import -> tree -> mapping -> structural/signature errors chạy end-to-end.

## Phase 4 — Consistency engine hoàn chỉnh (28/09–18/10)

### Deliverables
- orchestrator theo phases;
- 8–15 rules;
- issue evidence/source links;
- filter/suppression;
- report JSON/HTML;
- integration tests.

## Phase 5 — OCL Level 2 và bounded prototype (19/10–01/11)

### Deliverables
- operation precondition evaluation trên snapshot;
- context evaluation best effort;
- PASS/FAIL/UNKNOWN policy;
- một bounded effect path cho Auction;
- invariant re-check;
- OCL rules.

### MVP Gate 2
Đạt toàn bộ tiêu chí thành công tối thiểu của đề xuất.

## Phase 6 — Case study và fault injection (02/11–15/11)

### Deliverables
- Auction baseline;
- mutant suite;
- ground truth manifest;
- metric scripts/manual tables;
- performance data;
- optional House Building import.

## Phase 7 — Stabilization và thesis evidence (16/11–30/11)

### Deliverables
- code freeze;
- clean installation package;
- demo script;
- screenshots/diagrams;
- bảng kết quả;
- limitations;
- user/developer guide;
- tag release candidate.

## December buffer

- sửa theo giảng viên;
- hoàn thiện luận văn;
- rehearsal demo;
- dự phòng lỗi packaging/compatibility;
- stretch: materialized BDI model, plan-selection visualization.

## Must / Should / Could

### Must trước 30/11
- plugin shell;
- import + IR + source trace;
- BDI tree;
- mapping manual;
- 8+ rules;
- snapshot OCL check;
- Auction case study + evaluation;
- report + tests.

### Should
- mapping suggestions;
- HTML report;
- 12–15 rules;
- House Building subset;
- suppression.

### Could
- materialized metamodel in USE;
- relevant/applicable plan visualization;
- bounded multi-step simulation;
- runtime Jason trace.

## Weekly cadence

- Thứ Hai: chọn slice và acceptance criteria.
- Thứ Ba–Năm: implement + tests.
- Thứ Sáu: integration/demo, update docs/checklist.
- Cuối tuần: đọc paper, viết thesis notes, chuẩn bị tuần sau.
