<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { EditPen, OfficeBuilding, Refresh, Search } from '@element-plus/icons-vue'
import {
  fetchAdminHideoutStations,
  fetchAdminHideoutUpgrades,
  updateAdminHideoutUpgrade,
} from '../api/adminApi'
import { useUserStore } from '../stores/userStore'

const userStore = useUserStore()

const loading = ref(false)
const saving = ref(false)
const errorMessage = ref('')
const editDialogVisible = ref(false)
const upgradeFormRef = ref(null)
const editingRow = ref(null)
const stationOptions = ref([])
const filters = ref({
  stationId: null,
  keyword: '',
})
const upgradeForm = ref({
  stationId: null,
  level: 1,
  requiredItems: '',
  requiredTime: '',
  unlocks: '',
})
const upgradesPage = ref({
  page: 1,
  size: 10,
  total: 0,
  pages: 0,
  records: [],
})

const upgradeRules = {
  stationId: [
    { required: true, message: '请选择所属设施', trigger: 'change' },
  ],
  level: [
    { required: true, message: '请填写升级等级', trigger: 'blur' },
    { type: 'number', min: 1, message: '升级等级不能小于 1', trigger: 'blur' },
  ],
  requiredItems: [
    { max: 2000, message: '所需物品不能超过 2000 个字符', trigger: 'blur' },
  ],
  requiredTime: [
    { max: 80, message: '耗时不能超过 80 个字符', trigger: 'blur' },
  ],
  unlocks: [
    { max: 500, message: '解锁内容不能超过 500 个字符', trigger: 'blur' },
  ],
}

function resolveError(error, fallback) {
  return error?.response?.data?.message || error?.message || fallback
}

function optionLabel(item) {
  if (!item) {
    return ''
  }

  return item.nameZh ? `${item.nameZh} / ${item.nameEn}` : item.nameEn
}

function toPayload(row, overrides = {}) {
  return {
    stationId: row.stationId ?? null,
    level: row.level ?? 1,
    requiredItems: row.requiredItems || '',
    requiredTime: row.requiredTime || '',
    unlocks: row.unlocks || '',
    ...overrides,
  }
}

async function loadOptions() {
  try {
    const stations = await fetchAdminHideoutStations({ page: 1, size: 50 })
    stationOptions.value = stations.records || []
  } catch (error) {
    ElMessage.warning(resolveError(error, '藏身处设施选项暂时无法加载'))
  }
}

async function loadUpgrades(page = upgradesPage.value.page) {
  loading.value = true
  errorMessage.value = ''
  upgradesPage.value.page = page

  try {
    upgradesPage.value = await fetchAdminHideoutUpgrades({
      page,
      size: upgradesPage.value.size,
      stationId: filters.value.stationId || undefined,
      keyword: filters.value.keyword.trim() || undefined,
    })
  } catch (error) {
    errorMessage.value = resolveError(error, '藏身处升级列表暂时无法加载')
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  filters.value = {
    stationId: null,
    keyword: '',
  }
  loadUpgrades(1)
}

function openEditDialog(row) {
  editingRow.value = row
  upgradeForm.value = toPayload(row)
  editDialogVisible.value = true
}

async function submitUpgrade() {
  const valid = await upgradeFormRef.value?.validate().catch(() => false)

  if (!valid || !editingRow.value) {
    return
  }

  saving.value = true
  try {
    const updated = await updateAdminHideoutUpgrade(editingRow.value.id, {
      ...upgradeForm.value,
      level: Number(upgradeForm.value.level),
      requiredItems: upgradeForm.value.requiredItems.trim(),
      requiredTime: upgradeForm.value.requiredTime.trim(),
      unlocks: upgradeForm.value.unlocks.trim(),
    })
    Object.assign(editingRow.value, updated)
    ElMessage.success('藏身处升级要求已更新')
    editDialogVisible.value = false
  } catch (error) {
    ElMessage.error(resolveError(error, '藏身处升级要求保存失败'))
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadOptions()
  loadUpgrades(1)
})
</script>

<template>
  <div class="admin-review-view">
    <section class="admin-hero">
      <div>
        <h2>升级管理</h2>
        <p>维护藏身处每个设施的等级需求、耗时和解锁内容，方便玩家围绕制作路线、材料储备和升级顺序讨论。</p>
      </div>
      <el-button :icon="Refresh" :loading="loading" @click="loadUpgrades()">
        刷新升级
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
          placeholder="搜索所需物品、耗时或解锁内容"
          clearable
          @keyup.enter="loadUpgrades(1)"
        />
        <el-select v-model="filters.stationId" placeholder="全部设施" clearable filterable>
          <el-option
            v-for="station in stationOptions"
            :key="station.id"
            :label="optionLabel(station)"
            :value="station.id"
          />
        </el-select>
      </div>

      <div class="board-actions">
        <el-button type="primary" :icon="Search" @click="loadUpgrades(1)">
          查询
        </el-button>
        <el-button :icon="Refresh" @click="resetFilters">
          重置
        </el-button>
      </div>
    </section>

    <section class="admin-table-card">
      <el-table v-loading="loading" :data="upgradesPage.records" row-key="id">
        <el-table-column label="升级" min-width="320">
          <template #default="{ row }">
            <div class="admin-upgrade-cell">
              <OfficeBuilding />
              <div>
                <strong>{{ row.stationName || '未关联设施' }} Lv.{{ row.level }}</strong>
                <span>耗时：{{ row.requiredTime || '未设置' }}</span>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="所需物品" min-width="260">
          <template #default="{ row }">
            <span class="admin-table-muted">{{ row.requiredItems || '未设置' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="解锁内容" min-width="260">
          <template #default="{ row }">
            <span class="admin-table-muted">{{ row.unlocks || '未设置' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <div class="admin-row-actions">
              <el-button size="small" :icon="EditPen" @click="openEditDialog(row)">
                编辑
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="!loading && upgradesPage.records.length === 0" class="admin-table-empty">
        当前没有符合条件的藏身处升级要求
      </div>

      <el-pagination
        v-if="upgradesPage.pages > 1"
        class="board-pagination"
        background
        layout="prev, pager, next"
        :current-page="upgradesPage.page"
        :page-size="upgradesPage.size"
        :total="upgradesPage.total"
        @current-change="loadUpgrades"
      />
    </section>

    <el-dialog
      v-model="editDialogVisible"
      title="编辑升级要求"
      width="640px"
      class="profile-edit-dialog"
    >
      <el-form
        ref="upgradeFormRef"
        :model="upgradeForm"
        :rules="upgradeRules"
        label-position="top"
      >
        <el-form-item label="所属设施" prop="stationId">
          <el-select v-model="upgradeForm.stationId" filterable>
            <el-option
              v-for="station in stationOptions"
              :key="station.id"
              :label="optionLabel(station)"
              :value="station.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="等级" prop="level">
          <el-input-number v-model="upgradeForm.level" :min="1" />
        </el-form-item>
        <el-form-item label="所需物品" prop="requiredItems">
          <el-input
            v-model="upgradeForm.requiredItems"
            type="textarea"
            :rows="5"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="耗时" prop="requiredTime">
          <el-input v-model="upgradeForm.requiredTime" maxlength="80" show-word-limit />
        </el-form-item>
        <el-form-item label="解锁内容" prop="unlocks">
          <el-input
            v-model="upgradeForm.unlocks"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="profile-dialog-footer">
          <el-button @click="editDialogVisible = false">
            取消
          </el-button>
          <el-button type="primary" :loading="saving" @click="submitUpgrade">
            保存升级要求
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>
