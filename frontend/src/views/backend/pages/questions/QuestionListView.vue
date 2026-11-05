<!-- 后台题目列表页面 -->
<template>
  <section class="page">
    <header class="page__header">
      <h2 class="page__title">题目列表</h2>
      <el-button type="primary" :icon="Plus" @click="goCreate">新建题目</el-button>
    </header>

    <el-form class="page__filters" :inline="true" @submit.prevent>
      <el-form-item label="标题">
        <el-input
          v-model="filters.title"
          placeholder="标题模糊搜索"
          clearable
          class="page__input"
        />
      </el-form-item>
      <el-form-item label="难度">
        <el-select v-model="filters.difficulty" placeholder="全部" clearable class="page__select">
          <el-option
            v-for="option in DIFFICULTY_OPTIONS"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="判题模式">
        <el-select v-model="filters.judgeMode" placeholder="全部" clearable class="page__select">
          <el-option
            v-for="option in JUDGE_MODE_OPTIONS"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="filters.status" placeholder="全部" clearable class="page__select">
          <el-option
            v-for="option in STATUS_OPTIONS"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="来源">
        <el-input
          v-model="filters.source"
          placeholder="来源模糊搜索"
          clearable
          class="page__input"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="search">查询</el-button>
        <el-button @click="resetFilters">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table
      v-loading="loading"
      :data="rows"
      class="page__table"
      border
      @sort-change="handleSortChange"
    >
      <el-table-column prop="id" label="ID" width="170" show-overflow-tooltip sortable="custom" />
      <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
      <el-table-column label="难度" width="80">
        <template #default="{ row }">
          <el-tag :type="difficultyTagType(row.difficulty)">
            {{ difficultyLabel(row.difficulty) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="判题模式" width="115">
        <template #default="{ row }">{{ judgeModeLabel(row.judgeMode) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="85">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="通过/提交" width="130">
        <template #default="{ row }">{{ row.acceptedCount }} / {{ row.submitCount }}</template>
      </el-table-column>
      <el-table-column label="来源" min-width="130" show-overflow-tooltip>
        <template #default="{ row }">{{ row.source || '-' }}</template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" sortable="custom">
        <template #default="{ row }">{{ row.createTime || '-' }}</template>
      </el-table-column>
      <el-table-column prop="updateTime" label="更新时间" width="170" sortable="custom">
        <template #default="{ row }">{{ row.updateTime || '-' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="goEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="removeQuestion(row)">删除</el-button>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="暂无数据" :image-size="80" />
      </template>
    </el-table>

    <el-pagination
      v-model:current-page="pageNum"
      v-model:page-size="pageSize"
      class="page__pagination"
      :total="total"
      :page-sizes="PAGE_SIZES"
      layout="total, sizes, prev, pager, next"
      @current-change="loadQuestions"
      @size-change="handleSizeChange"
    />
  </section>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@lucide/vue'
import {
  deleteQuestion,
  pageQuestions,
  type QuestionQuery,
  type QuestionVO,
} from '@/api/backend/question'

// 难度选项：1 简单，2 中等，3 困难
const DIFFICULTY_OPTIONS = [
  { value: 1, label: '简单' },
  { value: 2, label: '中等' },
  { value: 3, label: '困难' },
] as const

// 判题模式选项：1 标准输入输出，2 核心代码
const JUDGE_MODE_OPTIONS = [
  { value: 1, label: '标准输入输出' },
  { value: 2, label: '核心代码' },
] as const

// 状态选项：0 草稿，1 已发布，2 已下架
const STATUS_OPTIONS = [
  { value: 0, label: '草稿' },
  { value: 1, label: '已发布' },
  { value: 2, label: '已下架' },
] as const

// 可选每页条数
const PAGE_SIZES = [10, 20, 50, 100]

const loading = ref(false)
const rows = ref<QuestionVO[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const router = useRouter()

// 跳转新建题目页
function goCreate() {
  router.push({ name: 'backend-question-new' })
}

// 跳转编辑题目页
function goEdit(row: QuestionVO) {
  router.push({ name: 'backend-question-edit', params: { id: row.id } })
}

const filters = reactive<{
  title: string
  difficulty?: number
  judgeMode?: number
  status?: number
  source: string
}>({
  title: '',
  difficulty: undefined,
  judgeMode: undefined,
  status: undefined,
  source: '',
})

// 当前排序字段与方向，空表示默认排序
const sortField = ref('')
const sortOrder = ref<'asc' | 'desc' | ''>('')

// 难度对应的标签类型
function difficultyTagType(difficulty: number): 'success' | 'warning' | 'danger' {
  if (difficulty === 1) return 'success'
  if (difficulty === 2) return 'warning'
  return 'danger'
}

// 难度文案
function difficultyLabel(difficulty: number): string {
  return DIFFICULTY_OPTIONS.find((option) => option.value === difficulty)?.label ?? '-'
}

// 判题模式文案
function judgeModeLabel(judgeMode: number): string {
  return JUDGE_MODE_OPTIONS.find((option) => option.value === judgeMode)?.label ?? '-'
}

// 状态文案
function statusLabel(status: number): string {
  return STATUS_OPTIONS.find((option) => option.value === status)?.label ?? '-'
}

// 状态对应的标签类型
function statusTagType(status: number): 'success' | 'info' | 'danger' {
  if (status === 1) return 'success'
  if (status === 2) return 'danger'
  return 'info'
}

// 表头排序变化后回到第一页重新查询
function handleSortChange({
  prop,
  order,
}: {
  prop: string | null
  order: 'ascending' | 'descending' | null
}) {
  sortField.value = order ? (prop ?? '') : ''
  sortOrder.value = order === 'ascending' ? 'asc' : order === 'descending' ? 'desc' : ''
  pageNum.value = 1
  loadQuestions()
}

// 加载当前页题目，返回本次查询的命中总数
async function loadQuestions(): Promise<number> {
  loading.value = true
  try {
    const query: QuestionQuery = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      title: filters.title || undefined,
      difficulty: filters.difficulty,
      judgeMode: filters.judgeMode,
      status: filters.status,
      source: filters.source || undefined,
      sortField: sortField.value || undefined,
      sortOrder: sortOrder.value || undefined,
    }
    const page = await pageQuestions(query)
    rows.value = page.records
    total.value = page.total
    return page.total
  } finally {
    loading.value = false
  }
}

// 筛选条件变化后自动查询，输入框防抖避免每次按键都请求
let filterTimer: ReturnType<typeof setTimeout> | null = null

watch(
  () => [filters.title, filters.difficulty, filters.judgeMode, filters.status, filters.source],
  () => {
    if (filterTimer) clearTimeout(filterTimer)
    filterTimer = setTimeout(() => {
      pageNum.value = 1
      loadQuestions()
    }, 300)
  },
)

// 条件变更后回到第一页重新查询，并提示命中情况
async function search() {
  pageNum.value = 1
  const hit = await loadQuestions()
  if (hit === 0) {
    ElMessage.warning('没有符合条件的题目')
    return
  }
  ElMessage.success(`查询完成，共 ${hit} 条题目`)
}

// 清空筛选条件并重新查询，只提示已重置
async function resetFilters() {
  // 取消待执行的自动查询，避免与本次重置重复请求
  if (filterTimer) {
    clearTimeout(filterTimer)
    filterTimer = null
  }
  filters.title = ''
  filters.difficulty = undefined
  filters.judgeMode = undefined
  filters.status = undefined
  filters.source = ''
  pageNum.value = 1
  await loadQuestions()
  ElMessage.success('筛选条件已重置')
}

// 切换每页条数后回到第一页
function handleSizeChange() {
  pageNum.value = 1
  loadQuestions()
}

// 删除题目，二次确认后执行
async function removeQuestion(row: QuestionVO) {
  try {
    await ElMessageBox.confirm(`确定删除题目「${row.title}」吗？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
  } catch {
    // 用户取消删除，不做处理。
    return
  }
  await deleteQuestion(row.id)
  ElMessage.success('删除成功')
  // 删掉当前页最后一条时回退一页，避免停在空页
  if (rows.value.length === 1 && pageNum.value > 1) pageNum.value -= 1
  loadQuestions()
}

onMounted(loadQuestions)

onBeforeUnmount(() => {
  if (filterTimer) clearTimeout(filterTimer)
})
</script>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.page__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.page__title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.page__filters {
  display: flex;
  flex-wrap: wrap;
  gap: 4px 0;
}

.page__input {
  width: 170px;
}

.page__select {
  width: 140px;
}

.page__pagination {
  justify-content: flex-end;
}
</style>
