<!-- 评测结果测试点面板 -->
<template>
  <section class="judge-panel">
    <h2 class="judge-panel__title">测试点信息</h2>

    <div v-if="cases.length" class="judge-panel__cells">
      <div
        v-for="item in cases"
        :key="item.caseNo"
        class="judge-panel__cell"
        :style="{ background: styleOf(item.status, item.passed).color }"
      >
        <span class="judge-panel__cell-no">#{{ item.caseNo }}</span>
        <span class="judge-panel__cell-status">
          {{ styleOf(item.status, item.passed).abbr }}
        </span>
        <span class="judge-panel__cell-meta">
          {{ item.time ?? 0 }}ms/{{ item.memory ?? 0 }}KB
        </span>
      </div>
    </div>

    <p v-else class="judge-panel__empty">该次评测没有测试点明细</p>
  </section>
</template>

<script setup lang="ts">
import { judgeStatusStyle } from '@/utils/judgeStatusUtil'
import type { JudgeCaseVO } from '@/api/problem/submitCode'

defineProps<{
  // 各测试点结果
  cases: JudgeCaseVO[]
}>()

// 状态配色查询入口，按单个测试点取样式
const styleOf = judgeStatusStyle</script>

<style scoped>
.judge-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.judge-panel__title {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
}

/* 测试点方块按固定尺寸铺开，放不下时自动换行 */
.judge-panel__cells {
  display: grid;
  grid-template-columns: repeat(auto-fill, 120px);
  gap: 9px;
}

.judge-panel__cell {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  width: 120px;
  height: 120px;
  padding: 8px 10px;
  border-radius: 3px;
  color: #fff;
  user-select: none;
}

.judge-panel__cell-no {
  font-size: 12px;
  font-weight: 600;
  line-height: 1.2;
}

/* 状态缩写居中放大，一眼能看出结果 */
.judge-panel__cell-status {
  align-self: center;
  font-size: 22px;
  font-weight: 600;
  line-height: 1;
}

.judge-panel__cell-meta {
  font-size: 12px;
  opacity: 0.95;
  text-align: center;
  white-space: nowrap;
}

.judge-panel__empty {
  margin: 0;
  color: var(--problem-muted);
  font-size: 13px;
}
</style>
