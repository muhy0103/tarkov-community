<script setup>
import { useRouter } from 'vue-router'
import { SwitchButton } from '@element-plus/icons-vue'
import { useUserStore } from './stores/userStore'

const router = useRouter()
const userStore = useUserStore()

function logout() {
  userStore.clearAuth()
  router.push('/')
}
</script>

<template>
  <div class="app-shell">
    <header class="app-header">
      <div>
        <p class="app-kicker">Tarkov Community</p>
        <h1>逃离塔科夫玩家情报社区</h1>
      </div>
      <nav class="app-nav" aria-label="主导航">
        <RouterLink to="/">社区概览</RouterLink>
        <RouterLink to="/posts">情报广场</RouterLink>
        <RouterLink v-if="userStore.isAdmin" to="/admin/dashboard">后台概览</RouterLink>
        <RouterLink v-if="!userStore.isLoggedIn" to="/login">登录注册</RouterLink>
        <div v-else class="user-chip">
          <span>{{ userStore.userInfo?.nickname }}</span>
          <el-button :icon="SwitchButton" circle text @click="logout" />
        </div>
      </nav>
    </header>

    <main class="app-main">
      <RouterView />
    </main>
  </div>
</template>
