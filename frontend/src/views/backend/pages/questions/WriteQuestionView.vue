<!-- 后台新建与编辑题目页面 -->
<template>
  <section v-loading="loading" class="page">
    <header class="page__header">
      <h2 class="page__title">{{ isEdit ? '编辑题目' : '新建题目' }}</h2>
      <div class="page__actions">
        <el-button :disabled="submitting" @click="previewVisible = true">预览</el-button>
        <el-button :disabled="submitting" @click="goBack">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">保存</el-button>
      </div>
    </header>

    <el-form
      ref="formRef"
      class="page__form"
      :model="form"
      :rules="rules"
      label-position="top"
      @submit.prevent
    >
      <div class="page__grid">
        <el-form-item label="题目标题" prop="title" class="page__field page__field--full">
          <el-input
            v-model="form.title"
            placeholder="请输入题目标题"
            maxlength="200"
            show-word-limit
            clearable
          />
        </el-form-item>

        <el-form-item label="难度" prop="difficulty">
          <el-select v-model="form.difficulty" placeholder="请选择难度" class="page__control">
            <el-option
              v-for="option in DIFFICULTY_OPTIONS"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="判题模式" prop="judgeMode">
          <el-select v-model="form.judgeMode" placeholder="请选择判题模式" class="page__control">
            <el-option
              v-for="option in JUDGE_MODE_OPTIONS"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择状态" class="page__control">
            <el-option
              v-for="option in STATUS_OPTIONS"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="时间限制（毫秒）" prop="timeLimit">
          <el-input-number
            v-model="form.timeLimit"
            class="page__control"
            :min="1"
            :step="100"
            controls-position="right"
          />
        </el-form-item>

        <el-form-item label="内存限制（MB）" prop="memoryLimit">
          <el-input-number
            v-model="form.memoryLimit"
            class="page__control"
            :min="1"
            :step="16"
            controls-position="right"
          />
        </el-form-item>

        <el-form-item label="题目来源" prop="source">
          <el-input
            v-model="form.source"
            placeholder="请输入题目来源"
            maxlength="100"
            show-word-limit
            clearable
          />
        </el-form-item>

        <el-form-item label="标签" prop="tagIds" class="page__field--full">
          <el-select
            v-model="form.tagIds"
            class="page__control"
            multiple
            filterable
            clearable
            collapse-tags
            collapse-tags-tooltip
            :max-collapse-tags="8"
            :loading="tagLoading"
            placeholder="请选择标签，如二分、动态规划"
          >
            <el-option v-for="tag in tagOptions" :key="tag.id" :label="tag.name" :value="tag.id">
              <span class="page__tag-dot" :style="tagDotStyle(tag.color)" aria-hidden="true" />
              <span>{{ tag.name }}</span>
            </el-option>
          </el-select>
        </el-form-item>
      </div>

      <el-form-item label="题目描述" prop="description">
        <MarkdownEditor v-model="form.description" height="420px" placeholder="请输入题目描述" />
      </el-form-item>

      <!-- 输入输出格式只在标准输入输出模式下有含义 -->
      <template v-if="form.judgeMode === JUDGE_MODE_STANDARD">
        <el-form-item label="输入格式" prop="inputFormat">
          <MarkdownEditor v-model="form.inputFormat" height="240px" placeholder="请输入输入格式说明" />
        </el-form-item>

        <el-form-item label="输出格式" prop="outputFormat">
          <MarkdownEditor
            v-model="form.outputFormat"
            height="240px"
            placeholder="请输入输出格式说明"
          />
        </el-form-item>
      </template>

      <el-form-item label="样例">
        <div class="page__samples">
          <article v-for="(sample, index) in samples" :key="index" class="page__sample">
            <header class="page__sample-header">
              <span class="page__sample-title">样例 {{ index + 1 }}</span>
              <el-button
                link
                type="danger"
                :icon="Trash2"
                :disabled="samples.length === 1"
                @click="removeSample(index)"
              >
                删除
              </el-button>
            </header>
            <el-input
              v-model="sample.input"
              type="textarea"
              :rows="4"
              resize="vertical"
              placeholder="输入"
              class="page__sample-input"
            />
            <el-input
              v-model="sample.output"
              type="textarea"
              :rows="4"
              resize="vertical"
              placeholder="输出"
              class="page__sample-input"
            />
          </article>
          <el-button class="page__sample-add" :icon="Plus" @click="addSample">添加样例</el-button>
        </div>
      </el-form-item>

      <el-form-item label="提示" prop="hint">
        <MarkdownEditor v-model="form.hint" height="240px" placeholder="请输入解题提示" />
      </el-form-item>
    </el-form>

    <!-- 新建时草稿暂存在前端，保存题目后一并提交 -->
    <el-tabs v-model="activeTab" class="page__tabs">
      <el-tab-pane label="代码模板" name="template">
        <QuestionCodeTemplatePanel v-model="draftTemplates" :question-id="questionId" />
      </el-tab-pane>
      <el-tab-pane label="测试用例" name="testcase">
        <QuestionTestCasePanel
          v-model="draftTestCases"
          :question-id="questionId"
          :judge-mode="form.judgeMode"
        />
      </el-tab-pane>
      <el-tab-pane label="评测数据" name="testdata">
        <QuestionTestDataPanel v-model="draftTestData" :question-id="questionId" />
      </el-tab-pane>
    </el-tabs>

    <el-dialog
      v-model="previewVisible"
      title="题目预览"
      width="min(900px, 92vw)"
      top="6vh"
      destroy-on-close
      class="page__preview-dialog"
    >
      <QuestionPreview
        :title="form.title"
        :description="form.description"
        :input-format="form.inputFormat"
        :output-format="form.outputFormat"
        :hint="form.hint"
        :difficulty="form.difficulty"
        :judge-mode="form.judgeMode"
        :time-limit="form.timeLimit"
        :memory-limit="form.memoryLimit"
        :source="form.source"
        :samples="samples"
        :tags="selectedTags"
      />
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Trash2 } from '@lucide/vue'
import MarkdownEditor from '@/components/editor/MarkdownEditor.vue'
import QuestionPreview from '@/views/backend/components/QuestionPreview.vue'
import QuestionCodeTemplatePanel, {
  type DraftCodeTemplate,
} from '@/views/backend/components/QuestionCodeTemplatePanel.vue'
import QuestionTestCasePanel, {
  type DraftTestCase,
} from '@/views/backend/components/QuestionTestCasePanel.vue'
import QuestionTestDataPanel, {
  type DraftTestData,
} from '@/views/backend/components/QuestionTestDataPanel.vue'
import {
  createQuestion,
  getQuestionById,
  updateQuestion,
  type QuestionCreatePayload,
} from '@/api/backend/question'
import { pageTags, type TagVO } from '@/api/backend/tag'
import {
  createQuestionCodeTemplate,
  createQuestionTestCase,
} from '@/api/backend/questionCode'
import { uploadQuestionTestData } from '@/api/backend/questionTestData'

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

// 判题模式：标准输入输出
const JUDGE_MODE_STANDARD = 1

// 状态选项：0 草稿，1 已发布，2 已下架
const STATUS_OPTIONS = [
  { value: 0, label: '草稿' },
  { value: 1, label: '已发布' },
  { value: 2, label: '已下架' },
] as const

// 样例结构，与后端 sampleCases 的 JSON 元素对应
interface SampleCase {
  input: string
  output: string
}

const route = useRoute()
const router = useRouter()

const formRef = ref<FormInstance>()
const loading = ref(false)
const submitting = ref(false)
const previewVisible = ref(false)

// 编辑态下方的附加面板标签页
const activeTab = ref('template')

// 新建时的草稿数据，题目保存后统一提交
const draftTemplates = ref<DraftCodeTemplate[]>([])
const draftTestCases = ref<DraftTestCase[]>([])
const draftTestData = ref<DraftTestData[]>([])

// 编辑态由路由参数判定，新建与编辑共用同一页面
const isEdit = computed(() => route.name === 'backend-question-edit')

const questionId = computed(() => {
  const id = route.params.id
  return typeof id === 'string' ? id : ''
})

const form = reactive({
  title: '',
  difficulty: 1 as number,
  judgeMode: JUDGE_MODE_STANDARD as number,
  status: 0 as number,
  timeLimit: 1000,
  memoryLimit: 128,
  source: '',
  description: '',
  inputFormat: '',
  outputFormat: '',
  hint: '',
  tagIds: [] as string[],
})

// 样例独立维护，提交时再序列化成后端要求的 JSON 字符串
const samples = ref<SampleCase[]>([{ input: '', output: '' }])

// 可选标签，进入页面时一次性拉取
const tagOptions = ref<TagVO[]>([])
const tagLoading = ref(false)

// 当前已选标签，按选择顺序映射成预览用结构
const selectedTags = computed(() =>
  form.tagIds
    .map((id) => tagOptions.value.find((tag) => tag.id === id))
    .filter((tag): tag is TagVO => tag !== undefined)
    .map((tag) => ({ id: tag.id, name: tag.name, color: tag.color })),
)

// 标签颜色点样式，未设颜色时不渲染圆点
function tagDotStyle(color: string | null): Record<string, string> | undefined {
  if (!color) return undefined
  return { background: color }
}

// 拉取全部标签，最多一次取 100 条，与后端分页上限一致
async function loadTags() {
  tagLoading.value = true
  try {
    const page = await pageTags({ pageNum: 1, pageSize: 100 })
    tagOptions.value = page.records
  } finally {
    tagLoading.value = false
  }
}

const rules: FormRules = {
  title: [
    { required: true, message: '请输入题目标题', trigger: 'blur' },
    { max: 200, message: '题目标题长度不能超过 200', trigger: 'blur' },
  ],
  description: [{ required: true, message: '请输入题目描述', trigger: 'blur' }],
  source: [{ max: 100, message: '题目来源长度不能超过 100', trigger: 'blur' }],
}

function addSample() {
  samples.value.push({ input: '', output: '' })
}

// 至少保留一组样例，避免提交空数组
function removeSample(index: number) {
  if (samples.value.length === 1) return
  samples.value.splice(index, 1)
}

// 解析后端返回的样例 JSON，结构异常时回退到一组空样例
function parseSampleCases(raw: string | null): SampleCase[] {
  if (!raw) return [{ input: '', output: '' }]
  try {
    const parsed: unknown = JSON.parse(raw)
    if (!Array.isArray(parsed) || parsed.length === 0) return [{ input: '', output: '' }]
    return parsed.map((item) => {
      const record = (item ?? {}) as Record<string, unknown>
      return {
        input: typeof record.input === 'string' ? record.input : '',
        output: typeof record.output === 'string' ? record.output : '',
      }
    })
  } catch {
    return [{ input: '', output: '' }]
  }
}

// 过滤全空样例，避免把空白条目写进数据库
function buildSampleCases(): string {
  const filled = samples.value
    .filter((sample) => sample.input.trim() || sample.output.trim())
    .map((sample) => ({ input: sample.input, output: sample.output }))
  return JSON.stringify(filled)
}

// 加载题目详情，编辑态才请求；显式传入 id 时忽略路由状态
async function loadQuestion(id?: string) {
  const targetId = id ?? questionId.value
  if (!targetId) return
  if (id === undefined && !isEdit.value) return
  loading.value = true
  try {
    const detail = await getQuestionById(targetId)
    form.title = detail.title
    form.difficulty = detail.difficulty
    form.judgeMode = detail.judgeMode
    form.status = detail.status
    form.timeLimit = detail.timeLimit
    form.memoryLimit = detail.memoryLimit
    form.source = detail.source ?? ''
    form.description = detail.description
    form.inputFormat = detail.inputFormat ?? ''
    form.outputFormat = detail.outputFormat ?? ''
    form.hint = detail.hint ?? ''
    form.tagIds = detail.tagIds ?? []
    samples.value = parseSampleCases(detail.sampleCases)
  } catch {
    // 请求层已提示错误，这里回到列表避免停在空表单
    goBack()
  } finally {
    loading.value = false
  }
}

// 组装提交参数，空字符串按未填写处理
function buildPayload(): QuestionCreatePayload {
  return {
    title: form.title.trim(),
    difficulty: form.difficulty,
    judgeMode: form.judgeMode,
    description: form.description,
    inputFormat: form.inputFormat || undefined,
    outputFormat: form.outputFormat || undefined,
    sampleCases: buildSampleCases(),
    hint: form.hint || undefined,
    timeLimit: form.timeLimit,
    memoryLimit: form.memoryLimit,
    source: form.source.trim() || undefined,
    status: form.status,
    tagIds: form.tagIds,
  }
}

async function submit() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (isEdit.value) {
      await updateQuestion({ id: questionId.value, ...buildPayload() })
      ElMessage.success('题目已更新')
      goBack()
      return
    }

    // 新建后先落库拿到主键，再提交草稿里的模板与用例
    const newId = await createQuestion(buildPayload())
    await submitDrafts(newId)
    ElMessage.success('题目已创建')
    await router.replace({ name: 'backend-question-edit', params: { id: newId } })
    await loadQuestion(newId)
    activeTab.value = 'template'
  } finally {
    submitting.value = false
  }
}

// 提交草稿中的代码模板、测试用例与评测数据，单个失败不阻断其余数据
async function submitDrafts(questionId: string) {
  const failures: string[] = []

  for (const item of draftTemplates.value) {
    try {
      await createQuestionCodeTemplate({ questionId, ...item })
    } catch {
      failures.push(`代码模板（${item.language}）`)
    }
  }

  for (const item of draftTestCases.value) {
    try {
      await createQuestionTestCase({ questionId, ...item })
    } catch {
      failures.push('测试用例')
    }
  }

  for (const item of draftTestData.value) {
    try {
      await uploadQuestionTestData(questionId, item.inputFile, item.outputFile, item.visible)
    } catch {
      failures.push(`评测数据（${item.inputFile.name}）`)
    }
  }

  draftTemplates.value = []
  draftTestCases.value = []
  draftTestData.value = []

  if (failures.length > 0) {
    ElMessage.warning(`部分数据保存失败：${failures.join('、')}`)
  }
}

// 返回题目列表
function goBack() {
  router.push({ name: 'backend-questions' })
}

onMounted(() => {
  loadTags()
  loadQuestion()
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

.page__actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.page__form {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.page__grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 0 18px;
}

.page__field--full {
  grid-column: 1 / -1;
}

.page__control {
  width: 100%;
}

.page__samples {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%;
}

.page__sample {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 14px;
  border: 1px solid var(--backend-border);
  border-radius: 8px;
}

.page__sample-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.page__sample-title {
  font-size: 14px;
  font-weight: 600;
}

/* 样例输入输出用等宽字体，便于核对空格与换行 */
.page__sample-input :deep(.el-textarea__inner) {
  font-family: 'JetBrains Mono', 'Cascadia Code', Consolas, monospace;
  font-size: 13px;
}

.page__sample-add {
  align-self: flex-start;
}

/* 标签下拉项左侧的颜色圆点 */
.page__tag-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  margin-right: 8px;
  border-radius: 50%;
  background: var(--backend-muted);
  vertical-align: middle;
}

@media (max-width: 760px) {
  .page__header {
    flex-direction: column;
    align-items: flex-start;
  }

  .page__actions {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>
