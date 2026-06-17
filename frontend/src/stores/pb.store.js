import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '@/api/axios'

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
    const res = await api.post(`/de-tai/${detaiId}/nop-ket-qua-pb`, payload)
    return res.data
  }

  return { danhSachAssigned, loading, layDanhSachAssigned, nopKetQua }
})
