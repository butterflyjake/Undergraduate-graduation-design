<template>
  <div class="page">
    <!-- 搜索区 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="query" @submit.prevent>
        <el-form-item label="巡检结果">
          <el-select v-model="query.result" placeholder="全部" clearable style="width: 150px">
            <el-option label="正常" :value="1" />
            <el-option label="异常" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="设备">
          <el-select v-model="query.deviceId" placeholder="全部设备" clearable filterable style="width: 240px">
            <el-option v-for="d in deviceOptions" :key="d.id" :label="`${d.deviceCode} - ${d.deviceName}`" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="load(1)">查询</el-button>
          <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 记录列表 -->
    <el-card shadow="never">
      <el-table :data="records" v-loading="loading" border stripe>
        <el-table-column prop="deviceCode" label="设备编号" width="110" />
        <el-table-column prop="deviceName" label="设备名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="inspectorName" label="巡检人" width="100" />
        <el-table-column prop="inspectTime" label="巡检时间" width="165" />
        <el-table-column label="巡检结果" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="RESULT_TAG_TYPES[row.result]">{{ RESULT_TYPES[row.result] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="summary" label="总体说明" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="100" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="openDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination class="pager" background layout="total, sizes, prev, pager, next" :total="total"
        v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
        @current-change="load()" @size-change="load(1)" />
    </el-card>

    <!-- 详情弹窗(只读) -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="640px">
      <div v-loading="detailLoading">
        <!-- 记录基本信息 -->
        <el-descriptions :column="2" border>
          <el-descriptions-item label="设备编号">{{ detail.deviceCode || '-' }}</el-descriptions-item>
          <el-descriptions-item label="设备名称">{{ detail.deviceName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="巡检人">{{ detail.inspectorName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="巡检时间">{{ detail.inspectTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="任务编号">{{ detail.taskId || '-' }}</el-descriptions-item>
          <el-descriptions-item label="巡检结果">
            <el-tag v-if="detail.result != null" :type="RESULT_TAG_TYPES[detail.result]">{{ RESULT_TYPES[detail.result] }}</el-tag>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="总体说明" :span="2">{{ detail.summary || '无' }}</el-descriptions-item>
        </el-descriptions>
        <!-- 检查明细表 -->
        <el-table :data="detail.items || []" border size="small" class="detail-table">
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column prop="itemName" label="检查项" min-width="180" />
          <el-table-column label="检查结果" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="RESULT_TAG_TYPES[row.itemResult]">{{ RESULT_TYPES[row.itemResult] }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="异常说明" min-width="160" show-overflow-tooltip>
            <template #default="{ row }">{{ row.remark || '-' }}</template>
          </el-table-column>
        </el-table>
      </div>
      <template #footer>
        <el-button type="primary" @click="dialog.visible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, Refresh } from '@element-plus/icons-vue'
import { pageRecordApi, getRecordApi } from '@/api/inspection'
import { listDeviceApi } from '@/api/device'

// 巡检结果字典: 1-正常 2-异常
const RESULT_TYPES = { 1: '正常', 2: '异常' }
const RESULT_TAG_TYPES = { 1: 'success', 2: 'danger' }

const loading = ref(false)
const records = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, result: null, deviceId: null })

// 设备下拉选项
const deviceOptions = ref([])

// 加载分页列表(仅传非空过滤条件)
const load = async (page) => {
  if (page) query.pageNum = page
  loading.value = true
  try {
    const params = { pageNum: query.pageNum, pageSize: query.pageSize }
    if (query.result !== null && query.result !== '') params.result = query.result
    if (query.deviceId) params.deviceId = query.deviceId
    const { data } = await pageRecordApi(params)
    records.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

// 重置搜索条件
const resetQuery = () => {
  query.pageNum = 1
  query.result = null
  query.deviceId = null
  load()
}

// 加载设备下拉数据
const loadDeviceOptions = async () => {
  const { data } = await listDeviceApi()
  deviceOptions.value = data || []
}

// ---- 详情弹窗 ----
const dialog = reactive({ visible: false, title: '巡检记录详情' })
const detailLoading = ref(false)
const detail = ref({})

// 打开详情(记录基本信息 + 检查明细)
const openDetail = async (row) => {
  dialog.visible = true
  detailLoading.value = true
  detail.value = {}
  try {
    const { data } = await getRecordApi(row.id)
    detail.value = data
  } finally {
    detailLoading.value = false
  }
}

onMounted(() => {
  load()
  loadDeviceOptions()
})
</script>

<style scoped>
.page { padding: 4px; }
.pager { margin-top: 14px; float: right; }
.detail-table { margin-top: 14px; }
</style>
