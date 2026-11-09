<template>
  <div class="page">
    <!-- 搜索区 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="query" @submit.prevent>
        <el-form-item label="账号/姓名">
          <el-input v-model="query.keyword" placeholder="输入账号或姓名" clearable style="width: 200px"
            @keyup.enter="load(1)" @clear="load(1)" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="query.roleId" placeholder="全部" clearable style="width: 160px">
            <el-option v-for="r in roles" :key="r.id" :label="r.roleName" :value="r.id" />
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
        <el-button type="primary" :icon="Plus" @click="openAdd">新增用户</el-button>
      </div>
      <el-table :data="records" v-loading="loading" border stripe>
        <el-table-column prop="username" label="登录账号" width="140" />
        <el-table-column prop="realName" label="姓名" width="120" />
        <el-table-column prop="phone" label="联系电话" width="140">
          <template #default="{ row }">{{ row.phone || '-' }}</template>
        </el-table-column>
        <el-table-column prop="roleName" label="角色" width="140">
          <template #default="{ row }">
            <el-tag>{{ row.roleName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ USER_STATUS[row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="Edit" @click="openEdit(row)">编辑</el-button>
            <!-- 登录用户本人不可删除 -->
            <el-tooltip :disabled="!isSelf(row)" content="不能删除自己" placement="top">
              <span>
                <el-popconfirm title="确认删除该记录?" :disabled="isSelf(row)" @confirm="handleDelete(row)">
                  <template #reference>
                    <el-button link type="danger" :icon="Delete" :disabled="isSelf(row)">删除</el-button>
                  </template>
                </el-popconfirm>
              </span>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination class="pager" background layout="total, sizes, prev, pager, next" :total="total"
        v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
        @current-change="load()" @size-change="load(1)" />
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="560px" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="登录账号" prop="username">
          <el-input v-model="form.username" placeholder="请输入登录账号" maxlength="50" clearable />
        </el-form-item>
        <!-- 初始密码仅新增时填写 -->
        <el-form-item v-if="!form.id" label="初始密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入初始密码" maxlength="20" />
          <div class="form-tip">初始密码长度 6-20 位, 请创建后告知用户</div>
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入姓名" maxlength="50" clearable />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入联系电话" maxlength="20" clearable />
        </el-form-item>
        <el-form-item label="角色" prop="roleId">
          <el-select v-model="form.roleId" placeholder="请选择角色" style="width: 100%">
            <el-option v-for="r in roles" :key="r.id" :label="r.roleName" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Plus, Edit, Delete } from '@element-plus/icons-vue'
import { pageUserApi, addUserApi, updateUserApi, deleteUserApi, listRoleApi } from '@/api/user'
import { useUserStore } from '@/stores/user'

// 用户状态字典: 1-启用 0-禁用
const USER_STATUS = { 1: '启用', 0: '禁用' }

const userStore = useUserStore()

const loading = ref(false)
const records = ref([])
const total = ref(0)
const roles = ref([])
// 分页查询条件: keyword 模糊匹配账号或姓名, roleId 按角色过滤
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', roleId: null })

// 加载列表
const load = async (page) => {
  if (page) query.pageNum = page
  loading.value = true
  try {
    const { data } = await pageUserApi(query)
    records.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

// 重置查询条件
const resetQuery = () => {
  Object.assign(query, { pageNum: 1, pageSize: 10, keyword: '', roleId: null })
  load()
}

// 加载角色下拉
const loadRoles = async () => {
  const { data } = await listRoleApi()
  roles.value = data || []
}

// ---------- 新增/编辑 ----------
const formRef = ref()
const saving = ref(false)
const dialog = reactive({ visible: false, title: '' })
// 表单默认值工厂(避免对象引用复用)
const initForm = () => ({ id: null, username: '', password: '', realName: '', phone: '', roleId: null, status: 1 })
const form = reactive(initForm())

const rules = {
  username: [
    { required: true, message: '请输入登录账号', trigger: 'blur' },
    { max: 50, message: '登录账号长度不能超过50个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入初始密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度需在6-20位之间', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { max: 50, message: '姓名长度不能超过50个字符', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1\d{10}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  roleId: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

// 是否当前登录用户本人
const isSelf = row => Number(row.id) === Number(userStore.userInfo.id)

// 新增
const openAdd = () => {
  dialog.title = '新增用户'
  dialog.visible = true
}

// 编辑(不含密码字段)
const openEdit = (row) => {
  dialog.title = '编辑用户'
  Object.assign(form, {
    id: row.id,
    username: row.username,
    realName: row.realName,
    phone: row.phone,
    roleId: row.roleId,
    status: row.status
  })
  dialog.visible = true
}

// 对话框关闭后重置表单与校验状态
const resetForm = () => {
  Object.assign(form, initForm())
  formRef.value?.clearValidate()
}

// 提交保存
const submit = async () => {
  await formRef.value.validate()
  saving.value = true
  try {
    if (form.id) {
      // 编辑: 不传密码
      await updateUserApi({
        id: form.id, username: form.username, realName: form.realName,
        phone: form.phone, roleId: form.roleId, status: form.status
      })
      ElMessage.success('修改成功')
    } else {
      await addUserApi(form)
      ElMessage.success('新增成功, 初始密码为: ' + form.password)
    }
    dialog.visible = false
    load()
  } finally {
    saving.value = false
  }
}

// 删除
const handleDelete = async (row) => {
  await deleteUserApi(row.id)
  ElMessage.success('删除成功')
  // 删除当前页最后一条时回退一页
  if (records.value.length === 1 && query.pageNum > 1) query.pageNum--
  load()
}

onMounted(() => {
  load()
  loadRoles()
})
</script>

<style scoped>
.page { padding: 4px; }
.toolbar { margin-bottom: 14px; }
.pager { margin-top: 14px; float: right; }
.form-tip { font-size: 12px; color: #909399; line-height: 1.5; }
</style>
