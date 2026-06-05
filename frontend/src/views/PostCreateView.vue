<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, EditPen } from '@element-plus/icons-vue'
import { fetchCategories } from '../api/catalogApi'
import { createPost, fetchPostDetail, updatePost } from '../api/postApi'
import { useUserStore } from '../stores/userStore'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const submitting = ref(false)
const errorMessage = ref('')
const categories = ref([])
const form = ref({
  categoryId: null,
  postType: 'ROUTE',
  title: '',
  content: '',
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

const postTypeOptions = [
  { label: '路线情报', value: 'ROUTE' },
  { label: '任务攻略', value: 'GUIDE' },
  { label: '问题求助', value: 'QUESTION' },
  { label: '市场讨论', value: 'MARKET' },
  { label: '组队招募', value: 'TEAM_UP' },
]

const canSubmit = computed(() => {
  return (
    userStore.isLoggedIn &&
    Boolean(userStore.userInfo?.id) &&
    Boolean(form.value.categoryId) &&
    form.value.title.trim().length > 0 &&
    form.value.content.trim().length >= 10
  )
})

function resolveError(error, fallback) {
  return error?.response?.data?.message || error?.message || fallback
}

async function loadCategories() {
  loading.value = true
  errorMessage.value = ''

  try {
    const [categoryData, postData] = await Promise.all([
      fetchCategories(),
      isEditMode.value ? fetchPostDetail(editPostId.value) : Promise.resolve(null),
    ])

    categories.value = categoryData
    if (postData) {
      form.value = {
        categoryId: postData.categoryId,
        postType: postData.postType,
        title: postData.title,
        content: postData.content,
      }
    } else {
      form.value.categoryId = categories.value[0]?.id ?? null
    }
  } catch (error) {
    errorMessage.value = resolveError(error, isEditMode.value ? '帖子暂时无法加载或没有编辑权限' : '社区分区暂时无法加载')
  } finally {
    loading.value = false
  }
}

async function submitPost() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后再发布情报')
    router.push({ name: 'login' })
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
    }
    const result = isEditMode.value
      ? await updatePost(editPostId.value, payload)
      : await createPost(payload)
    ElMessage.success(isEditMode.value ? '情报帖已更新' : '情报帖已发布')
    router.push({ name: 'post-detail', params: { id: result.id } })
  } catch (error) {
    ElMessage.error(resolveError(error, isEditMode.value ? '保存失败，请稍后重试' : '发布失败，请稍后重试'))
  } finally {
    submitting.value = false
  }
}

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

          <el-form-item label="标题">
            <el-input
              v-model="form.title"
              maxlength="160"
              show-word-limit
              placeholder="例如：森林夜图跑任务路线复盘"
              :disabled="!userStore.isLoggedIn"
            />
          </el-form-item>

          <el-form-item label="正文">
            <el-input
              v-model="form.content"
              type="textarea"
              :autosize="{ minRows: 8, maxRows: 14 }"
              placeholder="写清楚地图、路线、关键点位、风险位置和你希望其他玩家补充的内容..."
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
        <h3>{{ isEditMode ? '编辑建议' : '发帖建议' }}</h3>
        <ul>
          <li>标题尽量说明地图、任务或装备主题，方便其他玩家快速判断。</li>
          <li>正文优先写实战经验，例如撤离路线、危险点位、物资价值或失败复盘。</li>
          <li>{{ isEditMode ? '修改时尽量保留讨论上下文，避免让已有评论失去参考对象。' : '问题帖可以写清楚等级、任务阶段和预算，后续更容易得到有效回复。' }}</li>
        </ul>
      </aside>
    </div>
  </div>
</template>
