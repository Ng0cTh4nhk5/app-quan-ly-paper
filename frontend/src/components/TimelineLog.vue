<script setup>
const props = defineProps({
  logs: { type: Array, default: () => [] },
})

function fmtDate(iso) {
  if (!iso) return ''
  const d = new Date(iso)
  return d.toLocaleString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' })
}

const sorted = computed(() => [...props.logs].reverse())
</script>

<script>
import { computed } from 'vue'
</script>

<template>
  <div class="timeline">
    <div v-if="!logs.length" class="timeline-empty">
      Chưa có hoạt động nào được ghi nhận.
    </div>
    <div
      v-for="(log, i) in sorted"
      :key="i"
      class="timeline-item"
      :style="{ animationDelay: `${i * 60}ms` }"
    >
      <div class="timeline-line">
        <div class="timeline-dot"></div>
        <div v-if="i < sorted.length - 1" class="timeline-connector"></div>
      </div>
      <div class="timeline-content">
        <p class="timeline-action">{{ log.action }}</p>
        <p class="timeline-meta">
          <span class="timeline-actor">{{ log.actor }}</span>
          <span class="timeline-sep">·</span>
          <span class="timeline-time">{{ fmtDate(log.createdAt) }}</span>
        </p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.timeline { display: flex; flex-direction: column; gap: 0; }

.timeline-empty {
  font: var(--text-body);
  color: var(--color-text-muted);
  text-align: center;
  padding: var(--space-8) 0;
}

.timeline-item {
  display: flex;
  gap: var(--space-3);
  animation: tl-in 240ms ease both;
}

@keyframes tl-in {
  from { opacity: 0; transform: translateX(-8px); }
  to   { opacity: 1; transform: translateX(0); }
}

.timeline-line {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex-shrink: 0;
  width: 20px;
}

.timeline-dot {
  width: 8px; height: 8px;
  border-radius: 50%;
  background: var(--gradient-primary);
  border: 2px solid var(--color-surface);
  box-shadow: 0 0 0 2px var(--color-accent-light);
  flex-shrink: 0;
  margin-top: 4px;
}

.timeline-connector {
  width: 2px;
  flex: 1;
  min-height: 20px;
  background: var(--color-border);
  margin: 4px 0;
}

.timeline-content {
  padding-bottom: var(--space-4);
  flex: 1;
}

.timeline-action {
  font: var(--text-h4);
  color: var(--color-text-primary);
  margin: 0 0 2px;
}

.timeline-meta {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  font: var(--text-sm);
  color: var(--color-text-muted);
  margin: 0;
}

.timeline-sep { opacity: 0.4; }
</style>
