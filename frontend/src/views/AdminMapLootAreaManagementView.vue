<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { EditPen, MapLocation, Refresh, Search } from '@element-plus/icons-vue'
import { fetchAdminMapLootAreas, fetchAdminMaps, updateAdminMapLootArea } from '../api/adminApi'
import { useUserStore } from '../stores/userStore'

const userStore = useUserStore()

const loading = ref(false)
const saving = ref(false)
const errorMessage = ref('')
const editDialogVisible = ref(false)
const lootAreaFormRef = ref(null)
const editingRow = ref(null)
const mapOptions = ref([])
const filters = ref({
  mapId: null,
  riskLevel: '',
  keyword: '',
})
const lootAreaForm = ref({
  mapId: null,
  name: '',
  lootType: '',
  riskLevel: '',
  description: '',
})
const lootAreasPage = ref({
  page: 1,
  size: 10,
  total: 0,
  pages: 0,
  records: [],
})

const riskOptions = [
  { label: '高风险', value: 'HIGH' },
  { label: '中风险', value: 'MEDIUM' },
  { label: '低风险', value: 'LOW' },
]

const lootAreaRules = {
  mapId: [
    { required: true, message: '请选择所属地图', trigger: 'change' },
  ],
  name: [
    { required: true, message: '请填写资源点名称', trigger: 'blur' },
    { max: 120, message: '资源点名称不能超过 120 个字符', trigger: 'blur' },
  ],
  lootType: [
    { max: 80, message: '战利品类型不能超过 80 个字符', trigger: 'blur' },
  ],
  riskLevel: [
    { max: 30, message: '风险等级不能超过 30 个字符', trigger: 'blur' },
  ],
  description: [
    { max: 500, message: '说明不能超过 500 个字符', trigger: 'blur' },
  ],
}

function resolveError(error, fallback) {
  return error?.response?.data?.message || error?.message || fallback
}

function riskLabel(riskLevel) {
  const option = riskOptions.find((item) => item.value === riskLevel)
  return option ? `${option.label} / ${option.value}` : riskLevel || '未设置'
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
    lootType: row.lootType || '',
    riskLevel: row.riskLevel || '',
    description: row.description || '',
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

async function loadLootAreas(page = lootAreasPage.value.page) {
  loading.value = true
  errorMessage.value = ''
  lootAreasPage.value.page = page

  try {
    lootAreasPage.value = await fetchAdminMapLootAreas({
      page,
      size: lootAreasPage.value.size,
      mapId: filters.value.mapId || undefined,
      riskLevel: filters.value.riskLevel || undefined,
      keyword: filters.value.keyword.trim() || undefined,
    })
  } catch (error) {
    errorMessage.value = resolveError(error, '地图资源点列表暂时无法加载')
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  filters.value = {
    mapId: null,
    riskLevel: '',
    keyword: '',
  }
  loadLootAreas(1)
}

function openEditDialog(row) {
  editingRow.value = row
  lootAreaForm.value = toPayload(row)
  editDialogVisible.value = true
}

async function submitLootArea() {
  const valid = await lootAreaFormRef.value?.validate().catch(() => false)

  if (!valid || !editingRow.value) {
    return
  }

  saving.value = true
  try {
    const updated = await updateAdminMapLootArea(editingRow.value.id, {
      ...lootAreaForm.value,
      mapId: lootAreaForm.value.mapId,
      name: lootAreaForm.value.name.trim(),
      lootType: lootAreaForm.value.lootType.trim(),
      riskLevel: lootAreaForm.value.riskLevel.trim(),
      description: lootAreaForm.value.description.trim(),
    })
    Object.assign(editingRow.value, updated)
    ElMessage.success('资源点资料已更新')
    editDialogVisible.value = false
  } catch (error) {
    ElMessage.error(resolveError(error, '资源点资料保存失败'))
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadOptions()
  loadLootAreas(1)
})
</script>

<template>
  <div class="admin-review-view">
    <section class="admin-hero">
      <div>
        <h2>资源点管理</h2>
        <p>维护各地图的高价值房间、箱区、补给路线和风险等级，帮助玩家围绕搜刮路线和战局复盘组织讨论。</p>
      </div>
      <el-button :icon="Refresh" :loading="loading" @click="loadLootAreas()">
        刷新资源点
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
          placeholder="搜索资源点、战利品类型或说明"
          clearable
          @keyup.enter="loadLootAreas(1)"
        />
        <el-select v-model="filters.mapId" placeholder="全部地图" clearable filterable>
          <el-option
            v-for="map in mapOptions"
            :key="map.id"
            :label="optionLabel(map)"
            :value="map.id"
          />
        </el-select>
        <el-select v-model="filters.riskLevel" placeholder="全部风险" clearable>
          <el-option
            v-for="option in riskOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
      </div>

      <div class="board-actions">
        <el-button type="primary" :icon="Search" @click="loadLootAreas(1)">
          查询
        </el-button>
        <el-button :icon="Refresh" @click="resetFilters">
          重置
        </el-button>
      </div>
    </section>

    <section class="admin-table-card">
      <el-table v-loading="loading" :data="lootAreasPage.records" row-key="id">
        <el-table-column label="资源点" min-width="320">
          <template #default="{ row }">
            <div class="admin-loot-cell">
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

        <el-table-column label="战利品类型" width="180">
          <template #default="{ row }">
            <span class="admin-table-muted">{{ row.lootType || '未设置' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="风险等级" width="130">
          <template #default="{ row }">
            <span class="admin-table-muted">{{ riskLabel(row.riskLevel) }}</span>
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

      <div v-if="!loading && lootAreasPage.records.length === 0" class="admin-table-empty">
        当前没有符合条件的资源点
      </div>

      <el-pagination
        v-if="lootAreasPage.pages > 1"
        class="board-pagination"
        background
        layout="prev, pager, next"
        :current-page="lootAreasPage.page"
        :page-size="lootAreasPage.size"
        :total="lootAreasPage.total"
        @current-change="loadLootAreas"
      />
    </section>

    <el-dialog
      v-model="editDialogVisible"
      title="编辑资源点"
      width="620px"
      class="profile-edit-dialog"
    >
      <el-form
        ref="lootAreaFormRef"
        :model="lootAreaForm"
        :rules="lootAreaRules"
        label-position="top"
      >
        <el-form-item label="所属地图" prop="mapId">
          <el-select v-model="lootAreaForm.mapId" filterable>
            <el-option
              v-for="map in mapOptions"
              :key="map.id"
              :label="optionLabel(map)"
              :value="map.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="资源点名称" prop="name">
          <el-input v-model="lootAreaForm.name" maxlength="120" show-word-limit />
        </el-form-item>
        <el-form-item label="战利品类型" prop="lootType">
          <el-input v-model="lootAreaForm.lootType" maxlength="80" show-word-limit />
        </el-form-item>
        <el-form-item label="风险等级" prop="riskLevel">
          <el-select
            v-model="lootAreaForm.riskLevel"
            clearable
            filterable
            allow-create
            placeholder="选择或输入风险等级"
          >
            <el-option
              v-for="option in riskOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="说明" prop="description">
          <el-input
            v-model="lootAreaForm.description"
            type="textarea"
            :rows="4"
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
          <el-button type="primary" :loading="saving" @click="submitLootArea">
            保存资源点
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>
