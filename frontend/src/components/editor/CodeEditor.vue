<!-- 通用代码编辑器，基于 CodeMirror 6 -->
<template>
  <div class="code-editor" :class="{ 'code-editor--dark': darkMode }" :style="heightStyle">
    <div ref="hostRef" class="code-editor__host"></div>
    <!-- 缩放提示，滚轮缩放时短暂出现 -->
    <transition name="code-editor__zoom-fade">
      <span v-if="zoomHintVisible" class="code-editor__zoom-hint">{{ zoomPercent }}%</span>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, shallowRef, watch } from 'vue'
import { Compartment, EditorState, type Extension } from '@codemirror/state'
import { EditorView, keymap, lineNumbers, highlightActiveLine, highlightActiveLineGutter, placeholder } from '@codemirror/view'
import { defaultKeymap, history, historyKeymap, indentWithTab } from '@codemirror/commands'
import {
  bracketMatching,
  indentOnInput,
  syntaxHighlighting,
  HighlightStyle,
  codeFolding,
  foldGutter,
  foldKeymap,
} from '@codemirror/language'
import { tags as t } from '@lezer/highlight'
import { oneDark } from '@codemirror/theme-one-dark'
import { useThemeMode } from '@/utils/themeModeUtil'

// 跟随项目配色模式，深色时才叠加 oneDark
const { darkMode } = useThemeMode()

const props = withDefaults(
  defineProps<{
    // 编辑器高度
    height?: string
    // 占位提示，编辑器为空时展示
    placeholder?: string
    // 语言标识：java、cpp、python、go、javascript、json，其余按纯文本处理
    language?: string
    // 只读
    readOnly?: boolean
    // 字号，像素
    fontSize?: number
  }>(),
  {
    height: '',
    placeholder: '',
    language: 'cpp',
    readOnly: false,
    fontSize: 13,
  },
)

// 编辑器内容，对外提供 v-model
const modelValue = defineModel<string>({ default: '' })

// 高度为空时交给外部样式控制，便于放进 flex 布局撑满父容器
const heightStyle = computed(() => (props.height ? { height: props.height } : undefined))

// 语言包按需动态加载，避免首屏引入全部语法解析器
const LANGUAGE_LOADERS: Record<string, () => Promise<Extension>> = {
  cpp: async () => (await import('@codemirror/lang-cpp')).cpp(),
  java: async () => (await import('@codemirror/lang-java')).java(),
  python: async () => (await import('@codemirror/lang-python')).python(),
  javascript: async () => (await import('@codemirror/lang-javascript')).javascript(),
  go: async () => (await import('@codemirror/lang-go')).go(),
  json: async () => (await import('@codemirror/lang-json')).json(),
}

const hostRef = ref<HTMLDivElement>()
// 用 shallowRef 保存实例，Vue 的深度代理会破坏 CodeMirror 内部状态
const view = shallowRef<EditorView>()

// 当前字号，Ctrl + 滚轮可缩放，初始值取属性
const fontSize = ref(props.fontSize)

// 缩放范围，避免缩到看不清或撑破布局
const MIN_FONT_SIZE = 10
const MAX_FONT_SIZE = 32

// 每次滚轮的缩放步长，像素
const ZOOM_STEP = 1

// 缩放提示的显示状态与自动隐藏定时器
const zoomHintVisible = ref(false)
let zoomHintTimer: number | null = null

// 缩放倍率，以初始字号为 100%
const zoomPercent = computed(() => Math.round((fontSize.value / props.fontSize) * 100))

// 用 Compartment 动态重配扩展，避免整体重建编辑器
const languageCompartment = new Compartment()
const readOnlyCompartment = new Compartment()
const themeCompartment = new Compartment()
const placeholderCompartment = new Compartment()

// 加载语言扩展，未知语言或无网络时退化为无高亮
async function loadLanguageExtension(language: string): Promise<Extension> {
  const loader = LANGUAGE_LOADERS[language]
  if (!loader) return []
  try {
    return await loader()
  } catch {
    return []
  }
}

// 亮色语法配色，比默认样式对比度更高
const lightHighlightStyle = HighlightStyle.define([
  { tag: t.keyword, color: '#cf222e' },
  { tag: [t.name, t.deleted, t.character, t.propertyName, t.macroName], color: '#1f2328' },
  { tag: [t.function(t.variableName), t.labelName], color: '#8250df' },
  { tag: [t.color, t.constant(t.name), t.standard(t.name)], color: '#0550ae' },
  { tag: [t.definition(t.name), t.separator], color: '#1f2328' },
  { tag: [t.typeName, t.className, t.number, t.changed, t.annotation, t.modifier, t.self, t.namespace], color: '#953800' },
  { tag: [t.operator, t.operatorKeyword, t.url, t.escape, t.regexp, t.link, t.special(t.string)], color: '#0550ae' },
  { tag: [t.meta, t.comment], color: '#6e7781', fontStyle: 'italic' },
  { tag: [t.string, t.inserted], color: '#0a3069' },
  { tag: t.strong, fontWeight: 'bold' },
  { tag: t.emphasis, fontStyle: 'italic' },
  { tag: t.strikethrough, textDecoration: 'line-through' },
  { tag: t.heading, fontWeight: 'bold', color: '#cf222e' },
  { tag: [t.atom, t.bool, t.special(t.variableName)], color: '#0550ae' },
  { tag: [t.processingInstruction, t.string, t.inserted], color: '#0a3069' },
  { tag: t.invalid, color: '#cf222e' },
])

// 深色模式的语法配色用 oneDark，但它会把底色和高亮写成偏蓝的固定色；
// 这里在其后再叠一层覆盖，底色与高亮改读宿主主题变量，与后台深色面板保持一致。
function buildDarkSurface(): Extension {
  return EditorView.theme(
    {
      '&': { backgroundColor: 'var(--code-editor-bg)' },
      '.cm-gutters': {
        backgroundColor: 'var(--code-editor-soft)',
        color: 'var(--code-editor-muted)',
      },
      '.cm-activeLine': { backgroundColor: 'var(--code-editor-line-highlight)' },
      '.cm-activeLineGutter': { backgroundColor: 'var(--code-editor-gutter-highlight)' },
      '.cm-selectionBackground, .cm-content ::selection': {
        backgroundColor: 'var(--code-editor-selection)',
      },
    },
    { dark: true },
  )
}

// 编辑器外观，亮色为主，跟随项目配色模式
function buildTheme(): Extension {
  return [
    EditorView.theme({
      '&': { fontSize: `${fontSize.value}px`, height: '100%' },
      '.cm-content': {
        fontFamily: 'var(--code-font-family)',
        lineHeight: '1.6',
        caretColor: '#1f6feb',
      },
      '.cm-scroller': { overflow: 'auto' },
      '.cm-gutters': {
        borderRight: 'none',
        backgroundColor: 'var(--code-editor-soft, #f5f7fa)',
        color: 'var(--code-editor-muted, #909399)',
      },
      // 行号与折叠槽统一行高与垂直居中，避免箭头和数字错位
      '.cm-gutterElement': {
        display: 'flex',
        alignItems: 'center',
        lineHeight: '1.6',
      },
      '.cm-lineNumbers .cm-gutterElement': {
        justifyContent: 'flex-end',
        paddingRight: '6px',
      },
      '.cm-activeLine': { backgroundColor: 'var(--code-editor-line-highlight)' },
      '.cm-activeLineGutter': { backgroundColor: 'var(--code-editor-gutter-highlight)' },
      '.cm-selectionBackground, ::selection': {
        backgroundColor: 'var(--code-editor-selection)',
      },
      '&.cm-focused': { outline: 'none' },
    }),
    // 深色模式叠加 oneDark，亮色模式用自定义的浅色配色
    ...(darkMode.value ? [oneDark, buildDarkSurface()] : [syntaxHighlighting(lightHighlightStyle)]),
  ]
}

// 只读扩展
function buildReadOnly(): Extension {
  return props.readOnly ? EditorState.readOnly.of(true) : []
}

// 基础编辑能力，含行号、括号匹配、代码折叠、撤销重做、Tab 缩进
function buildBaseExtensions(): Extension[] {
  return [
    lineNumbers(),
    highlightActiveLineGutter(),
    highlightActiveLine(),
    history(),
    indentOnInput(),
    bracketMatching(),
    // 代码折叠：行号栏旁的箭头可折叠函数体与括号块
    codeFolding(),
    foldGutter({
      markerDOM: (open) => buildFoldMarker(open),
    }),
    keymap.of([...defaultKeymap, ...historyKeymap, ...foldKeymap, indentWithTab]),
    EditorView.lineWrapping,
  ]
}

// 折叠箭头用 SVG 描边绘制，只画轮廓线不填充
function buildFoldMarker(open: boolean): HTMLElement {
  const wrapper = document.createElement('span')
  wrapper.className = 'code-editor__fold-icon'

  const ns = 'http://www.w3.org/2000/svg'
  const svg = document.createElementNS(ns, 'svg')
  svg.setAttribute('width', '10')
  svg.setAttribute('height', '10')
  svg.setAttribute('viewBox', '0 0 10 10')
  svg.setAttribute('fill', 'none')
  svg.setAttribute('stroke', 'currentColor')
  svg.setAttribute('stroke-width', '1.2')
  svg.setAttribute('stroke-linecap', 'round')
  svg.setAttribute('stroke-linejoin', 'round')

  const path = document.createElementNS(ns, 'path')
  // 展开朝下，折叠朝右，都是纯描边路径
  path.setAttribute('d', open ? 'M2 4 L5 7 L8 4' : 'M4 2 L7 5 L4 8')
  svg.appendChild(path)

  wrapper.appendChild(svg)
  return wrapper
}

// 占位提示用 CodeMirror 自带的 placeholder 扩展，空文档时才有内容，样式见下方 cm-placeholder
function buildPlaceholder(): Extension {
  return props.placeholder ? placeholder(props.placeholder) : []
}

// Ctrl + 滚轮缩放字号，滚轮事件挂在编辑器 DOM 上
function handleWheel(event: WheelEvent) {
  if (!event.ctrlKey) return
  // 阻止浏览器默认的整页缩放
  event.preventDefault()

  const next = event.deltaY < 0
    ? fontSize.value + ZOOM_STEP
    : fontSize.value - ZOOM_STEP
  fontSize.value = Math.min(MAX_FONT_SIZE, Math.max(MIN_FONT_SIZE, next))

  showZoomHint()
}

// 显示缩放提示，1 秒后自动隐藏
function showZoomHint() {
  zoomHintVisible.value = true
  if (zoomHintTimer !== null) window.clearTimeout(zoomHintTimer)
  zoomHintTimer = window.setTimeout(() => {
    zoomHintVisible.value = false
    zoomHintTimer = null
  }, 1000)
}

// 创建编辑器实例
async function createEditor() {
  if (!hostRef.value) return

  const languageExtension = await loadLanguageExtension(props.language)

  const state = EditorState.create({
    doc: modelValue.value ?? '',
    extensions: [
      ...buildBaseExtensions(),
      themeCompartment.of(buildTheme()),
      placeholderCompartment.of(buildPlaceholder()),
      languageCompartment.of(languageExtension),
      readOnlyCompartment.of(buildReadOnly()),
      EditorView.updateListener.of((update) => {
        if (update.docChanged) {
          modelValue.value = update.state.doc.toString()
        }
      }),
    ],
  })

  view.value = new EditorView({ state, parent: hostRef.value })
  // 用 passive: false 才能调用 preventDefault 阻止整页缩放
  view.value.dom.addEventListener('wheel', handleWheel, { passive: false })
}

// 外部内容变化时同步，内容相同则跳过以保持光标位置
watch(modelValue, (value) => {
  const current = view.value
  if (!current) return
  const text = value ?? ''
  if (text === current.state.doc.toString()) return
  current.dispatch({
    changes: { from: 0, to: current.state.doc.length, insert: text },
  })
})

// 语言变化时重配语法高亮
watch(
  () => props.language,
  async (language) => {
    const current = view.value
    if (!current) return
    const extension = await loadLanguageExtension(language)
    current.dispatch({ effects: languageCompartment.reconfigure(extension) })
  },
)

// 只读状态变化时重配
watch(
  () => props.readOnly,
  () => {
    view.value?.dispatch({ effects: readOnlyCompartment.reconfigure(buildReadOnly()) })
  },
)

// 占位文案变化时重配
watch(
  () => props.placeholder,
  () => {
    view.value?.dispatch({ effects: placeholderCompartment.reconfigure(buildPlaceholder()) })
  },
)

// 字号变化时重配主题
watch(fontSize, () => {
  view.value?.dispatch({ effects: themeCompartment.reconfigure(buildTheme()) })
})

// 外部传入的字号变化时同步，Ctrl 滚轮改的是内部状态
watch(
  () => props.fontSize,
  (value) => {
    fontSize.value = value
  },
)

// 配色模式切换时重配主题
watch(darkMode, () => {
  view.value?.dispatch({ effects: themeCompartment.reconfigure(buildTheme()) })
})

onMounted(() => {
  void createEditor()
})

onUnmounted(() => {
  if (zoomHintTimer !== null) window.clearTimeout(zoomHintTimer)
  view.value?.dom.removeEventListener('wheel', handleWheel)
  view.value?.destroy()
  view.value = undefined
})

// 暴露常用方法，便于父组件聚焦或取值
defineExpose({
  focus: () => view.value?.focus(),
  getValue: () => view.value?.state.doc.toString() ?? '',
})
</script>

<style scoped>
/* 高度由外部 flex 或 height 属性决定，这里只保证内容撑满 */
.code-editor {
  /* 同时兼容做题模块的 --problem-* 与后台的 --backend-* 主题变量 */
  --code-editor-border: var(--problem-border, var(--backend-border, var(--el-border-color, #dcdfe6)));
  --code-editor-muted: var(--problem-muted, var(--backend-muted, #909399));
  --code-editor-soft: var(--problem-soft, var(--backend-active, #f5f7fa));
  --code-editor-bg: var(--problem-bg, var(--backend-surface, #fff));
  /* 亮色用项目主色做高亮，深色改由文字色派生，避免偏蓝高亮压在深色面板上 */
  --code-editor-line-highlight: rgb(31 111 235 / 6%);
  --code-editor-gutter-highlight: rgb(31 111 235 / 8%);
  --code-editor-selection: rgb(31 111 235 / 18%);

  position: relative;
  display: flex;
  flex-direction: column;
  width: 100%;
  min-height: 0;
  overflow: hidden;
  border: 1px solid var(--code-editor-border);
  border-radius: 6px;
}

/* 深色模式的高亮跟随文字色，与后台深色面板协调 */
.code-editor--dark {
  --code-editor-line-highlight: color-mix(in srgb, currentcolor 6%, transparent);
  --code-editor-gutter-highlight: color-mix(in srgb, currentcolor 10%, transparent);
  --code-editor-selection: color-mix(in srgb, currentcolor 18%, transparent);
}

.code-editor__host {
  flex: 1;
  min-height: 0;
}

/* 编辑器外层已有边框，去掉内层自带的 outline */
.code-editor__host :deep(.cm-editor) {
  height: 100%;
  outline: none;
}

/* 隐藏滚动条但保留滚动能力，避免右侧出现竖条影响观感 */
.code-editor__host :deep(.cm-scroller) {
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.code-editor__host :deep(.cm-scroller)::-webkit-scrollbar {
  display: none;
}

/* 内容为空时展示占位提示，CodeMirror 的 placeholder 扩展会渲染这个元素 */
.code-editor__host :deep(.cm-placeholder) {
  color: var(--code-editor-muted);
  font-family: var(--code-font-family);
  pointer-events: none;
}

/* 折叠箭头的容器，与行号垂直居中并占固定宽度 */
.code-editor__host :deep(.code-editor__fold-icon) {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 12px;
  height: 1.6em;
  color: var(--code-editor-muted);
  line-height: 1;
  vertical-align: middle;
  cursor: pointer;
  transition: color 150ms ease;
}

.code-editor__host :deep(.code-editor__fold-icon svg) {
  display: block;
}

.code-editor__host :deep(.code-editor__fold-icon:hover) {
  color: var(--el-color-primary, #1f6feb);
}

/* 折叠槽本身去掉额外内边距，避免与行号错位 */
.code-editor__host :deep(.cm-foldGutter .cm-gutterElement) {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
}

/* 折叠占位标记，折叠后显示的省略提示 */
.code-editor__host :deep(.cm-foldPlaceholder) {
  margin: 0 4px;
  padding: 0 6px;
  border: 0;
  border-radius: 3px;
  background: var(--code-editor-soft);
  color: var(--code-editor-muted);
  font-size: 12px;
  cursor: pointer;
}

/* 缩放提示浮在右上角，淡出后不占用布局 */
.code-editor__zoom-hint {
  position: absolute;
  top: 8px;
  right: 12px;
  z-index: 10;
  padding: 2px 8px;
  border-radius: 4px;
  background: rgb(0 0 0 / 55%);
  color: #fff;
  font-size: 12px;
  line-height: 18px;
  pointer-events: none;
}

.code-editor__zoom-fade-enter-active,
.code-editor__zoom-fade-leave-active {
  transition: opacity 200ms ease;
}

.code-editor__zoom-fade-enter-from,
.code-editor__zoom-fade-leave-to {
  opacity: 0;
}
</style>
