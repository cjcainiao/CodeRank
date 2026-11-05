<!-- 题目评测数据管理，维护成对的输入输出文件 -->
<template>
  <div class="test-data">
    <header class="test-data__header">
      <span class="test-data__hint">
        每组评测数据包含一个输入文件和一个输出文件，编号自动递增。
      </span>
      <el-button type="primary" :icon="Upload" @click="openUpload">上传一组</el-button>
    </header>

    <el-table v-loading="loading" :data="displayRows" border class="test-data__table">
      <el-table-column label="编号" width="90" align="center">
        <template #default="{ row }">{{ row.caseNo }}</template>
      </el-table-column>
      <el-table-column label="输入文件" min-width="240">
        <template #default="{ row }">
          <div class="test-data__cell">
            <span class="test-data__file-name">{{ row.caseNo }}.in</span>
            <span class="test-data__size">{{ formatSize(row.inputSize) }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="输出文件" min-width="240">
        <template #default="{ row }">
          <div class="test-data__cell">
            <span class="test-data__file-name">{{ row.caseNo }}.out</span>
            <span class="test-data__size">{{ formatSize(row.outputSize) }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="可见性" width="110" align="center">
        <template #default="{ row }">
          <el-switch
            :model-value="row.visible === VISIBLE_SHOWN"
            :loading="switchingId === row.id"
            :disabled="draftMode"
            inline-prompt
            active-text="展示"
            inactive-text="隐藏"
            @change="(value: boolean) => toggleVisible(row, value)"
          />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" align="center" fixed="right">
        <template #default="{ row }">
          <el-button link type="danger" @click="removeTestData(row)">删除</el-button>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="尚未上传评测数据" :image-size="70" />
      </template>
    </el-table>

    <el-dialog v-model="dialogVisible" title="上传评测数据" width="560px" destroy-on-close>
      <el-form label-width="100px">
        <el-form-item label="输入文件">
          <el-upload
            ref="inputUploadRef"
            class="test-data__upload"
            drag
            accept=".in"
            :limit="1"
            :auto-upload="false"
            :show-file-list="false"
            :on-change="(file: UploadFile) => handleFileChange(file, 'input')"
            :on-exceed="() => ElMessage.warning('只能选择一个输入文件')"
          >
            <Upload class="test-data__upload-icon" :size="26" :stroke-width="1.6" />
            <div class="test-data__upload-text">
              <span v-if="inputFile" class="test-data__file-name">{{ inputFile.name }}</span>
              <template v-else>
                将 .in 文件拖到此处，或<em>点击选择</em>
              </template>
            </div>
          </el-upload>
        </el-form-item>
        <el-form-item label="输出文件">
          <el-upload
            ref="outputUploadRef"
            class="test-data__upload"
            drag
            accept=".out"
            :limit="1"
            :auto-upload="false"
            :show-file-list="false"
            :on-change="(file: UploadFile) => handleFileChange(file, 'output')"
            :on-exceed="() => ElMessage.warning('只能选择一个输出文件')"
          >
            <Upload class="test-data__upload-icon" :size="26" :stroke-width="1.6" />
            <div class="test-data__upload-text">
              <span v-if="outputFile" class="test-data__file-name">{{ outputFile.name }}</span>
              <template v-else>
                将 .out 文件拖到此处，或<em>点击选择</em>
              </template>
            </div>
          </el-upload>
        </el-form-item>
        <el-form-item label="可见性">
          <el-radio-group v-model="visible">
            <el-radio :value="VISIBLE_SHOWN">展示</el-radio>
            <el-radio :value="VISIBLE_HIDDEN">隐藏</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">上传</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { ElMessage, ElMessageBox, type UploadFile, type UploadInstance } from 'element-plus'
import { Upload } from '@lucide/vue'
import {
  deleteQuestionTestData,
  listQuestionTestData,
  updateQuestionTestData,
  uploadQuestionTestData,
  type QuestionTestDataVO,
} from '@/api/backend/questionTestData'

// 可见性：展示
const VISIBLE_SHOWN = 1

// 可见性：隐藏
const VISIBLE_HIDDEN = 0

// 草稿评测数据，题目尚未保存时暂存在前端
export interface DraftTestData {
  inputFile: File
  outputFile: File
  visible: number
}

// 列表行，草稿态与接口态共用
interface TestDataRow {
  id: string
  caseNo: number
  inputSize: number
  outputSize: number
  visible: number
}

const props = defineProps<{
  // 所属题目，为空表示题目尚未保存，走草稿模式
  questionId: string
}>()

// 草稿模式下由父组件通过 v-model 持有数据
const drafts = defineModel<DraftTestData[]>({ default: () => [] })

const loading = ref(false)
const submitting = ref(false)
const remoteRows = ref<QuestionTestDataVO[]>([])

const dialogVisible = ref(false)
const inputUploadRef = ref<UploadInstance>()
const outputUploadRef = ref<UploadInstance>()
const inputFile = ref<File | null>(null)
const outputFile = ref<File | null>(null)
const visible = ref<number>(VISIBLE_HIDDEN)

// 正在切换可见性的行 id，用于按钮加载态
const switchingId = ref<string | null>(null)

// 题目未保存时只能暂存草稿
const draftMode = computed(() => !props.questionId)

// 表格展示行，草稿态按上传顺序编号
const displayRows = computed<TestDataRow[]>(() => {
  if (draftMode.value) {
    return drafts.value.map((item, index) => ({
      id: `draft-${index}`,
      caseNo: index + 1,
      inputSize: item.inputFile.size,
      outputSize: item.outputFile.size,
      visible: item.visible,
    }))
  }
  return remoteRows.value.map((item) => ({
    id: item.id,
    caseNo: item.caseNo,
    inputSize: item.inputSize,
    outputSize: item.outputSize,
    visible: item.visible,
  }))
})

// 文件大小转可读文案
function formatSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / 1024 / 1024).toFixed(1)} MB`
}

// 加载当前题目的评测数据
async function loadTestData() {
  if (draftMode.value) {
    remoteRows.value = []
    return
  }
  loading.value = true
  try {
    remoteRows.value = await listQuestionTestData(props.questionId)
  } finally {
    loading.value = false
  }
}

// 切换题目后重新加载
watch(() => props.questionId, loadTestData, { immediate: true })

// 打开上传弹窗，清空上次选择
function openUpload() {
  inputFile.value = null
  outputFile.value = null
  visible.value = VISIBLE_HIDDEN
  inputUploadRef.value?.clearFiles()
  outputUploadRef.value?.clearFiles()
  dialogVisible.value = true
}

// 选择文件后记录原始 File，提交时直接上传
function handleFileChange(file: UploadFile, target: 'input' | 'output') {
  const raw = file.raw
  if (!raw) return
  if (target === 'input') inputFile.value = raw
  else outputFile.value = raw
}

// 提交上传，草稿模式只改本地数组
async function submit() {
  if (!inputFile.value || !outputFile.value) {
    ElMessage.warning('请同时选择输入文件与输出文件')
    return
  }

  if (draftMode.value) {
    drafts.value = [
      ...drafts.value,
      { inputFile: inputFile.value, outputFile: outputFile.value, visible: visible.value },
    ]
    ElMessage.success('已添加，保存题目后自动上传')
    dialogVisible.value = false
    return
  }

  submitting.value = true
  try {
    await uploadQuestionTestData(
      props.questionId,
      inputFile.value,
      outputFile.value,
      visible.value,
    )
    ElMessage.success('上传成功')
    dialogVisible.value = false
    loadTestData()
  } finally {
    submitting.value = false
  }
}

// 切换可见性，草稿态只改本地数据
async function toggleVisible(row: TestDataRow, shown: boolean) {
  const next = shown ? VISIBLE_SHOWN : VISIBLE_HIDDEN

  if (draftMode.value) {
    const index = Number(row.id.replace('draft-', ''))
    const item = drafts.value[index]
    if (item) drafts.value[index] = { ...item, visible: next }
    return
  }

  switchingId.value = row.id
  try {
    await updateQuestionTestData({ id: row.id, visible: next })
    // 直接改本地行，避免整表重载导致闪烁
    const target = remoteRows.value.find((item) => item.id === row.id)
    if (target) target.visible = next
  } finally {
    switchingId.value = null
  }
}

// 删除评测数据，二次确认后执行
async function removeTestData(row: TestDataRow) {
  try {
    await ElMessageBox.confirm(`确定删除第 ${row.caseNo} 组评测数据吗？`, '删除确认', {
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
  await deleteQuestionTestData(row.id)
  ElMessage.success('删除成功')
  loadTestData()
}
</script>

<style scoped>
.test-data {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.test-data__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.test-data__hint {
  color: var(--backend-muted);
  font-size: 13px;
}

/* 文件名与大小同一行展示，大小靠右弱化 */
.test-data__cell {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  min-width: 0;
}

.test-data__file-name {
  overflow: hidden;
  font-family: 'JetBrains Mono', 'Cascadia Code', Consolas, monospace;
  font-size: 13px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.test-data__size {
  flex: none;
  color: var(--backend-muted);
  font-size: 12px;
}

/* 拖拽上传区：图标与提示上下排列，整体占满表单项宽度 */
.test-data__upload {
  width: 100%;
}

.test-data__upload :deep(.el-upload-dragger) {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  height: 110px;
  padding: 0 16px;
  border-color: var(--backend-border);
  border-radius: 8px;
  background: color-mix(in srgb, var(--backend-active) 40%, transparent);
}

.test-data__upload :deep(.el-upload-dragger:hover) {
  border-color: var(--backend-text);
}

.test-data__upload-icon {
  color: var(--backend-muted);
}

.test-data__upload-text {
  color: var(--backend-muted);
  font-size: 13px;
  line-height: 1.5;
  text-align: center;
}

.test-data__upload-text em {
  color: var(--backend-text);
  font-style: normal;
  font-weight: 600;
}

/* 已选文件名用等宽字体，过长时省略 */
.test-data__upload-text .test-data__file-name {
  display: block;
  max-width: 100%;
  color: var(--backend-text);
}
</style>
