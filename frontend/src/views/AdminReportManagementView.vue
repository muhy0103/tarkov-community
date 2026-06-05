<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { ChatLineRound, EditPen, Refresh, Search, Warning } from '@element-plus/icons-vue'
import { fetchAdminReports, reviewAdminReport } from '../api/adminApi'
import { useUserStore } from '../stores/userStore'

const userStore = useUserStore()

const loading = ref(false)
const saving = ref(false)
const actionReportId = ref(null)
const errorMessage = ref('')
const reviewDialogVisible = ref(false)
const reviewFormRef = ref(null)
const editingRow = ref(null)
const filters = ref({
  status: '',
  targetType: '',
  keyword: '',
})
const reviewForm = ref(emptyReviewForm())
const reportsPage = ref({
  page: 1,
  size: 10,
  total: 0,
  pages: 0,
  records: [],
})

const statusOptions = [
  { label: '待处理', value: 'PENDING' },
  { label: '已解决', value: 'RESOLVED' },
  { label: '已驳回', value: 'REJECTED' },
]

const targetTypeOptions = [
  { label: '帖子', value: 'POST' },
  { label: '评论', value: 'COMMENT' },
]

const reviewRules = {
  status: [
    { required: true, message: '请选择处理状态', trigger: 'change' },
  ],
  handleResult: [
    { max: 500, message: '处理说明不能超过 500 个字符', trigger: 'blur' },
  ],
}

const dialogTitle = computed(() => {
  if (!editingRow.value) {
    return '处理举报'
  }
  return `处理举报 #${editingRow.value.id}`
})

function emptyReviewForm() {
  return {
    status: 'RESOLVED',
    handleResult: '',
  }
}

function resolveError(error, fallback) {
  return error?.response?.data?.message || error?.message || fallback
}

function statusLabel(status) {
  return statusOptions.find((item) => item.value === status)?.label || status || '未知'
}

function statusType(status) {
  if (status === 'RESOLVED') {
    return 'success'
  }

  if (status === 'REJECTED') {
    return 'info'
  }

  return 'warning'
}

function targetTypeLabel(targetType) {
  return targetTypeOptions.find((item) => item.value === targetType)?.label || targetType || '未知'
}

function formatDate(value) {
  if (!value) {
    return '未记录'
  }

  return String(value).replace('T', ' ').slice(0, 16)
}

async function loadReports(page = reportsPage.value.page) {
  loading.value = true
  errorMessage.value = ''
  reportsPage.value.page = page

  try {
    reportsPage.value = await fetchAdminReports({
      page,
      size: reportsPage.value.size,
      status: filters.value.status || undefined,
      targetType: filters.value.targetType || undefined,
      keyword: filters.value.keyword.trim() || undefined,
    })
  } catch (error) {
    errorMessage.value = resolveError(error, '举报列表暂时无法加载')
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  filters.value = {
    status: '',
    targetType: '',
    keyword: '',
  }
  loadReports(1)
}

function openReviewDialog(row) {
  editingRow.value = row
  reviewForm.value = {
    status: row.status === 'PENDING' ? 'RESOLVED' : row.status,
    handleResult: row.handleResult || '',
  }
  reviewDialogVisible.value = true
}

async function submitReview() {
  const valid = await reviewFormRef.value?.validate().catch(() => false)

  if (!valid || !editingRow.value) {
    return
  }

  saving.value = true
  const payload = {
    status: reviewForm.value.status,
    handleResult: reviewForm.value.handleResult.trim(),
  }

  try {
    const updated = await reviewAdminReport(editingRow.value.id, payload)
    Object.assign(editingRow.value, updated)
    ElMessage.success('举报处理结果已保存')
    reviewDialogVisible.value = false
    if (filters.value.status && filters.value.status !== updated.status) {
      loadReports(1)
    }
  } catch (error) {
    ElMessage.error(resolveError(error, '举报处理失败'))
  } finally {
    saving.value = false
  }
}

async function quickReview(row, status) {
  actionReportId.value = row.id
  const handleResult = status === 'RESOLVED'
    ? '已核查并完成处理。'
    : '经核查暂未发现违规内容。'

  try {
    const updated = await reviewAdminReport(row.id, {
      status,
      handleResult,
    })
    Object.assign(row, updated)
    ElMessage.success(status === 'RESOLVED' ? '举报已标记解决' : '举报已驳回')
    if (filters.value.status && filters.value.status !== status) {
      loadReports(1)
    }
  } catch (error) {
    ElMessage.error(resolveError(error, '举报处理失败'))
  } finally {
    actionReportId.value = null
  }
}

onMounted(() => loadReports(1))
</script>

<template>
  <div class="admin-review-view">
    <section class="admin-hero">
      <div>
        <h2>举报管理</h2>
        <p>集中查看玩家提交的帖子和评论举报，按原因、类型和处理状态快速筛选并记录处理结果。</p>
      </div>
      <el-button :icon="Refresh" :loading="loading" @click="loadReports()">
        刷新举报
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
          placeholder="搜索举报原因或说明"
          clearable
          @keyup.enter="loadReports(1)"
        />
        <el-select v-model="filters.status" placeholder="全部状态" clearable>
          <el-option
            v-for="option in statusOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
        <el-select v-model="filters.targetType" placeholder="全部类型" clearable>
          <el-option
            v-for="option in targetTypeOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
      </div>

      <div class="board-actions">
        <el-button type="primary" :icon="Search" @click="loadReports(1)">
          查询
        </el-button>
        <el-button :icon="Refresh" @click="resetFilters">
          重置
        </el-button>
      </div>
    </section>

    <section class="admin-table-card">
      <el-table v-loading="loading" :data="reportsPage.records" row-key="id">
        <el-table-column label="举报目标" min-width="320">
          <template #default="{ row }">
            <div class="admin-report-cell">
              <ChatLineRound />
              <div>
                <RouterLink
                  v-if="row.targetType === 'POST'"
                  class="post-title-link"
                  :to="{ name: 'post-detail', params: { id: row.targetId } }"
                >
                  <strong>{{ row.targetTitle }}</strong>
                </RouterLink>
                <strong v-else>{{ row.targetTitle }}</strong>
                <span>{{ targetTypeLabel(row.targetType) }} #{{ row.targetId }} · {{ row.targetSummary || '暂无摘要' }}</span>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="举报信息" min-width="260">
          <template #default="{ row }">
            <div class="admin-report-reason">
              <strong>{{ row.reason }}</strong>
              <span>{{ row.reporterNickname }} · {{ row.description || '无补充说明' }}</span>
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

        <el-table-column label="处理结果" min-width="220">
          <template #default="{ row }">
            <div class="admin-report-result">
              <strong>{{ row.handlerNickname || '未处理' }}</strong>
              <span>{{ row.handleResult || '等待管理员处理' }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="时间" width="170">
          <template #default="{ row }">
            <div class="admin-time-stack">
              <span>提交 {{ formatDate(row.createdAt) }}</span>
              <span>处理 {{ formatDate(row.handledAt) }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <div class="admin-row-actions">
              <el-button size="small" :icon="EditPen" @click="openReviewDialog(row)">
                处理
              </el-button>
              <el-button
                v-if="row.status === 'PENDING'"
                size="small"
                :icon="Warning"
                :loading="actionReportId === row.id"
                @click="quickReview(row, 'RESOLVED')"
              >
                解决
              </el-button>
              <el-button
                v-if="row.status === 'PENDING'"
                size="small"
                :loading="actionReportId === row.id"
                @click="quickReview(row, 'REJECTED')"
              >
                驳回
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="!loading && reportsPage.records.length === 0" class="admin-table-empty">
        当前没有符合条件的举报
      </div>

      <el-pagination
        v-if="reportsPage.pages > 1"
        class="board-pagination"
        background
        layout="prev, pager, next"
        :current-page="reportsPage.page"
        :page-size="reportsPage.size"
        :total="reportsPage.total"
        @current-change="loadReports"
      />
    </section>

    <el-dialog
      v-model="reviewDialogVisible"
      :title="dialogTitle"
      width="min(560px, calc(100vw - 32px))"
      class="profile-edit-dialog"
    >
      <el-form
        ref="reviewFormRef"
        :model="reviewForm"
        :rules="reviewRules"
        label-position="top"
      >
        <el-form-item label="处理状态" prop="status">
          <el-select v-model="reviewForm.status">
            <el-option
              v-for="option in statusOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="处理说明" prop="handleResult">
          <el-input
            v-model="reviewForm.handleResult"
            type="textarea"
            :rows="5"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="profile-dialog-footer">
          <el-button @click="reviewDialogVisible = false">
            取消
          </el-button>
          <el-button type="primary" :loading="saving" @click="submitReview">
            保存处理结果
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>
