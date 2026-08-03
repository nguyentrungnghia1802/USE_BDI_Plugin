# Bộ tài liệu phát triển khóa luận USE–BDI

**Tên đề tài:** Mở rộng USE để nhập, ánh xạ và kiểm tra tính nhất quán giữa mô hình BDI AgentSpeak và mô hình UML/OCL.

**Tên tiếng Anh:** Extending USE for Importing, Mapping, and Consistency Checking between BDI AgentSpeak and UML/OCL Models.

Bộ tài liệu này chuyển đề xuất khóa luận thành một kế hoạch kỹ thuật có thể triển khai từ con số 0 đến prototype hoàn chỉnh trước tháng 12/2026.

## Nguyên tắc trung tâm

1. Phát triển dưới dạng plugin/module độc lập; không sửa sâu lexer, parser hoặc AST của USE trong phạm vi chính.
2. Tái sử dụng parser/AST của Jason; không viết lại parser AgentSpeak.
3. Có lớp mô hình trung gian để tách Jason khỏi logic USE.
4. Chia kiểm tra thành nhiều mức: cấu trúc BDI, ánh xạ UML, chữ ký operation và kiểm tra OCL dựa trên trạng thái/mô phỏng giới hạn.
5. Auction là case study thí điểm; House Building là case study mở rộng nếu đủ thời gian.
6. Tính hoàn thành và khả năng đánh giá quan trọng hơn hỗ trợ toàn bộ AgentSpeak.

## Thứ tự đọc đề xuất

1. `00_PROJECT_CONTEXT.md`
2. `01_RESEARCH_PROBLEM_AND_CONTRIBUTIONS.md`
3. `02_PRODUCT_REQUIREMENTS.md`
4. `03_SCOPE_AND_AGENTSPEAK_SUBSET.md`
5. `04_SYSTEM_ARCHITECTURE.md`
6. `05_INTERMEDIATE_MODEL.md`
7. `06_BDI_METAMODEL.md`
8. `07_MAPPING_SPECIFICATION.md`
9. `08_CONSISTENCY_RULE_CATALOG.md`
10. `09_UI_UX_AND_WORKFLOWS.md`
11. `10_PLUGIN_TECHNICAL_DESIGN.md`
12. `11_TEST_AND_QUALITY_STRATEGY.md`
13. `12_CASE_STUDY_AND_EXPERIMENT_PLAN.md`
14. `13_RESEARCH_METHODOLOGY_AND_EVALUATION.md`
15. `14_ROADMAP_TO_DECEMBER_2026.md`
16. `15_RISK_REGISTER.md`
17. `16_PROJECT_COMPLETION_CHECKLIST.md`
18. `AGENT.md`
19. `DECISION_LOG.md`
20. `GLOSSARY.md`
21. `REFERENCES.md`
22. `PROMPT_START_PROJECT.md`

## Kiến trúc một dòng

```text
.asl -> Jason Parser/AST -> Normalized BDI IR -> Mapping Registry -> Consistency Engine -> USE/OCL Adapter -> Report/UI
```

## Định nghĩa hoàn thành tối thiểu

Prototype được xem là đạt khi:

- chạy được cùng USE dưới dạng plugin/extension;
- nhập được ít nhất một file `.asl` hợp lệ;
- trích xuất và hiển thị Belief, Goal, Plan, Trigger, Context và Plan Step;
- có mô hình trung gian độc lập với AST của Jason;
- ánh xạ được Agent và Action sang UML Class/Object và Operation;
- phát hiện được các lỗi goal-plan, phần tử không tồn tại, sai arity và một nhóm lỗi OCL có điều kiện;
- báo cáo lỗi có severity, rule ID, file và dòng;
- chạy được trên case study có cả mẫu đúng và mẫu lỗi;
- có test tự động và số liệu đánh giá phục vụ viết khóa luận.
