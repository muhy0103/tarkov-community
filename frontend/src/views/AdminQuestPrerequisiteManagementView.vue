<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Document, EditPen, Refresh, Search } from '@element-plus/icons-vue'
import {
  fetchAdminQuestPrerequisites,
  fetchAdminQuests,
  updateAdminQuestPrerequisite,
} from '../api/adminApi'
import { useUserStore } from '../stores/userStore'

const userStore = useUserStore()

const loading = ref(false)
const saving = ref(false)
const errorMessage = ref('')
const editDialogVisible = ref(false)
const prerequisiteFormRef = ref(null)
const editingRow = ref(null)
const questOptions = ref([])
const filters = ref({
  questId: null,
  prerequisiteQuestId: null,
  keyword: '',
})
const prerequisiteForm = ref({
  questId: null,
  prerequisiteQuestId: null,
})
const prerequisitesPage = ref({
  page: 1,
  size: 10,
  total: 0,
  pages: 0,
  records: [],
})

const prerequisiteRules = {
  questId: [
    { required: true, message: '请选择当前任务', trigger: 'change' },
  ],
  prerequisiteQuestId: [
    { required: true, message: '请选择前置任务', trigger: 'change' },
    { validator: validateDifferentQuest, trigger: 'change' },
  ],
}

function resolveError(error, fallback) {
  return error?.response?.data?.message || error?.message || fallback
}

function questLabel(quest) {
  if (!quest) {
    return ''
  }
  return quest.nameZh ? `${quest.nameZh} / ${quest.nameEn}` : quest.nameEn
}

function relationSummary(row) {
  return `${row.prerequisiteQuestName || '未设置'} -> ${row.questName || '未设置'}`
}

function validateDifferentQuest(rule, value, callback) {
  if (value && prerequisiteForm.value.questId && value === prerequisiteForm.value.questId) {
    callback(new Error('前置任务不能与当前任务相同'))
    return
  }
  callback()
}

async function loadOptions() {
  try {
    const quests = await fetchAdminQuests({ page: 1, size: 50, status: 'ENABLED' })
    questOptions.value = quests.records || []
  } catch (error) {
    ElMessage.warning(resolveError(error, '任务选项暂时无法加载'))
  }
}

async function loadPrerequisites(page = prerequisitesPage.value.page) {
  loading.value = true
  errorMessage.value = ''
  prerequisitesPage.value.page = page

  try {
    prerequisitesPage.value = await fetchAdminQuestPrerequisites({
      page,
      size: prerequisitesPage.value.size,
      questId: filters.value.questId || undefined,
      prerequisiteQuestId: filters.value.prerequisiteQuestId || undefined,
      keyword: filters.value.keyword.trim() || undefined,
    })
  } catch (error) {
    errorMessage.value = resolveError(error, '任务链列表暂时无法加载')
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  filters.value = {
    questId: null,
    prerequisiteQuestId: null,
    keyword: '',
  }
  loadPrerequisites(1)
}

function openEditDialog(row) {
  editingRow.value = row
  prerequisiteForm.value = {
    questId: row.questId,
    prerequisiteQuestId: row.prerequisiteQuestId,
  }
  editDialogVisible.value = true
}

async function submitPrerequisite() {
  const valid = await prerequisiteFormRef.value?.validate().catch(() => false)

  if (!valid || !editingRow.value) {
    return
  }

  saving.value = true
  try {
    const updated = await updateAdminQuestPrerequisite(editingRow.value.id, {
      questId: prerequisiteForm.value.questId,
      prerequisiteQuestId: prerequisiteForm.value.prerequisiteQuestId,
    })
    Object.assign(editingRow.value, updated)
    ElMessage.success('任务链关系已更新')
    editDialogVisible.value = false
  } catch (error) {
    ElMessage.error(resolveError(error, '任务链关系保存失败'))
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadOptions()
  loadPrerequisites(1)
})
</script>

<template>
  <div class="admin-review-view">
    <section class="admin-hero">
      <div>
        <h2>任务链管理</h2>
        <p>维护任务前置关系，让玩家能围绕接取顺序、解锁路线和任务规划组织更清晰的社区讨论。</p>
      </div>
      <el-button :icon="Refresh" :loading="loading" @click="loadPrerequisites()">
        刷新任务链
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
          placeholder="搜索任务名或前置任务名"
          clearable
          @keyup.enter="loadPrerequisites(1)"
        />
        <el-select v-model="filters.questId" placeholder="全部任务" clearable filterable>
          <el-option
            v-for="quest in questOptions"
            :key="quest.id"
            :label="questLabel(quest)"
            :value="quest.id"
          />
        </el-select>
        <el-select
          v-model="filters.prerequisiteQuestId"
          placeholder="全部前置任务"
          clearable
          filterable
        >
          <el-option
            v-for="quest in questOptions"
            :key="quest.id"
            :label="questLabel(quest)"
            :value="quest.id"
          />
        </el-select>
      </div>

      <div class="board-actions">
        <el-button type="primary" :icon="Search" @click="loadPrerequisites(1)">
          查询
        </el-button>
        <el-button :icon="Refresh" @click="resetFilters">
          重置
        </el-button>
      </div>
    </section>

    <section class="admin-table-card">
      <el-table v-loading="loading" :data="prerequisitesPage.records" row-key="id">
        <el-table-column label="任务链" min-width="360">
          <template #default="{ row }">
            <div class="admin-chain-cell">
              <Document />
              <div>
                <strong>{{ relationSummary(row) }}</strong>
                <span>完成前置任务后推进当前任务</span>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="当前任务" width="230">
          <template #default="{ row }">
            <span class="admin-table-muted">{{ row.questName || '未设置' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="前置任务" width="230">
          <template #default="{ row }">
            <span class="admin-table-muted">{{ row.prerequisiteQuestName || '未设置' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="关系" width="150">
          <template #default>
            <el-tag type="success" effect="plain">
              前置解锁
            </el-tag>
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

      <div v-if="!loading && prerequisitesPage.records.length === 0" class="admin-table-empty">
        当前没有符合条件的任务链关系
      </div>

      <el-pagination
        v-if="prerequisitesPage.pages > 1"
        class="board-pagination"
        background
        layout="prev, pager, next"
        :current-page="prerequisitesPage.page"
        :page-size="prerequisitesPage.size"
        :total="prerequisitesPage.total"
        @current-change="loadPrerequisites"
      />
    </section>

    <el-dialog
      v-model="editDialogVisible"
      title="编辑任务链关系"
      width="620px"
      class="profile-edit-dialog"
    >
      <el-form
        ref="prerequisiteFormRef"
        :model="prerequisiteForm"
        :rules="prerequisiteRules"
        label-position="top"
      >
        <el-form-item label="当前任务" prop="questId">
          <el-select v-model="prerequisiteForm.questId" filterable>
            <el-option
              v-for="quest in questOptions"
              :key="quest.id"
              :label="questLabel(quest)"
              :value="quest.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="前置任务" prop="prerequisiteQuestId">
          <el-select v-model="prerequisiteForm.prerequisiteQuestId" filterable>
            <el-option
              v-for="quest in questOptions"
              :key="quest.id"
              :label="questLabel(quest)"
              :value="quest.id"
            />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="profile-dialog-footer">
          <el-button @click="editDialogVisible = false">
            取消
          </el-button>
          <el-button type="primary" :loading="saving" @click="submitPrerequisite">
            保存任务链
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>
