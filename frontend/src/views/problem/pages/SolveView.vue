<!-- 题目做题页面 -->
<template>
  <section v-loading="loading" class="question-detail">
    <!-- 手机端屏幕放不下左右分栏，用标签在题目与代码之间切换 -->
    <div
      v-if="detail && isMobile"
      class="question-detail__tabs"
      role="tablist"
      aria-label="题目与代码切换"
    >
      <button
        type="button"
        role="tab"
        class="question-detail__tab"
        :class="{ 'is-active': mobileTab === 'question' }"
        :aria-selected="mobileTab === 'question'"
        @click="mobileTab = 'question'"
      >
        题目
      </button>
      <button
        type="button"
        role="tab"
        class="question-detail__tab"
        :class="{ 'is-active': mobileTab === 'code' }"
        :aria-selected="mobileTab === 'code'"
        @click="mobileTab = 'code'"
      >
        代码
      </button>
    </div>

    <el-splitter v-if="detail" class="question-detail__splitter">
      <el-splitter-panel :min="300" :class="{ 'is-hidden': isMobile && mobileTab !== 'question' }">
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
              <span v-for="tag in detail.tags" :key="tag" class="question-panel__tag">{{
                tag
              }}</span>
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

            <!-- 多组样例按示例一、示例二编号；单组样例不显示编号，直接给出输入输出样例 -->
            <div v-if="samples.length" class="question-panel__samples">
              <section v-for="(sample, index) in samples" :key="index" class="sample">
                <h2 v-if="samples.length > 1" class="question-panel__heading">
                  示例{{ chineseNumeral(index + 1) }}
                </h2>

                <h3 class="sample__title">输入样例</h3>
                <!-- 复制按钮浮在色块右上角，鼠标移入才显现 -->
                <div class="sample__block">
                  <el-button
                    class="sample__copy"
                    link
                    type="primary"
                    :icon="Copy"
                    :aria-label="`复制示例${chineseNumeral(index + 1)}的输入样例`"
                    @click="copySample('输入样例', sample.input)"
                  >
                    复制
                  </el-button>
                  <pre class="sample__code">{{ sample.input }}</pre>
                </div>

                <h3 class="sample__title">输出样例</h3>
                <div class="sample__block">
                  <el-button
                    class="sample__copy"
                    link
                    type="primary"
                    :icon="Copy"
                    :aria-label="`复制示例${chineseNumeral(index + 1)}的输出样例`"
                    @click="copySample('输出样例', sample.output)"
                  >
                    复制
                  </el-button>
                  <pre class="sample__code">{{ sample.output }}</pre>
                </div>
              </section>
            </div>

            <template v-if="detail.hint">
              <h2 class="question-panel__heading">提示</h2>
              <MarkdownViewer :model-value="detail.hint" />
            </template>
          </div>
        </article>
      </el-splitter-panel>

      <el-splitter-panel :min="300" :class="{ 'is-hidden': isMobile && mobileTab !== 'code' }">
        <!-- 右侧：代码编辑与运行 -->
        <article class="code-panel">
          <header class="code-panel__header">
            <div class="code-panel__tools">
              <el-select v-model="language" class="code-panel__language" aria-label="编程语言">
                <el-option
                  v-for="option in availableLanguages"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                />
              </el-select>

              <span class="code-panel__mode">{{ judgeModeLabel(detail.judgeMode) }}</span>
            </div>

            <el-button :icon="RotateCcw" title="重置代码" @click="resetCode" />
          </header>

          <!-- 编辑器与测试区上下分栏，可拖拽调整高度 -->
          <el-splitter class="code-panel__splitter" layout="vertical">
            <el-splitter-panel :min="120" size="56%">
              <CodeEditor
                v-model="code"
                class="code-panel__editor"
                :language="currentLanguageCode"
                placeholder="在此编写代码"
              />
            </el-splitter-panel>

            <!-- 下方保留输入区和输出区所需的最小高度 -->
            <el-splitter-panel :min="240">
              <!-- 下方：测试用例与运行结果，整体滚动 -->
              <div class="code-panel__console">
                <div class="code-panel__test">
                  <div class="code-panel__test-header">
                    <!-- 用例标签，力扣式切换 -->
                    <div v-if="testCases.length" class="code-panel__case-tabs">
                      <button
                        v-for="item in testCases"
                        :key="item.caseNo"
                        type="button"
                        class="code-panel__case-tab"
                        :class="{ 'code-panel__case-tab--active': activeCaseNo === item.caseNo }"
                        @click="selectCase(item.caseNo)"
                      >
                        用例 {{ item.caseNo }}
                      </button>
                    </div>
                    <span v-else class="code-panel__test-title">测试用例</span>

                    <el-button
                      v-if="samples.length && !testCases.length"
                      link
                      type="primary"
                      @click="fillSampleCase"
                    >
                      填入示例
                    </el-button>

                    <div class="code-panel__actions">
                      <el-button
                        :icon="Play"
                        :loading="running"
                        aria-label="测试运行"
                        @click="runCode"
                      >
                        <span class="code-panel__action-label">测试运行</span>
                      </el-button>
                      <el-button
                        type="primary"
                        :icon="Send"
                        :loading="submitting"
                        aria-label="提交评测"
                        @click="submitCode"
                      >
                        <span class="code-panel__action-label">提交评测</span>
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
                        :rows="3"
                        resize="none"
                        spellcheck="false"
                        placeholder="手动输入测试数据"
                      />
                    </label>

                    <label v-if="activeCaseExpected !== null" class="code-panel__test-field">
                      <span class="code-panel__test-label">预期输出</span>
                      <el-input
                        v-model="expectedOutput"
                        class="code-panel__test-input"
                        type="textarea"
                        :rows="3"
                        resize="none"
                        spellcheck="false"
                        placeholder="选填，填写后会比对结果"
                      />
                    </label>
                  </div>
                </div>

                <div class="code-panel__output">
                  <span class="code-panel__test-label">输出</span>
                  <div class="code-panel__output-body" role="status" aria-live="polite">
                    <span
                      v-if="!running && !submitting && !result && !judging && !judgeResult"
                      class="code-panel__output-placeholder"
                    >
                      运行或提交后，结果会显示在这里
                    </span>
                    <span v-else-if="running || submitting" class="code-panel__output-placeholder">
                      {{ running ? '运行中…' : '提交中…' }}
                    </span>

                    <div v-if="result" class="code-panel__result">
                      <div class="code-panel__result-header">
                        <span class="code-panel__result-status" :class="resultClass">
                          {{ resultLabel }}
                        </span>
                        <!-- 比对结论紧跟状态，与耗时内存一起靠右排布 -->
                        <span class="code-panel__result-tail">
                          <span v-if="result.hint" class="code-panel__result-hint">
                            {{ result.hint }}
                          </span>
                          <span v-if="result.time !== undefined" class="code-panel__result-meta">
                            {{ result.time }} ms / {{ result.memory }} KB
                          </span>
                        </span>
                      </div>
                      <!-- 状态行固定在框顶，只有输出内容滚动 -->
                      <div class="code-panel__result-scroll">
                        <pre v-if="result.output" class="code-panel__result-output">{{
                          result.output
                        }}</pre>
                        <pre v-if="result.error" class="code-panel__result-error">{{
                          result.error
                        }}</pre>
                      </div>
                    </div>

                    <!-- 评测结果与测试运行结果互斥，避免两块内容同时占位 -->
                    <div v-if="!result && (judging || judgeResult)" class="code-panel__judge">
                      <div class="code-panel__judge-header">
                        <span
                          class="code-panel__judge-status"
                          :style="{ color: judgeStatusColor }"
                        >
                          {{ judgeLabel }}
                        </span>
                        <span v-if="judgeResult?.totalCount" class="code-panel__judge-meta">
                          {{ judgeResult.passedCount ?? 0 }} / {{ judgeResult.totalCount }} 通过
                          <template v-if="judgeResult.timeUsed !== null">
                            · {{ judgeResult.timeUsed }} ms / {{ judgeResult.memoryUsed }} KB
                          </template>
                        </span>
                        <el-button
                          v-if="judgeResult?.finished"
                          type="primary"
                          link
                          class="code-panel__judge-detail"
                          @click="judgeDialogVisible = true"
                        >
                          查看测试点
                        </el-button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </el-splitter-panel>
          </el-splitter>
        </article>
      </el-splitter-panel>
    </el-splitter>

    <el-empty v-else-if="!loading" description="题目不存在或未发布" />

    <!-- 测试点详情弹窗，评测完成后按需打开 -->
    <el-dialog
      v-model="judgeDialogVisible"
      title="评测结果"
      width="880px"
      class="judge-dialog"
      append-to-body
    >
      <JudgeResultPanel v-if="judgeResult" :cases="judgeResult.cases ?? []" />
    </el-dialog>

    <!-- 评测通过时两侧花筒齐放 -->
    <ConfettiClick ref="confettiRef" />
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Copy, Play, RotateCcw, Send } from '@lucide/vue'
import MarkdownViewer from '@/components/editor/MarkdownViewer.vue'
import CodeEditor from '@/components/editor/CodeEditor.vue'
import JudgeResultPanel from '@/views/problem/components/JudgeResultPanel.vue'
import ConfettiClick from '@/components/celebration/ConfettiClick.vue'
import { copyText } from '@/utils/clipboardUtil'
import { chineseNumeral } from '@/utils/numberUtil'
import { getQuestionDetail, type QuestionDetailVO } from '@/api/home/questionBank'
import { runCode as requestRunCode } from '@/api/problem/runCode'
import {
  submitCode as requestSubmitCode,
  getSubmissionStatus,
  type SubmissionStatusVO,
} from '@/api/problem/submitCode'

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
  // 状态行右侧的补充说明，如输出与预期不符
  hint?: string
  time?: number
  memory?: number
}

const route = useRoute()

// 手机端断点，与样式里的媒体查询保持一致
const MOBILE_BREAKPOINT = 960

// 视口是否为手机尺寸，用于切换上下分栏与标签模式
const isMobile = ref(false)

// 手机端当前展示的分区：题目或代码
const mobileTab = ref<'question' | 'code'>('question')

// 同步视口尺寸，resize 时重新判断
function syncMobile() {
  isMobile.value = window.innerWidth <= MOBILE_BREAKPOINT
}

const loading = ref(false)
const running = ref(false)
const detail = ref<QuestionDetailVO | null>(null)
const language = ref<number>(1)
const code = ref('')
const testInput = ref('')
const expectedOutput = ref('')
// 当前激活的用例标签，力扣式切换
const activeCaseNo = ref<number | undefined>(undefined)
const result = ref<RunResult | null>(null)
const submitting = ref(false)
const judging = ref(false)
const judgeResult = ref<SubmissionStatusVO | null>(null)

// 测试点详情弹窗的显示状态
const judgeDialogVisible = ref(false)

// 礼花组件的调用入口，评测通过时触发
const confettiRef = ref<InstanceType<typeof ConfettiClick> | null>(null)

// 轮询定时器，离开页面时需要清理
let pollTimer: number | null = null
const questionId = computed(() => {
  const id = route.params.id
  return typeof id === 'string' ? id : ''
})

// 当前语言标识，供代码编辑器切换语法高亮
const currentLanguageCode = computed(
  () => LANGUAGE_OPTIONS.find((item) => item.value === language.value)?.code ?? 'cpp',
)

// 只展示题目已配置模板的语言
const availableLanguages = computed(() => {
  const configured = new Set((detail.value?.codeTemplates ?? []).map((item) => item.language))
  if (configured.size === 0) return LANGUAGE_OPTIONS
  return LANGUAGE_OPTIONS.filter((option) => configured.has(option.value))
})

// 测试运行可快捷填入的用例，来自后台维护的测试用例表
const testCases = computed(() => detail.value?.testCases ?? [])

// 当前用例的预期输出，隐藏用例为空表示不展示该输入框
const activeCaseExpected = computed(() => {
  if (activeCaseNo.value === undefined) return null
  const matched = testCases.value.find((item) => item.caseNo === activeCaseNo.value)
  return matched?.expected ?? null
})

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
  return (
    detail.value?.codeTemplates?.find((item) => item.language === language.value)?.template ?? ''
  )
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

// 没有配测试用例时用题面样例兜底填入
function fillSampleCase() {
  const first = samples.value[0]
  if (!first) {
    ElMessage.warning('该题目没有示例')
    return
  }
  testInput.value = first.input
  expectedOutput.value = first.output
}

// 切换到指定用例标签并填入其输入
function selectCase(caseNo: number) {
  activeCaseNo.value = caseNo
  const matched = testCases.value.find((item) => item.caseNo === caseNo)
  if (!matched) return
  testInput.value = matched.input ?? ''
  expectedOutput.value = matched.expected ?? ''
}

// 复制样例内容，空内容没有可复制的东西
async function copySample(label: string, text: string) {
  if (!text.trim()) {
    ElMessage.warning(`该${label}为空`)
    return
  }
  const copied = await copyText(text)
  if (copied) ElMessage.success(`已复制${label}`)
  else ElMessage.error('复制失败，请手动选中复制')
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

// 提交结果的状态颜色：只有通过与未通过两种，具体失败原因由测试点卡片区分
const judgeStatusColor = computed(() => {
  if (judging.value || !judgeResult.value) return ''
  return judgeResult.value.accepted ? 'var(--status-success)' : 'var(--status-danger)'
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

// 状态对应的简要文案：优先中文映射，取不到时退回评测机原始状态
function statusText(status: string | null | undefined, fallback: string): string {
  if (!status) return fallback
  return STATUS_TEXT[status] ?? status
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
  // 测试运行与提交评测共用同一块输出区，开始运行前清掉上一次的评测结果
  judgeResult.value = null
  judging.value = false
  stopPolling()
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

    // 失败原因优先取状态中文名，状态取不到时才用评测机报错兜底
    const reason = ok
      ? matched === false
        ? '输出与预期不符'
        : ''
      : statusText(response.status, stageText + (response.error || response.stderr || '运行失败'))

    result.value = {
      status: ok && matched !== false ? 1 : 0,
      output: stdout,
      // 结论展示在状态行右侧，滚动区只放输出的原文与 stderr
      hint: reason || undefined,
      error: ok ? (response.stderr ?? '') : response.stderr || response.error || '',
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

// 查一次任务状态，完成后停止轮询并打开测试点弹窗
async function pollStatus(submissionId: string) {
  try {
    const status = await getSubmissionStatus(submissionId)
    judgeResult.value = status
    if (status.finished) {
      stopPolling()
      judging.value = false
      if (status.accepted) {
        ElMessage.success('全部测试点通过')
        // 通过时两侧花筒齐放
        void confettiRef.value?.celebrate()
      }
      // 评测完成后直接弹出测试点详情，省去手动点击
      judgeDialogVisible.value = true
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
    // 有测试用例时默认激活第一个，省去用户手动点选
    const firstCase = testCases.value[0]
    if (firstCase) {
      selectCase(firstCase.caseNo)
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  syncMobile()
  window.addEventListener('resize', syncMobile)
  void loadDetail()
})

// 离开页面时停止轮询并移除尺寸监听，避免定时器与监听泄漏
onUnmounted(() => {
  stopPolling()
  window.removeEventListener('resize', syncMobile)
})
</script>

<style scoped>
.question-detail {
  height: 100%;
  padding: 0;
}

/* 左右可拖拽分栏，撑满布局区域 */
.question-detail__splitter {
  height: 100%;
}

/* 拖拽条收细，默认几乎不可见，悬停才显现 */
.question-detail__splitter :deep(.el-splitter-bar) {
  width: 8px;
}

.question-detail__splitter :deep(.el-splitter-bar__dragger:before) {
  width: 1px;
  background: var(--problem-border);
  transition:
    width 150ms ease,
    background-color 150ms ease;
}

/* 仅在拖动时加深，悬停不做高亮 */
.question-detail__splitter :deep(.el-splitter-bar__dragger-active:before) {
  width: 2px;
  background: var(--el-color-primary, #1f6feb);
}

.question-panel {
  min-width: 0;
  height: 100%;
  padding: 20px 24px;
  overflow-y: auto;
  /* 隐藏滚动条但保留滚动，避免分栏间出现粗条 */
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.question-panel::-webkit-scrollbar {
  display: none;
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
  color: var(--status-success);
}

.question-panel__difficulty--medium {
  color: var(--status-warning);
}

.question-panel__difficulty--hard {
  color: var(--status-danger);
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
  gap: 20px;
}

/* 每组样例依次排开：示例标题、输入样例、输出样例 */
.sample {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

/* 示例标题与上方内容拉开距离，和输入输出格式等小节区分开 */
.sample .question-panel__heading {
  margin: 4px 0 0;
}

.sample__title {
  margin: 6px 0 0;
  font-size: 14px;
  font-weight: 600;
  line-height: 1.4;
}

/* 样例色块，复制按钮相对它定位在右上角 */
.sample__block {
  position: relative;
}

/* 复制按钮平时隐藏，鼠标移入色块或键盘聚焦时才显现 */
.sample__copy {
  position: absolute;
  top: 50%;
  right: 8px;
  padding: 2px 6px;
  border-radius: 4px;
  background: var(--problem-soft);
  font-size: 12px;
  opacity: 0;
  transform: translateY(-50%);
  transition: opacity 120ms ease;
}

.sample__block:hover .sample__copy,
.sample__copy:focus-visible {
  opacity: 1;
}

/* 触屏设备没有悬停态，直接常显 */
@media (hover: none) {
  .sample__copy {
    opacity: 1;
  }
}

/* 样例用等宽字体并保留原始空白，底纹与题目区的浅色块一致；右侧留出复制按钮的位置 */
.sample__code {
  margin: 0;
  padding: 10px 68px 10px 12px;
  border-radius: 6px;
  background: var(--problem-soft);
  font-family: var(--code-font-family);
  font-size: 13px;
  line-height: 1.6;
  white-space: pre-wrap;
  overflow-wrap: anywhere;
}

.code-panel {
  display: flex;
  flex-direction: column;
  min-width: 0;
  height: 100%;
  padding: 0;
  /* 上下分栏自行撑满，面板内部各自滚动；不留间隙让编辑器贴住上下区域 */
  overflow: hidden;
}

/* 编辑器与测试区的上下分栏，撑满头部以下的空间 */
.code-panel__splitter {
  flex: 1 1 0;
  /* 覆盖组件默认的 height:100%，否则在 flex 列里会被撑到整列高度把测试区挤出可视区 */
  height: auto;
  min-height: 0;
}

/* 分隔条本身不占高度，避免编辑器与下方用例区之间出现空白；拖拽热区由内部 dragger 撑出 */
.code-panel__splitter :deep(.el-splitter-bar) {
  width: 100%;
  height: 0;
}

/* 上下分隔线，仅在拖动时加深，悬停不做高亮 */
.code-panel__splitter :deep(.el-splitter-bar__dragger:before) {
  width: 100%;
  height: 1px;
  background: var(--problem-border);
  transition:
    height 150ms ease,
    background-color 150ms ease;
}

.code-panel__splitter :deep(.el-splitter-bar__dragger-active:before) {
  height: 2px;
  background: var(--el-color-primary, #1f6feb);
}

/* 分栏面板不加内边距，编辑器与用例区左右也贴满 */
.code-panel__splitter :deep(.el-splitter-panel) {
  display: flex;
  flex-direction: column;
  padding: 0;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.code-panel__splitter :deep(.el-splitter-panel::-webkit-scrollbar) {
  display: none;
}

/* 代码工具栏与编辑区分层 */
.code-panel__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex: none;
  height: 44px;
  padding: 0 14px;
  border-bottom: 1px solid var(--problem-border);
  background: color-mix(in srgb, var(--problem-soft) 45%, var(--problem-bg));
}

/* 编辑器去掉圆角与描边，直接贴着工具条与面板边缘 */
.code-panel__editor {
  flex: 1;
  min-height: 0;
  height: auto;
  border: 0;
  border-radius: 0;
}

/* 行号槽与代码之间加一条细分隔线，对齐力扣式编辑器 */
.code-panel__editor :deep(.cm-gutters) {
  border-right: 1px solid var(--problem-border);
}

/* 下半区保留左右留白，隐藏滚动条但保留滚动能力 */
.code-panel__console {
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: 10px;
  min-height: 0;
  padding: 0 16px 16px;
  overflow-y: auto;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.code-panel__console::-webkit-scrollbar {
  display: none;
}

.code-panel__language {
  width: 104px;
}

/* 语言选择与判题模式并排展示 */
.code-panel__tools {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.code-panel__mode {
  padding-left: 12px;
  border-left: 1px solid var(--problem-border);
  color: var(--problem-muted);
  font-size: 12px;
  white-space: nowrap;
}

/* 语言下拉使用轻量边框，形成明确的可点击区域 */
.code-panel__language :deep(.el-select__wrapper) {
  min-height: 30px;
  padding: 0 10px;
  border-radius: 6px;
  background: var(--problem-bg);
  box-shadow: 0 0 0 1px var(--problem-border) inset;
  color: var(--problem-text);
  font-size: 13px;
  font-weight: 600;
}

.code-panel__language :deep(.el-select__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--problem-muted) inset;
}

.code-panel__language :deep(.el-select__wrapper.is-focused) {
  box-shadow: 0 0 0 1px var(--problem-text) inset;
}

.code-panel__language :deep(.el-select__caret) {
  color: var(--problem-muted);
}

/* 重置按钮做成纯文字按钮，与力扣一致 */
.code-panel__header :deep(.el-button) {
  height: 28px;
  padding: 0 10px;
  border: 0;
  background: transparent;
  color: var(--problem-muted);
  font-size: 13px;
}

.code-panel__header :deep(.el-button:hover) {
  background: var(--problem-soft);
  color: var(--problem-text);
}

/* 编辑器字体与缩进由组件内部处理，这里只控制 tab 宽度 */
.code-panel__editor :deep(.cm-content) {
  tab-size: 4;
}

.code-panel__test {
  display: flex;
  flex-direction: column;
  gap: 10px;
  flex: none;
}

.code-panel__test-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  min-width: 0;
  border-bottom: 1px solid var(--problem-border);
}

/* 用例标签组，力扣式下划线切换 */
.code-panel__case-tabs {
  display: flex;
  flex: 1 1 auto;
  gap: 2px;
  min-width: 0;
  margin-bottom: -1px;
  overflow-x: auto;
  scrollbar-width: none;
}

.code-panel__case-tabs::-webkit-scrollbar {
  display: none;
}

.code-panel__case-tab {
  position: relative;
  flex: none;
  /* 纵向内边距撑高头部，按钮随之与上下边框拉开距离；下划线仍贴住底边框 */
  padding: 16px 14px;
  border: 0;
  background: transparent;
  color: var(--problem-muted);
  font-family: inherit;
  font-size: 13px;
  line-height: 18px;
  cursor: pointer;
  transition: color 150ms ease;
}

.code-panel__case-tab::after {
  content: '';
  position: absolute;
  right: 14px;
  bottom: 0;
  left: 14px;
  height: 2px;
  border-radius: 1px;
  background: transparent;
  transition: background-color 150ms ease;
}

.code-panel__case-tab:hover {
  color: var(--problem-text);
}

/* 当前用例：文字变主题色，下方显示短横线 */
.code-panel__case-tab--active {
  color: var(--el-color-primary, #1f6feb);
  font-weight: 600;
}

.code-panel__case-tab--active::after {
  background: var(--el-color-primary, #1f6feb);
}

.code-panel__test-title {
  /* 与用例标签同高，保证头部高度一致 */
  padding: 16px 0;
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
  padding: 8px 10px;
  border-radius: 6px;
  background: var(--problem-soft);
  color: var(--problem-text);
  font-family: var(--code-font-family);
  font-size: 13px;
  line-height: 1.6;
  scrollbar-color: var(--problem-scrollbar) transparent;
  scrollbar-width: thin;
  /* 力扣式无边框，只靠浅灰底区分 */
  box-shadow: none;
}

.code-panel__test-input :deep(.el-textarea__inner::-webkit-scrollbar) {
  width: 8px;
}

.code-panel__test-input :deep(.el-textarea__inner::-webkit-scrollbar-thumb) {
  border: 2px solid var(--problem-soft);
  border-radius: 999px;
  background: var(--problem-scrollbar);
}

.code-panel__test-input :deep(.el-textarea__inner::-webkit-scrollbar-track) {
  background: transparent;
}

.code-panel__test-input :deep(.el-textarea__inner:hover) {
  box-shadow: none;
}

.code-panel__test-input :deep(.el-textarea__inner:focus) {
  background: var(--problem-soft);
  box-shadow: 0 0 0 1px var(--el-color-primary, #1f6feb) inset;
}

.code-panel__test-input :deep(.el-textarea__inner::placeholder) {
  color: var(--problem-muted);
}

/* 运行与提交按钮固定在用例标签栏右侧 */
.code-panel__actions {
  display: flex;
  flex: none;
  align-items: center;
  gap: 8px;
  margin-left: auto;
}

.code-panel__actions :deep(.el-button + .el-button) {
  margin-left: 0;
}

.code-panel__output {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0;
}

/* 固定高度，内容超出时在框内滚动，避免结果把整个测试区顶开 */
.code-panel__output-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
  height: 180px;
  min-width: 0;
  padding: 10px 12px;
  border-radius: 6px;
  background: var(--problem-soft);
  color: var(--problem-text);
  /* 状态行与结果区自行分配高度，滚动只发生在结果区 */
  overflow: hidden;
}

.code-panel__output-placeholder {
  color: var(--problem-muted);
  font-size: 13px;
}

/* 测试运行结果：状态行占位不缩放，余下高度都给滚动区 */
.code-panel__result {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-height: 0;
}

/* 提交评测结果：同上 */
.code-panel__judge {
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-height: 0;
}

.code-panel__result-scroll {
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: 8px;
  min-height: 0;
  overflow-y: auto;
  scrollbar-color: var(--problem-scrollbar) transparent;
  scrollbar-width: thin;
}

/* 滚动条贴着面板底色，与题目区的细滚动条保持一致 */
.code-panel__result-scroll::-webkit-scrollbar {
  width: 8px;
}

.code-panel__result-scroll::-webkit-scrollbar-thumb {
  border: 2px solid var(--problem-soft);
  border-radius: 999px;
  background: var(--problem-scrollbar);
}

.code-panel__result-scroll::-webkit-scrollbar-track {
  background: transparent;
}

.code-panel__result-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex: none;
}

.code-panel__result-status {
  font-size: 13px;
  font-weight: 600;
}

.code-panel__result-status--ok {
  color: var(--status-success);
}

.code-panel__result-status--fail {
  color: var(--status-danger);
}

/* 状态行右侧：比对结论与耗时内存并排，窄屏可折行 */
.code-panel__result-tail {
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  justify-content: flex-end;
  gap: 4px 12px;
  min-width: 0;
}

/* 比对结论用警示色，与下方输出内容区分开 */
.code-panel__result-hint {
  color: var(--status-danger);
  font-size: 12px;
}

.code-panel__result-meta {
  color: var(--problem-muted);
  font-size: 12px;
}

.code-panel__result-output,
.code-panel__result-error {
  margin: 0;
  font-family: var(--code-font-family);
  font-size: 13px;
  line-height: 1.6;
  white-space: pre-wrap;
  overflow-wrap: anywhere;
}

.code-panel__result-error {
  color: var(--status-danger);
}

/* 提交评测结果 */
.code-panel__judge-header {
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  justify-content: space-between;
  gap: 8px 12px;
  flex: none;
}

.code-panel__judge-status {
  font-size: 15px;
  font-weight: 700;
}

.code-panel__judge-meta {
  color: var(--problem-muted);
  font-size: 12px;
}

/* 状态行右侧的“查看测试点”入口，不与耗时内存抢位置 */
.code-panel__judge-detail {
  flex: none;
  margin-left: 12px;
  font-size: 13px;
}

/* 窄屏：改为标签切换，题目与代码各自占满整屏 */
@media (max-width: 960px) {
  .question-detail {
    display: flex;
    flex-direction: column;
    height: 100%;
    overflow: hidden;
  }

  /* 标签栏固定在顶部，切换题目与代码 */
  .question-detail__tabs {
    display: flex;
    flex: none;
    gap: 4px;
    padding: 6px 12px 0;
    border-bottom: 1px solid var(--problem-border);
  }

  .question-detail__tab {
    flex: 1;
    padding: 8px 12px;
    border: 0;
    border-bottom: 2px solid transparent;
    background: transparent;
    color: var(--problem-muted);
    font-family: inherit;
    font-size: 14px;
    cursor: pointer;
    transition:
      color 150ms ease,
      border-color 150ms ease;
  }

  .question-detail__tab.is-active {
    border-bottom-color: var(--el-color-primary, #1f6feb);
    color: var(--problem-text);
    font-weight: 600;
  }

  /* 分栏退化为单列，只保留一个可见面板 */
  .question-detail__splitter {
    flex: 1;
    height: auto;
    min-height: 0;
  }

  .question-detail__splitter :deep(.el-splitter-bar) {
    display: none;
  }

  /* 面板宽度由组件内联 flex-basis 控制，这里用 !important 覆盖成整宽 */
  .question-detail__splitter :deep(.el-splitter-panel) {
    display: none;
    flex: 1 1 100% !important;
    max-width: 100% !important;
    height: 100%;
    overflow: hidden;
  }

  /* 面板改为纵向排列，让内部题目区与代码区各自撑满宽度 */
  .question-detail__splitter :deep(.el-splitter-panel:not(.is-hidden)) {
    display: flex;
    flex-direction: column;
  }

  /* 题目区自己滚动；代码区由内部上下分栏各自滚动，外层不再滚动 */
  .question-panel {
    padding: 16px 14px 24px;
    overflow-y: auto;
  }

  .question-panel__title {
    font-size: 20px;
  }

  .code-panel {
    padding-top: 0;
    overflow: hidden;
  }

  /* 代码区上下分栏给固定高度，避免 flex 撑不满导致测试区被压缩 */
  .code-panel__splitter {
    flex: 1 1 auto;
    height: auto;
    min-height: 0;
  }

  /* 手机端给测试区内边距，输入框与输出框不贴着屏幕边缘 */
  .code-panel__console {
    gap: 12px;
    padding: 0 12px 12px;
  }

  /* 用例输入与预期输出在窄屏改为一列，避免每个输入框只剩太窄的宽度 */
  .code-panel__test-grid {
    grid-template-columns: 1fr;
  }

  /* 触屏上没有悬停，按钮保持足够的点击高度 */
  .code-panel__actions :deep(.el-button) {
    height: 36px;
  }
}

@media (max-width: 560px) {
  /* 更小屏幕下工具条压缩，语言与重置按钮不换行 */
  .code-panel__header {
    padding: 0 10px;
  }

  .question-panel__meta {
    gap: 6px 12px;
    font-size: 12px;
  }

  /* 窄屏只显示按钮图标，给用例标签留出横向空间 */
  .code-panel__test-header {
    gap: 4px;
  }

  .code-panel__actions {
    gap: 6px;
  }

  .code-panel__action-label {
    display: none;
  }

  .code-panel__actions :deep(.el-button) {
    width: 36px;
    padding: 0;
  }
}
</style>
