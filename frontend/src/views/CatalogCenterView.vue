<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Refresh, Search } from '@element-plus/icons-vue'
import { fetchCatalogCollections } from '../api/catalogApi'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const errorMessage = ref('')
const activeKind = ref('maps')
const keyword = ref('')
const collections = ref([])

const activeCollection = computed(() => collections.value.find((item) => item.kind === activeKind.value))
const filteredItems = computed(() => {
  const normalized = keyword.value.trim().toLowerCase()
  const items = activeCollection.value?.items || []
  if (!normalized) {
    return items
  }

  return items.filter((item) => [
    item.nameZh,
    item.nameEn,
    item.name,
    item.description,
    item.caliber,
    item.weaponType,
    item.itemType,
    item.questType,
    item.difficulty,
    item.unlockCondition,
  ]
    .filter(Boolean)
    .some((value) => String(value).toLowerCase().includes(normalized)))
})

const totalCount = computed(() => collections.value.reduce((sum, collection) => sum + collection.items.length, 0))

function itemName(item, collection = activeCollection.value) {
  if (collection?.kind === 'traders' || collection?.kind === 'bosses') {
    return item.nameEn || item.name || 'Unnamed'
  }

  return item.nameZh || item.nameEn || item.name || '未命名资料'
}

function itemMeta(item, collection = activeCollection.value) {
  const values = []
  if (collection?.kind !== 'traders' && collection?.kind !== 'bosses') {
    values.push(item.nameEn)
  }
  values.push(
    item.difficulty,
    item.weaponType,
    item.itemType,
    item.questType,
    item.caliber,
    item.unlockCondition,
    item.rarity,
    item.penetration ? `Pen ${item.penetration}` : ''
  )
  return values.filter(Boolean).join(' · ')
}

function itemMedia(item) {
  return item.imageUrl || item.avatar || ''
}

function itemDescription(item, collection = activeCollection.value) {
  if (collection?.kind === 'bosses') {
    return item.equipmentSummary || item.description || '暂无说明'
  }
  return item.description || item.equipmentSummary || '暂无说明'
}

function hideBrokenImage(event) {
  event.target.style.display = 'none'
}

function resolveError(error) {
  return error?.response?.data?.message || error?.message || '资料中心暂时无法加载'
}

function routeTabKind() {
  const tab = Array.isArray(route.query.tab) ? route.query.tab[0] : route.query.tab
  return typeof tab === 'string' ? tab : ''
}

function hasCollectionKind(kind) {
  return Boolean(kind) && collections.value.some((collection) => collection.kind === kind)
}

function syncRouteTab(kind) {
  if (!kind || routeTabKind() === kind) {
    return
  }

  router.replace({
    query: {
      ...route.query,
      tab: kind,
    },
  })
}

function applyRouteTab() {
  const tabKind = routeTabKind()
  if (hasCollectionKind(tabKind)) {
    activeKind.value = tabKind
    return
  }

  if (!hasCollectionKind(activeKind.value)) {
    activeKind.value = collections.value[0]?.kind || 'maps'
  }

  syncRouteTab(activeKind.value)
}

async function loadCatalogCenter() {
  loading.value = true
  errorMessage.value = ''
  try {
    collections.value = await fetchCatalogCollections()
    applyRouteTab()
  } catch (error) {
    errorMessage.value = resolveError(error)
  } finally {
    loading.value = false
  }
}

watch(() => route.query.tab, () => {
  if (collections.value.length) {
    applyRouteTab()
  }
})

watch(activeKind, (kind) => {
  if (collections.value.length) {
    keyword.value = ''
    syncRouteTab(kind)
  }
})

onMounted(loadCatalogCenter)
</script>

<template>
  <div class="catalog-center-view">
    <section class="board-header catalog-center-header">
      <div>
        <h2>资料中心</h2>
        <p>按玩家讨论场景整理地图、任务、装备、市场和藏身处资料，保持轻量浏览，并能进入具体资料页继续讨论。</p>
      </div>
      <div class="catalog-center-actions">
        <span>{{ totalCount }} 条资料</span>
        <el-button :icon="Refresh" :loading="loading" @click="loadCatalogCenter">
          刷新资料
        </el-button>
      </div>
    </section>

    <section class="board-toolbar catalog-center-toolbar">
      <el-input
        v-model="keyword"
        :prefix-icon="Search"
        placeholder="搜索名称、口径、类型或说明"
        clearable
      />
      <el-tabs v-model="activeKind" class="catalog-center-tabs">
        <el-tab-pane
          v-for="collection in collections"
          :key="collection.kind"
          :name="collection.kind"
          :label="`${collection.label} ${collection.items.length}`"
        />
      </el-tabs>
    </section>

    <el-alert
      v-if="errorMessage"
      :title="errorMessage"
      type="warning"
      show-icon
      class="home-alert"
    />

    <el-skeleton v-if="loading" :rows="8" animated class="home-skeleton" />

    <section v-else class="catalog-center-grid">
      <RouterLink
        v-for="item in filteredItems"
        :key="`${activeCollection?.kind}-${item.id}`"
        class="catalog-center-card"
        :to="{ name: 'catalog-detail', params: { kind: activeCollection.kind, id: item.id } }"
      >
        <span class="catalog-center-media" :class="{ 'catalog-center-media--empty': !itemMedia(item) }">
          <img
            v-if="itemMedia(item)"
            :src="itemMedia(item)"
            :alt="itemName(item)"
            loading="lazy"
            @error="hideBrokenImage"
          />
          <span v-else>{{ activeCollection?.label?.slice(0, 1) || '?' }}</span>
        </span>
        <span class="catalog-center-copy">
          <strong>{{ itemName(item) }}</strong>
          <small>{{ itemMeta(item) || activeCollection?.label }}</small>
          <p>{{ itemDescription(item) }}</p>
        </span>
      </RouterLink>
    </section>

    <section v-if="!loading && !filteredItems.length" class="post-empty catalog-center-empty">
      <Search />
      <div>
        <h4>没有找到匹配资料</h4>
        <p>换一个关键词，或者切换上方分类继续浏览。</p>
      </div>
    </section>
  </div>
</template>
