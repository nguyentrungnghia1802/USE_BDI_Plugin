# Demo Smart Queue: Agent ra quyết định điều phối hàng đợi

## 1. Mục tiêu

Ví dụ này minh họa một agent quản lý hàng đợi lựa chọn kế hoạch dựa trên trạng
thái nhận thức: hàng đợi đông hay không và có quầy rảnh hay không. Đây là demo
ngắn, trực quan để giải thích quan hệ:

```text
Goal -> Plan -> Context -> Action -> UML Operation
reduce_waiting_time -> assign_customer -> queue crowded/free counter
                    -> assignCustomer -> Manager::assignCustomer(...)
```

AgentSpeak chỉ được import và phân tích; plugin không thực thi plan hay tự sửa
snapshot USE.

## 2. Các file trong ví dụ

| File | Nội dung và vai trò |
| --- | --- |
| `SmartQueue.use` | UML/OCL với `Queue`, `Customer`, `Counter`, `Manager`, `Staff`; operation điều phối và invariant `SizeMatchesCustomers` |
| `SmartQueue.cmd` | Tạo `queue1`, 6 customer, 2 counter (`counter2` rảnh), `manager1`, các link rồi chạy `check` |
| `smart_queue_manager.asl` | Belief về queue/counter/customer; goal `reduce_waiting_time`; ba nhánh plan và action ngoài `assignCustomer` |
| `SmartQueue.bdimap.json` | Mapping agent sang class `Manager` và action `assignCustomer` sang operation UML cùng ba tham số |
| `README.md` | Kịch bản demo hiện tại |

## 3. Mở đúng model và snapshot

Sau bước chuẩn bị trong [README bộ demo](../README.md), chạy từ repository root:

```powershell
.\run-use-gui.ps1 -Demo smart-queue
```

Nếu USE đã mở, chọn `File > Open specification...` và mở `SmartQueue.use` trước.
Chỉ khi model đã hiện trong cây bên trái mới gõ vào shell dưới cùng:

```text
open "D:\_CODE_BANK\Project_\vnu-sme-lab\use\use-bdi-plugin\demo\smart-queue\SmartQueue.cmd"
```

Đợi log báo command file hoàn tất trước khi tạo Object diagram.

## 4. Kịch bản 1 - flow demo nhanh (7-10 phút)

1. Chọn `View > Create View > Class diagram`.
   Chỉ `Queue`, `Customer`, `Counter`, `Manager`; mở node `Queue` để nói về
   `size` và invariant `SizeMatchesCustomers`.
2. Chọn `View > Create View > Object diagram`.
   Chỉ `queue1`, 6 customer, `counter1` busy, `counter2` free và `manager1`.
3. Chọn `Plugins > AgentSpeak > Import AgentSpeak...`, rồi chọn
   `smart_queue_manager.asl`.
4. Trong tab `Explorer`, bung goal `reduce_waiting_time` và ba plan:
   - queue đông + có quầy rảnh: chọn `assign_customer`;
   - queue đông + không có quầy rảnh: chọn `request_open_counter`;
   - queue không đông: không cần điều phối thêm.
5. Mở tab `Mapping`, bấm `Load...`, chọn `SmartQueue.bdimap.json`.
   Chỉ rõ mapping là binding đã xác nhận, không phải suggestion tự động.
6. Bấm `Refresh USE Snapshot` để đồng bộ analysis với snapshot đang hiển thị.
7. Mở `Diagram`, chọn `BDI Plan`, bấm `Fit`.
   Chọn goal hoặc plan rồi bấm `Focus Goal/Plan` để giữ nhánh chính.
8. Chỉ đường nối từ action `assignCustomer` tới
   `Manager::assignCustomer(q:Queue,c:Customer,counter:Counter)`.
9. Mở `Problems`; chọn một finding nếu có để highlight evidence trong
   `Diagram`. Giải thích `UNKNOWN` không phải PASS.
10. Chọn lại mode/layer/focus mong muốn và bấm `Export SVG...` nếu cần ảnh cho
    slide; `Reset` khôi phục toàn graph.

## 5. Kịch bản 2 - dựng Smart Queue thủ công từ GUI (15-20 phút)

Khởi động USE không kèm model theo [kịch bản GUI thủ công](../README.md#5-kịch-bản-2---dựng-toàn-bộ-thủ-công-từ-gui),
sau đó không mở `SmartQueue.cmd`.

### A. Mở model và tạo từng object

1. Chọn `File > Open specification...`, mở `SmartQueue.use`, rồi mở
   `View > Create View > Class diagram`.
2. Qua `State > Create object...`, tạo từng object sau:

   | Class | Object name |
   | --- | --- |
   | `Queue` | `queue1` |
   | `Customer` | `customer1` đến `customer6` |
   | `Counter` | `counter1`, `counter2` |
   | `Manager` | `manager1` |

3. Không cần tạo `Staff` trong snapshot này. Chọn
   `View > Create View > Object diagram`.

### B. Nhập từng giá trị

Nhấp đúp node, nhập giá trị OCL trong `Object properties`, bấm `Apply`:

| Object | Giá trị OCL cần nhập |
| --- | --- |
| `queue1` | `size = 6` |
| `customer1` ... `customer6` | `name = 'customer1'` ... `name = 'customer6'` |
| `counter1` | `name = 'counter1'`, `status = #busy` |
| `counter2` | `name = 'counter2'`, `status = #free` |
| `manager1` | `name = 'manager1'` |

### C. Nối từng quan hệ

1. Lần lượt giữ `Ctrl`, chọn `queue1` với từng `customer1` ... `customer6`,
   nhấp phải và chọn `insert (queue1,customerN) into QueueCustomers`.
2. Nối `queue1` với `counter1` và `counter2` bằng hai mục
   `insert (...) into QueueCounters`.
3. Nối `manager1` với `queue1` bằng
   `insert (manager1,queue1) into ManagerQueues`.
4. Chọn `State > Check structure now`. `SizeMatchesCustomers` phải hợp lệ vì
   `size = 6` và có đúng sáu link `QueueCustomers`.

### D. Import AgentSpeak và tạo mapping thủ công

1. Chọn `Plugins > AgentSpeak > Import AgentSpeak...`, mở
   `smart_queue_manager.asl`.
2. Trong `Explorer`, bung ba plan của goal `reduce_waiting_time` để đối chiếu
   với `queue1.size = 6` và `counter2.status = #free`.
3. Trong `Mapping > Suggestions`, áp dụng candidate agent sang `Manager`, sau
   đó candidate action `assignCustomer` sang
   `Manager::assignCustomer(q:Queue,c:Customer,counter:Counter)` bằng
   `Apply selected suggestion`.
4. Nếu cần nhập trực tiếp, chọn đúng `Kind`, chép `Source` đang hiển thị trong
   suggestion, nhập `Target`, rồi bấm `Add / update`.
5. Bấm `Refresh USE Snapshot`, chọn `Diagram > BDI Plan > Fit`; mở `Problems`
   và đọc certainty/evidence. Không bấm `Load...` `SmartQueue.bdimap.json` vì
   file đó chỉ dùng để đối chiếu mapping vừa tạo.

## 6. Lời thoại gợi ý

- "UML/OCL mô tả cấu trúc và ràng buộc của hệ thống hàng đợi."
- "AgentSpeak mô tả goal, context và cách agent lựa chọn plan."
- "Mapping nối action BDI với operation UML, nhưng chỉ mapping được người dùng
  xác nhận mới được dùng để kiểm tra nhất quán."
- "Plugin đọc snapshot USE; nó không chạy agent và không thay đổi `MSystem`."

## 7. Kết quả cần quan sát

- `queue1.size = 6` và có đúng 6 link `QueueCustomers`, nên
  `SizeMatchesCustomers` hợp lệ.
- Context của plan nhận biết queue đông (`N > 5`) và có `counter2` free.
- `Diagram` có goal, supporting plan, context, ordered steps, action và mapping
  tới operation UML.
- Sau khi đổi state thủ công, phải bấm `Refresh USE Snapshot`; điều này thể hiện
  boundary read-only và refresh có chủ đích.

## 8. Nếu demo không đúng

- Object diagram trống: chạy lại `.\run-use-gui.ps1 -Demo smart-queue`; nếu làm
  thủ công, phải mở `SmartQueue.use` trước lệnh `open "...cmd"`.
- Không thấy menu AgentSpeak: đóng USE và chạy `.\run-use-gui.ps1 -Rebuild`.
- Mapping không hiện: phải import `.asl` trước rồi mới `Load...` mapping.
- Graph quá rộng: chọn `BDI Plan`, bấm `Fit`, sau đó `Focus Goal/Plan`.
