<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  ChatLineRound,
  EditPen,
  Refresh,
  Search,
} from '@element-plus/icons-vue'
import { fetchCategories } from '../api/catalogApi'
import { fetchPosts } from '../api/postApi'
import { useUserStore } from '../stores/userStore'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const errorMessage = ref('')
const categories = ref([])
const filters = ref({
  keyword: '',
  categoryCode: '',
  postType: '',
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

const queryParams = computed(() => ({
  page: postsPage.value.page,
  size: postsPage.value.size,
  keyword: filters.value.keyword.trim() || undefined,
  categoryCode: filters.value.categoryCode || undefined,
  postType: filters.value.postType || undefined,
  recommended: filters.value.recommended ? true : undefined,
}))

function resolveError(error, fallback) {
  return error?.response?.data?.message || error?.message || fallback
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
    recommended: false,
  }
  loadBoard(1)
}

function goCreatePost() {
  router.push(userStore.isLoggedIn ? { name: 'post-create' } : { name: 'login' })
}

onMounted(() => loadBoard(1))
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

    <section class="board-toolbar">
      <div class="board-filter-grid">
        <el-input
          v-model="filters.keyword"
          placeholder="搜索标题或正文"
          clearable
          @keyup.enter="loadBoard(1)"
        />

        <el-select v-model="filters.categoryCode" placeholder="全部分区" clearable>
          <el-option
            v-for="category in categories"
            :key="category.id"
            :label="category.name"
            :value="category.code"
          />
        </el-select>

        <el-select v-model="filters.postType" placeholder="全部类型" clearable>
          <el-option
            v-for="option in postTypeOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>

        <el-switch
          v-model="filters.recommended"
          active-text="只看推荐"
          inactive-text="全部"
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
              <span>{{ post.authorNickname }}</span>
              <span>{{ post.postType }}</span>
            </div>
            <RouterLink
              class="post-title-link"
              :to="{ name: 'post-detail', params: { id: post.id } }"
            >
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
