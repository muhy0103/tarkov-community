<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  Aim,
  Bell,
  ChatLineRound,
  Collection,
  Connection,
  DataAnalysis,
  EditPen,
  MapLocation,
  Notebook,
  OfficeBuilding,
  Refresh,
  Suitcase,
  TrendCharts,
  User,
  View,
  Warning,
} from '@element-plus/icons-vue'
import { fetchAnnouncements, fetchHomeCatalog } from '../api/catalogApi'
import { fetchPosts } from '../api/postApi'
import { useUserStore } from '../stores/userStore'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const errorMessage = ref('')
const catalogSection = ref(null)
const postsPage = ref({
  page: 1,
  size: 5,
  total: 0,
  pages: 0,
  records: [],
})
const announcementsPage = ref({
  page: 1,
  size: 3,
  total: 0,
  pages: 0,
  records: [],
})

const catalog = ref({
  categories: [],
  maps: [],
  traders: [],
  quests: [],
  items: [],
  weapons: [],
  ammo: [],
  hideoutStations: [],
  bosses: [],
})

const categoryIconMap = {
  maps: MapLocation,
  quests: Notebook,
  loadouts: Suitcase,
  market: TrendCharts,
  hideout: OfficeBuilding,
  bosses: Warning,
  'raid-reviews': Aim,
  'team-up': User,
}

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

const stats = computed(() => [
  {
    label: '社区板块',
    value: catalog.value.categories.length,
    hint: '围绕地图、任务、配装、市场与组队分区',
  },
  {
    label: '地图资料',
    value: catalog.value.maps.length,
    hint: '用于撤离点、任务点位和路线讨论',
  },
  {
    label: '商人档案',
    value: catalog.value.traders.length,
    hint: '承接任务线、解锁条件和交易信息',
  },
  {
    label: '任务样本',
    value: catalog.value.quests.length,
    hint: `已有 ${postsPage.value.total} 条社区帖子可继续沉淀`,
  },
])

const quickCatalogGroups = computed(() => [
  {
    title: '地图情报',
    icon: MapLocation,
    items: catalog.value.maps.map((item) => ({
      id: item.id,
      name: item.nameZh,
      meta: `${item.nameEn} · ${item.difficulty || '未知难度'}`,
    })),
  },
  {
    title: '商人任务线',
    icon: Collection,
    items: catalog.value.traders.map((item) => ({
      id: item.id,
      name: item.nameZh,
      meta: item.unlockCondition || item.nameEn,
    })),
  },
  {
    title: '装备与弹药',
    icon: DataAnalysis,
    items: [
      ...catalog.value.weapons.map((item) => ({
        id: `weapon-${item.id}`,
        name: item.nameZh,
        meta: `${item.weaponType} · ${item.caliber}`,
      })),
      ...catalog.value.ammo.map((item) => ({
        id: `ammo-${item.id}`,
        name: item.nameZh,
        meta: `${item.caliber} · 穿透 ${item.penetration}`,
      })),
    ],
  },
  {
    title: '威胁与藏身处',
    icon: Connection,
    items: [
      ...catalog.value.bosses.map((item) => ({
        id: `boss-${item.id}`,
        name: item.nameZh,
        meta: 'Boss 情报',
      })),
      ...catalog.value.hideoutStations.map((item) => ({
        id: `hideout-${item.id}`,
        name: item.nameZh,
        meta: '藏身处设施',
      })),
    ],
  },
])
const announcements = computed(() => announcementsPage.value.records ?? [])

function formatDate(value) {
  if (!value) {
    return '时间未知'
  }

  return String(value).replace('T', ' ').slice(0, 16)
}

function postTypeLabel(type) {
  return postTypeOptions[type] || type || '普通讨论'
}

async function loadCatalog() {
  loading.value = true
  errorMessage.value = ''

  try {
    const [catalogData, postData, announcementData] = await Promise.all([
      fetchHomeCatalog(),
      fetchPosts({ page: 1, size: 5 }),
      fetchAnnouncements({ page: 1, size: 3 }),
    ])
    catalog.value = catalogData
    postsPage.value = postData
    announcementsPage.value = announcementData
  } catch (error) {
    errorMessage.value =
      error?.response?.data?.message ||
      error?.message ||
      '后端服务暂时无法连接，请确认 Spring Boot 已启动。'
  } finally {
    loading.value = false
  }
}

function scrollToCatalog() {
  catalogSection.value?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

function goCreatePost() {
  router.push(userStore.isLoggedIn ? { name: 'post-create' } : { name: 'login' })
}

onMounted(loadCatalog)
</script>

<template>
  <div class="home-view">
    <section class="home-hero">
      <div class="hero-copy">
        <h2>把战局情报、任务路线和玩家讨论放在一个清爽社区里</h2>
        <p>
          当前阶段先接入基础资料接口：首页会从 Spring Boot 读取社区板块、地图、商人、
          任务、装备弹药、Boss 与藏身处设施，为后续帖子、评论和后台管理打基础。
        </p>
        <div class="hero-actions">
          <el-button type="primary" :icon="View" @click="scrollToCatalog">
            查看资料
          </el-button>
          <el-button :icon="Refresh" :loading="loading" @click="loadCatalog">
            刷新数据
          </el-button>
          <el-button type="success" :icon="EditPen" @click="goCreatePost">
            发布情报
          </el-button>
        </div>
      </div>

      <div class="hero-summary" aria-label="项目数据概览">
        <div v-for="item in stats" :key="item.label" class="stat-tile">
          <strong>{{ loading ? '--' : item.value }}</strong>
          <span>{{ item.label }}</span>
          <p>{{ item.hint }}</p>
        </div>
      </div>
    </section>

    <el-alert
      v-if="errorMessage"
      :title="errorMessage"
      type="warning"
      show-icon
      class="home-alert"
    />

    <el-skeleton v-if="loading" :rows="6" animated class="home-skeleton" />

    <template v-else>
      <section v-if="announcements.length" class="section-block">
        <div class="section-heading">
          <h3>社区公告</h3>
          <p>展示后台已发布的维护通知、活动提醒和社区规则更新。</p>
        </div>

        <div class="announcement-list">
          <article
            v-for="announcement in announcements"
            :key="announcement.id"
            class="announcement-item"
          >
            <Bell />
            <div>
              <div class="announcement-meta">
                <strong>{{ announcement.title }}</strong>
                <span>{{ formatDate(announcement.updatedAt || announcement.createdAt) }}</span>
              </div>
              <p>{{ announcement.content }}</p>
            </div>
          </article>
        </div>
      </section>

      <section class="section-block">
        <div class="section-heading">
          <h3>社区帖子</h3>
          <p>首页先展示最新情报帖，后续可以继续扩展筛选、发帖表单和帖子详情页。</p>
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
                <span>{{ postTypeLabel(post.postType) }}</span>
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
              <RouterLink
                class="post-discuss-link"
                :to="{ name: 'post-detail', params: { id: post.id } }"
              >
                进入讨论
              </RouterLink>
            </div>
          </article>
        </div>

        <div v-else class="post-empty">
          <ChatLineRound />
          <div>
            <h4>等待玩家发布第一条情报</h4>
            <p>后端帖子接口已接入，当前数据库暂无帖子。后续做发帖功能后，这里会直接展示真实社区内容。</p>
          </div>
        </div>
      </section>

      <section ref="catalogSection" class="section-block">
        <div class="section-heading">
          <h3>社区分区</h3>
          <p>这些板块让论坛内容更像塔科夫玩家社区，而不是单纯的百科条目堆叠。</p>
        </div>

        <div class="category-grid">
          <article
            v-for="category in catalog.categories"
            :key="category.id"
            class="category-card"
          >
            <component
              :is="categoryIconMap[category.code] || Collection"
              class="category-icon"
            />
            <div>
              <h4>{{ category.name }}</h4>
              <p>{{ category.description }}</p>
              <el-tag size="small" effect="plain">{{ category.code }}</el-tag>
            </div>
          </article>
        </div>
      </section>

      <section class="section-block">
        <div class="section-heading">
          <h3>塔科夫资料预览</h3>
          <p>后端字典数据先做轻量展示，后续可以接入搜索、收藏、攻略帖关联和后台维护。</p>
        </div>

        <div class="catalog-columns">
          <article
            v-for="group in quickCatalogGroups"
            :key="group.title"
            class="catalog-column"
          >
            <div class="column-title">
              <component :is="group.icon" />
              <h4>{{ group.title }}</h4>
            </div>
            <ul>
              <li v-for="item in group.items.slice(0, 4)" :key="item.id">
                <span>{{ item.name }}</span>
                <small>{{ item.meta }}</small>
              </li>
            </ul>
          </article>
        </div>
      </section>
    </template>
  </div>
</template>
