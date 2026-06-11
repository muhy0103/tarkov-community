<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import {
  Bell,
  ChatLineRound,
  CircleCheck,
  Collection,
  EditPen,
  Lock,
  MessageBox,
  Refresh,
  Star,
  User,
  View,
} from '@element-plus/icons-vue'
import {
  fetchMyNotifications,
  fetchMyComments,
  fetchMyFavorites,
  fetchMyPosts,
  fetchUnreadNotificationCount,
  fetchUserCenterSummary,
  markAllNotificationsRead,
  markNotificationRead,
  updateMyPassword,
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
const passwordDialogVisible = ref(false)
const passwordSaving = ref(false)
const passwordFormRef = ref(null)
const profileForm = ref({
  nickname: '',
  email: '',
  avatar: '',
})
const passwordForm = ref({
  currentPassword: '',
  newPassword: '',
  confirmPassword: '',
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
const notificationsPage = ref(pageState(5))
const unreadNotificationCount = ref(0)
const notificationReadFilter = ref('ALL')

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

const passwordRules = {
  currentPassword: [
    { required: true, message: '请填写当前密码', trigger: 'blur' },
    { min: 6, max: 50, message: '当前密码需要在 6 到 50 位之间', trigger: 'blur' },
  ],
  newPassword: [
    { required: true, message: '请填写新密码', trigger: 'blur' },
    { min: 6, max: 50, message: '新密码需要在 6 到 50 位之间', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次填写新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.value.newPassword) {
          callback(new Error('两次新密码不一致'))
          return
        }
        callback()
      },
      trigger: 'blur',
    },
  ],
}

const summaryStats = computed(() => [
  { label: '贡献值', value: summary.value.contribution || 0, icon: User },
  { label: '我的帖子', value: summary.value.postCount || 0, icon: ChatLineRound },
  { label: '我的评论', value: summary.value.commentCount || 0, icon: Collection },
  { label: '我的收藏', value: summary.value.favoriteCount || 0, icon: Star },
  { label: '未读通知', value: unreadNotificationCount.value || 0, icon: Bell },
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

const notificationTypeOptions = {
  REPORT_RESULT: '举报结果',
  POST_COMMENT: '帖子评论',
  COMMENT_REPLY: '评论回复',
  POST_LIKE: '帖子点赞',
  COMMENT_LIKE: '评论点赞',
  SYSTEM: '系统通知',
  ANNOUNCEMENT: '公告提醒',
}

const notificationReadFilterOptions = [
  { label: '全部', value: 'ALL' },
  { label: '未读', value: 'UNREAD' },
]

const postInteractionNotificationTypes = new Set(['POST_COMMENT', 'COMMENT_REPLY', 'POST_LIKE', 'COMMENT_LIKE'])

function pageState(size = 6) {
  return {
    page: 1,
    size,
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

function notificationTypeLabel(type) {
  return notificationTypeOptions[type] || type || '通知'
}

function canOpenRelatedPost(notification) {
  return postInteractionNotificationTypes.has(notification?.type) && Boolean(notification.relatedId)
}

function notificationQueryParams(page) {
  const params = {
    page,
    size: notificationsPage.value.size,
  }

  if (notificationReadFilter.value === 'UNREAD') {
    params.readStatus = 0
  }

  return params
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
    const [summaryData, postData, commentData, favoriteData, notificationData, unreadCount] = await Promise.all([
      fetchUserCenterSummary(),
      fetchMyPosts({ page: postsPage.value.page, size: postsPage.value.size }),
      fetchMyComments({ page: commentsPage.value.page, size: commentsPage.value.size }),
      fetchMyFavorites({ page: favoritesPage.value.page, size: favoritesPage.value.size }),
      fetchMyNotifications(notificationQueryParams(notificationsPage.value.page)),
      fetchUnreadNotificationCount(),
    ])

    summary.value = summaryData
    syncUserInfo(summaryData)
    postsPage.value = postData
    commentsPage.value = commentData
    favoritesPage.value = favoriteData
    notificationsPage.value = notificationData
    unreadNotificationCount.value = unreadCount
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

async function loadNotifications(page = notificationsPage.value.page) {
  sectionLoading.value = 'notifications'
  notificationsPage.value.page = page

  try {
    const [notificationData, unreadCount] = await Promise.all([
      fetchMyNotifications(notificationQueryParams(page)),
      fetchUnreadNotificationCount(),
    ])
    notificationsPage.value = notificationData
    unreadNotificationCount.value = unreadCount
  } catch (error) {
    errorMessage.value = resolveError(error, '通知暂时无法加载')
  } finally {
    sectionLoading.value = ''
  }
}

function handleNotificationFilterChange() {
  loadNotifications(1)
}

async function markOneNotificationRead(notification) {
  if (!notification || notification.readStatus === 1) {
    return
  }

  sectionLoading.value = 'notifications'
  try {
    await markNotificationRead(notification.id)
    if (notificationReadFilter.value === 'UNREAD') {
      await loadNotifications(1)
    } else {
      applyNotificationReadLocally(notification.id)
      unreadNotificationCount.value = Math.max(0, unreadNotificationCount.value - 1)
    }
    ElMessage.success('通知已标记为已读')
  } catch (error) {
    ElMessage.error(resolveError(error, '通知状态暂时无法更新'))
  } finally {
    sectionLoading.value = ''
  }
}

async function openRelatedPost(notification) {
  if (!canOpenRelatedPost(notification)) {
    return
  }

  if (notification.readStatus === 0) {
    try {
      await markNotificationRead(notification.id)
      applyNotificationReadLocally(notification.id)
      unreadNotificationCount.value = Math.max(0, unreadNotificationCount.value - 1)
    } catch (error) {
      ElMessage.warning(resolveError(error, '通知已读状态暂时无法同步'))
    }
  }

  router.push({ name: 'post-detail', params: { id: notification.relatedId } })
}

function applyNotificationReadLocally(notificationId) {
  notificationsPage.value = {
    ...notificationsPage.value,
    records: notificationsPage.value.records.map((item) => (
      item.id === notificationId ? { ...item, readStatus: 1 } : item
    )),
  }
}

async function markAllNotifications() {
  if (unreadNotificationCount.value <= 0) {
    return
  }

  sectionLoading.value = 'notifications'
  try {
    await markAllNotificationsRead()
    if (notificationReadFilter.value === 'UNREAD') {
      notificationsPage.value = pageState(notificationsPage.value.size)
    } else {
      notificationsPage.value = {
        ...notificationsPage.value,
        records: notificationsPage.value.records.map((item) => ({ ...item, readStatus: 1 })),
      }
    }
    unreadNotificationCount.value = 0
    ElMessage.success('所有通知已标记为已读')
  } catch (error) {
    ElMessage.error(resolveError(error, '通知状态暂时无法更新'))
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

function openPasswordDialog() {
  passwordForm.value = {
    currentPassword: '',
    newPassword: '',
    confirmPassword: '',
  }
  passwordDialogVisible.value = true
}

async function submitPassword() {
  const valid = await passwordFormRef.value?.validate().catch(() => false)

  if (!valid) {
    return
  }

  passwordSaving.value = true
  try {
    await updateMyPassword({
      currentPassword: passwordForm.value.currentPassword,
      newPassword: passwordForm.value.newPassword,
    })

    ElMessage.success('密码已更新，请重新登录')
    passwordDialogVisible.value = false
    userStore.clearAuth()
    router.push({
      name: 'login',
      query: {
        redirect: '/me',
      },
    })
  } catch (error) {
    ElMessage.error(resolveError(error, '密码暂时无法修改'))
  } finally {
    passwordSaving.value = false
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
        <el-button :icon="Lock" @click="openPasswordDialog">
          修改密码
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
                      <RouterLink class="inline-action-link" :to="{ name: 'post-edit', params: { id: post.id } }">
                        编辑
                      </RouterLink>
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

          <el-tab-pane name="notifications">
            <template #label>
              <span class="notification-tab-label">
                通知
                <el-badge
                  v-if="unreadNotificationCount > 0"
                  :value="unreadNotificationCount"
                  :max="99"
                  class="notification-tab-badge"
                />
              </span>
            </template>

            <div v-loading="sectionLoading === 'notifications'" class="user-list-wrap">
              <div class="notification-toolbar">
                <div class="notification-toolbar-content">
                  <h4>社区通知</h4>
                  <p>查看举报处理、系统提醒和社区运营消息。</p>
                  <el-segmented
                    v-model="notificationReadFilter"
                    class="notification-filter"
                    :options="notificationReadFilterOptions"
                    @change="handleNotificationFilterChange"
                  />
                </div>
                <el-button
                  size="small"
                  :icon="CircleCheck"
                  :disabled="unreadNotificationCount <= 0"
                  @click="markAllNotifications"
                >
                  全部已读
                </el-button>
              </div>

              <div v-if="notificationsPage.records.length" class="notification-list">
                <article
                  v-for="notification in notificationsPage.records"
                  :key="notification.id"
                  class="user-notification-item"
                  :class="{ unread: notification.readStatus === 0 }"
                >
                  <div class="notification-icon">
                    <MessageBox />
                  </div>
                  <div class="notification-body">
                    <div class="notification-title-row">
                      <h4>{{ notification.title }}</h4>
                      <el-tag
                        size="small"
                        effect="plain"
                        :type="notification.readStatus === 0 ? 'warning' : 'info'"
                      >
                        {{ notification.readStatus === 0 ? '未读' : '已读' }}
                      </el-tag>
                    </div>
                    <p>{{ notification.content || '暂无通知内容' }}</p>
                    <div class="notification-meta">
                      <span>{{ notificationTypeLabel(notification.type) }}</span>
                      <span>{{ formatDate(notification.createdAt) }}</span>
                    </div>
                  </div>
                  <div class="notification-actions">
                    <el-button
                      v-if="canOpenRelatedPost(notification)"
                      text
                      type="primary"
                      size="small"
                      :icon="View"
                      @click="openRelatedPost(notification)"
                    >
                      查看帖子
                    </el-button>
                    <el-button
                      v-if="notification.readStatus === 0"
                      text
                      type="primary"
                      size="small"
                      :icon="CircleCheck"
                      @click="markOneNotificationRead(notification)"
                    >
                      已读
                    </el-button>
                  </div>
                </article>
              </div>

              <div v-else class="post-empty">
                <Bell />
                <div>
                  <h4>暂时没有通知</h4>
                  <p>举报处理结果、系统提醒和社区消息会出现在这里。</p>
                </div>
              </div>

              <el-pagination
                v-if="notificationsPage.pages > 1"
                class="board-pagination"
                background
                layout="prev, pager, next"
                :current-page="notificationsPage.page"
                :page-size="notificationsPage.size"
                :total="notificationsPage.total"
                @current-change="loadNotifications"
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

    <el-dialog
      v-model="passwordDialogVisible"
      title="修改密码"
      width="460px"
      class="profile-edit-dialog"
    >
      <el-form
        ref="passwordFormRef"
        :model="passwordForm"
        :rules="passwordRules"
        label-position="top"
      >
        <el-form-item label="当前密码" prop="currentPassword">
          <el-input
            v-model="passwordForm.currentPassword"
            type="password"
            show-password
            maxlength="50"
            autocomplete="current-password"
          />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            show-password
            maxlength="50"
            autocomplete="new-password"
          />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            show-password
            maxlength="50"
            autocomplete="new-password"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="profile-dialog-footer">
          <el-button @click="passwordDialogVisible = false">
            取消
          </el-button>
          <el-button type="primary" :loading="passwordSaving" @click="submitPassword">
            保存密码
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>
