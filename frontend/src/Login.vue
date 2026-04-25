<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useNcmUserStore } from './user'
import { ncmApi } from './api'

const tab = ref('password')
const store = useNcmUserStore()

const phone = ref('')
const password = ref('')
const captcha = ref('')
const rawCookie = ref('')
const songId = ref('29764545')
const losslessResult = ref(null)

async function sendCaptcha() {
  if (!phone.value) {
    ElMessage.warning('请先输入手机号')
    return
  }
  await ncmApi.sendCaptcha(phone.value)
  ElMessage.success('验证码已发送')
}

async function submitLogin() {
  try {
    if (tab.value === 'password') {
      await store.loginWithPassword(phone.value, password.value)
    } else if (tab.value === 'captcha') {
      await store.loginWithCaptcha(phone.value, captcha.value)
    } else {
      await store.loginWithCookie(rawCookie.value)
    }
    await store.refreshProfile()
    ElMessage.success('网易云登录成功')
  } catch (e) {
    ElMessage.error(e?.message || '登录失败')
  }
}

async function testLossless() {
  try {
    const resp = await ncmApi.testLosslessUrl(Number(songId.value))
    losslessResult.value = resp.data.data
    ElMessage.success('已获取测试结果')
  } catch (e) {
    ElMessage.error(e?.message || '无损 URL 获取失败')
  }
}

onMounted(() => {
  store.hydrate()
  rawCookie.value = store.rawCookie
})
</script>

<template>
  <el-card style="max-width: 760px; margin: 24px auto">
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center;">
        <span>网易云黑胶账号登录</span>
        <el-tag :type="store.loggedIn ? 'success' : 'info'">
          {{ store.loggedIn ? '已登录' : '未登录' }}
        </el-tag>
      </div>
    </template>

    <el-tabs v-model="tab">
      <el-tab-pane label="手机号+密码" name="password">
        <el-form label-width="90px">
          <el-form-item label="手机号"><el-input v-model="phone" /></el-form-item>
          <el-form-item label="密码"><el-input v-model="password" type="password" show-password /></el-form-item>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="手机号+验证码" name="captcha">
        <el-form label-width="90px">
          <el-form-item label="手机号"><el-input v-model="phone" /></el-form-item>
          <el-form-item label="验证码">
            <div style="display:flex;gap:8px;width:100%">
              <el-input v-model="captcha" />
              <el-button @click="sendCaptcha">发送验证码</el-button>
            </div>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="粘贴 Cookie" name="cookie">
        <el-form label-width="90px">
          <el-form-item label="NCM Cookie">
            <el-input
              v-model="rawCookie"
              type="textarea"
              :rows="5"
              placeholder="MUSIC_U=xxx; __csrf=xxx; ..."
            />
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>

    <div style="display:flex;gap:8px;flex-wrap:wrap">
      <el-button type="primary" :loading="store.loading" @click="submitLogin">登录</el-button>
      <el-button @click="store.refreshProfile">刷新登录态</el-button>
      <el-button @click="store.logoutLocal">清空本地态</el-button>
    </div>

    <el-divider />
    <el-form inline>
      <el-form-item label="测试歌曲ID">
        <el-input v-model="songId" style="width: 180px" />
      </el-form-item>
      <el-form-item>
        <el-button type="success" @click="testLossless">测试无损 URL</el-button>
      </el-form-item>
    </el-form>
    <el-alert
      v-if="losslessResult"
      title="无损接口返回"
      type="success"
      :closable="false"
      style="margin-top: 10px"
    >
      <pre style="white-space: pre-wrap; margin: 0">{{ JSON.stringify(losslessResult, null, 2) }}</pre>
    </el-alert>
  </el-card>
</template>
