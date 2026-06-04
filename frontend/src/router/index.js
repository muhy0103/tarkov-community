import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import LoginView from '../views/LoginView.vue'

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
