<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Document, EditPen, Refresh, Search, SwitchButton } from '@element-plus/icons-vue'
import { fetchAdminAmmo, updateAdminAmmo } from '../api/adminApi'
import { useUserStore } from '../stores/userStore'

const userStore = useUserStore()

const loading = ref(false)
const saving = ref(false)
const actionAmmoId = ref(null)
const errorMessage = ref('')
const editDialogVisible = ref(false)
const ammoFormRef = ref(null)
const editingRow = ref(null)
const filters = ref({
  status: '',
  keyword: '',
  caliber: '',
})
const ammoForm = ref({
  nameEn: '',
  nameZh: '',
  caliber: '',
  damage: null,
  penetration: null,
  armorDamage: null,
  description: '',
  imageUrl: '',
  status: 'ENABLED',
})
const ammoPage = ref({
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

const ammoRules = {
  nameEn: [
    { required: true, message: '请填写弹药英文名', trigger: 'blur' },
    { max: 120, message: '弹药英文名不能超过 120 个字符', trigger: 'blur' },
  ],
  nameZh: [
    { max: 120, message: '弹药中文名不能超过 120 个字符', trigger: 'blur' },
  ],
  caliber: [
    { required: true, message: '请填写口径', trigger: 'blur' },
    { max: 60, message: '口径不能超过 60 个字符', trigger: 'blur' },
  ],
  damage: [
    { type: 'number', min: 0, message: '伤害不能小于 0', trigger: 'blur' },
  ],
  penetration: [
    { type: 'number', min: 0, message: '穿透不能小于 0', trigger: 'blur' },
  ],
  armorDamage: [
    { type: 'number', min: 0, message: '护甲伤害不能小于 0', trigger: 'blur' },
  ],
  description: [
    { max: 500, message: '弹药说明不能超过 500 个字符', trigger: 'blur' },
  ],
  imageUrl: [
    { max: 500, message: '图片地址不能超过 500 个字符', trigger: 'blur' },
  ],
  status: [
    { required: true, message: '请选择弹药状态', trigger: 'change' },
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

function numberLabel(value) {
  return value ?? '未设置'
}

function toPayload(row, overrides = {}) {
  return {
    nameEn: row.nameEn,
    nameZh: row.nameZh || '',
    caliber: row.caliber || '',
    damage: row.damage ?? null,
    penetration: row.penetration ?? null,
    armorDamage: row.armorDamage ?? null,
    description: row.description || '',
    imageUrl: row.imageUrl || '',
    status: row.status,
    ...overrides,
  }
}

function normalizeNumber(value) {
  return value === '' || value === null ? null : Number(value)
}

async function loadAmmo(page = ammoPage.value.page) {
  loading.value = true
  errorMessage.value = ''
  ammoPage.value.page = page

  try {
    ammoPage.value = await fetchAdminAmmo({
      page,
      size: ammoPage.value.size,
      caliber: filters.value.caliber.trim() || undefined,
      status: filters.value.status || undefined,
      keyword: filters.value.keyword.trim() || undefined,
    })
  } catch (error) {
    errorMessage.value = resolveError(error, '弹药资料列表暂时无法加载')
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  filters.value = {
    status: '',
    keyword: '',
    caliber: '',
  }
  loadAmmo(1)
}

function openEditDialog(row) {
  editingRow.value = row
  ammoForm.value = toPayload(row)
  editDialogVisible.value = true
}

async function submitAmmo() {
  const valid = await ammoFormRef.value?.validate().catch(() => false)

  if (!valid || !editingRow.value) {
    return
  }

  saving.value = true
  try {
    const updated = await updateAdminAmmo(editingRow.value.id, {
      ...ammoForm.value,
      nameEn: ammoForm.value.nameEn.trim(),
      nameZh: ammoForm.value.nameZh.trim(),
      caliber: ammoForm.value.caliber.trim(),
      damage: normalizeNumber(ammoForm.value.damage),
      penetration: normalizeNumber(ammoForm.value.penetration),
      armorDamage: normalizeNumber(ammoForm.value.armorDamage),
      description: ammoForm.value.description.trim(),
      imageUrl: ammoForm.value.imageUrl.trim(),
    })
    Object.assign(editingRow.value, updated)
    ElMessage.success('弹药资料已更新')
    editDialogVisible.value = false
  } catch (error) {
    ElMessage.error(resolveError(error, '弹药资料保存失败'))
  } finally {
    saving.value = false
  }
}

async function toggleAmmo(row) {
  actionAmmoId.value = row.id
  const nextStatus = row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED'

  try {
    const updated = await updateAdminAmmo(row.id, toPayload(row, { status: nextStatus }))
    Object.assign(row, updated)
    ElMessage.success(nextStatus === 'ENABLED' ? '弹药已启用' : '弹药已停用')
    if (filters.value.status && filters.value.status !== nextStatus) {
      loadAmmo(1)
    }
  } catch (error) {
    ElMessage.error(resolveError(error, '弹药状态更新失败'))
  } finally {
    actionAmmoId.value = null
  }
}

onMounted(() => loadAmmo(1))
</script>

<template>
  <div class="admin-review-view">
    <section class="admin-hero">
      <div>
        <h2>弹药管理</h2>
        <p>维护弹药口径、伤害、穿透和护甲伤害，为配装建议和战斗讨论提供可靠数据。</p>
      </div>
      <el-button :icon="Refresh" :loading="loading" @click="loadAmmo()">
        刷新弹药
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
          placeholder="搜索弹药名称、口径或说明"
          clearable
          @keyup.enter="loadAmmo(1)"
        />
        <el-input
          v-model="filters.caliber"
          placeholder="口径"
          clearable
          @keyup.enter="loadAmmo(1)"
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
        <el-button type="primary" :icon="Search" @click="loadAmmo(1)">
          查询
        </el-button>
        <el-button :icon="Refresh" @click="resetFilters">
          重置
        </el-button>
      </div>
    </section>

    <section class="admin-table-card">
      <el-table v-loading="loading" :data="ammoPage.records" row-key="id">
        <el-table-column label="弹药" min-width="340">
          <template #default="{ row }">
            <div class="admin-ammo-cell">
              <img
                v-if="row.imageUrl"
                class="admin-media-thumb"
                :src="row.imageUrl"
                :alt="row.nameZh || row.nameEn"
                loading="lazy"
              />
              <Document v-else />
              <div>
                <strong>{{ row.nameZh || row.nameEn }} / {{ row.nameEn }}</strong>
                <span>{{ row.description || '暂无说明' }}</span>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="口径" width="130">
          <template #default="{ row }">
            <span class="admin-table-muted">{{ row.caliber || '未设置' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="伤害" width="90">
          <template #default="{ row }">
            <span class="admin-table-muted">{{ numberLabel(row.damage) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="穿透" width="90">
          <template #default="{ row }">
            <span class="admin-table-muted">{{ numberLabel(row.penetration) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="护甲伤害" width="110">
          <template #default="{ row }">
            <span class="admin-table-muted">{{ numberLabel(row.armorDamage) }}</span>
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
                :loading="actionAmmoId === row.id"
                @click="toggleAmmo(row)"
              >
                {{ row.status === 'ENABLED' ? '停用' : '启用' }}
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="!loading && ammoPage.records.length === 0" class="admin-table-empty">
        当前没有符合条件的弹药
      </div>

      <el-pagination
        v-if="ammoPage.pages > 1"
        class="board-pagination"
        background
        layout="prev, pager, next"
        :current-page="ammoPage.page"
        :page-size="ammoPage.size"
        :total="ammoPage.total"
        @current-change="loadAmmo"
      />
    </section>

    <el-dialog
      v-model="editDialogVisible"
      title="编辑弹药"
      width="580px"
      class="profile-edit-dialog"
    >
      <el-form
        ref="ammoFormRef"
        :model="ammoForm"
        :rules="ammoRules"
        label-position="top"
      >
        <el-form-item label="英文名" prop="nameEn">
          <el-input v-model="ammoForm.nameEn" maxlength="120" show-word-limit />
        </el-form-item>
        <el-form-item label="中文名" prop="nameZh">
          <el-input v-model="ammoForm.nameZh" maxlength="120" show-word-limit />
        </el-form-item>
        <el-form-item label="口径" prop="caliber">
          <el-input v-model="ammoForm.caliber" maxlength="60" show-word-limit />
        </el-form-item>
        <el-form-item label="伤害" prop="damage">
          <el-input v-model.number="ammoForm.damage" type="number" min="0" />
        </el-form-item>
        <el-form-item label="穿透" prop="penetration">
          <el-input v-model.number="ammoForm.penetration" type="number" min="0" />
        </el-form-item>
        <el-form-item label="护甲伤害" prop="armorDamage">
          <el-input v-model.number="ammoForm.armorDamage" type="number" min="0" />
        </el-form-item>
        <el-form-item label="图片地址" prop="imageUrl">
          <el-input
            v-model="ammoForm.imageUrl"
            maxlength="500"
            show-word-limit
            placeholder="https://..."
          />
        </el-form-item>
        <div v-if="ammoForm.imageUrl" class="admin-media-preview">
          <img :src="ammoForm.imageUrl" alt="图片预览" />
        </div>
        <el-form-item label="弹药说明" prop="description">
          <el-input
            v-model="ammoForm.description"
            type="textarea"
            :rows="4"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="ammoForm.status">
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
          <el-button type="primary" :loading="saving" @click="submitAmmo">
            保存弹药
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>
