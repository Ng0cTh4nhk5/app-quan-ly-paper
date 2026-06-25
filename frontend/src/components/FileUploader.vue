<script setup>
import { ref } from 'vue'
import { FileText, FileBadge2, File, Download, X, UploadCloud } from '@lucide/vue'
const emit = defineEmits(['upload', 'remove'])

const props = defineProps({
  files: { type: Array, default: () => [] },
  accept: { type: String, default: '.pdf,.doc,.docx' },
  maxMb: { type: Number, default: 20 },
  readonly: { type: Boolean, default: false },
})

const isDragging = ref(false)

function handleFiles(rawFiles) {
  for (const f of rawFiles) {
    if (f.size > props.maxMb * 1024 * 1024) {
      alert(`File "${f.name}" vượt quá ${props.maxMb}MB`)
      continue
    }
    emit('upload', f)
  }
}

function onDrop(e) {
  isDragging.value = false
  handleFiles([...e.dataTransfer.files])
}

function onPick(e) { handleFiles([...e.target.files]); e.target.value = '' }

function fileIcon(name) {
  if (name?.endsWith('.pdf')) return FileText
  if (name?.match(/\.docx?$/)) return FileBadge2
  return File
}
</script>

<template>
  <div class="fu-wrap">
    <!-- Drop zone -->
    <label
      v-if="!readonly"
      class="fu-dropzone"
      :class="{ 'fu-drag': isDragging }"
      @dragover.prevent="isDragging = true"
      @dragleave="isDragging = false"
      @drop.prevent="onDrop"
    >
      <input type="file" :accept="accept" multiple class="fu-input" @change="onPick" />
      <div class="fu-icon">
        <UploadCloud :size="24" stroke-width="1.5" />
      </div>
      <p class="fu-hint">Kéo thả hoặc <span class="fu-link">chọn file</span></p>
      <p class="fu-sub">{{ accept }} · tối đa {{ maxMb }}MB</p>
    </label>

    <!-- File list -->
    <ul v-if="files.length" class="fu-list">
      <li v-for="f in files" :key="f.id" class="fu-item">
        <span class="fu-item-icon">
          <component :is="fileIcon(f.tenFile)" :size="18" />
        </span>
        <div class="fu-item-info">
          <span class="fu-item-name">{{ f.tenFile }}</span>
          <span class="fu-item-size">{{ f.size }}</span>
        </div>
        <a :href="f.downloadUrl" target="_blank" class="fu-item-btn" title="Tải về"><Download :size="14" /></a>
        <button v-if="!readonly" class="fu-item-del" @click="$emit('remove', f.id)" title="Xóa"><X :size="14" /></button>
      </li>
    </ul>

    <p v-else-if="readonly" class="fu-empty">Chưa có tài liệu nào.</p>
  </div>
</template>

<style scoped>
.fu-wrap { display: flex; flex-direction: column; gap: var(--space-3); }

.fu-dropzone {
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  gap: var(--space-2);
  padding: var(--space-6) var(--space-4);
  border: 2px dashed var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--color-surface-alt);
  cursor: pointer;
  transition: border-color 160ms, background 160ms;
}
.fu-dropzone:hover, .fu-drag {
  border-color: var(--color-accent);
  background: var(--color-accent-light);
}
.fu-input { display: none; }
.fu-icon { color: var(--color-text-muted); }
.fu-hint { font: var(--text-body); color: var(--color-text-secondary); margin: 0; }
.fu-link { color: var(--color-accent); font-weight: 600; text-decoration: underline; }
.fu-sub  { font: var(--text-sm); color: var(--color-text-muted); margin: 0; }

.fu-list { list-style: none; padding: 0; margin: 0; display: flex; flex-direction: column; gap: var(--space-2); }
.fu-item {
  display: flex; align-items: center; gap: var(--space-2);
  padding: var(--space-2) var(--space-3);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
}
.fu-item-icon { display: flex; align-items: center; color: var(--color-text-muted); flex-shrink: 0; }
.fu-item-info { flex: 1; min-width: 0; }
.fu-item-name { display: block; font: var(--text-body); color: var(--color-text-primary); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.fu-item-size { font: var(--text-sm); color: var(--color-text-muted); }
.fu-item-btn, .fu-item-del {
  flex-shrink: 0; background: none; border: none;
  width: 28px; height: 28px; border-radius: var(--radius-sm);
  display: grid; place-items: center; cursor: pointer;
  font-size: 14px; color: var(--color-text-muted);
  transition: background 120ms, color 120ms;
}
.fu-item-btn:hover { background: var(--color-border); color: var(--color-accent); }
.fu-item-del:hover { background: var(--color-error-bg); color: var(--color-error-text); }
.fu-empty { font: var(--text-sm); color: var(--color-text-muted); text-align: center; padding: var(--space-4) 0; margin: 0; }
</style>
