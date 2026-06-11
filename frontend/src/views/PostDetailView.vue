<script setup>
import { computed, nextTick, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft,
  ChatLineRound,
  Close,
  Delete,
  EditPen,
  Pointer,
  Refresh,
  Star,
  StarFilled,
  Warning,
} from '@element-plus/icons-vue'
import {
  createReport,
  createPostComment,
  fetchPostComments,
  fetchPostDetail,
  toggleCommentLike,
  togglePostFavorite,
  togglePostLike,
  withdrawPost,
} from '../api/postApi'
import { useUserStore } from '../stores/userStore'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const commentsLoading = ref(false)
const actionLoading = ref('')
const errorMessage = ref('')
const post = ref(null)
const commentsPage = ref({
  page: 1,
  size: 20,
  total: 0,
  pages: 0,
  records: [],
})
const commentContent = ref('')
const likeActive = ref(false)
const favoriteActive = ref(false)
const favoriteCount = ref(null)
const reportDialogVisible = ref(false)
const reportSaving = ref(false)
const reportFormRef = ref(null)
const commentInputRef = ref(null)
const reportTarget = ref(null)
const reportForm = ref(emptyReportForm())
const replyTarget = ref(null)
const likedCommentIds = ref(new Set())

const postId = computed(() => Number(route.params.id))
const comments = computed(() => commentsPage.value.records ?? [])
const currentUserId = computed(() => userStore.userInfo?.id)
const canInteract = computed(() => userStore.isLoggedIn && Boolean(currentUserId.value))
const canEditPost = computed(() => (
  userStore.isLoggedIn && Number(post.value?.authorId) === Number(currentUserId.value)
))
const favoriteButtonText = computed(() => {
  const countText = favoriteCount.value === null ? '' : ` ${favoriteCount.value}`
  return `${favoriteActive.value ? '已收藏' : '收藏'}${countText}`
})
const reportDialogTitle = computed(() => {
  if (!reportTarget.value) {
    return '提交举报'
  }

  return `举报${targetTypeLabel(reportTarget.value.targetType)}`
})
const commentPlaceholder = computed(() => (
  replyTarget.value
    ? `回复 ${replyTarget.value.authorNickname || '玩家'}，补充你的看法...`
    : '补充你的情报、路线建议或复盘看法...'
))
const submitCommentText = computed(() => (replyTarget.value ? '发布回复' : '发布评论'))

const reportReasonOptions = [
  '违规内容',
  '误导情报',
  '广告或刷屏',
  '人身攻击',
  '其他问题',
]

const reportRules = {
  reason: [
    { required: true, message: '请选择举报原因', trigger: 'change' },
    { max: 120, message: '举报原因不能超过 120 个字符', trigger: 'blur' },
  ],
  description: [
    { max: 500, message: '补充说明不能超过 500 个字符', trigger: 'blur' },
  ],
}

function emptyReportForm() {
  return {
    reason: '',
    description: '',
  }
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

function resolveError(error, fallback) {
  return error?.response?.data?.message || error?.message || fallback
}

function requireLogin() {
  if (canInteract.value) {
    return true
  }

  ElMessage.warning('请先登录后再参与讨论')
  return false
}

function requireLoginForReport() {
  if (canInteract.value) {
    return true
  }

  ElMessage.warning('请先登录后再提交举报')
  router.push({
    name: 'login',
    query: {
      redirect: route.fullPath,
    },
  })
  return false
}

function targetTypeLabel(targetType) {
  return targetType === 'COMMENT' ? '评论' : '帖子'
}

function commentReportTitle(comment) {
  const content = comment.content || '评论内容'
  return content.length > 42 ? `${content.slice(0, 42)}...` : content
}

function applyPostState(postData) {
  post.value = postData
  likeActive.value = Boolean(postData?.likedByCurrentUser)
  favoriteActive.value = Boolean(postData?.favoritedByCurrentUser)
  favoriteCount.value = postData?.favoriteCount ?? null
}

function applyCommentsPage(pageData, options = {}) {
  commentsPage.value = pageData
  const nextLiked = options.resetLikedState ? new Set() : new Set(likedCommentIds.value)

  ;(pageData?.records ?? []).forEach((comment) => {
    if (comment.likedByCurrentUser) {
      nextLiked.add(comment.id)
    } else {
      nextLiked.delete(comment.id)
    }
  })

  likedCommentIds.value = nextLiked
}

async function loadDetail() {
  if (!Number.isFinite(postId.value)) {
    errorMessage.value = '帖子地址不正确'
    return
  }

  loading.value = true
  errorMessage.value = ''

  try {
    const [postData, commentData] = await Promise.all([
      fetchPostDetail(postId.value),
      fetchPostComments(postId.value, { page: 1, size: commentsPage.value.size }),
    ])
    applyPostState(postData)
    applyCommentsPage(commentData, { resetLikedState: true })
  } catch (error) {
    errorMessage.value = resolveError(error, '帖子详情暂时无法加载')
  } finally {
    loading.value = false
  }
}

async function loadComments(page = commentsPage.value.page) {
  if (!Number.isFinite(postId.value)) {
    return
  }

  commentsLoading.value = true
  commentsPage.value.page = page

  try {
    const commentData = await fetchPostComments(postId.value, {
      page,
      size: commentsPage.value.size,
    })
    applyCommentsPage(commentData)
  } catch (error) {
    ElMessage.error(resolveError(error, '评论暂时无法加载'))
  } finally {
    commentsLoading.value = false
  }
}

async function submitComment() {
  if (!requireLogin()) {
    return
  }

  const content = commentContent.value.trim()

  if (content.length < 5) {
    ElMessage.warning('评论至少需要 5 个字')
    return
  }

  actionLoading.value = 'comment'

  try {
    const target = replyTarget.value
    await createPostComment(postId.value, {
      content,
      parentId: target ? (target.parentId || target.id) : null,
      replyToUserId: target?.userId ?? null,
    })
    commentContent.value = ''
    replyTarget.value = null
    ElMessage.success(target ? '回复已发布' : '评论已发布')
    await loadDetail()
  } catch (error) {
    ElMessage.error(resolveError(error, replyTarget.value ? '回复发布失败' : '评论发布失败'))
  } finally {
    actionLoading.value = ''
  }
}

async function startReply(comment) {
  if (!requireLogin()) {
    return
  }

  replyTarget.value = comment
  await nextTick()
  commentInputRef.value?.focus?.()
}

function cancelReply() {
  replyTarget.value = null
}

async function handleLike() {
  if (!requireLogin()) {
    return
  }

  actionLoading.value = 'like'

  try {
    const result = await togglePostLike(postId.value)
    likeActive.value = result.active
    post.value.likeCount = result.count
  } catch (error) {
    ElMessage.error(resolveError(error, '点赞操作失败'))
  } finally {
    actionLoading.value = ''
  }
}

async function handleFavorite() {
  if (!requireLogin()) {
    return
  }

  actionLoading.value = 'favorite'

  try {
    const result = await togglePostFavorite(postId.value)
    favoriteActive.value = result.active
    favoriteCount.value = result.count
  } catch (error) {
    ElMessage.error(resolveError(error, '收藏操作失败'))
  } finally {
    actionLoading.value = ''
  }
}

async function handleCommentLike(comment) {
  if (!requireLogin() || !comment?.id) {
    return
  }

  actionLoading.value = `comment-like-${comment.id}`

  try {
    const result = await toggleCommentLike(postId.value, comment.id)
    commentsPage.value = {
      ...commentsPage.value,
      records: comments.value.map((item) => (
        item.id === comment.id ? { ...item, likeCount: result.count } : item
      )),
    }
    const nextLiked = new Set(likedCommentIds.value)
    if (result.active) {
      nextLiked.add(comment.id)
    } else {
      nextLiked.delete(comment.id)
    }
    likedCommentIds.value = nextLiked
  } catch (error) {
    ElMessage.error(resolveError(error, '评论点赞失败'))
  } finally {
    actionLoading.value = ''
  }
}

async function handleWithdrawPost() {
  if (!canEditPost.value || !post.value) {
    return
  }

  const confirmed = await ElMessageBox.confirm(
    '下架后普通玩家将无法继续浏览这条帖子，后台仍会保留审核记录。确定要下架吗？',
    '下架帖子',
    {
      confirmButtonText: '确认下架',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(() => true).catch(() => false)

  if (!confirmed) {
    return
  }

  actionLoading.value = 'withdraw'

  try {
    await withdrawPost(post.value.id)
    ElMessage.success('帖子已下架')
    router.push({ name: 'user-center' })
  } catch (error) {
    ElMessage.error(resolveError(error, '下架失败，请稍后重试'))
  } finally {
    actionLoading.value = ''
  }
}

function openReportDialog(targetType, targetId, title) {
  if (!requireLoginForReport()) {
    return
  }

  reportTarget.value = {
    targetType,
    targetId,
    title,
  }
  reportForm.value = emptyReportForm()
  reportDialogVisible.value = true
}

async function submitReport() {
  const valid = await reportFormRef.value?.validate().catch(() => false)

  if (!valid || !reportTarget.value) {
    return
  }

  reportSaving.value = true

  try {
    await createReport({
      targetType: reportTarget.value.targetType,
      targetId: reportTarget.value.targetId,
      reason: reportForm.value.reason,
      description: reportForm.value.description.trim(),
    })
    ElMessage.success('举报已提交，管理员会尽快处理')
    reportDialogVisible.value = false
  } catch (error) {
    ElMessage.error(resolveError(error, '举报提交失败'))
  } finally {
    reportSaving.value = false
  }
}

onMounted(loadDetail)
</script>

<template>
  <div class="post-detail-view">
    <RouterLink class="back-link" to="/">
      <ArrowLeft />
      返回社区概览
    </RouterLink>

    <el-alert
      v-if="errorMessage"
      :title="errorMessage"
      type="warning"
      show-icon
      class="home-alert"
    />

    <el-skeleton v-if="loading" :rows="8" animated class="home-skeleton" />

    <template v-else-if="post">
      <article class="detail-card">
        <div class="detail-topline">
          <el-tag effect="plain">{{ post.categoryName }}</el-tag>
          <span>{{ post.postType }}</span>
          <span v-if="post.recommended">推荐情报</span>
        </div>

        <h2>{{ post.title }}</h2>

        <div class="detail-meta">
          <span>{{ post.authorNickname }}</span>
          <span>{{ formatDate(post.createdAt) }}</span>
          <span>浏览 {{ post.viewCount }}</span>
          <span>评论 {{ post.commentCount }}</span>
        </div>

        <p class="detail-content">{{ post.content }}</p>

        <div class="detail-actions">
          <el-button
            v-if="canEditPost"
            :icon="EditPen"
            @click="router.push({ name: 'post-edit', params: { id: post.id } })"
          >
            编辑帖子
          </el-button>
          <el-button
            v-if="canEditPost"
            type="warning"
            plain
            :icon="Delete"
            :loading="actionLoading === 'withdraw'"
            @click="handleWithdrawPost"
          >
            下架帖子
          </el-button>
          <el-button
            :type="likeActive ? 'primary' : 'default'"
            :icon="Pointer"
            :loading="actionLoading === 'like'"
            @click="handleLike"
          >
            点赞 {{ post.likeCount }}
          </el-button>
          <el-button
            :type="favoriteActive ? 'primary' : 'default'"
            :icon="favoriteActive ? StarFilled : Star"
            :loading="actionLoading === 'favorite'"
            @click="handleFavorite"
          >
            {{ favoriteButtonText }}
          </el-button>
          <el-button :icon="Refresh" :loading="loading" @click="loadDetail">
            刷新
          </el-button>
          <el-button :icon="Warning" @click="openReportDialog('POST', post.id, post.title)">
            举报帖子
          </el-button>
        </div>
      </article>

      <section class="comments-card">
        <div class="comments-heading">
          <div>
            <h3>战局讨论</h3>
            <p>把路线、任务点位、配装思路和战局复盘继续沉淀到帖子下面。</p>
          </div>
          <el-tag effect="plain">{{ commentsPage.total }} 条评论</el-tag>
        </div>

        <div class="comment-form">
          <div v-if="replyTarget" class="reply-target-bar">
            <span>
              正在回复 <strong>{{ replyTarget.authorNickname || '玩家' }}</strong>
            </span>
            <el-button text size="small" :icon="Close" @click="cancelReply">
              取消回复
            </el-button>
          </div>
          <el-input
            ref="commentInputRef"
            v-model="commentContent"
            type="textarea"
            :autosize="{ minRows: 4, maxRows: 7 }"
            maxlength="1000"
            show-word-limit
            :placeholder="commentPlaceholder"
          />
          <div class="comment-form-footer">
            <span v-if="!canInteract">登录后可以发表评论、点赞和收藏</span>
            <span v-else>以 {{ userStore.userInfo.nickname }} 的身份参与讨论</span>
            <el-button
              type="primary"
              :icon="ChatLineRound"
              :loading="actionLoading === 'comment'"
              @click="submitComment"
            >
              {{ submitCommentText }}
            </el-button>
          </div>
        </div>

        <div v-if="comments.length" v-loading="commentsLoading" class="comment-list">
          <article v-for="comment in comments" :key="comment.id" class="comment-item">
            <div class="comment-avatar">{{ comment.authorNickname?.slice(0, 1) || 'P' }}</div>
            <div>
              <div class="comment-meta">
                <strong>{{ comment.authorNickname }}</strong>
                <span>{{ formatDate(comment.createdAt) }}</span>
                <el-tag v-if="comment.parentId" size="small" effect="plain">
                  回复
                </el-tag>
                <el-button
                  text
                  size="small"
                  :icon="ChatLineRound"
                  class="comment-reply-button"
                  @click="startReply(comment)"
                >
                  回复
                </el-button>
                <el-button
                  text
                  size="small"
                  :type="likedCommentIds.has(comment.id) ? 'primary' : 'default'"
                  :icon="Pointer"
                  :loading="actionLoading === `comment-like-${comment.id}`"
                  class="comment-reply-button"
                  @click="handleCommentLike(comment)"
                >
                  点赞 {{ comment.likeCount || 0 }}
                </el-button>
                <el-button
                  text
                  size="small"
                  :icon="Warning"
                  class="comment-report-button"
                  @click="openReportDialog('COMMENT', comment.id, commentReportTitle(comment))"
                >
                  举报
                </el-button>
              </div>
              <p>{{ comment.content }}</p>
            </div>
          </article>
        </div>

        <div v-else class="post-empty">
          <ChatLineRound />
          <div>
            <h4>还没有评论</h4>
            <p>可以先发布一条路线建议、任务点位提示或配装补充，让这个帖子真正变成讨论现场。</p>
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
      </section>

      <el-dialog
        v-model="reportDialogVisible"
        :title="reportDialogTitle"
        width="min(520px, calc(100vw - 32px))"
        class="profile-edit-dialog"
      >
        <div v-if="reportTarget" class="report-target-preview">
          <span>{{ targetTypeLabel(reportTarget.targetType) }}</span>
          <strong>{{ reportTarget.title }}</strong>
        </div>

        <el-form
          ref="reportFormRef"
          :model="reportForm"
          :rules="reportRules"
          label-position="top"
        >
          <el-form-item label="举报原因" prop="reason">
            <el-select v-model="reportForm.reason" placeholder="选择原因">
              <el-option
                v-for="option in reportReasonOptions"
                :key="option"
                :label="option"
                :value="option"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="补充说明" prop="description">
            <el-input
              v-model="reportForm.description"
              type="textarea"
              :rows="5"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
        </el-form>

        <template #footer>
          <div class="profile-dialog-footer">
            <el-button @click="reportDialogVisible = false">
              取消
            </el-button>
            <el-button type="primary" :loading="reportSaving" @click="submitReport">
              提交举报
            </el-button>
          </div>
        </template>
      </el-dialog>
    </template>
  </div>
</template>
