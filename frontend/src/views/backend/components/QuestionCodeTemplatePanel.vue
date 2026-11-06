<!-- 题目代码模板管理，按语言维护初始代码 -->
<template>
  <div class="code-template">
    <header class="code-template__header">
      <span class="code-template__hint">
        初始代码回显到编辑器；桩程序里把用户代码的插入位置写成左右各两个花括号包住 code，评测时拼成完整程序。
      </span>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增语言</el-button>
    </header>

    <el-table v-loading="loading" :data="displayRows" border class="code-template__table">
      <el-table-column label="语言" width="120">
        <template #default="{ row }">{{ languageLabel(row.language) }}</template>
      </el-table-column>
      <el-table-column label="初始代码" min-width="260">
        <template #default="{ row }">
          <pre class="code-template__preview">{{ row.template }}</pre>
        </template>
      </el-table-column>
      <el-table-column label="桩程序" min-width="260">
        <template #default="{ row }">
          <pre v-if="row.stubTemplate" class="code-template__preview">{{ row.stubTemplate }}</pre>
          <span v-else class="code-template__empty">未配置</span>
        </template>
      </el-table-column>
      <el-table-column v-if="!draftMode" label="更新时间" width="170">
        <template #default="{ row }">{{ row.updateTime || '-' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="removeTemplate(row)">删除</el-button>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="尚未添加任何语言的初始代码" :image-size="70" />
      </template>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="640px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="语言" prop="language">
          <el-select v-model="form.language" class="code-template__control">
            <el-option
              v-for="option in LANGUAGE_OPTIONS"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="初始代码" prop="template">
          <CodeEditor
            v-model="form.template"
            class="code-template__editor"
            :language="editorLanguage"
            height="240px"
            placeholder="用户打开题目时看到的代码，函数框架等"
          />
        </el-form-item>
        <el-form-item label="桩程序" prop="stubTemplate">
          <CodeEditor
            v-model="form.stubTemplate"
            class="code-template__editor"
            :language="editorLanguage"
            height="280px"
            :placeholder="STUB_PLACEHOLDER_HINT"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Plus } from '@lucide/vue'
import CodeEditor from '@/components/editor/CodeEditor.vue'
import {
  createQuestionCodeTemplate,
  deleteQuestionCodeTemplate,
  listQuestionCodeTemplates,
  updateQuestionCodeTemplate,
  type QuestionCodeTemplateVO,
} from '@/api/backend/questionCode'

// 语言选项，与后端枚举一致
const LANGUAGE_OPTIONS = [
  { value: 1, label: 'Java', code: 'java' },
  { value: 2, label: 'C++', code: 'cpp' },
  { value: 3, label: 'Python', code: 'python' },
  { value: 4, label: 'Go', code: 'go' },
  { value: 5, label: 'JavaScript', code: 'javascript' },
] as const

// 桩程序占位符说明，用变量拼接避免模板里出现双花括号
const STUB_PLACEHOLDER_HINT = `完整程序，用 ${'{{'}code${'}}'} 标记用户代码插入位置`

// 草稿模板，题目尚未保存时暂存在前端
export interface DraftCodeTemplate {
  language: number
  template: string
  stubTemplate?: string
}

// 列表行，草稿态与接口态共用
interface TemplateRow {
  id: string
  language: number
  template: string
  stubTemplate?: string | null
  updateTime?: string
}

const props = defineProps<{
  // 所属题目，为空表示题目尚未保存，走草稿模式
  questionId: string
}>()

// 草稿模式下由父组件通过 v-model 持有数据
const drafts = defineModel<DraftCodeTemplate[]>({ default: () => [] })

const loading = ref(false)
const submitting = ref(false)
const remoteRows = ref<QuestionCodeTemplateVO[]>([])

const dialogVisible = ref(false)
const editingId = ref<string | null>(null)
const editingLanguage = ref<number | null>(null)
const formRef = ref<FormInstance>()

// 题目未保存时只能暂存草稿
const draftMode = computed(() => !props.questionId)

const isEditing = computed(() => editingId.value !== null || editingLanguage.value !== null)
const dialogTitle = computed(() => (isEditing.value ? '编辑代码模板' : '新增代码模板'))

// 表格展示行，草稿态取本地数据，接口态取远端数据
const displayRows = computed<TemplateRow[]>(() => {
  if (draftMode.value) {
    return drafts.value.map((item) => ({
      id: `draft-${item.language}`,
      language: item.language,
      template: item.template,
      stubTemplate: item.stubTemplate,
    }))
  }
  return remoteRows.value.map((item) => ({
    id: item.id,
    language: item.language,
    template: item.template,
    stubTemplate: item.stubTemplate,
    updateTime: item.updateTime,
  }))
})

// 表单字段
const form = reactive<{ language: number; template: string; stubTemplate: string }>({
  language: 1,
  template: '',
  stubTemplate: '',
})

// 编辑器按表单选中的语言高亮，未知语言退化为纯文本
const editorLanguage = computed(
  () => LANGUAGE_OPTIONS.find((option) => option.value === form.language)?.code ?? 'text',
)

const rules: FormRules = {
  language: [{ required: true, message: '请选择语言', trigger: 'change' }],
  template: [{ required: true, message: '请输入初始代码', trigger: 'blur' }],
}

// 语言编号转文案
function languageLabel(language: number): string {
  return LANGUAGE_OPTIONS.find((option) => option.value === language)?.label ?? '-'
}

// 加载当前题目的代码模板
async function loadTemplates() {
  if (draftMode.value) {
    remoteRows.value = []
    return
  }
  loading.value = true
  try {
    remoteRows.value = await listQuestionCodeTemplates(props.questionId)
  } finally {
    loading.value = false
  }
}

// 切换题目后重新加载
watch(() => props.questionId, loadTemplates, { immediate: true })

// 恢复表单默认值
function resetForm() {
  form.language = 1
  form.template = ''
  form.stubTemplate = ''
  formRef.value?.clearValidate()
}

// 打开新增弹窗，跳过已被占用的语言
function openCreate() {
  editingId.value = null
  editingLanguage.value = null
  resetForm()
  const used = new Set(displayRows.value.map((row) => row.language))
  const available = LANGUAGE_OPTIONS.find((option) => !used.has(option.value))
  if (!available) {
    ElMessage.warning('所有语言都已添加模板')
    return
  }
  form.language = available.value
  dialogVisible.value = true
}

// 打开编辑弹窗，回填当前行
function openEdit(row: TemplateRow) {
  if (draftMode.value) {
    editingLanguage.value = row.language
    editingId.value = null
  } else {
    editingId.value = row.id
    editingLanguage.value = null
  }
  resetForm()
  form.language = row.language
  form.template = row.template
  form.stubTemplate = row.stubTemplate ?? ''
  dialogVisible.value = true
}

// 提交新增或修改
async function submit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (draftMode.value) {
      submitDraft()
    } else {
      await submitRemote()
    }
    dialogVisible.value = false
    await loadTemplates()
  } finally {
    submitting.value = false
  }
}

// 草稿模式只改本地数组，保存题目时再一起提交
function submitDraft() {
  // 先剔除正在编辑的那条旧记录，再按语言写入并保持有序
  const rest = drafts.value.filter((item) => item.language !== editingLanguage.value)
  drafts.value = [
    ...rest,
    { language: form.language, template: form.template, stubTemplate: form.stubTemplate },
  ].sort((a, b) => a.language - b.language)
  ElMessage.success(editingLanguage.value === null ? '已添加' : '已更新')
}

// 接口模式直接落库
async function submitRemote() {
  if (editingId.value === null) {
    await createQuestionCodeTemplate({
      questionId: props.questionId,
      language: form.language,
      template: form.template,
      stubTemplate: form.stubTemplate || undefined,
    })
    ElMessage.success('新增成功')
  } else {
    await updateQuestionCodeTemplate({
      id: editingId.value,
      language: form.language,
      template: form.template,
      stubTemplate: form.stubTemplate,
    })
    ElMessage.success('修改成功')
  }
}

// 删除模板，二次确认后执行
async function removeTemplate(row: TemplateRow) {
  try {
    await ElMessageBox.confirm(`确定删除「${languageLabel(row.language)}」的代码模板吗？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
  } catch {
    // 用户取消删除，不做处理。
    return
  }
  if (draftMode.value) {
    drafts.value = drafts.value.filter((item) => item.language !== row.language)
    ElMessage.success('已移除')
    return
  }
  await deleteQuestionCodeTemplate(row.id)
  ElMessage.success('删除成功')
  loadTemplates()
}
</script>

<style scoped>
.code-template {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.code-template__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.code-template__hint {
  color: var(--backend-muted);
  font-size: 13px;
}

.code-template__control {
  width: 100%;
}

/* 列表中的代码只展示前几行，完整内容在弹窗里编辑 */
.code-template__preview {
  max-height: 96px;
  margin: 0;
  overflow: hidden;
  font-family: var(--code-font-family);
  font-size: 12px;
  line-height: 1.5;
  white-space: pre-wrap;
  overflow-wrap: anywhere;
}

.code-template__empty {
  color: var(--backend-muted);
  font-size: 12px;
}

/* 代码编辑卡片占满表单项宽度 */
.code-template__editor {
  width: 100%;
}
</style>
