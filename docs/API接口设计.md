# 高校AED器械管理系统 — API 接口设计

> 本文档是后端实现与前端对接的唯一契约，也是毕业设计论文"系统设计"章节的素材。

## 1. 接口规范

- **基础地址**：`http://localhost:8080`，统一前缀 `/api`
- **认证方式**：请求头 `Authorization: Bearer <token>`（登录接口除外），基于 JWT 无状态认证
- **统一响应体**：`{ "code": 200, "message": "操作成功", "data": ... }`
- **分页响应体**：data 为 `{ "total": 100, "records": [...] }`
- **错误码**：200 成功 / 400 参数错误 / 401 未登录 / 403 无权限 / 500 业务或系统错误
- **日期格式**：`yyyy-MM-dd`（日期）、`yyyy-MM-dd HH:mm:ss`（时间）

## 2. 角色与权限矩阵

| 功能模块 | 系统管理员 admin | 设备管理员 device_manager | 巡检人员 inspector |
|---|---|---|---|
| 设备台账 | 增删改查 | 增删改查 | 查看 |
| 配件管理 | 增删改查 | 增删改查 | 查看 |
| 巡检任务 | 查看、生成 | 查看、生成 | 查看本人任务、执行巡检 |
| 巡检记录 | 查看 | 查看 | 查看本人记录、提交 |
| 维修工单 | 全部操作 | 上报、受理、完成、驳回 | 上报、查看 |
| 使用记录 | 查看、登记 | 查看、登记 | 查看、登记 |
| 到期预警 | 查看、处理 | 查看、处理 | 查看 |
| 公告 | 全部操作 | 查看已发布 | 查看已发布 |
| 楼宇管理 | 增删改查 | 增删改查 | 查看 |
| 用户/角色/日志 | 全部操作 | - | - |
| 数据看板 | 查看 | 查看 | 查看 |

## 3. 接口明细

### 3.1 认证与用户（auth-user）

| 接口 | 方法 | 权限 | 说明 |
|---|---|---|---|
| /api/auth/login | POST | 公开 | 登录，返回 token 与用户信息 |
| /api/auth/info | GET | 登录 | 获取当前登录用户信息 |
| /api/auth/password | PUT | 登录 | 修改自己的密码 |
| /api/user/page | GET | admin | 用户分页查询 |
| /api/user | POST | admin | 新增用户 |
| /api/user | PUT | admin | 修改用户 |
| /api/user/{id} | DELETE | admin | 删除用户（逻辑删除） |
| /api/role/list | GET | admin | 角色列表（下拉用） |
| /api/log/page | GET | admin | 操作日志分页查询 |

```json
// POST /api/auth/login 请求
{ "username": "admin", "password": "123456" }
// 响应 data
{ "token": "eyJhbGciOi...", "id": 1, "username": "admin", "realName": "系统管理员",
  "roleId": 1, "roleCode": "admin", "roleName": "系统管理员" }

// POST /api/user 请求
{ "username": "zhangsan", "password": "123456", "realName": "张三",
  "phone": "13800000000", "roleId": 3, "status": 1 }
// 分页响应 data (UserVO)
{ "total": 10, "records": [ { "id": 1, "username": "admin", "realName": "系统管理员",
  "phone": null, "roleId": 1, "roleName": "系统管理员", "status": 1,
  "createTime": "2026-10-28 10:00:00" } ] }

// PUT /api/auth/password 请求
{ "oldPassword": "123456", "newPassword": "abc12345" }
```

### 3.2 楼宇管理（building）

| 接口 | 方法 | 权限 | 说明 |
|---|---|---|---|
| /api/building/list | GET | 登录 | 楼宇列表（不分页） |
| /api/building | POST | admin, device_manager | 新增楼宇 |
| /api/building | PUT | admin, device_manager | 修改楼宇 |
| /api/building/{id} | DELETE | admin, device_manager | 删除楼宇 |

```json
{ "buildingName": "图书馆", "campusArea": "主校区", "address": "正门东侧", "remark": "" }
```

### 3.3 设备台账（device）

| 接口 | 方法 | 权限 | 说明 |
|---|---|---|---|
| /api/device/page | GET | 登录 | 设备分页查询（keyword/buildingId/status 过滤） |
| /api/device/list | GET | 登录 | 设备简表（下拉用，不分页） |
| /api/device/{id} | GET | 登录 | 设备详情 |
| /api/device | POST | admin, device_manager | 新增设备 |
| /api/device | PUT | admin, device_manager | 修改设备 |
| /api/device/status | PUT | admin, device_manager | 快速变更状态 {id, status} |
| /api/device/{id} | DELETE | admin, device_manager | 删除设备 |

```json
// POST /api/device 请求
{ "deviceCode": "AED-001", "deviceName": "迈瑞 BeneHeart D1", "brand": "迈瑞",
  "serialNo": "MR2026XXXX", "manufacturer": "深圳迈瑞", "purchaseDate": "2025-06-01",
  "serviceLife": 8, "buildingId": 1, "floor": "1层", "positionDesc": "图书馆大厅服务台旁",
  "status": 0, "photo": "/upload/xxx.jpg", "remark": "" }
// 响应 data (DeviceVO 增加 buildingName 字段)
```

### 3.4 配件管理（part）

| 接口 | 方法 | 权限 | 说明 |
|---|---|---|---|
| /api/part/page | GET | 登录 | 配件分页查询（deviceId/partType/status 过滤） |
| /api/part/list | GET | 登录 | 某设备配件列表（deviceId/status 过滤，不分页） |
| /api/part | POST | admin, device_manager | 新增配件（自动替换旧配件） |
| /api/part | PUT | admin, device_manager | 修改配件 |
| /api/part/{id} | DELETE | admin, device_manager | 删除配件 |

```json
// POST /api/part 请求
{ "deviceId": 1, "partType": 1, "batchNo": "EP-2026-01",
  "installDate": "2026-01-10", "expireDate": "2028-01-10", "remark": "" }
// 响应 data (PartVO 增加 deviceCode/deviceName 字段)
```

### 3.5 巡检管理（inspection）

| 接口 | 方法 | 权限 | 说明 |
|---|---|---|---|
| /api/inspection/task/page | GET | 登录 | 任务分页（status/deviceId/planDate 过滤；inspector 仅本人） |
| /api/inspection/task/generate | POST | admin, device_manager | 手动生成巡检任务 {planDate} |
| /api/inspection/task/{id} | GET | 登录 | 任务详情 |
| /api/inspection/record/submit | POST | inspector | 提交巡检记录 |
| /api/inspection/record/page | GET | 登录 | 记录分页（inspector 仅本人） |
| /api/inspection/record/{id} | GET | 登录 | 记录详情（含明细） |

```json
// POST /api/inspection/record/submit 请求
{ "taskId": 1, "summary": "设备运行正常",
  "items": [
    { "itemName": "主机外观完好无破损", "itemResult": 1, "remark": "" },
    { "itemName": "电源指示灯显示正常", "itemResult": 1, "remark": "" },
    { "itemName": "电极片在有效期内", "itemResult": 1, "remark": "" },
    { "itemName": "电池电量充足", "itemResult": 1, "remark": "" },
    { "itemName": "机柜箱体完好", "itemResult": 1, "remark": "" },
    { "itemName": "设备周边无障碍物", "itemResult": 1, "remark": "" }
  ] }
// 记录详情 data (InspectionRecordDetailVO)
{ "id": 1, "taskId": 1, "deviceId": 1, "deviceCode": "AED-001", "deviceName": "迈瑞 BeneHeart D1",
  "inspectorId": 3, "inspectorName": "张三", "inspectTime": "2026-10-28 09:30:00",
  "result": 1, "summary": "设备运行正常",
  "items": [ { "itemName": "主机外观完好无破损", "itemResult": 1, "remark": "" } ] }
```

### 3.6 维修工单（repair）

| 接口 | 方法 | 权限 | 说明 |
|---|---|---|---|
| /api/repair/page | GET | 登录 | 工单分页（status/deviceId 过滤） |
| /api/repair | POST | 登录 | 手动报修 |
| /api/repair/{id}/accept | POST | admin, device_manager | 受理（待处理→维修中） |
| /api/repair/{id}/complete | POST | admin, device_manager | 完成（维修中→已完成） |
| /api/repair/{id}/reject | POST | admin, device_manager | 驳回（待处理→已驳回） |
| /api/repair/{id} | GET | 登录 | 工单详情 |

```json
// POST /api/repair 请求
{ "deviceId": 1, "faultDesc": "电极片有效期临近, 需要更换", "faultPhoto": "/upload/xxx.jpg" }
// POST /api/repair/{id}/complete 请求
{ "repairDesc": "更换电极片并测试通过", "repairCost": 350.00 }
// POST /api/repair/{id}/reject 请求
{ "rejectReason": "该设备已停用, 无需维修" }
// 响应 data (RepairOrderVO 增加 deviceCode/deviceName/reporterName/handlerName 字段)
```

### 3.7 使用记录（usage）

| 接口 | 方法 | 权限 | 说明 |
|---|---|---|---|
| /api/usage/page | GET | 登录 | 使用记录分页（deviceId 过滤） |
| /api/usage | POST | 登录 | 登记使用记录 |
| /api/usage/{id} | GET | 登录 | 使用记录详情 |

```json
// POST /api/usage 请求
{ "deviceId": 1, "occurTime": "2026-10-20 14:30:00", "locationDesc": "图书馆一楼大厅",
  "patientDesc": "男性, 约50岁, 突发晕厥", "resultDesc": "现场心肺复苏后恢复意识, 已送医",
  "remark": "" }
```

### 3.8 到期预警（warning）

| 接口 | 方法 | 权限 | 说明 |
|---|---|---|---|
| /api/warning/page | GET | 登录 | 预警分页（status/warningType 过滤） |
| /api/warning/{id}/handle | POST | admin, device_manager | 处理预警 |

```json
// POST /api/warning/{id}/handle 请求
{ "handleDesc": "已采购新电极片, 本周更换" }
// 响应 data (WarningVO 增加 deviceCode/deviceName/partType/expireDate 字段)
```

### 3.9 公告（notice）

| 接口 | 方法 | 权限 | 说明 |
|---|---|---|---|
| /api/notice/page | GET | 登录 | 公告分页（非 admin 仅见已发布） |
| /api/notice/{id} | GET | 登录 | 公告详情 |
| /api/notice | POST | admin | 发布公告 |
| /api/notice | PUT | admin | 修改公告 |
| /api/notice/{id} | DELETE | admin | 删除公告 |

```json
{ "title": "关于开展AED急救培训的通知", "content": "……", "noticeType": 1, "status": 1 }
```

### 3.10 统计看板（dashboard）

| 接口 | 方法 | 说明 | 返回结构 |
|---|---|---|---|
| /api/dashboard/overview | GET | 总览卡片 | `{deviceTotal, inUseCount, pendingTaskCount, overdueTaskCount, pendingRepairCount, pendingWarningCount, expiredPartCount}` |
| /api/dashboard/deviceStatus | GET | 设备状态分布(饼图) | `[{status, name, count}]` |
| /api/dashboard/inspectionTrend?days=7 | GET | 近N天巡检趋势(折线) | `[{date, total, done}]` |
| /api/dashboard/repairTrend?months=6 | GET | 近N月维修趋势(柱状) | `[{month, count}]` |
| /api/dashboard/usageTrend?months=6 | GET | 近N月使用趋势(柱状) | `[{month, count}]` |
| /api/dashboard/buildingDist | GET | 设备楼宇分布(柱状) | `[{buildingName, count}]` |
| /api/dashboard/expiringList | GET | 临期/过期配件清单 | `[{deviceCode, deviceName, partType, expireDate, daysRemaining}]` |

### 3.11 文件上传（file）

| 接口 | 方法 | 说明 |
|---|---|---|
| /api/file/upload | POST | multipart 表单，字段名 `file`，仅限图片(jpg/jpeg/png/gif/webp)，返回 `{url: "/upload/xxx.jpg"}` |

## 4. 定时任务（Quartz）

| 任务 | 执行时间 | 逻辑说明 |
|---|---|---|
| 巡检任务自动生成 | 每天 08:00 | ① 将昨日未完成的待巡检任务标记为"已逾期"；② 为所有"在用"设备生成当日巡检任务；③ 任务分配给当前待巡检任务最少的巡检人员（负载均衡） |
| 配件到期预警扫描 | 每天 07:00 | ① 扫描"使用中"配件：已过期 → 类型2；30天内到期 → 类型1；② 同一配件已有未处理预警则不重复生成；③ 已被更换的配件的未处理预警自动关闭 |

## 5. 关键业务规则

1. **配件更换联动**：同一设备同一类型的新配件入库时，旧配件自动置为"已更换"，其未处理预警自动关闭；
2. **巡检闭环**：巡检提交时若存在异常项，自动生成维修工单（来源=巡检异常）；
3. **用户保护**：不能删除/禁用自己；系统必须至少保留一个启用状态的系统管理员；
4. **公告可见性**：非管理员角色仅可见"已发布"公告；
5. **密码安全**：密码 BCrypt 加密存储；接口响应与操作日志均不记录密码明文。
