import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { MOCK_USERS } from '@/mock/db.js'

/**
 * Mock JWT: base64(header).base64(payload).FAKESIG
 * Switch sang real JWT: chỉ cần thay hàm login() gọi API thật
 */
/** btoa an arbitrary UTF-8 string (hỗ trợ tiếng Việt) */
function utf8ToB64(str) {
  return btoa(encodeURIComponent(str).replace(/%([0-9A-F]{2})/g,
    (_, p1) => String.fromCharCode(parseInt(p1, 16))))
}
function b64ToUtf8(b64) {
  return decodeURIComponent(Array.from(atob(b64), c =>
    '%' + c.charCodeAt(0).toString(16).padStart(2, '0')).join(''))
}

function makeMockJWT(user) {
  const header  = utf8ToB64(JSON.stringify({ alg: 'HS256', typ: 'JWT' }))
  const payload = utf8ToB64(JSON.stringify({ ...user, exp: Date.now() + 86400000 }))
  return `${header}.${payload}.MOCK_SIG`
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('rgms_token') ?? null)

  const user = computed(() => {
    if (!token.value) return null
    try { return JSON.parse(b64ToUtf8(token.value.split('.')[1])) }
    catch { return null }
  })

  const isLoggedIn = computed(() => !!user.value)
  const role       = computed(() => user.value?.role ?? null)

  // Mock login — chọn role để test
  function loginAs(userKey) {
    const u = MOCK_USERS[userKey]
    if (!u) return
    token.value = makeMockJWT(u)
    localStorage.setItem('rgms_token', token.value)
  }

  function logout() {
    token.value = null
    localStorage.removeItem('rgms_token')
  }

  return { token, user, isLoggedIn, role, loginAs, logout }
})
