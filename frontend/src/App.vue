<script setup>
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowDown, Bell, Setting, SwitchButton } from '@element-plus/icons-vue'
import { fetchUnreadNotificationCount } from './api/userCenterApi'
import { useUserStore } from './stores/userStore'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const unreadNotificationCount = ref(0)

const adminLinks = [
  { label: '后台概览', to: '/admin/dashboard' },
  { label: '帖子审核', to: '/admin/posts' },
  { label: '用户管理', to: '/admin/users' },
  { label: '评论审核', to: '/admin/comments' },
  { label: '举报管理', to: '/admin/reports' },
  { label: '公告管理', to: '/admin/announcements' },
  { label: '分区管理', to: '/admin/categories' },
  { label: '标签管理', to: '/admin/tags' },
  { label: '地图管理', to: '/admin/maps' },
  { label: '撤离点管理', to: '/admin/map-extracts' },
  { label: '资源点管理', to: '/admin/map-loot-areas' },
  { label: '商人管理', to: '/admin/traders' },
  { label: '任务管理', to: '/admin/quests' },
  { label: '任务链管理', to: '/admin/quest-prerequisites' },
  { label: '物品管理', to: '/admin/items' },
  { label: '武器管理', to: '/admin/weapons' },
  { label: '弹药管理', to: '/admin/ammo' },
  { label: '藏身处管理', to: '/admin/hideout/stations' },
  { label: '升级管理', to: '/admin/hideout/upgrades' },
  { label: 'Boss 管理', to: '/admin/bosses' },
]

function goAdmin(path) {
  router.push(path)
}

async function refreshUnreadNotificationCount() {
  if (!userStore.isLoggedIn) {
    unreadNotificationCount.value = 0
    return
  }

  try {
    unreadNotificationCount.value = await fetchUnreadNotificationCount()
  } catch (error) {
    unreadNotificationCount.value = 0
  }
}

function logout() {
  userStore.clearAuth()
  unreadNotificationCount.value = 0
  router.push('/')
}

watch(
  () => userStore.isLoggedIn,
  () => refreshUnreadNotificationCount(),
  { immediate: true }
)

watch(
  () => route.fullPath,
  () => refreshUnreadNotificationCount()
)
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
        <RouterLink to="/catalog">资料中心</RouterLink>
        <RouterLink to="/posts">情报广场</RouterLink>
        <RouterLink v-if="userStore.isLoggedIn" to="/me">用户中心</RouterLink>
        <RouterLink
          v-if="userStore.isLoggedIn"
          class="nav-notification-link"
          :to="{ name: 'user-center', query: { tab: 'notifications' } }"
        >
          <el-badge
            :value="unreadNotificationCount"
            :max="99"
            :hidden="unreadNotificationCount <= 0"
          >
            <span class="nav-link-inner">
              <Bell />
              通知
            </span>
          </el-badge>
        </RouterLink>
        <el-dropdown
          v-if="userStore.isAdmin"
          trigger="click"
          class="admin-dropdown"
          @command="goAdmin"
        >
          <el-button class="admin-dropdown-trigger" :icon="Setting">
            后台管理
            <el-icon class="admin-dropdown-arrow">
              <ArrowDown />
            </el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item
                v-for="link in adminLinks"
                :key="link.to"
                :command="link.to"
              >
                {{ link.label }}
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
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
