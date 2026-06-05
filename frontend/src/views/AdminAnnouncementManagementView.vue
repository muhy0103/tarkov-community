<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Bell, EditPen, Plus, Refresh, Search, SwitchButton } from '@element-plus/icons-vue'
import {
  createAdminAnnouncement,
  fetchAdminAnnouncements,
  updateAdminAnnouncement,
} from '../api/adminApi'
import { useUserStore } from '../stores/userStore'

const userStore = useUserStore()

const loading = ref(false)
const saving = ref(false)
const actionAnnouncementId = ref(null)
const errorMessage = ref('')
const editDialogVisible = ref(false)
const announcementFormRef = ref(null)
const editingRow = ref(null)
const filters = ref({
  status: '',
  keyword: '',
})
const announcementForm = ref(emptyForm())
const announcementsPage = ref({
  page: 1,
  size: 10,
  total: 0,
  pages: 0,
  records: [],
})

const statusOptions = [
  { label: '已发布', value: 'PUBLISHED' },
  { label: '草稿', value: 'DRAFT' },
  { label: '已归档', value: 'ARCHIVED' },
]

const announcementRules = {
  title: [
    { required: true, message: '请填写公告标题', trigger: 'blur' },
    { max: 120, message: '公告标题不能超过 120 个字符', trigger: 'blur' },
  ],
  content: [
    { required: true, message: '请填写公告内容', trigger: 'blur' },
    { max: 4000, message: '公告内容不能超过 4000 个字符', trigger: 'blur' },
  ],
  status: [
    { required: true, message: '请选择公告状态', trigger: 'change' },
  ],
}

const dialogTitle = computed(() => (editingRow.value ? '编辑公告' : '新建公告'))
const submitText = computed(() => (editingRow.value ? '保存公告' : '发布公告'))

function emptyForm() {
  return {
    title: '',
    content: '',
    status: 'PUBLISHED',
  }
}

function resolveError(error, fallback) {
  return error?.response?.data?.message || error?.message || fallback
}

function statusLabel(status) {
  return statusOptions.find((item) => item.value === status)?.label || status || '未知'
}

function statusType(status) {
  if (status === 'PUBLISHED') {
    return 'success'
  }

  if (status === 'DRAFT') {
    return 'info'
  }

  return 'warning'
}

function formatDate(value) {
  if (!value) {
    return '未记录'
  }

  return String(value).replace('T', ' ').slice(0, 16)
}

function toPayload(row, overrides = {}) {
  return {
    title: row.title,
    content: row.content || '',
    status: row.status,
    ...overrides,
  }
}

async function loadAnnouncements(page = announcementsPage.value.page) {
  loading.value = true
  errorMessage.value = ''
  announcementsPage.value.page = page

  try {
    announcementsPage.value = await fetchAdminAnnouncements({
      page,
      size: announcementsPage.value.size,
      status: filters.value.status || undefined,
      keyword: filters.value.keyword.trim() || undefined,
    })
  } catch (error) {
    errorMessage.value = resolveError(error, '公告列表暂时无法加载')
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  filters.value = {
    status: '',
    keyword: '',
  }
  loadAnnouncements(1)
}

function openCreateDialog() {
  editingRow.value = null
  announcementForm.value = emptyForm()
  editDialogVisible.value = true
}

function openEditDialog(row) {
  editingRow.value = row
  announcementForm.value = toPayload(row)
  editDialogVisible.value = true
}

async function submitAnnouncement() {
  const valid = await announcementFormRef.value?.validate().catch(() => false)

  if (!valid) {
    return
  }

  saving.value = true
  const payload = {
    ...announcementForm.value,
    title: announcementForm.value.title.trim(),
    content: announcementForm.value.content.trim(),
  }

  try {
    if (editingRow.value) {
      const updated = await updateAdminAnnouncement(editingRow.value.id, payload)
      Object.assign(editingRow.value, updated)
      ElMessage.success('公告信息已更新')
    } else {
      await createAdminAnnouncement(payload)
      ElMessage.success(payload.status === 'PUBLISHED' ? '公告已发布' : '公告已保存')
      loadAnnouncements(1)
    }

    editDialogVisible.value = false
  } catch (error) {
    ElMessage.error(resolveError(error, '公告保存失败'))
  } finally {
    saving.value = false
  }
}

async function toggleAnnouncementStatus(row) {
  actionAnnouncementId.value = row.id
  const nextStatus = row.status === 'PUBLISHED' ? 'ARCHIVED' : 'PUBLISHED'

  try {
    const updated = await updateAdminAnnouncement(row.id, toPayload(row, { status: nextStatus }))
    Object.assign(row, updated)
    ElMessage.success(nextStatus === 'PUBLISHED' ? '公告已发布' : '公告已归档')
    if (filters.value.status && filters.value.status !== nextStatus) {
      loadAnnouncements(1)
    }
  } catch (error) {
    ElMessage.error(resolveError(error, '公告状态更新失败'))
  } finally {
    actionAnnouncementId.value = null
  }
}

onMounted(() => loadAnnouncements(1))
</script>

<template>
  <div class="admin-review-view">
    <section class="admin-hero">
      <div>
        <h2>公告管理</h2>
        <p>维护社区维护通知、活动提醒、版本情报和规则说明，让玩家能快速看到论坛当前重点。</p>
      </div>
      <div class="board-actions">
        <el-button :icon="Refresh" :loading="loading" @click="loadAnnouncements()">
          刷新公告
        </el-button>
        <el-button type="primary" :icon="Plus" @click="openCreateDialog">
          新建公告
        </el-button>
      </div>
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
          placeholder="搜索公告标题或正文"
          clearable
          @keyup.enter="loadAnnouncements(1)"
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
        <el-button type="primary" :icon="Search" @click="loadAnnouncements(1)">
          查询
        </el-button>
        <el-button :icon="Refresh" @click="resetFilters">
          重置
        </el-button>
      </div>
    </section>

    <section class="admin-table-card">
      <el-table v-loading="loading" :data="announcementsPage.records" row-key="id">
        <el-table-column label="公告" min-width="360">
          <template #default="{ row }">
            <div class="admin-announcement-cell">
              <Bell />
              <div>
                <strong>{{ row.title }}</strong>
                <span>{{ row.content || '暂无公告正文' }}</span>
              </div>
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

        <el-table-column label="创建人" width="140">
          <template #default="{ row }">
            <span class="admin-table-muted">{{ row.creatorNickname || '未知管理员' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="更新时间" width="170">
          <template #default="{ row }">
            <span class="admin-table-muted">{{ formatDate(row.updatedAt || row.createdAt) }}</span>
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
                :loading="actionAnnouncementId === row.id"
                @click="toggleAnnouncementStatus(row)"
              >
                {{ row.status === 'PUBLISHED' ? '归档' : '发布' }}
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="!loading && announcementsPage.records.length === 0" class="admin-table-empty">
        当前没有符合条件的公告
      </div>

      <el-pagination
        v-if="announcementsPage.pages > 1"
        class="board-pagination"
        background
        layout="prev, pager, next"
        :current-page="announcementsPage.page"
        :page-size="announcementsPage.size"
        :total="announcementsPage.total"
        @current-change="loadAnnouncements"
      />
    </section>

    <el-dialog
      v-model="editDialogVisible"
      :title="dialogTitle"
      width="min(620px, calc(100vw - 32px))"
      class="profile-edit-dialog"
    >
      <el-form
        ref="announcementFormRef"
        :model="announcementForm"
        :rules="announcementRules"
        label-position="top"
      >
        <el-form-item label="公告标题" prop="title">
          <el-input v-model="announcementForm.title" maxlength="120" show-word-limit />
        </el-form-item>
        <el-form-item label="公告正文" prop="content">
          <el-input
            v-model="announcementForm.content"
            type="textarea"
            :rows="7"
            maxlength="4000"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="公告状态" prop="status">
          <el-select v-model="announcementForm.status">
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
          <el-button type="primary" :loading="saving" @click="submitAnnouncement">
            {{ submitText }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>
