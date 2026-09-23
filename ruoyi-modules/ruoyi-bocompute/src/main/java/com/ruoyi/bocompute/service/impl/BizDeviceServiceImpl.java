package com.ruoyi.bocompute.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.bocompute.domain.BizAllocDevice;
import com.ruoyi.bocompute.domain.BizDevice;
import com.ruoyi.bocompute.domain.BizResource;
import com.ruoyi.bocompute.mapper.BizAllocDeviceMapper;
import com.ruoyi.bocompute.mapper.BizDeviceMapper;
import com.ruoyi.bocompute.mapper.BizResourceMapper;
import com.ruoyi.bocompute.schedule.DeviceCodeGenerator;
import com.ruoyi.bocompute.service.IBizDeviceService;
import com.ruoyi.common.core.constant.UserConstants;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.security.utils.SecurityUtils;

/**
 * 模拟算力设备 服务层处理
 *
 * @author bocloud
 */
@Service
public class BizDeviceServiceImpl implements IBizDeviceService
{
    /** 单池批量初始化上限，避免误填导致一次插入过多设备 */
    private static final int MAX_BATCH_INIT = 512;

    @Autowired
    private BizDeviceMapper bizDeviceMapper;

    @Autowired
    private BizAllocDeviceMapper bizAllocDeviceMapper;

    @Autowired
    private BizResourceMapper bizResourceMapper;

    /**
     * 查询模拟算力设备信息集合
     *
     * @param bizDevice 模拟算力设备信息
     * @return 模拟算力设备信息集合
     */
    @Override
    public List<BizDevice> selectBizDeviceList(BizDevice bizDevice)
    {
        return bizDeviceMapper.selectBizDeviceList(bizDevice);
    }

    /**
     * 通过设备ID查询模拟算力设备信息
     *
     * @param deviceId 设备ID
     * @return 模拟算力设备信息
     */
    @Override
    public BizDevice selectBizDeviceById(Long deviceId)
    {
        return bizDeviceMapper.selectBizDeviceById(deviceId);
    }

    /**
     * 查询某次分配绑定的设备清单
     *
     * @param allocId 分配单ID
     * @return 设备绑定清单
     */
    @Override
    public List<BizAllocDevice> selectDevicesByAllocId(Long allocId)
    {
        return bizAllocDeviceMapper.selectBizAllocDeviceByAllocId(allocId);
    }

    /**
     * 校验设备编号是否唯一
     *
     * @param bizDevice 模拟算力设备信息
     * @return 结果
     */
    @Override
    public boolean checkDeviceCodeUnique(BizDevice bizDevice)
    {
        Long deviceId = StringUtils.isNull(bizDevice.getDeviceId()) ? -1L : bizDevice.getDeviceId();
        BizDevice info = bizDeviceMapper.checkDeviceCodeUnique(bizDevice.getDeviceCode());
        if (StringUtils.isNotNull(info) && info.getDeviceId().longValue() != deviceId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 统计设备状态分布（看板使用）
     *
     * @return 设备状态统计
     */
    @Override
    public Map<String, Object> selectDeviceStatusStatistics()
    {
        int idle = bizDeviceMapper.countDeviceByStatus(BizDevice.STATUS_IDLE);
        int allocated = bizDeviceMapper.countDeviceByStatus(BizDevice.STATUS_ALLOCATED);
        int fault = bizDeviceMapper.countDeviceByStatus(BizDevice.STATUS_FAULT);
        int maintain = bizDeviceMapper.countDeviceByStatus(BizDevice.STATUS_MAINTAIN);
        Map<String, Object> data = new HashMap<String, Object>();
        // 设备总数
        data.put("total", idle + allocated + fault + maintain);
        // 空闲设备数
        data.put("idle", idle);
        // 已分配设备数
        data.put("allocated", allocated);
        // 故障设备数
        data.put("fault", fault);
        // 维护设备数
        data.put("maintain", maintain);
        return data;
    }

    /**
     * 新增模拟算力设备信息
     *
     * @param bizDevice 模拟算力设备信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertBizDevice(BizDevice bizDevice)
    {
        BizResource resource = bizResourceMapper.selectBizResourceById(bizDevice.getResourceId());
        if (StringUtils.isNull(resource))
        {
            throw new ServiceException("所属资源池不存在");
        }
        if (DeviceCodeGenerator.TYPE_CPU.equals(resource.getResourceType()))
        {
            throw new ServiceException("CPU资源按核数扣减，不支持注册设备");
        }
        // 冗余资源类型、规格、集群，便于列表展示与筛选
        bizDevice.setResourceType(resource.getResourceType());
        bizDevice.setResourceSpec(resource.getResourceSpec());
        bizDevice.setClusterName(resource.getClusterName());
        // 新注册设备默认空闲
        bizDevice.setStatus(BizDevice.STATUS_IDLE);
        bizDevice.setCreateBy(SecurityUtils.getUsername());
        bizDevice.setCreateTime(DateUtils.getNowDate());
        int rows = bizDeviceMapper.insertBizDevice(bizDevice);
        // 设备是库存真相源，注册后按设备汇总回写资源池
        bizResourceMapper.syncResourceCount(bizDevice.getResourceId());
        return rows;
    }

    /**
     * 按资源池批量初始化设备
     *
     * @param resourceId 资源池ID
     * @param nodePrefix 节点名前缀
     * @param nodeCount 节点数量
     * @param cardsPerNode 每节点卡数
     * @param confirm 池内已有设备时是否确认追加
     * @return 新增的设备数量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchInitDevices(Long resourceId, String nodePrefix, Integer nodeCount, Integer cardsPerNode,
            boolean confirm)
    {
        BizResource resource = bizResourceMapper.selectBizResourceById(resourceId);
        if (StringUtils.isNull(resource))
        {
            throw new ServiceException("所属资源池不存在");
        }
        if (DeviceCodeGenerator.TYPE_CPU.equals(resource.getResourceType()))
        {
            throw new ServiceException("CPU资源按核数扣减，不支持初始化设备");
        }
        if (StringUtils.isNull(nodeCount) || nodeCount <= 0)
        {
            throw new ServiceException("节点数量必须大于0");
        }
        if (StringUtils.isNull(cardsPerNode) || cardsPerNode <= 0)
        {
            throw new ServiceException("每节点卡数必须大于0");
        }
        // 计算节点类型：节点即设备，每节点固定 1 台
        if (DeviceCodeGenerator.TYPE_NODE.equals(resource.getResourceType()))
        {
            cardsPerNode = 1;
        }
        int total = nodeCount * cardsPerNode;
        if (total > MAX_BATCH_INIT)
        {
            throw new ServiceException("单次初始化设备数量不能超过" + MAX_BATCH_INIT + "台");
        }
        // 池内已有设备时需要二次确认，避免重复初始化
        int exists = bizDeviceMapper.countDeviceByResourceId(resourceId);
        if (exists > 0 && !confirm)
        {
            throw new ServiceException("该资源池已存在" + exists + "台设备，请确认后重试");
        }
        String resourceType = resource.getResourceType();
        int success = 0;
        for (int nodeIndex = 1; nodeIndex <= nodeCount; nodeIndex++)
        {
            String nodeName = DeviceCodeGenerator.generateNodeName(nodePrefix, nodeIndex);
            for (int slotIndex = 0; slotIndex < cardsPerNode; slotIndex++)
            {
                String deviceCode = DeviceCodeGenerator.generateDeviceCode(resourceType, nodePrefix, nodeIndex,
                        slotIndex);
                // 编号已存在则跳过，保证重复执行不会产生脏数据
                BizDevice exist = bizDeviceMapper.checkDeviceCodeUnique(deviceCode);
                if (StringUtils.isNotNull(exist))
                {
                    continue;
                }
                BizDevice device = new BizDevice();
                device.setDeviceCode(deviceCode);
                device.setResourceId(resourceId);
                device.setResourceType(resourceType);
                device.setResourceSpec(resource.getResourceSpec());
                device.setClusterName(resource.getClusterName());
                device.setNodeName(nodeName);
                device.setSlotIndex(slotIndex);
                // 模拟 UUID 与 PCI 使用稳定生成规则，便于演示时定位
                device.setUuid(DeviceCodeGenerator.generateUuid(deviceCode));
                device.setPciBus(DeviceCodeGenerator.generatePciBus(resourceType, slotIndex));
                device.setStatus(BizDevice.STATUS_IDLE);
                device.setCreateBy(SecurityUtils.getUsername());
                device.setCreateTime(DateUtils.getNowDate());
                success += bizDeviceMapper.insertBizDevice(device);
            }
        }
        // 初始化完成后按设备汇总回写资源池库存
        bizResourceMapper.syncResourceCount(resourceId);
        return success;
    }

    /**
     * 修改模拟算力设备信息
     *
     * @param bizDevice 模拟算力设备信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateBizDevice(BizDevice bizDevice)
    {
        BizDevice old = bizDeviceMapper.selectBizDeviceById(bizDevice.getDeviceId());
        if (StringUtils.isNull(old))
        {
            throw new ServiceException("设备不存在");
        }
        // 已分配的设备不允许改状态，必须先释放
        if (StringUtils.isNotNull(bizDevice.getStatus()) && !bizDevice.getStatus().equals(old.getStatus()))
        {
            changeDeviceStatus(bizDevice.getDeviceId(), bizDevice.getStatus());
        }
        BizDevice update = new BizDevice();
        update.setDeviceId(bizDevice.getDeviceId());
        update.setNodeName(bizDevice.getNodeName());
        update.setSlotIndex(bizDevice.getSlotIndex());
        update.setResourceSpec(bizDevice.getResourceSpec());
        update.setClusterName(bizDevice.getClusterName());
        update.setRemark(bizDevice.getRemark());
        update.setUpdateBy(SecurityUtils.getUsername());
        return bizDeviceMapper.updateBizDevice(update);
    }

    /**
     * 变更设备状态（故障 / 维护 / 上线）
     * 空闲 → 故障 / 维护：允许；已分配 → 故障 / 维护：不允许；故障 / 维护 → 空闲：允许。
     *
     * @param deviceId 设备ID
     * @param targetStatus 目标状态（2故障 3维护 0空闲）
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int changeDeviceStatus(Long deviceId, String targetStatus)
    {
        BizDevice device = bizDeviceMapper.selectBizDeviceById(deviceId);
        if (StringUtils.isNull(device))
        {
            throw new ServiceException("设备不存在");
        }
        String currentStatus = device.getStatus();
        if (currentStatus.equals(targetStatus))
        {
            throw new ServiceException("设备已处于该状态");
        }
        // 故障与维护只允许从空闲状态切入，已分配必须先释放
        if ((BizDevice.STATUS_FAULT.equals(targetStatus) || BizDevice.STATUS_MAINTAIN.equals(targetStatus))
                && !BizDevice.STATUS_IDLE.equals(currentStatus))
        {
            throw new ServiceException("设备已被分配，请先释放后再变更状态");
        }
        // 上线只允许故障、维护状态的设备执行
        if (BizDevice.STATUS_IDLE.equals(targetStatus) && !BizDevice.STATUS_FAULT.equals(currentStatus)
                && !BizDevice.STATUS_MAINTAIN.equals(currentStatus))
        {
            throw new ServiceException("仅故障或维护状态的设备可以上线");
        }
        int rows = bizDeviceMapper.updateDeviceStatus(deviceId, targetStatus, SecurityUtils.getUsername());
        // 状态变更后按设备汇总回写资源池库存
        bizResourceMapper.syncResourceCount(device.getResourceId());
        return rows;
    }

    /**
     * 批量删除模拟算力设备信息（已分配的设备不允许删除）
     *
     * @param deviceIds 需要删除的设备ID
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteBizDeviceByIds(Long[] deviceIds)
    {
        for (Long deviceId : deviceIds)
        {
            BizDevice device = bizDeviceMapper.selectBizDeviceById(deviceId);
            if (StringUtils.isNull(device))
            {
                continue;
            }
            if (BizDevice.STATUS_ALLOCATED.equals(device.getStatus()))
            {
                throw new ServiceException("设备" + device.getDeviceCode() + "已分配，请先释放后再删除");
            }
            bizDeviceMapper.deleteBizDeviceById(deviceId);
            // 删除后按设备汇总回写资源池库存
            bizResourceMapper.syncResourceCount(device.getResourceId());
        }
        return deviceIds.length;
    }
}
