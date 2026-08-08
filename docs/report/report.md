# Báo Cáo Tiến Độ Phát Triển USE BDI Plugin

* **Đề tài:** Nghiên cứu mở rộng công cụ USE hỗ trợ đặc tả và kiểm chứng hệ thống đa tác tử 
* **Github:** [USE BDI Plugin](https://github.com/nguyentrungnghia1802/USE_BDI_Plugin)
* **Đơn vị:** VNU SME Lab
* **Giảng viên hướng dẫn:** PGS.TS. Đặng Đức Hạnh

## 1. Mục tiêu hiện tại

USE BDI Plugin được phát triển dưới dạng phần mở rộng cho **USE (UML-based Specification Environment)** nhằm hỗ trợ làm việc đồng thời với mô hình tác tử BDI và mô hình UML/OCL.

Trong giai đoạn hiện tại, plugin tập trung vào các chức năng nền tảng:

* đọc mã nguồn AgentSpeak (`.asl`) bằng parser của Jason;
* chuyển kết quả phân tích sang mô hình BDI trung gian;
* hiển thị cấu trúc BDI trực tiếp trong USE;
* đọc mô hình và trạng thái UML hiện tại của USE;
* thiết lập ánh xạ giữa một số thành phần BDI và UML;
* thực hiện các kiểm tra bước đầu về tính nhất quán giữa hai phía;
* hiển thị các lỗi và cảnh báo phát hiện được cho người dùng.

Mục tiêu dài hơn của phần phát triển này là từng bước hỗ trợ kiểm tra không chỉ ở mức cấu trúc và tham chiếu mà còn ở mức ngữ nghĩa, dựa trên trạng thái UML/OCL.

# 2. Những nội dung đã thực hiện

## 2.1. Tích hợp plugin vào USE

Đã xây dựng module `use-bdi-plugin` và tích hợp vào cơ chế plugin hiện có của USE.

Plugin hiện có thể được truy cập từ giao diện USE và cung cấp giao diện riêng phục vụ việc import và quan sát mô hình BDI.

Việc phát triển được thực hiện theo hướng plugin độc lập, hạn chế sửa đổi trực tiếp vào phần lõi của USE.

## 2.2. Tích hợp Jason Parser và import AgentSpeak

Đã tích hợp parser của **Jason** để đọc file AgentSpeak (`.asl`).

Luồng xử lý hiện tại:

```text
AgentSpeak (.asl)
        ↓
   Jason Parser
        ↓
      AST
        ↓
Normalized BDI IR
```

Plugin hiện hỗ trợ:

* chọn và import file `.asl`;
* import nhiều file;
* phân tích cú pháp bằng Jason;
* nhận biết file AgentSpeak không hợp lệ;
* lưu thông tin vị trí nguồn để phục vụ việc báo lỗi và hiển thị sau này.

Việc sử dụng parser của Jason giúp plugin không phải xây dựng lại parser AgentSpeak riêng.

## 2.3. Xây dựng mô hình BDI trung gian

Kết quả từ Jason không được sử dụng trực tiếp trong toàn bộ plugin mà được chuyển sang một mô hình trung gian (**Normalized BDI Intermediate Representation – BDI IR**).

Một số thành phần hiện đã được biểu diễn gồm:

* Agent;
* Belief;
* Goal;
* Plan;
* Trigger;
* Context;
* Plan Step;
* Term;
* Source Span.

Việc sử dụng BDI IR giúp tách phần phụ thuộc vào Jason khỏi phần mapping, kiểm tra tính nhất quán và giao diện của plugin.

## 2.4. Xây dựng BDI Index

Trên mô hình BDI trung gian, plugin đã xây dựng cơ chế lập chỉ mục để hỗ trợ việc truy xuất và kiểm tra.

Hiện tại đã có các thông tin phục vụ tra cứu như:

* Goal và các Plan liên quan;
* Action và vị trí gọi;
* Predicate occurrence;
* các reference được phát hiện trong AgentSpeak;
* Plan label.

BDI Index hiện được sử dụng làm dữ liệu đầu vào cho một số bước mapping và consistency checking.

## 2.5. Xây dựng BDI Explorer

Đã xây dựng giao diện **BDI Explorer** bên trong USE.

Sau khi import AgentSpeak, người dùng có thể quan sát cấu trúc của Agent dưới dạng cây, bao gồm các thành phần như:

```text
Agent
├── Beliefs
├── Goals
└── Plans
    ├── Trigger
    ├── Context
    └── Plan Steps
```

Quá trình import được thực hiện ở background để tránh làm treo giao diện chính của USE.

Ngoài phần Explorer, plugin có tab **Problems** để hiển thị các vấn đề được phát hiện trong quá trình phân tích.

## 2.6. Đọc mô hình và trạng thái UML từ USE

Đã xây dựng lớp adapter để lấy thông tin từ model và state đang mở trong USE.

Các thông tin được sử dụng hiện gồm:

* UML Classes;
* Attributes;
* Operations;
* Associations;
* Objects trong current state;
* một số thông tin liên quan đến operation và OCL.

Các thông tin này được chuyển sang cấu trúc dữ liệu riêng của plugin trước khi sử dụng cho mapping và validation.

Luồng tổng quát hiện tại:

```text
AgentSpeak
    ↓
BDI IR
    ↓
BDI Index
    ↓
Mapping / Consistency Checking
    ↑
USE Model + Current State
```

## 2.7. Xây dựng bước đầu cơ chế Mapping

Plugin đã có nền tảng để thiết lập ánh xạ giữa mô hình BDI và mô hình UML.

Các loại mapping đang được hỗ trợ hoặc sử dụng trong quá trình phát triển gồm:

* Agent → UML Class/Object;
* Belief → UML Attribute;
* Action → UML Operation;
* Action Argument → Operation Parameter.

Plugin đã có giao diện phục vụ xem và xác nhận mapping, đồng thời có cơ chế lưu mapping để sử dụng lại.

Cơ chế gợi ý mapping đã được xây dựng bước đầu và sẽ tiếp tục được điều chỉnh để giảm số lượng mapping phải khai báo thủ công.

## 2.8. Xây dựng Consistency Engine ở mức ban đầu

Đã xây dựng kiến trúc cho **Consistency Engine**, trong đó các phép kiểm tra được tổ chức thành các rule và thực thi thông qua một luồng validation chung.

Một số nhóm kiểm tra đã được triển khai gồm:

* lỗi parse AgentSpeak;
* một số kiểm tra cấu trúc BDI;
* kiểm tra reference;
* kiểm tra mapping;
* kiểm tra chữ ký Action/Operation;
* một số kiểm tra liên quan đến ownership.

Kết quả kiểm tra được biểu diễn dưới dạng `ConsistencyIssue` và hiển thị trong tab **Problems**.

Ở thời điểm hiện tại, phần hoạt động rõ ràng nhất vẫn là các kiểm tra ở mức **cấu trúc, tham chiếu, mapping và signature**. Các kiểm tra ngữ nghĩa dựa trên OCL và trạng thái hệ thống vẫn cần tiếp tục hoàn thiện.

# 3. Kết quả demo hiện tại

Plugin hiện đã được chạy thử với **Smart Queue Case Study**.

Mô hình UML của case study có các thành phần như:

* `Manager`;
* `Counter`;
* `Staff`;
* `Queue`;
* `Customer`.

Một AgentSpeak fixture của Smart Queue đã được sử dụng để kiểm tra quá trình import.

Plugin có thể:

1. import file `.asl`;
2. parse AgentSpeak bằng Jason;
3. chuyển sang BDI IR;
4. hiển thị Belief, Goal và Plan trong BDI Explorer;
5. đọc UML model/state hiện tại;
6. chạy các kiểm tra đã được triển khai;
7. hiển thị kết quả trong Problems.

Ví dụ fixture `Smart_manager_agent.asl` hiện được sử dụng để kiểm tra luồng trên.

## 3.1. Kết quả trên BDI Explorer

Sau khi import, plugin hiển thị được cấu trúc Agent dưới dạng Tree View.

Với fixture Smart Queue hiện tại, parser nhận được các thành phần Belief, Goal và Plan và đưa chúng vào mô hình trung gian để hiển thị.

![Hình 1 – BDI Explorer sau khi import Smart_manager_agent.asl](images/demo_bdi_explorer.png)

*Hình 1. Kết quả import và hiển thị cấu trúc BDI của Smart Queue Agent.*

## 3.2. Kết quả trên Problems

Tab **Problems** hiển thị các vấn đề mà Consistency Engine phát hiện dựa trên AgentSpeak, mapping và trạng thái USE hiện tại.

Ví dụ:

### REF-001 – Named object reference absent

Rule kiểm tra một object reference được nhận diện từ AgentSpeak có tồn tại trong current state của USE hay không.

Nếu AgentSpeak tham chiếu tới một tên như:

```text
queue1
```

nhưng current USE state chưa có object tương ứng, plugin sẽ tạo một `ConsistencyIssue`.

### Mapping-related issues

Nếu một thành phần BDI chưa có mapping cần thiết sang UML, plugin có thể sinh cảnh báo tương ứng để người dùng kiểm tra hoặc xác nhận mapping.

![Hình 2 – Problems View](images/demo_bdi_problems.png)

*Hình 2. Các vấn đề được Consistency Engine phát hiện trong quá trình kiểm tra Smart Queue Agent.*

Các thông báo này là kết quả của quá trình kiểm tra tính nhất quán trên dữ liệu hiện tại. Tuy nhiên, một số rule vẫn đang được điều chỉnh để tránh trường hợp nhận diện sai và sinh cảnh báo không cần thiết.

# 4. Ví dụ logic kiểm tra hiện tại

Một ví dụ là rule **REF-001**, dùng để kiểm tra object reference trong AgentSpeak với các object đang tồn tại trong current USE state.

Logic chính có dạng:

```java
Set<String> objectNames = context.uml().orElseThrow().objects().stream()
        .map(UmlObjectRef::reference)
        .collect(Collectors.toSet());

allReferences(context.index().objectReferencesByName().values()).stream()
        .filter(reference -> !objectNames.contains(reference.name()))
        .forEach(reference -> issues.add(issue(
                "REF-001",
                IssueSeverity.ERROR,
                "Named object reference '" + reference.name()
                        + "' is absent from the current USE state",
                reference.sourceSpan()
        )));
```

Ở bước này, rule thực hiện một phép kiểm tra tương đối trực tiếp:

```text
Object Reference trong BDI
            ↓
     BDI Reference Index
            ↓
Danh sách Object trong USE State
            ↓
      Có tồn tại hay không?
```

Nếu không tìm thấy object tương ứng, một issue được tạo và đưa ra Problems View.

Đây là một trong các kiểm tra nền tảng hiện tại. Các kiểm tra có mức ngữ nghĩa cao hơn, đặc biệt liên quan tới Context, OCL và ảnh hưởng của Action lên trạng thái hệ thống, vẫn đang được tiếp tục phát triển.

# 5. Vấn đề hiện tại

## 5.1. Phân loại Object Reference chưa chính xác hoàn toàn

Vấn đề rõ nhất hiện tại nằm ở rule **REF-001**.

`BdiIndexBuilder` hiện vẫn có trường hợp nhận diện quá rộng các argument xuất hiện trong literal.

Điều này có thể khiến một số giá trị như:

```text
free
busy
```

hoặc thành phần xuất hiện trong Context bị xem như tên object.

Kết quả là REF-001 có thể sinh false positive.

Vấn đề này nằm ở bước phân loại reference trong BDI Index, không phải ở cơ chế hiển thị Problems.

## 5.2. Các kiểm tra ngữ nghĩa chưa hoàn chỉnh

Consistency Engine hiện đã có nền tảng cho việc tổ chức và thực thi rule, nhưng phần lớn các chức năng đã chạy ổn định vẫn tập trung vào:

* cấu trúc;
* reference;
* mapping;
* signature.

Phần kiểm tra dựa trên OCL và current UML state đã có một số thành phần nền tảng, nhưng chưa xem là hoàn chỉnh ở thời điểm báo cáo.

Do đó, giai đoạn tiếp theo cần tiếp tục phát triển phần này thay vì chỉ bổ sung thêm các rule kiểm tra đơn giản.

## 5.3. Case study hiện còn ở mức phục vụ phát triển

Smart Queue hiện được sử dụng chủ yếu để kiểm thử quá trình tích hợp và demo các chức năng đang có.

Chưa thực hiện đầy đủ:

* bộ dữ liệu lỗi có chủ đích;
* ground truth;
* thống kê khả năng phát hiện lỗi;
* benchmark;
* đánh giá định lượng.

Các nội dung này sẽ cần được thực hiện ở giai đoạn thực nghiệm của khóa luận.

# 6. Kế hoạch tuần tới

## 6.1. Sửa vấn đề REF-001

Ưu tiên trước mắt là điều chỉnh cách `BdiIndexBuilder` phân loại reference.

Cần phân biệt tốt hơn giữa:

* Object Reference;
* Literal Value;
* Enum-like Value;
* Variable;
* Context Expression.

Mục tiêu là giảm các false positive hiện tại của REF-001.

## 6.2. Tiếp tục hoàn thiện Mapping

Tiếp tục kiểm tra và cải thiện cơ chế gợi ý mapping giữa BDI và UML.

Tập trung vào:

* Agent → UML Class/Object;
* Belief → UML Attribute;
* Action → UML Operation;
* Action Argument → Operation Parameter.

Việc cải thiện auto-mapping sẽ được thực hiện dựa trên các thông tin đang có như tên, signature và arity, đồng thời vẫn giữ khả năng để người dùng xác nhận hoặc chỉnh sửa mapping.

## 6.3. Bắt đầu phát triển thêm các kiểm tra ngữ nghĩa

Sau khi phần reference ổn định hơn, tiếp tục nghiên cứu và triển khai các phân tích dựa trên mô hình BDI thay vì chỉ bổ sung các phép kiểm tra tồn tại đơn giản.

Trước mắt xem xét:

* quan hệ giữa Goal và supporting Plan;
* dependency giữa các Goal/Plan;
* Context của Plan;
* quan hệ giao tiếp giữa các Agent;
* việc sử dụng UML/OCL state trong quá trình đánh giá.

Phần này sẽ được triển khai từng bước và kiểm thử trên các case study cụ thể.

## 6.4. Nghiên cứu Visualization cho mô hình BDI

Khảo sát khả năng bổ sung biểu diễn đồ họa cho một số quan hệ hiện chỉ được hiển thị bằng Tree View.

Trước mắt xem xét:

* Goal–Plan Graph;
* Plan Dependency Graph;
* quan hệ giữa các Agent.

Đây mới là nội dung nghiên cứu phương án, chưa đặt mục tiêu phải hoàn thành toàn bộ visualization trong tuần tới.

## 6.5. Chuẩn bị Case Study tiếp theo

Tiếp tục sử dụng Smart Queue để kiểm tra các chức năng đang phát triển.

Đồng thời bắt đầu chuẩn bị **Auction Case Study** làm case study có cấu trúc rõ hơn cho việc đánh giá:

* Goal/Plan;
* nhiều Agent;
* Action;
* communication;
* UML/OCL constraints.

Phần đánh giá định lượng và mutant suite sẽ thực hiện ở giai đoạn sau khi các chức năng kiểm tra cần thiết đã ổn định.

## 6.6. Bắt đầu viết nội dung khóa luận

Song song với việc tiếp tục phát triển plugin, bắt đầu viết các phần của khóa luận đã có đủ cơ sở.

Trước mắt tập trung vào:

1. bối cảnh và bài toán nghiên cứu;
2. tổng quan về hệ thống đa tác tử;
3. kiến trúc BDI;
4. AgentSpeak và Jason;
5. tổng quan JaCaMo ở mức liên quan đến đề tài;
6. UML/OCL và công cụ USE;
7. bài toán liên kết mô hình BDI với UML/OCL;
8. kiến trúc tổng thể của USE BDI Plugin;
9. thiết kế Normalized BDI IR.

Các phần liên quan đến kết quả thực nghiệm, đánh giá hiệu quả và kết luận sẽ được viết sau khi implementation và case study đạt trạng thái ổn định hơn.

# 7. Tổng kết

Trong giai đoạn vừa qua, công việc chủ yếu tập trung xây dựng nền tảng kỹ thuật cho USE BDI Plugin.

Các thành phần chính đã có gồm:

```text
AgentSpeak Import
        ↓
Jason Parser
        ↓
Normalized BDI IR
        ↓
BDI Index
        ↓
Mapping
        ↓
Consistency Checking
        ↓
BDI Explorer / Problems
```

Plugin hiện đã có thể chạy trong USE, import AgentSpeak, hiển thị mô hình BDI, đọc mô hình/trạng thái UML và thực hiện một số kiểm tra cấu trúc, reference, mapping và signature.

Tuy nhiên, hệ thống hiện vẫn đang trong giai đoạn phát triển. Các phần kiểm tra ngữ nghĩa dựa trên Goal/Plan, communication và OCL chưa được xem là hoàn chỉnh và sẽ là trọng tâm của các giai đoạn tiếp theo.

Trong tuần tới, công việc tập trung vào sửa vấn đề phân loại reference, tiếp tục cải thiện mapping, bắt đầu mở rộng các kiểm tra ngữ nghĩa, nghiên cứu visualization, chuẩn bị case study tiếp theo và bắt đầu viết các phần nền tảng của khóa luận.
