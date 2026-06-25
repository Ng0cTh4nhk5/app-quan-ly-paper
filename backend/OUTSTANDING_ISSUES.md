# Báo cáo Các Vấn Đề Còn Tồn Đọng (Outstanding Issues) - Phase 1

**Dự án:** App Quản Lý Paper (RGMS)
**Người lập báo cáo:** Thư ký Hệ thống

Tài liệu này liệt kê các vấn đề kỹ thuật và kiến trúc còn tồn đọng sau Phase 1 và vị trí chính xác của chúng trong mã nguồn.

## 1. Xung Đột "Dual-Entity" (Trùng lặp cấu trúc dữ liệu)
**Mô tả:** Hệ thống hiện đang định nghĩa 2 class Entity `NguoiDung` khác nhau nhưng cùng map chung vào bảng `nguoi_dung` trong CSDL, gây ra rủi ro lỗi `MappingException` của Hibernate.
**Vị trí chính xác:**
- `app/backend/src/main/java/com/rgms/modules/detai/entity/NguoiDung.java` (Sử dụng ID kiểu Long)
- `app/backend/src/main/java/com/rgms/modules/nguoidung/entity/NguoiDung.java` (Sử dụng ID kiểu UUID)

## 2. Package Chứa Code Cũ (Code rác/Legacy)
**Mô tả:** Tồn tại các interface repository cũ sử dụng UUID đang nằm song song với các repository mới (sử dụng Long). Điều này gây ra lỗi `NoUniqueBeanDefinitionException` khi Spring Boot quét các bean.
**Vị trí chính xác:**
- Toàn bộ các file nằm trong package: `app/backend/src/main/java/com/rgms/modules/detai/repository/` (Ví dụ: `DeTaiRepository.java` bản dùng UUID).

## 3. Thiếu Unit Test cho FSM Guard
**Mô tả:** Các điều kiện bảo vệ (Guards) kiểm tra tính hợp lệ trước khi chuyển đổi trạng thái đề tài hiện chưa được viết Unit Test để đảm bảo tính chính xác của luồng nghiệp vụ.
**Vị trí chính xác:**
- Các logic kiểm tra trong file: `app/backend/src/main/java/com/rgms/modules/detai/service/FsmService.java`
