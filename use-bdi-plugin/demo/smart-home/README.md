# Demo Smart Home: Resident chuẩn bị buổi tối

## 1. Mục tiêu

Ví dụ minh họa một resident agent có goal `prepare_evening` và action
`turn_on_lights`, trong khi USE giữ snapshot gồm ngôi nhà, cư dân và đèn. File
`.jcm` còn khai báo workspace tĩnh để trên `MAS Overview` có thể phân biệt rõ
BDI layer và Environment layer.

Đây là baseline hợp lệ. Plugin không bật đèn thật, không khởi tạo CArtAgO
workspace và không chạy JaCaMo/Moise runtime.

## 2. Các file trong ví dụ

| File | Nội dung và vai trò |
| --- | --- |
| `SmartHome.use` | UML/OCL chính với `Home`, `Resident`, `Light`, các association và invariant tên/địa chỉ/phòng |
| `SmartHome.cmd` | Tạo `home1`, `resident1`, `livingRoomLight`; trạng thái evening/available và đèn đang tắt |
| `resident.asl` | Goal `prepare_evening`, plan gọi `turn_on_lights` rồi in thông báo |
| `smart-home.jcm` | Project tĩnh gồm resident agent, workspace `smart_home_environment` và organization |
| `smart-home-organization.xml` | Moise role `resident`, mission `mPrepareHome`, goal `prepare_evening` |
| `smart-home-organization.use` | UML/OCL độc lập cho `ResidentRole::prepareHome()` |
| `smart-home-organization.cmd` | Tạo `residentRole1` và kiểm tra invariant organization độc lập |
| `SmartHome.bdimap.json` | Mapping resident agent sang `Resident`, action sang `Resident::turn_on_lights()` |
| `.bdi-plugin/rules.json` | Rule set baseline; bỏ informational `OCL-004` vì không có `soil:` effect |
| `.bdi-plugin/suppressions.json` | Danh sách suppression rỗng |
| `README.md` | Kịch bản demo hiện tại |

## 3. Mở đúng model và snapshot

Sau bước chuẩn bị trong [README bộ demo](../README.md), chạy:

```powershell
$demo = Join-Path $repo 'use-bdi-plugin\demo\smart-home'
& $javaExecutable -jar (Join-Path $useHome 'lib\use-gui.jar') '-nr' "-H=$useHome" `
    (Join-Path $demo 'SmartHome.use') `
    (Join-Path $demo 'SmartHome.cmd')
```

Nếu USE đã mở, chọn `File > Open specification...`, mở `SmartHome.use`, rồi
gõ trong shell:

```text
open "D:\_CODE_BANK\Project_\vnu-sme-lab\use\use-bdi-plugin\demo\smart-home\SmartHome.cmd"
```

## 4. Flow demo trực quan (7-9 phút)

1. Chọn `View > Create View > Class diagram`.
   Chỉ ba class và hai association `HomeResidents`, `HomeLights`.
2. Chọn `View > Create View > Object diagram`.
   Chỉ `home1`, `resident1`, `livingRoomLight`; nhấn mạnh đèn đang `on=false`.
3. Chọn `Plugins > AgentSpeak > Import JaCaMo Project...`, chọn
   `smart-home.jcm`.
4. Trong `Explorer`, bung `resident.asl`, goal `prepare_evening`, plan và action
   `turn_on_lights`.
5. Mở `Mapping`, bấm `Load...`, chọn `SmartHome.bdimap.json`; bấm
   `Refresh USE Snapshot`.
6. Mở `Diagram`, chọn `MAS Overview`, bấm `Fit`.
   Chỉ resident ở BDI layer và `smart_home_environment` ở Environment layer.
   Bật/tắt checkbox Environment để chứng minh layer filtering chỉ là hiển thị.
7. Chuyển sang `BDI Plan`, bấm `Fit` và chỉ đường:

   ```text
   prepare_evening -> plan -> turn_on_lights
                   -> Resident::turn_on_lights()
   ```

8. Chọn goal/plan và bấm `Focus Goal/Plan`; dùng `Reset` để khôi phục overview.
9. Mở `Problems` để xác nhận baseline; nếu chọn finding, quay lại `Diagram` để
   xem evidence highlight.
10. Bấm `Export SVG...` nếu cần graph cho slide hoặc
    `Export Current Analysis...` để lưu report.

## 5. Flow phụ: organization độc lập

1. Mở `smart-home-organization.use` cùng `smart-home-organization.cmd` trong
   một phiên USE riêng.
2. Tạo Class/Object diagram để chỉ `ResidentRole`, `prepareHome()` và object
   `residentRole1`.
3. Đối chiếu với `smart-home-organization.xml`: role `resident`, mission
   `mPrepareHome`, goal `prepare_evening`.
4. Nói rõ đây là static consistency input, không chứng minh role đã được enact
   hay workspace đang chạy.

## 6. Lời thoại và kết quả mong đợi

- "BDI quyết định cần làm gì; USE biểu diễn cấu trúc và state để kiểm tra."
- "Workspace xuất hiện trong MAS Overview vì có trong `.jcm`, nhưng không có
  live CArtAgO state."
- "Mapping chỉ ra action `turn_on_lights` tương ứng operation nào trong UML."
- Snapshot vẫn giữ đèn tắt vì diagram không thực thi plan; đây là hành vi đúng
  của công cụ phân tích read-only.

## 7. Nếu demo không đúng

- Environment toggle bị disable: đã import `resident.asl` trực tiếp; hãy import
  `smart-home.jcm`.
- Object diagram trống: chạy `SmartHome.cmd` trước.
- Không thấy mapping: import project trước rồi load `SmartHome.bdimap.json`.
- Graph dày: tắt Organization/UML-OCL hoặc chuyển `BDI Plan`, sau đó bấm `Fit`.
