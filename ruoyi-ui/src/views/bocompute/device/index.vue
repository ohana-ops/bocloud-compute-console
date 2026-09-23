<template>
  <div class="app-container">
    <!-- 搜索表单 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="设备编号" prop="deviceCode">
        <el-input
          v-model="queryParams.deviceCode"
          placeholder="请输入设备编号"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="资源池" prop="resourceId">
        <el-select v-model="queryParams.resourceId" placeholder="请选择资源池" clearable filterable>
          <el-option
            v-for="item in resourceOptions"
            :key="item.resourceId"
            :label="item.resourceName"
            :value="item.resourceId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="节点" prop="nodeName">
        <el-input
          v-model="queryParams.nodeName"
          placeholder="请输入节点名"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="设备状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择设备状态" clearable>
          <el-option
            v-for="dict in dict.type.biz_device_status"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="使用人" prop="userName">
        <el-input
          v-model="queryParams.userName"
          placeholder="请输入使用人"
          clearable
          @keyup.enter.native="handleQuery"
        />
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
          v-hasPermi="['bocompute:device:add']"
        >注册</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-c-scale-to-original"
          size="mini"
          @click="handleBatchInit"
          v-hasPermi="['bocompute:device:add']"
        >批量初始化</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['bocompute:device:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 设备台账表格 -->
    <el-table v-loading="loading" :data="deviceList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="设备编号" align="center" prop="deviceCode" width="180" :show-overflow-tooltip="true" />
      <el-table-column label="资源池" align="center" prop="resourceName" :show-overflow-tooltip="true" />
      <el-table-column label="规格" align="center" prop="resourceSpec" width="110" />
      <el-table-column label="集群" align="center" prop="clusterName" width="120" :show-overflow-tooltip="true" />
      <el-table-column label="节点" align="center" prop="nodeName" width="120" />
      <el-table-column label="槽位" align="center" prop="slotIndex" width="60" />
      <el-table-column label="状态" align="center" prop="status" width="90">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.biz_device_status" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column label="当前使用人" align="center" prop="userName" width="110" />
      <el-table-column label="申请单号" align="center" prop="applyNo" width="180" :show-overflow-tooltip="true" />
      <el-table-column label="绑定时间" align="center" prop="bindTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.bindTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="260">
        <template slot-scope="scope">
          <!-- 已分配的设备不允许直接下线或删除，必须先释放 -->
          <el-button
            v-if="scope.row.status === '0'"
            size="mini"
            type="text"
            icon="el-icon-warning"
            @click="handleFault(scope.row)"
            v-hasPermi="['bocompute:device:fault']"
          >故障</el-button>
          <el-button
            v-if="scope.row.status === '0'"
            size="mini"
            type="text"
            icon="el-icon-setting"
            @click="handleMaintain(scope.row)"
            v-hasPermi="['bocompute:device:edit']"
          >维护</el-button>
          <el-button
            v-if="scope.row.status === '2' || scope.row.status === '3'"
            size="mini"
            type="text"
            icon="el-icon-circle-check"
            @click="handleOnline(scope.row)"
            v-hasPermi="['bocompute:device:online']"
          >上线</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['bocompute:device:edit']"
          >修改</el-button>
          <el-button
            v-if="scope.row.status !== '1'"
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['bocompute:device:remove']"
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

    <!-- 注册/修改设备对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="设备编号" prop="deviceCode">
          <el-input v-model="form.deviceCode" placeholder="如 GPU-NJ-N01-C0" />
        </el-form-item>
        <el-form-item label="所属资源池" prop="resourceId">
          <el-select
            v-model="form.resourceId"
            placeholder="请选择所属资源池"
            style="width: 100%"
            filterable
            :disabled="form.deviceId !== undefined"
          >
            <el-option
              v-for="item in resourceOptions"
              :key="item.resourceId"
              :label="item.resourceName"
              :value="item.resourceId"
            />
          </el-select>
        </el-form-item>
        <el-row>
          <el-col :span="12">
            <el-form-item label="宿主机节点" prop="nodeName">
              <el-input v-model="form.nodeName" placeholder="如 gpu-nj-01" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="节点内槽位" prop="slotIndex">
              <el-input-number
                v-model="form.slotIndex"
                controls-position="right"
                :min="0"
                :max="63"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="可选，如：3号卡风扇更换过" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 批量初始化设备对话框 -->
    <el-dialog title="批量初始化设备" :visible.sync="initOpen" width="500px" append-to-body>
      <el-form ref="initForm" :model="initForm" :rules="initRules" label-width="110px">
        <el-form-item label="资源池" prop="resourceId">
          <el-select v-model="initForm.resourceId" placeholder="请选择资源池" style="width: 100%" filterable>
            <el-option
              v-for="item in resourceOptions"
              :key="item.resourceId"
              :label="item.resourceName + '（' + item.resourceType + '）'"
              :value="item.resourceId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="节点名前缀" prop="nodePrefix">
          <el-input v-model="initForm.nodePrefix" placeholder="如 gpu-nj，将生成 gpu-nj-01、gpu-nj-02" />
        </el-form-item>
        <el-row>
          <el-col :span="12">
            <el-form-item label="节点数量" prop="nodeCount">
              <el-input-number v-model="initForm.nodeCount" controls-position="right" :min="1" :max="64" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="每节点卡数" prop="cardsPerNode">
              <el-input-number v-model="initForm.cardsPerNode" controls-position="right" :min="1" :max="16" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="已有设备" prop="confirm">
          <el-checkbox v-model="initForm.confirm">该池已有设备时确认追加（重复编号会自动跳过）</el-checkbox>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitInitForm">确 定</el-button>
        <el-button @click="initOpen = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  listDevice,
  getDevice,
  addDevice,
  updateDevice,
  batchInitDevice,
  faultDevice,
  maintainDevice,
  onlineDevice,
  delDevice
} from "@/api/bocompute/device"
import { optionselectResource } from "@/api/bocompute/resource"

export default {
  name: "BoComputeDevice",
  // 页面使用到的字典类型：设备状态
  dicts: ['biz_device_status'],
  data() {
    return {
      // 遮罩层
      loading: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 设备表格数据
      deviceList: [],
      // 资源池下拉数据
      resourceOptions: [],
      // 选中行的设备ID
      ids: [],
      // 弹出层标题
      title: "",
      // 是否显示注册/修改弹出层
      open: false,
      // 是否显示批量初始化弹出层
      initOpen: false,
      // 表单参数
      form: {},
      // 批量初始化表单参数
      initForm: {
        resourceId: undefined,
        nodePrefix: "gpu-nj",
        nodeCount: 1,
        cardsPerNode: 8,
        confirm: false
      },
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        deviceCode: undefined,
        resourceId: undefined,
        nodeName: undefined,
        status: undefined,
        userName: undefined
      },
      // 表单校验规则
      rules: {
        deviceCode: [
          { required: true, message: "设备编号不能为空", trigger: "blur" }
        ],
        resourceId: [
          { required: true, message: "请选择所属资源池", trigger: "change" }
        ],
        nodeName: [
          { required: true, message: "宿主机节点名不能为空", trigger: "blur" }
        ]
      },
      // 批量初始化校验规则
      initRules: {
        resourceId: [
          { required: true, message: "请选择资源池", trigger: "change" }
        ],
        nodePrefix: [
          { required: true, message: "节点名前缀不能为空", trigger: "blur" }
        ]
      }
    }
  },
  created() {
    // 支持从资源列表页带 resourceId 跳转过来，先同步查询条件再拉列表
    const resourceId = this.$route.query.resourceId
    if (resourceId) {
      this.queryParams.resourceId = Number(resourceId)
    }
    this.getResourceOptions()
    this.getList()
  },
  // 页面被缓存后再次进入（例如从资源列表跳转）仍需同步查询条件并刷新列表
  activated() {
    const resourceId = this.$route.query.resourceId
    if (resourceId) {
      this.queryParams.resourceId = Number(resourceId)
    }
    this.getList()
  },
  methods: {
    /** 查询算力设备列表 */
    getList() {
      this.loading = true
      listDevice(this.queryParams).then(response => {
        this.deviceList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    /** 查询资源池下拉数据 */
    getResourceOptions() {
      optionselectResource().then(response => {
        // CPU 资源按核数扣减，不注册设备，下拉里不展示
        this.resourceOptions = (response.data || []).filter(item => item.resourceType !== 'cpu')
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
        deviceId: undefined,
        deviceCode: undefined,
        resourceId: undefined,
        nodeName: undefined,
        slotIndex: 0,
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
      this.ids = selection.map(item => item.deviceId)
    },
    /** 注册设备按钮操作 */
    handleAdd() {
      this.reset()
      this.title = "注册算力设备"
      this.open = true
    },
    /** 修改设备按钮操作 */
    handleUpdate(row) {
      this.reset()
      getDevice(row.deviceId).then(response => {
        this.form = response.data
        this.title = "修改算力设备"
        this.open = true
      })
    },
    /** 提交注册/修改表单 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.deviceId !== undefined) {
            updateDevice(this.form).then(() => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addDevice(this.form).then(() => {
              this.$modal.msgSuccess("注册成功")
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    /** 批量初始化按钮操作 */
    handleBatchInit() {
      this.initForm = {
        resourceId: this.queryParams.resourceId,
        nodePrefix: "gpu-nj",
        nodeCount: 1,
        cardsPerNode: 8,
        confirm: false
      }
      this.initOpen = true
    },
    /** 提交批量初始化 */
    submitInitForm() {
      this.$refs["initForm"].validate(valid => {
        if (valid) {
          batchInitDevice(this.initForm).then(response => {
            this.$modal.msgSuccess("批量初始化完成，新增设备 " + (response.data || 0) + " 台")
            this.initOpen = false
            this.getList()
          })
        }
      })
    },
    /** 设为故障操作 */
    handleFault(row) {
      this.$modal.confirm('是否确认将设备"' + row.deviceCode + '"设为故障？').then(function() {
        return faultDevice(row.deviceId)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("已设为故障")
      }).catch(() => {})
    },
    /** 设为维护操作 */
    handleMaintain(row) {
      this.$modal.confirm('是否确认将设备"' + row.deviceCode + '"设为维护？').then(function() {
        return maintainDevice(row.deviceId)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("已设为维护")
      }).catch(() => {})
    },
    /** 设备上线操作 */
    handleOnline(row) {
      this.$modal.confirm('是否确认将设备"' + row.deviceCode + '"上线？').then(function() {
        return onlineDevice(row.deviceId)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("已上线")
      }).catch(() => {})
    },
    /** 删除设备操作 */
    handleDelete(row) {
      const deviceIds = row.deviceId || this.ids
      this.$modal.confirm('是否确认删除设备编号为"' + (row.deviceCode || deviceIds) + '"的数据项？').then(function() {
        return delDevice(deviceIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('bocompute/device/export', {
        ...this.queryParams
      }, `device_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
