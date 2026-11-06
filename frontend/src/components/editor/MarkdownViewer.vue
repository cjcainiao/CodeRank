<!-- 通用 Markdown 只读渲染 -->
<template>
  <div class="markdown-viewer" :style="{ maxHeight }">
    <MdPreview
      v-model="content"
      :theme="theme"
      :language="LANGUAGE"
      :preview-theme="previewTheme"
      :no-mermaid="noMermaid"
      :no-katex="noKatex"
      :no-echarts="noEcharts"
      :no-highlight="noHighlight"
    />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { MdPreview } from 'md-editor-v3'
import { useThemeMode } from '@/utils/themeModeUtil'
import 'md-editor-v3/lib/preview.css'

withDefaults(
  defineProps<{
    // 最大高度，超出后滚动，不传随内容撑开
    maxHeight?: string
    // 预览区主题
    previewTheme?: string
    // 关闭 mermaid 渲染，默认关闭避免加载 CDN 资源
    noMermaid?: boolean
    // 关闭 katex 公式渲染，默认关闭避免加载 CDN 资源
    noKatex?: boolean
    // 关闭 echarts 图表渲染，默认关闭避免加载 CDN 资源
    noEcharts?: boolean
    // 关闭代码高亮
    noHighlight?: boolean
  }>(),
  {
    maxHeight: undefined,
    previewTheme: 'github',
    noMermaid: true,
    noKatex: true,
    noEcharts: true,
    noHighlight: false,
  },
)

const content = defineModel<string>({ default: '' })

// 渲染区界面语言
const LANGUAGE = 'zh-CN'

const { darkMode } = useThemeMode()

// 跟随站点的明暗配色
const theme = computed(() => (darkMode.value ? 'dark' : 'light'))
</script>

<style scoped>
.markdown-viewer {
  overflow-y: auto;
}

/* 预览背景与页面主题一致，避免暗色模式出现纯黑内容块 */
.markdown-viewer :deep(.md-editor) {
  --md-bk-color: var(--backend-surface);
  --md-color: var(--backend-text);
  --md-border-color: var(--backend-border);
  --md-scrollbar-bg-color: var(--backend-surface);
  --md-scrollbar-thumb-color: var(--backend-muted);
}

/* 去掉只读渲染的默认外边距，交由父容器控制留白 */
.markdown-viewer :deep(.md-editor-preview-wrapper) {
  padding: 0;
}
</style>
