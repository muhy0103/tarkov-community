<script setup>
import { House, Lock, User } from '@element-plus/icons-vue'
</script>

<template>
  <section class="access-denied-view">
    <Lock />
    <div>
      <h2>无权限访问后台</h2>
      <p>后台管理仅限管理员账号使用。普通玩家可以继续浏览社区内容、发帖、评论和参与情报讨论。</p>
      <div class="access-actions">
        <RouterLink to="/">
          <el-button type="primary" :icon="House">返回社区概览</el-button>
        </RouterLink>
        <RouterLink to="/login">
          <el-button :icon="User">切换账号</el-button>
        </RouterLink>
      </div>
    </div>
  </section>
</template>
