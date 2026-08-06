# Tổng báo cáo — USE BDI Plugin

## Executive Summary

Plugin `use-bdi-plugin` mở rộng USE để nhập AgentSpeak (.asl), chuyển sang mô hình trung gian BDI, cung cấp explorer UI, gợi ý và lưu mapping sang UML/OCL, và chạy bộ luật kiểm tra tĩnh để phát hiện các vấn đề cấu trúc và chữ ký. Tài liệu, quyết định thiết kế và bằng chứng test được ghi trong `DECISION_LOG.md` và checklist ở [docs/16_PROJECT_COMPLETION_CHECKLIST.md](docs/16_PROJECT_COMPLETION_CHECKLIST.md).

**Implemented:** importer (Jason 3.3.0, shaded), normalized IR, BDI index, BDI Explorer UI, Mapping suggestion + editor, Mapping persistence (`.bdimap.json`), static consistency rule slice (many rules implemented), OCL precondition evaluation (Level 2 limited), packaging smoke and unit tests.

## Các chức năng chính (chi tiết)

- **Importer & Parser Boundary:** Jason 3.3.0 được tích hợp trong `use-bdi-plugin`; adapter `JasonAslParserAdapter` trả về immutable `AslParseSummary` và `AslDiagnostic` (see [DECISION_LOG.md](docs/DECISION_LOG.md)).
- **Normalized BDI IR:** Immutable `AgentModel`, `BeliefModel`, `GoalModel`, `PlanModel`, `TriggerModel`, `PlanStepModel`, `TermModel` với source locations.
- **Indexing:** `BdiIndexBuilder` tạo snapshot immutable: goal→plans, action call-sites, predicate occurrences, duplicate-label detection.
- **BDI Explorer UI:** `ImportBdiAction` + `BdiExplorerView`: menu `Plugins > AgentSpeak > Import AgentSpeak...`, tree view, source detail pane, Problems tab, background import worker.
- **Mapping:** `MappingSuggestionService` tạo candidate mappings; `MappingEditorPanel` cho confirm; `MappingFileRepository` lưu `.bdimap.json`.
- **Consistency Engine (Phase 3 slice):** `ValidationOrchestrator`, `ConsistencyRule` SPI, immutable `ConsistencyIssue`; rule catalog includes ASL-001..002, BDI-001..004, REF-001..002, MAP-001..003, SIG-001..003, OWN-001 (see [08_CONSISTENCY_RULE_CATALOG.md](docs/08_CONSISTENCY_RULE_CATALOG.md)).
- **OCL integration (Level 2 limited):** `UseOclEvaluator` wrapper để đánh giá preconditions trên snapshot; PASS/FAIL/UNKNOWN policy.
- **Packaging & Smoke tests:** shaded plugin JAR, `use-bdi-plugin/scripts/smoke.ps1`, unit tests in `use-bdi-plugin` module. Evidence and decisions recorded in [14_ROADMAP_TO_DECEMBER_2026.md](docs/14_ROADMAP_TO_DECEMBER_2026.md) and `16_PROJECT_COMPLETION_CHECKLIST.md`.

## Những thay đổi so với `use` gốc

- Thêm module `use-bdi-plugin` (multi-module Maven reactor). See module folder `use-bdi-plugin/`.
- Plugin menu/action và custom `ViewFrame` cho explorer (không sửa core USE).
- Shaded Jason runtime trong plugin; không thay đổi parser USE.
- Di chuyển prototype AgentSpeak fixtures vào `use-bdi-plugin/src/test/resources/fixtures/` (Smart_queue fixture).
- Thêm scripts smoke/tests để xác nhận tích hợp plugin.

## Luồng hoạt động chính (Flows)

**Runtime import → index → mapping → validation**

1. Người dùng chọn `Import AgentSpeak...` (multi-file chooser).
2. `BdiImportWorker` chạy `BdiImportService` (background). Mỗi file được parse bằng `JasonAslParserAdapter`.
3. `JasonAstToIrNormalizer` tạo `AgentModel` và node IR immutable cùng `SourceSpan`.
4. `BdiIndexBuilder` xây `BdiIndex` (goal/plan/action indexes).
5. `MappingSuggestionService` kết hợp `BdiIndex` và `UseModelSnapshot` để tạo candidate mappings.
6. Người dùng duyệt `MappingEditorPanel`, xác nhận mapping; `MappingFileRepository` lưu `.bdimap.json`.
7. `ValidationOrchestrator` chạy rule phases (parse → IR → reference → mapping → signature → OCL snapshot → bounded simulation) và xuất `ConsistencyIssue` vào Problems view.
8. Người dùng có thể re-import, apply mapping, và refresh kết quả (issues read-only; plugin không mutate USE state).

## Luồng demo / Hướng dẫn chạy (quick start)

- Build toàn repo (Windows/PowerShell):

```powershell
mvn -DskipTests=false clean package
```

- Chạy smoke script (xác nhận plugin menu + importer):

```powershell
cd use-bdi-plugin\scripts
.\smoke.ps1
```

- Trong GUI USE (giải nén `use-assembly/target/use*.zip` nếu cần): chọn `Plugins > AgentSpeak > Import AgentSpeak...` và mở các fixture ở `use-bdi-plugin/src/test/resources/fixtures/` (ví dụ `smartqueue/Smart_manager_agent.asl`).
- Sau import: mở tab `Mapping` trong `BdiExplorerView` để xem gợi ý và xác nhận mapping; xem Problems tab để kiểm tra `ConsistencyIssue`.

## File chính và vị trí

- Module plugin: [use-bdi-plugin](use-bdi-plugin)
- Smoke script: [use-bdi-plugin/scripts/smoke.ps1](use-bdi-plugin/scripts/smoke.ps1)
- Fixtures: [use-bdi-plugin/src/test/resources/fixtures/](use-bdi-plugin/src/test/resources/fixtures/)
- Docs chính: [docs/04_SYSTEM_ARCHITECTURE.md](docs/04_SYSTEM_ARCHITECTURE.md), [docs/10_PLUGIN_TECHNICAL_DESIGN.md](docs/10_PLUGIN_TECHNICAL_DESIGN.md), [docs/08_CONSISTENCY_RULE_CATALOG.md](docs/08_CONSISTENCY_RULE_CATALOG.md), [docs/16_PROJECT_COMPLETION_CHECKLIST.md](docs/16_PROJECT_COMPLETION_CHECKLIST.md), [docs/DECISION_LOG.md](docs/DECISION_LOG.md)

## Trạng thái hiện tại (tóm tắt checklist)

- Phase 0–2 core slices: largely completed (plugin shell, importer, IR, index, explorer, basic mapping, many rules). See checklist [docs/16_PROJECT_COMPLETION_CHECKLIST.md](docs/16_PROJECT_COMPLETION_CHECKLIST.md) (many items checked).
- Open / in-progress items: JSON/HTML report export, complete rule set (target 12–15), suppression UI persistence review, full OCL bounded simulation (Level 3), case study artifacts (Auction model, mutants, metrics), performance benchmarking, release packaging and 3rd-party notices consolidation.

## Hạn chế và Vấn đề đã biết

- Plugin reload/unload không hỗ trợ — phải restart USE để tái nạp plugin.
- OCL integration giới hạn (Level 2 precondition on snapshot); bounded-effect simulation còn chưa hoàn chỉnh.
- Reporting (JSON/HTML), suppression management và UI cho suppression chưa hoàn chỉnh.
- Một số tests/integration (performance, reproducibility on clean clone) còn thiếu.

## Khuyến nghị tiếp theo (ngắn hạn)

1. Hoàn thiện exporter JSON/HTML để lưu report (ưu tiên release evidence).
2. Triển khai suppression UI + persist (`suppressions.json`) và export tích hợp.
3. Hoàn thiện rule catalog đến 12–15 rule, thêm test positive/negative cho mỗi rule.
4. Triển khai case study Auction: model, fixtures, ground truth, mutants, demo script.
5. Gói release: đảm bảo `THIRD_PARTY_NOTICES` và Maven assembly reproducible.

## References

- Decision log: [docs/DECISION_LOG.md](docs/DECISION_LOG.md)
- Roadmap & checklist: [docs/14_ROADMAP_TO_DECEMBER_2026.md](docs/14_ROADMAP_TO_DECEMBER_2026.md), [docs/16_PROJECT_COMPLETION_CHECKLIST.md](docs/16_PROJECT_COMPLETION_CHECKLIST.md)
- Architecture: [docs/04_SYSTEM_ARCHITECTURE.md](docs/04_SYSTEM_ARCHITECTURE.md)

---

Report generated from project docs and repository inspection on 2026-08-07.

## Auto-generated report

The lightweight report exporter writes a summary JSON file to `docs/bdi-report.json`.
You can generate it locally from the repository root with:

```powershell
mvn -pl use-bdi-plugin exec:java -Dexec.mainClass=org.tzi.use.plugins.bdi.report.ReportMain
```

The smoke script `use-bdi-plugin\scripts\smoke.ps1` now runs this step and validates the generated file as part of the smoke checks.
