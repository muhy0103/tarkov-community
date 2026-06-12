<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { Bell, Refresh } from '@element-plus/icons-vue'
import { fetchAnnouncement } from '../api/catalogApi'

const route = useRoute()
const loading = ref(false)
const errorMessage = ref('')
const announcement = ref(null)

function formatDate(value) {
  if (!value) {
    return '时间未知'
  }

  return String(value).replace('T', ' ').slice(0, 16)
}

function resolveError(error, fallback) {
  return error?.response?.data?.message || error?.message || fallback
}

async function loadAnnouncement() {
  loading.value = true
  errorMessage.value = ''

  try {
    announcement.value = await fetchAnnouncement(route.params.id)
  } catch (error) {
    announcement.value = null
    errorMessage.value = resolveError(error, '公告详情暂时无法加载')
  } finally {
    loading.value = false
  }
}

onMounted(loadAnnouncement)
</script>

<template>
  <div class="announcement-detail-view">
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

    <section v-else-if="announcement" class="detail-card announcement-detail-card">
      <div class="detail-title-row">
        <div class="detail-title-icon">
          <Bell />
        </div>
        <div>
          <h2>{{ announcement.title }}</h2>
          <p>发布时间 {{ formatDate(announcement.updatedAt || announcement.createdAt) }}</p>
        </div>
      </div>

      <p class="announcement-detail-content">{{ announcement.content }}</p>

      <div class="detail-actions">
        <el-button :icon="Refresh" @click="loadAnnouncement">
          刷新公告
        </el-button>
      </div>
    </section>
  </div>
</template>
