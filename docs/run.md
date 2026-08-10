# Hướng Dẫn Chạy Dự Án USE & USE BDI Plugin

Tài liệu này tổng hợp các câu lệnh cơ bản để biên dịch, đóng gói và chạy dự án **USE** cùng **USE BDI Plugin**.

---

## 1. Chạy UI Dự Án (Chạy Giao Diện Đồ Họa - GUI)

Các câu lệnh dưới đây dùng để khởi chạy giao diện GUI của dự án USE kèm theo BDI Plugin.

### Cách 1: Chạy nhanh qua Script (Khuyên dùng trên Windows)
- **Windows Command Prompt (CMD):**
  ```cmd
  run-use-gui.cmd
  ```
- **PowerShell:**
  ```powershell
  .\run-use-gui.ps1
  ```

### Cách 2: Chạy bằng lệnh Java JAR trực tiếp
- **Chạy trực tiếp module `use-gui` (sau khi build):**
  ```powershell
  java -jar use-gui/target/use-gui.jar -nr
  ```
  *Hoặc chỉ định đường dẫn `JAVA_HOME`:*
  ```cmd
  "%JAVA_HOME%\bin\java.exe" -jar use-gui\target\use-gui.jar -nr
  ```

- **Chạy từ bản phân phối đóng gói (`use-assembly`):**
  - PowerShell:
    ```powershell
    $useHome = (Resolve-Path .\use-assembly\target\use-7.1.1).Path
    java -jar (Join-Path $useHome 'lib\use-gui.jar') -nr "-H=$useHome"
    ```
  - CMD:
    ```cmd
    java -jar "use-assembly\target\use-7.1.1\lib\use-gui.jar" -nr -H="use-assembly\target\use-7.1.1"
    ```

### Cách 3: Chạy script kiểm thử Smoke GUI
Tự động kích hoạt GUI và kiểm tra load Plugin:
```powershell
powershell -ExecutionPolicy Bypass -File .\use-bdi-plugin\scripts\smoke.ps1
```

---

## 2. Biên Dịch & Đóng Gói (Build & Package)

- **Đóng gói riêng `use-bdi-plugin` (chỉ plugin BDI):**
  ```bash
  mvn clean package -pl use-bdi-plugin -am
  ```

- **Đóng gói toàn bộ hệ thống (bao gồm GUI & Shaded Plugin JAR trong `use-assembly`):**
  ```bash
  mvn clean package -pl use-assembly -am
  ```
  *(File zip phân phối sẽ được tạo tại `use-assembly/target/use-7.1.1.zip`)*

- **Biên dịch và kiểm tra toàn bộ dự án (Full Build & Verify Gate):**
  ```bash
  mvn clean verify
  ```

---

## 3. Chạy Kiểm Thử & Script Quality Gates

- **Chạy Unit Test cho module `use-bdi-plugin`:**
  ```bash
  mvn test -pl use-bdi-plugin -am
  ```

- **Chạy Headless Quality Gate (Kiểm tra BDI dạng dòng lệnh):**
  ```powershell
  powershell -ExecutionPolicy Bypass -File .\use-bdi-plugin\scripts\headless-quality-gate.ps1
  ```

- **Chạy kịch bản Auction Evidence (Tạo báo cáo kiểm thử quy trình Auction):**
  ```powershell
  powershell -ExecutionPolicy Bypass -File .\use-bdi-plugin\scripts\auction-evidence.ps1
  ```

- **Kiểm tra Clean Clone (Đảm bảo mã nguồn build sạch từ git repository):**
  ```powershell
  powershell -ExecutionPolicy Bypass -File .\use-bdi-plugin\scripts\clean-clone.ps1
  ```

---

## 4. Chạy BDI Quality Gate Trực Tiếp Qua CLI Java

Dùng để phân tích mô hình BDI AgentSpeak và OCL/UML không qua GUI:

```powershell
$cp = "use-bdi-plugin\target\use-bdi-plugin-7.1.1.jar;use-gui\target\use-gui.jar"
java -cp $cp org.tzi.use.plugins.bdi.cli.BdiQualityGateMain `
  --use .\Auction.use `
  --asl .\auctioneer.asl --asl .\bidder.asl `
  --mapping .\Auction.bdimap.json `
  --rules .\.bdi-plugin\rules.json `
  --suppressions .\.bdi-plugin\suppressions.json `
  --json .\analysis.json --html .\analysis.html
```
