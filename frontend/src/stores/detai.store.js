import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/api/axios'

const isRealApi = !!import.meta.env.VITE_API_URL

export const useDetaiStore = defineStore('detai', () => {
  const danhSach = ref([])
  const chiTiet  = ref(null)
  const loading  = ref(false)
  const error    = ref(null)
  const canActions = ref({})
  const actionLoading = ref({
    guiHoSo: false,
  })

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
    } catch (e) {
      chiTiet.value = null
      error.value = e.response?.data?.message ?? 'Khong the tai chi tiet de tai.'
      throw e
    } finally { loading.value = false }
  }

  async function layCanActions(id) {
    try {
      const res = await api.get(`/de-tai/${id}/can-actions`)
      canActions.value = res.data ?? {}
      return canActions.value
    } catch {
      canActions.value = {}
      return canActions.value
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
    actionLoading.value.guiHoSo = true
    try {
      const res = await api.post(`/de-tai/${detaiId}/gui-ho-so`)
      _sync(detaiId, res.data)
      await layCanActions(detaiId)
      return res.data
    } finally {
      actionLoading.value.guiHoSo = false
    }
  }

  async function boSungHoSo(detaiId, payload) {
    const url = isRealApi ? `/de-tai/${detaiId}/nop-bo-sung` : `/de-tai/${detaiId}/bo-sung`
    const res = await api.post(url, payload)
    _sync(detaiId, res.data)
    await layCanActions(detaiId)
    return res.data
  }

  async function dongYHopDong(detaiId, payload = {}) {
    const url = isRealApi ? `/de-tai/${detaiId}/phan-hoi-hop-dong` : `/de-tai/${detaiId}/gv-dong-y-hop-dong`
    const body = isRealApi ? { dongY: true, noiDungGhiChu: payload.noiDungGhiChu } : payload
    const res = await api.post(url, body)
    _sync(detaiId, res.data)
    await layCanActions(detaiId)
    return res.data
  }

  async function yeuCauChinhSuaHopDong(detaiId, payload) {
    const url = isRealApi ? `/de-tai/${detaiId}/phan-hoi-hop-dong` : `/de-tai/${detaiId}/hop-dong/yeu-cau-chinh-sua`
    const body = isRealApi ? { dongY: false, noiDungGhiChu: payload.noiDung ?? payload.noiDungGhiChu } : payload
    const res = await api.post(url, body)
    _sync(detaiId, res.data)
    await layCanActions(detaiId)
    return res.data
  }

  async function uploadFile(detaiId, file, loai = 'THUYET_MINH') {
    const formData = new FormData()
    formData.append('file', file)
    if (!isRealApi) formData.append('loai', loai)
    const res = isRealApi
      ? await api.post('/files/upload', formData, { params: { deTaiId: detaiId, loai } })
      : await api.post(`/de-tai/${detaiId}/tai-lieu`, formData)
    if (isRealApi) await layChiTiet(detaiId)
    else _sync(detaiId, res.data)
    await layCanActions(detaiId)
    return res.data
  }

  async function xoaTaiLieu(detaiId, taiLieuId) {
    if (isRealApi) {
      throw new Error('Backend hien chua ho tro xoa tai lieu.')
    }
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
    danhSach, chiTiet, loading, error, canActions, actionLoading,
    deTaiDraft, deTaiChoBoSung,
    layDanhSach, layChiTiet, layCanActions, taoDeTai,
    guiHoSo, boSungHoSo, dongYHopDong, yeuCauChinhSuaHopDong, uploadFile, xoaTaiLieu,
  }
})
