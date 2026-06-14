<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  ChatLineRound,
  EditPen,
  Pointer,
  Refresh,
  Search,
  Star,
  View,
} from '@element-plus/icons-vue'
import { fetchCategories, routeKindForCatalogType } from '../api/catalogApi'
import { fetchPosts } from '../api/postApi'
import { useUserStore } from '../stores/userStore'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const errorMessage = ref('')
const categories = ref([])
const filters = ref({
  keyword: '',
  categoryCode: '',
  postType: '',
  sort: 'LATEST',
  recommended: false,
})
const postsPage = ref({
  page: 1,
  size: 10,
  total: 0,
  pages: 0,
  records: [],
})

const postTypeOptions = [
  { label: '路线情报', value: 'ROUTE' },
  { label: '任务攻略', value: 'GUIDE' },
  { label: '问题求助', value: 'QUESTION' },
  { label: '市场讨论', value: 'MARKET' },
  { label: '组队招募', value: 'TEAM_UP' },
]

const sortOptions = [
  { label: '最新发布', value: 'LATEST' },
  { label: '热度优先', value: 'HOT' },
  { label: '评论最多', value: 'MOST_COMMENTED' },
  { label: '点赞最多', value: 'MOST_LIKED' },
]

const routeSortValues = new Set(sortOptions.map((option) => option.value))

const postTypeLabelMap = postTypeOptions.reduce((map, option) => {
  map[option.value] = option.label
  return map
}, {})

const queryParams = computed(() => ({
  page: postsPage.value.page,
  size: postsPage.value.size,
  keyword: filters.value.keyword.trim() || undefined,
  categoryCode: filters.value.categoryCode || undefined,
  postType: filters.value.postType || undefined,
  sort: filters.value.sort || 'LATEST',
  recommended: filters.value.recommended ? true : undefined,
}))

function resolveError(error, fallback) {
  return error?.response?.data?.message || error?.message || fallback
}

function stringQueryValue(value) {
  return typeof value === 'string' ? value : ''
}

function applyRouteQuery() {
  const sort = stringQueryValue(route.query.sort)

  filters.value = {
    ...filters.value,
    keyword: stringQueryValue(route.query.keyword),
    categoryCode: stringQueryValue(route.query.categoryCode),
    postType: stringQueryValue(route.query.postType),
    sort: routeSortValues.has(sort) ? sort : 'LATEST',
    recommended: route.query.recommended === 'true',
  }
}

function postTypeLabel(type) {
  return postTypeLabelMap[type] || type || '普通讨论'
}

function catalogRoute(relation) {
  return {
    name: 'catalog-detail',
    params: {
      kind: relation.routeKind || routeKindForCatalogType(relation.catalogType),
      id: relation.catalogId,
    },
  }
}

async function loadBoard(page = postsPage.value.page) {
  loading.value = true
  errorMessage.value = ''
  postsPage.value.page = page

  try {
    const [categoryData, postData] = await Promise.all([
      categories.value.length ? Promise.resolve(categories.value) : fetchCategories(),
      fetchPosts(queryParams.value),
    ])
    categories.value = categoryData
    postsPage.value = postData
  } catch (error) {
    errorMessage.value = resolveError(error, '情报广场暂时无法加载')
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  filters.value = {
    keyword: '',
    categoryCode: '',
    postType: '',
    sort: 'LATEST',
    recommended: false,
  }
  if (Object.keys(route.query).length) {
    router.replace({ name: 'post-board' })
    return
  }
  loadBoard(1)
}

function goCreatePost() {
  router.push(userStore.isLoggedIn
    ? { name: 'post-create' }
    : { name: 'login', query: { redirect: '/posts/new' } }
  )
}

watch(
  () => route.query,
  () => {
    applyRouteQuery()
    loadBoard(1)
  }
)

onMounted(() => {
  applyRouteQuery()
  loadBoard(1)
})
</script>

<template>
  <div class="board-view">
    <section class="board-header">
      <div>
        <h2>情报广场</h2>
        <p>集中浏览玩家发布的路线、任务、配装、市场和组队讨论，避免首页变成拥挤的帖子墙。</p>
      </div>
      <el-button type="primary" :icon="EditPen" @click="goCreatePost">
        发布情报
      </el-button>
    </section>

    <section class="board-player-panel">
      <div>
        <h3>{{ userStore.isLoggedIn ? `欢迎回来，${userStore.userInfo?.nickname || 'PMC'}` : '登录后参与社区互动' }}</h3>
        <p>
          {{ userStore.isLoggedIn
            ? '你可以继续回复帖子、收藏路线、查看自己的通知，也可以发布新的战局情报。'
            : '登录后可以发布情报、评论补充、点赞收藏，并在用户中心查看自己的帖子和通知。' }}
        </p>
      </div>
      <div class="board-player-actions">
        <el-button
          v-if="userStore.isLoggedIn"
          :icon="Star"
          @click="router.push({ name: 'user-center', query: { tab: 'favorites' } })"
        >
          我的收藏
        </el-button>
        <el-button
          v-if="userStore.isLoggedIn"
          :icon="ChatLineRound"
          @click="router.push({ name: 'user-center', query: { tab: 'comments' } })"
        >
          我的讨论
        </el-button>
        <el-button
          v-else
          type="primary"
          @click="router.push({ name: 'login', query: { redirect: '/posts' } })"
        >
          去登录
        </el-button>
      </div>
    </section>

    <section class="board-toolbar">
      <div class="board-filter-grid">
        <el-input
          v-model="filters.keyword"
          placeholder="搜索标题或正文"
          clearable
          @clear="loadBoard(1)"
          @keyup.enter="loadBoard(1)"
        />

        <el-select v-model="filters.categoryCode" placeholder="全部分区" clearable @change="loadBoard(1)">
          <el-option
            v-for="category in categories"
            :key="category.id"
            :label="category.name"
            :value="category.code"
          />
        </el-select>

        <el-select v-model="filters.postType" placeholder="全部类型" clearable @change="loadBoard(1)">
          <el-option
            v-for="option in postTypeOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>

        <el-select v-model="filters.sort" placeholder="排序方式" @change="loadBoard(1)">
          <el-option
            v-for="option in sortOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>

        <el-switch
          v-model="filters.recommended"
          active-text="只看推荐"
          inactive-text="全部"
          @change="loadBoard(1)"
        />
      </div>

      <div class="board-actions">
        <el-button :icon="Search" type="primary" @click="loadBoard(1)">
          搜索
        </el-button>
        <el-button :icon="Refresh" :loading="loading" @click="resetFilters">
          重置
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

    <el-skeleton v-if="loading" :rows="7" animated class="home-skeleton" />

    <section v-else class="section-block">
      <div class="section-heading">
        <h3>帖子列表</h3>
        <p>当前共 {{ postsPage.total }} 条情报帖，点击标题可以进入详情页继续讨论。</p>
      </div>

      <div v-if="postsPage.records.length" class="post-list">
        <article v-for="post in postsPage.records" :key="post.id" class="post-item">
          <div class="post-icon">
            <ChatLineRound />
          </div>
          <div class="post-body">
            <div class="post-meta">
              <el-tag size="small" effect="plain">{{ post.categoryName }}</el-tag>
              <RouterLink
                v-if="post.authorId"
                class="author-inline-link"
                :to="{ name: 'user-public', params: { id: post.authorId } }"
              >
                {{ post.authorNickname }}
              </RouterLink>
              <span v-else>{{ post.authorNickname }}</span>
              <span>{{ postTypeLabel(post.postType) }}</span>
            </div>
            <div v-if="post.relations?.length" class="post-relation-tags">
              <RouterLink
                v-for="relation in post.relations.slice(0, 3)"
                :key="`${post.id}-${relation.catalogType}-${relation.catalogId}`"
                class="post-relation-tag"
                :to="catalogRoute(relation)"
              >
                {{ relation.name }}
              </RouterLink>
              <span v-if="post.relations.length > 3" class="post-relation-more">
                +{{ post.relations.length - 3 }}
              </span>
            </div>
            <RouterLink
              class="post-title-link"
              :to="{ name: 'post-detail', params: { id: post.id } }"
            >
              <h4>{{ post.title }}</h4>
            </RouterLink>
            <p>{{ post.summary }}</p>
            <div class="post-counts">
              <span><View /> 浏览 {{ post.viewCount }}</span>
              <span><Pointer /> 点赞 {{ post.likeCount }}</span>
              <span><ChatLineRound /> 评论 {{ post.commentCount }}</span>
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
          <h4>没有找到匹配的情报帖</h4>
          <p>可以换一个关键词或分区，也可以直接发布一条新的情报。</p>
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
        @current-change="loadBoard"
      />
    </section>
  </div>
</template>
