import request from '@/utils/request'

/** 设备分页查询 */
export const pageDeviceApi = params => request.get('/device/page', { params })

/** 设备简表(下拉用) */
export const listDeviceApi = () => request.get('/device/list')

/** 设备详情 */
export const getDeviceApi = id => request.get(`/device/${id}`)

/** 新增设备 */
export const addDeviceApi = data => request.post('/device', data)

/** 修改设备 */
export const updateDeviceApi = data => request.put('/device', data)

/** 快速变更设备状态 {id, status} */
export const updateDeviceStatusApi = data => request.put('/device/status', data)

/** 删除设备 */
export const deleteDeviceApi = id => request.delete(`/device/${id}`)
