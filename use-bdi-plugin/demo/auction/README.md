# Demo Auction: BDI/MAS đối chiếu UML/OCL

## 1. Mục tiêu

Auction là case study đầy đủ nhất của USE BDI Plugin. Demo đi qua toàn bộ flow:

```text
JaCaMo project / AgentSpeak
        -> normalized BDI/MAS model
        -> confirmed BDI-UML mappings
        -> USE UML/OCL snapshot
        -> consistency findings + traceability
        -> Diagram / JSON / HTML / SVG evidence
```

Project có một auctioneer và hai bidder instance ở lớp MAS, organization Moise,
workspace/institution tĩnh, operation mapping, OCL precondition và một mutant
âm tính để minh họa `OCL-001`.

Đây vẫn là phân tích tĩnh. Plugin không khởi chạy JaCaMo, không tạo CArtAgO
workspace, không enact organization và không sinh runtime trace.

## 2. Các file trong ví dụ

| File | Nội dung và vai trò |
| --- | --- |
| `Auction.use` | UML/OCL baseline với `Auctioneer`, `Auction`, `Bidder`, `Bid`, association, invariant và pre/postcondition |
| `Auction.cmd` | Tạo snapshot gồm `auctioneer1`, `auction1` đang open, `bidder1`, `bid1`, các link và chạy `check` |
| `auctioneer.asl` | Belief trạng thái/đăng ký; các goal-plan `run_auction`, `receive_bid`, `finish_auction`; action `open`, `placeBid`, `close` |
| `bidder.asl` | Belief budget/auction; goal `submit_bid`; action UML, message `.send` và `.print` |
| `auction.jcm` | Project MAS tĩnh: 1 auctioneer, 2 bidder instance, workspace, institution và organization |
| `auction-organization.xml` | Moise role `auctioneer`/`participant`, mission, cardinality và norm |
| `auction-organization.use` | UML/OCL counterpart tĩnh cho role/class, mission/operation và cardinality review |
| `auction-institution.sai` | Placeholder institution có chủ đích; plugin giữ lại dưới dạng unsupported metadata thay vì bỏ qua im lặng |
| `Auction.bdimap.json` | 14 binding đã xác nhận: agent-class/object, action-operation, parameter và belief-attribute |
| `.bdi-plugin/rules.json` | Bật đủ 22 standard consistency rule cho case study |
| `.bdi-plugin/suppressions.json` | Danh sách suppression rỗng |
| `mutants/ocl-open-closed.use` | Mutant đổi precondition `Auction::open()` từ `draft` thành `closed`, dùng để tạo evidence `OCL-001` |
| `README.md` | Kịch bản demo hiện tại |

Reviewed corpus đầy đủ gồm baseline và bốn mutant được khai báo tại
[`auction-evaluation-manifest.json`](../../../docs/project/evidence/auction-evaluation-manifest.json).
Thư mục demo chỉ đưa mutant OCL dễ trình bày trực quan; các mutant khác nằm
trong evidence/test corpus để bảo đảm tái lập.

## 3. Mở baseline cùng snapshot

Sau bước chuẩn bị trong [README bộ demo](../README.md), chạy từ repository root:

```powershell
.\run-use-gui.ps1 -Demo auction
```

Nếu USE đã mở, chọn `File > Open specification...` và mở `Auction.use` trước.
Chỉ khi model đã hiện trong cây bên trái mới gõ trong shell dưới cùng:

```text
open "D:\_CODE_BANK\Project_\vnu-sme-lab\use\use-bdi-plugin\demo\auction\Auction.cmd"
```

Đợi `check` hoàn tất. Snapshot baseline có `auction1.status = #open`, reserve
price 100, bidder budget 150 và bid amount 120.

## 4. Kịch bản 1 - flow demo baseline (12-15 phút)

### Bước A - UML/OCL và snapshot

1. Chọn `View > Create View > Class diagram`.
2. Chỉ bốn class, các association ownership/participation và operation
   `open`, `placeBid`, `close`, `submitBid`.
3. Chọn `View > Create View > Object diagram`.
4. Chỉ `auctioneer1`, `auction1`, `bidder1`, `bid1` cùng các link. Nhấn mạnh
   OCL được đánh giá trên state cụ thể này, không chỉ trên class diagram.

### Bước B - Import project BDI/MAS

5. Chọn `Plugins > AgentSpeak > Import JaCaMo Project...`, chọn `auction.jcm`.
6. Trong tab `Explorer`, bung `auctioneer.asl` và `bidder.asl`:
   - auctioneer có chuỗi goal mở phiên, nhận bid và kết thúc;
   - bidder gọi `submitBid` rồi gửi message `bid_submitted`;
   - project có hai bidder instance dù chỉ dùng chung một source `bidder.asl`.
7. Chỉ diagnostics workspace/institution nếu xuất hiện. Đây là giới hạn static
   được báo rõ, không phải parser silently ignore.

### Bước C - Mapping và USE snapshot

8. Mở tab `Mapping`, bấm `Load...`, chọn `Auction.bdimap.json`.
9. Chỉ một ví dụ của từng loại mapping:
   - `AGENT_CLASS`: auctioneer/bidder sang class UML;
   - `AGENT_OBJECT`: source agent sang object snapshot;
   - `ACTION_OPERATION`: `placeBid` sang `Auction::placeBid(...)`;
   - `PARAMETER`: argument AgentSpeak sang parameter UML;
   - `BELIEF_ATTRIBUTE`: `auction_status/1` sang `Auction::status`.
10. Bấm `Refresh USE Snapshot`. Giải thích mapping suggestion chỉ là candidate;
    file này chứa binding đã xác nhận mới được rule sử dụng.

### Bước D - Diagram và traceability

11. Mở `Diagram`, chọn `MAS Overview`, bấm `Fit`.
12. Chỉ ba agent instance, organization, environment/institution metadata,
    BDI, UML/OCL và issue/evidence layer.
13. Bật/tắt `Organization`, `Environment`, `UML/OCL`, `Issues` để trình bày
    từng lớp. Bấm `Reset` để trở lại graph đầy đủ.
14. Chuyển sang `BDI Plan`, bấm `Fit`, chọn plan rồi dùng `Focus Goal/Plan` để
    chỉ chuỗi action theo thứ tự.
15. Mở `Problems`, chọn một finding; quay lại `Diagram` để cho thấy incoming
    evidence path được highlight mà không kéo theo issue sibling không liên quan.
16. Bấm `Export SVG...` để lưu đúng mode/layer/focus hiện tại. Dùng
    `Export Current Analysis...` nếu cần JSON hoặc HTML đầy đủ.

## 5. Kịch bản 2 - dựng Auction thủ công từ GUI (18-25 phút)

Khởi động USE không kèm model theo [kịch bản GUI thủ công](../README.md#5-kịch-bản-2---dựng-toàn-bộ-thủ-công-từ-gui),
và không mở `Auction.cmd`.

### A. Mở model và tạo từng object

1. Chọn `File > Open specification...`, mở `Auction.use`, rồi tạo Class
   diagram.
2. Qua `State > Create object...`, tạo:

   | Class | Object name |
   | --- | --- |
   | `Auctioneer` | `auctioneer1` |
   | `Auction` | `auction1` |
   | `Bidder` | `bidder1` |
   | `Bid` | `bid1` |

3. Chọn `View > Create View > Object diagram`.

### B. Nhập từng giá trị

Nhấp đúp node, nhập giá trị trong `Object properties`, bấm `Apply`:

| Object | Giá trị OCL cần nhập |
| --- | --- |
| `auctioneer1` | `name = 'auctioneer'` |
| `auction1` | `title = 'Demo auction'`, `status = #open`, `reservePrice = 100.0` |
| `bidder1` | `name = 'bidder1'`, `budget = 150.0` |
| `bid1` | `amount = 120.0` |

### C. Nối từng quan hệ

Giữ `Ctrl`, chọn đúng hai object, nhấp phải và tạo lần lượt:

| Objects | Mục menu cần chọn |
| --- | --- |
| `auctioneer1`, `auction1` | `insert (auctioneer1,auction1) into AuctioneerAuctions` |
| `auction1`, `bidder1` | `insert (auction1,bidder1) into AuctionBidders` |
| `auction1`, `bid1` | `insert (auction1,bid1) into AuctionBids` |
| `bidder1`, `bid1` | `insert (bidder1,bid1) into BidderBids` |

Chọn `State > Check structure now`. Log phải xác nhận snapshot thỏa invariant
và multiplicity.

### D. Import từng source và xác nhận mapping

1. Để giới thiệu từng file, chọn
   `Plugins > AgentSpeak > Import AgentSpeak...`, mở `auctioneer.asl`; lặp lại
   thao tác và mở `bidder.asl` trong Explorer thứ hai.
2. Để phân tích hai source cùng nhau, gọi `Import AgentSpeak...` lần nữa và
   chọn đồng thời `auctioneer.asl`, `bidder.asl` trong file chooser. Cách này
   chỉ có source-level agents, chưa có ba MAS instance.
3. Trong `Mapping > Suggestions`, lần lượt áp dụng các candidate chính:
   `auctioneer -> Auctioneer`, `bidder -> Bidder`, `open -> Auction::open()`,
   `placeBid -> Auction::placeBid(...)`, `close -> Auction::close()` và
   `submitBid -> Bidder::submitBid(...)`. Tiếp tục với parameter/belief
   candidate nếu muốn tái tạo đủ 14 binding.
4. Dùng `Apply selected suggestion` cho từng candidate. Nếu nhập trực tiếp,
   chọn `Kind`, chép stable `Source` đang hiển thị, nhập qualified `Target`,
   rồi bấm `Add / update`.
5. Bấm `Refresh USE Snapshot`, mở `Diagram > BDI Plan`, bấm `Fit`, rồi kiểm tra
   `Problems`. Không `Load...` `Auction.bdimap.json` trong kịch bản này.
6. Cuối cùng, chọn `Plugins > AgentSpeak > Import JaCaMo Project...` và mở
   `auction.jcm` để chuyển sang `MAS Overview`. `.jcm` tự tham chiếu hai `.asl`,
   XML và SAI; plugin không có nút import riêng cho XML/SAI.

Lưu ý: import từng `.asl` chứng minh parser/source flow; import `.jcm` mới cho
thấy 1 auctioneer, 2 bidder instance và các layer MAS tĩnh.

## 6. Flow negative case `OCL-001` (5 phút)

Nên chạy trong một phiên USE mới để tránh lẫn model/snapshot baseline:

```powershell
.\run-use-gui.ps1 `
    -Specification '.\use-bdi-plugin\demo\auction\mutants\ocl-open-closed.use' `
    -CommandFile '.\use-bdi-plugin\demo\auction\Auction.cmd'
```

Sau đó:

1. Chọn `Plugins > AgentSpeak > Import JaCaMo Project...` và mở lại
   `auction.jcm`.
2. Vào `Mapping`, bấm `Load...`, chọn
   `docs/project/evidence/auction-ocl.bdimap.json`. File này có fingerprint của
   mutant; không dùng mapping baseline cho bước negative case.
3. Bấm `Refresh USE Snapshot`.
4. Mở `Problems`, lọc/chọn rule `OCL-001`.
5. Mở `Diagram`, bấm `Fit`; chỉ evidence từ source action qua mapping,
   `Auction::open()`, OCL precondition đến issue.
6. So sánh hai dòng:

   ```text
   Baseline: self.status = #draft
   Mutant:   self.status = #closed
   ```

7. Không gọi mọi finding khác là mutant detection. Reviewed scope được xác định
   bởi manifest và evidence token; baseline có thể chứa finding tiềm năng ngoài
   scope do static reference/runtime evidence chưa đủ.

## 7. Lời thoại gợi ý

- "Jason/JaCaMo parser là syntax authority; plugin không viết parser thứ hai."
- "Normalized IR và current analysis snapshot là immutable; Diagram không chạy
  lại validation."
- "USE là authority cho UML/OCL và state; plugin chỉ chiếu state read-only."
- "`UNKNOWN` hoặc unsupported metadata được giữ rõ ràng, không đổi thành PASS."
- "Kết quả mutant là evidence có scope, không phải tuyên bố chứng minh đúng cho
  mọi mô hình BDI/UML."

## 8. Kết quả mong đợi

- Class/Object diagram compile và snapshot baseline thỏa invariant cấu trúc.
- Explorer hiển thị hai source và ba MAS agent instance.
- Mapping đã load có các cạnh confirmed tới UML operation/attribute.
- `MAS Overview` phân tách BDI, Organization, Environment, UML/OCL và Issues.
- Mutant OCL tạo đường evidence có `OCL-001` trong reviewed flow.
- Đóng/mở Diagram, đổi filter hoặc export không thay đổi USE state fingerprint.

## 9. Kiểm tra headless trước buổi demo

```powershell
$reportDir = Join-Path $repo 'target\demo-reports'
New-Item -ItemType Directory -Force $reportDir | Out-Null
$pluginJar = Join-Path $useHome 'lib\plugins\use-bdi-plugin-7.1.1.jar'
$guiJar = Join-Path $useHome 'lib\use-gui.jar'
$cp = "$pluginJar;$guiJar"

& $javaExecutable -cp $cp org.tzi.use.plugins.bdi.cli.BdiQualityGateMain `
    --use (Join-Path $demo 'Auction.use') `
    --jcm (Join-Path $demo 'auction.jcm') `
    --mapping (Join-Path $demo 'Auction.bdimap.json') `
    --rules (Join-Path $demo '.bdi-plugin\rules.json') `
    --json (Join-Path $reportDir 'auction.json') `
    --html (Join-Path $reportDir 'auction.html') `
    --timestamp 2026-08-12T00:00:00Z --overwrite
$LASTEXITCODE
```

Exit `0` là clean, `1` là có confirmed finding, `2` là chỉ có
potential/unknown, `3` là input/config không hợp lệ, `4` là lỗi hạ tầng/output.
Không coi mọi exit khác 0 là lỗi parser; hãy mở report để đọc evidence.

## 10. Nếu demo không đúng

- Không thấy plugin/menu: build lại assembly, đóng toàn bộ USE và mở bằng đúng
  distribution cùng `-H=$useHome`.
- Object diagram trống: `Auction.cmd` chưa chạy.
- Chỉ thấy hai source nhưng không thấy ba instance: đang import hai `.asl` trực
  tiếp; hãy import `auction.jcm`.
- Không thấy organization/environment: kiểm tra `.jcm`, XML và `.sai` nằm cùng
  thư mục, rồi import project lại.
- Mapping stale/unknown: kiểm tra đang mở đúng model baseline hoặc mutant có
  cùng qualified UML target. Baseline dùng `Auction.bdimap.json`, còn mutant
  `OCL-001` dùng `docs/project/evidence/auction-ocl.bdimap.json`; sau đó bấm
  `Refresh USE Snapshot`.
- Graph quá dày: dùng layer checkbox, `Focus Goal/Plan`, rồi bấm `Fit`.
