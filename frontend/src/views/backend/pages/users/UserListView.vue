<!-- 后台用户列表页面 -->
<template>
  <section class="page">
    <header class="page__header">
      <h2 class="page__title">用户列表</h2>
      <el-button type="primary" @click="openCreate">新增用户</el-button>
    </header>

    <el-form class="page__filters" :inline="true" @submit.prevent>
      <el-form-item label="账号">
        <el-input
          v-model="filters.username"
          placeholder="账号模糊搜索"
          clearable
          class="page__input"
        />
      </el-form-item>
      <el-form-item label="昵称">
        <el-input
          v-model="filters.nickname"
          placeholder="昵称模糊搜索"
          clearable
          class="page__input"
        />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="filters.status" placeholder="全部" clearable class="page__select">
          <el-option label="正常" :value="0" />
          <el-option label="禁用" :value="1" />
        </el-select>
      </el-form-item>
      <el-form-item label="角色">
        <el-select v-model="filters.role" placeholder="全部" clearable class="page__select">
          <el-option label="普通用户" :value="0" />
          <el-option label="管理员" :value="1" />
        </el-select>
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
      <el-table-column prop="username" label="账号" min-width="90" show-overflow-tooltip />
      <el-table-column label="昵称" min-width="90" show-overflow-tooltip>
        <template #default="{ row }">{{ row.nickname || '-' }}</template>
      </el-table-column>
      <el-table-column label="邮箱" min-width="160" show-overflow-tooltip>
        <template #default="{ row }">{{ row.email || '-' }}</template>
      </el-table-column>
      <el-table-column label="手机号" width="120">
        <template #default="{ row }">{{ row.phone || '-' }}</template>
      </el-table-column>
      <el-table-column label="角色" width="95">
        <template #default="{ row }">
          <el-tag :type="row.role === ROLE_ADMIN ? 'warning' : 'info'">
            {{ row.role === ROLE_ADMIN ? '管理员' : '普通用户' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="85">
        <template #default="{ row }">
          <el-tag :type="row.status === STATUS_NORMAL ? 'success' : 'danger'">
            {{ row.status === STATUS_NORMAL ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="155" sortable="custom">
        <template #default="{ row }">{{ row.createTime || '-' }}</template>
      </el-table-column>
      <el-table-column prop="updateTime" label="更新时间" width="155" sortable="custom">
        <template #default="{ row }">{{ row.updateTime || '-' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="removeUser(row)">删除</el-button>
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
      @current-change="loadUsers"
      @size-change="handleSizeChange"
    />

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="480px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="账号" prop="username">
          <el-input v-model="form.username" :disabled="isEditing" />
        </el-form-item>
        <el-form-item v-if="!isEditing" label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-select v-model="form.gender" class="page__select">
            <el-option label="未知" :value="GENDER_UNKNOWN" />
            <el-option label="男" :value="1" />
            <el-option label="女" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" class="page__select">
            <el-option label="普通用户" :value="0" />
            <el-option label="管理员" :value="ROLE_ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="isEditing" label="状态" prop="status">
          <el-select v-model="form.status" class="page__select">
            <el-option label="正常" :value="STATUS_NORMAL" />
            <el-option label="禁用" :value="1" />
          </el-select>
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
import {
  createUser,
  deleteUser,
  pageUsers,
  updateUser,
  type UserCreatePayload,
  type UserQuery,
  type UserUpdatePayload,
  type UserVO,
} from '@/api/backend/user'

// 角色：管理员
const ROLE_ADMIN = 1

// 账号状态：正常
const STATUS_NORMAL = 0

// 性别：未知
const GENDER_UNKNOWN = 0

// 可选每页条数
const PAGE_SIZES = [10, 20, 50, 100]

const loading = ref(false)
const submitting = ref(false)
const rows = ref<UserVO[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const filters = reactive<{
  username: string
  nickname: string
  status?: number
  role?: number
}>({
  username: '',
  nickname: '',
  status: undefined,
  role: undefined,
})

const dialogVisible = ref(false)
const editingId = ref<string | null>(null)
const formRef = ref<FormInstance>()

const isEditing = computed(() => editingId.value !== null)
const dialogTitle = computed(() => (isEditing.value ? '编辑用户' : '新增用户'))

// 表单字段，新增与编辑共用，提交时按模式裁剪
const form = reactive<{
  username: string
  password: string
  nickname: string
  email: string
  phone: string
  gender: number
  role: number
  status: number
}>({
  username: '',
  password: '',
  nickname: '',
  email: '',
  phone: '',
  gender: GENDER_UNKNOWN,
  role: 0,
  status: STATUS_NORMAL,
})

// 校验规则，密码只在新增时必填
const rules = reactive<FormRules>({
  username: [{ required: true, message: '请输入登录账号', trigger: 'blur' }],
  password: [
    {
      validator: (_rule, value: string, callback) => {
        if (isEditing.value) return callback()
        if (!value) return callback(new Error('请输入登录密码'))
        if (value.length < 6 || value.length > 32) {
          return callback(new Error('密码长度须为 6 到 32 位'))
        }
        callback()
      },
      trigger: 'blur',
    },
  ],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
  phone: [{ pattern: /^$|^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }],
})

// 当前排序字段与方向，空表示默认排序
const sortField = ref('')
const sortOrder = ref<'asc' | 'desc' | ''>('')

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
  loadUsers()
}

// 加载当前页用户，返回本次查询的命中总数
async function loadUsers(): Promise<number> {
  loading.value = true
  try {
    const query: UserQuery = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      username: filters.username || undefined,
      nickname: filters.nickname || undefined,
      status: filters.status,
      role: filters.role,
      sortField: sortField.value || undefined,
      sortOrder: sortOrder.value || undefined,
    }
    const page = await pageUsers(query)
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
  () => [filters.username, filters.nickname, filters.status, filters.role],
  () => {
    if (filterTimer) clearTimeout(filterTimer)
    filterTimer = setTimeout(() => {
      pageNum.value = 1
      loadUsers()
    }, 300)
  },
)

// 条件变更后回到第一页重新查询，并提示命中情况
async function search() {
  pageNum.value = 1
  const hit = await loadUsers()
  if (hit === 0) {
    ElMessage.warning('没有符合条件的用户')
    return
  }
  ElMessage.success(`查询完成，共 ${hit} 条用户`)
}

// 清空筛选条件并重新查询，只提示已重置
async function resetFilters() {
  // 取消待执行的自动查询，避免与本次重置重复请求
  if (filterTimer) {
    clearTimeout(filterTimer)
    filterTimer = null
  }
  filters.username = ''
  filters.nickname = ''
  filters.status = undefined
  filters.role = undefined
  pageNum.value = 1
  await loadUsers()
  ElMessage.success('筛选条件已重置')
}

// 切换每页条数后回到第一页
function handleSizeChange() {
  pageNum.value = 1
  loadUsers()
}

// 恢复表单为默认值
function resetForm() {
  form.username = ''
  form.password = ''
  form.nickname = ''
  form.email = ''
  form.phone = ''
  form.gender = GENDER_UNKNOWN
  form.role = 0
  form.status = STATUS_NORMAL
  formRef.value?.clearValidate()
}

// 打开新增弹窗
function openCreate() {
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

// 打开编辑弹窗，回填当前行数据
function openEdit(row: UserVO) {
  editingId.value = row.id
  resetForm()
  form.username = row.username
  form.nickname = row.nickname ?? ''
  form.email = row.email ?? ''
  form.phone = row.phone ?? ''
  form.gender = row.gender
  form.role = row.role
  form.status = row.status
  dialogVisible.value = true
}

// 提交新增或修改
async function submit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (editingId.value === null) {
      const payload: UserCreatePayload = {
        username: form.username,
        password: form.password,
        nickname: form.nickname || undefined,
        email: form.email || undefined,
        phone: form.phone || undefined,
        gender: form.gender,
        role: form.role,
      }
      await createUser(payload)
      ElMessage.success('新增成功')
    } else {
      const payload: UserUpdatePayload = {
        id: editingId.value,
        nickname: form.nickname || undefined,
        email: form.email || undefined,
        phone: form.phone || undefined,
        gender: form.gender,
        role: form.role,
        status: form.status,
      }
      await updateUser(payload)
      ElMessage.success('修改成功')
    }
    dialogVisible.value = false
    loadUsers()
  } finally {
    submitting.value = false
  }
}

// 删除用户，二次确认后执行
async function removeUser(row: UserVO) {
  try {
    await ElMessageBox.confirm(`确定删除用户「${row.username}」吗？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
  } catch {
    // 用户取消删除，不做处理。
    return
  }
  await deleteUser(row.id)
  ElMessage.success('删除成功')
  // 删掉当前页最后一条时回退一页，避免停在空页
  if (rows.value.length === 1 && pageNum.value > 1) pageNum.value -= 1
  loadUsers()
}

onMounted(loadUsers)

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

.page__select {
  width: 140px;
}

.page__pagination {
  justify-content: flex-end;
}
</style>
