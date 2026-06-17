<script setup>
import { useToast } from '@/composables/useToast'
import { CheckCircle, XCircle, Info, AlertTriangle, X } from '@lucide/vue'

const { toasts, remove } = useToast()

const iconMap = {
  success: CheckCircle,
  error:   XCircle,
  warning: AlertTriangle,
  info:    Info,
}
</script>

<template>
  <Teleport to="body">
    <div class="toast-container" aria-live="polite">
      <Transition name="toast" v-for="t in toasts" :key="t.id">
        <div class="toast-item" :class="t.severity">
          <component :is="iconMap[t.severity] ?? Info" :size="18" class="toast-icon" />
          <div class="toast-body">
            <div v-if="t.summary" class="toast-summary">{{ t.summary }}</div>
            <div v-if="t.detail"  class="toast-detail">{{ t.detail }}</div>
          </div>
          <button class="toast-close" @click="remove(t.id)" aria-label="Đóng">
            <X :size="14" />
          </button>
        </div>
      </Transition>
    </div>
  </Teleport>
</template>

<style scoped>
.toast-container {
  position: fixed;
  bottom: var(--space-6);
  right: var(--space-6);
  z-index: 9999;
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  max-width: 360px;
  width: 100%;
}

.toast-item {
  display: flex;
  align-items: flex-start;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
  border-radius: var(--radius-lg);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-lg, 0 8px 24px rgba(0,0,0,.12));
  font: var(--text-body);
  pointer-events: auto;
}

.toast-item.success {
  background: var(--color-success-bg, #f0fdf4);
  border-color: var(--color-success-border, #86efac);
  color: var(--color-success-text, #166534);
}
.toast-item.error {
  background: var(--color-error-bg, #fff0f0);
  border-color: var(--color-error-border, #fca5a5);
  color: var(--color-error-text, #991b1b);
}
.toast-item.warning {
  background: color-mix(in srgb, var(--color-warning) 10%, transparent);
  border-color: color-mix(in srgb, var(--color-warning) 40%, transparent);
  color: var(--color-warning);
}

.toast-icon { flex-shrink: 0; margin-top: 1px; }
.toast-body { flex: 1; min-width: 0; }
.toast-summary { font-weight: 600; }
.toast-detail  { font: var(--text-sm); opacity: .85; margin-top: 2px; }

.toast-close {
  background: none; border: none; cursor: pointer;
  color: currentColor; opacity: .6; padding: 2px;
  border-radius: var(--radius-sm);
  flex-shrink: 0;
}
.toast-close:hover { opacity: 1; background: rgba(0,0,0,.08); }

/* Animations */
.toast-enter-active, .toast-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}
.toast-enter-from { opacity: 0; transform: translateX(24px); }
.toast-leave-to   { opacity: 0; transform: translateX(24px); }
</style>
