import request from '@/utils/request'

/** 巡检任务分页 */
export const pageTaskApi = params => request.get('/inspection/task/page', { params })

/** 生成巡检任务 {planDate} */
export const generateTaskApi = data => request.post('/inspection/task/generate', data)

/** 任务详情 */
export const getTaskApi = id => request.get(`/inspection/task/${id}`)

/** 提交巡检记录 {taskId, summary, items} */
export const submitRecordApi = data => request.post('/inspection/record/submit', data)

/** 巡检记录分页 */
export const pageRecordApi = params => request.get('/inspection/record/page', { params })

/** 巡检记录详情(含明细) */
export const getRecordApi = id => request.get(`/inspection/record/${id}`)
