## Mô tả

> Mô tả ngắn những gì PR này thay đổi và tại sao cần thay đổi đó.

---

## Loại thay đổi

- [ ] `feat` — Tính năng mới
- [ ] `fix` — Sửa lỗi
- [ ] `refactor` — Tái cấu trúc code, không thêm tính năng hoặc sửa lỗi
- [ ] `docs` — Tài liệu
- [ ] `chore` — Cập nhật thư viện, cấu hình build
- [ ] `style` — Định dạng code, không thay đổi logic
- [ ] `test` — Thêm hoặc sửa test

---

## Link Trello

> Dán link card Trello tương ứng tại đây.

---

## Cách kiểm tra

> Mô tả các bước để reviewer có thể tự chạy và xác nhận tính năng/fix hoạt động đúng.

1.
2.
3.

---

## Checklist (Definition of Done)

**Chức năng:**
- [ ] Tất cả checkbox trong mục "Acceptance test" của Story đã pass
- [ ] Không có lỗi hoặc exception khi chạy trên môi trường local

**Code:**
- [ ] Tuân thủ quy chuẩn đặt tên và cấu trúc trong `00-code-standards.md`
- [ ] Không còn `console.log`, code debug, hoặc đoạn code bị comment out thừa
- [ ] Không có file `.env` hoặc thông tin nhạy cảm nào nằm trong commit

**Git:**
- [ ] Commit message đúng format `type(scope): mô tả`
- [ ] Đã pull `develop` mới nhất vào nhánh trước khi push lần cuối
- [ ] Tên nhánh đúng quy ước (`feature/CARD-XX-ten-task`)

---

## Ghi chú thêm

> Ghi bất kỳ điểm chưa chắc chắn, trade-off kỹ thuật, hoặc điều muốn reviewer chú ý đặc biệt.
