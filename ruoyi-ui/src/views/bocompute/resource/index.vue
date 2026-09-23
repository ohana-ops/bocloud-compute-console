<template>
  <div class="app-container">
    <!-- 搜索表单 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="资源名称" prop="resourceName">
        <el-input
          v-model="queryParams.resourceName"
          placeholder="请输入资源名称"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="资源编码" prop="resourceCode">
        <el-input
          v-model="queryParams.resourceCode"
          placeholder="请输入资源编码"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="资源类型" prop="resourceType">
        <el-select v-model="queryParams.resourceType" placeholder="请选择资源类型" clearable>
          <el-option
            v-for="dict in dict.type.biz_resource_type"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="资源状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择资源状态" clearable>
          <el-option
            v-for="dict in dict.type.biz_resource_status"
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
          v-hasPermi="['bocompute:resource:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['bocompute:resource:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['bocompute:resource:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['bocompute:resource:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 资源数据表格 -->
    <el-table v-loading="loading" :data="resourceList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="资源ID" align="center" prop="resourceId" width="80" />
      <el-table-column label="资源名称" align="center" prop="resourceName" :show-overflow-tooltip="true" />
      <el-table-column label="资源编码" align="center" prop="resourceCode" width="140" />
      <el-table-column label="资源类型" align="center" prop="resourceType">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.biz_resource_type" :value="scope.row.resourceType"/>
        </template>
      </el-table-column>
      <el-table-column label="资源规格" align="center" prop="resourceSpec" />
      <el-table-column label="所属集群" align="center" prop="clusterName" />
      <el-table-column label="总数量" align="center" prop="totalCount" width="80" />
      <el-table-column label="已分配" align="center" prop="usedCount" width="80" />
      <el-table-column label="可用数量" align="center" prop="availableCount" width="90">
        <template slot-scope="scope">
          <span :style="{ color: scope.row.availableCount > 0 ? '#67C23A' : '#F56C6C' }">
            {{ scope.row.availableCount }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="单位" align="center" prop="unit" width="60" />
      <el-table-column label="资源状态" align="center" prop="status">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.biz_resource_status" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="220">
        <template slot-scope="scope">
          <!-- 走设备调度的资源类型可以跳转到设备台账查看具体卡号 -->
          <el-button
            v-if="scope.row.resourceType !== 'cpu'"
            size="mini"
            type="text"
            icon="el-icon-cpu"
            @click="handleViewDevice(scope.row)"
            v-hasPermi="['bocompute:device:list']"
          >查看设备</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['bocompute:resource:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['bocompute:resource:remove']"
          >删除</el-button>
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

    <!-- 添加或修改算力资源对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="90px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="资源名称" prop="resourceName">
              <el-input v-model="form.resourceName" placeholder="请输入资源名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="资源编码" prop="resourceCode">
              <el-input v-model="form.resourceCode" placeholder="请输入资源编码" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="资源类型" prop="resourceType">
              <el-select v-model="form.resourceType" placeholder="请选择资源类型" style="width: 100%">
                <el-option
                  v-for="dict in dict.type.biz_resource_type"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="资源规格" prop="resourceSpec">
              <el-input v-model="form.resourceSpec" placeholder="如 A100-80G、昇腾910B" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="所属集群" prop="clusterName">
              <el-input v-model="form.clusterName" placeholder="请输入所属集群" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="计量单位" prop="unit">
              <el-input v-model="form.unit" placeholder="如 卡 / 台 / 核" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="资源总数量" prop="totalCount">
              <el-input-number
                v-model="form.totalCount"
                controls-position="right"
                :min="0"
                :disabled="isDeviceResource"
                style="width: 100%"
              />
              <div v-if="isDeviceResource" class="form-tip">
                设备类资源的数量由设备台账汇总，请到「设备台账」维护设备
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="资源状态" prop="status">
              <el-select v-model="form.status" placeholder="请选择资源状态" style="width: 100%">
                <el-option
                  v-for="dict in dict.type.biz_resource_status"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listResource, getResource, delResource, addResource, updateResource } from "@/api/bocompute/resource"

export default {
  name: "BoComputeResource",
  // 页面使用到的字典类型：资源类型、资源状态
  dicts: ['biz_resource_type', 'biz_resource_status'],
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 算力资源表格数据
      resourceList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        resourceName: undefined,
        resourceCode: undefined,
        resourceType: undefined,
        status: undefined
      },
      // 表单参数
      form: {},
      // 表单校验规则
      rules: {
        resourceName: [
          { required: true, message: "资源名称不能为空", trigger: "blur" }
        ],
        resourceCode: [
          { required: true, message: "资源编码不能为空", trigger: "blur" }
        ],
        resourceType: [
          { required: true, message: "资源类型不能为空", trigger: "change" }
        ],
        totalCount: [
          { required: true, message: "资源总数量不能为空", trigger: "blur" }
        ]
      }
    }
  },
  computed: {
    // 走设备调度的资源类型（gpu/npu/node），总数量由设备台账汇总，页面禁止手改
    isDeviceResource() {
      return !!this.form.resourceType && this.form.resourceType !== 'cpu'
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询算力资源列表 */
    getList() {
      this.loading = true
      listResource(this.queryParams).then(response => {
        this.resourceList = response.rows
        this.total = response.total
        this.loading = false
      })
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
        resourceName: undefined,
        resourceCode: undefined,
        resourceType: undefined,
        resourceSpec: undefined,
        clusterName: undefined,
        totalCount: 0,
        unit: "卡",
        status: "0",
        remark: undefined
      }
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
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.resourceId)
      this.single = selection.length != 1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "添加算力资源"
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const resourceId = row.resourceId || this.ids
      getResource(resourceId).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改算力资源"
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.resourceId != undefined) {
            updateResource(this.form).then(() => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addResource(this.form).then(() => {
              this.$modal.msgSuccess("新增成功")
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    /** 查看设备操作：跳转到设备台账并按资源池过滤 */
    handleViewDevice(row) {
      this.$router.push({ path: '/bocompute/device', query: { resourceId: row.resourceId } })
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const resourceIds = row.resourceId || this.ids
      this.$modal.confirm('是否确认删除算力资源编号为"' + resourceIds + '"的数据项？').then(function() {
        return delResource(resourceIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('bocompute/resource/export', {
        ...this.queryParams
      }, `resource_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>

<style scoped>
/* 表单项下方的灰色提示文案 */
.form-tip {
  margin-top: 6px;
  font-size: 12px;
  line-height: 1.5;
  color: #909399;
}
</style>
