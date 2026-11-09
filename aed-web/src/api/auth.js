import request from '@/utils/request'

/** 登录 */
export const loginApi = data => request.post('/auth/login', data)

/** 获取当前登录用户信息 */
export const getInfoApi = () => request.get('/auth/info')

/** 修改自己的密码 */
export const changePasswordApi = data => request.put('/auth/password', data)
