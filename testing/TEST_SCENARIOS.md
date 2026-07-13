# Kịch bản Kiểm thử Khắc phục lỗi RGMS

**Phiên bản Tài liệu:** 1.0  
**Ngày thực hiện:** 09/07/2026  
**Phạm vi bao phủ:** RT-01, RT-02, RT-03, A7-1, B5-1, RT-04, A5-1, C2-1

---

## Mục lục
1. [Thiết lập Môi trường Kiểm thử](#thiết-lập-môi-trường-kiểm-thử)
2. [Kiểm thử cho vai trò GIANG_VIEN (Giảng viên)](#kiểm-thử-cho-vai-trò-giang_vien-giảng-viên)
3. [Kiểm thử cho vai trò NCKH (Phòng NCKH)](#kiểm-thử-cho-vai-trò-nckh-phòng-nckh)
4. [Kiểm thử cho vai trò TO_PHAN_BIEN (Tổ Phản biện)](#kiểm-thử-cho-vai-trò-to_phan_bien-tổ-phản-biện)
5. [Kiểm thử các Tính năng Chung (Cross-Cutting)](#kiểm-thử-các-tính-năng-chung-cross-cutting)

---

## Thiết lập Môi trường Kiểm thử

### Điều kiện tiên quyết
- Backend đang hoạt động tại `http://localhost:8080`
- Frontend đang hoạt động tại `http://localhost:3000`
- Dữ liệu cơ sở dữ liệu đã được nạp qua tệp `test-data.sql`
- Danh sách các tài khoản kiểm thử:
  - **GV (Giảng viên):** `gv01` / `test123`
  - **NCKH (Phòng NCKH):** `nckh01` / `test123`
  - **PB (Phản biện viên):** `pb01` / `test123`

### Tổng quan Dữ liệu Thử nghiệm
- 2 Kỳ NCKH (1 DANG_MO, 1 DA_DONG)
- 15 Đề tài bao phủ tất cả các trạng thái trong hệ thống (TopicState)
- Các bản ghi hợp đồng tương ứng cho các đề tài đủ điều kiện
- Các hội đồng phản biện và thành viên hội đồng
- Các phản hồi/yêu cầu sửa đổi cho các đề tài ở trạng thái CHO_BO_SUNG_HO_SO

---

## Kiểm thử cho vai trò GIANG_VIEN (Giảng viên)

### TC-GV-001: Xem các Hành động khả dụng của Đề tài ở trạng thái CHO_PNCKH_XEM_XET
**Độ ưu tiên:** CAO  
**Tính năng:** RT-01 (API can-actions)

**Điều kiện tiên quyết:**
- Đăng nhập dưới tài khoản `gv01`
- Đề tài số 1 tồn tại và ở trạng thái CHO_PNCKH_XEM_XET
- GV đăng nhập là chủ nhiệm đề tài (chu_nhiem_id trùng với ID của gv01)

**Các bước thực hiện:**
1. Truy cập trang chi tiết đề tài tại `/de-tai/1`
2. Quan sát các nút hành động được hiển thị trên giao diện
3. Gọi trực tiếp API: `GET /api/de-tai/1/can-actions`
4. Xác minh phản hồi từ API trùng khớp với các nút hiển thị trên giao diện UI

**Kết quả mong đợi:**
- API trả về: `{"canEdit": false, "canSubmit": false, "canWithdraw": true, ...}`
- Giao diện UI chỉ hiển thị duy nhất nút "Rút đề tài"
- Không xuất hiện nút sửa (Edit) hay nút nộp (Submit)
- Thời gian phản hồi API < 500ms

**API Endpoints:**
- `GET /api/de-tai/1/can-actions`

**Các tệp tin / Component liên quan:**
- Backend: `DeTaiController.getCanActions()`
- Backend: `CanActionService.evaluate()`
- Frontend: `pages/DeTaiDetail.tsx`
- Frontend: `components/DeTaiActionButtons.tsx`

---

### TC-GV-002: Rút đề tài thành công (RT-03)
**Độ ưu tiên:** CAO  
**Tính năng:** RT-03 (Rút đề tài)

**Điều kiện tiên quyết:**
- Đăng nhập dưới tài khoản `gv01`
- Đề tài số 2 tồn tại và ở trạng thái CHO_BO_SUNG_HO_SO
- GV đăng nhập là chủ nhiệm đề tài
- Thuộc tính `canWithdraw` trả về là `true`

**Các bước thực hiện:**
1. Truy cập trang `/de-tai/2`
2. Nhấp chọn nút "Rút đề tài"
3. Xác nhận trên hộp thoại cảnh báo: "Bạn có chắc muốn rút đề tài này?"
4. Đợi hệ thống hiển thị thông báo thành công
5. Xác minh trạng thái đề tài đã chuyển sang DA_RUT

**Kết quả mong đợi:**
- Hộp thoại cảnh báo xuất hiện để xác nhận hành động
- Yêu cầu API thành công: `POST /api/de-tai/2/rut-de-tai` → trả về 200 OK
- Một bản ghi log mới được lưu trong bảng `AuditLog` với sự kiện là `RUT_DE_TAI`
- Trạng thái đề tài được cập nhật thành `DA_RUT`
- Giao diện hiển thị thông báo toast: "Đã rút đề tài thành công"
- Không hiển thị bất kỳ nút hành động nào nữa (đề tài đã ở trạng thái cuối)

**API Endpoints:**
- `POST /api/de-tai/2/rut-de-tai`
- `GET /api/de-tai/2` (dùng để kiểm tra lại trạng thái đề tài)

**Các tệp tin / Component liên quan:**
- Backend: `DeTaiController.rutDeTai()`
- Backend: `FsmService.transition()`
- Frontend: `components/DeTaiActionButtons.tsx`
- Frontend: `hooks/useRutDeTai.ts`

---

### TC-GV-003: Cố gắng rút đề tài ở trạng thái không hợp lệ
**Độ ưu tiên:** TRUNG BÌNH  
**Tính năng:** RT-03 (Kiểm thử biên/tiêu cực)

**Điều kiện tiên quyết:**
- Đăng nhập dưới tài khoản `gv01`
- Đề tài số 8 tồn tại và ở trạng thái DANG_THUC_HIEN
- GV đăng nhập là chủ nhiệm đề tài

**Các bước thực hiện:**
1. Truy cập trang `/de-tai/8`
2. Xác minh không hiển thị nút "Rút đề tài" trên giao diện
3. Gọi API thủ công qua lệnh curl hoặc ứng dụng Postman: `POST /api/de-tai/8/rut-de-tai`

**Kết quả mong đợi:**
- Giao diện UI không hiển thị nút rút đề tài
- API trả về mã lỗi 400 Bad Request
- Thông báo lỗi: "Không thể rút đề tài ở trạng thái DANG_THUC_HIEN"
- Không có bất kỳ thay đổi trạng thái nào trong cơ sở dữ liệu
- Không có bản ghi mới nào được tạo trong `AuditLog`

**API Endpoints:**
- `POST /api/de-tai/8/rut-de-tai` (bắt buộc phải trả về lỗi)

**Các tệp tin / Component liên quan:**
- Backend: `DeTaiController.rutDeTai()`
- Backend: `FsmService.canTransition()`

---

### TC-GV-004: Chỉ xem được các Kỳ NCKH đang mở (RT-02)
**Độ ưu tiên:** CAO  
**Tính năng:** RT-02 (Bộ lọc kỳ đang hoạt động)

**Điều kiện tiên quyết:**
- Đăng nhập dưới tài khoản `gv01`
- Có ít nhất 2 Kỳ NCKH trong DB: 1 kỳ ở trạng thái `DANG_MO` và 1 kỳ ở trạng thái `DA_DONG`

**Các bước thực hiện:**
1. Truy cập trang "Tạo đề tài mới" tại `/de-tai/new`
2. Nhấp vào trường chọn "Kỳ NCKH" và quan sát các tùy chọn hiển thị
3. Gọi API: `GET /api/ky-nckh?trangThai=DANG_MO`
4. Kiểm tra xem danh sách có chứa kỳ đã đóng hay không

**Kết quả mong đợi:**
- API trả về mảng có 1 phần tử: `[{"id": 1, "tenKy": "Kỳ NCKH 2026-01", "trangThai": "DANG_MO"}]`
- Dropdown chỉ hiển thị tùy chọn "Kỳ NCKH 2026-01"
- Kỳ đã đóng "Kỳ NCKH 2025-02" hoàn toàn KHÔNG hiển thị
- Không cho phép người dùng chọn kỳ đã đóng

**API Endpoints:**
- `GET /api/ky-nckh?trangThai=DANG_MO`

**Các tệp tin / Component liên quan:**
- Backend: `KyNCKHController.getAllKyNCKH()`
- Frontend: `pages/CreateDeTai.tsx`
- Frontend: `components/KyNCKHSelect.tsx`

---

### TC-GV-005: Xem Trang chi tiết đề tài hiển thị đầy đủ thông tin (B5-1)
**Độ ưu tiên:** CAO  
**Tính năng:** B5-1 (Độ hoàn thiện của DeTaiDetailResponse)

**Điều kiện tiên quyết:**
- Đăng nhập dưới tài khoản `gv01`
- Đề tài số 1 tồn tại
- Đề tài có liên kết với hợp đồng (bản ghi `hop_dong`)
- Đề tài đã được thành lập tổ phản biện (bản ghi `to_phan_bien`)

**Các bước thực hiện:**
1. Gọi API: `GET /api/de-tai/1`
2. Kiểm tra xem tất cả các thuộc tính mô tả có xuất hiện đầy đủ trong kết quả phản hồi
3. Mở đường dẫn `/de-tai/1` trên trình duyệt
4. Xác minh giao diện hiển thị chính xác toàn bộ dữ liệu

**Kết quả mong đợi:**
- API phản hồi đầy đủ các trường của `DeTaiDetailResponse`:
  - `id`, `maSo`, `tenDeTai`, `moTa`, `linhVuc`
  - `status`, `ngayBatDau`, `ngayKetThuc`, `kinhPhi`
  - `chuNhiem` (chứa các thuộc tính con: id, hoTen, email)
  - `kyNckh` (chứa các thuộc tính con: id, tenKy, trangThai)
  - `hopDong` (nếu có: kinhPhi, ngayKy, tyLeTamUng)
  - `toPhanBien` (nếu có: ketQuaTongHop, mảng thanhVien)
  - `canActions` (đối tượng chứa các giá trị boolean tương ứng)
- Giao diện UI hiển thị:
  - Header của đề tài với mã số và nhãn trạng thái
  - Các khối thông tin chi tiết (Info cards)
  - Khối hợp đồng (nếu có)
  - Khối tổ phản biện (nếu có)
  - Các nút hành động hiển thị đúng dựa trên thuộc tính `canActions`

**API Endpoints:**
- `GET /api/de-tai/1`

**Các tệp tin / Component liên quan:**
- Backend: `DeTaiController.getDeTaiDetail()`
- Backend: `DeTaiDetailResponse.java`
- Frontend: `pages/DeTaiDetail.tsx`
- Frontend: `components/DeTaiInfoCard.tsx`
- Frontend: `components/HopDongSection.tsx`

---

### TC-GV-006: Truy cập trái phép vào đề tài của người dùng khác (A7-1)
**Độ ưu tiên:** CAO  
**Tính năng:** A7-1 (Xử lý lỗi 403)

**Điều kiện tiên quyết:**
- Đăng nhập dưới tài khoản `gv01`
- Đề tài số 10 tồn tại và thuộc sở hữu của giảng viên `gv02`

**Các bước thực hiện:**
1. Nhập thủ công đường dẫn `/de-tai/10` lên thanh địa chỉ trình duyệt
2. Quan sát kết quả xử lý và hiển thị từ hệ thống

**Kết quả mong đợi:**
- API phản hồi mã lỗi 403 Forbidden
- Frontend phát hiện mã lỗi và chuyển hướng người dùng sang trang báo lỗi `/403`
- Trang lỗi hiển thị thông báo: "Bạn không có quyền truy cập tài nguyên này"
- Có nút hành động "Quay lại trang chủ"
- Không rò rỉ bất kỳ thông tin nào của đề tài
- Địa chỉ URL đổi thành `/403`

**API Endpoints:**
- `GET /api/de-tai/10` (trả về lỗi 403)

**Các tệp tin / Component liên quan:**
- Backend: Kiểm tra phân quyền trong `DeTaiController.getDeTaiDetail()`
- Frontend: `pages/403.tsx` (sử dụng component EmptyState)
- Frontend: API Client Interceptor trong `utils/apiClient.ts`

---

## Kiểm thử cho vai trò NCKH (Phòng NCKH)

### TC-NCKH-001: Xem tất cả đề tài không phân biệt chủ nhiệm đề tài
**Độ ưu tiên:** CAO  
**Tính năng:** RT-01, A5-1 (Quyền hạn của tài khoản NCKH)

**Điều kiện tiên quyết:**
- Đăng nhập dưới tài khoản `nckh01`
- Có nhiều đề tài của nhiều giảng viên khác nhau trong hệ thống

**Các bước thực hiện:**
1. Truy cập trang danh sách đề tài `/de-tai`
2. Nhấp chọn ngẫu nhiên các đề tài khác nhau (ví dụ: ID 1, 5, 10)
3. Xác minh quyền truy cập được cấp đầy đủ cho mọi đề tài

**Kết quả mong đợi:**
- Cho phép xem chi tiết tất cả các đề tài mà không phụ thuộc vào người tạo (`chu_nhiem`)
- API phản hồi mã trạng thái 200 OK đối với tất cả các đề tài được chọn
- Không xuất hiện bất kỳ lỗi 403 Forbidden nào
- Nhìn thấy đầy đủ các thông tin chi tiết bao gồm cả thông tin giảng viên chủ nhiệm đề tài

**API Endpoints:**
- `GET /api/de-tai` (lấy danh sách)
- `GET /api/de-tai/{id}` (lấy chi tiết một ID bất kỳ)

**Các tệp tin / Component liên quan:**
- Backend: Cấu hình bảo mật bảo vệ các phương thức của `DeTaiController`
- Backend: Các quy tắc trong annotation `@PreAuthorize`

---

### TC-NCKH-002: Phê duyệt Đề tài (PNCKH_DUYET)
**Độ ưu tiên:** CAO  
**Tính năng:** RT-01 (Nút hành động khả dụng của NCKH)

**Điều kiện tiên quyết:**
- Đăng nhập dưới tài khoản `nckh01`
- Đề tài số 1 tồn tại và ở trạng thái CHO_PNCKH_XEM_XET

**Các bước thực hiện:**
1. Truy cập trang `/de-tai/1`
2. Gọi API `GET /api/de-tai/1/can-actions`
3. Nhấp chọn nút "Duyệt đề tài" trên giao diện
4. Nhập nội dung nhận xét/phê duyệt
5. Xác nhận gửi duyệt đề tài

**Kết quả mong đợi:**
- Mảng `canActions` trả về: `{"canApprove": true, "canReject": true, "canRequestSupplementary": true}`
- Nút "Duyệt đề tài" hiển thị rõ ràng trên giao diện
- Hộp thoại (modal) mở ra để nhập ý kiến phê duyệt
- API phản hồi thành công: `POST /api/de-tai/1/duyet` → trả về 200 OK
- Đề tài chuyển sang trạng thái tiếp theo trong quy trình (ví dụ: DANG_PHAN_BIEN hoặc tương đương)
- Bản ghi mới được ghi nhận trong bảng `AuditLog`

**API Endpoints:**
- `GET /api/de-tai/1/can-actions`
- `POST /api/de-tai/1/duyet`

**Các tệp tin / Component liên quan:**
- Backend: `DeTaiController.duyetDeTai()`
- Backend: `FsmService.transition()`
- Frontend: `components/NCKHActionButtons.tsx`

---

### TC-NCKH-003: Yêu cầu Bổ sung hồ sơ đề tài
**Độ ưu tiên:** CAO  
**Tính năng:** RT-01, RT-04 (Luồng góp ý/phản hồi)

**Điều kiện tiên quyết:**
- Đăng nhập dưới tài khoản `nckh01`
- Đề tài số 2 tồn tại và ở trạng thái CHO_PNCKH_XEM_XET

**Các bước thực hiện:**
1. Truy cập trang `/de-tai/2`
2. Nhấp nút "Yêu cầu bổ sung hồ sơ"
3. Nhập ý kiến phản hồi: "Thiếu thuyết minh chi tiết về phương pháp nghiên cứu"
4. Thiết lập thời hạn nộp bổ sung: 7 ngày tính từ ngày hiện tại
5. Xác nhận gửi yêu cầu

**Kết quả mong đợi:**
- Hộp thoại hiển thị biểu mẫu điền thông tin góp ý bổ sung
- API phản hồi thành công: `POST /api/de-tai/2/yeu-cau-bo-sung` → trả về 200 OK
- Đề tài chuyển trạng thái sang `CHO_BO_SUNG_HO_SO`
- Một bản ghi mới được tạo trong bảng `feedback`
- Bản ghi log mới được lưu trong bảng `AuditLog` với sự kiện `YEU_CAU_BO_SUNG`
- Hiển thị thông báo thành công trên giao diện
- Tài khoản giảng viên (`gv01`) truy cập vào đề tài này sẽ thấy đầy đủ nội dung yêu cầu bổ sung

**API Endpoints:**
- `POST /api/de-tai/2/yeu-cau-bo-sung`
- `GET /api/de-tai/2/feedback` (dùng để xác thực)

**Các tệp tin / Component liên quan:**
- Backend: `DeTaiController.yeuCauBoSung()`
- Backend: `FeedbackService.createFeedback()`
- Frontend: `components/FeedbackModal.tsx`
- Frontend: `hooks/useSubmitFeedback.ts`

---

### TC-NCKH-004: Xem toàn bộ các Kỳ NCKH (Bao gồm cả kỳ đã đóng)
**Độ ưu tiên:** TRUNG BÌNH  
**Tính năng:** RT-02 (Quyền hạn xem kỳ của NCKH)

**Điều kiện tiên quyết:**
- Đăng nhập dưới tài khoản `nckh01`
- Có ít nhất 2 Kỳ NCKH trong DB (1 kỳ `DANG_MO`, 1 kỳ `DA_DONG`)

**Các bước thực hiện:**
1. Truy cập trang quản lý Kỳ NCKH tại `/ky-nckh`
2. Gọi API `GET /api/ky-nckh` (không truyền tham số lọc)
3. Xác minh toàn bộ các kỳ được trả về trong danh sách

**Kết quả mong đợi:**
- API trả về danh sách chứa cả 2 Kỳ NCKH (cả đang mở và đã đóng)
- Giao diện bảng hiển thị đầy đủ thông tin của cả 2 kỳ
- Cho phép nhấp xem chi tiết các kỳ đã đóng
- Vẫn có thể sử dụng bộ lọc trạng thái nếu cần

**API Endpoints:**
- `GET /api/ky-nckh`

**Các tệp tin / Component liên quan:**
- Backend: `KyNCKHController.getAllKyNCKH()`
- Frontend: `pages/KyNCKHList.tsx`

---

### TC-NCKH-005: Thành lập Tổ Phản biện (PNCKH_LAP_TO_PB)
**Độ ưu tiên:** CAO  
**Tính năng:** RT-01 (Xác thực ràng buộc nghiệp vụ)

**Điều kiện tiên quyết:**
- Đăng nhập dưới tài khoản `nckh01`
- Đề tài số 3 tồn tại và ở trạng thái DANG_PHAN_BIEN
- Chưa được thiết lập tổ phản biện trước đó

**Các bước thực hiện:**
1. Truy cập trang `/de-tai/3`
2. Nhấp nút "Lập tổ phản biện"
3. Chọn 2 phản biện viên từ danh sách (`pb01`, `pb02`)
4. Thiết lập hạn nộp phản biện: 14 ngày tính từ ngày hiện tại
5. Xác nhận thành lập tổ

**Kết quả mong đợi:**
- Hộp thoại xuất hiện cho phép lựa chọn các phản biện viên
- Nghiệp vụ bắt buộc phải chọn tối thiểu 2 phản biện viên (ràng buộc kiểm tra của hành động `PNCKH_LAP_TO_PB`)
- Hệ thống chặn và báo lỗi nếu chỉ chọn 1 phản biện viên hoặc không chọn ai
- Khi chọn đủ từ 2 phản biện viên trở lên: cuộc gọi API thành công
- Bản ghi mới được tạo trong bảng `to_phan_bien`
- Các bản ghi thành viên tương ứng được tạo trong bảng `thanh_vien_to_phan_bien` (2 bản ghi)
- Ghi nhận sự kiện `LAP_TO_PHAN_BIEN` vào bảng `AuditLog`
- Thông tin hội đồng/tổ phản biện hiển thị ngay trên trang chi tiết đề tài

**API Endpoints:**
- `POST /api/de-tai/3/lap-to-phan-bien`
- `GET /api/de-tai/3` (để xác minh thông tin tổ phản biện hiển thị trên trang)

**Các tệp tin / Component liên quan:**
- Backend: `DeTaiController.lapToPhanBien()`
- Backend: `ToPhanBienService.createCommittee()`
- Frontend: `components/CreateCommitteeModal.tsx`

---

## Kiểm thử cho vai trò TO_PHAN_BIEN (Tổ Phản biện)

### TC-PB-001: Chỉ xem được các đề tài được phân công phản biện
**Độ ưu tiên:** CAO  
**Tính năng:** RT-01, C2-1 (Phân quyền của Phản biện viên)

**Điều kiện tiên quyết:**
- Đăng nhập dưới tài khoản `pb01`
- Phản biện viên `pb01` được phân công phản biện cho các đề tài số 3 và 4
- Phản biện viên `pb01` KHÔNG được phân công cho các đề tài 1, 2, 5

**Các bước thực hiện:**
1. Truy cập trang danh sách đề tài `/de-tai`
2. Xác minh chỉ hiển thị các đề tài được phân công (đề tài số 3, 4)
3. Thử truy cập trực tiếp đề tài số 1 qua API: `GET /api/de-tai/1`
4. Xác minh lỗi trả về là 403 Forbidden

**Kết quả mong đợi:**
- Giao diện danh sách đề tài chỉ chứa các đề tài có ID: 3, 4
- Có quyền truy cập bình thường vào `/de-tai/3` và `/de-tai/4`
- Truy cập vào `/de-tai/1` trả về lỗi 403 Forbidden
- Trình duyệt tự động chuyển hướng người dùng sang trang lỗi `/403`
- Component EmptyState hiển thị thông báo lỗi "Không có quyền truy cập"

**API Endpoints:**
- `GET /api/de-tai` (kết quả trả về được lọc tự động dựa trên phân công phản biện)
- `GET /api/de-tai/1` (bắt buộc trả về lỗi 403)
- `GET /api/de-tai/3` (trả về mã 200 OK)

**Các tệp tin / Component liên quan:**
- Backend: `DeTaiService.getTopicsForPhanBien()`
- Backend: Các bộ lọc phân quyền bảo mật trong `DeTaiController`
- Frontend: `pages/DeTaiList.tsx`

---

### TC-PB-002: Nộp ý kiến phản biện (PHAN_BIEN_NOP)
**Độ ưu tiên:** CAO  
**Tính năng:** RT-01 (Nút hành động khả dụng của PB)

**Điều kiện tiên quyết:**
- Đăng nhập dưới tài khoản `pb01`
- Đề tài số 3 tồn tại và ở trạng thái DANG_PHAN_BIEN
- Tài khoản `pb01` là thành viên hội đồng phản biện đề tài này
- Trạng thái nộp ý kiến phản biện hiện tại là `CHUA_NOP`

**Các bước thực hiện:**
1. Truy cập trang chi tiết tại `/de-tai/3`
2. Gọi API `GET /api/de-tai/3/can-actions`
3. Nhấp chọn nút "Nộp phản biện" trên giao diện
4. Chọn kết quả đánh giá: "CHAP_NHAN" (Chấp nhận)
5. Nhập ý kiến nhận xét: "Đề tài có tính khả thi cao, phương pháp hợp lý"
6. Xác nhận nộp kết quả phản biện

**Kết quả mong đợi:**
- `canActions` trả về: `{"canSubmitReview": true}`
- Nút bấm "Nộp phản biện" hiển thị trên giao diện
- Hộp thoại điền kết quả phản biện xuất hiện
- Yêu cầu API thành công: `POST /api/de-tai/3/nop-phan-bien` → trả về 200 OK
- Bản ghi trong bảng `thanh_vien_to_phan_bien` cập nhật thuộc tính: `ket_qua` = 'CHAP_NHAN', `ngay_nop` = thời gian hiện tại
- Ghi nhận sự kiện `NOP_PHAN_BIEN` vào bảng `AuditLog`
- Hiển thị thông báo thành công trên giao diện
- Nút hành động đổi thành "Đã nộp phản biện" và chuyển sang dạng vô hiệu hóa (disabled)

**API Endpoints:**
- `GET /api/de-tai/3/can-actions`
- `POST /api/de-tai/3/nop-phan-bien`

**Các tệp tin / Component liên quan:**
- Backend: `DeTaiController.nopPhanBien()`
- Backend: `ToPhanBienService.submitReview()`
- Frontend: `components/PhanBienActionButtons.tsx`
- Frontend: `components/SubmitReviewModal.tsx`

---

### TC-PB-003: Không cho phép phản biện lại sau khi đã nộp kết quả
**Độ ưu tiên:** TRUNG BÌNH  
**Tính năng:** RT-01 (Tính nhất quán và duy nhất)

**Điều kiện tiên quyết:**
- Đăng nhập dưới tài khoản `pb01`
- Đề tài số 4 tồn tại và ở trạng thái DANG_PHAN_BIEN
- Thành viên `pb01` đã nộp phản biện trước đó (có kết quả trong DB: `ket_qua` = 'CAN_SUA')

**Các bước thực hiện:**
1. Truy cập trang chi tiết đề tài `/de-tai/4`
2. Gọi API kiểm tra `GET /api/de-tai/4/can-actions`
3. Quan sát các nút hành động khả dụng trên giao diện

**Kết quả mong đợi:**
- Kết quả từ API trả về: `{"canSubmitReview": false}`
- Giao diện hoàn toàn không hiển thị nút "Nộp phản biện"
- Chỉ hiển thị nhãn/trạng thái thông báo "Đã nộp phản biện"
- Phần ý kiến phản biện cũ hiển thị ở chế độ chỉ đọc (read-only)
- Không cho phép chỉnh sửa hoặc nộp lại phản biện khác

**API Endpoints:**
- `GET /api/de-tai/4/can-actions`

**Các tệp tin / Component liên quan:**
- Backend: Logic kiểm tra trong `CanActionService.evaluate()` (kiểm tra điều kiện `ket_qua` phải là `CHUA_NOP`)
- Frontend: `components/PhanBienActionButtons.tsx`

---

### TC-PB-004: Truy cập trái phép vào đề tài không được phân công phản biện
**Độ ưu tiên:** CAO  
**Tính năng:** A7-1, C2-1 (Xử lý lỗi 403 đối với Phản biện viên)

**Điều kiện tiên quyết:**
- Đăng nhập dưới tài khoản `pb01`
- Đề tài số 10 tồn tại trong hệ thống
- Phản biện viên `pb01` không có tên trong tổ phản biện đề tài số 10

**Các bước thực hiện:**
1. Nhập thủ công đường dẫn `/de-tai/10` trên thanh địa chỉ trình duyệt
2. Quan sát kết quả xử lý hiển thị trên trang

**Kết quả mong đợi:**
- API chặn quyền truy cập và trả về mã lỗi 403 Forbidden
- Frontend thực hiện chuyển hướng người dùng sang trang lỗi `/403`
- Component EmptyState hiển thị giao diện báo lỗi:
  - Biểu tượng: Khóa hoặc Từ chối truy cập
  - Tiêu đề: "Không có quyền truy cập"
  - Mô tả: "Bạn chỉ có thể xem đề tài mà bạn được phân công phản biện"
  - Nút hành động: "Quay lại danh sách đề tài"
- Không rò rỉ bất kỳ thông tin nào của đề tài số 10

**API Endpoints:**
- `GET /api/de-tai/10` (trả về lỗi 403)

**Các tệp tin / Component liên quan:**
- Backend: Kiểm tra phân quyền truy cập trong `DeTaiController.getDeTaiDetail()`
- Frontend: `pages/403.tsx`
- Frontend: `components/EmptyState.tsx`

---

## Kiểm thử các Tính năng Chung (Cross-Cutting)

### TC-CC-001: Component EmptyState hoạt động khi không có dữ liệu
**Độ ưu tiên:** TRUNG BÌNH  
**Tính năng:** A5-1 (Sử dụng component trạng thái trống)

**Điều kiện tiên quyết:**
- Đăng nhập dưới tài khoản `gv01`
- Tài khoản giảng viên này chưa tạo bất kỳ đề tài nào (danh sách đề tài trống)

**Các bước thực hiện:**
1. Truy cập trang danh sách đề tài `/de-tai`
2. Quan sát giao diện hiển thị khi danh sách rỗng

**Kết quả mong đợi:**
- Component `EmptyState` được dựng (render) thành công trên màn hình
- Biểu tượng hiển thị: Tệp tin hoặc Thư mục rỗng
- Tiêu đề: "Chưa có đề tài nào"
- Mô tả: "Bạn chưa tạo đề tài nào. Hãy bắt đầu bằng cách tạo đề tài mới."
- Nút bấm hành động chính: "Tạo đề tài mới" (liên kết tới trang `/de-tai/new`)
- Không hiển thị khung xương bảng dữ liệu (table/list skeleton) rỗng
- Giao diện căn giữa cân đối, thiết kế đẹp mắt

**API Endpoints:**
- `GET /api/de-tai` (trả về mảng rỗng `[]`)

**Các tệp tin / Component liên quan:**
- Frontend: `components/EmptyState.tsx`
- Frontend: `pages/DeTaiList.tsx`

---

### TC-CC-002: Component EmptyState hoạt động cho lỗi 404 Not Found
**Độ ưu tiên:** TRUNG BÌNH  
**Tính năng:** A5-1 (Component trạng thái trống xử lý lỗi)

**Điều kiện tiên quyết:**
- Đăng nhập dưới một tài khoản bất kỳ
- Đề tài có ID là 9999 hoàn toàn không tồn tại trong hệ thống

**Các bước thực hiện:**
1. Truy cập đường dẫn `/de-tai/9999`
2. Quan sát giao diện lỗi hiển thị trên trang

**Kết quả mong đợi:**
- API phản hồi mã trạng thái 404 Not Found
- Frontend hiển thị trang lỗi 404 (sử dụng component EmptyState)
- Biểu tượng hiển thị: Tìm kiếm hoặc Dấu chấm hỏi
- Tiêu đề: "Không tìm thấy đề tài"
- Mô tả: "Đề tài bạn đang tìm không tồn tại hoặc đã bị xóa"
- Nút bấm hành động: "Quay lại danh sách"
- Địa chỉ URL vẫn giữ nguyên là `/de-tai/9999` hoặc chuyển hướng sang `/404`

**API Endpoints:**
- `GET /api/de-tai/9999` (trả về lỗi 404)

**Các tệp tin / Component liên quan:**
- Backend: Trình xử lý ngoại lệ chung 404
- Frontend: `pages/404.tsx` hoặc xử lý EmptyState trực tiếp trên trang chi tiết
- Frontend: `components/EmptyState.tsx`

---

### TC-CC-003: Tính nhất quán của can-actions giữa các vai trò khác nhau
**Độ ưu tiên:** CAO  
**Tính năng:** RT-01 (Đồng bộ hóa API can-actions)

**Điều kiện tiên quyết:**
- Đề tài số 1 tồn tại và đang ở trạng thái CHO_PNCKH_XEM_XET
- Chuẩn bị sẵn thông tin đăng nhập của cả 3 vai trò khác nhau

**Các bước thực hiện:**
1. Đăng nhập dưới tài khoản `gv01` → Gọi API `GET /api/de-tai/1/can-actions`
2. Đăng xuất, đăng nhập dưới tài khoản `nckh01` → Gọi lại API trên
3. Đăng xuất, đăng nhập dưới tài khoản `pb01` → Gọi lại API trên (yêu cầu chặn quyền truy cập)
4. So sánh kết quả phản hồi giữa các vai trò

**Kết quả mong đợi:**

**Dưới vai trò GV (Chủ nhiệm đề tài):**
```json
{
  "canEdit": false,
  "canSubmit": false,
  "canWithdraw": true,
  "canApprove": false,
  "canReject": false,
  "canRequestSupplementary": false,
  "canSubmitReview": false
}
```

**Dưới vai trò NCKH:**
```json
{
  "canEdit": false,
  "canSubmit": false,
  "canWithdraw": false,
  "canApprove": true,
  "canReject": true,
  "canRequestSupplementary": true,
  "canSubmitReview": false
}
```

**Dưới vai trò PB (Không thuộc tổ phản biện đề tài 1):**
- Trả về mã lỗi 403 Forbidden

**API Endpoints:**
- `GET /api/de-tai/1/can-actions` (cho từng vai trò tương ứng)

**Các tệp tin / Component liên quan:**
- Backend: `CanActionService.evaluate()`
- Backend: Phân tách logic nghiệp vụ theo vai trò người dùng

---

### TC-CC-004: Ghi nhận nhật ký thay đổi trạng thái đề tài (Audit Log)
**Độ ưu tiên:** CAO  
**Tính năng:** RT-03, RT-04 (Theo dõi hoạt động qua AuditLog)

**Điều kiện tiên quyết:**
- Đăng nhập dưới tài khoản `gv01`
- Đề tài số 2 tồn tại và ở trạng thái CHO_BO_SUNG_HO_SO

**Các bước thực hiện:**
1. Thực hiện gọi API rút đề tài: `POST /api/de-tai/2/rut-de-tai`
2. Kiểm tra trực tiếp dữ liệu trong bảng của cơ sở dữ liệu: `SELECT * FROM audit_log WHERE de_tai_id = 2 ORDER BY created_at DESC LIMIT 1`
3. Xác minh các thông tin lưu trong bản ghi log mới nhất

**Kết quả mong đợi:**
- Bản ghi mới được tạo thành công trong bảng `audit_log`:
  - `de_tai_id` = 2
  - `event` = "RUT_DE_TAI"
  - `from_state` = "CHO_BO_SUNG_HO_SO"
  - `to_state` = "DA_RUT"
  - `actor_id` = ID người dùng của giảng viên gv01
  - `created_at` = Thời gian hiện tại ghi nhận thao tác
- Bản ghi log được lưu trữ thành công trước khi API phản hồi về client
- Không bị trùng lặp bản ghi log cho một hành động

**API Endpoints:**
- `POST /api/de-tai/2/rut-de-tai`
- (Xác thực trực tiếp qua câu lệnh SQL trên DB)

**Các tệp tin / Component liên quan:**
- Backend: `FsmService.transition()`
- Backend: `AuditLogService.logTransition()`
- Database: Cấu trúc bảng `audit_log`

---

### TC-CC-005: Hiệu năng phản hồi của hệ thống
**Độ ưu tiên:** TRUNG BÌNH  
**Tính năng:** Yêu cầu phi chức năng (Hiệu năng hệ thống)

**Điều kiện tiên quyết:**
- Hệ thống hoạt động trong điều kiện tải bình thường
- Cơ sở dữ liệu chứa tối thiểu khoảng 100 đề tài

**Các bước thực hiện:**
1. Gọi API lấy danh sách đề tài `GET /api/de-tai` liên tục 10 lần
2. Gọi API lấy chi tiết đề tài `GET /api/de-tai/1` liên tục 10 lần
3. Gọi API lấy danh sách hành động `GET /api/de-tai/1/can-actions` liên tục 10 lần
4. Đo lường thời gian phản hồi trung bình của hệ thống

**Kết quả mong đợi:**
- Endpoint danh sách: thời gian phản hồi trung bình < 500ms
- Endpoint chi tiết: thời gian phản hồi trung bình < 300ms
- Endpoint hành động khả dụng: thời gian phản hồi trung bình < 200ms
- Tỷ lệ phân vị thứ 95 (95th percentile) đạt dưới 1000ms cho tất cả các cuộc gọi API
- Không xảy ra lỗi nghẽn hoặc lỗi cổng 504 Gateway Timeout

**API Endpoints:**
- Tất cả các endpoint dạng GET phục vụ đọc dữ liệu

**Các tệp tin / Component liên quan:**
- Backend: Các chỉ mục (indexes) và tối ưu hóa câu truy vấn cơ sở dữ liệu
- Backend: Sử dụng `@EntityGraph` để giải quyết vấn đề truy vấn N+1 (N+1 queries)

---

### TC-CC-006: Truy cập đồng thời (Tránh tranh chấp dữ liệu)
**Độ ưu tiên:** TRUNG BÌNH  
**Tính năng:** Bảo toàn tính toàn vẹn dữ liệu

**Điều kiện tiên quyết:**
- Đề tài số 5 tồn tại và ở trạng thái CHO_PNCKH_XEM_XET
- Chuẩn bị 2 tài khoản NCKH khác nhau: `nckh01` và `nckh02`

**Các bước thực hiện:**
1. Đăng nhập đồng thời `nckh01` và `nckh02` trên 2 trình duyệt/phiên làm việc độc lập
2. Cả hai người dùng cùng bấm nút "Duyệt đề tài" cùng một lúc
3. Quan sát kết quả xử lý lỗi và phản hồi từ hệ thống

**Kết quả mong đợi:**
- Chỉ có DUY NHẤT một yêu cầu phê duyệt thành công
- Yêu cầu thứ hai gửi lên sau đó sẽ thất bại và nhận được thông báo lỗi phù hợp
- Không xảy ra hiện tượng chuyển đổi trạng thái trùng lặp trong DB
- Không tạo ra các dòng nhật ký AuditLog bị chồng chéo
- Hệ thống sử dụng khóa lạc quan (optimistic locking) hoặc cơ chế kiểm soát giao dịch để ngăn chặn lỗi đồng thời
- Người dùng bị từ chối nhận được thông báo lỗi rõ ràng

**API Endpoints:**
- `POST /api/de-tai/5/duyet` (gọi đồng thời từ 2 nguồn khác nhau)

**Các tệp tin / Component liên quan:**
- Backend: Khóa dữ liệu lạc quan sử dụng `@Version`
- Backend: Mức độ cô lập giao dịch cơ sở dữ liệu (Transaction isolation levels)
- Backend: Cơ chế khóa chuyển đổi trạng thái trong `FsmService`

---

## Bảng Tổng hợp Độ bao phủ Kiểm thử

| Tính năng | Tổng số kịch bản | Độ ưu tiên CAO | Độ ưu tiên TRUNG BÌNH | Trạng thái |
|-----------|------------------|----------------|-----------------------|------------|
| RT-01 (can-actions) | 7 | 5 | 2 | Chưa thực hiện |
| RT-02 (lọc kỳ NCKH) | 2 | 1 | 1 | Chưa thực hiện |
| RT-03 (rút đề tài) | 3 | 2 | 1 | Chưa thực hiện |
| A7-1 (xử lý lỗi 403) | 3 | 3 | 0 | Chưa thực hiện |
| B5-1 (phản hồi chi tiết) | 1 | 1 | 0 | Chưa thực hiện |
| RT-04 (feedback) | 1 | 1 | 0 | Chưa thực hiện |
| A5-1 (EmptyState) | 2 | 0 | 2 | Chưa thực hiện |
| C2-1 (quyền phản biện) | 2 | 2 | 0 | Chưa thực hiện |
| Các tính năng chung | 6 | 2 | 4 | Chưa thực hiện |
| **TỔNG CỘNG** | **27** | **17** | **10** | **Hoàn thành 0%** |

---

## Lưu ý dành cho Kiểm thử viên

1. **Khởi tạo lại cơ sở dữ liệu:** Luôn chạy tệp `test-data.sql` trước mỗi chu kỳ kiểm thử mới để đảm bảo dữ liệu luôn ở trạng thái chuẩn nhất.
2. **Tab Console trình duyệt:** Luôn mở tab Console trong Browser DevTools để kịp thời phát hiện lỗi JavaScript hoặc lỗi cấu trúc UI.
3. **Tab Network trình duyệt:** Kiểm tra kỹ cấu trúc payload yêu cầu và phản hồi gửi lên để chắc chắn khớp với đặc tả kỹ thuật.
4. **Ảnh chụp màn hình:** Chụp lại các biến thể giao diện EmptyState và trang lỗi 403 phục vụ cho công tác kiểm thử chất lượng và lưu trữ tài liệu.
5. **Tệp tin log hệ thống:** Kiểm tra log file phía Backend để lấy thông tin chi tiết về lỗi (stack trace) khi có sự cố không mong muốn xảy ra.

---

## Danh sách Công việc Thực thi Kiểm thử

- [ ] Chạy tệp `test-data.sql` để thiết lập dữ liệu sạch cho DB
- [ ] Xác minh 3 tài khoản thử nghiệm đăng nhập thành công vào hệ thống
- [ ] Thực hiện kiểm tra toàn bộ các kịch bản kiểm thử có độ ưu tiên CAO trước
- [ ] Chạy script `api-test.sh` để kiểm thử tự động toàn bộ API
- [ ] Thực hiện kiểm thử thủ công qua tệp `MANUAL_TEST_CHECKLIST.md`
- [ ] Ghi lại các lỗi phát sinh kèm theo Mã TC tương ứng
- [ ] Chụp lại ảnh giao diện của tất cả các trang EmptyState lỗi
- [ ] Kiểm tra bảng `audit_log` trong DB xem trạng thái ghi nhận nhật ký sau khi thao tác
- [ ] Thử nghiệm kịch bản truy cập đồng thời
- [ ] Ghi nhận và lưu giữ số liệu đo lường hiệu năng của hệ thống
