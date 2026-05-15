<script setup>
import { computed, reactive, ref } from "vue"
import { ElMessage, ElMessageBox } from "element-plus"
import MoyuBackToHubButton from "../components/moyu/MoyuBackToHubButton.vue"
import ControlPanel from "../components/ControlPanel.vue"
import PlayArea from "../components/PlayArea.vue"
import PlayerHand from "../components/PlayerHand.vue"
import {
  aiBidDecision,
  analyzePlay,
  canBeatPlay,
  chooseOpeningPlay,
  createDeck,
  evaluateLandlordScore,
  findHint,
  shuffle,
  sortCards,
} from "../utils/cardUtils"
import {
  playCardSound,
  playBidSound,
  playPassSound,
  playWinSound,
  playDealSound,
  playHintSound,
} from "../utils/ddzSound"

const stage = ref("bidding") // bidding | playing | ended
const players = reactive([
  { id: 0, name: "你", isAI: false, hand: [], isLandlord: false },
  { id: 1, name: "农民1", isAI: true, hand: [], isLandlord: false },
  { id: 2, name: "农民2", isAI: true, hand: [], isLandlord: false },
])

const bottomCards = ref([])
const revealBottom = ref(false)
const currentTurn = ref(0)
const bidStartIndex = ref(0)
const bidTurnCount = ref(0)
const bidRecords = reactive([null, null, null]) // call | pass
const selectedIds = ref([])
const tableRows = reactive([
  { playerIndex: 0, name: "你", cards: [], isPass: false },
  { playerIndex: 1, name: "农民1", cards: [], isPass: false },
  { playerIndex: 2, name: "农民2", cards: [], isPass: false },
])
const trickOwner = ref(null) // 当前有效牌的拥有者
const lastPlay = ref(null)
const passCount = ref(0)
const winnerSide = ref("")
const aiThinking = ref(false)

const currentPlayer = computed(() => players[currentTurn.value])
const selfPlayer = computed(() => players[0])

const canPlay = computed(() => {
  if (stage.value !== "playing") return false
  if (currentTurn.value !== 0) return false
  return selectedIds.value.length > 0
})

const canPass = computed(() => {
  if (stage.value !== "playing") return false
  if (currentTurn.value !== 0) return false
  if (!lastPlay.value) return false
  return trickOwner.value !== 0
})

const canHint = computed(() => stage.value === "playing" && currentTurn.value === 0)
const canBid = computed(() => stage.value === "bidding" && currentTurn.value === 0)
const canSkipBid = computed(() => stage.value === "bidding" && currentTurn.value === 0)

function resetTableRows() {
  tableRows.forEach((row) => {
    row.cards = []
    row.isPass = false
  })
}

function clearRoundState() {
  trickOwner.value = null
  lastPlay.value = null
  passCount.value = 0
  resetTableRows()
}

function startNewGame() {
  stage.value = "bidding"
  revealBottom.value = false
  selectedIds.value = []
  bidTurnCount.value = 0
  winnerSide.value = ""
  bidRecords[0] = null
  bidRecords[1] = null
  bidRecords[2] = null
  clearRoundState()

  const deck = shuffle(createDeck())
  players.forEach((p) => {
    p.hand = sortCards(deck.splice(0, 17))
    p.isLandlord = false
  })
  bottomCards.value = sortCards(deck)

  bidStartIndex.value = Math.floor(Math.random() * 3)
  currentTurn.value = bidStartIndex.value
  playDealSound()
  runAITurnIfNeeded()
}

function toggleSelectCard(card) {
  if (stage.value !== "playing") return
  if (currentTurn.value !== 0) return
  const i = selectedIds.value.indexOf(card.id)
  if (i >= 0) selectedIds.value.splice(i, 1)
  else selectedIds.value.push(card.id)
}

function getSelectedCards() {
  const idSet = new Set(selectedIds.value)
  return selfPlayer.value.hand.filter((c) => idSet.has(c.id))
}

function applyPlay(playerIndex, cards, playInfo) {
  const player = players[playerIndex]
  const idSet = new Set(cards.map((c) => c.id))
  player.hand = sortCards(player.hand.filter((c) => !idSet.has(c.id)))
  tableRows[playerIndex].cards = sortCards(cards)
  tableRows[playerIndex].isPass = false
  lastPlay.value = playInfo
  trickOwner.value = playerIndex
  passCount.value = 0
  playCardSound(playInfo.type, cards.length)
}

function applyPass(playerIndex) {
  tableRows[playerIndex].cards = []
  tableRows[playerIndex].isPass = true
  passCount.value += 1
  if (playerIndex === 0) playPassSound()
}

function nextTurn() {
  currentTurn.value = (currentTurn.value + 1) % 3
}

function checkWin(playerIndex) {
  if (players[playerIndex].hand.length > 0) return false
  stage.value = "ended"
  const landlord = players.find((p) => p.isLandlord)
  const landlordWin = landlord && landlord.id === playerIndex
  winnerSide.value = landlordWin ? "地主胜利" : "农民胜利"
  playWinSound(landlordWin)
  ElMessageBox.alert(winnerSide.value, "对局结束", {
    confirmButtonText: "再来一局",
    callback: () => startNewGame(),
  })
  return true
}

function finishCurrentTrickIfNeeded() {
  if (trickOwner.value == null) return
  if (passCount.value < 2) return
  currentTurn.value = trickOwner.value
  trickOwner.value = null
  lastPlay.value = null
  passCount.value = 0
  tableRows.forEach((row) => {
    row.cards = []
    row.isPass = false
  })
}

function onPlayerPlay() {
  const cards = getSelectedCards()
  const play = analyzePlay(cards)
  if (!play) {
    ElMessage.warning("牌型不合法")
    return
  }
  if (!canBeatPlay(play, lastPlay.value && trickOwner.value !== 0 ? lastPlay.value : null)) {
    ElMessage.warning("需要大过上家")
    return
  }
  applyPlay(0, cards, play)
  selectedIds.value = []
  if (checkWin(0)) return
  nextTurn()
  runAITurnIfNeeded()
}

function onPlayerPass() {
  if (!canPass.value) return
  applyPass(0)
  nextTurn()
  finishCurrentTrickIfNeeded()
  runAITurnIfNeeded()
}

function onHint() {
  const hintCards = findHint(selfPlayer.value.hand, trickOwner.value !== 0 ? lastPlay.value : null)
  if (!hintCards.length) {
    ElMessage.info("没有可压过的牌")
    return
  }
  playHintSound()
  selectedIds.value = hintCards.map((c) => c.id)
}

function lockLandlord(index) {
  players.forEach((p) => (p.isLandlord = p.id === index))
  players[index].hand = sortCards(players[index].hand.concat(bottomCards.value))
  revealBottom.value = true
  stage.value = "playing"
  currentTurn.value = index
  selectedIds.value = []
  clearRoundState()
}

function finalizeBidding() {
  const callers = bidRecords
    .map((v, i) => ({ i, v }))
    .filter((x) => x.v === "call")
    .map((x) => x.i)
  if (callers.length > 0) {
    lockLandlord(callers[0])
    runAITurnIfNeeded()
    return
  }
  // 三家都不叫时，默认给手牌评估最高者做地主，避免死局。
  let best = 0
  let bestScore = -1
  players.forEach((p) => {
    const score = evaluateLandlordScore(p.hand)
    if (score > bestScore) {
      bestScore = score
      best = p.id
    }
  })
  lockLandlord(best)
  runAITurnIfNeeded()
}

function bidAction(call) {
  if (stage.value !== "bidding") return
  bidRecords[currentTurn.value] = call ? "call" : "pass"
  bidTurnCount.value += 1
  playBidSound(call)
  if (call) {
    lockLandlord(currentTurn.value)
    return
  }
  if (bidTurnCount.value >= 3) {
    finalizeBidding()
    return
  }
  nextTurn()
  runAITurnIfNeeded()
}

function sleep(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms))
}

async function aiTakeBiddingTurn(aiIndex) {
  aiThinking.value = true
  await sleep(900 + Math.floor(Math.random() * 700))
  const call = aiBidDecision(players[aiIndex].hand)
  aiThinking.value = false
  bidAction(call)
}

async function aiTakePlayTurn(aiIndex) {
  aiThinking.value = true
  await sleep(1000 + Math.floor(Math.random() * 1000))
  const player = players[aiIndex]
  const target = trickOwner.value !== aiIndex ? lastPlay.value : null
  const cards = target ? findHint(player.hand, target) : chooseOpeningPlay(player.hand)
  if (!cards || cards.length === 0) {
    applyPass(aiIndex)
    nextTurn()
    finishCurrentTrickIfNeeded()
    aiThinking.value = false
    runAITurnIfNeeded()
    return
  }
  const play = analyzePlay(cards)
  if (!play || !canBeatPlay(play, target)) {
    applyPass(aiIndex)
    nextTurn()
    finishCurrentTrickIfNeeded()
    aiThinking.value = false
    runAITurnIfNeeded()
    return
  }
  applyPlay(aiIndex, cards, play)
  if (checkWin(aiIndex)) {
    aiThinking.value = false
    return
  }
  nextTurn()
  aiThinking.value = false
  runAITurnIfNeeded()
}

async function runAITurnIfNeeded() {
  if (stage.value === "ended") return
  if (!currentPlayer.value.isAI) return
  if (stage.value === "bidding") {
    await aiTakeBiddingTurn(currentTurn.value)
  } else if (stage.value === "playing") {
    await aiTakePlayTurn(currentTurn.value)
  }
}

startNewGame()
</script>

<template>
  <div class="min-h-screen bg-[radial-gradient(circle_at_top,#0f5132,#0b3d28_38%,#07251a)] p-4 text-white">
    <div class="mx-auto max-w-[1500px]">
      <div class="mb-3 flex items-center justify-between">
        <MoyuBackToHubButton />
        <h1 class="text-xl font-bold tracking-wide">摸鱼小游戏 · 斗地主</h1>
      </div>

      <div class="grid grid-cols-1 gap-3 xl:grid-cols-[280px_1fr_280px]">
        <PlayerHand
          name="农民1"
          :cards="players[1].hand"
          :is-landlord="players[1].isLandlord"
          :active="currentTurn === 1"
        />

        <div class="space-y-3">
          <PlayArea
            :bottom-cards="bottomCards"
            :reveal-bottom="revealBottom"
            :table-plays="tableRows"
            :trick-owner="trickOwner"
          />

          <div class="rounded-xl border border-white/20 bg-black/25 px-3 py-2 text-sm">
            <p>阶段：{{ stage === "bidding" ? "叫地主" : stage === "playing" ? "出牌中" : "已结束" }}</p>
            <p>当前：{{ currentPlayer.name }} <span v-if="aiThinking">（思考中...）</span></p>
            <p v-if="stage === 'ended'" class="font-semibold text-amber-300">{{ winnerSide }}</p>
          </div>

          <ControlPanel
            :stage="stage"
            :can-play="canPlay"
            :can-pass="canPass"
            :can-hint="canHint"
            :can-bid="canBid"
            :can-skip-bid="canSkipBid"
            :thinking="aiThinking"
            @play="onPlayerPlay"
            @pass="onPlayerPass"
            @hint="onHint"
            @restart="startNewGame"
            @bid="() => bidAction(true)"
            @skip-bid="() => bidAction(false)"
          />
        </div>

        <PlayerHand
          name="农民2"
          :cards="players[2].hand"
          :is-landlord="players[2].isLandlord"
          :active="currentTurn === 2"
        />
      </div>

      <div class="mt-3">
        <PlayerHand
          name="你"
          :cards="players[0].hand"
          :is-self="true"
          :selected-ids="selectedIds"
          :is-landlord="players[0].isLandlord"
          :active="currentTurn === 0"
          @toggle-card="toggleSelectCard"
        />
      </div>
    </div>
  </div>
</template>

