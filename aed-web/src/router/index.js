import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('@/layout/Layout.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', component: () => import('@/views/Dashboard.vue'), meta: { title: '数据看板' } },
      { path: 'device', component: () => import('@/views/Device.vue'), meta: { title: '设备台账', roles: ['admin', 'device_manager'] } },
      { path: 'part', component: () => import('@/views/Part.vue'), meta: { title: '配件管理', roles: ['admin', 'device_manager'] } },
      { path: 'inspection/task', component: () => import('@/views/InspectionTask.vue'), meta: { title: '巡检任务' } },
      { path: 'inspection/record', component: () => import('@/views/InspectionRecord.vue'), meta: { title: '巡检记录' } },
      { path: 'repair', component: () => import('@/views/Repair.vue'), meta: { title: '维修工单' } },
      { path: 'usage', component: () => import('@/views/Usage.vue'), meta: { title: '使用记录' } },
      { path: 'warning', component: () => import('@/views/Warning.vue'), meta: { title: '到期预警' } },
      { path: 'notice', component: () => import('@/views/Notice.vue'), meta: { title: '公告管理' } },
      { path: 'building', component: () => import('@/views/Building.vue'), meta: { title: '楼宇管理', roles: ['admin', 'device_manager'] } },
      { path: 'user', component: () => import('@/views/User.vue'), meta: { title: '用户管理', roles: ['admin'] } },
      { path: 'log', component: () => import('@/views/Log.vue'), meta: { title: '操作日志', roles: ['admin'] } }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/dashboard' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫: 登录校验 + 角色权限校验
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path === '/login') {
    return next()
  }
  if (!token) {
    return next('/login')
  }
  if (to.meta.roles) {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    if (!to.meta.roles.includes(userInfo.roleCode)) {
      return next('/dashboard')
    }
  }
  next()
})

export default router
