<script setup>
import { computed, onMounted, ref } from 'vue'
import {
  Bell,
  ChatLineRound,
  Collection,
  DataAnalysis,
  Files,
  MapLocation,
  Refresh,
  User,
  Warning,
} from '@element-plus/icons-vue'
import { fetchAdminDashboardSummary } from '../api/adminApi'
import { useUserStore } from '../stores/userStore'

const userStore = useUserStore()

const loading = ref(false)
const errorMessage = ref('')
const summary = ref({
  userCount: 0,
  postCount: 0,
  commentCount: 0,
  categoryCount: 0,
  mapCount: 0,
  traderCount: 0,
  questCount: 0,
  itemCount: 0,
  weaponCount: 0,
  ammoCount: 0,
  hideoutStationCount: 0,
  bossCount: 0,
  pendingReportCount: 0,
  publishedAnnouncementCount: 0,
})

const communityStats = computed(() => [
  {
    label: '注册用户',
    value: summary.value.userCount,
    icon: User,
    hint: '社区身份与权限的基础数据',
  },
  {
    label: '情报帖子',
    value: summary.value.postCount,
    icon: ChatLineRound,
    hint: '玩家发布的路线、任务和复盘内容',
  },
  {
    label: '社区评论',
    value: summary.value.commentCount,
    icon: Collection,
    hint: '帖子下沉淀的讨论与补充情报',
  },
  {
    label: '社区分区',
    value: summary.value.categoryCount,
    icon: Files,
    hint: '论坛内容的主题组织方式',
  },
])

const catalogStats = computed(() => [
  { label: '地图', value: summary.value.mapCount },
  { label: '商人', value: summary.value.traderCount },
  { label: '任务', value: summary.value.questCount },
  { label: '物品', value: summary.value.itemCount },
  { label: '武器', value: summary.value.weaponCount },
  { label: '弹药', value: summary.value.ammoCount },
  { label: '藏身处', value: summary.value.hideoutStationCount },
  { label: 'Boss', value: summary.value.bossCount },
])

const governanceStats = computed(() => [
  {
    label: '待处理举报',
    value: summary.value.pendingReportCount,
    icon: Warning,
    tone: 'attention',
    description: '需要管理员判断的帖子或评论举报',
    action: '进入举报管理',
    to: '/admin/reports',
  },
  {
    label: '已发布公告',
    value: summary.value.publishedAnnouncementCount,
    icon: Bell,
    tone: 'published',
    description: '正在社区首页向玩家公开展示的公告',
    action: '进入公告管理',
    to: '/admin/announcements',
  },
])

const managementCards = [
  {
    title: '帖子审核',
    description: '处理帖子状态、推荐位和内容可见性。',
    icon: ChatLineRound,
    to: '/admin/posts',
  },
  {
    title: '用户管理',
    description: '维护账号角色、状态和贡献数据。',
    icon: User,
    to: '/admin/users',
  },
  {
    title: '资料维护',
    description: '维护地图、任务、装备和 Boss 资料。',
    icon: MapLocation,
    to: '/admin/maps',
  },
]

function resolveError(error) {
  return error?.response?.data?.message || error?.message || '后台概览暂时无法加载'
}

async function loadSummary() {
  loading.value = true
  errorMessage.value = ''

  try {
    summary.value = {
      ...summary.value,
      ...(await fetchAdminDashboardSummary()),
    }
  } catch (error) {
    errorMessage.value = resolveError(error)
  } finally {
    loading.value = false
  }
}

onMounted(loadSummary)
</script>

<template>
  <div class="admin-view">
    <section class="admin-hero">
      <div>
        <h2>后台概览</h2>
        <p>集中查看社区内容和塔科夫资料的数据规模，为后续审核、维护和课程展示提供入口。</p>
      </div>
      <el-button :icon="Refresh" :loading="loading" @click="loadSummary">
        刷新统计
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

    <el-skeleton v-if="loading" :rows="8" animated class="home-skeleton" />

    <template v-else>
      <section class="admin-stat-grid">
        <article v-for="item in communityStats" :key="item.label" class="admin-stat-card">
          <component :is="item.icon" />
          <div>
            <strong>{{ item.value }}</strong>
            <span>{{ item.label }}</span>
            <p>{{ item.hint }}</p>
          </div>
        </article>
      </section>

      <section class="section-block">
        <div class="section-heading">
          <h3>塔科夫资料库</h3>
          <p>这些统计来自后端资料表，后续可以逐步接入后台维护页面。</p>
        </div>

        <div class="catalog-stat-grid">
          <div v-for="item in catalogStats" :key="item.label" class="catalog-stat-item">
            <span>{{ item.label }}</span>
            <strong>{{ item.value }}</strong>
          </div>
        </div>
      </section>

      <section class="section-block">
        <div class="section-heading">
          <h3>社区治理</h3>
          <p>把需要管理员及时处理的事项放在概览页，让论坛管理从入口上更清晰。</p>
        </div>

        <div class="admin-governance-grid">
          <RouterLink
            v-for="item in governanceStats"
            :key="item.label"
            :to="item.to"
            :class="['admin-governance-card', item.tone]"
          >
            <component :is="item.icon" />
            <div>
              <span>{{ item.label }}</span>
              <strong>{{ item.value }}</strong>
              <p>{{ item.description }}</p>
              <small>{{ item.action }}</small>
            </div>
          </RouterLink>
        </div>
      </section>

      <section class="section-block">
        <div class="section-heading">
          <h3>管理入口</h3>
          <p>常用后台模块集中在这里，便于按内容、用户和资料三个方向维护社区。</p>
        </div>

        <div class="admin-module-grid">
          <RouterLink
            v-for="card in managementCards"
            :key="card.title"
            :to="card.to"
            class="admin-module-card"
          >
            <component :is="card.icon" />
            <div>
              <h4>{{ card.title }}</h4>
              <p>{{ card.description }}</p>
            </div>
          </RouterLink>
        </div>
      </section>

      <section class="admin-health">
        <Warning />
        <div>
          <h3>当前后台阶段</h3>
          <p>后台审核、用户管理和资料维护入口已基本接入，下一步重点是补充演示数据、整理接口说明和课程报告截图。</p>
        </div>
        <DataAnalysis />
      </section>
    </template>
  </div>
</template>
