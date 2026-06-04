<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import {
  ChatLineRound,
  Collection,
  EditPen,
  Refresh,
  Star,
  User,
} from '@element-plus/icons-vue'
import {
  fetchMyComments,
  fetchMyFavorites,
  fetchMyPosts,
  fetchUserCenterSummary,
  updateMyProfile,
} from '../api/userCenterApi'
import { useUserStore } from '../stores/userStore'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const sectionLoading = ref('')
const errorMessage = ref('')
const activeTab = ref('posts')
const profileDialogVisible = ref(false)
const profileSaving = ref(false)
const profileFormRef = ref(null)
const profileForm = ref({
  nickname: '',
  email: '',
  avatar: '',
})
const summary = ref({
  id: null,
  username: '',
  nickname: '',
  email: '',
  avatar: '',
  role: '',
  status: '',
  contribution: 0,
  postCount: 0,
  commentCount: 0,
  favoriteCount: 0,
  createdAt: '',
})
const postsPage = ref(pageState())
const commentsPage = ref(pageState())
const favoritesPage = ref(pageState())

const playerName = computed(() => summary.value.nickname || userStore.userInfo?.nickname || '塔科夫玩家')
const playerAvatar = computed(() => summary.value.avatar || userStore.userInfo?.avatar || '')
const userInitial = computed(() => playerName.value.slice(0, 1).toUpperCase())

const profileRules = {
  nickname: [
    { required: true, message: '请填写玩家昵称', trigger: 'blur' },
    { max: 50, message: '昵称不能超过 50 个字符', trigger: 'blur' },
  ],
  email: [
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' },
    { max: 120, message: '邮箱不能超过 120 个字符', trigger: 'blur' },
  ],
  avatar: [
    { max: 500, message: '头像链接不能超过 500 个字符', trigger: 'blur' },
  ],
}

const summaryStats = computed(() => [
  { label: '贡献值', value: summary.value.contribution || 0, icon: User },
  { label: '我的帖子', value: summary.value.postCount || 0, icon: ChatLineRound },
  { label: '我的评论', value: summary.value.commentCount || 0, icon: Collection },
  { label: '我的收藏', value: summary.value.favoriteCount || 0, icon: Star },
])

const postTypeOptions = {
  ROUTE: '路线情报',
  ROUTE_GUIDE: '路线情报',
  GUIDE: '任务攻略',
  QUEST_GUIDE: '任务攻略',
  LOADOUT: '装备配装',
  QUESTION: '问题求助',
  MARKET: '市场讨论',
  TEAM_UP: '组队招募',
}

function pageState() {
  return {
    page: 1,
    size: 6,
    total: 0,
    pages: 0,
    records: [],
  }
}

function resolveError(error, fallback) {
  return error?.response?.data?.message || error?.message || fallback
}

function syncUserInfo(summaryData) {
  if (!summaryData || !userStore.token) {
    return
  }

  userStore.setAuth(userStore.token, {
    ...userStore.userInfo,
    id: summaryData.id,
    username: summaryData.username,
    nickname: summaryData.nickname,
    email: summaryData.email,
    avatar: summaryData.avatar,
    role: summaryData.role,
    status: summaryData.status,
    contribution: summaryData.contribution,
  })
}

function postTypeLabel(type) {
  return postTypeOptions[type] || type || '普通讨论'
}

function statusLabel(status) {
  if (status === 'NORMAL') {
    return '正常'
  }
  if (status === 'HIDDEN') {
    return '已隐藏'
  }
  if (status === 'PENDING') {
    return '待处理'
  }
  return status || '未知'
}

function statusType(status) {
  if (status === 'NORMAL') {
    return 'success'
  }
  if (status === 'HIDDEN') {
    return 'warning'
  }
  return 'info'
}

function formatDate(value) {
  if (!value) {
    return '暂无记录'
  }

  return new Date(value).toLocaleString('zh-CN', {
    hour12: false,
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

async function loadAll() {
  loading.value = true
  errorMessage.value = ''

  try {
    const [summaryData, postData, commentData, favoriteData] = await Promise.all([
      fetchUserCenterSummary(),
      fetchMyPosts({ page: postsPage.value.page, size: postsPage.value.size }),
      fetchMyComments({ page: commentsPage.value.page, size: commentsPage.value.size }),
      fetchMyFavorites({ page: favoritesPage.value.page, size: favoritesPage.value.size }),
    ])

    summary.value = summaryData
    syncUserInfo(summaryData)
    postsPage.value = postData
    commentsPage.value = commentData
    favoritesPage.value = favoriteData
  } catch (error) {
    errorMessage.value = resolveError(error, '用户中心暂时无法加载')
  } finally {
    loading.value = false
  }
}

async function loadPosts(page = postsPage.value.page) {
  sectionLoading.value = 'posts'
  postsPage.value.page = page

  try {
    postsPage.value = await fetchMyPosts({ page, size: postsPage.value.size })
  } catch (error) {
    errorMessage.value = resolveError(error, '我的帖子暂时无法加载')
  } finally {
    sectionLoading.value = ''
  }
}

async function loadComments(page = commentsPage.value.page) {
  sectionLoading.value = 'comments'
  commentsPage.value.page = page

  try {
    commentsPage.value = await fetchMyComments({ page, size: commentsPage.value.size })
  } catch (error) {
    errorMessage.value = resolveError(error, '我的评论暂时无法加载')
  } finally {
    sectionLoading.value = ''
  }
}

async function loadFavorites(page = favoritesPage.value.page) {
  sectionLoading.value = 'favorites'
  favoritesPage.value.page = page

  try {
    favoritesPage.value = await fetchMyFavorites({ page, size: favoritesPage.value.size })
  } catch (error) {
    errorMessage.value = resolveError(error, '我的收藏暂时无法加载')
  } finally {
    sectionLoading.value = ''
  }
}

function openProfileDialog() {
  profileForm.value = {
    nickname: summary.value.nickname || userStore.userInfo?.nickname || '',
    email: summary.value.email || userStore.userInfo?.email || '',
    avatar: summary.value.avatar || userStore.userInfo?.avatar || '',
  }
  profileDialogVisible.value = true
}

async function submitProfile() {
  const valid = await profileFormRef.value?.validate().catch(() => false)

  if (!valid) {
    return
  }

  profileSaving.value = true
  try {
    const updatedSummary = await updateMyProfile({
      nickname: profileForm.value.nickname.trim(),
      email: profileForm.value.email.trim(),
      avatar: profileForm.value.avatar.trim(),
    })

    summary.value = {
      ...summary.value,
      ...updatedSummary,
    }
    syncUserInfo(updatedSummary)
    ElMessage.success('个人资料已更新')
    profileDialogVisible.value = false
  } catch (error) {
    ElMessage.error(resolveError(error, '个人资料暂时无法保存'))
  } finally {
    profileSaving.value = false
  }
}

function goCreatePost() {
  router.push({ name: 'post-create' })
}

onMounted(loadAll)
</script>

<template>
  <div class="user-center-view">
    <section class="user-center-hero">
      <div class="user-profile-main">
        <div class="user-avatar">
          <img v-if="playerAvatar" :src="playerAvatar" alt="玩家头像" />
          <span v-else>{{ userInitial }}</span>
        </div>
        <div>
          <h2>{{ playerName }}</h2>
          <p>{{ summary.username || userStore.userInfo?.username }} · {{ summary.email || '未填写邮箱' }}</p>
          <span>加入时间 {{ formatDate(summary.createdAt) }}</span>
        </div>
      </div>
      <div class="user-center-actions">
        <el-button :icon="User" @click="openProfileDialog">
          编辑资料
        </el-button>
        <el-button :icon="Refresh" :loading="loading" @click="loadAll">
          刷新
        </el-button>
        <el-button type="primary" :icon="EditPen" @click="goCreatePost">
          发布情报
        </el-button>
      </div>
    </section>

    <el-alert
      v-if="errorMessage"
      :title="errorMessage"
      type="warning"
      show-icon
      class="home-alert"
    />

    <el-skeleton v-if="loading" :rows="8" animated class="home-skeleton" />

    <template v-else>
      <section class="user-stat-grid">
        <article v-for="item in summaryStats" :key="item.label" class="user-stat-card">
          <component :is="item.icon" />
          <div>
            <strong>{{ item.value }}</strong>
            <span>{{ item.label }}</span>
          </div>
        </article>
      </section>

      <section class="user-content-panel">
        <el-tabs v-model="activeTab" class="user-tabs">
          <el-tab-pane label="我的帖子" name="posts">
            <div v-loading="sectionLoading === 'posts'" class="user-list-wrap">
              <div v-if="postsPage.records.length" class="post-list">
                <article v-for="post in postsPage.records" :key="post.id" class="post-item">
                  <div class="post-icon">
                    <ChatLineRound />
                  </div>
                  <div class="post-body">
                    <div class="post-meta">
                      <el-tag size="small" effect="plain">{{ post.categoryName }}</el-tag>
                      <span>{{ postTypeLabel(post.postType) }}</span>
                      <span>{{ formatDate(post.createdAt) }}</span>
                    </div>
                    <RouterLink class="post-title-link" :to="{ name: 'post-detail', params: { id: post.id } }">
                      <h4>{{ post.title }}</h4>
                    </RouterLink>
                    <p>{{ post.summary }}</p>
                    <div class="post-counts">
                      <span>浏览 {{ post.viewCount }}</span>
                      <span>点赞 {{ post.likeCount }}</span>
                      <span>评论 {{ post.commentCount }}</span>
                    </div>
                  </div>
                </article>
              </div>

              <div v-else class="post-empty">
                <ChatLineRound />
                <div>
                  <h4>还没有发布过情报帖</h4>
                  <p>可以先从路线、任务、配装或组队需求写一条轻量内容。</p>
                </div>
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
            </div>
          </el-tab-pane>

          <el-tab-pane label="我的评论" name="comments">
            <div v-loading="sectionLoading === 'comments'" class="user-list-wrap">
              <div v-if="commentsPage.records.length" class="comment-list">
                <article v-for="comment in commentsPage.records" :key="comment.id" class="user-comment-item">
                  <div class="post-icon">
                    <Collection />
                  </div>
                  <div>
                    <div class="post-meta">
                      <el-tag :type="statusType(comment.status)" size="small" effect="plain">
                        {{ statusLabel(comment.status) }}
                      </el-tag>
                      <span>点赞 {{ comment.likeCount }}</span>
                      <span>{{ formatDate(comment.createdAt) }}</span>
                    </div>
                    <RouterLink
                      class="post-title-link"
                      :to="{ name: 'post-detail', params: { id: comment.postId } }"
                    >
                      <h4>{{ comment.postTitle }}</h4>
                    </RouterLink>
                    <p>{{ comment.content }}</p>
                  </div>
                </article>
              </div>

              <div v-else class="post-empty">
                <Collection />
                <div>
                  <h4>还没有留下评论</h4>
                  <p>浏览帖子时可以补充路线经验、任务点位或配装反馈。</p>
                </div>
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
            </div>
          </el-tab-pane>

          <el-tab-pane label="我的收藏" name="favorites">
            <div v-loading="sectionLoading === 'favorites'" class="user-list-wrap">
              <div v-if="favoritesPage.records.length" class="post-list">
                <article v-for="post in favoritesPage.records" :key="post.id" class="post-item">
                  <div class="post-icon">
                    <Star />
                  </div>
                  <div class="post-body">
                    <div class="post-meta">
                      <el-tag size="small" effect="plain">{{ post.categoryName }}</el-tag>
                      <span>{{ post.authorNickname }}</span>
                      <span>{{ postTypeLabel(post.postType) }}</span>
                    </div>
                    <RouterLink class="post-title-link" :to="{ name: 'post-detail', params: { id: post.id } }">
                      <h4>{{ post.title }}</h4>
                    </RouterLink>
                    <p>{{ post.summary }}</p>
                    <div class="post-counts">
                      <span>浏览 {{ post.viewCount }}</span>
                      <span>点赞 {{ post.likeCount }}</span>
                      <span>评论 {{ post.commentCount }}</span>
                    </div>
                  </div>
                </article>
              </div>

              <div v-else class="post-empty">
                <Star />
                <div>
                  <h4>还没有收藏内容</h4>
                  <p>收藏常用路线、任务攻略或装备讨论，之后可以从这里快速找回。</p>
                </div>
              </div>

              <el-pagination
                v-if="favoritesPage.pages > 1"
                class="board-pagination"
                background
                layout="prev, pager, next"
                :current-page="favoritesPage.page"
                :page-size="favoritesPage.size"
                :total="favoritesPage.total"
                @current-change="loadFavorites"
              />
            </div>
          </el-tab-pane>
        </el-tabs>
      </section>
    </template>

    <el-dialog
      v-model="profileDialogVisible"
      title="编辑个人资料"
      width="460px"
      class="profile-edit-dialog"
    >
      <el-form
        ref="profileFormRef"
        :model="profileForm"
        :rules="profileRules"
        label-position="top"
      >
        <el-form-item label="玩家昵称" prop="nickname">
          <el-input
            v-model="profileForm.nickname"
            maxlength="50"
            show-word-limit
            placeholder="例如：海岸线侦察员"
          />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="profileForm.email"
            maxlength="120"
            placeholder="用于展示或后续通知，可留空"
          />
        </el-form-item>
        <el-form-item label="头像链接" prop="avatar">
          <el-input
            v-model="profileForm.avatar"
            maxlength="500"
            placeholder="可填写图片 URL，也可以暂时留空"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="profile-dialog-footer">
          <el-button @click="profileDialogVisible = false">
            取消
          </el-button>
          <el-button type="primary" :loading="profileSaving" @click="submitProfile">
            保存资料
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>
