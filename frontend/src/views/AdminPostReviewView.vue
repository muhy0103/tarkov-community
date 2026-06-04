<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  ChatLineRound,
  Refresh,
  Search,
  Star,
  StarFilled,
  Top,
  Warning,
} from '@element-plus/icons-vue'
import { fetchCategories } from '../api/catalogApi'
import { fetchAdminPosts, reviewAdminPost } from '../api/adminApi'
import { useUserStore } from '../stores/userStore'

const userStore = useUserStore()

const loading = ref(false)
const actionPostId = ref(null)
const errorMessage = ref('')
const categories = ref([])
const filters = ref({
  status: '',
  categoryCode: '',
  keyword: '',
})
const postsPage = ref({
  page: 1,
  size: 10,
  total: 0,
  pages: 0,
  records: [],
})

const statusOptions = [
  { label: '正常', value: 'NORMAL' },
  { label: '隐藏', value: 'HIDDEN' },
  { label: '待审', value: 'PENDING' },
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
  if (status === 'HIDDEN') {
    return 'warning'
  }
  if (status === 'DELETED') {
    return 'danger'
  }
  return 'info'
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

async function loadPosts(page = postsPage.value.page) {
  loading.value = true
  errorMessage.value = ''
  postsPage.value.page = page

  try {
    const [categoryData, postData] = await Promise.all([
      categories.value.length ? Promise.resolve(categories.value) : fetchCategories(),
      fetchAdminPosts({
        page,
        size: postsPage.value.size,
        status: filters.value.status || undefined,
        categoryCode: filters.value.categoryCode || undefined,
        keyword: filters.value.keyword.trim() || undefined,
      }),
    ])
    categories.value = categoryData
    postsPage.value = postData
  } catch (error) {
    errorMessage.value = resolveError(error, '帖子审核列表暂时无法加载')
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  filters.value = {
    status: '',
    categoryCode: '',
    keyword: '',
  }
  loadPosts(1)
}

async function updatePost(row, payload) {
  actionPostId.value = row.id

  try {
    const updated = await reviewAdminPost(row.id, payload)
    Object.assign(row, updated)
    ElMessage.success('审核状态已更新')
  } catch (error) {
    ElMessage.error(resolveError(error, '审核操作失败'))
  } finally {
    actionPostId.value = null
  }
}

onMounted(() => loadPosts(1))
</script>

<template>
  <div class="admin-review-view">
    <section class="admin-hero">
      <div>
        <h2>帖子审核</h2>
        <p>集中查看玩家发布的情报帖，快速处理隐藏、恢复、推荐和置顶。</p>
      </div>
      <el-button :icon="Refresh" :loading="loading" @click="loadPosts()">
        刷新列表
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
          placeholder="搜索标题或正文"
          clearable
          @keyup.enter="loadPosts(1)"
        />
        <el-select v-model="filters.status" placeholder="全部状态" clearable>
          <el-option
            v-for="option in statusOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
        <el-select v-model="filters.categoryCode" placeholder="全部分区" clearable>
          <el-option
            v-for="category in categories"
            :key="category.id"
            :label="category.name"
            :value="category.code"
          />
        </el-select>
      </div>

      <div class="board-actions">
        <el-button type="primary" :icon="Search" @click="loadPosts(1)">
          查询
        </el-button>
        <el-button :icon="Refresh" @click="resetFilters">
          重置
        </el-button>
      </div>
    </section>

    <section class="admin-table-card">
      <el-table v-loading="loading" :data="postsPage.records" row-key="id">
        <el-table-column label="帖子" min-width="280">
          <template #default="{ row }">
            <div class="admin-post-cell">
              <ChatLineRound />
              <div>
                <RouterLink
                  class="post-title-link"
                  :to="{ name: 'post-detail', params: { id: row.id } }"
                >
                  <strong>{{ row.title }}</strong>
                </RouterLink>
                <span>{{ row.authorNickname }} · {{ row.categoryName }} · {{ row.postType }}</span>
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

        <el-table-column label="标记" width="150">
          <template #default="{ row }">
            <div class="admin-marker-tags">
              <el-tag v-if="row.recommended" type="success" effect="plain">推荐</el-tag>
              <el-tag v-if="row.pinned" type="warning" effect="plain">置顶</el-tag>
              <span v-if="!row.recommended && !row.pinned">无</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="互动" width="150">
          <template #default="{ row }">
            <span class="admin-table-muted">
              赞 {{ row.likeCount }} · 评 {{ row.commentCount }}
            </span>
          </template>
        </el-table-column>

        <el-table-column label="时间" width="150">
          <template #default="{ row }">
            <span class="admin-table-muted">{{ formatDate(row.createdAt) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="310" fixed="right">
          <template #default="{ row }">
            <div class="admin-row-actions">
              <el-button
                v-if="row.status === 'NORMAL'"
                size="small"
                :icon="Warning"
                :loading="actionPostId === row.id"
                @click="updatePost(row, { status: 'HIDDEN' })"
              >
                隐藏
              </el-button>
              <el-button
                v-else
                size="small"
                :loading="actionPostId === row.id"
                @click="updatePost(row, { status: 'NORMAL' })"
              >
                恢复
              </el-button>
              <el-button
                size="small"
                :icon="row.recommended ? StarFilled : Star"
                :loading="actionPostId === row.id"
                @click="updatePost(row, { recommended: !row.recommended })"
              >
                {{ row.recommended ? '取消推荐' : '推荐' }}
              </el-button>
              <el-button
                size="small"
                :icon="Top"
                :loading="actionPostId === row.id"
                @click="updatePost(row, { pinned: !row.pinned })"
              >
                {{ row.pinned ? '取消置顶' : '置顶' }}
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="!loading && postsPage.records.length === 0" class="admin-table-empty">
        当前没有符合条件的帖子
      </div>

      <el-pagination
        v-if="postsPage.pages > 1"
        class="board-pagination"
        background
        layout="prev, pager, next"
        :current-page="postsPage.page"
        :page-size="postsPage.size"
        :total="postsPage.total"
        @current-change="loadPosts"
      />
    </section>
  </div>
</template>
