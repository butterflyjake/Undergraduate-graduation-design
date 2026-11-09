import request from '@/utils/request'

/** 使用记录分页 */
export const pageUsageApi = params => request.get('/usage/page', { params })

/** 登记使用记录 */
export const addUsageApi = data => request.post('/usage', data)

/** 使用记录详情 */
export const getUsageApi = id => request.get(`/usage/${id}`)
