<!-- 后台评测机运行状态页面 -->
<template>
  <section class="judge-status">
    <header class="judge-status__header">
      <h1 class="judge-status__title">运行状态</h1>
      <div class="judge-status__header-actions">
        <el-select
          v-model="machineId"
          class="judge-status__machine"
          placeholder="选择评测机"
          @change="loadMetrics"
        >
          <el-option
            v-for="machine in machines"
            :key="machine.id"
            :label="machine.name"
            :value="machine.id"
          />
        </el-select>
        <el-button
          :icon="RefreshCw"
          :loading="loading"
          :disabled="!machineId"
          @click="loadMetrics"
        >
          刷新
        </el-button>
      </div>
    </header>

    <el-alert
      v-if="snapshot && !snapshot.available"
      class="judge-status__alert"
      type="warning"
      show-icon
      :closable="false"
      :title="snapshot.message ?? '指标不可用'"
    />

    <template v-if="snapshot?.available">
      <p class="judge-status__meta">
        抓取于 {{ fetchedAt }}，耗时 {{ snapshot.elapsed }} ms，共 {{ snapshot.metrics?.length ?? 0 }}
        项指标
      </p>

      <div class="judge-status__cards">
        <div v-for="card in summaryCards" :key="card.label" class="judge-status__card">
          <span class="judge-status__card-label">{{ card.label }}</span>
          <span class="judge-status__card-value">{{ card.value }}</span>
        </div>
      </div>

      <el-table :data="tableRows" border class="judge-status__table">
        <el-table-column prop="name" label="指标" min-width="260" show-overflow-tooltip />
        <el-table-column prop="labels" label="标签" min-width="200" show-overflow-tooltip />
        <el-table-column prop="value" label="数值" width="160" align="right" />
        <el-table-column prop="help" label="说明" min-width="260" show-overflow-tooltip />
        <template #empty>
          <el-empty description="没有采集到评测机指标" :image-size="70" />
        </template>
      </el-table>
    </template>

    <el-empty
      v-else-if="!loading && !snapshot"
      description="选择评测机查看运行指标"
      :image-size="80"
    />
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { RefreshCw } from '@lucide/vue'
import { pageJudgeMachines, type JudgeMachineVO } from '@/api/backend/judgeMachine'
import { getJudgeMetrics, type JudgeMetricsVO } from '@/api/backend/judgeMetrics'

// 指标名到中文说明，按 go-judge v1.14 实际指标名收录
const METRIC_LABELS: Record<string, string> = {
  'go_judge_worker_running_count': '运行中任务',
  'go_judge_worker_queue_count': '排队中任务',
  'go_judge_environment_current_count': '使用中沙箱',
  'go_judge_environment_count': '沙箱创建总数',
  'go_judge_exec_error_count': '执行错误总数',
  'go_judge_file_current_bytes_count': '缓存文件数',
  'go_judge_file_current_bytes_sum': '缓存文件总字节',
}

const machines = ref<JudgeMachineVO[]>([])
const machineId = ref<string>('')
const snapshot = ref<JudgeMetricsVO | null>(null)
const loading = ref(false)
const fetchedAt = ref<string>('')

// 表格行，标签拼成可读文本
const tableRows = computed(() => {
  const metrics = snapshot.value?.metrics ?? []
  return metrics.map((metric) => {
    const entries = Object.entries(metric.labels)
    return {
      name: metric.name,
      labels: entries.length ? entries.map(([key, value]) => `${key}=${value}`).join(', ') : '-',
      value: formatNumber(metric.value),
      help: metric.help ?? '-',
    }
  })
})

// 顶部卡片，取无标签的汇总指标，具体有哪些由评测机决定
const summaryCards = computed(() => {
  const metrics = snapshot.value?.metrics ?? []
  return metrics
    .filter((metric) => Object.keys(metric.labels).length === 0)
    .map((metric) => ({
      label: METRIC_LABELS[metric.name] ?? metric.name,
      value: formatNumber(metric.value),
    }))
})

// 数值只保留两位小数，整数直接展示
function formatNumber(value: number | null): string {
  if (value === null) return '-'
  return Number.isInteger(value) ? String(value) : value.toFixed(2)
}

// 加载评测机列表，默认选中第一台启用的机器
async function loadMachines() {
  const page = await pageJudgeMachines({ pageNum: 1, pageSize: 100 })
  machines.value = page.records
  const first = page.records.find((item) => item.status === 1)
  if (first) {
    machineId.value = first.id
    loadMetrics()
  }
}

// 抓取所选评测机的指标
async function loadMetrics() {
  if (!machineId.value) return
  loading.value = true
  try {
    snapshot.value = await getJudgeMetrics(machineId.value)
    fetchedAt.value = new Date().toLocaleTimeString('zh-CN', { hour12: false })
  } finally {
    loading.value = false
  }
}

onMounted(loadMachines)
</script>

<style scoped>
.judge-status {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.judge-status__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.judge-status__title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
}

.judge-status__header-actions {
  display: flex;
  gap: 8px;
}

.judge-status__machine {
  width: 200px;
}

.judge-status__meta {
  margin: 0;
  color: var(--backend-muted);
  font-size: 13px;
}

.judge-status__cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 12px;
}

.judge-status__card {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 16px;
  border: 1px solid var(--backend-border);
  border-radius: 8px;
}

.judge-status__card-label {
  color: var(--backend-muted);
  font-size: 13px;
}

.judge-status__card-value {
  font-size: 22px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
}
</style>
