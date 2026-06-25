# 📋 QUY ĐỊNH GIT WORKFLOW - DỰ ÁN QLNCKH
**Hiệu lực từ:** 25/06/2026  
**Ban hành bởi:** Tech Lead  
**Phiên bản:** 1.0

---

## 🎯 NGUYÊN TẮC VÀNG: "MỘT NGUỒN CHÂN LÝ"

> **Nhánh `develop` là nguồn chân lý duy nhất của toàn team.**  
> Mọi công việc đều bắt đầu từ đây và kết thúc ở đây.

---

## 🗂️ CẤU TRÚC NHÁNH

| Nhánh | Mục đích | Ai được push? |
|-------|----------|--------------|
| `main` | Production-ready code (chốt từng milestone) | **Chỉ Tech Lead** |
| `develop` | Nhánh gốc tích hợp, luôn Build Success | **Qua PR - ai cũng được** |
| `feature/<ten-tinh-nang>` | Tính năng đang phát triển (max 2 ngày) | Thành viên phụ trách |

> ⛔ **CẤM** sử dụng các nhánh cũ: `MemberB`, `feature/phase1-be-fsm`, `feature/complete-sop-c-frontend`, `integration/phase1-merged`. Các nhánh này sẽ bị xóa sau 7 ngày.

---

## 🔄 QUY TRÌNH LÀM VIỆC HÀNG NGÀY

### BƯỚC 1: Bắt đầu task mới

```bash
# Luôn cập nhật develop mới nhất trước
git checkout develop
git pull origin develop

# Tạo nhánh mới từ develop (đặt tên theo task)
git checkout -b feature/ten-task-cua-ban
# Ví dụ: feature/d-ui-dashboard-pb, feature/b-unit-test-fsm
```

### BƯỚC 2: Làm việc & Commit thường xuyên

```bash
# Commit nhỏ, thường xuyên (ít nhất 1 lần/ngày)
git add .
git commit -m "feat: mo ta ngan gon nhung ro rang"

# Quy tắc đặt tên commit (Conventional Commits):
# feat: tính năng mới
# fix: sửa lỗi
# refactor: cải thiện code không thêm feature
# test: thêm unit test
# docs: cập nhật tài liệu
```

### BƯỚC 3: Tạo Pull Request (sau tối đa 2 ngày)

```bash
# Push nhánh lên remote
git push origin feature/ten-task-cua-ban
```

Sau đó vào GitHub → **New Pull Request** → base: `develop` ← compare: `feature/ten-task-cua-ban`

> **⚠️ Nếu để nhánh quá 2 ngày mà không PR → Tech Lead sẽ yêu cầu rebase.**

---

## ✅ CHECKLIST TRƯỚC KHI TẠO PULL REQUEST

- [ ] Code đã chạy ở máy local (Frontend: `npm run dev`, Backend: `mvn compile`)
- [ ] Không để `console.log()` thừa / code debug
- [ ] Tên nhánh, commit message đúng quy định
- [ ] Đã `git pull origin develop` và giải quyết mọi conflict

---

## 🚦 QUY TẮC REVIEW & MERGE

1. **PR phải được ít nhất 1 người khác review** trước khi merge
2. **Không được self-merge** (tự merge PR của mình)
3. **Tech Lead** là người có quyết định cuối nếu có tranh cãi về conflict
4. Sau khi merge, **xóa nhánh feature** để giữ repo sạch

---

## ⛔ NHỮNG ĐIỀU TUYỆT ĐỐI CẤM

| Hành động cấm | Lý do |
|---------------|-------|
| Commit thẳng vào `develop` hoặc `main` | Phá vỡ nguồn chân lý |
| Để nhánh feature sống quá 2 ngày | Nguy cơ Integration Hell |
| Merge mà không test build | Đưa code lỗi vào develop |
| Dùng lại các nhánh cũ đã bị deprecated | Gây nhầm lẫn lịch sử commit |

---

## 🆘 XỬ LÝ KHI CÓ CONFLICT

```bash
# 1. Cập nhật develop mới nhất vào nhánh của bạn
git checkout feature/ten-nhanh-cua-ban
git merge develop

# 2. Mở file conflict, tìm ký hiệu <<<<<<< và chỉnh sửa
# 3. Sau khi fix xong
git add .
git commit -m "fix: resolve merge conflict with develop"
git push origin feature/ten-nhanh-cua-ban
```

> 💡 **Tip:** Conflict nhỏ nếu merge thường xuyên (mỗi 1-2 ngày). Conflict khổng lồ nếu để 1-2 tuần.

---

## 📞 LIÊN HỆ KHI CẦN GIÚP ĐỠ

- **Git conflict phức tạp:** Liên hệ ngay Tech Lead qua nhóm chat
- **Build fail sau merge:** Tạo issue trên GitHub, tag `bug` + `high-priority`
- **Câu hỏi về workflow:** Xem file này hoặc hỏi trong nhóm

---

*Tài liệu này được cập nhật bởi Thư ký dự án. Mọi thay đổi quy trình cần được Tech Lead phê duyệt.*
