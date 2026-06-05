<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { EditPen, MapLocation, Refresh, Search, SwitchButton } from '@element-plus/icons-vue'
import { fetchAdminMapExtracts, fetchAdminMaps, updateAdminMapExtract } from '../api/adminApi'
import { useUserStore } from '../stores/userStore'

const userStore = useUserStore()

const loading = ref(false)
const saving = ref(false)
const actionExtractId = ref(null)
const errorMessage = ref('')
const editDialogVisible = ref(false)
const extractFormRef = ref(null)
const editingRow = ref(null)
const mapOptions = ref([])
const filters = ref({
  mapId: null,
  status: '',
  keyword: '',
})
const extractForm = ref({
  mapId: null,
  name: '',
  factionLimit: '',
  conditionText: '',
  description: '',
  status: 'ENABLED',
})
const extractsPage = ref({
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

const extractRules = {
  mapId: [
    { required: true, message: '请选择所属地图', trigger: 'change' },
  ],
  name: [
    { required: true, message: '请填写撤离点名称', trigger: 'blur' },
    { max: 120, message: '撤离点名称不能超过 120 个字符', trigger: 'blur' },
  ],
  factionLimit: [
    { max: 40, message: '阵营限制不能超过 40 个字符', trigger: 'blur' },
  ],
  conditionText: [
    { max: 255, message: '开启条件不能超过 255 个字符', trigger: 'blur' },
  ],
  description: [
    { max: 500, message: '说明不能超过 500 个字符', trigger: 'blur' },
  ],
  status: [
    { required: true, message: '请选择撤离点状态', trigger: 'change' },
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
    mapId: row.mapId ?? null,
    name: row.name || '',
    factionLimit: row.factionLimit || '',
    conditionText: row.conditionText || '',
    description: row.description || '',
    status: row.status,
    ...overrides,
  }
}

async function loadOptions() {
  try {
    const maps = await fetchAdminMaps({ page: 1, size: 50 })
    mapOptions.value = maps.records || []
  } catch (error) {
    ElMessage.warning(resolveError(error, '地图选项暂时无法加载'))
  }
}

async function loadExtracts(page = extractsPage.value.page) {
  loading.value = true
  errorMessage.value = ''
  extractsPage.value.page = page

  try {
    extractsPage.value = await fetchAdminMapExtracts({
      page,
      size: extractsPage.value.size,
      mapId: filters.value.mapId || undefined,
      status: filters.value.status || undefined,
      keyword: filters.value.keyword.trim() || undefined,
    })
  } catch (error) {
    errorMessage.value = resolveError(error, '撤离点列表暂时无法加载')
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  filters.value = {
    mapId: null,
    status: '',
    keyword: '',
  }
  loadExtracts(1)
}

function openEditDialog(row) {
  editingRow.value = row
  extractForm.value = toPayload(row)
  editDialogVisible.value = true
}

async function submitExtract() {
  const valid = await extractFormRef.value?.validate().catch(() => false)

  if (!valid || !editingRow.value) {
    return
  }

  saving.value = true
  try {
    const updated = await updateAdminMapExtract(editingRow.value.id, {
      ...extractForm.value,
      mapId: extractForm.value.mapId,
      name: extractForm.value.name.trim(),
      factionLimit: extractForm.value.factionLimit.trim(),
      conditionText: extractForm.value.conditionText.trim(),
      description: extractForm.value.description.trim(),
    })
    Object.assign(editingRow.value, updated)
    ElMessage.success('撤离点资料已更新')
    editDialogVisible.value = false
  } catch (error) {
    ElMessage.error(resolveError(error, '撤离点资料保存失败'))
  } finally {
    saving.value = false
  }
}

async function toggleExtract(row) {
  actionExtractId.value = row.id
  const nextStatus = row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED'

  try {
    const updated = await updateAdminMapExtract(row.id, toPayload(row, { status: nextStatus }))
    Object.assign(row, updated)
    ElMessage.success(nextStatus === 'ENABLED' ? '撤离点已启用' : '撤离点已停用')
    if (filters.value.status && filters.value.status !== nextStatus) {
      loadExtracts(1)
    }
  } catch (error) {
    ElMessage.error(resolveError(error, '撤离点状态更新失败'))
  } finally {
    actionExtractId.value = null
  }
}

onMounted(() => {
  loadOptions()
  loadExtracts(1)
})
</script>

<template>
  <div class="admin-review-view">
    <section class="admin-hero">
      <div>
        <h2>撤离点管理</h2>
        <p>维护各地图撤离点的阵营限制、开启条件和说明，让玩家讨论路线规划、任务撤离和组队分工时有统一参考。</p>
      </div>
      <el-button :icon="Refresh" :loading="loading" @click="loadExtracts()">
        刷新撤离点
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
          placeholder="搜索撤离点、阵营限制或开启条件"
          clearable
          @keyup.enter="loadExtracts(1)"
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
        <el-button type="primary" :icon="Search" @click="loadExtracts(1)">
          查询
        </el-button>
        <el-button :icon="Refresh" @click="resetFilters">
          重置
        </el-button>
      </div>
    </section>

    <section class="admin-table-card">
      <el-table v-loading="loading" :data="extractsPage.records" row-key="id">
        <el-table-column label="撤离点" min-width="320">
          <template #default="{ row }">
            <div class="admin-extract-cell">
              <MapLocation />
              <div>
                <strong>{{ row.name }}</strong>
                <span>{{ row.description || '暂无说明' }}</span>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="地图" width="180">
          <template #default="{ row }">
            <span class="admin-table-muted">{{ row.mapName || '未关联' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="阵营" width="120">
          <template #default="{ row }">
            <span class="admin-table-muted">{{ row.factionLimit || '不限' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="开启条件" min-width="220">
          <template #default="{ row }">
            <span class="admin-table-muted">{{ row.conditionText || '未设置' }}</span>
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
                :loading="actionExtractId === row.id"
                @click="toggleExtract(row)"
              >
                {{ row.status === 'ENABLED' ? '停用' : '启用' }}
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="!loading && extractsPage.records.length === 0" class="admin-table-empty">
        当前没有符合条件的撤离点
      </div>

      <el-pagination
        v-if="extractsPage.pages > 1"
        class="board-pagination"
        background
        layout="prev, pager, next"
        :current-page="extractsPage.page"
        :page-size="extractsPage.size"
        :total="extractsPage.total"
        @current-change="loadExtracts"
      />
    </section>

    <el-dialog
      v-model="editDialogVisible"
      title="编辑撤离点"
      width="620px"
      class="profile-edit-dialog"
    >
      <el-form
        ref="extractFormRef"
        :model="extractForm"
        :rules="extractRules"
        label-position="top"
      >
        <el-form-item label="所属地图" prop="mapId">
          <el-select v-model="extractForm.mapId" filterable>
            <el-option
              v-for="map in mapOptions"
              :key="map.id"
              :label="optionLabel(map)"
              :value="map.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="撤离点名称" prop="name">
          <el-input v-model="extractForm.name" maxlength="120" show-word-limit />
        </el-form-item>
        <el-form-item label="阵营限制" prop="factionLimit">
          <el-input v-model="extractForm.factionLimit" maxlength="40" show-word-limit />
        </el-form-item>
        <el-form-item label="开启条件" prop="conditionText">
          <el-input v-model="extractForm.conditionText" maxlength="255" show-word-limit />
        </el-form-item>
        <el-form-item label="说明" prop="description">
          <el-input
            v-model="extractForm.description"
            type="textarea"
            :rows="4"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="extractForm.status">
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
          <el-button type="primary" :loading="saving" @click="submitExtract">
            保存撤离点
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>
