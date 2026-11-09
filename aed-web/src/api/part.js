import request from '@/utils/request'

/** 配件分页查询 */
export const pagePartApi = params => request.get('/part/page', { params })

/** 某设备配件列表(不分页) */
export const listPartApi = params => request.get('/part/list', { params })

/** 新增配件(自动替换旧配件) */
export const addPartApi = data => request.post('/part', data)

/** 修改配件 */
export const updatePartApi = data => request.put('/part', data)

/** 删除配件 */
export const deletePartApi = id => request.delete(`/part/${id}`)
