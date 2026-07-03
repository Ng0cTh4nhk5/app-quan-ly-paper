import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '@/api/axios'
import {
  CONTRACT_DRAFTABLE_STATUSES,
  enforceContractActionGuards,
  hasOpenContractFeedback,
  normalizeContractStatus,
} from '@/mock/sopDGuards'

const EMPTY_ACTIONS = {
  tiepNhan: false,
  yeuCauBoSung: false,
  tuChoiHoSo: false,
  lapToPhanBien: false,
  xetDuyetPB: false,
  soanHopDong: false,
  kyHopDong: false,
}

function normalizeList(data) {
  return Array.isArray(data) ? data : (data?.content ?? [])
}

function fallbackActions(dt) {
  const contractStatus = normalizeContractStatus(dt)
  const hasContractFeedback = hasOpenContractFeedback(dt)
  return {
    ...EMPTY_ACTIONS,
    tiepNhan: dt?.trangThai === 'CHO_PNCKH_XEM_XET',
    yeuCauBoSung: dt?.trangThai === 'DANG_XEM_XET_BOI_PNCKH',
    tuChoiHoSo: dt?.trangThai === 'DANG_XEM_XET_BOI_PNCKH',
    lapToPhanBien: dt?.trangThai === 'DANG_XEM_XET_BOI_PNCKH',
    xetDuyetPB: dt?.trangThai === 'DANG_PHAN_BIEN',
    soanHopDong: dt?.trangThai === 'DANG_LAP_HOP_DONG' && CONTRACT_DRAFTABLE_STATUSES.includes(contractStatus),
    kyHopDong: dt?.trangThai === 'DANG_LAP_HOP_DONG' && contractStatus === 'CHO_KY' && dt?.gvDaDongYHopDong === true && !hasContractFeedback,
  }
}

const isRealApi = !!import.meta.env.VITE_API_URL
const pbDecisionMap = {
  CHAP_THUAN: 'CHAP_NHAN',
  CHO_CHINH_SUA: 'YEU_CAU_SUA',
  YEU_CAU_CHINH_SUA: 'YEU_CAU_SUA',
}

export const useNckhStore = defineStore('nckh', () => {
  const inbox = ref([])
  const danhSach = ref([])
  const chiTiet = ref(null)
  const canActions = ref({ ...EMPTY_ACTIONS })
  const loading = ref(false)
  const error = ref(null)
  const actionLoading = ref({})

  async function layInbox(trangThai = 'CHO_PNCKH_XEM_XET') {
    loading.value = true
    error.value = null
    try {
      const res = await api.get('/de-tai', { params: { trangThai } })
      inbox.value = normalizeList(res.data)
    } catch (e) {
      error.value = e.response?.data?.message ?? 'Không thể tải hộp thư đến.'
      throw e
    } finally {
      loading.value = false
    }
  }

  async function layTatCa() {
    loading.value = true
    error.value = null
    try {
      const res = await api.get('/de-tai')
      inbox.value = normalizeList(res.data)
      danhSach.value = inbox.value
    } catch (e) {
      error.value = e.response?.data?.message ?? 'Không thể tải danh sách đề tài.'
      throw e
    } finally {
      loading.value = false
    }
  }

  async function layDanhSach() {
    loading.value = true
    error.value = null
    try {
      const res = await api.get('/de-tai')
      danhSach.value = normalizeList(res.data)
    } catch (e) {
      error.value = e.response?.data?.message ?? 'Không thể tải danh sách đề tài.'
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
      canActions.value = { ...EMPTY_ACTIONS }
      error.value = e.response?.data?.message ?? 'Không thể tải chi tiết đề tài.'
      throw e
    } finally {
      loading.value = false
    }
  }

  async function layCanActions(id) {
    try {
      const res = await api.get(`/de-tai/${id}/can-actions`)
      canActions.value = enforceContractActionGuards({ ...EMPTY_ACTIONS, ...res.data }, chiTiet.value, 'NCKH')
    } catch {
      canActions.value = enforceContractActionGuards(fallbackActions(chiTiet.value), chiTiet.value, 'NCKH')
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

  function tiepNhan(id) {
    return runAction('tiepNhan', id, async () => {
      const res = await api.post(`/de-tai/${id}/tiep-nhan`)
      return res.data
    })
  }

  function yeuCauBoSung(id, payload) {
    return runAction('yeuCauBoSung', id, async () => {
      const body = {
        ...payload,
        noiDung: payload.noiDung ?? payload.lyDo,
      }
      const res = await api.post(`/de-tai/${id}/yeu-cau-bo-sung`, body)
      return res.data
    })
  }

  function tuChoiHoSo(id, payload) {
    return runAction('tuChoiHoSo', id, async () => {
      const res = await api.post(`/de-tai/${id}/tu-choi-ho-so`, payload)
      return res.data
    })
  }

  function lapToPhanBien(id, payload) {
    return runAction('lapToPhanBien', id, async () => {
      const defaultDeadline = new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString().slice(0, 10)
      let body
      if (Array.isArray(payload)) {
        body = { thanhVienIds: payload, deadlineNop: defaultDeadline }
      } else {
        body = {
          ...payload,
          deadlineNop: payload.deadlineNop ?? defaultDeadline,
        }
      }
      const res = await api.post(`/de-tai/${id}/lap-to-phan-bien`, body)
      return res.data
    })
  }

  function xetDuyetPB(id, payload) {
    return runAction('xetDuyetPB', id, async () => {
      const rawDecision = payload.quyetDinh ?? payload.ketQua
      const realPayload = {
        ...payload,
        quyetDinh: pbDecisionMap[rawDecision] ?? rawDecision,
        noiDungFeedback: payload.noiDungFeedback ?? payload.ghiChu,
        deadlineSua: payload.deadlineSua,
      }
      const res = await api.post(`/de-tai/${id}/xet-duyet-pb`, isRealApi ? realPayload : payload)
      return res.data
    })
  }

  function soanHopDong(id, payload = {}) {
    return runAction('soanHopDong', id, async () => {
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
      return res.data
    })
  }

  function kyHopDong(id, payload = {}) {
    return runAction('kyHopDong', id, async () => {
      let res
      if (isRealApi) {
        const data = new FormData()
        if (payload.fileScan) data.append('fileScan', payload.fileScan)
        const ngayKy = payload.ngayKy ?? new Date().toISOString().slice(0, 10)
        res = await api.post(`/de-tai/${id}/ky-hop-dong`, data, { params: { ngayKy } })
      } else {
        let body = payload
        if (payload.fileScan && typeof FormData !== 'undefined') {
          body = new FormData()
          body.append('fileScan', payload.fileScan)
          body.append('ngayKy', payload.ngayKy)
        }
        res = await api.post(`/de-tai/${id}/ky-hop-dong`, body)
      }
      return res.data
    })
  }

  function _sync(id, updated) {
    chiTiet.value = updated
    const numericId = parseInt(id)
    const syncOne = list => {
      const idx = list.value.findIndex(d => d.id === numericId)
      if (idx !== -1) list.value[idx] = updated
    }
    syncOne(inbox)
    syncOne(danhSach)
  }

  return {
    inbox,
    danhSach,
    chiTiet,
    canActions,
    loading,
    error,
    actionLoading,
    layInbox,
    layTatCa,
    layDanhSach,
    layChiTiet,
    layCanActions,
    tiepNhan,
    yeuCauBoSung,
    tuChoiHoSo,
    lapToPhanBien,
    xetDuyetPB,
    soanHopDong,
    kyHopDong,
  }
})
