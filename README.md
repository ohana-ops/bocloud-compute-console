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
| 资源分配记录 | 展示资源分配详情（分配给谁、数量、时间、拿到的卡号），支持资源释放 |
| 设备台账 | 模拟算力设备（GPU 卡 / NPU 卡 / 计算节点）的注册、批量初始化、故障 / 维护 / 上线、删除 |
| 统计看板 | 资源总数、已用、待审批、本月申请等统计指标，以及设备池空闲 / 已分配 / 故障 / 维护分布 |

## 核心业务设计

### 1. 审批状态流转

```
提交申请 ──> 待审批 ──> 审批通过
                            ↓
                  调度器选卡并锁定（同一事务）
                            ↓
                  回写卡号到分配记录 → 已分配
                            ↓
                  到期或手动释放 → 还卡
                │
                ├──> 已驳回
                └──> 已取消
```

### 2. 模拟设备池调度层

审批通过后不只是改库存，而是由调度模块从设备池里选出具体设备（卡号 / 节点号）、锁定并回写卡号；没有足够空闲设备时整单回滚，申请单保持待审批。

| 资源类型 | 是否走设备调度 | 一台设备代表 |
| :--- | :--- | :--- |
| `gpu` | 是 | 1 张 GPU 卡 |
| `npu` | 是 | 1 张国产加速卡 |
| `node` | 是 | 1 台计算节点 |
| `cpu` | 否，仍按数量扣减 | 不生成设备 |

调度器抽象为可替换接口，当前跑的是设备池实现，K8s 实现为预留空壳：

```java
public interface ComputeScheduler {
    ScheduleResult allocate(ScheduleRequest request);  // 选设备并锁定，选不够抛异常回滚
    void release(Long allocId);                        // 还设备
}
```

通过配置切换实现：

```yaml
bocompute:
  scheduler:
    type: device-pool        # device-pool | k8s
    auto-release-enabled: true
```

### 3. 选卡算法（同节点优先 binpack）

1. 查出该资源池 `status=0`（空闲）且未删除的设备，按 `node_name`、`slot_index` 升序；
2. 优先找「空闲数 ≥ 申请量」的节点，多个候选取空闲数最少的，减少碎片；
3. 没有单节点能放下，则按节点空闲数从多到少跨节点拼凑，`message` 里写明 `跨节点分配: a,b`；
4. 仍不够则抛 `空闲设备不足`，整单回滚。

### 4. 审批通过六步闭环（同一事务内）

```
校验单据与资源 → 先插分配单拿 allocId → 调度器选卡并锁定
→ 写入分配单-设备绑定 → 回写卡号/节点/调度说明到分配单
→ 按设备汇总回写库存 → 申请单改为已通过
```

顺序上必须「先插分配单再调度锁卡」，禁止先把申请单改为已通过。

### 5. 防超卖设计

设备锁定不采用「先查询再判断再更新」，而是将状态条件写入 SQL 的 `where` 子句，由数据库行锁保证并发安全：

```sql
update biz_device
set status = '1', alloc_id = #{allocId}, apply_no = #{applyNo},
    user_name = #{userName}, bind_time = sysdate()
where device_id in (...)
  and status = '0'
  and del_flag = '0'
```

影响行数必须等于申请数量，否则判定为并发抢占并回滚。CPU 资源沿用原有的数量扣减防超卖：

```sql
update biz_resource
set used_count = used_count + #{count},
    available_count = available_count - #{count}
where resource_id = #{resourceId}
  and available_count >= #{count}
```

### 6. 库存真相源

`biz_device.status` 是真相源，`biz_resource` 的 `total_count / used_count / available_count` 只是冗余缓存，
只允许在调度、释放、设备状态变更的同一事务里通过 `syncResourceCount` 回写：

- `total` = 未删除设备数（含故障与维护）
- `used` = `status=1` 的设备数
- `available` = `status=0` 的设备数

页面禁止手改这三个数字（CPU 资源无设备，仍可编辑总量）。

### 7. 资源释放回写

分配记录释放时，调度器把设备状态改回空闲并清空绑定信息，绑定明细置为已释放，
再按设备汇总回写库存；CPU 资源按数量回写：可用数量 `+N`、已用数量 `-N`。

结束时间早于当前时间且仍在使用的分配单，由 `AllocExpireTask`（每分钟一次）自动释放。

## 数据库表设计

| 表名 | 说明 |
| :--- | :--- |
| `biz_resource` | 算力资源表（GPU 卡 / 国产加速卡 / 计算节点 / CPU 资源） |
| `biz_resource_apply` | 资源申请表（含状态机字段、审批信息） |
| `biz_resource_alloc` | 资源分配记录表（记录分配给谁、数量、时间、释放、绑定卡号与调度说明） |
| `biz_device` | 模拟算力设备表（一张卡 / 一台节点，调度真相源） |
| `biz_alloc_device` | 分配单-设备绑定表（某次分配绑了哪些卡） |

业务字典（复用 `sys_dict_type` / `sys_dict_data`）：

| 字典类型 | 说明 |
| :--- | :--- |
| `biz_resource_type` | 资源类型（GPU 卡 / 国产加速卡 / 计算节点 / CPU 资源） |
| `biz_resource_status` | 资源状态（可用 / 停用 / 维护中） |
| `biz_apply_status` | 申请状态（待审批 / 已通过 / 已驳回 / 已取消） |
| `biz_alloc_status` | 分配状态（使用中 / 已释放） |
| `biz_device_status` | 设备状态（空闲 / 已分配 / 故障 / 维护） |

建表与初始化脚本：

| 脚本 | 说明 |
| :--- | :--- |
| `sql/bocompute_20260922.sql` | 业务基础表（资源 / 申请 / 分配）+ 字典 + 菜单 |
| `sql/bocompute_schedule_20260923.sql` | 调度层增量脚本（设备表、绑定表、分配单扩字段、设备字典、设备台账菜单、种子设备） |

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
