import request from '@/utils/request'

/** 维修工单分页 */
export const pageRepairApi = params => request.get('/repair/page', { params })

/** 手动报修 {deviceId, faultDesc, faultPhoto} */
export const reportRepairApi = data => request.post('/repair', data)

/** 受理工单(待处理→维修中) */
export const acceptRepairApi = id => request.post(`/repair/${id}/accept`)

/** 完成工单(维修中→已完成) */
export const completeRepairApi = (id, data) => request.post(`/repair/${id}/complete`, data)

/** 驳回工单(待处理→已驳回) */
export const rejectRepairApi = (id, data) => request.post(`/repair/${id}/reject`, data)

/** 工单详情 */
export const getRepairApi = id => request.get(`/repair/${id}`)
