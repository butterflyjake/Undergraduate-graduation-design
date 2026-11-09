<template>
  <div class="login-page">
    <el-card class="login-card">
      <div class="title">
        <el-icon class="logo-icon"><FirstAidKit /></el-icon>
        <h2>高校AED器械管理系统</h2>
        <p>College AED Equipment Management System</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" size="large" @keyup.enter="handleLogin">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="请输入账号" :prefix-icon="User" clearable />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" :prefix-icon="Lock" show-password />
        </el-form-item>
        <el-button type="primary" class="login-btn" :loading="loading" @click="handleLogin">登 录</el-button>
      </el-form>
      <div class="tip">默认管理员账号: admin / 123456</div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)
const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    await userStore.login(form)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0f6cbd 0%, #2b9a8f 100%);
}

.login-card {
  width: 420px;
  padding: 20px 10px;
  border-radius: 8px;
}

.title {
  text-align: center;
  margin-bottom: 28px;
}

.logo-icon {
  font-size: 44px;
  color: #d94f4f;
  margin-bottom: 8px;
}

.title h2 {
  font-size: 20px;
  color: #303133;
}

.title p {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.login-btn {
  width: 100%;
  margin-top: 6px;
}

.tip {
  margin-top: 16px;
  text-align: center;
  font-size: 12px;
  color: #909399;
}
</style>
