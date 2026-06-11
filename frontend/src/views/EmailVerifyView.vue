<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { CircleCheck, Message, Warning } from '@element-plus/icons-vue'
import { verifyEmail } from '../api/authApi'

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const status = ref('loading')
const message = ref('正在确认你的邮箱，请稍候。')
const verifiedUser = ref(null)

const statusIcon = computed(() => {
  if (status.value === 'success') {
    return CircleCheck
  }

  if (status.value === 'error') {
    return Warning
  }

  return Message
})

onMounted(() => {
  confirmEmail()
})

async function confirmEmail() {
  const token = typeof route.query.token === 'string' ? route.query.token : ''

  if (!token) {
    loading.value = false
    status.value = 'error'
    message.value = '验证链接缺少 token，请重新打开邮件中的完整链接。'
    return
  }

  loading.value = true
  status.value = 'loading'
  message.value = '正在确认你的邮箱，请稍候。'

  try {
    verifiedUser.value = await verifyEmail(token)
    status.value = 'success'
    message.value = verifiedUser.value?.message || '邮箱验证成功，现在可以登录。'
  } catch (error) {
    status.value = 'error'
    message.value =
      error?.response?.data?.message ||
      error?.message ||
      '验证失败，请确认链接是否已过期或已经使用。'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <section class="email-verify-view">
    <div class="verify-card" :class="`verify-card--${status}`" v-loading="loading">
      <component :is="statusIcon" class="verify-icon" />
      <div>
        <h2>{{ status === 'success' ? '邮箱确认完成' : status === 'error' ? '邮箱确认失败' : '正在确认邮箱' }}</h2>
        <p>{{ message }}</p>
        <div v-if="verifiedUser" class="verify-user">
          <span>{{ verifiedUser.username }}</span>
          <strong>{{ verifiedUser.email }}</strong>
        </div>
        <div class="verify-actions">
          <el-button
            v-if="status === 'success'"
            type="primary"
            @click="router.push({ name: 'login' })"
          >
            前往登录
          </el-button>
          <el-button
            v-if="status === 'error'"
            type="primary"
            plain
            @click="confirmEmail"
          >
            重新验证
          </el-button>
          <el-button @click="router.push({ name: 'home' })">
            返回社区概览
          </el-button>
        </div>
      </div>
    </div>
  </section>
</template>
