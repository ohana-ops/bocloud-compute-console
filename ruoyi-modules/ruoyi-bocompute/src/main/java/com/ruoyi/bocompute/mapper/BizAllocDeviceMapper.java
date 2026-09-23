package com.ruoyi.bocompute.mapper;

import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.bocompute.domain.BizAllocDevice;

/**
 * 分配单-设备绑定 数据层
 *
 * @author bocloud
 */
public interface BizAllocDeviceMapper
{
    /**
     * 通过分配单ID查询绑定的设备列表
     *
     * @param allocId 分配单ID
     * @return 设备绑定列表
     */
    public List<BizAllocDevice> selectBizAllocDeviceByAllocId(Long allocId);

    /**
     * 通过申请单ID查询绑定的设备列表
     *
     * @param applyId 申请单ID
     * @return 设备绑定列表
     */
    public List<BizAllocDevice> selectBizAllocDeviceByApplyId(Long applyId);

    /**
     * 新增分配单-设备绑定信息
     *
     * @param bizAllocDevice 分配单-设备绑定信息
     * @return 结果
     */
    public int insertBizAllocDevice(BizAllocDevice bizAllocDevice);

    /**
     * 批量新增分配单-设备绑定信息
     *
     * @param list 分配单-设备绑定信息集合
     * @return 结果
     */
    public int batchInsertBizAllocDevice(List<BizAllocDevice> list);

    /**
     * 释放分配单绑定的全部设备（绑定状态置为已释放，写释放时间）
     *
     * @param allocId 分配单ID
     * @param releaseTime 释放时间
     * @return 结果
     */
    public int releaseBizAllocDeviceByAllocId(@Param("allocId") Long allocId, @Param("releaseTime") Date releaseTime);

    /**
     * 通过分配单ID删除绑定信息
     *
     * @param allocId 分配单ID
     * @return 结果
     */
    public int deleteBizAllocDeviceByAllocId(Long allocId);
}
