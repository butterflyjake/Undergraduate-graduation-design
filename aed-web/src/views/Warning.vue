<template>
  <div class="page">
    <el-card class="search-card" shadow="never">
      <el-alert type="info" :closable="false" show-icon
        title="系统每日 07:00 自动扫描生成配件到期预警" class="tip-alert" />
      <el-form :inline="true" :model="query" @submit.prevent>
        <!-- 预警类型 -->
        <el-form-item label="预警类型">
          <el-select v-model="query.warningType" placeholder="全部" clearable style="width: 140px">
            <el-option v-for="(label, value) in WARNING_TYPES" :key="value" :label="label" :value="Number(value)" />
          </el-select>
        </el-form-item>
        <!-- 处理状态 -->
        <el-form-item label="处理状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 140px">
            <el-option v-for="(label, value) in WARNING_STATUS" :key="value" :label="label" :value="Number(value)" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="load(1)">查询</el-button>
          <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    <el-card shadow="never">
      <el-table :data="records" v-loading="loading" border stripe>
        <el-table-column prop="deviceCode" label="设备编号" width="130" />
        <el-table-column prop="deviceName" label="设备名称" width="150" show-overflow-tooltip />
        <el-table-column prop="partType" label="配件类型" width="90">
          <template #default="{ row }">{{ PART_TYPES[row.partType] || '-' }}</template>
        </el-table-column>
        <el-table-column prop="warningType" label="预警类型" width="100">
          <template #default="{ row }">
            <el-tag :type="WARNING_TYPE_TAG[row.warningType] || 'info'">{{ WARNING_TYPES[row.warningType] || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="daysRemaining" label="剩余天数" width="90" align="center">
          <template #default="{ row }">{{ row.daysRemaining != null ? row.daysRemaining + ' 天' : '-' }}</template>
        </el-table-column>
        <el-table-column prop="warningDate" label="预警日期" width="110" />
        <el-table-column prop="status" label="处理状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="WARNING_STATUS_TAG[row.status] || 'info'">{{ WARNING_STATUS[row.status] || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="handleDesc" label="处理说明" min-width="160" show-overflow-tooltip />
        <el-table-column prop="handleTime" label="处理时间" width="160" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <!-- 仅未处理且具备设备管理权限时可处理 -->
            <el-button v-if="row.status === 0 && canHandle" type="primary" link @click="openHandle(row)">处理</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination class="pager" background layout="total, sizes, prev, pager, next" :total="total"
        v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
        @current-change="load()" @size-change="load(1)" />
    </el-card>

    <!-- 处理预警弹窗 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="560px" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="处理说明" prop="handleDesc">
          <el-input v-model="form.handleDesc" type="textarea" :rows="4" maxlength="200" show-word-limit
            placeholder="请填写处理措施, 如: 已采购新电极片, 本周更换" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { Search, Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { pageWarningApi, handleWarningApi } from '@/api/warning'
import { useUserStore } from '@/stores/user'

// 常量字典
const PART_TYPES = { 1: '电极片', 2: '电池' }
const WARNING_TYPES = { 1: '临期', 2: '已过期' }
const WARNING_TYPE_TAG = { 1: 'warning', 2: 'danger' }
const WARNING_STATUS = { 0: '未处理', 1: '已处理' }
const WARNING_STATUS_TAG = { 0: 'warning', 1: 'success' }

const userStore = useUserStore()
// 处理接口仅 admin / device_manager 有权限
const canHandle = computed(() => ['admin', 'device_manager'].includes(userStore.roleCode))

const loading = ref(false)
const records = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, status: undefined, warningType: undefined })

// 加载预警列表
const load = async (page) => {
  if (page) query.pageNum = page
  loading.value = true
  try {
    const { data } = await pageWarningApi(query)
    records.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

// 重置查询条件
const resetQuery = () => {
  Object.assign(query, { pageNum: 1, pageSize: 10, status: undefined, warningType: undefined })
  load()
}

// 处理预警弹窗
const dialog = reactive({ visible: false, title: '处理预警' })
const formRef = ref()
const saving = ref(false)
const form = reactive({ id: null, handleDesc: '' })
const rules = {
  handleDesc: [{ required: true, message: '请填写处理说明', trigger: 'blur' }]
}

const openHandle = (row) => {
  form.id = row.id
  form.handleDesc = ''
  dialog.visible = true
}

const resetForm = () => {
  form.id = null
  form.handleDesc = ''
  formRef.value && formRef.value.clearValidate()
}

// 提交处理
const submit = async () => {
  await formRef.value.validate()
  saving.value = true
  try {
    await handleWarningApi(form.id, { handleDesc: form.handleDesc })
    ElMessage.success('处理成功')
    dialog.visible = false
    load()
  } finally {
    saving.value = false
  }
}

onMounted(() => load())
</script>

<style scoped>
.page { padding: 4px; }
.tip-alert { margin-bottom: 14px; }
.toolbar { margin-bottom: 14px; }
.pager { margin-top: 14px; float: right; }
</style>
