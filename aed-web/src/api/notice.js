import request from '@/utils/request'

/** 公告分页 */
export const pageNoticeApi = params => request.get('/notice/page', { params })

/** 公告详情 */
export const getNoticeApi = id => request.get(`/notice/${id}`)

/** 发布公告 */
export const addNoticeApi = data => request.post('/notice', data)

/** 修改公告 */
export const updateNoticeApi = data => request.put('/notice', data)

/** 删除公告 */
export const deleteNoticeApi = id => request.delete(`/notice/${id}`)
