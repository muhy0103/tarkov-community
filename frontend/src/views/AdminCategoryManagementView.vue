<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { EditPen, Files, Refresh, Search, SwitchButton } from '@element-plus/icons-vue'
import { fetchAdminCategories, updateAdminCategory } from '../api/adminApi'
import { useUserStore } from '../stores/userStore'

const userStore = useUserStore()

const loading = ref(false)
const saving = ref(false)
const actionCategoryId = ref(null)
const errorMessage = ref('')
const editDialogVisible = ref(false)
const categoryFormRef = ref(null)
const editingRow = ref(null)
const filters = ref({
  status: '',
  keyword: '',
})
const categoryForm = ref({
  name: '',
  description: '',
  icon: '',
  sortOrder: 0,
  status: 'ENABLED',
})
const categoriesPage = ref({
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

const categoryRules = {
  name: [
    { required: true, message: '请填写分区名称', trigger: 'blur' },
    { max: 50, message: '分区名称不能超过 50 个字符', trigger: 'blur' },
  ],
  description: [
    { max: 500, message: '分区说明不能超过 500 个字符', trigger: 'blur' },
  ],
  icon: [
    { max: 100, message: '分区图标不能超过 100 个字符', trigger: 'blur' },
  ],
  sortOrder: [
    { required: true, message: '请填写排序值', trigger: 'blur' },
  ],
  status: [
    { required: true, message: '请选择分区状态', trigger: 'change' },
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

function toPayload(row, overrides = {}) {
  return {
    name: row.name,
    description: row.description || '',
    icon: row.icon || '',
    sortOrder: row.sortOrder ?? 0,
    status: row.status,
    ...overrides,
  }
}

async function loadCategories(page = categoriesPage.value.page) {
  loading.value = true
  errorMessage.value = ''
  categoriesPage.value.page = page

  try {
    categoriesPage.value = await fetchAdminCategories({
      page,
      size: categoriesPage.value.size,
      status: filters.value.status || undefined,
      keyword: filters.value.keyword.trim() || undefined,
    })
  } catch (error) {
    errorMessage.value = resolveError(error, '分区管理列表暂时无法加载')
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  filters.value = {
    status: '',
    keyword: '',
  }
  loadCategories(1)
}

function openEditDialog(row) {
  editingRow.value = row
  categoryForm.value = toPayload(row)
  editDialogVisible.value = true
}

async function submitCategory() {
  const valid = await categoryFormRef.value?.validate().catch(() => false)

  if (!valid || !editingRow.value) {
    return
  }

  saving.value = true
  try {
    const updated = await updateAdminCategory(editingRow.value.id, {
      ...categoryForm.value,
      name: categoryForm.value.name.trim(),
      description: categoryForm.value.description.trim(),
      icon: categoryForm.value.icon.trim(),
    })
    Object.assign(editingRow.value, updated)
    ElMessage.success('分区信息已更新')
    editDialogVisible.value = false
  } catch (error) {
    ElMessage.error(resolveError(error, '分区保存失败'))
  } finally {
    saving.value = false
  }
}

async function toggleCategory(row) {
  actionCategoryId.value = row.id
  const nextStatus = row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED'

  try {
    const updated = await updateAdminCategory(row.id, toPayload(row, { status: nextStatus }))
    Object.assign(row, updated)
    ElMessage.success(nextStatus === 'ENABLED' ? '分区已启用' : '分区已停用')
    if (filters.value.status && filters.value.status !== nextStatus) {
      loadCategories(1)
    }
  } catch (error) {
    ElMessage.error(resolveError(error, '分区状态更新失败'))
  } finally {
    actionCategoryId.value = null
  }
}

onMounted(() => loadCategories(1))
</script>

<template>
  <div class="admin-review-view">
    <section class="admin-hero">
      <div>
        <h2>分区管理</h2>
        <p>维护社区内容分区的名称、展示顺序和启用状态，让发帖和筛选结构保持清晰。</p>
      </div>
      <el-button :icon="Refresh" :loading="loading" @click="loadCategories()">
        刷新分区
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
      <div class="admin-comment-filters">
        <el-input
          v-model="filters.keyword"
          placeholder="搜索分区名称、编码或说明"
          clearable
          @keyup.enter="loadCategories(1)"
        />
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
        <el-button type="primary" :icon="Search" @click="loadCategories(1)">
          查询
        </el-button>
        <el-button :icon="Refresh" @click="resetFilters">
          重置
        </el-button>
      </div>
    </section>

    <section class="admin-table-card">
      <el-table v-loading="loading" :data="categoriesPage.records" row-key="id">
        <el-table-column label="分区" min-width="280">
          <template #default="{ row }">
            <div class="admin-category-cell">
              <Files />
              <div>
                <strong>{{ row.name }}</strong>
                <span>{{ row.code }} · {{ row.description || '暂无说明' }}</span>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="图标" width="120">
          <template #default="{ row }">
            <span class="admin-table-muted">{{ row.icon || '未设置' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="排序" width="100">
          <template #default="{ row }">
            <span class="admin-table-muted">{{ row.sortOrder }}</span>
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
                :loading="actionCategoryId === row.id"
                @click="toggleCategory(row)"
              >
                {{ row.status === 'ENABLED' ? '停用' : '启用' }}
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="!loading && categoriesPage.records.length === 0" class="admin-table-empty">
        当前没有符合条件的分区
      </div>

      <el-pagination
        v-if="categoriesPage.pages > 1"
        class="board-pagination"
        background
        layout="prev, pager, next"
        :current-page="categoriesPage.page"
        :page-size="categoriesPage.size"
        :total="categoriesPage.total"
        @current-change="loadCategories"
      />
    </section>

    <el-dialog
      v-model="editDialogVisible"
      title="编辑分区"
      width="520px"
      class="profile-edit-dialog"
    >
      <el-form
        ref="categoryFormRef"
        :model="categoryForm"
        :rules="categoryRules"
        label-position="top"
      >
        <el-form-item label="分区名称" prop="name">
          <el-input v-model="categoryForm.name" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="分区说明" prop="description">
          <el-input
            v-model="categoryForm.description"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="图标标识" prop="icon">
          <el-input v-model="categoryForm.icon" maxlength="100" />
        </el-form-item>
        <el-form-item label="排序值" prop="sortOrder">
          <el-input-number
            v-model="categoryForm.sortOrder"
            :min="0"
            :step="1"
            controls-position="right"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="categoryForm.status">
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
          <el-button type="primary" :loading="saving" @click="submitCategory">
            保存分区
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>
