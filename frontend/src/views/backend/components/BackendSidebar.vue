<!-- 后台侧栏、收起入口和手机端遮罩 -->
<template>
  <header
    class="backend-sidebar-header"
    :class="{ 'backend-sidebar-header--collapsed': collapsed }"
  >
    <RouterLink
      class="backend-brand"
      to="/"
      :aria-label="`返回 ${siteTitle} 首页`"
      @click="closeMobileSidebar"
    >
      <span class="backend-brand__name">{{ siteTitle }}</span>
    </RouterLink>
    <PanelLeftClose
      class="backend-sidebar-toggle"
      :size="17"
      :stroke-width="1.7"
      role="button"
      tabindex="0"
      aria-label="收起侧边栏"
      title="收起侧边栏"
      :aria-expanded="!collapsed"
      aria-controls="backend-sidebar"
      @click="toggle"
      @keydown.enter.prevent="toggle"
      @keydown.space.prevent="toggle"
    />
  </header>

  <aside
    id="backend-sidebar"
    class="backend-sidebar"
    :class="{ 'backend-sidebar--collapsed': collapsed }"
  >
    <nav class="backend-sidebar__nav" aria-label="后台导航">
      <RouterLink
        class="backend-sidebar__item"
        :class="{ 'backend-sidebar__item--active': route.name === 'backend-home' }"
        to="/backend"
        @click="selectWorkbench"
      >
        <LayoutDashboard aria-hidden="true" />工作台
      </RouterLink>
      <el-menu
        ref="groupMenu"
        class="backend-sidebar__groups"
        :default-active="activeMenuPath"
        :default-openeds="initialOpenGroups"
        router
        @select="closeMobileSidebar"
      >
        <el-sub-menu index="questions">
          <template #title>
            <FileText class="backend-sidebar__group-icon" aria-hidden="true" />题目管理
            <ChevronDown class="backend-sidebar__expand-icon" aria-hidden="true" />
          </template>
          <el-menu-item index="/backend/questions">
            <List class="backend-sidebar__item-icon" aria-hidden="true" />题目列表
          </el-menu-item>
          <el-menu-item index="/backend/questions/new">
            <PenLine class="backend-sidebar__item-icon" aria-hidden="true" />新建题目
          </el-menu-item>
          <el-menu-item index="/backend/tags">
            <Tags class="backend-sidebar__item-icon" aria-hidden="true" />标签管理
          </el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="submissions">
          <template #title>
            <History class="backend-sidebar__group-icon" aria-hidden="true" />提交管理
            <ChevronDown class="backend-sidebar__expand-icon" aria-hidden="true" />
          </template>
          <el-menu-item index="/backend/submissions">
            <List class="backend-sidebar__item-icon" aria-hidden="true" />提交列表
          </el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="judge">
          <template #title>
            <Cpu class="backend-sidebar__group-icon" aria-hidden="true" />评测机
            <ChevronDown class="backend-sidebar__expand-icon" aria-hidden="true" />
          </template>
          <el-menu-item index="/backend/judge/machines">
            <Server class="backend-sidebar__item-icon" aria-hidden="true" />机器管理
          </el-menu-item>
          <el-menu-item index="/backend/judge/status">
            <Activity class="backend-sidebar__item-icon" aria-hidden="true" />运行状态
          </el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="users">
          <template #title>
            <UsersRound class="backend-sidebar__group-icon" aria-hidden="true" />用户管理
            <ChevronDown class="backend-sidebar__expand-icon" aria-hidden="true" />
          </template>
          <el-menu-item index="/backend/users">
            <UserCog class="backend-sidebar__item-icon" aria-hidden="true" />用户列表
          </el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="system-settings">
          <template #title>
            <Settings class="backend-sidebar__group-icon" aria-hidden="true" />系统设置
            <ChevronDown class="backend-sidebar__expand-icon" aria-hidden="true" />
          </template>
          <el-menu-item index="/backend/settings">
            <Settings2 class="backend-sidebar__item-icon" aria-hidden="true" />设置
          </el-menu-item>
        </el-sub-menu>
      </el-menu>
    </nav>
  </aside>

  <button
    class="backend-scrim"
    :class="{ 'backend-scrim--hidden': collapsed }"
    type="button"
    aria-label="关闭侧边栏"
    @click="toggle"
  />
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch, type Ref, inject } from 'vue'
import type { MenuInstance } from 'element-plus'
import {
  Activity,
  ChevronDown,
  Cpu,
  FileText,
  History,
  LayoutDashboard,
  List,
  PanelLeftClose,
  PenLine,
  Server,
  Settings,
  Settings2,
  Tags,
  UserCog,
  UsersRound,
} from '@lucide/vue'
import { useRoute } from 'vue-router'

const collapsed = defineModel<boolean>('collapsed', { required: true })
const mobileQuery = window.matchMedia('(max-width: 760px)')
const route = useRoute()
// 站点名称由后台布局统一注入
const siteTitle = inject<Ref<string>>('backendSiteTitle', ref('CodeRank'))
const groupMenu = ref<MenuInstance>()

// 高亮当前页面对应的菜单项
const activeMenuPath = computed(() => {
  if (route.name === 'backend-questions' || route.name === 'backend-question-edit')
    return '/backend/questions'
  if (route.name === 'backend-question-new') return '/backend/questions/new'
  if (route.name === 'backend-tags') return '/backend/tags'
  if (route.name === 'backend-judge-machines') return '/backend/judge/machines'
  if (route.name === 'backend-judge-status') return '/backend/judge/status'
  if (route.name === 'backend-submissions') return '/backend/submissions'
  if (route.name === 'backend-users') return '/backend/users'
  if (route.name === 'backend-settings') return '/backend/settings'
  return ''
})

// 进入页面时展开对应分组
const initialOpenGroups = (() => {
  const name = route.name
  if (
    name === 'backend-questions' ||
    name === 'backend-question-new' ||
    name === 'backend-question-edit' ||
    name === 'backend-tags'
  )
    return ['questions']
  if (name === 'backend-submissions') return ['submissions']
  if (name === 'backend-judge-machines' || name === 'backend-judge-status') return ['judge']
  if (name === 'backend-users') return ['users']
  if (name === 'backend-settings') return ['system-settings']
  return []
})()

// 打开工作台时收起所有分组
function selectWorkbench() {
  groupMenu.value?.close('questions')
  groupMenu.value?.close('submissions')
  groupMenu.value?.close('judge')
  groupMenu.value?.close('users')
  groupMenu.value?.close('system-settings')
  closeMobileSidebar()
}

// 手机端选择菜单后自动收起侧栏
function closeMobileSidebar() {
  if (mobileQuery.matches) collapsed.value = true
}

watch(
  () => route.name,
  (name) => {
    if (name === 'backend-home') selectWorkbench()
    if (
      name === 'backend-questions' ||
      name === 'backend-question-new' ||
      name === 'backend-question-edit' ||
      name === 'backend-tags'
    ) {
      groupMenu.value?.open('questions')
    }
    if (name === 'backend-submissions') {
      groupMenu.value?.open('submissions')
    }
    if (name === 'backend-judge-machines' || name === 'backend-judge-status') {
      groupMenu.value?.open('judge')
    }
    if (name === 'backend-users') {
      groupMenu.value?.open('users')
    }
    if (name === 'backend-settings') {
      groupMenu.value?.open('system-settings')
    }
  },
)

function toggle() {
  collapsed.value = !collapsed.value
}

function handleViewportChange(event: MediaQueryListEvent) {
  collapsed.value = event.matches
}

function handleEscape(event: KeyboardEvent) {
  if (event.key === 'Escape' && mobileQuery.matches && !collapsed.value) {
    collapsed.value = true
  }
}

onMounted(() => {
  mobileQuery.addEventListener('change', handleViewportChange)
  window.addEventListener('keydown', handleEscape)
})

onUnmounted(() => {
  mobileQuery.removeEventListener('change', handleViewportChange)
  window.removeEventListener('keydown', handleEscape)
})
</script>

<style scoped>
.backend-sidebar-header {
  grid-area: brand;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  min-width: 0;
  padding: 0 12px;
  border-right: 1px solid var(--backend-border);
  overflow: hidden;
  /* 导航区域不参与文字选中，避免拖选出现选中色块。 */
  user-select: none;
  -webkit-user-select: none;
  background: var(--backend-sidebar-surface);
  transition:
    opacity 180ms ease,
    visibility 0s;
}

.backend-brand {
  display: inline-flex;
  align-items: center;
  min-width: 0;
  color: inherit;
  text-decoration: none;
}

.backend-brand__name {
  font-size: 15px;
  font-weight: 700;
  line-height: 1.1;
}

.backend-sidebar-toggle {
  box-sizing: content-box;
  display: block;
  flex: none;
  width: 17px;
  height: 17px;
  padding: 5.5px;
  color: var(--backend-muted);
  cursor: pointer;
}

.backend-sidebar-toggle:hover {
  color: var(--backend-active-text);
}

.backend-sidebar-toggle:focus-visible {
  outline: 2px solid var(--backend-active-text);
  outline-offset: 2px;
}

.backend-sidebar {
  grid-area: sidebar;
  min-width: 0;
  min-height: 0;
  padding: 10px 0 14px;
  border-right: 1px solid var(--backend-border);
  overflow-x: hidden;
  overflow-y: auto;
  scrollbar-width: none;
  -ms-overflow-style: none;
  background: var(--backend-sidebar-surface);
  /* 菜单文字只作为导航标签，不参与文字选中。 */
  user-select: none;
  -webkit-user-select: none;
  transition:
    opacity 180ms ease,
    visibility 0s;
}

.backend-sidebar::-webkit-scrollbar {
  display: none;
}

.backend-sidebar__nav {
  display: grid;
  align-content: start;
}

.backend-sidebar__item {
  display: flex;
  align-items: center;
  gap: 13px;
  min-height: 47px;
  padding: 0 24px;
  color: var(--backend-text);
  font-size: 16px;
  text-decoration: none;
}

.backend-sidebar__item svg {
  flex: none;
  width: 17px;
  height: 17px;
  stroke-width: 1.9;
}

.backend-sidebar__item:hover,
.backend-sidebar__item:focus-visible,
.backend-sidebar__item--active {
  background: var(--backend-active);
  color: var(--backend-active-text);
}

.backend-sidebar__groups {
  border-right: 0;
  background: transparent;
  --el-menu-base-level-padding: 24px;
  --el-menu-bg-color: transparent;
  --el-menu-hover-bg-color: var(--backend-active);
  --el-menu-text-color: var(--backend-text);
  --el-menu-active-color: var(--backend-active-text);
}

.backend-sidebar__groups :deep(.el-sub-menu__title) {
  gap: 13px;
  height: 47px;
  font-size: 16px;
  line-height: 47px;
}

.backend-sidebar__groups :deep(.el-sub-menu__title:hover) {
  background: transparent;
}

.backend-sidebar__group-icon,
.backend-sidebar__item-icon {
  flex: none;
  width: 17px;
  height: 17px;
  stroke-width: 1.9;
}

.backend-sidebar__expand-icon {
  flex: none;
  margin-left: auto;
  width: 16px;
  height: 16px;
  stroke-width: 1.9;
  transition: transform 220ms ease;
}

.backend-sidebar__groups :deep(.el-sub-menu.is-opened) .backend-sidebar__expand-icon {
  transform: rotate(180deg);
}

.backend-sidebar__groups :deep(.el-sub-menu__icon-arrow) {
  display: none;
}

.backend-sidebar__groups :deep(.el-menu-item) {
  gap: 13px;
  height: 47px;
  font-size: 16px;
  line-height: 47px;
}

.backend-sidebar__groups :deep(.el-menu-item:hover) {
  background: var(--backend-active);
}

.backend-sidebar__groups :deep(.el-menu-item.is-active) {
  background: var(--backend-active);
}

.backend-scrim {
  display: none;
}

.backend-sidebar-header--collapsed,
.backend-sidebar--collapsed {
  visibility: hidden;
  opacity: 0;
  pointer-events: none;
  transition:
    opacity 180ms ease,
    visibility 0s linear 320ms;
}

@media (max-width: 760px) {
  .backend-sidebar-header,
  .backend-sidebar {
    position: fixed;
    left: 0;
    z-index: 31;
    width: min(280px, calc(100vw - 48px));
    transform: translateX(0);
    transition:
      transform 320ms cubic-bezier(0.22, 1, 0.36, 1),
      opacity 180ms ease,
      visibility 0s;
  }

  .backend-sidebar-header--collapsed,
  .backend-sidebar--collapsed {
    transform: translateX(-100%);
    transition:
      transform 320ms cubic-bezier(0.22, 1, 0.36, 1),
      opacity 180ms ease,
      visibility 0s linear 320ms;
  }

  .backend-sidebar-header {
    top: 0;
    height: 44px;
  }

  .backend-sidebar {
    top: 44px;
    bottom: 0;
  }

  .backend-scrim {
    position: fixed;
    z-index: 30;
    inset: 0;
    display: block;
    width: 100%;
    height: 100%;
    padding: 0;
    border: 0;
    background: rgb(0 0 0 / 28%);
    cursor: pointer;
    transition:
      opacity 320ms ease,
      visibility 0s;
  }

  .backend-scrim--hidden {
    visibility: hidden;
    opacity: 0;
    pointer-events: none;
    transition:
      opacity 320ms ease,
      visibility 0s linear 320ms;
  }
}

@media (prefers-reduced-motion: reduce) {
  .backend-sidebar__expand-icon {
    transition: none;
  }

  .backend-sidebar-header,
  .backend-sidebar,
  .backend-scrim,
  .backend-sidebar-header--collapsed,
  .backend-sidebar--collapsed,
  .backend-scrim--hidden {
    transition: none;
  }
}
</style>
