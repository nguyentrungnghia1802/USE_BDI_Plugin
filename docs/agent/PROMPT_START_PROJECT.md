# Prompt khởi động dự án cho AI Coding Agent

```text
Bạn đang làm việc trong repository USE đã clone để phát triển khóa luận “Mở rộng USE để nhập, ánh xạ và kiểm tra tính nhất quán giữa mô hình BDI AgentSpeak và mô hình UML/OCL”.

Hãy bắt đầu bằng cách:
1. Đọc toàn bộ file docs/agent/AGENT.md.
2. Đọc docs/agent/README.md, docs/agent/00_PROJECT_CONTEXT.md, docs/agent/04_SYSTEM_ARCHITECTURE.md, docs/agent/10_PLUGIN_TECHNICAL_DESIGN.md, docs/agent/14_ROADMAP_TO_DECEMBER_2026.md và docs/agent/16_PROJECT_COMPLETION_CHECKLIST.md.
3. Kiểm tra repository hiện tại, Java/Maven version, git status và khả năng build/run USE. Không xóa hoặc ghi đè thay đổi hiện có.
4. Thực hiện Phase 0: baseline và plugin technical spike. Tìm trong source USE cơ chế plugin thực tế, lifecycle/interface, cách thêm menu/view, cách truy cập current model/session/system state và cách package dependency ngoài như Jason.
5. Ghi các phát hiện có bằng chứng vào docs/agent/DECISION_LOG.md. Không đoán API.
6. Tạo module `use-bdi-plugin` nhỏ nhất có thể build, package và hiển thị một menu/Hello action trong USE; chưa triển khai feature rộng.
7. Viết test hoặc smoke script phù hợp, cập nhật docs/agent/16_PROJECT_COMPLETION_CHECKLIST.md.
8. Chạy các test/build liên quan.
9. Commit bằng message rõ ràng; không force push, không merge main và không sửa sâu USE core nếu chưa có ADR.
10. Cuối phiên, báo cáo file thay đổi, lệnh/test đã chạy, kết quả, commit hash, blocker và task tiếp theo.
```

## Prompt cho lần tiếp theo

```text
Đọc docs/agent/AGENT.md và tiếp tục task chưa hoàn thành có ưu tiên cao nhất trong docs/agent/16_PROJECT_COMPLETION_CHECKLIST.md. Tuân thủ các ADR, triển khai một vertical slice nhỏ, có test, cập nhật tài liệu/checklist và commit khi test pass.
```
