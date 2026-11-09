<template>
  <div class="page">
    <!-- 搜索区 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="query" @submit.prevent>
        <el-form-item label="设备">
          <el-select v-model="query.deviceId" placeholder="全部设备" clearable filterable style="width: 220px">
            <el-option v-for="d in deviceOptions" :key="d.id" :label="`${d.deviceCode} - ${d.deviceName}`" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="配件类型">
          <el-select v-model="query.partType" placeholder="全部类型" clearable style="width: 140px">
            <el-option label="电极片" :value="1" />
            <el-option label="电池" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 140px">
            <el-option label="使用中" :value="0" />
            <el-option label="已更换" :value="1" />
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
        <el-button type="primary" :icon="Plus" @click="openAdd">新增配件</el-button>
      </div>
      <el-table :data="records" v-loading="loading" border stripe>
        <el-table-column prop="deviceCode" label="设备编号" width="110" />
        <el-table-column prop="deviceName" label="设备名称" min-width="150" show-overflow-tooltip />
        <el-table-column label="配件类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="PART_TAG_TYPES[row.partType] || 'info'">{{ PART_TYPES[row.partType] || '未知' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="batchNo" label="批次号" width="130" show-overflow-tooltip />
        <el-table-column prop="installDate" label="安装日期" width="120" align="center" />
        <el-table-column label="到期日期" width="190" align="center">
          <template #default="{ row }">
            <span :style="expireInfo(row.expireDate).type === 'danger' ? 'color:#f56c6c;font-weight:600' : ''">{{ row.expireDate }}</span>
            <el-tag v-if="expireInfo(row.expireDate).type" :type="expireInfo(row.expireDate).type" size="small" style="margin-left:6px">
              {{ expireInfo(row.expireDate).label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="STATUS_TAG_TYPES[row.status] || 'info'">{{ STATUS_TEXTS[row.status] ?? '未知' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link :icon="Edit" @click="openEdit(row)">编辑</el-button>
            <el-popconfirm title="确认删除该记录?" @confirm="handleDelete(row)">
              <template #reference>
                <el-button type="danger" link :icon="Delete">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination class="pager" background layout="total, sizes, prev, pager, next" :total="total"
        v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
        @current-change="load()" @size-change="load(1)" />
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="560px" @closed="resetForm">
      <el-alert v-if="!dialog.isEdit" type="info" :closable="false" show-icon
        title="新增配件后, 同设备同类型的旧配件将自动标记为已更换" style="margin-bottom: 14px" />
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" :disabled="formDisabled">
        <el-form-item label="设备" prop="deviceId">
          <el-select v-model="form.deviceId" placeholder="请选择设备" filterable style="width: 100%">
            <el-option v-for="d in deviceOptions" :key="d.id" :label="`${d.deviceCode} - ${d.deviceName}`" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="配件类型" prop="partType">
          <el-radio-group v-model="form.partType">
            <el-radio :value="1">电极片</el-radio>
            <el-radio :value="2">电池</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="批次号" prop="batchNo">
          <el-input v-model="form.batchNo" placeholder="请输入批次号" maxlength="50" />
        </el-form-item>
        <el-form-item label="安装日期" prop="installDate">
          <el-date-picker v-model="form.installDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择安装日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="到期日期" prop="expireDate">
          <el-date-picker v-model="form.expireDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择到期日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" maxlength="200" show-word-limit placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button v-if="!formDisabled" type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Plus, Edit, Delete } from '@element-plus/icons-vue'
import { pagePartApi, addPartApi, updatePartApi, deletePartApi } from '@/api/part'
import { listDeviceApi } from '@/api/device'

// ---- 枚举字典 ----
const PART_TYPES = { 1: '电极片', 2: '电池' }
const PART_TAG_TYPES = { 1: 'success', 2: 'warning' }
const STATUS_TEXTS = { 0: '使用中', 1: '已更换' }
const STATUS_TAG_TYPES = { 0: 'success', 1: 'info' }

// ---- 列表数据 ----
const loading = ref(false)
const records = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, deviceId: null, partType: null, status: null })

// 设备下拉选项
const deviceOptions = ref([])
const loadDeviceOptions = async () => {
  const { data } = await listDeviceApi()
  deviceOptions.value = data || []
}

// 分页加载配件列表
const load = async page => {
  if (page) query.pageNum = page
  loading.value = true
  try {
    const { data } = await pagePartApi(query)
    records.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

// 重置查询条件并重新加载
const resetQuery = () => {
  Object.assign(query, { pageNum: 1, pageSize: 10, deviceId: null, partType: null, status: null })
  load()
}

// 计算到期状态: 已过期 -> danger, 30天内到期 -> warning, 其余正常
const expireInfo = date => {
  if (!date) return { type: '', label: '' }
  const [y, m, d] = date.split('-').map(Number)
  const expire = new Date(y, m - 1, d)
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const days = Math.round((expire.getTime() - today.getTime()) / 86400000)
  if (days < 0) return { type: 'danger', label: '已过期' }
  if (days <= 30) return { type: 'warning', label: `${days}天后到期` }
  return { type: '', label: '' }
}

// ---- 新增/编辑弹窗 ----
const dialog = reactive({ visible: false, title: '新增配件', isEdit: false })
const formRef = ref()
const saving = ref(false)
const formDisabled = ref(false)
const form = reactive({ id: null, deviceId: null, partType: null, batchNo: '', installDate: '', expireDate: '', remark: '' })

const rules = {
  deviceId: [{ required: true, message: '请选择设备', trigger: 'change' }],
  partType: [{ required: true, message: '请选择配件类型', trigger: 'change' }],
  expireDate: [
    { required: true, message: '请选择到期日期', trigger: 'change' },
    {
      validator: (rule, value, callback) => {
        if (value && form.installDate && value <= form.installDate) {
          callback(new Error('到期日期必须晚于安装日期'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ]
}

// 新增
const openAdd = () => {
  dialog.isEdit = false
  dialog.title = '新增配件'
  formDisabled.value = false
  Object.assign(form, { id: null, deviceId: null, partType: null, batchNo: '', installDate: '', expireDate: '', remark: '' })
  dialog.visible = true
}

// 编辑: 已更换(1)的配件只读查看
const openEdit = row => {
  dialog.isEdit = true
  if (row.status === 1) {
    formDisabled.value = true
    dialog.title = '配件详情'
    ElMessage.warning('该配件已更换, 仅可查看, 不可修改')
  } else {
    formDisabled.value = false
    dialog.title = '编辑配件'
  }
  Object.assign(form, {
    id: row.id,
    deviceId: row.deviceId,
    partType: row.partType,
    batchNo: row.batchNo || '',
    installDate: row.installDate || '',
    expireDate: row.expireDate || '',
    remark: row.remark || ''
  })
  dialog.visible = true
}

// 弹窗关闭后重置表单
const resetForm = () => {
  formRef.value?.clearValidate()
  Object.assign(form, { id: null, deviceId: null, partType: null, batchNo: '', installDate: '', expireDate: '', remark: '' })
  formDisabled.value = false
}

// 保存(新增/编辑)
const submit = async () => {
  await formRef.value.validate()
  saving.value = true
  try {
    if (dialog.isEdit) {
      await updatePartApi(form)
      ElMessage.success('修改成功')
    } else {
      await addPartApi(form)
      ElMessage.success('新增成功')
    }
    dialog.visible = false
    load()
  } finally {
    saving.value = false
  }
}

// 删除配件
const handleDelete = async row => {
  await deletePartApi(row.id)
  ElMessage.success('删除成功')
  load()
}

onMounted(() => {
  load()
  loadDeviceOptions()
})
</script>

<style scoped>
.page {
  padding: 4px;
}

.toolbar {
  margin-bottom: 14px;
}

.pager {
  margin-top: 14px;
  float: right;
}
</style>
