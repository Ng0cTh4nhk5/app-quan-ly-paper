import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '@/api/axios'

export const useNckhStore = defineStore('nckh', () => {
  const inbox    = ref([])
  const danhSach = ref([]) // alias — all projects for dashboard
  const chiTiet  = ref(null)
  const loading  = ref(false)

  async function layInbox(trangThai = 'CHO_PNCKH_XEM_XET') {
    loading.value = true
    try {
      const res = await api.get('/de-tai', { params: { trangThai } })
      inbox.value = Array.isArray(res.data) ? res.data : res.data.content
    } finally { loading.value = false }
  }

  async function layTatCa() {
    loading.value = true
    try {
      const res = await api.get('/de-tai')
      inbox.value = Array.isArray(res.data) ? res.data : res.data.content
    } finally { loading.value = false }
  }

  async function layDanhSach() {
    loading.value = true
    try {
      const res = await api.get('/de-tai')
      danhSach.value = Array.isArray(res.data) ? res.data : res.data.content
    } finally { loading.value = false }
  }

  async function layChiTiet(id) {
    loading.value = true
    try {
      const res = await api.get(`/de-tai/${id}`)
      chiTiet.value = res.data
    } finally { loading.value = false }
  }

  async function tiepNhan(id) {
    const res = await api.post(`/de-tai/${id}/tiep-nhan`)
    chiTiet.value = res.data
    return res.data
  }

  async function yeuCauBoSung(id, payload) {
    const res = await api.post(`/de-tai/${id}/yeu-cau-bo-sung`, payload)
    chiTiet.value = res.data
    return res.data
  }

  async function lapToPhanBien(id, thanhVienIds) {
    const res = await api.post(`/de-tai/${id}/lap-to-phan-bien`, { thanhVienIds })
    chiTiet.value = res.data
    return res.data
  }

  async function xetDuyetPB(id, payload) {
    const res = await api.post(`/de-tai/${id}/xet-duyet-pb`, payload)
    chiTiet.value = res.data
    return res.data
  }

  async function soanHopDong(id, payload = {}) {
    const res = await api.post(`/de-tai/${id}/hop-dong/soan`, payload)
    chiTiet.value = res.data
    return res.data
  }

  async function kyHopDong(id, payload = {}) {
    const res = await api.post(`/de-tai/${id}/ky-hop-dong`, payload)
    chiTiet.value = res.data
    return res.data
  }

  return { inbox, danhSach, chiTiet, loading,
           layInbox, layTatCa, layDanhSach, layChiTiet, tiepNhan,
           yeuCauBoSung, lapToPhanBien, xetDuyetPB, soanHopDong, kyHopDong }
})
