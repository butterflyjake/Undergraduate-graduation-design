import request from '@/utils/request'

/** 看板总览卡片数据 */
export const getOverviewApi = () => request.get('/dashboard/overview')

/** 设备状态分布(饼图) */
export const getDeviceStatusApi = () => request.get('/dashboard/deviceStatus')

/** 近N天巡检趋势(折线) */
export const getInspectionTrendApi = days => request.get('/dashboard/inspectionTrend', { params: { days } })

/** 近N月维修趋势(柱状) */
export const getRepairTrendApi = months => request.get('/dashboard/repairTrend', { params: { months } })

/** 近N月使用趋势(柱状) */
export const getUsageTrendApi = months => request.get('/dashboard/usageTrend', { params: { months } })

/** 设备楼宇分布(柱状) */
export const getBuildingDistApi = () => request.get('/dashboard/buildingDist')

/** 临期/过期配件清单 */
export const getExpiringListApi = () => request.get('/dashboard/expiringList')
