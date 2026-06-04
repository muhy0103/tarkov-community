<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ArrowLeft,
  ChatLineRound,
  Pointer,
  Refresh,
  Star,
  StarFilled,
} from '@element-plus/icons-vue'
import {
  createPostComment,
  fetchPostComments,
  fetchPostDetail,
  togglePostFavorite,
  togglePostLike,
} from '../api/postApi'
import { useUserStore } from '../stores/userStore'

const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
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

const postId = computed(() => Number(route.params.id))
const comments = computed(() => commentsPage.value.records ?? [])
const currentUserId = computed(() => userStore.userInfo?.id)
const canInteract = computed(() => userStore.isLoggedIn && Boolean(currentUserId.value))
const favoriteButtonText = computed(() => {
  const countText = favoriteCount.value === null ? '' : ` ${favoriteCount.value}`
  return `${favoriteActive.value ? '已收藏' : '收藏'}${countText}`
})

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
      fetchPostComments(postId.value),
    ])
    post.value = postData
    commentsPage.value = commentData
  } catch (error) {
    errorMessage.value = resolveError(error, '帖子详情暂时无法加载')
  } finally {
    loading.value = false
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
    await createPostComment(postId.value, {
      userId: currentUserId.value,
      content,
      parentId: null,
      replyToUserId: null,
    })
    commentContent.value = ''
    ElMessage.success('评论已发布')
    await loadDetail()
  } catch (error) {
    ElMessage.error(resolveError(error, '评论发布失败'))
  } finally {
    actionLoading.value = ''
  }
}

async function handleLike() {
  if (!requireLogin()) {
    return
  }

  actionLoading.value = 'like'

  try {
    const result = await togglePostLike(postId.value, currentUserId.value)
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
    const result = await togglePostFavorite(postId.value, currentUserId.value)
    favoriteActive.value = result.active
    favoriteCount.value = result.count
  } catch (error) {
    ElMessage.error(resolveError(error, '收藏操作失败'))
  } finally {
    actionLoading.value = ''
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
          <el-input
            v-model="commentContent"
            type="textarea"
            :autosize="{ minRows: 4, maxRows: 7 }"
            maxlength="1000"
            show-word-limit
            placeholder="补充你的情报、路线建议或复盘看法..."
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
              发布评论
            </el-button>
          </div>
        </div>

        <div v-if="comments.length" class="comment-list">
          <article v-for="comment in comments" :key="comment.id" class="comment-item">
            <div class="comment-avatar">{{ comment.authorNickname?.slice(0, 1) || 'P' }}</div>
            <div>
              <div class="comment-meta">
                <strong>{{ comment.authorNickname }}</strong>
                <span>{{ formatDate(comment.createdAt) }}</span>
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
      </section>
    </template>
  </div>
</template>
