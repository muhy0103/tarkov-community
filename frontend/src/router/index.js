import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import AdminDashboardView from '../views/AdminDashboardView.vue'
import AdminPostReviewView from '../views/AdminPostReviewView.vue'
import AdminUserManagementView from '../views/AdminUserManagementView.vue'
import LoginView from '../views/LoginView.vue'
import PostBoardView from '../views/PostBoardView.vue'
import PostCreateView from '../views/PostCreateView.vue'
import PostDetailView from '../views/PostDetailView.vue'

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
    path: '/admin/dashboard',
    name: 'admin-dashboard',
    component: AdminDashboardView,
    meta: {
      title: '后台概览',
    },
  },
  {
    path: '/admin/posts',
    name: 'admin-posts',
    component: AdminPostReviewView,
    meta: {
      title: '帖子审核',
    },
  },
  {
    path: '/admin/users',
    name: 'admin-users',
    component: AdminUserManagementView,
    meta: {
      title: '用户管理',
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
})

export default router
