import request from '@/utils/request'

/** 操作日志分页 */
export const pageLogApi = params => request.get('/log/page', { params })
