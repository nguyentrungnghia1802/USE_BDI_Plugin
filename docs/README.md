# USE-BDI Thesis Documentation

Tên đề tài: Mở rộng USE để nhập, ánh xạ và kiểm tra tính nhất quán giữa mô
hình BDI AgentSpeak và mô hình UML/OCL.

The canonical, source-backed documentation set starts at
[project/README.md](project/README.md). It covers context, requirements,
architecture, workflows, persistence, internal APIs, codebase, testing,
operations, decisions/risks, state safety, and requirement traceability.

The one-line architecture is:

```text
.asl -> Jason Parser/AST -> Normalized BDI IR -> Mapping -> Rules/OCL -> Problems/Report
```

Project documents distinguish current implementation from planned, partial,
optional, and historical behavior. Local AI-agent prompts are intentionally
not versioned as project documentation.
