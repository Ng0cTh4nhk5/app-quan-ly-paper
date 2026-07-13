import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/api/axios'
import {
  CONTRACT_REVIEWABLE_STATUSES,
  enforceContractActionGuards,
  hasOpenContractFeedback,
  normalizeContractStatus,
} from '@/utils/sopDGuards'

const EMPTY_ACTIONS = {
  guiHoSo: false,
  boSungHoSo: false,
  nopLaiThuyetMinh: false,
  xoaTaiLieu: false,
  xemHopDong: false,
  dongYHopDong: false,
}

function fallbackActions(dt) {
  const hasThuyetMinh = dt?.taiLieu?.some(t => t.loai === 'THUYET_MINH')
  const contractStatus = normalizeContractStatus(dt)
  const hasContractFeedback = hasOpenContractFeedback(dt)
  return {
    ...EMPTY_ACTIONS,
    guiHoSo: ['DRAFT', 'CHO_CHINH_SUA_THUYET_MINH'].includes(dt?.trangThai) && hasThuyetMinh,
    boSungHoSo: dt?.trangThai === 'CHO_BO_SUNG_HO_SO',
    nopLaiThuyetMinh: dt?.trangThai === 'CHO_CHINH_SUA_THUYET_MINH' && hasThuyetMinh,
    xoaTaiLieu: dt?.trangThai === 'DRAFT',
    xemHopDong: ['DANG_LAP_HOP_DONG', 'DANG_THUC_HIEN', 'HOAN_TAT'].includes(dt?.trangThai),
    dongYHopDong: dt?.trangThai === 'DANG_LAP_HOP_DONG' && CONTRACT_REVIEWABLE_STATUSES.includes(contractStatus) && !dt?.gvDaDongYHopDong && !hasContractFeedback,
  }
}

export const useDetaiStore = defineStore('detai', () => {
  const danhSach = ref([])
  const chiTiet  = ref(null)
  const canActions = ref({ ...EMPTY_ACTIONS })
  const loading  = ref(false)
  const error    = ref(null)
  const actionLoading = ref({})

  const deTaiDraft     = computed(() => danhSach.value.filter(d => d.trangThai === 'DRAFT'))
  const deTaiChoBoSung = computed(() => danhSach.value.filter(d => d.trangThai === 'CHO_BO_SUNG_HO_SO'))

  async function layDanhSach() {
    loading.value = true; error.value = null
    try {
      const res = await api.get('/de-tai')
      danhSach.value = Array.isArray(res.data) ? res.data : res.data.content
    } catch (e) {
      error.value = e.response?.data?.message ?? 'Không thể tải danh sách.'
    } finally { loading.value = false }
  }

  async function layChiTiet(id) {
    loading.value = true; error.value = null
    try {
      const res = await api.get(`/de-tai/${id}`)
      chiTiet.value = res.data
      await layCanActions(id)
    } catch (e) {
      chiTiet.value = null
      canActions.value = { ...EMPTY_ACTIONS }
      error.value = e.response?.data?.message ?? 'Không thể tải chi tiết đề tài.'
      throw e
    } finally { loading.value = false }
  }

  async function layCanActions(id) {
    try {
      const res = await api.get(`/de-tai/${id}/can-actions`)
      canActions.value = enforceContractActionGuards({ ...EMPTY_ACTIONS, ...res.data }, chiTiet.value, 'GIANG_VIEN')
    } catch {
      canActions.value = enforceContractActionGuards(fallbackActions(chiTiet.value), chiTiet.value, 'GIANG_VIEN')
    }
    return canActions.value
  }

  async function runAction(key, id, fn) {
    actionLoading.value = { ...actionLoading.value, [key]: true }
    try {
      const updated = await fn()
      _sync(id, updated)
      await layCanActions(id)
      return updated
    } finally {
      actionLoading.value = { ...actionLoading.value, [key]: false }
    }
  }

  async function taoDeTai(payload) {
    loading.value = true; error.value = null
    try {
      const res = await api.post('/de-tai', payload)
      danhSach.value.unshift(res.data)
      return res.data
    } catch (e) {
      error.value = e.response?.data?.message ?? 'Khong the tao de tai.'
      throw e
    } finally { loading.value = false }
  }

  async function capNhatDeTai(detaiId, payload) {
    loading.value = true; error.value = null
    try {
      const res = await api.put(`/de-tai/${detaiId}`, payload)
      _sync(detaiId, res.data)
      return res.data
    } catch (e) {
      error.value = e.response?.data?.message ?? 'Không thể cập nhật đề tài.'
      throw e
    } finally { loading.value = false }
  }

  async function guiHoSo(detaiId) {
    return runAction('guiHoSo', detaiId, async () => {
      const res = await api.post(`/de-tai/${detaiId}/gui-ho-so`)
      return res.data
    })
  }

  async function boSungHoSo(detaiId, payload) {
    return runAction('boSungHoSo', detaiId, async () => {
      const res = await api.post(`/de-tai/${detaiId}/nop-bo-sung`, payload)
      return res.data
    })
  }

  async function nopLaiThuyetMinh(detaiId) {
    return runAction('nopLaiThuyetMinh', detaiId, async () => {
      const res = await api.post(`/de-tai/${detaiId}/nop-lai-thuyet-minh`)
      return res.data
    })
  }

  async function dongYHopDong(detaiId, payload = {}) {
    return runAction('dongYHopDong', detaiId, async () => {
      const res = await api.post(`/de-tai/${detaiId}/phan-hoi-hop-dong`, {
        dongY: true,
        noiDungGhiChu: payload.noiDungGhiChu,
      })
      return res.data
    })
  }

  async function yeuCauChinhSuaHopDong(detaiId, payload) {
    return runAction('yeuCauChinhSuaHopDong', detaiId, async () => {
      const res = await api.post(`/de-tai/${detaiId}/phan-hoi-hop-dong`, {
        dongY: false,
        noiDungGhiChu: payload.noiDung ?? payload.noiDungGhiChu,
      })
      return res.data
    })
  }

  async function uploadFile(detaiId, file, loai = 'THUYET_MINH') {
    const formData = new FormData()
    formData.append('file', file)
    await api.post('/files/upload', formData, { params: { deTaiId: detaiId, loai } })
    await layChiTiet(detaiId)
    await layCanActions(detaiId)
  }

  async function xoaTaiLieu(detaiId, taiLieuId) {
    await api.delete(`/files/${taiLieuId}`)
    await layChiTiet(detaiId)
    await layCanActions(detaiId)
  }

  function _sync(id, updated) {
    // FIXED(TL): Dùng String() thay vì parseInt để tránh Long ID overflow.
    const strId = String(id)
    const idx = danhSach.value.findIndex(d => String(d.id) === strId)
    if (idx !== -1) danhSach.value[idx] = updated
    if (String(chiTiet.value?.id) === strId) chiTiet.value = updated
  }

  return {
    danhSach, chiTiet, canActions, loading, error, actionLoading,
    deTaiDraft, deTaiChoBoSung,
    layDanhSach, layChiTiet, layCanActions, taoDeTai, capNhatDeTai,
    guiHoSo, boSungHoSo, nopLaiThuyetMinh, dongYHopDong, yeuCauChinhSuaHopDong, uploadFile, xoaTaiLieu,
  }
})
