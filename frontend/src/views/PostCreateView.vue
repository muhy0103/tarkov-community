<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Close, EditPen, Plus } from '@element-plus/icons-vue'
import { catalogTypeOptions, fetchCatalogCollections, fetchCategories, routeKindForCatalogType } from '../api/catalogApi'
import { createPost, fetchPostDetail, updatePost } from '../api/postApi'
import { useUserStore } from '../stores/userStore'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const submitting = ref(false)
const errorMessage = ref('')
const draftReady = ref(false)
const draftSavedAt = ref('')
const categories = ref([])
const catalogCollections = ref([])
const relationPicker = ref({
  catalogType: 'MAP',
  catalogId: null,
})
const form = ref({
  categoryId: null,
  postType: 'ROUTE',
  title: '',
  content: '',
  relations: [],
})
const editPostId = computed(() => Number(route.params.id))
const isEditMode = computed(() => route.name === 'post-edit' && Number.isFinite(editPostId.value))
const pageTitle = computed(() => (isEditMode.value ? '编辑战局情报' : '发布战局情报'))
const pageDescription = computed(() => (
  isEditMode.value
    ? '更新标题、分区、类型和正文，让已有情报继续保持准确、清楚、可讨论。'
    : '把跑图路线、任务点位、装备建议或复盘内容整理成帖子，让其他玩家可以继续讨论。'
))
const submitText = computed(() => (isEditMode.value ? '保存修改' : '发布情报'))
const backTarget = computed(() => (
  isEditMode.value
    ? { name: 'post-detail', params: { id: editPostId.value } }
    : { name: 'home' }
))
const draftKey = computed(() => `tarkov-post-draft:${userStore.userInfo?.id || 'guest'}`)

const postTypeOptions = [
  { label: '路线情报', value: 'ROUTE' },
  { label: '任务攻略', value: 'GUIDE' },
  { label: '问题求助', value: 'QUESTION' },
  { label: '市场讨论', value: 'MARKET' },
  { label: '组队招募', value: 'TEAM_UP' },
]

const postTypeGuides = {
  ROUTE: {
    title: '路线情报模板',
    description: '适合写地图路线、撤离方案、危险点和高价值资源区。',
    titlePlaceholder: '例如：森林夜图跑任务路线复盘',
    contentPlaceholder: '建议写清楚：地图、出生点、目标路线、危险点位、撤离选择、适合携带的装备和失败经验。',
    checklist: ['地图和出生点', '路线步骤', '危险区域', '撤离与备用方案'],
  },
  GUIDE: {
    title: '任务攻略模板',
    description: '适合整理商人任务、任务物品、点位和前置条件。',
    titlePlaceholder: '例如：Delivery From the Past 低风险路线',
    contentPlaceholder: '建议写清楚：任务名称、商人、前置条件、目标物品或点位、路线顺序、容易翻车的位置。',
    checklist: ['任务名称和商人', '目标点位', '前置条件', '注意事项'],
  },
  QUESTION: {
    title: '问题求助模板',
    description: '适合请教任务卡点、配装选择、地图路线或经济问题。',
    titlePlaceholder: '例如：12 级新手海关任务卡住了该怎么走',
    contentPlaceholder: '建议写清楚：等级、预算、地图、任务阶段、已经尝试过的方法和希望大家帮你判断的问题。',
    checklist: ['等级和预算', '具体卡点', '已尝试方法', '希望获得的建议'],
  },
  MARKET: {
    title: '市场讨论模板',
    description: '适合讨论物品价值、藏身处材料、跳蚤市场和版本经济。',
    titlePlaceholder: '例如：这周显卡和燃料价格适合囤吗',
    contentPlaceholder: '建议写清楚：物品名称、当前价格、用途、你的判断和希望别人补充的数据。',
    checklist: ['物品和价格', '用途场景', '风险判断', '讨论问题'],
  },
  TEAM_UP: {
    title: '组队招募模板',
    description: '适合找任务队友、跑图搭子、复盘队友或固定开黑。',
    titlePlaceholder: '例如：今晚海关任务队找两名稳定队友',
    contentPlaceholder: '建议写清楚：时间、服务器、地图、任务目标、语音方式、队友要求和风险偏好。',
    checklist: ['时间和服务器', '地图与目标', '语音方式', '队友要求'],
  },
}

const currentPostGuide = computed(() => postTypeGuides[form.value.postType] || postTypeGuides.ROUTE)
const activeRelationCollection = computed(() => (
  catalogCollections.value.find((item) => item.type === relationPicker.value.catalogType)
))
const selectedRelationIds = computed(() => new Set(
  form.value.relations
    .filter((item) => item.catalogType === relationPicker.value.catalogType)
    .map((item) => item.catalogId)
))

const canSubmit = computed(() => {
  return (
    userStore.isLoggedIn &&
    Boolean(userStore.userInfo?.id) &&
    Boolean(form.value.categoryId) &&
    form.value.title.trim().length > 0 &&
    form.value.content.trim().length >= 10
  )
})
const hasDraftContent = computed(() => (
  form.value.title.trim().length > 0 ||
  form.value.content.trim().length > 0 ||
  form.value.relations.length > 0
))

function resolveError(error, fallback) {
  return error?.response?.data?.message || error?.message || fallback
}

async function loadCategories() {
  loading.value = true
  errorMessage.value = ''

  try {
    const [categoryData, catalogData, postData] = await Promise.all([
      fetchCategories(),
      fetchCatalogCollections(),
      isEditMode.value ? fetchPostDetail(editPostId.value) : Promise.resolve(null),
    ])

    categories.value = categoryData
    catalogCollections.value = catalogData
    if (postData) {
      form.value = {
        categoryId: postData.categoryId,
        postType: postData.postType,
        title: postData.title,
        content: postData.content,
        relations: normalizeStoredRelations(postData.relations),
      }
    } else {
      form.value.categoryId = categories.value[0]?.id ?? null
      restoreDraft()
    }
  } catch (error) {
    errorMessage.value = resolveError(error, isEditMode.value ? '帖子暂时无法加载或没有编辑权限' : '社区分区暂时无法加载')
  } finally {
    draftReady.value = true
    loading.value = false
  }
}

function restoreDraft() {
  if (isEditMode.value || !userStore.isLoggedIn) {
    return
  }

  try {
    const rawDraft = localStorage.getItem(draftKey.value)
    if (!rawDraft) {
      return
    }

    const draft = JSON.parse(rawDraft)
    form.value = {
      categoryId: categories.value.some((category) => category.id === draft.categoryId)
        ? draft.categoryId
        : categories.value[0]?.id ?? null,
      postType: draft.postType || 'ROUTE',
      title: draft.title || '',
      content: draft.content || '',
      relations: normalizeStoredRelations(draft.relations),
    }
    draftSavedAt.value = draft.savedAt || ''
  } catch (error) {
    localStorage.removeItem(draftKey.value)
  }
}

function saveDraft(showMessage = false) {
  if (!draftReady.value || isEditMode.value || !userStore.isLoggedIn || !hasDraftContent.value) {
    return
  }

  const savedAt = new Date().toISOString()
  localStorage.setItem(draftKey.value, JSON.stringify({
    categoryId: form.value.categoryId,
    postType: form.value.postType,
    title: form.value.title,
    content: form.value.content,
    relations: form.value.relations,
    savedAt,
  }))
  draftSavedAt.value = savedAt

  if (showMessage) {
    ElMessage.success('草稿已保存到本机浏览器')
  }
}

function clearDraft(showMessage = true) {
  localStorage.removeItem(draftKey.value)
  draftSavedAt.value = ''

  if (showMessage) {
    ElMessage.success('草稿已清空')
  }
}

function formatDraftTime(value) {
  if (!value) {
    return '尚未保存'
  }

  return new Date(value).toLocaleString('zh-CN', {
    hour12: false,
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

function relationName(item, collection = activeRelationCollection.value) {
  if (collection?.kind === 'traders' || collection?.kind === 'bosses') {
    return item.nameEn || item.name || 'Unnamed'
  }

  return item.nameZh || item.nameEn || item.name || '未命名资料'
}

function relationSubtitle(item, collection = activeRelationCollection.value) {
  const values = []
  if (collection?.kind !== 'traders' && collection?.kind !== 'bosses') {
    values.push(item.nameEn)
  }
  values.push(
    item.weaponType,
    item.caliber,
    item.difficulty,
    item.unlockCondition,
    item.itemType,
    item.questType
  )
  return values.filter(Boolean).join(' · ')
}

function normalizeStoredRelations(relations = []) {
  if (!Array.isArray(relations)) {
    return []
  }

  return relations
    .filter((relation) => relation?.catalogType && relation?.catalogId)
    .slice(0, 6)
    .map((relation) => ({
      catalogType: relation.catalogType,
      catalogId: relation.catalogId,
      relationNote: relation.relationNote || '',
      name: relation.name || '未命名资料',
      subtitle: relation.subtitle || '',
      imageUrl: relation.imageUrl || '',
      routeKind: relation.routeKind || routeKindForCatalogType(relation.catalogType),
    }))
}

function addRelation() {
  const collection = activeRelationCollection.value
  const item = collection?.items.find((entry) => entry.id === relationPicker.value.catalogId)

  if (!item || form.value.relations.length >= 6) {
    return
  }
  if (form.value.relations.some((relation) => (
    relation.catalogType === collection.type && relation.catalogId === item.id
  ))) {
    return
  }

  form.value.relations.push({
    catalogType: collection.type,
    catalogId: item.id,
    relationNote: '',
    name: relationName(item, collection),
    subtitle: relationSubtitle(item, collection),
    imageUrl: item.imageUrl || item.avatar || '',
    routeKind: collection.kind,
  })
  relationPicker.value.catalogId = null
}

function removeRelation(index) {
  form.value.relations.splice(index, 1)
}

async function submitPost() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后再发布情报')
    router.push({ name: 'login', query: { redirect: route.fullPath } })
    return
  }

  if (!canSubmit.value) {
    ElMessage.warning('请补充分区、标题和至少 10 个字的正文')
    return
  }

  submitting.value = true

  try {
    const payload = {
      categoryId: form.value.categoryId,
      title: form.value.title.trim(),
      content: form.value.content.trim(),
      postType: form.value.postType,
      coverImage: null,
      relations: form.value.relations.map((relation) => ({
        catalogType: relation.catalogType,
        catalogId: relation.catalogId,
        relationNote: relation.relationNote || null,
      })),
    }
    const result = isEditMode.value
      ? await updatePost(editPostId.value, payload)
      : await createPost(payload)
    if (!isEditMode.value) {
      clearDraft(false)
    }
    ElMessage.success(isEditMode.value ? '情报帖已更新' : '情报帖已发布')
    router.push({ name: 'post-detail', params: { id: result.id } })
  } catch (error) {
    ElMessage.error(resolveError(error, isEditMode.value ? '保存失败，请稍后重试' : '发布失败，请稍后重试'))
  } finally {
    submitting.value = false
  }
}

watch(
  form,
  () => saveDraft(false),
  { deep: true }
)

watch(
  () => relationPicker.value.catalogType,
  () => {
    relationPicker.value.catalogId = null
  }
)

onMounted(loadCategories)
</script>

<template>
  <div class="post-create-view">
    <RouterLink class="back-link" :to="backTarget">
      <ArrowLeft />
      {{ isEditMode ? '返回帖子详情' : '返回社区概览' }}
    </RouterLink>

    <div class="create-layout">
      <section class="detail-card create-card">
        <div class="create-heading">
          <h2>{{ pageTitle }}</h2>
          <p>{{ pageDescription }}</p>
        </div>

        <el-alert
          v-if="!userStore.isLoggedIn"
          title="当前未登录，登录后才能发布情报帖。"
          type="info"
          show-icon
          class="auth-alert"
        />

        <el-alert
          v-if="errorMessage"
          :title="errorMessage"
          type="warning"
          show-icon
          class="auth-alert"
        />

        <el-form label-position="top" class="create-form">
          <div v-if="!isEditMode && userStore.isLoggedIn" class="draft-toolbar">
            <span>草稿状态：{{ formatDraftTime(draftSavedAt) }}</span>
            <div>
              <el-button size="small" @click="saveDraft(true)">
                保存草稿
              </el-button>
              <el-button size="small" :disabled="!draftSavedAt" @click="clearDraft(true)">
                清空草稿
              </el-button>
            </div>
          </div>

          <div class="create-form-grid">
            <el-form-item label="社区分区">
              <el-select
                v-model="form.categoryId"
                :loading="loading"
                placeholder="选择分区"
                :disabled="!userStore.isLoggedIn"
              >
                <el-option
                  v-for="category in categories"
                  :key="category.id"
                  :label="category.name"
                  :value="category.id"
                />
              </el-select>
            </el-form-item>

            <el-form-item label="帖子类型">
              <el-select v-model="form.postType" :disabled="!userStore.isLoggedIn">
                <el-option
                  v-for="option in postTypeOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                />
              </el-select>
            </el-form-item>
          </div>

          <el-form-item label="关联资料">
            <div class="post-relation-panel">
              <div class="relation-picker-row">
                <el-select
                  v-model="relationPicker.catalogType"
                  placeholder="资料类型"
                  :disabled="!userStore.isLoggedIn"
                >
                  <el-option
                    v-for="option in catalogTypeOptions"
                    :key="option.type"
                    :label="option.label"
                    :value="option.type"
                  />
                </el-select>
                <el-select
                  v-model="relationPicker.catalogId"
                  placeholder="选择关联资料"
                  :disabled="!userStore.isLoggedIn"
                  filterable
                  clearable
                >
                  <el-option
                    v-for="item in activeRelationCollection?.items || []"
                    :key="item.id"
                    :label="relationName(item)"
                    :value="item.id"
                    :disabled="selectedRelationIds.has(item.id)"
                  >
                    <span class="relation-option">
                      <span>{{ relationName(item) }}</span>
                      <small>{{ relationSubtitle(item) }}</small>
                    </span>
                  </el-option>
                </el-select>
                <el-button
                  :icon="Plus"
                  :disabled="!relationPicker.catalogId || form.relations.length >= 6"
                  @click="addRelation"
                >
                  添加
                </el-button>
              </div>

              <div v-if="form.relations.length" class="relation-chip-list">
                <span
                  v-for="(relation, index) in form.relations"
                  :key="`${relation.catalogType}-${relation.catalogId}`"
                  class="relation-chip"
                >
                  <img v-if="relation.imageUrl" :src="relation.imageUrl" :alt="relation.name" />
                  <span>{{ relation.name }}</span>
                  <small v-if="relation.subtitle">{{ relation.subtitle }}</small>
                  <button type="button" aria-label="移除关联资料" @click="removeRelation(index)">
                    <Close />
                  </button>
                </span>
              </div>
            </div>
          </el-form-item>

          <el-form-item label="标题">
            <el-input
              v-model="form.title"
              maxlength="160"
              show-word-limit
              :placeholder="currentPostGuide.titlePlaceholder"
              :disabled="!userStore.isLoggedIn"
            />
          </el-form-item>

          <el-form-item label="正文">
            <el-input
              v-model="form.content"
              type="textarea"
              :autosize="{ minRows: 8, maxRows: 14 }"
              :placeholder="currentPostGuide.contentPlaceholder"
              :disabled="!userStore.isLoggedIn"
            />
          </el-form-item>

          <div class="create-actions">
            <el-button @click="router.push({ name: 'home' })">取消</el-button>
            <el-button
              type="primary"
              :icon="EditPen"
              :loading="submitting"
              :disabled="!canSubmit"
              @click="submitPost"
            >
              {{ submitText }}
            </el-button>
          </div>
        </el-form>
      </section>

      <aside class="create-side">
        <h3>{{ currentPostGuide.title }}</h3>
        <p>{{ currentPostGuide.description }}</p>
        <ul>
          <li v-for="item in currentPostGuide.checklist" :key="item">{{ item }}</li>
        </ul>
        <div class="create-side-note">
          {{ isEditMode ? '编辑时尽量保留讨论上下文，避免让已有评论失去参考对象。' : '写得越具体，其他玩家越容易给出有效补充。' }}
        </div>
      </aside>
    </div>
  </div>
</template>
