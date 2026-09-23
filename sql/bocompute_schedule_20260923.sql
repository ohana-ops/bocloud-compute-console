SET NAMES utf8mb4;

-- ==============================================================
-- 博云算力资源管理后台系统 - 模拟设备池调度层 增量脚本
-- 说明：
--   1. 本脚本为增量脚本，不 drop 任何已有表，不改动 bocompute_20260922.sql 的历史含义
--   2. 执行顺序：先执行 bocompute_20260922.sql，再执行本脚本
--   3. 新增：biz_device（模拟设备表）、biz_alloc_device（分配单-设备绑定表）
--   4. 改造：biz_resource_alloc 增加 4 个调度回写字段
--   5. 新增：设备状态字典、设备台账菜单与按钮权限、种子设备数据
-- 生成时间：2026-09-23
-- ==============================================================


-- ----------------------------
-- 1、模拟算力设备表
-- ----------------------------
create table if not exists biz_device (
  device_id         bigint(20)    not null auto_increment comment '设备ID',
  device_code       varchar(64)   not null                comment '设备编号，唯一，如 GPU-NJ-N01-C0',
  resource_id       bigint(20)    not null                comment '所属资源池ID',
  resource_type     varchar(32)   not null                comment '冗余：gpu/npu/node',
  resource_spec     varchar(64)   default ''              comment '冗余规格，如 A100-80G',
  cluster_name      varchar(64)   default ''              comment '集群',
  node_name         varchar(64)   not null                comment '宿主机节点名，如 gpu-node-01',
  slot_index        int(11)       default 0               comment '节点内槽位 0~7',
  uuid              varchar(64)   default ''              comment '模拟 UUID',
  pci_bus           varchar(32)   default ''              comment '模拟 PCI，如 0000:17:00.0',
  status            char(1)       default '0'             comment '设备状态（0空闲 1已分配 2故障 3维护）',
  alloc_id          bigint(20)    default null            comment '当前绑定的分配单ID，空闲则为空',
  apply_no          varchar(64)   default ''              comment '当前申请单号，便于排查',
  user_name         varchar(64)   default ''              comment '当前使用人',
  bind_time         datetime      default null            comment '本次绑定时间',
  del_flag          char(1)       default '0'             comment '删除标志（0代表存在 2代表删除）',
  create_by         varchar(64)   default ''              comment '创建者',
  create_time       datetime                              comment '创建时间',
  update_by         varchar(64)   default ''              comment '更新者',
  update_time       datetime                              comment '更新时间',
  remark            varchar(500)  default null            comment '备注',
  primary key (device_id),
  unique key uk_device_code (device_code),
  key idx_device_resource (resource_id, status),
  key idx_device_node (node_name),
  key idx_device_alloc (alloc_id)
) engine=innodb auto_increment=100 comment = '模拟算力设备表';


-- ----------------------------
-- 2、分配单-设备绑定表
-- ----------------------------
create table if not exists biz_alloc_device (
  id            bigint(20)   not null auto_increment comment '主键ID',
  alloc_id      bigint(20)   not null                comment '分配单ID',
  apply_id      bigint(20)   not null                comment '申请单ID',
  device_id     bigint(20)   not null                comment '设备ID',
  device_code   varchar(64)  not null                comment '设备编号（冗余，便于展示）',
  node_name     varchar(64)  default ''              comment '宿主机节点名（冗余）',
  resource_id   bigint(20)   not null                comment '资源池ID',
  bind_time     datetime                             comment '绑定时间',
  release_time  datetime     default null            comment '释放时间',
  status        char(1)      default '0'             comment '绑定状态（0占用中 1已释放）',
  primary key (id),
  key idx_ad_alloc (alloc_id),
  key idx_ad_device (device_id)
) engine=innodb auto_increment=100 comment = '分配单-设备绑定表';


-- ----------------------------
-- 3、改造资源分配记录表（新增调度回写字段，不 drop 表）
-- ----------------------------
alter table biz_resource_alloc add column device_codes   varchar(1000) default '' comment '绑定设备编号，逗号拼接';
alter table biz_resource_alloc add column node_names     varchar(500)  default '' comment '涉及节点，逗号分隔';
alter table biz_resource_alloc add column schedule_type  varchar(32)   default 'device-pool' comment '调度实现标识（device-pool/k8s）';
alter table biz_resource_alloc add column schedule_msg   varchar(500)  default '' comment '调度结果说明';


-- ----------------------------
-- 4、字典数据：设备状态 biz_device_status
-- ----------------------------
insert into sys_dict_type values(104, '设备状态', 'biz_device_status', '0', 'admin', sysdate(), '', null, '模拟算力设备状态列表');

insert into sys_dict_data values(113, 1, '空闲',   '0', 'biz_device_status', '', 'success', 'Y', '0', 'admin', sysdate(), '', null, '设备空闲可分配');
insert into sys_dict_data values(114, 2, '已分配', '1', 'biz_device_status', '', 'primary', 'N', '0', 'admin', sysdate(), '', null, '设备已被分配占用');
insert into sys_dict_data values(115, 3, '故障',   '2', 'biz_device_status', '', 'danger',  'N', '0', 'admin', sysdate(), '', null, '设备故障不可用');
insert into sys_dict_data values(116, 4, '维护',   '3', 'biz_device_status', '', 'warning', 'N', '0', 'admin', sysdate(), '', null, '设备维护中');


-- ==============================================================
-- 5、种子设备数据（按 bocompute_20260922.sql 的 5 条资源生成）
--    资源1 RES-GPU-A100：4 节点 × 8 卡 = 32 张
--    资源2 RES-GPU-V100：2 节点 × 8 卡 = 16 张
--    资源3 RES-NPU-910B：1 节点 × 8 卡 = 8 张
--    资源4 RES-NODE-X86：20 台节点（节点即设备）
--    资源5 RES-CPU-HIGH：CPU 按核数扣减，不生成设备
--    uuid 使用 md5(设备编号) 生成稳定的模拟值
-- ==============================================================

-- ----------------------------
-- 5.1 资源1：GPU训练资源池-A100（resource_id=1，32 卡）
-- ----------------------------
insert into biz_device(device_code, resource_id, resource_type, resource_spec, cluster_name, node_name, slot_index, uuid, pci_bus, status, del_flag, create_by, create_time) values
('GPU-NJ-N01-C0', 1, 'gpu', 'A100-80G', '南京主集群', 'gpu-nj-01', 0, md5('GPU-NJ-N01-C0'), '0000:10:00.0', '0', '0', 'admin', sysdate()),
('GPU-NJ-N01-C1', 1, 'gpu', 'A100-80G', '南京主集群', 'gpu-nj-01', 1, md5('GPU-NJ-N01-C1'), '0000:11:00.0', '0', '0', 'admin', sysdate()),
('GPU-NJ-N01-C2', 1, 'gpu', 'A100-80G', '南京主集群', 'gpu-nj-01', 2, md5('GPU-NJ-N01-C2'), '0000:12:00.0', '0', '0', 'admin', sysdate()),
('GPU-NJ-N01-C3', 1, 'gpu', 'A100-80G', '南京主集群', 'gpu-nj-01', 3, md5('GPU-NJ-N01-C3'), '0000:13:00.0', '0', '0', 'admin', sysdate()),
('GPU-NJ-N01-C4', 1, 'gpu', 'A100-80G', '南京主集群', 'gpu-nj-01', 4, md5('GPU-NJ-N01-C4'), '0000:14:00.0', '0', '0', 'admin', sysdate()),
('GPU-NJ-N01-C5', 1, 'gpu', 'A100-80G', '南京主集群', 'gpu-nj-01', 5, md5('GPU-NJ-N01-C5'), '0000:15:00.0', '0', '0', 'admin', sysdate()),
('GPU-NJ-N01-C6', 1, 'gpu', 'A100-80G', '南京主集群', 'gpu-nj-01', 6, md5('GPU-NJ-N01-C6'), '0000:16:00.0', '0', '0', 'admin', sysdate()),
('GPU-NJ-N01-C7', 1, 'gpu', 'A100-80G', '南京主集群', 'gpu-nj-01', 7, md5('GPU-NJ-N01-C7'), '0000:17:00.0', '0', '0', 'admin', sysdate()),
('GPU-NJ-N02-C0', 1, 'gpu', 'A100-80G', '南京主集群', 'gpu-nj-02', 0, md5('GPU-NJ-N02-C0'), '0000:10:00.0', '0', '0', 'admin', sysdate()),
('GPU-NJ-N02-C1', 1, 'gpu', 'A100-80G', '南京主集群', 'gpu-nj-02', 1, md5('GPU-NJ-N02-C1'), '0000:11:00.0', '0', '0', 'admin', sysdate()),
('GPU-NJ-N02-C2', 1, 'gpu', 'A100-80G', '南京主集群', 'gpu-nj-02', 2, md5('GPU-NJ-N02-C2'), '0000:12:00.0', '0', '0', 'admin', sysdate()),
('GPU-NJ-N02-C3', 1, 'gpu', 'A100-80G', '南京主集群', 'gpu-nj-02', 3, md5('GPU-NJ-N02-C3'), '0000:13:00.0', '0', '0', 'admin', sysdate()),
('GPU-NJ-N02-C4', 1, 'gpu', 'A100-80G', '南京主集群', 'gpu-nj-02', 4, md5('GPU-NJ-N02-C4'), '0000:14:00.0', '0', '0', 'admin', sysdate()),
('GPU-NJ-N02-C5', 1, 'gpu', 'A100-80G', '南京主集群', 'gpu-nj-02', 5, md5('GPU-NJ-N02-C5'), '0000:15:00.0', '0', '0', 'admin', sysdate()),
('GPU-NJ-N02-C6', 1, 'gpu', 'A100-80G', '南京主集群', 'gpu-nj-02', 6, md5('GPU-NJ-N02-C6'), '0000:16:00.0', '0', '0', 'admin', sysdate()),
('GPU-NJ-N02-C7', 1, 'gpu', 'A100-80G', '南京主集群', 'gpu-nj-02', 7, md5('GPU-NJ-N02-C7'), '0000:17:00.0', '0', '0', 'admin', sysdate()),
('GPU-NJ-N03-C0', 1, 'gpu', 'A100-80G', '南京主集群', 'gpu-nj-03', 0, md5('GPU-NJ-N03-C0'), '0000:10:00.0', '0', '0', 'admin', sysdate()),
('GPU-NJ-N03-C1', 1, 'gpu', 'A100-80G', '南京主集群', 'gpu-nj-03', 1, md5('GPU-NJ-N03-C1'), '0000:11:00.0', '0', '0', 'admin', sysdate()),
('GPU-NJ-N03-C2', 1, 'gpu', 'A100-80G', '南京主集群', 'gpu-nj-03', 2, md5('GPU-NJ-N03-C2'), '0000:12:00.0', '0', '0', 'admin', sysdate()),
('GPU-NJ-N03-C3', 1, 'gpu', 'A100-80G', '南京主集群', 'gpu-nj-03', 3, md5('GPU-NJ-N03-C3'), '0000:13:00.0', '0', '0', 'admin', sysdate()),
('GPU-NJ-N03-C4', 1, 'gpu', 'A100-80G', '南京主集群', 'gpu-nj-03', 4, md5('GPU-NJ-N03-C4'), '0000:14:00.0', '0', '0', 'admin', sysdate()),
('GPU-NJ-N03-C5', 1, 'gpu', 'A100-80G', '南京主集群', 'gpu-nj-03', 5, md5('GPU-NJ-N03-C5'), '0000:15:00.0', '0', '0', 'admin', sysdate()),
('GPU-NJ-N03-C6', 1, 'gpu', 'A100-80G', '南京主集群', 'gpu-nj-03', 6, md5('GPU-NJ-N03-C6'), '0000:16:00.0', '0', '0', 'admin', sysdate()),
('GPU-NJ-N03-C7', 1, 'gpu', 'A100-80G', '南京主集群', 'gpu-nj-03', 7, md5('GPU-NJ-N03-C7'), '0000:17:00.0', '0', '0', 'admin', sysdate()),
('GPU-NJ-N04-C0', 1, 'gpu', 'A100-80G', '南京主集群', 'gpu-nj-04', 0, md5('GPU-NJ-N04-C0'), '0000:10:00.0', '0', '0', 'admin', sysdate()),
('GPU-NJ-N04-C1', 1, 'gpu', 'A100-80G', '南京主集群', 'gpu-nj-04', 1, md5('GPU-NJ-N04-C1'), '0000:11:00.0', '0', '0', 'admin', sysdate()),
('GPU-NJ-N04-C2', 1, 'gpu', 'A100-80G', '南京主集群', 'gpu-nj-04', 2, md5('GPU-NJ-N04-C2'), '0000:12:00.0', '0', '0', 'admin', sysdate()),
('GPU-NJ-N04-C3', 1, 'gpu', 'A100-80G', '南京主集群', 'gpu-nj-04', 3, md5('GPU-NJ-N04-C3'), '0000:13:00.0', '0', '0', 'admin', sysdate()),
('GPU-NJ-N04-C4', 1, 'gpu', 'A100-80G', '南京主集群', 'gpu-nj-04', 4, md5('GPU-NJ-N04-C4'), '0000:14:00.0', '0', '0', 'admin', sysdate()),
('GPU-NJ-N04-C5', 1, 'gpu', 'A100-80G', '南京主集群', 'gpu-nj-04', 5, md5('GPU-NJ-N04-C5'), '0000:15:00.0', '0', '0', 'admin', sysdate()),
('GPU-NJ-N04-C6', 1, 'gpu', 'A100-80G', '南京主集群', 'gpu-nj-04', 6, md5('GPU-NJ-N04-C6'), '0000:16:00.0', '0', '0', 'admin', sysdate()),
('GPU-NJ-N04-C7', 1, 'gpu', 'A100-80G', '南京主集群', 'gpu-nj-04', 7, md5('GPU-NJ-N04-C7'), '0000:17:00.0', '0', '0', 'admin', sysdate());

-- ----------------------------
-- 5.2 资源2：GPU推理资源池-V100（resource_id=2，16 卡）
-- ----------------------------
insert into biz_device(device_code, resource_id, resource_type, resource_spec, cluster_name, node_name, slot_index, uuid, pci_bus, status, del_flag, create_by, create_time) values
('GPU-NJ-N05-C0', 2, 'gpu', 'V100-32G', '南京主集群', 'gpu-nj-05', 0, md5('GPU-NJ-N05-C0'), '0000:18:00.0', '0', '0', 'admin', sysdate()),
('GPU-NJ-N05-C1', 2, 'gpu', 'V100-32G', '南京主集群', 'gpu-nj-05', 1, md5('GPU-NJ-N05-C1'), '0000:19:00.0', '0', '0', 'admin', sysdate()),
('GPU-NJ-N05-C2', 2, 'gpu', 'V100-32G', '南京主集群', 'gpu-nj-05', 2, md5('GPU-NJ-N05-C2'), '0000:1a:00.0', '0', '0', 'admin', sysdate()),
('GPU-NJ-N05-C3', 2, 'gpu', 'V100-32G', '南京主集群', 'gpu-nj-05', 3, md5('GPU-NJ-N05-C3'), '0000:1b:00.0', '0', '0', 'admin', sysdate()),
('GPU-NJ-N05-C4', 2, 'gpu', 'V100-32G', '南京主集群', 'gpu-nj-05', 4, md5('GPU-NJ-N05-C4'), '0000:1c:00.0', '0', '0', 'admin', sysdate()),
('GPU-NJ-N05-C5', 2, 'gpu', 'V100-32G', '南京主集群', 'gpu-nj-05', 5, md5('GPU-NJ-N05-C5'), '0000:1d:00.0', '0', '0', 'admin', sysdate()),
('GPU-NJ-N05-C6', 2, 'gpu', 'V100-32G', '南京主集群', 'gpu-nj-05', 6, md5('GPU-NJ-N05-C6'), '0000:1e:00.0', '0', '0', 'admin', sysdate()),
('GPU-NJ-N05-C7', 2, 'gpu', 'V100-32G', '南京主集群', 'gpu-nj-05', 7, md5('GPU-NJ-N05-C7'), '0000:1f:00.0', '0', '0', 'admin', sysdate()),
('GPU-NJ-N06-C0', 2, 'gpu', 'V100-32G', '南京主集群', 'gpu-nj-06', 0, md5('GPU-NJ-N06-C0'), '0000:20:00.0', '0', '0', 'admin', sysdate()),
('GPU-NJ-N06-C1', 2, 'gpu', 'V100-32G', '南京主集群', 'gpu-nj-06', 1, md5('GPU-NJ-N06-C1'), '0000:21:00.0', '0', '0', 'admin', sysdate()),
('GPU-NJ-N06-C2', 2, 'gpu', 'V100-32G', '南京主集群', 'gpu-nj-06', 2, md5('GPU-NJ-N06-C2'), '0000:22:00.0', '0', '0', 'admin', sysdate()),
('GPU-NJ-N06-C3', 2, 'gpu', 'V100-32G', '南京主集群', 'gpu-nj-06', 3, md5('GPU-NJ-N06-C3'), '0000:23:00.0', '0', '0', 'admin', sysdate()),
('GPU-NJ-N06-C4', 2, 'gpu', 'V100-32G', '南京主集群', 'gpu-nj-06', 4, md5('GPU-NJ-N06-C4'), '0000:24:00.0', '0', '0', 'admin', sysdate()),
('GPU-NJ-N06-C5', 2, 'gpu', 'V100-32G', '南京主集群', 'gpu-nj-06', 5, md5('GPU-NJ-N06-C5'), '0000:25:00.0', '0', '0', 'admin', sysdate()),
('GPU-NJ-N06-C6', 2, 'gpu', 'V100-32G', '南京主集群', 'gpu-nj-06', 6, md5('GPU-NJ-N06-C6'), '0000:26:00.0', '0', '0', 'admin', sysdate()),
('GPU-NJ-N06-C7', 2, 'gpu', 'V100-32G', '南京主集群', 'gpu-nj-06', 7, md5('GPU-NJ-N06-C7'), '0000:27:00.0', '0', '0', 'admin', sysdate());

-- ----------------------------
-- 5.3 资源3：昇腾加速卡资源池（resource_id=3，8 卡）
-- ----------------------------
insert into biz_device(device_code, resource_id, resource_type, resource_spec, cluster_name, node_name, slot_index, uuid, pci_bus, status, del_flag, create_by, create_time) values
('NPU-SZ-N01-C0', 3, 'npu', '昇腾910B', '苏州智算集群', 'npu-sz-01', 0, md5('NPU-SZ-N01-C0'), '0000:30:00.0', '0', '0', 'admin', sysdate()),
('NPU-SZ-N01-C1', 3, 'npu', '昇腾910B', '苏州智算集群', 'npu-sz-01', 1, md5('NPU-SZ-N01-C1'), '0000:31:00.0', '0', '0', 'admin', sysdate()),
('NPU-SZ-N01-C2', 3, 'npu', '昇腾910B', '苏州智算集群', 'npu-sz-01', 2, md5('NPU-SZ-N01-C2'), '0000:32:00.0', '0', '0', 'admin', sysdate()),
('NPU-SZ-N01-C3', 3, 'npu', '昇腾910B', '苏州智算集群', 'npu-sz-01', 3, md5('NPU-SZ-N01-C3'), '0000:33:00.0', '0', '0', 'admin', sysdate()),
('NPU-SZ-N01-C4', 3, 'npu', '昇腾910B', '苏州智算集群', 'npu-sz-01', 4, md5('NPU-SZ-N01-C4'), '0000:34:00.0', '0', '0', 'admin', sysdate()),
('NPU-SZ-N01-C5', 3, 'npu', '昇腾910B', '苏州智算集群', 'npu-sz-01', 5, md5('NPU-SZ-N01-C5'), '0000:35:00.0', '0', '0', 'admin', sysdate()),
('NPU-SZ-N01-C6', 3, 'npu', '昇腾910B', '苏州智算集群', 'npu-sz-01', 6, md5('NPU-SZ-N01-C6'), '0000:36:00.0', '0', '0', 'admin', sysdate()),
('NPU-SZ-N01-C7', 3, 'npu', '昇腾910B', '苏州智算集群', 'npu-sz-01', 7, md5('NPU-SZ-N01-C7'), '0000:37:00.0', '0', '0', 'admin', sysdate());

-- ----------------------------
-- 5.4 资源4：通用计算节点池（resource_id=4，20 台，节点即设备）
-- ----------------------------
insert into biz_device(device_code, resource_id, resource_type, resource_spec, cluster_name, node_name, slot_index, uuid, pci_bus, status, del_flag, create_by, create_time) values
('NODE-NJ-01', 4, 'node', '32C-128G', '南京主集群', 'node-nj-01', 0, md5('NODE-NJ-01'), '', '0', '0', 'admin', sysdate()),
('NODE-NJ-02', 4, 'node', '32C-128G', '南京主集群', 'node-nj-02', 0, md5('NODE-NJ-02'), '', '0', '0', 'admin', sysdate()),
('NODE-NJ-03', 4, 'node', '32C-128G', '南京主集群', 'node-nj-03', 0, md5('NODE-NJ-03'), '', '0', '0', 'admin', sysdate()),
('NODE-NJ-04', 4, 'node', '32C-128G', '南京主集群', 'node-nj-04', 0, md5('NODE-NJ-04'), '', '0', '0', 'admin', sysdate()),
('NODE-NJ-05', 4, 'node', '32C-128G', '南京主集群', 'node-nj-05', 0, md5('NODE-NJ-05'), '', '0', '0', 'admin', sysdate()),
('NODE-NJ-06', 4, 'node', '32C-128G', '南京主集群', 'node-nj-06', 0, md5('NODE-NJ-06'), '', '0', '0', 'admin', sysdate()),
('NODE-NJ-07', 4, 'node', '32C-128G', '南京主集群', 'node-nj-07', 0, md5('NODE-NJ-07'), '', '0', '0', 'admin', sysdate()),
('NODE-NJ-08', 4, 'node', '32C-128G', '南京主集群', 'node-nj-08', 0, md5('NODE-NJ-08'), '', '0', '0', 'admin', sysdate()),
('NODE-NJ-09', 4, 'node', '32C-128G', '南京主集群', 'node-nj-09', 0, md5('NODE-NJ-09'), '', '0', '0', 'admin', sysdate()),
('NODE-NJ-10', 4, 'node', '32C-128G', '南京主集群', 'node-nj-10', 0, md5('NODE-NJ-10'), '', '0', '0', 'admin', sysdate()),
('NODE-NJ-11', 4, 'node', '32C-128G', '南京主集群', 'node-nj-11', 0, md5('NODE-NJ-11'), '', '0', '0', 'admin', sysdate()),
('NODE-NJ-12', 4, 'node', '32C-128G', '南京主集群', 'node-nj-12', 0, md5('NODE-NJ-12'), '', '0', '0', 'admin', sysdate()),
('NODE-NJ-13', 4, 'node', '32C-128G', '南京主集群', 'node-nj-13', 0, md5('NODE-NJ-13'), '', '0', '0', 'admin', sysdate()),
('NODE-NJ-14', 4, 'node', '32C-128G', '南京主集群', 'node-nj-14', 0, md5('NODE-NJ-14'), '', '0', '0', 'admin', sysdate()),
('NODE-NJ-15', 4, 'node', '32C-128G', '南京主集群', 'node-nj-15', 0, md5('NODE-NJ-15'), '', '0', '0', 'admin', sysdate()),
('NODE-NJ-16', 4, 'node', '32C-128G', '南京主集群', 'node-nj-16', 0, md5('NODE-NJ-16'), '', '0', '0', 'admin', sysdate()),
('NODE-NJ-17', 4, 'node', '32C-128G', '南京主集群', 'node-nj-17', 0, md5('NODE-NJ-17'), '', '0', '0', 'admin', sysdate()),
('NODE-NJ-18', 4, 'node', '32C-128G', '南京主集群', 'node-nj-18', 0, md5('NODE-NJ-18'), '', '0', '0', 'admin', sysdate()),
('NODE-NJ-19', 4, 'node', '32C-128G', '南京主集群', 'node-nj-19', 0, md5('NODE-NJ-19'), '', '0', '0', 'admin', sysdate()),
('NODE-NJ-20', 4, 'node', '32C-128G', '南京主集群', 'node-nj-20', 0, md5('NODE-NJ-20'), '', '0', '0', 'admin', sysdate());


-- ----------------------------
-- 6、按池回写库存（设备是真相源，初始化后保证库存与设备一致）
--    total = 未删除设备数；used = status=1 设备数；available = status=0 设备数
--    故障(2)+维护(3) 计入 total，不计入 used / available
-- ----------------------------
update biz_resource r
set r.total_count = (select count(*) from biz_device d where d.resource_id = r.resource_id and d.del_flag = '0'),
    r.used_count  = (select count(*) from biz_device d where d.resource_id = r.resource_id and d.del_flag = '0' and d.status = '1'),
    r.available_count = (select count(*) from biz_device d where d.resource_id = r.resource_id and d.del_flag = '0' and d.status = '0')
where r.resource_id in (1, 2, 3, 4);


-- ==============================================================
-- 7、菜单权限数据（设备台账）
--    二级菜单 2006；按钮 2066 ~ 2072
--    sys_menu 列顺序：menu_id, menu_name, parent_id, order_num, path, component, query,
--                    route_name, is_frame, is_cache, menu_type, visible, status, perms,
--                    icon, create_by, create_time, update_by, update_time, remark
-- ==============================================================
insert into sys_menu values('2006', '设备台账', '2000', '6', 'device', 'bocompute/device/index', '', '', 1, 0, 'C', '0', '0', 'bocompute:device:list', 'component', 'admin', sysdate(), '', null, '模拟算力设备台账菜单');

insert into sys_menu values('2066', '设备查询', '2006', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'bocompute:device:query',  '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2067', '设备新增', '2006', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'bocompute:device:add',    '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2068', '设备修改', '2006', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'bocompute:device:edit',   '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2069', '设备删除', '2006', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'bocompute:device:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2070', '设备导出', '2006', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'bocompute:device:export', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2071', '设备下线', '2006', '6', '', '', '', '', 1, 0, 'F', '0', '0', 'bocompute:device:fault',  '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2072', '设备上线', '2006', '7', '', '', '', '', 1, 0, 'F', '0', '0', 'bocompute:device:online', '#', 'admin', sysdate(), '', null, '');


-- ==============================================================
-- 8、角色授权说明
--    超级管理员 admin 默认拥有全部菜单权限，无需额外授权
--    普通员工角色（role_id = 2）不授予设备台账菜单（2006）
-- ==============================================================
