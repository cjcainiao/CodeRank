<!-- 后台题目预览，按用户端样式展示当前编辑内容 -->
<template>
  <div class="question-preview">
    <header class="question-preview__header">
      <h1 class="question-preview__title">{{ title || '未填写标题' }}</h1>

      <div class="question-preview__meta">
        <el-tag :type="difficultyTagType" size="small">{{ difficultyLabel }}</el-tag>
        <span class="question-preview__meta-item">{{ judgeModeLabel }}</span>
        <span class="question-preview__meta-item">时间 {{ timeLimit }} ms</span>
        <span class="question-preview__meta-item">内存 {{ memoryLimit }} MB</span>
        <span v-if="source" class="question-preview__meta-item">{{ source }}</span>
      </div>

      <div v-if="tags.length" class="question-preview__tags">
        <el-tag
          v-for="tag in tags"
          :key="tag.id"
          size="small"
          :color="tag.color || undefined"
          :style="tagStyle(tag.color)"
        >
          {{ tag.name }}
        </el-tag>
      </div>
    </header>

    <section class="question-preview__section">
      <h2 class="question-preview__heading">题目描述</h2>
      <MarkdownViewer v-if="description" :model-value="description" />
      <p v-else class="question-preview__empty">尚未填写题目描述</p>
    </section>

    <!-- 输入输出格式只在标准输入输出模式下展示 -->
    <template v-if="judgeMode === JUDGE_MODE_STANDARD">
      <section class="question-preview__section">
        <h2 class="question-preview__heading">输入格式</h2>
        <MarkdownViewer v-if="inputFormat" :model-value="inputFormat" />
        <p v-else class="question-preview__empty">尚未填写输入格式</p>
      </section>

      <section class="question-preview__section">
        <h2 class="question-preview__heading">输出格式</h2>
        <MarkdownViewer v-if="outputFormat" :model-value="outputFormat" />
        <p v-else class="question-preview__empty">尚未填写输出格式</p>
      </section>
    </template>

    <section class="question-preview__section">
      <h2 class="question-preview__heading">样例</h2>
      <div v-if="filledSamples.length" class="question-preview__samples">
        <article v-for="(sample, index) in filledSamples" :key="index" class="question-preview__sample">
          <div class="question-preview__sample-row">
            <span class="question-preview__sample-label">输入</span>
            <pre class="question-preview__sample-code">{{ sample.input || '（空）' }}</pre>
          </div>
          <div class="question-preview__sample-row">
            <span class="question-preview__sample-label">输出</span>
            <pre class="question-preview__sample-code">{{ sample.output || '（空）' }}</pre>
          </div>
        </article>
      </div>
      <p v-else class="question-preview__empty">尚未填写样例</p>
    </section>

    <section v-if="hint" class="question-preview__section">
      <h2 class="question-preview__heading">提示</h2>
      <MarkdownViewer :model-value="hint" />
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import MarkdownViewer from '@/components/editor/MarkdownViewer.vue'

// 判题模式：标准输入输出
const JUDGE_MODE_STANDARD = 1

// 难度文案映射
const DIFFICULTY_LABELS: Record<number, string> = {
  1: '简单',
  2: '中等',
  3: '困难',
}

// 判题模式文案映射
const JUDGE_MODE_LABELS: Record<number, string> = {
  1: '标准输入输出',
  2: '核心代码',
}

// 样例结构，与编辑页保持一致
export interface PreviewSample {
  input: string
  output: string
}

// 预览用标签
export interface PreviewTag {
  id: string
  name: string
  color: string | null
}

const props = withDefaults(
  defineProps<{
    // 题目标题
    title?: string
    // 题目描述，Markdown
    description?: string
    // 输入格式说明，Markdown
    inputFormat?: string
    // 输出格式说明，Markdown
    outputFormat?: string
    // 解题提示，Markdown
    hint?: string
    // 难度：1 简单，2 中等，3 困难
    difficulty?: number
    // 判题模式：1 标准输入输出，2 核心代码
    judgeMode?: number
    // 时间限制，毫秒
    timeLimit?: number
    // 内存限制，MB
    memoryLimit?: number
    // 题目来源
    source?: string
    // 样例列表
    samples?: PreviewSample[]
    // 已选标签
    tags?: PreviewTag[]
  }>(),
  {
    title: '',
    description: '',
    inputFormat: '',
    outputFormat: '',
    hint: '',
    difficulty: 1,
    judgeMode: JUDGE_MODE_STANDARD,
    timeLimit: 1000,
    memoryLimit: 128,
    source: '',
    samples: () => [],
    tags: () => [],
  },
)

// 只展示有内容的样例，避免空块
const filledSamples = computed(() =>
  props.samples.filter((sample) => sample.input.trim() || sample.output.trim()),
)

const difficultyLabel = computed(() => DIFFICULTY_LABELS[props.difficulty] ?? '-')

const judgeModeLabel = computed(() => JUDGE_MODE_LABELS[props.judgeMode] ?? '-')

// 难度对应的标签类型
const difficultyTagType = computed<'success' | 'warning' | 'danger'>(() => {
  if (props.difficulty === 1) return 'success'
  if (props.difficulty === 2) return 'warning'
  return 'danger'
})

// 标签按自定义色渲染
function tagStyle(color: string | null): Record<string, string> | undefined {
  if (!color) return undefined
  return { color: '#fff', borderColor: color }
}
</script>

<style scoped>
.question-preview {
  display: flex;
  flex-direction: column;
  gap: 26px;
  padding: 28px 32px 36px;
  color: var(--backend-text);
}

.question-preview__header {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--backend-border);
}

.question-preview__title {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  line-height: 1.35;
  overflow-wrap: anywhere;
}

.question-preview__meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px 16px;
  color: var(--backend-muted);
  font-size: 13px;
}

.question-preview__meta-item {
  white-space: nowrap;
}

.question-preview__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.question-preview__section {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.question-preview__heading {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.question-preview__empty {
  margin: 0;
  color: var(--backend-muted);
  font-size: 13px;
}

.question-preview__samples {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.question-preview__sample {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 14px 16px;
  border: 1px solid var(--backend-border);
  border-radius: 8px;
  background: color-mix(in srgb, var(--backend-active) 40%, transparent);
}

.question-preview__sample-row {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.question-preview__sample-label {
  color: var(--backend-muted);
  font-size: 12px;
  font-weight: 600;
}

/* 样例用等宽字体并保留原始空白 */
.question-preview__sample-code {
  margin: 0;
  font-family: 'JetBrains Mono', 'Cascadia Code', Consolas, monospace;
  font-size: 13px;
  line-height: 1.6;
  white-space: pre-wrap;
  overflow-wrap: anywhere;
}

@media (max-width: 760px) {
  .question-preview {
    padding: 20px 16px 28px;
  }

  .question-preview__title {
    font-size: 20px;
  }
}
</style>
