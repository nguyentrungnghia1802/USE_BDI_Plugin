# AGENT.md — Quy tắc làm việc tự động cho AI coding agent

## 1. Mission

Phát triển `use-bdi-plugin` theo bộ tài liệu này đến khi hoàn thành các mục Must trong `16_PROJECT_COMPLETION_CHECKLIST.md`, ưu tiên khả năng bảo vệ khóa luận trước tháng 12/2026.

## 2. Bắt đầu mỗi phiên

1. Đọc `README.md`.
2. Đọc `00_PROJECT_CONTEXT.md`, `04_SYSTEM_ARCHITECTURE.md`, `10_PLUGIN_TECHNICAL_DESIGN.md`.
3. Đọc `16_PROJECT_COMPLETION_CHECKLIST.md` và chọn task chưa hoàn thành có dependency đã xong.
4. Đọc `DECISION_LOG.md` để không làm trái quyết định đã chấp nhận.
5. Kiểm tra `git status`, branch, commit gần nhất và build hiện tại.

## 3. Quy tắc phạm vi

- Plugin-first; không sửa lexer/parser/AST/lõi USE nếu chưa có ADR được ghi trong `DECISION_LOG.md`.
- Tái sử dụng Jason parser; không viết parser AgentSpeak thay thế.
- Rule engine chỉ dùng normalized IR, không phụ thuộc trực tiếp Jason AST.
- Unsupported syntax phải tạo diagnostic, không silently ignore.
- OCL check phải phân biệt PASS/FAIL/UNKNOWN.
- Không mở rộng syntax chỉ vì “có thể”; phải phục vụ case study hoặc rule đã xác định.
- Auction MVP trước House Building.

## 4. Quy trình một task

1. Viết acceptance criteria ngắn trong plan/commit note.
2. Tìm code liên quan trong USE trước khi thiết kế API mới.
3. Implement vertical slice nhỏ nhất chạy được.
4. Viết/cập nhật unit test và fixture.
5. Chạy test module; sau đó chạy test toàn project khi phù hợp.
6. Cập nhật tài liệu liên quan.
7. Cập nhật checkbox và bằng chứng trong `16_PROJECT_COMPLETION_CHECKLIST.md`.
8. Ghi ADR nếu có quyết định kiến trúc mới.
9. Commit coherent change.

## 5. Git rules

- Làm việc trên branch `thesis/bdi-plugin` hoặc feature branch con.
- Không commit trực tiếp vào upstream `main`.
- Không force push.
- Không reset/xóa thay đổi của người dùng.
- Commit message dạng:

```text
feat(bdi-import): normalize Jason plans into IR
fix(mapping): mark removed UML operation mappings stale
test(ocl): add false-precondition snapshot fixture
docs(roadmap): update Phase 2 completion
```

- Chỉ push sau khi test liên quan pass.
- Không tự merge vào `main`; tạo PR hoặc chờ người dùng yêu cầu.

## 6. Quality gates

Không đánh dấu task hoàn thành nếu:

- code chưa compile;
- test chưa có hoặc chưa pass;
- lỗi không có source/evidence dù dữ liệu có sẵn;
- tài liệu/checklist chưa cập nhật;
- có hành vi silently ignore unsupported syntax;
- làm thay đổi current USE state không thể phục hồi.

## 7. Khi bị block

Sau tối đa 2 giờ hoặc khi gặp uncertainty lớn:

1. dừng mở rộng code;
2. tạo investigation note trong `DECISION_LOG.md` hoặc issue;
3. mô tả bằng chứng đã kiểm tra;
4. đưa ra phương án A/B;
5. chọn phương án ít rủi ro cho MVP;
6. tiếp tục bằng mock/facade nếu có thể.

## 8. Thứ tự ưu tiên

1. Build/plugin shell.
2. Import + IR + tests.
3. BDI tree/index.
4. USE adapter.
5. Mapping.
6. Rules.
7. Snapshot OCL.
8. Case study/evaluation.
9. UI polish.
10. Stretch features.

## 9. Báo cáo cuối phiên

Luôn ghi:

- task đã làm;
- file chính thay đổi;
- test đã chạy và kết quả;
- checklist đã cập nhật;
- commit hash;
- blocker/risk;
- task tiếp theo hợp lý nhất.
