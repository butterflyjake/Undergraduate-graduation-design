import request from '@/utils/request'

/** 到期预警分页 */
export const pageWarningApi = params => request.get('/warning/page', { params })

/** 处理预警 {handleDesc} */
export const handleWarningApi = (id, data) => request.post(`/warning/${id}/handle`, data)
