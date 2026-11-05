<!-- 首页模块整体布局 -->
<template>
  <div class="home-shell">
    <HomeNavbar>
      <template #actions>
        <HomeAvatar />
      </template>
    </HomeNavbar>

    <main class="home-main">
      <div class="home-main__inner">
        <RouterView />
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import HomeNavbar from './components/HomeNavbar.vue'
import HomeAvatar from './components/HomeAvatar.vue'
</script>

<style scoped>
.home-shell {
  --home-navbar-bg: #fff;
  --home-navbar-text: #252733;
  --home-navbar-muted: #777b88;
  --home-popover-surface: #fff;
  --home-popover-soft: #f2f2f2;
  --home-popover-border: #e8e9eb;

  min-height: 100vh;
  min-height: 100dvh;
  background: var(--home-navbar-bg);
  color: var(--home-navbar-text);
}

.home-main {
  min-height: inherit;
  padding-top: 8px;
}

/* 内容区与导航栏使用同一固定宽度，两者左右对齐 */
.home-main__inner {
  width: min(100%, 1200px);
  margin: 0 auto;
  padding: 0 32px;
}

@media (max-width: 760px) {
  .home-main__inner {
    padding: 0 16px;
  }
}
</style>
