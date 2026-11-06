<!-- 题目做题页面 -->
<template>
  <section v-loading="loading" class="question-detail">
    <div v-if="detail" class="question-detail__grid">
        <!-- 左侧：题目描述 -->
        <article class="question-panel">
          <header class="question-panel__header">
            <h1 class="question-panel__title">{{ detail.title }}</h1>

            <div class="question-panel__meta">
              <span class="question-panel__difficulty" :class="difficultyClass(detail.difficulty)">
                {{ difficultyLabel(detail.difficulty) }}
              </span>
              <span class="question-panel__meta-item">时间 {{ detail.timeLimit }} ms</span>
              <span class="question-panel__meta-item">内存 {{ detail.memoryLimit }} MB</span>
              <span class="question-panel__meta-item">{{ judgeModeLabel(detail.judgeMode) }}</span>
            </div>

            <div v-if="detail.tags?.length" class="question-panel__tags">
              <span v-for="tag in detail.tags" :key="tag" class="question-panel__tag">{{ tag }}</span>
            </div>
          </header>

          <div class="question-panel__body">
            <MarkdownViewer :model-value="detail.description" />

            <!-- 输入输出格式只在标准输入输出模式下展示 -->
            <template v-if="detail.judgeMode === JUDGE_MODE_STANDARD">
              <h2 v-if="detail.inputFormat" class="question-panel__heading">输入格式</h2>
              <MarkdownViewer v-if="detail.inputFormat" :model-value="detail.inputFormat" />

              <h2 v-if="detail.outputFormat" class="question-panel__heading">输出格式</h2>
              <MarkdownViewer v-if="detail.outputFormat" :model-value="detail.outputFormat" />
            </template>

            <h2 v-if="samples.length" class="question-panel__heading">示例</h2>
            <div v-if="samples.length" class="question-panel__samples">
              <div v-for="(sample, index) in samples" :key="index" class="sample">
                <div class="sample__row">
                  <span class="sample__label">输入</span>
                  <pre class="sample__code">{{ sample.input }}</pre>
                </div>
                <div class="sample__row">
                  <span class="sample__label">输出</span>
                  <pre class="sample__code">{{ sample.output }}</pre>
                </div>
              </div>
            </div>

            <template v-if="detail.hint">
              <h2 class="question-panel__heading">提示</h2>
              <MarkdownViewer :model-value="detail.hint" />
            </template>
          </div>
        </article>

        <!-- 右侧：代码编辑与运行 -->
        <article class="code-panel">
          <header class="code-panel__header">
            <el-select v-model="language" class="code-panel__language">
              <el-option
                v-for="option in availableLanguages"
                :key="option.value"
                :label="option.label"
                :value="option.value"
              />
            </el-select>

            <el-button :icon="RotateCcw" @click="resetCode">重置</el-button>
          </header>

          <el-input
            v-model="code"
            class="code-panel__editor"
            type="textarea"
            :rows="20"
            resize="none"
            spellcheck="false"
            placeholder="在此编写代码"
          />

          <div class="code-panel__test">
            <div class="code-panel__test-header">
              <span class="code-panel__test-title">测试用例</span>
              <div class="code-panel__test-actions">
                <el-select
                  v-if="testCases.length"
                  v-model="selectedCaseNo"
                  class="code-panel__case-select"
                  placeholder="选用例"
                  clearable
                  @change="fillCase"
                >
                  <el-option
                    v-for="item in testCases"
                    :key="item.caseNo"
                    :label="`用例 ${item.caseNo}`"
                    :value="item.caseNo"
                  />
                </el-select>
                <el-button v-if="samples.length" link type="primary" @click="fillSample">
                  填入示例
                </el-button>
              </div>
            </div>

            <div class="code-panel__test-grid">
              <label class="code-panel__test-field">
                <span class="code-panel__test-label">输入</span>
                <el-input
                  v-model="testInput"
                  class="code-panel__test-input"
                  type="textarea"
                  :rows="4"
                  resize="vertical"
                  spellcheck="false"
                  placeholder="手动输入测试数据，或从上方选用例"
                />
              </label>

              <label class="code-panel__test-field">
                <span class="code-panel__test-label">预期输出</span>
                <el-input
                  v-model="expectedOutput"
                  class="code-panel__test-input"
                  type="textarea"
                  :rows="4"
                  resize="vertical"
                  spellcheck="false"
                  placeholder="选填，填写后会比对结果"
                />
              </label>
            </div>

            <div class="code-panel__actions">
              <el-button :icon="Play" :loading="running" @click="runCode">测试运行</el-button>
              <el-button
                type="primary"
                :icon="Send"
                :loading="submitting"
                @click="submitCode"
              >
                提交评测
              </el-button>
            </div>
          </div>

          <div v-if="result" class="code-panel__result">
            <div class="code-panel__result-header">
              <span class="code-panel__result-status" :class="resultClass">
                {{ resultLabel }}
              </span>
              <span v-if="result.time !== undefined" class="code-panel__result-meta">
                {{ result.time }} ms / {{ result.memory }} KB
              </span>
            </div>
            <pre v-if="result.output" class="code-panel__result-output">{{ result.output }}</pre>
            <pre v-if="result.error" class="code-panel__result-error">{{ result.error }}</pre>
          </div>

          <div v-if="judging || judgeResult" class="code-panel__judge">
            <div class="code-panel__judge-header">
              <span class="code-panel__judge-status" :class="judgeClass">
                {{ judgeLabel }}
              </span>
              <span v-if="judgeResult?.totalCount" class="code-panel__judge-meta">
                {{ judgeResult.passedCount ?? 0 }} / {{ judgeResult.totalCount }} 通过
                <template v-if="judgeResult.timeUsed !== null">
                  · {{ judgeResult.timeUsed }} ms / {{ judgeResult.memoryUsed }} KB
                </template>
              </span>
            </div>

            <el-progress
              v-if="judging"
              class="code-panel__judge-progress"
              :percentage="100"
              :indeterminate="true"
              :duration="2"
              :show-text="false"
              :stroke-width="4"
            />

            <pre v-if="judgeResult?.message" class="code-panel__judge-message">{{
              judgeResult.message
            }}</pre>

            <el-table
              v-if="judgeResult?.cases?.length"
              :data="judgeResult.cases"
              size="small"
              border
              class="code-panel__judge-table"
            >
              <el-table-column prop="caseNo" label="测试点" width="80" align="center" />
              <el-table-column label="结果" width="140">
                <template #default="{ row }">
                  <el-tag size="small" :type="row.passed ? 'success' : 'danger'">
                    {{ row.status }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="time" label="耗时" width="90" align="right">
                <template #default="{ row }">{{ row.time }} ms</template>
              </el-table-column>
              <el-table-column prop="memory" label="内存" width="100" align="right">
                <template #default="{ row }">{{ row.memory }} KB</template>
              </el-table-column>
              <el-table-column prop="message" label="说明" min-width="180" show-overflow-tooltip />
            </el-table>
          </div>
        </article>
    </div>

    <el-empty v-else-if="!loading" description="题目不存在或未发布" />
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Play, RotateCcw, Send } from '@lucide/vue'
import MarkdownViewer from '@/components/editor/MarkdownViewer.vue'
import { getQuestionDetail, type QuestionDetailVO } from '@/api/home/questionBank'
import { runCode as requestRunCode } from '@/api/problem/runCode'
import { submitCode as requestSubmitCode, getSubmissionStatus, type SubmissionStatusVO } from '@/api/problem/submitCode'

// 判题模式：标准输入输出
const JUDGE_MODE_STANDARD = 1

// 评测任务轮询间隔，毫秒
const POLL_INTERVAL_MS = 1000

// 难度选项：1 简单，2 中等，3 困难
const DIFFICULTY_OPTIONS = [
  { value: 1, label: '简单' },
  { value: 2, label: '中等' },
  { value: 3, label: '困难' },
] as const

// 语言选项，value 对应题库模板的 language，code 对应评测机接口的语言标识
const LANGUAGE_OPTIONS = [
  { value: 1, code: 'java', label: 'Java' },
  { value: 2, code: 'cpp', label: 'C++' },
  { value: 3, code: 'python', label: 'Python' },
  { value: 4, code: 'go', label: 'Go' },
  { value: 5, code: 'javascript', label: 'JavaScript' },
] as const

// 样例结构，与后端 sampleCases 的 JSON 元素对应
interface SampleCase {
  input: string
  output: string
}

// 运行结果
interface RunResult {
  status: number
  output: string
  error: string
  time?: number
  memory?: number
}

const route = useRoute()

const loading = ref(false)
const running = ref(false)
const detail = ref<QuestionDetailVO | null>(null)
const language = ref<number>(1)
const code = ref('')
const testInput = ref('')
const expectedOutput = ref('')
const selectedCaseNo = ref<number | undefined>(undefined)
const result = ref<RunResult | null>(null)
const submitting = ref(false)
const judging = ref(false)
const judgeResult = ref<SubmissionStatusVO | null>(null)

// 轮询定时器，离开页面时需要清理
let pollTimer: number | null = null
const questionId = computed(() => {
  const id = route.params.id
  return typeof id === 'string' ? id : ''
})

// 只展示题目已配置模板的语言
const availableLanguages = computed(() => {
  const configured = new Set((detail.value?.codeTemplates ?? []).map((item) => item.language))
  if (configured.size === 0) return LANGUAGE_OPTIONS
  return LANGUAGE_OPTIONS.filter((option) => configured.has(option.value))
})

// 测试运行可快捷填入的用例，来自后台维护的测试用例表
const testCases = computed(() => detail.value?.testCases ?? [])

// 解析后端返回的样例 JSON，结构异常时回退为空
const samples = computed<SampleCase[]>(() => {
  const raw = detail.value?.sampleCases
  if (!raw) return []
  try {
    const parsed: unknown = JSON.parse(raw)
    if (!Array.isArray(parsed)) return []
    return parsed.map((item) => {
      const record = (item ?? {}) as Record<string, unknown>
      return {
        input: typeof record.input === 'string' ? record.input : '',
        output: typeof record.output === 'string' ? record.output : '',
      }
    })
  } catch {
    return []
  }
})

// 难度文案
function difficultyLabel(difficulty: number): string {
  return DIFFICULTY_OPTIONS.find((option) => option.value === difficulty)?.label ?? '-'
}

// 难度对应的样式类
function difficultyClass(difficulty: number): string {
  if (difficulty === 1) return 'question-panel__difficulty--easy'
  if (difficulty === 2) return 'question-panel__difficulty--medium'
  return 'question-panel__difficulty--hard'
}

// 判题模式文案
function judgeModeLabel(judgeMode: number): string {
  return judgeMode === JUDGE_MODE_STANDARD ? '标准输入输出' : '核心代码'
}

// 按当前语言取初始代码
function currentTemplate(): string {
  return detail.value?.codeTemplates?.find((item) => item.language === language.value)?.template ?? ''
}

// 切换语言时替换为对应模板
watch(language, () => {
  code.value = currentTemplate()
})

// 恢复到当前语言的初始代码
function resetCode() {
  code.value = currentTemplate()
  result.value = null
}

// 把第一个示例的输入与预期输出填到测试框
function fillSample() {
  const first = samples.value[0]
  if (!first) {
    ElMessage.warning('该题目没有示例')
    return
  }
  testInput.value = first.input
  expectedOutput.value = first.output
}

// 填入所选用例，隐藏用例只有输入没有期望输出
function fillCase(caseNo: number | undefined) {
  if (caseNo === undefined) return
  const matched = testCases.value.find((item) => item.caseNo === caseNo)
  if (!matched) return
  testInput.value = matched.input ?? ''
  expectedOutput.value = matched.expected ?? ''
}

// 逐行去除行尾空白与末尾空行后比较，与常见 OJ 的比对规则一致
function normalizeOutput(text: string): string {
  return text
    .replace(/\r\n/g, '\n')
    .split('\n')
    .map((line) => line.trimEnd())
    .join('\n')
    .trimEnd()
}

// 比对实际输出与预期输出，未填预期时不做判断
function matchExpected(actual: string): boolean | null {
  const expected = expectedOutput.value.trim()
  if (!expected) return null
  return normalizeOutput(actual) === normalizeOutput(expectedOutput.value)
}

// 运行状态文案
const resultLabel = computed(() => {
  if (!result.value) return ''
  return result.value.status === 1 ? '运行完成' : '运行失败'
})

// 运行结果对应的样式类
const resultClass = computed(() =>
  result.value?.status === 1 ? 'code-panel__result-status--ok' : 'code-panel__result-status--fail',
)

// 提交结果对应的样式类
const judgeClass = computed(() => {
  if (judging.value) return ''
  return judgeResult.value?.accepted
    ? 'code-panel__judge-status--ok'
    : 'code-panel__judge-status--fail'
})

// 提交结果的展示文案，评测中显示进度提示
const judgeLabel = computed(() => {
  if (judging.value) return '评测中…'
  return judgeResult.value?.result ?? '-'
})

// 评测机状态到中文文案
const STATUS_TEXT: Record<string, string> = {
  Accepted: '运行完成',
  'Time Limit Exceeded': '超出时间限制',
  'Memory Limit Exceeded': '超出内存限制',
  'Nonzero Exit Status': '运行错误',
  'Runtime Error': '运行错误',
  'Internal Error': '评测机内部错误',
  'Invalid Request': '请求参数有误',
}

// 运行代码，由后端挑选评测机执行
async function runCode() {
  if (!code.value.trim()) {
    ElMessage.warning('请先编写代码')
    return
  }

  const option = LANGUAGE_OPTIONS.find((item) => item.value === language.value)
  if (!option) {
    ElMessage.warning('暂不支持该语言')
    return
  }

  running.value = true
  result.value = null
  try {
    const response = await requestRunCode({
      questionId: questionId.value,
      language: option.code,
      code: code.value,
      input: testInput.value,
    })

    const stageText = response.stage === 'compile' ? '编译失败：' : ''
    const ok = response.status === 'Accepted'
    const stdout = response.stdout ?? ''
    // 填了预期输出时以比对结果决定状态
    const matched = ok ? matchExpected(stdout) : null

    result.value = {
      status: ok && matched !== false ? 1 : 0,
      output: stdout,
      error: ok
        ? matched === false
          ? '输出与预期不符'
          : (response.stderr ?? '')
        : stageText + (response.stderr || response.error || STATUS_TEXT[response.status ?? ''] || '运行失败'),
      time: response.time === null ? undefined : Math.round(response.time / 1_000_000),
      memory: response.memory === null ? undefined : Math.round(response.memory / 1024),
    }
  } catch (error) {
    result.value = {
      status: 0,
      output: '',
      error: error instanceof Error ? error.message : '运行失败',
    }
  } finally {
    running.value = false
  }
}

// 提交代码并同步评测全部测试点
async function submitCode() {
  if (!code.value.trim()) {
    ElMessage.warning('请先编写代码')
    return
  }

  const option = LANGUAGE_OPTIONS.find((item) => item.value === language.value)
  if (!option) {
    ElMessage.warning('暂不支持该语言')
    return
  }

  submitting.value = true
  judging.value = false
  result.value = null
  judgeResult.value = null
  stopPolling()
  try {
    const submissionId = await requestSubmitCode({
      questionId: questionId.value,
      language: option.code,
      code: code.value,
    })
    // 提交成功即进入评测中，同时开始轮询
    judging.value = true
    startPolling(submissionId)
  } catch {
    // 提交失败时保持未评测状态，错误提示由请求拦截器统一弹出
    judging.value = false
  } finally {
    submitting.value = false
  }
}

// 轮询评测任务状态，间隔取配置值
function startPolling(submissionId: string) {
  pollTimer = window.setInterval(() => {
    void pollStatus(submissionId)
  }, POLL_INTERVAL_MS)
  // 首次立即查一次，避免必须等一个间隔才有反馈
  void pollStatus(submissionId)
}

// 查一次任务状态，完成后停止轮询并展示结果
async function pollStatus(submissionId: string) {
  try {
    const status = await getSubmissionStatus(submissionId)
    judgeResult.value = status
    if (status.finished) {
      stopPolling()
      judging.value = false
      if (status.accepted) {
        ElMessage.success('全部测试点通过')
      }
    }
  } catch {
    // 查询失败时停止轮询，避免持续弹错
    stopPolling()
    judging.value = false
  }
}

// 停止轮询并清理定时器
function stopPolling() {
  if (pollTimer !== null) {
    window.clearInterval(pollTimer)
    pollTimer = null
  }
}

// 加载题目详情
async function loadDetail() {
  if (!questionId.value) return
  loading.value = true
  try {
    detail.value = await getQuestionDetail(questionId.value)
    // 默认选中第一个可用语言并填入其模板
    const first = availableLanguages.value[0]
    if (first) {
      language.value = first.value
      code.value = currentTemplate()
    }
  } finally {
    loading.value = false
  }
}

onMounted(loadDetail)

// 离开页面时停止轮询，避免定时器泄漏
onUnmounted(stopPolling)
</script>

<style scoped>
.question-detail {
  padding: 0;
}

/* 左题面右代码，窄屏下改为上下排列 */
.question-detail__grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  gap: 24px;
  align-items: start;
}

.question-panel {
  min-width: 0;
}

.question-panel__header {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--problem-border);
}

.question-panel__title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  line-height: 1.35;
  overflow-wrap: anywhere;
}

.question-panel__meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px 16px;
  color: var(--problem-muted);
  font-size: 13px;
}

.question-panel__difficulty {
  font-weight: 600;
}

.question-panel__difficulty--easy {
  color: #00b8a3;
}

.question-panel__difficulty--medium {
  color: #ffc01e;
}

.question-panel__difficulty--hard {
  color: #ff375f;
}

.question-panel__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.question-panel__tag {
  padding: 2px 10px;
  border-radius: 999px;
  background: var(--problem-soft);
  color: var(--problem-muted);
  font-size: 12px;
}

.question-panel__body {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding-top: 20px;
}

.question-panel__heading {
  margin: 8px 0 0;
  font-size: 15px;
  font-weight: 600;
}

.question-panel__samples {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.sample {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 14px 16px;
  border-radius: 8px;
  background: var(--problem-soft);
}

.sample__row {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.sample__label {
  color: var(--problem-muted);
  font-size: 12px;
  font-weight: 600;
}

/* 样例用等宽字体并保留原始空白 */
.sample__code {
  margin: 0;
  font-family: 'JetBrains Mono', 'Cascadia Code', Consolas, monospace;
  font-size: 13px;
  line-height: 1.6;
  white-space: pre-wrap;
  overflow-wrap: anywhere;
}

.code-panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-width: 0;
  padding: 16px;
  border: 1px solid var(--problem-border);
  border-radius: 10px;
}

.code-panel__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.code-panel__language {
  width: 140px;
}

/* 代码区用等宽字体，关闭换行以便横向滚动 */
.code-panel__editor :deep(.el-textarea__inner) {
  font-family: 'JetBrains Mono', 'Cascadia Code', Consolas, monospace;
  font-size: 13px;
  line-height: 1.7;
  tab-size: 4;
  white-space: pre;
  overflow-x: auto;
}

.code-panel__test {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.code-panel__test-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.code-panel__test-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.code-panel__case-select {
  width: 120px;
}

.code-panel__test-title {
  color: var(--problem-muted);
  font-size: 13px;
  font-weight: 600;
}

/* 输入与预期输出并排，窄屏下自动折行 */
.code-panel__test-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 12px;
}

.code-panel__test-field {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0;
}

.code-panel__test-label {
  color: var(--problem-muted);
  font-size: 12px;
  font-weight: 600;
}

.code-panel__test-input :deep(.el-textarea__inner) {
  font-family: 'JetBrains Mono', 'Cascadia Code', Consolas, monospace;
  font-size: 13px;
  line-height: 1.6;
}

/* 运行与提交按钮并排靠右 */
.code-panel__actions {
  display: flex;
  gap: 8px;
  align-self: flex-end;
}

.code-panel__result {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px 14px;
  border-radius: 8px;
  background: var(--problem-soft);
}

.code-panel__result-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.code-panel__result-status {
  font-size: 13px;
  font-weight: 600;
}

.code-panel__result-status--ok {
  color: #00b8a3;
}

.code-panel__result-status--fail {
  color: #ff375f;
}

.code-panel__result-meta {
  color: var(--problem-muted);
  font-size: 12px;
}

.code-panel__result-output,
.code-panel__result-error {
  margin: 0;
  font-family: 'JetBrains Mono', 'Cascadia Code', Consolas, monospace;
  font-size: 13px;
  line-height: 1.6;
  white-space: pre-wrap;
  overflow-wrap: anywhere;
}

.code-panel__result-error {
  color: #ff375f;
}

/* 提交评测结果 */
.code-panel__judge {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 12px 14px;
  border: 1px solid var(--problem-border);
  border-radius: 8px;
}

.code-panel__judge-header {
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  justify-content: space-between;
  gap: 8px 12px;
}

.code-panel__judge-status {
  font-size: 15px;
  font-weight: 700;
}

.code-panel__judge-status--ok {
  color: #00b8a3;
}

.code-panel__judge-status--fail {
  color: #ff375f;
}

.code-panel__judge-meta {
  color: var(--problem-muted);
  font-size: 12px;
}

.code-panel__judge-progress {
  width: 100%;
}

.code-panel__judge-message {
  margin: 0;
  padding: 10px 12px;
  border-radius: 6px;
  background: var(--problem-soft);
  font-family: 'JetBrains Mono', 'Cascadia Code', Consolas, monospace;
  font-size: 13px;
  line-height: 1.6;
  white-space: pre-wrap;
  overflow-wrap: anywhere;
}

.code-panel__judge-table {
  font-size: 13px;
}

@media (max-width: 960px) {
  .question-detail__grid {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
