<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useNckhStore } from '@/stores/nckh.store'
import { useToast } from '@/composables/useToast'
import { MOCK_USERS } from '@/mock/db.js'
import StatusBadge from '@/components/StatusBadge.vue'
import { CheckCircle, Edit3, ClipboardList, Users, FileText, X, ChevronLeft } from '@lucide/vue'

const route  = useRoute()
const router = useRouter()
const store  = useNckhStore()
const toast  = useToast()
const { chiTiet, loading } = storeToRefs(store)

// Dialogs
const showYeuCauDialog  = ref(false)
const showPBDialog      = ref(false)
const showXetDuyetDialog = ref(false)
const lyDoYeuCau = ref('')
const selectedPB = ref([])
const ketQuaPB   = ref('CHAP_THUAN')
const ghiChuPB   = ref('')

onMounted(() => store.layChiTiet(route.params.id))

const canTiepNhan    = computed(() => chiTiet.value?.trangThai === 'CHO_PNCKH_XEM_XET')
const canYeuCauBoSung= computed(() => chiTiet.value?.trangThai === 'DANG_XEM_XET_BOI_PNCKH')
const canLapPB       = computed(() => chiTiet.value?.trangThai === 'DANG_XEM_XET_BOI_PNCKH')
const canXetDuyetPB  = computed(() => chiTiet.value?.trangThai === 'DANG_PHAN_BIEN')
const canKyHopDong = computed(() =>
  chiTiet.value?.trangThai === 'DANG_LAP_HOP_DONG' &&
  chiTiet.value?.gvDaDongYHopDong &&
  !chiTiet.value?.hopDongFeedback
)

const pbMembers = Object.values(MOCK_USERS).filter(u => u.role === 'TO_PHAN_BIEN')

async function tiepNhan() {
  try {
    await store.tiepNhan(route.params.id)
    toast.add({ severity: 'success', summary: 'Đã tiếp nhận', life: 3000 })
  } catch { toast.add({ severity: 'error', summary: 'Lỗi', life: 3000 }) }
}

async function submitYeuCau() {
  if (!lyDoYeuCau.value.trim()) return
  try {
    await store.yeuCauBoSung(route.params.id, { lyDo: lyDoYeuCau.value })
    showYeuCauDialog.value = false
    lyDoYeuCau.value = ''
    toast.add({ severity: 'success', summary: 'Đã gửi yêu cầu bổ sung', life: 3000 })
  } catch { toast.add({ severity: 'error', summary: 'Lỗi', life: 3000 }) }
}

async function submitLapPB() {
  if (!selectedPB.value.length) return
  try {
    await store.lapToPhanBien(route.params.id, selectedPB.value)
    showPBDialog.value = false
    selectedPB.value = []
    toast.add({ severity: 'success', summary: 'Đã lập tổ phản biện', life: 3000 })
  } catch { toast.add({ severity: 'error', summary: 'Lỗi', life: 3000 }) }
}

async function submitXetDuyetPB() {
  try {
    await store.xetDuyetPB(route.params.id, { ketQua: ketQuaPB.value, ghiChu: ghiChuPB.value })
    showXetDuyetDialog.value = false
    toast.add({ severity: 'success', summary: 'Đã cập nhật kết quả PB', life: 3000 })
  } catch { toast.add({ severity: 'error', summary: 'Lỗi', life: 3000 }) }
}

async function kyHopDong() {
  try {
    await store.kyHopDong(route.params.id)
    toast.add({ severity: 'success', summary: 'Đã ký hợp đồng', life: 3000 })
  } catch { toast.add({ severity: 'error', summary: 'Lỗi', life: 3000 }) }
}

function fmt(iso) { return iso ? new Date(iso).toLocaleString('vi-VN') : '—' }
function fmtM(n)  { return n?.toLocaleString('vi-VN') + ' đ' }
</script>

<template>
  <div class="page-container">
    <button class="btn btn-ghost btn-sm mb-4 flex items-center gap-2" @click="router.back()">
      <ChevronLeft :size="16" /> Quay lại
    </button>

    <div v-if="loading" class="skeleton" style="height:200px"></div>

    <template v-else-if="chiTiet">
      <!-- Header -->
      <div class="page-header">
        <div class="page-header-left">
          <div class="flex items-center gap-3 mb-2">
            <StatusBadge :status="chiTiet.trangThai" />
            <span class="mono text-muted text-sm">{{ chiTiet.maSo }}</span>
          </div>
          <h1 class="page-title">{{ chiTiet.tenDeTai }}</h1>
        </div>
        <div class="action-bar">
          <button v-if="canTiepNhan" class="btn btn-primary flex items-center gap-2" @click="tiepNhan"><CheckCircle :size="16" /> Tiếp nhận</button>
          <button v-if="canYeuCauBoSung" class="btn btn-warning flex items-center gap-2" @click="showYeuCauDialog = true"><ClipboardList :size="16" /> Yêu cầu bổ sung</button>
          <button v-if="canLapPB" class="btn btn-secondary flex items-center gap-2" @click="showPBDialog = true"><Users :size="16" /> Lập tổ PB</button>
          <button v-if="canXetDuyetPB" class="btn btn-primary flex items-center gap-2" @click="showXetDuyetDialog = true"><ClipboardList :size="16" /> Kết quả PB</button>
          <button v-if="canKyHopDong" class="btn btn-success flex items-center gap-2" @click="kyHopDong"><Edit3 :size="16" /> Ký hợp đồng</button>
          <button v-if="chiTiet.trangThai === 'DANG_LAP_HOP_DONG'" class="btn btn-secondary flex items-center gap-2" @click="router.push(`/nckh/de-tai/${chiTiet.id}/hop-dong`)"><FileText :size="16" /> Soạn HĐ</button>
        </div>
      </div>

      <!-- Info -->
      <div class="detail-grid">
        <div class="detail-card">
          <h3 class="detail-card-title">Thông tin đề tài</h3>
          <dl class="info-list">
            <dt>Giảng viên</dt> <dd>{{ chiTiet.giangVien?.hoTen }}</dd>
            <dt>Kỳ NCKH</dt>  <dd>{{ chiTiet.kyNckh }}</dd>
            <dt>Lĩnh vực</dt> <dd>{{ chiTiet.linhVuc || '—' }}</dd>
            <dt>Kinh phí</dt> <dd>{{ chiTiet.kinhPhi ? fmtM(chiTiet.kinhPhi) : '—' }}</dd>
            <dt>Cập nhật</dt> <dd>{{ fmt(chiTiet.updatedAt) }}</dd>
          </dl>
        </div>
        <div class="detail-card">
          <h3 class="detail-card-title">Mô tả</h3>
          <p class="detail-desc">{{ chiTiet.moTa || '(Chưa có)' }}</p>
        </div>
      </div>

      <!-- Tổ PB -->
      <div v-if="chiTiet.toPhanBien?.length" class="detail-card mt-4">
        <h3 class="detail-card-title">Tổ phản biện được phân công</h3>
        <ul class="pb-list">
          <li v-for="pb in chiTiet.toPhanBien" :key="pb.id">
            <span class="pb-avatar">{{ pb.hoTen.split(' ').at(-1)[0] }}</span>
            {{ pb.hoTen }}
            <span class="text-muted text-sm ml-2">{{ pb.email }}</span>
          </li>
        </ul>
      </div>
    </template>

    <!-- ─── Dialog: Yêu cầu bổ sung ─── -->
    <Teleport to="body">
      <div v-if="showYeuCauDialog" class="dialog-overlay" @click.self="showYeuCauDialog = false">
        <div class="dialog">
          <div class="dialog-header">
            <h3>Yêu cầu bổ sung hồ sơ</h3>
            <button class="dialog-close" @click="showYeuCauDialog = false"><X :size="20" /></button>
          </div>
          <div class="dialog-body">
            <div class="form-group">
              <label class="form-label">Nội dung yêu cầu <span class="required">*</span></label>
              <textarea v-model="lyDoYeuCau" class="form-textarea" rows="5"
                placeholder="Mô tả rõ tài liệu/thông tin cần bổ sung..."></textarea>
            </div>
          </div>
          <div class="dialog-footer">
            <button class="btn btn-secondary" @click="showYeuCauDialog = false">Hủy</button>
            <button class="btn btn-warning" @click="submitYeuCau">Gửi yêu cầu</button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- ─── Dialog: Lập tổ PB ─── -->
    <Teleport to="body">
      <div v-if="showPBDialog" class="dialog-overlay" @click.self="showPBDialog = false">
        <div class="dialog">
          <div class="dialog-header">
            <h3>Lập tổ phản biện</h3>
            <button class="dialog-close" @click="showPBDialog = false"><X :size="20" /></button>
          </div>
          <div class="dialog-body">
            <p class="text-muted text-sm mb-3">Chọn ít nhất 1 thành viên tổ phản biện:</p>
            <div class="pb-member-list">
              <label v-for="pb in pbMembers" :key="pb.id" class="pb-member-item">
                <input type="checkbox" :value="pb.id" v-model="selectedPB" />
                <span class="pb-member-name">{{ pb.hoTen }}</span>
                <span class="text-muted text-sm">{{ pb.email }}</span>
              </label>
            </div>
          </div>
          <div class="dialog-footer">
            <button class="btn btn-secondary" @click="showPBDialog = false">Hủy</button>
            <button class="btn btn-primary" :disabled="!selectedPB.length" @click="submitLapPB">Xác nhận</button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- ─── Dialog: Kết quả PB ─── -->
    <Teleport to="body">
      <div v-if="showXetDuyetDialog" class="dialog-overlay" @click.self="showXetDuyetDialog = false">
        <div class="dialog">
          <div class="dialog-header">
            <h3>Nhập kết quả phản biện</h3>
            <button class="dialog-close" @click="showXetDuyetDialog = false"><X :size="20" /></button>
          </div>
          <div class="dialog-body">
            <div class="form-group">
              <label class="form-label">Kết quả</label>
              <select v-model="ketQuaPB" class="form-select">
                <option value="CHAP_THUAN">Chấp thuận</option>
                <option value="CHO_CHINH_SUA">Yêu cầu chỉnh sửa thuyết minh</option>
                <option value="TU_CHOI">Từ chối</option>
              </select>
            </div>
            <div class="form-group">
              <label class="form-label">Ghi chú</label>
              <textarea v-model="ghiChuPB" class="form-textarea" rows="4" placeholder="Ghi chú thêm..."></textarea>
            </div>
          </div>
          <div class="dialog-footer">
            <button class="btn btn-secondary" @click="showXetDuyetDialog = false">Hủy</button>
            <button class="btn btn-primary" @click="submitXetDuyetPB">Xác nhận</button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<style scoped>
.action-bar { display: flex; gap: var(--space-2); flex-wrap: wrap; }
.detail-grid {
  display: grid; grid-template-columns: 320px 1fr; gap: var(--space-4);
}
.detail-card {
  background: var(--color-surface); border: 1px solid var(--color-border);
  border-radius: var(--radius-lg); padding: var(--space-5);
}
.detail-card-title { font: var(--text-h4); color: var(--color-text-primary); margin-bottom: var(--space-4); }
.info-list {
  display: grid; grid-template-columns: auto 1fr; row-gap: var(--space-3); column-gap: var(--space-4); align-items: center;
}
.info-list dt { font: var(--text-sm); color: var(--color-text-muted); }
.info-list dd { font: var(--text-body); color: var(--color-text-primary); }
.detail-desc { font: var(--text-body); color: var(--color-text-secondary); white-space: pre-wrap; }

.pb-list { list-style: none; display: flex; flex-direction: column; gap: var(--space-2); }
.pb-list li { display: flex; align-items: center; gap: var(--space-2); }
.pb-avatar {
  width: 28px; height: 28px; border-radius: 50%;
  background: var(--color-accent); color: #fff;
  font: 600 12px/1 var(--font-sans); display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}

.pb-member-list { display: flex; flex-direction: column; gap: var(--space-2); }
.pb-member-item {
  display: flex; align-items: center; gap: var(--space-2);
  padding: var(--space-2) var(--space-3);
  border: 1px solid var(--color-border); border-radius: var(--radius-md);
  cursor: pointer;
}
.pb-member-item:hover { background: var(--color-surface-alt); }
.pb-member-name { flex: 1; font: var(--text-body); color: var(--color-text-primary); }

@media (max-width: 768px) { .detail-grid { grid-template-columns: 1fr; } }
</style>
