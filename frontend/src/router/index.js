import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/userStore'
import AccessDeniedView from '../views/AccessDeniedView.vue'
import AdminAmmoManagementView from '../views/AdminAmmoManagementView.vue'
import AdminBossManagementView from '../views/AdminBossManagementView.vue'
import HomeView from '../views/HomeView.vue'
import AdminDashboardView from '../views/AdminDashboardView.vue'
import AdminCategoryManagementView from '../views/AdminCategoryManagementView.vue'
import AdminCommentReviewView from '../views/AdminCommentReviewView.vue'
import AdminHideoutStationManagementView from '../views/AdminHideoutStationManagementView.vue'
import AdminHideoutUpgradeManagementView from '../views/AdminHideoutUpgradeManagementView.vue'
import AdminItemManagementView from '../views/AdminItemManagementView.vue'
import AdminMapExtractManagementView from '../views/AdminMapExtractManagementView.vue'
import AdminMapManagementView from '../views/AdminMapManagementView.vue'
import AdminPostReviewView from '../views/AdminPostReviewView.vue'
import AdminQuestManagementView from '../views/AdminQuestManagementView.vue'
import AdminTraderManagementView from '../views/AdminTraderManagementView.vue'
import AdminUserManagementView from '../views/AdminUserManagementView.vue'
import AdminWeaponManagementView from '../views/AdminWeaponManagementView.vue'
import LoginView from '../views/LoginView.vue'
import PostBoardView from '../views/PostBoardView.vue'
import PostCreateView from '../views/PostCreateView.vue'
import PostDetailView from '../views/PostDetailView.vue'
import UserCenterView from '../views/UserCenterView.vue'

const routes = [
  {
    path: '/',
    name: 'home',
    component: HomeView,
    meta: {
      title: '社区概览',
    },
  },
  {
    path: '/login',
    name: 'login',
    component: LoginView,
    meta: {
      title: '登录注册',
    },
  },
  {
    path: '/access-denied',
    name: 'access-denied',
    component: AccessDeniedView,
    meta: {
      title: '无权限访问',
    },
  },
  {
    path: '/me',
    name: 'user-center',
    component: UserCenterView,
    meta: {
      title: '用户中心',
      requiresAuth: true,
    },
  },
  {
    path: '/admin/dashboard',
    name: 'admin-dashboard',
    component: AdminDashboardView,
    meta: {
      title: '后台概览',
      requiresAuth: true,
      requiresAdmin: true,
    },
  },
  {
    path: '/admin/posts',
    name: 'admin-posts',
    component: AdminPostReviewView,
    meta: {
      title: '帖子审核',
      requiresAuth: true,
      requiresAdmin: true,
    },
  },
  {
    path: '/admin/users',
    name: 'admin-users',
    component: AdminUserManagementView,
    meta: {
      title: '用户管理',
      requiresAuth: true,
      requiresAdmin: true,
    },
  },
  {
    path: '/admin/comments',
    name: 'admin-comments',
    component: AdminCommentReviewView,
    meta: {
      title: '评论审核',
      requiresAuth: true,
      requiresAdmin: true,
    },
  },
  {
    path: '/admin/categories',
    name: 'admin-categories',
    component: AdminCategoryManagementView,
    meta: {
      title: '分区管理',
      requiresAuth: true,
      requiresAdmin: true,
    },
  },
  {
    path: '/admin/maps',
    name: 'admin-maps',
    component: AdminMapManagementView,
    meta: {
      title: '地图管理',
      requiresAuth: true,
      requiresAdmin: true,
    },
  },
  {
    path: '/admin/map-extracts',
    name: 'admin-map-extracts',
    component: AdminMapExtractManagementView,
    meta: {
      title: '撤离点管理',
      requiresAuth: true,
      requiresAdmin: true,
    },
  },
  {
    path: '/admin/traders',
    name: 'admin-traders',
    component: AdminTraderManagementView,
    meta: {
      title: '商人管理',
      requiresAuth: true,
      requiresAdmin: true,
    },
  },
  {
    path: '/admin/quests',
    name: 'admin-quests',
    component: AdminQuestManagementView,
    meta: {
      title: '任务管理',
      requiresAuth: true,
      requiresAdmin: true,
    },
  },
  {
    path: '/admin/items',
    name: 'admin-items',
    component: AdminItemManagementView,
    meta: {
      title: '物品管理',
      requiresAuth: true,
      requiresAdmin: true,
    },
  },
  {
    path: '/admin/weapons',
    name: 'admin-weapons',
    component: AdminWeaponManagementView,
    meta: {
      title: '武器管理',
      requiresAuth: true,
      requiresAdmin: true,
    },
  },
  {
    path: '/admin/ammo',
    name: 'admin-ammo',
    component: AdminAmmoManagementView,
    meta: {
      title: '弹药管理',
      requiresAuth: true,
      requiresAdmin: true,
    },
  },
  {
    path: '/admin/hideout/stations',
    name: 'admin-hideout-stations',
    component: AdminHideoutStationManagementView,
    meta: {
      title: '藏身处管理',
      requiresAuth: true,
      requiresAdmin: true,
    },
  },
  {
    path: '/admin/hideout/upgrades',
    name: 'admin-hideout-upgrades',
    component: AdminHideoutUpgradeManagementView,
    meta: {
      title: '升级管理',
      requiresAuth: true,
      requiresAdmin: true,
    },
  },
  {
    path: '/admin/bosses',
    name: 'admin-bosses',
    component: AdminBossManagementView,
    meta: {
      title: 'Boss 管理',
      requiresAuth: true,
      requiresAdmin: true,
    },
  },
  {
    path: '/posts',
    name: 'post-board',
    component: PostBoardView,
    meta: {
      title: '情报广场',
    },
  },
  {
    path: '/posts/new',
    name: 'post-create',
    component: PostCreateView,
    meta: {
      title: '发布情报',
    },
  },
  {
    path: '/posts/:id',
    name: 'post-detail',
    component: PostDetailView,
    meta: {
      title: '帖子详情',
    },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  document.title = to.meta.title
    ? `${to.meta.title} - 逃离塔科夫玩家情报社区`
    : '逃离塔科夫玩家情报社区'

  const userStore = useUserStore()

  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    return {
      name: 'login',
      query: {
        redirect: to.fullPath,
      },
    }
  }

  if (to.meta.requiresAdmin && !userStore.isAdmin) {
    return {
      name: 'access-denied',
      query: {
        from: to.fullPath,
      },
    }
  }
})

export default router
