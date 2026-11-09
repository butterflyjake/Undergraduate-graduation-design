import request from '@/utils/request'

/** 用户分页查询 */
export const pageUserApi = params => request.get('/user/page', { params })

/** 新增用户 */
export const addUserApi = data => request.post('/user', data)

/** 修改用户 */
export const updateUserApi = data => request.put('/user', data)

/** 删除用户 */
export const deleteUserApi = id => request.delete(`/user/${id}`)

/** 角色列表(下拉用) */
export const listRoleApi = () => request.get('/role/list')
