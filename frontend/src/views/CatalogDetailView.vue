<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  Aim,
  Collection,
  Connection,
  DataAnalysis,
  MapLocation,
  OfficeBuilding,
  Refresh,
} from '@element-plus/icons-vue'
import {
  fetchAmmo,
  fetchBosses,
  fetchHideoutStations,
  fetchMaps,
  fetchTraders,
  fetchWeapons,
} from '../api/catalogApi'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const errorMessage = ref('')
const detail = ref(null)

const catalogConfig = {
  maps: {
    label: '地图情报',
    icon: MapLocation,
    fetcher: fetchMaps,
    discussionCategory: 'maps',
    fields: [
      ['英文名', 'nameEn'],
      ['中文名', 'nameZh'],
      ['难度', 'difficulty'],
      ['推荐等级', 'recommendedLevel'],
    ],
  },
  traders: {
    label: '商人档案',
    icon: Collection,
    fetcher: fetchTraders,
    discussionCategory: 'quests',
    fields: [
      ['英文名', 'nameEn'],
      ['中文名', 'nameZh'],
      ['解锁条件', 'unlockCondition'],
    ],
  },
  weapons: {
    label: '武器资料',
    icon: DataAnalysis,
    fetcher: fetchWeapons,
    discussionCategory: 'loadouts',
    fields: [
      ['英文名', 'nameEn'],
      ['中文名', 'nameZh'],
      ['武器类型', 'weaponType'],
      ['口径', 'caliber'],
    ],
  },
  ammo: {
    label: '弹药资料',
    icon: DataAnalysis,
    fetcher: fetchAmmo,
    discussionCategory: 'loadouts',
    fields: [
      ['英文名', 'nameEn'],
      ['中文名', 'nameZh'],
      ['口径', 'caliber'],
      ['肉伤', 'damage'],
      ['穿透', 'penetration'],
    ],
  },
  bosses: {
    label: 'Boss 情报',
    icon: Aim,
    fetcher: fetchBosses,
    discussionCategory: 'bosses',
    fields: [
      ['英文名', 'nameEn'],
      ['中文名', 'nameZh'],
      ['地图 ID', 'mapId'],
    ],
  },
  hideout: {
    label: '藏身处设施',
    icon: OfficeBuilding,
    fetcher: fetchHideoutStations,
    discussionCategory: 'hideout',
    fields: [
      ['英文名', 'nameEn'],
      ['中文名', 'nameZh'],
    ],
  },
}

const config = computed(() => catalogConfig[route.params.kind] || null)
const title = computed(() => detail.value?.nameZh || detail.value?.nameEn || config.value?.label || '资料详情')
const subtitle = computed(() => detail.value?.nameEn || config.value?.label || 'Tarkov Catalog')

const fieldRows = computed(() => {
  if (!detail.value || !config.value) {
    return []
  }

  return config.value.fields
    .map(([label, key]) => ({
      label,
      value: detail.value[key],
    }))
    .filter((row) => row.value !== undefined && row.value !== null && row.value !== '')
})

function resolveError(error, fallback) {
  return error?.response?.data?.message || error?.message || fallback
}

async function loadDetail() {
  if (!config.value) {
    detail.value = null
    errorMessage.value = '资料类型不存在'
    return
  }

  loading.value = true
  errorMessage.value = ''

  try {
    const records = await config.value.fetcher()
    detail.value = records.find((item) => String(item.id) === String(route.params.id)) || null
    if (!detail.value) {
      errorMessage.value = '没有找到这条资料'
    }
  } catch (error) {
    detail.value = null
    errorMessage.value = resolveError(error, '资料详情暂时无法加载')
  } finally {
    loading.value = false
  }
}

function goDiscussion() {
  const categoryCode = config.value?.discussionCategory
  router.push({
    name: 'post-board',
    query: categoryCode ? { categoryCode } : {},
  })
}

watch(
  () => [route.params.kind, route.params.id],
  () => loadDetail()
)

onMounted(loadDetail)
</script>

<template>
  <div class="catalog-detail-view">
    <RouterLink class="back-link" to="/">
      返回社区概览
    </RouterLink>

    <el-skeleton v-if="loading" :rows="6" animated class="home-skeleton" />

    <el-alert
      v-else-if="errorMessage"
      :title="errorMessage"
      type="warning"
      show-icon
      class="home-alert"
    />

    <section v-else-if="detail && config" class="detail-card catalog-detail-card">
      <div class="detail-title-row">
        <div class="detail-title-icon">
          <component :is="config.icon || Connection" />
        </div>
        <div>
          <el-tag effect="plain">{{ config.label }}</el-tag>
          <h2>{{ title }}</h2>
          <p>{{ subtitle }}</p>
        </div>
      </div>

      <div class="detail-meta-grid">
        <article v-for="row in fieldRows" :key="row.label">
          <span>{{ row.label }}</span>
          <strong>{{ row.value }}</strong>
        </article>
      </div>

      <div class="detail-note">
        <h3>社区使用方式</h3>
        <p>这里先展示资料库中的核心字段，后续可以继续扩展撤离点、资源点、任务链、相关帖子和收藏能力。</p>
      </div>

      <div class="detail-actions">
        <el-button type="primary" @click="goDiscussion">
          去对应分区讨论
        </el-button>
        <el-button :icon="Refresh" @click="loadDetail">
          刷新资料
        </el-button>
      </div>
    </section>
  </div>
</template>
