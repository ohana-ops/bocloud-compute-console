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
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['bocompute:apply:add']"
        >提交申请</el-button>
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
      <el-table-column label="审批人" align="center" prop="auditBy" width="100" />
      <el-table-column label="审批意见" align="center" prop="auditOpinion" :show-overflow-tooltip="true" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="150">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-view"
            @click="handleView(scope.row)"
          >详情</el-button>
          <el-button
            v-if="scope.row.status === '0'"
            size="mini"
            type="text"
            icon="el-icon-close"
            @click="handleCancel(scope.row)"
            v-hasPermi="['bocompute:apply:cancel']"
          >取消</el-button>
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

    <!-- 提交申请对话框 -->
    <el-dialog title="提交资源申请" :visible.sync="open" width="600px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="申请资源" prop="resourceId">
          <el-select
            v-model="form.resourceId"
            placeholder="请选择需要申请的算力资源"
            style="width: 100%"
            @change="handleResourceChange"
          >
            <el-option
              v-for="item in resourceOptions"
              :key="item.resourceId"
              :label="item.resourceName + '（可用：' + item.availableCount + item.unit + '）'"
              :value="item.resourceId"
            />
          </el-select>
        </el-form-item>
        <el-row>
          <el-col :span="12">
            <el-form-item label="资源类型" prop="resourceType">
              <el-input v-model="form.resourceType" disabled placeholder="选择资源后自动填充" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="申请数量" prop="applyCount">
              <el-input-number
                v-model="form.applyCount"
                controls-position="right"
                :min="1"
                :max="currentMax"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="开始时间" prop="beginTime">
              <el-date-picker
                v-model="form.beginTime"
                type="datetime"
                placeholder="选择开始时间"
                value-format="yyyy-MM-dd HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间" prop="endTime">
              <el-date-picker
                v-model="form.endTime"
                type="datetime"
                placeholder="选择结束时间"
                value-format="yyyy-MM-dd HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="申请用途" prop="purpose">
          <el-input
            v-model="form.purpose"
            type="textarea"
            :rows="3"
            placeholder="请简要说明申请用途，如：用于大模型训练"
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">提 交</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 申请单详情对话框 -->
    <el-dialog title="申请单详情" :visible.sync="detailOpen" width="600px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="申请单号">{{ detail.applyNo }}</el-descriptions-item>
        <el-descriptions-item label="申请状态">
          <dict-tag :options="dict.type.biz_apply_status" :value="detail.status"/>
        </el-descriptions-item>
        <el-descriptions-item label="申请资源">{{ detail.resourceName }}</el-descriptions-item>
        <el-descriptions-item label="申请数量">{{ detail.applyCount }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ detail.applyUserName }}</el-descriptions-item>
        <el-descriptions-item label="申请部门">{{ detail.applyDeptName }}</el-descriptions-item>
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
import { listMyApply, getApply, addApply, cancelApply } from "@/api/bocompute/apply"
import { optionselectResource } from "@/api/bocompute/resource"

export default {
  name: "BoComputeMyApply",
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
      // 可申请的算力资源下拉数据
      resourceOptions: [],
      // 当前选中资源的可用数量上限
      currentMax: 9999,
      // 是否显示提交申请弹出层
      open: false,
      // 是否显示详情弹出层
      detailOpen: false,
      // 申请单详情数据
      detail: {},
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        applyNo: undefined,
        resourceName: undefined,
        status: undefined
      },
      // 表单参数
      form: {},
      // 表单校验规则
      rules: {
        resourceId: [
          { required: true, message: "请选择申请资源", trigger: "change" }
        ],
        applyCount: [
          { required: true, message: "申请数量不能为空", trigger: "blur" }
        ],
        purpose: [
          { required: true, message: "申请用途不能为空", trigger: "blur" }
        ],
        beginTime: [
          { required: true, message: "开始时间不能为空", trigger: "change" }
        ],
        endTime: [
          { required: true, message: "结束时间不能为空", trigger: "change" }
        ]
      }
    }
  },
  created() {
    this.getList()
    this.getResourceOptions()
  },
  methods: {
    /** 查询我的申请列表 */
    getList() {
      this.loading = true
      listMyApply(this.queryParams).then(response => {
        this.applyList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    /** 查询可申请的算力资源（仅可用状态） */
    getResourceOptions() {
      optionselectResource().then(response => {
        this.resourceOptions = response.data || []
      })
    },
    // 选择资源后自动填充资源类型，并限制最大可申请数量
    handleResourceChange(resourceId) {
      const resource = this.resourceOptions.find(item => item.resourceId === resourceId)
      if (resource) {
        this.form.resourceType = resource.resourceType
        this.currentMax = resource.availableCount
        // 若填写数量超过可用数量，自动修正为可用数量
        if (this.form.applyCount > resource.availableCount) {
          this.form.applyCount = resource.availableCount
        }
      }
    },
    // 取消按钮
    cancel() {
      this.open = false
      this.reset()
    },
    // 表单重置
    reset() {
      this.form = {
        resourceId: undefined,
        resourceType: undefined,
        applyCount: 1,
        beginTime: undefined,
        endTime: undefined,
        purpose: undefined
      }
      this.currentMax = 9999
      this.resetForm("form")
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
    /** 提交申请按钮操作 */
    handleAdd() {
      this.reset()
      // 每次打开都刷新一次可选资源，保证可用数量最新
      this.getResourceOptions()
      this.open = true
    },
    /** 查看详情操作 */
    handleView(row) {
      getApply(row.applyId).then(response => {
        this.detail = response.data
        this.detailOpen = true
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          addApply(this.form).then(() => {
            this.$modal.msgSuccess("申请提交成功，请等待审批")
            this.open = false
            this.getList()
          })
        }
      })
    },
    /** 取消申请操作 */
    handleCancel(row) {
      this.$modal.confirm('是否确认取消申请单"' + row.applyNo + '"？').then(function() {
        return cancelApply(row.applyId)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("取消成功")
      }).catch(() => {})
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
