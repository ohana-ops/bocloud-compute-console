<h1 align="center" style="margin: 30px 0 30px; font-weight: bold;">博云算力资源管理后台系统</h1>
<h4 align="center">BoCloud Compute Console · 基于若依微服务框架二次开发的企业级算力资源管理平台</h4>

## 项目简介

博云算力资源管理后台系统，是基于 [RuoYi-Cloud](https://gitee.com/y_project/RuoYi-Cloud)（若依微服务框架 v3.6.8）二次开发的企业级算力资源管理平台。

系统面向算力运营场景，实现了算力资源（GPU 卡 / 国产加速卡 / 计算节点 / CPU 资源）的统一纳管、资源申请、审批分配、使用统计等核心业务流程，支持多角色权限控制与全链路操作审计。

> 技术底座采用若依微服务架构，业务层新增 `ruoyi-bocompute` 微服务模块，复用若依自带的用户、角色、菜单、字典、日志等系统能力。

## 技术栈

| 分类 | 技术 | 版本 |
| :--- | :--- | :--- |
| 后端框架 | Spring Boot | 4.0.3 |
| 微服务 | Spring Cloud / Spring Cloud Alibaba | 2025.1.0 / 2025.1.0.0 |
| 注册/配置中心 | Nacos | 3.x |
| 网关 | Spring Cloud Gateway | - |
| 认证授权 | Spring Security + Redis Token | - |
| ORM | MyBatis | 4.0.1 |
| 数据库连接池 | Druid | 1.2.28 |
| 分页 | PageHelper | 2.1.0 |
| 缓存 | Redis | - |
| 前端框架 | Vue | 2.6.12 |
| UI 组件库 | Element UI | 2.15.14 |
| 状态管理 | Vuex | 3.6.0 |
| 路由管理 | Vue Router | 3.4.9 |
| 图表 | ECharts | 5.4.0 |
| 运行环境 | JDK | 17+ |
| 数据库 | MySQL | 8.x |

## 系统模块

~~~
com.ruoyi
├── ruoyi-ui              // 前端框架 [80]
├── ruoyi-gateway         // 网关模块 [8080]
├── ruoyi-auth            // 认证中心 [9200]
├── ruoyi-api             // 接口模块
│       └── ruoyi-api-system                          // 系统接口
├── ruoyi-common          // 通用模块
│       └── ruoyi-common-core                         // 核心模块
│       └── ruoyi-common-datascope                    // 权限范围
│       └── ruoyi-common-datasource                   // 多数据源
│       └── ruoyi-common-log                          // 日志记录
│       └── ruoyi-common-redis                        // 缓存服务
│       └── ruoyi-common-seata                        // 分布式事务
│       └── ruoyi-common-security                     // 安全模块
│       └── ruoyi-common-sensitive                    // 数据脱敏
│       └── ruoyi-common-swagger                      // 系统接口
├── ruoyi-modules         // 业务模块
│       └── ruoyi-system                              // 系统模块 [9201]
│       └── ruoyi-gen                                 // 代码生成 [9202]
│       └── ruoyi-job                                 // 定时任务 [9203]
│       └── ruoyi-file                                // 文件服务 [9300]
│       └── ruoyi-bocompute                           // 算力资源管理 [9400]
├── ruoyi-visual          // 图形化管理模块
│       └── ruoyi-visual-monitor                      // 监控中心 [9100]
├── pom.xml               // 公共依赖
~~~

## 内置功能

### 系统基础功能（复用若依原生）

1. 用户管理：系统操作用户的配置与维护。
2. 部门管理：树形组织机构配置，支持数据权限隔离。
3. 岗位管理：配置用户所属职务。
4. 菜单管理：配置系统菜单、操作权限、按钮权限标识。
5. 角色管理：角色菜单权限分配、数据范围权限划分。
6. 字典管理：维护系统常用固定数据。
7. 参数管理：系统动态配置参数。
8. 通知公告：系统公告信息发布维护。
9. 操作日志：系统操作日志记录与查询。
10. 登录日志：登录日志记录与异常查询。
11. 定时任务：任务调度与执行日志。
12. 代码生成：前后端代码一键生成。
13. 系统接口：Swagger 接口文档自动生成。
14. 服务监控：系统 CPU、内存、磁盘等监控。

### 算力资源管理（二次开发新增业务）

| 功能模块 | 说明 |
| :--- | :--- |
| 算力资源列表 | 算力资源的增删改查、上下架，维护资源类型、规格、总/可用/已用数量 |
| 我的申请 | 用户提交资源申请（选择资源、数量、用途、使用时间段），查看单据状态 |
| 申请审批 | 管理员查看待审批申请，通过/驳回（填审批意见），通过后自动占用资源 |
| 资源分配记录 | 展示资源分配详情（分配给谁、数量、时间），支持资源释放 |
| 统计看板 | 资源总数、已用、待审批、本月申请等统计指标与资源占用率可视化 |

## 核心业务设计

### 1. 审批状态流转

```
提交申请 ──> 待审批 ──> 已通过 ──> 已分配
                │
                ├──> 已驳回
                └──> 已取消
```

### 2. 审批通过三步闭环（同一事务内）

```
校验申请单状态 → 占用资源(扣减可用数量) → 更新申请单 → 生成分配记录
```

### 3. 防超卖设计

资源占用不采用「先查询再判断再更新」，而是将校验条件写入 SQL 的 `where` 子句，实现并发安全：

```sql
update biz_resource
set used_count = used_count + #{count},
    available_count = available_count - #{count}
where resource_id = #{resourceId}
  and available_count >= #{count}
```

### 4. 资源释放回写

分配记录释放时，自动回写资源表：可用数量 `+N`、已用数量 `-N`。

## 数据库表设计

| 表名 | 说明 |
| :--- | :--- |
| `biz_resource` | 算力资源表（GPU 卡 / 国产加速卡 / 计算节点 / CPU 资源） |
| `biz_resource_apply` | 资源申请表（含状态机字段、审批信息） |
| `biz_resource_alloc` | 资源分配记录表（记录分配给谁、数量、时间、释放） |

业务字典（复用 `sys_dict_type` / `sys_dict_data`）：

| 字典类型 | 说明 |
| :--- | :--- |
| `biz_resource_type` | 资源类型（GPU 卡 / 国产加速卡 / 计算节点 / CPU 资源） |
| `biz_resource_status` | 资源状态（可用 / 停用 / 维护中） |
| `biz_apply_status` | 申请状态（待审批 / 已通过 / 已驳回 / 已取消） |
| `biz_alloc_status` | 分配状态（使用中 / 已释放） |

## 快速启动

### 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.x
- Redis 5.x+
- Nacos 3.x
- Node.js（前端）

### 初始化步骤

1. 创建数据库并导入基础脚本 `sql/ry_20260417.sql`。
2. 导入业务建表脚本 `sql/bocompute_20260922.sql`（业务表 + 字典 + 菜单）。
3. 导入 Nacos 配置脚本 `sql/ry_config_20260311.sql`。
4. 导入业务 Nacos 配置脚本 `sql/bocompute_config_20260922.sql`（新模块配置 + 网关路由）。

### 后端启动

按顺序启动以下服务：

```
nacos → redis → mysql → ruoyi-gateway → ruoyi-auth → ruoyi-system → ruoyi-bocompute
```

### 前端启动

```bash
cd ruoyi-ui
npm install
npm run dev
```

访问 `http://localhost:80`，默认账号 `admin` / `admin123`。

## 接口文档

各服务启动后，可通过网关访问 Swagger 接口文档：`http://localhost:8080/swagger-ui/index.html`。

## 版权声明

Copyright © 2026 BoCloud. All Rights Reserved.
