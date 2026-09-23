package com.ruoyi.bocompute.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.bocompute.domain.BizAllocDevice;
import com.ruoyi.bocompute.domain.BizDevice;

/**
 * 模拟算力设备 服务层
 *
 * @author bocloud
 */
public interface IBizDeviceService
{
    /**
     * 查询模拟算力设备信息集合
     *
     * @param bizDevice 模拟算力设备信息
     * @return 模拟算力设备信息集合
     */
    public List<BizDevice> selectBizDeviceList(BizDevice bizDevice);

    /**
     * 通过设备ID查询模拟算力设备信息
     *
     * @param deviceId 设备ID
     * @return 模拟算力设备信息
     */
    public BizDevice selectBizDeviceById(Long deviceId);

    /**
     * 查询某次分配绑定的设备清单
     *
     * @param allocId 分配单ID
     * @return 设备绑定清单
     */
    public List<BizAllocDevice> selectDevicesByAllocId(Long allocId);

    /**
     * 校验设备编号是否唯一
     *
     * @param bizDevice 模拟算力设备信息
     * @return 结果
     */
    public boolean checkDeviceCodeUnique(BizDevice bizDevice);

    /**
     * 统计设备状态分布（看板使用）
     *
     * @return 设备状态统计，key 为 total / idle / allocated / fault / maintain
     */
    public Map<String, Object> selectDeviceStatusStatistics();

    /**
     * 新增模拟算力设备信息
     *
     * @param bizDevice 模拟算力设备信息
     * @return 结果
     */
    public int insertBizDevice(BizDevice bizDevice);

    /**
     * 按资源池批量初始化设备
     *
     * @param resourceId 资源池ID
     * @param nodePrefix 节点名前缀，如 gpu-nj
     * @param nodeCount 节点数量
     * @param cardsPerNode 每节点卡数（计算节点类型固定为 1）
     * @param confirm 池内已有设备时是否确认覆盖追加
     * @return 新增的设备数量
     */
    public int batchInitDevices(Long resourceId, String nodePrefix, Integer nodeCount, Integer cardsPerNode,
            boolean confirm);

    /**
     * 修改模拟算力设备信息
     *
     * @param bizDevice 模拟算力设备信息
     * @return 结果
     */
    public int updateBizDevice(BizDevice bizDevice);

    /**
     * 变更设备状态（故障 / 维护 / 上线）
     *
     * @param deviceId 设备ID
     * @param targetStatus 目标状态（2故障 3维护 0空闲）
     * @return 结果
     */
    public int changeDeviceStatus(Long deviceId, String targetStatus);

    /**
     * 批量删除模拟算力设备信息
     *
     * @param deviceIds 需要删除的设备ID
     * @return 结果
     */
    public int deleteBizDeviceByIds(Long[] deviceIds);
}
