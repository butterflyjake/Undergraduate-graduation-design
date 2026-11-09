<template>
  <div class="page">
    <!-- 搜索区 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="query" @submit.prevent>
        <el-form-item label="操作用户">
          <el-input v-model="query.username" placeholder="请输入用户名" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="操作内容">
          <el-input v-model="query.operation" placeholder="请输入操作内容" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="操作时间">
          <el-date-picker v-model="query.createTime" type="datetimerange" range-separator="至"
            start-placeholder="开始时间" end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss" style="width: 360px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="load(1)">查询</el-button>
          <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 日志列表(只读) -->
    <el-card shadow="never">
      <el-table :data="records" v-loading="loading" border stripe empty-text="暂无数据">
        <el-table-column prop="username" label="操作用户" width="120" />
        <el-table-column prop="operation" label="操作内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="method" label="请求方式" width="100" />
        <el-table-column prop="params" label="请求参数" min-width="220" show-overflow-tooltip />
        <el-table-column prop="ip" label="IP地址" width="140" />
        <el-table-column prop="createTime" label="操作时间" width="170" />
      </el-table>
      <el-pagination class="pager" background layout="total, sizes, prev, pager, next" :total="total"
        v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
        @current-change="load()" @size-change="load(1)" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, Refresh } from '@element-plus/icons-vue'
import { pageLogApi } from '@/api/log'

const loading = ref(false)
const records = ref([])
const total = ref(0)

// 分页与搜索条件
const query = reactive({ pageNum: 1, pageSize: 10, username: '', operation: '', createTime: null })

// 加载日志列表
const load = async page => {
  if (page) query.pageNum = page
  loading.value = true
  try {
    const { data } = await pageLogApi(query)
    records.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

// 重置搜索条件并刷新
const resetQuery = () => {
  Object.assign(query, { pageNum: 1, pageSize: 10, username: '', operation: '', createTime: null })
  load()
}

onMounted(() => load())
</script>

<style scoped>
.page {
  padding: 4px;
}

.pager {
  margin-top: 14px;
  float: right;
}
</style>
