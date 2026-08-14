# Demo Family Person: một Person trong Family

## 1. Mục tiêu

Đây là ví dụ cơ bản nhất của bộ demo. Nó cho thấy một agent `person` có goal
`introduce_family`, thực hiện plan gọi action `greet`, rồi action được mapping
tới `Person::greet()` trong UML. Snapshot USE cho thấy `alice` và `ben` thuộc
`family1` và thỏa các invariant cơ bản.

Ví dụ là baseline hợp lệ, không có mutant và không khởi chạy JaCaMo/Moise
runtime.

## 2. Các file trong ví dụ

| File | Nội dung và vai trò |
| --- | --- |
| `Family.use` | UML/OCL chính với `Person`, `Family`, association `FamilyMembers`, invariant tên/tuổi và `HasAdult` |
| `Family.cmd` | Tạo `family1`, `alice` (28 tuổi), `ben` (8 tuổi), link membership và chạy `check` |
| `person.asl` | Goal `introduce_family`, plan cùng action `greet` và lệnh in |
| `family-person.jcm` | Project JaCaMo tĩnh gồm agent `person` và organization `family_organization` |
| `family-organization.xml` | Moise organization: role `family_member`, mission `mIntroduce`, goal `introduce` |
| `family-organization.use` | UML/OCL độc lập để minh họa class `FamilyMember` và operation `introduce()` |
| `family-organization.cmd` | Tạo `familyMember1` và kiểm tra invariant organization độc lập |
| `FamilyPerson.bdimap.json` | Mapping agent sang class `Person` và action `greet` sang `Person::greet()` |
| `.bdi-plugin/rules.json` | Rule set của baseline; bỏ `OCL-004` vì demo không khai báo bounded `soil:` effect |
| `.bdi-plugin/suppressions.json` | Danh sách suppression rỗng, chứng minh baseline không che finding bằng suppression |
| `README.md` | Kịch bản demo hiện tại |

## 3. Mở đúng model và snapshot

Sau bước chuẩn bị trong [README bộ demo](../README.md), chạy:

```powershell
.\run-use-gui.ps1 -Demo family-person
```

Nếu USE đã mở, phải mở `Family.use` qua `File > Open specification...` trước.
Chỉ khi model đã hiện trong cây bên trái mới nhập:

```text
open "D:\_CODE_BANK\Project_\vnu-sme-lab\use\use-bdi-plugin\demo\family-person\Family.cmd"
```

## 4. Kịch bản 1 - flow demo nhanh (5-7 phút)

1. Chọn `View > Create View > Class diagram`.
   Chỉ hai class, association `FamilyMembers` và invariant `HasAdult`.
2. Chọn `View > Create View > Object diagram`.
   Chỉ `family1`, `alice`, `ben` và hai link membership.
3. Chọn `Plugins > AgentSpeak > Import JaCaMo Project...`, chọn
   `family-person.jcm`.
4. Trong tab `Explorer`, bung `person.asl`, goal `introduce_family`, plan cùng
   ordered steps `greet` và `.print`.
5. Mở `Mapping`, bấm `Load...`, chọn `FamilyPerson.bdimap.json`.
6. Bấm `Refresh USE Snapshot`, sau đó mở `Diagram`.
7. Chọn `BDI Plan`, bấm `Fit` và chỉ đường:

   ```text
   introduce_family -> introduce_family plan -> greet -> Person::greet()
   ```

8. Chọn goal/plan rồi bấm `Focus Goal/Plan`; bấm `Reset` để trở lại toàn graph.
9. Mở `Problems`. Baseline phải sạch theo cấu hình rule cục bộ; nếu có finding,
   mở evidence thay vì bỏ qua.
10. Bấm `Export SVG...` để lưu đúng graph đang hiển thị hoặc
    `Export Current Analysis...` để lưu JSON/HTML.

## 5. Kịch bản 2 - dựng Family thủ công từ GUI (8-10 phút)

Khởi động USE không kèm model theo [kịch bản GUI thủ công](../README.md#5-kịch-bản-2---dựng-toàn-bộ-thủ-công-từ-gui),
sau đó làm lần lượt và không mở `Family.cmd`.

### A. Mở model và tạo từng object

1. Chọn `File > Open specification...` và mở `Family.use`.
2. Chọn `View > Create View > Class diagram` để chỉ `Family`, `Person` và
   association `FamilyMembers`.
3. Chọn `State > Create object...`, tạo lần lượt:

   | Class | Object name |
   | --- | --- |
   | `Family` | `family1` |
   | `Person` | `alice` |
   | `Person` | `ben` |

4. Chọn `View > Create View > Object diagram`.

### B. Nhập từng giá trị

Nhấp đúp từng node, sửa cột giá trị trong `Object properties` rồi bấm `Apply`:

| Object | Giá trị OCL cần nhập |
| --- | --- |
| `family1` | `name = 'Nguyen family'` |
| `alice` | `name = 'Alice'`, `age = 28` |
| `ben` | `name = 'Ben'`, `age = 8` |

### C. Nối từng quan hệ

1. Giữ `Ctrl`, chọn `family1` và `alice`, nhấp phải rồi chọn
   `insert (family1,alice) into FamilyMembers`.
2. Làm tương tự với `family1` và `ben`.
3. Chọn `State > Check structure now`; invariant `HasAdult` phải hợp lệ vì
   `alice.age = 28`.

### D. Import từng file và tạo mapping thủ công

1. Chọn `Plugins > AgentSpeak > Import AgentSpeak...`, mở `person.asl` để chỉ
   riêng goal, plan và action của agent.
2. Trong Explorer vừa mở, vào `Mapping`, chọn candidate `person -> Person` rồi
   bấm `Apply selected suggestion`; làm tương tự với
   `greet -> Person::greet()`.
3. Nếu candidate không có, chọn `AGENT_CLASS` hoặc `ACTION_OPERATION`, nhập
   `Source`/`Target` đúng như trên rồi bấm `Add / update`.
4. Bấm `Refresh USE Snapshot`, mở `Diagram`, chọn `BDI Plan` và bấm `Fit`.
5. Muốn bổ sung organization, chọn
   `Plugins > AgentSpeak > Import JaCaMo Project...` và mở
   `family-person.jcm`. File này tự tham chiếu `person.asl` và
   `family-organization.xml`; không import XML bằng menu riêng.

Đối chiếu cuối cùng: mapping tự tạo phải có cùng ý nghĩa với
`FamilyPerson.bdimap.json`, nhưng kịch bản này không dùng nút `Load...`.

## 6. Flow phụ: organization UML/OCL độc lập (2 phút)

Flow này chỉ dùng khi cần giải thích ánh xạ organization, không thay thế model
Family chính:

1. Mở phiên USE khác với `family-organization.use` và
   `family-organization.cmd`.
2. Tạo Class diagram và Object diagram.
3. Chỉ class `FamilyMember`, operation `introduce()` và object
   `familyMember1` thỏa invariant.
4. Quay lại `family-person.jcm` để nhấn mạnh Moise XML được import tĩnh, không
   enact role/mission ở runtime.

## 7. Lời thoại và kết quả mong đợi

- "Đây là vertical slice nhỏ nhất: từ goal AgentSpeak tới operation UML."
- "`Family.cmd` cung cấp state cụ thể để USE kiểm tra invariant, còn `.asl` mô
  tả hành vi có chủ đích của agent."
- "File `.jcm` chỉ gom nguồn agent và organization; plugin không chạy MAS."
- Class/Object diagram hợp lệ, BDI tree có một goal/plan, mapping `greet` hiện
  rõ và `Diagram` không làm thay đổi snapshot.

## 8. Nếu demo không đúng

- Không thấy `alice`/`ben`: chạy lại `Family.cmd` trước khi mở Object diagram.
- Không thấy organization: phải import `family-person.jcm`, không chỉ
  `person.asl`.
- Có `OCL-004`: kiểm tra plugin đã tự load `.bdi-plugin/rules.json` cạnh model.
- Mapping trống: import project trước, sau đó mới load `FamilyPerson.bdimap.json`.
