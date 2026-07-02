import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/api/axios'
import {
  CONTRACT_REVIEWABLE_STATUSES,
  enforceContractActionGuards,
  hasOpenContractFeedback,
  normalizeContractStatus,
} from '@/mock/sopDGuards'

const EMPTY_ACTIONS = {
  guiHoSo: false,
  boSungHoSo: false,
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
      error.value = e.response?.data?.message ?? 'Khong the tai chi tiet de tai.'
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

  async function guiHoSo(detaiId) {
    return runAction('guiHoSo', detaiId, async () => {
      const res = await api.post(`/de-tai/${detaiId}/gui-ho-so`)
      return res.data
    })
  }

  async function boSungHoSo(detaiId, payload) {
    return runAction('boSungHoSo', detaiId, async () => {
      const res = await api.post(`/de-tai/${detaiId}/bo-sung`, payload)
      return res.data
    })
  }

  async function dongYHopDong(detaiId, payload = {}) {
    return runAction('dongYHopDong', detaiId, async () => {
      const res = await api.post(`/de-tai/${detaiId}/gv-dong-y-hop-dong`, payload)
      return res.data
    })
  }

  async function yeuCauChinhSuaHopDong(detaiId, payload) {
    return runAction('yeuCauChinhSuaHopDong', detaiId, async () => {
      const res = await api.post(`/de-tai/${detaiId}/hop-dong/yeu-cau-chinh-sua`, payload)
      return res.data
    })
  }

  async function uploadFile(detaiId, file, loai = 'THUYET_MINH') {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('loai', loai)
    const res = await api.post(`/de-tai/${detaiId}/tai-lieu`, formData)
    _sync(detaiId, res.data)
    await layCanActions(detaiId)
    return res.data
  }

  async function xoaTaiLieu(detaiId, taiLieuId) {
    const res = await api.delete(`/de-tai/${detaiId}/tai-lieu/${taiLieuId}`)
    _sync(detaiId, res.data)
    await layCanActions(detaiId)
    return res.data
  }

  function _sync(id, updated) {
    const idx = danhSach.value.findIndex(d => d.id === parseInt(id))
    if (idx !== -1) danhSach.value[idx] = updated
    if (chiTiet.value?.id === parseInt(id)) chiTiet.value = updated
  }

  return {
    danhSach, chiTiet, canActions, loading, error, actionLoading,
    deTaiDraft, deTaiChoBoSung,
    layDanhSach, layChiTiet, layCanActions, taoDeTai,
    guiHoSo, boSungHoSo, dongYHopDong, yeuCauChinhSuaHopDong, uploadFile, xoaTaiLieu,
  }
})
