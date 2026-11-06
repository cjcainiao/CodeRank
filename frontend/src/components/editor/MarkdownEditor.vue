<!-- 通用 Markdown 编辑器 -->
<template>
  <div class="markdown-editor" :style="{ height }">
    <MdEditor
      ref="editorRef"
      v-model="content"
      :editor-id="resolvedEditorId"
      :theme="theme"
      :language="LANGUAGE"
      :preview-theme="previewTheme"
      :placeholder="placeholder"
      :read-only="readOnly"
      :disabled="disabled"
      :max-length="maxLength"
      :toolbars-exclude="resolvedToolbarsExclude"
      :no-mermaid="noMermaid"
      :no-katex="noKatex"
      :no-echarts="noEcharts"
      :no-highlight="noHighlight"
      :no-upload-img="!uploadImage"
      :on-upload-img="handleUploadImg"
      :on-save="handleSave"
      :on-change="handleChange"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, ref, useId } from 'vue'
import { ElMessage } from 'element-plus'
import { MdEditor } from 'md-editor-v3'
import type {
  ExposeParam,
  FocusOption,
  InsertContentGenerator,
  ToolbarNames,
  UploadImgCallBack,
} from 'md-editor-v3'
import { useThemeMode } from '@/utils/themeModeUtil'
import 'md-editor-v3/lib/style.css'

const props = withDefaults(
  defineProps<{
    // 编辑器高度
    height?: string
    // 占位提示
    placeholder?: string
    // 预览区主题
    previewTheme?: string
    // 只读
    readOnly?: boolean
    // 禁用
    disabled?: boolean
    // 最大字符数，不传不限制
    maxLength?: number
    // 需要排除的工具栏按钮，不传使用默认规则
    toolbarsExclude?: ToolbarNames[]
    // 图片上传处理，不传则隐藏上传入口
    uploadImage?: (files: File[]) => Promise<string[]> | string[]
    // 关闭 mermaid 渲染，默认关闭避免加载 CDN 资源
    noMermaid?: boolean
    // 关闭 katex 公式渲染，默认关闭避免加载 CDN 资源
    noKatex?: boolean
    // 关闭 echarts 图表渲染，默认关闭避免加载 CDN 资源
    noEcharts?: boolean
    // 关闭代码高亮
    noHighlight?: boolean
    // 固定编辑器 id，用于与 MdCatalog 绑定
    editorId?: string
  }>(),
  {
    height: '480px',
    placeholder: '请输入内容',
    previewTheme: 'github',
    readOnly: false,
    disabled: false,
    maxLength: undefined,
    toolbarsExclude: undefined,
    uploadImage: undefined,
    noMermaid: true,
    noKatex: true,
    noEcharts: true,
    noHighlight: false,
    editorId: undefined,
  },
)

const emit = defineEmits<{
  // 内容变化
  change: [value: string]
  // 触发保存
  save: [value: string]
}>()

const content = defineModel<string>({ default: '' })

// 编辑器界面语言
const LANGUAGE = 'zh-CN'

// 组件类型未声明 expose 的方法，按包内 ExposeParam 约定取用
const editorRef = ref<ExposeParam>()

const { darkMode } = useThemeMode()

// 跟随站点的明暗配色
const theme = computed(() => (darkMode.value ? 'dark' : 'light'))

// useId 只能在 setup 顶层调用，先取到再参与计算
const autoEditorId = `md-editor-${useId()}`

// 生成合法且稳定的编辑器 id，避免特殊字符影响选择器
const resolvedEditorId = computed(() => {
  const id = props.editorId ?? autoEditorId
  return id.replace(/[^a-zA-Z0-9_-]/g, '-')
})

// 默认隐藏源码地址入口，被关闭的渲染能力也一并隐藏按钮
const defaultExclude = computed<ToolbarNames[]>(() => {
  const list: ToolbarNames[] = ['github']
  if (props.noMermaid) list.push('mermaid')
  if (props.noKatex) list.push('katex')
  return list
})

const resolvedToolbarsExclude = computed(() => props.toolbarsExclude ?? defaultExclude.value)

// 转交图片上传，失败时给出提示避免静默无响应
async function handleUploadImg(files: File[], callback: UploadImgCallBack): Promise<void> {
  if (!props.uploadImage) return
  try {
    const urls = await props.uploadImage(files)
    callback(urls)
  } catch {
    ElMessage.error('图片上传失败')
  }
}

function handleChange(value: string): void {
  emit('change', value)
}

function handleSave(value: string): void {
  emit('save', value)
}

// 暴露常用实例方法，便于父组件控制焦点与插入内容
defineExpose({
  focus: (options?: FocusOption) => editorRef.value?.focus(options),
  insert: (generate: InsertContentGenerator) => editorRef.value?.insert(generate),
  togglePreview: (status?: boolean) => editorRef.value?.togglePreview(status),
  toggleFullscreen: (status?: boolean) => editorRef.value?.toggleFullscreen(status),
  getSelectedText: () => editorRef.value?.getSelectedText(),
  rerender: () => editorRef.value?.rerender(),
})
</script>

<style scoped>
.markdown-editor {
  width: 100%;
}

/* 让编辑器撑满外层容器，高度统一由 height 控制 */
.markdown-editor :deep(.md-editor) {
  height: 100%;
  --md-bk-color: var(--backend-surface);
  --md-bk-color-outstand: var(--backend-sidebar-surface);
  --md-bk-hover-color: var(--backend-active);
  --md-color: var(--backend-text);
  --md-hover-color: var(--backend-text);
  --md-border-color: var(--backend-border);
  --md-border-hover-color: var(--backend-muted);
  --md-scrollbar-bg-color: var(--backend-surface);
  --md-scrollbar-thumb-color: var(--backend-muted);
}
</style>
