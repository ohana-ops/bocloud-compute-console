package com.ruoyi.bocompute.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ruoyi.bocompute.domain.BizDevice;

/**
 * 模拟算力设备 数据层
 *
 * @author bocloud
 */
public interface BizDeviceMapper extends BaseMapper<BizDevice>
{
    /**
     * 查询模拟算力设备数据集合
     *
     * @param bizDevice 模拟算力设备信息
     * @return 模拟算力设备数据集合
     */
    public IPage<BizDevice> selectBizDeviceList(IPage<BizDevice> page, @Param("bizDevice") BizDevice bizDevice);

    /**
     * 通过设备ID查询模拟算力设备信息
     *
     * @param deviceId 设备ID
     * @return 模拟算力设备信息
     */
    public BizDevice selectBizDeviceById(Long deviceId);

    /**
     * 校验设备编号是否唯一
     *
     * @param deviceCode 设备编号
     * @return 结果
     */
    public BizDevice checkDeviceCodeUnique(String deviceCode);

    /**
     * 查询指定资源池下的空闲设备（按节点、槽位升序，供调度选卡使用）
     *
     * @param resourceId 资源池ID
     * @return 空闲设备列表
     */
    public List<BizDevice> selectIdleDevicesByResourceId(Long resourceId);

    /**
     * 统计指定资源池下未删除的设备数量
     *
     * @param resourceId 资源池ID
     * @return 设备数量
     */
    public int countDeviceByResourceId(Long resourceId);

    /**
     * 统计指定状态的设备数量（看板使用）
     *
     * @param status 设备状态
     * @return 设备数量
     */
    public int countDeviceByStatus(@Param("status") String status);

    /**
     * 新增模拟算力设备信息
     *
     * @param bizDevice 模拟算力设备信息
     * @return 结果
     */
    public int insertBizDevice(BizDevice bizDevice);

    /**
     * 修改模拟算力设备信息
     *
     * @param bizDevice 模拟算力设备信息
     * @return 结果
     */
    public int updateBizDevice(BizDevice bizDevice);

    /**
     * 锁定设备并绑定到分配单（带 status = '0' 条件，防止并发重复占用）
     *
     * @param deviceIds 需要锁定的设备ID集合
     * @param allocId 分配单ID
     * @param applyNo 申请单号
     * @param userName 使用人
     * @return 影响行数，必须等于传入的设备数量
     */
    public int lockDevicesByIds(@Param("deviceIds") List<Long> deviceIds, @Param("allocId") Long allocId,
            @Param("applyNo") String applyNo, @Param("userName") String userName);

    /**
     * 按分配单释放设备（状态回到空闲，清空绑定信息）
     *
     * @param allocId 分配单ID
     * @return 影响行数
     */
    public int releaseDevicesByAllocId(Long allocId);

    /**
     * 变更设备状态（设备运维：故障 / 维护 / 上线）
     *
     * @param deviceId 设备ID
     * @param status 目标状态
     * @param updateBy 更新人
     * @return 结果
     */
    public int updateDeviceStatus(@Param("deviceId") Long deviceId, @Param("status") String status,
            @Param("updateBy") String updateBy);

    /**
     * 批量删除模拟算力设备信息
     *
     * @param deviceIds 需要删除的设备ID
     * @return 结果
     */
    public int deleteBizDeviceByIds(Long[] deviceIds);

    /**
     * 删除模拟算力设备信息
     *
     * @param deviceId 设备ID
     * @return 结果
     */
    public int deleteBizDeviceById(Long deviceId);
}
