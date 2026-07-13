# Bảng kiểm tra Kiểm thử Thủ công RGMS

**Mục đích:** Kiểm thử giao diện UI/UX trên trình duyệt cho các tính năng được khắc phục lỗi/nâng cấp  
**Ngày thực hiện:** 09/07/2026  
**Người kiểm thử:** _________________  
**Môi trường:** Frontend: http://localhost:3000 | Backend: http://localhost:8080

---

## Thiết lập Trước khi Kiểm thử

- [ ] Chạy tệp `test-data.sql` để nạp dữ liệu vào cơ sở dữ liệu
- [ ] Đảm bảo máy chủ Backend đang hoạt động ổn định
- [ ] Đảm bảo máy chủ phát triển Frontend đang hoạt động
- [ ] Mở sẵn Trình công cụ dành cho nhà phát triển (Browser DevTools - các tab Console + Network)
- [ ] Xóa cache và cookies của trình duyệt
- [ ] Kiểm thử trên Chrome/Edge (chính), Firefox (phụ)

---

## 1. Xác thực & Đăng nhập

### 1.1 Đăng nhập dưới vai trò GIANG_VIEN (gv01)
- [ ] Truy cập đường dẫn `http://localhost:3000/login`
- [ ] Nhập tên đăng nhập: `gv01`, mật khẩu: `test123`
- [ ] Nhấp nút "Đăng nhập"
- [ ] **Kết quả mong đợi:** Được chuyển hướng đến bảng điều khiển (dashboard) hoặc trang chủ
- [ ] **Kết quả mong đợi:** Tên người dùng "Nguyễn Văn Anh" hiển thị trên thanh header
- [ ] **Kết quả mong đợi:** Mã JWT token được lưu trong `localStorage`
- [ ] Kiểm tra tab Network: Yêu cầu POST `/api/auth/login` trả về mã trạng thái 200 OK

### 1.2 Đăng xuất và Đăng nhập lại
- [ ] Nhấp nút đăng xuất
- [ ] **Kết quả mong đợi:** Chuyển hướng về trang đăng nhập
- [ ] **Kết quả mong đợi:** `localStorage` được xóa sạch dữ liệu token
- [ ] Đăng nhập lại với tài khoản `gv01` để tiếp tục các bước kiểm thử tiếp theo

---

## 2. Danh sách Đề tài & Trạng thái trống (A5-1)

### 2.1 Xem Danh sách Đề tài
- [ ] Truy cập đường dẫn `/de-tai`
- [ ] **Kết quả mong đợi:** Bảng/lưới hiển thị danh sách các đề tài của GV đăng nhập (không hiển thị toàn bộ 15 đề tài trong DB)
- [ ] **Kết quả mong đợi:** Chỉ nhìn thấy các đề tài thuộc sở hữu của `gv01` (có ID tương ứng: 1, 2, 3, 5, 7, 10, 12, 14, 15)
- [ ] **Kết quả mong đợi:** Các đề tài thuộc sở hữu của `gv02` KHÔNG xuất hiện trong danh sách
- [ ] Xác nhận mỗi đề tài hiển thị đầy đủ thông tin: mã số, tên đề tài, trạng thái, ngày tạo

### 2.2 Trạng thái trống - Chưa có đề tài (Nếu có thể)
- [ ] Nếu GV chưa có đề tài nào, kiểm tra component EmptyState hiển thị các thành phần:
  - [ ] Biểu tượng (Tệp tin hoặc Thư mục)
  - [ ] Tiêu đề: "Chưa có đề tài nào"
  - [ ] Mô tả: Một thông báo hữu ích hướng dẫn người dùng
  - [ ] Nút hành động chính: "Tạo đề tài mới"
- [ ] Nhấp nút hành động → chuyển hướng người dùng đến trang `/de-tai/new`

### 2.3 Trạng thái trống - Tìm kiếm không có kết quả
- [ ] Trên trang danh sách đề tài, sử dụng hộp tìm kiếm
- [ ] Nhập một từ khóa vô nghĩa: "xyzabc123"
- [ ] **Kết quả mong đợi:** Hiển thị giao diện EmptyState dành cho trường hợp không tìm thấy kết quả tìm kiếm
  - [ ] Biểu tượng (Tìm kiếm hoặc Kính lúp)
  - [ ] Tiêu đề: "Không tìm thấy kết quả"
  - [ ] Mô tả có chứa từ khóa vừa tìm kiếm

---

## 3. Trang Chi tiết Đề tài (B5-1)

### 3.1 Xem Chi tiết Đề tài - Các trường cơ bản
- [ ] Nhấp vào đề tài có ID là 1 (trạng thái CHO_PNCKH_XEM_XET)
- [ ] **Kết quả mong đợi:** Chuyển hướng tới `/de-tai/1`
- [ ] Xác minh hiển thị đầy đủ các trường thông tin:
  - [ ] Mã số: NCKH-2026-001
  - [ ] Tên đề tài: "Nghiên cứu ứng dụng AI trong chẩn đoán bệnh"
  - [ ] Mô tả (nội dung đầy đủ)
  - [ ] Lĩnh vực: Trí tuệ nhân tạo
  - [ ] Nhãn trạng thái (badge): CHO_PNCKH_XEM_XET (màu vàng/cam)
  - [ ] Chủ nhiệm: Nguyễn Văn Anh
  - [ ] Kỳ NCKH: Kỳ NCKH 2026-01
  - [ ] Ngày tạo, ngày cập nhật

### 3.2 Xem Đề tài có Hợp đồng (Đề tài số 7)
- [ ] Truy cập đường dẫn `/de-tai/7`
- [ ] Xác minh phần thông tin hợp đồng hiển thị đúng:
  - [ ] Kinh phí: 150,000,000 VNĐ
  - [ ] Ngày bắt đầu: 2026-06-01
  - [ ] Ngày kết thúc: 2027-05-31
  - [ ] Tỷ lệ tạm ứng: 30%
  - [ ] Trạng thái hợp đồng: Đã ký
- [ ] Xác minh nhãn trạng thái đề tài: DANG_THUC_HIEN (màu xanh lá)

### 3.3 Xem Đề tài có Tổ Phản biện (Đề tài số 3)
- [ ] Đăng xuất và đăng nhập lại bằng tài khoản `nckh01`
- [ ] Truy cập đường dẫn `/de-tai/3`
- [ ] Xác minh phần thông tin tổ phản biện hiển thị đúng:
  - [ ] Tiêu đề phần: "Tổ phản biện"
  - [ ] Danh sách các phản biện viên: Phạm Đức Dũng, Hoàng Thị Oanh
  - [ ] Trạng thái của từng phản biện viên: "Chưa nộp"
  - [ ] Hạn nộp hiển thị rõ ràng
  - [ ] Kết quả tổng hợp: CHUA_CO

---

## 4. Trạng thái hành động khả dụng & Các nút hành động (RT-01)

### 4.1 Hành động của GV - CHO_PNCKH_XEM_XET
- [ ] Đăng nhập dưới tài khoản `gv01`
- [ ] Truy cập đường dẫn `/de-tai/1`
- [ ] **Các nút mong đợi:**
  - [ ] Nút "Rút đề tài" hiển thị (màu đỏ/cảnh báo)
  - [ ] KHÔNG hiển thị nút "Chỉnh sửa"
  - [ ] KHÔNG hiển thị nút "Nộp đề tài"
- [ ] Kiểm tra tab Network: Yêu cầu GET `/api/de-tai/1/can-actions` trả về đúng các giá trị boolean tương ứng

### 4.2 Hành động của GV - CHO_BO_SUNG_HO_SO
- [ ] Truy cập đường dẫn `/de-tai/2`
- [ ] **Các nút mong đợi:**
  - [ ] Nút "Rút đề tài" hiển thị
  - [ ] Nút "Bổ sung hồ sơ" hiển thị (hoặc tương tự)
- [ ] Phần phản hồi góp ý hiển thị yêu cầu bổ sung của PNCKH
- [ ] Hiển thị đồng hồ đếm ngược hạn nộp phản hồi

### 4.3 Hành động của GV - DANG_THUC_HIEN (Trạng thái cuối)
- [ ] Truy cập đường dẫn `/de-tai/7`
- [ ] **Kết quả mong đợi:** KHÔNG hiển thị bất kỳ nút hành động nào (đề tài đang thực hiện)
- [ ] Nhãn trạng thái: DANG_THUC_HIEN (màu xanh lá)
- [ ] Thông tin hợp đồng được hiển thị đầy đủ

### 4.4 Hành động của NCKH - CHO_PNCKH_XEM_XET
- [ ] Đăng nhập dưới tài khoản `nckh01`
- [ ] Truy cập đường dẫn `/de-tai/1`
- [ ] **Các nút mong đợi:**
  - [ ] Nút "Duyệt đề tài" hiển thị (màu xanh lá/chính)
  - [ ] Nút "Yêu cầu bổ sung hồ sơ" hiển thị (màu vàng/phụ)
  - [ ] Nút "Từ chối đề tài" hiển thị (màu đỏ/nguy hiểm)
- [ ] Tất cả các nút này đều có thể nhấp được bình thường

### 4.5 Hành động của PB - DANG_PHAN_BIEN
- [ ] Đăng nhập dưới tài khoản `pb01`
- [ ] Truy cập đường dẫn `/de-tai/3` (đề tài được phân công phản biện)
- [ ] **Các nút mong đợi:**
  - [ ] Nút "Nộp phản biện" hiển thị rõ ràng
- [ ] Truy cập đường dẫn `/de-tai/4` (đề tài đã nộp ý kiến phản biện)
- [ ] **Kết quả mong đợi:** Hiển thị nhãn/trạng thái "Đã nộp phản biện"
  - [ ] KHÔNG hiển thị nút "Nộp phản biện"
  - [ ] Nội dung phản biện hiển thị ở chế độ chỉ đọc (read-only)

---

## 5. Rút Đề Tài (RT-03)

### 5.1 Rút Đề tài Thành công
- [ ] Đăng nhập dưới tài khoản `gv01`
- [ ] Truy cập đường dẫn `/de-tai/2` (trạng thái CHO_BO_SUNG_HO_SO)
- [ ] Nhấp nút "Rút đề tài"
- [ ] **Kết quả mong đợi:** Hộp thoại xác nhận (modal) mở ra
  - [ ] Tiêu đề: "Xác nhận rút đề tài"
  - [ ] Nội dung: "Bạn có chắc muốn rút đề tài này? Hành động này không thể hoàn tác."
  - [ ] Nút "Hủy bỏ"
  - [ ] Nút "Xác nhận" (màu đỏ)
- [ ] Nhấp nút "Xác nhận"
- [ ] Kiểm tra tab Network: POST `/api/de-tai/2/rut-de-tai` → phản hồi mã 200 OK
- [ ] **Kết quả mong đợi:** Hiển thị thông báo toast thành công
  - [ ] Nội dung: "Đã rút đề tài thành công"
  - [ ] Tự động biến mất sau 3-5 giây
- [ ] **Kết quả mong đợi:** Trang tự động làm mới hoặc trạng thái cập nhật ngay
- [ ] Nhãn trạng thái đổi thành: DA_RUT (màu xám)
- [ ] Các nút hành động biến mất (đây là trạng thái cuối cùng)

### 5.2 Rút Đề tài Không cho phép
- [ ] Truy cập đường dẫn `/de-tai/7` (trạng thái DANG_THUC_HIEN)
- [ ] **Kết quả mong đợi:** KHÔNG hiển thị nút "Rút đề tài"
- [ ] Gọi API thủ công (qua console trình duyệt hoặc Postman):
  ```js
  fetch('/api/de-tai/7/rut-de-tai', {
    method: 'POST',
    headers: { 'Authorization': 'Bearer <token>' }
  })
  ```
- [ ] **Kết quả mong đợi:** Trả về lỗi 400 Bad Request
- [ ] Thông điệp lỗi: "Không thể rút đề tài ở trạng thái DANG_THUC_HIEN"
- [ ] Trạng thái đề tài không thay đổi, không sinh bản ghi nhật ký AuditLog mới

---

## 6. Lọc Kỳ NCKH (RT-02)

### 6.1 GV Chỉ nhìn thấy các Kỳ NCKH Đang mở
- [ ] Đăng nhập dưới tài khoản `gv01`
- [ ] Truy cập trang "Tạo đề tài mới" `/de-tai/new`
- [ ] Tìm trường chọn dropdown "Kỳ NCKH"
- [ ] Nhấp vào dropdown
- [ ] **Kết quả mong đợi:** Chỉ hiển thị 1 tùy chọn duy nhất
  - [ ] "Kỳ NCKH 2026-01" (trạng thái DANG_MO)
  - [ ] Tùy chọn "Kỳ NCKH 2025-02" KHÔNG xuất hiện (trạng thái DA_DONG)
- [ ] Kiểm tra Network: GET `/api/ky-nckh?trangThai=DANG_MO` → trả về danh sách có 1 phần tử

### 6.2 NCKH Nhìn thấy Tất cả các Kỳ NCKH
- [ ] Đăng nhập dưới tài khoản `nckh01`
- [ ] Truy cập trang quản lý Kỳ NCKH `/ky-nckh`
- [ ] **Kết quả mong đợi:** Bảng hiển thị đầy đủ 2 dòng dữ liệu
  - [ ] Kỳ NCKH 2026-01 (DANG_MO) - có nhãn hoạt động
  - [ ] Kỳ NCKH 2025-02 (DA_DONG) - có nhãn đã đóng
- [ ] Kiểm tra Network: GET `/api/ky-nckh` (không truyền tham số lọc) → trả về đầy đủ 2 phần tử

---

## 7. Xử lý Lỗi Truy cập 403 Forbidden (A7-1)

### 7.1 GV Không được xem Đề tài của GV Khác
- [ ] Đăng nhập dưới tài khoản `gv01`
- [ ] Nhập thủ công đường dẫn `/de-tai/11` (đề tài thuộc sở hữu của gv02) lên thanh địa chỉ
- [ ] **Kết quả mong đợi:** Chuyển hướng người dùng sang trang báo lỗi `/403`
- [ ] Xác minh giao diện EmptyState hiển thị:
  - [ ] Biểu tượng: Khóa hoặc Từ chối truy cập
  - [ ] Tiêu đề: "Không có quyền truy cập"
  - [ ] Mô tả: "Bạn không có quyền xem đề tài này"
  - [ ] Nút hành động: "Quay lại trang chủ" hoặc "Quay lại danh sách"
- [ ] Kiểm tra Network: GET `/api/de-tai/11` trả về mã lỗi 403 Forbidden
- [ ] Nhấp vào nút hành động → chuyển hướng thành công về trang chủ hoặc danh sách

### 7.2 PB Không được xem Đề tài không được phân công
- [ ] Đăng nhập dưới tài khoản `pb01`
- [ ] Nhập thủ công đường dẫn `/de-tai/1` (đề tài không phân công cho pb01)
- [ ] **Kết quả mong đợi:** Hệ thống chuyển hướng sang trang `/403`
- [ ] Giao diện EmptyState hiển thị thông báo phù hợp:
  - [ ] "Bạn chỉ có thể xem đề tài mà bạn được phân công phản biện"
- [ ] Kiểm tra Network: Trả về mã lỗi 403 Forbidden

### 7.3 PB Truy cập được Đề tài được phân công
- [ ] Đang đăng nhập dưới tài khoản `pb01`
- [ ] Truy cập đường dẫn `/de-tai/3` (đề tài pb01 là thành viên phản biện)
- [ ] **Kết quả mong đợi:** Trang hiển thị nội dung bình thường (mã 200 OK)
- [ ] Toàn bộ chi tiết đề tài hiển thị rõ ràng
- [ ] Có sẵn các nút hành động phản biện

---

## 8. Xử lý Lỗi Không tìm thấy 404 Not Found

### 8.1 Truy cập Đề tài Không Tồn tại
- [ ] Đăng nhập dưới một tài khoản bất kỳ
- [ ] Truy cập đường dẫn `/de-tai/9999`
- [ ] **Kết quả mong đợi:** Hiển thị trang báo lỗi 404 hoặc EmptyState tương ứng
- [ ] Xác minh nội dung hiển thị:
  - [ ] Biểu tượng: Tìm kiếm hoặc Dấu hỏi chấm
  - [ ] Tiêu đề: "Không tìm thấy đề tài"
  - [ ] Mô tả: "Đề tài không tồn tại hoặc đã bị xóa"
  - [ ] Nút hành động: "Quay lại danh sách"
- [ ] Kiểm tra Network: GET `/api/de-tai/9999` trả về mã lỗi 404 Not Found

---

## 9. Đánh giá Chất lượng UI/UX

### 9.1 Nhãn trạng thái (Status Badges)
- [ ] Lần lượt xem các đề tài khác nhau (IDs từ 1 đến 15)
- [ ] Xác minh mỗi trạng thái đề tài có một màu sắc nhãn phân biệt riêng:
  - [ ] DRAFT: Màu xám
  - [ ] CHO_PNCKH_XEM_XET: Màu vàng/cam
  - [ ] DANG_PHAN_BIEN: Màu xanh dương
  - [ ] DANG_THUC_HIEN: Màu xanh lá
  - [ ] DA_RUT: Màu xám nhạt (muted)
  - [ ] BI_TU_CHOI: Màu đỏ
- [ ] Chữ trên nhãn dễ đọc, viết hoa hoặc hiển thị đẹp mắt

### 9.2 Trạng thái của Nút bấm
- [ ] Rê chuột (hover) qua các nút hành động
  - [ ] Hiệu ứng hover hiển thị rõ ràng (đổi tông màu hoặc đổ bóng)
- [ ] Các nút bị vô hiệu hóa (disabled) hiển thị màu xám mờ
- [ ] Hành động chính (primary) sử dụng màu thương hiệu (xanh dương hoặc xanh lá)
- [ ] Hành động hủy hoại/xóa (destructive) sử dụng màu đỏ hoặc màu cảnh báo

### 9.3 Trạng thái Loading
- [ ] Mở trang chi tiết đề tài
- [ ] **Kết quả mong đợi:** Hiển thị vòng xoay loading hoặc bộ xương giao diện (skeleton) trong khi tải dữ liệu
- [ ] Không bị hiện tượng giật khung hình hay nháy thông tin cũ trước khi tải xong
- [ ] Hiển thị mượt mà khi dữ liệu được nạp thành công

### 9.4 Thông báo nhanh (Toast Notifications)
- [ ] Thực hiện một hành động (ví dụ: rút đề tài)
- [ ] Xác minh thông báo toast:
  - [ ] Xuất hiện ở vị trí nhất quán (góc trên bên phải hoặc góc dưới bên phải)
  - [ ] Thành công (Success) = nền xanh lá
  - [ ] Lỗi (Error) = nền đỏ
  - [ ] Tự động biến mất sau 3-5 giây
  - [ ] Nhấp nút đóng (X) hoạt động tốt

### 9.5 Giao diện Responsive (Tùy chọn)
- [ ] Thu nhỏ cửa sổ trình duyệt về kích thước di động (375px)
- [ ] Xác minh giao diện tự động thích ứng:
  - [ ] Thanh điều hướng thu gọn lại thành menu hamburger
  - [ ] Các bảng dữ liệu cho phép cuộn ngang hoặc tự chuyển sang dạng danh sách xếp chồng
  - [ ] Các nút hành động vẫn dễ dàng chạm/nhấp vào

---

## 10. Kiểm tra Console & Network

### 10.1 Lỗi Console
- [ ] Kiểm tra tab Console của trình duyệt xuyên suốt quá trình kiểm thử
- [ ] **Kết quả mong đợi:** KHÔNG xuất hiện lỗi đỏ nào
- [ ] Chấp nhận các cảnh báo màu vàng (nhưng cần lưu ý nếu xuất hiện quá nhiều)
- [ ] Ghi lại bất kỳ lỗi nào xuất hiện:
  - Thông báo lỗi: ______________________________
  - Trang / hành động: ______________________________

### 10.2 Tab Network
- [ ] Theo dõi tất cả cuộc gọi API
- [ ] Xác minh tiêu đề xác thực:
  - [ ] Truyền đúng `Authorization: Bearer <token>` trên mỗi yêu cầu gửi đi
- [ ] Xác minh thời gian phản hồi:
  - [ ] Phần lớn các yêu cầu đạt dưới 500ms
  - [ ] Các yêu cầu phức tạp đạt dưới 1000ms
- [ ] Không có yêu cầu nào bị lỗi mạng (trừ các kiểm thử cố ý kích hoạt lỗi 403/404)

### 10.3 Lỗi CORS
- [ ] Không xuất hiện lỗi CORS trong Console trình duyệt
- [ ] Nếu có, cần báo kỹ thuật kiểm tra cấu hình CORS phía Backend

---

## 11. Hỗ trợ Tiếp cận - Accessibility (Tùy chọn khuyến khích)

### 11.1 Sử dụng Bàn phím
- [ ] Sử dụng phím `Tab` để di chuyển qua các thành phần trên trang
- [ ] Tất cả phần tử tương tác đều có viền tập trung (focus ring) nhìn thấy rõ
- [ ] Phím `Enter` kích hoạt được các nút bấm
- [ ] Phím `Esc` đóng được các hộp thoại modal

### 11.2 Trình đọc màn hình (Tùy chọn)
- [ ] Bật các trình đọc màn hình (NVDA, JAWS, VoiceOver)
- [ ] Đọc thử trang chi tiết đề tài
- [ ] Mọi thông tin được phát âm chuẩn xác, cấu trúc rõ ràng
- [ ] Mục đích sử dụng của các nút bấm được công bố rõ

---

## 12. Kiểm thử Đa trình duyệt (Nếu có thời gian)

### 12.1 Firefox
- [ ] Lặp lại các kiểm thử quan trọng (đăng nhập, xem đề tài, rút đề tài)
- [ ] Xác minh giao diện và hành vi hoạt động giống hệt trên Chrome
- [ ] Ghi chú các điểm khác biệt: ______________________________

### 12.2 Safari (macOS/iOS)
- [ ] Lặp lại các kiểm thử quan trọng
- [ ] Ghi chú các điểm khác biệt: ______________________________

---

## Hoàn thành Kiểm thử

### Tổng hợp Kết quả
- **Số bài test đạt:** _____ / _____
- **Số bài test lỗi:** _____
- **Số vấn đề chặn (Blocker):** _____
- **Ghi chú / Chi tiết lỗi:**

  _______________________________________________
  
  _______________________________________________
  
  _______________________________________________

### Danh sách Ảnh chụp màn hình cần lưu lại
- [ ] EmptyState - Không có đề tài nào
- [ ] EmptyState - Bị lỗi 403 Forbidden
- [ ] EmptyState - Bị lỗi 404 Not Found
- [ ] Chi tiết đề tài hiển thị đầy đủ các trường thông tin
- [ ] Hộp thoại xác nhận rút đề tài
- [ ] Thông báo toast thành công
- [ ] Các nhãn trạng thái đề tài (tất cả các màu)

### Chữ ký Xác nhận
- **Tên người kiểm thử:** _____________________
- **Ngày thực hiện:** _____________________
- **Kết luận:** ☐ ĐẠT ☐ KHÔNG ĐẠT ☐ BỊ CHẶN

---

## Phụ lục: Tài liệu Dữ liệu Thử nghiệm Tham khảo

| ID Đề tài | Trạng thái | Chủ sở hữu | Ghi chú |
|-----------|------------|------------|---------|
| 1 | CHO_PNCKH_XEM_XET | gv01 | Có thể rút |
| 2 | CHO_BO_SUNG_HO_SO | gv01 | Có thể rút, có nội dung phản hồi |
| 3 | DANG_PHAN_BIEN | gv01 | Có tổ phản biện được thiết lập |
| 4 | DANG_PHAN_BIEN | gv02 | pb01 đã nộp phản biện |
| 5 | CHO_CHINH_SUA_THUYET_MINH | gv01 | - |
| 6 | DANG_LAP_HOP_DONG | gv02 | GV đã đồng ý hợp đồng |
| 7 | DANG_THUC_HIEN | gv01 | Đã ký hợp đồng, không cho rút |
| 8 | DANG_THUC_HIEN | gv02 | Đã ký hợp đồng |
| 9 | BI_TU_CHOI | gv02 | Trạng thái cuối |
| 10 | DA_RUT | gv01 | Trạng thái cuối |
| 11 | CHO_PNCKH_XEM_XET | gv02 | Phục vụ test lỗi 403 |
| 12 | DRAFT | gv01 | Chưa nộp đề tài |
| 13 | BI_TREO | gv02 | Trạng thái cuối |
| 14 | DA_HUY | gv01 | Trạng thái cuối |
| 15 | DANG_XEM_XET_BOI_PNCKH | gv01 | NCKH đang xem xét |

**Tài khoản Thử nghiệm:**
- GV: `gv01` / `test123` (Nguyễn Văn Anh)
- GV: `gv02` / `test123` (Trần Thị Bình)
- NCKH: `nckh01` / `test123` (Lê Minh Châu)
- PB: `pb01` / `test123` (Phạm Đức Dũng)
- PB: `pb02` / `test123` (Hoàng Thị Oanh)
