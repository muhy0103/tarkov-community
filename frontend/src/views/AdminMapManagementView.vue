<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { EditPen, MapLocation, Refresh, Search, SwitchButton } from '@element-plus/icons-vue'
import { fetchAdminMaps, updateAdminMap } from '../api/adminApi'
import { useUserStore } from '../stores/userStore'

const userStore = useUserStore()

const loading = ref(false)
const saving = ref(false)
const actionMapId = ref(null)
const errorMessage = ref('')
const editDialogVisible = ref(false)
const mapFormRef = ref(null)
const editingRow = ref(null)
const filters = ref({
  status: '',
  keyword: '',
})
const mapForm = ref({
  nameEn: '',
  nameZh: '',
  difficulty: '',
  description: '',
  recommendedLevel: '',
  status: 'ENABLED',
})
const mapsPage = ref({
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

const mapRules = {
  nameEn: [
    { required: true, message: '请填写地图英文名', trigger: 'blur' },
    { max: 80, message: '地图英文名不能超过 80 个字符', trigger: 'blur' },
  ],
  nameZh: [
    { required: true, message: '请填写地图中文名', trigger: 'blur' },
    { max: 80, message: '地图中文名不能超过 80 个字符', trigger: 'blur' },
  ],
  difficulty: [
    { max: 30, message: '地图难度不能超过 30 个字符', trigger: 'blur' },
  ],
  description: [
    { max: 500, message: '地图说明不能超过 500 个字符', trigger: 'blur' },
  ],
  recommendedLevel: [
    { max: 50, message: '推荐等级不能超过 50 个字符', trigger: 'blur' },
  ],
  status: [
    { required: true, message: '请选择地图状态', trigger: 'change' },
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
    nameZh: row.nameZh,
    difficulty: row.difficulty || '',
    description: row.description || '',
    recommendedLevel: row.recommendedLevel || '',
    status: row.status,
    ...overrides,
  }
}

async function loadMaps(page = mapsPage.value.page) {
  loading.value = true
  errorMessage.value = ''
  mapsPage.value.page = page

  try {
    mapsPage.value = await fetchAdminMaps({
      page,
      size: mapsPage.value.size,
      status: filters.value.status || undefined,
      keyword: filters.value.keyword.trim() || undefined,
    })
  } catch (error) {
    errorMessage.value = resolveError(error, '地图资料列表暂时无法加载')
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  filters.value = {
    status: '',
    keyword: '',
  }
  loadMaps(1)
}

function openEditDialog(row) {
  editingRow.value = row
  mapForm.value = toPayload(row)
  editDialogVisible.value = true
}

async function submitMap() {
  const valid = await mapFormRef.value?.validate().catch(() => false)

  if (!valid || !editingRow.value) {
    return
  }

  saving.value = true
  try {
    const updated = await updateAdminMap(editingRow.value.id, {
      ...mapForm.value,
      nameEn: mapForm.value.nameEn.trim(),
      nameZh: mapForm.value.nameZh.trim(),
      difficulty: mapForm.value.difficulty.trim(),
      description: mapForm.value.description.trim(),
      recommendedLevel: mapForm.value.recommendedLevel.trim(),
    })
    Object.assign(editingRow.value, updated)
    ElMessage.success('地图资料已更新')
    editDialogVisible.value = false
  } catch (error) {
    ElMessage.error(resolveError(error, '地图资料保存失败'))
  } finally {
    saving.value = false
  }
}

async function toggleMap(row) {
  actionMapId.value = row.id
  const nextStatus = row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED'

  try {
    const updated = await updateAdminMap(row.id, toPayload(row, { status: nextStatus }))
    Object.assign(row, updated)
    ElMessage.success(nextStatus === 'ENABLED' ? '地图已启用' : '地图已停用')
    if (filters.value.status && filters.value.status !== nextStatus) {
      loadMaps(1)
    }
  } catch (error) {
    ElMessage.error(resolveError(error, '地图状态更新失败'))
  } finally {
    actionMapId.value = null
  }
}

onMounted(() => loadMaps(1))
</script>

<template>
  <div class="admin-review-view">
    <section class="admin-hero">
      <div>
        <h2>地图管理</h2>
        <p>维护地图名称、难度、推荐等级和说明，为任务讨论、复盘和组队招募提供统一资料。</p>
      </div>
      <el-button :icon="Refresh" :loading="loading" @click="loadMaps()">
        刷新地图
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
          placeholder="搜索地图名称、难度或说明"
          clearable
          @keyup.enter="loadMaps(1)"
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
        <el-button type="primary" :icon="Search" @click="loadMaps(1)">
          查询
        </el-button>
        <el-button :icon="Refresh" @click="resetFilters">
          重置
        </el-button>
      </div>
    </section>

    <section class="admin-table-card">
      <el-table v-loading="loading" :data="mapsPage.records" row-key="id">
        <el-table-column label="地图" min-width="320">
          <template #default="{ row }">
            <div class="admin-map-cell">
              <MapLocation />
              <div>
                <strong>{{ row.nameZh }} / {{ row.nameEn }}</strong>
                <span>{{ row.description || '暂无说明' }}</span>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="难度" width="120">
          <template #default="{ row }">
            <span class="admin-table-muted">{{ row.difficulty || '未设置' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="推荐等级" width="120">
          <template #default="{ row }">
            <span class="admin-table-muted">{{ row.recommendedLevel || '未设置' }}</span>
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
                :loading="actionMapId === row.id"
                @click="toggleMap(row)"
              >
                {{ row.status === 'ENABLED' ? '停用' : '启用' }}
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="!loading && mapsPage.records.length === 0" class="admin-table-empty">
        当前没有符合条件的地图
      </div>

      <el-pagination
        v-if="mapsPage.pages > 1"
        class="board-pagination"
        background
        layout="prev, pager, next"
        :current-page="mapsPage.page"
        :page-size="mapsPage.size"
        :total="mapsPage.total"
        @current-change="loadMaps"
      />
    </section>

    <el-dialog
      v-model="editDialogVisible"
      title="编辑地图"
      width="560px"
      class="profile-edit-dialog"
    >
      <el-form
        ref="mapFormRef"
        :model="mapForm"
        :rules="mapRules"
        label-position="top"
      >
        <el-form-item label="英文名" prop="nameEn">
          <el-input v-model="mapForm.nameEn" maxlength="80" show-word-limit />
        </el-form-item>
        <el-form-item label="中文名" prop="nameZh">
          <el-input v-model="mapForm.nameZh" maxlength="80" show-word-limit />
        </el-form-item>
        <el-form-item label="难度" prop="difficulty">
          <el-input v-model="mapForm.difficulty" maxlength="30" show-word-limit />
        </el-form-item>
        <el-form-item label="推荐等级" prop="recommendedLevel">
          <el-input v-model="mapForm.recommendedLevel" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="地图说明" prop="description">
          <el-input
            v-model="mapForm.description"
            type="textarea"
            :rows="4"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="mapForm.status">
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
          <el-button type="primary" :loading="saving" @click="submitMap">
            保存地图
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>
