<template>
  <div class="app-container">
    <!-- 搜索表单 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="申请单号" prop="applyNo">
        <el-input
          v-model="queryParams.applyNo"
          placeholder="请输入申请单号"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="资源名称" prop="resourceName">
        <el-input
          v-model="queryParams.resourceName"
          placeholder="请输入资源名称"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="使用人" prop="userName">
        <el-input
          v-model="queryParams.userName"
          placeholder="请输入使用人账号"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="使用状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择使用状态" clearable>
          <el-option
            v-for="dict in dict.type.biz_alloc_status"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['bocompute:alloc:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 分配记录数据表格 -->
    <el-table v-loading="loading" :data="allocList">
      <el-table-column label="分配记录ID" align="center" prop="allocId" width="100" />
      <el-table-column label="申请单号" align="center" prop="applyNo" width="180" />
      <el-table-column label="资源名称" align="center" prop="resourceName" :show-overflow-tooltip="true" />
      <el-table-column label="分配数量" align="center" prop="allocCount" width="90" />
      <el-table-column label="使用人" align="center" prop="userName" width="100" />
      <el-table-column label="使用部门" align="center" prop="deptName" width="120" />
      <el-table-column label="开始时间" align="center" prop="beginTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.beginTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="结束时间" align="center" prop="endTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.endTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="使用状态" align="center" prop="status">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.biz_alloc_status" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column label="释放时间" align="center" prop="releaseTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.releaseTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="100">
        <template slot-scope="scope">
          <el-button
            v-if="scope.row.status === '0'"
            size="mini"
            type="text"
            icon="el-icon-refresh-left"
            @click="handleRelease(scope.row)"
            v-hasPermi="['bocompute:alloc:release']"
          >释放</el-button>
          <span v-else>-</span>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页组件 -->
    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />
  </div>
</template>

<script>
import { listAlloc, releaseAlloc } from "@/api/bocompute/alloc"

export default {
  name: "BoComputeAlloc",
  // 页面使用到的字典类型：分配状态
  dicts: ['biz_alloc_status'],
  data() {
    return {
      // 遮罩层
      loading: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 分配记录表格数据
      allocList: [],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        applyNo: undefined,
        resourceName: undefined,
        userName: undefined,
        status: undefined
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询资源分配记录列表 */
    getList() {
      this.loading = true
      listAlloc(this.queryParams).then(response => {
        this.allocList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm")
      this.handleQuery()
    },
    /** 释放资源操作 */
    handleRelease(row) {
      this.$modal.confirm('是否确认释放资源"' + row.resourceName + '"共' + row.allocCount + '个？').then(function() {
        return releaseAlloc(row.allocId)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("释放成功")
      }).catch(() => {})
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('bocompute/alloc/export', {
        ...this.queryParams
      }, `alloc_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
