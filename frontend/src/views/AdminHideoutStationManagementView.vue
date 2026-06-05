<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { EditPen, OfficeBuilding, Refresh, Search, SwitchButton } from '@element-plus/icons-vue'
import { fetchAdminHideoutStations, updateAdminHideoutStation } from '../api/adminApi'
import { useUserStore } from '../stores/userStore'

const userStore = useUserStore()

const loading = ref(false)
const saving = ref(false)
const actionStationId = ref(null)
const errorMessage = ref('')
const editDialogVisible = ref(false)
const stationFormRef = ref(null)
const editingRow = ref(null)
const filters = ref({
  status: '',
  keyword: '',
})
const stationForm = ref({
  nameEn: '',
  nameZh: '',
  description: '',
  status: 'ENABLED',
})
const stationsPage = ref({
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

const stationRules = {
  nameEn: [
    { required: true, message: '请填写设施英文名', trigger: 'blur' },
    { max: 120, message: '设施英文名不能超过 120 个字符', trigger: 'blur' },
  ],
  nameZh: [
    { max: 120, message: '设施中文名不能超过 120 个字符', trigger: 'blur' },
  ],
  description: [
    { max: 500, message: '设施说明不能超过 500 个字符', trigger: 'blur' },
  ],
  status: [
    { required: true, message: '请选择设施状态', trigger: 'change' },
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
    nameZh: row.nameZh || '',
    description: row.description || '',
    status: row.status,
    ...overrides,
  }
}

async function loadStations(page = stationsPage.value.page) {
  loading.value = true
  errorMessage.value = ''
  stationsPage.value.page = page

  try {
    stationsPage.value = await fetchAdminHideoutStations({
      page,
      size: stationsPage.value.size,
      status: filters.value.status || undefined,
      keyword: filters.value.keyword.trim() || undefined,
    })
  } catch (error) {
    errorMessage.value = resolveError(error, '藏身处设施列表暂时无法加载')
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  filters.value = {
    status: '',
    keyword: '',
  }
  loadStations(1)
}

function openEditDialog(row) {
  editingRow.value = row
  stationForm.value = toPayload(row)
  editDialogVisible.value = true
}

async function submitStation() {
  const valid = await stationFormRef.value?.validate().catch(() => false)

  if (!valid || !editingRow.value) {
    return
  }

  saving.value = true
  try {
    const updated = await updateAdminHideoutStation(editingRow.value.id, {
      ...stationForm.value,
      nameEn: stationForm.value.nameEn.trim(),
      nameZh: stationForm.value.nameZh.trim(),
      description: stationForm.value.description.trim(),
    })
    Object.assign(editingRow.value, updated)
    ElMessage.success('藏身处设施已更新')
    editDialogVisible.value = false
  } catch (error) {
    ElMessage.error(resolveError(error, '藏身处设施保存失败'))
  } finally {
    saving.value = false
  }
}

async function toggleStation(row) {
  actionStationId.value = row.id
  const nextStatus = row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED'

  try {
    const updated = await updateAdminHideoutStation(row.id, toPayload(row, { status: nextStatus }))
    Object.assign(row, updated)
    ElMessage.success(nextStatus === 'ENABLED' ? '设施已启用' : '设施已停用')
    if (filters.value.status && filters.value.status !== nextStatus) {
      loadStations(1)
    }
  } catch (error) {
    ElMessage.error(resolveError(error, '设施状态更新失败'))
  } finally {
    actionStationId.value = null
  }
}

onMounted(() => loadStations(1))
</script>

<template>
  <div class="admin-review-view">
    <section class="admin-hero">
      <div>
        <h2>藏身处管理</h2>
        <p>维护工作台、医疗站、情报中心等设施资料，为材料讨论、制作收益和升级路线提供基础数据。</p>
      </div>
      <el-button :icon="Refresh" :loading="loading" @click="loadStations()">
        刷新设施
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
          placeholder="搜索设施名称或说明"
          clearable
          @keyup.enter="loadStations(1)"
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
        <el-button type="primary" :icon="Search" @click="loadStations(1)">
          查询
        </el-button>
        <el-button :icon="Refresh" @click="resetFilters">
          重置
        </el-button>
      </div>
    </section>

    <section class="admin-table-card">
      <el-table v-loading="loading" :data="stationsPage.records" row-key="id">
        <el-table-column label="设施" min-width="360">
          <template #default="{ row }">
            <div class="admin-hideout-cell">
              <OfficeBuilding />
              <div>
                <strong>{{ row.nameZh || row.nameEn }} / {{ row.nameEn }}</strong>
                <span>{{ row.description || '暂无说明' }}</span>
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

        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <div class="admin-row-actions">
              <el-button size="small" :icon="EditPen" @click="openEditDialog(row)">
                编辑
              </el-button>
              <el-button
                size="small"
                :icon="SwitchButton"
                :loading="actionStationId === row.id"
                @click="toggleStation(row)"
              >
                {{ row.status === 'ENABLED' ? '停用' : '启用' }}
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="!loading && stationsPage.records.length === 0" class="admin-table-empty">
        当前没有符合条件的藏身处设施
      </div>

      <el-pagination
        v-if="stationsPage.pages > 1"
        class="board-pagination"
        background
        layout="prev, pager, next"
        :current-page="stationsPage.page"
        :page-size="stationsPage.size"
        :total="stationsPage.total"
        @current-change="loadStations"
      />
    </section>

    <el-dialog
      v-model="editDialogVisible"
      title="编辑藏身处设施"
      width="560px"
      class="profile-edit-dialog"
    >
      <el-form
        ref="stationFormRef"
        :model="stationForm"
        :rules="stationRules"
        label-position="top"
      >
        <el-form-item label="英文名" prop="nameEn">
          <el-input v-model="stationForm.nameEn" maxlength="120" show-word-limit />
        </el-form-item>
        <el-form-item label="中文名" prop="nameZh">
          <el-input v-model="stationForm.nameZh" maxlength="120" show-word-limit />
        </el-form-item>
        <el-form-item label="设施说明" prop="description">
          <el-input
            v-model="stationForm.description"
            type="textarea"
            :rows="4"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="stationForm.status">
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
          <el-button type="primary" :loading="saving" @click="submitStation">
            保存设施
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>
