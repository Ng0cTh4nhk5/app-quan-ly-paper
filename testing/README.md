# Tài liệu Kiểm thử RGMS

Tài liệu kiểm thử toàn diện cho các tính năng khắc phục lỗi và nâng cấp của hệ thống RGMS.

## Tổng quan

Thư mục này chứa tất cả các tài nguyên kiểm thử để xác thực việc triển khai khắc phục lỗi trong hệ thống RGMS:

- **RT-01:** API endpoint `can-actions` (các hành động khả dụng)
- **RT-02:** Lọc Kỳ NCKH (chỉ hiển thị các kỳ đang mở đối với Giảng viên - GV)
- **RT-03:** Rút đề tài (hủy đề tài)
- **A7-1:** Xử lý lỗi 403 Forbidden (truy cập trái phép)
- **B5-1:** Tính đầy đủ của đối tượng phản hồi `DeTaiDetailResponse`
- **RT-04:** Luồng phản hồi góp ý (Feedback flow)
- **A5-1:** Sử dụng component `EmptyState` (trạng thái trống)
- **C2-1:** Phân quyền cho Phản biện viên

## Các tệp tin trong thư mục này

### 1. `TEST_SCENARIOS.md`
Đặc tả chi tiết các kịch bản kiểm thử bao gồm cả 3 vai trò (GV - Giảng viên, NCKH - Phòng Nghiên cứu Khoa học, PB - Tổ Phản biện).

**Nội dung:**
- 27 kịch bản kiểm thử (test cases) chia thành 9 nhóm tính năng
- Mỗi kịch bản kiểm thử bao gồm:
  - Mã TC (TC ID), tiêu đề, vai trò, mức độ ưu tiên
  - Điều kiện tiên quyết (Preconditions)
  - Các bước thực hiện được đánh số
  - Kết quả mong đợi (Expected results)
  - Các API endpoint liên quan
  - Các tệp tin/component liên quan
- Bảng tóm tắt độ bao phủ kiểm thử

**Cách dùng:**
```bash
# Mở và thực hiện tuần tự các kịch bản kiểm thử
code TEST_SCENARIOS.md
```

### 2. `test-data.sql`
Script PostgreSQL để nạp dữ liệu thử nghiệm thực tế bằng tiếng Việt vào cơ sở dữ liệu.

**Dữ liệu tạo ra:**
- 5 người dùng thử nghiệm (2 GV, 1 NCKH, 2 PB)
- 2 Kỳ NCKH (1 đang mở, 1 đã đóng)
- 15 đề tài bao phủ tất cả các trạng thái (`trang_thai`)
- 2 hợp đồng cho các đề tài ở trạng thái `DANG_THUC_HIEN`
- 2 tổ phản biện kèm theo các thành viên
- 1 bản ghi phản hồi góp ý (feedback)

**Cách dùng:**
```bash
# Chạy trực tiếp từ client cơ sở dữ liệu
psql -U postgres -d rgms < test-data.sql

# Hoặc thông qua Docker
docker exec -i rgms-postgres psql -U postgres -d rgms < test-data.sql
```

**Xác minh dữ liệu đã nạp:**
```sql
SELECT trang_thai, COUNT(*) FROM de_tai GROUP BY trang_thai;
-- Kết quả hiển thị 15 đề tài ở các trạng thái khác nhau
```

### 3. `api-test.sh`
Script Bash tự động để kiểm thử tất cả các API endpoint.

**Tính năng:**
- Kết quả đầu ra được tô màu (xanh = đạt, đỏ = lỗi)
- Kiểm thử cho cả 3 vai trò (GV, NCKH, PB)
- Xác thực các tính năng RT-01, RT-02, RT-03, A7-1, B5-1
- Kiểm tra thời gian phản hồi
- Bảng tóm tắt kết quả kiểm thử (số lượng đạt/lỗi)

**Cách dùng:**
```bash
# Cấp quyền thực thi
chmod +x api-test.sh

# Chạy tất cả kiểm thử
./api-test.sh

# Kết quả mong đợi:
# ========================================
# 1. AUTHENTICATION
# ========================================
# TEST: Login as gv01
# ✓ PASS: GV01 login successful
# ...
# ========================================
# TEST SUMMARY
# ========================================
# Total Tests: 25
# Passed: 25
# Failed: 0
```

**Điều kiện tiên quyết:**
- Backend đang chạy tại `localhost:8080`
- Dữ liệu thử nghiệm đã được nạp
- Máy tính đã cài đặt `curl`

### 4. `MANUAL_TEST_CHECKLIST.md`
Danh sách kiểm tra (checklist) từng bước trên trình duyệt để xác thực UI/UX.

**Phạm vi bao phủ:**
- Luồng đăng nhập và xác thực
- Danh sách đề tài & các component trạng thái trống (EmptyState)
- Trang chi tiết đề tài (B5-1)
- Các nút hành động & trạng thái khả dụng (RT-01)
- Luồng rút đề tài (RT-03)
- Lọc kỳ NCKH (RT-02)
- Xử lý lỗi 403/404 (A7-1)
- Chất lượng UI/UX (màu sắc trạng thái, nút nhấn, thông báo nhanh - toast)
- Kiểm tra bảng điều khiển Console & mạng Network
- Hỗ trợ tiếp cận - Accessibility (tùy chọn)

**Cách dùng:**
```bash
# Mở bảng kiểm tra
code MANUAL_TEST_CHECKLIST.md

# Hoặc dùng như một danh sách kiểm tra vật lý (in ra PDF)
# Đánh dấu tick vào các mục sau khi đã kiểm tra xong
```

## Hướng dẫn Bắt đầu Nhanh

### Bước 1: Thiết lập Môi trường

```bash
# Khởi động Backend
cd app/backend
./mvnw spring-boot:run

# Khởi động Frontend (ở một terminal khác)
cd app/frontend
npm run dev
```

### Bước 2: Nạp Dữ liệu Thử nghiệm

```bash
cd app/testing
psql -U postgres -d rgms < test-data.sql
```

Xác minh các tài khoản hoạt động bình thường:
- **GV:** `gv01` / `test123`
- **NCKH:** `nckh01` / `test123`
- **PB:** `pb01` / `test123`

### Bước 3: Chạy Kiểm thử API Tự động

```bash
chmod +x api-test.sh
./api-test.sh
```

Xem kết quả. Tất cả các bài kiểm tra cần phải đạt (màu xanh).

### Bước 4: Kiểm thử Thủ công trên Trình duyệt

1. Mở tệp `MANUAL_TEST_CHECKLIST.md`
2. Thực hiện theo danh sách kiểm tra từng bước một
3. Đánh dấu tick vào các mục đã hoàn thành
4. Chụp ảnh màn hình cho các biến thể giao diện EmptyState
5. Ghi nhận các lỗi phát hiện được

### Bước 5: Xem lại Kịch bản Kiểm thử

Đối với bất kỳ bài kiểm tra nào bị lỗi, hãy tham khảo `TEST_SCENARIOS.md` để xem đặc tả chi tiết:
- Tìm kịch bản kiểm thử bằng Mã TC (ví dụ: TC-GV-002)
- Xem lại kết quả mong đợi
- Kiểm tra các API endpoint và các component liên quan
- Tiến hành debug tương ứng

## Tài khoản Thử nghiệm

Mật khẩu chung cho tất cả tài khoản: `test123`

| Tên đăng nhập | Vai trò | Họ và Tên | Ghi chú |
|---------------|---------|-----------|---------|
| gv01 | GIANG_VIEN | Nguyễn Văn Anh | Sở hữu đề tài 1, 2, 3, 5, 7, 10, 12, 14, 15 |
| gv02 | GIANG_VIEN | Trần Thị Bình | Sở hữu đề tài 4, 6, 8, 9, 11, 13 |
| nckh01 | NCKH | Lê Minh Châu | Có quyền truy cập tất cả đề tài |
| pb01 | TO_PHAN_BIEN | Phạm Đức Dũng | Được phân công phản biện đề tài 3, 4 |
| pb02 | TO_PHAN_BIEN | Hoàng Thị Oanh | Được phân công phản biện đề tài 3, 4 |

## Tóm tắt Dữ liệu Thử nghiệm

### Số lượng Đề tài theo Trạng thái

| Trạng thái | Số lượng | ID Đề tài |
|------------|----------|-----------|
| DRAFT | 1 | 12 |
| CHO_PNCKH_XEM_XET | 2 | 1, 11 |
| DANG_XEM_XET_BOI_PNCKH | 1 | 15 |
| CHO_BO_SUNG_HO_SO | 1 | 2 |
| DANG_PHAN_BIEN | 2 | 3, 4 |
| CHO_CHINH_SUA_THUYET_MINH | 1 | 5 |
| DANG_LAP_HOP_DONG | 1 | 6 |
| DANG_THUC_HIEN | 2 | 7, 8 |
| BI_TU_CHOI | 1 | 9 |
| DA_RUT | 1 | 10 |
| BI_TREO | 1 | 13 |
| DA_HUY | 1 | 14 |
| **TỔNG CỘNG** | **15** | |

### Các Đề tài Kiểm thử Trọng tâm

- **Đề tài 1:** CHO_PNCKH_XEM_XET (gv01) - Kiểm thử trạng thái và nút hành động khả dụng của GV.
- **Đề tài 2:** CHO_BO_SUNG_HO_SO (gv01) - Kiểm thử luồng rút đề tài + xem phản hồi/góp ý.
- **Đề tài 3:** DANG_PHAN_BIEN (gv01) - Kiểm thử hiển thị tổ phản biện, tài khoản PB có quyền truy cập.
- **Đề tài 4:** DANG_PHAN_BIEN (gv02) - Kiểm thử trường hợp PB01 đã nộp phản biện trước đó.
- **Đề tài 7:** DANG_THUC_HIEN (gv01) - Đã có hợp đồng, không cho phép rút đề tài.
- **Đề tài 11:** CHO_PNCKH_XEM_XET (gv02) - Dùng để kiểm thử lỗi truy cập trái phép 403 của gv01.

## Ma trận Bao phủ Kiểm thử

| Tính năng | Test API | Test Thủ công | Số Kịch bản | Độ ưu tiên |
|-----------|----------|---------------|-------------|------------|
| RT-01 (can-actions) | ✓ | ✓ | 7 ca | CAO |
| RT-02 (lọc kỳ NCKH) | ✓ | ✓ | 2 ca | CAO |
| RT-03 (rút đề tài) | ✓ | ✓ | 3 ca | CAO |
| A7-1 (xử lý lỗi 403) | ✓ | ✓ | 3 ca | CAO |
| B5-1 (phản hồi chi tiết) | ✓ | ✓ | 1 ca | CAO |
| RT-04 (feedback) | - | ✓ | 1 ca | CAO |
| A5-1 (EmptyState) | - | ✓ | 2 ca | TRUNG BÌNH |
| C2-1 (quyền phản biện) | ✓ | ✓ | 2 ca | CAO |
| Các tính năng chung | ✓ | ✓ | 6 ca | TRUNG BÌNH |

## Tóm tắt Kết quả Mong đợi

### Kết quả Kiểm thử API
- **Tổng số bài test:** 25+
- **Tỷ lệ đạt mong đợi:** 100%
- **Thời gian phản hồi:** < 1000ms cho tất cả các endpoint

### Kết quả Kiểm thử Thủ công
- **Luồng chính:** Tất cả đều phải vượt qua bình thường.
- **Hiển thị EmptyState:** 4 biến thể (không có dữ liệu, lỗi 403, lỗi 404, tìm kiếm không có kết quả).
- **Các nút hành động:** Hiển thị phù hợp với từng vai trò người dùng và trạng thái đề tài.
- **Không có lỗi Console:** Không xuất hiện lỗi đỏ (chấp nhận cảnh báo - warning).

## Xử lý Sự cố (Troubleshooting)

### Kiểm thử API bị lỗi

**Vấn đề:** Các kiểm thử thất bại do lỗi xác thực (authentication)
```bash
# Kiểm tra xem Backend có đang chạy hay không
curl http://localhost:8080/actuator/health

# Xác minh dữ liệu thử nghiệm đã được nạp chưa
psql -U postgres -d rgms -c "SELECT COUNT(*) FROM nguoi_dung WHERE username = 'gv01';"
```

**Vấn đề:** Các kiểm thử 403/404 đáng lẽ phải lỗi nhưng lại thành công
- Kiểm tra cấu hình bảo mật ở Backend.
- Đảm bảo kiểm soát truy cập dựa trên vai trò (Role-based access control) đã được kích hoạt.

### Kiểm thử Thủ công bị lỗi

**Vấn đề:** Chuyển hướng lỗi 403 không hoạt động
- Kiểm tra API interceptor ở Frontend (`apiClient.ts`).
- Xác minh handler xử lý phản hồi 403 thực hiện chuyển hướng tới `/403`.

**Vấn đề:** EmptyState không hiển thị
- Kiểm tra đường dẫn import component.
- Đảm bảo component được sử dụng đúng trong các trang danh sách/chi tiết.

**Vấn đề:** can-actions hiển thị sai nút hành động
- Kiểm tra logic trong `CanActionService.evaluate()` ở Backend.
- Xác thực xem các bước chuyển đổi trạng thái của FSM có cho phép hành động đó hay không.

## Tài nguyên Bổ sung

- **Tài liệu API Backend:** `app/backend/API.md`
- **Sơ đồ chuyển trạng thái FSM:** `docs/state-machine.md`
- **Sơ đồ ER:** `docs/er-diagram.md`
- **Các component Frontend:** `app/frontend/src/components/`

## Báo cáo Lỗi

Khi báo cáo lỗi từ các kiểm thử thất bại, vui lòng cung cấp:

1. **Mã TC:** (ví dụ: TC-GV-002)
2. **Các bước tái hiện:** Lấy từ kịch bản kiểm thử
3. **Kết quả mong đợi:** Lấy từ kịch bản kiểm thử
4. **Kết quả thực tế:** Những gì bạn quan sát được
5. **Ảnh chụp màn hình:** Đặc biệt đối với các lỗi giao diện
6. **Lỗi Console:** Sao chép toàn bộ thông báo lỗi
7. **Nhật ký Mạng (Network Log):** Chi tiết yêu cầu/phản hồi API nếu có

Ví dụ:
```
BUG: Nút rút đề tài vẫn hiển thị ở trạng thái sai

Mã kịch bản: TC-GV-003
Kết quả mong đợi: Không hiển thị nút "Rút đề tài" khi đề tài ở trạng thái DANG_THUC_HIEN
Kết quả thực tế: Nút vẫn hiển thị và click được
API Response: POST /api/de-tai/7/rut-de-tai → 400 Bad Request (chính xác)
Vấn đề: Frontend hiển thị nút dù canWithdraw=false
Component: DeTaiActionButtons.tsx dòng 45
```

## Bảo trì

### Cập nhật Dữ liệu Thử nghiệm

Để thêm đề tài thử nghiệm mới:
```sql
INSERT INTO de_tai (ma_so, ten_de_tai, trang_thai, chu_nhiem_id, ky_nckh_id, ...)
VALUES ('NCKH-2026-016', 'Tên đề tài mới', 'DRAFT', 5, 1, ...);
```

### Thêm Kịch bản Kiểm thử Mới

1. Thêm vào tệp `TEST_SCENARIOS.md` theo định dạng hiện có.
2. Cập nhật script `api-test.sh` nếu kiểm thử các endpoint mới.
3. Thêm vào `MANUAL_TEST_CHECKLIST.md` nếu có thay đổi về UI.
4. Cập nhật ma trận bao phủ kiểm thử trong tài liệu README này.

---

**Cập nhật lần cuối:** 09/07/2026  
**Phiên bản:** 1.0  
**Bảo trì bởi:** Đội ngũ Phát triển RGMS
