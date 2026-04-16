<script setup lang="ts">
/**
 * 留言墙管理：审核、拒绝、站长回复、删除。
 */
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  adminApproveWall,
  adminDeleteWall,
  adminPageWallMessages,
  adminRejectWall,
  adminReplyWall,
  type WallMessageAdmin,
} from '../../api/wall'

const loading = ref(false)
const page = ref(0)
const size = ref(10)
const total = ref(0)
const status = ref<string | undefined>(undefined)
const items = ref<WallMessageAdmin[]>([])

const replyDialog = reactive({ visible: false, id: 0, text: '' })

async function load() {
  loading.value = true
  try {
    const res = await adminPageWallMessages({
      status: status.value,
      page: page.value,
      size: size.value,
    })
    items.value = res.items
    total.value = res.total
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '加载失败')
  } finally {
    loading.value = false
  }
}

async function onApprove(row: WallMessageAdmin) {
  try {
    await adminApproveWall(row.id)
    ElMessage.success('已通过')
    await load()
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '操作失败')
  }
}

async function onReject(row: WallMessageAdmin) {
  try {
    await ElMessageBox.confirm('确认拒绝该留言？前台将不会展示。', '提示', { type: 'warning' })
    await adminRejectWall(row.id)
    ElMessage.success('已拒绝')
    await load()
  } catch (e: unknown) {
    if (e !== 'cancel') ElMessage.error(e instanceof Error ? e.message : '操作失败')
  }
}

function openReply(row: WallMessageAdmin) {
  replyDialog.id = row.id
  replyDialog.text = row.adminReply || ''
  replyDialog.visible = true
}

async function confirmReply() {
  if (!replyDialog.text.trim()) {
    ElMessage.warning('请填写回复内容')
    return
  }
  try {
    await adminReplyWall(replyDialog.id, replyDialog.text.trim())
    ElMessage.success('已保存回复')
    replyDialog.visible = false
    await load()
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '保存失败')
  }
}

async function onDelete(row: WallMessageAdmin) {
  try {
    await ElMessageBox.confirm('确认永久删除？', '危险操作', { type: 'warning' })
    await adminDeleteWall(row.id)
    ElMessage.success('已删除')
    await load()
  } catch (e: unknown) {
    if (e !== 'cancel') ElMessage.error(e instanceof Error ? e.message : '删除失败')
  }
}

onMounted(load)
</script>

<template>
  <div class="admin-page">
    <el-card>
      <template #header>
        <div class="head">
          <div class="head__title">留言审核</div>
          <div class="filters">
            <el-select
              v-model="status"
              clearable
              placeholder="审核状态"
              style="width: 160px"
              @change="
                () => {
                  page = 0
                  load()
                }
              "
            >
              <el-option label="待审核" value="PENDING" />
              <el-option label="已通过" value="APPROVED" />
              <el-option label="已拒绝" value="REJECTED" />
            </el-select>
            <el-button @click="load">刷新</el-button>
          </div>
        </div>
      </template>

      <template v-if="!loading && items.length === 0">
        <el-empty description="暂无留言数据" />
        <p class="empty-hint">
          访客在留言墙提交的内容会出现在「待审核」中，请先通过审核再公开展示。
        </p>
      </template>

      <el-table v-else :data="items" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="nickname" label="昵称" width="120" />
        <el-table-column prop="content" label="内容" min-width="220" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'PENDING'" type="warning" effect="plain">待审核</el-tag>
            <el-tag v-else-if="row.status === 'APPROVED'" type="success" effect="plain">已通过</el-tag>
            <el-tag v-else type="info" effect="plain">已拒绝</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="回复" width="100">
          <template #default="{ row }">
            <span class="muted">{{ row.adminReply ? '有' : '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="时间" width="170">
          <template #default="{ row }">
            <span class="muted">{{ new Date(row.createdAt).toLocaleString() }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'PENDING'" size="small" type="success" plain @click="onApprove(row)">
              通过
            </el-button>
            <el-button v-if="row.status === 'PENDING'" size="small" type="warning" plain @click="onReject(row)">
              拒绝
            </el-button>
            <el-button size="small" @click="openReply(row)">回复</el-button>
            <el-button size="small" type="danger" plain @click="onDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="total > 0" class="pager">
        <el-pagination
          background
          layout="prev, pager, next, total"
          :current-page="page + 1"
          :page-size="size"
          :total="total"
          @current-change="(p: number) => { page = p - 1; load() }"
        />
      </div>
    </el-card>

    <el-dialog v-model="replyDialog.visible" title="站长回复" width="520px" destroy-on-close>
      <el-input v-model="replyDialog.text" type="textarea" :rows="5" maxlength="1000" show-word-limit />
      <template #footer>
        <el-button @click="replyDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="confirmReply">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.admin-page {
  max-width: 1200px;
  margin: 0 auto;
}
.head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}
.head__title {
  font-weight: 900;
}
.filters {
  display: flex;
  gap: 8px;
  align-items: center;
}
.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
.muted {
  color: rgba(226, 232, 240, 0.65);
}
.empty-hint {
  margin: 0;
  max-width: 420px;
  margin-left: auto;
  margin-right: auto;
  color: rgba(226, 232, 240, 0.75);
  line-height: 1.6;
  font-size: 0.9rem;
}
</style>
