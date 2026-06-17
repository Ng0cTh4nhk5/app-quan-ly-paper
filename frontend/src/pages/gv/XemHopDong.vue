<script setup>
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useDetaiStore } from '@/stores/detai.store'

const route  = useRoute()
const router = useRouter()
const store  = useDetaiStore()
const { chiTiet, loading } = storeToRefs(store)
onMounted(() => store.layChiTiet(route.params.id))

function fmt(iso) { return iso ? new Date(iso).toLocaleDateString('vi-VN') : '—' }
function fmtM(n)  { return n?.toLocaleString('vi-VN') + ' đ' }
</script>

<template>
  <div class="page-container">
    <button class="btn btn-ghost btn-sm mb-4" @click="router.back()">← Quay lại</button>

    <div class="page-header">
      <h1 class="page-title">Xem hợp đồng</h1>
      <button class="btn btn-secondary" onclick="window.print()">🖨️ In hợp đồng</button>
    </div>

    <div v-if="loading" class="skeleton" style="height:400px"></div>

    <div v-else-if="chiTiet" class="hop-dong-card">
      <div class="hop-dong-header">
        <div class="hop-dong-seal">HĐ</div>
        <div>
          <h2 class="hop-dong-title">HỢP ĐỒNG NGHIÊN CỨU KHOA HỌC</h2>
          <p class="hop-dong-sub">Kỳ NCKH: {{ chiTiet.kyNckh }}</p>
        </div>
      </div>

      <dl class="info-list mt-4">
        <dt>Mã đề tài</dt>   <dd class="mono">{{ chiTiet.maSo }}</dd>
        <dt>Tên đề tài</dt>  <dd>{{ chiTiet.tenDeTai }}</dd>
        <dt>Chủ nhiệm</dt>   <dd>{{ chiTiet.giangVien?.hoTen }}</dd>
        <dt>Kinh phí</dt>    <dd>{{ chiTiet.kinhPhi ? fmtM(chiTiet.kinhPhi) : '—' }}</dd>
        <dt>Ngày lập</dt>    <dd>{{ fmt(chiTiet.updatedAt) }}</dd>
        <dt>Tình trạng</dt>
        <dd>
          <span class="badge badge-warning" v-if="chiTiet.trangThai === 'DANG_LAP_HOP_DONG'">
            Chờ ký kết
          </span>
          <span class="badge badge-success" v-else>Đã ký</span>
        </dd>
      </dl>

      <div class="hop-dong-footer">
        <div class="sig-block">
          <div class="sig-label">Chủ nhiệm đề tài</div>
          <div class="sig-line"></div>
          <div class="sig-name">{{ chiTiet.giangVien?.hoTen }}</div>
        </div>
        <div class="sig-block">
          <div class="sig-label">Trưởng P.NCKH</div>
          <div class="sig-line"></div>
          <div class="sig-name">Đã duyệt</div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.hop-dong-card {
  background: var(--color-surface); border: 1px solid var(--color-border);
  border-radius: var(--radius-lg); padding: var(--space-8); max-width: 720px;
}
.hop-dong-header  { display: flex; align-items: center; gap: var(--space-4); border-bottom: 2px solid var(--color-border); padding-bottom: var(--space-4); margin-bottom: var(--space-2); }
.hop-dong-seal    { width: 56px; height: 56px; border-radius: 50%; background: var(--color-accent); color: #fff; font: 700 18px/1 var(--font-sans); display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.hop-dong-title   { font: var(--text-h2); color: var(--color-text-primary); }
.hop-dong-sub     { font: var(--text-body); color: var(--color-text-muted); margin-top: 2px; }

.info-list {
  display: grid; grid-template-columns: 140px 1fr; row-gap: var(--space-3); column-gap: var(--space-4);
}
.info-list dt { font: var(--text-sm); color: var(--color-text-muted); }
.info-list dd { font: var(--text-body); color: var(--color-text-primary); }

.hop-dong-footer {
  display: flex; justify-content: space-around;
  border-top: 1px solid var(--color-border); margin-top: var(--space-8); padding-top: var(--space-6);
}
.sig-block { text-align: center; }
.sig-label { font: var(--text-sm); color: var(--color-text-muted); margin-bottom: var(--space-6); }
.sig-line  { width: 160px; border-bottom: 1px solid var(--color-border-strong); margin-bottom: var(--space-2); }
.sig-name  { font: var(--text-body); color: var(--color-text-primary); }
</style>
