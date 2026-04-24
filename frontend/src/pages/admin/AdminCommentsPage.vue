<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  adminDeleteComment,
  adminPageComments,
  adminUpdateComment,
  type AdminCommentListItem,
} from '../../api/blog'

const loading = ref(false)
const keyword = ref('')
const page = ref(0)
const size = ref(10)
const total = ref(0)
const items = ref<AdminCommentListItem[]>([])
const saving = ref(false)

const editDialog = reactive({
  visible: false,
  id: 0,
  author: '',
  content: '',
})

async function load() {
  loading.value = true
  try {
    const res = await adminPageComments({
      keyword: keyword.value || undefined,
      page: page.value,
      size: size.value,
    })
    items.value = res.items
    total.value = res.total
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '评论加载失败')
  } finally {
    loading.value = false
  }
}

function onEdit(row: AdminCommentListItem) {
  editDialog.id = row.id
  editDialog.author = row.author
  editDialog.content = row.content
  editDialog.visible = true
}

async function onSaveEdit() {
  if (!editDialog.author.trim() || !editDialog.content.trim()) {
    ElMessage.warning('昵称和评论内容不能为空')
    return
  }
  saving.value = true
  try {
    await adminUpdateComment(editDialog.id, {
      author: editDialog.author.trim(),
      content: editDialog.content.trim(),
    })
    ElMessage.success('评论已更新')
    editDialog.visible = false
    await load()
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '保存失败')
  } finally {
    saving.value = false
  }
}

async function onDelete(row: AdminCommentListItem) {
  await ElMessageBox.confirm('确认删除这条评论？删除后不可恢复。', '提示', { type: 'warning' })
  await adminDeleteComment(row.id)
  ElMessage.success('已删除')
  await load()
}

function onPageChange(p: number) {
  page.value = p - 1
  void load()
}

onMounted(load)
</script>

<template>
  <div class="admin-page">
    <el-card>
      <template #header>
        <div class="head">
          <div class="head__title">评论管理</div>
          <div class="head__actions">
            <el-input
              v-model="keyword"
              clearable
              placeholder="按作者 / 内容 / 文章标题搜索"
              style="width: 280px"
              @keyup.enter="
                () => {
                  page = 0
                  load()
                }
              "
            />
            <el-button
              @click="
                () => {
                  page = 0
                  load()
                }
              "
            >
              查询
            </el-button>
          </div>
        </div>
      </template>

      <el-empty v-if="!loading && items.length === 0" description="暂无评论">
        <p class="empty-tip">可在此查看博客评论并修正文案，保持评论区内容质量。</p>
      </el-empty>

      <el-table v-else :data="items" v-loading="loading">
        <el-table-column prop="id" label="ID" width="90" />
        <el-table-column prop="articleTitle" label="文章" min-width="180" />
        <el-table-column prop="author" label="作者" width="140" />
        <el-table-column prop="content" label="内容" min-width="260" show-overflow-tooltip />
        <el-table-column label="时间" width="180">
          <template #default="{ row }">
            {{ new Date(row.createTime).toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="onEdit(row)">编辑</el-button>
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
          @current-change="onPageChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="editDialog.visible" title="编辑评论" width="640px" destroy-on-close>
      <el-form label-position="top">
        <el-form-item label="作者昵称">
          <el-input v-model="editDialog.author" maxlength="50" />
        </el-form-item>
        <el-form-item label="评论内容">
          <el-input v-model="editDialog.content" type="textarea" :rows="5" maxlength="1000" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onSaveEdit">保存</el-button>
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
  gap: 12px;
}
.head__title {
  font-weight: 900;
}
.head__actions {
  display: flex;
  align-items: center;
  gap: 10px;
}
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 14px;
}
.empty-tip {
  margin-top: 8px;
  max-width: 420px;
  color: rgba(226, 232, 240, 0.72);
  font-size: 0.88rem;
  line-height: 1.55;
}
</style>
