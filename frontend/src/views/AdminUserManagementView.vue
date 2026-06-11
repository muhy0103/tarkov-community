<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Search, User, Warning } from '@element-plus/icons-vue'
import { fetchAdminUsers, updateAdminUser } from '../api/adminApi'
import { useUserStore } from '../stores/userStore'

const userStore = useUserStore()

const loading = ref(false)
const actionUserId = ref(null)
const errorMessage = ref('')
const filters = ref({
  role: '',
  status: '',
  keyword: '',
})
const usersPage = ref({
  page: 1,
  size: 10,
  total: 0,
  pages: 0,
  records: [],
})

const roleOptions = [
  { label: '普通玩家', value: 'USER' },
  { label: '版主', value: 'MODERATOR' },
  { label: '管理员', value: 'ADMIN' },
]

const statusOptions = [
  { label: '正常', value: 'NORMAL' },
  { label: '待验证', value: 'PENDING' },
  { label: '禁用', value: 'DISABLED' },
  { label: '封禁', value: 'BANNED' },
]

function resolveError(error, fallback) {
  return error?.response?.data?.message || error?.message || fallback
}

function roleLabel(role) {
  return roleOptions.find((item) => item.value === role)?.label || role || '未知'
}

function roleType(role) {
  if (role === 'ADMIN') {
    return 'danger'
  }
  if (role === 'MODERATOR') {
    return 'warning'
  }
  return 'info'
}

function statusLabel(status) {
  return statusOptions.find((item) => item.value === status)?.label || status || '未知'
}

function statusType(status) {
  if (status === 'NORMAL') {
    return 'success'
  }
  if (status === 'PENDING') {
    return 'warning'
  }
  if (status === 'BANNED') {
    return 'danger'
  }
  return 'warning'
}

function formatDate(value) {
  if (!value) {
    return '暂无记录'
  }

  return new Date(value).toLocaleString('zh-CN', {
    hour12: false,
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

async function loadUsers(page = usersPage.value.page) {
  loading.value = true
  errorMessage.value = ''
  usersPage.value.page = page

  try {
    usersPage.value = await fetchAdminUsers({
      page,
      size: usersPage.value.size,
      role: filters.value.role || undefined,
      status: filters.value.status || undefined,
      keyword: filters.value.keyword.trim() || undefined,
    })
  } catch (error) {
    errorMessage.value = resolveError(error, '用户管理列表暂时无法加载')
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  filters.value = {
    role: '',
    status: '',
    keyword: '',
  }
  loadUsers(1)
}

async function updateUser(row, payload) {
  actionUserId.value = row.id

  try {
    const updated = await updateAdminUser(row.id, payload)
    Object.assign(row, updated)
    ElMessage.success('用户状态已更新')
  } catch (error) {
    ElMessage.error(resolveError(error, '用户操作失败'))
  } finally {
    actionUserId.value = null
  }
}

onMounted(() => loadUsers(1))
</script>

<template>
  <div class="admin-review-view">
    <section class="admin-hero">
      <div>
        <h2>用户管理</h2>
        <p>查看社区成员的账号状态、角色和贡献值，快速处理版主授权和账号禁用。</p>
      </div>
      <el-button :icon="Refresh" :loading="loading" @click="loadUsers()">
        刷新用户
      </el-button>
    </section>

    <el-alert
      v-if="!userStore.isAdmin"
      title="当前账号不是管理员，页面仅作为开发阶段预览。"
      type="info"
      show-icon
      class="home-alert"
    />

    <el-alert
      v-if="errorMessage"
      :title="errorMessage"
      type="warning"
      show-icon
      class="home-alert"
    />

    <section class="board-toolbar">
      <div class="admin-review-filters">
        <el-input
          v-model="filters.keyword"
          placeholder="搜索用户名、昵称或邮箱"
          clearable
          @keyup.enter="loadUsers(1)"
        />
        <el-select v-model="filters.role" placeholder="全部角色" clearable>
          <el-option
            v-for="option in roleOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
        <el-select v-model="filters.status" placeholder="全部状态" clearable>
          <el-option
            v-for="option in statusOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
      </div>

      <div class="board-actions">
        <el-button type="primary" :icon="Search" @click="loadUsers(1)">
          查询
        </el-button>
        <el-button :icon="Refresh" @click="resetFilters">
          重置
        </el-button>
      </div>
    </section>

    <section class="admin-table-card">
      <el-table v-loading="loading" :data="usersPage.records" row-key="id">
        <el-table-column label="玩家" min-width="260">
          <template #default="{ row }">
            <div class="admin-user-cell">
              <User />
              <div>
                <strong>{{ row.nickname }}</strong>
                <span>{{ row.username }} · {{ row.email || '未填写邮箱' }}</span>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="角色" width="120">
          <template #default="{ row }">
            <el-tag :type="roleType(row.role)" effect="plain">
              {{ roleLabel(row.role) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" effect="plain">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="贡献" width="100">
          <template #default="{ row }">
            <span class="admin-table-muted">{{ row.contribution || 0 }}</span>
          </template>
        </el-table-column>

        <el-table-column label="时间" min-width="210">
          <template #default="{ row }">
            <div class="admin-time-stack">
              <span>注册 {{ formatDate(row.createdAt) }}</span>
              <span>登录 {{ formatDate(row.lastLoginAt) }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <div class="admin-row-actions">
              <el-button
                v-if="row.role === 'USER'"
                size="small"
                :loading="actionUserId === row.id"
                @click="updateUser(row, { role: 'MODERATOR' })"
              >
                设为版主
              </el-button>
              <el-button
                v-else-if="row.role === 'MODERATOR'"
                size="small"
                :loading="actionUserId === row.id"
                @click="updateUser(row, { role: 'USER' })"
              >
                恢复普通
              </el-button>
              <el-button v-else size="small" disabled>管理员</el-button>
              <el-button
                v-if="row.status === 'NORMAL'"
                size="small"
                :icon="Warning"
                :loading="actionUserId === row.id"
                @click="updateUser(row, { status: 'DISABLED' })"
              >
                禁用
              </el-button>
              <el-button
                v-else
                size="small"
                :loading="actionUserId === row.id"
                @click="updateUser(row, { status: 'NORMAL' })"
              >
                恢复
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="!loading && usersPage.records.length === 0" class="admin-table-empty">
        当前没有符合条件的用户
      </div>

      <el-pagination
        v-if="usersPage.pages > 1"
        class="board-pagination"
        background
        layout="prev, pager, next"
        :current-page="usersPage.page"
        :page-size="usersPage.size"
        :total="usersPage.total"
        @current-change="loadUsers"
      />
    </section>
  </div>
</template>
