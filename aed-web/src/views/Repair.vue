<template>
  <div class="page">
    <!-- 搜索区 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="query" @submit.prevent>
        <el-form-item label="工单状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 130px">
            <el-option v-for="(label, value) in STATUS" :key="value" :label="label" :value="Number(value)" />
          </el-select>
        </el-form-item>
        <el-form-item label="报修来源">
          <el-select v-model="query.reportType" placeholder="全部" clearable style="width: 130px">
            <el-option v-for="(label, value) in REPORT_TYPES" :key="value" :label="label" :value="Number(value)" />
          </el-select>
        </el-form-item>
        <el-form-item label="设备">
          <el-select v-model="query.deviceId" placeholder="全部设备" clearable filterable style="width: 220px">
            <el-option v-for="d in deviceOptions" :key="d.id" :label="`${d.deviceCode} / ${d.deviceName}`" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="load(1)">查询</el-button>
          <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 列表区 -->
    <el-card shadow="never">
      <div class="toolbar">
        <el-button type="primary" :icon="Plus" @click="openReport">手动报修</el-button>
      </div>
      <el-table :data="records" v-loading="loading" border stripe>
        <el-table-column prop="orderNo" label="工单编号" width="170" />
        <el-table-column label="设备" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">{{ row.deviceCode }} / {{ row.deviceName }}</template>
        </el-table-column>
        <el-table-column label="报修来源" width="100">
          <template #default="{ row }">
            <el-tag :type="REPORT_TAG[row.reportType]">{{ REPORT_TYPES[row.reportType] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reporterName" label="报修人" width="100" />
        <el-table-column prop="faultDesc" label="故障描述" min-width="180" show-overflow-tooltip />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="STATUS_TAG[row.status]">{{ STATUS[row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="处理人" width="100">
          <template #default="{ row }">{{ row.handlerName || '-' }}</template>
        </el-table-column>
        <el-table-column label="完成时间" width="160">
          <template #default="{ row }">{{ row.repairTime || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="210" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 0 && canHandle">
              <el-button link type="primary" @click="handleAccept(row)">接单</el-button>
              <el-button link type="danger" @click="handleReject(row)">驳回</el-button>
            </template>
            <el-button v-if="row.status === 1 && canHandle" link type="success" @click="openComplete(row)">完成</el-button>
            <el-button v-if="row.status === 2 || row.status === 3" link type="primary" @click="openDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination class="pager" background layout="total, sizes, prev, pager, next" :total="total"
        v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
        @current-change="load()" @size-change="load(1)" />
    </el-card>

    <!-- 手动报修弹窗 -->
    <el-dialog v-model="reportDialog" title="手动报修" width="560px" @closed="resetReportForm">
      <el-form ref="reportFormRef" :model="reportForm" :rules="reportRules" label-width="90px">
        <el-form-item label="设备" prop="deviceId">
          <el-select v-model="reportForm.deviceId" placeholder="请选择故障设备" filterable style="width: 100%">
            <el-option v-for="d in deviceOptions" :key="d.id" :label="`${d.deviceCode} / ${d.deviceName}`" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="故障描述" prop="faultDesc">
          <el-input v-model="reportForm.faultDesc" type="textarea" :rows="4" maxlength="500" show-word-limit
            placeholder="请描述设备故障情况" />
        </el-form-item>
        <el-form-item label="故障照片">
          <div class="photo-upload">
            <el-upload v-if="!reportForm.faultPhoto" :http-request="uploadFile" :headers="uploadHeaders()"
              :before-upload="beforeUpload" :show-file-list="false" accept="image/*">
              <el-button :icon="Upload" :loading="uploading">上传图片</el-button>
            </el-upload>
            <div v-else class="photo-preview">
              <el-image :src="reportForm.faultPhoto" :preview-src-list="[reportForm.faultPhoto]" fit="cover" class="photo-img" />
              <el-button link type="danger" @click="reportForm.faultPhoto = ''">移除图片</el-button>
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reportDialog = false">取消</el-button>
        <el-button type="primary" :loading="reportSaving" @click="submitReport">提交报修</el-button>
      </template>
    </el-dialog>

    <!-- 完成工单弹窗 -->
    <el-dialog v-model="completeDialog" title="完成工单" width="560px" @closed="resetCompleteForm">
      <el-alert class="complete-alert" type="info" :closable="false" show-icon
        title="提示: 若该工单来自巡检异常或配件更换, 系统会自动联动处理" />
      <el-form ref="completeFormRef" :model="completeForm" :rules="completeRules" label-width="90px">
        <el-form-item label="维修说明" prop="repairDesc">
          <el-input v-model="completeForm.repairDesc" type="textarea" :rows="4" maxlength="500" show-word-limit
            placeholder="请填写维修处理说明" />
        </el-form-item>
        <el-form-item label="维修费用">
          <el-input-number v-model="completeForm.repairCost" :min="0" :precision="2" :step="100" controls-position="right"
            style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="completeDialog = false">取消</el-button>
        <el-button type="primary" :loading="completeSaving" @click="submitComplete">确认完成</el-button>
      </template>
    </el-dialog>

    <!-- 工单详情弹窗(只读) -->
    <el-dialog v-model="detailDialog" title="工单详情" width="640px">
      <el-descriptions :column="2" border v-loading="detailLoading">
        <el-descriptions-item label="工单编号">{{ detail.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="STATUS_TAG[detail.status]">{{ STATUS[detail.status] }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="设备编号">{{ detail.deviceCode }}</el-descriptions-item>
        <el-descriptions-item label="设备名称">{{ detail.deviceName }}</el-descriptions-item>
        <el-descriptions-item label="报修来源">
          <el-tag :type="REPORT_TAG[detail.reportType]">{{ REPORT_TYPES[detail.reportType] }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="报修人">{{ detail.reporterName }}</el-descriptions-item>
        <el-descriptions-item label="故障描述" :span="2">{{ detail.faultDesc }}</el-descriptions-item>
        <el-descriptions-item label="故障照片" :span="2">
          <el-image v-if="detail.faultPhoto" :src="detail.faultPhoto" :preview-src-list="[detail.faultPhoto]"
            fit="cover" class="photo-img" />
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="处理人">{{ detail.handlerName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="完成时间">{{ detail.repairTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="维修说明" :span="2">{{ detail.repairDesc || '-' }}</el-descriptions-item>
        <el-descriptions-item label="维修费用">{{ detail.repairCost != null ? detail.repairCost + ' 元' : '-' }}</el-descriptions-item>
        <el-descriptions-item label="驳回原因">{{ detail.rejectReason || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { Search, Refresh, Plus, Upload } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pageRepairApi, reportRepairApi, acceptRepairApi, completeRepairApi, rejectRepairApi, getRepairApi } from '@/api/repair'
import { listDeviceApi } from '@/api/device'
import { uploadApi, uploadHeaders } from '@/api/file'
import { useUserStore } from '@/stores/user'

// 工单状态字典: 0-待处理 1-维修中 2-已完成 3-已驳回
const STATUS = { 0: '待处理', 1: '维修中', 2: '已完成', 3: '已驳回' }
const STATUS_TAG = { 0: 'warning', 1: 'warning', 2: 'success', 3: 'danger' }
// 报修来源字典: 1-巡检异常 2-手动报修
const REPORT_TYPES = { 1: '巡检异常', 2: '手动报修' }
const REPORT_TAG = { 1: 'warning', 2: 'primary' }

const userStore = useUserStore()
// 受理/完成/驳回仅 admin、device_manager 可见(接口权限一致)
const canHandle = computed(() => ['admin', 'device_manager'].includes(userStore.roleCode))

const loading = ref(false)
const records = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, status: undefined, reportType: undefined, deviceId: undefined })

// 设备下拉选项
const deviceOptions = ref([])
const loadDevices = async () => {
  try {
    const res = await listDeviceApi()
    deviceOptions.value = Array.isArray(res.data) ? res.data : (res.data?.records || [])
  } catch { /* 错误消息已由拦截器统一弹出 */ }
}

// 分页查询工单
const load = async page => {
  if (page) query.pageNum = page
  loading.value = true
  try {
    const res = await pageRepairApi(query)
    records.value = res.data.records
    total.value = res.data.total
  } catch { /* 错误消息已由拦截器统一弹出 */ } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  Object.assign(query, { pageNum: 1, pageSize: 10, status: undefined, reportType: undefined, deviceId: undefined })
  load()
}

// ---- 手动报修 ----
const reportDialog = ref(false)
const reportFormRef = ref()
const reportSaving = ref(false)
const uploading = ref(false)
const reportForm = reactive({ deviceId: undefined, faultDesc: '', faultPhoto: '' })
const reportRules = {
  deviceId: [{ required: true, message: '请选择设备', trigger: 'change' }],
  faultDesc: [
    { required: true, message: '请输入故障描述', trigger: 'blur' },
    { max: 500, message: '故障描述不能超过 500 字', trigger: 'blur' }
  ]
}

const openReport = () => { reportDialog.value = true }

const beforeUpload = file => {
  if (!file.type.startsWith('image/')) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (file.size / 1024 / 1024 > 5) {
    ElMessage.error('图片大小不能超过 5MB')
    return false
  }
  return true
}

// 自定义上传: 走 axios 统一拦截器, 成功后把返回 url 写入表单
const uploadFile = async ({ file }) => {
  const formData = new FormData()
  formData.append('file', file)
  uploading.value = true
  try {
    const res = await uploadApi(formData)
    reportForm.faultPhoto = res.data.url
    ElMessage.success('图片上传成功')
  } catch { /* 错误消息已由拦截器统一弹出 */ } finally {
    uploading.value = false
  }
}

const submitReport = async () => {
  await reportFormRef.value.validate()
  reportSaving.value = true
  try {
    await reportRepairApi({
      deviceId: reportForm.deviceId,
      faultDesc: reportForm.faultDesc,
      faultPhoto: reportForm.faultPhoto
    })
    ElMessage.success('报修成功, 已生成维修工单')
    reportDialog.value = false
    load()
  } catch { /* 错误消息已由拦截器统一弹出 */ } finally {
    reportSaving.value = false
  }
}

const resetReportForm = () => {
  reportFormRef.value?.resetFields()
  reportForm.faultPhoto = ''
}

// ---- 接单(待处理→维修中) ----
const handleAccept = async row => {
  try {
    await ElMessageBox.confirm(`确认受理工单「${row.orderNo}」?`, '提示', { type: 'warning' })
  } catch { return }
  try {
    await acceptRepairApi(row.id)
    ElMessage.success('已受理, 工单进入维修中')
    load()
  } catch { /* 错误消息已由拦截器统一弹出 */ }
}

// ---- 驳回(待处理→已驳回, 需填写原因) ----
const handleReject = async row => {
  let reason = ''
  try {
    const { value } = await ElMessageBox.prompt('请输入驳回原因(必填)', '驳回工单', {
      inputType: 'textarea',
      inputPlaceholder: '请说明驳回原因',
      inputValidator: v => (v && v.trim() ? true : '驳回原因不能为空')
    })
    reason = value.trim()
  } catch { return }
  try {
    await rejectRepairApi(row.id, { rejectReason: reason })
    ElMessage.success('工单已驳回')
    load()
  } catch { /* 错误消息已由拦截器统一弹出 */ }
}

// ---- 完成工单(维修中→已完成) ----
const completeDialog = ref(false)
const completeFormRef = ref()
const completeSaving = ref(false)
const completeRow = ref(null)
const completeForm = reactive({ repairDesc: '', repairCost: undefined })
const completeRules = {
  repairDesc: [
    { required: true, message: '请输入维修说明', trigger: 'blur' },
    { max: 500, message: '维修说明不能超过 500 字', trigger: 'blur' }
  ]
}

const openComplete = row => {
  completeRow.value = row
  completeDialog.value = true
}

const submitComplete = async () => {
  await completeFormRef.value.validate()
  completeSaving.value = true
  try {
    await completeRepairApi(completeRow.value.id, {
      repairDesc: completeForm.repairDesc,
      repairCost: completeForm.repairCost ?? 0
    })
    ElMessage.success('工单已完成')
    completeDialog.value = false
    load()
  } catch { /* 错误消息已由拦截器统一弹出 */ } finally {
    completeSaving.value = false
  }
}

const resetCompleteForm = () => {
  completeFormRef.value?.resetFields()
  completeRow.value = null
}

// ---- 工单详情(只读) ----
const detailDialog = ref(false)
const detailLoading = ref(false)
const detail = ref({})

const openDetail = async row => {
  detail.value = {}
  detailDialog.value = true
  detailLoading.value = true
  try {
    const res = await getRepairApi(row.id)
    detail.value = res.data
  } catch { /* 错误消息已由拦截器统一弹出 */ } finally {
    detailLoading.value = false
  }
}

onMounted(() => {
  load()
  loadDevices()
})
</script>

<style scoped>
.page { padding: 4px; }
.toolbar { margin-bottom: 14px; }
.pager { margin-top: 14px; float: right; }
.photo-preview { display: flex; align-items: center; gap: 12px; }
.photo-img { width: 100px; height: 100px; border-radius: 4px; }
.complete-alert { margin-bottom: 16px; }
</style>
