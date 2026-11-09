import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

// Axios 实例: 统一携带 token、统一处理响应与错误
const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 请求拦截器: 自动携带 JWT
request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器: 统一处理业务码
request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code === 200) {
      return res
    }
    if (res.code === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      ElMessage.error('登录已过期, 请重新登录')
      router.push('/login')
      return Promise.reject(new Error(res.message))
    }
    ElMessage.error(res.message || '操作失败')
    return Promise.reject(new Error(res.message))
  },
  error => {
    ElMessage.error(error.message === 'Network Error' ? '网络连接失败, 请检查后端服务' : '请求失败')
    return Promise.reject(error)
  }
)

export default request
