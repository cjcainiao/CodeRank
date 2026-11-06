<!-- 做题模块整体布局 -->
<template>
  <div class="problem-shell">
    <header class="problem-bar">
      <RouterLink class="problem-bar__back" :to="{ name: 'home-questions' }">
        <ChevronLeft :size="16" :stroke-width="2" aria-hidden="true" />返回题库
      </RouterLink>
      <span class="problem-bar__brand">CodeRank</span>
    </header>

    <main class="problem-main">
      <RouterView />
    </main>
  </div>
</template>

<script setup lang="ts">
import { ChevronLeft } from '@lucide/vue'
</script>

<style scoped>
/* 做题模块独立于首页，主题变量挂在本模块根节点 */
.problem-shell {
  --problem-bg: var(--backend-surface);
  --problem-text: var(--backend-text);
  --problem-muted: var(--backend-muted);
  --problem-soft: color-mix(in srgb, var(--backend-active) 55%, var(--backend-surface));
  --problem-border: var(--backend-border);
  --problem-scrollbar: color-mix(in srgb, var(--problem-muted) 55%, var(--problem-bg));

  display: flex;
  flex-direction: column;
  height: 100vh;
  height: 100dvh;
  background: var(--problem-bg);
  color: var(--problem-text);
}

/* 顶部返回条，横向铺满并贴边 */
.problem-bar {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: none;
  padding: 10px 20px;
  border-bottom: 1px solid var(--problem-border);
}

.problem-bar__back {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: var(--problem-muted);
  font-size: 13px;
  text-decoration: none;
}

.problem-bar__back:hover {
  color: var(--problem-text);
}

.problem-bar__brand {
  margin-left: auto;
  font-size: 15px;
  font-weight: 700;
}

/* 内容区铺满剩余高度，页面内部自行滚动 */
.problem-main {
  flex: 1;
  min-height: 0;
  padding: 0;
  overflow: hidden;
}

@media (max-width: 760px) {
  .problem-bar {
    padding-right: 14px;
    padding-left: 14px;
  }
}
</style>
