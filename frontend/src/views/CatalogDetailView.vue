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
import { fetchCatalogDetail } from '../api/catalogApi'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const errorMessage = ref('')
const detail = ref(null)

const catalogConfig = {
  maps: {
    label: '地图情报',
    icon: MapLocation,
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
    discussionCategory: 'quests',
    fields: [
      ['英文名', 'nameEn'],
      ['解锁条件', 'unlockCondition'],
    ],
  },
  quests: {
    label: '任务档案',
    icon: Collection,
    discussionCategory: 'quests',
    fields: [
      ['英文名', 'nameEn'],
      ['中文名', 'nameZh'],
      ['任务类型', 'questType'],
    ],
  },
  items: {
    label: '物品资料',
    icon: DataAnalysis,
    discussionCategory: 'market',
    fields: [
      ['英文名', 'nameEn'],
      ['中文名', 'nameZh'],
      ['物品类型', 'itemType'],
      ['稀有度', 'rarity'],
      ['格子尺寸', 'gridSize'],
      ['任务需要', 'questNeeded'],
      ['藏身处需要', 'hideoutNeeded'],
      ['建议保留', 'keepSuggestion'],
    ],
  },
  weapons: {
    label: '武器资料',
    icon: DataAnalysis,
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
    discussionCategory: 'loadouts',
    fields: [
      ['英文名', 'nameEn'],
      ['中文名', 'nameZh'],
      ['口径', 'caliber'],
      ['肉伤', 'damage'],
      ['穿透', 'penetration'],
      ['护甲伤害', 'armorDamage'],
    ],
  },
  bosses: {
    label: 'Boss 情报',
    icon: Aim,
    discussionCategory: 'bosses',
    fields: [
      ['英文名', 'nameEn'],
      ['出现地图', (value) => value.map?.nameZh || value.map?.nameEn || value.mapId],
    ],
  },
  hideout: {
    label: '藏身处设施',
    icon: OfficeBuilding,
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
const description = computed(() => detail.value?.description || detail.value?.equipmentSummary || '')

const fieldRows = computed(() => {
  if (!detail.value || !config.value) {
    return []
  }

  return config.value.fields
    .map(([label, source]) => ({
      label,
      value: formatValue(typeof source === 'function' ? source(detail.value) : detail.value[source]),
    }))
    .filter((row) => row.value !== undefined && row.value !== null && row.value !== '')
})

const relationSections = computed(() => {
  if (!detail.value || !config.value) {
    return []
  }

  const currentKind = route.params.kind
  const value = detail.value

  if (currentKind === 'maps') {
    return compactSections([
      relationSection('撤离点', '地图可用撤离选择', value.extracts, (item) => ({
        title: item.name,
        meta: compactText([item.factionLimit, item.conditionText]).join(' · '),
        description: item.description,
      }), '暂无撤离点数据'),
      relationSection('资源与交火区域', '适合做路线与收益讨论', value.lootAreas, (item) => ({
        title: item.name,
        meta: compactText([item.lootType, item.riskLevel && `风险 ${item.riskLevel}`]).join(' · '),
        description: item.description,
      }), '暂无资源点数据'),
      relationSection('相关 Boss', '出现于该地图的威胁情报', value.bosses, (item) => ({
        title: item.nameEn,
        meta: 'Boss 情报',
        to: catalogRoute('bosses', item.id),
      }), '暂无 Boss 数据'),
      relationSection('相关任务', '可在该地图完成或推进的任务', value.quests, questCard, '暂无任务数据'),
    ])
  }

  if (currentKind === 'traders') {
    return compactSections([
      relationSection('任务线', '该商人发布的任务', value.quests, questCard, '暂无任务数据'),
    ])
  }

  if (currentKind === 'quests') {
    return compactSections([
      relationSection('任务来源', '商人与地图关联', compactText([value.trader, value.map]), (item) => ({
        title: item.nameZh || item.nameEn,
        meta: item.unlockCondition || item.difficulty || '关联资料',
        to: catalogRoute(item.unlockCondition !== undefined ? 'traders' : 'maps', item.id),
      }), '暂无来源数据'),
      relationSection('前置任务', '开始前建议先完成', value.prerequisites, questCard, '暂无前置任务'),
    ])
  }

  if (currentKind === 'weapons') {
    return compactSections([
      relationSection('可用弹药', '同口径弹药对比', value.compatibleAmmo, (item) => ({
        title: item.nameZh || item.nameEn,
        meta: `${item.caliber} · 肉伤 ${item.damage} · 穿透 ${item.penetration}`,
        to: catalogRoute('ammo', item.id),
      }), '暂无同口径弹药'),
    ])
  }

  if (currentKind === 'ammo') {
    return compactSections([
      relationSection('可用武器', '使用该口径的武器', value.compatibleWeapons, (item) => ({
        title: item.nameZh || item.nameEn,
        meta: `${item.weaponType} · ${item.caliber}`,
        to: catalogRoute('weapons', item.id),
      }), '暂无同口径武器'),
    ])
  }

  if (currentKind === 'bosses') {
    return compactSections([
      relationSection('出现地图', '进入地图详情查看撤离和资源点', compactText([value.map]), (item) => ({
        title: item.nameZh || item.nameEn,
        meta: `${item.nameEn} · ${item.difficulty || '未知难度'}`,
        to: catalogRoute('maps', item.id),
      }), '暂无地图关联'),
      relationSection('装备摘要', '演示数据中的威胁特征', compactText([value.equipmentSummary]), (item) => ({
        title: item,
        meta: '装备与打法讨论入口',
      }), '暂无装备摘要'),
    ])
  }

  if (currentKind === 'hideout') {
    return compactSections([
      relationSection('升级等级', '材料、耗时与解锁内容', value.upgrades, (item) => ({
        title: `等级 ${item.level}`,
        meta: compactText([item.requiredItems, item.requiredTime]).join(' · '),
        description: item.unlocks,
      }), '暂无升级等级数据'),
    ])
  }

  return []
})

function compactText(values) {
  return values.filter((value) => value !== undefined && value !== null && value !== '')
}

function compactSections(sections) {
  return sections.filter((section) => section.items.length || section.emptyText)
}

function relationSection(title, subtitle, records = [], mapper, emptyText) {
  return {
    title,
    subtitle,
    emptyText,
    items: (records || []).map(mapper),
  }
}

function questCard(item) {
  return {
    title: item.nameZh || item.nameEn,
    meta: compactText([item.questType, item.nameEn]).join(' · '),
    to: catalogRoute('quests', item.id),
  }
}

function catalogRoute(kind, id) {
  return { name: 'catalog-detail', params: { kind, id } }
}

function formatValue(value) {
  if (typeof value === 'boolean') {
    return value ? '是' : '否'
  }

  return value
}

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
  detail.value = null

  try {
    detail.value = await fetchCatalogDetail(route.params.kind, route.params.id)
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

    <el-skeleton v-if="loading" :rows="8" animated class="home-skeleton" />

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

      <div v-if="description" class="detail-note">
        <h3>情报摘要</h3>
        <p>{{ description }}</p>
      </div>

      <div v-if="relationSections.length" class="detail-relation-sections">
        <section v-for="section in relationSections" :key="section.title" class="detail-relation-section">
          <div class="detail-section-heading">
            <h3>{{ section.title }}</h3>
            <p>{{ section.subtitle }}</p>
          </div>

          <div v-if="section.items.length" class="detail-relation-list">
            <component
              :is="item.to ? 'RouterLink' : 'article'"
              v-for="item in section.items"
              :key="`${section.title}-${item.title}-${item.meta || ''}`"
              class="detail-relation-item"
              :to="item.to"
            >
              <strong>{{ item.title }}</strong>
              <span v-if="item.meta">{{ item.meta }}</span>
              <p v-if="item.description">{{ item.description }}</p>
            </component>
          </div>
          <p v-else class="detail-empty-text">{{ section.emptyText }}</p>
        </section>
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
