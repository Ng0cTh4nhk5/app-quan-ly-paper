import axios from 'axios'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
  headers: { 'Content-Type': 'application/json' },
})

// ── Request: Attach JWT token & handle FormData ────
api.interceptors.request.use(config => {
  const token = localStorage.getItem('rgms_token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  if (typeof FormData !== 'undefined' && config.data instanceof FormData) {
    if (typeof config.headers?.delete === 'function') {
      config.headers.delete('Content-Type')
    } else {
      delete config.headers['Content-Type']
    }
  }
  return config
})

// ── Response: Unwrap API envelope & handle auth errors ────
api.interceptors.response.use(
  res => {
    if (res.data?.success === true && Object.prototype.hasOwnProperty.call(res.data, 'data')) {
      return { ...res, data: res.data.data }
    }
    return res
  },
  err => {
    if (err.response?.status === 401) {
      localStorage.removeItem('rgms_token')
      window.location.href = '/login'
    }
    // A7-1: Removed 403 redirect — handled by router guards (sopDGuards)
    return Promise.reject(err)
  }
)

export default api
