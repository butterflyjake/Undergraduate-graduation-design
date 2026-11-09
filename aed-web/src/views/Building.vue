<template>
  <div class="page">
    <!-- 搜索区: 楼宇名称前端过滤 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="query" @submit.prevent>
        <el-form-item label="楼宇名称">
          <el-input v-model="query.buildingName" placeholder="请输入楼宇名称" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="load">查询</el-button>
          <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 楼宇列表(不分页) -->
    <el-card shadow="never">
      <div class="toolbar">
        <el-button type="primary" :icon="Plus" @click="openAdd">新增楼宇</el-button>
      </div>
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="buildingName" label="楼宇名称" min-width="120" />
        <el-table-column prop="campusArea" label="所在校区" min-width="110" />
        <el-table-column prop="address" label="详细地址" min-width="150" show-overflow-tooltip />
        <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :icon="Edit" @click="openEdit(row)">编辑</el-button>
            <el-popconfirm title="确认删除该记录?" @confirm="handleDelete(row)">
              <el-button type="danger" link :icon="Delete">删除</el-button>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="560px" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="楼宇名称" prop="buildingName">
          <el-input v-model="form.buildingName" placeholder="如: 图书馆" maxlength="50" />
        </el-form-item>
        <el-form-item label="所在校区" prop="campusArea">
          <el-input v-model="form.campusArea" placeholder="如: 主校区" maxlength="50" />
        </el-form-item>
        <el-form-item label="详细地址" prop="address">
          <el-input v-model="form.address" placeholder="如: 正门东侧" maxlength="100" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="选填" maxlength="200" />
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
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Plus, Edit, Delete } from '@element-plus/icons-vue'
import { listBuildingApi, addBuildingApi, updateBuildingApi, deleteBuildingApi } from '@/api/building'

// 楼宇列表(接口返回全量数组, 不分页)
const loading = ref(false)
const records = ref([])
const query = reactive({ buildingName: '' })

// 加载楼宇列表
const load = async () => {
  loading.value = true
  try {
    const { data } = await listBuildingApi()
    records.value = data
  } finally {
    loading.value = false
  }
}

// 按楼宇名称前端过滤
const tableData = computed(() => {
  const keyword = query.buildingName.trim()
  if (!keyword) return records.value
  return records.value.filter(item => (item.buildingName || '').includes(keyword))
})

// 重置搜索条件
const resetQuery = () => {
  query.buildingName = ''
  load()
}

// ---- 新增/编辑弹窗 ----
const dialog = reactive({ visible: false, title: '' })
const formRef = ref()
const saving = ref(false)
const form = reactive({ id: null, buildingName: '', campusArea: '', address: '', remark: '' })
const rules = {
  buildingName: [{ required: true, message: '请输入楼宇名称', trigger: 'blur' }]
}

// 打开新增弹窗
const openAdd = () => {
  dialog.title = '新增楼宇'
  dialog.visible = true
}

// 打开编辑弹窗
const openEdit = row => {
  dialog.title = '编辑楼宇'
  Object.assign(form, {
    id: row.id,
    buildingName: row.buildingName,
    campusArea: row.campusArea,
    address: row.address,
    remark: row.remark
  })
  dialog.visible = true
}

// 弹窗关闭后重置表单
const resetForm = () => {
  Object.assign(form, { id: null, buildingName: '', campusArea: '', address: '', remark: '' })
  formRef.value?.clearValidate()
}

// 提交新增/编辑
const submit = async () => {
  await formRef.value.validate()
  saving.value = true
  try {
    const payload = {
      buildingName: form.buildingName,
      campusArea: form.campusArea,
      address: form.address,
      remark: form.remark
    }
    if (form.id) {
      await updateBuildingApi({ id: form.id, ...payload })
      ElMessage.success('修改成功')
    } else {
      await addBuildingApi(payload)
      ElMessage.success('新增成功')
    }
    dialog.visible = false
    load()
  } finally {
    saving.value = false
  }
}

// 删除楼宇(后端可能因存在关联设备拒绝, 错误消息由拦截器统一弹出)
const handleDelete = async row => {
  await deleteBuildingApi(row.id)
  ElMessage.success('删除成功')
  load()
}

onMounted(() => load())
</script>

<style scoped>
.page {
  padding: 4px;
}

.toolbar {
  margin-bottom: 14px;
}
</style>
