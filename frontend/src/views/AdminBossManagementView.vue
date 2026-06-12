<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { EditPen, Refresh, Search, SwitchButton, WarningFilled } from '@element-plus/icons-vue'
import { fetchAdminBosses, fetchAdminMaps, updateAdminBoss } from '../api/adminApi'
import { useUserStore } from '../stores/userStore'

const userStore = useUserStore()

const loading = ref(false)
const saving = ref(false)
const actionBossId = ref(null)
const errorMessage = ref('')
const editDialogVisible = ref(false)
const bossFormRef = ref(null)
const editingRow = ref(null)
const mapOptions = ref([])
const filters = ref({
  status: '',
  keyword: '',
  mapId: null,
})
const bossForm = ref({
  nameEn: '',
  mapId: null,
  description: '',
  equipmentSummary: '',
  imageUrl: '',
  status: 'ENABLED',
})
const bossesPage = ref({
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

const bossRules = {
  nameEn: [
    { required: true, message: '请填写 Boss 英文名', trigger: 'blur' },
    { max: 120, message: 'Boss 英文名不能超过 120 个字符', trigger: 'blur' },
  ],
  description: [
    { max: 500, message: 'Boss 说明不能超过 500 个字符', trigger: 'blur' },
  ],
  equipmentSummary: [
    { max: 500, message: '装备摘要不能超过 500 个字符', trigger: 'blur' },
  ],
  imageUrl: [
    { max: 500, message: '图片地址不能超过 500 个字符', trigger: 'blur' },
  ],
  status: [
    { required: true, message: '请选择 Boss 状态', trigger: 'change' },
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
    nameEn: row.nameEn,
    mapId: row.mapId ?? null,
    description: row.description || '',
    equipmentSummary: row.equipmentSummary || '',
    imageUrl: row.imageUrl || '',
    status: row.status,
    ...overrides,
  }
}

async function loadOptions() {
  try {
    const maps = await fetchAdminMaps({ page: 1, size: 50, status: 'ENABLED' })
    mapOptions.value = maps.records || []
  } catch (error) {
    ElMessage.warning(resolveError(error, 'Boss 地图选项暂时无法加载'))
  }
}

async function loadBosses(page = bossesPage.value.page) {
  loading.value = true
  errorMessage.value = ''
  bossesPage.value.page = page

  try {
    bossesPage.value = await fetchAdminBosses({
      page,
      size: bossesPage.value.size,
      mapId: filters.value.mapId || undefined,
      status: filters.value.status || undefined,
      keyword: filters.value.keyword.trim() || undefined,
    })
  } catch (error) {
    errorMessage.value = resolveError(error, 'Boss 资料列表暂时无法加载')
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  filters.value = {
    status: '',
    keyword: '',
    mapId: null,
  }
  loadBosses(1)
}

function openEditDialog(row) {
  editingRow.value = row
  bossForm.value = toPayload(row)
  editDialogVisible.value = true
}

async function submitBoss() {
  const valid = await bossFormRef.value?.validate().catch(() => false)

  if (!valid || !editingRow.value) {
    return
  }

  saving.value = true
  try {
    const updated = await updateAdminBoss(editingRow.value.id, {
      ...bossForm.value,
      nameEn: bossForm.value.nameEn.trim(),
      mapId: bossForm.value.mapId || null,
      description: bossForm.value.description.trim(),
      equipmentSummary: bossForm.value.equipmentSummary.trim(),
      imageUrl: bossForm.value.imageUrl.trim(),
    })
    Object.assign(editingRow.value, updated)
    ElMessage.success('Boss 资料已更新')
    editDialogVisible.value = false
  } catch (error) {
    ElMessage.error(resolveError(error, 'Boss 资料保存失败'))
  } finally {
    saving.value = false
  }
}

async function toggleBoss(row) {
  actionBossId.value = row.id
  const nextStatus = row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED'

  try {
    const updated = await updateAdminBoss(row.id, toPayload(row, { status: nextStatus }))
    Object.assign(row, updated)
    ElMessage.success(nextStatus === 'ENABLED' ? 'Boss 已启用' : 'Boss 已停用')
    if (filters.value.status && filters.value.status !== nextStatus) {
      loadBosses(1)
    }
  } catch (error) {
    ElMessage.error(resolveError(error, 'Boss 状态更新失败'))
  } finally {
    actionBossId.value = null
  }
}

onMounted(() => {
  loadOptions()
  loadBosses(1)
})
</script>

<template>
  <div class="admin-review-view">
    <section class="admin-hero">
      <div>
        <h2>Boss 管理</h2>
        <p>维护 Boss 所属地图、行为说明和装备摘要，为战局复盘、路线规划和组队讨论提供统一资料。</p>
      </div>
      <el-button :icon="Refresh" :loading="loading" @click="loadBosses()">
        刷新 Boss
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
          placeholder="搜索 Boss 名称、说明或装备"
          clearable
          @keyup.enter="loadBosses(1)"
        />
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
        <el-button type="primary" :icon="Search" @click="loadBosses(1)">
          查询
        </el-button>
        <el-button :icon="Refresh" @click="resetFilters">
          重置
        </el-button>
      </div>
    </section>

    <section class="admin-table-card">
      <el-table v-loading="loading" :data="bossesPage.records" row-key="id">
        <el-table-column label="Boss" min-width="340">
          <template #default="{ row }">
            <div class="admin-boss-cell">
              <img
                v-if="row.imageUrl"
                class="admin-media-thumb"
                :src="row.imageUrl"
                :alt="row.nameEn"
                loading="lazy"
              />
              <WarningFilled v-else />
              <div>
                <strong>{{ row.nameEn }}</strong>
                <span>{{ row.description || '暂无说明' }}</span>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="地图" width="170">
          <template #default="{ row }">
            <span class="admin-table-muted">{{ row.mapName || '未关联' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="装备摘要" width="230">
          <template #default="{ row }">
            <span class="admin-table-muted">{{ row.equipmentSummary || '未设置' }}</span>
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
                :loading="actionBossId === row.id"
                @click="toggleBoss(row)"
              >
                {{ row.status === 'ENABLED' ? '停用' : '启用' }}
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="!loading && bossesPage.records.length === 0" class="admin-table-empty">
        当前没有符合条件的 Boss
      </div>

      <el-pagination
        v-if="bossesPage.pages > 1"
        class="board-pagination"
        background
        layout="prev, pager, next"
        :current-page="bossesPage.page"
        :page-size="bossesPage.size"
        :total="bossesPage.total"
        @current-change="loadBosses"
      />
    </section>

    <el-dialog
      v-model="editDialogVisible"
      title="编辑 Boss"
      width="620px"
      class="profile-edit-dialog"
    >
      <el-form
        ref="bossFormRef"
        :model="bossForm"
        :rules="bossRules"
        label-position="top"
      >
        <el-form-item label="英文名" prop="nameEn">
          <el-input v-model="bossForm.nameEn" maxlength="120" show-word-limit />
        </el-form-item>
        <el-form-item label="所属地图" prop="mapId">
          <el-select v-model="bossForm.mapId" clearable filterable>
            <el-option
              v-for="map in mapOptions"
              :key="map.id"
              :label="optionLabel(map)"
              :value="map.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="图片地址" prop="imageUrl">
          <el-input
            v-model="bossForm.imageUrl"
            maxlength="500"
            show-word-limit
            placeholder="https://..."
          />
        </el-form-item>
        <div v-if="bossForm.imageUrl" class="admin-media-preview">
          <img :src="bossForm.imageUrl" alt="图片预览" />
        </div>
        <el-form-item label="Boss 说明" prop="description">
          <el-input
            v-model="bossForm.description"
            type="textarea"
            :rows="4"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="装备摘要" prop="equipmentSummary">
          <el-input
            v-model="bossForm.equipmentSummary"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="bossForm.status">
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
          <el-button type="primary" :loading="saving" @click="submitBoss">
            保存 Boss
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>
