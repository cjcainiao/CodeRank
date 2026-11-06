<!-- 后台代码测试提交页面 -->
<template>
  <section class="test-submit">
    <header class="test-submit__header">
      <h1 class="test-submit__title">代码测试</h1>
    </header>

    <div class="test-submit__toolbar">
      <el-select
        v-model="machineId"
        class="test-submit__machine"
        placeholder="选择评测机"
        :loading="machinesLoading"
      >
        <el-option
          v-for="machine in machines"
          :key="machine.id"
          :label="machine.name"
          :value="machine.id"
          :disabled="machine.status !== STATUS_ENABLED"
        />
      </el-select>

      <el-select v-model="language" class="test-submit__language">
        <el-option
          v-for="option in LANGUAGE_OPTIONS"
          :key="option.value"
          :label="option.label"
          :value="option.value"
        />
      </el-select>

      <el-button :icon="RotateCcw" @click="resetCode">重置代码</el-button>
      <el-button type="primary" :icon="Play" :loading="submitting" @click="submitCode">
        提交运行
      </el-button>
    </div>

    <div class="test-submit__grid">
      <label class="test-submit__field">
        <span class="test-submit__label">代码</span>
        <el-input
          v-model="code"
          class="test-submit__editor"
          type="textarea"
          :rows="22"
          resize="none"
          spellcheck="false"
          placeholder="在此编写代码"
        />
      </label>

      <label class="test-submit__field">
        <span class="test-submit__label">标准输入</span>
        <el-input
          v-model="input"
          class="test-submit__input"
          type="textarea"
          :rows="8"
          resize="vertical"
          spellcheck="false"
          placeholder="选填，作为程序的标准输入"
        />
      </label>
    </div>

    <div v-if="result" class="test-submit__result">
      <div class="test-submit__result-header">
        <el-tag :type="resultTagType" size="large">{{ result.status ?? '未知状态' }}</el-tag>
        <span class="test-submit__result-meta">
          {{ stageLabel }} · 退出码 {{ result.exitStatus ?? '-' }} ·
          {{ formatDuration(result.time) }} / {{ formatBytes(result.memory) }}
        </span>
      </div>

      <div v-if="result.error" class="test-submit__result-block">
        <div class="test-submit__result-head">
          <span class="test-submit__result-label">评测机错误</span>
        </div>
        <pre class="test-submit__result-error">{{ result.error }}</pre>
      </div>

      <div class="test-submit__result-block">
        <div class="test-submit__result-head">
          <span class="test-submit__result-label">输出</span>
          <el-button
            link
            type="primary"
            :icon="Copy"
            :disabled="!result.stdout"
            @click="copyText(result.stdout)"
          >
            复制
          </el-button>
        </div>
        <pre class="test-submit__result-output">{{ result.stdout || '（无输出）' }}</pre>
      </div>

      <div v-if="result.stderr" class="test-submit__result-block">
        <div class="test-submit__result-head">
          <span class="test-submit__result-label">错误输出</span>
          <el-button link type="primary" :icon="Copy" @click="copyText(result.stderr)">
            复制
          </el-button>
        </div>
        <pre class="test-submit__result-error">{{ result.stderr }}</pre>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Play, RotateCcw, Copy } from '@lucide/vue'
import { pageJudgeMachines, type JudgeMachineVO } from '@/api/backend/judgeMachine'
import { submitTestCode, type TestSubmitResultVO } from '@/api/backend/testSubmit'

// 状态：启用
const STATUS_ENABLED = 1

// 阶段：编译
const STAGE_COMPILE = 'compile'

// 判题状态：通过
const JUDGE_STATUS_ACCEPTED = 'Accepted'

// 可选语言，与后端 LanguageEnum 的 code 对应
const LANGUAGE_OPTIONS = [
  { value: 'java', label: 'Java' },
  { value: 'cpp', label: 'C++' },
  { value: 'python', label: 'Python' },
  { value: 'go', label: 'Go' },
  { value: 'javascript', label: 'JavaScript' },
]

// 各语言的初始代码
const CODE_TEMPLATES: Record<string, string> = {
  java: `import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int a = scanner.nextInt();
        int b = scanner.nextInt();
        System.out.println(a + b);
    }
}`,
  cpp: `#include <bits/stdc++.h>

int main() {
    int a, b;
    std::cin >> a >> b;
    std::cout << a + b << std::endl;
    return 0;
}`,
  python: `a, b = map(int, input().split())
print(a + b)`,
  go: `package main

import "fmt"

func main() {
    var a, b int
    fmt.Scan(&a, &b)
    fmt.Println(a + b)
}`,
  javascript: `const lines = require('fs').readFileSync(0, 'utf-8').trim().split(/\\s+/)
const [a, b] = lines.map(Number)
console.log(a + b)`,
}

const machines = ref<JudgeMachineVO[]>([])
const machinesLoading = ref(false)
const machineId = ref<string>('')
const language = ref<string>('python')
const code = ref<string>(CODE_TEMPLATES.python ?? '')
const input = ref<string>('')
const submitting = ref(false)
const result = ref<TestSubmitResultVO | null>(null)

// 切换语言时同步默认代码
watch(language, (value) => {
  code.value = CODE_TEMPLATES[value] ?? ''
  result.value = null
})

// 阶段名称展示
const stageLabel = computed(() => (result.value?.stage === STAGE_COMPILE ? '编译阶段' : '运行阶段'))

// 结果标签配色
const resultTagType = computed(() => {
  if (!result.value) return 'info'
  return result.value.status === JUDGE_STATUS_ACCEPTED ? 'success' : 'danger'
})

// 纳秒转毫秒展示
function formatDuration(nanoseconds: number | null): string {
  if (nanoseconds === null) return '-'
  return `${Math.round(nanoseconds / 1_000_000)} ms`
}

// 字节转 MB 展示
function formatBytes(bytes: number | null): string {
  if (bytes === null) return '-'
  return `${(bytes / 1024 / 1024).toFixed(2)} MB`
}

// 加载可用的评测机，默认选中第一台启用的机器
async function loadMachines() {
  machinesLoading.value = true
  try {
    const page = await pageJudgeMachines({ pageNum: 1, pageSize: 100 })
    machines.value = page.records
    const first = page.records.find((item) => item.status === STATUS_ENABLED)
    if (first && !machineId.value) machineId.value = first.id
  } finally {
    machinesLoading.value = false
  }
}

// 重置为当前语言的初始代码
function resetCode() {
  code.value = CODE_TEMPLATES[language.value] ?? ''
  result.value = null
}

// 提交代码到所选评测机运行
async function submitCode() {
  if (!machineId.value) {
    ElMessage.warning('请先选择评测机')
    return
  }
  if (!code.value.trim()) {
    ElMessage.warning('请先编写代码')
    return
  }

  submitting.value = true
  try {
    result.value = await submitTestCode({
      machineId: machineId.value,
      language: language.value,
      code: code.value,
      input: input.value,
    })
  } finally {
    submitting.value = false
  }
}

// 复制文本到剪贴板
async function copyText(text: string | null) {
  if (!text) return
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success('已复制')
  } catch {
    ElMessage.error('复制失败，请手动选中复制')
  }
}

onMounted(loadMachines)
</script>

<style scoped>
.test-submit {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.test-submit__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.test-submit__title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
}

.test-submit__toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.test-submit__machine {
  width: 200px;
}

.test-submit__language {
  width: 140px;
}

.test-submit__grid {
  display: grid;
  gap: 16px;
}

.test-submit__field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.test-submit__label {
  color: var(--backend-muted);
  font-size: 13px;
}

.test-submit__editor :deep(.el-textarea__inner),
.test-submit__input :deep(.el-textarea__inner) {
  font-family: 'JetBrains Mono', 'Cascadia Code', Consolas, monospace;
  font-size: 13px;
  line-height: 1.6;
}

.test-submit__result {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 16px;
  border: 1px solid var(--backend-border);
  border-radius: 8px;
}

.test-submit__result-header {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
}

.test-submit__result-meta {
  color: var(--backend-muted);
  font-size: 13px;
}

.test-submit__result-block {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.test-submit__result-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.test-submit__result-label {
  color: var(--backend-muted);
  font-size: 13px;
}

.test-submit__result-output,
.test-submit__result-error {
  margin: 0;
  padding: 12px;
  max-height: 320px;
  overflow: auto;
  border-radius: 6px;
  font-family: 'JetBrains Mono', 'Cascadia Code', Consolas, monospace;
  font-size: 13px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.test-submit__result-output {
  background: var(--backend-active);
}

.test-submit__result-error {
  background: rgb(245 108 108 / 10%);
  color: #c45656;
}
</style>
