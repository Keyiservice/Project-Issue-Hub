<template>
  <div class="login-shell">
    <section class="hero-panel opl-glass-card">
      <div class="hero-top">
        <div class="hero-kicker">{{ t('login.kicker') }}</div>
        <el-select v-model="localeValue" class="hero-language" size="large">
          <el-option
            v-for="item in localeOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </div>

      <h1 class="hero-title">{{ t('login.heroTitle') }}</h1>
      <p class="hero-desc">{{ t('login.heroDesc') }}</p>

      <div class="hero-metrics">
        <div class="metric-tile">{{ t('login.metric1') }}</div>
        <div class="metric-tile">{{ t('login.metric2') }}</div>
        <div class="metric-tile">{{ t('login.metric3') }}</div>
      </div>

      <div class="hero-footnote">{{ t('login.footnote') }}</div>
    </section>

    <section class="login-panel opl-glass-card">
      <div class="panel-head">
        <p class="opl-section-title">{{ t('login.signIn') }}</p>
        <h2 class="panel-title">{{ t('login.title') }}</h2>
        <p class="panel-desc">{{ t('login.desc') }}</p>
      </div>

      <el-form :model="form" class="login-form" @keyup.enter="handleLogin">
        <el-form-item>
          <el-input v-model="form.username" size="large" :placeholder="t('login.usernamePlaceholder')" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" size="large" type="password" show-password :placeholder="t('login.passwordPlaceholder')" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" class="submit-button" :loading="loading" @click="handleLogin">
            {{ t('login.enter') }}
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-hints">
        <div class="hint-item">
          <span class="hint-index">01</span>
          <span>{{ t('login.hint1') }}</span>
        </div>
        <div class="hint-item">
          <span class="hint-index">02</span>
          <span>{{ t('login.hint2') }}</span>
        </div>
        <div class="hint-item">
          <span class="hint-index">03</span>
          <span>{{ t('login.hint3') }}</span>
        </div>
      </div>
    </section>

    <el-dialog v-model="passwordDialogVisible" width="460px" :show-close="false" class="auth-dialog">
      <template #header>
        <div class="dialog-title">{{ t('login.passwordChangeTitle') }}</div>
      </template>
      <div class="dialog-desc">{{ t('login.passwordChangeDesc') }}</div>
      <el-form :model="passwordForm" class="dialog-form">
        <el-form-item>
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            show-password
            :placeholder="t('login.newPasswordPlaceholder')"
          />
        </el-form-item>
        <el-form-item>
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            show-password
            :placeholder="t('login.confirmPasswordPlaceholder')"
          />
        </el-form-item>
      </el-form>
      <div class="dialog-hint">{{ t('login.passwordPolicyHint') }}</div>
      <template #footer>
        <el-button type="primary" :loading="dialogLoading" @click="handlePasswordChange">
          {{ t('login.passwordChangeAction') }}
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="mfaDialogVisible" width="520px" :show-close="false" class="auth-dialog">
      <template #header>
        <div class="dialog-title">
          {{ mfaMode === 'setup' ? t('login.mfaSetupTitle') : t('login.mfaVerifyTitle') }}
        </div>
      </template>
      <div class="dialog-desc">
        {{ mfaMode === 'setup' ? t('login.mfaSetupDesc') : t('login.mfaVerifyDesc') }}
      </div>
      <div v-if="mfaMode === 'setup'" class="mfa-setup">
        <div class="mfa-qr">
          <img v-if="mfaQrDataUrl" :src="mfaQrDataUrl" alt="MFA QR" />
        </div>
        <div class="mfa-secret">
          <div class="mfa-label">{{ t('login.mfaSecretLabel') }}</div>
          <div class="mfa-code">{{ mfaSecret }}</div>
        </div>
      </div>
      <el-form :model="mfaForm" class="dialog-form">
        <el-form-item>
          <el-input v-model="mfaForm.code" :placeholder="t('login.mfaCodePlaceholder')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" :loading="dialogLoading" @click="handleMfaVerify">
          {{ t('login.mfaVerifyAction') }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { localeLabelMap, supportedLocales, type AppLocale } from '@/i18n/locales'
import { useAuthStore } from '@/stores/auth'
import { useLocaleStore } from '@/stores/locale'
import { changePassword, setupMfa, verifyMfa } from '@/api/auth'
import QRCode from 'qrcode'

const router = useRouter()
const authStore = useAuthStore()
const localeStore = useLocaleStore()
const { t } = useI18n()
const loading = ref(false)
const dialogLoading = ref(false)
const passwordDialogVisible = ref(false)
const mfaDialogVisible = ref(false)
const mfaMode = ref<'setup' | 'verify'>('setup')
const mfaToken = ref('')
const mfaQrDataUrl = ref('')
const mfaSecret = ref('')

const form = reactive({
  username: 'admin',
  password: '123456'
})

const passwordForm = reactive({
  newPassword: '',
  confirmPassword: ''
})

const mfaForm = reactive({
  code: ''
})

const localeOptions = supportedLocales.map((value) => ({
  value,
  label: localeLabelMap[value]
}))

const localeValue = computed({
  get: () => localeStore.currentLocale,
  set: (value: AppLocale) => localeStore.setLocale(value)
})

async function handleLogin() {
  if (!form.username || !form.password) {
    ElMessage.warning(t('login.missingCredentials'))
    return
  }
  loading.value = true
  try {
    const data = await authStore.signIn(form)
    if (data.passwordChangeRequired) {
      passwordDialogVisible.value = true
      return
    }
    if (data.mfaSetupRequired) {
      await startMfaSetup(data.mfaToken || '')
      return
    }
    if (data.mfaRequired) {
      startMfaVerify(data.mfaToken || '')
      return
    }
    router.push('/dashboard')
  } finally {
    loading.value = false
  }
}

async function handlePasswordChange() {
  if (!passwordForm.newPassword || !passwordForm.confirmPassword) {
    ElMessage.warning(t('login.passwordMissing'))
    return
  }
  dialogLoading.value = true
  try {
    await changePassword({
      newPassword: passwordForm.newPassword,
      confirmPassword: passwordForm.confirmPassword
    })
    passwordDialogVisible.value = false
    form.password = passwordForm.newPassword
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
    const data = await authStore.signIn(form)
    if (data.mfaSetupRequired) {
      await startMfaSetup(data.mfaToken || '')
      return
    }
    if (data.mfaRequired) {
      startMfaVerify(data.mfaToken || '')
      return
    }
    router.push('/dashboard')
  } finally {
    dialogLoading.value = false
  }
}

async function startMfaSetup(token: string) {
  mfaMode.value = 'setup'
  mfaToken.value = token
  mfaForm.code = ''
  const { data } = await setupMfa({ mfaToken: token })
  mfaSecret.value = data.secret
  mfaQrDataUrl.value = await QRCode.toDataURL(data.otpauthUrl)
  mfaDialogVisible.value = true
}

function startMfaVerify(token: string) {
  mfaMode.value = 'verify'
  mfaToken.value = token
  mfaForm.code = ''
  mfaDialogVisible.value = true
}

async function handleMfaVerify() {
  if (!mfaForm.code) {
    ElMessage.warning(t('login.mfaCodeMissing'))
    return
  }
  dialogLoading.value = true
  try {
    const { data } = await verifyMfa({ mfaToken: mfaToken.value, code: mfaForm.code })
    authStore.applyLogin(data)
    mfaDialogVisible.value = false
    mfaForm.code = ''
    router.push('/dashboard')
  } finally {
    dialogLoading.value = false
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

.hero-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.hero-language {
  width: 140px;
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
  font-size: 56px;
  line-height: 1.02;
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
  color: var(--opl-text);
  font-weight: 700;
  line-height: 1.6;
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

.auth-dialog :deep(.el-dialog) {
  border-radius: 18px;
}

.dialog-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--opl-text);
}

.dialog-desc {
  margin-bottom: 14px;
  color: var(--opl-muted);
}

.dialog-form {
  margin-top: 12px;
}

.dialog-hint {
  margin-top: 8px;
  font-size: 12px;
  color: var(--opl-muted);
}

.mfa-setup {
  display: grid;
  grid-template-columns: 140px 1fr;
  gap: 16px;
  margin: 16px 0;
}

.mfa-qr {
  width: 140px;
  height: 140px;
  padding: 10px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(24, 33, 43, 0.08);
  display: flex;
  align-items: center;
  justify-content: center;
}

.mfa-qr img {
  width: 120px;
  height: 120px;
}

.mfa-secret {
  display: flex;
  flex-direction: column;
  gap: 8px;
  justify-content: center;
}

.mfa-label {
  font-size: 12px;
  color: var(--opl-muted);
}

.mfa-code {
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 0.08em;
  color: var(--opl-text);
  word-break: break-all;
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

  .hero-top {
    flex-direction: column;
    align-items: flex-start;
  }

  .hero-language {
    width: 100%;
  }

  .hero-title {
    font-size: 42px;
  }

  .hero-metrics {
    grid-template-columns: 1fr;
  }
}
</style>
