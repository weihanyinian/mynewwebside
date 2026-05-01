<script setup>
import { onMounted, onUnmounted, ref, watch } from 'vue'
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

/** 扫码登录 */
const qrImg = ref('')
const qrUnikey = ref('')
const qrStatus = ref('')
const qrPolling = ref(false)
let qrPollTimer = null

function pickNcmUnikey(inner) {
  const d = inner?.data ?? inner
  if (d?.unikey) return d.unikey
  if (d?.data?.unikey) return d.data.unikey
  return ''
}

function stopQrPoll() {
  if (qrPollTimer != null) {
    clearInterval(qrPollTimer)
    qrPollTimer = null
  }
  qrPolling.value = false
}

async function refreshQrCode() {
  stopQrPoll()
  qrImg.value = ''
  qrUnikey.value = ''
  qrStatus.value = '正在获取二维码…'
  try {
    const keyResp = await ncmApi.qrLoginKey()
    const innerKey = keyResp.data?.data
    const unikey = pickNcmUnikey(innerKey)
    if (!unikey) {
      throw new Error('未获取到二维码 key')
    }
    qrUnikey.value = unikey
    const createResp = await ncmApi.qrLoginCreate(unikey, true)
    const payload = createResp.data?.data
    const img = payload?.data?.qrimg ?? payload?.qrimg
    if (!img) {
      throw new Error('未返回二维码图片')
    }
    qrImg.value = img
    qrStatus.value = '请使用网易云音乐 App 扫码'
    qrPolling.value = true
    qrPollTimer = setInterval(async () => {
      if (!qrUnikey.value) return
      try {
        const checkResp = await ncmApi.qrLoginCheck(qrUnikey.value)
        const body = checkResp.data?.data
        const code = body?.code
        if (code === 800) {
          qrStatus.value = '二维码已过期，请点击刷新'
          stopQrPoll()
          return
        }
        if (code === 801) {
          qrStatus.value = '等待扫码…'
          return
        }
        if (code === 802) {
          qrStatus.value = '请在手机上确认登录'
          return
        }
        if (code === 803) {
          stopQrPoll()
          store.loginType = 'qr'
          store.loggedIn = true
          await store.refreshProfile()
          qrStatus.value = '登录成功'
          ElMessage.success('网易云扫码登录成功')
          return
        }
        qrStatus.value = body?.message || `状态码 ${code ?? '未知'}`
      } catch (e) {
        stopQrPoll()
        ElMessage.error(e?.message || '轮询失败')
      }
    }, 2000)
  } catch (e) {
    qrStatus.value = ''
    ElMessage.error(e?.message || '获取二维码失败')
  }
}

watch(tab, (v) => {
  if (v !== 'qr') stopQrPoll()
})

onUnmounted(() => stopQrPoll())

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

      <el-tab-pane label="扫码登录" name="qr">
        <el-space direction="vertical" alignment="start" :size="12" style="width: 100%">
          <el-button type="primary" :disabled="qrPolling" @click="refreshQrCode">
            {{ qrImg ? '刷新二维码' : '生成二维码' }}
          </el-button>
          <div v-if="qrImg" style="background: #fff; padding: 12px; border-radius: 8px; display: inline-block">
            <img :src="qrImg" alt="网易云登录二维码" style="width: 200px; height: 200px; display: block" />
          </div>
          <el-text v-if="qrStatus" type="info">{{ qrStatus }}</el-text>
          <el-text v-else type="info">与官方文档一致：先取 key，再生成图，轮询 check（803 成功）。</el-text>
        </el-space>
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
