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
  --problem-bg: #fff;
  --problem-text: #252733;
  --problem-muted: #777b88;
  --problem-soft: #f2f2f2;
  --problem-border: #e8e9eb;

  display: flex;
  flex-direction: column;
  min-height: 100vh;
  min-height: 100dvh;
  background: var(--problem-bg);
  color: var(--problem-text);
}

/* 顶部返回条，宽度与内容区对齐 */
.problem-bar {
  display: flex;
  align-items: center;
  gap: 16px;
  width: min(100%, 1400px);
  margin: 0 auto;
  padding: 14px 32px;
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

.problem-main {
  flex: 1;
  width: min(100%, 1400px);
  margin: 0 auto;
  padding: 24px 32px 48px;
}

@media (max-width: 760px) {
  .problem-bar,
  .problem-main {
    padding-right: 16px;
    padding-left: 16px;
  }
}
</style>
