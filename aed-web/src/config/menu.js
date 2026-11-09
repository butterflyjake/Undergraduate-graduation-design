// 侧边栏菜单配置: roles 缺省表示所有角色可见; icon 为 Element Plus 图标组件名
export const menus = [
  { path: '/dashboard', title: '数据看板', icon: 'Odometer' },
  { path: '/device', title: '设备台账', icon: 'Monitor', roles: ['admin', 'device_manager'] },
  { path: '/part', title: '配件管理', icon: 'Box', roles: ['admin', 'device_manager'] },
  { path: '/inspection/task', title: '巡检任务', icon: 'List' },
  { path: '/inspection/record', title: '巡检记录', icon: 'Document' },
  { path: '/repair', title: '维修工单', icon: 'Tools' },
  { path: '/usage', title: '使用记录', icon: 'Histogram' },
  { path: '/warning', title: '到期预警', icon: 'Warning' },
  { path: '/notice', title: '公告管理', icon: 'Bell' },
  { path: '/building', title: '楼宇管理', icon: 'OfficeBuilding', roles: ['admin', 'device_manager'] },
  { path: '/user', title: '用户管理', icon: 'UserFilled', roles: ['admin'] },
  { path: '/log', title: '操作日志', icon: 'Tickets', roles: ['admin'] }
]
