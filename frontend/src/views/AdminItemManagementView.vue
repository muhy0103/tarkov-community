<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Document, EditPen, Refresh, Search, SwitchButton } from '@element-plus/icons-vue'
import { fetchAdminItems, updateAdminItem } from '../api/adminApi'
import { useUserStore } from '../stores/userStore'

const userStore = useUserStore()

const loading = ref(false)
const saving = ref(false)
const actionItemId = ref(null)
const errorMessage = ref('')
const editDialogVisible = ref(false)
const itemFormRef = ref(null)
const editingRow = ref(null)
const filters = ref({
  status: '',
  keyword: '',
  itemType: '',
  questNeeded: null,
  hideoutNeeded: null,
  keepSuggestion: null,
})
const itemForm = ref({
  nameEn: '',
  nameZh: '',
  itemType: '',
  rarity: '',
  gridSize: '',
  questNeeded: false,
  hideoutNeeded: false,
  keepSuggestion: false,
  description: '',
  status: 'ENABLED',
})
const itemsPage = ref({
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
const booleanOptions = [
  { label: '是', value: true },
  { label: '否', value: false },
]

const itemRules = {
  nameEn: [
    { required: true, message: '请填写物品英文名', trigger: 'blur' },
    { max: 120, message: '物品英文名不能超过 120 个字符', trigger: 'blur' },
  ],
  nameZh: [
    { max: 120, message: '物品中文名不能超过 120 个字符', trigger: 'blur' },
  ],
  itemType: [
    { max: 60, message: '物品类型不能超过 60 个字符', trigger: 'blur' },
  ],
  rarity: [
    { max: 40, message: '稀有度不能超过 40 个字符', trigger: 'blur' },
  ],
  gridSize: [
    { max: 20, message: '格子尺寸不能超过 20 个字符', trigger: 'blur' },
  ],
  description: [
    { max: 500, message: '物品说明不能超过 500 个字符', trigger: 'blur' },
  ],
  status: [
    { required: true, message: '请选择物品状态', trigger: 'change' },
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

function booleanLabel(value) {
  return value ? '是' : '否'
}

function toPayload(row, overrides = {}) {
  return {
    nameEn: row.nameEn,
    nameZh: row.nameZh || '',
    itemType: row.itemType || '',
    rarity: row.rarity || '',
    gridSize: row.gridSize || '',
    questNeeded: Boolean(row.questNeeded),
    hideoutNeeded: Boolean(row.hideoutNeeded),
    keepSuggestion: Boolean(row.keepSuggestion),
    description: row.description || '',
    status: row.status,
    ...overrides,
  }
}

function filterFlagValue(value) {
  return value === null || value === '' ? undefined : value
}

async function loadItems(page = itemsPage.value.page) {
  loading.value = true
  errorMessage.value = ''
  itemsPage.value.page = page

  try {
    itemsPage.value = await fetchAdminItems({
      page,
      size: itemsPage.value.size,
      itemType: filters.value.itemType.trim() || undefined,
      status: filters.value.status || undefined,
      questNeeded: filterFlagValue(filters.value.questNeeded),
      hideoutNeeded: filterFlagValue(filters.value.hideoutNeeded),
      keepSuggestion: filterFlagValue(filters.value.keepSuggestion),
      keyword: filters.value.keyword.trim() || undefined,
    })
  } catch (error) {
    errorMessage.value = resolveError(error, '物品资料列表暂时无法加载')
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  filters.value = {
    status: '',
    keyword: '',
    itemType: '',
    questNeeded: null,
    hideoutNeeded: null,
    keepSuggestion: null,
  }
  loadItems(1)
}

function openEditDialog(row) {
  editingRow.value = row
  itemForm.value = toPayload(row)
  editDialogVisible.value = true
}

async function submitItem() {
  const valid = await itemFormRef.value?.validate().catch(() => false)

  if (!valid || !editingRow.value) {
    return
  }

  saving.value = true
  try {
    const updated = await updateAdminItem(editingRow.value.id, {
      ...itemForm.value,
      nameEn: itemForm.value.nameEn.trim(),
      nameZh: itemForm.value.nameZh.trim(),
      itemType: itemForm.value.itemType.trim(),
      rarity: itemForm.value.rarity.trim(),
      gridSize: itemForm.value.gridSize.trim(),
      description: itemForm.value.description.trim(),
    })
    Object.assign(editingRow.value, updated)
    ElMessage.success('物品资料已更新')
    editDialogVisible.value = false
  } catch (error) {
    ElMessage.error(resolveError(error, '物品资料保存失败'))
  } finally {
    saving.value = false
  }
}

async function toggleItem(row) {
  actionItemId.value = row.id
  const nextStatus = row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED'

  try {
    const updated = await updateAdminItem(row.id, toPayload(row, { status: nextStatus }))
    Object.assign(row, updated)
    ElMessage.success(nextStatus === 'ENABLED' ? '物品已启用' : '物品已停用')
    if (filters.value.status && filters.value.status !== nextStatus) {
      loadItems(1)
    }
  } catch (error) {
    ElMessage.error(resolveError(error, '物品状态更新失败'))
  } finally {
    actionItemId.value = null
  }
}

onMounted(() => loadItems(1))
</script>

<template>
  <div class="admin-review-view">
    <section class="admin-hero">
      <div>
        <h2>物品管理</h2>
        <p>维护钥匙、任务物品、藏身处材料和可保留物品，让资料库服务于玩家讨论和路线规划。</p>
      </div>
      <el-button :icon="Refresh" :loading="loading" @click="loadItems()">
        刷新物品
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
          placeholder="搜索物品名称、类型、稀有度、尺寸或说明"
          clearable
          @keyup.enter="loadItems(1)"
        />
        <el-input
          v-model="filters.itemType"
          placeholder="物品类型"
          clearable
          @keyup.enter="loadItems(1)"
        />
        <el-select v-model="filters.questNeeded" placeholder="任务需求" clearable>
          <el-option
            v-for="option in booleanOptions"
            :key="`quest-${option.label}`"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
        <el-select v-model="filters.hideoutNeeded" placeholder="藏身处需求" clearable>
          <el-option
            v-for="option in booleanOptions"
            :key="`hideout-${option.label}`"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
        <el-select v-model="filters.keepSuggestion" placeholder="保留建议" clearable>
          <el-option
            v-for="option in booleanOptions"
            :key="`keep-${option.label}`"
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
        <el-button type="primary" :icon="Search" @click="loadItems(1)">
          查询
        </el-button>
        <el-button :icon="Refresh" @click="resetFilters">
          重置
        </el-button>
      </div>
    </section>

    <section class="admin-table-card">
      <el-table v-loading="loading" :data="itemsPage.records" row-key="id">
        <el-table-column label="物品" min-width="340">
          <template #default="{ row }">
            <div class="admin-item-cell">
              <Document />
              <div>
                <strong>{{ row.nameZh || row.nameEn }} / {{ row.nameEn }}</strong>
                <span>{{ row.description || '暂无说明' }}</span>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="类型" width="120">
          <template #default="{ row }">
            <span class="admin-table-muted">{{ row.itemType || '未设置' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="稀有度" width="130">
          <template #default="{ row }">
            <span class="admin-table-muted">{{ row.rarity || '未设置' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="尺寸" width="90">
          <template #default="{ row }">
            <span class="admin-table-muted">{{ row.gridSize || '未设置' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="需求" width="210">
          <template #default="{ row }">
            <span class="admin-table-muted">
              任务 {{ booleanLabel(row.questNeeded) }} / 藏身处 {{ booleanLabel(row.hideoutNeeded) }} / 保留 {{ booleanLabel(row.keepSuggestion) }}
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
                :loading="actionItemId === row.id"
                @click="toggleItem(row)"
              >
                {{ row.status === 'ENABLED' ? '停用' : '启用' }}
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="!loading && itemsPage.records.length === 0" class="admin-table-empty">
        当前没有符合条件的物品
      </div>

      <el-pagination
        v-if="itemsPage.pages > 1"
        class="board-pagination"
        background
        layout="prev, pager, next"
        :current-page="itemsPage.page"
        :page-size="itemsPage.size"
        :total="itemsPage.total"
        @current-change="loadItems"
      />
    </section>

    <el-dialog
      v-model="editDialogVisible"
      title="编辑物品"
      width="620px"
      class="profile-edit-dialog"
    >
      <el-form
        ref="itemFormRef"
        :model="itemForm"
        :rules="itemRules"
        label-position="top"
      >
        <el-form-item label="英文名" prop="nameEn">
          <el-input v-model="itemForm.nameEn" maxlength="120" show-word-limit />
        </el-form-item>
        <el-form-item label="中文名" prop="nameZh">
          <el-input v-model="itemForm.nameZh" maxlength="120" show-word-limit />
        </el-form-item>
        <el-form-item label="物品类型" prop="itemType">
          <el-input v-model="itemForm.itemType" maxlength="60" show-word-limit />
        </el-form-item>
        <el-form-item label="稀有度" prop="rarity">
          <el-input v-model="itemForm.rarity" maxlength="40" show-word-limit />
        </el-form-item>
        <el-form-item label="格子尺寸" prop="gridSize">
          <el-input v-model="itemForm.gridSize" maxlength="20" show-word-limit />
        </el-form-item>
        <el-form-item label="需求标记">
          <el-checkbox v-model="itemForm.questNeeded">
            任务需求
          </el-checkbox>
          <el-checkbox v-model="itemForm.hideoutNeeded">
            藏身处需求
          </el-checkbox>
          <el-checkbox v-model="itemForm.keepSuggestion">
            建议保留
          </el-checkbox>
        </el-form-item>
        <el-form-item label="物品说明" prop="description">
          <el-input
            v-model="itemForm.description"
            type="textarea"
            :rows="4"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="itemForm.status">
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
          <el-button type="primary" :loading="saving" @click="submitItem">
            保存物品
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>
