# Comprehensive Semantic CSS Gradient & Aesthetics Upgrade

Dựa trên phản hồi, bản nâng cấp CSS này sẽ không chỉ bổ sung gradient một cách chung chung mà sẽ **phân tích ý nghĩa ngữ nghĩa (semantics) của từng thành phần UI** (VD: Duyệt, Từ chối, Chỉnh sửa, Thông tin) để áp dụng các gradient tương ứng. Mục tiêu là tạo ra sự phân cấp thị giác rõ ràng, giúp người dùng nhận diện ngay tính chất của thao tác, đồng thời vẫn giữ nguyên phong cách chủ đạo "Slate & Teal Neo-SaaS".

## User Review Required
> [!IMPORTANT]
> - Các class button mới (như `.btn-warning`, `.btn-success`, `.btn-info`) sẽ được thêm vào để phục vụ cho các action mang tính chất khác nhau (Chỉnh sửa, Phê duyệt, Xem).
> - Xin vui lòng xem xét chi tiết cách phối gradient dưới đây để đảm bảo phù hợp với luồng nghiệp vụ của dự án.

## 1. Mở rộng Design Tokens (`design-tokens.css`)

Chúng ta sẽ khai báo thêm các biến `--gradient-*` ánh xạ trực tiếp từ các dải màu (palette) đã có sẵn để đảm bảo sự đồng bộ.

### A. Gradient cho Buttons & Actions (Sắc độ đậm, nổi bật)
- `--gradient-primary` (Emerald Teal): Dành cho các action chính yếu (Lưu, Xác nhận).
- `--gradient-success` (Green/Mint): Dành cho hành động Phê duyệt (Approve), Hoàn thành.
- `--gradient-warning` (Amber/Orange): Dành cho hành động Chỉnh sửa (Edit), Tạm hoãn.
- `--gradient-danger` (Red/Rose): Dành cho hành động Từ chối (Reject), Xóa.
- `--gradient-info` (Slate Blue): Dành cho hành động Xem chi tiết (View), Thông tin.

### B. Gradient cho Backgrounds & Surfaces (Sắc độ nhạt, tinh tế)
- `--gradient-surface-hover`: Dành cho hover state của Card, tạo cảm giác thẻ nổi lên.
- `--gradient-active-menu`: Dành cho `.router-link-active` ở sidebar, tạo hiệu ứng dải sáng từ trái sang phải.
- `--gradient-focus-ring`: Dành cho border/box-shadow khi focus vào `.form-input`, `.form-select`.

### C. Gradient cho Icons & Badges
- Cập nhật các biến màu nền cho `.stat-icon-*` và `.badge-*` thành dạng linear-gradient nhẹ, giúp element trông có chiều sâu (3D effect) thay vì màu phẳng (flat).

## 2. Nâng cấp CSS Elements (`style.css`)

### A. Hệ thống Buttons (`.btn`)
Dựa vào tính chất công việc:
- **`.btn-primary` (Main CTA):** Sử dụng `--gradient-primary`. Hover sẽ đổi góc (angle) hoặc tăng độ sáng.
- **`.btn-success` (Approve):** Thêm class mới. Sử dụng `--gradient-success` (Xanh lá).
- **`.btn-warning` (Edit/Update):** Thêm class mới. Sử dụng `--gradient-warning` (Vàng/Cam).
- **`.btn-danger` (Reject/Delete):** Nâng cấp dùng `--gradient-danger` (Đỏ) thay vì màu solid cũ.
- **`.btn-info` (View Detail):** Thêm class mới. Sử dụng `--gradient-info` (Xanh dương).
- **`.btn-secondary` / `.btn-ghost`:** Giữ nguyên tính chất trung tính (Neutral), nhưng khi hover sẽ có một lớp gradient surface thật nhẹ.

### B. Form Controls & Selection Boxes (`.form-select`, `.form-input`)
- **Trạng thái Default:** Giữ nguyên background trắng/xám nhẹ để đảm bảo độ tương phản khi đọc text.
- **Trạng thái Focus (Đang nhập/Đang chọn):** Thay vì chỉ viền 1 màu Teal, sẽ áp dụng hiệu ứng viền gradient (sử dụng `box-shadow` hoặc `border-image`) kết hợp background glow cực nhẹ bên trong field.
- **Trạng thái Error (`.is-error`):** Áp dụng viền gradient Đỏ và background nhạt màu đỏ để cảnh báo mạnh mẽ hơn.

### C. Cards & Bảng Thống Kê (`.card`, `.stat-card`, `.project-card`)
- **Hover Effect:** Khi hover chuột, background của thẻ sẽ chuyển sang dùng `--gradient-surface-hover` tạo cảm giác ánh sáng lướt qua (glassmorphism nhẹ).
- **Icon trong Stat Card (`.stat-icon-*`):** Các icon báo cáo (Doanh thu, Đơn hàng) sẽ sử dụng gradient nổi bật làm nền thay vì màu nhạt tĩnh. (Ví dụ: Icon Xanh Teal cho Tiền, Cam cho Cảnh báo, Đỏ cho Rủi ro).

### D. Navigation & Stepper (`.sidebar`, `.step-wizard`)
- **Active Menu (`.sidebar-nav a.router-link-active`):** Dùng gradient từ Teal sang trong suốt (từ trái qua phải) làm background, kết hợp border trái sáng rực rỡ, giúp user nhận diện trang hiện tại rõ nét hơn.
- **Steppers (`.step-item.done`, `.stepper-step.done`):** Các bước đã hoàn thành sẽ dùng gradient Success (Xanh lá) hoặc Primary (Teal) cho số thứ tự/chấm tròn, mang lại cảm giác "Achievement" khi hoàn tất quy trình.
- **Progress Bar (`.progress-fill`):** Sử dụng gradient xuyên suốt thanh tiến trình để hiển thị sự mượt mà.

## Verification Plan
1. **Kiểm tra file token:** Đảm bảo `design-tokens.css` chứa đầy đủ các dải gradient ngữ nghĩa mà không làm vỡ các token cũ.
2. **Kiểm tra hiển thị:** Chạy server frontend và render các component:
   - Các nút Phê duyệt (Xanh lá), Từ chối (Đỏ), Chỉnh sửa (Vàng) hiển thị khác biệt rõ rệt.
   - Click/Focus vào Select box / Input để xem độ nổi khối của gradient viền.
   - Hover qua Sidebar và Card để kiểm tra độ mượt của animation ánh sáng.
3. **Responsive & Contrast:** Đảm bảo chữ trắng trên nền gradient của button vẫn đạt chuẩn tương phản và dễ đọc.
