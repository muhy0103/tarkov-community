<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  ArrowLeft,
  ChatLineRound,
  Collection,
  EditPen,
  Refresh,
  User,
  View,
} from '@element-plus/icons-vue'
import { fetchPublicUserPosts, fetchPublicUserProfile } from '../api/userPublicApi'
import { useUserStore } from '../stores/userStore'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const postsLoading = ref(false)
const errorMessage = ref('')
const profile = ref(null)
const postsPage = ref(pageState())

const userId = computed(() => Number(route.params.id))
const displayName = computed(() => profile.value?.nickname || profile.value?.username || '玩家档案')
const userInitial = computed(() => displayName.value.slice(0, 1).toUpperCase())
const isOwnProfile = computed(() => Number(userStore.userInfo?.id) === userId.value)

const stats = computed(() => [
  { label: '贡献值', value: profile.value?.contribution || 0, icon: User },
  { label: '公开帖子', value: profile.value?.postCount || 0, icon: ChatLineRound },
  { label: '参与评论', value: profile.value?.commentCount || 0, icon: Collection },
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

function postTypeLabel(type) {
  return postTypeOptions[type] || type || '普通讨论'
}

function formatDate(value) {
  if (!value) {
    return '暂无记录'
  }

  return new Date(value).toLocaleString('zh-CN', {
    hour12: false,
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  })
}

async function loadProfile() {
  if (!Number.isFinite(userId.value)) {
    errorMessage.value = '玩家地址不正确'
    return
  }

  loading.value = true
  errorMessage.value = ''

  try {
    const [profileData, postData] = await Promise.all([
      fetchPublicUserProfile(userId.value),
      fetchPublicUserPosts(userId.value, { page: 1, size: postsPage.value.size }),
    ])
    profile.value = profileData
    postsPage.value = postData
  } catch (error) {
    errorMessage.value = resolveError(error, '玩家主页暂时无法加载')
  } finally {
    loading.value = false
  }
}

async function loadPosts(page = postsPage.value.page) {
  postsLoading.value = true
  postsPage.value.page = page

  try {
    postsPage.value = await fetchPublicUserPosts(userId.value, {
      page,
      size: postsPage.value.size,
    })
  } catch (error) {
    errorMessage.value = resolveError(error, '玩家帖子暂时无法加载')
  } finally {
    postsLoading.value = false
  }
}

watch(
  () => route.params.id,
  () => loadProfile()
)

onMounted(loadProfile)
</script>

<template>
  <div class="user-public-view">
    <RouterLink class="back-link" to="/posts">
      <ArrowLeft />
      返回情报广场
    </RouterLink>

    <el-alert
      v-if="errorMessage"
      :title="errorMessage"
      type="warning"
      show-icon
      class="home-alert"
    />

    <el-skeleton v-if="loading" :rows="8" animated class="home-skeleton" />

    <template v-else-if="profile">
      <section class="public-profile-hero">
        <div class="user-profile-main">
          <div class="user-avatar">
            <img v-if="profile.avatar" :src="profile.avatar" alt="玩家头像" />
            <span v-else>{{ userInitial }}</span>
          </div>
          <div>
            <h2>{{ displayName }}</h2>
            <p>{{ profile.username }} · {{ profile.role }}</p>
            <span>加入时间 {{ formatDate(profile.createdAt) }}</span>
          </div>
        </div>
        <div class="public-profile-actions">
          <el-button v-if="isOwnProfile" :icon="User" @click="router.push({ name: 'user-center' })">
            回到用户中心
          </el-button>
          <el-button type="primary" :icon="EditPen" @click="router.push({ name: 'post-create' })">
            发布情报
          </el-button>
          <el-button :icon="Refresh" :loading="loading" @click="loadProfile">
            刷新
          </el-button>
        </div>
      </section>

      <section class="user-stat-grid">
        <article v-for="item in stats" :key="item.label" class="user-stat-card">
          <component :is="item.icon" />
          <div>
            <strong>{{ item.value }}</strong>
            <span>{{ item.label }}</span>
          </div>
        </article>
      </section>

      <section class="section-block" v-loading="postsLoading">
        <div class="section-heading">
          <h3>{{ displayName }} 的公开情报</h3>
          <p>只展示仍处于正常状态的公开帖子，方便其他玩家继续浏览和讨论。</p>
        </div>

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
                <span><View /> 浏览 {{ post.viewCount }}</span>
                <span>点赞 {{ post.likeCount }}</span>
                <span>评论 {{ post.commentCount }}</span>
                <RouterLink
                  class="inline-action-link"
                  :to="{ name: 'post-detail', params: { id: post.id } }"
                >
                  进入讨论
                </RouterLink>
              </div>
            </div>
          </article>
        </div>

        <div v-else class="post-empty">
          <ChatLineRound />
          <div>
            <h4>暂时没有公开情报</h4>
            <p>这个玩家还没有保留公开帖子，之后发布的内容会出现在这里。</p>
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
      </section>
    </template>
  </div>
</template>
