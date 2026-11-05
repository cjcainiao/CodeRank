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
              <el-button link type="primary" @click="fillSample">填入示例</el-button>
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
                  placeholder="手动输入测试数据"
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

            <el-button
              class="code-panel__run"
              type="primary"
              :icon="Play"
              :loading="running"
              @click="runCode"
            >
              测试运行
            </el-button>
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
        </article>
    </div>

    <el-empty v-else-if="!loading" description="题目不存在或未发布" />
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Play, RotateCcw } from '@lucide/vue'
import MarkdownViewer from '@/components/editor/MarkdownViewer.vue'
import { getQuestionDetail, type QuestionDetailVO } from '@/api/home/questionBank'

// 判题模式：标准输入输出
const JUDGE_MODE_STANDARD = 1

// 难度选项：1 简单，2 中等，3 困难
const DIFFICULTY_OPTIONS = [
  { value: 1, label: '简单' },
  { value: 2, label: '中等' },
  { value: 3, label: '困难' },
] as const

// 语言选项，与后端枚举一致
const LANGUAGE_OPTIONS = [
  { value: 1, label: 'Java' },
  { value: 2, label: 'C++' },
  { value: 3, label: 'Python' },
  { value: 4, label: 'Go' },
  { value: 5, label: 'JavaScript' },
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
const result = ref<RunResult | null>(null)
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

// 各语言的源文件名、编译命令与运行命令
const LANGUAGE_CONFIG: Record<number, { filename: string; compile?: string[]; run: string[] }> = {
  1: {
    filename: 'Main.java',
    compile: ['/usr/bin/javac', 'Main.java'],
    run: ['/usr/bin/java', 'Main'],
  },
  2: {
    filename: 'main.cpp',
    compile: ['/usr/bin/g++', '-O2', '-o', 'main', 'main.cpp'],
    run: ['./main'],
  },
  3: { filename: 'main.py', run: ['/usr/bin/python3', 'main.py'] },
  4: {
    filename: 'main.go',
    compile: ['/usr/bin/go', 'build', '-o', 'main', 'main.go'],
    run: ['./main'],
  },
  5: { filename: 'main.js', run: ['/usr/bin/node', 'main.js'] },
}

// 评测机地址，开发环境通过 vite 代理转发
const JUDGE_ENDPOINT = '/judge'

// 时间与内存上限，单位为评测机要求的纳秒与字节
const CPU_LIMIT_NS = 5_000_000_000
const CLOCK_LIMIT_NS = 10_000_000_000
const MEMORY_LIMIT_BYTE = 268_435_456
const OUTPUT_LIMIT_BYTE = 1_048_576

// 评测机单条命令
interface JudgeCmd {
  args: string[]
  env: string[]
  files: { content?: string; name?: string; max?: number }[]
  cpuLimit: number
  clockLimit: number
  memoryLimit: number
  procLimit: number
}

// 评测机返回的单条结果
interface JudgeResult {
  status: string
  exitStatus: number
  error?: string
  time: number
  memory: number
  files?: Record<string, string>
}

// 组装评测机请求，编译与运行分两步
function buildJudgeCmds(config: (typeof LANGUAGE_CONFIG)[number]): JudgeCmd[] {
  const env = ['PATH=/usr/bin:/bin:/usr/local/bin']
  const limits = {
    cpuLimit: CPU_LIMIT_NS,
    clockLimit: CLOCK_LIMIT_NS,
    memoryLimit: MEMORY_LIMIT_BYTE,
    procLimit: 64,
  }
  const cmds: JudgeCmd[] = []

  // 需要编译的语言先编译，源码通过首个文件写入
  if (config.compile) {
    cmds.push({
      args: config.compile,
      env,
      files: [
        { content: code.value },
        { name: 'stdout', max: OUTPUT_LIMIT_BYTE },
        { name: 'stderr', max: OUTPUT_LIMIT_BYTE },
      ],
      ...limits,
    })
  }

  // 运行阶段：编译型语言直接读上一步的产物，解释型语言先把源码写入文件
  const runFiles: JudgeCmd['files'] = []
  if (!config.compile) {
    runFiles.push({ name: config.filename, content: code.value })
  }
  runFiles.push({ content: testInput.value })
  runFiles.push({ name: 'stdout', max: OUTPUT_LIMIT_BYTE })
  runFiles.push({ name: 'stderr', max: OUTPUT_LIMIT_BYTE })

  cmds.push({ args: config.run, env, files: runFiles, ...limits })
  return cmds
}

// 把评测机状态翻译成用户能看懂的文案
function translateStatus(status: string): string {
  const map: Record<string, string> = {
    Accepted: '运行完成',
    'Time Limit Exceeded': '超出时间限制',
    'Memory Limit Exceeded': '超出内存限制',
    'Runtime Error': '运行错误',
    'Internal Error': '评测机内部错误',
    'Invalid Request': '请求参数有误',
  }
  return map[status] ?? status
}

// 运行代码，调用评测机执行
async function runCode() {
  if (!code.value.trim()) {
    ElMessage.warning('请先编写代码')
    return
  }

  const config = LANGUAGE_CONFIG[language.value]
  if (!config) {
    ElMessage.warning('暂不支持该语言')
    return
  }

  running.value = true
  result.value = null
  try {
    const response = await fetch(`${JUDGE_ENDPOINT}/run`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ cmd: buildJudgeCmds(config) }),
    })
    if (!response.ok) throw new Error(`评测机返回 ${response.status}`)

    const results = (await response.json()) as JudgeResult[]
    const last = results[results.length - 1]
    if (!last) throw new Error('评测机没有返回结果')

    // 编译失败时直接展示编译器输出，不看运行结果
    const compileResult = config.compile ? results[0] : undefined
    if (compileResult && compileResult.status !== 'Accepted') {
      result.value = {
        status: 0,
        output: '',
        error: compileResult.files?.stderr || translateStatus(compileResult.status),
        time: Math.round(compileResult.time / 1_000_000),
        memory: Math.round(compileResult.memory / 1024),
      }
      return
    }

    const ok = last.status === 'Accepted'
    const stdout = last.files?.stdout ?? ''
    // 填了预期输出时以比对结果决定状态
    const matched = ok ? matchExpected(stdout) : null
    result.value = {
      status: ok && matched !== false ? 1 : 0,
      output: stdout,
      error: ok
        ? matched === false
          ? '输出与预期不符'
          : (last.files?.stderr ?? '')
        : translateStatus(last.status),
      time: Math.round(last.time / 1_000_000),
      memory: Math.round(last.memory / 1024),
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

/* 运行按钮紧跟在测试用例下方，靠右对齐 */
.code-panel__run {
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

@media (max-width: 960px) {
  .question-detail__grid {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
