<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { login, register } from '../api/authApi'
import { useUserStore } from '../stores/userStore'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const activeTab = ref('login')
const loading = ref(false)
const errorMessage = ref('')

const loginForm = reactive({
  username: '',
  password: '',
})

const registerForm = reactive({
  username: '',
  password: '',
  nickname: '',
  email: '',
})

async function submitLogin() {
  await submitAuth(() => login(loginForm))
}

async function submitRegister() {
  await submitAuth(() => register(registerForm))
}

async function submitAuth(action) {
  loading.value = true
  errorMessage.value = ''

  try {
    const auth = await action()
    userStore.setAuth(auth.token, auth.user)
    router.push(resolveRedirect())
  } catch (error) {
    errorMessage.value =
      error?.response?.data?.message ||
      error?.message ||
      '请求失败，请稍后重试。'
  } finally {
    loading.value = false
  }
}

function resolveRedirect() {
  const redirect = route.query.redirect

  if (typeof redirect === 'string' && redirect.startsWith('/')) {
    return redirect
  }

  return '/'
}
</script>

<template>
  <section class="auth-layout">
    <div class="auth-copy">
      <h2>进入社区身份系统</h2>
      <p>登录后前端会保存后端返回的 token 和用户信息，后续发帖、评论、点赞和后台管理都可以接入这个用户状态。</p>
    </div>

    <div class="auth-panel">
      <el-tabs v-model="activeTab" stretch>
        <el-tab-pane label="登录" name="login">
          <el-form label-position="top" @submit.prevent>
            <el-form-item label="用户名">
              <el-input v-model="loginForm.username" placeholder="pmc_rookie" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="loginForm.password" type="password" show-password />
            </el-form-item>
            <el-button type="primary" :loading="loading" @click="submitLogin">
              登录
            </el-button>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="注册" name="register">
          <el-form label-position="top" @submit.prevent>
            <el-form-item label="用户名">
              <el-input v-model="registerForm.username" placeholder="woods_scout" />
            </el-form-item>
            <el-form-item label="昵称">
              <el-input v-model="registerForm.nickname" placeholder="森林侦察员" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="registerForm.email" placeholder="woods@example.com" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="registerForm.password" type="password" show-password />
            </el-form-item>
            <el-button type="primary" :loading="loading" @click="submitRegister">
              注册并登录
            </el-button>
          </el-form>
        </el-tab-pane>
      </el-tabs>

      <el-alert
        v-if="errorMessage"
        :title="errorMessage"
        type="warning"
        show-icon
        class="auth-alert"
      />
    </div>
  </section>
</template>
