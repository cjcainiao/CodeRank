<!-- 后台标签管理页面 -->
<template>
  <section class="page">
    <header class="page__header">
      <h2 class="page__title">标签管理</h2>
      <el-button type="primary" @click="openCreate">新增标签</el-button>
    </header>

    <el-form class="page__filters" :inline="true" @submit.prevent>
      <el-form-item label="标签名">
        <el-input
          v-model="filters.name"
          placeholder="标签名模糊搜索"
          clearable
          class="page__input"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="search">查询</el-button>
        <el-button @click="resetFilters">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table
      v-loading="loading"
      :data="rows"
      class="page__table"
      border
      @sort-change="handleSortChange"
    >
      <el-table-column prop="id" label="ID" width="180" show-overflow-tooltip sortable="custom" />
      <el-table-column label="标签名" min-width="160">
        <template #default="{ row }">
          <el-tag :color="row.color || undefined" :style="tagStyle(row.color)">
            {{ row.name }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="颜色" width="110">
        <template #default="{ row }">{{ row.color || '-' }}</template>
      </el-table-column>
      <el-table-column prop="sortOrder" label="排序值" width="100" sortable="custom" />
      <el-table-column prop="createTime" label="创建时间" width="155" sortable="custom">
        <template #default="{ row }">{{ row.createTime || '-' }}</template>
      </el-table-column>
      <el-table-column prop="updateTime" label="更新时间" width="155" sortable="custom">
        <template #default="{ row }">{{ row.updateTime || '-' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="removeTag(row)">删除</el-button>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="暂无数据" :image-size="80" />
      </template>
    </el-table>

    <el-pagination
      v-model:current-page="pageNum"
      v-model:page-size="pageSize"
      class="page__pagination"
      :total="total"
      :page-sizes="PAGE_SIZES"
      layout="total, sizes, prev, pager, next"
      @current-change="loadTags"
      @size-change="handleSizeChange"
    />

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="420px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="标签名" prop="name">
          <el-input v-model="form.name" placeholder="如二分、动态规划" />
        </el-form-item>
        <el-form-item label="颜色" prop="color">
          <div class="color-picker">
            <button
              class="color-picker__swatch color-picker__swatch--none"
              :class="{ 'color-picker__swatch--active': !form.color }"
              type="button"
              aria-label="不使用颜色"
              title="不使用颜色"
              @click="form.color = ''"
            >
              <X :size="12" :stroke-width="2" aria-hidden="true" />
            </button>
            <button
              v-for="color in PRESET_COLORS"
              :key="color"
              class="color-picker__swatch"
              :class="{ 'color-picker__swatch--active': form.color === color }"
              type="button"
              :style="{ background: color, color: contrastTextColor(color) }"
              :aria-label="`使用颜色 ${color}`"
              :title="color"
              @click="form.color = color"
            >
              <Check
                v-if="form.color === color"
                :size="14"
                :stroke-width="3"
                aria-hidden="true"
              />
            </button>
          </div>
        </el-form-item>
        <el-form-item label="排序值" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="9999" controls-position="right" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">确定</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Check, X } from '@lucide/vue'
import { contrastTextColor } from '@/utils/colorUtil'
import {
  createTag,
  deleteTag,
  pageTags,
  updateTag,
  type TagCreatePayload,
  type TagQuery,
  type TagUpdatePayload,
  type TagVO,
} from '@/api/backend/tag'

// 可选每页条数
const PAGE_SIZES = [10, 20, 50, 100]

// 颜色选择器的预置色
const PRESET_COLORS = [
  '#409eff',
  '#67c23a',
  '#e6a23c',
  '#f56c6c',
  '#909399',
  '#9b59b6',
  '#16a085',
  '#2c3e50',
]

const loading = ref(false)
const submitting = ref(false)
const rows = ref<TagVO[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const filters = reactive<{ name: string }>({ name: '' })

const dialogVisible = ref(false)
const editingId = ref<string | null>(null)
const formRef = ref<FormInstance>()

const isEditing = computed(() => editingId.value !== null)
const dialogTitle = computed(() => (isEditing.value ? '编辑标签' : '新增标签'))

// 表单字段，新增与编辑共用
const form = reactive<{ name: string; color: string; sortOrder: number }>({
  name: '',
  color: '',
  sortOrder: 0,
})

// 校验规则
const rules = reactive<FormRules>({
  name: [
    { required: true, message: '请输入标签名', trigger: 'blur' },
    { max: 50, message: '标签名长度不能超过 50', trigger: 'blur' },
  ],
})

// 当前排序字段与方向，空表示默认排序
const sortField = ref('')
const sortOrder = ref<'asc' | 'desc' | ''>('')

// 有自定义颜色时按色值渲染标签，无颜色时走默认样式
function tagStyle(color: string | null): Record<string, string> | undefined {
  if (!color) return undefined
  return { color: contrastTextColor(color), borderColor: color }
}

// 表头排序变化后回到第一页重新查询
function handleSortChange({
  prop,
  order,
}: {
  prop: string | null
  order: 'ascending' | 'descending' | null
}) {
  sortField.value = order ? (prop ?? '') : ''
  sortOrder.value = order === 'ascending' ? 'asc' : order === 'descending' ? 'desc' : ''
  pageNum.value = 1
  loadTags()
}

// 加载当前页标签，返回本次查询的命中总数
async function loadTags(): Promise<number> {
  loading.value = true
  try {
    const query: TagQuery = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      name: filters.name || undefined,
      sortField: sortField.value || undefined,
      sortOrder: sortOrder.value || undefined,
    }
    const page = await pageTags(query)
    rows.value = page.records
    total.value = page.total
    return page.total
  } finally {
    loading.value = false
  }
}

// 筛选条件变化后自动查询，输入框防抖避免每次按键都请求
let filterTimer: ReturnType<typeof setTimeout> | null = null

watch(
  () => filters.name,
  () => {
    if (filterTimer) clearTimeout(filterTimer)
    filterTimer = setTimeout(() => {
      pageNum.value = 1
      loadTags()
    }, 300)
  },
)

// 条件变更后回到第一页重新查询，并提示命中情况
async function search() {
  pageNum.value = 1
  const hit = await loadTags()
  if (hit === 0) {
    ElMessage.warning('没有符合条件的标签')
    return
  }
  ElMessage.success(`查询完成，共 ${hit} 条标签`)
}

// 清空筛选条件并重新查询，只提示已重置
async function resetFilters() {
  // 取消待执行的自动查询，避免与本次重置重复请求
  if (filterTimer) {
    clearTimeout(filterTimer)
    filterTimer = null
  }
  filters.name = ''
  pageNum.value = 1
  await loadTags()
  ElMessage.success('筛选条件已重置')
}

// 切换每页条数后回到第一页
function handleSizeChange() {
  pageNum.value = 1
  loadTags()
}

// 恢复表单为默认值
function resetForm() {
  form.name = ''
  form.color = ''
  form.sortOrder = 0
  formRef.value?.clearValidate()
}

// 打开新增弹窗
function openCreate() {
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

// 打开编辑弹窗，回填当前行数据
function openEdit(row: TagVO) {
  editingId.value = row.id
  resetForm()
  form.name = row.name
  form.color = row.color ?? ''
  form.sortOrder = row.sortOrder
  dialogVisible.value = true
}

// 提交新增或修改
async function submit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (editingId.value === null) {
      const payload: TagCreatePayload = {
        name: form.name,
        color: form.color || undefined,
        sortOrder: form.sortOrder,
      }
      await createTag(payload)
      ElMessage.success('新增成功')
    } else {
      const payload: TagUpdatePayload = {
        id: editingId.value,
        name: form.name,
        color: form.color || undefined,
        sortOrder: form.sortOrder,
      }
      await updateTag(payload)
      ElMessage.success('修改成功')
    }
    dialogVisible.value = false
    loadTags()
  } finally {
    submitting.value = false
  }
}

// 删除标签，二次确认后执行
async function removeTag(row: TagVO) {
  try {
    await ElMessageBox.confirm(`确定删除标签「${row.name}」吗？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
  } catch {
    // 用户取消删除，不做处理。
    return
  }
  await deleteTag(row.id)
  ElMessage.success('删除成功')
  // 删掉当前页最后一条时回退一页，避免停在空页
  if (rows.value.length === 1 && pageNum.value > 1) pageNum.value -= 1
  loadTags()
}

onMounted(loadTags)

onBeforeUnmount(() => {
  if (filterTimer) clearTimeout(filterTimer)
})
</script>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.page__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.page__title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.page__filters {
  display: flex;
  flex-wrap: wrap;
  gap: 4px 0;
}

.page__input {
  width: 170px;
}

.page__pagination {
  justify-content: flex-end;
}

/* 颜色色块选择器 */
.color-picker {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.color-picker__swatch {
  display: grid;
  place-items: center;
  width: 24px;
  height: 24px;
  padding: 0;
  border: 0;
  border-radius: 50%;
  box-shadow: inset 0 0 0 1px rgb(0 0 0 / 12%);
  color: #fff;
  cursor: pointer;
  transition: transform 140ms ease;
}

.color-picker__swatch:hover {
  transform: scale(1.12);
}

/* 未选中时只显示描边，选中时用对比色对勾标记，不加外框 */
.color-picker__swatch--none {
  background: var(--backend-surface);
  box-shadow: inset 0 0 0 1px var(--backend-border);
  color: var(--backend-muted);
}

.color-picker__swatch--none.color-picker__swatch--active {
  box-shadow: inset 0 0 0 1.5px var(--backend-muted);
  color: var(--backend-text);
}

.color-picker__swatch:focus-visible {
  outline: 2px solid var(--backend-text);
  outline-offset: 2px;
}

@media (prefers-reduced-motion: reduce) {
  .color-picker__swatch {
    transition: none;
  }
}
</style>
