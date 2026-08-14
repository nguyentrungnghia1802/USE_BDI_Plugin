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
.\run-use-gui.ps1 -Demo smart-home
```

Nếu USE đã mở, chọn `File > Open specification...` và mở `SmartHome.use` trước.
Chỉ khi model đã hiện trong cây bên trái mới gõ trong shell:

```text
open "D:\_CODE_BANK\Project_\vnu-sme-lab\use\use-bdi-plugin\demo\smart-home\SmartHome.cmd"
```

## 4. Kịch bản 1 - flow demo nhanh (7-9 phút)

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

## 5. Kịch bản 2 - dựng Smart Home thủ công từ GUI (10-12 phút)

Khởi động USE không kèm model theo [kịch bản GUI thủ công](../README.md#5-kịch-bản-2---dựng-toàn-bộ-thủ-công-từ-gui),
rồi không mở `SmartHome.cmd`.

### A. Mở model và tạo từng object

1. Chọn `File > Open specification...`, mở `SmartHome.use`, sau đó mở
   `View > Create View > Class diagram`.
2. Qua `State > Create object...`, tạo:

   | Class | Object name |
   | --- | --- |
   | `Home` | `home1` |
   | `Resident` | `resident1` |
   | `Light` | `livingRoomLight` |

3. Chọn `View > Create View > Object diagram`.

### B. Nhập từng giá trị

Nhấp đúp node, nhập literal OCL trong `Object properties`, rồi bấm `Apply`:

| Object | Giá trị OCL cần nhập |
| --- | --- |
| `home1` | `address = '12 Demo Street'` |
| `resident1` | `name = 'Minh'`, `evening = true`, `lightsAvailable = true` |
| `livingRoomLight` | `room = 'living room'`, `on = false` |

### C. Nối từng quan hệ

1. Giữ `Ctrl`, chọn `home1` và `resident1`, nhấp phải, chọn
   `insert (home1,resident1) into HomeResidents`.
2. Chọn `home1` và `livingRoomLight`, nhấp phải, chọn
   `insert (home1,livingRoomLight) into HomeLights`.
3. Chọn `State > Check structure now`; log phải xác nhận state hợp lệ.

### D. Import từng file và tạo mapping thủ công

1. Chọn `Plugins > AgentSpeak > Import AgentSpeak...`, mở `resident.asl`.
2. Trong `Mapping`, chọn và áp dụng lần lượt các suggestion
   `resident -> Resident` và
   `turn_on_lights -> Resident::turn_on_lights()` bằng
   `Apply selected suggestion`. Có thể dùng `Kind`, `Source`, `Target` và
   `Add / update` nếu cần nhập trực tiếp.
3. Bấm `Refresh USE Snapshot`; chọn `Diagram > BDI Plan > Fit` để chỉ đường từ
   `prepare_evening` tới operation UML.
4. Để thêm Environment/Organization, chọn
   `Plugins > AgentSpeak > Import JaCaMo Project...` và mở `smart-home.jcm`.
   `.jcm` tự tham chiếu `.asl` và XML; chuyển `Diagram` sang `MAS Overview`.

Kết thúc bằng cách so sánh mapping vừa xác nhận với `SmartHome.bdimap.json`.
Không bấm `Load...` file baseline trong kịch bản thủ công.

## 6. Flow phụ: organization độc lập

1. Mở `smart-home-organization.use` cùng `smart-home-organization.cmd` trong
   một phiên USE riêng.
2. Tạo Class/Object diagram để chỉ `ResidentRole`, `prepareHome()` và object
   `residentRole1`.
3. Đối chiếu với `smart-home-organization.xml`: role `resident`, mission
   `mPrepareHome`, goal `prepare_evening`.
4. Nói rõ đây là static consistency input, không chứng minh role đã được enact
   hay workspace đang chạy.

## 7. Lời thoại và kết quả mong đợi

- "BDI quyết định cần làm gì; USE biểu diễn cấu trúc và state để kiểm tra."
- "Workspace xuất hiện trong MAS Overview vì có trong `.jcm`, nhưng không có
  live CArtAgO state."
- "Mapping chỉ ra action `turn_on_lights` tương ứng operation nào trong UML."
- Snapshot vẫn giữ đèn tắt vì diagram không thực thi plan; đây là hành vi đúng
  của công cụ phân tích read-only.

## 8. Nếu demo không đúng

- Environment toggle bị disable: đã import `resident.asl` trực tiếp; hãy import
  `smart-home.jcm`.
- Object diagram trống: chạy `SmartHome.cmd` trước.
- Không thấy mapping: import project trước rồi load `SmartHome.bdimap.json`.
- Graph dày: tắt Organization/UML-OCL hoặc chuyển `BDI Plan`, sau đó bấm `Fit`.
