<!-- 首页导航栏右侧的用户头像与点击气泡 -->
<template>
  <el-popover
    v-model:visible="menuVisible"
    placement="bottom-end"
    trigger="click"
    :width="220"
    :offset="10"
    :teleported="false"
    popper-class="home-avatar__popover"
    role="dialog"
  >
    <template #reference>
      <button
        class="home-avatar"
        type="button"
        :aria-label="label"
        :aria-expanded="menuVisible"
        @click="emit('click', $event)"
      >
        <el-avatar class="home-avatar__image" :size="size" :src="src ?? undefined">
          <UserRound :size="iconSize" :stroke-width="1.8" aria-hidden="true" />
        </el-avatar>
      </button>
    </template>

    <div class="home-avatar__menu">
      <div v-if="userName" class="home-avatar__user">
        <strong>{{ userName }}</strong>
        <span v-if="userEmail">{{ userEmail }}</span>
      </div>

      <button class="home-avatar__action" type="button" @click="select('profile')">
        <UserRound :size="17" :stroke-width="1.8" aria-hidden="true" />个人中心
      </button>
      <button class="home-avatar__action" type="button" @click="select('logout')">
        <LogOut :size="17" :stroke-width="1.8" aria-hidden="true" />退出登录
      </button>
    </div>
  </el-popover>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { LogOut, UserRound } from '@lucide/vue'

const props = withDefaults(
  defineProps<{
    // 头像图片地址，未提供时显示图标兜底
    src?: string | null
    // 头像直径
    size?: number
    // 无障碍名称
    label?: string
    // 登录用户名，未登录时不显示用户信息区
    userName?: string | null
    // 登录邮箱
    userEmail?: string | null
  }>(),
  {
    src: null,
    size: 32,
    label: '用户菜单',
    userName: null,
    userEmail: null,
  },
)

const emit = defineEmits<{
  click: [event: MouseEvent]
  profile: []
  logout: []
}>()

const menuVisible = ref(false)

// 图标随头像尺寸等比缩放
const iconSize = computed(() => Math.round(props.size * 0.56))

// 点击菜单项后收起气泡
function select(action: 'profile' | 'logout') {
  menuVisible.value = false
  if (action === 'profile') emit('profile')
  else emit('logout')
}
</script>

<style scoped>
.home-avatar {
  display: inline-flex;
  flex: none;
  align-items: center;
  justify-content: center;
  padding: 0;
  border: 0;
  background: transparent;
  color: var(--home-navbar-muted);
  cursor: pointer;
}

.home-avatar:hover,
.home-avatar:focus-visible {
  color: var(--home-navbar-text);
}

.home-avatar:focus-visible {
  outline: 2px solid var(--home-navbar-text);
  outline-offset: 2px;
  border-radius: 50%;
}

.home-avatar__image {
  background: color-mix(in srgb, currentColor 8%, transparent);
  box-shadow: inset 0 0 0 1px currentColor;
  color: inherit;
}

/* 悬停与聚焦时加深描边，提示可点击 */
.home-avatar:hover .home-avatar__image,
.home-avatar:focus-visible .home-avatar__image {
  background: color-mix(in srgb, currentColor 14%, transparent);
  box-shadow: inset 0 0 0 1.5px currentColor;
}

.home-avatar__menu {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

/* 登录后展示用户名与邮箱，未登录时整块不渲染 */
.home-avatar__user {
  display: flex;
  flex-direction: column;
  gap: 3px;
  min-width: 0;
  margin-bottom: 5px;
  padding: 9px 10px;
  border-radius: 10px;
  background: var(--home-popover-soft);
}

.home-avatar__user strong,
.home-avatar__user span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.home-avatar__user strong {
  font-size: 14px;
}

.home-avatar__user span {
  color: var(--home-navbar-muted);
  font-size: 12px;
}

.home-avatar__action {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  min-height: 40px;
  padding: 0 10px;
  border: 0;
  border-radius: 10px;
  background: transparent;
  color: var(--home-navbar-text);
  font: inherit;
  font-size: 13px;
  text-align: left;
  cursor: pointer;
}

.home-avatar__action:hover,
.home-avatar__action:focus-visible {
  background: var(--home-popover-soft);
}

.home-avatar__action:focus-visible {
  outline: 2px solid var(--home-navbar-text);
  outline-offset: -2px;
}

/* 气泡挂在本组件内，用 :deep 覆盖 Element Plus 默认配色 */
.home-avatar :deep(.home-avatar__popover) {
  border-color: var(--home-popover-border);
  background: var(--home-popover-surface);
  color: var(--home-navbar-text);
}
</style>
