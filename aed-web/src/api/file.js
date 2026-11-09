import request from '@/utils/request'

/** 上传图片(multipart 表单, 字段名 file), 返回 {url} */
export const uploadApi = formData => request.post('/file/upload', formData, {
  headers: { 'Content-Type': 'multipart/form-data' }
})

/** el-upload 组件所需的请求头(携带 token) */
export const uploadHeaders = () => ({ Authorization: `Bearer ${localStorage.getItem('token')}` })
