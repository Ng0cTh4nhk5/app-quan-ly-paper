# RGMS Project Knowledge Base cho Frontend

Tài liệu này chưng cất phần BA trong `ba-docs` thành định hướng triển khai FE. Khi sửa hoặc xây mới giao diện, ưu tiên bám theo tài liệu này trước, rồi đối chiếu lại BA gốc khi cần chi tiết nghiệp vụ.

## 1. Bản chất dự án

RGMS là hệ thống quản lý đề tài nghiên cứu khoa học cấp cơ sở cho một trường đại học. Mục tiêu là số hóa quy trình hiện đang vận hành thủ công qua email và hồ sơ rời rạc.

Luồng nghiệp vụ chính:

```text
Đề xuất -> Phản biện -> Ký hợp đồng -> Thực hiện -> Nghiệm thu -> Quyết toán -> Lưu trữ
```

FE không chỉ hiển thị dữ liệu. FE phải giúp người dùng hiểu ngay:

- Đề tài đang ở trạng thái nào.
- Vai trò hiện tại được phép làm gì.
- Hành động nào bị khóa vì sai trạng thái hoặc sai quyền.
- Deadline, feedback, file và lịch sử xử lý nằm ở đâu.

## 2. Actor và góc nhìn FE

| Actor | Vai trò nghiệp vụ | Trọng tâm giao diện |
|---|---|---|
| `GIANG_VIEN` | Chủ nhiệm đề tài, tạo/nộp/bổ sung hồ sơ, phản hồi hợp đồng, nộp báo cáo, quyết toán | Dashboard cá nhân, danh sách đề tài của tôi, chi tiết đề tài, form tạo đề tài, upload file, phản hồi feedback |
| `NCKH` | Tiếp nhận, sơ thẩm, lập tổ phản biện, xét kết quả phản biện, soạn hợp đồng, lập hội đồng nghiệm thu, thanh lý | Inbox cần xử lý, hàng đợi theo trạng thái, chi tiết hồ sơ trung tâm, action theo guard |
| `TO_PHAN_BIEN` | Đánh giá tính khả thi trước nghiệm thu | Danh sách đề tài được giao, form phản biện, nộp kết quả `CHAP_NHAN` / `CAN_SUA` / `TU_CHOI` |
| Hội đồng nghiệm thu tài khoản tạm | Đánh giá kết quả thực hiện đề tài | Dashboard hội đồng, xem hồ sơ read-only, nhập nhận xét, kết luận nghiệm thu |
| `KE_TOAN` | Duyệt tạm ứng, xử lý quyết toán, thu hồi tạm ứng | Hàng đợi tài chính, duyệt/từ chối, đối chiếu chi phí, trạng thái thu hồi |
| `ADMIN` | Người dùng, template, cấu hình, tài khoản tạm, audit log | Bảng quản trị, form cấu hình, audit log read-only |
| `LANH_DAO` | Theo dõi, báo cáo, thống kê | Dashboard KPI read-only, lọc/xuất báo cáo |

Giai đoạn source hiện tại tập trung rõ vào `GIANG_VIEN`, `NCKH`, `TO_PHAN_BIEN`.

## 3. State machine cốt lõi

`DeTai.status` là trục điều khiển toàn bộ UI. Không hard-code theo text tiếng Việt nếu có thể; dùng enum chuẩn.

### Trạng thái trung gian

| Enum | Ý nghĩa FE |
|---|---|
| `Draft` | GV đang soạn bản nháp, có thể chỉnh sửa và gửi hồ sơ |
| `Cho_PNCKH_Xem_Xet` | GV đã gửi, NCKH cần tiếp nhận |
| `Dang_Xem_Xet_Boi_PNCKH` | NCKH đang sơ thẩm, có thể yêu cầu bổ sung, từ chối, hoặc lập tổ phản biện |
| `Cho_Bo_Sung_Ho_So` | GV cần bổ sung hồ sơ theo feedback và deadline |
| `Dang_Phan_Bien` | Tổ phản biện đang đánh giá, NCKH chờ/tổng hợp kết quả |
| `Cho_Chinh_Sua_Thuyet_Minh` | GV sửa thuyết minh theo phản biện |
| `Dang_Lap_Hop_Dong` | NCKH soạn hợp đồng, GV xem/phản hồi/xác nhận |
| `Dang_Thuc_Hien` | Đề tài đang thực hiện; có sub-flow tạm ứng, gia hạn, nộp báo cáo nghiệm thu |
| `Cho_Nghiem_Thu` | Đã lập hội đồng, chờ phiên nghiệm thu |
| `Cho_Chinh_Sua_Sau_Nghiem_Thu` | GV sửa sau nghiệm thu; Chủ tịch + Phản biện duyệt lại |
| `Cho_Quyet_Toan` | Mở giao diện quyết toán cho GV |
| `Dang_Quyet_Toan` | Kế toán đang xử lý quyết toán |
| `Cho_Hoan_Tra_Tam_Ung` | Kế toán/GV xử lý hoàn trả/thu hồi tạm ứng; phải đọc thêm `nguon_cho_hoan_tra` |

### Trạng thái kết thúc

| Enum | Ý nghĩa |
|---|---|
| `Hoan_Tat` | Hoàn tất thành công |
| `Bi_Tu_Choi` | Bị từ chối trước khi thực hiện |
| `Da_Huy` | Bị hủy khi đang thực hiện |
| `Khong_Nghiem_Thu` | Hội đồng không nghiệm thu |
| `Da_Rut` | GV rút đề tài |
| `Bi_Treo` | Quá hạn/không phản hồi nhiều lần |
| `Bi_Thu_Hoi` | Vi phạm học thuật, đóng thủ công |

Terminal state không có action nghiệp vụ tiếp theo, chỉ nên cho xem chi tiết, tài liệu, lịch sử.

## 4. Quy tắc hiển thị action

Mọi nút chính trên FE phải đi qua 2 lớp kiểm tra:

1. Role hiện tại có quyền không.
2. `DeTai.status` có đúng guard không.

Ví dụ quan trọng:

| Action | Actor | Guard |
|---|---|---|
| Tạo đề tài | `GIANG_VIEN` | Đã đăng nhập, kỳ NCKH đang mở |
| Gửi hồ sơ | `GIANG_VIEN` | `Draft`, đủ thuyết minh + danh sách phản biện đề xuất + biểu mẫu |
| Bổ sung hồ sơ | `GIANG_VIEN` | `Cho_Bo_Sung_Ho_So`, còn deadline |
| Nộp lại thuyết minh | `GIANG_VIEN` | `Cho_Chinh_Sua_Thuyet_Minh`, còn deadline |
| Xem/phản hồi hợp đồng | `GIANG_VIEN` | `Dang_Lap_Hop_Dong` |
| Tạm ứng | `GIANG_VIEN` | `Dang_Thuc_Hien`, tổng tạm ứng không vượt `max_advance_rate * kinh_phi` |
| Gia hạn | `GIANG_VIEN` | `Dang_Thuc_Hien` |
| Nộp báo cáo nghiệm thu | `GIANG_VIEN` | `Dang_Thuc_Hien` |
| Nộp quyết toán | `GIANG_VIEN` | `Cho_Quyet_Toan` |
| Tiếp nhận hồ sơ | `NCKH` | `Cho_PNCKH_Xem_Xet` |
| Yêu cầu bổ sung | `NCKH` | `Dang_Xem_Xet_Boi_PNCKH` |
| Lập tổ phản biện | `NCKH` | `Dang_Xem_Xet_Boi_PNCKH`, hồ sơ hợp lệ |
| Xét duyệt phản biện | `NCKH` | `Dang_Phan_Bien` |
| Soạn/xác nhận hợp đồng | `NCKH` | `Dang_Lap_Hop_Dong` |
| Nộp kết quả phản biện | `TO_PHAN_BIEN` | `Dang_Phan_Bien`, còn deadline |

Nếu action bị khóa, FE nên hiển thị lý do ngắn gọn thay vì chỉ ẩn im lặng trong các màn chi tiết.

## 5. Màn hình chuẩn cần hướng tới

BA mô tả 32 màn hình/module đầy đủ. Với FE hiện tại, ưu tiên các màn đã có route:

| Route hiện tại | Vai trò | Ý nghĩa BA |
|---|---|---|
| `/login` | Tất cả | Đăng nhập và điều hướng theo role |
| `/gv/dashboard` | GV | Dashboard Chủ nhiệm đề tài |
| `/gv/de-tai` | GV | Danh sách đề tài cá nhân |
| `/gv/de-tai/tao-moi` | GV | Form tạo đề tài |
| `/gv/de-tai/:id` | GV | Chi tiết đề tài, timeline, file, feedback |
| `/gv/de-tai/:id/bo-sung` | GV | Bổ sung hồ sơ theo feedback |
| `/gv/de-tai/:id/hop-dong` | GV | Xem và phản hồi hợp đồng |
| `/nckh/dashboard` | NCKH | Tổng quan hồ sơ cần xử lý |
| `/nckh/inbox` | NCKH | Hồ sơ mới chờ tiếp nhận |
| `/nckh/dang-xu-ly` | NCKH | Danh sách đang xử lý |
| `/nckh/de-tai/:id` | NCKH | Trung tâm xử lý hồ sơ |
| `/nckh/de-tai/:id/hop-dong` | NCKH | Soạn hợp đồng |
| `/pb/de-tai` | Tổ phản biện | Danh sách đề tài được giao |
| `/pb/de-tai/:id` | Tổ phản biện | Form đánh giá phản biện |

Khi thêm màn mới, ưu tiên đồng bộ với danh sách S-01 đến S-32 trong BA, đặc biệt: tài chính, nghiệm thu, admin, lãnh đạo, notification.

## 6. Dữ liệu cốt lõi FE cần hiểu

`DeTai` là entity trung tâm. Các trường quan trọng với FE:

- `id`, `ma_so` / `maSo`
- `ten_de_tai` / `tenDeTai`
- `status` / `trangThai`
- `ngay_tao`, `ngay_bat_dau`, `ngay_ket_thuc`
- `kinh_phi`
- `tong_tam_ung_da_giai_ngan`
- `chu_nhiem_id`, `don_vi_id`, `ky_nckh_id`
- `nguon_cho_hoan_tra` khi ở `Cho_Hoan_Tra_Tam_Ung`

Các entity liên quan hay xuất hiện ở UI:

| Entity | Dùng ở FE |
|---|---|
| `TaiLieu` | Upload/download/preview file, versioning theo loại tài liệu |
| `Feedback` | Hiển thị yêu cầu bổ sung/sửa, deadline phản hồi |
| `AuditLog` | Timeline lịch sử trạng thái và hành động |
| `HopDong` | Chi tiết hợp đồng, file scan, tỷ lệ tạm ứng |
| `ToPhanBien`, `ThanhVienToPhanBien` | Lập tổ, form phản biện, tổng hợp kết quả |
| `HoiDongNghiemThu`, `ThanhVienHoiDong` | Giai đoạn nghiệm thu sau này |
| `TamUng`, `QuyetToan`, `ThuHoiTamUng` | Giai đoạn tài chính sau này |
| `BieuMauTemplate` | Kho biểu mẫu và sinh file tự động |

Lưu ý khác biệt naming: BA dùng enum dạng `Draft`, `Cho_PNCKH_Xem_Xet`; một số code/mock cũ có thể dùng uppercase như `DRAFT`, `CHO_PNCKH_XEM_XET`. Khi chỉnh code, nên chuẩn hóa mapping ở một nơi duy nhất trong component/helper trạng thái.

## 7. File và tài liệu

Quy tắc upload/download:

- Định dạng cho phép: `pdf`, `docx`, `xlsx`, `jpg`, `png`.
- Dung lượng tối đa mỗi file: 20 MB.
- `pdf`, `jpg`, `png` có thể preview trực tiếp.
- `docx`, `xlsx` chỉ download.
- Nếu upload cùng loại tài liệu cho cùng đề tài, tăng `phien_ban`; phiên bản cũ vẫn giữ để audit.
- Không tự sinh dữ liệu giả trong template nếu placeholder thiếu dữ liệu.

FE nên luôn có loading, empty, error, success toast cho upload và submit form.

## 8. Nguyên tắc UI/UX

Hướng thiết kế từ BA:

- App là công cụ nội bộ kiểu SaaS, ưu tiên rõ ràng, dày thông tin vừa đủ, không làm landing page.
- Màu nền chính: `#F7F8FA`; card/nền nổi: `#FFFFFF`; accent: `#6366F1`.
- Trạng thái thành công: `#10B981`; cảnh báo: `#F59E0B`; nguy hiểm/từ chối: `#EF4444`.
- Font BA đề xuất: Be Vietnam Pro; code hiện có cần kiểm tra xem đã import đầy đủ chưa.
- Desktop là trọng tâm; tablet cần responsive; mobile ngoài phạm vi chính.
- Mỗi màn cần trả lời nhanh: "Việc gì đang chờ tôi?" và "Tôi có thể bấm gì tiếp?"
- Lãnh đạo luôn read-only.
- Tài khoản tạm chỉ thấy đề tài được phân công, không thấy dữ liệu tài chính.

## 9. Kiến trúc FE hiện tại

Source hiện đang dùng:

- Vue 3 + Vite
- Pinia
- Vue Router
- Axios
- Lucide Vue
- Mock data trong `src/mock/db.js`

Các thư mục chính:

```text
source-code/frontend/src
├── api/axios.js
├── components/
├── composables/
├── layouts/
├── pages/
│   ├── gv/
│   ├── nckh/
│   └── pb/
├── router/index.js
├── stores/
└── style.css / design-tokens.css
```

Khi phát triển tiếp:

- Tái dùng `StatusBadge.vue` và thống nhất map trạng thái.
- Mọi API call nên đi qua `src/api/axios.js`.
- Role guard nằm trong `router/index.js`; không bỏ qua guard ở UI.
- Store nên giữ loading/error và action nghiệp vụ rõ ràng, ví dụ `guiHoSo`, `tiepNhan`, `yeuCauBoSung`, `lapToPhanBien`.
- Component chi tiết đề tài nên là trung tâm: header, status badge, timeline, tabs tài liệu/feedback/hợp đồng/tài chính, action panel theo role.

## 10. Phase 1 ưu tiên xây dựng

Phase 1: Core Lifecycle từ khởi tạo đến ký hợp đồng.

Ưu tiên FE:

1. Login và điều hướng đúng role.
2. GV dashboard, danh sách đề tài, tạo đề tài, chi tiết đề tài.
3. GV upload thuyết minh, gửi hồ sơ, xem feedback, bổ sung hồ sơ.
4. NCKH inbox, tiếp nhận hồ sơ, yêu cầu bổ sung, lập tổ phản biện.
5. Tổ phản biện xem đề tài được giao và nộp đánh giá.
6. NCKH tổng hợp/xét duyệt phản biện.
7. NCKH soạn hợp đồng, GV xem/phản hồi/xác nhận, NCKH xác nhận ký để chuyển `Dang_Thuc_Hien`.

Chưa ưu tiên sâu nếu scope chỉ là Phase 1:

- Nghiệm thu đầy đủ.
- Quyết toán, thu hồi tạm ứng.
- Admin cấu hình.
- Dashboard lãnh đạo hoàn chỉnh.

## 11. Checklist khi sửa hoặc thêm một màn FE

Trước khi code:

- Xác định actor chính.
- Xác định trạng thái `DeTai.status` màn này phục vụ.
- Xác định action nào được phép và action nào read-only.
- Xem dữ liệu lấy từ entity nào.

Khi code:

- Có loading state.
- Có empty state.
- Có error/success toast.
- Có form validation phía FE.
- Có guard hiển thị nút theo role + status.
- Không để người dùng đoán deadline hoặc bước tiếp theo.

Trước khi bàn giao:

- Test ít nhất một happy path.
- Test role sai bị chặn.
- Test trạng thái sai không hiện action nguy hiểm.
- Test text tiếng Việt không vỡ layout.
- Test bảng/danh sách khi dữ liệu rỗng.

## 12. Nguồn BA đã đọc

Các file nền tảng chính:

- `ba-docs/reality/core-business.md`
- `ba-docs/reality/analysis/state-machine.md`
- `ba-docs/reality/analysis/use-case-permission.md`
- `ba-docs/reality/analysis/er-diagram.md`
- `ba-docs/reality/analysis/srs.md`
- `ba-docs/reality/analysis/srs/02-actor-requirements.md`
- `ba-docs/reality/analysis/srs/03-supporting-requirements.md`
- `ba-docs/uiux-design/ui-design-spec/README.md`
- `ba-docs/plans/phase1/sop-member-c.md`
- `ba-docs/plans/phase1/sop-member-d.md`

Các nhóm tài liệu còn lại trong `ba-docs/specs`, `ba-docs/plans`, `ba-docs/uiux-design` dùng để đối chiếu chi tiết từng giai đoạn khi triển khai sâu hơn.
