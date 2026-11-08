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
        <span aria-hidden="true"></span>
        <span class="question-header__title">题目</span>

        <!-- 标签是独立一列，列宽恒定占位，切换只换内容，其他列不移动 -->
        <button
          type="button"
          class="question-header__tags"
          :class="{ 'is-open': showTags }"
          :aria-pressed="showTags"
          :aria-label="showTags ? '隐藏标签列内容' : '显示标签列内容'"
          :title="showTags ? '隐藏标签' : '显示标签'"
          @click="showTags = !showTags"
        >
          <span>标签</span>
          <ChevronUp v-if="showTags" :size="12" />
          <ChevronDown v-else :size="12" />
        </button>

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

          <span class="question-item__main">
            <span class="question-item__title">{{ item.title }}</span>
            <!-- 只有核心代码题标注模式，标准输入输出是默认形态，标注反而制造噪音 -->
            <span v-if="item.judgeMode === JUDGE_MODE_CORE" class="question-item__mode">
              核心代码
            </span>
          </span>

          <span class="question-item__tags">
            <template v-if="showTags && item.tags?.length">
              <span v-for="tag in visibleTags(item.tags)" :key="tag" class="question-item__tag">
                {{ tag }}
              </span>
              <span
                v-if="extraTagCount(item.tags) > 0"
                class="question-item__tag question-item__tag--more"
              >
                +{{ extraTagCount(item.tags) }}
              </span>
            </template>
          </span>

          <span class="question-item__rate">
            <template v-if="item.submitCount">
              <span class="question-item__rate-value">{{ rateText(item) }}</span>
              <span class="question-item__rate-detail">{{ rateDetail(item) }}</span>
            </template>
            <span v-else class="question-item__rate-empty">—</span>
          </span>

          <span class="question-item__difficulty" :class="difficultyClass(item.difficulty)">
            {{ difficultyLabel(item.difficulty) }}
          </span>
        </RouterLink>

        <el-empty v-if="!loading && rows.length === 0" description="没有符合条件的题目" :image-size="96" />
      </div>
    </div>

    <el-pagination
      v-if="total > 0"
      v-model:current-page="pageNum"
      v-model:page-size="pageSize"
      class="question-bank__pagination"
      :total="total"
      :page-sizes="PAGE_SIZES"
      layout="total, sizes, prev, pager, next"
      background
      @current-change="loadQuestions"
      @size-change="handleSizeChange"
    />
  </section>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { ChevronDown, ChevronUp, Search } from '@lucide/vue'
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

// 判题模式：核心代码，需要在标题旁标注
const JUDGE_MODE_CORE = 2

// 标签列最多展示的个数，超出部分折叠为计数
const MAX_VISIBLE_TAGS = 2

// 可选每页条数
const PAGE_SIZES = [10, 20, 50]

const loading = ref(false)
const rows = ref<QuestionListItemVO[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(20)
const tagOptions = ref<TagOption[]>([])

// 标签列默认收起，避免挤占标题宽度
const showTags = ref(false)

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

// 当前行需要展示的标签
function visibleTags(tags: string[] | null): string[] {
  if (!tags || tags.length === 0) return []
  return tags.slice(0, MAX_VISIBLE_TAGS)
}

// 折叠掉的标签个数
function extraTagCount(tags: string[] | null): number {
  if (!tags || tags.length <= MAX_VISIBLE_TAGS) return 0
  return tags.length - MAX_VISIBLE_TAGS
}

// 通过率，保留一位小数
function rateText(item: QuestionListItemVO): string {
  if (!item.submitCount) return '—'
  return `${((item.acceptedCount / item.submitCount) * 100).toFixed(1)}%`
}

// 通过次数与提交次数，作为通过率的明细
function rateDetail(item: QuestionListItemVO): string {
  return `${item.acceptedCount}/${item.submitCount}`
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

// 切换每页条数后回到第一页
function handleSizeChange() {
  pageNum.value = 1
  loadQuestions()
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

/* 表头与数据行共用一套五列，标签列始终占位，切换只换内容不改变布局 */
.question-header,
.question-item {
  display: grid;
  grid-template-columns: 48px minmax(0, 1fr) 176px 132px 76px;
  align-items: center;
  gap: 16px;
}

.question-header {
  padding: 10px 8px;
  border-bottom: 1px solid var(--home-popover-border);
  color: var(--home-navbar-muted);
  font-size: 12px;
  font-weight: 500;
}

.question-header__rate {
  text-align: right;
}

.question-header__difficulty {
  text-align: right;
}

/* 标签表头本身是切换按钮，外观仍是普通表头文字 */
.question-header__tags {
  display: inline-flex;
  align-items: center;
  justify-self: start;
  gap: 4px;
  padding: 2px 6px;
  border: 1px solid transparent;
  border-radius: 4px;
  background: none;
  color: inherit;
  font: inherit;
  cursor: pointer;
  transition:
    color 140ms ease,
    border-color 140ms ease,
    background-color 140ms ease;
}

.question-header__tags:hover {
  border-color: var(--home-popover-border);
  background: var(--home-popover-soft);
  color: var(--home-navbar-text);
}

.question-header__tags.is-open {
  color: var(--home-navbar-text);
}

.question-header__tags:focus-visible {
  outline: 2px solid var(--el-color-primary);
  outline-offset: 1px;
}

.question-bank__list {
  display: flex;
  flex-direction: column;
  min-height: 200px;
}

/* 题目行只补自己的内边距与交互，列宽由表头那段统一声明 */
.question-item {
  padding: 14px 8px;
  border-bottom: 1px solid var(--home-popover-border);
  border-radius: 6px;
  color: inherit;
  text-decoration: none;
  transition:
    background-color 140ms ease,
    box-shadow 140ms ease;
}

.question-item:hover {
  background: var(--home-popover-soft);
}

/* 悬停时整行可点，用标题变色提示跳转 */
.question-item:hover .question-item__title {
  color: var(--el-color-primary);
}

.question-item__index {
  color: var(--home-navbar-muted);
  font-size: 14px;
  font-variant-numeric: tabular-nums;
  text-align: right;
}

/* 标题与模式标记同一行，标题过长时截断而不是挤压标记 */
.question-item__main {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.question-item__title {
  overflow: hidden;
  font-size: 14px;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color 140ms ease;
}

.question-item__mode {
  flex: none;
  padding: 1px 6px;
  border: 1px solid var(--home-popover-border);
  border-radius: 4px;
  color: var(--home-navbar-muted);
  font-size: 11px;
  line-height: 16px;
}

/* 标签列恒定单行，超出宽度直接裁掉，行高不随标签数量变化 */
.question-item__tags {
  display: flex;
  flex-wrap: nowrap;
  align-items: center;
  gap: 6px;
  overflow: hidden;
}

.question-item__tag {
  overflow: hidden;
  max-width: 96px;
  padding: 1px 8px;
  border: 1px solid var(--home-popover-border);
  border-radius: 999px;
  background: var(--home-popover-soft);
  color: var(--home-navbar-muted);
  font-size: 12px;
  line-height: 18px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.question-item__tag--more {
  flex: none;
  border-style: dashed;
  font-variant-numeric: tabular-nums;
}

/* 百分比固定宽度右对齐、明细左对齐，各行数字才能上下对齐 */
.question-item__rate {
  display: grid;
  grid-template-columns: 44px minmax(0, 1fr);
  align-items: baseline;
  gap: 8px;
  font-variant-numeric: tabular-nums;
}

.question-item__rate-value {
  font-size: 13px;
  text-align: right;
}

.question-item__rate-detail {
  overflow: hidden;
  color: var(--home-navbar-muted);
  font-size: 12px;
  text-align: right;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.question-item__rate-empty {
  grid-column: 1 / -1;
  color: var(--home-navbar-muted);
  font-size: 13px;
  text-align: right;
}

/* 难度用同色系淡底胶囊，色值靠 currentColor 派生，明暗模式都能自适应 */
.question-item__difficulty {
  justify-self: end;
  padding: 2px 10px;
  border-radius: 999px;
  background: color-mix(in srgb, currentColor 12%, transparent);
  font-size: 12px;
  font-weight: 500;
  line-height: 18px;
  white-space: nowrap;
}

.question-item__difficulty--easy {
  color: var(--status-success);
}

.question-item__difficulty--medium {
  color: var(--status-warning);
}

.question-item__difficulty--hard {
  color: var(--status-danger);
}

.question-bank__pagination {
  justify-content: center;
}

/* 中等宽度收紧各列，标签列同步收窄但仍保留占位 */
@media (max-width: 1080px) {
  .question-header,
  .question-item {
    grid-template-columns: 44px minmax(0, 1fr) 132px 124px 72px;
    gap: 12px;
  }
}

@media (max-width: 760px) {
  .question-bank {
    padding: 24px 0 40px;
  }

  .question-bank__title {
    font-size: 22px;
  }

  /* 工具栏改为两列网格，搜索占满一行，其余三个下拉均分，避免三个下拉溢出视口 */
  .question-bank__toolbar {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 8px;
  }

  .question-bank__search {
    grid-column: 1 / -1;
    width: 100%;
  }

  .question-bank__select {
    width: 100%;
  }

  /* 移动端只留序号、标题与难度，标签列与通过率一并隐藏 */
  .question-header,
  .question-item {
    grid-template-columns: 32px minmax(0, 1fr) 60px;
    gap: 10px;
    padding: 12px 4px;
  }

  .question-header__tags,
  .question-item__tags,
  .question-header__rate,
  .question-item__rate {
    display: none;
  }

  .question-item__difficulty {
    padding: 2px 8px;
  }

  .question-bank__pagination {
    justify-content: center;
  }
}

/* 窄屏下分页只保留翻页按钮，避免 total 与 sizes 撑破一行 */
:deep(.el-pagination__total),
:deep(.el-pagination__sizes) {
  display: none;
}

@media (min-width: 761px) {
  :deep(.el-pagination__total),
  :deep(.el-pagination__sizes) {
    display: inline-flex;
  }
}

@media (prefers-reduced-motion: reduce) {
  .question-item,
  .question-item__title {
    transition: none;
  }
}
</style>
