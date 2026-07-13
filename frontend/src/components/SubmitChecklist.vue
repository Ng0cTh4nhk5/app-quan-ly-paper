<script setup>
import { computed } from 'vue'
import { Check, X } from '@lucide/vue'
const props = defineProps({
  checks: { type: Array, default: () => [] },
  // [{ label: string, ok: boolean }]
})
const allOk = computed(() => props.checks.every(c => c.ok))
</script>

<template>
  <div class="checklist">
    <div class="checklist-header">
      <span class="checklist-title">аi?u ki?n n?p h? so</span>
      <span :class="['checklist-status', allOk ? 'ok' : 'fail']">
        {{ allOk ? 'ау d? di?u ki?n' : 'Chua d? di?u ki?n' }}
      </span>
    </div>
    <ul class="checklist-list">
      <li v-for="(c, i) in checks" :key="i" class="checklist-item">
        <span :class="['checklist-icon', c.ok ? 'icon-ok' : 'icon-fail']">
          <Check v-if="c.ok" :size="14" stroke-width="2.5" />
          <X v-else :size="14" stroke-width="2.5" />
        </span>
        <span :class="['checklist-label', { 'label-ok': c.ok, 'label-fail': !c.ok }]">
          {{ c.label }}
        </span>
      </li>
    </ul>
  </div>
</template>

<style scoped>
.checklist {
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.checklist-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-3) var(--space-4);
  background: var(--color-surface-alt);
  border-bottom: 1px solid var(--color-border);
}

.checklist-title { font: var(--text-h4); color: var(--color-text-primary); }

.checklist-status {
  font: var(--text-sm);
  font-weight: 600;
  padding: 2px 10px;
  border-radius: 999px;
}
.ok   { background: var(--color-success-bg-subtle); color: var(--color-success-text); }
.fail { background: var(--color-warning-bg-subtle); color: var(--color-warning-text); }

.checklist-list { list-style: none; padding: var(--space-2) 0; margin: 0; }

.checklist-item {
  display: flex;
  align-items: flex-start;
  gap: var(--space-2);
  padding: var(--space-2) var(--space-4);
}

.checklist-icon {
  width: 20px; height: 20px;
  border-radius: 50%;
  display: grid; place-items: center;
  flex-shrink: 0;
  margin-top: 1px;
}
.icon-ok   { background: var(--color-success-bg-subtle); color: var(--color-success-text); }
.icon-fail { background: var(--color-warning-bg-subtle); color: var(--color-warning-text); }

.checklist-label { font: var(--text-body); }
.label-ok   { color: var(--color-text-primary); }
.label-fail { color: var(--color-text-secondary); }
</style>
