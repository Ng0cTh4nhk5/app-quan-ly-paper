import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/api/axios'

export const useDetaiStore = defineStore('detai', () => {
  const danhSach = ref([])
  const chiTiet  = ref(null)
  const loading  = ref(false)
  const error    = ref(null)

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
    loading.value = true
    try {
      const res = await api.get(`/de-tai/${id}`)
      chiTiet.value = res.data
    } finally { loading.value = false }
  }

  async function taoDeTai(payload) {
    loading.value = true
    try {
      const res = await api.post('/de-tai', payload)
      danhSach.value.unshift(res.data)
      return res.data
    } finally { loading.value = false }
  }

  async function guiHoSo(detaiId) {
    const res = await api.post(`/de-tai/${detaiId}/gui-ho-so`)
    _sync(detaiId, res.data)
    return res.data
  }

  async function boSungHoSo(detaiId, payload) {
    const res = await api.post(`/de-tai/${detaiId}/bo-sung`, payload)
    _sync(detaiId, res.data)
    return res.data
  }

  async function kyHopDong(detaiId) {
    const res = await api.post(`/de-tai/${detaiId}/ky-hop-dong`)
    _sync(detaiId, res.data)
    return res.data
  }

  async function uploadFile(detaiId, file, loai = 'THUYET_MINH') {
    const res = await api.post(`/de-tai/${detaiId}/upload`, { file, loai })
    _sync(detaiId, res.data)
    return res.data
  }

  function _sync(id, updated) {
    const idx = danhSach.value.findIndex(d => d.id === parseInt(id))
    if (idx !== -1) danhSach.value[idx] = updated
    if (chiTiet.value?.id === parseInt(id)) chiTiet.value = updated
  }

  return {
    danhSach, chiTiet, loading, error,
    deTaiDraft, deTaiChoBoSung,
    layDanhSach, layChiTiet, taoDeTai,
    guiHoSo, boSungHoSo, kyHopDong, uploadFile,
  }
})
