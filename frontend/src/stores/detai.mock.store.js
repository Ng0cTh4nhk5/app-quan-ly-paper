/**
 * Mock Detai Store — gọi mock/db.js thay vì API thật.
 * Dùng cho frontend mockup (không cần backend).
 * Khi tích hợp backend: thay import bằng api/axios.
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { useAuthStore } from '@/stores/auth.store'
import * as db from '@/mock/db.js'

export const useDetaiMockStore = defineStore('detai-mock', () => {
  const authStore  = useAuthStore()
  const loading    = ref(false)
  const error      = ref(null)

  // ── Computed list theo role ──────────────────────────
  const danhSach = computed(() => {
    const u = authStore.user
    if (!u) return []
    return db.getDeTaiByRole(u.role, u.id)
  })

  const chiTiet = ref(null)

  function layChiTiet(id) {
    chiTiet.value = db.getDeTaiById(parseInt(id))
    return chiTiet.value
  }

  function layDanhSach() {
    const u = authStore.user
    if (!u) return []
    return db.getDeTaiByRole(u.role, u.id)
  }

  // ── GV Actions ───────────────────────────────────────
  function taoDeTai(payload) {
    const u = authStore.user
    const dt = db.createDeTai(payload, u)
    if (payload.phanBienDeXuat?.length) {
      payload.phanBienDeXuat.forEach(pb => db.addPhanBienDeXuat(dt.id, pb))
    }
    return dt
  }

  function uploadFile(detaiId, fileOrMeta, loai = 'THUYET_MINH') {
    return db.addTaiLieu(detaiId, fileOrMeta, loai)
  }

  function removeFile(detaiId, taiLieuId) {
    db.removeTaiLieu(detaiId, taiLieuId)
    if (chiTiet.value?.id === detaiId) chiTiet.value = db.getDeTaiById(detaiId)
  }

  function guiHoSo(detaiId) {
    const u = authStore.user
    const check = db.checkCanSubmit(detaiId)
    if (!check.ok) throw new Error('Hồ sơ chưa đủ điều kiện nộp.')
    db.markFeedbackDone(detaiId)
    const dt = db.updateTrangThai(detaiId, 'CHO_PNCKH_XEM_XET', u.hoTen, 'Gửi hồ sơ lên P.NCKH')
    if (chiTiet.value?.id === detaiId) chiTiet.value = db.getDeTaiById(detaiId)
    return dt
  }

  function boSungHoSo(detaiId) {
    const u = authStore.user
    db.markFeedbackDone(detaiId)
    const dt = db.updateTrangThai(detaiId, 'CHO_PNCKH_XEM_XET', u.hoTen, 'Bổ sung hồ sơ & nộp lại')
    if (chiTiet.value?.id === detaiId) chiTiet.value = db.getDeTaiById(detaiId)
    return dt
  }

  function gvDongYHopDong(detaiId) {
    const u = authStore.user
    db.gvDongYHopDong(detaiId, u.hoTen)
    if (chiTiet.value?.id === detaiId) chiTiet.value = db.getDeTaiById(detaiId)
  }

  // ── NCKH Actions ─────────────────────────────────────
  function tiepNhan(detaiId) {
    const u = authStore.user
    return db.updateTrangThai(detaiId, 'DANG_XEM_XET_BOI_PNCKH', u.hoTen, 'P.NCKH tiếp nhận hồ sơ')
  }

  function yeuCauBoSung(detaiId, { noiDung, deadline }) {
    const u = authStore.user
    db.taoFeedback(detaiId, { noiDung, deadline }, u.hoTen)
    db.updateTrangThai(detaiId, 'CHO_BO_SUNG_HO_SO', u.hoTen, 'Yêu cầu bổ sung hồ sơ')
    if (chiTiet.value?.id === detaiId) chiTiet.value = db.getDeTaiById(detaiId)
  }

  function tuChoiHoSo(detaiId, lyDo) {
    if (!lyDo?.trim()) throw new Error('Vui lòng nhập lý do từ chối. (BR-14)')
    const u = authStore.user
    return db.updateTrangThai(detaiId, 'BI_TU_CHOI', u.hoTen, `Từ chối: ${lyDo}`)
  }

  function lapToPhanBien(detaiId, { thanhVien, deadline }) {
    const u = authStore.user
    db.lapToPhanBien(detaiId, { thanhVien, deadline }, u.hoTen)
    if (chiTiet.value?.id === detaiId) chiTiet.value = db.getDeTaiById(detaiId)
  }

  function chuyenLapHopDong(detaiId, payload) {
    const u = authStore.user
    db.taoHopDong(detaiId, payload, u.hoTen)
    db.updateTrangThai(detaiId, 'DANG_LAP_HOP_DONG', u.hoTen, 'Chuyển sang giai đoạn lập hợp đồng')
    if (chiTiet.value?.id === detaiId) chiTiet.value = db.getDeTaiById(detaiId)
  }

  function xacNhanKyHopDong(detaiId, { ngayKy, fileScanUrl }) {
    const u = authStore.user
    db.xacNhanKyHopDong(detaiId, { ngayKy, fileScanUrl }, u.hoTen)
    if (chiTiet.value?.id === detaiId) chiTiet.value = db.getDeTaiById(detaiId)
  }

  // ── PB Actions ───────────────────────────────────────
  function nopKetQuaPhanBien(detaiId, { ketQua, nhanXet }) {
    const u = authStore.user
    db.nopKetQuaPhanBien(detaiId, u.id, { ketQua, nhanXet })
    // Nếu tất cả thành viên tổ phản biện đã nộp → chuyển sang CHO_PHAN_QUYET
    const dt = db.getDeTaiById(detaiId)
    const allDone = dt?.toPhanBien?.length > 0 &&
      dt.toPhanBien.every(tv => tv.ketQua !== null)
    if (allDone) {
      db.updateTrangThai(detaiId, 'CHO_PHAN_QUYET', u.hoTen, 'Tổ phản biện đã hoàn thành')
    } else {
      db.updateTrangThai(detaiId, 'DANG_PHAN_BIEN', u.hoTen, `Nộp kết quả phản biện: ${ketQua}`)
    }
    if (chiTiet.value?.id === detaiId) chiTiet.value = db.getDeTaiById(detaiId)
  }

  function capNhatDeCuong(detaiId, deCuong) {
    db.updateDeCuong(detaiId, deCuong)
    if (chiTiet.value?.id === detaiId) chiTiet.value = db.getDeTaiById(detaiId)
  }

  function capNhatChiPhi(detaiId, rows) {
    db.updateChiPhi(detaiId, rows)
    if (chiTiet.value?.id === detaiId) chiTiet.value = db.getDeTaiById(detaiId)
  }

  // ── Stats ────────────────────────────────────────────
  function getStats() {
    const u = authStore.user
    if (!u) return {}
    return db.getStatsByStatus(u.id, u.role)
  }

  // Alias cho GV ký hợp đồng
  const kyHopDongGV = gvDongYHopDong

  return {
    danhSach, chiTiet, loading, error,
    layChiTiet, layDanhSach, taoDeTai, uploadFile, removeFile,
    guiHoSo, boSungHoSo, gvDongYHopDong, kyHopDongGV,
    capNhatDeCuong, capNhatChiPhi,
    tiepNhan, yeuCauBoSung, tuChoiHoSo,
    lapToPhanBien, chuyenLapHopDong, xacNhanKyHopDong,
    nopKetQuaPhanBien, getStats,
  }
})
