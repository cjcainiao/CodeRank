<!-- 后台提交列表页面 -->
<template>
  <section class="submission">
    <header class="submission__header">
      <h1 class="submission__title">提交列表</h1>
    </header>

    <div class="submission__toolbar">
      <el-select v-model="filters.status" class="submission__select" placeholder="判题结果" clearable>
        <el-option
          v-for="option in STATUS_OPTIONS"
          :key="option.value"
          :label="option.label"
          :value="option.value"
        />
      </el-select>
      <el-select v-model="filters.language" class="submission__select" placeholder="语言" clearable>
        <el-option
          v-for="option in LANGUAGE_OPTIONS"
          :key="option.value"
          :label="option.label"
          :value="option.value"
        />
      </el-select>
    </div>

    <el-table v-loading="loading" :data="rows" border class="submission__table">
      <el-table-column prop="id" label="提交 ID" width="180" show-overflow-tooltip />
      <el-table-column label="题目" min-width="180" show-overflow-tooltip>
        <template #default="{ row }">{{ row.questionTitle ?? row.questionId }}</template>
      </el-table-column>
      <el-table-column label="语言" width="110">
        <template #default="{ row }">{{ languageLabel(row.language) }}</template>
      </el-table-column>
      <el-table-column label="结果" width="140">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="测试点" width="100" align="center">
        <template #default="{ row }">{{ row.passedCount }} / {{ row.totalCount }}</template>
      </el-table-column>
      <el-table-column label="耗时" width="95" align="right">
        <template #default="{ row }">{{ row.timeUsed }} ms</template>
      </el-table-column>
      <el-table-column label="内存" width="105" align="right">
        <template #default="{ row }">{{ row.memoryUsed }} KB</template>
      </el-table-column>
      <el-table-column label="提交时间" width="170">
        <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="100" align="center" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row)">详情</el-button>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="还没有提交记录" :image-size="70" />
      </template>
    </el-table>

    <el-pagination
      v-if="total > 0"
      v-model:current-page="pageNum"
      v-model:page-size="pageSize"
      class="submission__pagination"
      :total="total"
      :page-sizes="PAGE_SIZES"
      layout="total, sizes, prev, pager, next"
      background
      @current-change="loadSubmissions"
      @size-change="handleSizeChange"
    />

    <el-dialog v-model="detailVisible" title="提交详情" width="760px" destroy-on-close>
      <template v-if="current">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="提交 ID">{{ current.id }}</el-descriptions-item>
          <el-descriptions-item label="题目">
            {{ current.questionTitle ?? current.questionId }}
          </el-descriptions-item>
          <el-descriptions-item label="语言">{{ languageLabel(current.language) }}</el-descriptions-item>
          <el-descriptions-item label="结果">
            <el-tag :type="statusTagType(current.status)">{{ statusLabel(current.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="测试点">
            {{ current.passedCount }} / {{ current.totalCount }}
          </el-descriptions-item>
          <el-descriptions-item label="耗时 / 内存">
            {{ current.timeUsed }} ms / {{ current.memoryUsed }} KB
          </el-descriptions-item>
          <el-descriptions-item label="评测机">{{ current.judgeMachineId ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ formatTime(current.createTime) }}</el-descriptions-item>
        </el-descriptions>

        <template v-if="current.errorMessage">
          <h3 class="submission__section">错误信息</h3>
          <pre class="submission__error">{{ current.errorMessage }}</pre>
        </template>

        <h3 class="submission__section">提交代码</h3>
        <pre class="submission__code">{{ current.sourceCode }}</pre>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import { pageSubmissions, type SubmissionVO } from '@/api/backend/submission'

// 可选每页条数
const PAGE_SIZES = [10, 20, 50]

// 判题结果选项，与后端 submission.status 一致
const STATUS_OPTIONS = [
  { value: 0, label: '等待' },
  { value: 8, label: '评测中' },
  { value: 1, label: '通过' },
  { value: 2, label: '答案错误' },
  { value: 3, label: '超出时间限制' },
  { value: 4, label: '超出内存限制' },
  { value: 5, label: '运行错误' },
  { value: 6, label: '编译错误' },
  { value: 7, label: '系统错误' },
]

// 语言选项，与后端 LanguageEnum 的 code 对应
const LANGUAGE_OPTIONS = [
  { value: 'java', label: 'Java' },
  { value: 'cpp', label: 'C++' },
  { value: 'python', label: 'Python' },
  { value: 'go', label: 'Go' },
  { value: 'javascript', label: 'JavaScript' },
]

const loading = ref(false)
const rows = ref<SubmissionVO[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const detailVisible = ref(false)
const current = ref<SubmissionVO | null>(null)

const filters = reactive<{ status?: number; language?: string }>({
  status: undefined,
  language: undefined,
})

// 状态码转文案
function statusLabel(status: number): string {
  return STATUS_OPTIONS.find((option) => option.value === status)?.label ?? `未知(${status})`
}

// 状态码转标签配色
function statusTagType(status: number): 'success' | 'danger' | 'warning' | 'info' {
  if (status === 1) return 'success'
  if (status === 0 || status === 8) return 'info'
  if (status === 3 || status === 4 || status === 7) return 'warning'
  return 'danger'
}

// 语言标识转文案
function languageLabel(language: string): string {
  return LANGUAGE_OPTIONS.find((option) => option.value === language)?.label ?? language
}

// 时间只保留到秒
function formatTime(value: string): string {
  return value ? value.replace('T', ' ').slice(0, 19) : '-'
}

// 加载当前页提交记录
async function loadSubmissions() {
  loading.value = true
  try {
    const page = await pageSubmissions({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      status: filters.status,
      language: filters.language || undefined,
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
  loadSubmissions()
}

// 打开详情弹窗
function openDetail(row: SubmissionVO) {
  current.value = row
  detailVisible.value = true
}

// 筛选条件变化后自动查询，避免每次按键都请求
let filterTimer: ReturnType<typeof setTimeout> | null = null

watch(
  () => [filters.status, filters.language],
  () => {
    if (filterTimer) clearTimeout(filterTimer)
    filterTimer = setTimeout(() => {
      pageNum.value = 1
      loadSubmissions()
    }, 300)
  },
)

onMounted(loadSubmissions)
</script>

<style scoped>
.submission {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.submission__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.submission__title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
}

.submission__toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.submission__select {
  width: 150px;
}

.submission__pagination {
  justify-content: flex-end;
}

.submission__section {
  margin: 18px 0 8px;
  font-size: 14px;
  font-weight: 600;
}

.submission__code,
.submission__error {
  margin: 0;
  padding: 12px;
  max-height: 320px;
  overflow: auto;
  border-radius: 6px;
  background: var(--backend-active);
  font-family: var(--code-font-family);
  font-size: 13px;
  line-height: 1.6;
  white-space: pre-wrap;
  overflow-wrap: anywhere;
}

.submission__error {
  background: color-mix(in srgb, var(--status-danger) 10%, var(--backend-surface));
  color: var(--status-danger);
}
</style>
