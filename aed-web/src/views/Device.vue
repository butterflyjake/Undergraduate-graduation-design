<template>
  <div class="page">
    <!-- 搜索区 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="query" @submit.prevent>
        <el-form-item label="关键字">
          <el-input v-model="query.keyword" placeholder="设备编号/设备名称" clearable style="width: 200px"
            @keyup.enter="load(1)" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 140px">
            <el-option v-for="opt in DEVICE_STATUS_OPTIONS" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="楼宇">
          <el-select v-model="query.buildingId" placeholder="全部" clearable style="width: 160px">
            <el-option v-for="b in buildingOptions" :key="b.id" :label="b.buildingName" :value="b.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="load(1)">查询</el-button>
          <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格区 -->
    <el-card shadow="never">
      <div class="toolbar">
        <el-button type="primary" :icon="Plus" @click="openAdd">新增设备</el-button>
      </div>
      <el-table :data="records" v-loading="loading" border stripe>
        <el-table-column prop="deviceCode" label="设备编号" width="130" />
        <el-table-column prop="deviceName" label="设备名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="brand" label="品牌" width="100" />
        <el-table-column prop="buildingName" label="所在楼宇" width="110" />
        <el-table-column prop="floor" label="楼层" width="80" />
        <el-table-column prop="positionDesc" label="具体位置" min-width="160" show-overflow-tooltip />
        <el-table-column prop="purchaseDate" label="购置日期" width="110" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="DEVICE_STATUS[row.status]?.type || 'info'">
              {{ DEVICE_STATUS[row.status]?.text || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="warning" @click="openStatus(row)">状态切换</el-button>
            <el-popconfirm title="确认删除该记录?" @confirm="handleDelete(row)">
              <template #reference>
                <el-button link type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination class="pager" background layout="total, sizes, prev, pager, next" :total="total"
        v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
        @current-change="load()" @size-change="load(1)" />
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="640px" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="设备编号" prop="deviceCode">
              <el-input v-model="form.deviceCode" placeholder="如 AED-001" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="设备名称" prop="deviceName">
              <el-input v-model="form.deviceName" placeholder="设备型号名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="品牌" prop="brand">
              <el-input v-model="form.brand" placeholder="如 迈瑞" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="序列号" prop="serialNo">
              <el-input v-model="form.serialNo" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="生产厂家" prop="manufacturer">
              <el-input v-model="form.manufacturer" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="购置日期" prop="purchaseDate">
              <el-date-picker v-model="form.purchaseDate" type="date" value-format="YYYY-MM-DD"
                placeholder="选择日期" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="使用年限" prop="serviceLife">
              <el-input-number v-model="form.serviceLife" :min="1" :max="50" controls-position="right"
                style="width: 100%" />
              <span class="unit-text">年</span>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所在楼宇" prop="buildingId">
              <el-select v-model="form.buildingId" placeholder="请选择楼宇" clearable style="width: 100%">
                <el-option v-for="b in buildingOptions" :key="b.id" :label="b.buildingName" :value="b.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所在楼层" prop="floor">
              <el-input v-model="form.floor" placeholder="如 1层" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="设备状态" prop="status">
              <el-select v-model="form.status" style="width: 100%">
                <el-option v-for="opt in DEVICE_STATUS_OPTIONS" :key="opt.value" :label="opt.label" :value="opt.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="具体位置" prop="positionDesc">
              <el-input v-model="form.positionDesc" placeholder="如 图书馆大厅服务台旁" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="设备照片" prop="photo">
              <div class="photo-box">
                <el-upload :show-file-list="false" :http-request="handleUpload" :headers="uploadHeaders()"
                  accept="image/jpeg,image/png,image/gif,image/webp" :before-upload="beforeUpload">
                  <img v-if="form.photo" :src="form.photo" class="photo-preview" alt="设备照片" />
                  <div v-else class="photo-trigger">
                    <el-icon><Plus /></el-icon>
                  </div>
                </el-upload>
                <el-button v-if="form.photo" link type="danger" @click="form.photo = ''">移除照片</el-button>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" :rows="2" maxlength="200" show-word-limit />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 状态快捷切换对话框 -->
    <el-dialog v-model="statusDialog.visible" title="状态切换" width="380px">
      <p class="status-tip">
        设备「{{ statusDialog.deviceCode }}」当前状态:
        <el-tag :type="DEVICE_STATUS[statusDialog.oldStatus]?.type || 'info'" size="small">
          {{ DEVICE_STATUS[statusDialog.oldStatus]?.text || statusDialog.oldStatus }}
        </el-tag>
      </p>
      <el-radio-group v-model="statusDialog.status" class="status-radios">
        <el-radio v-for="opt in DEVICE_STATUS_OPTIONS" :key="opt.value" :value="opt.value">{{ opt.label }}</el-radio>
      </el-radio-group>
      <template #footer>
        <el-button @click="statusDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="statusDialog.saving" @click="submitStatus">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { pageDeviceApi, addDeviceApi, updateDeviceApi, updateDeviceStatusApi, deleteDeviceApi } from '@/api/device'
import { listBuildingApi } from '@/api/building'
import { uploadApi, uploadHeaders } from '@/api/file'

// 设备状态字典: 0-在用 1-维修中 2-停用 3-已报废
const DEVICE_STATUS = {
  0: { text: '在用', type: 'success' },
  1: { text: '维修中', type: 'warning' },
  2: { text: '停用', type: 'info' },
  3: { text: '已报废', type: 'danger' }
}
const DEVICE_STATUS_OPTIONS = Object.entries(DEVICE_STATUS).map(([value, v]) => ({ value: Number(value), label: v.text }))

// ---- 列表查询 ----
const loading = ref(false)
const records = ref([])
const total = ref(0)
const query = reactive({ keyword: '', status: null, buildingId: null, pageNum: 1, pageSize: 10 })

const load = async (page) => {
  if (page) query.pageNum = page
  loading.value = true
  try {
    const { data } = await pageDeviceApi(query)
    records.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  Object.assign(query, { keyword: '', status: null, buildingId: null, pageNum: 1, pageSize: 10 })
  load()
}

// ---- 楼宇下拉 ----
const buildingOptions = ref([])
const loadBuildings = async () => {
  try {
    const { data } = await listBuildingApi()
    buildingOptions.value = data || []
  } catch (e) {
    // 拦截器已统一弹出错误消息, 这里不再重复提示
  }
}

// ---- 新增/编辑 ----
const dialog = reactive({ visible: false, title: '' })
const formRef = ref()
const saving = ref(false)

const defaultForm = () => ({
  id: null,
  deviceCode: '',
  deviceName: '',
  brand: '',
  serialNo: '',
  manufacturer: '',
  purchaseDate: '',
  serviceLife: null,
  buildingId: null,
  floor: '',
  positionDesc: '',
  status: 0,
  photo: '',
  remark: ''
})
const form = reactive(defaultForm())

const rules = {
  deviceCode: [{ required: true, message: '请输入设备编号', trigger: 'blur' }],
  deviceName: [{ required: true, message: '请输入设备名称', trigger: 'blur' }]
}

const openAdd = () => {
  dialog.title = '新增设备'
  dialog.visible = true
}

const openEdit = (row) => {
  Object.assign(form, {
    id: row.id,
    deviceCode: row.deviceCode,
    deviceName: row.deviceName,
    brand: row.brand,
    serialNo: row.serialNo,
    manufacturer: row.manufacturer,
    purchaseDate: row.purchaseDate,
    serviceLife: row.serviceLife,
    buildingId: row.buildingId,
    floor: row.floor,
    positionDesc: row.positionDesc,
    status: row.status,
    photo: row.photo,
    remark: row.remark
  })
  dialog.title = '编辑设备'
  dialog.visible = true
}

const resetForm = () => {
  Object.assign(form, defaultForm())
  formRef.value?.clearValidate()
}

const submit = async () => {
  await formRef.value.validate()
  saving.value = true
  try {
    if (form.id) {
      await updateDeviceApi(form)
      ElMessage.success('修改成功')
    } else {
      await addDeviceApi(form)
      ElMessage.success('新增成功')
    }
    dialog.visible = false
    load()
  } finally {
    saving.value = false
  }
}

// ---- 状态快捷切换 ----
const statusDialog = reactive({ visible: false, saving: false, id: null, deviceCode: '', oldStatus: null, status: null })

const openStatus = (row) => {
  statusDialog.id = row.id
  statusDialog.deviceCode = row.deviceCode
  statusDialog.oldStatus = row.status
  statusDialog.status = row.status
  statusDialog.visible = true
}

const submitStatus = async () => {
  statusDialog.saving = true
  try {
    await updateDeviceStatusApi({ id: statusDialog.id, status: statusDialog.status })
    ElMessage.success('状态已更新')
    statusDialog.visible = false
    load()
  } finally {
    statusDialog.saving = false
  }
}

// ---- 删除 ----
const handleDelete = async (row) => {
  await deleteDeviceApi(row.id)
  ElMessage.success('删除成功')
  load()
}

// ---- 照片上传 ----
const beforeUpload = (file) => {
  if (!['image/jpeg', 'image/png', 'image/gif', 'image/webp'].includes(file.type)) {
    ElMessage.error('仅支持 jpg/png/gif/webp 格式图片')
    return false
  }
  if (file.size > 2 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过 2MB')
    return false
  }
  return true
}

const handleUpload = async ({ file, onSuccess, onError }) => {
  try {
    const fd = new FormData()
    fd.append('file', file)
    const { data } = await uploadApi(fd)
    form.photo = data.url
    onSuccess(data)
  } catch (e) {
    onError(e)
  }
}

onMounted(() => {
  load()
  loadBuildings()
})
</script>

<style scoped>
.page { padding: 4px; }
.toolbar { margin-bottom: 14px; }
.pager { margin-top: 14px; float: right; }
.unit-text { margin-left: 6px; color: #909399; font-size: 13px; }

/* 照片上传区域 */
.photo-box { display: flex; align-items: center; gap: 10px; }
.photo-preview {
  width: 100px;
  height: 100px;
  border-radius: 6px;
  object-fit: cover;
  display: block;
}
.photo-trigger {
  width: 100px;
  height: 100px;
  border: 1px dashed #dcdfe6;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: #8c939d;
  cursor: pointer;
}
.photo-trigger:hover { border-color: #409eff; color: #409eff; }

/* 状态切换对话框 */
.status-tip { margin: 0 0 16px; color: #606266; }
.status-radios { display: flex; flex-direction: column; align-items: flex-start; }
.status-radios .el-radio { margin-right: 0; height: 32px; }
</style>
