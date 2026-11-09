<template>
  <div class="page">
    <!-- 搜索区 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="query" @submit.prevent>
        <el-form-item label="任务状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 140px">
            <el-option v-for="(label, value) in TASK_STATUS" :key="value" :label="label" :value="Number(value)" />
          </el-select>
        </el-form-item>
        <el-form-item label="计划日期">
          <el-date-picker v-model="query.planDate" type="date" value-format="YYYY-MM-DD"
            placeholder="选择日期" clearable style="width: 170px" />
        </el-form-item>
        <el-form-item label="设备">
          <el-select v-model="query.deviceId" placeholder="全部设备" clearable filterable style="width: 240px">
            <el-option v-for="d in deviceOptions" :key="d.id" :label="`${d.deviceCode} ${d.deviceName}`" :value="d.id" />
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
        <el-button v-if="canGenerate" type="primary" :icon="Plus" :loading="generating" @click="handleGenerate">
          生成今日任务
        </el-button>
      </div>
      <el-table :data="records" v-loading="loading" border stripe>
        <el-table-column label="设备编号" prop="deviceCode" min-width="110" show-overflow-tooltip />
        <el-table-column label="设备名称" prop="deviceName" min-width="160" show-overflow-tooltip />
        <el-table-column label="巡检人员" prop="inspectorName" width="100" />
        <el-table-column label="计划日期" prop="planDate" width="110" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="TASK_STATUS_TAG[row.status]" disable-transitions>{{ TASK_STATUS[row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="巡检结果" width="90">
          <template #default="{ row }">
            <el-tag v-if="row.result === 1 || row.result === 2" :type="RESULT_TAG[row.result]" disable-transitions>
              {{ RESULT[row.result] }}
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="完成时间" prop="completeTime" width="160" />
        <el-table-column label="操作" width="110" fixed="right">
          <template #default="{ row }">
            <el-button v-if="canExecute(row)" type="primary" link :icon="CircleCheck" @click="openExecute(row)">
              执行巡检
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination class="pager" background layout="total, sizes, prev, pager, next" :total="total"
        v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
        @current-change="load()" @size-change="load(1)" />
    </el-card>

    <!-- 执行巡检对话框 -->
    <el-dialog v-model="dialog.visible" title="执行巡检" width="640px" @closed="resetForm">
      <div class="task-info">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="设备编号">{{ currentTask.deviceCode || '-' }}</el-descriptions-item>
          <el-descriptions-item label="设备名称">{{ currentTask.deviceName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="巡检人员">{{ currentTask.inspectorName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="计划日期">{{ currentTask.planDate || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <el-form v-loading="detailLoading" label-width="140px">
        <el-form-item v-for="(item, index) in form.items" :key="index" :label="item.itemName">
          <el-radio-group v-model="item.itemResult">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="2">异常</el-radio>
          </el-radio-group>
          <el-input v-if="item.itemResult === 2" v-model="item.remark" placeholder="请填写异常说明"
            style="margin-top: 8px" />
        </el-form-item>
        <el-form-item label="总体说明">
          <el-input v-model="form.summary" type="textarea" :rows="3" placeholder="请输入总体说明(选填)" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, CircleCheck } from '@element-plus/icons-vue'
import { pageTaskApi, generateTaskApi, getTaskApi, submitRecordApi } from '@/api/inspection'
import { listDeviceApi } from '@/api/device'
import { useUserStore } from '@/stores/user'

// 巡检任务状态字典: 0-待巡检 1-已完成 2-已逾期 3-已取消
const TASK_STATUS = { 0: '待巡检', 1: '已完成', 2: '已逾期', 3: '已取消' }
const TASK_STATUS_TAG = { 0: 'warning', 1: 'success', 2: 'danger', 3: 'info' }
// 巡检结果字典: 1-正常 2-异常
const RESULT = { 1: '正常', 2: '异常' }
const RESULT_TAG = { 1: 'success', 2: 'danger' }
// 固定 6 项巡检检查项(与文档巡检项模板一致)
const INSPECT_ITEMS = [
  '主机外观完好无破损',
  '电源指示灯显示正常',
  '电极片在有效期内',
  '电池电量充足',
  '机柜箱体完好',
  '设备周边无障碍物'
]

const userStore = useUserStore()

const loading = ref(false)
const records = ref([])
const total = ref(0)
const deviceOptions = ref([])
const query = reactive({ pageNum: 1, pageSize: 10, status: null, planDate: '', deviceId: null })

// admin/device_manager 可手动生成巡检任务
const canGenerate = computed(() => ['admin', 'device_manager'].includes(userStore.roleCode))

const load = async (page) => {
  if (page) query.pageNum = page
  loading.value = true
  try {
    // 仅携带非空筛选条件
    const params = { pageNum: query.pageNum, pageSize: query.pageSize }
    if (query.status !== null && query.status !== undefined && query.status !== '') params.status = query.status
    if (query.deviceId !== null && query.deviceId !== undefined && query.deviceId !== '') params.deviceId = query.deviceId
    if (query.planDate) params.planDate = query.planDate
    const { data } = await pageTaskApi(params)
    records.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  Object.assign(query, { pageNum: 1, pageSize: 10, status: null, planDate: '', deviceId: null })
  load()
}

// 加载设备简表(设备筛选下拉用)
const loadDeviceOptions = async () => {
  try {
    const { data } = await listDeviceApi()
    deviceOptions.value = data || []
  } catch { /* 拦截器已统一提示错误 */ }
}

// 生成今日巡检任务
const generating = ref(false)
const formatDate = (date) => {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

const handleGenerate = async () => {
  const today = formatDate(new Date())
  try {
    await ElMessageBox.confirm(`确定生成 ${today} 的巡检任务? 系统将为全部在用设备生成当日任务并自动分配巡检人员`,
      '生成今日任务', { type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消' })
  } catch {
    return
  }
  generating.value = true
  try {
    const { data } = await generateTaskApi({ planDate: today })
    ElMessage.success(data > 0 ? `已生成 ${data} 条巡检任务` : '今日任务已全部生成, 无新增')
    load()
  } finally {
    generating.value = false
  }
}

// 执行巡检: 仅巡检人员本人可执行"待巡检/已逾期"任务(逾期可补交, 与后端一致)
const canExecute = (row) =>
  userStore.roleCode === 'inspector' && (row.status === 0 || row.status === 2) &&
  Number(userStore.userInfo.id) === Number(row.inspectorId)

const dialog = reactive({ visible: false })
const detailLoading = ref(false)
const saving = ref(false)
const currentTask = ref({})
const form = reactive({ summary: '', items: [] })

const openExecute = async (row) => {
  form.summary = ''
  form.items = INSPECT_ITEMS.map(name => ({ itemName: name, itemResult: null, remark: '' }))
  currentTask.value = { ...row }
  dialog.visible = true
  // 拉取任务详情, 刷新设备信息
  detailLoading.value = true
  try {
    const { data } = await getTaskApi(row.id)
    currentTask.value = data
  } catch { /* 拦截器已统一提示错误 */ } finally {
    detailLoading.value = false
  }
}

// 提交巡检记录: 任一检查项异常则整体记为异常(后端按明细自动判定并生成维修工单)
const submit = async () => {
  const empty = form.items.find(item => !item.itemResult)
  if (empty) {
    ElMessage.warning(`请选择「${empty.itemName}」的检查结果`)
    return
  }
  const abnormal = form.items.find(item => item.itemResult === 2 && !item.remark.trim())
  if (abnormal) {
    ElMessage.warning(`请填写「${abnormal.itemName}」的异常说明`)
    return
  }
  saving.value = true
  try {
    await submitRecordApi({
      taskId: currentTask.value.id,
      summary: form.summary,
      items: form.items.map(item => ({
        itemName: item.itemName,
        itemResult: item.itemResult,
        remark: item.itemResult === 2 ? item.remark : ''
      }))
    })
    ElMessage.success('巡检记录提交成功')
    dialog.visible = false
    load()
  } finally {
    saving.value = false
  }
}

const resetForm = () => {
  form.summary = ''
  form.items = []
  currentTask.value = {}
}

onMounted(() => {
  load()
  loadDeviceOptions()
})
</script>

<style scoped>
.page { padding: 4px; }
.toolbar { margin-bottom: 14px; }
.pager { margin-top: 14px; float: right; }
.task-info { margin-bottom: 16px; }
</style>
