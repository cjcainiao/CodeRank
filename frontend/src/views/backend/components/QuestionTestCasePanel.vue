<!-- 题目测试用例管理，按判题模式维护入参与期望值 -->
<template>
  <div class="test-case">
    <header class="test-case__header">
      <span class="test-case__hint">
        <template v-if="isCoreMode">
          核心代码模式：入参为 JSON 数组，期望值为 JSON，隐藏用例不会展示给用户。
        </template>
        <template v-else>
          标准输入输出模式：入参与输出均为纯文本，隐藏用例不会展示给用户。
        </template>
      </span>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增用例</el-button>
    </header>

    <el-table v-loading="loading" :data="displayRows" border class="test-case__table">
      <el-table-column label="序号" width="70">
        <template #default="{ $index }">{{ $index + 1 }}</template>
      </el-table-column>
      <el-table-column :label="isCoreMode ? '入参' : '标准输入'" min-width="220">
        <template #default="{ row }">
          <pre class="test-case__code">{{ caseInput(row) }}</pre>
        </template>
      </el-table-column>
      <el-table-column :label="isCoreMode ? '期望返回值' : '标准输出'" min-width="160">
        <template #default="{ row }">
          <pre class="test-case__code">{{ caseOutput(row) }}</pre>
        </template>
      </el-table-column>
      <el-table-column label="可见性" width="100">
        <template #default="{ row }">
          <el-tag :type="row.visible === VISIBLE_SHOWN ? 'success' : 'info'">
            {{ row.visible === VISIBLE_SHOWN ? '展示' : '隐藏' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="sortOrder" label="排序" width="80" />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="removeTestCase(row)">删除</el-button>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="尚未添加测试用例" :image-size="70" />
      </template>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <!-- 核心代码模式用 JSON 入参 -->
        <template v-if="isCoreMode">
          <el-form-item label="入参" prop="args">
            <CodeEditor
              v-model="form.args"
              class="test-case__editor"
              language="json"
              height="150px"
              placeholder="JSON 数组，如 [[2,7,11,15],9]"
            />
          </el-form-item>
          <el-form-item label="期望返回值" prop="expected">
            <CodeEditor
              v-model="form.expected"
              class="test-case__editor"
              language="json"
              height="120px"
              placeholder="JSON 值，如 [0,1]"
            />
          </el-form-item>
        </template>

        <!-- 标准输入输出模式用纯文本 -->
        <template v-else>
          <el-form-item label="标准输入" prop="inputData">
            <CodeEditor
              v-model="form.inputData"
              class="test-case__editor"
              language="text"
              height="180px"
              placeholder="程序的标准输入内容，注意保留换行"
            />
          </el-form-item>
          <el-form-item label="标准输出" prop="expectedOut">
            <CodeEditor
              v-model="form.expectedOut"
              class="test-case__editor"
              language="text"
              height="150px"
              placeholder="期望的标准输出内容"
            />
          </el-form-item>
        </template>

        <el-form-item label="可见性" prop="visible">
          <el-radio-group v-model="form.visible">
            <el-radio :value="VISIBLE_SHOWN">展示</el-radio>
            <el-radio :value="VISIBLE_HIDDEN">隐藏</el-radio>
          </el-radio-group>
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
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Plus } from '@lucide/vue'
import CodeEditor from '@/components/editor/CodeEditor.vue'
import {
  createQuestionTestCase,
  deleteQuestionTestCase,
  listQuestionTestCases,
  updateQuestionTestCase,
  type QuestionTestCaseVO,
} from '@/api/backend/questionCode'

// 可见性：展示
const VISIBLE_SHOWN = 1

// 可见性：隐藏
const VISIBLE_HIDDEN = 0

// 判题模式：核心代码
const JUDGE_MODE_CORE = 2

// 草稿用例，题目尚未保存时暂存在前端
export interface DraftTestCase {
  args: string
  expected: string
  inputData: string
  expectedOut: string
  visible: number
  sortOrder: number
}

// 列表行，草稿态与接口态共用
interface CaseRow {
  id: string
  args: string
  expected: string
  inputData: string
  expectedOut: string
  visible: number
  sortOrder: number
}

const props = defineProps<{
  // 所属题目，为空表示题目尚未保存，走草稿模式
  questionId: string
  // 判题模式：1 标准输入输出，2 核心代码
  judgeMode: number
}>()

// 草稿模式下由父组件通过 v-model 持有数据
const drafts = defineModel<DraftTestCase[]>({ default: () => [] })

const loading = ref(false)
const submitting = ref(false)
const remoteRows = ref<QuestionTestCaseVO[]>([])

const dialogVisible = ref(false)
const editingId = ref<string | null>(null)
const editingIndex = ref<number | null>(null)
const formRef = ref<FormInstance>()

// 题目未保存时只能暂存草稿
const draftMode = computed(() => !props.questionId)

// 核心代码模式用 JSON 入参，标准输入输出模式用纯文本
const isCoreMode = computed(() => props.judgeMode === JUDGE_MODE_CORE)

const isEditing = computed(() => editingId.value !== null || editingIndex.value !== null)
const dialogTitle = computed(() => (isEditing.value ? '编辑测试用例' : '新增测试用例'))

// 表格展示行，草稿态取本地数据，接口态取远端数据
const displayRows = computed<CaseRow[]>(() => {
  if (draftMode.value) {
    return drafts.value.map((item, index) => ({ id: `draft-${index}`, ...item }))
  }
  return remoteRows.value.map((item) => ({
    id: item.id,
    args: item.args ?? '',
    expected: item.expected ?? '',
    inputData: item.inputData ?? '',
    expectedOut: item.expectedOut ?? '',
    visible: item.visible,
    sortOrder: item.sortOrder,
  }))
})

// 表单字段
const form = reactive<{
  args: string
  expected: string
  inputData: string
  expectedOut: string
  visible: number
  sortOrder: number
}>({
  args: '',
  expected: '',
  inputData: '',
  expectedOut: '',
  visible: VISIBLE_HIDDEN,
  sortOrder: 0,
})

// 校验规则随判题模式切换
const rules = computed<FormRules>(() =>
  isCoreMode.value
    ? {
        args: [{ required: true, message: '请输入函数入参', trigger: 'blur' }],
        expected: [{ required: true, message: '请输入期望返回值', trigger: 'blur' }],
      }
    : {
        inputData: [{ required: true, message: '请输入标准输入', trigger: 'blur' }],
        expectedOut: [{ required: true, message: '请输入标准输出', trigger: 'blur' }],
      },
)

// 列表里按模式取对应的输入内容
function caseInput(row: CaseRow): string {
  return (isCoreMode.value ? row.args : row.inputData) || '（空）'
}

// 列表里按模式取对应的输出内容
function caseOutput(row: CaseRow): string {
  return (isCoreMode.value ? row.expected : row.expectedOut) || '（空）'
}

// 加载当前题目的测试用例
async function loadTestCases() {
  if (draftMode.value) {
    remoteRows.value = []
    return
  }
  loading.value = true
  try {
    remoteRows.value = await listQuestionTestCases(props.questionId)
  } finally {
    loading.value = false
  }
}

// 切换题目后重新加载
watch(() => props.questionId, loadTestCases, { immediate: true })

// 恢复表单默认值
function resetForm() {
  form.args = ''
  form.expected = ''
  form.inputData = ''
  form.expectedOut = ''
  form.visible = VISIBLE_HIDDEN
  form.sortOrder = displayRows.value.length
  formRef.value?.clearValidate()
}

// 打开新增弹窗
function openCreate() {
  editingId.value = null
  editingIndex.value = null
  resetForm()
  dialogVisible.value = true
}

// 打开编辑弹窗，回填当前行
function openEdit(row: CaseRow) {
  if (draftMode.value) {
    editingIndex.value = Number(row.id.replace('draft-', ''))
    editingId.value = null
  } else {
    editingId.value = row.id
    editingIndex.value = null
  }
  resetForm()
  form.args = row.args
  form.expected = row.expected
  form.inputData = row.inputData
  form.expectedOut = row.expectedOut
  form.visible = row.visible
  form.sortOrder = row.sortOrder
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
    await loadTestCases()
  } finally {
    submitting.value = false
  }
}

// 按判题模式只提交对应字段，避免另一种模式留下脏数据
function buildCasePayload() {
  const base = { visible: form.visible, sortOrder: form.sortOrder }
  if (isCoreMode.value) {
    return { ...base, args: form.args, expected: form.expected }
  }
  return { ...base, inputData: form.inputData, expectedOut: form.expectedOut }
}

// 草稿模式只改本地数组
function submitDraft() {
  const next: DraftTestCase = {
    args: isCoreMode.value ? form.args : '',
    expected: isCoreMode.value ? form.expected : '',
    inputData: isCoreMode.value ? '' : form.inputData,
    expectedOut: isCoreMode.value ? '' : form.expectedOut,
    visible: form.visible,
    sortOrder: form.sortOrder,
  }
  const list = [...drafts.value]
  if (editingIndex.value === null) {
    list.push(next)
  } else {
    list[editingIndex.value] = next
  }
  drafts.value = list
  ElMessage.success(editingIndex.value === null ? '已添加' : '已更新')
}

// 接口模式直接落库
async function submitRemote() {
  const payload = buildCasePayload()
  if (editingId.value === null) {
    await createQuestionTestCase({ questionId: props.questionId, ...payload })
    ElMessage.success('新增成功')
  } else {
    await updateQuestionTestCase({ id: editingId.value, ...payload })
    ElMessage.success('修改成功')
  }
}

// 删除用例，二次确认后执行
async function removeTestCase(row: CaseRow) {
  try {
    await ElMessageBox.confirm('确定删除这条测试用例吗？', '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
  } catch {
    // 用户取消删除，不做处理。
    return
  }
  if (draftMode.value) {
    const index = Number(row.id.replace('draft-', ''))
    drafts.value = drafts.value.filter((_, i) => i !== index)
    ElMessage.success('已移除')
    return
  }
  await deleteQuestionTestCase(row.id)
  ElMessage.success('删除成功')
  loadTestCases()
}
</script>

<style scoped>
.test-case {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.test-case__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.test-case__hint {
  color: var(--backend-muted);
  font-size: 13px;
}

/* 用例内容用等宽字体，便于核对空白与换行 */
.test-case__code {
  max-height: 72px;
  margin: 0;
  overflow: hidden;
  font-family: var(--code-font-family);
  font-size: 12px;
  line-height: 1.5;
  white-space: pre-wrap;
  overflow-wrap: anywhere;
}

/* 代码编辑卡片占满表单项宽度 */
.test-case__editor {
  width: 100%;
}
</style>
