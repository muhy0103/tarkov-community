<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Document, EditPen, Refresh, Search, SwitchButton } from '@element-plus/icons-vue'
import { fetchAdminWeapons, updateAdminWeapon } from '../api/adminApi'
import { useUserStore } from '../stores/userStore'

const userStore = useUserStore()

const loading = ref(false)
const saving = ref(false)
const actionWeaponId = ref(null)
const errorMessage = ref('')
const editDialogVisible = ref(false)
const weaponFormRef = ref(null)
const editingRow = ref(null)
const filters = ref({
  status: '',
  keyword: '',
  weaponType: '',
  caliber: '',
})
const weaponForm = ref({
  nameEn: '',
  nameZh: '',
  weaponType: '',
  caliber: '',
  description: '',
  imageUrl: '',
  status: 'ENABLED',
})
const weaponsPage = ref({
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

const weaponRules = {
  nameEn: [
    { required: true, message: '请填写武器英文名', trigger: 'blur' },
    { max: 120, message: '武器英文名不能超过 120 个字符', trigger: 'blur' },
  ],
  nameZh: [
    { max: 120, message: '武器中文名不能超过 120 个字符', trigger: 'blur' },
  ],
  weaponType: [
    { max: 60, message: '武器类型不能超过 60 个字符', trigger: 'blur' },
  ],
  caliber: [
    { max: 60, message: '口径不能超过 60 个字符', trigger: 'blur' },
  ],
  description: [
    { max: 500, message: '武器说明不能超过 500 个字符', trigger: 'blur' },
  ],
  imageUrl: [
    { max: 500, message: '图片地址不能超过 500 个字符', trigger: 'blur' },
  ],
  status: [
    { required: true, message: '请选择武器状态', trigger: 'change' },
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
    weaponType: row.weaponType || '',
    caliber: row.caliber || '',
    description: row.description || '',
    imageUrl: row.imageUrl || '',
    status: row.status,
    ...overrides,
  }
}

async function loadWeapons(page = weaponsPage.value.page) {
  loading.value = true
  errorMessage.value = ''
  weaponsPage.value.page = page

  try {
    weaponsPage.value = await fetchAdminWeapons({
      page,
      size: weaponsPage.value.size,
      weaponType: filters.value.weaponType.trim() || undefined,
      caliber: filters.value.caliber.trim() || undefined,
      status: filters.value.status || undefined,
      keyword: filters.value.keyword.trim() || undefined,
    })
  } catch (error) {
    errorMessage.value = resolveError(error, '武器资料列表暂时无法加载')
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  filters.value = {
    status: '',
    keyword: '',
    weaponType: '',
    caliber: '',
  }
  loadWeapons(1)
}

function openEditDialog(row) {
  editingRow.value = row
  weaponForm.value = toPayload(row)
  editDialogVisible.value = true
}

async function submitWeapon() {
  const valid = await weaponFormRef.value?.validate().catch(() => false)

  if (!valid || !editingRow.value) {
    return
  }

  saving.value = true
  try {
    const updated = await updateAdminWeapon(editingRow.value.id, {
      ...weaponForm.value,
      nameEn: weaponForm.value.nameEn.trim(),
      nameZh: weaponForm.value.nameZh.trim(),
      weaponType: weaponForm.value.weaponType.trim(),
      caliber: weaponForm.value.caliber.trim(),
      description: weaponForm.value.description.trim(),
      imageUrl: weaponForm.value.imageUrl.trim(),
    })
    Object.assign(editingRow.value, updated)
    ElMessage.success('武器资料已更新')
    editDialogVisible.value = false
  } catch (error) {
    ElMessage.error(resolveError(error, '武器资料保存失败'))
  } finally {
    saving.value = false
  }
}

async function toggleWeapon(row) {
  actionWeaponId.value = row.id
  const nextStatus = row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED'

  try {
    const updated = await updateAdminWeapon(row.id, toPayload(row, { status: nextStatus }))
    Object.assign(row, updated)
    ElMessage.success(nextStatus === 'ENABLED' ? '武器已启用' : '武器已停用')
    if (filters.value.status && filters.value.status !== nextStatus) {
      loadWeapons(1)
    }
  } catch (error) {
    ElMessage.error(resolveError(error, '武器状态更新失败'))
  } finally {
    actionWeaponId.value = null
  }
}

onMounted(() => loadWeapons(1))
</script>

<template>
  <div class="admin-review-view">
    <section class="admin-hero">
      <div>
        <h2>武器管理</h2>
        <p>维护武器类型、口径和说明，让配装讨论、弹药选择和新手推荐有统一资料来源。</p>
      </div>
      <el-button :icon="Refresh" :loading="loading" @click="loadWeapons()">
        刷新武器
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
          placeholder="搜索武器名称、类型、口径或说明"
          clearable
          @keyup.enter="loadWeapons(1)"
        />
        <el-input
          v-model="filters.weaponType"
          placeholder="武器类型"
          clearable
          @keyup.enter="loadWeapons(1)"
        />
        <el-input
          v-model="filters.caliber"
          placeholder="口径"
          clearable
          @keyup.enter="loadWeapons(1)"
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
        <el-button type="primary" :icon="Search" @click="loadWeapons(1)">
          查询
        </el-button>
        <el-button :icon="Refresh" @click="resetFilters">
          重置
        </el-button>
      </div>
    </section>

    <section class="admin-table-card">
      <el-table v-loading="loading" :data="weaponsPage.records" row-key="id">
        <el-table-column label="武器" min-width="340">
          <template #default="{ row }">
            <div class="admin-weapon-cell">
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

        <el-table-column label="类型" width="150">
          <template #default="{ row }">
            <span class="admin-table-muted">{{ row.weaponType || '未设置' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="口径" width="140">
          <template #default="{ row }">
            <span class="admin-table-muted">{{ row.caliber || '未设置' }}</span>
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
                :loading="actionWeaponId === row.id"
                @click="toggleWeapon(row)"
              >
                {{ row.status === 'ENABLED' ? '停用' : '启用' }}
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="!loading && weaponsPage.records.length === 0" class="admin-table-empty">
        当前没有符合条件的武器
      </div>

      <el-pagination
        v-if="weaponsPage.pages > 1"
        class="board-pagination"
        background
        layout="prev, pager, next"
        :current-page="weaponsPage.page"
        :page-size="weaponsPage.size"
        :total="weaponsPage.total"
        @current-change="loadWeapons"
      />
    </section>

    <el-dialog
      v-model="editDialogVisible"
      title="编辑武器"
      width="580px"
      class="profile-edit-dialog"
    >
      <el-form
        ref="weaponFormRef"
        :model="weaponForm"
        :rules="weaponRules"
        label-position="top"
      >
        <el-form-item label="英文名" prop="nameEn">
          <el-input v-model="weaponForm.nameEn" maxlength="120" show-word-limit />
        </el-form-item>
        <el-form-item label="中文名" prop="nameZh">
          <el-input v-model="weaponForm.nameZh" maxlength="120" show-word-limit />
        </el-form-item>
        <el-form-item label="武器类型" prop="weaponType">
          <el-input v-model="weaponForm.weaponType" maxlength="60" show-word-limit />
        </el-form-item>
        <el-form-item label="口径" prop="caliber">
          <el-input v-model="weaponForm.caliber" maxlength="60" show-word-limit />
        </el-form-item>
        <el-form-item label="图片地址" prop="imageUrl">
          <el-input
            v-model="weaponForm.imageUrl"
            maxlength="500"
            show-word-limit
            placeholder="https://..."
          />
        </el-form-item>
        <div v-if="weaponForm.imageUrl" class="admin-media-preview">
          <img :src="weaponForm.imageUrl" alt="图片预览" />
        </div>
        <el-form-item label="武器说明" prop="description">
          <el-input
            v-model="weaponForm.description"
            type="textarea"
            :rows="4"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="weaponForm.status">
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
          <el-button type="primary" :loading="saving" @click="submitWeapon">
            保存武器
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>
