import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '@/api/axios'

const isRealApi = !!import.meta.env.VITE_API_URL
const pbResultMap = {
  CHAP_THUAN: 'CHAP_NHAN',
  YEU_CAU_CHINH_SUA: 'CAN_SUA',
}

export const usePBStore = defineStore('pb', () => {
  const danhSachAssigned = ref([])
  const loading = ref(false)

  async function layDanhSachAssigned() {
    loading.value = true
    try {
      const res = await api.get('/de-tai', { params: { trangThai: 'DANG_PHAN_BIEN' } })
      danhSachAssigned.value = Array.isArray(res.data) ? res.data : res.data.content
    } finally { loading.value = false }
  }

  async function nopKetQua(detaiId, payload) {
    const rawResult = payload.ketQua ?? payload.deXuat
    const realPayload = {
      ketQua: pbResultMap[rawResult] ?? rawResult,
      nhanXet: payload.nhanXet,
    }
    const res = await api.post(`/de-tai/${detaiId}/nop-ket-qua-pb`, isRealApi ? realPayload : payload)
    danhSachAssigned.value = danhSachAssigned.value.filter(d => d.id !== parseInt(detaiId))
    return res.data
  }

  return { danhSachAssigned, loading, layDanhSachAssigned, nopKetQua }
})
