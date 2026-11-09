import request from '@/utils/request'

/** 楼宇列表(不分页) */
export const listBuildingApi = () => request.get('/building/list')

/** 新增楼宇 */
export const addBuildingApi = data => request.post('/building', data)

/** 修改楼宇 */
export const updateBuildingApi = data => request.put('/building', data)

/** 删除楼宇 */
export const deleteBuildingApi = id => request.delete(`/building/${id}`)
