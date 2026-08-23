# Bộ demo USE BDI Plugin

Đây là bộ ví dụ chính thức dùng để học, kiểm thử và trình bày trực tiếp plugin
BDI trong USE 7.1.1. Mỗi thư mục là một gói độc lập, gồm mô hình UML/OCL,
snapshot đối tượng, mã AgentSpeak, mapping và tài nguyên JaCaMo tĩnh khi cần.

## 1. Chọn demo phù hợp

| Demo | Mức độ | Nội dung nổi bật | File bắt đầu |
| --- | --- | --- | --- |
| [`family-person`](family-person/) | Cơ bản | Goal - plan - action và mapping `Person::greet()` | `Family.use`, `Family.cmd` |
| [`smart-home`](smart-home/) | Cơ bản | BDI và Environment trong một project `.jcm` tĩnh | `SmartHome.use`, `SmartHome.cmd` |
| [`smart-queue`](smart-queue/) | Trung bình | Ba nhánh ra quyết định theo độ dài hàng đợi và quầy rảnh | `SmartQueue.use`, `SmartQueue.cmd` |
| [`auction`](auction/) | Đầy đủ | MAS, mapping, UML/OCL, evidence và mutant `OCL-001` | `Auction.use`, `Auction.cmd` |

Nếu chỉ có 5 phút, dùng `family-person`. Nếu cần trình bày contribution của
khóa luận, dùng `auction`. `smart-queue` phù hợp nhất để giải thích decision
making, còn `smart-home` phù hợp để phân biệt BDI, organization và environment.

## 2. Ý nghĩa các loại file

| Loại file | Vai trò trong demo |
| --- | --- |
| `*.use` | Đặc tả UML gồm class, association, operation và OCL |
| `*.cmd` | Tạo snapshot object/link xác định trong USE và chạy `check` |
| `*.asl` | Belief, goal, plan, context và action của Jason AgentSpeak |
| `*.jcm` | Thành phần project JaCaMo được plugin phân tích tĩnh |
| `*-organization.xml` | Mô hình organization Moise được chuẩn hóa tĩnh |
| `*.bdimap.json` | Các mapping BDI - UML đã xác nhận, dùng được sau khi import |
| `.bdi-plugin/rules.json` | Danh sách rule được bật riêng cho demo |
| `.bdi-plugin/suppressions.json` | Suppression có fingerprint; các baseline hiện để trống |
| `mutants/*` | Biến thể cố ý sai, chỉ dùng cho phần negative case |

Plugin không chạy Jason/JaCaMo/CArtAgO/Moise runtime. Diagram và finding là
kết quả phân tích tĩnh từ immutable snapshot, không phải runtime trace.

## 3. Chuẩn bị USE trước buổi demo

Từ thư mục gốc repository, dùng launcher để tự tìm Java, build khi chưa có
distribution, giải nén và đặt đúng `USE_HOME`:

```powershell
Set-Location '<repo-root>'
.\run-use-gui.ps1 -ValidateOnly
```

Thay `<repo-root>` bằng thư mục checkout hiện tại của repository.

Nếu vừa sửa source/plugin, dùng `.\run-use-gui.ps1 -Rebuild`. Không chạy riêng
`use-gui\target\use-gui.jar`: launcher luôn dùng packaged distribution có đúng
`oclextensions`, plugin JAR và `-H=<USE_HOME>`.

## 4. Kịch bản 1 - chạy nhanh bằng snapshot có sẵn

Mỗi README con có lệnh mở đúng model và snapshot. Sau khi USE hiện lên, flow
chuẩn là:

1. Chọn `View > Create View > Class diagram` để giới thiệu UML/OCL.
2. Chọn `View > Create View > Object diagram` để giới thiệu snapshot từ `.cmd`.
3. Chọn `Plugins > AgentSpeak > Import AgentSpeak...` cho `.asl`, hoặc
   `Plugins > AgentSpeak > Import JaCaMo Project...` cho `.jcm`.
4. Trong BDI Explorer, mở tab `Explorer` và bung agent, goal, plan, context,
   ordered steps.
5. Mở tab `Mapping`, bấm `Load...` và chọn file `.bdimap.json` của demo.
6. Bấm `Refresh USE Snapshot` để analysis dùng đúng object state đang mở.
7. Mở tab `Diagram`, chọn mode phù hợp và bấm `Fit`.
8. Chọn node/issue, dùng `Focus Goal/Plan` hoặc layer checkbox để làm rõ graph.
9. Mở `Problems` để xem rule ID, certainty, source và evidence.
10. Dùng `Export SVG...` cho graph hiện tại hoặc `Export Current Analysis...`
    để xuất JSON/HTML.

`View > Create View > Object diagram` chỉ tạo cửa sổ hiển thị. Object phải được
tạo trước bằng file `.cmd`. Launcher luôn nạp `.use` trước `.cmd`; không mở
`.cmd` khi log còn báo `No System available`.

```text
open "D:\...\demo-name\Model.cmd"
```

## 5. Kịch bản 2 - dựng toàn bộ thủ công từ GUI

Kịch bản này phù hợp khi cần chứng minh USE đang tạo state thật, thay vì chỉ
đọc một snapshot chuẩn bị sẵn. Không truyền `.use`/`.cmd` và không chạy
`open "...cmd"`:

```powershell
.\run-use-gui.ps1
```

### A. Mở UML/OCL và tạo object

1. Chọn `File > Open specification...`, vào thư mục demo và mở file `.use`.
2. Chọn `View > Create View > Class diagram` để xác nhận model đã compile.
3. Với từng object trong bảng của README con, chọn `State > Create object...`.
4. Chọn class, nhập `Object name`, bấm `Create`; lặp lại rồi bấm `Close`.
5. Chọn `View > Create View > Object diagram`. Object mới tạo sẽ xuất hiện.

### B. Nhập từng thuộc tính bằng chuột

1. Nhấp đúp object trong Object diagram. USE mở cửa sổ `Object properties`.
2. Sửa ô ở cột giá trị, dùng đúng literal OCL: chuỗi `'Alice'`, số `28` hoặc
   `100.0`, Boolean `true`/`false`, enum `#open`.
3. Bấm `Apply` sau khi nhập xong một object. Có thể chọn object tiếp theo trong
   hộp chọn ở đầu cửa sổ thay vì đóng cửa sổ.
4. Nếu nhập sai, bấm `Reset`, sửa literal rồi `Apply` lại.

### C. Nối từng association bằng chuột

1. Trong Object diagram, giữ `Ctrl` và bấm các object tham gia association.
2. Nhấp phải vùng đang chọn. Chọn đúng mục có dạng
   `insert (object1,object2) into AssociationName`.
3. Lặp lại cho từng link trong bảng của README con. Link xuất hiện ngay trên
   Object diagram; nếu không thấy mục `insert`, kiểm tra class và số object đã
   chọn.
4. Chọn `State > Check structure now`. Cửa sổ log phải không báo vi phạm
   invariant hoặc multiplicity.

Nếu thao tác chọn node khó khi trình chiếu, vẫn giữ GUI mở và nhập từng lệnh
riêng lẻ trong shell dưới cùng. Đây vẫn là dựng state thủ công, không chạy file
`.cmd`:

```text
!create alice : Person
!set alice.name := 'Alice'
!insert (family1, alice) into FamilyMembers
check
```

### D. Import từng đầu vào BDI và xác nhận mapping

1. Chọn `Plugins > AgentSpeak > Import AgentSpeak...` và chọn file `.asl` được
   ghi trong README con. Với nhiều source, có thể mở từng source để giới thiệu;
   khi cần phân tích chung, chọn các source cùng lúc trong file chooser.
2. Nếu demo có `.jcm`, chọn `Plugins > AgentSpeak > Import JaCaMo Project...`
   và mở `.jcm` để có MAS instance, organization và environment tĩnh. XML/SAI
   được `.jcm` tham chiếu, không có nút import XML/SAI riêng.
3. Vào tab `Mapping`. Để nối thủ công, chọn từng candidate trong
   `Suggestions` rồi bấm `Apply selected suggestion`. Nếu thiếu candidate,
   chọn `Kind`, nhập `Source`/`Target` và bấm `Add / update`.
4. Không bấm `Load...` file `.bdimap.json` trong kịch bản này; file đó chỉ là
   baseline để đối chiếu. Bấm `Save...` nếu muốn lưu mapping vừa xác nhận.
5. Bấm `Refresh USE Snapshot`, mở `Diagram`, chọn mode, bấm `Fit`, rồi kiểm tra
   `Problems`. Mọi thay đổi state sau đó đều cần `Refresh USE Snapshot` lại.

README từng demo cung cấp danh sách object, literal, link và mapping tối thiểu.
Nên tập trước bằng `family-person`; `smart-queue` và `auction` có nhiều object
nên cần nhiều thời gian hơn kịch bản chạy nhanh.

## 6. Kết quả mong đợi

- Class/Object diagram của USE hiển thị đúng model và snapshot.
- BDI Explorer giữ cấu trúc AgentSpeak, mapping và finding có source/evidence.
- `Diagram` chỉ là presentation read-only; đổi mode, layer hoặc `Fit` không làm
  thay đổi model USE.
- `family-person` và `smart-home` là baseline sạch theo rule configuration cục
  bộ; không chứa mutant.
- `auction` có thể có finding ngoài scope mutant. Luôn đọc rule ID, certainty
  và evidence thay vì coi mọi finding là lỗi parser.

## 7. Trước khi lên trình bày

```powershell
mvn --batch-mode --no-transfer-progress -pl use-bdi-plugin -am test
powershell -ExecutionPolicy Bypass -File .\use-bdi-plugin\scripts\smoke.ps1
```

Kỳ vọng có `BUILD SUCCESS` và `GUI_SMOKE_OK`. Nên mở thử demo, đặt sẵn kích
thước cửa sổ, chạy `Fit`, kiểm tra font tiếng Việt và chuẩn bị một thư mục trống
để xuất SVG/report. Hướng dẫn tổng thể chi tiết hơn nằm tại
[`docs/guide/guide.md`](../../docs/guide/guide.md).
