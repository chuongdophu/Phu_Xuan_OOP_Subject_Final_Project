# Hệ Thống Quản Lý Bãi Xe Tự Động (Console Java)

Danh sách thành viên nhóm:

Chương Do Phú: NBS2503ITA0032
Lê Kiều Khánh Linh: NBS2503ITA0038
Phan Thanh Tâm: NBS2603ITA0013
Lê Bá Nghi Truyền: NBS2503ITA0055

## 1. Yêu cầu trước khi chạy

- Hệ điều hành: Windows, macOS hoặc Linux
- Đã cài Java (khuyến nghị JDK 8 trở lên)
- Có Terminal/Command Prompt/PowerShell

Kiểm tra Java đã cài chưa:
java -version
javac -version

Nếu lệnh không nhận, hãy cài JDK và thêm Java vào biến môi trường `PATH`.

## 2. Tải và giải nén ứng dụng

1. Tải file `.zip` về máy.
2. Giải nén.
3. Mở thư mục đã giải nén, `Auto_Parking_Program`.

Quan trọng: luôn chạy lệnh tại thư mục gốc chứa các thư mục `src`, `bin`, `database`.

## 3. Cách chạy ứng dụng

Bạn có 2 cách chạy.

### Cách A - Chạy nhanh bằng file đã biên dịch sẵn (thư mục `bin`)

Nếu trong gói đã có sẵn class trong `bin`, chạy:

java -cp bin com.autoparking.MainApp

### Cách B - Tự biên dịch từ mã nguồn rồi chạy

Biên dịch toàn bộ source bằng danh sách trong `java_sources.txt`:

javac -d bin @java_sources.txt

Sau đó chạy:

java -cp bin com.autoparking.MainApp

## 4. Hướng dẫn sử dụng lần đầu

Khi chạy lần đầu, ứng dụng sẽ vào bước cấu hình ban đầu (onboarding):

1. Nhập thông tin cấu hình bãi xe theo câu hỏi trên màn hình.
2. Hệ thống lưu cấu hình vào `database/user_profile.properties`.
3. Từ lần sau, ứng dụng tự đọc lại cấu hình này.

## 5. Các chức năng chính trong menu

- `1` - Check-in xe mới
  - Nhập biển số.
  - Chọn loại xe.
  - Hệ thống cấp chỗ đỗ và in phiếu gửi xe (Ticket ID).

- `2` - Check-out
  - Nhập `Ticket ID`.
  - Hệ thống tính tiền và in biên nhận.

- `3` - Xem danh sách xe đang gửi / xuất CSV
  - Xem trực tiếp trên màn hình.
  - Hoặc xuất file CSV.

- `4` - Xem lịch sử / xuất lịch sử (tùy chế độ cấu hình)

- `5` - Xem quy tắc zone (tùy chế độ cấu hình)

- `6` - Cập nhật lại cấu hình bãi xe

- `0` - Thoát ứng dụng

## 6. Dữ liệu được lưu ở đâu

Thư mục `database/` chứa dữ liệu vận hành:

- `active_tickets.txt`: Danh sách xe đang gửi.
- `history_parking.txt`: Lịch sử giao dịch check-in/check-out.
- `user_profile.properties`: Cấu hình hệ thống.

Khi xuất báo cáo, có thể phát sinh thêm:

- `database/active_tickets.csv`
- `database/history_parking.csv`

## 7. Khắc phục lỗi thường gặp

- Lỗi `'java' is not recognized...` hoặc `command not found`
  - Nguyên nhân: chưa cài Java hoặc chưa thêm `PATH`.

- Lỗi không tìm thấy class `com.autoparking.MainApp`
  - Kiểm tra đang đứng đúng thư mục gốc project.
  - Nếu cần, biên dịch lại bằng:

javac -d bin @java_sources.txt

- Lỗi không ghi được file trong `database/`
  - Kiểm tra quyền ghi file/thư mục.
  - Đóng các ứng dụng khác đang mở các file này.

---

Nếu bạn là người dùng mới, chỉ cần nhớ 2 lệnh quan trọng nhất:

javac -d bin @java_sources.txt
java -cp bin com.autoparking.MainApp
