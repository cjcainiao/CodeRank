<!-- 首页顶部导航栏 -->
<template>
  <header class="home-navbar" :class="{ 'home-navbar--sticky': sticky }">
    <div class="home-navbar__inner">
      <RouterLink class="home-navbar__brand" :to="{ name: 'home-index' }" :aria-label="brandLabel">
        <span class="home-navbar__brand-name">{{ title }}</span>
      </RouterLink>

      <nav class="home-navbar__nav" aria-label="首页导航">
        <RouterLink
          v-for="item in navItems"
          :key="item.name"
          class="home-navbar__link"
          :class="{ 'home-navbar__link--active': route.name === item.name }"
          :to="{ name: item.name }"
          :aria-current="route.name === item.name ? 'page' : undefined"
        >
          {{ item.label }}
        </RouterLink>
      </nav>

      <div class="home-navbar__actions">
        <slot name="actions" />
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

// 导航栏菜单项，新增页面时在此追加
const navItems = [
  { name: 'home-index', label: '首页' },
  { name: 'home-questions', label: '题库' },
  { name: 'home-contests', label: '比赛' },
] as const

const route = useRoute()

const props = withDefaults(
  defineProps<{
    // 品牌文字，默认取站点名称
    title?: string
    // 滚动时是否吸顶
    sticky?: boolean
  }>(),
  {
    title: 'CodeRank',
    sticky: true,
  },
)

// 品牌链接的无障碍名称
const brandLabel = computed(() => `返回 ${props.title} 首页`)
</script>

<style scoped>
.home-navbar {
  height: 60px;
  background: var(--home-navbar-bg);
  color: var(--home-navbar-text);
}

.home-navbar--sticky {
  position: sticky;
  top: 0;
  z-index: 10;
}

/* 内容宽度固定，超出后两侧留白，不随屏幕宽度拉伸 */
.home-navbar__inner {
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  align-items: center;
  gap: 24px;
  width: min(100%, 1200px);
  height: 100%;
  margin: 0 auto;
  padding: 0 32px;
}

.home-navbar__brand {
  display: inline-flex;
  align-items: center;
  justify-self: start;
  color: inherit;
  text-decoration: none;
}

.home-navbar__brand-name {
  font-size: 17px;
  font-weight: 700;
  line-height: 1.1;
}

.home-navbar__nav {
  display: flex;
  align-items: stretch;
  justify-self: center;
  height: 100%;
  gap: 4px;
}

.home-navbar__link {
  position: relative;
  display: inline-flex;
  align-items: center;
  padding: 0 14px;
  color: var(--home-navbar-muted);
  font-size: 15px;
  text-decoration: none;
  white-space: nowrap;
}

/* 激活菜单项下方的高亮条 */
.home-navbar__link::after {
  content: '';
  position: absolute;
  right: 0;
  bottom: 0;
  left: 0;
  border-bottom: 2px solid currentColor;
  opacity: 0;
  transform: scaleX(0);
  transition:
    opacity 180ms ease,
    transform 180ms ease;
}

.home-navbar__link:hover,
.home-navbar__link:focus-visible,
.home-navbar__link--active {
  color: var(--home-navbar-text);
}

.home-navbar__link--active {
  font-weight: 600;
}

.home-navbar__link--active::after {
  opacity: 1;
  transform: scaleX(1);
}

.home-navbar__link:focus-visible {
  outline: 2px solid var(--home-navbar-text);
  outline-offset: 2px;
}

.home-navbar__actions {
  display: flex;
  align-items: center;
  justify-self: end;
  gap: 12px;
}

@media (max-width: 760px) {
  .home-navbar {
    height: 54px;
  }

  .home-navbar__inner {
    gap: 16px;
    padding: 0 16px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .home-navbar__link::after {
    transition: none;
  }
}
</style>
