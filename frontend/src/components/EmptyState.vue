<script setup>
defineProps({
  icon:    { type: String, default: 'pi pi-inbox' },
  title:   { type: String, default: 'Không có dữ liệu' },
  message: { type: String, default: '' },
  action:  { type: String, default: '' },
})
const emit = defineEmits(['action'])
</script>

<template>
  <div class="empty-state">
    <i v-if="icon.startsWith('pi')" :class="icon" class="empty-icon-pi"></i>
    <div v-else class="empty-icon">{{ icon }}</div>
    <h3 class="empty-title">{{ title }}</h3>
    <p v-if="message" class="empty-msg">{{ message }}</p>
    <button v-if="action" class="btn btn-primary mt-4" @click="emit('action')">{{ action }}</button>
  </div>
</template>

<style scoped>
.empty-state {
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  padding: 64px 24px; text-align: center;
}
.empty-icon  { font-size: 40px; margin-bottom: 16px; }
.empty-icon-pi { font-size: 3rem; color: var(--color-text-muted); margin-bottom: 16px; display: block; }
.empty-title { font: var(--text-h3); color: var(--color-text-primary); margin-bottom: 8px; }
.empty-msg   { font: var(--text-body); color: var(--color-text-muted); max-width: 360px; }
</style>
