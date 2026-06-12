<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Avatar, EditPen, Refresh, Search, SwitchButton } from '@element-plus/icons-vue'
import { fetchAdminTraders, updateAdminTrader } from '../api/adminApi'
import { useUserStore } from '../stores/userStore'

const userStore = useUserStore()

const loading = ref(false)
const saving = ref(false)
const actionTraderId = ref(null)
const errorMessage = ref('')
const editDialogVisible = ref(false)
const traderFormRef = ref(null)
const editingRow = ref(null)
const filters = ref({
  status: '',
  keyword: '',
})
const traderForm = ref({
  nameEn: '',
  description: '',
  unlockCondition: '',
  avatar: '',
  status: 'ENABLED',
})
const tradersPage = ref({
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

const traderRules = {
  nameEn: [
    { required: true, message: '请填写商人英文名', trigger: 'blur' },
    { max: 80, message: '商人英文名不能超过 80 个字符', trigger: 'blur' },
  ],
  description: [
    { max: 500, message: '商人说明不能超过 500 个字符', trigger: 'blur' },
  ],
  unlockCondition: [
    { max: 255, message: '解锁条件不能超过 255 个字符', trigger: 'blur' },
  ],
  avatar: [
    { max: 500, message: '头像地址不能超过 500 个字符', trigger: 'blur' },
  ],
  status: [
    { required: true, message: '请选择商人状态', trigger: 'change' },
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
    nameEn: row.nameEn,
    description: row.description || '',
    unlockCondition: row.unlockCondition || '',
    avatar: row.avatar || '',
    status: row.status,
    ...overrides,
  }
}

async function loadTraders(page = tradersPage.value.page) {
  loading.value = true
  errorMessage.value = ''
  tradersPage.value.page = page

  try {
    tradersPage.value = await fetchAdminTraders({
      page,
      size: tradersPage.value.size,
      status: filters.value.status || undefined,
      keyword: filters.value.keyword.trim() || undefined,
    })
  } catch (error) {
    errorMessage.value = resolveError(error, '商人资料列表暂时无法加载')
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  filters.value = {
    status: '',
    keyword: '',
  }
  loadTraders(1)
}

function openEditDialog(row) {
  editingRow.value = row
  traderForm.value = toPayload(row)
  editDialogVisible.value = true
}

async function submitTrader() {
  const valid = await traderFormRef.value?.validate().catch(() => false)

  if (!valid || !editingRow.value) {
    return
  }

  saving.value = true
  try {
    const updated = await updateAdminTrader(editingRow.value.id, {
      ...traderForm.value,
      nameEn: traderForm.value.nameEn.trim(),
      description: traderForm.value.description.trim(),
      unlockCondition: traderForm.value.unlockCondition.trim(),
      avatar: traderForm.value.avatar.trim(),
    })
    Object.assign(editingRow.value, updated)
    ElMessage.success('商人资料已更新')
    editDialogVisible.value = false
  } catch (error) {
    ElMessage.error(resolveError(error, '商人资料保存失败'))
  } finally {
    saving.value = false
  }
}

async function toggleTrader(row) {
  actionTraderId.value = row.id
  const nextStatus = row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED'

  try {
    const updated = await updateAdminTrader(row.id, toPayload(row, { status: nextStatus }))
    Object.assign(row, updated)
    ElMessage.success(nextStatus === 'ENABLED' ? '商人已启用' : '商人已停用')
    if (filters.value.status && filters.value.status !== nextStatus) {
      loadTraders(1)
    }
  } catch (error) {
    ElMessage.error(resolveError(error, '商人状态更新失败'))
  } finally {
    actionTraderId.value = null
  }
}

onMounted(() => loadTraders(1))
</script>

<template>
  <div class="admin-review-view">
    <section class="admin-hero">
      <div>
        <h2>商人管理</h2>
        <p>维护商人资料、解锁条件和头像信息，为任务档案和装备讨论提供统一来源。</p>
      </div>
      <el-button :icon="Refresh" :loading="loading" @click="loadTraders()">
        刷新商人
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
          placeholder="搜索商人名称、说明或解锁条件"
          clearable
          @keyup.enter="loadTraders(1)"
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
        <el-button type="primary" :icon="Search" @click="loadTraders(1)">
          查询
        </el-button>
        <el-button :icon="Refresh" @click="resetFilters">
          重置
        </el-button>
      </div>
    </section>

    <section class="admin-table-card">
      <el-table v-loading="loading" :data="tradersPage.records" row-key="id">
        <el-table-column label="商人" min-width="320">
          <template #default="{ row }">
            <div class="admin-trader-cell">
              <img
                v-if="row.avatar"
                class="admin-media-thumb"
                :src="row.avatar"
                :alt="row.nameEn"
                loading="lazy"
              />
              <Avatar v-else />
              <div>
                <strong>{{ row.nameEn }}</strong>
                <span>{{ row.description || '暂无说明' }}</span>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="解锁条件" width="180">
          <template #default="{ row }">
            <span class="admin-table-muted">{{ row.unlockCondition || '未设置' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="头像" width="180">
          <template #default="{ row }">
            <span class="admin-media-state">
              {{ row.avatar ? '已设置' : '未设置' }}
            </span>
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
                :loading="actionTraderId === row.id"
                @click="toggleTrader(row)"
              >
                {{ row.status === 'ENABLED' ? '停用' : '启用' }}
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="!loading && tradersPage.records.length === 0" class="admin-table-empty">
        当前没有符合条件的商人
      </div>

      <el-pagination
        v-if="tradersPage.pages > 1"
        class="board-pagination"
        background
        layout="prev, pager, next"
        :current-page="tradersPage.page"
        :page-size="tradersPage.size"
        :total="tradersPage.total"
        @current-change="loadTraders"
      />
    </section>

    <el-dialog
      v-model="editDialogVisible"
      title="编辑商人"
      width="560px"
      class="profile-edit-dialog"
    >
      <el-form
        ref="traderFormRef"
        :model="traderForm"
        :rules="traderRules"
        label-position="top"
      >
        <el-form-item label="英文名" prop="nameEn">
          <el-input v-model="traderForm.nameEn" maxlength="80" show-word-limit />
        </el-form-item>
        <el-form-item label="解锁条件" prop="unlockCondition">
          <el-input v-model="traderForm.unlockCondition" maxlength="255" show-word-limit />
        </el-form-item>
        <el-form-item label="头像地址" prop="avatar">
          <el-input v-model="traderForm.avatar" maxlength="500" show-word-limit />
        </el-form-item>
        <div v-if="traderForm.avatar" class="admin-media-preview">
          <img :src="traderForm.avatar" alt="头像预览" />
        </div>
        <el-form-item label="商人说明" prop="description">
          <el-input
            v-model="traderForm.description"
            type="textarea"
            :rows="4"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="traderForm.status">
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
          <el-button type="primary" :loading="saving" @click="submitTrader">
            保存商人
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>
