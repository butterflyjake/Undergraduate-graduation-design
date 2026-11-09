<template>
  <div class="page">
    <!-- 搜索区 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="query" @submit.prevent>
        <el-form-item label="设备">
          <el-select v-model="query.deviceId" placeholder="全部设备" clearable filterable style="width: 240px">
            <el-option v-for="d in deviceOptions" :key="d.id" :value="d.id"
              :label="`${d.deviceCode} ${d.deviceName}`" />
          </el-select>
        </el-form-item>
        <el-form-item label="使用时间">
          <el-date-picker v-model="query.occurTimeRange" type="datetimerange"
            value-format="YYYY-MM-DD HH:mm:ss" range-separator="至"
            start-placeholder="开始时间" end-placeholder="结束时间" style="width: 380px" />
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
        <el-button type="primary" :icon="Plus" @click="openAdd">登记使用记录</el-button>
      </div>
      <el-table :data="records" v-loading="loading" border stripe>
        <el-table-column prop="deviceCode" label="设备编号" width="130" />
        <el-table-column prop="deviceName" label="设备名称" width="150" show-overflow-tooltip />
        <el-table-column prop="occurTime" label="使用时间" width="165" />
        <el-table-column prop="locationDesc" label="事发地点" min-width="140" show-overflow-tooltip />
        <el-table-column prop="patientDesc" label="患者情况" min-width="140" show-overflow-tooltip />
        <el-table-column prop="resultDesc" label="处置结果" min-width="160" show-overflow-tooltip />
        <el-table-column prop="recorderName" label="登记人" width="100" />
        <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination class="pager" background layout="total, sizes, prev, pager, next" :total="total"
        v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
        @current-change="load()" @size-change="load(1)" />
    </el-card>

    <!-- 登记使用记录对话框 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="560px" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="设备" prop="deviceId">
          <el-select v-model="form.deviceId" placeholder="请选择设备" filterable style="width: 100%">
            <el-option v-for="d in deviceOptions" :key="d.id" :value="d.id"
              :label="`${d.deviceCode} ${d.deviceName}`" />
          </el-select>
        </el-form-item>
        <el-form-item label="使用时间" prop="occurTime">
          <el-date-picker v-model="form.occurTime" type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss" placeholder="请选择使用时间" style="width: 100%" />
        </el-form-item>
        <el-form-item label="事发地点" prop="locationDesc">
          <el-input v-model="form.locationDesc" placeholder="请输入事发地点" maxlength="200" show-word-limit clearable />
        </el-form-item>
        <el-form-item label="患者情况" prop="patientDesc">
          <el-input v-model="form.patientDesc" placeholder="患者情况(匿名描述)" maxlength="200" show-word-limit clearable />
        </el-form-item>
        <el-form-item label="处置结果" prop="resultDesc">
          <el-input v-model="form.resultDesc" type="textarea" :rows="2" placeholder="请输入处置结果"
            maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注"
            maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框(只读) -->
    <el-dialog v-model="detailVisible" title="使用记录详情" width="640px">
      <el-descriptions :column="2" border v-loading="detailLoading">
        <el-descriptions-item label="设备编号">{{ dash(detail.deviceCode) }}</el-descriptions-item>
        <el-descriptions-item label="设备名称">{{ dash(detail.deviceName) }}</el-descriptions-item>
        <el-descriptions-item label="使用时间">{{ dash(detail.occurTime) }}</el-descriptions-item>
        <el-descriptions-item label="登记人">{{ dash(detail.recorderName) }}</el-descriptions-item>
        <el-descriptions-item label="事发地点" :span="2">{{ dash(detail.locationDesc) }}</el-descriptions-item>
        <el-descriptions-item label="患者情况" :span="2">{{ dash(detail.patientDesc) }}</el-descriptions-item>
        <el-descriptions-item label="处置结果" :span="2">{{ dash(detail.resultDesc) }}</el-descriptions-item>
        <el-descriptions-item label="登记时间">{{ dash(detail.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ dash(detail.remark) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { pageUsageApi, addUsageApi, getUsageApi } from '@/api/usage'
import { listDeviceApi } from '@/api/device'

// 列表数据
const loading = ref(false)
const records = ref([])
const total = ref(0)

// 查询条件
const query = reactive({ pageNum: 1, pageSize: 10, deviceId: null, occurTimeRange: [] })

// 设备下拉选项(搜索与登记共用)
const deviceOptions = ref([])

const loadDevices = async () => {
  try {
    const { data } = await listDeviceApi()
    deviceOptions.value = data
  } catch { /* 拦截器已统一提示 */ }
}

// 分页加载列表
const load = async (page) => {
  if (page) query.pageNum = page
  loading.value = true
  try {
    const params = { pageNum: query.pageNum, pageSize: query.pageSize }
    if (query.deviceId) params.deviceId = query.deviceId
    if (query.occurTimeRange && query.occurTimeRange.length === 2) {
      params.beginTime = query.occurTimeRange[0]
      params.endTime = query.occurTimeRange[1]
    }
    const { data } = await pageUsageApi(params)
    records.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

// 重置查询条件
const resetQuery = () => {
  Object.assign(query, { pageNum: 1, pageSize: 10, deviceId: null, occurTimeRange: [] })
  load()
}

// 登记对话框
const dialog = reactive({ visible: false, title: '登记使用记录' })
const formRef = ref()
const saving = ref(false)
const defaultForm = { deviceId: null, occurTime: '', locationDesc: '', patientDesc: '', resultDesc: '', remark: '' }
const form = reactive({ ...defaultForm })

const rules = {
  deviceId: [{ required: true, message: '请选择设备', trigger: 'change' }],
  occurTime: [{ required: true, message: '请选择使用时间', trigger: 'change' }],
  locationDesc: [{ max: 200, message: '事发地点不能超过200个字符', trigger: 'blur' }],
  patientDesc: [{ max: 200, message: '患者情况不能超过200个字符', trigger: 'blur' }],
  resultDesc: [{ max: 500, message: '处置结果不能超过500个字符', trigger: 'blur' }],
  remark: [{ max: 500, message: '备注不能超过500个字符', trigger: 'blur' }]
}

// 打开登记对话框
const openAdd = () => {
  Object.assign(form, defaultForm)
  dialog.visible = true
}

// 关闭对话框后复位表单
const resetForm = () => {
  Object.assign(form, defaultForm)
  formRef.value?.clearValidate()
}

// 提交登记
const submit = async () => {
  await formRef.value.validate()
  saving.value = true
  try {
    await addUsageApi(form)
    ElMessage.success('登记成功')
    dialog.visible = false
    load()
  } finally {
    saving.value = false
  }
}

// 详情对话框
const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = reactive({})

const openDetail = async (row) => {
  detailVisible.value = true
  detailLoading.value = true
  // 清空上次详情, 避免残留旧数据
  Object.keys(detail).forEach(key => delete detail[key])
  try {
    const { data } = await getUsageApi(row.id)
    Object.assign(detail, data)
  } finally {
    detailLoading.value = false
  }
}

// 空值展示占位符
const dash = value => (value === null || value === undefined || value === '' ? '—' : value)

onMounted(() => {
  load()
  loadDevices()
})
</script>

<style scoped>
.page { padding: 4px; }
.toolbar { margin-bottom: 14px; }
.pager { margin-top: 14px; float: right; }
</style>
