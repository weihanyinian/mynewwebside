<script setup lang="ts">
/**
 * 通用 iframe 游戏页面
 * 根据 route.params.gameId 从注册表查找游戏信息，渲染 MoyuGameEmbed
 * 替代之前每个 iframe 游戏各自一个 Vue 页面的模式
 */
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import MoyuGameEmbed from '../../components/moyu/MoyuGameEmbed.vue'
import { getMoyuGame } from '../../config/moyuGames'

const route = useRoute()
const gameId = computed(() => String(route.params.gameId))
const game = computed(() => getMoyuGame(gameId.value))
</script>

<template>
  <MoyuGameEmbed v-if="game" :game-id="game.id" :title="game.name" />
  <div v-else class="not-found">
    <p>游戏不存在</p>
    <router-link to="/moyu">返回摸鱼中心</router-link>
  </div>
</template>

<style scoped>
.not-found {
  text-align: center;
  padding: 80px 16px;
  color: var(--text-color);
}
.not-found p {
  font-size: 1.2rem;
  font-weight: 700;
  margin-bottom: 16px;
}
</style>
