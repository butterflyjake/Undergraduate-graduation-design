<template>
  <div class="page">
    <!-- 顶部统计卡片 -->
    <el-row :gutter="16" v-loading="statsLoading">
      <el-col :span="4" v-for="item in statItems" :key="item.key">
        <el-card shadow="never">
          <div class="stat-item">
            <div class="stat-icon" :style="{ color: item.color, background: item.color + '1a' }">
              <el-icon><component :is="item.icon" /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ overview[item.key] ?? 0 }}</div>
              <div class="stat-label">{{ item.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表第一行: 设备状态分布 / 楼宇设备分布 -->
    <el-row :gutter="16" class="chart-row">
      <el-col :span="8">
        <el-card shadow="never">
          <template #header><span>设备状态分布</span></template>
          <div ref="pieRef" class="chart" v-loading="pieLoading"></div>
        </el-card>
      </el-col>
      <el-col :span="16">
        <el-card shadow="never">
          <template #header><span>楼宇设备分布</span></template>
          <div ref="buildingRef" class="chart" v-loading="buildingLoading"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表第二行: 巡检完成趋势 / 维修工单趋势 -->
    <el-row :gutter="16" class="chart-row">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span>近30天巡检完成趋势</span></template>
          <div ref="inspectionRef" class="chart" v-loading="inspectionLoading"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span>近6个月维修工单趋势</span></template>
          <div ref="repairRef" class="chart" v-loading="repairLoading"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 临期配件清单 -->
    <el-card shadow="never" class="chart-row">
      <template #header><span>临期配件清单（30天内到期）</span></template>
      <el-table :data="expiringList" v-loading="listLoading" border stripe>
        <el-table-column prop="deviceCode" label="设备编号" min-width="110" />
        <el-table-column prop="deviceName" label="设备名称" min-width="160" />
        <el-table-column label="配件类型" width="110">
          <template #default="{ row }">
            <el-tag type="info">{{ PART_TYPES[row.partType] || '未知' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="expireDate" label="到期日期" width="120" />
        <el-table-column label="剩余天数" width="120">
          <template #default="{ row }">
            <el-tag :type="row.daysRemaining < 0 ? 'danger' : 'warning'">
              {{ row.daysRemaining < 0 ? '已过期' : '剩余 ' + row.daysRemaining + ' 天' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import { Monitor, CircleCheck, List, AlarmClock, Tools, WarningFilled } from '@element-plus/icons-vue'
import {
  getOverviewApi,
  getDeviceStatusApi,
  getBuildingDistApi,
  getInspectionTrendApi,
  getRepairTrendApi,
  getExpiringListApi
} from '@/api/dashboard'

// 配件类型字典
const PART_TYPES = { 1: '电极片', 2: '电池' }
// 设备状态饼图配色(0-在用 1-维修中 2-停用 3-已报废), 经色觉无障碍校验
const STATUS_COLORS = { 0: '#1baf7a', 1: '#eb6834', 2: '#8a8f98', 3: '#e34948' }

// ---- 顶部统计卡片 ----
const statsLoading = ref(false)
const overview = reactive({
  deviceTotal: 0,
  inUseCount: 0,
  pendingTaskCount: 0,
  overdueTaskCount: 0,
  pendingRepairCount: 0,
  pendingWarningCount: 0
})
const statItems = [
  { key: 'deviceTotal', label: '设备总数', icon: Monitor, color: '#409eff' },
  { key: 'inUseCount', label: '在用设备', icon: CircleCheck, color: '#67c23a' },
  { key: 'pendingTaskCount', label: '待巡检任务', icon: List, color: '#e6a23c' },
  { key: 'overdueTaskCount', label: '已逾期任务', icon: AlarmClock, color: '#f56c6c' },
  { key: 'pendingRepairCount', label: '待处理工单', icon: Tools, color: '#e6a23c' },
  { key: 'pendingWarningCount', label: '未处理预警', icon: WarningFilled, color: '#f56c6c' }
]

// ---- 图表容器 ----
const pieRef = ref(null)
const buildingRef = ref(null)
const inspectionRef = ref(null)
const repairRef = ref(null)
let pieChart = null
let buildingChart = null
let inspectionChart = null
let repairChart = null

const pieLoading = ref(false)
const buildingLoading = ref(false)
const inspectionLoading = ref(false)
const repairLoading = ref(false)

// 空数据时在图表中央显示的标题
const emptyTitle = empty => ({
  show: empty,
  text: '暂无数据',
  left: 'center',
  top: 'middle',
  textStyle: { color: '#909399', fontSize: 14, fontWeight: 400 }
})

// ---- 图表渲染 ----
// 设备状态分布(环形饼图)
const renderDeviceStatus = data => {
  if (!pieChart) return
  const list = data || []
  const empty = !list.length
  pieChart.setOption({
    title: emptyTitle(empty),
    tooltip: { trigger: 'item', formatter: '{b}: {c} 台 ({d}%)' },
    legend: { show: !empty, bottom: 0, icon: 'circle', itemWidth: 10, itemHeight: 10 },
    series: [
      {
        type: 'pie',
        radius: ['45%', '70%'],
        center: ['50%', '42%'],
        itemStyle: { borderColor: '#ffffff', borderWidth: 2, borderRadius: 4 },
        label: { show: !empty, formatter: '{b}\n{c} 台' },
        labelLine: { length: 12, length2: 8 },
        data: list.map(item => ({
          name: item.name,
          value: item.count,
          itemStyle: { color: STATUS_COLORS[item.status] || '#909399' }
        }))
      }
    ]
  })
}

// 楼宇设备分布(柱状图)
const renderBuildingDist = data => {
  if (!buildingChart) return
  const list = data || []
  const empty = !list.length
  buildingChart.setOption({
    title: emptyTitle(empty),
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: 8, right: 16, top: 28, bottom: 4, containLabel: true },
    xAxis: {
      type: 'category',
      data: list.map(item => item.buildingName),
      axisTick: { alignWithLabel: true },
      axisLabel: { interval: 0, rotate: list.length > 10 ? 30 : 0 }
    },
    yAxis: { type: 'value', minInterval: 1 },
    series: [
      {
        name: '设备数',
        type: 'bar',
        data: list.map(item => item.count),
        barMaxWidth: 34,
        itemStyle: { color: '#409eff', borderRadius: [4, 4, 0, 0] },
        label: { show: !empty, position: 'top', color: '#606266', fontSize: 11 }
      }
    ]
  })
}

// 近30天巡检完成趋势(折线)
const renderInspectionTrend = data => {
  if (!inspectionChart) return
  const list = data || []
  const empty = !list.length
  inspectionChart.setOption({
    color: ['#409eff', '#67c23a'],
    title: emptyTitle(empty),
    tooltip: { trigger: 'axis' },
    legend: { show: !empty, top: 0, icon: 'roundRect', itemWidth: 12, itemHeight: 4 },
    grid: { left: 8, right: 24, top: empty ? 8 : 36, bottom: 4, containLabel: true },
    xAxis: { type: 'category', boundaryGap: false, data: list.map(item => item.date) },
    yAxis: { type: 'value', minInterval: 1 },
    series: [
      {
        name: '任务总数',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: { width: 2 },
        itemStyle: { borderColor: '#ffffff', borderWidth: 2 },
        endLabel: { show: !empty, formatter: '{a}', color: '#606266' },
        data: list.map(item => item.total)
      },
      {
        name: '已完成',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: { width: 2 },
        itemStyle: { borderColor: '#ffffff', borderWidth: 2 },
        endLabel: { show: !empty, formatter: '{a}', color: '#606266' },
        data: list.map(item => item.done)
      }
    ]
  })
}

// 近6个月维修工单趋势(折线)
const renderRepairTrend = data => {
  if (!repairChart) return
  const list = data || []
  const empty = !list.length
  repairChart.setOption({
    title: emptyTitle(empty),
    tooltip: { trigger: 'axis' },
    grid: { left: 8, right: 24, top: 28, bottom: 4, containLabel: true },
    xAxis: { type: 'category', boundaryGap: false, data: list.map(item => item.month) },
    yAxis: { type: 'value', minInterval: 1 },
    series: [
      {
        name: '工单数',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: { width: 2 },
        itemStyle: { color: '#409eff', borderColor: '#ffffff', borderWidth: 2 },
        data: list.map(item => item.count)
      }
    ]
  })
}

// ---- 数据加载 ----
const loadOverview = async () => {
  statsLoading.value = true
  try {
    const { data } = await getOverviewApi()
    Object.assign(overview, data || {})
  } catch {
    // 错误提示已由 request 拦截器统一弹出
  } finally {
    statsLoading.value = false
  }
}

const loadDeviceStatus = async () => {
  pieLoading.value = true
  try {
    const { data } = await getDeviceStatusApi()
    renderDeviceStatus(data)
  } catch {
    // 错误提示已由 request 拦截器统一弹出
  } finally {
    pieLoading.value = false
  }
}

const loadBuildingDist = async () => {
  buildingLoading.value = true
  try {
    const { data } = await getBuildingDistApi()
    renderBuildingDist(data)
  } catch {
    // 错误提示已由 request 拦截器统一弹出
  } finally {
    buildingLoading.value = false
  }
}

const loadInspectionTrend = async () => {
  inspectionLoading.value = true
  try {
    const { data } = await getInspectionTrendApi(30)
    renderInspectionTrend(data)
  } catch {
    // 错误提示已由 request 拦截器统一弹出
  } finally {
    inspectionLoading.value = false
  }
}

const loadRepairTrend = async () => {
  repairLoading.value = true
  try {
    const { data } = await getRepairTrendApi(6)
    renderRepairTrend(data)
  } catch {
    // 错误提示已由 request 拦截器统一弹出
  } finally {
    repairLoading.value = false
  }
}

// 临期配件清单
const listLoading = ref(false)
const expiringList = ref([])

const loadExpiring = async () => {
  listLoading.value = true
  try {
    const { data } = await getExpiringListApi()
    expiringList.value = data || []
  } catch {
    // 错误提示已由 request 拦截器统一弹出
  } finally {
    listLoading.value = false
  }
}

// ---- 图表生命周期 ----
const initCharts = () => {
  pieChart = echarts.init(pieRef.value)
  buildingChart = echarts.init(buildingRef.value)
  inspectionChart = echarts.init(inspectionRef.value)
  repairChart = echarts.init(repairRef.value)
}

// 窗口尺寸变化时自适应
const handleResize = () => {
  ;[pieChart, buildingChart, inspectionChart, repairChart].forEach(chart => chart && chart.resize())
}

onMounted(() => {
  initCharts()
  window.addEventListener('resize', handleResize)
  loadOverview()
  loadDeviceStatus()
  loadBuildingDist()
  loadInspectionTrend()
  loadRepairTrend()
  loadExpiring()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  ;[pieChart, buildingChart, inspectionChart, repairChart].forEach(chart => chart && chart.dispose())
})
</script>

<style scoped>
.page {
  padding: 4px;
}

.chart-row {
  margin-top: 16px;
}

.chart {
  height: 300px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.stat-icon {
  width: 46px;
  height: 46px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  flex-shrink: 0;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 2px;
}
</style>
