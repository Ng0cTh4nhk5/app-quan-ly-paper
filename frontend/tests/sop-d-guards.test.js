import test from 'node:test'
import assert from 'node:assert/strict'
import { getDeTaiById, getDeTaiByRole, MOCK_USERS } from '../src/mock/db.js'
import {
  allPbSubmitted,
  canActionsFor,
  enforceContractActionGuards,
  normalizeContractStatus,
  normalizeReviewDecision,
  validateKyHopDong,
  validateLapToPhanBien,
  validatePbKetQua,
  validateSoanHopDong,
  validateTuChoiHoSo,
  validateXetDuyetPB,
  validateYeuCauBoSung,
} from '../src/mock/sopDGuards.js'

function isoPlusDays(days) {
  const date = new Date()
  date.setDate(date.getDate() + days)
  return date.toISOString().slice(0, 10)
}

test('SOP D can-actions expose NCKH review actions by topic state', () => {
  assert.equal(canActionsFor(getDeTaiById(2), MOCK_USERS.nckh).tiepNhan, true)

  const reviewing = canActionsFor(getDeTaiById(3), MOCK_USERS.nckh)
  assert.equal(reviewing.yeuCauBoSung, true)
  assert.equal(reviewing.tuChoiHoSo, true)
  assert.equal(reviewing.lapToPhanBien, true)

  assert.equal(canActionsFor(getDeTaiById(5), MOCK_USERS.nckh).xetDuyetPB, true)
  const readyForContract = canActionsFor(getDeTaiById(10), MOCK_USERS.nckh)
  assert.equal(readyForContract.xetDuyetPB, true)
  assert.equal(allPbSubmitted(getDeTaiById(10).toPhanBien), true)
  assert.equal(validateXetDuyetPB({ quyetDinh: 'CHAP_NHAN' }, getDeTaiById(10).toPhanBien), '')

  const waitingForGv = canActionsFor(getDeTaiById(6), MOCK_USERS.nckh)
  assert.equal(waitingForGv.soanHopDong, false)
  assert.equal(waitingForGv.kyHopDong, false)

  const waitingForSignature = canActionsFor(getDeTaiById(9), MOCK_USERS.nckh)
  assert.equal(waitingForSignature.soanHopDong, false)
  assert.equal(waitingForSignature.kyHopDong, true)

  assert.equal(normalizeContractStatus(getDeTaiById(7)), 'DA_KY')
})

test('GV can-actions follow submission and contract states', () => {
  const draftWithThuyetMinh = canActionsFor(getDeTaiById(1), MOCK_USERS.gv_a)
  assert.equal(draftWithThuyetMinh.guiHoSo, true)
  assert.equal(draftWithThuyetMinh.xoaTaiLieu, true)

  const needSupplement = canActionsFor(getDeTaiById(4), MOCK_USERS.gv_a)
  assert.equal(needSupplement.boSungHoSo, true)
  assert.equal(needSupplement.guiHoSo, false)

  const contractReady = canActionsFor(getDeTaiById(9), MOCK_USERS.gv_a)
  assert.equal(contractReady.xemHopDong, true)
  assert.equal(contractReady.dongYHopDong, false)

  const waitingTeacherReview = canActionsFor(getDeTaiById(11), MOCK_USERS.gv_a)
  assert.equal(waitingTeacherReview.xemHopDong, true)
  assert.equal(waitingTeacherReview.dongYHopDong, true)

  const teacherRequestedRevision = canActionsFor(getDeTaiById(12), MOCK_USERS.gv_a)
  assert.equal(teacherRequestedRevision.xemHopDong, true)
  assert.equal(teacherRequestedRevision.dongYHopDong, false)
})

test('contract feedback state exposes only the revision action', () => {
  const revisionTopic = getDeTaiById(12)

  const nckhActions = canActionsFor(revisionTopic, MOCK_USERS.nckh)
  assert.equal(nckhActions.soanHopDong, true)
  assert.equal(nckhActions.kyHopDong, false)

  const gvActions = canActionsFor(revisionTopic, MOCK_USERS.gv_a)
  assert.equal(gvActions.dongYHopDong, false)
})

test('server can-actions are masked by FE contract guards', () => {
  const staleServerActions = {
    soanHopDong: true,
    kyHopDong: true,
    dongYHopDong: true,
  }

  assert.deepEqual(
    enforceContractActionGuards(staleServerActions, getDeTaiById(9), 'NCKH'),
    { soanHopDong: false, kyHopDong: true, dongYHopDong: true },
  )

  assert.equal(
    enforceContractActionGuards(staleServerActions, getDeTaiById(6), 'NCKH').soanHopDong,
    false,
  )

  const revisionTopic = {
    ...getDeTaiById(6),
    hopDongStatus: 'CAN_SUA',
    hopDongFeedback: { noiDung: 'Can chinh sua dieu khoan hop dong.' },
  }
  assert.equal(
    enforceContractActionGuards(staleServerActions, revisionTopic, 'GIANG_VIEN').dongYHopDong,
    false,
  )
})

test('PB assigned list hides topics that current reviewer already submitted', () => {
  const pb1Assigned = getDeTaiByRole('TO_PHAN_BIEN', MOCK_USERS.pb_1.id).map(d => d.id)
  const pb2Assigned = getDeTaiByRole('TO_PHAN_BIEN', MOCK_USERS.pb_2.id).map(d => d.id)

  assert.deepEqual(pb1Assigned, [5])
  assert.deepEqual(pb2Assigned, [])
})

test('PB can submit only once for an assigned topic', () => {
  const topic = getDeTaiById(5)

  assert.equal(canActionsFor(topic, MOCK_USERS.pb_1).nopKetQuaPB, true)
  assert.equal(canActionsFor(topic, MOCK_USERS.pb_2).nopKetQuaPB, false)
  assert.equal(canActionsFor(topic, MOCK_USERS.pb_3).nopKetQuaPB, false)
})

test('review decision aliases normalize to current SOP D enum', () => {
  assert.equal(normalizeReviewDecision('CHAP_THUAN'), 'CHAP_NHAN')
  assert.equal(normalizeReviewDecision('YEU_CAU_CHINH_SUA'), 'CAN_SUA')
  assert.equal(normalizeReviewDecision('YEU_CAU_SUA'), 'CAN_SUA')
  assert.equal(normalizeReviewDecision('TU_CHOI'), 'TU_CHOI')
})

test('NCKH dialog validations enforce required SOP D fields', () => {
  assert.match(validateYeuCauBoSung({ noiDung: 'qua ngan', deadlinePhanHoi: '2026-07-10' }), /20/)
  assert.match(validateYeuCauBoSung({ noiDung: 'Noi dung yeu cau bo sung da du dai' }), /hạn/)
  assert.equal(validateYeuCauBoSung({ noiDung: 'Noi dung yeu cau bo sung da du dai', deadlinePhanHoi: '2026-07-10' }), '')

  assert.match(validateTuChoiHoSo({ lyDo: 'ngan' }), /10/)
  assert.equal(validateTuChoiHoSo({ lyDo: 'Ly do tu choi hop le' }), '')

  assert.match(validateLapToPhanBien({ thanhVienIds: [4], deadlineNop: '2026-07-15' }), /2/)
  assert.match(validateLapToPhanBien({ thanhVienIds: [4, 5] }), /hạn/)
  assert.equal(validateLapToPhanBien({ thanhVienIds: [4, 5], deadlineNop: '2026-07-15' }), '')
})

test('PB result and NCKH review validations match checklist', () => {
  assert.match(validatePbKetQua({ nhanXet: 'qua ngan' }), /20/)
  assert.equal(validatePbKetQua({ nhanXet: 'Nhan xet chi tiet du dai de nop ket qua' }), '')

  assert.equal(validateXetDuyetPB({ quyetDinh: 'CHAP_NHAN' }), '')
  assert.match(validateXetDuyetPB({ quyetDinh: 'CHAP_NHAN' }, getDeTaiById(5).toPhanBien), /tất cả/)
  assert.match(validateXetDuyetPB({ quyetDinh: 'CAN_SUA', noiDung: 'ngan', deadlinePhanHoi: '2026-07-20' }), /20/)
  assert.match(validateXetDuyetPB({ quyetDinh: 'CAN_SUA', noiDung: 'Noi dung chinh sua thuyet minh da du dai' }), /hạn/)
  assert.match(validateXetDuyetPB({ quyetDinh: 'TU_CHOI', ghiChu: 'ngan' }), /10/)
})

test('contract drafting and signing validations protect final transition', () => {
  const yesterday = isoPlusDays(-1)
  const twoDaysAgo = isoPlusDays(-2)
  const today = isoPlusDays(0)
  const tomorrow = isoPlusDays(1)
  const nextMonth = isoPlusDays(30)

  assert.match(validateSoanHopDong({ kinhPhi: 0, ngayBatDau: tomorrow, ngayKetThuc: nextMonth }), /Kinh phí/)
  assert.match(validateSoanHopDong({ kinhPhi: 1000000, ngayBatDau: tomorrow, ngayKetThuc: today }), /sau/)
  assert.match(validateSoanHopDong({ kinhPhi: 1000000, ngayBatDau: yesterday, ngayKetThuc: nextMonth }), /hiện tại/)
  assert.equal(
    validateSoanHopDong(
      { kinhPhi: 1000000, ngayBatDau: yesterday, ngayKetThuc: nextMonth },
      { hopDongStatus: 'CAN_SUA', ngayBatDau: yesterday },
    ),
    '',
  )
  assert.match(
    validateSoanHopDong(
      { kinhPhi: 1000000, ngayBatDau: twoDaysAgo, ngayKetThuc: nextMonth },
      { hopDongStatus: 'CAN_SUA', ngayBatDau: yesterday },
    ),
    /hiện tại/,
  )
  assert.match(validateSoanHopDong({ kinhPhi: 1000000, ngayBatDau: tomorrow, ngayKetThuc: nextMonth, tyLeTamUng: 0.75 }), /50%/)
  assert.equal(validateSoanHopDong({ kinhPhi: 1000000, ngayBatDau: tomorrow, ngayKetThuc: nextMonth, tyLeTamUng: 0.5 }), '')

  assert.match(validateKyHopDong({ ngayKy: today }), /file scan/)
  assert.match(validateKyHopDong({ fileScan: { name: 'hd.pdf' } }), /ngày ký/)
  assert.match(validateKyHopDong({ fileScan: { name: 'hd.pdf' }, ngayKy: tomorrow }), /tương lai/)
  assert.equal(validateKyHopDong({ fileScan: { name: 'hd.pdf' }, ngayKy: today }), '')
})
