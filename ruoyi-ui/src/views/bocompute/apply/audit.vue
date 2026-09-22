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
      <el-form-item label="申请人" prop="applyUserName">
        <el-input
          v-model="queryParams.applyUserName"
          placeholder="请输入申请人账号"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="申请状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择申请状态" clearable>
          <el-option
            v-for="dict in dict.type.biz_apply_status"
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
          type="primary"
          plain
          icon="el-icon-s-check"
          size="mini"
          @click="handleQuickPending"
        >只看待审批</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['bocompute:apply:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 申请单数据表格 -->
    <el-table v-loading="loading" :data="applyList">
      <el-table-column label="申请单号" align="center" prop="applyNo" width="180" />
      <el-table-column label="申请人" align="center" prop="applyUserName" width="100" />
      <el-table-column label="申请部门" align="center" prop="applyDeptName" width="120" />
      <el-table-column label="申请资源" align="center" prop="resourceName" :show-overflow-tooltip="true" />
      <el-table-column label="资源类型" align="center" prop="resourceType">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.biz_resource_type" :value="scope.row.resourceType"/>
        </template>
      </el-table-column>
      <el-table-column label="申请数量" align="center" prop="applyCount" width="90" />
      <el-table-column label="申请用途" align="center" prop="purpose" :show-overflow-tooltip="true" />
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
      <el-table-column label="申请状态" align="center" prop="status">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.biz_apply_status" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="180">
        <template slot-scope="scope">
          <el-button
            v-if="scope.row.status === '0'"
            size="mini"
            type="text"
            icon="el-icon-check"
            @click="handlePass(scope.row)"
            v-hasPermi="['bocompute:audit:pass']"
          >通过</el-button>
          <el-button
            v-if="scope.row.status === '0'"
            size="mini"
            type="text"
            icon="el-icon-close"
            @click="handleReject(scope.row)"
            v-hasPermi="['bocompute:audit:reject']"
          >驳回</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-view"
            @click="handleView(scope.row)"
          >详情</el-button>
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

    <!-- 审批对话框 -->
    <el-dialog :title="auditTitle" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="auditForm" :model="auditForm" :rules="auditRules" label-width="90px">
        <el-form-item label="申请单号">
          <el-input v-model="auditForm.applyNo" disabled />
        </el-form-item>
        <el-form-item label="申请资源">
          <el-input v-model="auditForm.resourceName" disabled />
        </el-form-item>
        <el-form-item label="申请数量">
          <el-input v-model="auditForm.applyCount" disabled />
        </el-form-item>
        <el-form-item label="审批意见" prop="auditOpinion">
          <el-input
            v-model="auditForm.auditOpinion"
            type="textarea"
            :rows="3"
            :placeholder="auditType === 'pass' ? '请输入审批意见（可选）' : '请填写驳回原因（必填）'"
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitAudit">确 定</el-button>
        <el-button @click="open = false">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 申请单详情对话框 -->
    <el-dialog title="申请单详情" :visible.sync="detailOpen" width="600px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="申请单号">{{ detail.applyNo }}</el-descriptions-item>
        <el-descriptions-item label="申请状态">
          <dict-tag :options="dict.type.biz_apply_status" :value="detail.status"/>
        </el-descriptions-item>
        <el-descriptions-item label="申请人">{{ detail.applyUserName }}</el-descriptions-item>
        <el-descriptions-item label="申请部门">{{ detail.applyDeptName }}</el-descriptions-item>
        <el-descriptions-item label="申请资源">{{ detail.resourceName }}</el-descriptions-item>
        <el-descriptions-item label="申请数量">{{ detail.applyCount }}</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ parseTime(detail.beginTime) }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ parseTime(detail.endTime) }}</el-descriptions-item>
        <el-descriptions-item label="审批人">{{ detail.auditBy }}</el-descriptions-item>
        <el-descriptions-item label="审批时间">{{ parseTime(detail.auditTime) }}</el-descriptions-item>
        <el-descriptions-item label="申请用途" :span="2">{{ detail.purpose }}</el-descriptions-item>
        <el-descriptions-item label="审批意见" :span="2">{{ detail.auditOpinion }}</el-descriptions-item>
      </el-descriptions>
      <div slot="footer" class="dialog-footer">
        <el-button @click="detailOpen = false">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listApply, getApply, passApply, rejectApply } from "@/api/bocompute/apply"

export default {
  name: "BoComputeAudit",
  // 页面使用到的字典类型：申请状态、资源类型
  dicts: ['biz_apply_status', 'biz_resource_type'],
  data() {
    return {
      // 遮罩层
      loading: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 申请单表格数据
      applyList: [],
      // 是否显示审批弹出层
      open: false,
      // 审批弹窗标题
      auditTitle: "",
      // 审批类型：pass 通过 / reject 驳回
      auditType: "pass",
      // 审批表单参数
      auditForm: {},
      // 是否显示详情弹出层
      detailOpen: false,
      // 申请单详情数据
      detail: {},
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        applyNo: undefined,
        applyUserName: undefined,
        status: undefined
      },
      // 审批表单校验规则
      auditRules: {}
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询申请单列表（全部） */
    getList() {
      this.loading = true
      listApply(this.queryParams).then(response => {
        this.applyList = response.rows
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
    /** 只看待审批操作 */
    handleQuickPending() {
      this.queryParams.status = "0"
      this.handleQuery()
    },
    /** 通过按钮操作 */
    handlePass(row) {
      this.auditType = "pass"
      this.auditTitle = "审批通过"
      // 通过时审批意见可空
      this.auditRules = {}
      this.auditForm = {
        applyId: row.applyId,
        applyNo: row.applyNo,
        resourceName: row.resourceName,
        applyCount: row.applyCount,
        auditOpinion: undefined
      }
      this.open = true
    },
    /** 驳回按钮操作 */
    handleReject(row) {
      this.auditType = "reject"
      this.auditTitle = "审批驳回"
      // 驳回时审批意见必填
      this.auditRules = {
        auditOpinion: [
          { required: true, message: "驳回原因不能为空", trigger: "blur" }
        ]
      }
      this.auditForm = {
        applyId: row.applyId,
        applyNo: row.applyNo,
        resourceName: row.resourceName,
        applyCount: row.applyCount,
        auditOpinion: undefined
      }
      this.open = true
    },
    /** 提交审批操作 */
    submitAudit() {
      this.$refs["auditForm"].validate(valid => {
        if (valid) {
          // 根据审批类型调用对应接口
          const request = this.auditType === "pass" ? passApply : rejectApply
          request({
            applyId: this.auditForm.applyId,
            auditOpinion: this.auditForm.auditOpinion
          }).then(() => {
            this.$modal.msgSuccess(this.auditType === "pass" ? "审批通过成功" : "审批驳回成功")
            this.open = false
            this.getList()
          })
        }
      })
    },
    /** 查看详情操作 */
    handleView(row) {
      getApply(row.applyId).then(response => {
        this.detail = response.data
        this.detailOpen = true
      })
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('bocompute/apply/export', {
        ...this.queryParams
      }, `apply_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
