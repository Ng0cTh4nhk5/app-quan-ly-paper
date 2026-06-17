<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useDetaiStore } from '@/stores/detai.store'

const route  = useRoute()
const router = useRouter()
const store  = useDetaiStore()
const { chiTiet, loading } = storeToRefs(store)
const signing = ref(false)
const signed  = ref(false)

onMounted(() => store.layChiTiet(route.params.id))

const canKy = computed(() =>
  chiTiet.value?.trangThai === 'DANG_LAP_HOP_DONG' && !signed.value
)

async function kyHopDong() {
  signing.value = true
  try {
    await store.kyHopDong(route.params.id)
    signed.value = true
  } catch (e) {
    alert(e.response?.data?.message ?? 'Ký hợp đồng thất bại.')
  } finally {
    signing.value = false
  }
}

function fmt(iso) { return iso ? new Date(iso).toLocaleDateString('vi-VN') : '—' }
function fmtM(n)  { return n ? n.toLocaleString('vi-VN') + ' đ' : '—' }
</script>

<template>
  <div>
    <button class="btn btn-ghost btn-sm mb-4" @click="router.back()">← Quay lại</button>

    <!-- Success notice after signing -->
    <div v-if="signed" class="alert-success mb-4">
      ✅ Bạn đã ký xác nhận hợp đồng thành công. Đề tài chuyển sang trạng thái <strong>Đang thực hiện</strong>.
      <button class="btn btn-ghost btn-sm ml-4" @click="router.push('/gv/dashboard')">Về trang chủ</button>
    </div>

    <div v-if="loading" style="display:flex;flex-direction:column;gap:12px">
      <div class="skeleton" style="height:60px;border-radius:8px"></div>
      <div class="skeleton" style="height:300px;border-radius:8px"></div>
    </div>

    <div v-else-if="chiTiet" class="hop-dong-wrapper">
      <!-- Action bar -->
      <div class="page-header mb-4">
        <div class="page-header-left">
          <h1 class="page-title">Hợp đồng nghiên cứu khoa học</h1>
          <p class="page-subtitle mono">{{ chiTiet.maSo }}</p>
        </div>
        <div style="display:flex;gap:var(--space-2)">
          <button class="btn btn-secondary" onclick="window.print()">🖨️ In hợp đồng</button>
          <button
            v-if="canKy"
            class="btn btn-primary"
            :disabled="signing"
            @click="kyHopDong"
          >
            <span v-if="signing" class="spinner" style="width:14px;height:14px;border-width:2px"></span>
            ✍️ {{ signing ? 'Đang ký...' : 'Ký xác nhận' }}
          </button>
          <span v-else-if="chiTiet.trangThai === 'DANG_THUC_HIEN'" class="badge badge-success">
            ✅ Đã ký
          </span>
        </div>
      </div>

      <!-- Contract document -->
      <div class="hop-dong-doc">
        <!-- Header seal -->
        <div class="hop-dong-head print-only-show">
          <div class="hop-dong-logo">
            <div class="seal-ring">HĐ</div>
          </div>
          <div class="hop-dong-title-block">
            <p class="hop-dong-school">TRƯỜNG ĐẠI HỌC — PHÒNG NGHIÊN CỨU KHOA HỌC</p>
            <h2 class="hop-dong-main-title">HỢP ĐỒNG NGHIÊN CỨU KHOA HỌC</h2>
            <p class="hop-dong-sub-title">Kỳ NCKH: {{ chiTiet.kyNckh }}</p>
          </div>
        </div>

        <!-- Status banner -->
        <div
          class="status-banner"
          :class="chiTiet.trangThai === 'DANG_THUC_HIEN' ? 'signed' : 'pending'"
        >
          <span v-if="chiTiet.trangThai === 'DANG_THUC_HIEN'">✅ Hợp đồng đã có hiệu lực — Cả hai bên đã ký</span>
          <span v-else>⏳ Đang chờ xác nhận ký kết của Chủ nhiệm đề tài</span>
        </div>

        <!-- Main content -->
        <div class="contract-body">
          <section class="contract-section">
            <h3 class="section-title">I. THÔNG TIN ĐỀ TÀI</h3>
            <dl class="contract-dl">
              <div class="contract-row">
                <dt>Tên đề tài</dt>
                <dd>{{ chiTiet.tenDeTai }}</dd>
              </div>
              <div class="contract-row">
                <dt>Mã số</dt>
                <dd class="mono">{{ chiTiet.maSo }}</dd>
              </div>
              <div class="contract-row">
                <dt>Lĩnh vực</dt>
                <dd>{{ chiTiet.linhVuc || '—' }}</dd>
              </div>
              <div class="contract-row">
                <dt>Kỳ NCKH</dt>
                <dd>{{ chiTiet.kyNckh }}</dd>
              </div>
              <div class="contract-row">
                <dt>Kinh phí</dt>
                <dd class="money">{{ fmtM(chiTiet.kinhPhi) }}</dd>
              </div>
              <div class="contract-row">
                <dt>Thời gian</dt>
                <dd>{{ chiTiet.thoiGianThucHien ? chiTiet.thoiGianThucHien + ' tháng' : '—' }}</dd>
              </div>
            </dl>
          </section>

          <section class="contract-section">
            <h3 class="section-title">II. CHỦ NHIỆM ĐỀ TÀI</h3>
            <dl class="contract-dl">
              <div class="contract-row">
                <dt>Họ và tên</dt>
                <dd>{{ chiTiet.giangVien?.hoTen ?? chiTiet.chuNhiem }}</dd>
              </div>
              <div class="contract-row">
                <dt>Đơn vị</dt>
                <dd>{{ chiTiet.giangVien?.khoa ?? '—' }}</dd>
              </div>
            </dl>
          </section>

          <section class="contract-section" v-if="chiTiet.ghiChu">
            <h3 class="section-title">III. ĐIỀU KHOẢN ĐẶC BIỆT</h3>
            <p class="contract-notes">{{ chiTiet.ghiChu }}</p>
          </section>

          <!-- Signature block -->
          <div class="sig-row">
            <div class="sig-block">
              <div class="sig-label">Chủ nhiệm đề tài</div>
              <div class="sig-area" :class="{ done: chiTiet.trangThai === 'DANG_THUC_HIEN' }">
                <span v-if="chiTiet.trangThai === 'DANG_THUC_HIEN'" class="sig-check">✍️</span>
              </div>
              <div class="sig-name">{{ chiTiet.giangVien?.hoTen ?? chiTiet.chuNhiem }}</div>
              <div class="sig-date">{{ chiTiet.trangThai === 'DANG_THUC_HIEN' ? fmt(chiTiet.updatedAt) : '' }}</div>
            </div>
            <div class="sig-block">
              <div class="sig-label">Trưởng P.NCKH</div>
              <div class="sig-area done">
                <span class="sig-check">✍️</span>
              </div>
              <div class="sig-name">P.NCKH</div>
              <div class="sig-date">{{ fmt(chiTiet.updatedAt) }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.hop-dong-wrapper { max-width: 780px; }

.alert-success {
  display: flex; align-items: center;
  background: color-mix(in srgb, var(--color-success) 10%, transparent);
  border: 1px solid color-mix(in srgb, var(--color-success) 30%, transparent);
  border-radius: var(--radius-lg); padding: var(--space-3) var(--space-5);
  color: var(--color-success); font: var(--text-body);
}

.hop-dong-doc {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-md);
}

/* Document header */
.hop-dong-head {
  background: linear-gradient(135deg, var(--color-accent) 0%, color-mix(in srgb, var(--color-accent) 70%, #000) 100%);
  padding: var(--space-6);
  display: flex; align-items: center; gap: var(--space-5);
}
.seal-ring {
  width: 64px; height: 64px; border-radius: 50%;
  border: 3px solid rgba(255,255,255,0.6);
  background: rgba(255,255,255,0.15);
  display: flex; align-items: center; justify-content: center;
  font: 700 20px/1 var(--font-sans); color: #fff;
}
.hop-dong-school {
  font: var(--text-sm); color: rgba(255,255,255,0.8);
  text-transform: uppercase; letter-spacing: 0.05em;
}
.hop-dong-main-title {
  font: 700 20px/1.2 var(--font-sans); color: #fff; margin: 4px 0;
}
.hop-dong-sub-title {
  font: var(--text-sm); color: rgba(255,255,255,0.7);
}

/* Status banner */
.status-banner {
  padding: var(--space-3) var(--space-6);
  font: var(--text-sm); font-weight: 600;
  text-align: center;
}
.status-banner.signed {
  background: color-mix(in srgb, var(--color-success) 10%, transparent);
  color: var(--color-success);
}
.status-banner.pending {
  background: color-mix(in srgb, var(--color-warning) 10%, transparent);
  color: var(--color-warning);
}

/* Contract body */
.contract-body { padding: var(--space-6); }
.contract-section { margin-bottom: var(--space-6); }
.section-title {
  font: var(--text-h4); color: var(--color-text-primary);
  border-bottom: 1px solid var(--color-border);
  padding-bottom: var(--space-2); margin-bottom: var(--space-4);
  text-transform: uppercase; letter-spacing: 0.05em; font-size: 13px;
}

.contract-dl { display: flex; flex-direction: column; gap: var(--space-2); }
.contract-row {
  display: grid; grid-template-columns: 180px 1fr;
  gap: var(--space-4); align-items: baseline;
  padding: var(--space-2) 0;
  border-bottom: 1px solid var(--color-border);
}
.contract-row:last-child { border-bottom: none; }
.contract-row dt { font: var(--text-sm); color: var(--color-text-muted); }
.contract-row dd { font: var(--text-body); color: var(--color-text-primary); }
.contract-row dd.money { font-weight: 600; color: var(--color-accent); font-size: 15px; }

.contract-notes {
  font: var(--text-body); color: var(--color-text-secondary);
  white-space: pre-wrap; line-height: 1.7;
  background: var(--color-surface-alt); border-radius: var(--radius-md);
  padding: var(--space-3) var(--space-4);
}

/* Signature */
.sig-row {
  display: flex; justify-content: space-around;
  border-top: 1px solid var(--color-border);
  padding-top: var(--space-6); margin-top: var(--space-6);
}
.sig-block { text-align: center; }
.sig-label {
  font: var(--text-sm); color: var(--color-text-muted);
  text-transform: uppercase; letter-spacing: 0.05em;
  margin-bottom: var(--space-3);
}
.sig-area {
  width: 160px; height: 60px; margin: 0 auto var(--space-2);
  border-bottom: 2px solid var(--color-border);
  display: flex; align-items: center; justify-content: center;
  transition: border-color var(--transition-fast);
}
.sig-area.done { border-color: var(--color-accent); }
.sig-check { font-size: 28px; }
.sig-name { font: var(--text-body); color: var(--color-text-primary); font-weight: 600; }
.sig-date { font: var(--text-sm); color: var(--color-text-muted); margin-top: 2px; }

@media print {
  .btn, .page-header { display: none !important; }
}
</style>
