<!-- 题库页面 -->
<template>
  <section class="question-bank">
    <header class="question-bank__header">
      <h1 class="question-bank__title">题库</h1>
      <p class="question-bank__subtitle">共 {{ total }} 道题目</p>
    </header>

    <div class="question-bank__toolbar">
      <el-input
        v-model="filters.title"
        class="question-bank__search"
        placeholder="搜索题目"
        clearable
        :prefix-icon="Search"
      />
      <el-select v-model="filters.difficulty" class="question-bank__select" placeholder="难度" clearable>
        <el-option
          v-for="option in DIFFICULTY_OPTIONS"
          :key="option.value"
          :label="option.label"
          :value="option.value"
        />
      </el-select>
      <el-select v-model="filters.tagId" class="question-bank__select" placeholder="标签" clearable>
        <el-option v-for="tag in tagOptions" :key="tag.id" :label="tag.name" :value="tag.id" />
      </el-select>
      <el-select v-model="filters.sortField" class="question-bank__select" placeholder="排序">
        <el-option
          v-for="option in SORT_OPTIONS"
          :key="option.value"
          :label="option.label"
          :value="option.value"
        />
      </el-select>
    </div>

    <div class="question-bank__table">
      <div class="question-header">
        <span class="question-header__title">题目</span>
        <span class="question-header__rate">通过率</span>
        <span class="question-header__difficulty">难度</span>
      </div>

      <div v-loading="loading" class="question-bank__list">
        <RouterLink
          v-for="(item, index) in rows"
          :key="item.id"
          class="question-item"
          :to="{ name: 'problem-solve', params: { id: item.id } }"
        >
          <span class="question-item__index">{{ rowNumber(index) }}.</span>
          <span class="question-item__title">{{ item.title }}</span>
          <span class="question-item__rate">{{ formatRate(item) }}</span>
          <span class="question-item__difficulty" :class="difficultyClass(item.difficulty)">
            {{ difficultyLabel(item.difficulty) }}
          </span>
        </RouterLink>

        <el-empty v-if="!loading && rows.length === 0" description="没有符合条件的题目" />
      </div>
    </div>

    <el-pagination
      v-if="total > 0"
      v-model:current-page="pageNum"
      v-model:page-size="pageSize"
      class="question-bank__pagination"
      :total="total"
      :page-sizes="PAGE_SIZES"
      layout="prev, pager, next"
      background
      @current-change="loadQuestions"
    />
  </section>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { Search } from '@lucide/vue'
import {
  listTagOptions,
  pageQuestionBank,
  type QuestionListItemVO,
  type QuestionListQuery,
  type TagOption,
} from '@/api/home/questionBank'

// 难度选项：1 简单，2 中等，3 困难
const DIFFICULTY_OPTIONS = [
  { value: 1, label: '简单' },
  { value: 2, label: '中等' },
  { value: 3, label: '困难' },
] as const

// 排序选项
const SORT_OPTIONS = [
  { value: 'id', label: '默认' },
  { value: 'submitCount', label: '提交最多' },
  { value: 'acceptedCount', label: '通过最多' },
] as const

// 可选每页条数
const PAGE_SIZES = [10, 20, 50]

const loading = ref(false)
const rows = ref<QuestionListItemVO[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(20)
const tagOptions = ref<TagOption[]>([])

const filters = reactive<{ title: string; difficulty?: number; tagId?: string; sortField: string }>({
  title: '',
  difficulty: undefined,
  tagId: undefined,
  sortField: 'id',
})

// 难度文案
function difficultyLabel(difficulty: number): string {
  return DIFFICULTY_OPTIONS.find((option) => option.value === difficulty)?.label ?? '-'
}

// 难度对应的样式类
function difficultyClass(difficulty: number): string {
  if (difficulty === 1) return 'question-item__difficulty--easy'
  if (difficulty === 2) return 'question-item__difficulty--medium'
  return 'question-item__difficulty--hard'
}

// 行号，跨页连续
function rowNumber(index: number): number {
  return (pageNum.value - 1) * pageSize.value + index + 1
}

// 通过率，无提交时显示占位符
function formatRate(item: QuestionListItemVO): string {
  if (!item.submitCount) return '-'
  const rate = (item.acceptedCount / item.submitCount) * 100
  return `${rate.toFixed(1)}%`
}

// 加载当前页题目
async function loadQuestions() {
  loading.value = true
  try {
    const query: QuestionListQuery = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      title: filters.title || undefined,
      difficulty: filters.difficulty,
      tagId: filters.tagId,
      sortField: filters.sortField,
    }
    const page = await pageQuestionBank(query)
    rows.value = page.records
    total.value = page.total
  } finally {
    loading.value = false
  }
}

// 加载可筛选标签
async function loadTags() {
  tagOptions.value = await listTagOptions()
}

// 筛选条件变化后自动查询，输入框防抖避免每次按键都请求
let filterTimer: ReturnType<typeof setTimeout> | null = null

watch(
  () => [filters.title, filters.difficulty, filters.tagId, filters.sortField],
  () => {
    if (filterTimer) clearTimeout(filterTimer)
    filterTimer = setTimeout(() => {
      pageNum.value = 1
      loadQuestions()
    }, 300)
  },
)

onMounted(() => {
  loadQuestions()
  loadTags()
})

onBeforeUnmount(() => {
  if (filterTimer) clearTimeout(filterTimer)
})
</script>

<style scoped>
.question-bank {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 40px 0 60px;
}

.question-bank__header {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.question-bank__title {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
}

.question-bank__subtitle {
  margin: 0;
  color: var(--home-navbar-muted);
  font-size: 14px;
}

.question-bank__toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.question-bank__search {
  width: 260px;
}

.question-bank__select {
  width: 130px;
}

.question-bank__table {
  display: flex;
  flex-direction: column;
}

/* 表头与数据行共用同一套列宽，保证左右对齐 */
.question-header {
  display: grid;
  grid-template-columns: 44px minmax(0, 1fr) 90px 60px;
  align-items: center;
  gap: 8px;
  padding: 10px 8px;
  border-bottom: 1px solid var(--home-popover-border);
  color: var(--home-navbar-muted);
  font-size: 12px;
  font-weight: 500;
}

/* 题目列从序号右侧开始，通过率与难度各自落到数据行对应的列 */
.question-header__title {
  grid-column: 2;
}

.question-header__rate {
  grid-column: 3;
  text-align: right;
}

.question-header__difficulty {
  grid-column: 4;
  text-align: right;
}

.question-bank__list {
  display: flex;
  flex-direction: column;
  min-height: 200px;
}

/* 题目行：序号 + 标题占满左侧，通过率与难度固定宽度靠右 */
.question-item {
  display: grid;
  grid-template-columns: 44px minmax(0, 1fr) 90px 60px;
  align-items: center;
  gap: 8px;
  padding: 14px 8px;
  border-bottom: 1px solid var(--home-popover-border);
  color: inherit;
  text-decoration: none;
  transition: background-color 140ms ease;
}

.question-item:hover {
  background: var(--home-popover-soft);
}

.question-item__index {
  color: var(--home-navbar-muted);
  font-size: 14px;
  text-align: right;
}

.question-item__title {
  overflow: hidden;
  font-size: 14px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.question-item__rate {
  color: var(--home-navbar-muted);
  font-size: 13px;
  text-align: right;
}

.question-item__difficulty {
  font-size: 13px;
  font-weight: 500;
  text-align: right;
}

.question-item__difficulty--easy {
  color: #00b8a3;
}

.question-item__difficulty--medium {
  color: #ffc01e;
}

.question-item__difficulty--hard {
  color: #ff375f;
}

.question-bank__pagination {
  justify-content: center;
}

@media (max-width: 760px) {
  .question-bank {
    padding: 24px 0 40px;
  }

  .question-bank__title {
    font-size: 22px;
  }

  .question-bank__search {
    width: 100%;
  }

  /* 移动端隐藏通过率，表头与数据行同步收窄到三列 */
  .question-header,
  .question-item {
    grid-template-columns: 32px minmax(0, 1fr) 56px;
    padding: 12px 4px;
  }

  .question-header__rate {
    display: none;
  }

  .question-header__difficulty {
    grid-column: 3;
  }

  .question-item__rate {
    display: none;
  }
}

@media (prefers-reduced-motion: reduce) {
  .question-item {
    transition: none;
  }
}
</style>
