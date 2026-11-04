<!-- 后台顶部记录已访问的菜单页面 -->
<template>
  <header class="backend-history">
    <PanelLeftOpen
      v-if="collapsed"
      class="backend-history__toggle"
      :size="17"
      :stroke-width="1.7"
      role="button"
      tabindex="0"
      aria-label="展开侧边栏"
      title="展开侧边栏"
      :aria-expanded="false"
      aria-controls="backend-sidebar"
      @click="collapsed = false"
      @keydown.enter.prevent="collapsed = false"
      @keydown.space.prevent="collapsed = false"
    />

    <nav
      ref="pagesRef"
      class="backend-history__pages"
      :class="{ 'backend-history__pages--dragging': isDragging }"
      aria-label="已访问的后台页面"
      @pointerdown="startHistoryDrag"
      @pointermove="moveHistoryDrag"
      @pointerup="stopHistoryDrag"
      @pointercancel="stopHistoryDrag"
      @click.capture="preventDragClick"
      @dragstart.prevent
    >
      <div
        v-for="entry in entries"
        :key="entry.key"
        class="backend-history__tab"
        :class="{ 'backend-history__tab--active': route.name === entry.key }"
      >
        <RouterLink class="backend-history__link" :to="entry.path" draggable="false">
          <h1 v-if="route.name === entry.key" class="backend-history__title">{{ entry.title }}</h1>
          <span v-else class="backend-history__title">{{ entry.title }}</span>
        </RouterLink>
        <button
          class="backend-history__close"
          type="button"
          :aria-label="`关闭${entry.title}标签`"
          :title="`关闭${entry.title}标签`"
          @click="closeEntry(entry.key)"
        >
          <X :size="8" :stroke-width="1.8" />
        </button>
      </div>
    </nav>

    <el-popover
      v-model:visible="profileMenuVisible"
      placement="bottom-end"
      trigger="click"
      :width="168"
      :teleported="false"
      popper-class="backend-history__popover"
      role="dialog"
    >
      <template #reference>
        <button class="backend-history__avatar-button" type="button" aria-label="打开用户菜单">
          <el-avatar class="backend-history__avatar" :size="28">
            <UserRound :size="16" :stroke-width="1.8" aria-hidden="true" />
          </el-avatar>
        </button>
      </template>

      <button
        class="backend-history__profile-item"
        type="button"
        @click="profileMenuVisible = false"
      >
        <UserRound :size="16" :stroke-width="1.8" aria-hidden="true" />
        个人中心
      </button>
      <div class="backend-history__menu-divider" role="separator"></div>
      <el-popover
        v-model:visible="themeMenuVisible"
        placement="left-start"
        trigger="click"
        :width="132"
        :offset="4"
        :teleported="false"
        :show-arrow="false"
        popper-class="backend-history__theme-popover"
      >
        <template #reference>
          <button
            class="backend-history__theme-entry"
            type="button"
            :aria-expanded="themeMenuVisible"
            aria-label="选择主题"
          >
            <Palette :size="16" :stroke-width="1.8" aria-hidden="true" />
            <span>主题</span>
            <ChevronLeft :size="15" :stroke-width="1.8" aria-hidden="true" />
          </button>
        </template>

        <div class="backend-history__theme-options" role="group" aria-label="主题选项">
          <button
            v-for="option in themeOptions"
            :key="option.mode"
            class="backend-history__theme-option"
            :class="{ 'backend-history__theme-option--selected': themeMode === option.mode }"
            type="button"
            :aria-pressed="themeMode === option.mode"
            @click="chooseTheme(option.mode)"
          >
            <component :is="option.icon" :size="16" :stroke-width="1.8" aria-hidden="true" />
            <span>{{ option.label }}</span>
            <Check v-if="themeMode === option.mode" :size="15" aria-hidden="true" />
          </button>
        </div>
      </el-popover>
    </el-popover>
  </header>
</template>

<script setup lang="ts">
import { nextTick, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  Check,
  ChevronLeft,
  Contrast,
  Moon,
  Palette,
  PanelLeftOpen,
  Sun,
  UserRound,
  X,
} from '@lucide/vue'
import type { ThemeMode } from '@/utils/themeModeUtil'

interface HistoryEntry {
  key: string
  path: string
  title: string
}

defineProps<{ themeMode: ThemeMode }>()
const emit = defineEmits<{ 'change-theme': [mode: ThemeMode] }>()
const collapsed = defineModel<boolean>('collapsed', { required: true })
const route = useRoute()
const router = useRouter()
const entries = ref<HistoryEntry[]>([])
const pagesRef = ref<HTMLElement | null>(null)
const isDragging = ref(false)
const profileMenuVisible = ref(false)
const themeMenuVisible = ref(false)
const themeOptions = [
  { mode: 'light', label: '亮色', icon: Sun },
  { mode: 'dark', label: '暗色', icon: Moon },
  { mode: 'system', label: '跟随系统', icon: Contrast },
] as const
let dragPointerId: number | null = null
let dragStartX = 0
let dragStartScrollLeft = 0
let suppressClick = false

function chooseTheme(mode: ThemeMode) {
  emit('change-theme', mode)
  themeMenuVisible.value = false
  profileMenuVisible.value = false
}

watch(profileMenuVisible, (visible) => {
  if (!visible) themeMenuVisible.value = false
})

// 鼠标拖动横向滚动标签栏
function startHistoryDrag(event: PointerEvent) {
  if (event.pointerType !== 'mouse' || event.button !== 0) return
  if (event.target instanceof Element && event.target.closest('.backend-history__close')) return

  const pages = pagesRef.value
  if (!pages || pages.scrollWidth <= pages.clientWidth) return

  dragPointerId = event.pointerId
  dragStartX = event.clientX
  dragStartScrollLeft = pages.scrollLeft
}

function moveHistoryDrag(event: PointerEvent) {
  const pages = pagesRef.value
  if (!pages || event.pointerId !== dragPointerId) return

  const distance = event.clientX - dragStartX
  if (!isDragging.value && Math.abs(distance) < 5) return
  if (!isDragging.value) {
    isDragging.value = true
    pages.setPointerCapture(event.pointerId)
  }

  event.preventDefault()
  pages.scrollLeft = dragStartScrollLeft - distance
}

function stopHistoryDrag(event: PointerEvent) {
  if (event.pointerId !== dragPointerId) return

  const pages = pagesRef.value
  if (isDragging.value) {
    suppressClick = true
    if (pages?.hasPointerCapture(event.pointerId)) pages.releasePointerCapture(event.pointerId)
    window.setTimeout(() => {
      suppressClick = false
    }, 0)
  }

  dragPointerId = null
  isDragging.value = false
}

function preventDragClick(event: MouseEvent) {
  if (!suppressClick) return
  event.preventDefault()
  event.stopPropagation()
  suppressClick = false
}

// 按路由记录访问历史，同一页面只保留一个标签
watch(
  () => route.name,
  (name) => {
    const title = route.meta.backendTitle
    if (typeof name !== 'string' || typeof title !== 'string') return
    if (!entries.value.some((entry) => entry.key === name)) {
      entries.value.push({ key: name, path: route.path, title })
    }

    void nextTick(showActiveEntry)
  },
  { immediate: true },
)

// 让当前标签进入可视区域
function showActiveEntry() {
  const pages = pagesRef.value
  const activeTab = pages?.querySelector<HTMLElement>('.backend-history__tab--active')
  if (!pages || !activeTab) return

  const pagesRect = pages.getBoundingClientRect()
  const tabRect = activeTab.getBoundingClientRect()
  if (tabRect.left < pagesRect.left) {
    pages.scrollLeft += tabRect.left - pagesRect.left
  } else if (tabRect.right + 8 > pagesRect.right) {
    pages.scrollLeft += tabRect.right + 8 - pagesRect.right
  }
}

// 关闭标签，当前标签关闭后切到相邻标签
function closeEntry(key: string) {
  const index = entries.value.findIndex((entry) => entry.key === key)
  if (index < 0) return

  const isCurrent = route.name === key
  entries.value.splice(index, 1)
  if (!isCurrent) return

  const next = entries.value[index] ?? entries.value[index - 1]
  if (next) {
    void router.push(next.path)
    return
  }

  const workbench = { key: 'backend-home', path: '/backend', title: '工作台' }
  entries.value.push(workbench)
  void router.push(workbench.path)
}
</script>

<style scoped>
.backend-history {
  display: flex;
  flex: none;
  align-items: center;
  gap: 8px;
  min-width: 0;
  height: 44px;
  padding: 0 20px 0 0;
  border-bottom: 1px solid var(--backend-border);
}

.backend-history__toggle {
  box-sizing: content-box;
  display: block;
  flex: none;
  width: 17px;
  height: 17px;
  padding: 5.5px;
  color: var(--backend-muted);
  cursor: pointer;
}

.backend-history__toggle:hover {
  color: var(--backend-active-text);
}

.backend-history__toggle:focus-visible {
  outline: 2px solid var(--backend-active-text);
  outline-offset: 2px;
}

.backend-history__pages {
  box-sizing: border-box;
  display: flex;
  flex: 1;
  align-items: center;
  gap: 0;
  min-width: 0;
  height: 100%;
  padding-right: 12px;
  overflow-x: auto;
  overflow-y: hidden;
  overscroll-behavior-inline: contain;
  scrollbar-width: none;
  -webkit-overflow-scrolling: touch;
  touch-action: pan-x;
  user-select: none;
  cursor: grab;
}

.backend-history__pages--dragging {
  cursor: grabbing;
}

.backend-history__pages::-webkit-scrollbar {
  display: none;
}

.backend-history__avatar-button {
  flex: none;
  display: grid;
  place-items: center;
  padding: 0;
  border: 0;
  border-radius: 50%;
  background: transparent;
  cursor: pointer;
}

.backend-history__avatar-button:focus-visible {
  outline: 2px solid var(--backend-active-text);
  outline-offset: 2px;
}

.backend-history__avatar {
  background: var(--backend-active);
  color: var(--backend-muted);
}

.backend-history :deep(.backend-history__popover) {
  --el-bg-color-overlay: var(--backend-sidebar-surface);
  background: var(--backend-sidebar-surface);
  border-color: var(--backend-border);
  color: var(--backend-text);
}

.backend-history__profile-item {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  min-height: 36px;
  padding: 0 8px;
  border: 0;
  border-radius: 6px;
  background: var(--backend-active);
  color: var(--backend-active-text);
  font: inherit;
  font-size: 14px;
  font-weight: 600;
  text-align: left;
  cursor: pointer;
}

.backend-history__profile-item:hover,
.backend-history__profile-item:focus-visible {
  background: color-mix(in srgb, var(--backend-active) 78%, var(--backend-active-text));
}

.backend-history__menu-divider {
  margin: 10px 0 8px;
  border-top: 1px solid var(--backend-border);
}

.backend-history :deep(.backend-history__theme-popover) {
  --el-bg-color-overlay: var(--backend-sidebar-surface);
  min-width: 132px;
  padding: 6px;
  background: var(--backend-sidebar-surface);
  border-color: var(--backend-border);
  color: var(--backend-text);
}

.backend-history__theme-options {
  display: grid;
  gap: 2px;
}

.backend-history__theme-entry,
.backend-history__theme-option {
  display: flex;
  align-items: center;
  gap: 7px;
  width: 100%;
  min-height: 36px;
  padding: 0 6px;
  border: 0;
  border-radius: 6px;
  background: transparent;
  color: var(--backend-text);
  font: inherit;
  font-size: 14px;
  text-align: left;
  cursor: pointer;
}

.backend-history__theme-entry > svg:last-child,
.backend-history__theme-option > svg:last-child {
  margin-left: auto;
}

.backend-history__theme-entry:hover,
.backend-history__theme-entry:focus-visible,
.backend-history__theme-entry[aria-expanded='true'],
.backend-history__theme-option:hover,
.backend-history__theme-option:focus-visible,
.backend-history__theme-option--selected {
  background: var(--backend-active);
  color: var(--backend-active-text);
}

.backend-history__tab {
  position: relative;
  display: inline-flex;
  flex: none;
  align-items: center;
  height: 100%;
}

.backend-history__tab:hover,
.backend-history__tab--active {
  background: var(--backend-active);
}

.backend-history__link {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  padding: 0 26px;
  color: var(--backend-muted);
  font-size: 14px;
  text-decoration: none;
  white-space: nowrap;
}

.backend-history__link:hover,
.backend-history__link:focus-visible,
.backend-history__tab--active .backend-history__link {
  color: var(--backend-text);
}

.backend-history__close {
  position: absolute;
  z-index: 1;
  top: 0;
  right: -8px;
  display: grid;
  place-items: center;
  width: 14px;
  height: 14px;
  padding: 0;
  border: 0;
  border-radius: 50%;
  background: var(--backend-close);
  box-shadow: 0 0 0 3px var(--backend-surface);
  color: var(--backend-surface);
  cursor: pointer;
  opacity: 0;
  pointer-events: none;
}

.backend-history__tab:hover .backend-history__close,
.backend-history__tab:focus-within .backend-history__close {
  opacity: 1;
  pointer-events: auto;
}

.backend-history__close:hover,
.backend-history__close:focus-visible {
  background: var(--backend-close-hover);
}

.backend-history__title {
  margin: 0;
  font-size: inherit;
  font-weight: 500;
}

@media (max-width: 760px) {
  .backend-history {
    padding: 0 16px;
  }

  .backend-history__close {
    visibility: hidden;
  }

  .backend-history__tab--active .backend-history__close {
    visibility: visible;
    opacity: 1;
    pointer-events: auto;
  }
}
</style>
