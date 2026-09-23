package com.ruoyi.bocompute.schedule;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import com.ruoyi.common.core.utils.StringUtils;

/**
 * 设备编号生成器
 * 
 * 生成规则示例（nodePrefix 形如 gpu-nj / npu-sz / node-nj）：
 * GPU/NPU：GPU-NJ-N01-C0（前缀大写 + 节点序号 + 槽位）
 * NODE：NODE-NJ-01（节点即设备，不带槽位后缀）
 * 
 * 模拟 UUID 使用 UUID.nameUUIDFromBytes(种子) 生成，保证同一编号始终得到同一个值。
 *
 * @author bocloud
 */
public class DeviceCodeGenerator
{
    /** 资源类型：GPU 卡 */
    public static final String TYPE_GPU = "gpu";

    /** 资源类型：国产加速卡 */
    public static final String TYPE_NPU = "npu";

    /** 资源类型：计算节点 */
    public static final String TYPE_NODE = "node";

    /** 资源类型：CPU（按核数扣减，不生成设备） */
    public static final String TYPE_CPU = "cpu";

    /** PCI 总线起始地址，槽位在此基础上递增 */
    private static final int PCI_BUS_BASE = 0x10;

    /**
     * 生成宿主机节点名
     *
     * @param nodePrefix 节点名前缀，如 gpu-nj
     * @param nodeIndex 节点序号，从 1 开始
     * @return 节点名，如 gpu-nj-01
     */
    public static String generateNodeName(String nodePrefix, int nodeIndex)
    {
        String prefix = StringUtils.isBlank(nodePrefix) ? "node" : nodePrefix.trim();
        // 前缀里用户可能已经带上了短横线，这里统一去掉再拼接
        while (prefix.endsWith("-"))
        {
            prefix = prefix.substring(0, prefix.length() - 1);
        }
        return prefix + "-" + String.format("%02d", nodeIndex);
    }

    /**
     * 生成设备编号
     *
     * @param resourceType 资源类型（gpu/npu/node）
     * @param nodePrefix 节点名前缀，如 gpu-nj
     * @param nodeIndex 节点序号，从 1 开始
     * @param slotIndex 节点内槽位，从 0 开始
     * @return 设备编号，如 GPU-NJ-N01-C0 / NODE-NJ-01
     */
    public static String generateDeviceCode(String resourceType, String nodePrefix, int nodeIndex, int slotIndex)
    {
        String prefix = StringUtils.isBlank(nodePrefix) ? "node" : nodePrefix.trim();
        while (prefix.endsWith("-"))
        {
            prefix = prefix.substring(0, prefix.length() - 1);
        }
        String codePrefix = prefix.toUpperCase();
        // 计算节点类型：节点即设备，编号不带槽位
        if (TYPE_NODE.equals(resourceType))
        {
            return codePrefix + "-" + String.format("%02d", nodeIndex);
        }
        return codePrefix + "-N" + String.format("%02d", nodeIndex) + "-C" + slotIndex;
    }

    /**
     * 生成稳定的模拟 UUID（同一种子始终得到同一个值）
     *
     * @param seed 种子，一般传设备编号
     * @return 模拟 UUID
     */
    public static String generateUuid(String seed)
    {
        return UUID.nameUUIDFromBytes(String.valueOf(seed).getBytes(StandardCharsets.UTF_8)).toString();
    }

    /**
     * 生成模拟 PCI 总线号
     *
     * @param resourceType 资源类型（gpu/npu/node）
     * @param slotIndex 节点内槽位，从 0 开始
     * @return PCI 总线号，如 0000:10:00.0；计算节点返回空串
     */
    public static String generatePciBus(String resourceType, int slotIndex)
    {
        // 计算节点没有 PCIe 设备概念，返回空串
        if (TYPE_NODE.equals(resourceType))
        {
            return "";
        }
        return String.format("0000:%02x:00.0", PCI_BUS_BASE + slotIndex);
    }

    /**
     * 判断资源类型是否需要走设备调度（cpu 按核数扣减，不生成设备）
     *
     * @param resourceType 资源类型
     * @return 需要设备调度返回 true
     */
    public static boolean needDevice(String resourceType)
    {
        return !TYPE_CPU.equals(resourceType);
    }
}
