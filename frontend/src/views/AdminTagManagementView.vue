<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { EditPen, PriceTag, Refresh, Search, SwitchButton } from '@element-plus/icons-vue'
import { fetchAdminTags, updateAdminTag } from '../api/adminApi'
import { useUserStore } from '../stores/userStore'

const userStore = useUserStore()

const loading = ref(false)
const saving = ref(false)
const actionTagId = ref(null)
const errorMessage = ref('')
const editDialogVisible = ref(false)
const tagFormRef = ref(null)
const editingRow = ref(null)
const filters = ref({
  type: '',
  status: '',
  keyword: '',
})
const tagForm = ref({
  name: '',
  type: '',
  color: '',
  status: 'ENABLED',
})
const tagsPage = ref({
  page: 1,
  size: 10,
  total: 0,
  pages: 0,
  records: [],
})

const statusOptions = [
  { label: '启用', value: 'ENABLED' },
  { label: '停用', value: 'DISABLED' },
]

const typeOptions = [
  { label: '系统标签', value: 'SYSTEM' },
  { label: '风险标签', value: 'RISK' },
  { label: '玩法风格', value: 'PLAY_STYLE' },
  { label: '地图主题', value: 'MAP' },
  { label: '任务主题', value: 'QUEST' },
  { label: '配装主题', value: 'GEAR' },
  { label: '市场主题', value: 'MARKET' },
  { label: '组队主题', value: 'TEAMUP' },
]

const colorPresets = [
  '#22C55E',
  '#2563EB',
  '#0EA5A4',
  '#F59E0B',
  '#8B5CF6',
  '#EF4444',
  '#10B981',
  '#6B7280',
]

const tagRules = {
  name: [
    { required: true, message: '请填写标签名称', trigger: 'blur' },
    { max: 50, message: '标签名称不能超过 50 个字符', trigger: 'blur' },
  ],
  type: [
    { required: true, message: '请填写标签类型', trigger: 'blur' },
    { max: 30, message: '标签类型不能超过 30 个字符', trigger: 'blur' },
  ],
  color: [
    { pattern: /^$|#[0-9A-Fa-f]{6}$/, message: '颜色需要使用 #RRGGBB 格式', trigger: 'blur' },
  ],
  status: [
    { required: true, message: '请选择标签状态', trigger: 'change' },
  ],
}

function resolveError(error, fallback) {
  return error?.response?.data?.message || error?.message || fallback
}

function statusLabel(status) {
  return statusOptions.find((item) => item.value === status)?.label || status || '未知'
}

function statusType(status) {
  return status === 'ENABLED' ? 'success' : 'warning'
}

function typeLabel(type) {
  const option = typeOptions.find((item) => item.value === type)
  return option ? `${option.label} / ${option.value}` : type || '未设置'
}

function toPayload(row, overrides = {}) {
  return {
    name: row.name,
    type: row.type,
    color: row.color || '',
    status: row.status,
    ...overrides,
  }
}

async function loadTags(page = tagsPage.value.page) {
  loading.value = true
  errorMessage.value = ''
  tagsPage.value.page = page

  try {
    tagsPage.value = await fetchAdminTags({
      page,
      size: tagsPage.value.size,
      type: filters.value.type || undefined,
      status: filters.value.status || undefined,
      keyword: filters.value.keyword.trim() || undefined,
    })
  } catch (error) {
    errorMessage.value = resolveError(error, '标签列表暂时无法加载')
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  filters.value = {
    type: '',
    status: '',
    keyword: '',
  }
  loadTags(1)
}

function openEditDialog(row) {
  editingRow.value = row
  tagForm.value = toPayload(row)
  editDialogVisible.value = true
}

async function submitTag() {
  const valid = await tagFormRef.value?.validate().catch(() => false)

  if (!valid || !editingRow.value) {
    return
  }

  saving.value = true
  try {
    const updated = await updateAdminTag(editingRow.value.id, {
      ...tagForm.value,
      name: tagForm.value.name.trim(),
      type: tagForm.value.type.trim(),
      color: tagForm.value.color.trim(),
    })
    Object.assign(editingRow.value, updated)
    ElMessage.success('标签信息已更新')
    editDialogVisible.value = false
  } catch (error) {
    ElMessage.error(resolveError(error, '标签保存失败'))
  } finally {
    saving.value = false
  }
}

async function toggleTag(row) {
  actionTagId.value = row.id
  const nextStatus = row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED'

  try {
    const updated = await updateAdminTag(row.id, toPayload(row, { status: nextStatus }))
    Object.assign(row, updated)
    ElMessage.success(nextStatus === 'ENABLED' ? '标签已启用' : '标签已停用')
    if (filters.value.status && filters.value.status !== nextStatus) {
      loadTags(1)
    }
  } catch (error) {
    ElMessage.error(resolveError(error, '标签状态更新失败'))
  } finally {
    actionTagId.value = null
  }
}

onMounted(() => loadTags(1))
</script>

<template>
  <div class="admin-review-view">
    <section class="admin-hero">
      <div>
        <h2>标签管理</h2>
        <p>维护社区标签的名称、类型、颜色和状态，让任务攻略、市场行情、配装和风险提示有统一标识。</p>
      </div>
      <el-button :icon="Refresh" :loading="loading" @click="loadTags()">
        刷新标签
      </el-button>
    </section>

    <el-alert
      v-if="!userStore.isAdmin"
      title="当前账号不是管理员，页面仅作为开发阶段预览。"
      type="info"
      show-icon
      class="home-alert"
    />

    <el-alert
      v-if="errorMessage"
      :title="errorMessage"
      type="warning"
      show-icon
      class="home-alert"
    />

    <section class="board-toolbar">
      <div class="admin-review-filters">
        <el-input
          v-model="filters.keyword"
          placeholder="搜索标签名称、类型或颜色"
          clearable
          @keyup.enter="loadTags(1)"
        />
        <el-select
          v-model="filters.type"
          placeholder="全部类型"
          clearable
          filterable
          allow-create
        >
          <el-option
            v-for="option in typeOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
        <el-select v-model="filters.status" placeholder="全部状态" clearable>
          <el-option
            v-for="option in statusOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
      </div>

      <div class="board-actions">
        <el-button type="primary" :icon="Search" @click="loadTags(1)">
          查询
        </el-button>
        <el-button :icon="Refresh" @click="resetFilters">
          重置
        </el-button>
      </div>
    </section>

    <section class="admin-table-card">
      <el-table v-loading="loading" :data="tagsPage.records" row-key="id">
        <el-table-column label="标签" min-width="280">
          <template #default="{ row }">
            <div class="admin-tag-cell">
              <PriceTag />
              <div>
                <strong>{{ row.name }}</strong>
                <span>{{ typeLabel(row.type) }}</span>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="颜色" width="150">
          <template #default="{ row }">
            <div class="admin-color-chip">
              <span class="admin-color-swatch" :style="{ backgroundColor: row.color || '#dbe7e6' }" />
              <span class="admin-table-muted">{{ row.color || '未设置' }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" effect="plain">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <div class="admin-row-actions">
              <el-button size="small" :icon="EditPen" @click="openEditDialog(row)">
                编辑
              </el-button>
              <el-button
                size="small"
                :icon="SwitchButton"
                :loading="actionTagId === row.id"
                @click="toggleTag(row)"
              >
                {{ row.status === 'ENABLED' ? '停用' : '启用' }}
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="!loading && tagsPage.records.length === 0" class="admin-table-empty">
        当前没有符合条件的标签
      </div>

      <el-pagination
        v-if="tagsPage.pages > 1"
        class="board-pagination"
        background
        layout="prev, pager, next"
        :current-page="tagsPage.page"
        :page-size="tagsPage.size"
        :total="tagsPage.total"
        @current-change="loadTags"
      />
    </section>

    <el-dialog
      v-model="editDialogVisible"
      title="编辑标签"
      width="560px"
      class="profile-edit-dialog"
    >
      <el-form
        ref="tagFormRef"
        :model="tagForm"
        :rules="tagRules"
        label-position="top"
      >
        <el-form-item label="标签名称" prop="name">
          <el-input v-model="tagForm.name" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="标签类型" prop="type">
          <el-select
            v-model="tagForm.type"
            filterable
            allow-create
            placeholder="选择或输入标签类型"
          >
            <el-option
              v-for="option in typeOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="颜色" prop="color">
          <div class="admin-color-editor">
            <el-color-picker v-model="tagForm.color" :predefine="colorPresets" />
            <el-input v-model="tagForm.color" maxlength="7" placeholder="#2563EB" />
          </div>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="tagForm.status">
            <el-option
              v-for="option in statusOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="profile-dialog-footer">
          <el-button @click="editDialogVisible = false">
            取消
          </el-button>
          <el-button type="primary" :loading="saving" @click="submitTag">
            保存标签
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>
