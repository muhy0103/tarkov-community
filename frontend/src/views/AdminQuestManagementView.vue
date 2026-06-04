<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Document, EditPen, Refresh, Search, SwitchButton } from '@element-plus/icons-vue'
import {
  fetchAdminMaps,
  fetchAdminQuests,
  fetchAdminTraders,
  updateAdminQuest,
} from '../api/adminApi'
import { useUserStore } from '../stores/userStore'

const userStore = useUserStore()

const loading = ref(false)
const saving = ref(false)
const actionQuestId = ref(null)
const errorMessage = ref('')
const editDialogVisible = ref(false)
const questFormRef = ref(null)
const editingRow = ref(null)
const traderOptions = ref([])
const mapOptions = ref([])
const filters = ref({
  status: '',
  keyword: '',
  traderId: null,
  mapId: null,
})
const questForm = ref({
  traderId: null,
  nameEn: '',
  nameZh: '',
  questType: '',
  mapId: null,
  description: '',
  rewards: '',
  unlocks: '',
  status: 'ENABLED',
})
const questsPage = ref({
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

const questRules = {
  traderId: [
    { required: true, message: '请选择所属商人', trigger: 'change' },
  ],
  nameEn: [
    { required: true, message: '请填写任务英文名', trigger: 'blur' },
    { max: 120, message: '任务英文名不能超过 120 个字符', trigger: 'blur' },
  ],
  nameZh: [
    { max: 120, message: '任务中文名不能超过 120 个字符', trigger: 'blur' },
  ],
  questType: [
    { max: 50, message: '任务类型不能超过 50 个字符', trigger: 'blur' },
  ],
  description: [
    { max: 2000, message: '任务说明不能超过 2000 个字符', trigger: 'blur' },
  ],
  rewards: [
    { max: 500, message: '任务奖励不能超过 500 个字符', trigger: 'blur' },
  ],
  unlocks: [
    { max: 500, message: '后续解锁不能超过 500 个字符', trigger: 'blur' },
  ],
  status: [
    { required: true, message: '请选择任务状态', trigger: 'change' },
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

function optionLabel(item) {
  if (!item) {
    return ''
  }
  return item.nameZh ? `${item.nameZh} / ${item.nameEn}` : item.nameEn
}

function toPayload(row, overrides = {}) {
  return {
    traderId: row.traderId,
    nameEn: row.nameEn,
    nameZh: row.nameZh || '',
    questType: row.questType || '',
    mapId: row.mapId ?? null,
    description: row.description || '',
    rewards: row.rewards || '',
    unlocks: row.unlocks || '',
    status: row.status,
    ...overrides,
  }
}

async function loadOptions() {
  try {
    const [traders, maps] = await Promise.all([
      fetchAdminTraders({ page: 1, size: 50, status: 'ENABLED' }),
      fetchAdminMaps({ page: 1, size: 50, status: 'ENABLED' }),
    ])
    traderOptions.value = traders.records || []
    mapOptions.value = maps.records || []
  } catch (error) {
    ElMessage.warning(resolveError(error, '任务筛选选项暂时无法加载'))
  }
}

async function loadQuests(page = questsPage.value.page) {
  loading.value = true
  errorMessage.value = ''
  questsPage.value.page = page

  try {
    questsPage.value = await fetchAdminQuests({
      page,
      size: questsPage.value.size,
      traderId: filters.value.traderId || undefined,
      mapId: filters.value.mapId || undefined,
      status: filters.value.status || undefined,
      keyword: filters.value.keyword.trim() || undefined,
    })
  } catch (error) {
    errorMessage.value = resolveError(error, '任务资料列表暂时无法加载')
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  filters.value = {
    status: '',
    keyword: '',
    traderId: null,
    mapId: null,
  }
  loadQuests(1)
}

function openEditDialog(row) {
  editingRow.value = row
  questForm.value = toPayload(row)
  editDialogVisible.value = true
}

async function submitQuest() {
  const valid = await questFormRef.value?.validate().catch(() => false)

  if (!valid || !editingRow.value) {
    return
  }

  saving.value = true
  try {
    const updated = await updateAdminQuest(editingRow.value.id, {
      ...questForm.value,
      nameEn: questForm.value.nameEn.trim(),
      nameZh: questForm.value.nameZh.trim(),
      questType: questForm.value.questType.trim(),
      mapId: questForm.value.mapId || null,
      description: questForm.value.description.trim(),
      rewards: questForm.value.rewards.trim(),
      unlocks: questForm.value.unlocks.trim(),
    })
    Object.assign(editingRow.value, updated)
    ElMessage.success('任务资料已更新')
    editDialogVisible.value = false
  } catch (error) {
    ElMessage.error(resolveError(error, '任务资料保存失败'))
  } finally {
    saving.value = false
  }
}

async function toggleQuest(row) {
  actionQuestId.value = row.id
  const nextStatus = row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED'

  try {
    const updated = await updateAdminQuest(row.id, toPayload(row, { status: nextStatus }))
    Object.assign(row, updated)
    ElMessage.success(nextStatus === 'ENABLED' ? '任务已启用' : '任务已停用')
    if (filters.value.status && filters.value.status !== nextStatus) {
      loadQuests(1)
    }
  } catch (error) {
    ElMessage.error(resolveError(error, '任务状态更新失败'))
  } finally {
    actionQuestId.value = null
  }
}

onMounted(() => {
  loadOptions()
  loadQuests(1)
})
</script>

<template>
  <div class="admin-review-view">
    <section class="admin-hero">
      <div>
        <h2>任务管理</h2>
        <p>维护任务所属商人、地图、目标说明、奖励和后续解锁，让任务档案更适合社区讨论。</p>
      </div>
      <el-button :icon="Refresh" :loading="loading" @click="loadQuests()">
        刷新任务
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
          placeholder="搜索任务名称、类型、说明或奖励"
          clearable
          @keyup.enter="loadQuests(1)"
        />
        <el-select v-model="filters.traderId" placeholder="全部商人" clearable filterable>
          <el-option
            v-for="trader in traderOptions"
            :key="trader.id"
            :label="optionLabel(trader)"
            :value="trader.id"
          />
        </el-select>
        <el-select v-model="filters.mapId" placeholder="全部地图" clearable filterable>
          <el-option
            v-for="map in mapOptions"
            :key="map.id"
            :label="optionLabel(map)"
            :value="map.id"
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
        <el-button type="primary" :icon="Search" @click="loadQuests(1)">
          查询
        </el-button>
        <el-button :icon="Refresh" @click="resetFilters">
          重置
        </el-button>
      </div>
    </section>

    <section class="admin-table-card">
      <el-table v-loading="loading" :data="questsPage.records" row-key="id">
        <el-table-column label="任务" min-width="340">
          <template #default="{ row }">
            <div class="admin-quest-cell">
              <Document />
              <div>
                <strong>{{ row.nameZh || row.nameEn }} / {{ row.nameEn }}</strong>
                <span>{{ row.description || '暂无说明' }}</span>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="商人" width="170">
          <template #default="{ row }">
            <span class="admin-table-muted">{{ row.traderName || '未关联' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="地图" width="170">
          <template #default="{ row }">
            <span class="admin-table-muted">{{ row.mapName || '未关联' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="类型" width="120">
          <template #default="{ row }">
            <span class="admin-table-muted">{{ row.questType || '未设置' }}</span>
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
                :loading="actionQuestId === row.id"
                @click="toggleQuest(row)"
              >
                {{ row.status === 'ENABLED' ? '停用' : '启用' }}
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="!loading && questsPage.records.length === 0" class="admin-table-empty">
        当前没有符合条件的任务
      </div>

      <el-pagination
        v-if="questsPage.pages > 1"
        class="board-pagination"
        background
        layout="prev, pager, next"
        :current-page="questsPage.page"
        :page-size="questsPage.size"
        :total="questsPage.total"
        @current-change="loadQuests"
      />
    </section>

    <el-dialog
      v-model="editDialogVisible"
      title="编辑任务"
      width="640px"
      class="profile-edit-dialog"
    >
      <el-form
        ref="questFormRef"
        :model="questForm"
        :rules="questRules"
        label-position="top"
      >
        <el-form-item label="所属商人" prop="traderId">
          <el-select v-model="questForm.traderId" filterable>
            <el-option
              v-for="trader in traderOptions"
              :key="trader.id"
              :label="optionLabel(trader)"
              :value="trader.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="关联地图" prop="mapId">
          <el-select v-model="questForm.mapId" clearable filterable>
            <el-option
              v-for="map in mapOptions"
              :key="map.id"
              :label="optionLabel(map)"
              :value="map.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="英文名" prop="nameEn">
          <el-input v-model="questForm.nameEn" maxlength="120" show-word-limit />
        </el-form-item>
        <el-form-item label="中文名" prop="nameZh">
          <el-input v-model="questForm.nameZh" maxlength="120" show-word-limit />
        </el-form-item>
        <el-form-item label="任务类型" prop="questType">
          <el-input v-model="questForm.questType" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="任务说明" prop="description">
          <el-input
            v-model="questForm.description"
            type="textarea"
            :rows="4"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="任务奖励" prop="rewards">
          <el-input v-model="questForm.rewards" maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item label="后续解锁" prop="unlocks">
          <el-input v-model="questForm.unlocks" maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="questForm.status">
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
          <el-button type="primary" :loading="saving" @click="submitQuest">
            保存任务
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>
