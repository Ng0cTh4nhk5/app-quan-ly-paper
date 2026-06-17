/**
 * Lightweight toast composable — replaces PrimeVue's useToast.
 * Uses a single reactive global state so any component can share toasts.
 */
import { ref } from 'vue'

const toasts = ref([])
let _nextId = 0

export function useToast() {
  function add({ severity = 'info', summary = '', detail = '', life = 4000 }) {
    const id = ++_nextId
    toasts.value.push({ id, severity, summary, detail })
    if (life > 0) {
      setTimeout(() => remove(id), life)
    }
  }

  function remove(id) {
    const idx = toasts.value.findIndex(t => t.id === id)
    if (idx !== -1) toasts.value.splice(idx, 1)
  }

  return { toasts, add, remove }
}
