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
const registerResult = ref(null)

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
  loading.value = true
  errorMessage.value = ''
  registerResult.value = null

  try {
    const auth = await login(loginForm)
    userStore.setAuth(auth.token, auth.user)
    router.push(resolveRedirect())
  } catch (error) {
    errorMessage.value = resolveError(error, '登录失败，请检查用户名、密码或邮箱验证状态。')
  } finally {
    loading.value = false
  }
}

async function submitRegister() {
  loading.value = true
  errorMessage.value = ''
  registerResult.value = null

  try {
    registerResult.value = await register(registerForm)
    registerForm.password = ''
  } catch (error) {
    errorMessage.value = resolveError(error, '注册失败，请稍后重试。')
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

function resolveError(error, fallback) {
  return error?.response?.data?.message || error?.message || fallback
}
</script>

<template>
  <section class="auth-layout">
    <div class="auth-copy">
      <h2>进入社区身份系统</h2>
      <p>注册后需要先完成邮箱确认，账号才会转为可登录状态。确认后即可参与发帖、评论、收藏和个人中心功能。</p>
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
              注册并发送验证邮件
            </el-button>
          </el-form>
        </el-tab-pane>
      </el-tabs>

      <el-alert
        v-if="registerResult"
        :title="registerResult.message || '注册成功，请查收邮箱'"
        type="success"
        show-icon
        class="auth-alert auth-success"
        :closable="false"
      >
        <div class="auth-success-body">
          <p>
            账号 {{ registerResult.username }} 已创建，当前状态为待邮箱验证。请打开邮箱中的确认链接，完成后再回到登录页进入社区。
          </p>
        </div>
      </el-alert>

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
