```
# 算力资源管理控制台 · 从零研发全过程文档

> 文档目标：按真实研发顺序说明系统如何从 0 搭建到业务闭环完成；标明各目录与关键文件职责。  
> 技术栈：Spring Boot / Spring Cloud Alibaba / Spring Cloud Gateway / Nacos / Redis / MySQL / MyBatis / Vue2 + Element UI / ECharts  
> 核心业务：算力资源纳管、申请审批、设备级调度锁卡、防超卖、到期释放、运营看板  

---

## 〇、代码获取

```bash
git clone https://github.com/ohana-ops/bocloud-compute-console.git

# 建议使用包含完整业务代码的分支
git clone -b dependabot/npm_and_yarn/ruoyi-ui/axios-0.32.0 \
  https://github.com/ohana-ops/bocloud-compute-console.git
```

| 项           | 地址                                                     |
| ------------ | -------------------------------------------------------- |
| 仓库         | https://github.com/ohana-ops/bocloud-compute-console     |
| Clone        | https://github.com/ohana-ops/bocloud-compute-console.git |
| 业务完整分支 | dependabot/npm_and_yarn/ruoyi-ui/axios-0.32.0            |

以仓库中存在 ruoyi-modules/ruoyi-bocompute 与 sql/bocompute_*.sql 为准。

------

## 一、要解决什么问题

企业内部算力（GPU / 国产加速卡 / 计算节点 / CPU）需要统一运营：

1. 资源池可维护（规格、集群、总量与可用量）
2. 用户提交使用申请（数量、用途、使用时段）
3. 管理员审批；通过后必须落到**具体设备（卡号/节点）**，不能只改一个数字
4. 并发审批时不能超卖
5. 使用结束可手动或到期自动归还
6. 运营侧可看统计与设备状态分布

因此系统拆成：**接入层（网关）→ 认证 → 系统管理（用户权限菜单）→ 算力业务服务 → 前端控制台**。

------

## 二、总体架构

text

```
浏览器控制台 (前端 :80)
        ↓
API 网关 (:8080)  路由 + Token 校验
        ↓
┌──────────┬──────────────┬────────────────────┐
│ 认证服务  │ 系统管理服务  │ 算力业务服务         │
│ :9200    │ :9201        │ :9400               │
│ 登录发证  │ 用户角色菜单  │ 资源/申请/设备/调度  │
└──────────┴──────────────┴────────────────────┘
        ↑
  公共组件（安全、Redis、日志、数据源等）
        ↓
   MySQL  ·  Redis  ·  Nacos（注册与配置）
```

### 仓库顶层目录职责

| 目录           | 职责                                               |
| -------------- | -------------------------------------------------- |
| pom.xml        | 多模块父工程，统一依赖与版本                       |
| sql/           | 库表、字典、菜单、配置中心初始化脚本               |
| docker/        | 本地依赖组件编排（MySQL/Redis/Nacos 等）           |
| bin/           | 启停脚本                                           |
| docs/          | 文档                                               |
| ruoyi-ui/      | 前端工程                                           |
| ruoyi-gateway/ | 网关服务                                           |
| ruoyi-auth/    | 认证服务                                           |
| ruoyi-api/     | 跨服务 API 定义（仅接口与 DTO，无业务实现）        |
| ruoyi-common/  | 公共能力库（可被各服务依赖）                       |
| ruoyi-modules/ | 业务微服务（系统管理、文件、定时任务、**算力**等） |
| ruoyi-visual/  | 监控类组件                                         |

------

## 三、研发阶段总顺序（按时间线）

text

```
阶段 1  技术选型与工程骨架（多模块 + 注册配置中心）
阶段 2  网关、认证、系统管理、前端壳子跑通
阶段 3  公共能力落地（安全、Redis、日志等）
阶段 4  新建算力业务微服务并接入网关
阶段 5  业务库表 v1（资源 / 申请 / 分配）+ 权限菜单
阶段 6  申请审批数量闭环（先跑通流程）
阶段 7  前端业务页
阶段 8  设备池 + 调度器 + 同事务锁卡防超卖
阶段 9  设备台账、到期释放、看板
阶段 10 联调与验收
```

------

## 四、阶段 1：工程骨架与基础设施

### 1.1 环境

- JDK 17+
- Maven 3.8+
- MySQL 8
- Redis
- Nacos 3.x
- Node.js（前端）

### 1.2 父工程与模块拆分原则

- **一个可独立部署的进程 = 一个 modules 下的子模块**（或 gateway/auth）
- **多处复用的代码进 common**，避免复制
- **服务间只依赖 api 模块的接口**，不依赖对方实现 jar

### 1.3 基础库脚本（先系统后业务）

| 顺序 | 脚本                                | 用途                                       |
| ---- | ----------------------------------- | ------------------------------------------ |
| 1    | sql/ry_20260417.sql                 | 用户、角色、菜单、部门、字典、日志等基础表 |
| 2    | sql/ry_config_20260311.sql          | 配置中心初始数据                           |
| 3    | sql/bocompute_20260922.sql          | 算力业务表 v1 + 业务字典 + 菜单            |
| 4    | sql/bocompute_schedule_20260923.sql | 设备池与调度增量                           |
| 5    | sql/bocompute_config_20260922.sql   | 算力服务配置与网关路由                     |

### 1.4 进程启动顺序

text

```
Nacos → Redis → MySQL
  → 网关
  → 认证
  → 系统管理
  → 算力业务（业务开发完成后）
  → 前端
```

Bash

```
cd ruoyi-ui && npm install && npm run dev
# http://localhost:80  默认 admin / admin123
```

------

## 五、阶段 2～3：接入层、认证、系统管理、公共库

### 5.1 网关 ruoyi-gateway

| 职责     | 说明                                             |
| -------- | ------------------------------------------------ |
| 统一入口 | 对外端口 8080，前端只认网关                      |
| 路由     | /auth/**、/system/**、/bocompute/** 等到对应服务 |
| 鉴权过滤 | 校验 Token；登录、验证码等走白名单               |

### 5.2 认证 ruoyi-auth

| 职责        | 说明                                 |
| ----------- | ------------------------------------ |
| 登录 / 登出 | 校验账号密码，签发 Token，写入 Redis |
| 验证码      | 短 TTL 存在 Redis                    |
| 边界        | 不承载算力业务表逻辑                 |

### 5.3 系统管理 ruoyi-modules/ruoyi-system

分层（后续算力服务同样采用）：

text

```
controller → service/impl → mapper + XML → MySQL
domain 实体与表字段对应
```

覆盖：用户、角色、菜单、部门、字典、参数、通知、操作/登录日志。
 字典、参数等可在启动时加载到 Redis，降低热点读库。

### 5.4 跨服务 API ruoyi-api/ruoyi-api-system

定义「查用户、部门」等 Feign 接口与 DTO。
 算力服务需要当前用户信息时依赖本模块，而不是直接依赖系统管理实现。

### 5.5 公共库 ruoyi-common（按需依赖）

| 子模块            | 关键内容                          | 用途                           |
| ----------------- | --------------------------------- | ------------------------------ |
| common-core       | 统一返回体、常量、异常、工具      | 全局基础                       |
| common-redis      | RedisConfig、RedisService、序列化 | Token、验证码、字典缓存等      |
| common-security   | Token 解析、@RequiresPermissions  | 接口鉴权、登录用户上下文       |
| common-log        | AOP 操作日志                      | 审计                           |
| common-datascope  | 数据权限                          | 按部门过滤列表                 |
| common-datasource | 数据源                            | 持久化                         |
| common-swagger    | 接口文档                          | 联调                           |
| common-sensitive  | 脱敏                              | 敏感字段                       |
| common-seata      | 分布式事务                        | 可选；算力主路径用本地事务即可 |

**说明：** 登录 Token、验证码、字典等使用 Redis；算力库存与锁卡**以 MySQL 为准**，业务路径不做库存二级缓存。

### 5.6 前端壳 ruoyi-ui

| 路径                 | 用途                          |
| -------------------- | ----------------------------- |
| src/api/             | 后端 API 封装                 |
| src/views/           | 页面                          |
| src/store/           | 全局状态（token、用户、路由） |
| src/router/          | 路由；业务菜单由后端动态下发  |
| src/layout/          | 布局                          |
| src/utils/request.js | 携带 Token、统一错误处理      |
| src/directive/       | 按钮级权限                    |

### 5.7 请求链路

text

```
浏览器 → 网关（Token）→ 业务服务（权限注解）→ Service → Mapper → MySQL
                              ↓
                         操作日志（可选）
```

------

## 六、阶段 4：算力业务微服务从零接入

### 6.1 步骤

1. 在 ruoyi-modules 下创建 ruoyi-bocompute
2. 编写模块 pom.xml，并在 modules 父 POM 中注册
3. 启动类 + bootstrap.yml / application.yml
4. 配置中心增加服务名、数据源、以及网关到本服务的路由
5. 独立进程启动，端口 **9400**，在 Nacos 中可见

### 6.2 模块依赖要点

- 注册/配置中心客户端
- 数据源、日志、安全、Swagger
- 系统 API 模块（用户上下文）
- Kubernetes 客户端（调度对接集群时使用，可切换）

### 6.3 关键文件

| 文件                           | 用途               |
| ------------------------------ | ------------------ |
| RuoYiBoComputeApplication.java | 启动类             |
| bootstrap.yml                  | 服务名、Nacos 地址 |
| application.yml                | 本地默认配置       |
| logback.xml                    | 日志               |

### 6.4 目标包结构

text

```
com.ruoyi.bocompute
├── RuoYiBoComputeApplication.java
├── controller/
├── domain/
├── mapper/
├── service/、service/impl/
└── schedule/              # 调度与定时释放
resources/mapper/bocompute/*.xml
```

------

## 七、阶段 5：业务模型 v1（表、字典、菜单）

脚本：sql/bocompute_20260922.sql

### 7.1 表（按依赖建立）

| 表                 | 用途                                             |
| ------------------ | ------------------------------------------------ |
| biz_resource       | 资源池：类型、规格、集群、总量/已用/可用         |
| biz_resource_apply | 申请单：申请人、资源、数量、时段、状态、审批信息 |
| biz_resource_alloc | 分配单：对象、数量、起止时间、释放状态           |

### 7.2 字典

| 类型                | 含义                        |
| ------------------- | --------------------------- |
| biz_resource_type   | gpu / npu / node / cpu      |
| biz_resource_status | 可用 / 停用 / 维护          |
| biz_apply_status    | 待审批 / 通过 / 驳回 / 取消 |
| biz_alloc_status    | 使用中 / 已释放             |

### 7.3 菜单与权限标识

- 资源管理 → bocompute:resource:*
- 我的申请 → bocompute:apply:*
- 申请审批 → bocompute:audit:*
- 分配记录 → bocompute:alloc:*

在角色管理中勾选即可生效。

------

## 八、阶段 6：后端流程闭环（先数量模型）

目标：申请 → 审批通过 → 扣减可用数量 → 生成分配记录 → 释放加回。
 此阶段尚未绑定物理卡号。

### 8.1 实现顺序

text

```
domain → mapper 接口 + XML → service（事务）→ controller（权限）
```

### 8.2 Domain

| 类               | 表                 |
| ---------------- | ------------------ |
| BizResource      | biz_resource       |
| BizResourceApply | biz_resource_apply |
| BizResourceAlloc | biz_resource_alloc |

### 8.3 Mapper

| 接口与 XML             | 职责                    |
| ---------------------- | ----------------------- |
| BizResourceMapper      | 资源 CRUD；库存条件更新 |
| BizResourceApplyMapper | 申请 CRUD、状态查询     |
| BizResourceAllocMapper | 分配 CRUD、释放更新     |

数量防超卖：

SQL

```
UPDATE biz_resource
SET used_count = used_count + #{count},
    available_count = available_count - #{count}
WHERE resource_id = #{resourceId}
  AND available_count >= #{count}
```

### 8.4 Service

| 类                          | 职责                   |
| --------------------------- | ---------------------- |
| BizResourceServiceImpl      | 资源维护、上下架       |
| BizResourceApplyServiceImpl | 提交、取消、通过、驳回 |
| BizResourceAllocServiceImpl | 分配查询、手动释放     |

### 8.5 Controller

| 类                         | 职责           |
| -------------------------- | -------------- |
| BizResourceController      | 资源 API       |
| BizResourceApplyController | 申请与审批 API |
| BizResourceAllocController | 分配与释放 API |

------

## 九、阶段 7：前端业务页

### 9.1 API

| 文件                           | 职责      |
| ------------------------------ | --------- |
| src/api/bocompute/resource.js  | 资源      |
| src/api/bocompute/apply.js     | 申请/审批 |
| src/api/bocompute/alloc.js     | 分配/释放 |
| src/api/bocompute/device.js    | 设备      |
| src/api/bocompute/dashboard.js | 看板      |

### 9.2 页面

| 文件                                | 职责       |
| ----------------------------------- | ---------- |
| views/bocompute/resource/index.vue  | 资源池管理 |
| views/bocompute/apply/my.vue        | 我的申请   |
| views/bocompute/apply/audit.vue     | 审批台     |
| views/bocompute/alloc/index.vue     | 分配记录   |
| views/bocompute/device/index.vue    | 设备台账   |
| views/bocompute/dashboard/index.vue | 统计看板   |

------

## 十、阶段 8：设备池调度与防超卖（核心）

脚本：sql/bocompute_schedule_20260923.sql（增量）

### 10.1 新增模型

| 表               | 用途                                     |
| ---------------- | ---------------------------------------- |
| biz_device       | 设备（卡/节点）；**status 为库存真相源** |
| biz_alloc_device | 分配单与设备的绑定关系                   |

分配表扩展：卡号、节点、调度说明等回写字段；补充设备字典、台账菜单、种子数据。

### 10.2 设计约束

1. 以 biz_device.status 为准；资源表数量字段仅冗余，通过汇总回写。
2. gpu / npu / node 必须调度到设备；cpu 可继续走数量模型。
3. 审批通过与锁设备在**同一本地事务**。
4. 顺序固定：**先插入分配单得到 allocId → 再锁设备 → 最后将申请单置为已通过**。
5. 锁定使用条件更新，用影响行数校验：

SQL

```
UPDATE biz_device
SET status = '1', alloc_id = #{allocId}, ...
WHERE device_id IN (...)
  AND status = '0'
  AND del_flag = '0'
```

### 10.3 调度组件 schedule 包

| 文件                             | 职责                     |
| -------------------------------- | ------------------------ |
| ComputeScheduler                 | 接口：allocate / release |
| ScheduleRequest / ScheduleResult | 调度入参/出参            |
| DevicePoolScheduler              | 默认：设备池选卡 + 锁定  |
| DeviceCodeGenerator              | 设备编号                 |
| K8sComputeScheduler              | 集群侧实现（配置切换）   |
| K8sTemplate                      | 集群客户端封装           |
| AllocExpireTask                  | 到期自动释放             |

配置示例：

YAML

```
bocompute:
  scheduler:
    type: device-pool   # 或 k8s
    auto-release-enabled: true
```

### 10.4 选卡策略（设备池）

1. 取空闲设备，按节点名、槽位排序
2. 优先单节点装下整单（节点空闲更少者优先，降低碎片）
3. 不够则跨节点拼凑
4. 仍不足则失败并回滚整个审批事务

### 10.5 审批通过步骤（passApply）

text

```
校验申请与资源
→ 插入分配单得到 allocId
→ scheduler.allocate 选卡并锁定
→ 写入绑定表
→ 回写卡号/说明，汇总同步资源池数量
→ 申请单改为已通过
```

失败则全部回滚，申请保持待审批。

### 10.6 新增实体与 Mapper

| 文件                        | 职责                    |
| --------------------------- | ----------------------- |
| BizDevice + Mapper/XML      | 设备维护与条件锁定/释放 |
| BizAllocDevice + Mapper/XML | 分配-设备绑定           |

------

## 十一、阶段 9：台账、自动释放、看板

| 能力     | 后端                                       | 前端                |
| -------- | ------------------------------------------ | ------------------- |
| 设备台账 | BizDeviceController / BizDeviceServiceImpl | device/index.vue    |
| 到期释放 | AllocExpireTask                            | —                   |
| 运营看板 | BizDashboardController                     | dashboard/index.vue |

设备故障/维护/上线后同步回写资源池汇总数量。

------

## 十二、阶段 10：验收清单

1. 脚本按顺序执行完毕
2. 网关、认证、系统管理、算力服务均注册成功
3. 菜单与按钮权限正确
4. 主路径：申请 → 通过 → 设备已分配且有卡号 → 释放后空闲
5. 并发双审批同一资源，仅一单成功
6. 到期自动释放生效
7. 接口文档：http://localhost:8080/swagger-ui/index.html

------

## 十三、算力服务文件树

text

```
ruoyi-modules/ruoyi-bocompute/
├── pom.xml
└── src/main/
    ├── java/com/ruoyi/bocompute/
    │   ├── RuoYiBoComputeApplication.java
    │   ├── controller/
    │   │   ├── BizResourceController.java
    │   │   ├── BizResourceApplyController.java
    │   │   ├── BizResourceAllocController.java
    │   │   ├── BizDeviceController.java
    │   │   └── BizDashboardController.java
    │   ├── domain/          # 五个业务实体
    │   ├── mapper/
    │   ├── service/、impl/
    │   └── schedule/        # 调度与定时任务
    └── resources/
        ├── bootstrap.yml
        ├── application.yml
        ├── logback.xml
        └── mapper/bocompute/*.xml
```

前端：ruoyi-ui/src/api/bocompute/、ruoyi-ui/src/views/bocompute/
 脚本：sql/bocompute_20260922.sql、bocompute_schedule_20260923.sql、bocompute_config_20260922.sql

------

## 十四、端口

| 组件     | 端口 |
| -------- | ---- |
| 前端     | 80   |
| 网关     | 8080 |
| 认证     | 9200 |
| 系统管理 | 9201 |
| 算力业务 | 9400 |

默认账号：admin / admin123

------

## 十五、核心设计回顾（实现时优先保证）

1. **设备状态是库存真相源**，资源池数字是汇总结果。
2. **锁卡与审批同事务**，且先有分配单 ID 再锁设备。
3. **用 WHERE status='0' 更新 + 影响行数** 防并发超卖。
4. **调度可替换**（设备池 / K8s），业务审批代码只依赖接口。
5. **到期任务**负责归还，避免只靠人工释放。