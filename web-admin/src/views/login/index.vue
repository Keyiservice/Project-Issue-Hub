<template>
  <div class="login-shell">
    <section class="hero-panel opl-glass-card">
      <div class="hero-kicker">Project Issue Hub / Delivery Control</div>
      <h1 class="hero-title">让问题信息像现场一样清楚，而不是像 Excel 一样模糊。</h1>
      <p class="hero-desc">
        用图片、视频、责任人、截止时间和状态闭环，把非标设备项目里的问题协作从“反复追问”变成“可直接推进”。
      </p>

      <div class="hero-metrics">
        <div class="metric-tile">
          <span class="metric-label">10 秒理解</span>
          <strong class="metric-value">问题本质</strong>
        </div>
        <div class="metric-tile">
          <span class="metric-label">统一责任</span>
          <strong class="metric-value">状态闭环</strong>
        </div>
        <div class="metric-tile">
          <span class="metric-label">现场优先</span>
          <strong class="metric-value">图像驱动</strong>
        </div>
      </div>

      <div class="hero-footnote">
        推荐先用 `admin / 123456` 进入系统，再继续创建项目和问题主链路数据。
      </div>
    </section>

    <section class="login-panel opl-glass-card">
      <div class="panel-head">
        <p class="opl-section-title">Sign In</p>
        <h2 class="panel-title">登录 Project Issue Hub</h2>
        <p class="panel-desc">使用统一账号进入问题库、项目协同和统计分析视图。</p>
      </div>

      <el-form :model="form" class="login-form" @keyup.enter="handleLogin">
        <el-form-item>
          <el-input v-model="form.username" size="large" placeholder="请输入账号" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" size="large" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" class="submit-button" :loading="loading" @click="handleLogin">
            进入系统
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-hints">
        <div class="hint-item">
          <span class="hint-index">01</span>
          <span>Web 负责管理和分析，小程序负责现场录入和跟进。</span>
        </div>
        <div class="hint-item">
          <span class="hint-index">02</span>
          <span>状态流转、权限和编号统一由后端控制。</span>
        </div>
        <div class="hint-item">
          <span class="hint-index">03</span>
          <span>上传图片和视频后，管理层打开即可理解现场问题。</span>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)

const form = reactive({
  username: 'admin',
  password: '123456'
})

async function handleLogin() {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入账号和密码')
    return
  }
  loading.value = true
  try {
    await authStore.signIn(form)
    router.push('/dashboard')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-shell {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(420px, 520px);
  gap: 20px;
  min-height: 100vh;
  padding: 24px;
}

.hero-panel,
.login-panel {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: calc(100vh - 48px);
  padding: 36px;
}

.hero-panel {
  background:
    radial-gradient(circle at top right, rgba(15, 118, 110, 0.18), transparent 30%),
    radial-gradient(circle at left, rgba(203, 93, 31, 0.16), transparent 28%),
    linear-gradient(145deg, rgba(255, 250, 244, 0.96), rgba(245, 255, 250, 0.78));
}

.hero-kicker {
  font-size: 12px;
  letter-spacing: 0.24em;
  text-transform: uppercase;
  color: var(--opl-muted);
}

.hero-title {
  max-width: 760px;
  margin: 18px 0 16px;
  font-size: 62px;
  line-height: 0.98;
  letter-spacing: -0.04em;
  color: #16212b;
}

.hero-desc {
  max-width: 760px;
  font-size: 17px;
  line-height: 1.9;
  color: var(--opl-muted);
}

.hero-metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
  margin-top: 32px;
}

.metric-tile {
  padding: 20px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.74);
  border: 1px solid rgba(24, 33, 43, 0.08);
}

.metric-label {
  display: block;
  font-size: 12px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--opl-subtle);
}

.metric-value {
  display: block;
  margin-top: 14px;
  font-size: 24px;
  line-height: 1.2;
  color: var(--opl-text);
}

.hero-footnote {
  margin-top: 28px;
  color: var(--opl-muted);
  font-size: 14px;
}

.login-panel {
  max-width: 520px;
  justify-self: end;
  background: linear-gradient(145deg, rgba(255, 252, 247, 0.96), rgba(255, 255, 255, 0.9));
}

.panel-head {
  margin-bottom: 32px;
}

.panel-title {
  margin: 10px 0 0;
  font-size: 34px;
  line-height: 1.08;
  color: var(--opl-text);
}

.panel-desc {
  margin: 12px 0 0;
  line-height: 1.8;
  color: var(--opl-muted);
}

.login-form {
  margin-top: 8px;
}

.submit-button {
  width: 100%;
  height: 52px;
}

.login-hints {
  margin-top: 28px;
  display: grid;
  gap: 14px;
}

.hint-item {
  display: flex;
  gap: 14px;
  align-items: center;
  color: var(--opl-muted);
  line-height: 1.7;
}

.hint-index {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border-radius: 50%;
  font-size: 12px;
  font-weight: 700;
  color: var(--opl-accent-deep);
  background: var(--opl-accent-soft);
}

@media (max-width: 1180px) {
  .login-shell {
    grid-template-columns: 1fr;
    padding: 16px;
  }

  .hero-panel,
  .login-panel {
    min-height: auto;
    padding: 26px;
  }

  .login-panel {
    justify-self: stretch;
    max-width: none;
  }

  .hero-title {
    font-size: 42px;
  }

  .hero-metrics {
    grid-template-columns: 1fr;
  }
}
</style>
