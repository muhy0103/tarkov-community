<script setup>
import { computed, onMounted, ref } from 'vue'
import {
  Aim,
  Collection,
  Connection,
  DataAnalysis,
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
import { fetchHomeCatalog } from '../api/catalogApi'

const loading = ref(false)
const errorMessage = ref('')
const catalogSection = ref(null)

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
    hint: '后续可扩展为玩家攻略沉淀',
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

async function loadCatalog() {
  loading.value = true
  errorMessage.value = ''

  try {
    catalog.value = await fetchHomeCatalog()
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
