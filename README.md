# 高校AED器械管理系统

毕业设计项目：基于 SpringBoot 的高校AED器械管理系统的设计与实现。

> 📖 完整版说明文档（架构/模块/接口清单/业务流程/数据库设计/部署）见 [docs/项目说明文档.md](docs/项目说明文档.md)

- **前端**：Vue 3 + Vite + Element Plus + ECharts + Pinia + Axios
- **后端**：Spring Boot 3.2 + MyBatis-Plus + Spring Security(JWT) + Quartz + Knife4j
- **数据库**：MySQL 5.5+（utf8mb4）

## 目录结构

```
毕业设计代码/
├── aed-server/          # 后端(Spring Boot, 端口 8080)
│   └── src/main/java/com/campus/aed/
├── aed-web/             # 前端(Vue3 + Vite, 端口 5173)
├── sql/
│   ├── init.sql         # 建库建表脚本(13 张表 + 3 个角色)
│   └── demo_data.sql    # 演示数据(可选, 答辩截图用)
└── docs/                # 设计文档(论文素材)
    ├── 数据库设计.md
    ├── API接口设计.md
    └── 前端设计.md
```

## 环境要求

| 软件 | 版本要求 |
|---|---|
| JDK | 17 |
| Maven | 3.6+ |
| Node.js | 18+ |
| MySQL | 5.5 及以上（原开发环境为 5.5.45） |

## 启动步骤

### 1. 初始化数据库

注意：`sql/init.sql` 会删除并重建 `aed_system` 数据库。仅在新建的演示数据库执行；已有数据请先备份。以下导入命令适用于 Bash 或 cmd，PowerShell 可使用 MySQL 客户端的 `source` 命令。

```bash
mysql -uroot -p --default-character-set=utf8mb4 < sql/init.sql
```

- 建库 `aed_system` + 13 张表 + 3 个角色（系统管理员/设备管理员/巡检人员）
- **默认管理员 admin/123456 在后端首次启动时自动创建**（BCrypt 加密）

### 2. 配置并启动后端（终端 1）

首次下载后，将 `aed-server/application-local.example.yml` 复制为同目录下的 `application-local.yml`，填写数据库密码与自行生成的 JWT 随机密钥（至少 32 字节）。从 `aed-server` 目录启动时会自动加载该文件。该文件不上传 GitHub；也可通过 `DB_USERNAME`、`DB_PASSWORD`、`JWT_SECRET` 环境变量配置。

```bash
cd aed-server
mvn spring-boot:run
```

- 启动成功后访问接口文档：<http://localhost:8080/doc.html>（Knife4j）
- 首次启动会自动创建 admin 账号

### 3. 启动前端（终端 2）

```bash
cd aed-web
npm install     # 首次运行需要
npm run dev
```

- 浏览器访问：<http://localhost:5173>

### 4.（可选）导入演示数据

演示数据包含 12 台设备、配件临期/过期、巡检任务、维修工单等完整业务场景：

```bash
mysql -uroot -p --default-character-set=utf8mb4 aed_system < sql/demo_data.sql
```

## 演示账号（密码均为 123456）

| 账号 | 姓名 | 角色 | 说明 |
|---|---|---|---|
| admin | 系统管理员 | admin | 全部权限：用户管理、日志、公告 |
| zhangwei | 张伟 | device_manager | 设备台账、配件、维修工单、预警处理 |
| liming | 李明 | device_manager | 同上 |
| wangfang | 王芳 | inspector | 巡检任务执行、异常上报 |
| liuyang | 刘洋 | inspector | 同上 |
| chenjing | 陈静 | inspector | 同上 |

## 核心业务流程

1. **巡检闭环**：每天 08:00 Quartz 自动生成巡检任务并均衡分配给巡检员 → 巡检员执行巡检（6 个固定检查项）→ 任一异常自动创建维修工单（待处理）
2. **维修闭环**：设备管理员接单 → 维修中 → 完成（配件更换自动联动更新配件有效期、恢复设备状态）→ 也可驳回
3. **到期预警**：每天 07:00 Quartz 扫描 30 天内到期 / 已过期的配件 → 生成预警 → 设备管理员处理
4. **台账管理**：AED 设备、电极片/电池配件有效期、楼宇分布、使用记录

## 常见问题

- **MySQL 连接报错**：本项目兼容 MySQL 5.5，后端使用 `mysql-connector-java 5.1.49` 驱动，请勿升级为 8.x 驱动
- **端口占用**：后端 8080、前端 5173，占用时改 [aed-server/src/main/resources/application.yml](aed-server/src/main/resources/application.yml) 或 [aed-web/vite.config.js](aed-web/vite.config.js)
- **图片上传**：图片保存在后端 `aed-server/uploads/` 目录，通过 `/upload/**` 访问

## 文档导航

- [项目说明文档](docs/项目说明文档.md)：架构、业务流程和部署说明
- [数据库设计](docs/数据库设计.md)：表结构与关联
- [API 接口设计](docs/API接口设计.md)：接口清单与参数
- [前端设计](docs/前端设计.md)：页面与交互
- [开发与版本管理](docs/开发与版本管理.md)：构建检查与后续上传方式

## 仓库内容与数据

仓库保留前后端源码、npm 锁文件、SQL 初始化和演示脚本、设计文档及接口联调脚本。依赖目录、编译产物、运行日志、上传图片和本地密码配置不纳入版本管理。现有本地文件仍保留。

演示账号仅用于本地演示，部署前请修改密码。数据库和上传图片需要单独备份，GitHub 源码仓库不包含当前运行数据库。
