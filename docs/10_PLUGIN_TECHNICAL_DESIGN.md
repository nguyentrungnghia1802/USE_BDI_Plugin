# 10. Plugin Technical Design

## 1. Baseline kỹ thuật

- Java 21.
- Maven multi-module.
- USE modules hiện tại: `use-core`, `use-gui`, `use-assembly`.
- Jason dependency đề xuất: `io.github.jason-lang:jason-interpreter:3.3.0`.
- Jason API có các kiểu phù hợp như `Plan`, `PlanBody`, `Trigger`, `Literal`, `SourceInfo` và package parser.

## 2. Technical spike bắt buộc trước khi code feature

Trong 1–2 ngày, đọc source USE để xác nhận:

1. plugin descriptor/manifest cần gì;
2. interface lifecycle và GUI action chính xác;
3. cách lấy `MainWindow`, current `Session`, `MModel`, `MSystemState`;
4. plugin classloader có nạp dependency transitively hay cần fat JAR;
5. cách thêm menu/view mà không sửa `use-gui`;
6. cách reload plugin khi debug;
7. headless integration test khả thi đến đâu.

Kết quả spike phải ghi vào `DECISION_LOG.md`; không đoán API rồi code rộng.

## 3. Module structure

```text
use-bdi-plugin/
  pom.xml
  src/main/java/org/tzi/use/plugins/bdi/
    plugin/
    application/
    importer/jason/
    model/ir/
    model/mapping/
    model/issues/
    index/
    validation/rules/
    ocl/
    persistence/
    report/
    ui/
  src/main/resources/
    plugin metadata
    messages.properties
    icons/
  src/test/java/
  src/test/resources/fixtures/
    asl/valid/
    asl/invalid/
    use/
    mappings/
    expected/
```

## 4. Core interfaces

```java
interface AslImporter {
    ParseResult importFiles(List<Path> files);
}

interface BdiModelIndex {
    List<PlanModel> supportingPlans(PredicateSignature goal);
    List<ActionCallSite> actionCalls(PredicateSignature action);
}

interface UmlModelFacade {
    Optional<UmlClassRef> findClass(String name);
    Optional<UmlObjectRef> findObject(String name);
    List<UmlOperationRef> operations();
    OclEvaluationResult evaluate(String expression, EvaluationBindings bindings);
}

interface ConsistencyRule {
    String id();
    RulePhase phase();
    List<Issue> evaluate(ValidationContext context);
}
```

## 5. Proposed services

- `JasonAslImporter`
- `JasonAstToIrNormalizer`
- `BdiProjectRepository`
- `BdiIndexBuilder`
- `UseUmlModelFacade`
- `MappingRepository`
- `MappingSuggestionService`
- `ValidationOrchestrator`
- `OclPreconditionChecker`
- `BoundedStateSimulator`
- `ReportExporter`

### Implemented Phase 2 slice

- `BdiIndexBuilder` and immutable `BdiIndex` provide goal, action, predicate,
  agent/object, and duplicate-label indexes over the normalized IR.
- `BdiImportService` is the application boundary for full-tree imports and
  partial per-file diagnostics.
- `BdiImportWorker` keeps parser/index work off the Swing EDT.
- `ImportBdiAction` and `BdiExplorerView` implement the first file chooser,
  tree, and source-detail UI through the verified USE `ViewFrame` API.

The current syntactic reference policy is deliberately conservative: `.send`
receivers are agent references and named terms in arguments are object
references, but neither is resolved to a USE class/object until the USE adapter
slice exists.

## 6. Dependency packaging

Plugin JAR có thể cần chứa Jason và dependency. Hai phương án:

- copy dependency JAR cạnh plugin nếu loader hỗ trợ;
- tạo shaded/fat JAR bằng `maven-shade-plugin`.

Phải kiểm tra classloader trước. Nếu shade, tránh relocate các package mà Jason dùng reflection; kiểm tra LGPL/GPL notices và tạo `THIRD_PARTY_NOTICES` khi phân phối.

## 7. Configuration

```text
.bdi-plugin/
  project.json
  mappings.bdimap.json
  suppressions.json
  rules.json
  reports/
```

Không ghi absolute path nếu có thể; dùng path tương đối so với project root.

## 8. Logging

- logger theo package;
- import summary ở INFO;
- unsupported syntax ở WARN/diagnostic, không spam stack trace;
- exception parser/plugin ở ERROR;
- report chứa plugin version, USE version, Jason version và model hash.

## 9. Git strategy

- giữ `main` gần upstream/stable;
- làm việc trên `feature/bdi-plugin` hoặc `thesis/bdi-plugin`;
- commit theo vertical slice;
- không force push;
- không sửa core nếu chưa có ADR;
- mỗi milestone tạo tag: `m0-baseline`, `m1-import`, `m2-mapping`, `m3-validation`, `m4-case-study`.

## 10. Quản lý file prototype hiện tại

Inventory ngày 04/08/2026 chỉ tìm thấy prototype
`Smart_manager_agent.asl` ở root. File này đã được chuyển vào:

```text
use-bdi-plugin/src/test/resources/fixtures/smartqueue/Smart_manager_agent.asl
```

Các tên `SmartQueue.use`, `.cmd` và `.clt` trong ghi chú cũ không có trong
checkout hiện tại; không tạo placeholder và không coi chúng là đã di chuyển.
Root repository chỉ nên giữ module, mã nguồn chính và tài liệu cấp project.
