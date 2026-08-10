### Project: Automated Parking System

(Kiến trúc SOLID & Multi-scale Design)

### 1. System Overview & Scalability

Hệ thống được thiết kế theo kiến trúc mô-đun, tuân thủ các nguyên lý SOLID và tư duy trừu tượng hóa. Mục tiêu thiết kế là cho phép tùy biến linh hoạt theo quy mô vận hành thực tế của từng đơn vị thông qua cơ chế Onboarding cấu hình ban đầu.

- Small-scale Model (Bãi xe lẻ, cửa hàng, chung cư mini).
- Medium & Large-scale Model (Tòa nhà văn phòng, chuỗi bãi đỗ, trung tâm thương mại).

### 2. Performance Optimization & Operational Practice

Tối ưu hiệu năng:

- `Map<String, Ticket>`: Quản lý tập vé đang hoạt động trong bộ nhớ, hỗ trợ tra cứu, check-out theo `TicketID` với độ phức tạp trung bình O(1).
- `Map<VehicleType, List<Integer>>` kết hợp `Map<Integer, Integer>`: Quản lý vùng hoạt động theo loại xe và số lượng slot theo từng tầng.
- Min-Heap theo từng zone, loại xe: Cấp phát slot bằng thao tác `pop` slot ưu tiên theo thứ tự tầng-vị trí; tái nạp bằng `push` khi slot được giải phóng, độ phức tạp O(log N).
- `ParkingSlot[][]` (grid 2D theo tầng, slot): Cập nhật trạng thái `occupied`, `release` theo tọa độ trực tiếp với độ phức tạp O(1), đồng thời rút ngắn thời gian truy xuất khi hiển thị ma trận bãi.
- File I/O nhẹ: `active_tickets.txt` và `history_parking.txt` lưu dữ liệu giao dịch theo định dạng pipe-delimited; `user_profile.properties` lưu cấu hình hệ thống.

Giá trị trong vận hành:

- Đối với bãi đỗ tự động: Có thể tích hợp luồng cấp slot vào lớp điều khiển ngoại vi (LED, Barrier, API tích hợp) ở giai đoạn mở rộng.
- Đối với bãi đỗ thủ công quy mô vừa, lớn: Hệ thống cấp phát vị trí theo quy tắc zone giúp nhân sự điều phối nhanh, giảm thời gian tìm chỗ trống.

### 3. Task Allocation & Project Deliverables

- Task 1: Interfaces & Abstraction Layer.
  - Hoàn thành tập interface lõi cho thời gian, vé, định giá, lịch sử, báo cáo và cấu hình người dùng.
- Task 2: Domain Model & Factory Layer.
  - Hoàn thành mô hình miền (`Ticket`, `UserProfile`, `VehicleType`, `ParkingSlot`) và các factory chính.
- Task 3: Persistence & Report Engine.
  - Hoàn thành cơ chế lưu, đọc dữ liệu bằng `.txt`, `.properties`, xuất báo cáo định dạng `.csv`, và hiển thị báo cáo trên console.
- Task 4: Service & User Interface Layer.
  - Hoàn thành `ParkingManagerService`, điều phối menu và in ticket, receipt theo luồng check-in, check-out.

### 4. Project Directory Structure

```plaintext
Auto_Parking_Program/
├── .gitignore
├── README.md
├── database/
│   ├── active_tickets.txt
│   ├── history_parking.txt
│   └── user_profile.properties
└── src/
    └── com/
        └── autoparking/
            ├── MainApp.java
            ├── config/
            │   └── MultiLotConfig.java
            ├── core/
            │   └── interfaces/
            │       ├── IExcelTableBuilder.java
            │       ├── IHistoryManager.java
            │       ├── IPricingCalculator.java
            │       ├── IReportExporter.java
            │       ├── ITicketFactory.java
            │       ├── ITicketValidator.java
            │       ├── ITimeProvider.java
            │       └── IUserProfileProvider.java
            ├── factory/
            │   ├── TicketFactoryImpl.java
            │   └── UserProfileFactory.java
            ├── model/
            │   ├── ParkingSlot.java
            │   ├── Ticket.java
            │   ├── UserProfile.java
            │   └── VehicleType.java
            ├── service/
            │   ├── ParkingManagerService.java
            │   └── impl/
            │       ├── ExcelHistoryManagerImpl.java
            │       ├── ExcelReportExporterImpl.java
            │       ├── ExcelTableBuilderImpl.java
            │       ├── FlexiblePricingCalculatorImpl.java
            │       ├── RealTimeProviderImpl.java
            │       ├── TicketValidatorImpl.java
            │       └── UserProfileProviderImpl.java
            └── ui/
                ├── ClientViewMapper.java
                └── TicketConsolePrinter.java
```

Ghi chú runtime:

- File CSV được tạo khi xuất báo cáo: `database/active_tickets.csv`, `database/history_parking.csv`.

### 5. Detailed Component & File Responsibilities

### Root Package

- `MainApp.java` (`MainApp`): Điểm khởi chạy hệ thống; khởi tạo dependencies, thực hiện onboarding, load cấu hình, điều phối menu và các luồng nghiệp vụ chính.
- `config/MultiLotConfig.java` (`MultiLotConfig`): Tập hằng số cấu hình chung (đường dẫn dữ liệu, tham số mặc định số tầng, số slot mỗi tầng, đơn giá cơ sở).

### core.interfaces Package

- `ITimeProvider.java` (`ITimeProvider`): Cung cấp thời gian hệ thống theo thời gian thực cho vòng đời ticket.
- `ITicketValidator.java` (`ITicketValidator`): Kiểm tra định dạng hợp lệ của `TicketID`.
- `IPricingCalculator.java` (`IPricingCalculator`): Tính phí theo loại xe, thời gian vào, ra và mức giá cấu hình.
- `IHistoryManager.java` (`IHistoryManager`): Ghi, đọc dữ liệu vé đang hoạt động và lịch sử giao dịch.
- `IExcelTableBuilder.java` (`IExcelTableBuilder`): Khởi tạo cấu trúc file dữ liệu và header khi hệ thống chạy lần đầu.
- `IReportExporter.java` (`IReportExporter`): Xuất dữ liệu active, history ra CSV và hỗ trợ hiển thị báo cáo trên console.
- `ITicketFactory.java` (`ITicketFactory`): Chuẩn hóa cơ chế tạo ticket mới.
- `IUserProfileProvider.java` (`IUserProfileProvider`): Lưu, đọc cấu hình vận hành doanh nghiệp.

### factory Package

- `UserProfileFactory.java` (`UserProfileFactory`): Triển khai onboarding wizard, tạo profile mới, cập nhật profile hiện hữu và đồng bộ xuống storage.
- `TicketFactoryImpl.java` (`TicketFactoryImpl`): Sinh `TicketID`, khởi tạo ticket và gắn `timeIn` theo thời gian thực.

### model Package

- `VehicleType.java` (`VehicleType`): Định nghĩa các loại phương tiện và hệ số giá.
- `ParkingSlot.java` (`ParkingSlot`): Biểu diễn một ô đỗ trong grid 2D, quản lý trạng thái chiếm dụng, thông tin liên quan.
- `Ticket.java` (`Ticket`): Mô hình hóa vòng đời một lượt gửi xe từ check-in, đến check-out.
- `UserProfile.java` (`UserProfile`): Lưu cấu hình hệ thống theo doanh nghiệp (quy mô, slot theo tầng, zone, bảng giá, chính sách qua đêm).

### service Package

- `ParkingManagerService.java` (`ParkingManagerService`): Dịch vụ điều phối trung tâm.
  - Quản lý `activeTicketsMap` trong bộ nhớ.
  - Quản lý `ParkingSlot[][]` để cập nhật trạng thái slot theo tọa độ O(1).
  - Quản lý Min-Heap allocator theo từng zone loại xe để cấp phát slot hiệu quả.
  - Thực thi đầy đủ quy trình check-in, check-out.
  - Đồng bộ dữ liệu active ticket xuống storage và hiển thị ma trận bãi.

### service.impl Package

- `RealTimeProviderImpl.java` (`RealTimeProviderImpl`): Cài đặt `ITimeProvider` dựa trên thời gian hệ thống.
- `TicketValidatorImpl.java` (`TicketValidatorImpl`): Cài đặt kiểm tra regex cho `TicketID`.
- `FlexiblePricingCalculatorImpl.java` (`FlexiblePricingCalculatorImpl`): Cài đặt thuật toán tính phí theo block thời gian, bảng giá cấu hình và phụ phí qua đêm.
- `ExcelTableBuilderImpl.java` (`ExcelTableBuilderImpl`): Khởi tạo file dữ liệu nếu chưa tồn tại, bảo toàn dữ liệu cũ.
- `ExcelHistoryManagerImpl.java` (`ExcelHistoryManagerImpl`): Cài đặt ghi, đọc dữ liệu active và history theo định dạng text phân tách bằng `|`.
- `ExcelReportExporterImpl.java` (`ExcelReportExporterImpl`): Cài đặt xuất báo cáo active, history sang CSV và in ra console.
- `UserProfileProviderImpl.java` (`UserProfileProviderImpl`): Cài đặt lưu, đọc `UserProfile` bằng file `.properties`.

### ui Package

- `ClientViewMapper.java` (`ClientViewMapper`): Ánh xạ và hiển thị menu theo chế độ vận hành (small-scale hoặc large-scale).
- `TicketConsolePrinter.java` (`TicketConsolePrinter`): Định dạng và in thông tin check-in, check-out trên giao diện console.

### 6. System Execution Workflow

[ Onboarding Stage ]

MainApp -> UserProfileFactory.checkOrRunOnboarding
-> Load profile từ user_profile.properties (nếu đã tồn tại)
-> Hoặc chạy onboarding wizard và lưu profile mới
-> ClientViewMapper render menu theo cấu hình profile

[ Check-in Workflow ]

Người dùng chọn Check-in
-> ParkingManagerService.findAvailableSlot pop từ Min-Heap theo zone loại xe
-> TicketFactoryImpl + RealTimeProviderImpl tạo ticket mới (ticketId + timeIn)
-> TicketValidatorImpl xác thực ticketId
-> activeTicketsMap và ParkingSlot[][] cập nhật trạng thái occupied
-> ExcelHistoryManagerImpl.recordActiveTicket append vào active_tickets.txt
-> TicketConsolePrinter in phiếu check-in

[ Check-out Workflow ]

Người dùng chọn Check-out và nhập Ticket ID
-> ParkingManagerService tra cứu ticket trong activeTicketsMap
-> RealTimeProviderImpl lấy timeOut
-> FlexiblePricingCalculatorImpl tính tổng phí
-> ExcelHistoryManagerImpl.recordTransaction append vào history_parking.txt
-> ParkingManagerService release slot trên grid, push lại Min-Heap và rewrite active_tickets.txt
-> TicketConsolePrinter in biên nhận check-out

[ Report Workflow ]

Người dùng chọn chức năng report, matrix
-> displayMatrix hiển thị trạng thái slot theo tầng trên console
-> ExcelReportExporterImpl xuất active, history sang CSV hoặc in báo cáo console

### 7. Kết luận

Nhóm triển khai hệ thống theo đúng định hướng kiến trúc mô-đun và nguyên lý SOLID, đáp ứng được yêu cầu vận hành cho nhiều quy mô bãi xe. Phiên bản hiện tại đã hoàn thiện cơ chế cấp phát slot bằng Min-Heap kết hợp grid 2D để tăng tốc độ xử lý, đồng thời duy trì định dạng xuất báo cáo CSV phù hợp với nhu cầu theo dõi và tổng hợp dữ liệu trong thực tế.
