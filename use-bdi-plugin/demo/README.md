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

Từ thư mục gốc repository, dùng Java 21 và build distribution:

```powershell
Set-Location 'D:\_CODE_BANK\Project_\vnu-sme-lab\use'
mvn --batch-mode --no-transfer-progress -pl use-assembly -am package

$repo = (Resolve-Path '.').Path
$zip = Join-Path $repo 'use-assembly\target\use-7.1.1.zip'
$runtime = Join-Path $repo 'use-assembly\target\demo-runtime'
Expand-Archive -LiteralPath $zip -DestinationPath $runtime -Force
$useHome = (Get-ChildItem $runtime -Directory |
    Where-Object { Test-Path (Join-Path $_.FullName 'lib\use-gui.jar') } |
    Select-Object -First 1).FullName
$javaExecutable = if ($env:JAVA_HOME) {
    Join-Path $env:JAVA_HOME 'bin\java.exe'
} else {
    (Get-Command java -ErrorAction Stop).Source
}
```

Không chạy riêng `use-gui.jar` trong source tree khi demo package. Distribution
đã giải nén chứa đúng `lib/plugins/use-bdi-plugin-7.1.1.jar` và cần tham số
`-H=$useHome`.

## 4. Flow UI chung

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
tạo trước bằng file `.cmd`. Nếu mở model thủ công, gõ lệnh sau trong shell ở
đáy cửa sổ USE:

```text
open "D:\...\demo-name\Model.cmd"
```

## 5. Kết quả mong đợi

- Class/Object diagram của USE hiển thị đúng model và snapshot.
- BDI Explorer giữ cấu trúc AgentSpeak, mapping và finding có source/evidence.
- `Diagram` chỉ là presentation read-only; đổi mode, layer hoặc `Fit` không làm
  thay đổi model USE.
- `family-person` và `smart-home` là baseline sạch theo rule configuration cục
  bộ; không chứa mutant.
- `auction` có thể có finding ngoài scope mutant. Luôn đọc rule ID, certainty
  và evidence thay vì coi mọi finding là lỗi parser.

## 6. Trước khi lên trình bày

```powershell
mvn --batch-mode --no-transfer-progress -pl use-bdi-plugin -am test
powershell -ExecutionPolicy Bypass -File .\use-bdi-plugin\scripts\smoke.ps1
```

Kỳ vọng có `BUILD SUCCESS` và `GUI_SMOKE_OK`. Nên mở thử demo, đặt sẵn kích
thước cửa sổ, chạy `Fit`, kiểm tra font tiếng Việt và chuẩn bị một thư mục trống
để xuất SVG/report. Hướng dẫn tổng thể chi tiết hơn nằm tại
[`docs/guide/guide.md`](../../docs/guide/guide.md).
