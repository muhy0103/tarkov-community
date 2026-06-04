<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { ChatLineRound, Refresh, Search, Warning } from '@element-plus/icons-vue'
import { fetchAdminComments, reviewAdminComment } from '../api/adminApi'
import { useUserStore } from '../stores/userStore'

const userStore = useUserStore()

const loading = ref(false)
const actionCommentId = ref(null)
const errorMessage = ref('')
const filters = ref({
  status: '',
  keyword: '',
})
const commentsPage = ref({
  page: 1,
  size: 10,
  total: 0,
  pages: 0,
  records: [],
})

const statusOptions = [
  { label: '正常', value: 'NORMAL' },
  { label: '隐藏', value: 'HIDDEN' },
  { label: '删除', value: 'DELETED' },
]

function resolveError(error, fallback) {
  return error?.response?.data?.message || error?.message || fallback
}

function statusLabel(status) {
  return statusOptions.find((item) => item.value === status)?.label || status || '未知'
}

function statusType(status) {
  if (status === 'NORMAL') {
    return 'success'
  }
  if (status === 'DELETED') {
    return 'danger'
  }
  return 'warning'
}

function formatDate(value) {
  if (!value) {
    return '时间未知'
  }

  return new Date(value).toLocaleString('zh-CN', {
    hour12: false,
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

async function loadComments(page = commentsPage.value.page) {
  loading.value = true
  errorMessage.value = ''
  commentsPage.value.page = page

  try {
    commentsPage.value = await fetchAdminComments({
      page,
      size: commentsPage.value.size,
      status: filters.value.status || undefined,
      keyword: filters.value.keyword.trim() || undefined,
    })
  } catch (error) {
    errorMessage.value = resolveError(error, '评论审核列表暂时无法加载')
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  filters.value = {
    status: '',
    keyword: '',
  }
  loadComments(1)
}

async function updateComment(row, payload) {
  actionCommentId.value = row.id

  try {
    const updated = await reviewAdminComment(row.id, payload)
    Object.assign(row, updated)
    ElMessage.success('评论状态已更新')
  } catch (error) {
    ElMessage.error(resolveError(error, '评论审核失败'))
  } finally {
    actionCommentId.value = null
  }
}

onMounted(() => loadComments(1))
</script>

<template>
  <div class="admin-review-view">
    <section class="admin-hero">
      <div>
        <h2>评论审核</h2>
        <p>集中处理帖子下的评论内容，快速隐藏违规回复或恢复误处理评论。</p>
      </div>
      <el-button :icon="Refresh" :loading="loading" @click="loadComments()">
        刷新评论
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
          placeholder="搜索评论内容"
          clearable
          @keyup.enter="loadComments(1)"
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
        <el-button type="primary" :icon="Search" @click="loadComments(1)">
          查询
        </el-button>
        <el-button :icon="Refresh" @click="resetFilters">
          重置
        </el-button>
      </div>
    </section>

    <section class="admin-table-card">
      <el-table v-loading="loading" :data="commentsPage.records" row-key="id">
        <el-table-column label="评论内容" min-width="360">
          <template #default="{ row }">
            <div class="admin-comment-cell">
              <ChatLineRound />
              <div>
                <strong>{{ row.content }}</strong>
                <span>{{ row.authorNickname }} · 所属帖子：{{ row.postTitle }}</span>
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

        <el-table-column label="互动" width="100">
          <template #default="{ row }">
            <span class="admin-table-muted">赞 {{ row.likeCount || 0 }}</span>
          </template>
        </el-table-column>

        <el-table-column label="时间" width="150">
          <template #default="{ row }">
            <span class="admin-table-muted">{{ formatDate(row.createdAt) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <div class="admin-row-actions">
              <el-button
                v-if="row.status === 'NORMAL'"
                size="small"
                :icon="Warning"
                :loading="actionCommentId === row.id"
                @click="updateComment(row, { status: 'HIDDEN' })"
              >
                隐藏
              </el-button>
              <el-button
                v-else
                size="small"
                :loading="actionCommentId === row.id"
                @click="updateComment(row, { status: 'NORMAL' })"
              >
                恢复
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="!loading && commentsPage.records.length === 0" class="admin-table-empty">
        当前没有符合条件的评论
      </div>

      <el-pagination
        v-if="commentsPage.pages > 1"
        class="board-pagination"
        background
        layout="prev, pager, next"
        :current-page="commentsPage.page"
        :page-size="commentsPage.size"
        :total="commentsPage.total"
        @current-change="loadComments"
      />
    </section>
  </div>
</template>
