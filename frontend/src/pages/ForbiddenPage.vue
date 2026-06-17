<script setup>
import { useRouter } from 'vue-router'
const router = useRouter()
</script>

<template>
  <div class="forbidden-page">
    <div class="forbidden-card">
      <div class="forbidden-code">403</div>
      <h1>Không có quyền truy cập</h1>
      <p>Bạn không có quyền xem trang này.</p>
      <button class="btn btn-primary mt-6" @click="router.push('/login')">
        Về trang đăng nhập
      </button>
    </div>
  </div>
</template>

<style scoped>
.forbidden-page {
  min-height: 100svh;
  display: flex; align-items: center; justify-content: center;
  background: var(--color-bg);
}
.forbidden-card {
  text-align: center; padding: var(--space-10);
}
.forbidden-code {
  font: 800 80px/1 var(--font-sans);
  color: var(--color-border-strong);
  letter-spacing: -0.05em;
  margin-bottom: var(--space-4);
}
</style>
