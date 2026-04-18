<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import * as api from '../../api/friends'
import type { FriendLinkAdmin } from '../../api/friends'

const { t } = useI18n()
const rows = ref<FriendLinkAdmin[]>([])
const loading = ref(false)
const dialog = ref(false)
const form = reactive({
  id: 0 as number | null,
  title: '',
  url: '',
  description: '',
  avatarUrl: '',
  sortOrder: 0,
})

async function load() {
  loading.value = true
  try {
    rows.value = await api.adminListFriends()
  } finally {
    loading.value = false
  }
}

function openCreate() {
  form.id = null
  form.title = ''
  form.url = ''
  form.description = ''
  form.avatarUrl = ''
  form.sortOrder = 0
  dialog.value = true
}

function openEdit(row: FriendLinkAdmin) {
  form.id = row.id
  form.title = row.title
  form.url = row.url
  form.description = row.description ?? ''
  form.avatarUrl = row.avatarUrl ?? ''
  form.sortOrder = row.sortOrder ?? 0
  dialog.value = true
}

async function save() {
  const body = {
    title: form.title,
    url: form.url,
    description: form.description,
    avatarUrl: form.avatarUrl,
    sortOrder: form.sortOrder,
  }
  try {
    if (form.id == null) await api.adminCreateFriend(body)
    else await api.adminUpdateFriend(form.id, body)
    ElMessage.success('OK')
    dialog.value = false
    await load()
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : String(e))
  }
}

async function remove(row: FriendLinkAdmin) {
  if (!confirm('Delete?')) return
  try {
    await api.adminDeleteFriend(row.id)
    await load()
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : String(e))
  }
}

onMounted(() => load())
</script>

<template>
  <div class="admin-friends">
    <div class="head">
      <h1 class="page-title">{{ t('adminFriends.title') }}</h1>
      <el-button type="primary" @click="openCreate">{{ t('adminFriends.add') }}</el-button>
    </div>
    <el-table v-loading="loading" :data="rows" stripe class="glass-table">
      <el-table-column prop="title" :label="t('adminFriends.name')" />
      <el-table-column prop="url" label="URL" min-width="200" show-overflow-tooltip />
      <el-table-column prop="sortOrder" :label="t('adminFriends.sort')" width="90" />
      <el-table-column width="160">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">{{ t('adminFriends.save') }}</el-button>
          <el-button size="small" type="danger" @click="remove(row)">{{ t('adminFriends.delete') }}</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialog" :title="t('adminFriends.title')" width="520px">
      <el-form label-position="top">
        <el-form-item :label="t('adminFriends.name')">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item :label="t('adminFriends.url')">
          <el-input v-model="form.url" />
        </el-form-item>
        <el-form-item :label="t('adminFriends.desc')">
          <el-input v-model="form.description" type="textarea" rows="3" />
        </el-form-item>
        <el-form-item :label="t('adminFriends.avatar')">
          <el-input v-model="form.avatarUrl" />
        </el-form-item>
        <el-form-item :label="t('adminFriends.sort')">
          <el-input-number v-model="form.sortOrder" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">Cancel</el-button>
        <el-button type="primary" @click="save">{{ t('adminFriends.save') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.admin-friends {
  padding: 8px 0;
}
.head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}
.glass-table {
  border-radius: 12px;
  overflow: hidden;
}
</style>
