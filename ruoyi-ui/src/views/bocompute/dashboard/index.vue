<template>
  <div class="app-container">
    <!-- 顶部统计卡片 -->
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background-color: #409EFF;">
            <i class="el-icon-cpu"></i>
          </div>
          <div class="stat-content">
            <div class="stat-label">资源总数量</div>
            <div class="stat-value">{{ statistics.totalCount }}</div>
            <div class="stat-desc">共 {{ statistics.resourceKinds }} 类资源</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background-color: #67C23A;">
            <i class="el-icon-circle-check"></i>
          </div>
          <div class="stat-content">
            <div class="stat-label">可用数量</div>
            <div class="stat-value" style="color: #67C23A;">{{ statistics.availableCount }}</div>
            <div class="stat-desc">当前可申请的资源</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background-color: #E6A23C;">
            <i class="el-icon-time"></i>
          </div>
          <div class="stat-content">
            <div class="stat-label">待审批申请</div>
            <div class="stat-value" style="color: #E6A23C;">{{ statistics.pendingApplyCount }}</div>
            <div class="stat-desc">需要尽快处理</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background-color: #909399;">
            <i class="el-icon-document"></i>
          </div>
          <div class="stat-content">
            <div class="stat-label">本月申请数</div>
            <div class="stat-value">{{ statistics.monthApplyCount }}</div>
            <div class="stat-desc">当月提交的申请单</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 资源利用率 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="8">
        <el-card shadow="hover">
          <div slot="header">
            <span>整体资源利用率</span>
          </div>
          <div class="usage-wrapper">
            <el-progress
              type="circle"
              :percentage="statistics.usageRate"
              :color="usageColor"
              :width="180"
            />
            <div class="usage-desc">
              已分配 {{ statistics.usedCount }} / 总数 {{ statistics.totalCount }}
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="16">
        <el-card shadow="hover">
          <div slot="header">
            <span>各资源占用情况</span>
          </div>
          <el-table :data="statistics.resources" size="small" max-height="320">
            <el-table-column label="资源名称" prop="resourceName" :show-overflow-tooltip="true" />
            <el-table-column label="资源类型" align="center" prop="resourceType" width="110">
              <template slot-scope="scope">
                <dict-tag :options="dict.type.biz_resource_type" :value="scope.row.resourceType"/>
              </template>
            </el-table-column>
            <el-table-column label="总数" align="center" prop="totalCount" width="70" />
            <el-table-column label="已分配" align="center" prop="usedCount" width="70" />
            <el-table-column label="可用" align="center" prop="availableCount" width="70" />
            <el-table-column label="利用率" align="center" width="160">
              <template slot-scope="scope">
                <el-progress
                  :percentage="calcRate(scope.row.usedCount, scope.row.totalCount)"
                  :color="usageColor"
                />
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { getStatistics } from "@/api/bocompute/dashboard"

export default {
  name: "BoComputeDashboard",
  // 页面使用到的字典类型：资源类型
  dicts: ['biz_resource_type'],
  data() {
    return {
      // 看板统计数据
      statistics: {
        totalCount: 0,
        usedCount: 0,
        availableCount: 0,
        usageRate: 0,
        resourceKinds: 0,
        pendingApplyCount: 0,
        monthApplyCount: 0,
        resources: []
      }
    }
  },
  computed: {
    // 根据利用率动态返回进度条颜色
    usageColor() {
      return this.getUsageColor(this.statistics.usageRate)
    }
  },
  created() {
    this.getStatisticsData()
  },
  methods: {
    /** 查询看板统计数据 */
    getStatisticsData() {
      getStatistics().then(response => {
        this.statistics = response.data
      })
    },
    /** 计算单个资源的利用率（保留整数） */
    calcRate(used, total) {
      if (!total || total === 0) {
        return 0
      }
      return Math.round((used / total) * 100)
    },
    /** 根据利用率返回颜色：低绿、中橙、高红 */
    getUsageColor(rate) {
      if (rate >= 80) {
        return '#F56C6C'
      } else if (rate >= 50) {
        return '#E6A23C'
      }
      return '#67C23A'
    }
  }
}
</script>

<style scoped>
.stat-card .el-card__body {
  display: flex;
  align-items: center;
  padding: 20px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  flex-shrink: 0;
}

.stat-icon i {
  font-size: 30px;
  color: #fff;
}

.stat-content {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.stat-value {
  font-size: 26px;
  font-weight: bold;
  color: #303133;
  line-height: 1.4;
}

.stat-desc {
  font-size: 12px;
  color: #C0C4CC;
}

.usage-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px 0;
}

.usage-desc {
  margin-top: 16px;
  font-size: 14px;
  color: #606266;
}
</style>
