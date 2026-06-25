# Nguyên Tắc Thiết Kế Giao Diện & Chuẩn Responsive (RGMS)

Tài liệu này quy định các triết lý thiết kế và tiêu chuẩn Responsive áp dụng cho toàn bộ dự án Hệ thống Quản lý Đề tài Nghiên cứu Khoa học (RGMS), nhằm đảm bảo một trải nghiệm người dùng (UX) hiện đại, nhất quán và đạt chuẩn quốc tế trên mọi thiết bị.

---

## 1. Triết Lý Thiết Kế (Design Philosophy)

### 1.1. Hybrid Layout (Vỏ co giãn - Nội dung căn giữa)
- Không để nội dung giãn vô hạn trên màn hình siêu rộng (Ultra-wide).
- Thiết lập giới hạn chiều rộng tối đa (`max-width`) cho từng loại trang để đảm bảo khả năng đọc (Readability):
  - **Trang chung (Dashboard, Danh sách):** Max-width `1440px`.
  - **Trang đọc/Form đơn:** Max-width `760px` (để tối ưu luồng chuyển động của mắt).
  - **Văn bản Hợp đồng:** Max-width `860px`.

### 1.2. Responsive Stacking (Xếp chồng nội dung)
- **Tuyệt đối không dùng thanh cuộn ngang (Horizontal Scroll)** cho các giao diện dạng Khối (Card), Danh sách (List), hoặc Biểu mẫu (Form).
- Khi không gian ngang bị thu hẹp, các phần tử đang nằm ngang (Row) phải được **tự động xếp dọc (Column)** xuống dưới.

### 1.3. Line-clamp thay cho Nowrap
- Trên thiết bị di động, không sử dụng `white-space: nowrap` cho các tiêu đề dài vì sẽ gây tràn/vỡ layout.
- Áp dụng kỹ thuật `-webkit-line-clamp: 2` để cho phép chữ tự động xuống dòng tối đa 2 hàng, giúp người dùng đọc đủ ý mà không phá vỡ cấu trúc khối.

---

## 2. Hệ Thống Breakpoints

Hệ thống tuân thủ 4 ngưỡng màn hình chuẩn (Breakpoints) để điều hướng UI:

1. **Mobile (`max-width: 640px`)**
   - Mọi thành phần đều trở về dạng 1 cột (1-column layout).
   - Sidebar bị ẩn, điều hướng qua Hamburger Menu.
2. **Tablet (`max-width: 1024px`)**
   - Không gian rộng hơn nhưng vẫn ưu tiên cảm ứng.
   - Sidebar được thiết lập dạng **Drawer** (Trượt từ mép trái ra) với Overlay phủ mờ (Backdrop blur) để khóa tương tác với nội dung nền.
3. **Desktop (`1024px – 1440px`)**
   - Sidebar cố định bên trái (Static). Nội dung trải tự do theo phần diện tích còn lại.
4. **Ultra-wide (`> 1440px`)**
   - Vỏ trang (shell) vẫn full-width nhưng phần nội dung (page-content) tự động thêm Padding để căn giữa và giữ nguyên ở kích thước 1440px.

---

## 3. Tiêu Chuẩn Các Thành Phần UI (UI Components Standards)

### 3.1. Navigation (Menu Điều Hướng)
- Thiết bị Mobile/Tablet bắt buộc phải dùng **Hamburger Menu** (`44x44px`) thay cho Sidebar lấn chiếm diện tích.
- Khi Menu mở, bắt buộc phải chặn cuộn trang (lock `body overflow`) và có lớp phủ mờ (Overlay) làm điểm chạm (touch area) để đóng menu dễ dàng.

### 3.2. Touch Targets (Vùng Chạm Cảm Ứng)
- Theo chuẩn của Apple (HIG) và Google (Material Design), mọi phần tử có thể tương tác (Nút bấm, Link, Icon toggle) trên Mobile phải có kích thước tối thiểu là **`44px x 44px`** để ngón tay dễ chạm, tránh bấm nhầm.

### 3.3. Form & Action Buttons (Biểu mẫu & Nút hành động)
- **Form Columns:** Form 2-3 cột trên Desktop tự động gập thành 1 cột duy nhất trên Mobile.
- **Action Buttons:** Các hàng chứa cụm nút hành động (Ví dụ: [Hủy] - [Lưu]) sẽ chuyển thành `flex-direction: column-reverse`.
  - Nút quan trọng nhất (Lưu/Gửi) nằm trên cùng.
  - Nút phụ (Hủy/Quay lại) nằm dưới cùng.
  - Chiều ngang nút chiếm 100% (full-width) để dễ chạm bằng ngón cái.

### 3.4. Bảng Dữ Liệu (Data Tables)
- Đây là ngoại lệ duy nhất được phép có **Thanh cuộn ngang**.
- Bảng phải luôn được bọc trong một `.table-wrapper` có `overflow-x: auto` và `-webkit-overflow-scrolling: touch` để người dùng vuốt trượt ngang vùng bảng mà không làm trượt toàn bộ trang web.
- Thiết lập `min-width` cho bảng (ví dụ: `600px`) để chống vỡ các cột.

---
*Tài liệu này được cập nhật vào lúc hoàn thành đợt Nâng cấp Responsive Toàn cục RGMS.*
