import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '@/api/axios'

const isRealApi = !!import.meta.env.VITE_API_URL
const pbDecisionMap = {
  CHAP_THUAN: 'CHAP_NHAN',
  CHO_CHINH_SUA: 'YEU_CAU_SUA',
  YEU_CAU_CHINH_SUA: 'YEU_CAU_SUA',
}

export const useNckhStore = defineStore('nckh', () => {
  const inbox    = ref([])
  const danhSach = ref([]) // alias — all projects for dashboard
  const chiTiet  = ref(null)
  const loading  = ref(false)
  const canActions = ref({})

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

  async function tiepNhan(id) {
    const res = await api.post(`/de-tai/${id}/tiep-nhan`)
    chiTiet.value = res.data
    await layCanActions(id)
    return res.data
  }

  async function yeuCauBoSung(id, payload) {
    const res = await api.post(`/de-tai/${id}/yeu-cau-bo-sung`, {
      ...payload,
      noiDung: payload.noiDung ?? payload.lyDo,
    })
    chiTiet.value = res.data
    await layCanActions(id)
    return res.data
  }

  async function lapToPhanBien(id, thanhVienIds, deadlineNop) {
    const defaultDeadline = new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString().slice(0, 10)
    const res = await api.post(`/de-tai/${id}/lap-to-phan-bien`, {
      thanhVienIds,
      deadlineNop: deadlineNop ?? defaultDeadline,
    })
    chiTiet.value = res.data
    await layCanActions(id)
    return res.data
  }

  async function xetDuyetPB(id, payload) {
    const rawDecision = payload.quyetDinh ?? payload.ketQua
    const realPayload = {
      ...payload,
      quyetDinh: pbDecisionMap[rawDecision] ?? rawDecision,
      noiDungFeedback: payload.noiDungFeedback ?? payload.ghiChu,
      deadlineSua: payload.deadlineSua,
    }
    const res = await api.post(`/de-tai/${id}/xet-duyet-pb`, isRealApi ? realPayload : payload)
    chiTiet.value = res.data
    await layCanActions(id)
    return res.data
  }

  async function soanHopDong(id, payload = {}) {
    const today = new Date()
    const end = new Date(today)
    end.setMonth(end.getMonth() + Number(payload.thoiGian ?? 12))
    const realPayload = {
      kinhPhi: payload.kinhPhi,
      ngayBatDau: payload.ngayBatDau ?? today.toISOString().slice(0, 10),
      ngayKetThuc: payload.ngayKetThuc ?? end.toISOString().slice(0, 10),
      tyLeTamUng: payload.tyLeTamUng ?? 0.5,
    }
    const url = isRealApi ? `/de-tai/${id}/soan-hop-dong` : `/de-tai/${id}/hop-dong/soan`
    const res = await api.post(url, isRealApi ? realPayload : payload)
    chiTiet.value = res.data
    await layCanActions(id)
    return res.data
  }

  async function kyHopDong(id, payload = {}) {
    const data = new FormData()
    if (payload.fileScan) data.append('fileScan', payload.fileScan)
    const ngayKy = payload.ngayKy ?? new Date().toISOString().slice(0, 10)
    const res = isRealApi
      ? await api.post(`/de-tai/${id}/ky-hop-dong`, data, { params: { ngayKy } })
      : await api.post(`/de-tai/${id}/ky-hop-dong`, payload)
    chiTiet.value = res.data
    await layCanActions(id)
    return res.data
  }

  return { inbox, danhSach, chiTiet, loading, canActions,
           layInbox, layTatCa, layDanhSach, layChiTiet, layCanActions, tiepNhan,
           yeuCauBoSung, lapToPhanBien, xetDuyetPB, soanHopDong, kyHopDong }
})
