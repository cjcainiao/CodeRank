<!-- 后台评测机管理页面 -->
<template>
  <section class="judge-machine">
    <header class="judge-machine__header">
      <h1 class="judge-machine__title">评测机管理</h1>
      <div class="judge-machine__header-actions">
        <el-button :icon="Activity" :loading="checkingAll" @click="checkAll">一键探测</el-button>
        <el-button type="primary" :icon="Plus" @click="openCreate">新增评测机</el-button>
      </div>
    </header>

    <div class="judge-machine__toolbar">
      <el-input
        v-model="filters.name"
        class="judge-machine__search"
        placeholder="搜索机器名称"
        clearable
        :prefix-icon="Search"
      />
      <el-select v-model="filters.status" class="judge-machine__select" placeholder="状态" clearable>
        <el-option label="启用" :value="1" />
        <el-option label="停用" :value="0" />
      </el-select>
    </div>

    <el-table v-loading="loading" :data="rows" border class="judge-machine__table">
      <el-table-column prop="name" label="机器名称" min-width="140" show-overflow-tooltip />
      <el-table-column prop="endpoint" label="服务地址" min-width="220" show-overflow-tooltip />
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="weight" label="权重" width="80" align="center" />
      <el-table-column prop="maxParallel" label="最大并发" width="100" align="center" />
      <el-table-column label="资源限制" min-width="200">
        <template #default="{ row }">
          <span class="judge-machine__limits">
            {{ formatDuration(row.cpuLimit) }} / {{ formatBytes(row.memoryLimit) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="最后探测" min-width="160">
        <template #default="{ row }">
          <span v-if="!row.lastCheckTime" class="judge-machine__muted">未探测</span>
          <span v-else class="judge-machine__muted">
            {{ formatTime(row.lastCheckTime) }}
            <el-tag
              class="judge-machine__check"
              size="small"
              :type="row.lastCheckOk === 1 ? 'success' : 'danger'"
            >
              {{ row.lastCheckOk === 1 ? '在线' : '离线' }}
            </el-tag>
          </span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" align="center" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" :loading="checkingId === row.id" @click="checkMachine(row)">
            测试
          </el-button>
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="removeMachine(row)">删除</el-button>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="还没有配置评测机" :image-size="70" />
      </template>
    </el-table>

    <el-pagination
      v-if="total > 0"
      v-model:current-page="pageNum"
      v-model:page-size="pageSize"
      class="judge-machine__pagination"
      :total="total"
      :page-sizes="PAGE_SIZES"
      layout="total, sizes, prev, pager, next"
      background
      @current-change="loadMachines"
      @size-change="handleSizeChange"
    />

    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '编辑评测机' : '新增评测机'"
      width="640px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="机器名称" prop="name">
          <el-input v-model="form.name" placeholder="如 主评测机" maxlength="100" />
        </el-form-item>
        <el-form-item label="服务地址" prop="endpoint">
          <el-input v-model="form.endpoint" placeholder="如 http://127.0.0.1:5050" maxlength="255" />
        </el-form-item>
        <el-form-item label="指标地址">
          <el-input
            v-model="form.metricsEndpoint"
            placeholder="如 http://127.0.0.1:5052，未开启监控时留空"
            maxlength="255"
          />
        </el-form-item>
        <el-form-item label="认证令牌">
          <el-input
            v-model="form.authToken"
            placeholder="未启用认证时留空"
            maxlength="255"
            show-password
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-divider content-position="left">调度配置</el-divider>

        <el-form-item label="权重">
          <el-input-number v-model="form.weight" :min="1" :max="100" />
          <span class="judge-machine__hint">值越大分到的任务越多</span>
        </el-form-item>
        <el-form-item label="最大并发">
          <el-input-number v-model="form.maxParallel" :min="1" :max="128" />
        </el-form-item>

        <el-divider content-position="left">资源限制</el-divider>

        <el-form-item label="CPU 上限">
          <el-input-number v-model="form.cpuLimitMs" :min="100" :max="60000" :step="500" />
          <span class="judge-machine__hint">毫秒</span>
        </el-form-item>
        <el-form-item label="墙钟上限">
          <el-input-number v-model="form.clockLimitMs" :min="100" :max="120000" :step="500" />
          <span class="judge-machine__hint">毫秒</span>
        </el-form-item>
        <el-form-item label="内存上限">
          <el-input-number v-model="form.memoryLimitMb" :min="16" :max="4096" :step="16" />
          <span class="judge-machine__hint">MB</span>
        </el-form-item>
        <el-form-item label="进程上限">
          <el-input-number v-model="form.procLimit" :min="1" :max="1024" />
        </el-form-item>
        <el-form-item label="输出上限">
          <el-input-number v-model="form.outputLimitKb" :min="1" :max="10240" :step="64" />
          <span class="judge-machine__hint">KB</span>
        </el-form-item>

        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" maxlength="255" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Search, Activity } from '@lucide/vue'
import {
  checkAllJudgeMachines,
  checkJudgeMachine,
  createJudgeMachine,
  deleteJudgeMachine,
  pageJudgeMachines,
  updateJudgeMachine,
  type JudgeMachineVO,
} from '@/api/backend/judgeMachine'

// 可选每页条数
const PAGE_SIZES = [10, 20, 50]

// 状态：启用
const STATUS_ENABLED = 1

const loading = ref(false)
const submitting = ref(false)
// 正在探测的行 id，用于按钮加载态
const checkingId = ref<string | null>(null)
// 是否正在一键探测
const checkingAll = ref(false)
const rows = ref<JudgeMachineVO[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const dialogVisible = ref(false)
const editingId = ref<string | null>(null)
const formRef = ref<FormInstance>()

const filters = reactive<{ name: string; status?: number }>({
  name: '',
  status: undefined,
})

// 表单内部用毫秒、MB、KB 展示，提交前换算成后端要求的单位
const form = ref({
  name: '',
  endpoint: '',
  metricsEndpoint: '',
  authToken: '',
  status: STATUS_ENABLED,
  weight: 1,
  maxParallel: 4,
  cpuLimitMs: 5000,
  clockLimitMs: 10000,
  memoryLimitMb: 256,
  procLimit: 64,
  outputLimitKb: 1024,
  remark: '',
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入机器名称', trigger: 'blur' }],
  endpoint: [{ required: true, message: '请输入服务地址', trigger: 'blur' }],
}

// 纳秒转毫秒展示
function formatDuration(nanoseconds: number): string {
  return `${Math.round(nanoseconds / 1_000_000)} ms`
}

// 字节转 MB 展示
function formatBytes(bytes: number): string {
  return `${Math.round(bytes / 1024 / 1024)} MB`
}

// 时间只保留到秒
function formatTime(value: string): string {
  return value.replace('T', ' ').slice(0, 19)
}

// 加载当前页评测机
async function loadMachines() {
  loading.value = true
  try {
    const page = await pageJudgeMachines({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      name: filters.name || undefined,
      status: filters.status,
    })
    rows.value = page.records
    total.value = page.total
  } finally {
    loading.value = false
  }
}

// 每页条数变化后回到第一页
function handleSizeChange() {
  pageNum.value = 1
  loadMachines()
}

// 重置表单为默认值
function resetForm() {
  form.value.name = ''
  form.value.endpoint = ''
  form.value.metricsEndpoint = ''
  form.value.authToken = ''
  form.value.status = STATUS_ENABLED
  form.value.weight = 1
  form.value.maxParallel = 4
  form.value.cpuLimitMs = 5000
  form.value.clockLimitMs = 10000
  form.value.memoryLimitMb = 256
  form.value.procLimit = 64
  form.value.outputLimitKb = 1024
  form.value.remark = ''
}

// 打开新增弹窗
function openCreate() {
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

// 打开编辑弹窗，把后端单位换算回表单单位
function openEdit(row: JudgeMachineVO) {
  editingId.value = row.id
  form.value.name = row.name
  form.value.endpoint = row.endpoint
  form.value.metricsEndpoint = row.metricsEndpoint ?? ''
  form.value.authToken = ''
  form.value.status = row.status
  form.value.weight = row.weight
  form.value.maxParallel = row.maxParallel
  form.value.cpuLimitMs = Math.round(row.cpuLimit / 1_000_000)
  form.value.clockLimitMs = Math.round(row.clockLimit / 1_000_000)
  form.value.memoryLimitMb = Math.round(row.memoryLimit / 1024 / 1024)
  form.value.procLimit = row.procLimit
  form.value.outputLimitKb = Math.round(row.outputLimit / 1024)
  form.value.remark = row.remark ?? ''
  dialogVisible.value = true
}

// 保存，新增与修改共用同一弹窗
async function submit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const payload = {
      name: form.value.name,
      endpoint: form.value.endpoint,
      metricsEndpoint: form.value.metricsEndpoint || undefined,
      authToken: form.value.authToken || undefined,
      status: form.value.status,
      weight: form.value.weight,
      maxParallel: form.value.maxParallel,
      cpuLimit: form.value.cpuLimitMs * 1_000_000,
      clockLimit: form.value.clockLimitMs * 1_000_000,
      memoryLimit: form.value.memoryLimitMb * 1024 * 1024,
      procLimit: form.value.procLimit,
      outputLimit: form.value.outputLimitKb * 1024,
      remark: form.value.remark || undefined,
    }

    if (editingId.value) {
      await updateJudgeMachine({ id: editingId.value, ...payload })
      ElMessage.success('修改成功')
    } else {
      await createJudgeMachine(payload)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadMachines()
  } finally {
    submitting.value = false
  }
}

// 探测评测机是否可用，结果直接提示并刷新列表
async function checkMachine(row: JudgeMachineVO) {
  checkingId.value = row.id
  try {
    const result = await checkJudgeMachine(row.id)
    if (result.ok) {
      ElMessage.success(
        `连接正常（${result.version}，${result.os}/${result.platform}，耗时 ${result.elapsed} ms）`,
      )
    } else {
      ElMessage.error(`连接失败：${result.message ?? '未知原因'}`)
    }
    loadMachines()
  } finally {
    checkingId.value = null
  }
}

// 一键探测全部评测机，汇总成功与失败数量
async function checkAll() {
  if (total.value === 0) {
    ElMessage.warning('还没有配置评测机')
    return
  }
  checkingAll.value = true
  try {
    const results = await checkAllJudgeMachines()
    const okCount = results.filter((item) => item.ok).length
    const failCount = results.length - okCount
    if (failCount === 0) {
      ElMessage.success(`全部 ${okCount} 台连接正常`)
    } else {
      ElMessage.warning(`${okCount} 台正常，${failCount} 台失败`)
    }
    loadMachines()
  } finally {
    checkingAll.value = false
  }
}

// 删除评测机，二次确认后执行
async function removeMachine(row: JudgeMachineVO) {
  try {
    await ElMessageBox.confirm(`确定删除评测机「${row.name}」吗？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
  } catch {
    // 用户取消删除，不做处理。
    return
  }
  await deleteJudgeMachine(row.id)
  ElMessage.success('删除成功')
  loadMachines()
}

// 筛选条件变化后自动查询，输入框防抖避免每次按键都请求
let filterTimer: ReturnType<typeof setTimeout> | null = null

watch(
  () => [filters.name, filters.status],
  () => {
    if (filterTimer) clearTimeout(filterTimer)
    filterTimer = setTimeout(() => {
      pageNum.value = 1
      loadMachines()
    }, 300)
  },
)

onMounted(loadMachines)
</script>

<style scoped>
.judge-machine {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.judge-machine__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.judge-machine__title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
}

.judge-machine__header-actions {
  display: flex;
  gap: 8px;
}

.judge-machine__toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.judge-machine__search {
  width: 240px;
}

.judge-machine__select {
  width: 120px;
}

.judge-machine__limits {
  font-family: 'JetBrains Mono', 'Cascadia Code', Consolas, monospace;
  font-size: 12px;
}

.judge-machine__muted {
  color: var(--backend-muted);
  font-size: 13px;
}

.judge-machine__check {
  margin-left: 6px;
}

.judge-machine__pagination {
  justify-content: flex-end;
}

.judge-machine__hint {
  margin-left: 10px;
  color: var(--backend-muted);
  font-size: 12px;
}
</style>
