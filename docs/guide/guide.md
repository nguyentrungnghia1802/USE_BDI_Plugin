# USE BDI Plugin Demo Guide

Tài liệu này là kịch bản chạy demo trực tiếp trên USE 7.1.1. Mục tiêu là
trình bày rõ đường đi từ mô hình UML/OCL và AgentSpeak đến BDI Explorer,
mapping, Problems và report. Các demo chính nằm trong
[`use-bdi-plugin/demo`](../../use-bdi-plugin/demo/).

## 1. Chuẩn bị

Thực hiện từ thư mục repository:

```powershell
Set-Location 'D:\_CODE_BANK\Project_\vnu-sme-lab\use'
java -version
mvn -version
```

Nên dùng Java 21. Nếu máy có nhiều Java, đặt `JAVA_HOME` trước khi build và
chạy để tránh Java cũ trong `PATH`:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21'
$javaExecutable = Join-Path $env:JAVA_HOME 'bin\java.exe'
& $javaExecutable -version
```

## 2. Build và mở USE GUI

Cách khuyến nghị là dùng launcher. Lệnh này tự tìm Java, build nếu chưa có
distribution, giải nén và truyền đúng `-H=<USE_HOME>`:

```powershell
.\run-use-gui.ps1
```

Mở thẳng baseline hoàn chỉnh hoặc kiểm tra cấu hình mà không mở GUI:

```powershell
.\run-use-gui.ps1 -Demo smart-queue
.\run-use-gui.ps1 -Demo smart-queue -ValidateOnly
```

Sau khi chỉnh code, đóng USE hoàn toàn rồi chạy `.\run-use-gui.ps1 -Rebuild` để
plugin JAR mới được đóng gói. Không chạy `use-gui\target\use-gui.jar`; launcher
dùng distribution có `oclextensions` và `lib\plugins` đầy đủ.

## 3. Quy tắc chọn file

Các dialog do plugin sở hữu hiện mở mặc định tại:

```text
D:\_CODE_BANK\Project_\vnu-sme-lab\use
```

Quy tắc này áp dụng cho `Import AgentSpeak`, `Import JaCaMo Project`, load/save
mapping và export report. Khi plugin khởi tạo, nó cũng đặt USE
`Options.setLastDirectory(...)`, nên `File > Open specification...` bắt đầu ở
cùng thư mục sau khi plugin đã được nạp. Nếu checkout được chuyển sang máy
khác, plugin dùng working directory hiện tại làm fallback.

## 4. Demo A: Auction

### Nội dung

Mở thư mục [`demo/auction`](../../use-bdi-plugin/demo/auction/). Các file cần
biết:

| File | Vai trò |
| --- | --- |
| `Auction.use` | UML classes, associations, operations và OCL pre/postconditions |
| `auctioneer.asl`, `bidder.asl` | AgentSpeak beliefs, goals, plans và actions |
| `auction.jcm` | Khai báo static JaCaMo agents/resources |
| `auction-organization.xml` | Moise organization subset để import tĩnh |
| `Auction.bdimap.json` | Mapping baseline đã xác nhận |
| `Auction.cmd` | Snapshot object/link để kiểm tra OCL trên trạng thái cụ thể |
| `mutants/ocl-open-closed.use` | Biến thể cố ý sai precondition để show `OCL-001` |

### Kịch bản UI khuyến nghị

1. Khởi động USE với `Auction.use` và `Auction.cmd` làm specification/command
   file, hoặc mở `Auction.use` rồi thực thi `Auction.cmd`.
2. Chọn `View > Create View > Class diagram`. Đây là lúc nên trình bày các
   class `Auctioneer`, `Auction`, `Bidder`, `Bid` và các operation OCL.
3. Chọn `View > Create View > Object diagram` để show object/link snapshot.
4. Chọn `Plugins > AgentSpeak > Import JaCaMo Project...`. Dialog đã mở sẵn
   repository root; chọn `use-bdi-plugin\demo\auction\auction.jcm`.
5. Trong BDI Explorer, mở rộng cây để show hai AgentSpeak sources, beliefs,
   goals, plans và ordered steps. `.jcm` được phân tích tĩnh, không khởi chạy
   JaCaMo runtime.
6. Mở tab `Problems`. Các cảnh báo về workspace/institution là resource
   unsupported được giữ lại có chủ đích; không được xem là parser bị im lặng.
7. Mở tab `Mapping`, bấm `Load...`, chọn `Auction.bdimap.json`, sau đó show các
   binding `AGENT_CLASS`, `AGENT_OBJECT`, `ACTION_OPERATION` và `PARAMETER`.
   Có thể chọn `Apply selected suggestion`, nhưng suggestion chỉ là candidate.
8. Bấm `Refresh USE Snapshot` sau khi thay đổi object/state. Bấm
   `Export Current Analysis...`, chọn JSON hoặc HTML và lưu report để làm bằng
   chứng trình bày.
9. Negative case: đóng USE hoặc mở lại, load
   `mutants\ocl-open-closed.use`, import lại hai file `.asl`, bấm
   `Refresh USE Snapshot` và show finding `OCL-001` trong `Problems`.

### Điều cần nói khi thuyết trình

Plugin dùng Jason và JaCaMo parser chính thức để tạo IR bất biến, sau đó chiếu
USE theo hướng read-only. `.jcm`, CArtAgO và Moise ở phiên bản này là static
inspection; chưa có enactment, live workspace hay runtime trace.

## 5. Demo B: Smart Queue

### Nội dung

Mở thư mục [`demo/smart-queue`](../../use-bdi-plugin/demo/smart-queue/):

- `SmartQueue.use`: UML/OCL cho `Queue`, `Customer`, `Counter`, `Manager` và
  `Staff`.
- `smart_queue_manager.asl`: decision-making plans cho queue đông, counter
  rảnh và không có counter rảnh.
- `SmartQueue.cmd`: snapshot xác định gồm một queue, sáu customer và hai
  counter.

### Mở GUI cùng snapshot

Cách này chạy GUI và tự thực thi command file sau khi compile model:

```powershell
.\run-use-gui.ps1 -Demo smart-queue
```

Nếu USE đã mở sẵn, có thể mở `SmartQueue.use` bằng `File > Open specification...`
và chạy từng dòng trong shell/command workflow; cách truyền `.cmd` ở trên là
ổn định nhất cho demo vì tạo lại đúng snapshot mỗi lần.

### Kịch bản UI

1. Chọn `View > Create View > Class diagram` để show UML/OCL structure.
2. Chọn `View > Create View > Object diagram` để show `queue1`, sáu customer
   và hai counter sau khi `SmartQueue.cmd` chạy.
3. Chọn `Plugins > AgentSpeak > Import AgentSpeak...` hoặc bấm `Import .asl...`
   trong BDI Explorer; chọn `smart_queue_manager.asl`.
4. Mở rộng goal `reduce_waiting_time` và các plan để show nhánh quyết định
   `assign_customer` khi queue có sáu customer và `counter2` đang free.
5. Trong `Mapping`, bấm `Load...`, chọn `SmartQueue.bdimap.json`; mở
   `Diagram`, chọn `BDI Plan`, bấm `Fit` và show đường đi tới
   `Manager::assignCustomer(...)`. Có thể chọn plan rồi bấm `Focus Goal/Plan`.
6. Bấm `Refresh USE Snapshot`, rồi mở `Problems`. Invariant
   `SizeMatchesCustomers` được kiểm tra trên snapshot USE hiện tại.
7. Thay đổi state trong USE, bấm lại `Refresh USE Snapshot` và giải thích rằng
   plugin không tự sửa `MSystem`, không tự chạy plan và không tự nhận event live.
8. Sau khi chọn mode/layer/focus phù hợp trong `Diagram`, bấm `Export SVG...`
   để lưu đúng graph đang hiển thị; chỉ xác nhận overwrite khi thực sự muốn.

## 6. Demo C: Family Person

### Nội dung

Mở thư mục [`demo/family-person`](../../use-bdi-plugin/demo/family-person/):

- `Family.use` và `Family.cmd`: UML/OCL cùng snapshot có `family1`, `alice`,
  `ben` và liên kết `FamilyMembers`.
- `person.asl`: goal `introduce_family`, plan và action `greet`.
- `family-person.jcm` và `family-organization.xml`: project JaCaMo/Moise tĩnh.
- `family-organization.use` và `family-organization.cmd`: snapshot UML/OCL
  độc lập cho organization layer.
- `FamilyPerson.bdimap.json`: mapping AgentSpeak agent và `greet` action.

### Kịch bản UI

1. Mở `Family.use`, chạy `Family.cmd`, rồi chọn `View > Create View > Class
   diagram` và `View > Create View > Object diagram`.
2. Chọn `Plugins > AgentSpeak > Import JaCaMo Project...`, mở
   `family-person.jcm` và mở rộng `person.asl` trong BDI Explorer.
3. Trong `Mapping`, bấm `Load...` và chọn `FamilyPerson.bdimap.json`; sau đó
   mở `Diagram`, chọn `BDI Plan`, bấm `Fit` và show đường đi
   `introduce_family -> greet -> Person::greet()`.
4. Bấm `Refresh USE Snapshot` và mở `Problems`.

Đây là ví dụ cơ bản nhất để giải thích quan hệ một `Person` thuộc một
`Family`, rồi nối goal/plan/action AgentSpeak vào mô hình UML/OCL.

## 7. Demo D: Smart Home

### Nội dung

Mở thư mục [`demo/smart-home`](../../use-bdi-plugin/demo/smart-home/):

- `SmartHome.use` và `SmartHome.cmd`: UML/OCL cho `Home`, `Resident`, `Light`
  với resident buổi tối và đèn đang tắt.
- `resident.asl`: goal `prepare_evening`, plan và action `turn_on_lights`.
- `smart-home.jcm` và `smart-home-organization.xml`: project JaCaMo/Moise tĩnh.
- `smart-home-organization.use` và `smart-home-organization.cmd`: snapshot
  UML/OCL độc lập cho organization layer.
- `SmartHome.bdimap.json`: mapping Resident agent và decision operation.

### Kịch bản UI

1. Mở `SmartHome.use`, chạy `SmartHome.cmd`, sau đó tạo `Class diagram` và
   `Object diagram` để show `home1`, `resident1`, `livingRoomLight`.
2. Chọn `Plugins > AgentSpeak > Import JaCaMo Project...`, mở
   `smart-home.jcm`, rồi mở rộng `resident.asl`.
3. Trong `Mapping`, load `SmartHome.bdimap.json`, bấm `Refresh USE Snapshot`
   rồi mở `Diagram`. Dùng `MAS Overview` để tách BDI/Environment và dùng
   `BDI Plan` để show `prepare_evening -> turn_on_lights`.

Ví dụ này phù hợp để giải thích decision-making: goal/plan/action nằm ở BDI,
còn Home/Resident/Light và trạng thái snapshot nằm ở USE. Các belief/context
mapping nâng cao được trình bày trong Auction và Smart Queue.

## 8. Headless validation

### Auction static JaCaMo

Chạy script baseline từ repository root. Script tự dùng đúng dependencies và
tạo JSON/HTML dưới `use-bdi-plugin\target\case-study\auction`:

```powershell
powershell -ExecutionPolicy Bypass `
    -File .\use-bdi-plugin\scripts\auction-baseline.ps1
$LASTEXITCODE
```

Exit `0` và marker `AUCTION_BASELINE_REPORT_OK` xác nhận report đã được tạo.
Đọc JSON/HTML thay vì suy diễn mọi finding là lỗi parser.

### USE snapshot command file

To check the Smart Queue command file without the BDI plugin UI:

```powershell
.\run-use-gui.ps1 -Demo smart-queue -Headless
$LASTEXITCODE
```

USE returns `0` when the command file finishes with valid constraints and `1`
when a constraint fails. This command is a validation check, not the visual
BDI demo.

## 9. Quality gates before presentation

```powershell
mvn --batch-mode --no-transfer-progress -pl use-bdi-plugin -am test
mvn --batch-mode --no-transfer-progress -pl use-assembly -am package
powershell -ExecutionPolicy Bypass -File .\use-bdi-plugin\scripts\smoke.ps1
powershell -ExecutionPolicy Bypass -File .\use-bdi-plugin\scripts\auction-evidence.ps1
```

Expected script markers are `GUI_SMOKE_OK` and `AUCTION_EVIDENCE_OK`. For the
complete repository workflow, use
[`docs/project/USER_GUIDE.md`](../project/USER_GUIDE.md),
[`docs/project/DEVELOPER_GUIDE.md`](../project/DEVELOPER_GUIDE.md) and the
canonical [technical design](../project/10_PLUGIN_TECHNICAL_DESIGN.md).

## 10. Troubleshooting

- **AgentSpeak menu is missing:** close USE, verify
  `lib\plugins\use-bdi-plugin-7.1.1.jar`, then relaunch with the same `-H`.
- **Dialog opens in another folder:** use a plugin-owned `Import .asl...`,
  `Import .jcm...`, `Load...`, `Save...` or `Export Current Analysis...`
  dialog. The preferred repository root is only selected when that directory
  exists.
- **Java class/version error:** check `$env:JAVA_HOME` and invoke its
  `bin\java.exe` explicitly; do not rely on a stale `java` in `PATH`.
- **Unexpected finding:** inspect rule ID, certainty, source span and evidence
  in `Problems`. `UNKNOWN` and unsupported-resource diagnostics are intentional
  results, not silently converted to PASS.
- **Generated report appears in Git:** delete only the generated
  `target\demo-reports` directory before committing; demo source files should
  remain under `use-bdi-plugin\demo`.
