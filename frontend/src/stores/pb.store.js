import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '@/api/axios'

const isRealApi = !!import.meta.env.VITE_API_URL
const pbResultMap = {
  CHAP_THUAN: 'CHAP_NHAN',
  YEU_CAU_CHINH_SUA: 'CAN_SUA',
}

function normalizeList(data) {
  return Array.isArray(data) ? data : (data?.content ?? [])
}

export const usePBStore = defineStore('pb', () => {
  const danhSachAssigned = ref([])
  const chiTiet = ref(null)
  const canActions = ref({ nopKetQuaPB: false })
  const loading = ref(false)
  const error = ref(null)

  async function layDanhSachAssigned() {
    loading.value = true
    error.value = null
    try {
      const res = await api.get('/de-tai', { params: { trangThai: 'DANG_PHAN_BIEN' } })
      danhSachAssigned.value = normalizeList(res.data)
    } catch (e) {
      error.value = e.response?.data?.message ?? 'Không thể tải danh sách được phân công.'
      throw e
    } finally {
      loading.value = false
    }
  }

  async function layChiTiet(id) {
    loading.value = true
    error.value = null
    try {
      const res = await api.get(`/de-tai/${id}`)
      chiTiet.value = res.data
      await layCanActions(id)
    } catch (e) {
      chiTiet.value = null
      canActions.value = { nopKetQuaPB: false }
      error.value = e.response?.data?.message ?? 'Không thể tải chi tiết đề tài.'
      throw e
    } finally {
      loading.value = false
    }
  }

  async function layCanActions(id) {
    try {
      const res = await api.get(`/de-tai/${id}/can-actions`)
      canActions.value = { nopKetQuaPB: false, ...res.data }
    } catch {
      canActions.value = { nopKetQuaPB: false }
    }
    return canActions.value
  }

  async function nopKetQua(detaiId, payload) {
    const rawResult = payload.ketQua ?? payload.deXuat
    const realPayload = {
      ketQua: pbResultMap[rawResult] ?? rawResult,
      nhanXet: payload.nhanXet,
    }
    const res = await api.post(`/de-tai/${detaiId}/nop-ket-qua-pb`, isRealApi ? realPayload : payload)
    // FIXED(TL): Dùng String() thay vì parseInt để tránh Long ID overflow.
    danhSachAssigned.value = danhSachAssigned.value.filter(d => String(d.id) !== String(detaiId))
    chiTiet.value = res.data
    await layCanActions(detaiId)
    return res.data
  }

  return {
    danhSachAssigned,
    chiTiet,
    canActions,
    loading,
    error,
    layDanhSachAssigned,
    layChiTiet,
    layCanActions,
    nopKetQua,
  }
})
