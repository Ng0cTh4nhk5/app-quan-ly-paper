import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/api/axios'

function b64ToUtf8(b64) {
  return decodeURIComponent(Array.from(atob(b64), c =>
    '%' + c.charCodeAt(0).toString(16).padStart(2, '0')).join(''))
}

function normalizeJwtPayload(payload) {
  return {
    ...payload,
    id: payload.id ?? (payload.sub ? Number(payload.sub) : undefined),
    hoTen: payload.hoTen ?? payload.name ?? payload.username,
  }
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('rgms_token') ?? null)

  const user = computed(() => {
    if (!token.value) return null
    try { return normalizeJwtPayload(JSON.parse(b64ToUtf8(token.value.split('.')[1]))) }
    catch { return null }
  })

  const isLoggedIn = computed(() => !!user.value)
  const role       = computed(() => user.value?.role ?? null)

  async function login(credentials) {
    const res = await api.post('/auth/login', credentials)
    const nextToken = res.data?.token ?? res.data?.accessToken ?? res.data?.jwt
    if (!nextToken) throw new Error('Đăng nhập thành công nhưng API không trả về token.')
    token.value = nextToken
    localStorage.setItem('rgms_token', token.value)
    return user.value
  }

  function logout() {
    token.value = null
    localStorage.removeItem('rgms_token')
  }

  return { token, user, isLoggedIn, role, login, logout }
})
