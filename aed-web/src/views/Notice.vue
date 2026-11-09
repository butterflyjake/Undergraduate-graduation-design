<template>
  <div class="page">
    <!-- 搜索区 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="query" @submit.prevent>
        <el-form-item label="标题">
          <el-input v-model="query.title" placeholder="请输入公告标题" clearable style="width: 200px" @keyup.enter="load(1)" />
        </el-form-item>
        <el-form-item label="公告类型">
          <el-select v-model="query.noticeType" placeholder="请选择" clearable style="width: 140px">
            <el-option v-for="(label, val) in NOTICE_TYPES" :key="val" :label="label" :value="Number(val)" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="请选择" clearable style="width: 140px">
            <el-option v-for="(label, val) in NOTICE_STATUS" :key="val" :label="label" :value="Number(val)" />
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
      <div v-if="isAdmin" class="toolbar">
        <el-button type="primary" :icon="Plus" @click="openAdd">新增公告</el-button>
      </div>
      <el-table :data="records" v-loading="loading" border stripe>
        <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
        <el-table-column prop="noticeType" label="公告类型" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="NOTICE_TYPE_TAG[row.noticeType]">{{ NOTICE_TYPES[row.noticeType] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="publisherName" label="发布人" width="120" show-overflow-tooltip />
        <el-table-column prop="publishTime" label="发布时间" width="170">
          <template #default="{ row }">{{ row.publishTime || '-' }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="NOTICE_STATUS_TAG[row.status]">{{ NOTICE_STATUS[row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openDetail(row)">详情</el-button>
            <template v-if="isAdmin">
              <el-button type="primary" link @click="openEdit(row)">编辑</el-button>
              <el-popconfirm title="确认删除该记录?" @confirm="handleDelete(row)">
                <template #reference>
                  <el-button type="danger" link>删除</el-button>
                </template>
              </el-popconfirm>
            </template>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination class="pager" background layout="total, sizes, prev, pager, next" :total="total"
        v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
        @current-change="load()" @size-change="load(1)" />
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="560px" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" v-loading="formLoading">
        <el-form-item label="公告标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入公告标题" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="公告内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="6" placeholder="请输入公告内容" />
        </el-form-item>
        <el-form-item label="公告类型" prop="noticeType">
          <el-radio-group v-model="form.noticeType">
            <el-radio :value="1">系统公告</el-radio>
            <el-radio :value="2">急救科普</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="发布状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">发布</el-radio>
            <el-radio :value="0">存草稿</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框(只读) -->
    <el-dialog v-model="detailVisible" title="公告详情" width="640px">
      <div v-loading="detailLoading">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="标题">{{ detail.title }}</el-descriptions-item>
          <el-descriptions-item label="公告类型">
            <el-tag :type="NOTICE_TYPE_TAG[detail.noticeType]">{{ NOTICE_TYPES[detail.noticeType] }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="发布人">{{ detail.publisherName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="发布时间">{{ detail.publishTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="NOTICE_STATUS_TAG[detail.status]">{{ NOTICE_STATUS[detail.status] }}</el-tag>
          </el-descriptions-item>
        </el-descriptions>
        <div class="detail-content">{{ detail.content }}</div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { pageNoticeApi, getNoticeApi, addNoticeApi, updateNoticeApi, deleteNoticeApi } from '@/api/notice'
import { useUserStore } from '@/stores/user'

// 枚举字典: 公告类型 1-系统公告 2-急救科普; 公告状态 1-已发布 0-草稿
const NOTICE_TYPES = { 1: '系统公告', 2: '急救科普' }
const NOTICE_TYPE_TAG = { 1: 'primary', 2: 'success' }
const NOTICE_STATUS = { 1: '已发布', 0: '草稿' }
const NOTICE_STATUS_TAG = { 1: 'success', 0: 'info' }

// 发布/修改/删除仅管理员可用(接口权限), 详情全员可见
const userStore = useUserStore()
const isAdmin = computed(() => userStore.roleCode === 'admin')

const loading = ref(false)
const records = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, title: '', noticeType: '', status: '' })

// 分页查询(空条件不传参, 避免后端绑定空字符串失败)
const load = async (page) => {
  if (page) query.pageNum = page
  loading.value = true
  try {
    const params = { pageNum: query.pageNum, pageSize: query.pageSize }
    if (query.title) params.title = query.title
    if (query.noticeType !== '' && query.noticeType != null) params.noticeType = query.noticeType
    if (query.status !== '' && query.status != null) params.status = query.status
    const { data } = await pageNoticeApi(params)
    records.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  Object.assign(query, { pageNum: 1, pageSize: 10, title: '', noticeType: '', status: '' })
  load()
}

// 新增/编辑表单
const dialog = reactive({ visible: false, title: '新增公告' })
const formRef = ref()
const formLoading = ref(false)
const saving = ref(false)
const form = reactive({ id: null, title: '', content: '', noticeType: 1, status: 1 })

const rules = {
  title: [{ required: true, message: '请输入公告标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入公告内容', trigger: 'blur' }]
}

const resetForm = () => {
  Object.assign(form, { id: null, title: '', content: '', noticeType: 1, status: 1 })
  formRef.value?.clearValidate()
}

const openAdd = () => {
  dialog.title = '新增公告'
  dialog.visible = true
}

// 编辑: 拉取详情回填(保证 content 为最新)
const openEdit = async (row) => {
  dialog.title = '编辑公告'
  dialog.visible = true
  formLoading.value = true
  try {
    const { data } = await getNoticeApi(row.id)
    Object.assign(form, {
      id: data.id, title: data.title, content: data.content,
      noticeType: data.noticeType, status: data.status
    })
  } finally {
    formLoading.value = false
  }
}

const submit = async () => {
  await formRef.value.validate()
  saving.value = true
  try {
    if (form.id) {
      await updateNoticeApi(form)
    } else {
      await addNoticeApi(form)
    }
    ElMessage.success('保存成功')
    dialog.visible = false
    load()
  } finally {
    saving.value = false
  }
}

const handleDelete = async (row) => {
  await deleteNoticeApi(row.id)
  ElMessage.success('删除成功')
  load()
}

// 详情(只读)
const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = ref({})

const openDetail = async (row) => {
  detailVisible.value = true
  detailLoading.value = true
  try {
    const { data } = await getNoticeApi(row.id)
    detail.value = data
  } finally {
    detailLoading.value = false
  }
}

onMounted(() => load())
</script>

<style scoped>
.page { padding: 4px; }
.toolbar { margin-bottom: 14px; }
.pager { margin-top: 14px; float: right; }
.detail-content { margin-top: 14px; padding: 12px; background: #f5f7fa; border-radius: 4px; line-height: 1.8; white-space: pre-wrap; }
</style>
