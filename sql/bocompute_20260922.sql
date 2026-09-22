SET NAMES utf8mb4;

-- ==============================================================
-- 博云算力资源管理后台系统 - 业务库脚本
-- 项目名称：博云算力资源管理后台系统（BoCloud Compute Console）
-- 说明：
--   1. 本脚本基于若依（RuoYi-Cloud 3.6.8）二次开发的业务表
--   2. 业务库与若依系统库同为 ry-cloud，业务表统一使用 biz_ 前缀
--   3. 复用若依 sys_user / sys_dept / sys_dict_* / sys_menu 等系统表
--   4. 执行顺序：先执行 ry_20260417.sql，再执行本脚本
-- 生成时间：2026-09-22
-- ==============================================================


-- ----------------------------
-- 1、算力资源表
-- ----------------------------
drop table if exists biz_resource;
create table biz_resource (
  resource_id       bigint(20)      not null auto_increment    comment '资源ID',
  resource_name     varchar(64)     not null                   comment '资源名称',
  resource_code     varchar(64)     not null                   comment '资源编码（唯一）',
  resource_type     varchar(32)     not null                   comment '资源类型（gpu GPU卡 / npu 国产加速卡 / node 计算节点 / cpu CPU资源）',
  resource_spec     varchar(64)     default ''                 comment '资源规格（如 A100-80G、昇腾910B）',
  cluster_name      varchar(64)     default ''                 comment '所属集群名称',
  total_count       int(11)         default 0                  comment '资源总数量',
  used_count        int(11)         default 0                  comment '已分配数量',
  available_count   int(11)         default 0                  comment '可用数量',
  unit              varchar(16)     default '卡'               comment '计量单位（卡/台/核）',
  status            char(1)         default '0'                comment '资源状态（0可用 1停用 2维护中）',
  del_flag          char(1)         default '0'                comment '删除标志（0代表存在 2代表删除）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (resource_id),
  unique key uk_resource_code (resource_code),
  key idx_resource_type (resource_type)
) engine=innodb auto_increment=100 comment = '算力资源表';

-- ----------------------------
-- 初始化-算力资源表数据
-- ----------------------------
insert into biz_resource values(1, 'GPU训练资源池-A100', 'RES-GPU-A100', 'gpu', 'A100-80G', '南京主集群', 32, 0, 32, '卡', '0', '0', 'admin', sysdate(), '', null, '用于大模型训练的高性能GPU资源');
insert into biz_resource values(2, 'GPU推理资源池-V100', 'RES-GPU-V100', 'gpu', 'V100-32G', '南京主集群', 16, 0, 16, '卡', '0', '0', 'admin', sysdate(), '', null, '用于模型推理的GPU资源');
insert into biz_resource values(3, '昇腾加速卡资源池',   'RES-NPU-910B', 'npu', '昇腾910B',  '苏州智算集群', 8,  0, 8,  '卡', '0', '0', 'admin', sysdate(), '', null, '国产化算力资源，满足信创要求');
insert into biz_resource values(4, '通用计算节点池',     'RES-NODE-X86', 'node', '32C-128G',  '南京主集群', 20, 0, 20, '台', '0', '0', 'admin', sysdate(), '', null, '通用x86计算节点');
insert into biz_resource values(5, 'CPU算力资源池',     'RES-CPU-HIGH', 'cpu', '64C-256G',  '苏州智算集群', 200, 0, 200, '核', '0', '0', 'admin', sysdate(), '', null, '高性能CPU算力资源');


-- ----------------------------
-- 2、资源申请表
-- ----------------------------
drop table if exists biz_resource_apply;
create table biz_resource_apply (
  apply_id          bigint(20)      not null auto_increment    comment '申请单ID',
  apply_no          varchar(64)     not null                   comment '申请单编号（业务唯一号）',
  resource_id       bigint(20)      not null                   comment '申请的资源ID',
  resource_name     varchar(64)     default ''                 comment '资源名称（冗余字段，便于列表展示）',
  resource_type     varchar(32)     default ''                 comment '资源类型（冗余字段）',
  apply_count       int(11)         not null                   comment '申请数量',
  apply_user_id     bigint(20)      not null                   comment '申请人ID',
  apply_user_name   varchar(64)     default ''                 comment '申请人账号',
  apply_dept_id     bigint(20)      default null               comment '申请人部门ID',
  apply_dept_name   varchar(64)     default ''                 comment '申请人部门名称',
  purpose           varchar(500)    default ''                 comment '申请用途',
  begin_time        datetime                                   comment '使用开始时间',
  end_time          datetime                                   comment '使用结束时间',
  status            char(1)         default '0'                comment '申请状态（0待审批 1已通过 2已驳回 3已取消）',
  audit_by          varchar(64)     default ''                 comment '审批人账号',
  audit_time        datetime                                   comment '审批时间',
  audit_opinion     varchar(500)    default ''                 comment '审批意见',
  del_flag          char(1)         default '0'                comment '删除标志（0代表存在 2代表删除）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (apply_id),
  unique key uk_apply_no (apply_no),
  key idx_apply_user (apply_user_id),
  key idx_apply_status (status)
) engine=innodb auto_increment=100 comment = '资源申请表';


-- ----------------------------
-- 3、资源分配记录表
-- ----------------------------
drop table if exists biz_resource_alloc;
create table biz_resource_alloc (
  alloc_id          bigint(20)      not null auto_increment    comment '分配记录ID',
  apply_id          bigint(20)      not null                   comment '关联申请单ID',
  apply_no          varchar(64)     default ''                 comment '申请单编号（冗余字段）',
  resource_id       bigint(20)      not null                   comment '资源ID',
  resource_name     varchar(64)     default ''                 comment '资源名称（冗余字段）',
  alloc_count       int(11)         not null                   comment '本次分配数量',
  user_id           bigint(20)      not null                   comment '资源使用人ID',
  user_name         varchar(64)     default ''                 comment '资源使用人账号',
  dept_id           bigint(20)      default null               comment '使用人部门ID',
  dept_name         varchar(64)     default ''                 comment '使用人部门名称',
  begin_time        datetime                                   comment '使用开始时间',
  end_time          datetime                                   comment '使用结束时间',
  status            char(1)         default '0'                comment '使用状态（0使用中 1已释放）',
  release_time      datetime                                   comment '释放时间',
  del_flag          char(1)         default '0'                comment '删除标志（0代表存在 2代表删除）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (alloc_id),
  key idx_alloc_apply (apply_id),
  key idx_alloc_resource (resource_id),
  key idx_alloc_user (user_id)
) engine=innodb auto_increment=100 comment = '资源分配记录表';


-- ==============================================================
-- 4、字典数据（复用若依 sys_dict_type / sys_dict_data）
-- ==============================================================
-- 字典类型
insert into sys_dict_type values(100, '算力资源类型', 'biz_resource_type',   '0', 'admin', sysdate(), '', null, '算力资源类型列表');
insert into sys_dict_type values(101, '资源状态',     'biz_resource_status', '0', 'admin', sysdate(), '', null, '算力资源状态列表');
insert into sys_dict_type values(102, '申请状态',     'biz_apply_status',    '0', 'admin', sysdate(), '', null, '资源申请状态列表');
insert into sys_dict_type values(103, '分配状态',     'biz_alloc_status',    '0', 'admin', sysdate(), '', null, '资源分配状态列表');

-- 字典数据：算力资源类型
insert into sys_dict_data values(100, 1, 'GPU卡',      'gpu',  'biz_resource_type',   '', 'danger',  'Y', '0', 'admin', sysdate(), '', null, 'GPU加速卡');
insert into sys_dict_data values(101, 2, '国产加速卡', 'npu',  'biz_resource_type',   '', 'warning', 'N', '0', 'admin', sysdate(), '', null, '昇腾等国产NPU加速卡');
insert into sys_dict_data values(102, 3, '计算节点',   'node', 'biz_resource_type',   '', 'primary', 'N', '0', 'admin', sysdate(), '', null, '通用计算节点');
insert into sys_dict_data values(103, 4, 'CPU资源',    'cpu',  'biz_resource_type',   '', 'success', 'N', '0', 'admin', sysdate(), '', null, 'CPU算力资源');

-- 字典数据：资源状态
insert into sys_dict_data values(104, 1, '可用',   '0', 'biz_resource_status', '', 'success', 'Y', '0', 'admin', sysdate(), '', null, '资源可用');
insert into sys_dict_data values(105, 2, '停用',   '1', 'biz_resource_status', '', 'danger',  'N', '0', 'admin', sysdate(), '', null, '资源已停用');
insert into sys_dict_data values(106, 3, '维护中', '2', 'biz_resource_status', '', 'warning', 'N', '0', 'admin', sysdate(), '', null, '资源维护中');

-- 字典数据：申请状态
insert into sys_dict_data values(107, 1, '待审批', '0', 'biz_apply_status', '', 'warning', 'Y', '0', 'admin', sysdate(), '', null, '等待审批');
insert into sys_dict_data values(108, 2, '已通过', '1', 'biz_apply_status', '', 'success', 'N', '0', 'admin', sysdate(), '', null, '审批通过');
insert into sys_dict_data values(109, 3, '已驳回', '2', 'biz_apply_status', '', 'danger',  'N', '0', 'admin', sysdate(), '', null, '审批驳回');
insert into sys_dict_data values(110, 4, '已取消', '3', 'biz_apply_status', '', 'info',    'N', '0', 'admin', sysdate(), '', null, '申请人主动取消');

-- 字典数据：分配状态
insert into sys_dict_data values(111, 1, '使用中', '0', 'biz_alloc_status', '', 'primary', 'Y', '0', 'admin', sysdate(), '', null, '资源使用中');
insert into sys_dict_data values(112, 2, '已释放', '1', 'biz_alloc_status', '', 'info',    'N', '0', 'admin', sysdate(), '', null, '资源已释放');


-- ==============================================================
-- 5、菜单权限数据（对接若依动态路由）
-- 说明：
--   一级目录：算力资源管理（menu_id = 2000）
--   二级菜单：算力资源列表 2001 / 我的申请 2002 / 申请审批 2003 / 资源分配记录 2004 / 统计看板 2005
--   按钮权限：2050 ~ 2099
-- ==============================================================

-- 一级目录
insert into sys_menu values('2000', '算力资源管理', '0', '1', 'bocompute', null, '', '', 1, 0, 'M', '0', '0', '', 'server', 'admin', sysdate(), '', null, '算力资源管理目录');

-- 二级菜单
insert into sys_menu values('2001', '算力资源列表', '2000', '1', 'resource',    'bocompute/resource/index',    '', '', 1, 0, 'C', '0', '0', 'bocompute:resource:list',    'server',   'admin', sysdate(), '', null, '算力资源列表菜单');
insert into sys_menu values('2002', '我的申请',     '2000', '2', 'myapply',     'bocompute/apply/my',          '', '', 1, 0, 'C', '0', '0', 'bocompute:apply:list',       'form',     'admin', sysdate(), '', null, '我的申请菜单');
insert into sys_menu values('2003', '申请审批',     '2000', '3', 'audit',       'bocompute/apply/audit',       '', '', 1, 0, 'C', '0', '0', 'bocompute:apply:audit',      'checkbox', 'admin', sysdate(), '', null, '申请审批菜单');
insert into sys_menu values('2004', '资源分配记录', '2000', '4', 'alloc',       'bocompute/alloc/index',       '', '', 1, 0, 'C', '0', '0', 'bocompute:alloc:list',       'tree-table','admin', sysdate(), '', null, '资源分配记录菜单');
insert into sys_menu values('2005', '统计看板',     '2000', '5', 'dashboard',   'bocompute/dashboard/index',   '', '', 1, 0, 'C', '0', '0', 'bocompute:dashboard:list',   'chart',    'admin', sysdate(), '', null, '使用统计看板菜单');

-- 按钮权限：算力资源列表
insert into sys_menu values('2050', '资源查询', '2001', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'bocompute:resource:query',  '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2051', '资源新增', '2001', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'bocompute:resource:add',    '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2052', '资源修改', '2001', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'bocompute:resource:edit',   '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2053', '资源删除', '2001', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'bocompute:resource:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2054', '资源导出', '2001', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'bocompute:resource:export', '#', 'admin', sysdate(), '', null, '');

-- 按钮权限：我的申请
insert into sys_menu values('2055', '申请查询', '2002', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'bocompute:apply:query',  '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2056', '提交申请', '2002', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'bocompute:apply:add',    '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2057', '取消申请', '2002', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'bocompute:apply:cancel', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2058', '申请导出', '2002', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'bocompute:apply:export', '#', 'admin', sysdate(), '', null, '');

-- 按钮权限：申请审批
insert into sys_menu values('2059', '审批查询', '2003', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'bocompute:audit:query',  '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2060', '审批通过', '2003', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'bocompute:audit:pass',   '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2061', '审批驳回', '2003', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'bocompute:audit:reject', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2065', '审批列表', '2003', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'bocompute:audit:list',   '#', 'admin', sysdate(), '', null, '审批管理员查看全部申请单');

-- 按钮权限：资源分配记录
insert into sys_menu values('2062', '分配查询', '2004', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'bocompute:alloc:query',   '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2063', '释放资源', '2004', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'bocompute:alloc:release', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2064', '分配导出', '2004', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'bocompute:alloc:export',  '#', 'admin', sysdate(), '', null, '');


-- ==============================================================
-- 6、角色授权说明（可选，手动在"角色管理"中勾选即可）
-- ------------------------------------------------------------
-- 普通用户角色：勾选"我的申请"（2002）及其按钮
-- 审批管理员角色：勾选"算力资源管理"全部菜单，重点包含"申请审批"（2003）
-- 超级管理员 admin：默认拥有全部菜单权限，无需额外授权
-- ==============================================================

-- 若希望"普通员工"角色（role_id = 2，若依默认）仅拥有"我的申请"权限，可执行以下语句：
-- insert into sys_role_menu values(2, 2000);
-- insert into sys_role_menu values(2, 2002);
-- insert into sys_role_menu values(2, 2055);
-- insert into sys_role_menu values(2, 2056);
-- insert into sys_role_menu values(2, 2057);
-- insert into sys_role_menu values(2, 2058);
